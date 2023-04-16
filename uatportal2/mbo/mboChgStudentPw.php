<?php
require_once ("mbolib.php");
require_once ("mboedits.php");
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
        echo "<h1>Change Password</h1><P>";

	$passedErr = $_GET['err'];


	if (($passedErr != "0001") && ($passedErr != "0003"))
	{

		$stupw1 = htmlspecialchars($_POST['spw1']);
		$stupw2 = htmlspecialchars($_POST['spw2']);

		// Validate user entered data
		$edits_ok = "Y";

		if (isValidLength($stupw2, 6, 20) != "Y")
		{
			$passedErr = "1041";
			//mboLogError ("badinput: Invalid length (" . strlen($stupw2) . ") for stupw2: stuid=$stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($stupw2)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid password for stuid=$stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstupw2 = $stupw2;
			}
		}


		if (isValidLength($stupw1, 6, 20) != "Y")
		{
			$passedErr = "1031";
			//mboLogError ("badinput: Invalid length (" . strlen($stupw1) . ") for stupw1: stuid=$stuid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($stupw1)!="Y") 
			{
				$passedErr = "1032";
				mboLogError ("badinput: Invalid password for stuid=$stuid"); 
				$edits_ok = "N";
			}
			else
			{
				$valstupw1 = $stupw1;
			}
		}

		if ($edits_ok == "Y")	
		{
			if ($valstupw1 != $valstupw2)
			{
				$passedErr = "1043";
				$edits_ok = "N";
			}
			else
			{
				if ( (isValidStudentPw ($valstupw1)) != "Y")
				{
					$passedErr = "1044";
					$edits_ok = "N";
				}
			}
		}

		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: CHANGING STUDENT PASSWORD: stusite=$stusite stuuid=$stuuid ");

			$valsalt = rand(10000001,99999999);

			$sql = "update mboUserSecurity "
			. " SET password = PASSWORD('" . $stusite . strtoupper($stuuid) . $valsalt . strtoupper($valstupw1) . "')"
			. " , pw_salt = $valsalt "
			. " , force_new_pw = 'N' "
			. " , last_new_pw_date = NOW() "
			. " WHERE user_id = '" . strtoupper($stuuid) . "'"
			. " and site = '" . $stusite . "'"
			. " and active = 'Y'"
			;

			$safesql = "update mboUserSecurity "
			. " SET password = PASSWORD('" . $stusite . strtoupper($stuuid) . $valsalt . '********' . "')"
			. " , pw_salt = $valsalt "
			. " , force_new_pw = 'N' "
			. " , last_new_pw_date = NOW() "
			. " WHERE user_id = '" . strtoupper($stuuid) . "'"
			. " and site = '" . $stusite . "'"
			. " and active = 'Y'"
			;
			//(user_id, site, password, active, "
			//. "disabled, attempts_left, force_new_pw, last_new_pw_date, last_failed_attempt, "
			//. "last_login, pw_salt "
			
			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboChgStudentPw.php: SQL Error: SQL=$safesql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			//2006-07-10: Maintain Student Exam Hash Table for use by Sight Singing Exam Applications

			$hashsql = "delete from mboUserExamHash "
			. " WHERE user_id = '" . strtoupper($stuuid) . "'"
			. " and site = '" . strtoupper($stusite) . "'"
			;

			//mboLogError ("debug: " . $hashsql);

			if (!($results = mysql_query($hashsql)))
			{
				mboLogError("mboChgStudentPw.php: SQL Error: SQL=$hashsql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			$smp = array (2,3,5,7,11,13,17,19,23,29
					,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97);

			foreach ($smp as $p)
			{
				$hashstring = strtoupper($stuuid) . strtoupper($stusite) .  strtoupper($valstupw1) . $p ;

				//mboLogError ("debug: hashstring=" . $hashstring);

				$calchexhash = strtoupper(md5( $hashstring ));

				//mboLogError ("debug: " . $p . " hash=" . $calchexhash);

				$hashsql = "insert into mboUserExamHash "
				. " (user_id, site, pnum, hash) "
				. " VALUES ( "
				. "'" . strtoupper($stuuid) . "', "
				. "'" . strtoupper($stusite) . "', "
				. $p . ", "
				. "'" . $calchexhash . "')"
				;
	
				//mboLogError ("debug: " . $p . " hashsql=" . $hashsql);

				if (!($results = mysql_query($hashsql)))
				{
					mboLogError("mboChgStudentPw.php: SQL Error: SQL=$hashsql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}
			}

			$passedErr = "0002";

			//mboLogError ("debug: passedErr=$passedErr");
		}
	}

	//mboLogError ("debug: passedErr=$passedErr");


	if ($passedErr == "0001")
	{
		echo "<h3><font color=blue>Please Enter all data below.</font></h3>";
		$passedErr = "1001";
	}
	else if ($passedErr == "0003")
	{
		echo "<h3><font color=blue>You need to change your password at this time.</font></h3>";
		$passedErr = "1001";
	}
	else
	{ 
		if ($passedErr == "0002")
		{
			echo "<h3><font color=blue>Password changed.</font></h3>";
		}
		if ($passedErr == "1001")
		{
			echo "<h3><font color=red>Error: Invalid Data Was Entered.  Please double-check your input.</font></h3>";
		}
		if ($passedErr == "1031")
		{
			echo "<h3><font color=red>Error: New Password is required and must have at least 6 characters.</font></h3>";
		}
		if ($passedErr == "1032")
		{
			echo "<h3><font color=red>Error: Invalid data in New Password.</font></h3>";
		}
		if ($passedErr == "1041")
		{
			echo "<h3><font color=red>Error: Confirm Password is required and must have at least 6 characters.</font></h3>";
		}
		if ($passedErr == "1042")
		{
			echo "<h3><font color=red>Error: Invalid data in Confirm Password</font></h3>";
		}
		if ($passedErr == "1043")
		{
			echo "<h3><font color=red>Error: New Password does not match Confirm Password</font></h3>";
		}
		if ($passedErr == "1044")
		{
			echo "<h3><font color=red>Error: New Password must contain at least one letter and one number</font></h3>";
		}

	}

	echo '<P><form action="mboChgStudentPw.php?err=';
	echo "$passedErr";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboChgStudentPwGood.txt");
	}
	else
	{
		include("mboChgStudentPwAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
