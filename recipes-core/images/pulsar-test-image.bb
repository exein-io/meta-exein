SUMMARY = "Minimal QEMU image for testing the pulsar recipe"
DESCRIPTION = "Minimal QEMU image with pulsar and its ptest suite installed, \
               used to verify the open-source Pulsar build end to end."
LICENSE = "MIT"

require recipes-core/images/core-image-minimal.bb

inherit testimage

# Intentional for a test image
IMAGE_FEATURES += "allow-empty-password empty-root-password allow-root-login"

IMAGE_INSTALL += "pulsar pulsar-ptest ptest-runner dropbear"

# Test suites to run under bitbake -c testimage:
#   ping  — verifies target IP is set; skips ICMP when IP is 127.0.0.x
#           (slirp is user-space TCP/UDP only, ICMP cannot pass through it)
#   ssh   — SSH login as root
#   ptest — runs ptest-runner and collects PASS/FAIL from all ptest packages
TEST_SUITES = "ping ssh ptest"

# Disable KVM: pulsar's file-system-monitor kprobes (security_file_open,
# security_path_*) silently fail to attach under KVM, so rule-based detection
# does not work in that environment. Same constraint as exein-pulsar-bin in
# meta-exein-runtime.
QEMU_USE_KVM = "0"

# The eBPF probes need the kernel's own BTF blob at /sys/kernel/btf/vmlinux,
# which linux-%_btf.inc only adds when "btf" is in DISTRO_FEATURES.
inherit features_check
REQUIRED_DISTRO_FEATURES = "btf"
