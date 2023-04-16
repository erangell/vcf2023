<?php

$passedSite = $_GET['site'];
$passedErr = $_GET['err'];

include "mboHeader.txt";
include "authCas.txt";

echo '<a href="authMbo.php?err=';
echo $passedErr;
echo '&site=';
echo $passedSite;
echo '">Click here to login using MBO Authentication</a>';

echo '</td></tr></table>';
echo "</html>";
?>
