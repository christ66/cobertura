<?php
	include("cc.inc.php");
	$cc_branch = $HTTP_GET_VARS['branch'];
	$cc_project = $HTTP_GET_VARS['project'];

	$totalBuilds = 0;
	$goodBuilds = 0;
	if (isset($builds)) {
		$totalBuilds = count($builds);
		foreach ($builds as $build) {
			if (cc_getBuildNumberOfLogFile($build)) {
				$goodBuilds++;
			}
		}
	}

	//header("Content-type: image/png");
	header("Content-type: text/plain");

	$im = imagecreatefrompng("logo.gif");
	// Maybe it should be like this...
	//$im = @imagecreatefrompng("logo.gif");
	if (!$im) {
		$im  = imagecreate(150, 30);
		$bgc = imagecolorallocate($im, 255, 255, 255);
		$tc  = imagecolorallocate($im, 0, 0, 0);
		imagefilledrectangle($im, 0, 0, 150, 30, $bgc);
		imagestring($im, 1, 5, 5, "Error loading $imgname", $tc);
	}

	//$orange = imagecolorallocate($im, 220, 210, 60);
	//$px = (imagesx($im) - 7.5 * strlen("Hello, world!")) / 2;
	//imagestring($im, 3, $px, 9, $string, $orange);
	//imagepng($im);
	//imagedestroy($im);

?>
