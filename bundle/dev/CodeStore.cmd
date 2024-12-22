@echo off

::start .\startCore.cmd
call .\runtime\bin\java.exe -Dspring.profiles.active=dev -jar .\client\CodeStoreClient.jar