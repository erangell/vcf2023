<?php
require_once ("mbolib.php");
require_once ("mboedits.php");
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

	include("mboHeader.txt");
	echo "<h1>Add Student</h1><P>";

	$passedErr = $_GET['err'];

	$passedCourse = $_GET['c'];
	$passedSection = $_GET['s'];

	$passedCsect = $_GET['csect'];
	if ($passedCsect != "")
	{
		$cpart = explode ('|',$passedCsect);
		$passedCourse = $cpart[0];
		$passedSection = $cpart[1];
	}

	//Validate querystring to prevent sql injection attack
	$sql = ' SELECT DISTINCT courseId '
	. ' FROM '
	. '  mboSiteCourse  '
	. "  where siteId = '" . $admsite . "'"
	;
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$good_course = 'N';
	while ($row = mysql_fetch_array($results))
	{
		if ($passedCourse == $row[0])
		{
			$good_course = 'Y';
		}
	}
	if ($good_course != 'Y')
	{
		mboLogError ("badinput: POSSIBLE HACKING - Invalid course id in querystring (" . $passedCourse . ") in mboAddStudent.php");
		mboFatalError();
	}
	$sql = ' SELECT DISTINCT sectionId '
	. ' FROM '
	. '  mboSection  '
	. "  where siteId = '" . $admsite . "'"
	. "  and courseId = '" . $passedCourse . "'"
	;
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$good_sect = 'N';
	while ($row = mysql_fetch_array($results))
	{
		if ($passedSection == $row[0])
		{
			$good_sect = 'Y';
		}
	}
	if ($good_sect != 'Y')
	{
		mboLogError ("badinput: POSSIBLE HACKING - Invalid section id in querystring (" . $passedSection . ") in mboAddStudent.php");
		mboFatalError();
	}

	if ($passedErr != "0001") 
	{

		$stuid = htmlspecialchars($_POST['sid']);
		$stulast = htmlspecialchars($_POST['slast']);
		$stufirst = htmlspecialchars($_POST['sfirst']);
		$stuemail = htmlspecialchars($_POST['semail']);

		// Validate user entered data
		$edits_ok = "Y";

		if (isValidLength($stuemail, 1, 40) != "Y")
		{
			$passedErr = "1041";
			mboLogError ("badinput: Invalid length (" . strlen($stuemail) . ") for stuemail: stuid=$stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isValidEmail($stuemail)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid email address for stuemail:($stuemail) stuid=$stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstuemail = $stuemail;
			}
		}

		if (isValidLength($stufirst, 1, 35) != "Y")
		{
			$passedErr = "1031";
			mboLogError ("badinput: Invalid length (" . strlen($stufirst) . ") for stufirst: stuid=$stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($stufirst)!="Y") 
			{
				$passedErr = "1032";
				mboLogError ("badinput: Invalid characters in stufirst($stufirst): stuid=$stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstufirst = $stufirst;
			}
		}

		if (isValidLength($stulast, 1, 35) != "Y")
		{
			$passedErr = "1021";
			mboLogError ("badinput: Invalid length (" . strlen($stulast) . ") for stulast: stuid=$stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($stulast)!="Y") 
			{
				$passedErr = "1022";
				mboLogError ("badinput: Invalid characters in stulast($stulast): stuid=$stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstulast = $stulast;
			}
		}

		if (isValidLength($stuid, 1, 20) != "Y")
		{
			$passedErr = "1011";
			mboLogError ("badinput: Invalid length (" . strlen($stuid) . ") for stuid: $stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($stuid)!="Y") 
			{
				$passedErr = "1012";
				mboLogError ("badinput: Invalid characters in stuid($stuid): $stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstuid = $stuid;
			}
		}
	
		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: ADDING STUDENT: admsite=$admsite admuid=$admuid valstuid=$valstuid passedCourse=$passedCourse passedSection=$passedSection");

			$sql = "select user_id from mboUser "
			. " where user_id = '" . strtoupper($valstuid) . "'"
			. " and siteId = '" . $admsite . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			$numRows = mysql_num_rows($results);
			//mboLogError ("debug: numrows=$numRows");
		
			if ($numRows != 0)
			{
				$passedErr = "1051";
			}
			else
			{

				$valsalt = rand(10000001,99999999);
				$valspin = genTempPw();

				$sql = "insert into mboUser (user_id, siteId, lastName,	firstName, emailAdrs ) values ("
				. "'" . strtoupper($valstuid) . "'"
				. ",'" . $admsite . "'"
				. ",'" . $valstulast . "'"
				. ",'" . $valstufirst . "'"
				. ",'" . $valstuemail . "')";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				$sql = "insert into mboUserSecurity (user_id, site, password, active, "
				. "disabled, attempts_left, force_new_pw, last_new_pw_date, last_failed_attempt, "
				. "last_login, pw_salt "
				. ") values ("
				. "'" . strtoupper($valstuid) . "'"
				. ",'" . $admsite . "'"
				. ',PASSWORD("' . $admsite . strtoupper($valstuid) . $valsalt . strtoupper($valspin) . '")'
				. ",'Y'"
				. ",'N'"
				. ", 3 "
				. ",'Y'"
				. ", NULL"
				. ", NULL"
				. ", NULL"
				. "," . $valsalt . ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				
				$sql = "insert into mboRoster (siteId, courseId, sectionId, studentId ) values ("
				. "'" . $admsite . "'"
				. ",'" . $passedCourse . "'"
				. ",'" . $passedSection . "'"
				. ",'" . strtoupper($valstuid) . "')";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddStudent.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}


				$passedErr = "0002";
			}
			//mboLogError ("debug: passedErr=$passedErr");
		}
	}

	//mboLogError ("debug: passedErr=$passedErr");


	if ($passedErr == "0001")
	{
		echo "<h3><font color=blue>Please Enter all data below.</font></h3>";
		$passedErr = "1001";
	}
	else
	{ 
		if ($passedErr == "0002")
		{
			echo "<h3><font color=blue>Record added.</font></h3>";

			echo "<li>Last Name: $valstulast";
			echo "<li>First Name: $valstufirst";
			echo "<li>Email Address: $valstuemail";
			
			echo "<h3>Please give the following information to this student:</h3>";
			echo "<li>User ID: $valstuid";
			echo "<li>Temporary Password: $valspin";
			echo "<h3>This password is only valid for one login.</h3>";

		}
		if ($passedErr == "1001")
		{
			echo "<h3><font color=red>Error: Invalid Data Was Entered.  Please double-check your input.</font></h3>";
		}
		if ($passedErr == "1011")
		{
			echo "<h3><font color=red>Error: User ID is required.</font></h3>";
		}
		if ($passedErr == "1012")
		{
			echo "<h3><font color=red>Error: Invalid data in User ID.</font></h3>";
		}
		if ($passedErr == "1021")
		{
			echo "<h3><font color=red>Error: Last Name is required.</font></h3>";
		}
		if ($passedErr == "1022")
		{
			echo "<h3><font color=red>Error: Invalid data in Last Name.</font></h3>";
		}
		if ($passedErr == "1031")
		{
			echo "<h3><font color=red>Error: First Name is required.</font></h3>";
		}
		if ($passedErr == "1032")
		{
			echo "<h3><font color=red>Error: Invalid data in First Name.</font></h3>";
		}
		if ($passedErr == "1041")
		{
			echo "<h3><font color=red>Error: Email Address is required.</font></h3>";
		}
		if ($passedErr == "1042")
		{
			echo "<h3><font color=red>Error: Invalid Email Address.  Must contain one \"@\" and at least one \".\"</font></h3>";
		}
		if ($passedErr == "1051")
		{
			echo "<h3><font color=red>Error: Record already exists for User ID.</font></h3>";
		}

	}

	echo '<P><form action="mboAddStudent.php?err=';
	echo "$passedErr";
	echo '&c=';
	echo "$passedCourse";
	echo '&s=';
	echo "$passedSection";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboAddStudentGood.txt");
	}
	else
	{
		include("mboAddStudentAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
