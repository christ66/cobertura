<?php

function getFileExtension($filename) {
    return strchr($filename, ".");
}

function listFilesInDirectory($directory) {
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

function println($text) {
	print($text . "\n");
}

?>
