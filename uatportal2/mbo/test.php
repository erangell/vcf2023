<?php

require_once 'mbocfg.php';
require_once 'mbolib.php';

echo "For debugging:";
echo "mboInstallPath = $mboInstallPath";

if (!file_exists("mbopass.fyl"))
{
	if (file_exists("mboFatal.htm"))
	{
		
	}
	else
	{	
		die ($mboFatalErrMsg);
	}
}


include("mbologin.html");


$connect=mysql_connect("localhost","root","welc0me") or
  die ("Unable to connect to mysql!!!");

mysql_select_db ("mbotest");

$sql = ' SELECT * ' 
	. ' FROM mboStudent p '
;


$results = mysql_query($sql) or die (mysql_error());

#echo "Number of rows: ";
#echo mysql_num_rows($results);

echo "<p><table border=2>";

while ($row = mysql_fetch_array($results))
{	
	echo "<tr>";
	echo "<td> $row[0] </td><td> $row[1] </td><td> $row[2] </td>  ";
	echo "<td> $row[3] </td><td> $row[4] </td><td> $row[5] </td> ";
	echo "</tr>";

}

echo "</table>"

?>
</html>