# kas configs (scarthgap)

[kas](https://github.com/siemens/kas) configurations for building and testing
the Pulsar test image on Yocto **scarthgap** (5.0 LTS).

Scarthgap predates `bitbake-setup` — its bitbake (2.8) does not ship the tool —
so this replaces the `bitbake-setup.conf.json` workflow used on `main` and on
the `wrynose` branch. Same split as `meta-exein-runtime`.

## The rust mixin is mandatory here

Pulsar 0.10.x is Rust edition 2024 (MSRV 1.85). Scarthgap ships rustc 1.75,
and cargo 1.75 cannot even parse the workspace manifest:

```
feature `edition2024` is required
```

`base.yml` therefore pulls in `meta-lts-mixins` on its `scarthgap/rust`
branch, which provides rust 1.98.1 for scarthgap. Without that layer this
recipe cannot build on this release at all.

## Composition

- `base.yml` — layers (poky / meta-oe / the rust mixin, all pinned to
  scarthgap), `btf` in `DISTRO_FEATURES`, and the QEMU/testimage settings.
- `qemu{x86-64,arm64}.yml` — one `MACHINE` per file.
- `pulsar-test-<machine>.yml` — composed entry points; what you invoke.

## Usage

```bash
pip install --user kas

kas build kas/pulsar-test-qemux86-64.yml

# Run the ptest suite under QEMU
kas shell kas/pulsar-test-qemux86-64.yml -c "bitbake -c testimage pulsar-test-image"
```
