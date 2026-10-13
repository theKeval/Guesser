# Versioning and Releasing NumbCrunch

This guide covers versioning, signing, and publishing the NumbCrunch revamp
(`:numbcrunch`, application ID `io.keval.apps.numbcrunch`) to Google Play.
Guesser V1 (`:guesser_v1`) is not published and keeps its own fixed version in
`app/build.gradle`.

AI agents should also read the rules in the root [`AGENTS.md`](../AGENTS.md).

## Version Numbers

The version lives in one file, [`version.properties`](../version.properties):

```properties
VERSION_CODE=2
VERSION_NAME=1.0.1
```

`numbcrunch/build.gradle` reads it into `defaultConfig`, and the in-app About
screen shows `VERSION_NAME` at runtime. Do not set versions anywhere else.

| Field | Read by | Rule |
| --- | --- | --- |
| `VERSION_CODE` | Google Play and Android | Positive integer. Every upload must be **higher than any code ever uploaded** on any track. A code is consumed as soon as a bundle is uploaded, even if that release is discarded. Increment by exactly 1. |
| `VERSION_NAME` | Players (Play listing, About screen) | `MAJOR.MINOR.PATCH` semantic version. The build fails on any other format. |

Choosing `VERSION_NAME`:

- **PATCH** (`1.0.1` → `1.0.2`): bug fixes, copy fixes, small polish, and
  build or tooling changes.
- **MINOR** (`1.0.2` → `1.1.0`): new player-visible features or screens.
- **MAJOR** (`1.4.0` → `2.0.0`): a redesign or a change that breaks saved
  progress or established rules.

You never enter a version in Play Console. Play reads both values from the
uploaded bundle and pre-fills the release name, for example `2 (1.0.1)`.
Promoting a release between tracks reuses the same bundle and the same version.

Check the configured version and signing status at any time:

```bash
./gradlew -q :numbcrunch:printReleaseVersion
```

### When to Bump

Bump the version only in a dedicated release change, immediately before
building the bundle you will upload. Feature and fix pull requests do not touch
`version.properties`. Any number of merged pull requests ship together in the
next release.

## Signing

### Two Keys

Play App Signing uses two keys:

- **Upload key** (yours): signs every bundle you upload. Play rejects a bundle
  signed by any other key.
