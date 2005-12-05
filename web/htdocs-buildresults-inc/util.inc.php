<?php

function endsWith($needle, $haystack)
{
	if (substr($haystack, strlen($haystack) - strlen($needle)) == $needle)
		return true;
	return false;
}

function getFileExtension($filename)
{
	$pos = strrpos($filename, ".");
	if ($pos === false)
		return "";
	return substr($filename, $pos + 1);
}

function listFilesInDirectory($directory)
{
	// Does directory exist?
	if (!is_dir($directory))
		return;

	// Do we have read access?
	if (!($dh = opendir($directory)))
		return;

	while (($file = readdir($dh)) !== false) {
		if (($file == ".") ||
			($file == ".."))
			continue;
		$files[] = $file;
	}

	if (count($files) <= 0)
		return;

	sort($files);

	return $files;
}

function println($text)
{
	print($text . "\n");
}

?>
