Set WshShell = CreateObject("WScript.Shell")
' Use javaw to run the jar without a console window
WshShell.Run "javaw -jar target\sica.jar", 0, False
