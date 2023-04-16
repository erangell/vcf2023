<?php

require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = validateMboStudentSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: stuIndex.php");
}
$stupart = explode('|',$valkey);
$stusite = $stupart[1];
$stuuid  = $stupart[2];


require_once ("mboconfig.php");
$mboInstallPath = mboGetInstallPath();
$dirPath = mboGetUploadPath() ;

$logfile = $dirPath . "bumplog.fyl";
$fnumfile = $dirPath . "nextfnum.fyl";
$nextfile = $dirPath . "nextfnum.nxt";
$lockfile = $dirPath . "nextfnum.lck";

if (file_exists($logfile))
{
	if (!$loghandle = fopen($logfile, 'a')) {
		die ("*** FATAL ERROR: Unable to open LOG file");
	}
}
else
{
	if (!$loghandle = fopen($logfile, 'w')) {
		die ("*** FATAL ERROR: Unable to create LOG file");
  	}
}
$log = "\n--------------------\n$valkey\n--------------------\n" . date ("Y-m-d H:i:s") . " *** BEGIN mboupldw.php";
if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
}


$arrzero = $HTTP_POST_FILES['udmtsrec'];
$tmp_name=$arrzero['tmp_name'];
$name=$arrzero['name'];
$type=$arrzero['type'];
$error=$arrzero['error'];
$size=$arrzero['size'];

$log = "\nUpload Parameters: \ntmp_name=|$tmp_name| \n name=|$name| \n type=|$type| \n error=|$error| \n size=|$size| \n ";
if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
}


if (($error != 0) || ($size == 0))
{
  $log = "\nINVALID UPLOAD: ERROR NOT EQUAL TO ZERO, OR SIZE = 0 ";
  if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
  }
  include ("mboHeader.txt");
  echo "<h1><font color=red>ERROR: MBO Upload Unsuccessful</font></h1>";
  echo "<hr size=1>";
  echo "<h3>Your file was not uploaded.</h3>";
  echo "<h3>Please double check the name of the filename you specified. <h3>";
  echo "<h3>Please close this window to continue.</h3>";
  
  echo "<hr size=1>";
  echo "</body>";
  
  $log = "\n" . date ("Y-m-d H:i:s") . " *** END mboupldw.php";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
  
  fclose($loghandle);
  die ("</html>");
}

$expectedftype1 = "audio/wav";
$expectedftype2 = "audio/x-wav";
$boolCmpr1 = strcasecmp ($type,$expectedftype1);
$boolCmpr2 = strcasecmp ($type,$expectedftype2); 
$log = "\nDebug: type=|$type| expectedftype1=|$expectedftype1| expectedftype2=|$expectedftype2| boolCmpr1=$boolCmpr1| boolCmpr2=$boolCmpr2 ";
if (fwrite($loghandle, $log) === FALSE) { 
  die ("*** ERROR : Cannot write to log file"); 
}

$expectedfname = "udmtsrec.wav";
$boolCmpr = strcasecmp ($name,$expectedfname); 
$log = "\nDebug: name=|$name| expectedfname=|$expectedfname| boolCmpr=$boolCmpr ";
if (fwrite($loghandle, $log) === FALSE) { 
  die ("*** ERROR : Cannot write to log file"); 
}

if ((strcasecmp ($type,$expectedftype1) != 0) && (strcasecmp ($type,$expectedftype2) != 0))
{
  $log = "\nINVALID UPLOAD: FILE TYPE NOT EQUAL TO $expectedftype1 OR $expectedftype2 ";
  if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
  }
  include ("mboHeader.txt");
  echo "<h1><font color=red>ERROR: MBO Upload Unsuccessful</font></h1>";
  echo "<hr size=1>";
  echo "<h3>Your file was not uploaded.</h3>";
  echo "<h3>The file you upload must be a WAV file.<h3>";
  echo "<h3>Please close this window to continue.</h3>";
  
  echo "<hr size=1>";
  echo "</body>";
  
  $log = "\n" . date ("Y-m-d H:i:s") . " *** END mboupldw.php";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
  
  fclose($loghandle);
  die ("</html>");
}
else
{
  $log = "\nVALID UPLOAD: FILE TYPE IN ( $expectedftype1 $expectedftype2 ) ";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
}


if (strcasecmp ($name,$expectedfname) != 0)
{
  $log = "\nINVALID UPLOAD: FILE NAME NOT EQUAL TO $expectedfname ";
  if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
  }
  include ("mboHeader.txt");
  echo "<h1><font color=red>ERROR: MBO Upload Unsuccessful</font></h1>";
  echo "<hr size=1>";
  echo "<h3>Your file was not uploaded.</h3>";
  echo "<h3>The file you upload must be named: udmtsrec.wav<h3>";
  echo "<h3>Please close this window to continue.</h3>";
  
  echo "<hr size=1>";
  echo "</body>";
  
  $log = "\n" . date ("Y-m-d H:i:s") . " *** END mboupldw.php";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
  
  fclose($loghandle);
  die ("</html>");
}
else
{
  $log = "\nVALID UPLOAD: FILE NAME EQUALS $expectedfname ";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
}


if ($size > 5300000)
{
  $log = "\nINVALID UPLOAD: SIZE GREATER THAN 5MB ";
  if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
  }
  include ("mboHeader.txt");
  echo "<h1><font color=red>ERROR: MBO Upload Unsuccessful</font></h1>";
  echo "<hr size=1>";
  echo "<h3>Your file was not uploaded.</h3>";
  echo "<h3>Maximum file size (1MB) exceeded.<h3>";
  echo "<h3>Please close this window to continue.</h3>";
  
  echo "<hr size=1>";
  echo "</body>";
  
  $log = "\n" . date ("Y-m-d H:i:s") . " *** END mboupldw.php";
  if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
  }
  
  fclose($loghandle);
  die ("</html>");
}





