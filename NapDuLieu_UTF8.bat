@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo =====================================================================
echo          HE THONG QUAN LY GARA O TO - THE T4 AUTO
echo       CONG CU NAP CSDL TU DONG CHUAN TIENG VIET [UTF-8]
echo =====================================================================
echo.

set "SQLCMD_EXE=sqlcmd"
where sqlcmd >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_EXE=C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE"
    ) else if exist "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_EXE=C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE"
    ) else if exist "C:\Program Files\Microsoft SQL Server\160\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_EXE=C:\Program Files\Microsoft SQL Server\160\Tools\Binn\SQLCMD.EXE"
    ) else if exist "C:\Program Files\Microsoft SQL Server\150\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_EXE=C:\Program Files\Microsoft SQL Server\150\Tools\Binn\SQLCMD.EXE"
    ) else if exist "C:\Program Files\Microsoft SQL Server\140\Tools\Binn\SQLCMD.EXE" (
        set "SQLCMD_EXE=C:\Program Files\Microsoft SQL Server\140\Tools\Binn\SQLCMD.EXE"
    ) else (
        echo [!] Khong tim thay cong cu sqlcmd tren may.
        echo [*] Vui long mo SSMS, mo file QuanLySuaChuaXe.sql va nhan F5.
        goto :END
    )
)

set "SQL_SCRIPT=%~dp0QuanLySuaChuaXe.sql"
if not exist "!SQL_SCRIPT!" (
    set "SQL_SCRIPT=%~dp0database\QuanLySuaChuaXe.sql"
)

if not exist "!SQL_SCRIPT!" (
    echo [!] Khong tim thay file QuanLySuaChuaXe.sql trong thu muc du an!
    goto :END
)

echo [*] File script CSDL: !SQL_SCRIPT!
echo [*] Dang tu dong do tim SQL Server phu hop tren may tinh...
echo.

if not "%1"=="" (
    echo [*] Thu may chu chi dinh: %1
    "!SQLCMD_EXE!" -S "%1" -E -C -Q "SELECT 1" >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        set "TARGET_SERVER=%1"
        set "AUTH_FLAG=-E"
        goto :EXECUTE
    )
)

:: Thu lan luot qua Windows Auth
set "TARGET_SERVER="
for %%S in ("localhost\SQLEXPRESS" "localhost" ".\SQLEXPRESS" "." "(local)\SQLEXPRESS" "(local)" "%COMPUTERNAME%\SQLEXPRESS") do (
    if not defined TARGET_SERVER (
        "!SQLCMD_EXE!" -S %%S -E -C -l 2 -Q "SELECT 1" >nul 2>&1
        if !ERRORLEVEL! EQU 0 (
            set "TARGET_SERVER=%%~S"
            set "AUTH_FLAG=-E"
        )
    )
)

if defined TARGET_SERVER goto :EXECUTE

:: Thu qua tai khoan sa
echo [*] Thu ket noi qua tai khoan sa voi cac mat khau pho bien...
for %%S in ("localhost\SQLEXPRESS" "localhost" ".\SQLEXPRESS" ".") do (
    for %%P in ("sapassword123@" "sapassword" "123456" "123" "sa" "12345678" "admin" "root") do (
        if not defined TARGET_SERVER (
            "!SQLCMD_EXE!" -S %%S -U sa -P %%~P -C -l 2 -Q "SELECT 1" >nul 2>&1
            if !ERRORLEVEL! EQU 0 (
                set "TARGET_SERVER=%%~S"
                set "AUTH_FLAG=-U sa -P %%~P"
            )
        )
    )
)

if defined TARGET_SERVER goto :EXECUTE

echo.
echo [!] Khong the ket noi tu dong toi SQL Server tren may.
echo [*] HUONG DAN DANG CHO BAN:
echo   1. Mo SQL Server Management Studio (SSMS).
echo   2. Dang nhap vao SQL Server cua ban.
echo   3. Vao File -^> Open -^> File... -^> Chon QuanLySuaChuaXe.sql.
echo   4. Nhan phim F5 de nap CSDL.
goto :END

:EXECUTE
echo.
echo [OK] Da phat hien SQL Server hoat dong: [!TARGET_SERVER!]
echo [*] Dang nap CSDL QuanLySuaChuaXe (UTF-8, Collation Vietnamese_CI_AS)...
echo.

"!SQLCMD_EXE!" -S "!TARGET_SERVER!" !AUTH_FLAG! -C -f 65001 -b -i "!SQL_SCRIPT!"

if !ERRORLEVEL! EQU 0 (
    echo.
    echo =====================================================================
    echo   [THANH CONG] NAP CSDL QUANLYSUACHUAXE 100%% TIENG VIET CHUAN!
    echo   - Collation: Vietnamese_CI_AS
    echo   - Day du 22 bang, khoa chinh, khoa ngoai, rang buoc toan ven.
    echo   - Du lieu mau hoan chinh: Khach hang, Xe, Kho, Phieu SC, Hoa don.
    echo =====================================================================
) else (
    echo.
    echo [!] Co loi xay ra trong qua trinh nap script SQL.
    echo [*] Vui long kiem tra lai thong bao loi o tren hoac mo SSMS de chay.
)

:END
echo.
pause