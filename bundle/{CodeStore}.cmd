@echo off

call .\startCore.cmd
call .\runtime\bin\javaw.exe -jar .\client\CodeStoreClient.jar
