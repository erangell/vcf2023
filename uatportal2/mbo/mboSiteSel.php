<?PHP


$postedSite = $_POST['selsite'];

if ( $postedSite == "" )
{
	header ("Location: stuIndex.php?err=1001");
}

require_once ("mbolib.php");

mboDBlogin();

$sql = ' SELECT authMethId ' 
	. ' FROM mboSiteAuthMeth '
	. ' WHERE siteID = "' . $postedSite . '"'
;

if (!($results = mysql_query($sql)))
{
	mboLogError("mboSiteSel.php: SQL Error: SQL=$sql");
	mboLogError ("DB error: " . mysql_error());
	mboFatalError();
}

$row = mysql_fetch_array($results);

mysql_close();

$authmeth = $row[0];

switch ($authmeth)
{
	case 'CAS' :
		header ("Location: authCas.php?site=$postedSite&err=0001");
		break;
	case 'MBOFORM' :
		header ("Location: authMbo.php?site=$postedSite&err=0001");
		break;
}


?>
