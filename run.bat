@echo off
setlocal
set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

set "JAVA_VERSION_FILE=%TEMP%\currency-parser-java-version-%RANDOM%.txt"
"%JAVA_EXE%" -version >"%JAVA_VERSION_FILE%" 2>&1
if errorlevel 1 (
  del /q "%JAVA_VERSION_FILE%" >nul 2>&1
  echo Java 17 or newer is required. Install JDK 17 and run this file again.
  exit /b 1
)
for /f "tokens=3" %%v in ('findstr /i "version" "%JAVA_VERSION_FILE%"') do set JAVA_VERSION=%%v
del /q "%JAVA_VERSION_FILE%" >nul 2>&1
if not defined JAVA_VERSION (
  echo Could not determine the Java version. Check the selected Java executable.
  exit /b 1
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "tokens=1 delims=." %%v in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%v
if %JAVA_MAJOR% LSS 17 (
  echo Java 17 or newer is required. Current Java version: %JAVA_VERSION%
  exit /b 1
)
set "APP_JAR=%~dp0release\currency-rate-parser-1.0.0.jar"
if exist "%~dp0target\currency-rate-parser-1.0.0.jar" set "APP_JAR=%~dp0target\currency-rate-parser-1.0.0.jar"
"%JAVA_EXE%" -jar "%APP_JAR%"
