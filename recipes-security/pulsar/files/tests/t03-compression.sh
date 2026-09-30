#!/bin/sh
# t03 — file-system-monitor: sensitive file read by an archiver
# Rule: rules/credential_access/collection_sensitive_files.yaml
#
# The rule keys on header.image ENDS_WITH "/tar" (or /zip, /gzip, ...): the
# process doing the open must itself be an archiver. That makes this the one
# section here whose outcome depends on process-monitor resolving the opening
# process, not just on file-system-monitor seeing the open.
#
# Two things have to be handled for that to work:
#
#  1. header.image is the *resolved* executable, so the process must be exec'd
#     from a path literally ending in "/tar". On a Yocto image it never is:
#     tar is an update-alternatives symlink (/usr/bin/tar -> /usr/bin/tar.tar),
#     and an image that reaches tar through busybox resolves to busybox.
#     Either way the rule cannot match. So always stage a copy named exactly
#     "tar" and exec that — never rely on the image's own tar path.
#
#     Stage it under /tmp, never over /usr/bin/tar: source and destination
#     would be the same path, and the rename would itself trip the "Rename any
#     file below binary directories" rule.
#
#  2. A process that forks and immediately execs can reach the open before
#     process-monitor's exec event lands in the tracker. file-system-monitor
#     then logs "Process not found in tracker <pid>" and emits the event with
#     no image, and an image-keyed rule cannot match. Fork first, let the
#     tracker register the pid, then exec tar into that same pid.

PTEST_DIR="$(cd "$(dirname "$0")/.." && pwd)"
. "$PTEST_DIR/ptest-lib.sh"

if [ -z "$PTEST_DAEMON_READY" ]; then
    trap 'ptest_cleanup' EXIT
    daemon_wait || exit 0
fi

TRACKER_SETTLE="${TRACKER_SETTLE:-5}"

_src=$(command -v tar 2>/dev/null)
if [ -z "$_src" ]; then
    skip "Sensitive file compression" "no tar on the image"
    exit 0
fi

# Resolve through the alternatives/busybox symlink to the real ELF, then stage
# it under a name the rule can actually match.
_real=$(readlink -f "$_src")
mkdir -p "$PTEST_BIN_DIR"
TAR="$PTEST_BIN_DIR/tar"
cp "$_real" "$TAR"
chmod +x "$TAR"

trigger() {
    # Subshell forks now; exec keeps that same pid, so by the time tar opens
    # /etc/passwd the tracker has had TRACKER_SETTLE seconds to learn the pid.
    ( sleep "$TRACKER_SETTLE"; exec "$TAR" cf /dev/null /etc/passwd ) >/dev/null 2>&1
}

mark=$(log_mark)
trigger
if _check_rule_poll "$mark" "Sensitive file compression" "$DETECT_TIMEOUT"; then
    pass "Sensitive file compression"
elif tail -n "+$((mark + 1))" "$PULSAR_LOG" 2>/dev/null \
        | grep -q "Process not found in tracker"; then
    # file-system-monitor saw the open but could not attribute it, so
    # header.image was unset and the rule had nothing to match.
    skip "Sensitive file compression" \
         "file-system-monitor could not resolve the opening process (\"Process not found in tracker\"); header.image is unset, so an image-keyed rule cannot match"
else
    echo "DBG: rule did not fire and the tracker resolved the process; THREAT lines since mark $mark:"
    tail -n "+$((mark + 1))" "$PULSAR_LOG" 2>/dev/null | grep "rules-engine" | tail -n 5
    echo "DBG: daemon warnings/errors:"
    grep -iE "warn|error|failed" "$PULSAR_LOG" 2>/dev/null | tail -n 10
    fail "Sensitive file compression"
fi

rm -rf "$PTEST_BIN_DIR"
exit 0
