Set WshShell = CreateObject("WScript.Shell")
Set http = CreateObject("MSXML2.ServerXMLHTTP")
On Error Resume Next
http.Open "HEAD", "http://localhost:8080/portal", False
http.Send
If Err.Number = 0 Then
    WshShell.Run "http://localhost:8080/portal"
Else
    MsgBox "La app no se ha iniciado." & vbCrLf & "Por favor, ejecuta 'SICA_App' primero.", 48, "SICA Portal"
End If
On Error GoTo 0
