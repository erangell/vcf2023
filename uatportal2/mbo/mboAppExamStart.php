<?php

require_once ("mbolib.php");

echo "<pre>";
echo "BEGIN=mboAppExamStart.php";
echo "</pre>";

mboDBlogin();

$passedSite = $_GET['s'];
$passedUser = $_GET['u'];
$passedHash = $_GET['h'];
$examHash   = $_GET['e'];

echo "<pre>";
echo "passedSite=$passedSite";
echo "</pre>";

echo "<pre>";
echo "passedUser=$passedUser";
echo "</pre>";

echo "<pre>";
echo "passedHash=$passedHash";
echo "</pre>";

$sql = ' SELECT hash  ' 
	. ' FROM mboUserExamHash '
	. ' WHERE site = "' . strtoupper($passedSite) . '"'
	. ' AND user_id = "' . strtoupper($passedUser) . '"'
	;

//mboLogError ("sql:$sql");

if (!($results = mysql_query($sql)))
{
	mboLogError("authMbo.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

$numRows = mysql_num_rows($results);
//mboLogError ("debug: numrows=$numRows");
	
if ($numRows == 0)
{	
	echo "<pre>";
	echo "RESULT=ERR";
	echo "</pre>";
	echo "<pre>";
	echo "ERRMSG=You need to change your password online before beginning the exam";
	echo "</pre>";
}
else
{
	$match = 0;

	while ($row = mysql_fetch_array($results))
	{	
		if ($row[0] == $passedHash)
		{
			$match = 1;
		}
	}
	if ($match == 1)
	{

		$sql = "select pnum from mboPrimes where pHash = $examHash";

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboAppExamStart.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());

			echo "<pre>";
			echo "RESULT=ERR";
			echo "</pre>";
			echo "<pre>";
			echo "ERRMSG=A Database Error Has Occurred - please Exit and Retry";
			echo "</pre>";
		}
		else
		{
			$numRows = mysql_num_rows($results);
			//mboLogError ("debug: numrows=$numRows");

			if ($numRows != 1)
			{	
				mboLogError("mboexam.php: Exam ID not found in pHash column!");

				echo "<pre>";
				echo "RESULT=ERR";
				echo "</pre>";
				echo "<pre>";
				echo "ERRMSG=A Configuration Error Has Occurred - please Exit and Retry";
				echo "</pre>";
			}
			else
			{
				$row = mysql_fetch_array($results);
			
				$examnum = $row[0];

				$sql = "insert into mboExamLog (site_id, sectionId, user_id, examId";
				$sql = $sql . ", updateTime, result, numCorrect, numQuestions, elapsedTimeSecs)";
				$sql = $sql . " values (";
				$sql = $sql . "'" . strtoupper($passedSite) . "'";
				$sql = $sql . ", '" . "UNKNOWN" . "'";
				$sql = $sql . ", '" . strtoupper($passedUser) . "'";
				$sql = $sql . ", '" . $examnum . "'";
				$sql = $sql . ", CURRENT_TIMESTAMP() ";
				$sql = $sql . ", 'STARTED' ";
				$sql = $sql . ", 0 , 0 , 0 )";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboexam.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());

					echo "<pre>";
					echo "RESULT=ERR";
					echo "</pre>";
					echo "<pre>";
					echo "ERRMSG=A Database Error Has Occurred - please Exit and Retry";
					echo "</pre>";
				}
				else
				{
					$snum = createMboStudentSession( $passedUser, $passedSite );

					echo "<pre>";
					echo "RESULT=OK";
					echo "</pre>";
					echo "<pre>";
					echo "ERRMSG=Login Successful";
					echo "</pre>";
					echo "<pre>";
					echo "SESSNUM=" . $snum;
					echo "</pre>";

				}
			}
		}
	}
	else
	{
		echo "<pre>";
		echo "RESULT=ERR";
		echo "</pre>";
		echo "<pre>";
		echo "ERRMSG=Invalid Login";
		echo "</pre>";
	}
}


echo "<pre>";
echo "END=mboAppExamStart.php";
echo "</pre>";

?>
