@echo off
setlocal

set JAVA=java.exe
if "%1" == "-w" (
    set JAVA=javaw.exe
)

if not exist ".\data\core-api-url" (
    start .\runtime\bin\%JAVA% -Dspring.profiles.active=dev -jar .\core\CodeStoreCore.jar
)

endlocal