require ${@bb.utils.contains('DISTRO_FEATURES', 'btf', '${BPN}_btf.inc', '', d)}

DEPENDS += "pahole-native"

do_kernel_configme[depends] += "pahole-native:do_populate_sysroot"
do_kernel_configme[depends] += "elfutils-native:do_populate_sysroot"

EXTRA_OEMAKE += " PAHOLE=pahole"
KCONFIG_CONFIG_COMMAND:append = " PAHOLE=pahole"