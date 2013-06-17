@echo off

REM
REM Grab the directory where this script resides, for use later
REM
set COBERTURA_HOME=%~dp0

REM
REM Read all parameters into a single variable using an ugly loop
REM
set CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:getArgs
if ""%1""=="""" goto doneStart
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto getArgs
:doneStart

java -cp "%COBERTURA_HOME%cobertura.jar;%COBERTURA_HOME%lib\asm-3.3.1.jar;%COBERTURA_HOME%lib\asm-tree-3.3.1.jar;%COBERTURA_HOME%lib\asm-commons-3.3.1.jar;%COBERTURA_HOME%lib\log4j-1.2.9.jar;%COBERTURA_HOME%lib\jakarta-oro-2.0.8.jar" net.sourceforge.cobertura.reporting.Main %CMD_LINE_ARGS%
