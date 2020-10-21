@ECHO OFF

SET HOME=%~dp0

SET JDK=
IF EXIST "%JAVA_HOME%" SET JDK=%JAVA_HOME%

:check
SET JAVA_EXE=%JDK%\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=%JDK%\jre\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" (
  ECHO ERROR: cannot start QR Tranfer.
  ECHO No JDK found. Please validate either JDK_HOME or JAVA_HOME points to valid JDK installation.
  EXIT /B
)

SET JRE=%JDK%
IF EXIST "%JRE%\jre" SET JRE=%JDK%\jre

SET JVM_ARGS=-Dapp.home.dir=%HOME% -Dapp.log.dir=%HOME%log\system.log

:: ---------------------------------------------------------------------
:: Run the program.
:: ---------------------------------------------------------------------

java  %JVM_ARGS% -jar %HOME%\mypass-launcher-1.0-SNAPSHOT.jar

