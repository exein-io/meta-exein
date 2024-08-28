inherit cargo cargo-update-recipe-crates pkgconfig

SUMMARY = "pulsar"
HOMEPAGE = "https://pulsar.sh"
LICENSE = "Apache-2.0"
SRC_URI += "git://git@github.com/Exein-io/pulsar.git;protocol=ssh;nobranch=1;branch=main"
LIC_FILES_CHKSUM = "file://LICENSES/LICENSE-APACHE-2.0;md5=a0b5614acd31d1f66c2b9fe2c035f5dd"
SRCREV = "58cede7bf20821af4575115878d935afb9f392ef"

S = "${WORKDIR}/git"
PV:append = ".AUTOINC+58cede7bf2"

# Already stripped when built in release
INSANE_SKIP:${PN} += "already-stripped"

DEPENDS = "openssl zlib elfutils"

# Enable networking at compile time for Rust non-crates dependencies
do_compile[network] = "1"
CARGO_BUILD_FLAGS:remove = " --frozen"

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

    # Install pulsar helper scripts
    install -m 755 ${S}/scripts/pulsar ${D}${bindir}/pulsar
    install -m 755 ${S}/scripts/pulsard ${D}${bindir}/pulsard

    # Install pulsar base executable
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/pulsar-exec ${D}${bindir}
}

require ${BPN}-crates.inc
