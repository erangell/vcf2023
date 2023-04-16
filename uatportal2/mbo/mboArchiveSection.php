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

	$filtuco = $_GET['co'];
	$filtusc = $_GET['se'];
	if (($filtuco != "") && ($filtusc != ""))
	{
		//Validate querystring to prevent sql injection attack
		$sql = ' SELECT DISTINCT courseId, sectionId '
		. ' FROM '
		. '  mboSection '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		;
		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboArchiveSection.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		$good_sect = 'N';
		while ($row = mysql_fetch_array($results))
		{
			if (($filtuco == $row[0]) && ($filtusc == $row[1]))
			{
				$good_sect = 'Y';
			}
		}
		if ($good_sect == 'Y')
		{
			$filtparm = " and courseId = '" . $filtuco . "'"
				  . " and sectionId = '" . $filtusc . "'";
			$filtdescr = "Section";
			$filtactive = "Y";
		}
		else
		{
			mboLogError ("badinput: Invalid section in querystring (" . $filtuco . "/" . $filtusc . ") in mboArchiveSection.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboArchiveSection.php");
		mboFatalError();
	}

	include "mboHeader.txt";
	echo ("<H1>Archive Section</h1>");
	echo ("The following section data will be ARCHIVED for Course: $filtuco Section: $filtusc");
	echo ("<Hr size=1>");
	

	$sql = ' SELECT u.user_id, u.lastName, u.firstName '
	. ' FROM '
	. '  mboUser u, mboRoster r '
	. ' WHERE '
	. '  u.siteId = ' . "'" . strtoupper($admsite) . "'"
	. '  and r.siteId = ' . "'" . strtoupper($admsite) . "'"
	. '  and r.courseId = ' . "'" . $filtuco . "'"
	. '  and r.sectionId = ' . "'" . $filtusc . "'"
	. '  and r.studentId = u.user_id '
	. " ORDER BY  1 "
	;

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboArchiveSection.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numstudents = 0;
	while ($row = mysql_fetch_array($results))
	{
		$filtuid = $row[0];
		
		echo ("<li>STUDENT: $filtuid ($row[1], $row[2])");

		$studentlist[$numstudents] = $filtuid;
		$numstudents++;			
	}

	echo ("<hr size=1>");

	// ARCHIVE EACH STUDENT IN THE ARRAY - NOTE: SAME CODE IN mboArchiveSection

	for ($stuix=0; $stuix < $numstudents ; $stuix++)
	{
		$filtuid = $studentlist[$stuix];


		$arcvsql = ' SELECT '
		. ' site_id, sectionId, user_id, examId, updateTime, result, '
		. ' numCorrect,	numQuestions, elapsedTimeSecs '
		. ' FROM mboExamLog '
		. ' WHERE site_id = ' . "'" . $admsite . "'"
		. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
		;
	
		if (!($results = mysql_query($arcvsql)))
		{
			mboLogError("mboArchiveSection.php: SQL Error: SQL=$arcvsql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		while ($row = mysql_fetch_array($results))
		{
			echo ("<li>EXAM LOG: ($row[2] $row[4])");
		}

	
		$arcvsql = ' SELECT '
		. ' user_id, site, password, active, disabled, attempts_left, '
		. ' force_new_pw, last_new_pw_date, last_failed_attempt, last_login, pw_salt '
		. ' FROM '
		. '   mboUserSecurity '
		. ' WHERE site = ' . "'" . $admsite . "'"
		. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
		;
	

		if (!($results = mysql_query($arcvsql)))
		{
			mboLogError("mboArchiveSection.php: SQL Error: SQL=$arcvsql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		while ($row = mysql_fetch_array($results))
		{
			echo ("<li>USER SCTY: ($row[0])");
		}
	

		$arcvsql = ' SELECT '
		. ' user_id, siteId, lastName, firstName, emailAdrs '
		. ' FROM '
		. '   mboUser '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		. ' and user_id = ' . "'" . strtoupper($filtuid) . "'"
		;

		if (!($results = mysql_query($arcvsql)))
		{
			mboLogError("mboArchiveSection.php: SQL Error: SQL=$arcvsql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		while ($row = mysql_fetch_array($results))
		{
			echo ("<li>USER: ($row[0])");
		}


		$arcvsql = ' SELECT '
		. ' siteId , courseId, sectionId, studentId '
		. ' FROM '
		. '   mboRoster '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		. '  and courseId = ' . "'" . $filtuco . "'"
		. '  and sectionId = ' . "'" . $filtusc . "'"
		. ' and studentId = ' . "'" . strtoupper($filtuid) . "'"
		;

		if (!($results = mysql_query($arcvsql)))
		{
			mboLogError("mboArchiveSection.php: SQL Error: SQL=$arcvsql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		while ($row = mysql_fetch_array($results))
		{
			echo ("<li>ROSTER: ($row[0] $row[1] $row[2] $row[3])");
		}

		echo ("<hr size=1>");
	
	}

	// ARCHIVE THE SECTION

	$arcvsql = ' SELECT '
	. ' siteId, courseId, sectionId, instructorId, sectDescription ' 
	. ' FROM '
	. '   mboSection '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. '  and courseId = ' . "'" . $filtuco . "'"
	. '  and sectionId = ' . "'" . $filtusc . "'"
	;

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboArchiveSection.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	while ($row = mysql_fetch_array($results))
	{
		echo ("<li>SECTION: ($row[0] $row[1] $row[2] $row[3] $row[4])");
	}

	echo ("<hr size=1>");

	echo ('<h4><a href="mboPurgeSection.php?co=' . $filtuco . '&se=' . $filtusc . '">Click here to ARCHIVE this section (NOT UNDOABLE!!!)</a>');

	echo ("<hr size=1>");

	echo ('<h4><a href="mboMaintSection.php">Click here to return to Section Maintenance</a>');

	echo ("<hr size=1>");
}

?>
