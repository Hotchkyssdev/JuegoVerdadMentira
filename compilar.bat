@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > fuentes.txt
javac --release 11 -encoding UTF-8 -d out @fuentes.txt
set resultado=%errorlevel%
del fuentes.txt
if not "%resultado%"=="0" exit /b %resultado%
robocopy src out /e /xf *.java /njh /njs /ndl /nfl /nc /ns >nul
echo Compilado en out\. Para jugar: ejecutar.bat
exit /b 0
