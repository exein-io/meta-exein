inherit cargo pkgconfig

# If this is git based prefer versioned ones if they exist
# DEFAULT_PREFERENCE = "-1"

# how to get pulsar_linux could be as easy as but default to a git checkout:
SRC_URI += "git://git@github.com/Exein-io/pulsar.git;protocol=ssh;nobranch=1;branch=main"
SRCREV = "67d782ee4eb54739a4262adf591d6297e79af9a1"

S = "${WORKDIR}/git"
CARGO_SRC_DIR = "pulsar"


# please note if you have entries that do not begin with crate://
# you must change them to how that package can be fetched
SRC_URI += " \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/ahash/0.4.7 \
    crate://crates.io/aho-corasick/0.7.18 \
    crate://crates.io/anyhow/1.0.57 \
    crate://crates.io/async-trait/0.1.53 \
    crate://crates.io/atty/0.2.14 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/axum-core/0.1.2 \
    crate://crates.io/axum/0.4.8 \
    crate://crates.io/aya/0.10.7 \
    crate://crates.io/bindgen/0.59.2 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/byteorder/1.4.3 \
    crate://crates.io/bytes/1.1.0 \
    crate://crates.io/cc/1.0.73 \
    crate://crates.io/cexpr/0.6.0 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/chrono/0.4.19 \
    crate://crates.io/clang-sys/1.3.3 \
    crate://crates.io/clap/3.1.14 \
    crate://crates.io/clap_derive/3.1.7 \
    crate://crates.io/clap_lex/0.2.0 \
    crate://crates.io/comfy-table/5.0.1 \
    crate://crates.io/crc32fast/1.3.2 \
    crate://crates.io/crossterm/0.23.2 \
    crate://crates.io/crossterm_winapi/0.9.0 \
    crate://crates.io/dlv-list/0.2.3 \
    crate://crates.io/dns-parser/0.8.0 \
    crate://crates.io/env_logger/0.9.0 \
    crate://crates.io/errno-dragonfly/0.1.2 \
    crate://crates.io/errno/0.2.8 \
    crate://crates.io/fastrand/1.7.0 \
    crate://crates.io/flate2/1.0.24 \
    crate://crates.io/fnv/1.0.7 \
    crate://crates.io/form_urlencoded/1.0.1 \
    crate://crates.io/futures-channel/0.3.21 \
    crate://crates.io/futures-core/0.3.21 \
    crate://crates.io/futures-executor/0.3.21 \
    crate://crates.io/futures-io/0.3.21 \
    crate://crates.io/futures-macro/0.3.21 \
    crate://crates.io/futures-sink/0.3.21 \
    crate://crates.io/futures-task/0.3.21 \
    crate://crates.io/futures-util/0.3.21 \
    crate://crates.io/futures/0.3.21 \
    crate://crates.io/getrandom/0.2.6 \
    crate://crates.io/glob/0.3.0 \
    crate://crates.io/hashbrown/0.11.2 \
    crate://crates.io/hashbrown/0.9.1 \
    crate://crates.io/heck/0.3.3 \
    crate://crates.io/heck/0.4.0 \
    crate://crates.io/hermit-abi/0.1.19 \
    crate://crates.io/hex/0.4.3 \
    crate://crates.io/http-body/0.4.4 \
    crate://crates.io/http-range-header/0.3.0 \
    crate://crates.io/http/0.2.7 \
    crate://crates.io/httparse/1.7.1 \
    crate://crates.io/httpdate/1.0.2 \
    crate://crates.io/humantime/2.1.0 \
    crate://crates.io/hyper/0.14.18 \
    crate://crates.io/hyperlocal/0.8.0 \
    crate://crates.io/indexmap/1.8.1 \
    crate://crates.io/instant/0.1.12 \
    crate://crates.io/itoa/1.0.1 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/lazycell/1.3.0 \
    crate://crates.io/libc/0.2.125 \
    crate://crates.io/libloading/0.7.3 \
    crate://crates.io/lock_api/0.4.7 \
    crate://crates.io/log/0.4.17 \
    crate://crates.io/loopdev/0.4.0 \
    crate://crates.io/matches/0.1.9 \
    crate://crates.io/matchit/0.4.6 \
    crate://crates.io/memchr/2.5.0 \
    crate://crates.io/memoffset/0.6.5 \
    crate://crates.io/mime/0.3.16 \
    crate://crates.io/minimal-lexical/0.2.1 \
    crate://crates.io/miniz_oxide/0.5.3 \
    crate://crates.io/mio/0.8.2 \
    crate://crates.io/miow/0.3.7 \
    crate://crates.io/nix/0.24.1 \
    crate://crates.io/nom/7.1.1 \
    crate://crates.io/ntapi/0.3.7 \
    crate://crates.io/num-integer/0.1.45 \
    crate://crates.io/num-traits/0.2.15 \
    crate://crates.io/num_cpus/1.13.1 \
    crate://crates.io/object/0.28.3 \
    crate://crates.io/once_cell/1.10.0 \
    crate://crates.io/ordered-multimap/0.3.1 \
    crate://crates.io/os_str_bytes/6.0.0 \
    crate://crates.io/parking_lot/0.11.2 \
    crate://crates.io/parking_lot/0.12.0 \
    crate://crates.io/parking_lot_core/0.8.5 \
    crate://crates.io/parking_lot_core/0.9.3 \
    crate://crates.io/peeking_take_while/0.1.2 \
    crate://crates.io/percent-encoding/2.1.0 \
    crate://crates.io/pin-project-internal/1.0.10 \
    crate://crates.io/pin-project-lite/0.2.9 \
    crate://crates.io/pin-project/1.0.10 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/ppv-lite86/0.2.16 \
    crate://crates.io/proc-macro-error-attr/1.0.4 \
    crate://crates.io/proc-macro-error/1.0.4 \
    crate://crates.io/proc-macro2/1.0.37 \
    crate://crates.io/procfs/0.12.0 \
    crate://crates.io/quick-error/1.2.3 \
    crate://crates.io/quote/1.0.18 \
    crate://crates.io/rand/0.8.5 \
    crate://crates.io/rand_chacha/0.3.1 \
    crate://crates.io/rand_core/0.6.3 \
    crate://crates.io/redox_syscall/0.2.13 \
    crate://crates.io/regex-syntax/0.6.25 \
    crate://crates.io/regex/1.5.5 \
    crate://crates.io/remove_dir_all/0.5.3 \
    crate://crates.io/rust-ini/0.17.0 \
    crate://crates.io/rustc-hash/1.1.0 \
    crate://crates.io/rustversion/1.0.6 \
    crate://crates.io/ryu/1.0.9 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/semver/1.0.9 \
    crate://crates.io/serde/1.0.137 \
    crate://crates.io/serde_derive/1.0.137 \
    crate://crates.io/serde_json/1.0.80 \
    crate://crates.io/serde_urlencoded/0.7.1 \
    crate://crates.io/serial_test/0.6.0 \
    crate://crates.io/serial_test_derive/0.6.0 \
    crate://crates.io/shlex/1.1.0 \
    crate://crates.io/signal-hook-mio/0.2.3 \
    crate://crates.io/signal-hook-registry/1.4.0 \
    crate://crates.io/signal-hook/0.3.13 \
    crate://crates.io/slab/0.4.6 \
    crate://crates.io/smallvec/1.8.0 \
    crate://crates.io/smart-default/0.6.0 \
    crate://crates.io/socket2/0.4.4 \
    crate://crates.io/strsim/0.10.0 \
    crate://crates.io/strum/0.23.0 \
    crate://crates.io/strum_macros/0.23.1 \
    crate://crates.io/syn/1.0.92 \
    crate://crates.io/sync_wrapper/0.1.1 \
    crate://crates.io/sys-mount/1.5.1 \
    crate://crates.io/tempfile/3.3.0 \
    crate://crates.io/termcolor/1.1.3 \
    crate://crates.io/textwrap/0.15.0 \
    crate://crates.io/thiserror-impl/1.0.31 \
    crate://crates.io/thiserror/1.0.31 \
    crate://crates.io/time/0.1.44 \
    crate://crates.io/tokio-fd/0.3.0 \
    crate://crates.io/tokio-macros/1.7.0 \
    crate://crates.io/tokio-util/0.7.1 \
    crate://crates.io/tokio/1.18.1 \
    crate://crates.io/tower-http/0.2.5 \
    crate://crates.io/tower-layer/0.3.1 \
    crate://crates.io/tower-service/0.3.1 \
    crate://crates.io/tower/0.4.12 \
    crate://crates.io/tracing-attributes/0.1.21 \
    crate://crates.io/tracing-core/0.1.26 \
    crate://crates.io/tracing/0.1.34 \
    crate://crates.io/try-lock/0.2.3 \
    crate://crates.io/unicode-segmentation/1.9.0 \
    crate://crates.io/unicode-width/0.1.9 \
    crate://crates.io/unicode-xid/0.2.3 \
    crate://crates.io/version_check/0.9.4 \
    crate://crates.io/want/0.3.0 \
    crate://crates.io/wasi/0.10.0+wasi-snapshot-preview1 \
    crate://crates.io/wasi/0.11.0+wasi-snapshot-preview1 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-util/0.1.5 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/windows-sys/0.36.1 \
    crate://crates.io/windows_aarch64_msvc/0.36.1 \
    crate://crates.io/windows_i686_gnu/0.36.1 \
    crate://crates.io/windows_i686_msvc/0.36.1 \
    crate://crates.io/windows_x86_64_gnu/0.36.1 \
    crate://crates.io/windows_x86_64_msvc/0.36.1 \
"


SRC_URI += " \
    file://pulsar.ini \
    file://rules/example_rules1.json \
    file://pulsar \
    file://pulsard \
"

do_install () {
    install -d ${D}${bindir}
    install -d ${D}/var/lib/pulsar
    install -d ${D}/var/lib/pulsar/rules

    install -m 644 ${WORKDIR}/pulsar.ini ${D}/var/lib/pulsar
    install -m 644 ${WORKDIR}/rules/example_rules1.json ${D}/var/lib/pulsar/rules
    install -m 755 ${WORKDIR}/pulsar ${D}${bindir}/pulsar
    install -m 755 ${WORKDIR}/pulsard ${D}${bindir}/pulsard
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/pulsar-exec ${D}${bindir}
}

LIC_FILES_CHKSUM = "file://LICENSE-APACHE-2.0;md5=a0b5614acd31d1f66c2b9fe2c035f5dd"

SUMMARY = "pulsar linux"
HOMEPAGE = "https://exein.io/pulsar"
LICENSE = "Apache-2.0"

DEPENDS = "openssl zlib elfutils"

do_compile[nostamp] = "1"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include pulsar_linux-${PV}.inc
include pulsar_linux.inc
