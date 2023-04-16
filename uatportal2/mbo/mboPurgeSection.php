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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
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
			mboLogError ("badinput: Invalid section in querystring (" . $filtuco . "/" . $filtusc . ") in mboPurgeSection.php");
			mboFatalError();
		}
	}
	else
	{
		mboLogError ("badinput: Blank user id in querystring in mboPurgeSection.php");
		mboFatalError();
	}


	mboLogError ("audit: ARCHIVING SECTION: admsite=$admsite admuid=$admuid filtuco=$filtuco filtusc=$filtusc");

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
		mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numstudents = 0;
	while ($row = mysql_fetch_array($results))
	{
		$filtuid = $row[0];
		
		mboLogError ("audit: ARCHIVING STUDENT: admsite=$admsite admuid=$admuid filtuid=$filtuid ($row[1], $row[2])");

		$studentlist[$numstudents] = $filtuid;
		$numstudents++;			
	}

	// ARCHIVE EACH STUDENT IN THE ARRAY - NOTE: SAME CODE IN mboPurgeSection

	for ($stuix=0; $stuix < $numstudents ; $stuix++)
	{
		$filtuid = $studentlist[$stuix];


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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$arcvsql");
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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$arcvsql");
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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$arcvsql");
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
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
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
		. '  and courseId = ' . "'" . $filtuco . "'"
		. '  and sectionId = ' . "'" . $filtusc . "'"
		. ' and studentId = ' . "'" . strtoupper($filtuid) . "'"
		;

		mboLogArchive ($arcvsql);

		if (!($results = mysql_query($arcvsql)))
		{
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$arcvsql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
	
		$sql = ' DELETE '
		. ' FROM '
		. '   mboRoster '
		. ' WHERE siteId = ' . "'" . $admsite . "'"
		. '  and courseId = ' . "'" . $filtuco . "'"
		. '  and sectionId = ' . "'" . $filtusc . "'"
		. ' and studentId = ' . "'" . strtoupper($filtuid) . "'"
		;

		mboLogArchive ($sql);

		if (!($results = mysql_query($sql)))
		{
			mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

	}

	// ARCHIVE THE SECTION

	$arcvsql = ' INSERT INTO mboArcvSection (archive_time, '
	. ' siteId, courseId, sectionId, instructorId, sectDescription ' 
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' siteId, courseId, sectionId, instructorId, sectDescription ' 
	. ' FROM '
	. '   mboSection '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. '  and courseId = ' . "'" . $filtuco . "'"
	. '  and sectionId = ' . "'" . $filtusc . "'"
	;

	mboLogArchive ($arcvsql);

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("mboPurgeSection.php: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sql = ' DELETE '
	. ' FROM '
	. '   mboSection '
	. ' WHERE siteId = ' . "'" . $admsite . "'"
	. '  and courseId = ' . "'" . $filtuco . "'"
	. '  and sectionId = ' . "'" . $filtusc . "'"
	;

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboPurgeSection.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	header ("Location: mboMaintSection.php");
}

?>
