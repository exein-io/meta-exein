# Exein Layer for Yocto
This layer contains Pulsar and kernel recipes to add the Pulsar security framework to an image.

> **Note:** `meta-exein` is tested only on Yocto Kirkstone, Langdale, Mickledore, Nanbield and Scarthgap.


# Pulsar

## Yocto dependencies
This layer currently depends on the additional mandatory layers:

- [meta-openembedded/meta-oe](https://github.com/openembedded/meta-openembedded)
- [meta-rust-bin](https://github.com/rust-embedded/meta-rust-bin)


## System dependencies
This layer depends on these system tools:
 
- `clang` >= 13
- `llvm-strip` >= 13 


## Usage
Before start: review the Yocto system requirements at 
https://docs.yoctoproject.org/4.2.3/ref-manual/system-requirements.html

1. Install dependencies. Example for Debian/Ubuntu:
    ```bash
    apt-get install dwarves clang
    ```
2. Download the `meta-exein` layer
3. Add the `meta-exein` layer to your `bblayers.conf` file
4. Add `IMAGE_INSTALL:append = " pulsar"` to `local.conf` file
5. Add `btf` to `DISTRO_FEATURES` in your distro or local config: `DISTRO_FEATURES:append = " btf"`
6. Build your image, for example run:
    ```bash
    bitbake core-image-minimal
    ```

> **Note:** If you intend to use non-standard containers, particularly a manually configured one (i.e., not managed by typical container engines like Docker, Podman, Kubernetes, etc.), ensure `CONFIG_MEMCG=y` is enabled in your Linux kernel configuration (`recipes-kernel/linux/files/btf.cfg`) for correct Pulsar detection.
**Standard container environments enable this configuration by default.**

# License and Copyright
Copyright 2023 Exein SpA

All metadata is Apache 2-0 licensed unless otherwise stated. Source code included in tree for individual recipes is under the LICENSE stated in the associated recipe (.bb file) unless otherwise stated.

License information for any other files is either explicitly stated or defaults to Apache 2.0 license.