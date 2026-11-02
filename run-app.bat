@echo off
title Research Publication Verification & Management System
echo =====================================================================
echo Starting Research Publication Verification & Management Web System...
echo =====================================================================

set "JAVA_EXE=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\jbr\bin\java.exe"

if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)

echo Opening Web Browser at http://localhost:8080 ...
start "" "http://localhost:8080"

echo Running Spring Boot Web Server on Port 8080...
echo (Keep this window open while using the application)
"%JAVA_EXE%" -jar "%~dp0target\publication-verification-system-1.0.0.jar"
pause
