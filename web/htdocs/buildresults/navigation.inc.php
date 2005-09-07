<?php



function cc_printNavigation($project, $build, $file) {
	if (isset($file) && isset($build) && isset($project))
		cc_printNavigationBuilds($project, $build);
	else if (isset($build) && isset($project))
		cc_printNavigationBuilds($project, $build);
	else if (isset($project))
		cc_printNavigationBuilds($project, $build);
	else
		cc_printNavigationProjects();
}



function cc_printNavigationProjects() {
	$projects = cc_getListOfProjects();

	println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
	println("	<tr><td><span class=\"header-title\"><b>PROJECTS</b></span></td></tr>");
	if (!isset($projects)) {
		println("	<tr><td>&nbsp;No projects found</td></tr>");
	} else {
		foreach ($projects as $project)
			println("	<tr><td class=\"table-nowrap\">&nbsp;<a class=\"link\" href=\"projectinfo.php?project=${project}\">${project}</a></td></tr>");
	}
	println("</table>");
}



function cc_printNavigationBuilds($project, $build) {
	$builds = cc_getListOfBuilds($project);

	println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
	println("	<tr><td><span class=\"header-title\"><b>BUILDS</b></span></td></tr>");
	if (!isset($builds)) {
		println("	<tr><td>&nbsp;No builds for project ${project}</td></tr>");
	} else {
		println("	<tr><td><span class=\"header-data\">Most Recent</span></td></tr>");
		for ($i = 0; ($i < count($builds)) && ($i < 10); $i++) {
			print("	<tr><td class=\"table-nowrap\">");
			print("&nbsp;<a class=\"link\" href=\"buildinfo.php?project=${project}&amp;log=" . $builds[$i] . "\">" . cc_getDateOfLogFile($builds[$i]) . "</a>");
			if ($label = cc_getLabelOfLogFile($builds[$i]))
				print(" (<span class=\"table-text-success\">build.${label}</span>)");
			else
				print(" (<span class=\"table-text-error\">FAILED!</span>)");
			println("</td></tr>");
		}
		if (count($builds) > 10) {
			println("	<tr><td>&nbsp;</td></tr>");
			println("	<tr><td><span class=\"header-data\">Older</span></td></tr>");
			println("	<tr><td>");
			println("		<form method=\"get\" action=\"buildinfo.php\">");
			println("		<input type=\"hidden\" name=\"project\" value=\"${project}\"/>");
			println("		<select name=\"log\" onchange=\"form.submit()\">");
			for ($i = 10; $i < count($builds); $i++) {
				print("			<option value=\"" . $builds[$i] . "\"");
				if (isset($build) && $build == $builds[$i])
					print(" selected=\"true\"");
				print(">" . cc_getDateOfLogFile($builds[$i]) . "&nbsp;");
				if ($buildnum = cc_getLabelOfLogFile($builds[$i]))
					print(" (build.${buildnum})");
				else
					print(" (FAILED!)");
				println("</option>");
			}
			println("		</select>");
			println("		</form>");
			println("	</td></tr>");
		}
	}
	println("</table>");
}



function cc_getListOfProjects() {
	global $buildlogs;

	$files = listFilesInDirectory("${buildlogs}");
	if (is_null($files))
		return;

	foreach ($files as $file) {
		$projects[] = $file;
	}

	return $projects;
}



function cc_getListOfBuilds($project) {
	global $buildlogs;
	$builds = array();

	$files = listFilesInDirectory("${buildlogs}/${project}");
	if (is_null($files))
		return;

	foreach ($files as $file) {
		if (($file == "_cache") ||
			($file == "status") ||
			(cc_isValidLogFilename($file)))
			continue;
		$builds[] = $file;
	}

	if (count($builds) <= 0)
		return;

	rsort($builds);

	return $builds;
}



?>
