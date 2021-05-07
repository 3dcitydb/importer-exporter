@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  @name@ start up script for Windows
@rem  This is simply a wrapper, launching the @cliName@.bat CLI
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set CLI_NAME=@cliName@.bat
set CLI_DIR=@cliDir@
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

start /min cmd /c ""%DIRNAME%%CLI_DIR%\%CLI_NAME%" gui"