<?php

require_once ("mbolib.php");

echo "<pre>";
echo "BEGIN=mboAppExamPing.php";
echo "</pre>";

mboDBlogin();

$sql = ' SELECT siteId, siteDesc ' 
	. ' FROM mboSite '
	. ' ORDER BY 2 '
;


if (!($results = mysql_query($sql)))
{
	mboLogError("stuindex.php: SQL Error: SQL=$sql");
	$saveError = mysql_error();
	mboLogError ("DB error: " . $saveError);
	echo "<pre>";
	echo "ERROR=$saveError";
	echo "</pre>";
}

while ($row = mysql_fetch_array($results))
{	
	echo "<pre>";
	echo "SITEID=$row[0]";
	echo "</pre>";
	echo "<pre>";
	echo "SITEDESC=$row[1]";
	echo "</pre>";
}

mysql_close();

echo "<pre>";
echo "END=mboAppExamPing.php";
echo "</pre>";

?>
