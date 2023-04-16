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
	include("mboAdmMenu.txt");

	echo "<h1>ERROR: Function Not Implemented</h1>";

	echo '<p><a href="mboAdmMenu.php">Return To Menu</a>';
}	

?>
