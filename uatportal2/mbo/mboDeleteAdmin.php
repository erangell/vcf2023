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
	if ($filtuid != "")
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT user_id '
		. ' FROM '
		. '  mboAdmUser  '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
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
			}
		}
		if ($good_user == 'Y')
		{
			$filtparm = " and u.user_id = '" . $filtuid . "'";
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
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	;

	IF ($filtactive == "Y")
	{
		$sql = $sql . $filtparm;
	}	

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboDeleteAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		mboLogError ("mboDeleteAdmin.php: No data found for user");
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

		include ("mboDelAdminAft.txt");
	}

	echo '<p>';
	echo '<li><a href="mboArchiveAdmin.php?ui=' . $filtuid . '">Delete this Administrator</a>';
	echo '<p>';
	echo '<li><a href="mboMaintInstructor.php">Return to Instructor Maintenance</a>';

}

?>
