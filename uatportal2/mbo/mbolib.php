<?php

// we will do our own error handling
error_reporting(0);
set_error_handler (mboPhpErrorHandler);

require_once ("mboconfig.php");

function mboGetFatalErrMsg ( $debugmsg )
{
	$mboFatalErrMsgProd = "<h1>Fatal Application Error</h1><p>"
	. "<h3>An application error has occurred which prevented processing of your request."
	. " <p> Please report this problem to your systems support representative."
	. " <p> Please exit your browser at this time. 	</h3>";

	// For Production
	return $mboFatalErrMsgProd;
	
	// For Development debugging
	//return "Fatal Error: " . $debugmsg;
}

function mboLogError ( $err )
// Adapted from PHP Manual sample code for fwrite
{
	//$mboInstallPath = "/phpdev/mboadm/";
	$mboInstallPath = mboGetInstallPath();

	$mboLogPath = $mboInstallPath . "logs/" ;

	if (substr($err,0,6) == "debug:")
	{
		$mboErrLog = $mboLogPath . "mboDebug.txt" ;
	}
	else if (substr($err,0,4) == "sql:")
	{
		$mboErrLog = $mboLogPath . "mboSqlErr.txt" ;
	}
	else if (substr($err,0,9) == "badinput:")
	{
		$mboErrLog = $mboLogPath . "mboBadInput.txt" ;
	}
	else if (substr($err,0,9) == "badlogin:")
	{
		$mboErrLog = $mboLogPath . "mboBadLogin.txt" ;
	}
	else if (substr($err,0,7) == "access:")
	{
		$mboErrLog = $mboLogPath . "mboAccess.txt" ;
	}
	else if (substr($err,0,6) == "audit:")
	{
		$mboErrLog = $mboLogPath . "mboAudit.txt" ;
	}
	else
	{
		$mboErrLog = $mboLogPath . "mboError.txt" ;
	}

	if (is_writable($mboErrLog)) {

		if (!$handle = fopen($mboErrLog, 'a')) {
         		die (mboGetFatalErrMsg ("Cannot open file ($mboErrLog)"));
    		}


		if (fwrite($handle, date("Y-m-d H:i:s" . "|")) === FALSE) {
        		die (mboGetFatalErrMsg ("Cannot write to file ($mboErrLog)"));
    		}

		if (fwrite($handle, $err . "\r\n") === FALSE) {
        		die (mboGetFatalErrMsg ("Cannot write to file ($mboErrLog)"));
    		}
       
		fclose($handle);

	} else {
    		die (mboGetFatalErrMsg ("The file $mboErrLog is not writable"));
	}
}


function mboLogArchive ( $sql )
{
	$mboInstallPath = mboGetInstallPath();

	$mboLogPath = $mboInstallPath . "logs/" ;
	$mboArcvLog = $mboLogPath . "mboArchive.txt" ;
	
	if (is_writable($mboArcvLog)) {

		if (!$handle = fopen($mboArcvLog, 'a')) {
         		die (mboGetFatalErrMsg ("Cannot open file ($mboArcvLog)"));
    		}


		if (fwrite($handle, date("Y-m-d H:i:s" . "|")) === FALSE) {
        		die (mboGetFatalErrMsg ("Cannot write to file ($mboArcvLog)"));
    		}

		if (fwrite($handle, $sql . "\r\n") === FALSE) {
        		die (mboGetFatalErrMsg ("Cannot write to file ($mboArcvLog)"));
    		}
       
		fclose($handle);

	} else {
    		die (mboGetFatalErrMsg ("The file $mboArcvLog is not writable"));
	}
}


function redirect($url)
// adapted from reference #1, p. 517
{
	if (!headers_sent())
	{
		header ('Location: http://' 
		. $_SERVER['HTTP_HOST']
		. dirname ( $_SERVER['PHP_SELF'] )
		. '/' . $url );
	}
	else
	{
		mboLogError("redirect: Could not redirect; Headers already sent (output).");
		die (mboGetFatalErrMsg("redirect: Could not redirect; Headers already sent (output)."));
	}
}

