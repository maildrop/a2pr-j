@echo off
@rem
@rem
@rem

setlocal

if exist "%~dp0\a2pr-1.0-SNAPSHOT.jar" set jarfile="%~dp0\a2pr-1.0-SNAPSHOT.jar"
if exist "%~dp0\a2pr-1.0-SNAPSHOT-jar-with-dependencies.jar" set jarfile="%~dp0\a2pr-1.0-SNAPSHOT-jar-with-dependencies.jar"

if DEFINED jarfile (java -jar "%~dp0\a2pr-1.0-SNAPSHOT-jar-with-dependencies.jar" "%1" "%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9") else (echo file not found "a2pr-1.0-SNAPSHOT.jar" or "a2pr-1.0-SNAPSHOT-jar-with-dependencies.jar" )

endlocal
