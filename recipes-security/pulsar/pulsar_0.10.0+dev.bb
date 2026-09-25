inherit cargo cargo-update-recipe-crates pkgconfig

SUMMARY = "pulsar"
HOMEPAGE = "https://pulsar.sh"
LICENSE = "Apache-2.0"
SRC_URI += "git://git@github.com/Exein-io/pulsar.git;protocol=ssh;nobranch=1;branch=main"
LIC_FILES_CHKSUM = "file://LICENSES/LICENSE-APACHE-2.0;md5=a0b5614acd31d1f66c2b9fe2c035f5dd"
SRCREV = "25f141bc2504bb58d3cf35a05ccd8057504602e5"

PV:append = ".AUTOINC+25f141bc25"

# Already stripped when built in release
INSANE_SKIP:${PN} += "already-stripped"
# FIXME: Build paths are currently embedded
INSANE_SKIP:${PN} += "buildpaths"

DEPENDS = "openssl zlib elfutils"

do_install () {
    install -d ${D}${bindir}
    install -d ${D}/var/lib/pulsar
    install -d ${D}/var/lib/pulsar/rules

    # Init empty configuration
    install -m 644 /dev/null ${D}/var/lib/pulsar
 
    # Copy Pulsar empty configuration
    install -m 644 ${S}/.github/docker/pulsar.ini ${D}/var/lib/pulsar/pulsar.ini

    # Copy rules
    cp -R ${S}/rules/* ${D}/var/lib/pulsar/rules/

    # Install pulsar executables. Since 0.10.0 the single pulsar-exec binary is
    # split into the pulsard daemon and the pulsar CLI, replacing the former
    # scripts/pulsar and scripts/pulsard wrappers.
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/pulsard ${D}${bindir}/pulsard
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/pulsar ${D}${bindir}/pulsar
}

require ${BPN}-crates.inc