function mboFatalError()
{
	if (file_exists("mboFatal.htm"))
	{
		redirect("mboFatal.htm");			
	}
	else
	{	
		mboLogError("mboFatalError: File not found: mboFatal.htm");
		die(mboGetFatalErrMsg("mboFatalError: File not found: mboFatal.htm"));		
	}
}


function mboDBLogin ()
{
	$mboInstallPath = mboGetInstallPath();

	$passfile = $mboInstallPath . "security/mbopass.fyl";

	$db_type = 'mysql';

	if (file_exists($passfile))
	{
		if (!$handle = fopen($passfile, 'r')) {
	       		mboLogError ("mboDBLogin: Cannot open file ($passfile)");
			mboFatalError();
  		}

		$passdata = fgets($handle,80);

		list($hlit,$dbhost,$dlit,$dbdb, $ulit,$dbuser,$plit,$dbpass) = split("~",$passdata);

		if ($db_type == 'mysql')
		{
			if (!($conn = mysql_connect ($dbhost, $dbuser, $dbpass)))					
			{
		       		mboLogError ("mboDBLogin: Cannot connect to $db_type database");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}
			if (!(mysql_select_db ($dbdb, $conn) ))
			{
		       		mboLogError ("mboDBLogin: Cannot connect to $db_type database: $dbdb");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();		
			}
		}
		else
		{
			mboLogError ("mboDBLogin: Unimplemented db_type: $db_type");
			mboFatalError();
		}

	}
	else
	{	
		mboLogError("mboDBLogin: File not found: $passfile");
		mboFatalError();
	}
}



function mboPhpErrorHandler($errno, $errmsg, $filename, $linenum, $vars) 
// adapted from PHP Manual
{
    // timestamp for the error entry
    $dt = date("Y-m-d H:i:s (T)");

    // define an assoc array of error string
    // in reality the only entries we should
    // consider are E_WARNING, E_NOTICE, E_USER_ERROR,
    // E_USER_WARNING and E_USER_NOTICE
    $errortype = array (
                E_ERROR           => "Error",
                E_WARNING         => "Warning",
                E_PARSE           => "Parsing Error",
                E_NOTICE          => "Notice",
                E_CORE_ERROR      => "Core Error",
                E_CORE_WARNING    => "Core Warning",
                E_COMPILE_ERROR   => "Compile Error",
                E_COMPILE_WARNING => "Compile Warning",
                E_USER_ERROR      => "User Error",
                E_USER_WARNING    => "User Warning",
                E_USER_NOTICE     => "User Notice",
                E_STRICT          => "Runtime Notice"
                );
    // set of errors for which a var trace will be saved
    $user_errors = array(E_USER_ERROR, E_USER_WARNING, E_USER_NOTICE);
    
    $err = "<errorentry>\n";
    $err .= "\t<datetime>" . $dt . "</datetime>\n";
    $err .= "\t<errornum>" . $errno . "</errornum>\n";
    $err .= "\t<errortype>" . $errortype[$errno] . "</errortype>\n";
    $err .= "\t<errormsg>" . $errmsg . "</errormsg>\n";
    $err .= "\t<scriptname>" . $filename . "</scriptname>\n";
    $err .= "\t<scriptlinenum>" . $linenum . "</scriptlinenum>\n";

    if (in_array($errno, $user_errors)) {
        $err .= "\t<vartrace>" . wddx_serialize_value($vars, "Variables") . "</vartrace>\n";
    }
    $err .= "</errorentry>\n\n";
    
    // for testing
    // echo $err;

    // save to the error log, and e-mail me if there is a critical user error
    //error_log($err, 3, "/usr/local/php4/error.log");

    if ($errortype[$errno] != "Notice")
    {
    	mboLogError ( $err );
    }
}

