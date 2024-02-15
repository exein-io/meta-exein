FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'btf', 'file://btf.scc', '', d)}"

DEPENDS += "pahole-native"

EXTRA_OEMAKE += " PAHOLE=pahole"
KCONFIG_CONFIG_COMMAND:append = " PAHOLE=pahole"
