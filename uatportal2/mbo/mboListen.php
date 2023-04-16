<? 
require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = validateMboAdminSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: admIndex.php");
}
else
{
	$admpart = explode('|',$valkey);
	$admsite = $admpart[1];
	$admuid  = $admpart[2];
	$admsitedesc  = $admpart[3];
	$admulname  = $admpart[4];
	$admufname  = $admpart[5];
	$admuemail  = $admpart[6];

	require_once ("mboconfig.php");
	$dirPath = mboGetUploadPath();
	$uplURL = mboGetUploadURL();

	$maxFeed = 10000;
	$rptnumrows = 0;

	header('Content-type: text/html', true);

	include("mboHeader.txt");

	print "<html><head><title>UD MBOBASIC Sight Singing</title>\n";
	print "<body>\n";

	$sql = "select u.lastName, u.firstName, u.user_id, se.sectDescription "
	//. ' SELECT c.courseId, c.courseDesc, se.sectionId, se.sectDescription, '
	//. ' u.lastName, u.firstName, u.user_id, u.emailAdrs '
	. ' FROM '
	. '  mboCourse c, mboSiteCourse sc, mboSection se, '
	. '  mboUser u, mboRoster r '
	. ' WHERE '
	. '  sc.siteId = ' . "'" . strtoupper($admsite) . "'"
	. ' and sc.courseId = c.courseId '
	. ' and se.siteId = sc.siteId and se.courseId = sc.courseId '
	. ' and se.instructorId = ' . "'" . strtoupper($admuid) . "'"
	. ' and u.siteId = ' . "'" . strtoupper($admsite) . "'"
	. '  and r.siteId = ' . "'" . strtoupper($admsite) . "'"
	. '  and r.courseId = c.courseId '
	. '  and r.sectionId = se.sectionId '
	. '  and r.studentId = u.user_id '
	. ' ORDER BY 1,2,3 '
	;

	//echo "<li>$sql";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboListen.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numstudents = 0;
	while ($row = mysql_fetch_array($results))
	{	
		$studentlname[$numstudents] = $row[0];
		$studentfname[$numstudents] = $row[1];
		$studentarr[$numstudents] = strtoupper($row[2]);
		$studentsect[$numstudents] = $row[3];
		$numstudents++;
	}

	print"	<h1>Sight Singing Exercises - Student Recordings</h1>\n";
	print"  <h3>Instructor: " . $admulname . ", " . $admufname . "</h3>";

	$dirArray = getDir($dirPath);	// Get a list of the current directory
	echo "<table border=1><tr><td><b>Date Uploaded</b></td><td><b>Section</b></td><td><b>Student</b></td><td><b>Filename</b></td>";
	while (list($filename, $filedate) = each($dirArray)AND $maxFeed > 0) {

		//2005-11-22: Only include file if student is in instructor's class

		list($fnameid, $fnamesite, $fnamewav) = split("\_",$filename);
		list($fnamestudent, $fnameext) = split("\.",$fnamewav);

		$mystudent = 0;
		$saveindex = 0;
		for ($index = 0 ; $index < $numstudents ; $index++)
		{
			//echo "<li>" . $index . "|" . $studentarr[$index] . "|" . $fnamestudent;
			if ($studentarr[$index] == strtoupper($fnamestudent))
			{
				$mystudent++;
				$saveindex = $index;
			}
		}

		if ($mystudent > 0)
		{	
			
			print "<tr>\n";
			//print "<td>$fnameid</td><td>$fnamesite</td><td>$fnamewav</td><td>$fnamestudent</td><td>$fnameext</td>";
			print "<td>" . date("r",$filedate) . "</td>\n";
			print "<td>" . $studentsect[$saveindex] . "</td>";
			print "<td>" . $studentlname[$saveindex] . ", " . $studentfname[$saveindex] . "</td>";
			print "<td>";
			echo ("<a href=" . '"' . $uplURL .  $filename . '" target=new_window>' . $filename . "</a> \n");
			print "</td>";
			print "</tr>\n";
			$maxFeed--;
			$rptnumrows++;
		}
	}

	print "</table>\n";

	print "<center>";

	if ($rptnumrows == 0)
	{
		print "<h2>No recordings were found for students in your class sections.</h2>\n";
	}

	print "<h3>$copyrightTAG</h3>\n";
	print "<a href=\"mboAdmMenu.php\">Return to menu</a>";
	print "</center>";

}

// Functions and Classes
function stripJunk ($text) {
// Strip non-text characters
	for ($c=0; $c<strlen($text); $c++) {
		if (ord($text[$c]) >= 32 AND ord($text[$c]) <= 122)
			$outText.=$text[$c];
	}
	return $outText;
}


function getDir($mp3Dir) {	
// Returns directory as array[file]=date in newest to oldest order
	$dirArray = array();
	$diskdir = "./$mp3Dir/";
	if (is_dir($diskdir)) {
		$dh = opendir($diskdir);
		while (($file = readdir($dh)) != false ) {
			if (filetype($diskdir . $file) == "file" && $file[0]  != ".") {
				if (strrchr(strtolower($file), ".") == ".wav") {
					$ftime = filemtime($mp3Dir."/".$file); 
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
