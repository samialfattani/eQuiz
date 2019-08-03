@echo off

REM Arguments are initial values that can be assigend 
REM to the application to start with.
REM examples:
REM     eqClient.bat -help
REM     eqClient.bat -h 192.168.1.105
REM     eqClient.bat -h 192.168.1.105
REM     eqClient.bat -h 192.168.1.105 -p 1500 -u "Sami Alfattani"
REM     eqClient.bat -h 10.4.10.100 -p 10000 -i SAM000 -u "Sami Alfattani"
REM     eqClient.bat -host 10.4.10.100 -port 
REM     eqClient.bat -user-id SAM000 -user-name "Sami Alfattani"

set CMD_LINE_ARGS=
if "x%~1" == "x" goto execute
    set CMD_LINE_ARGS=%*
:execute
    REM echo %CMD_LINE_ARGS%
    call equiz-client\bin\eqClient.bat %CMD_LINE_ARGS% 