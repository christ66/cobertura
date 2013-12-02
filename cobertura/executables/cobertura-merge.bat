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

java -cp "%COBERTURA_HOME%cobertura-${project.version}.jar;%COBERTURA_HOME%lib\asm-${asmVersion}.jar;%COBERTURA_HOME%lib\asm-tree-${asmVersion}.jar;%COBERTURA_HOME%lib\asm-commons-${asmVersion}.jar;%COBERTURA_HOME%lib\slf4j-api-${slf4jVersion}.jar;%COBERTURA_HOME%lib\oro-${oroVersion}.jar" net.sourceforge.cobertura.merge.MergeMain %CMD_LINE_ARGS%
