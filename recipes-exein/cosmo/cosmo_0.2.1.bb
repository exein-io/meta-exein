inherit cargo pkgconfig

SRC_URI += "git://git@github.com/Exein-io/cosmo-cli.git;protocol=ssh;nobranch=1;branch=main"
SRCREV = "d5578d2b015f4ca44e70707c5b3311282a3110f6"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""
PV:append = ".AUTOINC+d5578d2b01"

LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SUMMARY = "Easy Cosmo pipeline helper"
HOMEPAGE = "https://cosmo.exein.io"
LICENSE = "Apache-2.0"

DEPENDS = "openssl-native"

do_install() {
    cp ${CARGO_RELEASE_DIR}/cosmo ${TOPDIR}/../scripts/cosmo
}

do_compile[network] = "1"

INSANE_SKIP:${PN} += "already-stripped"
BBCLASSEXTEND = "native"
