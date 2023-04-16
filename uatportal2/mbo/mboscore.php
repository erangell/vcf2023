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
	$trace_debug = "N";

	$studpart = explode('|',$valkey);
	//echo "part 0 = $studpart[0]<br>";
	//echo "part 1 = $studpart[1]<br>";
	//echo "part 2 = $studpart[2]<br>";

	$examrslt = $_GET['r'];
	//echo "examrslt=$examrslt<br>";

	$studsite = $studpart[1];
	$studuid  = $studpart[2];
	//echo "Student Site = $studsite<br>";
	//echo "Student ID = $studuid<br>";


//2006-07-12: NO LONGER NEED TO LOOK FOR PRIME ON USER HASH
//	$sql = "select pnum from mboUserHash where site_id = '" .  $studsite . "' and user_id = '" . strtoupper($studuid) . "'" ; 
//
//	if ($trace_debug == "Y")
//	{
//		mboLogError ("debug: sql=$sql");
//	}
//
//	if (!($results = mysql_query($sql)))
//	{
//		mboLogError("mboscore.php: SQL Error: SQL=$sql");
//		mboLogError ("DB error: " . mysql_error());
//		mboFatalError();
//	}
//
//	$numRows = mysql_num_rows($results);
//	if ($trace_debug == "Y")
//	{
//		mboLogError ("debug: numrows=$numRows");
//	}
//
//	if ($numRows != 1)
//	{	
//		mboLogError("mboscore.php: Row not found on mboUserHash table!");
//		mboFatalError();	
//	}
//	else
//	{
//		$row = mysql_fetch_array($results);
//	}
//
//	$stuprime = $row[0];
//
//	//echo "stuPrime = $stuprime";
//-------------------------------------------------------------------------------

	$sql = "select examId, updateTime from mboExamLog a ";
	$sql = $sql . " where site_id = '" . $studsite . "'";
	$sql = $sql . " and user_id = '" . strtoupper($studuid) . "'";
	$sql = $sql . " and result = 'STARTED' ";
	$sql = $sql . " and updateTime = (select max(updateTime) from mboExamLog b";
	$sql = $sql . "   where	a.site_id = b.site_id and a.user_id = b.user_id";
        $sql = $sql . "   and a.result = b.result)";

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: sql=$sql");
	}
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboscore.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: numrows=$numRows");
	}
	if ($numRows != 1)
	{	
		mboLogError("mboscore.php: Exam row not found on mboExamLog table!");
		mboFatalError();	
	}
	else
	{
		$row = mysql_fetch_array($results);
	}

	$examPrime = $row[0];
	$examStartTime = $row[1];


	$expArr = array (
 709
,719
,727
,733
,739
,743
,751
,757
,761
,769
,773
,787
,797
,809
,811
,821
,823
,827
,829
,839
,853
,857
,859
,863
,877
,881
,883
,887
,907
,911
,919
,929
,937
,941
,947
,953
,967
,971
,977
,983
,991
,997 
	);
	$expPos = array_search ($examPrime, $expArr);

	$hashArr = array (
 1699
,1693
,1667
,1657
,1627
,1619
,1609
,1601
,1583
,1571
,1559
,1549
,1531
,1511
,1493
,1487

,1097
,1109
,1123
,1151
,1163
,1181
,1193
,1213
,1223
,1231
,1249
,1277
,1283
,1291
,1301
,1307
,1321
,1361
,1373
,1399
,1423
,1429
,1439
,1451
,1459
,1481
	);
	$expHash = $hashArr[$expPos];

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: Exam Prime=$examPrime Position=$expPos");
		mboLogError ("debug: examStartTIme=$examStartTime expHash=$expHash");
	}
	$sql = "select courseId, examId, examTitle, examUrl, passUrl, failUrl from mboExam where examId = $examPrime";

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: sql=$sql");
	}

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboscore.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: numrows=$numRows");
	}
	if ($numRows != 1)
	{	
		mboLogError("mboscore.php: Exam row not found on mboExam table!");
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

	$passfail = factor( $examrslt, $expHash, $trace_debug );
	//echo "<br>passfail = $passfail";

	$sql = "insert into mboExamLog (site_id, sectionId, user_id, examId";
	$sql = $sql . ", updateTime, result, numCorrect, numQuestions, elapsedTimeSecs)";
	$sql = $sql . " values (";
	$sql = $sql . "'" . $studsite . "'";
	$sql = $sql . ", 'UNKNOWN' ";
	$sql = $sql . ", '" . $studuid . "'";
	$sql = $sql . ", '" . $examPrime . "'";
	$sql = $sql . ", CURRENT_TIMESTAMP() ";
	$sql = $sql . ", '" . $passfail . "'";
	$sql = $sql . ", 0 , 0 , 0 )";

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: sql=$sql");
	}
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboscore.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	if ($passfail == "PASSED")
	{
		header ("Location: $exPassUrl");
	}
	else
	{
		header ("Location: $exFailUrl");
	}
} // main else

function factor ( $in, $exh, $trace_debug)
{
	//The applet appends the querystring to the url
	//First eliminate the random small prime that the java applet multiplies the
	//result by in order to randomize the querystrings created in the URL.
	//Then, after dividing out that random small prime:
	//If result is the product of 2 primes, the student passed.
	//(But it must have the factor that corresponds to the exam that was taken: $exh)
	//If result is a composite number not the product of 2 primes, the student failed.
	
	$origin = $in;

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: origin=$origin");
	}

	$smp = array (2,3,5,7,11,13,17,19,23,29
		,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97);

	foreach ($smp as $smprime)
	{
		if (($in % $smprime) == 0)
		{
			$in = $in / $smprime;
			break;
		}		
	}

	$stop = sqrt($in);

	if ($trace_debug == "Y")
	{
		mboLogError ("debug: in=$in stop=$stop");
	}

	$numFactors = 0;
	if (($in % 2) == 0)
	{
		//echo "<li>Factor found.  numFactors = $numFactors";		
		$numFactors++;
	}
	for ($i = 3 ; $i < $stop ;$i=$i+2)
	{
		//echo "<li>i=$i";
		if ( ($in % $i) == 0)
		{
			$numFactors++;
			//echo "<li>Factor found.  numFactors = $numFactors";
			$lastfactor = $i;
			
			if ($numFactors > 1)
			{
				break;
			}
		}
	}
	if ($numFactors == 1)
	{
		if ($trace_debug == "Y")
		{
			mboLogError ("debug: lastfactor=$lastfactor");
		}

		if ($lastfactor == $exh)
		{		
			return "PASSED";
		}
		else
		{
			mboLogError ("badinput: POSSIBLE QUERYSTRING HACKING IN mboscore.php: origin=$origin exh= $exh lastfactor=$lastfactor");
			return "FAILED";
		}
	}
	else
	{
		return "FAILED";
	}
}

?>
