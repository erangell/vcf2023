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
        echo "<h1>Edit Adminstrator</h1><P>";

	$filtuid = htmlspecialchars($_GET['ui']);
	$inputadmsiteid = htmlspecialchars($_GET['sy']);

	$filtusite = $admsite;
	if ($filtuid != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT siteId '
		. ' FROM '
		. '  mboSite  '
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditSiteAdmin.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_site = 'N';
		while ($row = mysql_fetch_array($results))
		{
			if ($inputadmsiteid == $row[0])
			{
				$good_site = 'Y';
			}
		}

		if ($good_site != 'Y')
		{
			mboLogError ("badinput: Invalid site id in querystring (" . $inputadmsiteid . ") in mboEditSiteAdmin.php");
			mboFatalError();
		}


		$sql = ' SELECT DISTINCT user_id, siteId '
		. ' FROM '
		. '  mboAdmUser  '
		. ' WHERE siteId = ' . "'" . $inputadmsiteid . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditSiteAdmin.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_user = 'N';
		while ($row = mysql_fetch_array($results))
		{
			if ($filtuid == $row[0])
			{
				$good_user = 'Y';
				$filtusite = $row[1];
				$inputadmsite = $filtusite;
				$valadmsite = $inputadmsite;
			}
		}
		if ($good_user == 'Y')
		{
			$filtparm = " AND u.user_id = '" . strtoupper($filtuid) . "'";
			$filtdescr = "Administrator";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboEditSiteAdmin.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboEditSiteAdmin.php");
		mboFatalError();
	}


	$passedErr = $_GET['err'];

	if ($passedErr != "0001") 
	{

		$inputadmid = $filtuid;
		$inputadmlast = htmlspecialchars($_POST['slast']);
		$inputadmfirst = htmlspecialchars($_POST['sfirst']);
		$inputadmemail = htmlspecialchars($_POST['semail']);
		$inputadmflagsysadm = htmlspecialchars($_POST['sflagsysadm']);

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
				$valadmemail = $inputadmemail;
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
				$valadmfirst = $inputadmfirst;
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
				$valadmlast = $inputadmlast;
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
				$valadmid = $inputadmid;
			}
		}


		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: UPDATING ADMIN: admsite=$admsite admuid=$admuid valadmid=$valadmid");

			$sql = "UPDATE mboAdmUser "
			. " SET lastName = '" . $valadmlast . "'"
			. " , firstName = '" . $valadmfirst . "'"
			. " , emailAdrs = '" . $valadmemail . "'"
			. " where user_id = '" . strtoupper($valadmid) . "'"
			. " and siteId = '" . $valadmsite . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboEditSiteAdmin.php: SQL Error: SQL=$sql");
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
		. '   mboAdmUser u '
		. ' WHERE siteId = ' . "'" . $inputadmsiteid . "'"
		;
	
		IF ($filtactive == "Y")
		{
			$sql = $sql . $filtparm;
		}	

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditSiteAdmin.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		#echo "Number of rows: ";
		$numrows = mysql_num_rows($results);
	
		if ($numrows == 0)
		{	
			mboLogError ("mboEditSiteAdmin.php: No data found for user");
			mboFatalError();
		}
		else
		{
			while ($row = mysql_fetch_array($results))
			{	
				$valadmid = $filtuid;
				$valadmlast = $row[0];
				$valadmfirst = $row[1];
				$valadmemail = $row[2];
			}
		}

		$passedErr = "1001";
	}
	else
	{ 
		if ($passedErr == "0002")
		{
			echo "<h3><font color=blue>Record updated.</font></h3>";

			echo "<li>Last Name: $valadmlast";
			echo "<li>First Name: $valadmfirst";
			echo "<li>Email Address: $valadmemail";
			
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

	echo '<P><form action="mboEditSiteAdmin.php?err=';
	echo "$passedErr";
	echo '&ui=';
	echo $filtuid;
	echo '&sy=';
	echo $inputadmsiteid;

	echo '" method=POST>';

	//mboLogError ("debug: valadmsite=$valadmsite");

	if ($passedErr == "0002")
	{
		include("mboEditSiteAdminGood.txt");
	}
	else
	{
		include("mboEditSiteAdminAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
