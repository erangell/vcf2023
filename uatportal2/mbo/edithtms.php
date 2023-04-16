<?php
$dirArray = getDir(".");	// Get a list of the current directory
$renfile = "renhtmx.bat";
if (!$ehandle = fopen($renfile, 'w')) {
 		die ("Cannot open file ($renfile)");
}

while (list($filename, $filedate) = each($dirArray)) {
	$newfile = $filename . "x";
	echo ("<li>$filename \n");

	if (!$whandle = fopen($newfile, 'w')) {
         		die ("Cannot open file ($newfile)");
    	}

	if (!$rhandle = fopen($filename, 'r')) {
         		die ("Cannot open file ($newfile)");
    	}
	
	$hrcount = 0;

	while (($line = fgets($rhandle)) != false)
	{
		$outline = $line;
		if (substr($line,4,9) == ("hr width="))
		{
			$hrcount++;
			if ($hrcount == 1)
			{
				$outline .= " <!--HEADER BUTTONS COMMENTED OUT APR 2006";
			}
			if ($hrcount == 2)
			{
				$outline .= " -->";
			}

		}
		fputs($whandle,$outline);
	}

	fputs ($ehandle,"rename " . $newfile . " " . $filename . "\r\n");
}

function getDir($htmdir) {	
// Returns directory as array[file]=date in newest to oldest order
	$dirArray = array();
	$diskdir = "./$htmdir/";
	if (is_dir($diskdir)) {
		$dh = opendir($diskdir);
		while (($file = readdir($dh)) != false ) {
			if (filetype($diskdir . $file) == "file" && $file[0]  != ".") {
				if (strrchr(strtolower($file), ".") == ".htm") {
					$ftime = filemtime($htmdir."/".$file); 
					$dirArray[$file] = $ftime;
				}
			}
		}
		closedir($dh);
	}
	asort($dirArray);
	$dirArray = array_reverse($dirArray);
	return $dirArray;
}

?>