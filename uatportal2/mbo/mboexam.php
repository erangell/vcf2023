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
	$examhash = $_GET['e'];
	//echo "examnum=$examnum <br> ";
	//echo "valkey=$valkey <br>";

	$studpart = explode('|',$valkey);
	//echo "part 0 = $studpart[0]<br>";
	//echo "part 1 = $studpart[1]<br>";
	//echo "part 2 = $studpart[2]<br>";

	$studsite = $studpart[1];
	$studuid  = $studpart[2];
	//echo "Student Site = $studsite<br>";
	//echo "Student ID = $studuid<br>";

	$randstart = rand(2000,8000);

	$sql = "select pnum from mboPrimes where pHash = $examhash";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboexam.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);
	//mboLogError ("debug: numrows=$numRows");

	if ($numRows != 1)
	{	
		mboLogError("mboexam.php: Exam ID not found in pHash column!");
		mboFatalError();	
	}
	else
	{
		$row = mysql_fetch_array($results);
	}

	$examnum = $row[0];


	$sql = "select courseId, examId, examTitle, examUrl, passUrl, failUrl from mboExam where examId = $examnum";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboexam.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);
	//mboLogError ("debug: numrows=$numRows");

	if ($numRows != 1)
	{	
		mboLogError("mboexam.php: Exam row not found on mboExam table!");
		mboFatalError();	
	}
	else
	{
		$row = mysql_fetch_array($results);
	}

	$exCourseId = $row[0];
	$exExamId = $row[1];
	$exExamTitle = $row[2];
	$exExamUrl = $row[3];
	$exPassUrl = $row[4];
	$exFailUrl = $row[5];

	//echo "<br>Course ID=$exCourseId";
	//echo "<br>Exam ID=$exExamId";
	//echo "<br>Exam Title=$exExamTitle";
	//echo "<br>Exam URL=$exExamUrl";
	//echo "<br>Pass URL=$exPassUrl";
	//echo "<br>Fail URL=$exFailUrl";

//2006-07-12: NO LONGER NEED TO ALLOCATE PRIMES FOR USER
//	$sql = "select min(pnum) from mboPrimes where inuse='N' and pnum > " . $randstart;
//
//	if (!($results = mysql_query($sql)))
//	{
//		mboLogError("mboexam.php: SQL Error: SQL=$sql");
//		mboLogError ("DB error: " . mysql_error());
//		mboFatalError();
//	}
//
//	$numRows = mysql_num_rows($results);
//	//mboLogError ("debug: numrows=$numRows");
//
//	if ($numRows != 1)
//	{	
//		mboLogError("mboexam.php: No available primes for hashing!");
//		mboFatalError();	
//	}
//	else
//	{
//		$row = mysql_fetch_array($results);
//	}
//
//	$pnum = $row[0];
//
//	$sql = "update mboPrimes set inuse = 'Y' where inuse='N' and pnum = " . $pnum; 
//
//	if (!($results = mysql_query($sql)))
//	{
//		mboLogError("mboexam.php: SQL Error: SQL=$sql");
//		mboLogError ("DB error: " . mysql_error());
//		mboFatalError();
//	}
//
//	//mboLogError("debug:mboexam.php:pnum=$pnum");
//	//echo "<br>pnum=$pnum";
//
//	$sql = "delete from mboUserHash where site_id = '" .  $studsite . "' and user_id = '" . strtoupper($studuid) . "'" ; 
//
//	if (!($results = mysql_query($sql)))
//	{
//		mboLogError("mboexam.php: SQL Error: SQL=$sql");
//		mboLogError ("DB error: " . mysql_error());
//		mboFatalError();
//	}
//
//	$sql = "insert into mboUserHash (site_id, user_id, pnum) values (";
//	$sql = $sql . "'" . $studsite . "'";
//	$sql = $sql . ", '" . $studuid . "'";
//	$sql = $sql . "," . $pnum . ")";
//
//	if (!($results = mysql_query($sql)))
//	{
//		mboLogError("mboexam.php: SQL Error: SQL=$sql");
//		mboLogError ("DB error: " . mysql_error());
//		mboFatalError();
//	}
//--------------------------------------------------------------


	$sql = "insert into mboExamLog (site_id, sectionId, user_id, examId";
	$sql = $sql . ", updateTime, result, numCorrect, numQuestions, elapsedTimeSecs)";
	$sql = $sql . " values (";
	$sql = $sql . "'" . $studsite . "'";
	$sql = $sql . ", '" . "UNKNOWN" . "'";
	$sql = $sql . ", '" . $studuid . "'";
	$sql = $sql . ", '" . $examnum . "'";
	$sql = $sql . ", CURRENT_TIMESTAMP() ";
	$sql = $sql . ", 'STARTED' ";
	$sql = $sql . ", 0 , 0 , 0 )";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboexam.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	header ("Location: $exExamUrl");

}	

?>
