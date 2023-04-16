<?php
require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = deleteMboAdminSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: admIndex.php");
}
else
{
	$admpart = explode('|',$valkey);
	$admsite = $admpart[1];
	$admuid  = $admpart[2];

	mboLogError ("access: ADMIN LOGGED OUT: admsite=$admsite admuid=$admuid");

	header ("Location: admIndex.php");
}

?>
