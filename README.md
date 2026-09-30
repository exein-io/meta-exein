# Exein Layer for Yocto

<p align="center">
  <a href="https://www.yoctoproject.org/development/yocto-project-compatible-layers/" target="_blank">
    <img src="Yocto_Compatible_Badge.png" alt="Yocto Compatible" width="100"/>
  </a>
</p>

This layer contains Pulsar and kernel recipes to add the [Pulsar](https://github.com/Exein-io/pulsar) security framework to an image.


> **Note:** `main` targets the current Yocto development series, **Blacksail**.
> Each release has its own branch of this layer — `wrynose`, `whinlatter`,
> `walnascar`, `styhead`, `scarthgap`, `nanbield`, `mickledore`, `kirkstone`.
> Check out the branch matching your Yocto release.
>
> Pulsar 0.10.0 is written against the Rust 2024 edition, so it needs
> `rustc` >= 1.85. Blacksail (1.98.1), Wrynose (1.94.1) and Whinlatter (1.90)
> satisfy that; Walnascar (1.84.1) and older do not, so the branches for those
> releases stay on an earlier Pulsar.

# Pulsar

## Yocto dependencies
This layer currently depends on the additional mandatory layers:

- [meta-openembedded/meta-oe](https://github.com/openembedded/meta-openembedded)


## System dependencies

None beyond the standard Yocto host requirements. The eBPF probes are compiled
with oe-core's `clang-native`, so no host clang or llvm is needed — and the
probe bytecode no longer varies with the build machine's compiler.


## Usage
Before start: review the Yocto system requirements at 
https://docs.yoctoproject.org/dev/ref-manual/system-requirements.html

1. Download the `meta-exein` layer
2. Add the `meta-exein` layer to your `bblayers.conf` file
3. Add `IMAGE_INSTALL:append = " pulsar"` to `local.conf` file
4. Add `btf` to `DISTRO_FEATURES` in your distro or local config: `DISTRO_FEATURES:append = " btf"`
5. Build your image, for example run:
    ```bash
    bitbake core-image-minimal
    ```

The recipe installs two binaries: `pulsard` (the daemon) and `pulsar` (the
CLI). Up to 0.9.0 these were shell wrappers around a single `pulsar-exec`
binary; 0.10.0 replaced all three with two real binaries.


## Testing

A `bitbake-setup.conf.json` is provided to build a minimal QEMU image with
Pulsar and its ptest suite installed, targeting `qemux86-64` or `qemuarm64`.

```bash
./bitbake/bin/bitbake-setup init --setup-dir-name qemux86-64 \
    bitbake-setup.conf.json exein-pulsar-test machine/qemux86-64 --non-interactive
```

Then build and run the suite under QEMU:

```bash
. bitbake-builds/qemux86-64/build/init-build-env
bitbake pulsar-test-image
bitbake -c testimage pulsar-test-image
```

`pulsar-test-image` runs the `ping`, `ssh` and `ptest` suites. The ptest suite
(`recipes-security/pulsar/files/`) starts `pulsard`, waits for its eBPF probes
to attach, and asserts that rules under `/var/lib/pulsar/rules` actually fire:

| Section | Covers |
|---|---|
| `t00-binaries` | the 0.10.0 `pulsard`/`pulsar` split, `--version`, rule install, `pulsar status` |
| `t01-file-create` | `Create files below /root` (FileCreated) |
| `t02-history` | `Shell history truncation` / `Shell history deletion` |
| `t03-compression` | `Sensitive file compression` (FileOpened by an archiver) |

> **Note:** the test image sets `QEMU_USE_KVM = "0"`. Under KVM the
> file-system-monitor kprobes silently fail to attach and no rule fires.


> **Note:** If you intend to use non-standard containers, particularly a manually configured one (i.e., not managed by typical container engines like Docker, Podman, Kubernetes, etc.), ensure `CONFIG_MEMCG=y` is enabled in your Linux kernel configuration (`recipes-kernel/linux/files/btf.cfg`) for correct Pulsar detection.
**Standard container environments enable this configuration by default.**

# Contributing
Feel free to utilize the GitHub PR workflow to share your patches. The following  guidelines are recommended: [Github PR guidelines](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request).

Layer maintainer: Gianluigi Spagnuolo <gianluigi@exein.io>


# License and Copyright
Copyright 2024 Exein SpA

All metadata is Apache 2-0 licensed unless otherwise stated. Source code included in tree for individual recipes is under the LICENSE stated in the associated recipe (.bb file) unless otherwise stated.

License information for any other files is either explicitly stated or defaults to Apache 2.0 license.
