<?php
	print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	include("/home/groups/c/co/cobertura/htdocs-buildresults-inc/cc.inc.php");
	$project = $HTTP_GET_VARS['project'];
	$build = $HTTP_GET_VARS['build'];
	$file = $HTTP_GET_VARS['file'];

	// TODO: Add better error checking
	if (strpos($file, "/") || strpos($file, "\\"))
		$file = null;

	$title = cc_getPageTitle($project, $build, $file);
?>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">

<head profile="http://www.w3.org/2000/08/w3c-synd/#">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<link rel="stylesheet" type="text/css" href="StyleSheets/buildresults.css"/>
	<title>Build Results<?php if (isset($title)) { print(": $title"); } ?></title>

<script type="text/javascript">
	function expandable(loc) {
		if (document.getElementById) {
			var foc=loc.firstChild.innerHTML?
				loc.firstChild:
				loc.firstChild.nextSibling;
			foc.innerHTML=foc.innerHTML == '+' ? '-' : '+';
			foc=loc.nextSibling.style?
				loc.nextSibling:
				loc.nextSibling.nextSibling;
			foc.style.display=foc.style.display=='block'?'none':'block';
		}
	}
</script>
</head>

<body>



<div id="navigation">

<?php cc_printNavigation($project, $build, $file); ?>

<a href="http://sourceforge.net/"><img src="http://sflogo.sourceforge.net/sflogo.php?group_id=130558&amp;type=1" width="88" height="31" alt="SourceForge.net Logo" /></a>

</div>



<div id="main">

<?php print(cc_getBreadcrumbs($project, $build, $file)); ?>

