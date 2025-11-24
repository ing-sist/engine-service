#!/usr/bin/env bash
set -e

git config core.hooksPath .githooks
chmod +x .githooks/* || true

echo "Hooks configurados en .githooks/"
git config --get core.hooksPath