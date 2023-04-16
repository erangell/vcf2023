<?php

function mboGetInstallPath ()
{
	// NOTE: Path names MUST end with a slash

	$mboInstallPath = "/home/music/usra/mboappid/mb0uat1data/";

	return $mboInstallPath;
}

function mboGetUploadPath ()
{
	// NOTE: Path names MUST end with a slash
	// Path is relative to /home/music/usra/docs/portal/mbo

	$mboUploadPath = "../mb0upldw/";


	return $mboUploadPath;
}

function mboGetUploadURL ()
{
	// NOTE: Path names MUST end with a slash
	// Path is relative to /home/music/usra/docs/portal/mbo

	$mboUploadURL = "https://arlen.music.udel.edu/uatportal1/mb0upldw/";


	return $mboUploadURL;
}

?>
