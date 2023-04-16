<?php
require_once ("mbolib.php");
mboDBlogin();

echo "<pre>";
echo "BEGIN=mboAppExamResult.php";
echo "</pre>";

$passedResult = $_GET['r'];
$passedSessionHash = $_GET['h'];
$passedExamHash = $_GET['e'];

echo "<pre>";
echo "passedResult=$passedResult";
echo "</pre>";

echo "<pre>";
echo "passedSessionHash=$passedSessionHash";
echo "</pre>";

echo "<pre>";
echo "passedExamHash=$passedExamHash";
echo "</pre>";

$sql = "select User_ID, Site from mboSession where Session_ID = '" . $passedSessionHash . "'";

if (!($results = mysql_query($sql)))
{
	mboLogError("mboAppExamResult.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

$numRows = mysql_num_rows($results);

if ($numRows != 1)
{	
	mboLogError("mboAppExamResults.php: Session ID not found on mboSession table!");
	mboFatalError();	
}
else
{
	$row = mysql_fetch_array($results);
}

$studuid = $row[0];
$studsite = $row[1];

echo "<pre>";
echo "STUDUID=$studuid" ;
echo "</pre>";

echo "<pre>";
echo "STUDSITE=$studsite" ;
echo "</pre>";


$sql = "select examId, updateTime from mboExamLog a ";
$sql = $sql . " where site_id = '" . $studsite . "'";
$sql = $sql . " and user_id = '" . strtoupper($studuid) . "'";
$sql = $sql . " and result = 'STARTED' ";
$sql = $sql . " and updateTime = (select max(updateTime) from mboExamLog b";
$sql = $sql . "   where	a.site_id = b.site_id and a.user_id = b.user_id";
$sql = $sql . "   and a.result = b.result)";

if (!($results = mysql_query($sql)))
{
	mboLogError("mboAppExamResults.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

$numRows = mysql_num_rows($results);

if ($numRows != 1)
{	
	mboLogError("mboAppExamResults.php: Exam row not found on mboExamLog table!");
	mboFatalError();	
}
else
{
	$row = mysql_fetch_array($results);
}

$examPrime = $row[0];
$examStartTime = $row[1];


$expArr = array (
  769,773,787,797,
  809, 811,821,
  823,827,829,839,853,857,859,863,877,881,883,887,907
, 911,919,929,937,941,947,953,967,971,977,983,991,997 
);
$expPos = array_search ($examPrime, $expArr);

$hashArr = array (
  1531,1549,1559,1571,
  1511,1493,1487,	  	  
  1097,1109,1123,1151,1163,1181,1193,1213,1223,1231,1249,1277,1283
, 1291,1301,1307,1321,1361,1373,1399,1423,1429,1439,1451,1459,1481
);
$expHash = $hashArr[$expPos];

$sql = "select courseId, examId, examTitle, examUrl, passUrl, failUrl from mboExam where examId = $examPrime";

if (!($results = mysql_query($sql)))
{
	mboLogError("mboAppExamResults.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

$numRows = mysql_num_rows($results);

if ($numRows != 1)
{	
	mboLogError("mboAppExamResults.php: Exam row not found on mboExam table!");
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

$passfail = factor( $passedResult, $expHash );

echo "<pre>";
echo "PASSFAIL=$passfail";
echo "</pre>";

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

if (!($results = mysql_query($sql)))
{
	mboLogError("mboAppExamResult.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

echo "<pre>";
echo "END=mboAppExamResult.php";
echo "</pre>";


function factor ( $in, $exh)
{
	//echo "factor: in=$in exh=$exh";
	
	//The applet appends the querystring to the url
	//First eliminate the random small prime that the java applet multiplies the
	//result by in order to randomize the querystrings created in the URL.
	//Then, after dividing out that random small prime:
	//If result is the product of 2 primes, the student passed.
	//(But it must have the factor that corresponds to the exam that was taken: $exh)
	//If result is a composite number not the product of 2 primes, the student failed.
	
	$origin = $in;

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
		//echo "numfactors=1 lastfactor=$lastfactor exh=$exh";
		if ($lastfactor == $exh)
		{		
			return "PASSED";
		}
		else
		{
			return "FAILED";
		}
	}
	else
	{
		//echo "numfactors != 1";
		return "FAILED";
	}
}

?>
