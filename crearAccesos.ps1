# Script para generar Accesos Directos SICA en el Escritorio (Portatil para cualquier usuario)
$WScriptShell = New-Object -ComObject WScript.Shell
$ProjectPath = $PSScriptRoot
if (-not $ProjectPath) { $ProjectPath = (Get-Location).Path }

# Deteccion dinamica y portatil de carpetas de Escritorio del usuario (Nativa y OneDrive)
$UserProfile = [System.Environment]::GetFolderPath('UserProfile')
$DesktopPaths = @(
    [System.Environment]::GetFolderPath('Desktop'),
    "$UserProfile\Desktop",
    "$UserProfile\OneDrive\Desktop"
) | Select-Object -Unique

foreach ($DesktopPath in $DesktopPaths) {
    if (Test-Path $DesktopPath) {
        # 1. Acceso Directo Aplicacion Administracion Swing
        $ShortcutApp = $WScriptShell.CreateShortcut("$DesktopPath\SICA Admin App.lnk")
        $ShortcutApp.TargetPath = "javaw.exe"
        $ShortcutApp.Arguments = "-jar `"$ProjectPath\target\sica.jar`""
        $ShortcutApp.WorkingDirectory = "$ProjectPath"
        $ShortcutApp.Description = "Sistema Integrado de Control de Acceso SICA"
        if (Test-Path "$ProjectPath\src\main\resources\img\sica_app.ico") {
            $ShortcutApp.IconLocation = "$ProjectPath\src\main\resources\img\sica_app.ico"
        }
        $ShortcutApp.Save()

        # 2. Acceso Directo Portal Web Autoservicio (LNK)
        $ShortcutWeb = $WScriptShell.CreateShortcut("$DesktopPath\SICA Portal Web.lnk")
        $ShortcutWeb.TargetPath = "explorer.exe"
        $ShortcutWeb.Arguments = "http://localhost:8080/portal"
        $ShortcutWeb.WorkingDirectory = "$ProjectPath"
        $ShortcutWeb.Description = "Portal Web de Autoservicio Biometrico SICA"
        if (Test-Path "$ProjectPath\src\main\resources\img\sica_web.ico") {
            $ShortcutWeb.IconLocation = "$ProjectPath\src\main\resources\img\sica_web.ico"
        }
        $ShortcutWeb.Save()

        # 3. Acceso Directo URL Nativo adicional (.url)
        $UrlContent = "[InternetShortcut]`r`nURL=http://localhost:8080/portal`r`nIDList=`r`nHotKey=0`r`nIconFile=$ProjectPath\src\main\resources\img\sica_web.ico`r`nIconIndex=0"
        Set-Content -Path "$DesktopPath\SICA Portal Web.url" -Value $UrlContent -Encoding ASCII

        Write-Host "Accesos Directos creados exitosamente en: $DesktopPath"
    }
}
