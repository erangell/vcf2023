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
        echo "<h1>Edit Site Admin Role</h1><P>";

	$user_already_has_sysadm_role = "N";

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


		$sql = ' SELECT DISTINCT user_id, siteId, lastName, firstName '
		. ' FROM '
		. '  mboAdmUser  '
		. ' WHERE siteId = ' . "'" . $inputadmsiteid . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
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
				$valadmlast = $row[2];
				$valadmfirst = $row[3];

				$sql = ' SELECT 1 '
				. ' FROM '
				. '  mboAdmRole  '
				. " WHERE user_id = '" . strtoupper($filtuid) . "'" 
				. "   and site = '" . $filtusite . "'"
				. "   and role_id = 'SYSADM'";

				if (!($result2 = mysql_query($sql)))
				{
					mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				$numRows = mysql_num_rows($result2);
				if ($numRows == 1)
				{
					$user_already_has_sysadm_role = "Y";
					$valadmflagsysadm = "Y";
				}
				else
				{
					$user_already_has_sysadm_role = "N";
					$valadmflagsysadm = "N";
				}
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
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboEditSiteAdminRole.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboEditSiteAdminRole.php");
		mboFatalError();
	}




	


	$passedErr = $_GET['err'];

	if ($passedErr != "0001") 
	{

		$inputadmid = $filtuid;
		$inputadmflagsysadm = htmlspecialchars($_POST['sflagsysadm']);

		// Validate user entered data
		$edits_ok = "Y";


		if (isValidLength($inputadmflagsysadm, 1, 1) != "Y")
		{
			$passedErr = "1091";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmflagsysadm) . ") for inputadmflagsysadm: $inputadmflagsysadm");
			$edits_ok = "N";
		}
		else
		{
			if (!(($inputadmflagsysadm == "Y") || ($inputadmflagsysadm == "N")))
			{
				$passedErr = "1091";
				mboLogError ("badinput: Invalid characters in inputadmflagsysadm($inputadmflagsysadm): $inputadmflagsysadm"); 
				$edits_ok = "N";
			}
			else
			{
				$valadmflagsysadm = $inputadmflagsysadm;
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

			if ($valadmflagsysadm == "Y")
			{
				mboLogError ("audit: UPDATING ADMIN ROLE: GRANTING SYSTEM ADMIN ROLE FOR USER: admsite=$admsite admuid=$admuid valadmid=$valadmid useralreadyhassysadmrole=$user_already_has_sysadm_role valadmflagsysadm=$valadmflagsysadm ");
			}
			else
			{
				mboLogError ("audit: UPDATING ADMIN ROLE: REVOKING SYSTEM ADMIN ROLE FOR USER: admsite=$admsite admuid=$admuid valadmid=$valadmid useralreadyhassysadmrole=$user_already_has_sysadm_role valadmflagsysadm=$valadmflagsysadm ");
			}

			$passedErr = "0003";

			if ($user_already_has_sysadm_role == "N")
			{

				if ($valadmflagsysadm == "Y")
				{

					$sql = "INSERT INTO mboAdmRole "
					. " ( user_id, site, role_id ) "
					. " VALUES "
					. " ( '" . strtoupper($valadmid) . "'"
					. " , '" . $valadmsite . "'"
					. " , 'SYSADM' )";
					if (!($results = mysql_query($sql)))
					{
						mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
						mboLogError ("DB error: " . mysql_error());
						mboFatalError();

					}
					$passedErr = "0002";
				}

			}
			else
			{
				if ($valadmflagsysadm == "N")
				{

					$sql = "DELETE FROM mboAdmRole "
					. " where user_id =  '" . strtoupper($valadmid) . "'"
					. "   and site =  '" . $valadmsite . "'"
					. "   and role_id = 'SYSADM' ";
					if (!($results = mysql_query($sql)))
					{
						mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
						mboLogError ("DB error: " . mysql_error());
						mboFatalError();
					}
					$passedErr = "0002";
				}
			}


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
			mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		#echo "Number of rows: ";
		$numrows = mysql_num_rows($results);
	
		if ($numrows == 0)
		{	
			mboLogError ("mboEditSiteAdminRole.php: No data found for user");
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
			
		}
		if ($passedErr == "0003")
		{
			echo "<h3><font color=blue>No changes were made.</font></h3>";
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
		if ($passedErr == "1091")
		{
			echo "<h3><font color=red>Error: System Admin Flag must be either Y or N</font></h3>";
		}


	}

	echo '<P><form action="mboEditSiteAdminRole.php?err=';
	echo "$passedErr";
	echo '&ui=';
	echo $filtuid;
	echo '&sy=';
	echo $inputadmsiteid;
	echo '" method=POST>';

	//mboLogError ("debug: valadmsite=$valadmsite");

	if (($passedErr == "0002") || ($passedErr == "0003"))
	{
		include("mboEditSiteAdminRoleGood.txt");
	}
	else
	{
		include("mboEditSiteAdminRoleAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
