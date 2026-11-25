#!/bin/sh
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir out
javac --release 11 -encoding UTF-8 -d out $(find src -name "*.java")
(cd src && find . -type f ! -name "*.java" -exec sh -c 'mkdir -p "../out/$(dirname "$1")" && cp "$1" "../out/$1"' _ {} \;)
echo "Compilado en out/. Para jugar: ./ejecutar.sh"
