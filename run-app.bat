@echo off
title Research Publication Verification & Duplicate Detection System
echo ==============================================================================
echo Starting Research Publication Verification & Duplicate Detection System (Java + MySQL)
echo ==============================================================================

set "JAVA_EXE=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\jbr\bin\java.exe"

if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)

echo Opening Browser at http://localhost:8080 ...
start "" "http://localhost:8080"

echo Running Server on Port 8080 connected to MySQL Database 'research_pub_db'...
"%JAVA_EXE%" -jar "%~dp0target\publication-system.jar" ^
  --server.port=8080 ^
  --spring.datasource.url="jdbc:mysql://localhost:3306/research_pub_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" ^
  --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver ^
  --spring.datasource.username=root ^
  --spring.datasource.password=Manish07@
pause