- **App signing key** (Google's): Play re-signs what players install. You never
  handle it.

**Use the same upload key for every upload, permanently.** Never generate a new
one. If it is lost or leaked, request an upload key reset in Play Console under
*Test and release → App integrity → App signing*; that takes several days.

### Where the Key Lives

The upload key is kept outside the repository, in
`~/data/projects/vault/numbcrunch-release/keys/`:

| File | Purpose |
| --- | --- |
| `numbcrunch-upload.p12` | Private PKCS12 keystore. **This is the file used for signing.** |
| `numbcrunch-upload.password` | Keystore password (also the key password). |
| `numbcrunch-upload-certificate.pem` | Public certificate, for fingerprint checks and key-reset requests only. |

- Key alias: `numbcrunch-upload`
- Certificate: `CN=NumbCrunch Upload`, RSA 3072, valid until 2054
- SHA-256 fingerprint:
  `9D:5C:30:17:D5:EB:63:34:3B:E5:DC:06:2B:99:D8:DB:FE:67:58:14:26:8D:60:A0:C4:6F:D5:78:73:FB:75:81`

This fingerprint must match *Upload key certificate* in Play Console under
*App integrity → App signing*. Keep an encrypted backup of the keystore and
password, for example in a password manager. `.gitignore` excludes keystores,
certificates, and password files; never commit them.

### One-Time Gradle Setup

Add the key paths to your user-level `~/.gradle/gradle.properties`. This file
is outside the repository and applies to every checkout and worktree:

```properties
NUMBCRUNCH_UPLOAD_STORE_FILE=~/data/projects/vault/numbcrunch-release/keys/numbcrunch-upload.p12
NUMBCRUNCH_UPLOAD_STORE_PASSWORD_FILE=~/data/projects/vault/numbcrunch-release/keys/numbcrunch-upload.password
```

Paths may be absolute or start with `~/`.

Supported properties. Each can also be an environment variable, for example a
CI secret:

| Property | Required | Default |
| --- | --- | --- |
| `NUMBCRUNCH_UPLOAD_STORE_FILE` | Yes | none |
| `NUMBCRUNCH_UPLOAD_STORE_PASSWORD_FILE` (preferred) or `NUMBCRUNCH_UPLOAD_STORE_PASSWORD` | Yes | none |
| `NUMBCRUNCH_UPLOAD_KEY_ALIAS` | No | `numbcrunch-upload` |
| `NUMBCRUNCH_UPLOAD_KEY_PASSWORD_FILE` (preferred) or `NUMBCRUNCH_UPLOAD_KEY_PASSWORD` | No | the store password |

**Keep passwords off the command line.** Never pass a password value as a `-P`
flag or an inline environment assignment: it is kept in shell history and is
visible in process listings and CI logs. Use the `*_PASSWORD_FILE` properties,
which take a path to a private file (`chmod 600`). Put the plain `*_PASSWORD`
properties only in `~/.gradle/gradle.properties` or a CI secret store. Paths
and the key alias are safe to pass as `-P` flags.

Confirm with `./gradlew -q :numbcrunch:printReleaseVersion`, which should report
`uploadSigning=configured`. Without these properties, release builds still
succeed but are **unsigned**, Gradle prints a warning, and Play will reject the
bundle.

### Signing From Android Studio Instead

*Build → Generate Signed App Bundle or APK → Android App Bundle*, module
`numbcrunch`:

- Key store path: `~/data/projects/vault/numbcrunch-release/keys/numbcrunch-upload.p12`
  (paste the path if the file picker greys out `.p12` files)
- Key store password and key password: the contents of
  `numbcrunch-upload.password`
- Key alias: `numbcrunch-upload`
- Build variant: `release`

## Release Checklist

Use this for every Play upload, starting from an up-to-date `main`.

1. **Check the last uploaded version code.** Use the top entry in
   [`CHANGELOG.md`](../CHANGELOG.md), and confirm in Play Console under
   *App bundle explorer*.
2. **Open a release branch and bump the version** in `version.properties`:
   `VERSION_CODE` + 1, and the appropriate `VERSION_NAME`.
3. **Update [`CHANGELOG.md`](../CHANGELOG.md)** with a section for the new
   version: its code, date, and player-visible changes. Keep the first bullets
   short enough to paste as Play release notes (500 characters per language).
4. **Verify:**

   ```bash
   ./gradlew -q :numbcrunch:printReleaseVersion
   ./gradlew --no-daemon :numbcrunch:lintRelease :gameplay:testDebugUnitTest :domain:test :numbcrunch:bundleRelease
   ```

5. **Confirm the bundle is signed by the upload key:**

   ```bash
   jarsigner -verify numbcrunch/build/outputs/bundle/release/numbcrunch-<name>-<code>-release.aab
   keytool -printcert -jarfile numbcrunch/build/outputs/bundle/release/numbcrunch-<name>-<code>-release.aab
   ```

   `jarsigner` must print `jar verified.`, and the SHA-256 must match the
   fingerprint above.
6. **Merge the release pull request**, then tag the merge commit on `main`:

   ```bash
   git tag -a v<name> -m "NumbCrunch <name> (versionCode <code>)"
   git push origin v<name>
   ```

   If the bundle was built before merging, rebuild it from the tagged commit so
   the upload exactly matches the tag.
7. **Archive the bundle** outside the repository:

   ```bash
   mkdir -p ~/data/projects/vault/numbcrunch-release/releases/<name>-<code>
   cp numbcrunch/build/outputs/bundle/release/numbcrunch-<name>-<code>-release.aab ~/data/projects/vault/numbcrunch-release/releases/<name>-<code>/
   (cd ~/data/projects/vault/numbcrunch-release/releases/<name>-<code> && shasum -a 256 *.aab > SHA256SUMS)
   ```

8. **Upload to internal testing** (next section), then install the build from
   the Play testing link on a real device and smoke-test it.
9. **Record the upload** in `CHANGELOG.md`: the date and track.

## Publishing in Play Console

### Internal Testing Update

1. *Test and release → Testing → Internal testing → Create new release*.
2. Upload the signed `.aab`. Play reads the version from the bundle.
3. Leave the pre-filled release name, paste release notes, then *Next*.
4. Resolve any errors, then *Save and publish* or *Start rollout*. Internal
   testing builds are usually available to testers within minutes.

### Promoting to Closed Testing or Production

Promote the tested release instead of uploading it again: open the release on
its track and choose *Promote release*, then pick the target track. Use a
staged rollout for production (for example 20%, then 100%) and watch Android
vitals for crashes and ANRs before widening it.

Before the first production release, Play requires:

- *Policy → App content*: privacy policy URL, Data safety, content rating,
  target audience, and ads declaration.
- *Grow → Store presence → Main store listing*: 512×512 icon, 1024×500
  feature graphic, phone screenshots, and descriptions.
- For personal developer accounts created after November 2023: a closed test
  with at least 12 opted-in testers for 14 consecutive days before production
  access can be requested.

Production releases are reviewed by Google, which can take from hours to
several days.

## Troubleshooting

| Problem | Cause and fix |
| --- | --- |
| *Version code N has already been used* | Increase `VERSION_CODE` past the highest code shown in *App bundle explorer* and rebuild. |
| *Your Android App Bundle is signed with the wrong key* | The bundle was not signed with `numbcrunch-upload.p12`. Check the fingerprint with `keytool -printcert -jarfile`. |
| *You uploaded an APK or App Bundle that was not signed* / `jarsigner` prints `no manifest.` | Upload signing is not configured; see *One-Time Gradle Setup*. |
| Build fails with `version.properties: ...` | Fix the reported value: a positive integer code and a `MAJOR.MINOR.PATCH` name. |
| About screen shows an old version | You installed an older build; reinstall from Play or rebuild. |
