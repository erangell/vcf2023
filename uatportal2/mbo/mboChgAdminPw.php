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
        echo "<h1>Change Password</h1><P>";

	$passedErr = $_GET['err'];


	if (($passedErr != "0001") && ($passedErr != "0003"))
	{

		$admpw1 = htmlspecialchars($_POST['spw1']);
		$admpw2 = htmlspecialchars($_POST['spw2']);

		// Validate user entered data
		$edits_ok = "Y";

		if (isValidLength($admpw2, 6, 20) != "Y")
		{
			$passedErr = "1041";
			//mboLogError ("badinput: Invalid length (" . strlen($admpw2) . ") for admpw2: admid=$admid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($admpw2)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid password for admid=$admid"); 
				$edits_ok = "N";
			}
			else
			{
				$valadmpw2 = $admpw2;
			}
		}


		if (isValidLength($admpw1, 6, 20) != "Y")
		{
			$passedErr = "1031";
			//mboLogError ("badinput: Invalid length (" . strlen($admpw1) . ") for admpw1: admid=$admid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($admpw1)!="Y") 
			{
				$passedErr = "1032";
				mboLogError ("badinput: Invalid password for admid=$admid"); 
				$edits_ok = "N";
			}
			else
			{
				$valadmpw1 = $admpw1;
			}
		}

		if ($edits_ok == "Y")	
		{
			if ($valadmpw1 != $valadmpw2)
			{
				$passedErr = "1043";
				$edits_ok = "N";
			}
			else
			{
				if ( (isValidAdminPw ($valadmpw1)) != "Y")
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

			mboLogError ("audit: CHANGING ADMIN PASSWORD: admsite=$admsite admuid=$admuid ");

			$valsalt = rand(10000001,99999999);

			$sql = "update mboAdmSecurity "
			. " SET password = PASSWORD('" . $valsalt . strtoupper($valadmpw1) . $admsite . strtoupper($admuid) . "')"
			. " , pw_salt = $valsalt "
			. " , force_new_pw = 'N' "
			. " , last_new_pw_date = NOW() "
			. " WHERE user_id = '" . strtoupper($admuid) . "'"
			. " and site = '" . $admsite . "'"
			. " and active = 'Y'"
			;
			$safeql = "update mboAdmSecurity "
			. " SET password = PASSWORD('" . $valsalt . '********' . $admsite . strtoupper($admuid) . "')"
			. " , pw_salt = $valsalt "
			. " , force_new_pw = 'N' "
			. " , last_new_pw_date = NOW() "
			. " WHERE user_id = '" . strtoupper($admuid) . "'"
			. " and site = '" . $admsite . "'"
			. " and active = 'Y'"
			;

			//(user_id, site, password, active, "
			//. "disabled, attempts_left, force_new_pw, last_new_pw_date, last_failed_attempt, "
			//. "last_login, pw_salt "
			
			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboChgAdminPw.php: SQL Error: SQL=$safesql");
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
			echo "<h3><font color=red>Error: New Password is required and must contain at least 6 characters.</font></h3>";
		}
		if ($passedErr == "1032")
		{
			echo "<h3><font color=red>Error: Invalid data in New Password.</font></h3>";
		}
		if ($passedErr == "1041")
		{
			echo "<h3><font color=red>Error: Confirm Password is required and must contain at least 6 characters.</font></h3>";
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

	echo '<P><form action="mboChgAdminPw.php?err=';
	echo "$passedErr";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboChgAdminPwGood.txt");
	}
	else
	{
		include("mboChgAdminPwAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
