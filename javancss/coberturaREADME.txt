
This project (assuming this file is at the root of the project) will not be problem 
free until you run the maven build.  At a command line, go to the root of the project, and execute:

mvn clean test



PROCEDURE FOR MERGING NEW JAVANCSS VERSIONS INTO COBERTURA

Occasionally, the latest Javancss has to be merged into Cobertura.   This is actually not
too difficult to do even though the following instructions are pretty long.

I am using a procedure recommended in Version Control with Subversion in 
the section called General Vendor Branch Management Procedure (in Chapter 4 on 
Branching and Merging (p. 112)).  The book is online at http://svnbook.red-bean.com/. 

First let me explain what I did to set all of this up.   This is just to give you
some historical context.   You will not need to do the following steps.

I followed the procedure and created a vendor/javancss/current directory in the
repository:

https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss

Then, I copied it over into the cobertura project under a directory
called javancss.  This file (the one you are reading) is at the root of that
javancss directory.

I made the changes needed for Cobertura in the copy that is in the cobertura 
trunk under cobertura/javancss) (not the one under vendor).  A description of the
changes needed comes later.

So, when Javancss comes out with a new version, you will need to start by looking
at the vendor/javancss/current directory.   First, figure out what Javancss revision or
tag it is from.   Hopefully, you can see this in the svn history.   The last revision
comment should mention the Javancss url the files were checked out from.  Something like:

Revision 25 of https://svn.codehaus.org/javancss/trunk

or a tag version:

https://svn.codehaus.org/javancss/tags/javancss-32.53

If you can't tell from the history, you can try doing comparisons between "current"
and the version directories that are also at this location:

https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss

You can do the comparisons with commands like (with REVISION changed appropriately):

svn diff --summarize --depth=infinity https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current@HEAD https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/REVISION@HEAD

If the command exits without printing anything, they are the same.


If you don't see a directory under vendor/javancss that looks like the revision (ex. rev25)
or tag (ex. javancss-32.53), then execute (with REVISION changed appropriately):

svn copy https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/REVISION -m "tagging REVISION"

If instead a directory already exists, you can make sure they are the same with:

svn diff --summarize --depth=infinity https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current@HEAD https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/REVISION@HEAD

If the command exits without printing anything, they are the same.

If they are not the same, I'd be tempted to delete the existing one and do the above copy.
Otherwise, you will need to copy to another directory name.


Now, we need to check out the "current" directory.   Note that it is very important to use the current
directory because it is the "ancestor" of the copy that is under cobertura.   The merge that
is done later requires that to be true.  Execute:


svn checkout https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current javancssCurrent --depth=infinity --force

It will be checked out to a directory called javancssCurrent.

Now, checkout the version of javancss you want to merge with.  That may be a particular
revision of the trunk.  Here is an example of checking out revision 25 of the trunk to
a directory called javancssSource:

svn checkout https://svn.codehaus.org/javancss/trunk -r 25 javancssSource --depth=infinity --force

Or, more likely it will be a tagged version:

svn checkout https://svn.codehaus.org/javancss/tags/javancss-32.53 -r HEAD javancssSource --depth=infinity --force


Either way, it will be checked out to a directory called javancssSource.

Delete all .svn directories under javancssSource.

Now, copy over the files from javancssSource to javancssCurrent.  You can optionally
use a directory comparison tool to help remove any files that exist in javancssCurrent but
do not exist in javancssCurrent (ignore the .svn directories).   These are files that are now obsolete.


PREPARING FOR COMMIT OF THE CHANGES TO CURRENT


If the new version has new files or directories, we will need to add them or get an
IDE to do it.  Either way, before doing svn adds, it will 
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
javancssCurrent and start the procedure over again starting with the
checkout (mentioned above) that created javancssCurrent.


If you are using an IDE svn plugin, then you can probably skip to the 
section entitled COMMITTING TO CURRENT.

Now that the config file is ready, create a file called cobertura.groovy 
in the javancssCurrent directory.  Make sure the contents have this:

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

This will add the files one by one.

Now, delete the cobertura.groovy file and execute:

svn delete cobertura.groovy




COMMITTING TO CURRENT

Now, execute (making sure the url is where you did the checkout from javancss):

svn commit -m "Changes from https://svn.codehaus.org/javancss/tags/javancss-32.53"


Now let's do a check.  Delete the javancssCurrent directory and do the checkout again:

svn checkout https://cobertura.svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current javancssCurrent --depth=infinity --force

Delete the .svn directories from javancssCurrent.

Compare the javancssSource and javancssCurrent directories.   The directories should be the 
same - with maybe some minor CRLF differences.


However, you may see differences.  For example, when they added test/TestEncoding.java,
they added it as a binary file because it had unusual encoding.  So, to change the 
file to binary, I had to check out javancssCurrent again (I had deleted the .svn directories).
Then, I had to execute:

cd javancssCurrent
svn propset svn:mime-type application/octet-stream test/TestEncoding.java
svn propdel svn:eol-style test/TestEncoding.java

Then, I copied the test/TestEncoding.java file from javancssSource to javancssCurrent.

Then, commit it again:

svn commit test/TestEncoding.java -m "This is a binary file in the javancss repository."



Then, I deleted javancssCurrent, checked it out again, and compared 
with javancssSource again.


