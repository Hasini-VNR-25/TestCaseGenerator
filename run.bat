@echo off
setlocal
echo ========================================================
echo   Starting Intelligent Java Test Case Generator
echo ========================================================

cd /d "%~dp0"

REM 1. If pre-built JAR exists in target or current directory, run directly with Java
if exist "target\cbp-testcase-generator-1.0.0.jar" (
    echo Found packaged JAR in target folder.
    echo Open your browser at: http://localhost:8080
    echo.
    java -jar "target\cbp-testcase-generator-1.0.0.jar"
    goto end
)
if exist "cbp-testcase-generator-1.0.0.jar" (
    echo Found packaged JAR in current folder.
    echo Open your browser at: http://localhost:8080
    echo.
    java -jar "cbp-testcase-generator-1.0.0.jar"
    goto end
)

REM 2. Otherwise run using Maven wrapper or installed Maven
if exist "mvnw.cmd" (
    set "MVN_CMD=mvnw.cmd"
) else if exist "%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    set "MVN_CMD=mvn"
)

echo Running Spring Boot Application via Maven (%MVN_CMD%)...
echo Open your browser at: http://localhost:8080
echo.

call "%MVN_CMD%" spring-boot:run

:end
pause
