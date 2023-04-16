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
        echo "<h1>Edit Student</h1><P>";

	$filtuid = $_GET['ui'];
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
			mboLogError("mboEditStudent.php: SQL Error: SQL=$sql");
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
			$filtparm = " and u.user_id = '" . strtoupper($filtuid) . "'";
			$filtdescr = "Student";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboEditStudent.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboEditStudent.php");
		mboFatalError();
	}


	$passedErr = $_GET['err'];


	if ($passedErr != "0001") 
	{

		$stuid = $filtuid;
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

			mboLogError ("audit: UPDATING STUDENT: admsite=$admsite admuid=$admuid valstuid=$valstuid");

			$sql = "UPDATE mboUser "
			. " SET lastName = '" . $valstulast . "'"
			. " , firstName = '" . $valstufirst . "'"
			. " , emailAdrs = '" . $valstuemail . "'"
			. " where user_id = '" . strtoupper($valstuid) . "'"
			. " and siteId = '" . $admsite . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboEditStudent.php: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}
	

			$passedErr = "0002";
			//mboLogError ("debug: passedErr=$passedErr");
		}
	}

	//mboLogError ("debug: passedErr=$passedErr");


	if ($passedErr == "0001")
	{
		echo "<h3><font color=blue>Please Enter all data below.</font></h3>";

		$sql = ' SELECT u.lastName, u.firstName '
		. ' , u.emailAdrs  '
		. ' FROM '
		. '   mboUser u '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
	
		IF ($filtactive == "Y")
		{
			$sql = $sql . $filtparm;
		}	

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditStudent.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		#echo "Number of rows: ";
		$numrows = mysql_num_rows($results);
	
		if ($numrows == 0)
		{	
			mboLogError ("mboEditStudent.php: No data found for user");
			mboFatalError();
		}
		else
		{
			while ($row = mysql_fetch_array($results))
			{	
				$valstuid = $filtuid;
				$valstulast = $row[0];
				$valstufirst = $row[1];
				$valstuemail = $row[2];
			}
		}

		$passedErr = "1001";
	}
	else
	{ 
		if ($passedErr == "0002")
		{
			echo "<h3><font color=blue>Record updated.</font></h3>";

			echo "<li>Last Name: $valstulast";
			echo "<li>First Name: $valstufirst";
			echo "<li>Email Address: $valstuemail";
			
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

	echo '<P><form action="mboEditStudent.php?err=';
	echo "$passedErr";
	echo '&ui=';
	echo $filtuid;
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboEditStudentGood.txt");
	}
	else
	{
		include("mboEditStudentAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
