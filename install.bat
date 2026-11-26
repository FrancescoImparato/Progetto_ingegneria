@echo off
title SideQuest - Installazione
setlocal

set "ROOT_DIR=%~dp0"
if exist "%ROOT_DIR%backend\mvnw.cmd" (
    set "BACKEND_DIR=%ROOT_DIR%backend"
) else (
    set "BACKEND_DIR=%ROOT_DIR%backend\demo\demo"
)
set "FRONTEND_DIR=%ROOT_DIR%frontend"

echo ==========================================
echo   SideQuest - Installazione dipendenze
echo ==========================================

echo.
echo [1/2] Verifica e compilazione Back-end (Maven)...
cd /d "%BACKEND_DIR%"
call mvnw.cmd clean test-compile
if errorlevel 1 (
    echo [ERRORE] Compilazione Back-end fallita!
    pause
    exit /b 1
)

echo.
echo [2/2] Installazione dipendenze Front-end (npm)...
cd /d "%FRONTEND_DIR%"
call npm install
if errorlevel 1 (
    echo [ERRORE] Installazione Front-end fallita!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   Installazione completata con successo!
echo   Ora puoi avviare il progetto con start.bat
echo ==========================================
pause