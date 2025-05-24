@echo off
setlocal

set JAVA=java.exe
if "%1" == "-w" (
    set JAVA=javaw.exe
)

start .\startCore.cmd %1
start .\runtime\bin\%JAVA% -Dspring.profiles.active=dev -jar .\client\CodeStoreClient.jar

endlocal