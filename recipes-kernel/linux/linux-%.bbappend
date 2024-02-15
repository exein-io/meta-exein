# Enable BTF support in the kernel if the feature is enabled
require ${@bb.utils.contains('DISTRO_FEATURES', 'btf', '${BPN}_btf.inc', '', d)}
