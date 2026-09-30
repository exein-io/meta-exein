#!/bin/sh
# ptest-lib.sh — shared helpers for the pulsar ptest suite
#
# Source this file; do not execute directly.
# Constants may be overridden by the caller before sourcing.
#
# This is the open-source pulsar recipe, which ships no systemd unit (unlike
# meta-exein-runtime's exein-pulsar-bin). The suite therefore starts pulsard
# itself and reads rule hits from its stdout rather than from the journal.
#
# Public API:
#   pass <name> / fail <name> / skip <name> <reason>
#   kernel_has_btf         — 0 when /sys/kernel/btf/vmlinux exists
#   daemon_start           — start pulsard in the background
#   daemon_stop            — stop it
#   daemon_wait            — start + wait until the eBPF hooks actually fire;
#                            emits pass/fail entries, returns 0 on success
#   log_mark               — current offset in the daemon log
#   check_rule <mark> <rule> [timeout] [retry_fn]
#   ptest_cleanup          — use as EXIT trap handler

PULSARD="${PULSARD:-/usr/bin/pulsard}"
PULSAR="${PULSAR:-/usr/bin/pulsar}"
PULSAR_CONF="${PULSAR_CONF:-/var/lib/pulsar/pulsar.ini}"
PULSAR_LOG="${PULSAR_LOG:-/tmp/pulsard-ptest.log}"
PULSAR_PIDFILE="${PULSAR_PIDFILE:-/tmp/pulsard-ptest.pid}"
# Scratch dir for binaries a test stages under a specific name. Kept out of
# /usr/bin so staging never trips the binary-directory tampering rules.
PTEST_BIN_DIR="${PTEST_BIN_DIR:-/tmp/.ptest_bin}"

START_TIMEOUT="${START_TIMEOUT:-60}"
HOOK_READY_TIMEOUT="${HOOK_READY_TIMEOUT:-180}"
DETECT_TIMEOUT="${DETECT_TIMEOUT:-60}"
POLL="${POLL:-2}"

pass() { echo "PASS: $1"; }
fail() { echo "FAIL: $1"; }
skip() { echo "SKIP: $1${2:+ — $2}"; }

# Returns 0 when the kernel exposes its in-kernel BTF blob
# (CONFIG_DEBUG_INFO_BTF=y, i.e. DISTRO_FEATURES contains "btf"). Without it
# the eBPF probes cannot load at all and every detection test is meaningless.
kernel_has_btf() { [ -e /sys/kernel/btf/vmlinux ]; }

# Current line offset in the daemon log. Pass the result to check_rule so a
# hit left over from an earlier section can never satisfy a later assertion.
log_mark() { wc -l < "$PULSAR_LOG" 2>/dev/null || echo 0; }

daemon_running() {
    [ -s "$PULSAR_PIDFILE" ] && kill -0 "$(cat "$PULSAR_PIDFILE")" 2>/dev/null
}

daemon_start() {
    daemon_running && return 0
    : > "$PULSAR_LOG"
    # pulsard runs in the foreground and writes THREAT lines to stdout.
    "$PULSARD" --config-file "$PULSAR_CONF" >> "$PULSAR_LOG" 2>&1 &
    echo $! > "$PULSAR_PIDFILE"

    local elapsed=0
    while [ "$elapsed" -lt "$START_TIMEOUT" ]; do
        # Every module logs "Starting module <name>"; rules-engine is started
        # last of the ones we assert on, so it is the useful marker.
        grep -q "Starting module rules-engine" "$PULSAR_LOG" 2>/dev/null && return 0
        daemon_running || return 1
        sleep "$POLL"
        elapsed=$((elapsed + POLL))
    done
    return 1
}

daemon_stop() {
    daemon_running || { rm -f "$PULSAR_PIDFILE"; return 0; }
    kill "$(cat "$PULSAR_PIDFILE")" 2>/dev/null
    local elapsed=0
    while [ "$elapsed" -lt 10 ] && daemon_running; do
        sleep 1
        elapsed=$((elapsed + 1))
    done
    daemon_running && kill -9 "$(cat "$PULSAR_PIDFILE")" 2>/dev/null
    rm -f "$PULSAR_PIDFILE"
}

# Internal poll engine: 0 = found, 1 = timeout. No PASS/FAIL output.
_check_rule_poll() {
    local mark="$1"
    local rule="$2"
    local timeout="${3:-$DETECT_TIMEOUT}"
    local elapsed=0
    # The THREAT line is "[<ts> THREAT <image> (<pid>)] [<module> - <rule>] <payload>".
    # The THREAT word itself is wrapped in ANSI escapes (Display uses the
    # alternate flag unconditionally), but the "<module> - <rule>]" part is
    # plain, so match on that.
    local pat="rules-engine - ${rule}]"
    while :; do
        tail -n "+$((mark + 1))" "$PULSAR_LOG" 2>/dev/null | grep -qF "$pat" && return 0
        [ "$elapsed" -ge "$timeout" ] && return 1
        sleep "$POLL"
        elapsed=$((elapsed + POLL))
    done
}

