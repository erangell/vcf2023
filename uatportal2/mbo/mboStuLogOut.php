<?php
require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = deleteMboStudentSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: stuIndex.php");
}
else
{
	$stupart = explode('|',$valkey);
	$stusite = $stupart[1];
	$stuuid  = $stupart[2];

	mboLogError ("access: STUDENT LOGGED OUT: stusite=$stusite stuuid=$stuuid");

	header ("Location: stuIndex.php");
}

?>
