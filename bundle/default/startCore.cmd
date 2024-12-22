@echo off
if not exist "%userprofile%\CodeStore\core-api-url" (
    call .\runtime\bin\javaw.exe -jar .\core\CodeStoreCore.jar
)