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

	$defsort = "u.lastName, u.firstName";
	$defdescr = "Last Name, First Name";
	$sortparm = $defsort;
	$sortdescr = $defdescr;
	
	if ($_GET['sort'] == "ui")
	{
		$sortparm = "u.user_id";
		$sortdescr = "User ID";
	}
	if ($_GET['sort'] == "em")
	{
		$sortparm = "u.emailAdrs";
		$sortdescr = "Email";
	}
	

	$sql = ' SELECT u.lastName, u.firstName, u.user_id, u.emailAdrs, u.siteId '
	. ' FROM '
	. '  mboAdmUser u , mboAdmRole r'
	. ' WHERE '
	. "      u.user_id <> '" . strtoupper($admuid) . "'"
	. '  and u.user_id = r.user_id '
	. '  and u.siteId = r.site '
	. "  and r.role_id = 'SITEADM'";

	;

	$sql = $sql . " ORDER BY  " . $sortparm;


	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboMaintSiteAdmin.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}

	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		echo "<p><h3>No admin data found (for your site)</h3>";
	}
	else
	{
		echo "<h1>Site Administrator Maintenance</h1>";

		echo "<h4>$admsitedesc : $admulname" . ", " . "$admufname";

		echo "<h4>";

		echo "&nbsp;&nbsp; Sorted by: $sortdescr</h4>";

		echo "<p><table border=1 cellpadding=3>";

		echo '<tr>';
		echo '<td><b>Site</b></td>';

		echo '<td><a href="mboMaintSiteAdmin.php"><b>Last Name</b></a></td>';
		echo "</td><td><b>First Name</b></td>";
		echo '<td><a href="mboMaintSiteAdmin.php?sort=ui"><b>User ID</b></a></td>';
		echo '<td><a href="mboMaintSiteAdmin.php?sort=em"><b>Email</b></a></td>';
		echo '<td><b>Action</b></td>';

		while ($row = mysql_fetch_array($results))
		{	
			echo "<tr>";
			echo "<td> $row[4] </td>";

			echo "<td> $row[0] </td>";
			echo "<td> $row[1] </td>";
			echo "<td> $row[2] </td>";
			echo "<td> $row[3] </td>";

			echo '<td><a href="mboEditSiteAdmin.php?err=0001&ui=' . $row[2] 
				. '&sy=' . $row[4] . '">Edit</a>';
			echo '&nbsp;&nbsp;&nbsp;<a href="mboEditSiteAdminRole.php?err=0001&ui=' . $row[2] . '&sy=' . $row[4] . '">Roles</a>';
			echo '&nbsp;&nbsp;&nbsp;<a href="mboDeleteSiteAdmin.php?err=0001&ui=' . $row[2] 
				. '&sy=' . $row[4] . '">Delete</a>';
			echo '&nbsp;&nbsp;&nbsp;<a href="mboResetAdminPw.php?err=0001&ui=' . $row[2] 
				. '&sy=' . $row[4] . '">Reset PW</a></td>';
			echo "</tr>";
		}

		echo "</table>";
	}

	echo '<p>';
	echo '<li><a href="mboAddSiteAdmin.php?err=0001">Add New Site Administrator</a>';
	echo '<p>';
	echo '<li><a href="mboAdmMenu.php">Return to menu</a>';

}

?>
