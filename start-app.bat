@echo off
title Smart Retail Price Monitoring App Launcher
echo ===================================================
echo   Starting Smart Retail Price Monitoring System
echo ===================================================
echo.

:: 1. Set Java 17 Home
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: 2. Start Spring Boot Backend Server on Port 8090
echo [1/2] Launching Spring Boot Backend Server (Port 8090)...
start "Spring Boot Backend (Port 8090)" cmd /k "maven\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run"

:: 3. Wait 5 seconds for backend to start
timeout /t 5 /nobreak >nul

:: 4. Start React Frontend Dev Server on Port 3000
echo [2/2] Launching Vite React Desktop UI (Port 3000)...
start "React Desktop App (Port 3000)" cmd /k "cd frontend && npm run dev -- --port 3000 --host"

:: 5. Open Web Browser
echo.
echo Application starting! Opening http://localhost:3000 in your browser...
timeout /t 3 /nobreak >nul
start http://localhost:3000

echo.
echo ===================================================
echo System is running! Keep the command windows open.
echo ===================================================
