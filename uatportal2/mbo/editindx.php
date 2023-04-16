<?php
$dirArray = getDir(".");	// Get a list of the current directory
$renfile = "renindx.bat";
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
		if (strpos($line,"src=\"logout.gif\"></a></font></div>") > 0)
		{
			$hrcount++;
			if ($hrcount == 1)
			{
				$outline .= " <!-- COURSE INDEX ADDED APRIL 2006 -->";
				$outline .= '<div align="center"><span class="credits">
<br>
<font color="yellow">';
				$outline .= '<a href="mboRubric.php" onMouseOut="MM_swapImgRestore()" ';
				$outline .= "onMouseOver=\"MM_swapImage('Image22','','CourseIndex_Over.GIF',1)\">";
				$outline .= '<img name="Image22" border="0" src="CourseIndex_Normal.GIF"></a></font></span></div>';
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

<!--								

<div align="center">


										<font color="yellow">
<a href="mboStuLogOut.php" 
onMouseOut="MM_swapImgRestore()" 
onMouseOver="MM_swapImage('Image20','','logoutover.gif',1)">
<img name="Image20" border="0" src="logout.gif">
</a>
</font>
</div>
									
<div align="center">
										<span class="credits">

<br>
<font color="yellow">
<a href="mboRubric.php" 
onMouseOut="MM_swapImgRestore()" 
onMouseOver="MM_swapImage('Image22','','CourseIndex_Over.GIF',1)">
<img name="Image22" border="0" 
src="CourseIndex_Normal.GIF">
</a>
</font>


</span>
</div>


-->



									