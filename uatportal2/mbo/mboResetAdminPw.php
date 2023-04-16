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

	$passedErr = $_GET['err'];

	$inputadmid = htmlspecialchars($_GET['ui']);
	$inputadmsiteid = htmlspecialchars($_GET['sy']);

	if (isValidLength($inputadmid, 1, 20) != "Y")
	{
		mboLogError ("badinput: Invalid length (" . strlen($inputadmid) . ") for inputadmid: $inputadmid");
		$edits_ok = "N";
		mboFatalError();
	}
	else
	{
		if (isAlphaNumeric($inputadmid)!="Y") 
		{
			$passedErr = "1012";
			mboLogError ("badinput: Invalid characters in inputadmid($inputadmid): $inputadmid"); 
			$edits_ok = "N";
			mboFatalError();
		}
		else
		{
			$valinputadmid = $inputadmid;
		}
	}

	mboLogError ("audit: RESETTING ADMIN PW: admsite=$admsite admuid=$admuid valinputadmid=$valinputadmid");

	// first check if admuid is a System Administrator
	// if he/she is, then allow maintenance of admins at a different site

	$sa_loggedin = "N";
	$sql = "select 1 from mboAdmRole "
	. " where user_id = '" . strtoupper($admuid) . "'"
	. " and site = '" . $admsite . "'"
	. " and role_id = 'SYSADM'";
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboResetAdminPw.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);		
	if ($numRows == 1)
	{
		$sa_loggedin = "Y";
	}


	$sql = "select user_id, siteId from mboAdmUser "
	. " where user_id = '" . strtoupper($valinputadmid) . "'";
	
	if ($sa_loggedin != "Y")
	{
		$sql = $sql . " and siteId = '" . $admsite . "'";
	}
	if ($inputadmsiteid > "")
	{
		$sql = $sql . " and siteId = '" . $inputadmsiteid . "'";
	}

	//mboLogError("debug: SQL=$sql");

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboResetAdminPw.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);
	//mboLogError ("debug: numrows=$numRows");
		
	if ($numRows == 0)
	{
		mboLogError("mboResetAdminPw.php: Row not found for Administrator - POSSIBLE HACKING");
		mboFatalError();
	}
	else if ($numRows > 1)
	{
		mboLogError("mboResetAdminPw.php: More than 1 row not found for Administrator - CONTACT SYSTEMS SUPPORT");
		mboFatalError();
	}
	else
	{

		$row = mysql_fetch_array($results);
		$userssite = $row[1];

		$valsalt = rand(10000001,99999999);
		$valspin = genTempPw();


		$sql = "update mboAdmSecurity "
		. " SET password = PASSWORD('" . $valsalt . strtoupper($valspin) . $userssite . strtoupper($valinputadmid) . "')"
		. " , pw_salt = $valsalt "
		. " , disabled = 'N'"
		. " , attempts_left = 3 "
		. " , force_new_pw = 'Y' "
		. " WHERE user_id = '" . strtoupper($valinputadmid) . "'"
		. " and site = '" . $userssite . "'"
		;

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboResetAdminPw.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		else
		{
			//mboLogError ("debug: SQL=$sql");

			//mboLogError ("debug: Admin Pw SUCCESSFULLY RESET");
		}

	}

	//mboLogError ("debug: passedErr=$passedErr");

	include("mboHeader.txt");

	echo "<h1>Reset Administrator Password</h1><P>";

	echo "<h3><font color=blue>Administrator's password has been reset.</font></h3>";
		
	echo "<h3>Please give the following information to this administrator:</h3>";
	echo "<li>User ID: $valinputadmid";
	echo "<li>Temporary Password: $valspin";
	echo "<h3>This password is only valid for one login.</h3>";

        echo "<table border=0 cellpadding=5>";
        echo "<tr></tr></table>";
	echo "<h3>Please select one of the following options</h3>";
	//echo '<li><a href="mboMaintInstructor.php">Return to Instructor Maintenance</a>';
	echo '<br><br><li><a href="mboAdmMenu.php">Return to Main Menu</a>';
	echo "</form></center>";

	echo "</table>";
	echo "</html>";
}
?>
