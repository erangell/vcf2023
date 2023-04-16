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
	echo "<h1>Add Site Administrator</h1><P>";

	$passedErr = $_GET['err'];


	if ($passedErr != "0001") 
	{

		$inputadmid = htmlspecialchars($_POST['sid']);
		$inputadmsiteid = htmlspecialchars($_POST['siteid']);
		$inputadmlast = htmlspecialchars($_POST['slast']);
		$inputadmfirst = htmlspecialchars($_POST['sfirst']);
		$inputadmemail = htmlspecialchars($_POST['semail']);

		// Validate user entered data
		$edits_ok = "Y";

		if (isValidLength($inputadmemail, 1, 40) != "Y")
		{
			$passedErr = "1041";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmemail) . ") for inputadmemail: inputadmid=$inputadmid");
			$edits_ok = "N";
		}
		else
		{
			if (isValidEmail($inputadmemail)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid email address for inputadmemail:($inputadmemail) inputadmid=$inputadmid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmemail = $inputadmemail;
			}
		}

		if (isValidLength($inputadmfirst, 1, 35) != "Y")
		{
			$passedErr = "1031";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmfirst) . ") for inputadmfirst: inputadmid=$inputadmid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmfirst)!="Y") 
			{
				$passedErr = "1032";
				mboLogError ("badinput: Invalid characters in inputadmfirst($inputadmfirst): inputadmid=$inputadmid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmfirst = $inputadmfirst;
			}
		}

		if (isValidLength($inputadmlast, 1, 35) != "Y")
		{
			$passedErr = "1021";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmlast) . ") for inputadmlast: inputadmid=$inputadmid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmlast)!="Y") 
			{
				$passedErr = "1022";
				mboLogError ("badinput: Invalid characters in inputadmlast($inputadmlast): inputadmid=$inputadmid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmlast = $inputadmlast;
			}
		}

		if (isValidLength($inputadmsiteid, 1, 10) != "Y")
		{
			$passedErr = "1061";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmsiteid) . ") for inputadmsiteid: $inputadmsiteid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmsiteid)!="Y") 
			{
				$passedErr = "1062";
				mboLogError ("badinput: Invalid characters in inputadmsiteid($inputadmsiteid): $inputadmsiteid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmsiteid = $inputadmsiteid;
			}
		}


		if (isValidLength($inputadmid, 1, 20) != "Y")
		{
			$passedErr = "1011";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmid) . ") for inputadmid: $inputadmid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmid)!="Y") 
			{
				$passedErr = "1012";
				mboLogError ("badinput: Invalid characters in inputadmid($inputadmid): $inputadmid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmid = $inputadmid;
			}
		}
	
		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: ADDING ADMIN: admsite=$admsite admuid=$admuid valinputadmid=$valinputadmid");

			$sql = "select user_id from mboAdmUser "
			. " where user_id = '" . strtoupper($valinputadmid) . "'"
			. " and siteId = '" . $valinputadmsiteid . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboAddAdmin.php: SQL Error: SQL=$sql");
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

				$sql = "insert into mboAdmUser (user_id, siteId, lastName, firstName, emailAdrs ) values ("
				. "'" . strtoupper($valinputadmid) . "'"
				. ",'" . strtoupper($valinputadmsiteid) . "'"
				. ",'" . $valinputadmlast . "'"
				. ",'" . $valinputadmfirst . "'"
				. ",'" . $valinputadmemail . "')";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddAdmin.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				$sql = "insert into mboAdmSecurity (user_id, site, password, active, "
				. "disabled, attempts_left, force_new_pw, last_new_pw_date, last_failed_attempt, "
				. "last_login, pw_salt "
				. ") values ("
				. "'" . strtoupper($valinputadmid) . "'"
				. ",'" . strtoupper($valinputadmsiteid) . "'"
				. ',PASSWORD("' . $valsalt . strtoupper($valspin) . strtoupper($valinputadmsiteid) . strtoupper($valinputadmid) . '")'
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
					mboLogError("mboAddAdmin.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}


				$sql = "insert into mboAdmRole (user_id, site, role_id ) values ("
				. "'" . strtoupper($valinputadmid) . "'"
				. ",'" . strtoupper($valinputadmsiteid) . "'"
				. ",'SITEADM'"
				. ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddAdmin.php: SQL Error: SQL=$sql");
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

			echo "<li>Site: $valinputadmsiteid";
			echo "<li>Last Name: $valinputadmlast";
			echo "<li>First Name: $valinputadmfirst";
			echo "<li>Email Address: $valinputadmemail";
			
			echo "<h3>Please give the following information to this Site Administrator:</h3>";
			echo "<li>User ID: $valinputadmid";
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
		if ($passedErr == "1061")
		{
			echo "<h3><font color=red>Error: Site is required.</font></h3>";
		}
		if ($passedErr == "1062")
		{
			echo "<h3><font color=red>Error: Invalid data in Site.</font></h3>";
		}


	}

	echo '<P><form action="mboAddSiteAdmin.php?err=';
	echo "$passedErr";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboAddSiteAdmGood.txt");
	}
	else
	{
		include("mboAddSiteAdmAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
