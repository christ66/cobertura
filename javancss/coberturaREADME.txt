
Add doc here.

remove files from javancss that are not in the rev.

commit javancss and the supporting build files - but not the cobertura javancss files - and not this file.

make sure it can be checked out on its own.

add coberturaREADME.txt.

do a build with maven and add svn:ignore stuff after it.


do a merge with the checked out project.

once the tests are successful, commit.

Then, go back to the cobertura project and rename it.

Checkout the cobertura project.  Add the cobertura/javancss/coberturaREADME.txt.

run the javancss-merge target.   Fix any compile errors.

Run the coverage target to make sure the tests pass.

Commit the new cobertura javancss files.

