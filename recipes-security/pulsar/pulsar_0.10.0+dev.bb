inherit cargo cargo-update-recipe-crates pkgconfig ptest

SUMMARY = "pulsar"
HOMEPAGE = "https://pulsar.sh"
LICENSE = "Apache-2.0"
SRC_URI += "git://github.com/Exein-io/pulsar.git;protocol=https;nobranch=1;branch=main"
SRC_URI += "file://run-ptest \
            file://ptest-lib.sh \
            file://tests/"

# Blacksail ships OpenSSL 4; the openssl-sys pinned by 0.10.0 refuses to build
# against it. Lockfile-only bump, dropped once upstream carries it.
SRC_URI += "file://0001-Cargo.lock-bump-openssl-sys-for-OpenSSL-4.x.patch"

# process-monitor's sched_process_exec probe is rejected by the Linux 6.18
# verifier; without it nothing populates the process tracker.
SRC_URI += "file://0002-eBPF-fix-6.18-verifier-rejection.patch"
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

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    install -m 644 ${UNPACKDIR}/ptest-lib.sh ${D}${PTEST_PATH}/ptest-lib.sh
    install -d ${D}${PTEST_PATH}/tests
    for t in ${UNPACKDIR}/tests/t[0-9]*.sh; do
        install -m 755 "$t" ${D}${PTEST_PATH}/tests/
    done
}

require ${BPN}-crates.inc
