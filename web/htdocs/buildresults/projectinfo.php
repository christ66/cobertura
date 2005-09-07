<?php
	include("cc.inc.php");
	$cc_project = $HTTP_GET_VARS['project'];

	// Need better error checking!
	if (strpos($cc_project, "/") || strpos($cc_project, "\\"))
		$cc_project = null;

	cc_printPageHeader($cc_project, null, null, null);

	$builds = cc_getListOfBuilds($cc_project);
	$lastbuild = $builds[0];
	$buildInfo = new cc_LogParser();
	$buildInfo->parseFile("${buildlogs}/${cc_project}/${lastbuild}");
?>

<table width="98%" border="0" cellspacing="0" cellpadding="2" align="center">
	<tr><td>&nbsp;</td></tr>

	<tr><td class="header-title"><?php print($cc_project); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Build Started At:</span> <?php print(cc_getDateOfLogFile($lastbuild)); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Last Changed:</span> <?php print($buildInfo->modifications[count($buildInfo->modifications)-1]->date); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Last Log Entry:</span> <?php print($buildInfo->modifications[count($buildInfo->modifications)-1]->comment); ?></td></tr>

	<tr><td>&nbsp;</td></tr>
	<tr><td><hr /></td></tr>
	<tr><td>&nbsp;</td></tr>

<?php
	$totalBuilds = 0;
	$goodBuilds = 0;
	if (isset($builds)) {
		$totalBuilds = count($builds);
		foreach ($builds as $build) {
			if (cc_getlabelOfLogFile($build)) {
				$goodBuilds++;
			}
		}
	}

	println("<tr><td class=\"header-title\">Build Statistics</td></tr>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Build Attempts:</span> ${totalBuilds}</td></tr>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Broken Builds:</span> " . ($totalBuilds - $goodBuilds) . "</td></tr>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Successful Builds:</span> ${goodBuilds}</td></tr>");
?>

	<tr><td>&nbsp;</td></tr>

</table>

<?php cc_printPageFooter(); ?>