# check_rule <mark> <rule> [timeout] [retry_fn]
# $4 (optional): name of a function that re-fires the trigger. On a first
# timeout it is called once and the poll is repeated before declaring failure —
# the eBPF ringbuffer can drop an event under a burst of unrelated forks.
check_rule() {
    local mark="$1"
    local rule="$2"
    local timeout="${3:-$DETECT_TIMEOUT}"
    local retry_fn="${4:-}"
    echo "Testing: ${rule}..."
    if _check_rule_poll "$mark" "$rule" "$timeout"; then
        pass "$rule"
        return
    fi
    if [ -n "$retry_fn" ]; then
        local rmark
        rmark=$(log_mark)
        "$retry_fn"
        if _check_rule_poll "$rmark" "$rule" "$timeout"; then
            pass "$rule"
            return
        fi
    fi
    echo "DBG: check_rule timed out for '$rule'; THREAT lines since mark $mark:"
    tail -n "+$((mark + 1))" "$PULSAR_LOG" 2>/dev/null | grep "rules-engine" | tail -n 5 \
        || echo "DBG: (none)"
    echo "DBG: daemon warnings/errors:"
    grep -iE "warn|error|failed" "$PULSAR_LOG" 2>/dev/null | tail -n 10 || echo "DBG: (none)"
    fail "$rule"
}

# Strip ANSI SGR escapes. Threat lines and the `pulsar status` table are
# colour-wrapped unconditionally (the Display impl always uses the alternate
# flag), so text matching must go through this.
strip_ansi() {
    _esc=$(printf '\033')
    sed "s/${_esc}\[[0-9;]*m//g"
}

# Dump the daemon log. Probe-attach problems surface here and nowhere else, so
# print it whenever a run ends with anything other than a clean pass.
daemon_log_dump() {
    echo ""
    echo "=== pulsard log (last ${1:-60} lines) ==="
    tail -n "${1:-60}" "$PULSAR_LOG" 2>/dev/null | strip_ansi || echo "(no log)"
    echo "=== end pulsard log ==="
}

ptest_cleanup() {
    daemon_stop
    rm -f /root/.pulsar_ptest_probe /root/.ptest_canary
    rm -rf "$PTEST_BIN_DIR"
    rm -f "$PULSAR_PIDFILE"
}

# Start the daemon and wait until the file-system-monitor eBPF hooks actually
# produce events. "Starting module ..." only means the module was constructed;
# probe attach lags it by seconds to minutes on an emulated target. Poll by
# re-firing a known-good trigger ("Create files below /root") until it is seen.
daemon_wait() {
    if ! kernel_has_btf; then
        fail "daemon-running"
        echo "DBG: /sys/kernel/btf/vmlinux missing — the kernel was built"
        echo "     without CONFIG_DEBUG_INFO_BTF. Add 'btf' to DISTRO_FEATURES."
        return 1
    fi

    if ! daemon_start; then
        fail "daemon-running"
        echo "DBG: pulsard did not start; last 20 log lines:"
        tail -n 20 "$PULSAR_LOG" 2>/dev/null || echo "DBG: (no log)"
        return 1
    fi
    pass "daemon-running"

    local mark
    mark=$(log_mark)
    local i=0
    while [ $((i * POLL)) -lt "$HOOK_READY_TIMEOUT" ]; do
        # Re-fire the trigger every 10 s so a hit is possible the moment the
        # hooks go live.
        if [ $((i % 5)) -eq 0 ]; then
            rm -f /root/.pulsar_ptest_probe
            : > /root/.pulsar_ptest_probe 2>/dev/null
        fi
        sleep "$POLL"
        if tail -n "+$((mark + 1))" "$PULSAR_LOG" 2>/dev/null \
             | grep -qF "rules-engine - Create files below /root]"; then
            rm -f /root/.pulsar_ptest_probe
            pass "daemon-hooks-ready"
            return 0
        fi
        i=$((i + 1))
    done

    rm -f /root/.pulsar_ptest_probe
    echo "DBG: eBPF hooks not producing events after ${HOOK_READY_TIMEOUT}s"
    tail -n 20 "$PULSAR_LOG" 2>/dev/null
    fail "daemon-hooks-ready"
    return 1
}

[ "${PTEST_VERBOSE:-0}" = "1" ] && set -x
