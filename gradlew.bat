@echo off
SET DIRNAME=%~dp0
IF EXIST "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
  java -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
  goto :eof
)
gradle %*
