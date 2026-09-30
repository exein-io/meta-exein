#!/bin/sh
# t01 — file-system-monitor: FileCreated below /root
# Rule: rules/credential_access/create_file_in_root.yaml

PTEST_DIR="$(cd "$(dirname "$0")/.." && pwd)"
. "$PTEST_DIR/ptest-lib.sh"

if [ -z "$PTEST_DAEMON_READY" ]; then
    trap 'ptest_cleanup' EXIT
    daemon_wait || exit 0
fi

# A synthetic path, so an unrelated system write can never satisfy this.
# It is also not on the rule's allow-list of well-known /root dotfiles.
CANARY=/root/.ptest_canary

trigger() {
    rm -f "$CANARY"
    : > "$CANARY"
}

mark=$(log_mark)
trigger
check_rule "$mark" "Create files below /root" "$DETECT_TIMEOUT" trigger

rm -f "$CANARY"
