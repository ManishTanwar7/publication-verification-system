@echo off
title Research Publication Verification & Duplicate Detection System
echo ==============================================================================
echo Starting Research Publication Verification & Duplicate Detection System (Java + MySQL)
echo ==============================================================================

set "JAVA_EXE=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\jbr\bin\java.exe"

if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)

echo Opening Browser at http://localhost:8081 ...
start "" "http://localhost:8081"

echo Running Server on Port 8081 connected to MySQL Database 'research_pub_db'...
"%JAVA_EXE%" -jar "%~dp0target\publication-system.jar"
pause
