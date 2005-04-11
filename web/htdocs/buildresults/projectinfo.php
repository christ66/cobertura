<?php
	include("cc.inc.php");
	$cc_project = $HTTP_GET_VARS['project'];
	cc_printPageHeader($cc_project, null, null, null);

	$builds = cc_getListOfBuilds($cc_project);
	$lastbuild = $builds[0];
	$buildInfo = new cc_LogParser();
	$buildInfo->parseFile("${buildlogs}/${cc_project}/${lastbuild}");
?>

<table width="98%" border="0" cellspacing="0" cellpadding="2" align="center">
	<tr><td>&nbsp;</td></tr>

	<tr><td class="header-title"><?php print($cc_project); ?></td></tr>
	<tr><td class="header-data"><span class="header-label">Next Build At:</span> </td></tr>
	<tr><td class="header-data"><span class="header-label">Last Build At:</span> <?php print(cc_getDateOfLogFile($lastbuild)); ?></td></tr>
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
	println("<tr><td>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Build Attempts:</span> ${totalBuilds}</td></tr>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Broken Builds:</span> " . ($totalBuilds - $goodBuilds) . "</td></tr>");
	println("<tr><td class=\"header-data\"><span class=\"header-label\">Number of Successful Builds:</span> ${goodBuilds}</td></tr>");
	println("</td></tr>");
?>
<!--
	<tr><td><img border="1" height="300" width="400" src="images/piechart.png"></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><img border="1" height="300" width="400" src="images/lineplot.png"></td></tr>

	<tr><td>&nbsp;</td></tr>
	<tr><td><hr /></td></tr>
	<tr><td>&nbsp;</td></tr>

	<tr><td class="header-title">Testing Statistics</td></tr>
	<tr>
		<td class="header-data"><span class="header-label">
			<table border="1" width="98%">
				<tr><th colspan="5">Last Test Run</th></tr>
				<tr><th>Host</th><th>Tests Run</th><th>Tests Passed</th><th>Tests Failed</th><th>Percent Passed</th></tr>
				<tr><td>linux</td><td>23</td><td>23</td><td>4</td><td>100%</td></tr>
				<tr><td>s64</td><td>23</td><td>23</td><td>4</td><td>100%</td></tr>
				<tr><td>win32</td><td>23</td><td>23</td><td>0</td><td>100%</td></tr>
			</table>
		</td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr>
		<td class="header-data"><span class="header-label">
			<table border="1" width="98%">
				<tr><th colspan="5">Past 7 Days</th></tr>
				<tr><th>Host</th><th>Tests Run</th><th>Tests Passed</th><th>Tests Failed</th><th>Percent Passed</th></tr>
				<tr><td>linux</td><td>245</td><td>243</td><td>2</td><td>99.18%</td></tr>
				<tr><td>s64</td><td>245</td><td>243</td><td>2</td><td>99.18%</td></tr>
				<tr><td>win32</td><td>245</td><td>245</td><td>0</td><td>100%</td></tr>
			</table>
		</td>
	</tr>
-->

	<tr><td>&nbsp;</td></tr>

</table>

<?php cc_printPageFooter(); ?>