$log = "\nReading next file number file...";
if (fwrite($loghandle, $log) === FALSE) {
	die ("*** ERROR : Cannot write to log file");
}

if (file_exists($lockfile))
{
	$log = "\nWaiting for lock...";
	if (fwrite($loghandle, $log) === FALSE) {
		die ("*** ERROR : Cannot write to log file");
	}

	for ($retry = 0 ; $retry < 99999 ; $retry++)
	{
		if (!file_exists($lockfile))
		{
			break;
		}
	}
	if (file_exists($lockfile))
	{
		$log = "\n***ERROR: Unable to obtain lock";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

		die ("***ERROR: Unable to obtain lock - CONTACT SYSTEMS SUPPORT");
	}
}
if (!file_exists($lockfile))
{
	if (!$lckhandl = fopen($lockfile, 'w')) {

       		$log = "\n***ERROR : Cannot open lock file for writing.";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

       		die("***ERROR : Cannot open lock file for writing.");
  	}
	$out = "LOCK FILE";
	if (fwrite($lckhandl, $out) === FALSE) {

		$log = "\n***ERROR : Cannot write to lock file";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

		die("***ERROR : Cannot write to lock file");
    	} 
	fclose($lckhandl);
}

$log =  "\nFilename: $fnumfile";
if (fwrite($loghandle, $log) === FALSE) {
	die ("*** ERROR : Cannot write to log file");
}

if (file_exists($fnumfile))
{
	if (!$handle = fopen($fnumfile, 'r')) {

		$log =  "\nERROR: Cannot open fnumfile for reading";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

       		die( "***ERROR : Cannot open file for reading." );
  	}
	else
	{
		$fnumdata = fgets($handle,8);
		fclose($handle);

		$log = "\nFnum File read successfully: fnumdata=$fnumdata";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

		$fnumdata++;
		$fnumdata = "00000000" . $fnumdata;
		$fnumlen = strlen($fnumdata);
		$fnumdata = substr($fnumdata,$fnumlen - 7, 7);

		$log = "\nfnumdata=$fnumdata";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}
	 
		if (file_exists($nextfile))
		{
			$log = "\nWARNING: deleting file: $nextfile (Did not expect it to exist)";
			if (fwrite($loghandle, $log) === FALSE) {
				die ("*** ERROR : Cannot write to log file");
			}

			unlink ($nextfile);
		}
		if (!$whandle = fopen($nextfile, 'w')) {

       			$log = "\n***ERROR : Cannot open $nextfile for writing.";
			if (fwrite($loghandle, $log) === FALSE) {
				die ("*** ERROR : Cannot write to log file");
			}

       			die( "***ERROR : Cannot open nextfile for writing.");
		}
		$out = $fnumdata;
		if (fwrite($whandle, $out) === FALSE) {

       			$log = "\n***ERROR : Cannot write new sequence number to file. " ;
			if (fwrite($loghandle, $log) === FALSE) {
				die ("*** ERROR : Cannot write to log file");
			}

       			die( "***ERROR : Cannot write new sequence number to file. ") ;
		}
		fclose($whandle);

		$log =  "\nData successfully written to next sequence number file.  Deleting original fnumfile.";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

		unlink ($fnumfile);

		$log =  "\nRenaming next sequence number file";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}

		rename ($nextfile, $fnumfile);
	}
}
else
{
	$log = "\nfnumfile does not exist - creating it.";
	if (fwrite($loghandle, $log) === FALSE) {
		die ("*** ERROR : Cannot write to log file");
	}

	if (!$nhandle = fopen($fnumfile, 'w')) {

       		$log = "\nERROR : Cannot open fnumfile for writing.";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}
  	}
	else
	{
		$fnumdata = "0000001";
		$out = $fnumdata;
		if (fwrite($nhandle, $out) === FALSE) {

			$log = "\n*** ERROR : Cannot write to fnumfile";
			if (fwrite($loghandle, $log) === FALSE) {
				die ("*** ERROR : Cannot write to log file");
			}

			die ("*** ERROR : Cannot write to fnumfile");
		}
		fclose($nhandle);

	    	$log = "\nData successfully written to the new file number file.  Deleting lock file.";
		if (fwrite($loghandle, $log) === FALSE) {
			die ("*** ERROR : Cannot write to log file");
		}
	}
}


$newfname = "S" . $fnumdata . "_" . $stusite . "_" . $stuuid . ".wav";
$confirmnum = "S" . $fnumdata ;

$log = "\nUploading file to: $newfname.";
if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
}


$destpath=$dirPath . $newfname;
$log = "\n About to move uploaded file to: destpath=$destpath";
if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
}

move_uploaded_file($tmp_name,$destpath);

$log = "\nUploaded file has been moved to destination.";
if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
}

unlink ($lockfile);

$log = "\nLock file has been deleted.";
if (fwrite($loghandle, $log) === FALSE) { 
	die ("*** ERROR : Cannot write to log file"); 
}

include ("mboHeader.txt");
echo "<h1>MBO Upload Confirmation</h1>";
echo "<hr size=1>";
echo "<h3>Your file was successfully uploaded.</h3>";
echo "<h3>Your confirmation number for this upload is: <b>$confirmnum</b></h3>";
echo "<h3>Please close this window to continue.</h3>";

echo "<hr size=1>";
echo "</body>";

$log = "\n" . date ("Y-m-d H:i:s") . " *** END mboupldw.php";
if (fwrite($loghandle, $log) === FALSE) {
	die ("*** FATAL ERROR : Cannot write to log file");
}

fclose($loghandle);

?>
