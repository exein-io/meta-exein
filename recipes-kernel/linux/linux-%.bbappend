require ${@bb.utils.contains('DISTRO_FEATURES', 'btf', '${BPN}_btf.inc', '', d)}

HOSTTOOLS += "pahole"

EXTRA_OEMAKE += " PAHOLE=pahole"
KCONFIG_CONFIG_COMMAND:append = " PAHOLE=pahole"
