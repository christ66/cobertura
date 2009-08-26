
This project (assuming this file is at the root of the project) will not be problem 
free until you run the maven build.  At a command line, go to the root of the project, and execute:

mvn clean test



PROCEDURE FOR MERGING NEW JAVANCSS VERSIONS INTO COBERTURA

I am using a procedure similar to the procedure recommended in Version Control with Subversion in 
the section called General Vendor Branch Management Procedure in Chapter 4 on Branching and Merging (p. 112). 
The book is online at http://svnbook.red-bean.com/.





First, checkout the version of javancss you want to merge with.  That may be a particular
revision of the trunk.  Here is an example of checking out revision 25 of the trunk to
a directory called javancssSource:

svn checkout https://svn.codehaus.org/javancss/trunk -r 25 javancssSource --depth=infinity --force

Or, more likely it will be a tagged version:

svn checkout https://svn.codehaus.org/javancss/tags/javancss-32.53 -r HEAD javancssSource --depth=infinity --force


Either way, it will be checked out to a directory called javancssSource.

Delete all .svn directories under javancssSource.

Next, I tried using the "svn import" command, but that was having problems.   I saw the following
message:

Adding         trunk\test\Test38.java
svn: Inconsistent line ending style

That file has some special characters in it.

So, I figured out another way.   First, create a directory under the vendor area.   Use a name
that is descriptive like "rev25" or "javancss-32.53".

svn mkdir https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53 -m "importing https://svn.codehaus.org/javancss/tags/javancss-32.53"

Now check that same directory out:

svn checkout https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53 coberturaJavancssSource

This checks it out to a directory called coberturaJavancssSource.   

Copy the contents of the javancssSource directory to coberturaJavancssSource.

At this point, we need to do a svn add for each file.   Before doing adds it will 
be most convenient if you edit your subversion config file.   
On unix this is at ~/.subversion/config.  
On windows it is at %USERPROFILE%\Application Data\Subversion\config.

Make sure enable-auto-props is set to yes.   Then, go to the [auto-props] section and add the following:

*.sh = svn:mime-type=text/plain;svn:eol-style=native;svn:executable
COPYING = svn:mime-type=text/plain;svn:eol-style=native
*.TXT = svn:mime-type=text/plain;svn:eol-style=native
*.txt = svn:mime-type=text/plain;svn:eol-style=native
*.java = svn:mime-type=text/plain;svn:eol-style=native
javancss = svn:mime-type=text/plain;svn:eol-style=native
*.bat = svn:mime-type=text/plain;svn:eol-style=native
*.html = svn:mime-type=text/plain;svn:eol-style=native
*.jhm = svn:mime-type=text/plain;svn:eol-style=native
*.svg = svn:mime-type=text/plain;svn:eol-style=native
*.hs = svn:mime-type=text/plain;svn:eol-style=native
*.dtd = svn:mime-type=text/plain;svn:eol-style=native
*.jj = svn:mime-type=text/plain;svn:eol-style=native
*.xsl = svn:mime-type=text/plain;svn:eol-style=native
*.apt = svn:mime-type=text/plain;svn:eol-style=native

Note that this file is used when you do "svn add" commands.   So, if you find out that javancss
has added a new extension and you have to add a line, you may have to delete
coberturaJavancssSource and start the procedure over again starting with the
checkout (mentioned above) that created coberturaJavancssSource.



Now that the config file is ready, try doing this:

cd coberturaJavancssSource
svn add *

If this works you can skip down to the section entitled COMMITTING TO THE VENDOR AREA.

If it does not work, it probably aborted with this message:

svn: File 'test\Test38.java' has inconsistent newlines
svn: Inconsistent line ending style

If so, create a file called cobertura.groovy in the coberturaJavancssSource directory.  Make sure the contents have this:

def ant = new AntBuilder()

new File(".").eachFileRecurse() { file ->
            
	if (file.absolutePath =~ /\.svn/) {
		//don't change svn metadata files
	} else {
		ant.exec(executable:'svn', vmlauncher:false) {
			arg(value:'add')
			arg(value:'--force')
			arg(value:'--non-recursive')
			arg(value:file.absolutePath)
		}
	}
}


Then, execute (you will need groovy installed):

groovy cobertura.groovy

Now, delete the cobertura.groovy file and execute:

svn delete cobertura.groovy

There are a few files that need to be set as binary files (SPECIAL BINARY FILES):

svn propset svn:mime-type application/octet-stream test/Test38.java
svn propset svn:mime-type application/octet-stream xslt/svg/javancss_out.html
svn propset svn:mime-type application/octet-stream test/TestEncoding.java




COMMITTING TO THE VENDOR AREA

Now, execute:

svn commit -m "importing https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53"


Now let's do a check.  Delete the coberturaJavancssSource directory and do the checkout again:

svn checkout https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53 coberturaJavancssSource

Compare the javancssSource and coberturaJavancssSource directories.   They should be the same - with maybe some
minor CRLF differences.

However, I one saw a difference where a java source file was checked in as a binary file (test/TestEncoding.java).
So, I went to coberturaJavancssSource and executed:

svn propset svn:mime-type application/octet-stream test/TestEncoding.java
svn propdel svn:eol-style test/TestEncoding.java

Then, I copied test/TestEncoding.java from javancssSource to coberturaJavancssSource and executed:

svn commit test/TestEncoding.java -m "This is a binary file in the javancss repository."

Then, I deleted coberturaJavancssSource, checked it out again, and compared 
with javancssSource again.

If you find any more cases like this make sure you add the file to the list 
above (search for SPECIAL BINARY FILES).



At this point, you can delete the javancssSource and coberturaJavancssSource 
directories if you want to.



Now, we need to checkout the javancss directory under the cobertura project (note we do not
want to checkout the whole cobertura project yet):

svn checkout https://svn.sourceforge.net/svnroot/cobertura/trunk/cobertura/javancss javancssInCobertura

The project will not be problem free until you run the maven build.  At a
command line, go to the root of the project, and execute:

cd javancssInCobertura
mvn clean test


Now do the merge.   The first url is the vendor version that you last merged with.  The second
url is the vendor version that you are merging with now.  Here is what it looks like when you last
merged with rev25, and you are merging with javancss-32.53 (assuming you are in the javancssInCobertura directory):

svn merge --depth=infinity https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/rev25@HEAD https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53@HEAD .


After the merge, you will want to pay attention to whether the lib/ccl.jar changed.  If it did then
be aware that you may have to change some of the files in cobertura/net/sourceforge/cobertura/javancss/ccl.
That package has stubs of the ccl classes used by cobertura.  More on this later.

Now, resolve any conflicts and do the maven build again:

mvn clean test


Assuming this is successful, you can commit.   But, first delete the target directory (I am having trouble
adding it to svn:ignore).

svn commit -m "Merge with https://svn.codehaus.org/javancss/tags/javancss-32.53"


Then, go back to the cobertura project and rename it.

Checkout the cobertura project.  Add the cobertura/javancss/coberturaREADME.txt.

run the javancss-merge target.   Fix any compile errors.

Run the coverage target to make sure the tests pass.

Commit the new cobertura javancss files.

