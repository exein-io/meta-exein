#!/bin/sh
# t00 — the 0.10.x binary split.
#
# Up to 0.9.0 the recipe installed a single pulsar-exec binary plus the
# scripts/pulsar and scripts/pulsard shell wrappers. 0.10.0 replaced all three
# with two real binaries (upstream #369). Assert the new layout is what got
# packaged, and that the CLI can talk to the running daemon.

PTEST_DIR="$(cd "$(dirname "$0")/.." && pwd)"
. "$PTEST_DIR/ptest-lib.sh"

if [ -z "$PTEST_DAEMON_READY" ]; then
    trap 'ptest_cleanup' EXIT
    daemon_wait || exit 0
fi

for _b in "$PULSARD" "$PULSAR"; do
    if [ -x "$_b" ]; then
        pass "installed $_b"
    else
        fail "installed $_b"
    fi
done

# The old single binary must be gone — if it is still here, do_install is
# packaging a stale artifact.
if [ -e /usr/bin/pulsar-exec ]; then
    fail "pulsar-exec removed"
else
    pass "pulsar-exec removed"
fi

# --version reports the crate version plus the git sha and build profile
# (upstream #369). The expected version comes from the recipe's PV via
# do_install_ptest -- never hardcode it here, or every version bump fails the
# suite for the wrong reason.
_expected=$(cat "$PTEST_DIR/expected-version" 2>/dev/null)
if [ -z "$_expected" ]; then
    fail "expected-version installed"
else
    pass "expected-version installed ($_expected)"
fi

for _b in "$PULSARD" "$PULSAR"; do
    _v=$("$_b" --version 2>&1)
    _n=$(basename "$_b")
    case "$_v" in
        *"$_expected"*) pass "$_n --version ($_v)" ;;
        *)              fail "$_n --version"
                        echo "DBG: expected '$_expected', got '$_v'" ;;
    esac
done

# Rules ship to the rules-engine default path (/var/lib/pulsar/rules) and are
# split per MITRE tactic since 0.10.0 (upstream #362).
_rules=$(find /var/lib/pulsar/rules -name '*.yaml' 2>/dev/null | wc -l)
if [ "$_rules" -gt 0 ]; then
    pass "rules installed ($_rules yaml files)"
else
    fail "rules installed"
fi

# The CLI reaching the daemon over the engine-api socket exercises both halves
# of the split at once.
_status=$("$PULSAR" status 2>&1)
case "$_status" in
    *rules-engine*) pass "pulsar status" ;;
    *)              fail "pulsar status"; echo "DBG: got '$_status'" ;;
esac

# Every module must be Running, and Running *without warnings*. ModuleStatus is
# Running(Vec<String>) and renders as Running(["..."]) when a module came up
# degraded — a probe that failed to attach shows up here and nowhere else, so a
# bare "is the module listed" check would sail straight past it.
echo "DBG: pulsar status ---"
printf '%s\n' "$_status"
echo "DBG: ---"

for _m in process-monitor file-system-monitor network-monitor rules-engine threat-logger; do
    _line=$(printf '%s\n' "$_status" | strip_ansi | grep -F "$_m")
    if [ -z "$_line" ]; then
        fail "module $_m listed"
        continue
    fi
    case "$_line" in
        *'Running(['*) fail "module $_m healthy"
                       echo "DBG: degraded: $_line" ;;
        *Running*)     pass "module $_m healthy" ;;
        *)             fail "module $_m healthy"
                       echo "DBG: $_line" ;;
    esac
done
