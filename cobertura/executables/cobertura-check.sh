#!/bin/bash
COBERTURA_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $COBERTURA_HOME/coberturatura-2.1.1.jar:$COBERTURA_HOME/lib/asm-5.0.1.jar:$COBERTURA_HOME/lib/asm-analysis-5.0.1.jar:$COBERTURA_HOME/lib/asm-tree-5.0.1.jar:$COBERTURA_HOME/lib/asm-commons-5.0.1.jar:$COBERTURA_HOME/lib/asm-util-5.0.1.jar:$COBERTURA_HOME/lib/slf4j-api-1.7.5.jar:$COBERTURA_HOME/lib/logback-core-1.0.13.jar:$COBERTURA_HOME/lib/logback-classic-1.0.13.jar:$COBERTURA_HOME/lib/oro-2.0.8.jar net.sourceforge.cobertura.instrument.InstrumentMain $*
