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


	$csect = $_GET['selsect'];
	if ($csect != "")
	{
		$loc="location:mboAddStudent.php?err=0001&csect=$csect";
		header($loc);
	}

	include("mboHeader.txt");

	$defsort = "u.lastName, u.firstName";
	$defdescr = "Last Name, First Name";
	$sortparm = $defsort;
	$sortdescr = $defdescr;
	
	if ($_GET['sort'] == "ui")
	{
		$sortparm = "u.user_id";
		$sortdescr = "User ID";
	}
	if ($_GET['sort'] == "em")
	{
		$sortparm = "u.emailAdrs";
		$sortdescr = "Email";
	}

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

	echo "<h1>Student Maintenance</h1>";

	echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";

	echo "<h4>";

	echo "&nbsp;&nbsp; Sorted by: $sortdescr</h4>";

	
	for ($sect = 0 ; $sect < $numSections ; $sect++ )
	{

	
		$sql = ' SELECT u.lastName, u.firstName, u.user_id, u.emailAdrs '
		. ' FROM '
		. '  mboUser u, mboRoster r '
		. ' WHERE '
		. '  u.siteId = ' . "'" . strtoupper($admsite) . "'"
		. '  and r.siteId = ' . "'" . strtoupper($admsite) . "'"
		. '  and r.courseId = ' . "'" . $courseId[$sect] . "'"
		. '  and r.sectionId = ' . "'" . $section[$sect] . "'"
		. '  and r.studentId = u.user_id '
		;

		$sql = $sql . " ORDER BY  " . $sortparm;


		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboMaintStudent.php: SQL Error: SQL=$sql");
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
			echo "<tr><td>No student data found (for this section)</td></tr>";
			echo "</table>";

		}
		else
		{
			echo "<p><table border=1 cellpadding=3>";
	
			echo '<tr>';
			echo '<td colspan=5><b>';
			echo "Course: $courseId[$sect] - $courseDesc[$sect]";
			echo "<br>Section: $section[$sect] - $sectionDesc[$sect]";
			echo '</b></td>';
			echo '</tr>';
			echo '<tr>';
			echo '<td><a href="mboMaintStudent.php"><b>Last Name</b></a></td>';
			echo "</td><td><b>First Name</b></td>";
			echo '<td><a href="mboMaintStudent.php?sort=ui"><b>User ID</b></a></td>';
			echo '<td><a href="mboMaintStudent.php?sort=em"><b>Email</b></a></td>';
			echo '<td><b>Action</b></td>';

			while ($row = mysql_fetch_array($results))
			{	
				echo "<tr>";
				echo "<td> $row[0] </td>";
				echo "<td> $row[1] </td>";
				echo "<td> $row[2] </td>";
				echo "<td> $row[3] </td>";
				echo '<td><a href="mboEditStudent.php?err=0001&ui=' . $row[2] . '">Edit</a>';
				echo '&nbsp;&nbsp;&nbsp;<a href="mboDeleteStudent.php?err=0001&ui=' . $row[2] . '">Delete</a>';
				echo '&nbsp;&nbsp;&nbsp;<a href="mboResetStudentPw.php?err=0001&ui=' . $row[2] . '">Reset PW</a></td>';
				echo "</tr>";
			}
	
			echo "</table>";
		}

		echo '<p>';
	}

	if ($numSections == 1)
	{
		echo '<p>';
		echo 'You are only teaching one section, therefore new students ';
		echo 'will be added to the section shown above. ';
		$courseForAdds = $courseId[0];
		$sectForAdds = $section[0];
	}
	else if ($numSections == 0)
	{
		echo '<p>';
		echo 'No active class sections were found for your ID.';
		echo '<br>Please contact your site administrator to rectify this problem.';

	}
	else
	{
		echo '<p>';

		echo '<form action="mboMaintStudent.php?">';

		echo 'Add New Students to Section: &nbsp;';

		echo '<SELECT name="selsect">';
		
		for ($sect = 0 ; $sect < $numSections ; $sect++ )
		{
			//echo "<li>";
			//echo "$section[$sect] - $sectionDesc[$sect]";

			echo '<OPTION value="';
			echo "$courseId[$sect]|$section[$sect]";
			echo '">';
			echo "$section[$sect] - $sectionDesc[$sect]</OPTION>";

		}
		echo "</SELECT>";
		echo "&nbsp;<input type=submit value=Add>";
		echo '</form>';
		
		//$courseForAdds = $courseId[1];
		//$sectForAdds = $section[1];
	}
	echo '<p>';

	if ($numSections == 1)
	{
		echo '<li><a href="mboAddStudent.php?err=0001&c=';
		echo "$courseForAdds";
		echo '&s=';
		echo "$sectForAdds";
		echo '">Add New Student</a>';
		echo '<p>';
	}

	echo '<li><a href="mboAdmMenu.php">Return to menu</a>';

}

?>
