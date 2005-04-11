<?php
	include("cc.inc.php");
	$cc_project = $HTTP_GET_VARS['project'];
	$cc_build = $HTTP_GET_VARS['build'];
	$cc_file = $HTTP_GET_VARS['file'];
	cc_printPageHeader($cc_project, $cc_build, null, $cc_file);

	$buildInfo = new cc_LogParser();
	$buildInfo->parseFile("${buildlogs}/${cc_project}/${cc_build}");
?>

<table width="98%" border="0" cellspacing="0" cellpadding="2" align="center">
<?php
	println("<tr><td class=\"header-data\">");
	$builddate = substr($cc_build, 3, 14);
	include("${buildlogs}/${cc_project}/${builddate}/${cc_file}");
	println("</td></tr>");
?>
</table>

<?php cc_printPageFooter(); ?>
