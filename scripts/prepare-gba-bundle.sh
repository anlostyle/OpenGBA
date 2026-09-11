#!/usr/bin/env bash
set -euo pipefail

root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
jni_dir="$root_dir/lemuroid-cores/bundled-cores/src/main/jniLibs"
mgba_name="libmgba_libretro_android.so"

test -d "$jni_dir"
test -f "$jni_dir/arm64-v8a/$mgba_name"

# Keep the one core and ABI supported by the dedicated GBA handheld.
find "$jni_dir" \( -type f -o -type l \) -name '*.so' ! -name "$mgba_name" -delete
find "$jni_dir" -mindepth 1 -maxdepth 1 -type d ! -name arm64-v8a -exec rm -rf {} +

remaining=$(find "$jni_dir" \( -type f -o -type l \) -name '*.so' -print)
test "$remaining" = "$jni_dir/arm64-v8a/$mgba_name"
echo "Prepared GBA-only core bundle: $remaining"
