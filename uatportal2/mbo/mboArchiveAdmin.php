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
			mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$sql");
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
			$filtparm = " and user_id = '" . $filtuid . "'";
			$filtdescr = "Administrator";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboArchiveAdmin.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboArchiveAdmin.php");
		mboFatalError();
	}

	mboLogError ("audit: DELETING ADMIN: admsite=$admsite admuid=$admuid filtuid=$filtuid");


	$arcvsql = ' INSERT INTO mboArcvAdmRole (archive_time, '
	. ' user_id, site , role_id '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' user_id, site , role_id '
	. ' FROM mboAdmRole '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboAdmRole '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}


	$arcvsql = ' INSERT INTO mboArcvAdmSecurity (archive_time, '
	. ' user_id, site, password, active, disabled, attempts_left, '
	. ' force_new_pw, last_new_pw_date, last_failed_attempt, '
	. ' last_login, pw_salt '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' user_id, site, password, active, disabled, attempts_left, '
	. ' force_new_pw, last_new_pw_date, last_failed_attempt, '
	. ' last_login, pw_salt '
	. ' FROM '
	. '   mboAdmSecurity '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboAdmSecurity '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$arcvsql = ' INSERT INTO mboArcvAdmUser (archive_time, '
	. ' user_id, siteId, lastName, firstName, emailAdrs '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' user_id, siteId, lastName, firstName, emailAdrs '
	. ' FROM '
	. '   mboAdmUser '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboAdmUser '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	header ("Location: mboAdmMenu.php");
}

?>
