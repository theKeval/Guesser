# Changelog

Releases of the NumbCrunch app (`:numbcrunch`) on Google Play, newest first.
Each entry records the version name, the version code Play consumed, and where
it shipped. The version comes from [`version.properties`](version.properties);
see [docs/releasing.md](docs/releasing.md) for the release process.

## 1.0.1 (versionCode 2) — 2026-10-12, internal testing

- No player-visible changes.
- Version is read from `version.properties`, and release bundles are signed
  with the upload key when the `NUMBCRUNCH_UPLOAD_*` properties are set.
- Bundles are named `numbcrunch-<versionName>-<versionCode>`.
- Built from commit `e3cb553` (tag `v1.0.1`).

## 1.0 (versionCode 1) — 2026-10-11, internal testing

- First NumbCrunch release: Single and Double Player modes, interactive
  tutorial, Gameplay rules guide, and About screen.
- Built from commit `1391373`.