There may also be some files that were deleted.  You can delete the file, and execute:

svn delete the/file
svn commit the/file -m "the/file was deleted from Javancss version ???"



At this point, you can delete the javancssSource and javancssCurrent 
directories if you want to.


Now, go ahead and tag "current" with the revision (ex. rev25)
or tag (ex. javancss-32.53) by executing (with REVISION changed appropriately):

svn copy https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/current https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/REVISION -m "tagging REVISION"






Now, we need to checkout the javancss directory under the cobertura project (note we do not
want to checkout the whole cobertura project yet):

svn checkout https://svn.sourceforge.net/svnroot/cobertura/trunk/cobertura/javancss javancssInCobertura

The project will not be problem free until you run the maven build.  At a
command line, go to the root of the project, and execute:

cd javancssInCobertura
mvn clean test


Now to do the merge.   Before we go into details, let me mention that you will probably have to 
change the code especially if there are merge conflicts.   In general, we want to change the
code as little as possible so the build and tests will succeed.   So, try to not comment out any
lines.   Instead, use the following tags to designate code to be removed:

//COBERTURA REMOVE BEGIN
//COBERTURA REMOVE END

Cobertura uses a small amount of the functionality, so a lot is tagged this way.

When you make a change to the code, make sure "cobertura" is in a comment close by.   The merge
is a lot easier when the cobertura changes are marked this way.

I recommend using an IDE to do the merge.  If you do, you can skip to RESOLVING CONFLICTS.


Now I'll show the merge command.   The first url is the vendor version that you last merged with.  The second
url is the vendor version that you are merging with now.  Here is what it looks like when you last
merged with rev25, and you are merging with javancss-32.53 (assuming you are in the javancssInCobertura directory):

svn merge https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/rev25@HEAD https://svn.sourceforge.net/svnroot/cobertura/vendor/javancss/javancss-32.53@HEAD .


RESOLVING CONFLICTS

There may be some conflicts that need to be looked into.   Fortunately, 
there are not a lot of changes that Cobertura needs, so this will probably not be too
difficult to figure out.  Here is a list of the changes that were made to the Cobertura
version:

	- The package name is changed from javancss to net.sourceforge.cobertura.javancss.  This
	  change is not done manually, but there is javancss-merge target in the cobertura
	  build.xml file that is used.  It copies the files from 
	  cobertura/javancss/src/main/java/javancss to 
	  cobertura/src/net/sourceforge/cobertura/javancss.  It will change package and
	  import declarations to use the new package name.

	- The ccl.jar is not used.   Instead, the classes that are in the jar are stubbed and
	  put in the net.sourceforge.cobertura.javancss.ccl package.  The stubs work a lot like
	  the classes in ccl.jar.  One particular exception is the Util.sort().  It was changed
	  so it does not actually do a sort.   Sorting is apparently not needed.  
	  For new releases of Javancss, you will want to pay attention to whether the new 
	  lib/ccl.jar changed.  If it does, then you may need to change the stubbed classes.
	  At the time of this writing, I could not locate the source.  I am planning on getting
	  with the Javancss group about it.
	  
	- Some classes in javancss.test have also been stubbed out and moved to
	  net.sourceforge.cobertura.javancss.test.
	
	- A Javancss(InputStream isJavaSource_) constructor was added.  This is pretty much
	  a copy of Javancss(String).  See the constructor for more details.  It is cobertura's
	  entry point.

	- There is a lot of Javancss code that is not used.   Since cobertura does not use
	  the jars that Javancss typically uses, the unused code fails at compile time.  So,
	  care is taken to remove the code by putting comments in the code.   The script that
	  copies the source from the javancss directory into the main cobertura source directory
	  (see javancss-merge in cobertura/build.xml) looks for these comments.   If it sees
	  //COBERTURA EXCLUDE THIS FILE at the top, it will not copy the file.   If it sees
	  //COBERTURA REMOVE BEGIN, it will not copy over the lines until it sees
	  //COBERTURA REMOVE END.  So, generally, if you need to remove something from javancss,
	  do it with these comments.   Try to keep the uncommented code in javancss unmodified and
	  intact.   This way the javancss build will succeed.

Now, resolve any conflicts and make sure any change you do has a comment with the "cobertura"
in it.   It is much easier to do a merge with those comments.   You may also need to 
do the maven build to generate the source:

mvn clean test


Assuming this is successful, you can commit.

svn commit -m "Merge with https://svn.codehaus.org/javancss/tags/javancss-32.53"


Now checkout the whole cobertura project (you have previously been using only the
cobertura/javancss directory):

svn checkout https://cobertura.svn.sourceforge.net/svnroot/cobertura/trunk/cobertura cobertura

It will be checked out to a directory called cobertura.

Execute:

cd cobertura
ant javancss-merge

This will run the script that copies source from the javancss directory to the
main cobertura source directory (package net.sourceforge.cobertura.javancss).

Fix any compile errors that may now occur.   You may have to go back to the javancssInCobertura
project (where the merge was done) and have some code removed (using COBERTURA REMOVE comments
as mentioned above).

Run the coverage target to make sure the tests pass:

ant coverage

If they do, you can commit the new cobertura javancss files:

svn commit -m "Merge with https://svn.codehaus.org/javancss/tags/javancss-32.53"





