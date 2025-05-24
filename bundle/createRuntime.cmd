@echo off

SET JDK_PATH="%1"
SET JAVAFX_PATH="%2"
SET OUTPUT_PATH="target\application\runtime"
SET MODULE_PATH="%JDK_PATH%\jmods;%JAVAFX_PATH%"
SET MODULES=jdk.localedata,^
java.logging,^
java.net.http,^
java.management,^
java.naming,^
java.security.jgss,^
java.instrument,^
javafx.graphics,^
javafx.controls,^
javafx.fxml,^
javafx.web

if exist "%JAVA_HOME%\bin\jlink.exe" (
    SET JLINK_EXE="%JAVA_HOME%\bin\jlink.exe"
) else (
    SET JLINK_EXE="%JAVA_HOME%\bin\jlink"
)

if exist %OUTPUT_PATH% rmdir /s /q %OUTPUT_PATH%

%JLINK_EXE% ^
--module-path %MODULE_PATH% ^
--add-modules %MODULES% ^
--include-locales en,de ^
--strip-debug ^
--no-header-files ^
--no-man-pages ^
--output %OUTPUT_PATH%
