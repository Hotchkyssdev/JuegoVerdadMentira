#!/bin/sh
cd "$(dirname "$0")"
java -Dstdout.encoding=UTF-8 -cp out "${1:-juego.Main}"
