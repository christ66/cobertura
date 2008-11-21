

//load testUtil definitions
evaluate(new File("${ant.project.baseDir}/../../testUtil.groovy"))

runReports = { set ->

	
	ant."${reportTaskName}"(datafile:'${basedir}/cobertura.ser', srcdir:'${src.dir}', destdir:'${coverage.xml.dir}', format:'xml')

	ant.mkdir(dir:'${coverage.html.dir}')
	
	// maxmemory is only specified to test the attribute
	ant."${reportTaskName}"([datafile:'${basedir}/cobertura.ser', destdir:'${coverage.html.dir}', maxmemory:'512M'], set)

}