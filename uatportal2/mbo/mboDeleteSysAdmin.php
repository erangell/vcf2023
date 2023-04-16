<?php
require_once ("mbolib.php");
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
	$admsitedesc  = $admpart[3];
	$admulname  = $admpart[4];
	$admufname  = $admpart[5];
	$admuemail  = $admpart[6];

	include("mboHeader.txt");

	$filtuid = $_GET['ui'];
	$filtusite = $admsite;
	if ($filtuid != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT user_id , siteId '
		. ' FROM '
		. '  mboAdmUser  '
		//. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboDeleteAdmin.php: SQL Error: SQL=$sql");
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
			$filtparm = " WHERE u.user_id = '" . strtoupper($filtuid) . "'";
			$filtdescr = "Administrator";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboDeleteAdmin.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboDeleteAdmin.php");
		mboFatalError();
	}

	$sql = ' SELECT u.lastName, u.firstName '
	. ' , u.emailAdrs  '
	. ' FROM '
	. '   mboAdmUser u '
	;

	IF ($filtactive == "Y")
	{
		$sql = $sql . $filtparm;
	}	

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboDeleteSysAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		mboLogError ("mboDeleteSysAdmin.php: No data found for user");
		mboFatalError();
	}
	else
	{
	        echo "<h1>Confirm Deletion</h1>";

		//echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";

		echo "<h4>";

		while ($row = mysql_fetch_array($results))
		{	
			$valadminid = $filtuid;
			$valadminlast = $row[0];
			$valadminfirst = $row[1];
			$valadminemail = $row[2];
		}

		include ("mboDelSysAdminAft.txt");
	}

	echo '<p>';
	echo '<li><a href="mboArchiveSysAdmin.php?ui=' . $filtuid . '">Delete this Administrator</a>';
	echo '<p>';
	echo '<li><a href="mboMaintSysAdmin.php">Return to System Admin Maintenance</a>';

}

?>
