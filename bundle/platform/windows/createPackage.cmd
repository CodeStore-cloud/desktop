@echo off

if "%2" == "" (
    echo "No path to Inno Setup Compiler specified. Skip bundling executable."
) else (
    "%2\ISCC.exe" platform\windows\windows-bundle-config.iss
)