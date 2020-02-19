@echo off

REM   ..\..\eqClient\src\main\resources\images\splash-client.bmp
REM   ..\..\eqClient\src\main\resources\images\exam.ico 

set imgDir=..\..\eqClient\src\main\resources\images
set buildDir=D:\build\equiz-client-java12
set jreImagePath=%buildDir%\equiz-client

set filename="eQuiz-Client-Installer-with-jre15.exe"


del %buildDir%\%filename%

REM -ep1: this is to include the target only without its path.
rar a -m5 ^
   -sfx %buildDir%\%filename% ^
   -z"SFX-package.conf" ^
   -iicon%imgDir%\javafx-icon.png ^
   -iimg%imgDir%\exam.bmp ^
   -ep1 ^
   %jreImagePath% ^
   %buildDir%\eQuiz-Client.exe ^
   %buildDir%\exam.ico ^
   %buildDir%\splash-client.jpg

REM pause

REM %imgDir%\install.ico