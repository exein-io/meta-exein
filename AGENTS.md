# AGENTS.md

Working notes for `meta-exein` — the Yocto layer that builds the
**open-source** Pulsar from source (`github.com/Exein-io/pulsar`).

Do not confuse it with `meta-exein-runtime`, the sibling layer that ships
**pre-built** enterprise binaries (`exein-pulsar-bin`, `exein-photon-bin`,
`exein-lite-bin`). Recipes, rules, config paths and detection signals differ.
Its `knowledge/` directory is worth reading, but its conclusions are about the
enterprise agent and do not automatically transfer here.

## Layout

| Path | What |
|---|---|
| `recipes-security/pulsar/pulsar_<ver>+dev.bb` | the recipe; `cargo` + `ptest` |
| `recipes-security/pulsar/pulsar-crates.inc` | generated crate list — **never hand-edit** |
| `recipes-security/pulsar/files/` | ptest suite (`run-ptest`, `ptest-lib.sh`, `tests/t*.sh`) |
| `recipes-kernel/linux/` | `btf.cfg` / `btf.scc`, applied when `btf` is in `DISTRO_FEATURES` |
| `recipes-core/images/pulsar-test-image.bb` | QEMU test image, `TEST_SUITES = "ping ssh ptest"` |
| `conf/fragments/` | conf fragments: `pulsar` (enables `btf`), `optimizations` |
| `bitbake-setup.conf.json` | pinned layer SHAs + the `exein-pulsar-test` build config |

---

# Part 1 — Upgrading Pulsar

## Procedure

1. **Find the release commit.** Pin the tag's commit, not a branch head:
   ```bash
   gh api repos/Exein-io/pulsar/commits/v<X.Y.Z> --jq '{sha:.sha,msg:.commit.message}'
   ```

2. **Rename the recipe** and update `SRCREV` + `PV:append`. The layer's
   convention is `pulsar_<X.Y.Z>+dev.bb` with
   `PV:append = ".AUTOINC+<first 10 of sha>"`, kept even when pinning a
   release tag. Use `git mv` so the diff stays reviewable.

