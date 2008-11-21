import org.apache.tools.ant.BuildException;

coberturaNameSpace = 'antlib:net.sourceforge.cobertura.ant'

if (ant.project.getTaskDefinitions().keySet().contains('cobertura-instrument')) {
	instrumentTaskName = 'cobertura-instrument'
	reportTaskName = 'cobertura-report'
	mergeTaskName = 'cobertura-merge'
	checkTaskName = 'cobertura-check'
} else if (ant.project.getTaskDefinitions().keySet().contains("${coberturaNameSpace}:instrument" as String)) {
	/*
	 *  Antlib was used to load the ant tasks
	 */
	instrumentTaskName = "${coberturaNameSpace}:instrument"
	reportTaskName = "${coberturaNameSpace}:report"
	mergeTaskName = "${coberturaNameSpace}:merge"
	checkTaskName = "${coberturaNameSpace}:check"
} else {
	throw new BuildException("Must do taskdef to load Cobertura ant tasks")
}


