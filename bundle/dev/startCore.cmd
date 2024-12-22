@echo off

if not exist ".\data\core-api-url" (
    call .\runtime\bin\java.exe -Dspring.profiles.active=dev -jar .\core\CodeStoreCore.jar
)