3. **Read `CHANGELOG.md` for the release, specifically `### Changed`.**
   This is the step that is easy to skip and expensive to skip. Pulsar marks
   breaking changes `**BREAKING**`, and they regularly invalidate `do_install`.
   0.10.0 (#369) split the single `pulsar-exec` binary into `pulsard` + `pulsar`
   and deleted the `scripts/pulsar` / `scripts/pulsard` wrappers — the old
   `do_install` referenced all three and would have failed outright.

4. **Check the MSRV.** `edition` in `[workspace.package]` of `Cargo.toml` and
   any MSRV note in the changelog. Cross-reference against the rustc the target
   series ships. If the floor rises above what a release branch can provide,
   that branch stays on the older Pulsar — do **not** widen `main`'s
   `LAYERSERIES_COMPAT` to compensate. See [Branching](#branching).

5. **Regenerate the crate list** — with bitbake, not by hand:
   ```bash
   bitbake -c update_crates pulsar
   ```
   It rewrites `recipes-security/pulsar/pulsar-crates.inc` in place from
   `Cargo.lock`.

6. **Verify install paths still exist** in the new tree before building —
   `.github/docker/pulsar.ini`, `rules/`, the binary names under
   `target/${CARGO_TARGET_SUBDIR}/`, and the `LIC_FILES_CHKSUM` md5.

7. **Lint, diffing against the pre-bump recipe** so pre-existing noise does not
   drown the signal:
   ```bash
   oelint-adv --quiet recipes-security/pulsar/pulsar_<ver>+dev.bb
   ```
   The recipe currently carries 18 findings (INSANE_SKIP, missing DESCRIPTION,
   var ordering, hardcoded `/var`). A clean bump adds **zero** new ones.

8. **Build and test** — see Part 2. A bump is not done until
   `bitbake -c testimage pulsar-test-image` passes.

## Branching

**`main` tracks the current Yocto development series and declares exactly one
value in `LAYERSERIES_COMPAT` — right now `blacksail`.** It is never a list.
Every Yocto release has its own branch of this layer:

```
kirkstone  mickledore  nanbield  scarthgap
styhead    walnascar   whinlatter  wrynose
```

Supporting an older release means landing on its branch, not widening main's
compat. A layer whose `LAYERSERIES_COMPAT` omits the core layer's
`LAYERSERIES_CORENAMES` is rejected at parse time, so main's value and the
oe-core pin in `bitbake-setup.conf.json` must move together.

Confirm the current series rather than assuming:
```bash
git -C <oe-core> show origin/master:meta/conf/layer.conf | grep LAYERSERIES_CORENAMES
```

## Rust floor per Yocto release

Pulsar 0.10.0 is edition 2024 (MSRV 1.85).

| Release | rustc | edition 2024 |
|---|---|:-:|
| nanbield | 1.70.0 | ✗ |
| scarthgap | 1.75.0 | ✗ |
| styhead | 1.79.0 | ✗ |
| walnascar | 1.84.1 | ✗ |
| whinlatter | 1.90.0 | ✓ |
| wrynose | 1.94.1 | ✓ |
| blacksail | 1.98.1 | ✓ |

Re-derive rather than trusting this table after an upgrade:
```bash
git -C <oe-core> ls-tree -r --name-only origin/<branch> \
  | grep -oP 'recipes-devtools/rust/rust_\K[0-9.]+(?=\.bb)'
```

## Recipe gotchas

- **`pulsar-crates.inc` is generated.** `bitbake -c update_crates pulsar`
  writes it from `Cargo.lock`; the format is deterministic, so a correct run
  produces a diff containing only genuine crate changes.
- **`vergen-gitcl` in `build.rs`** shells out to git against `${S}/.git` during
  `do_compile`. It works under Yocto because the git fetcher leaves `.git` in
  place — but it means `do_compile` depends on git being in `HOSTTOOLS`.
- **`INSANE_SKIP` `already-stripped` / `buildpaths`** are deliberate. Release
  builds self-strip and embed build paths.

---

# Part 2 — Testing

Three layers of confidence, cheapest first. Do not stop at the first one —
"it compiles" says nothing about whether the eBPF probes load.

| Layer | Command | Proves |
|---|---|---|
| Recipe builds | `bitbake pulsar` | fetch, crates, compile, install, packaging QA |
| Image builds | `bitbake pulsar-test-image` | ptest packaging, rootfs assembly |
| It actually runs | `bitbake -c testimage pulsar-test-image` | probes attach, rules fire on live events |

## Setting up a build

```bash
./bitbake/bin/bitbake-setup init --setup-dir-name qemux86-64 \
    bitbake-setup.conf.json exein-pulsar-test machine/qemux86-64 --non-interactive

. bitbake-builds/qemux86-64/build/init-build-env
bitbake pulsar-test-image
bitbake -c testimage pulsar-test-image
```

One build dir per machine: `bitbake-builds/qemux86-64/`, `bitbake-builds/qemuarm64/`.

**Host prerequisite:** `conf/layer.conf` puts `clang` and `llvm-strip` in
`HOSTTOOLS`, and bitbake refuses to *start* if either is missing — the error
arrives before any recipe parses, which makes it look like a config problem.
Distributions that ship only versioned binaries need symlinks:
```bash
ln -s /usr/bin/llvm-strip-18 ~/bin/llvm-strip
ln -s /usr/bin/clang-18      ~/bin/clang
```

## Reading ptest results

`testimage` prints a pass/fail summary, but the per-assertion detail is on disk:

```
tmp/work/<machine>-poky-linux/pulsar-test-image/1.0/testimage/ptest_log.<ts>/
├── pulsar              # one PASSED/FAILED/SKIPPED line per assertion
└── ptest-runner.log    # full stdout, including DBG dumps — read this on failure
```

`ptest-lib.sh` dumps daemon warnings/errors on a failed assertion. That output
is usually the answer; read it before forming a theory.

## How the suite works

The open-source recipe ships **no systemd unit** (unlike `exein-pulsar-bin`),
so `ptest-lib.sh` starts `pulsard` itself and reads THREAT lines from its
stdout — not from the journal. Everything else follows the sibling layer's shape.

A rule hit renders as:
```
[<ts> THREAT <image> (<pid>)] [rules-engine - <rule name>] <payload>
```
`THREAT` is ANSI-wrapped unconditionally (the `Display` impl always uses the
alternate flag, even to a file), so match on the plain `rules-engine - <rule>]`
portion, never on `THREAT` itself.

| Section | Rule exercised | Keys on |
|---|---|---|
| `t00-binaries` | — | binaries, `--version`, rule install, `pulsar status` |
| `t01-file-create` | Create files below /root | file path only |
| `t02-history` | Shell history truncation / deletion | file path only |
| `t03-compression` | Sensitive file compression | file path **+ `header.image`** |

`log_mark` before every trigger. `check_rule` compares only lines after that
offset, so a hit from an earlier section cannot satisfy a later assertion.

## Writing new test sections

- **Use synthetic paths** (`/root/.ptest_canary`, `/tmp/.ptest_*`) so unrelated
  system activity cannot trip the assertion.
- **Never stage a mock over its own source.** Copying `/usr/bin/tar` to
  `/usr/bin/tar` after `mv`-ing it aside deletes the source. Stage under
  `$PTEST_BIN_DIR` (`/tmp/.ptest_bin`). Staging inside `/usr/bin` also trips
  the "Rename any file below binary directories" rule and pollutes the log.
- **Check the rule's `condition` before writing the trigger.** A rule keyed on
  `header.image ENDS_WITH "/tar"` needs the *opening process* to be tar; a
  busybox symlink resolves to busybox and can never match.
- **BusyBox, not coreutils.** `head -1` fails — use `head -n 1`. Same for
  `tail -N` → `tail -n N`. `head -c` is unsupported entirely. `ps -ef`/`ps aux`
  unsupported. No `timeout`. A failing option inside a pipeline fails
  *silently* in terms of test outcome and can invert a result.
- **`expect`-style checks that pass on any non-zero exit prove little** — a
  missing binary looks identical to a denial.

## Known environment limits

- **`QEMU_USE_KVM = "0"` is required.** Under KVM the file-system-monitor
  kprobes (`security_file_open`, `security_path_*`) silently fail to attach and
  no rule fires. Set in `pulsar-test-image.bb`; do not "optimise" it away.
- **Probe attach is slow under TCG.** `daemon_wait` polls up to
  `HOOK_READY_TIMEOUT` (180 s), re-firing a known-good trigger so a hit is
  possible the moment hooks go live. Do not replace with a fixed sleep.
- **Process attribution does not work on emulated targets.** The daemon logs
  `Process not found in tracker <pid>` and emits events with `header.image`
  unset, so **every rule keyed on the calling binary is unmatchable** — a large
  share of the shipped ruleset, including most of `command_and_control` and
  `defense_evasion`. File-path rules are unaffected.

  This is not a fork/exec race: a pid given 5 seconds to settle before `exec`
  still missed. `t03` detects the condition and SKIPs with that reason rather
  than failing. **Unverified whether this affects real hardware** — only TCG
  QEMU has been tested. Settle it before relying on exec-keyed rules.

---

# Open items

- [ ] **Confirm process-tracker behaviour on real hardware / KVM-capable
      target.** If attribution works there, `t03` should pass rather than skip,
      and the limitation is purely emulation. If it does not, it is an upstream
      bug worth filing.
- [ ] **No `COMPATIBLE_HOST` on the pulsar recipe**, so it claims every
      architecture. Upstream builds only `x86_64`, `aarch64` and `riscv64gc`;
      on armv7/mips/ppc the recipe fails deep in the Rust/eBPF compile instead
      of reporting itself incompatible. `exein-pulsar-bin` constrains this with
      `COMPATIBLE_HOST = "(x86_64|aarch64).*-linux"`.
- [ ] **`/var/lib/pulsar/null` ships in the package.** `do_install` has
      `install -m 644 /dev/null ${D}/var/lib/pulsar` with a *directory*
      destination, so install creates a file named `null`. The line is inert
      otherwise — the directory is already created above and `pulsar.ini` is
      installed right after. One-line delete.
- [ ] **riscv64 untested** and not offered in `bitbake-setup.conf.json`, though
      upstream releases for it.
