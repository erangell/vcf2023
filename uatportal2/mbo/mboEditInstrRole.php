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
        echo "<h1>Edit Instructor Role</h1><P>";

	$user_already_has_siteadm_role = "N";

	$filtuid = htmlspecialchars($_GET['ui']);
	//mboLogError("debug: filtuid=$filtuid");

	if ($filtuid != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT user_id, siteId, lastName, firstName '
		. ' FROM '
		. '  mboAdmUser  '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
		//mboLogError("sql: $sql");

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditInstrRole.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_user = 'N';
		while ($row = mysql_fetch_array($results))
		{
			//mboLogError("debug: filtuid=$filtuid row0=$row[0]");

			if ($filtuid == $row[0])
			{
				$good_user = 'Y';
				$valadmsite = $admsite;
				$valadmlast = $row[2];
				$valadmfirst = $row[3];
			}
			$sql = ' SELECT 1 '
			. ' FROM '
			. '  mboAdmRole  '
			. " WHERE user_id = '" . strtoupper($filtuid) . "'" 
			. "   and site = '" . $admsite . "'"
			. "   and role_id = 'SITEADM'";

			//mboLogError ("debug: sql=$sql");

			if (!($result2 = mysql_query($sql)))
			{
				mboLogError("mboEditSiteAdminRole.php: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			$numRows = mysql_num_rows($result2);
			if ($numRows == 1)
			{
				$user_already_has_siteadm_role = "Y";
				$valadmflagsiteadm = "Y";
			}
			else
			{
				$user_already_has_siteadm_role = "N";
				$valadmflagsiteadm = "N";
			}

		}
		if ($good_user == 'Y')
		{
			$filtparm = " and u.user_id = '" . strtoupper($filtuid) . "'";
			$filtdescr = "Administrator";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboEditInstrRole.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboEditInstrRole.php");
		mboFatalError();
	}


	$passedErr = $_GET['err'];


	if ($passedErr != "0001") 
	{

		$inputadmid = $filtuid;
		$inputadmflagsiteadm = htmlspecialchars($_POST['sflagsiteadm']);

		// Validate user entered data
		$edits_ok = "Y";


		if (isValidLength($inputadmflagsiteadm, 1, 1) != "Y")
		{
			$passedErr = "1091";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmflagsiteadm) . ") for inputadmflagsiteadm: $inputadmflagsiteadm");
			$edits_ok = "N";
		}
		else
		{
			if (!(($inputadmflagsiteadm == "Y") || ($inputadmflagsiteadm == "N")))
			{
				$passedErr = "1091";
				mboLogError ("badinput: Invalid characters in inputadmflagsiteadm($inputadmflagsiteadm): $inputadmflagsiteadm"); 
				$edits_ok = "N";
			}
			else
			{
				$valadmflagsiteadm = $inputadmflagsiteadm;
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

if ($valadmflagsiteadm == "Y")
			{
				mboLogError ("audit: UPDATING ADMIN ROLE: GRANTING SITE ADMIN ROLE FOR USER: admsite=$admsite admuid=$admuid valadmid=$valadmid useralreadyhassiteadmrole=$user_already_has_siteadm_role valadmflagsiteadm=$valadmflagsiteadm ");
			}
			else
			{
				mboLogError ("audit: UPDATING ADMIN ROLE: REVOKING SITE ADMIN ROLE FOR USER: admsite=$admsite admuid=$admuid valadmid=$valadmid useralreadyhassiteadmrole=$user_already_has_siteadm_role valadmflagsiteadm=$valadmflagsiteadm ");
			}


			$passedErr = "0003";

			if ($user_already_has_siteadm_role == "N")
			{

				if ($valadmflagsiteadm == "Y")
				{

					$sql = "INSERT INTO mboAdmRole "
					. " ( user_id, site, role_id ) "
					. " VALUES "
					. " ( '" . strtoupper($valadmid) . "'"
					. " , '" . $valadmsite . "'"
					. " , 'SITEADM' )";
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
				if ($valadmflagsiteadm == "N")
				{

					$sql = "DELETE FROM mboAdmRole "
					. " where user_id =  '" . strtoupper($valadmid) . "'"
					. "   and site =  '" . $valadmsite . "'"
					. "   and role_id = 'SITEADM' ";
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
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
	
		IF ($filtactive == "Y")
		{
			$sql = $sql . $filtparm;
		}	

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboEditInstrRole.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		#echo "Number of rows: ";
		$numrows = mysql_num_rows($results);
	
		if ($numrows == 0)
		{	
			mboLogError ("mboEditInstrRole.php: No data found for user");
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

	echo '<P><form action="mboEditInstrRole.php?err=';
	echo "$passedErr";
	echo '&ui=';
	echo $filtuid;
	echo '" method=POST>';

	if (($passedErr == "0002") || ($passedErr == "0003"))
	{
		include("mboEditInstrRoleGood.txt");
	}
	else
	{
		include("mboEditInstrRoleAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
