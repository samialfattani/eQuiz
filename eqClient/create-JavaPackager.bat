@echo off

rem winrar a -sfx eQuiz-Client-Installer -z"sfx.conf" eQuiz-client-v2.12.0.1.exe exam.ico lib
rem -iimgexam.bmp -iiconexam.ico
rem winrar a -sfx -iimgclogo1.bmp setup.exe c:\myfiles
rem -appclass"..\..\eqClient\src\main\resources\images\exam.bmp" ^

javapackager -createjar -v ^
    -appclass "frawla.equiz.client.Main"  ^
    -srcdir "build/classes"  ^
    -outfile "build/package.jar"  ^
    -classpath "build/distributions"

REM    "build/classes"

javapackager -deploy -v ^
    -native "installer" ^
   -title "eQuiz-Client-Title"   ^
   -vendor "Eng. Sami Alfattani" ^
   -name "eQuiz-installer" ^
   -description "This is a Desktop application for electronic quizes." ^
   -appclass "frawla.equiz.client.Main" ^
   -srcdir build/distributions ^
   -srcfiles "build/package.jar" ^
   -outdir "build/package" ^
   -outfile "eQuiz-installer-file"

pause