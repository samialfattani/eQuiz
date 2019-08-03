@echo off

set imgDir=..\src\main\resources\images
set outFile=eQuiz-Client-Installer.exe
set buildDir=D:\build\equiz-client\launch4j

REM FOR /F "tokens=*" %a in ('git describe --tags') do SET gitVersion=%a
REM set gitVersion=(cmd /c git describe --tag)
REM powershell -Command "(gc %~dp0\sfx-package.conf) -replace '--version', 'ff' | Out-File -encoding ASCII %~dp0\myFile.txt"

del %buildDir%\%outFile% 
rar a -m5 ^
    -sfx %buildDir%\%outFile% ^
    -iicon%imgDir%\install.ico ^
    -iimg%imgDir%\exam.bmp ^
    -ep1 ^
    -z"%~dp0\SFX-package.conf" ^
    %buildDir%\lib ^
    %buildDir%\eQuiz-Client.exe ^
    %buildDir%\exam.ico ^
    %buildDir%\splash-client.jpg

REM -ep1: this is to include the target only without its path.

REM pause