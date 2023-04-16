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

//	//For Exam Grid - need sort by user name only to correctly produce grid
//	if ($_GET['sort'] == "ts")
//	{
//		$sortparm = "el.updateTime DESC";
//		$sortdescr = "Timestamp (Descending)";
//	}
//	if ($_GET['sort'] == "ex")
//	{
//		$sortparm = "ex.examTitle, u.lastName, u.firstName, el.updateTime DESC";
//		$sortdescr = "Exam, Student, Timestamp (Descending)";
//	}

	//Create array of exam titles

	$sql = ' SELECT examID, examTitle, examsort '
	. ' FROM '
	. '  mboExam '
	. ' ORDER BY examsort '
	;
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numExams = mysql_num_rows($results);
	$examindx = 0;
	while ($row = mysql_fetch_array($results))
	{
		$examId[$examindx] = $row[0];
		$examTitle[$examindx] = $row[1];
		$examindx++;
	}



	$filtexam = $_GET['filtexam'];
	if ($filtexam != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT examTitle '
		. ' FROM '
		. '  mboExam '
		. ' ORDER BY '
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
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
			mboLogError ("badinput: Invalid exam title in querystring (" . $filtexam . ") in mboExamGrid.php");
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
			mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
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
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboExamGrid.php");
		}
	}


        echo "<h1>Student Exam Mastery Grid</h1>";

	echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";


//	//Temporarily comment out sort/filter for mastery grid display
//	echo "<h4>";
//	if ($filtactive == "Y")
//	{
//		echo " Filtered by: $filtdescr";
//	}
//	echo "&nbsp;&nbsp; Sorted by: $sortdescr</h4>";


        echo "<h3>Exam Index</h3>";

	echo "<p><table border=1 cellpadding=3>";

	$halfNumExams = $numExams / 2;
	for ($i=0 ; $i < $halfNumExams ; $i++)
	{
		$dispindx = $i + 1;
		echo "<tr>";
		echo "<td>$dispindx</td><td>";
		echo '<a href="mboexlog.php?filtexam=' . $examTitle[$i] .'">' . $examTitle[$i] . "</a></td>";


		$dispcol2 = $dispindx + $halfNumExams;
		if ($dispcol2 != floor($dispcol2))
		{
			if ($i == 0)
			{
				echo '<td>&nbsp;</td>';
			}
			else
			{
				$disp = floor($dispcol2);
				echo "<td>$disp</td>";
			}
		}
		else
		{
			echo "<td>$dispcol2</td>";
		}

		echo "<td>";

		$rightcolindex = $i+$halfNumExams;
		if ($rightcolindex != floor($rightcolindex))
		{
			if ($i == 0)
			{
				echo '&nbsp;</td>';
			}
			else
			{
				$ix = floor($rightcolindex);
				echo '<a href="mboexlog.php?filtexam=' . $examTitle[$ix] .'">' . $examTitle[$ix] . "</a></td>";

			}
		}
		else
		{
			echo '<a href="mboexlog.php?filtexam=' . $examTitle[$rightcolindex] .'">' . $examTitle[$rightcolindex] . "</a></td>";
		}
		echo "</tr>";
	}

	echo "</table>";


	
	$outersql = ' SELECT c.courseId, c.courseDesc, se.sectionId, se.sectDescription '
	. ' FROM '
	. '  mboCourse c, mboSiteCourse sc, mboSection se '
	. ' WHERE '
	. '  sc.siteId = ' . "'" . strtoupper($admsite) . "'"
	. ' and sc.courseId = c.courseId '
	. ' and se.siteId = sc.siteId and se.courseId = sc.courseId '
	. ' and se.instructorId = ' . "'" . strtoupper($admuid) . "'"
	. ' ORDER BY 1,2,3 '
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
		. ' , el.Result, ex.examTitle, el.updateTime, u.user_id, ex.examId  '
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
			mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
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
	
			$cspan = $numExams+1;
			echo "<tr><td colspan=$cspan><b>Course: $courseId[$sect] - $courseDesc[$sect]";
			echo "<br>Section: $section[$sect] - $sectionDesc[$sect]</b></td>";

			echo '<tr>';
			echo '<td><b>Student</b></td>';
			
			for ($ix=1 ; $ix <= $numExams ; $ix++)
			{
				echo "<td><b>$ix</b></td>";
			}
			echo '</tr>';

			for ($ix=1 ; $ix <= $numExams ; $ix++)
			{
				$gridRow[$ix]="&nbsp;";
			}

			$prevStudentId = "";
			while ($row = mysql_fetch_array($results))
			{	
				if ($prevStudentId == "")
				{
					$prevStudentId = $row[5];
				}
				if ($prevStudentId != $row[5])
				{
					//Student
					echo "<tr><td>";
					echo '<a href="';
					echo "mboexlog.php?filtuid=$prevStudentId";
					echo '"> ';
					echo $prevStudentName . "</a></td>";

					for ($ix=1 ; $ix <= $numExams ; $ix++)
					{
						echo "<td>$gridRow[$ix]</td>";
					}


	
					for ($ix=1 ; $ix <= $numExams ; $ix++)
					{
						$gridRow[$ix]="&nbsp;";
					}
	
					//Result
					//echo "<td> $row[2] </td>  ";

					//Exam
					//echo '<td> <a href="';
					//echo "mboExamGrid.php?filtexam=$row[3]";
					//echo '"> ';
					//echo "$row[3] </a> </td>";

					//Timestamp
					//echo "<td> $row[4] </td>";

			

				}
				echo "</tr>";

				$prevStudentId = $row[5];
				$prevStudentName = $row[0] . ", " .$row[1];
				$prevStudentResult = $row[2];
				$prevStudentExamName = $row[3];
				$prevStudentExamTime = $row[4];
				$prevStudentExamId = $row[6];

				//Get grid row index for current exam ID

				$gridIndex = -1;
				for ($ix = 0 ; $ix < $numExams ; $ix++)
				{
					if ($prevStudentExamId == $examId[$ix])
					{
						$gridIndex = $ix+1;
					}
				}

				if ($gridIndex >= 0)
				{
					//If student has one PASSED record, mastery = Y
					//If student has FAILED record, and no PASSED record yet found, mastery = N

					if ($prevStudentResult == "FAILED")
					{
						if ($gridRow[$gridIndex] != "Y")
						{
							$gridRow[$gridIndex] = "<b>N</b>";
						}
					}				
					else
					{	if ($prevStudentResult == "PASSED")
						{
							$gridRow[$gridIndex] = "Y";
						}
					}
				}

			}
			//Display the LAST student
			echo "<tr><td>";
			echo '<a href="';
			echo "mboexlog.php?filtuid=$prevStudentId";
			echo '"> ';
			echo $prevStudentName . "</a></td>";

			for ($ix=1 ; $ix <= $numExams ; $ix++)
			{
				echo "<td>$gridRow[$ix]</td>";
			}

			for ($ix=1 ; $ix <= $numExams ; $ix++)
			{
				$gridRow[$ix]="&nbsp;";
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
				mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
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
		echo '<li><a href="mboExamGrid.php">Remove Filter</a>';
	}


	echo '<p>';
	echo '<li><a href="mboAdmMenu.php">Return to menu</a>';

}

?>
