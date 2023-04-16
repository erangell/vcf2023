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
	include("mboHeader.txt");
	echo "<h1>Main Menu</h1>";
	
	$admpart = explode('|',$valkey);
	$admsite = $admpart[1];
	$admuid  = $admpart[2];

	echo "<table border=0 cellpadding=5><tr></tr></table>";
	echo "<table border=2>";

	$sql = "select menuTitle, menuUrl from mboAdmMenu"
        . " where roleId in ("
        . " SELECT role_id FROM mboAdmRole "
	. " WHERE user_id = '" . strtoupper($admuid) . "'"
	. " and site = '" . $admsite . "') ";


	//mboLogError ("sql:$sql");

	if (!($results = mysql_query($sql)))
	{
		mboLogError("authMboAdm.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	$numRows = mysql_num_rows($results);

	while ($row = mysql_fetch_array($results))
	{	
		echo '<tr><td><a href="';
		echo "$row[1]";
		echo '">';
		echo "$row[0]";
		echo '</a></td></tr>';
	}
	echo '<tr><td><a href="mboAdmLogOut.php">Log Out</a>';

	echo "</table></form></center>";
}	

?>
