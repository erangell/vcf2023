<?php
require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = validateMboStudentSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: stuIndex.php");
}
else
{

	$stupart = explode('|',$valkey);
	$stusite = $stupart[1];
	$stuuid  = $stupart[2];


	include("mboHeader.txt");


	$sql = ' SELECT u.lastName, u.firstName '
	. ' , el.updateTime , el.Result, ex.examTitle  '
	. ' FROM '
	. '   mboExamLog el '
	. ' , mboUser u '
	. ' , mboExam ex  '
	. ' WHERE ex.examId = el.examId '
	. ' and el.site_id = ' . "'" . $stusite . "'"
	. ' and el.result <> ' . "'STARTED'"
	. ' and u.user_id = el.user_id '
	. ' and u.siteId = el.site_id '
	. ' and u.user_id = ' . "'" . strtoupper($stuuid) . "'"
	. ' ORDER BY u.lastName, u.firstName, el.updateTime DESC '
	;


	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboStuExlog.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}


	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		echo "<p><h3>No exam log data found</h3>";
	}
	else
	{
		echo "<p><h1>Exam Log Report</h1>";

		$firstrow = 'Y';

		while ($row = mysql_fetch_array($results))
		{	
			if ($firstrow == 'Y')
			{
				$firstrow = 'N';
				echo "<h3>Student: $row[0], $row[1]</h3>";
				echo "<p><table border=1 cellpadding=3>";
				echo "<tr>";
				echo "<td><b>Date/Time</b></td>";
				echo "<td><b>Result</b></td>  ";
				echo "<td><b>Exam</b></td>";
				echo "</tr>";
			}
			echo "<tr>";
			echo "<td> $row[2] </td>  ";
			echo "<td> $row[3] </td><td> $row[4] </td>";
			echo "</tr>";
		}

		echo "</table>";
	}

	echo '<P><li><a href="mutheoryhome.php">Return to homepage</a>';
}

?>
