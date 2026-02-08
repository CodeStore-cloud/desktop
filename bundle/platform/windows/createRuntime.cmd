@echo off

SET JDK_VERSION=21.0.2
SET JDK_URL=https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip
SET JAVAFX_VERSION=21.0.10
SET JAVAFX_URL=https://download2.gluonhq.com/openjfx/%JAVAFX_VERSION%/openjfx-%JAVAFX_VERSION%_windows-x64_bin-jmods.zip

SET TEMP_DIR=%LOCALAPPDATA%\Temp
SET JDK_PATH=%TEMP_DIR%\jdk-%JDK_VERSION%
SET JDK_JMODS=%JDK_PATH%\jmods
SET JAVAFX_JMODS=%TEMP_DIR%\javafx-jmods-%JAVAFX_VERSION%

:: ------------------------------------------------------------
:: Download OpenJDK (cached)
:: ------------------------------------------------------------
SET ZIP_FILE="%TEMP_DIR%\jdk-%JDK_VERSION%.zip"
if exist %JDK_PATH% (
    echo Using cached OpenJDK
) else (
    echo Downloading OpenJDK %JDK_VERSION% ...
    curl --output %ZIP_FILE% %JDK_URL%
    tar -xf %ZIP_FILE% --directory "%TEMP_DIR%"
    del %ZIP_FILE%
)

:: ------------------------------------------------------------
:: Download JavaFX (cached)
:: ------------------------------------------------------------
SET ZIP_FILE="%TEMP_DIR%\javafx-%JAVAFX_VERSION%.zip"
if exist %JAVAFX_JMODS% (
    echo Using cached JavaFX
) else (
    echo Downloading JavaFX %JAVAFX_VERSION% jmods ...
    curl --output %ZIP_FILE% %JAVAFX_URL%
    tar -xf %ZIP_FILE% --directory "%TEMP_DIR%"
    del %ZIP_FILE%
)

:: ------------------------------------------------------------
:: Build Runtime
:: ------------------------------------------------------------
echo Creating runtime ...

SET OUTPUT_PATH="target\application\runtime"
SET MODULE_PATH="%JDK_JMODS%;%JAVAFX_JMODS%"
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

if exist %OUTPUT_PATH% rmdir /s /q %OUTPUT_PATH%

%JDK_PATH%\bin\jlink.exe ^
--module-path %MODULE_PATH% ^
--add-modules %MODULES% ^
--include-locales en,de ^
--strip-debug ^
--no-header-files ^
--no-man-pages ^
--output %OUTPUT_PATH%
