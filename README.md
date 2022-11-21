# Exein Layer for Yocto
This layer contains Pulsar and Cosmo recipes to add the Pulsar security framework to an image and to perform an analysis with Cosmo.

Note: `meta-exein` works on Yocto Kirkstone and Langdale.


# Pulsar

## Yocto dependencies
This layer currently depends on the additional mandatory layers:


- meta-poky
- meta-yocto-bsp
- meta-openembedded/meta-oe
- meta-rust-bin


## System dependencies
This layer depends on these system tools:
 
- `clang` >= 13
- `llvm-strip` >= 13 


## Usage
Before start: review the Yocto system requirements at 
https://docs.yoctoproject.org/4.0.1/ref-manual/system-requirements.html

1. Install dependencies: `apt-get install dwarves clang`
2. Download the *meta-exein* layer
2. Add the *meta-exein* layer to your `bblayers.conf` file
3. Add `IMAGE_INSTALL:append = " pulsar"` to `local.conf` file
4. Add `btf` to `DISTRO_FEATURES` in your distro or local config: `DISTRO_FEATURES:append = " btf"`
6. Build your image, for example run `bitbake core-image-minimal`


# Cosmo

## Usage

1. Download and extract *meta-exein* layer
2. Run `bitbake cosmo-native -c install`
2. Run `cosmo login`
3. Add the *meta-exein* layer to your `bblayers.conf` file
4. Add `USER_CLASSES += "cosmo"` to `local.conf` file
5. Build your image, for example run `bitbake core-image-minimal`
6. Use the dashboard at https://cosmo.exein.io to explore your project


# License and Copyright
Copyright 2022 Exein SpA

All metadata is Apache 2-0 licensed unless otherwise stated. Source code included in tree for individual recipes is under the LICENSE stated in the associated recipe (.bb file) unless otherwise stated.

License information for any other files is either explicitly stated or defaults to Apache 2.0 license.