function createMboStudentSession( $stuId, $passedSite )
{			
	$sql = ' SELECT session_num FROM mboNextSessionId ';
	//mboLogError ("debug: SQL=$sql");
	
	if (!($results = mysql_query($sql)))
	{
		mboLogError("createMboStudentSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);
	if ($numRows == 0)
	{
		$nextSessNum = 1;
		$sql = ' INSERT INTO mboNextSessionId (session_num) values (1)';
	  	if (!($results = mysql_query($sql)))
	  	{
			mboLogError("createMboStudentSession: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
	  	}
	}
	else if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);
		$nextSessNum = $row[0] + 1;

		$sql = ' UPDATE mboNextSessionId set session_num = ' . $nextSessNum ;
	  	if (!($results = mysql_query($sql)))
	  	{
			mboLogError("createMboStudentSession: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
	  	}
	}
	else
	{
		mboLogError("createMboStudentSession: More than 1 row exists on mboNextSessionId");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sesssalt = rand(10000000,99999999) ;
	$ssid = $nextSessNum ;
	$skey = md5 ( md5($ssid) . $sesssalt );

	//mboLogError("debug: nextSessNum=$nextSessNum");
	//mboLogError("debug: sesssalt=$sesssalt");
	//mboLogError("debug: ssid=$ssid");
	//mboLogError("debug: skey=$skey");

	$_SESSION["ssid"] = $ssid;
	$_SESSION["salt"] = $sesssalt;



	$sql = ' INSERT INTO mboSession '
	. ' (session_num, Session_ID, Time_Stamp, User_ID, Site, Live) '
	. ' VALUES ( ' . $nextSessNum 
	. ' , "' . $skey . '"'
	. ' , CURRENT_TIMESTAMP() '
	. ' , "' . $stuId . '"'
	. ' , "' . $passedSite . '"'
	. ' , "Y")'
	;

	//mboLogError ("debug: SQL=$sql");
		
	if (!($results = mysql_query($sql)))
	{
		mboLogError("createMboStudentSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	return $skey;
}


function validateMboStudentSession()
{

	$retval = "INVALID";

	if ( (!isset($_SESSION["ssid"])) || (!isset($_SESSION["salt"])) )
	{
		//mboLogError ("debug:validateMboStudentSession:ssid and/or salt not found - INVALID");
		return $retval;
	}

	$recvssid = $_SESSION['ssid'];
	$recvsalt = $_SESSION['salt'];

	//mboLogError ("debug:validateMboStudentSession: ssid=$recvssid salt=$recvsalt");
	$lkupskey = md5(md5($recvssid) . $recvsalt);


	$sql = "select site, User_ID from mboSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("validateMboStudentSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);
	
	if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);				
		//mboLogError ("debug:validateMboStudentSession: SUCCESSFULLY VALIDATED: $row[0] | $row[1]");
		$retval = "SUCCS_VLDTN|" . $row[0] . "|" . $row[1];
	}
	else
	{
		//mboLogError ("debug:validateMboStudentSession: INVALID: numrows=$numrows");
	}

	//mboLogError ("debug:validateMboStudentSession: retval=$retval");
	return $retval;
}


function createMboAdminSession( $admId, $passedSite )
{			
	$sql = ' SELECT session_num FROM mboAdmNextSessionId ';
	//mboLogError ("debug: SQL=$sql");
	
	if (!($results = mysql_query($sql)))
	{
		mboLogError("createMboAdminSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);
	if ($numRows == 0)
	{
		$nextSessNum = 1;
		$sql = ' INSERT INTO mboAdmNextSessionId (session_num) values (1)';
	  	if (!($results = mysql_query($sql)))
	  	{
			mboLogError("createMboAdminSession: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
	  	}
	}
	else if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);
		$nextSessNum = $row[0] + 1;

		$sql = ' UPDATE mboAdmNextSessionId set session_num = ' . $nextSessNum ;
	  	if (!($results = mysql_query($sql)))
	  	{
			mboLogError("createMboAdminSession: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
	  	}
	}
	else
	{
		mboLogError("createMboAdminSession: More than 1 row exists on mboNextSessionId");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$sesssalt = rand(10000000,99999999) ;
	$ssid = $nextSessNum ;
	$skey = md5 ( md5($ssid) . $sesssalt );

	//mboLogError("debug: nextSessNum=$nextSessNum");
	//mboLogError("debug: sesssalt=$sesssalt");
	//mboLogError("debug: ssid=$ssid");
	//mboLogError("debug: skey=$skey");

	$_SESSION["ssid"] = $ssid;
	$_SESSION["salt"] = $sesssalt;



	$sql = ' INSERT INTO mboAdmSession '
	. ' (session_num, Session_ID, Time_Stamp, User_ID, Site, Live) '
	. ' VALUES ( ' . $nextSessNum 
	. ' , "' . $skey . '"'
	. ' , CURRENT_TIMESTAMP() '
	. ' , "' . $admId . '"'
	. ' , "' . $passedSite . '"'
	. ' , "Y")'
	;

	//mboLogError ("debug: SQL=$sql");
		
	if (!($results = mysql_query($sql)))
	{
		mboLogError("createMboAdminSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
}


function validateMboAdminSession()
{

	$retval = "INVALID";

	if ( (!isset($_SESSION["ssid"])) || (!isset($_SESSION["salt"])) )
	{
		//mboLogError ("debug:validateMboAdminSession:ssid and/or salt not found - INVALID");
		return $retval;
	}

	$recvssid = $_SESSION['ssid'];
	$recvsalt = $_SESSION['salt'];

	//mboLogError ("debug:validateMboAdminSession: ssid=$recvssid salt=$recvsalt");
	$lkupskey = md5(md5($recvssid) . $recvsalt);


	$sql = "select site, User_ID from mboAdmSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("validateMboAdminSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);

	$admuid = "";
	$admulname = "";
	$admufname = "";
	$admuemail = "";
	$admsite = "";
	$admsitedesc = "";

	if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);				
		//mboLogError ("debug:validateMboAdminSession: SUCCESSFULLY VALIDATED: $row[0] | $row[1]");
		$admsite = $row[0];
		$admuid = $row[1];

		//Look up instructor and site and return their descriptions
		$sql = "select siteDesc from mboSite where siteId = '" . $admsite . "'";

		if (!($results = mysql_query($sql)))
		{
			mboLogError("validateMboAdminSession: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}
		if ($numRows == 1)
		{
			$row = mysql_fetch_array($results);				
			$admsitedesc = $row[0];

			$sql = "select lastName, firstName, emailAdrs from mboAdmUser "
			. " where user_id = '" . strtoupper($admuid) . "'"
			. " and siteId = '" . $admsite . "'";	

			if (!($results = mysql_query($sql)))
			{
				mboLogError("validateMboAdminSession: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}
			if ($numRows == 1)
			{
				$row = mysql_fetch_array($results);
				$admulname = $row[0];
				$admufname = $row[1];
				$admuemail = $row[2];
			
				$retval = "SUCCS_VLDTN|" 
				. $admsite 
				. "|" . $admuid
				. "|" . $admsitedesc
				. "|" . $admulname
				. "|" . $admufname
				. "|" . $admuemail
				;
			}
		}
	}
	else
	{
		//mboLogError ("debug:validateMboAdminSession: INVALID: numrows=$numrows");
	}


	//mboLogError ("debug:validateMboAdminSession: retval=$retval");
	return $retval;
}


function deleteMboAdminSession()
{

	$retval = "INVALID";

	if ( (!isset($_SESSION["ssid"])) || (!isset($_SESSION["salt"])) )
	{
		//mboLogError ("debug:deleteMboAdminSession:ssid and/or salt not found - INVALID");
		return $retval;
	}

	$recvssid = $_SESSION['ssid'];
	$recvsalt = $_SESSION['salt'];

	//mboLogError ("debug:deleteMboAdminSession: ssid=$recvssid salt=$recvsalt");
	$lkupskey = md5(md5($recvssid) . $recvsalt);

	$sql = "select site, User_ID from mboAdmSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("validateMboAdminSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);

	$admuid = "";
	$admsite = "";

	if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);				
		//mboLogError ("debug:validateMboAdminSession: SUCCESSFULLY VALIDATED: $row[0] | $row[1]");
		$admsite = $row[0];
		$admuid = $row[1];
	}
	else
	{
		//mboLogError ("debug:validateMboAdminSession: INVALID: numrows=$numrows");
	}



	$arcvsql = 'INSERT INTO mboArcvAdmSession (	archive_time, '
	. ' session_num, Session_ID, Time_Stamp, User_ID, Site, Live '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' session_num, Session_ID, Time_Stamp, User_ID, Site, Live '
	. " FROM mboAdmSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("deleteMboAdminSession: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	mboLogArchive ($arcvsql);


	$sql = "DELETE from mboAdmSession where Session_ID = '" . $lkupskey . "'";

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("deleteMboAdminSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	else
	{
		$retval = "SUCCS_VLDTN|"
			. $admsite 
			. "|" . $admuid;
	}

	//mboLogError ("debug:deleteMboAdminSession: retval=$retval");
	return $retval;
}

function deleteMboStudentSession()
{

	$retval = "INVALID";

	if ( (!isset($_SESSION["ssid"])) || (!isset($_SESSION["salt"])) )
	{
		//mboLogError ("debug:deleteMboStudentSession:ssid and/or salt not found - INVALID");
		return $retval;
	}

	$recvssid = $_SESSION['ssid'];
	$recvsalt = $_SESSION['salt'];

	//mboLogError ("debug:deleteMboStudentSession: ssid=$recvssid salt=$recvsalt");
	$lkupskey = md5(md5($recvssid) . $recvsalt);

	$sql = "select site, User_ID from mboSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($sql)))
	{
		mboLogError("deleteMboStudentSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numRows = mysql_num_rows($results);

	$savesite = 'NOTFOUND';
	$saveuid =  'NOTFOUND';
	
	if ($numRows == 1)
	{
		$row = mysql_fetch_array($results);				
		//mboLogError ("debug:deleteMboStudentSession: SUCCESSFULLY VALIDATED: $row[0] | $row[1]");
		$savesite = $row[0];
		$saveuid =  $row[1];
	}
	else
	{
		//mboLogError ("debug:validateMboStudentSession: INVALID: numrows=$numrows");
	}

	$arcvsql = 'INSERT INTO mboArcvSession (	archive_time, '
	. ' session_num, Session_ID, Time_Stamp, User_ID, Site, Live '
	. ' ) SELECT CURRENT_TIMESTAMP(), '
	. ' session_num, Session_ID, Time_Stamp, User_ID, Site, Live '
	. " FROM mboSession where Session_ID = '" . $lkupskey . "'";

	if (!($results = mysql_query($arcvsql)))
	{
		mboLogError("deleteMboStudentSession: SQL Error: SQL=$arcvsql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	mboLogArchive ($arcvsql);

	$sql = "DELETE from mboSession where Session_ID = '" . $lkupskey . "'";

	mboLogArchive ($sql);

	if (!($results = mysql_query($sql)))
	{
		mboLogError("deleteMboStudentSession: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	else
	{
		$retval = "SUCCS_VLDTN|" . $savesite . "|" . $saveuid;
	}

	//mboLogError ("debug:deleteMboStudentSession: retval=$retval");
	return $retval;
}


function genTempPw()
{
	$valspin = rand(1000001,9999999);
	$spinlet = rand(0,25);
	$spinchr = substr("ABCDEFGHIJKLMNOPQRSTUVWXYZ",$spinlet,1);
	return $spinchr . $valspin;
}

//References:
// 1. Beginning PHP, Apache, MySql Web Development, Wrox Press
// Published by Wiley Publishing Inc., 2004.
// 2. PHP Manual
?>
