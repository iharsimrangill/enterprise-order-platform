#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 git@github.com:<username>/enterprise-order-platform.git"
  exit 1
fi

git init
git branch -M main
git add .
git commit -m "chore: establish enterprise order platform foundation"
git remote add origin "$1"
git push -u origin main
