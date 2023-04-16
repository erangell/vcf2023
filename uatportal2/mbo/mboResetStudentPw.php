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


	$stuid = htmlspecialchars($_GET['ui']);

	if (isValidLength($stuid, 1, 20) != "Y")
	{
		mboLogError ("badinput: Invalid length (" . strlen($stuid) . ") for stuid: $stuid");
		$edits_ok = "N";
		mboFatalError();
	}
	else
	{
		if (isAlphaNumeric($stuid)!="Y") 
		{
			$passedErr = "1012";
			mboLogError ("badinput: Invalid characters in stuid($stuid): $stuid"); 
			$edits_ok = "N";
			mboFatalError();
		}
		else
		{
			$valstuid = $stuid;
		}
	}

	mboLogError ("audit: RESETTING STUDENT PW: admsite=$admsite admuid=$admuid valstuid=$valstuid");

	$sql = "select user_id from mboUser "
	. " where user_id = '" . strtoupper($valstuid) . "'"
	. " and siteId = '" . $admsite . "'";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboResetStudentPw.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);
	//mboLogError ("debug: numrows=$numRows");
		
	if ($numRows != 1)
	{
		mboLogError("mboResetStudentPw.php: Row not found for Student - POSSIBLE HACKING");
		mboFatalError();
	}
	else
	{

		$valsalt = rand(10000001,99999999);
		$valspin = genTempPw();


		$sql = "update mboUserSecurity "
		. " SET password = PASSWORD('" . $admsite . strtoupper($valstuid) . $valsalt . strtoupper($valspin) . "')"
		. " , pw_salt = $valsalt "
		. " , disabled = 'N'"
		. " , attempts_left = 3 "
		. " , force_new_pw = 'Y' "
		. " WHERE user_id = '" . strtoupper($valstuid) . "'"
		. " and site = '" . $admsite . "'"
		;

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboResetStudentPw.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
	}

	//mboLogError ("debug: passedErr=$passedErr");

	include("mboHeader.txt");

	echo "<h1>Reset Student Password</h1><P>";

	echo "<h3><font color=blue>Student's password has been reset.</font></h3>";
		
	echo "<h3>Please give the following information to the student:</h3>";
	echo "<li>User ID: $valstuid";
	echo "<li>Temporary Password: $valspin";
	echo "<h3>This password is only valid for one login.</h3>";

	include("mboResetStudentPWGood.txt");
	
	echo "</table>";
	echo "</html>";
}
?>
