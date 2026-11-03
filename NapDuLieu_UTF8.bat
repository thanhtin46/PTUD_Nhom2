@echo off
chcp 65001 >nul
echo ===================================================
echo     HE THONG QUAN LY GARA O TO - THE T4 AUTO
echo   NAP DU LIEU CHUAN TIENG VIET (UTF-8) VAO CSDL
echo ===================================================
echo.

set SERVER_NAME=localhost
if not "%1"=="" set SERVER_NAME=%1

echo Dang nap du lieu vao SQL Server [%SERVER_NAME%]...
sqlcmd -S %SERVER_NAME% -d master -i "%~dp0QuanLySuaChuaXe.sql" -f 65001 -C

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo   NAP DU LIEU THANH CONG 100%% TIENG VIET CHUAN!
    echo ===================================================
) else (
    echo.
    echo Thu lai voi SQL Server Instance SQLEXPRESS...
    sqlcmd -S %SERVER_NAME%\SQLEXPRESS -d master -i "%~dp0QuanLySuaChuaXe.sql" -f 65001 -C
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ===================================================
        echo   NAP DU LIEU THANH CONG 100%% TIENG VIET CHUAN!
        echo ===================================================
    ) else (
        echo.
        echo Co loi xay ra khi chay sqlcmd. Vui long kiem tra lai SQL Server!
    )
)

echo.
pause
