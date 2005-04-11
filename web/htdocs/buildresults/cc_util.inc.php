<?php



$buildlogs = "/home/groups/c/co/cobertura/buildlogs";
$additionalfiles = "/home/groups/c/co/cobertura/buildlogs/files";



class cc_Modification {
	var $type;
	var $filename;
	var $project;
	var $date;
	var $user;
	var $comment;
	var $revision;
}



class cc_CompileMessage {
	var $priority;
	var $message;
}



class cc_TestSuite {
	var $errors;
	var $failures;
	var $name;
	var $tests;
	var $time;
	var $testcases;
}



class cc_TestCase {
	var $classname;
	var $name;
	var $time;
	var $sysout;
	var $syserr;
	var $errors;
	var $failures;
}



class cc_TestError {
	var $message;
	var $type;
	var $text;
}



class cc_TestFailure {
	var $message;
	var $type;
	var $text;
}



class cc_LogParser {
	// Variables that will contain info extracted from the logs
	var $info;
	var $modifications;
	var $compileTime;
	var $compileError;
	var $compileMessages;
	var $testerrors;
	var $testfailures;
	var $teststotal;
	var $testsuites;

	// State variables
	var $stack;
	var $stacksize;
	var $nextmodification;
	var $nextcompilemessage;
	var $nexttestsuite;
	var $nexttestcase;
	var $nextproblem;

	function cc_LogParser() {
		// Initialize our state variables
		$this->stack = array();
		$this->stacksize = 0;

		// Initialize the variables that will contain info from the log
		$this->info = array();
		$this->modifications = array();
		$this->compileTime = 0;
		$this->compileError = 0;
		$this->compileMessages = array();
		$this->testerrors = 0;
		$this->testfailures = 0;
		$this->teststotal = 0;
		$this->testsuites = array();
	}

