@echo off
if not exist "%userprofile%\CodeStore\core-api-url" (
    start .\runtime\bin\javaw.exe -jar .\core\CodeStoreCore.jar
)