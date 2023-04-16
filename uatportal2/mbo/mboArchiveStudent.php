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
		. '  mboUser  '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboArchiveStudent.php: SQL Error: SQL=$sql");
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
			$filtdescr = "Student";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid user id in querystring (" . $filtuid . ") in mboArchiveStudent.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring n mboArchiveStudent.php");
		mboFatalError();
	}

	mboLogError ("audit: DELETING STUDENT: admsite=$admsite admuid=$admuid filtuid=$filtuid");


	$arcvsql = ' INSERT INTO mboArcvExamLog (archive_time, '
	. ' site_id, sectionId, user_id, examId, updateTime, result, '
	. ' numCorrect,	numQuestions, elapsedTimeSecs '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' site_id, sectionId, user_id, examId, updateTime, result, '
	. ' numCorrect,	numQuestions, elapsedTimeSecs '
	. ' FROM mboExamLog '
	. ' WHERE site_id = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboExamLog '
	. ' WHERE site_id = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$arcvsql = ' INSERT INTO mboArcvUserSecurity (archive_time, '
	. ' user_id, site, password, active, disabled, attempts_left, '
	. ' force_new_pw, last_new_pw_date, last_failed_attempt, last_login, pw_salt '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' user_id, site, password, active, disabled, attempts_left, '
	. ' force_new_pw, last_new_pw_date, last_failed_attempt, last_login, pw_salt '
	. ' FROM '
	. '   mboUserSecurity '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboUserSecurity '
	. ' WHERE site = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}


	$arcvsql = ' INSERT INTO mboArcvUser (archive_time, '
	. ' user_id, siteId, lastName, firstName, emailAdrs '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' user_id, siteId, lastName, firstName, emailAdrs '
	. ' FROM '
	. '   mboUser '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboUser '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}


	//2005-08-14: ARCHIVE MBOROSTER WHEN STUDENT DELETED

	$arcvsql = ' INSERT INTO mboArcvRoster (archive_time, '
	. ' siteId , courseId, sectionId, studentId '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' siteId , courseId, sectionId, studentId '
	. ' FROM '
	. '   mboRoster '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and studentId = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboRoster '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. ' and studentId = ' . "'" . strtoupper($filtuid) . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveStudent.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	header ("Location: mboMaintStudent.php");
}

?>
