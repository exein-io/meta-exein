# Exein Layer for Yocto
This layer contains Pulsar and Cosmo recipes to add the Pulsar security framework to an image and to perform an analysis with Cosmo.

> **Note:** `meta-exein` is tested only on Yocto Kirkstone and Langdale.


# Pulsar

## Yocto dependencies
This layer currently depends on the additional mandatory layers:

- [meta-openembedded/meta-oe](https://github.com/openembedded/meta-openembedded)
- [meta-rust-bin](https://github.com/Exein-io/meta-rust-bin/tree/exein)

> **Note:** make sure you are using the branch `exein` for `meta-rust-bin`.


## System dependencies
This layer depends on these system tools:
 
- `clang` >= 13
- `llvm-strip` >= 13 


## Usage
Before start: review the Yocto system requirements at 
https://docs.yoctoproject.org/4.0.1/ref-manual/system-requirements.html

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


# Cosmo

## Usage

1. Download the `meta-exein` layer
2. Run:
    ```bash
    bitbake cosmo-native -c install
    ```
3. Run:
    ```bash
    cosmo login
    ```
4. Add the `meta-exein` layer to your `bblayers.conf` file
5. Add `USER_CLASSES += "cosmo"` to `local.conf` file
6. Build your image, for example run:
    ```bash
    bitbake core-image-minimal
    ```
7. Use the [Cosmo dashboard](https://cosmo.exein.io) to explore your project


# License and Copyright
Copyright 2023 Exein SpA

All metadata is Apache 2-0 licensed unless otherwise stated. Source code included in tree for individual recipes is under the LICENSE stated in the associated recipe (.bb file) unless otherwise stated.

License information for any other files is either explicitly stated or defaults to Apache 2.0 license.