	function startElement($parser, $name, $attrs) {
		if (($this->stacksize >= 2) && ($name == "MESSAGE") && ($this->stack[1] == "BUILD") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextcompilemessage = new cc_CompileMessage();
				$this->nextcompilemessage->priority = $attrs["PRIORITY"];
		} else if ($this->stacksize == 1) {
			if (($name == "BUILD") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->compileTime = $attrs["TIME"];
				$this->compileError = $attrs["ERROR"];
			} else if (($name == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nexttestsuite = new cc_TestSuite();
				$this->nexttestsuite->testcases = array();
				$this->nexttestsuite->errors = $attrs["ERRORS"];
				$this->nexttestsuite->failures = $attrs["FAILURES"];
				$this->nexttestsuite->name = $attrs["NAME"];
				$this->nexttestsuite->tests = $attrs["TESTS"];
				$this->nexttestsuite->time = $attrs["TIME"];
				$this->testerrors += $this->nexttestsuite->errors;
				$this->testfailures += $this->nexttestsuite->failures;
				$this->teststotal += $this->nexttestsuite->tests;
			}
		} else if ($this->stacksize == 2) {
			if (($name == "PROPERTY") && ($this->stack[1] == "INFO") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->info[$attrs["NAME"]] = $attrs["VALUE"];
			} else if (($name == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification = new cc_Modification();
				$this->nextmodification->type = $attrs["TYPE"];
			} else if (($name == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nexttestcase = new cc_TestCase();
				$this->nexttestcase->errors = array();
				$this->nexttestcase->failures = array();
				$this->nexttestcase->classname = $attrs["CLASSNAME"];
				$this->nexttestcase->name = $attrs["NAME"];
				$this->nexttestcase->time = $attrs["TIME"];
			}
		} else if ($this->stacksize == 3) {
			if (($name == "ERROR") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextproblem = new cc_testError();
				$this->nextproblem->message = $attrs["MESSAGE"];
				$this->nextproblem->type = $attrs["TYPE"];
			} else if (($name == "FAILURE") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextproblem = new cc_testFailure();
				$this->nextproblem->message = $attrs["MESSAGE"];
				$this->nextproblem->type = $attrs["TYPE"];
			}
		}

		array_push($this->stack, $name);
		$this->stacksize++;
	}

	function endElement($parser, $name) {
		$popped = array_pop($this->stack);
		$this->stacksize--;
		if (($name != $popped) || ($this->stacksize < 0))
			print("Malformed XML!");

		if (($this->stacksize >= 2) && ($name == "MESSAGE") && ($this->stack[1] == "BUILD") && ($this->stack[0] == "CRUISECONTROL")) {
			array_push($this->compileMessages, $this->nextcompilemessage);
			unset($this->nextcompilemessage);
		} else if ($this->stacksize == 1) {
			if (($name == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				array_push($this->testsuites, $this->nexttestsuite);
				unset($this->nexttestsuite);
			}
		} else if ($this->stacksize == 2) {
			if (($name == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				array_push($this->modifications, $this->nextmodification);
				unset($this->nextmodification);
			} else if (($name == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				array_push($this->nexttestsuite->testcases, $this->nexttestcase);
				unset($this->nexttestcase);
			}
		} else if ($this->stacksize == 3) {
			if (($name == "ERROR") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				array_push($this->nexttestcase->errors, $this->nextproblem);
				unset($this->nextproblem);
			} else if (($name == "FAILURE") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				array_push($this->nexttestcase->failures, $this->nextproblem);
				unset($this->nextproblem);
			}
		}
	}

	function characterData($parser, $data) {
		if (($this->stacksize >= 3) && ($this->stack[$this->stacksize - 1] == "MESSAGE") && ($this->stack[1] == "BUILD") && ($this->stack[0] == "CRUISECONTROL")) {
			$this->nextcompilemessage->message = $data;
		} else if ($this->stacksize == 4) {
			if (($this->stack[3] == "DATE") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->date = $data;
			} else if (($this->stack[3] == "USER") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->user = $data;
			} else if (($this->stack[3] == "COMMENT") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->comment = $data;
			} else if (($this->stack[3] == "SYSTEM-OUT") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nexttestcase->sysout = $data;
			} else if (($this->stack[3] == "SYSTEM-ERR") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nexttestcase->syserr = $data;
			} else if (($this->stack[3] == "ERROR") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextproblem->text .= $data;
			} else if (($this->stack[3] == "FAILURE") && ($this->stack[2] == "TESTCASE") && ($this->stack[1] == "TESTSUITE") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextproblem->text .= $data;
			}
		} else if ($this->stacksize == 5) {
			if (($this->stack[4] == "FILENAME") && ($this->stack[3] == "FILE") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->filename = $data;
			} else if (($this->stack[4] == "PROJECT") && ($this->stack[3] == "FILE") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->project = $data;
			} else if (($this->stack[4] == "REVISION") && ($this->stack[3] == "FILE") && ($this->stack[2] == "MODIFICATION") && ($this->stack[1] == "MODIFICATIONS") && ($this->stack[0] == "CRUISECONTROL")) {
				$this->nextmodification->revision = $data;
			}
		}
	}

	function parseFile($filename) {
		$parser = xml_parser_create();
		xml_set_object($parser, &$this);
		xml_set_element_handler($parser, "startElement", "endElement");
		xml_set_character_data_handler($parser, "characterData");
		if (is_dir($filename)) {
			cc_printErrorBox("Could not open ${filename} because it is a directory");
			return;
		}
		if (!$fp = fopen($filename,"r")) {
			cc_printErrorBox("Could not open file ${filename}.");
			return;
		}
		while ($data = fread($fp, 4096)) {
			if (!xml_parse($parser, $data, feof($fp))) {
				cc_printErrorBox("XML error: "
					. xml_error_string(xml_get_error_code($xml_parser))
					. " at line "
					. xml_get_current_line_number($xml_parser));
				return;
			}
		}
		fclose($fp);
		xml_parser_free($parser);
	}
}



function cc_getMostRecentBuild($project) {
	$builds = cc_getListOfBuilds($project);

	if (count($builds) <= 0)
		return;

	rsort($builds);

	return $builds[0];
}



/*
 * Return the date that a given CruiseControl log file
 * was created.  The date is extracted from the file name.
 * An example filename is:
 *   log20040511085930Lbuild.7.xml
 *      YYYYMMDDHHMMSS
 */
function cc_getDateOfLogFile($filename) {
	$year = substr($filename, 3, 4);
	$month = substr($filename, 7, 2);
	$day = substr($filename, 9, 2);
	$hour = substr($filename, 11, 2) - 1;
	$minute = substr($filename, 13, 2);
	$second = substr($filename, 15, 2);

	$seconds_since_epoch = strtotime("${year}-${month}-${day} ${hour}:${minute}:${second} EST");

	return date("j M Y\, g:ia", $seconds_since_epoch);
}



/*
 * Return the build number of a CruiseControl build based
 * on the name of the log file for that build.  If the
 * build was not successfull then false is returned.
 */
function cc_getLabelOfLogFile($filename) {
	if (!strpos($filename, "L"))
		return false;
	$period1 = 23;
	$period2 = strpos(substr($filename, $period1 + 1), ".");
	return substr($filename, $period1 + 1, $period2);
}



/*
 *
 *
 */
function cc_isValidLogFilename($filename) {
	if (getFileExtension($file) != "xml")
		return false;

	if (strncmp($filename, "log", 3) != 0)
		return false;

	return true;
}



/*
 * TODO: Make sure the font color is black.
 */
function cc_printErrorBox($text) {
	println("<table align=\"center\" bgcolor=\"#ffffff\" border=\"0\">");
	println("\t<tr>");
	println("\t\t<td>");
	println("\t\t\t${text}");
	println("\t\t</td>");
	println("\t</tr>");
	println("</table>");
}



function cc_printPageHeader($project, $build, $file) {
	println("<html>");
	println("<head>");
	println("<title>Build Results: " . cc_getPageTitle($project, $build, $file) . "</title>");
	println("<link type=\"text/css\" rel=\"stylesheet\" href=\"StyleSheets/main.css\"/>");
	println("<script type=\"text/javascript\" language=\"javascript\">");
	println("	function expandable(loc) {");
	println("		if (document.getElementById) {");
	println("			var foc=loc.firstChild.innerHTML?");
	println("				loc.firstChild:");
	println("				loc.firstChild.nextSibling;");
	println("			foc.innerHTML=foc.innerHTML == '+' ? '-' : '+';");
	println("			foc=loc.nextSibling.style?");
	println("				loc.nextSibling:");
	println("				loc.nextSibling.nextSibling;");
	println("			foc.style.display=foc.style.display=='block'?'none':'block';");
	println("		}");
	println("	}");
	println("</script>");
	println("</head>");
	println("");
	println("<body background=\"images/bluebg.gif\" topmargin=\"0\" leftmargin=\"0\" marginheight=\"0\" marginwidth=\"0\">");
	println("	<br/>");
	println("	<table border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"1\" width=\"98%\">");
	println("		<tr>");
	println("			<!-- Left hand column, containing the navigation menu -->");
	println("			<td valign=\"top\" width=\"15%\">");
	println("				<p>");
	println("				<table border=\"0\" align=\"center\" width=\"98%\" bgcolor=\"#FFFFFF\" cellpadding=\"0\" cellspacing=\"0\">");
	println("					<tr><td bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripestop.gif\"></td><td align=\"right\" bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripestopright.gif\"></td></tr>");
	println("					<tr>");
	println("						<td colspan=\"2\">");
	cc_printNavigation($project, $build, $file);
	println("						</td>");
	println("					</tr>");
	println("					<tr><td bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripesbottom.gif\"></td><td align=\"right\" bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripesbottomright.gif\"></td></tr>");
	println("				</table>");
	println("			</td>");
	println("");
	println("			<!-- Main information frame -->");
	println("			<td valign=\"top\">");
	println("				<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tbody>");
	println("					<tr><td bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripestop.gif\"></td><td align=\"right\" bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripestopright.gif\"></td></tr>");
	println("					<tr>");
	println("						<td bgcolor=\"#FFFFFF\" colspan=\"2\">");
	println("							<span class=\"header-data\">&nbsp;" . cc_getBreadcrumbs($project, $build, $file) . "</span>");
	println("\n\n\n\n");
}



function cc_printPageFooter() {
	println("\n\n\n\n");
	println("						</td>");
	println("					</tr>");
	println("					<tr><td bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripesbottom.gif\"></td><td align=\"right\" bgcolor=\"#FFFFFF\"><img border=\"0\" src=\"images/bluestripesbottomright.gif\"></td></tr>");
	println("				</tbody></table>");
	println("			</td>");
	println("		</tr>");
	println("	</table>");
	println("	<br/>");
	println("</body>");
	println("</html>");
}



function cc_getPageTitle($project, $build, $file) {
	if (isset($file) && isset($build) && isset($project))
		return "${project}, ${build}, ${file}";
	else if (isset($build) && isset($project))
		return "${project}, ${build}";
	else if (isset($project))
		return "${project}";
	else
		return "Projects";
}



function cc_getBreadcrumbs($project, $build, $file) {
	$breadcrumbs = "";

	// Print "Home"
	if (!isset($project)) {
		$breadcrumbs .= "Home";
		return $breadcrumbs;
	}
	$breadcrumbs .= "<a class=\"link\" href=\"projects.php\">Home</a> &gt; ";

	// Print the project name
	if (!isset($build)) {
		$breadcrumbs .= $project;
		return $breadcrumbs;
	}
	$breadcrumbs .= "<a class=\"link\" href=\"projectinfo.php?project=${project}\">${project}</a> &gt; ";

	// Print the build number
	if (!isset($file)) {
		$breadcrumbs .= "Build " . cc_getDateOfLogFile($build);
		if ($label = cc_getLabelOfLogFile($build))
			$breadcrumbs .= " (<span class=\"table-text-success\">build.${label}</span>)";
		else
			$breadcrumbs .= " (<span class=\"table-text-error\">FAILED!</span>)";
		return $breadcrumbs;
	}
	$breadcrumbs .= "<a class=\"link\" href=\"buildinfo.php?project=${project}&log=${build}\">Build " . cc_getDateOfLogFile($build) . "</a>";
	if ($label = cc_getLabelOfLogFile($build))
		$breadcrumbs .= " (<span class=\"table-text-success\">build.${label}</span>)";
	else
		$breadcrumbs .= " (<span class=\"table-text-error\">FAILED!</span>)";
	$breadcrumbs .= "&gt; ";

	// Print either the test number or the file name
	if (isset($file))
		$breadcrumbs .= "File " . $file;

	 return $breadcrumbs;
}



?>
