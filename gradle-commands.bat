:: gradle -b build-server.gralde cleaneclipse
:: gradle -b build-server.gralde eclipse

:: make jar files
:: gradle -b build-server.gralde samiJar
:: gradle -b build-client.gralde samiJar

:: create EXE file using Launch4j
::gradle -b build-server.gralde createExe
::gradle -b build-client.gralde createExe

::gradle -b build-server.gralde execute
::gradle -b build-client.gralde execute
pause