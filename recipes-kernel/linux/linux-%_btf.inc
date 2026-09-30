FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'btf', 'file://btf.scc', '', d)}"

# pahole must be present during do_kernel_configme, not just do_compile:
# init/Kconfig evaluates PAHOLE_VERSION via
# $(shell,scripts/pahole-version.sh $(PAHOLE)) while the config is being
# assembled. If pahole is not there yet, PAHOLE_VERSION is 0, the
# "depends on PAHOLE_VERSION >= 116" guard on DEBUG_INFO_BTF is unsatisfied,
# and BTF is dropped silently -- before do_configure ever runs. olddefconfig
# later computes PAHOLE_VERSION correctly and writes it to .config, which makes
# the resulting config look like BTF should have been enabled when it was not.
#
# DEPENDS alone only orders pahole-native ahead of do_configure/do_compile, so
# whether it is staged in time for do_kernel_configme comes down to build
# scheduling -- it would appear on one machine and not another. Wire the task
# dependency explicitly and pass an absolute path rather than relying on PATH.
DEPENDS += "pahole-native"
do_kernel_configme[depends] += "pahole-native:do_populate_sysroot"
do_kernel_configme:prepend() {
    export PAHOLE="${STAGING_BINDIR_NATIVE}/pahole"
}
EXTRA_OEMAKE += "PAHOLE=${STAGING_BINDIR_NATIVE}/pahole"
