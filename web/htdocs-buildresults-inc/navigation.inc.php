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

	println("<p>");
	println("PROJECTS<br/>");
	if (!isset($projects)) {
		println("No projects found");
	} else {
		foreach ($projects as $project)
			println("<a class=\"link\" href=\"?project=${project}\">${project}</a><br/>");
	}
	println("</p>");
}



function cc_printNavigationBuilds($project, $build) {
	$builds = cc_getListOfBuilds($project);

	println("<form method=\"get\" action=\"buildresults.html\">");
	println("<p class=\"table-nowrap\">");
	println("BUILDS<br/>");
	println("<br/>");
	if (!isset($builds)) {
		println("No builds for project ${project}");
	} else {
		println("Most Recent<br/>");
		for ($i = 0; ($i < count($builds)) && ($i < 10); $i++) {
			print("<a class=\"link\" href=\"?project=${project}&amp;build=" . $builds[$i] . "\">" . cc_getDateOfLogFile($builds[$i]) . "</a>");
			if ($label = cc_getLabelOfLogFile($builds[$i]))
				print(" (<span class=\"table-text-success\">build.${label}</span>)");
			else
				print(" (<span class=\"table-text-error\">FAILED!</span>)");
			println("<br/>");
		}
		if (count($builds) > 10) {
			println("<br/>");
			println("Older<br/>");
			println("		<input type=\"hidden\" name=\"project\" value=\"${project}\"/>");
			println("		<select name=\"build\" onchange=\"form.submit()\">");
			for ($i = 10; $i < count($builds); $i++) {
				print("			<option value=\"" . $builds[$i] . "\"");
				if (isset($build) && $build == $builds[$i])
					print(" selected=\"selected\"");
				print(">" . cc_getDateOfLogFile($builds[$i]) . "&nbsp;");
				if ($buildnum = cc_getLabelOfLogFile($builds[$i]))
					print(" (build.${buildnum})");
				else
					print(" (FAILED!)");
				println("</option>");
			}
			println("		</select>");
		}
	}
	println("</p>");
	println("</form>");
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
