#!/bin/bash
COBERTURA_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $COBERTURA_HOME/cobertura-${project.version}.jar:$COBERTURA_HOME/lib/asm-${asmVersion}.jar:$COBERTURA_HOME/lib/asm-analysis-${asmVersion}.jar:$COBERTURA_HOME/lib/asm-tree-${asmVersion}.jar:$COBERTURA_HOME/lib/asm-commons-${asmVersion}.jar:$COBERTURA_HOME/lib/asm-util-${asmVersion}.jar:$COBERTURA_HOME/lib/slf4j-api-${slf4jVersion}.jar:$COBERTURA_HOME/lib/logback-core-${logbackVersion}.jar:$COBERTURA_HOME/lib/logback-classic-${logbackVersion}.jar:$COBERTURA_HOME/lib/oro-${oroVersion}.jar net.sourceforge.cobertura.instrument.InstrumentMain $*
