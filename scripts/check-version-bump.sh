#!/usr/bin/env bash
# Version guard for the NumbCrunch app (:numbcrunch). See docs/releasing.md.
#
# Compares the working tree with the latest release tag (v*). If anything that
# can affect the published app changed since that tag, version.properties must
# hold a VERSION_CODE higher than the released one, a higher VERSION_NAME, and
# a matching CHANGELOG.md heading. Docs, CI, scripts, IDE files and the
# unpublished Guesser V1 app (app/) are exempt.
#
# Runs in CI on every pull request and before every :numbcrunch:bundleRelease.
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

read_prop() {
    sed -n "s/^$1=//p" | tr -d '[:space:]'
}

fail() {
    echo "version-guard: $*" >&2
    exit 1
}

latest_tag=$(git tag --list 'v[0-9]*' --sort=-v:refname | head -n 1)
if [[ -z "$latest_tag" ]]; then
    echo "version-guard: no release tags found; run 'git fetch --tags'. Skipping." >&2
    exit 0
fi

released_props=$(git show "$latest_tag:version.properties" 2>/dev/null) ||
    fail "release tag $latest_tag has no version.properties."
released_code=$(read_prop VERSION_CODE <<<"$released_props")
released_name=$(read_prop VERSION_NAME <<<"$released_props")
current_code=$(read_prop VERSION_CODE <version.properties)
current_name=$(read_prop VERSION_NAME <version.properties)

exempt='^(docs/|\.github/|scripts/|app/|design_assets/|\.idea/)|\.md$|^(LICENSE|\.gitignore|\.gitattributes|version\.properties)$'
app_changes=$({
    git diff --name-only "$latest_tag" --
    git ls-files --others --exclude-standard
} | sort -u | grep -Ev "$exempt" || true)

if ((current_code < released_code)); then
    fail "VERSION_CODE $current_code is lower than $released_code released as $latest_tag. Version codes never go down."
fi

if ((current_code == released_code)); then
    if [[ -n "$app_changes" ]]; then
        {
            echo "version-guard: app files changed since $latest_tag, but version.properties still holds"
            echo "the released version $released_name (versionCode $released_code). Changed files:"
            sed 's/^/  /' <<<"$app_changes" | head -n 20
            echo
            echo "Start the next version: set VERSION_CODE=$((released_code + 1)) and a higher VERSION_NAME"
            echo "in version.properties, and add a '## <name> (versionCode $((released_code + 1))) — unreleased'"
            echo "section to CHANGELOG.md. See docs/releasing.md#version-lifecycle."
        } >&2
        exit 1
    fi
    echo "version-guard: OK, no app changes since $latest_tag ($released_name, versionCode $released_code)."
    exit 0
fi

highest_name=$(printf '%s\n%s\n' "$released_name" "$current_name" | sort -V | tail -n 1)
if [[ "$current_name" == "$released_name" || "$highest_name" != "$current_name" ]]; then
    fail "VERSION_NAME $current_name must be higher than $released_name released as $latest_tag."
fi

if ! grep -q "^## $current_name (versionCode $current_code)" CHANGELOG.md; then
    fail "CHANGELOG.md needs a '## $current_name (versionCode $current_code) — unreleased' section."
fi

echo "version-guard: OK, next version $current_name (versionCode $current_code) after $latest_tag ($released_name, versionCode $released_code)."
