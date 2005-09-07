<?php
	include("cc.inc.php");
	$cc_project = $HTTP_GET_VARS['project'];
	$cc_log = $HTTP_GET_VARS['log'];
	if (!isset($cc_log)) {
		$cc_log = cc_getMostRecentBuild($cc_project);
	}
	cc_printPageHeader($cc_project, $cc_log, null, null);

	$buildInfo = new cc_LogParser();
	$buildInfo->parseFile("${buildlogs}/${cc_project}/${cc_log}");
?>

<table width="98%" border="0" cellspacing="0" cellpadding="2" align="center">
	<tr><td>&nbsp;</td></tr>

	<tr><td class="header-title"><?php print($cc_project); ?> - <?php
		if ($buildnum = cc_getLabelOfLogFile($cc_log)) {
			print("BUILD COMPLETE - build." . $buildnum);
		} else {
			print("<span class=\"header-title-error\">FAILED!</span>");
		}
	?></td></tr>
	<tr><td class="header-data"><span class="header-label">Date of build:</span> <?php print(cc_getDateOfLogFile($cc_log)); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Time to build:</span> <?php print($buildInfo->compileTime); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Additional Files:</span></td></tr>
<?php
	println("<tr><td class=\"header-data\">");

	$builddate = substr($cc_log, 3, 14);
	$additionalfiles = listFilesInDirectory("${buildlogs}/${cc_project}/${builddate}");
	if (!isset($additionalfiles)) {
		print("&nbsp;&nbsp;<i>No additional files.</i>");
	} else {
		println("<ul>");
		foreach ($additionalfiles as $additionalfile) {
			println("<li><a class=\"link\" href=\"viewfile.php?project=${cc_project}&log=${cc_log}&file=${additionalfile}\">$additionalfile</a></li>");
		}
		println("</ul>");
	}

	println("</td></tr>");
?>

	<tr><td>&nbsp;</td></tr>

	<tr><td>
		<table width="100%" border="0" cellspacing="0" cellpadding="2" align="center">
			<tr><td colspan="5" class="table-sectionheader">Modifications since last build: (<?php print(count($buildInfo->modifications)); ?>)</td></tr>
			<tr>
				<th align="left" class="table-title">User</th>
				<th align="left" class="table-title">Type of Change</th>
				<th align="left" class="table-title">File Name</th>
				<th align="left" class="table-title">Date</th>
				<th align="left" class="table-title">Commit Message</th>
			</tr>
<?php
	$i = 0;
	foreach ($buildInfo->modifications as $modification) {
		if ($i & 1) print("\t\t\t\t<tr class=\"table-oddrow\">");
		else print("\t\t\t\t<tr class=\"table-evenrow\">");
		print("<td class=\"table-text\">$modification->user</td>");
		print("<td class=\"table-text\">$modification->action</td>");
		print("<td class=\"table-text\">$modification->filename, $modification->revision</td>");
		print("<td class=\"table-text\">$modification->date</td>");
		print("<td class=\"table-text\">$modification->comment</td>");
		println("</tr>");
		$i++;
	}
?>
		</table>
	</td></tr>

	<tr><td>&nbsp;</td></tr>

	<tr><td>
		<table width="100%" border="0" cellspacing="0" cellpadding="2" align="center">
			<tr><td class="table-sectionheader">Compile Messages:</td></tr>
			<?php
				if ($buildInfo->compileError != "") {
					print("<tr><td class=\"table-text-error\">$buildInfo->compileError</td></tr>");
				}
			?>
			<tr><td class="table-text"><pre>
<?php
	$haveOutput = false;
	foreach ($buildInfo->compileMessages as $compileMessage) {
		if ($compileMessage->priority != "debug") {
			if ($compileMessage->priority != "info") {
				print("$compileMessage->message\n");
				$haveOutput = true;
			}
		}
	}
	if (!$haveOutput)
	{
		print("(None)");
	}
?>
			</pre></td></tr>
		</table>
	</td></tr>

	<tr><td>&nbsp;</td></tr>

	<tr><td>
		<table width="100%" border="0" cellspacing="0" cellpadding="2" align="center">
			<tr><td colspan="5" class="table-sectionheader">Test Results:</td></tr>
			<tr>
				<th align="left" class="table-title">Name</th>
				<th align="left" class="table-title">Tests</th>
				<th align="left" class="table-title">Errors</th>
				<th align="left" class="table-title">Failures</th>
				<th align="left" class="table-title">Time (s)</th>
			</tr>
<?php
	foreach ($buildInfo->testsuites as $testSuite) {
		print("<tr>");
		print("<td class=\"table-text\"><b>$testSuite->name</b></td>");
		print("<td class=\"table-text\">$testSuite->tests</td>");
		print("<td class=\"table-text\">$testSuite->errors</td>");
		print("<td class=\"table-text\">$testSuite->failures</td>");
		print("<td class=\"table-text\">$testSuite->time</td>");
		println("</tr>");
		$i = 0;
		foreach ($testSuite->testcases as $testCase) {
			if ($i & 1) print("\t\t\t\t<tr class=\"table-oddrow\">");
			else print("\t\t\t\t<tr class=\"table-evenrow\">");
			print("<td colspan=\"4\" class=\"table-text\">$testCase->name");
			if ($testCase->errors)
				print(" <font class=\"table-text-error\">(ERROR!)</font>");
			else if ($testCase->failures)
				print(" <font class=\"table-text-failure\">(FAILURE!)</font>");
			else
				print(" <font class=\"table-text-success\">(PASSED!)</font>");
			print("</td>");
			print("<td class=\"table-text\">$testCase->time</td>");
			println("</tr>");
			$i++;
		}
		print("<tr><td colspan=\"5\">&nbsp;</td></tr>");
	}
?>
		</table>
	</td></tr>

	<tr><td>&nbsp;</td></tr>

</table>

<?php cc_printPageFooter(); ?>
