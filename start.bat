@echo off
title SideQuest - Avvio Applicazione
setlocal

set "ROOT_DIR=%~dp0"
if exist "%ROOT_DIR%backend\mvnw.cmd" (
    set "BACKEND_DIR=%ROOT_DIR%backend"
) else (
    set "BACKEND_DIR=%ROOT_DIR%backend\demo\demo"
)
set "FRONTEND_DIR=%ROOT_DIR%frontend"

echo ==========================================
echo   SideQuest - Avvio Back-end e Front-end
echo ==========================================

echo Avvio Back-end su http://localhost:8080 ...
start "SideQuest Back-end" cmd /k "cd /d "%BACKEND_DIR%" && mvnw.cmd spring-boot:run"

echo Avvio Front-end su http://localhost:5173 ...
start "SideQuest Front-end" cmd /k "cd /d "%FRONTEND_DIR%" && npm run dev"

echo.
echo Attendo qualche secondo e apro il browser...
timeout /t 8 /nobreak >nul
start http://localhost:5173

echo.
echo Applicazione avviata! Per fermarla chiudi le due finestre aperte.
pause