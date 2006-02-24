<?php
	print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	include("${projecthome}/htdocs-buildresults-inc/cc.inc.php");
	$project = $HTTP_GET_VARS['project'];
	$log = $HTTP_GET_VARS['log'];
	$file = $HTTP_GET_VARS['file'];

	if (isset($log) && !endsWith(".xml", $log))
		$log .= ".xml";

	// TODO: Add better error checking
	if (strpos($log, "/") || strpos($log, "\\"))
		$log = null;
	if (strpos($file, "/") || strpos($file, "\\"))
		$file = null;

	$title = cc_getPageTitle($project, $log, $file);
?>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">

<head profile="http://www.w3.org/2000/08/w3c-synd/#">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<link rel="stylesheet" type="text/css" href="StyleSheets/buildresults.css"/>
	<title>Build Results<?php if (isset($title)) { print(": $title"); } ?></title>
</head>

<body>



<div id="navigation">

<?php cc_printNavigation($project, $log, $file); ?>

<a href="http://sourceforge.net/"><img src="http://sflogo.sourceforge.net/sflogo.php?group_id=130558&amp;type=1" width="88" height="31" alt="SourceForge.net" /></a>

</div>



<div id="main">

<?php print(cc_getBreadcrumbs($project, $log, $file)); ?>

