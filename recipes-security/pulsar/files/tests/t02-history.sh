#!/bin/sh
# t02 — file-system-monitor: shell history tampering
# Rule file: rules/defense_evasion/shell_history_deletion.yaml
#   "Shell history deletion"    — FileDeleted, filename ENDS_WITH bash_history
#   "Shell history truncation"  — FileOpened with O_TRUNC on the same names

PTEST_DIR="$(cd "$(dirname "$0")/.." && pwd)"
. "$PTEST_DIR/ptest-lib.sh"

if [ -z "$PTEST_DAEMON_READY" ]; then
    trap 'ptest_cleanup' EXIT
    daemon_wait || exit 0
fi

HIST=/root/.bash_history

trigger_truncate() {
    echo "ptest" > "$HIST"
    : > "$HIST"          # O_WRONLY|O_CREAT|O_TRUNC
}

trigger_delete() {
    echo "ptest" > "$HIST"
    rm -f "$HIST"
}

mark=$(log_mark)
trigger_truncate
check_rule "$mark" "Shell history truncation" "$DETECT_TIMEOUT" trigger_truncate

mark=$(log_mark)
trigger_delete
check_rule "$mark" "Shell history deletion" "$DETECT_TIMEOUT" trigger_delete
