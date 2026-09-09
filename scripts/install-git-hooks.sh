#!/bin/sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
HOOKS_DIR="$ROOT_DIR/.git/hooks"
SOURCE_HOOK="$ROOT_DIR/scripts/hooks/commit-msg"
TARGET_HOOK="$HOOKS_DIR/commit-msg"

if [ ! -d "$ROOT_DIR/.git" ]; then
    echo "Error: .git directory not found. Run this script from a cloned repository." >&2
    exit 1
fi

mkdir -p "$HOOKS_DIR"
cp "$SOURCE_HOOK" "$TARGET_HOOK"
chmod +x "$TARGET_HOOK"

echo "Installed commit-msg hook:"
echo "  $TARGET_HOOK"
echo "Cursor co-author trailers will be removed automatically before each commit."
