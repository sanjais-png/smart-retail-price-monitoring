@echo off
setlocal enabledelayedexpansion
title Smart Retail Price Monitoring System Launcher

cls
echo =========================================================================
echo               SMART RETAIL PRICE MONITORING SYSTEM
echo =========================================================================
echo.
echo Initializing environment variables...
echo.

:: 1. Set JDK 17 Path
if exist "C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
    set "PATH=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin;%PATH%"
)

:: 2. Determine Maven executable path
set "MVN_CMD=mvn"
if exist "%~dp0maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=%~dp0maven\apache-maven-3.9.9\bin\mvn.cmd"
)

echo [1/2] Launching Spring Boot Backend Microservice on Port 8090...
start "Smart Retail Backend (Port 8090)" cmd /k "cd /d "%~dp0" && set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot" && set "PATH=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin;%%PATH%%" && "%MVN_CMD%" spring-boot:run"

echo [2/2] Launching React Desktop UI Application on Port 3000...
start "Smart Retail React UI (Port 3000)" cmd /k "cd /d "%~dp0frontend" && npm run dev -- --port 3000 --host"

echo.
echo Waiting for services to initialize...
ping 127.0.0.1 -n 6 >nul

echo Opening browser at http://localhost:3000...
start http://localhost:3000

echo.
echo =========================================================================
echo   System launched successfully!
echo   - Backend Service: http://localhost:8090
echo   - React Desktop UI: http://localhost:3000
echo   Keep the command windows open while using the app.
echo =========================================================================
echo.
pause
