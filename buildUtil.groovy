/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2008 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import static org.junit.Assert.*

buildWar = { destWarFile ->
	def classesDir = new File("build/warClasses")
	classesDir.mkdirs()
	ant.javac(srcdir:'src', destdir:classesDir, debug:true) {
		classpath {
			fileset(dir:'lib') {
				include(name:'**/*.jar')
			}
			fileset(dir:'jetty') {
				include(name:'**/*.jar')
			}
		}
		include(name:"**/FlushCoberturaServlet.java")
	}
	ant.war(destfile:destWarFile, webxml:"src/net/sourceforge/cobertura/webapp/web.xml") {
		classes(dir:classesDir)
	}

}

 
/*
 * Makes a call to maven's deploy:deploy-file.
 * 
 * This assumes that Maven (currently 2.1.0) has been installed and is on the PATH
 */
def callMavenDeploy = { artifact ->
	//list of common arguments used in each call
	def deployArgs = [
                   "deploy:deploy-file",
                   "-Durl=${(new File(properties.'build.maven.repo')).toURL()}",
                   //"-DrepositoryId=cobertura-repo", //not needed unless you need to authenticate
                   "-DgroupId=net.sourceforge.cobertura",
                   ]
	ant.echo(message:'Deploying ' + artifact.file)
	def execResults = callAntExec(executable: 'mvn') {
		deployArgs.each {
			arg(value:it)
		}
		arg(value:"-DartifactId=${artifact.id}")
		if (artifact.pom) {
			arg(value:'-DpomFile=' + artifact.pom)
		} else {
			arg(value:'-Dversion=${version}')
			arg(value:'-Dclassifier=' + artifact.classifier)
			arg(value:'-Dpackaging=jar')
		}
		arg(value:"-Dfile=" + artifact.file)
	}
	assertNoErrors(execResults.outText)
	
	if (execResults.returnCode != 0) {
		ant.fail(message:"Deploy failed.   See output above.")
	}
}

 /**
  * Deploys the release artifacts to a local maven repo
  */
deployToLocalMavenRepo = {
		 
	//list of artifacts in the order they will be deployed
	def artifacts = [
	             //note that single quotes are used so the ${} will be passed to Ant
	             [classifier:'javadoc', file:'${build.bundle.source.dir}/${ant.project.name}-${version}-javadoc.jar'],
	             [classifier:'sources', file:'${build.bundle.source.dir}/${ant.project.name}-${version}-sources.jar'],

	             //must do this one last so our pom will be the last one put in the repo
	             [pom:'${build.bundle.source.dir}/pom.xml', file:'${build.bundle.source.dir}/${ant.project.name}-${version}.jar'],
	             ]
	
	def startingRepo = 'maven/repo'
	//use single quotes so the ${} will be passed unaltered to Ant
	def buildRepo = '${build.maven.repo}/net/sourceforge/cobertura'
	
	//clean the local repo if it is there
	ant.delete(dir:buildRepo, failonerror:false)
	ant.mkdir(dir:buildRepo)
	
	/*
	 * First, copy the maven-metadata.xml files of cobertura and cobertura-runtime
	 * into the local repo.
	 */
	def relativeCoberturaMetadata = '/cobertura/maven-metadata.xml'
	def relativeCoberturaRuntimeMetadata = '/cobertura-runtime/maven-metadata.xml'
	def startingCoberturaMetadata = startingRepo + relativeCoberturaMetadata
	def buildCoberturaMetadata = buildRepo + relativeCoberturaMetadata
	def startingCoberturaRuntimeMetadata = startingRepo + relativeCoberturaRuntimeMetadata
	def buildCoberturaRuntimeMetadata = buildRepo + relativeCoberturaRuntimeMetadata
	
	copyMetadataFile(fromFile:startingCoberturaMetadata , toFile:buildCoberturaMetadata)
	copyMetadataFile(fromFile:startingCoberturaRuntimeMetadata , toFile:buildCoberturaRuntimeMetadata)


	/*
	 * Now deploy all the cobertura artifacts
	 */
	artifacts.each { artifact ->
		artifact.id = 'cobertura'
		
		callMavenDeploy(artifact)
	}

	/*
	 * now deploy the runtime pom
	 */
	callMavenDeploy(
			id:'cobertura-runtime',
			pom:'${build.bundle.runtime.dir}/pom.xml', 
			file:'${build.bundle.runtime.dir}/pom.xml'
			)
	
	/*
	 * Now copy the updated metadata xml file to the "starting" repo.
	 */
	ant.copy(file:buildCoberturaMetadata, tofile:startingCoberturaMetadata)
	ant.copy(file:buildCoberturaRuntimeMetadata, tofile:startingCoberturaRuntimeMetadata)

	/*
	 * replace the version with @VERSION@ so the next time we deploy, the new
	 * version will be filled in.
	 */
	replaceVersionWithToken(startingCoberturaMetadata)
	replaceVersionWithToken(startingCoberturaRuntimeMetadata)
	
	/*
	 * Now zip up the local repo
	 */
	ant.zip(destfile:'cobertura-maven-${version}.zip') {
		fileset(dir:'${build.maven.repo}')
	}
	
	/*
	 * Now print out a message explaining how to get the release to the real repo.
	 */
	def message = """
Now copy to the maven repository.   That repository is automatically synched with the central maven repo.

Use the following command from the root of this project (with your username):

scp cobertura-maven-${properties.version}.zip username,cobertura@web.sourceforge.net:/home/groups/c/co/cobertura/htdocs/m2repo

Logon to sourceforge and unzip it.

MAKE SURE YOU MERGE THE CHANGES TO THESE FILES TO THE TRUNK:
${ant.project.resolveFile(startingCoberturaMetadata).getAbsolutePath()}
${ant.project.resolveFile(startingCoberturaRuntimeMetadata).getAbsolutePath()}
"""
	ant.echo(message:message)
	
}

