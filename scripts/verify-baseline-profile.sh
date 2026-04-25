#!/usr/bin/env bash
#
# Verifies that a built APK/AAB contains a baseline profile.
# Usage: scripts/verify-baseline-profile.sh <path-to-apk-or-aab>
#
# Exits non-zero if the profile artifacts (baseline.prof / baseline.profm)
# are missing — so it can be used as a post-build guard in CI or hooks.
#
set -euo pipefail

artifact="${1:?usage: $0 <path-to-apk-or-aab>}"

if [[ ! -f "$artifact" ]]; then
  echo "FAIL: $artifact not found" >&2
  exit 1
fi

contents="$(unzip -l "$artifact")"

if ! grep -qE 'assets/dexopt/baseline\.prof$' <<<"$contents"; then
  echo "FAIL: assets/dexopt/baseline.prof missing from $artifact" >&2
  echo "      Did ./gradlew :androidApp:generateReleaseBaselineProfile run?" >&2
  exit 2
fi

if ! grep -qE 'assets/dexopt/baseline\.profm$' <<<"$contents"; then
  echo "FAIL: assets/dexopt/baseline.profm missing from $artifact" >&2
  exit 3
fi

echo "OK: baseline profile present in $artifact"
