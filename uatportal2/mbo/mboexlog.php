<?php
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

	include("mboHeader.txt");

	$defsort = "u.lastName, u.firstName, el.updateTime DESC";
	$defdescr = "Student, Timestamp (descending)";
	$sortparm = $defsort;
	$sortdescr = $defdescr;
	$filtparm = " ";
	$filtactive = "N";

	if ($_GET['sort'] == "ts")
	{
		$sortparm = "el.updateTime DESC";
		$sortdescr = "Timestamp (Descending)";
	}
	if ($_GET['sort'] == "ex")
	{
		$sortparm = "ex.examTitle, u.lastName, u.firstName, el.updateTime DESC";
		$sortdescr = "Exam, Student, Timestamp (Descending)";
	}
	$filtexam = $_GET['filtexam'];
	if ($filtexam != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT examTitle '
		. ' FROM '
		. '  mboExam '
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboexlog.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_exam = 'N';
		while ($row = mysql_fetch_array($results))
		{
			if ($filtexam == $row[0])
			{
				$good_exam = 'Y';
			}
		}
		if ($good_exam == 'Y')
		{
			$filtparm = "and ex.examTitle = '" . $filtexam. "'";
			$filtdescr = "Exam";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid exam title in querystring (" . $filtexam . ") in mboexlog.php");
		}
	}
	$filtuid = $_GET['filtuid'];
	if ($filtuid != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT user_id '
		. ' FROM '
		. '  mboUser  '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboexlog.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_user = 'N';
		while ($row = mysql_fetch_array($results))
		{
			if ($filtuid == $row[0])
			{
				$good_user = 'Y';
			}
		}
		if ($good_user == 'Y')
		{
			$filtparm = "and u.user_id = '" . strtoupper($filtuid) . "'";
			$filtdescr = "Student";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboexlog.php");
		}
	}


        echo "<h1>Student Exam Log Report</h1>";

	echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";

	echo "<h4>";
	if ($filtactive == "Y")
	{
		echo " Filtered by: $filtdescr";
	}
	echo "&nbsp;&nbsp; Sorted by: $sortdescr</h4>";

	
	$outersql = ' SELECT c.courseId, c.courseDesc, se.sectionId, se.sectDescription '
	. ' FROM '
	. '  mboCourse c, mboSiteCourse sc, mboSection se '
	. ' WHERE '
	. '  sc.siteId = ' . "'" . strtoupper($admsite) . "'"
	. ' and sc.courseId = c.courseId '
	. ' and se.siteId = sc.siteId and se.courseId = sc.courseId '
	. ' and se.instructorId = ' . "'" . strtoupper($admuid) . "'"
	;

	//mboLogError("debug: outerSQL=$outersql");


	if (!($outerresults = mysql_query($outersql)))
	{
		mboLogError("mboMaintStudent.php: SQL Error: SQL=$outersql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numSections = 0;
	while ($outerrow = mysql_fetch_array($outerresults))
	{	
		$courseId[$numSections] = $outerrow[0];
		$courseDesc[$numSections] = $outerrow[1];
		$section[$numSections] = $outerrow[2];
		$sectionDesc[$numSections] = $outerrow[3];
		$numSections++;
	}

	if ($numSections == 0)
	{
		echo '<p>';
		echo 'No active class sections were found for your ID.';
		echo '<br>Please contact your site administrator to rectify this problem.';
		echo '<p>';
	}

	for ($sect = 0 ; $sect < $numSections ; $sect++ )
	{

		$sql = ' SELECT u.lastName, u.firstName '
		. ' , el.Result, ex.examTitle, el.updateTime, u.user_id  '
		. ' FROM '
		. '   mboExamLog el '
		. ' , mboUser u '
		. ' , mboExam ex  '
		. ' , mboRoster r '
		. ' WHERE ex.examId = el.examId '
		. ' and el.site_id = ' . "'" . $admsite . "'"
		. ' and el.result <> ' . "'STARTED'"
		. ' and u.user_id = el.user_id '
		. ' and u.siteId = el.site_id '
		. ' and r.siteId = ' . "'" . strtoupper($admsite) . "'"
		. ' and r.courseId = ' . "'" . $courseId[$sect] . "'"
		. ' and r.sectionId = ' . "'" . $section[$sect] . "'"
		. ' and r.studentId = u.user_id '
		;

		IF ($filtactive == "Y")
		{
			$sql = $sql . $filtparm;
		}	

		$sql = $sql . " ORDER BY  " . $sortparm;


		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboexlog.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		#echo "Number of rows: ";
		$numrows = mysql_num_rows($results);

		if ($numrows == 0)
		{	
			echo "<p><table border=1 cellpadding=3>";
			echo "<tr><td><b>Course: $courseId[$sect] - $courseDesc[$sect]";
			echo "<br>Section: $section[$sect] - $sectionDesc[$sect]</b></td></tr>";
			echo "<tr><td>No exam log data found (for this section)</td></tr>";
			echo "</table>";
		}
		else
		{

			echo "<p><table border=1 cellpadding=3>";
	
			echo "<tr><td colspan=5><b>Course: $courseId[$sect] - $courseDesc[$sect]";
			echo "<br>Section: $section[$sect] - $sectionDesc[$sect]</b></td></tr>";
			echo '<tr><td><a href="mboexlog.php"><b>Student</b></a></td>';
			echo "</td><td><b>Result</b></td>";
			echo '<td><a href="mboexlog.php?sort=ex"><b>Exam</b></a></td>';
			echo '<td><a href="mboexlog.php?sort=ts"><b>Timestamp</b></a></td>';

			while ($row = mysql_fetch_array($results))
			{	
				echo "<tr><td>";
				echo '<a href="';
				echo "mboexlog.php?filtuid=$row[5]";
				echo '"> ';
				echo $row[0] . ", " .$row[1] . "</a></td>";
				echo "<td> $row[2] </td>  ";
				echo '<td> <a href="';
				echo "mboexlog.php?filtexam=$row[3]";
				echo '"> ';
				echo "$row[3] </a> </td>";
				echo "<td> $row[4] </td>";
				echo "</tr>";
			}

			echo "</table>";
		}

		IF ($filtactive != "Y")
		{

			$sql = "select lastname, firstname from mboUser u , mboRoster r "
			. " where u.siteId = '" . $admsite . "'" 
			. ' and r.siteId = ' . "'" . strtoupper($admsite) . "'"
			. ' and r.courseId = ' . "'" . $courseId[$sect] . "'"
			. ' and r.sectionId = ' . "'" . $section[$sect] . "'"
			. ' and r.studentId = u.user_id '
			. " and not exists "
	        	. " (select 1 from mboExamLog el where u.user_id = el.user_id and u.siteId = el.site_id) "
			. " order by lastname, firstname "; 

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboexlog.php: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			#echo "Number of rows: ";
			$numrows = mysql_num_rows($results);
	
			if ($numrows > 0)
			{
				echo "<p><table border=1 cellpadding=3>";

				//echo "<tr><td><b>";
				//echo "Course: $courseId[$sect] - $courseDesc[$sect]";
				//echo "<br>Section: $section[$sect] - $sectionDesc[$sect]</b></td></tr>";

				echo "<tr><td><b>Students who have not completed any exams</b></td>";

				while ($row = mysql_fetch_array($results))
				{	
					echo "<tr>";
					echo "<td>" . $row[0] . ", " .$row[1] . "</td>  ";
					echo "</tr>";
				}

				echo "</table>";
			}
		}


		echo '<p>';
	}

	if ($filtactive == "Y")
	{
		echo '<li><a href="mboexlog.php">Remove Filter</a>';
		echo '<p>';
	}

	echo '<li><a href="mboAdmMenu.php">Return to main menu</a>';
	echo '<p>';

	echo '<li><a href="mboExamGrid.php">Return to Exam Mastery Grid</a>';
}

?>
