inherit cargo cargo-update-recipe-crates pkgconfig ptest

SUMMARY = "pulsar"
HOMEPAGE = "https://pulsar.sh"
LICENSE = "Apache-2.0"
SRC_URI += "git://github.com/Exein-io/pulsar.git;protocol=https;nobranch=1;branch=main"
SRC_URI += "file://run-ptest \
            file://ptest-lib.sh \
            file://tests/"
LIC_FILES_CHKSUM = "file://LICENSES/LICENSE-APACHE-2.0;md5=a0b5614acd31d1f66c2b9fe2c035f5dd"
SRCREV = "e79d2eb3771b7ad657c0029ca2c7bbaeac5e61fb"

PV:append = ".AUTOINC+e79d2eb377"

# Already stripped when built in release
INSANE_SKIP:${PN} += "already-stripped"
# FIXME: Build paths are currently embedded
INSANE_SKIP:${PN} += "buildpaths"

DEPENDS = "clang-native elfutils llvm-native openssl zlib"

# pulsar's bpf-builder compiles the eBPF probes with whatever "clang" it finds
# on PATH, and strips them with "llvm-strip". Point it at oe-core's clang-native
# rather than the build host's compiler: the probe ISA depends on the clang
# version, so relying on the host makes the bytecode vary per builder. That is
# what produced BPF ISA v1 objects on hosts with clang < 20, which the Linux
# 6.18 verifier rejected.
#
# llvm-native supplies llvm-strip. clang-native already depends on it, but
# bpf-builder invokes llvm-strip itself, so depend on it directly rather than
# rely on that staying true.
export CLANG = "${STAGING_BINDIR_NATIVE}/clang"

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
    # The upstream crate version, for t00 to assert --version against. Derived
    # from PV so a version bump does not need the test edited.
    echo "${PV}" | cut -d+ -f1 > ${D}${PTEST_PATH}/expected-version
    install -d ${D}${PTEST_PATH}/tests
    for t in ${UNPACKDIR}/tests/t[0-9]*.sh; do
        install -m 755 "$t" ${D}${PTEST_PATH}/tests/
    done
}

require ${BPN}-crates.inc
