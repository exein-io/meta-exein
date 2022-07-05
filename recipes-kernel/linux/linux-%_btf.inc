FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Enable BTF support in the kernel if the feature is enabled
SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'btf', 'file://btf.scc', '', d)}"