/*
 * Makes a call to Ant's exec task printing out the output and returning
 * the returnCode and output.
 */
callAntExec = { parms, args ->
	def results = [returnCode:null]
	
	withExecParms { execParms ->
		execParms.putAll(parms)

		ant.exec(execParms, args)
		
		results.returnCode = properties.get(execParms.resultProperty.toString()).toInteger()
		results.outText = execParms.output.text
		results.errorText = execParms.error.text
		def message = """
return code:  ${results.returnCode}
stderr:
${results.errorText}
stdout:
${results.outText}
"""
		ant.echo(message:message)
	}
	return results
}

/*
 * Get a property that has not been set yet using the input suffix.
 * For suffix == 'mysuffix', return 'mysuffix0' if not used already.  Else,
 * return 'mysuffix1', etc.
 */
def getUnusedAntProperty = { suffix ->
	def ret = null
	for (int i = 0; ; i++) {
		ret = "$suffix$i"
		def value = properties.get(ret)
		if (value == null) {
			break
		}
	}
	assertNotNull(ret)
	assertNull(properties.get(ret))
	return ret
}

/*
 * Sets up typically needed parameters in any call to Ant's exec
 */
withExecParms = { closure ->
	def outputFile = File.createTempFile('coberturaAntExecOutput', 'txt')
	def errorFile = File.createTempFile('coberturaAntExecError', 'txt')
	def resultProperty = getUnusedAntProperty('coberturaAntExecResultProperty')
	
	try
	{
		closure.call(
				output:outputFile, 
				error:errorFile, 
				resultProperty:resultProperty,
				vmlauncher: false,
				failonerror:false,
				)
	} finally {
		outputFile.delete()
		outputFile.delete()
	}	
}
	

/*
 * Copies a file replacing all @VERSION@ tokens with the actual version.
 */
copyMetadataFile = { map ->
	ant.copy(file:map.fromFile, tofile:map.toFile, overwrite:true) {
		filterset {
			//@VERSION@ will be replaced with the version
			filter(token:'VERSION', value:'${version}')
		}
	}
}

/*
 * Looks for the first line that has <version>something</version> and replaces
 * it with <version>@VERSION@</version>.
 */
replaceVersionWithToken = { file ->
	file = ant.project.resolveFile(file)
	def text = file.text
	def pw = file.newPrintWriter()
	def pattern = ~/(^.*<version>).*(<\/version>.*$)/
	def foundVersion = false
	text.eachLine { line ->
		def matcher = pattern.matcher(line)
		if (!foundVersion && matcher.matches()) {
			pw.write(matcher.group(1) + "@VERSION@" + matcher.group(2) + "\n")
			foundVersion = true
		} else {
			pw.write(line + "\n")
		}
	}
	pw.close()
	assertTrue("<version> not found in $file", foundVersion)
}

/*
 * assert the text does not have the "[ERROR]" token.
 */
assertNoErrors = { text ->
	text.eachLine { line ->
		assertFalse("[ERROR] found in output", line.matches(".*\\[ERROR\\].*"))
	}
}