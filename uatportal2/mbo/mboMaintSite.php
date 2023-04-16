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

	include("mboHeader.txt");
	

	$sql = ' SELECT siteId, siteDesc '
	. ' FROM '
	. '  mboSite '
	;

	$sql = $sql . " ORDER BY  1,2";

	//mboLogError("debug: SQL=$sql");


	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboMaintSite.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		echo "<p><h3>No site data found (for your site)</h3>";
	}
	else
	{
		echo "<h1>Site Maintenance</h1>";

		echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";

		echo "<h4>";

		echo "<p><table border=1 cellpadding=3>";

		echo '<tr>';

		echo '<td><b>Site</b></td>';
		echo '<td><b>Site Description</b></td>';
//		echo '<td><b>Action</b></td>';

		while ($row = mysql_fetch_array($results))
		{	
			echo "<tr>";

			echo "<td> $row[0] </td>";
			echo "<td> $row[1] </td>";
			echo "<td>";

//			echo '&nbsp;<a href="mboEditSite.php?err=0001&si=' . $row[0] . '">Edit</a>';

			echo "</td>";
			echo "</tr>";
		}

		echo "</table>";
	}

	echo '<p>';
	echo '<li><a href="mboAddSite.php?err=0001">Add New Site</a>';
	echo '<p>';
	echo '<li><a href="mboAdmMenu.php">Return to menu</a>';

}

?>
