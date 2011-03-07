@ECHO OFF
REM Example shell script to execute the !app.title!
REM The tool can be started using a graphical user interface (default) or
REM as a shell version (using -shell switch, e.g., for batch processing)
REM Use '!app.jar! -help' to get a list of available options

REM Make sure JAVA_HOME is set
cd /d %~dp0
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME
set JAVA=java
goto LAUNCH

:USE_JAVA_HOME
set JAVA="%JAVA_HOME%\bin\java"
goto LAUNCH

:LAUNCH
REM Uncomment the following line to use this script as execution wrapper, 
REM e.g., from external programs. Command line arguments are passed to the 
REM jar file.
REM %JAVA% -jar -Xms128m -Xmx768m !app.jar! %*

REM The following command executes the GUI version 
%JAVA% -jar -Xms128m -Xmx768m !app.jar!