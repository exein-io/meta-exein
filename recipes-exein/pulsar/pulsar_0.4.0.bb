inherit cargo pkgconfig

SRC_URI += "git://git@github.com/Exein-io/pulsar.git;protocol=ssh;nobranch=1;branch=main"
SRCREV = "797f68641ed92b35b152e0d147b9cdcf3bfa49e5"

S = "${WORKDIR}/git"
CARGO_SRC_DIR = "pulsar"
PV:append = ".AUTOINC+797f68641e"

SRC_URI += " \
    file://pulsar.ini \
    file://rules/basic_rules.yaml \
    file://pulsar \
    file://pulsard \
"

do_install () {
    install -d ${D}${bindir}
    install -d ${D}/var/lib/pulsar
    install -d ${D}/var/lib/pulsar/rules

    install -m 644 ${WORKDIR}/pulsar.ini ${D}/var/lib/pulsar
    install -m 644 ${WORKDIR}/rules/basic_rules.yaml ${D}/var/lib/pulsar/rules
    install -m 755 ${WORKDIR}/pulsar ${D}${bindir}/pulsar
    install -m 755 ${WORKDIR}/pulsard ${D}${bindir}/pulsard
    install -m 755 ${CARGO_RELEASE_DIR}/pulsar-exec ${D}${bindir}
}

LIC_FILES_CHKSUM = "file://LICENSES/LICENSE-APACHE-2.0;md5=a0b5614acd31d1f66c2b9fe2c035f5dd"

SUMMARY = "pulsar linux"
HOMEPAGE = "https://pulsar.sh"
LICENSE = "Apache-2.0"

INSANE_SKIP:${PN} += "already-stripped"

do_compile[network] = "1"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include pulsar_linux-${PV}.inc
include pulsar_linux.inc
