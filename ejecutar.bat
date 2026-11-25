@echo off
cd /d "%~dp0"
chcp 65001 >nul
if "%~1"=="" (
    java -Dstdout.encoding=UTF-8 -cp out juego.Main
) else (
    java -Dstdout.encoding=UTF-8 -cp out %1
)
