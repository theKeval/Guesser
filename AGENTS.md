# Agent Guide

Instructions for AI coding agents working in this repository. Read the
[README](README.md) and [development guide](docs/development.md) for the module
layout, build commands, and change conventions. This file covers versioning
and releases, where mistakes are expensive or irreversible.

## Applications

- `:numbcrunch` (`numbcrunch/`) is the published NumbCrunch app, application
  ID `io.keval.apps.numbcrunch`. All release rules below apply to it. Debug
  builds install as `io.keval.apps.numbcrunch.debug` so they never conflict
  with the Play-signed app; keep that suffix.
- `:guesser_v1` (`app/`) is the unpublished legacy app. Leave its version in
  `app/build.gradle` alone.

## Versioning Rules

- The single source of truth is [`version.properties`](version.properties):
  `VERSION_CODE` (integer) and `VERSION_NAME` (`MAJOR.MINOR.PATCH`). Never set
  `versionCode` or `versionName` in a `build.gradle` file.
- A released version code is never built again. `main` always holds the
  **next** version once the app has changed after the latest `v*` release tag.
  Details: [Version Lifecycle](docs/releasing.md#version-lifecycle).
- Before finishing any change that touches app files (anything outside
  `docs/`, `*.md`, `.github/`, `scripts/`, `app/`, `design_assets/`, `.idea/`),
  run `scripts/check-version-bump.sh` (after `git fetch --tags`):
  - If `version.properties` still holds the released code, this change starts
    the next version: increase `VERSION_CODE` by exactly 1, set the next PATCH
    `VERSION_NAME`, and add
    `## <name> (versionCode <code>) — unreleased` at the top of
    `CHANGELOG.md`.
  - If an unreleased version is already in progress, keep its code. Add your
    change to its changelog section, and raise `VERSION_NAME` only if your
    change warrants it (renaming the heading to match).
- Never lower or reuse a code, and never increase it by more than 1 per
  release; Play rejects reused codes permanently, even for discarded releases.
- Choose `VERSION_NAME` by semantic versioning: PATCH for fixes, polish, and
  tooling; MINOR for new player-visible features; MAJOR for redesigns or
  breaking changes. Ask the user if the choice is unclear.
- The guard also runs in CI and before `:numbcrunch:bundleRelease`. Never
  bypass, weaken, or edit it to make a change pass.

## Privacy Policy

[`PRIVACY.md`](PRIVACY.md) is the source of the privacy policy published at
`https://keval.io/apps/numbcrunch/privacy`, the URL in Play Console. It states that the app collects and shares no data, has no
internet permission, ads, analytics, or accounts, and stores only settings,
tutorial progress, and the current round on the device.

- Any change that adds a permission, SDK, network access, or new stored data
  must update `PRIVACY.md` (and its effective date) in the same pull request,
  and tell the user that Play Console's Data safety form must be updated too.
- After any `PRIVACY.md` change merges, remind the user to republish it to
  `https://keval.io/apps/numbcrunch/privacy`; the website is not updated
  automatically.
- Never put personal contact details in it beyond what the user provides.

## Signing and Secrets

- The upload key lives outside the repository in
  `~/data/projects/vault/numbcrunch-release/keys/` (`numbcrunch-upload.p12`, alias
  `numbcrunch-upload`). Gradle finds it through `NUMBCRUNCH_UPLOAD_*`
  properties in `~/.gradle/gradle.properties`; see
  [docs/releasing.md](docs/releasing.md).
- **Never** read, print, copy, or commit the keystore password, the keystore,
  or `~/.gradle/gradle.properties` values. Reference passwords only through
  the `NUMBCRUNCH_UPLOAD_*_PASSWORD_FILE` properties, and never put a password
  value in a `-P` flag, an inline environment assignment, or any command.
- **Never generate a new upload key** or change the key alias. Play accepts
  only bundles signed by the registered key, whose SHA-256 fingerprint is
  `9D:5C:30:17:D5:EB:63:34:3B:E5:DC:06:2B:99:D8:DB:FE:67:58:14:26:8D:60:A0:C4:6F:D5:78:73:FB:75:81`.
- Never commit `.aab`, `.apk`, `.p12`, `.jks`, `.keystore`, `.pem`, or
  `.password` files. `.gitignore` excludes them; do not weaken it.

## Preparing a Release

When the user asks for a release, follow the checklist in
[docs/releasing.md](docs/releasing.md#release-checklist). In short:

1. Branch from an up-to-date `main` and run `git fetch --tags`.
2. Confirm `version.properties` holds the next version (one code above the
   last release) and finish its `CHANGELOG.md` section.
3. Verify:

   ```bash
   ./gradlew -q :numbcrunch:printReleaseVersion
   ./gradlew --no-daemon :numbcrunch:lintRelease :gameplay:testDebugUnitTest :domain:test :numbcrunch:bundleRelease
   jarsigner -verify numbcrunch/build/outputs/bundle/release/numbcrunch-<name>-<code>-release.aab
   keytool -printcert -jarfile numbcrunch/build/outputs/bundle/release/numbcrunch-<name>-<code>-release.aab
   ```

   `printReleaseVersion` must report `uploadSigning=configured`. `jarsigner`
   must print `jar verified.`, and the SHA-256 printed by `keytool` must equal
   the fingerprint above. If signing is missing, stop and tell the user; do not
   work around it.
4. Report the bundle path, version, and checksum to the user.

Agents do **not** upload to Play Console, promote releases, or push tags. These
are the user's actions; give them the exact commands or steps from
[docs/releasing.md](docs/releasing.md) instead. Ask before archiving bundles to
`~/data/projects/vault/numbcrunch-release/releases/`.

## Environment Notes

- The project builds with JDK 17 or later. If the default `java` is too new for
  Gradle, use Android Studio's bundled runtime:
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- Worktrees lack `local.properties`; set `ANDROID_HOME=~/Library/Android/sdk`
  or create an untracked `local.properties` with `sdk.dir`.
