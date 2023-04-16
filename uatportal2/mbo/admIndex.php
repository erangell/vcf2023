<?php

require_once ("mbolib.php");

mboDBlogin();

include ("mboHeader.txt");

include ("admIndexBef.txt");

$sql = ' SELECT siteId, siteDesc ' 
	. ' FROM mboSite '
	. ' ORDER BY 2 '
;

//mboLogError ("sql: $sql");

if (!($results = mysql_query($sql)))
{
	mboLogError("admindex.php: SQL Error");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

while ($row = mysql_fetch_array($results))
{	
	echo '<OPTION value="';
	echo "$row[0]";
	echo '">';
	echo "$row[1]</OPTION>";
}

mysql_close();

echo "</SELECT>";
echo "</td>";
echo "<td class=units>";
echo '<input type=submit value="Log In">';
echo "</td>";
echo "</tr>";

if ($_GET['err'] == "1001")
{
	echo "<tr><td colspan=2><font color=red>Error: You need to select your organization in order to proceed.</font></td></tr>";
}

echo "</table></form></center></body></html>";

?>
