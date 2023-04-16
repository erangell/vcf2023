<?php
require_once ("mbolib.php");
require_once ("mboedits.php");
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

	$inputadmsiteid = $admsite;

	include("mboHeader.txt");
	echo "<h1>Add Site</h1><P>";

	$passedErr = $_GET['err'];


	if ($passedErr != "0001") 
	{
		$inputadmsiteid = htmlspecialchars($_POST['siteid']);
		$inputadmsitedesc = htmlspecialchars($_POST['sitedesc']);

		// Validate user entered data
		$edits_ok = "Y";


		if (isValidLength($inputadmsitedesc, 1, 35) != "Y")
		{
			$passedErr = "1011";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmsitedesc) . ") for inputadmsitedesc: $inputadmsitedesc");
			$edits_ok = "N";
		}
		else
		{
			if (isValidDescription($inputadmsitedesc)!="Y") 
			{
				$passedErr = "1012";
				mboLogError ("badinput: Invalid characters in inputadmsitedesc($inputadmsitedesc): $inputadmsitedesc"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmsitedesc = $inputadmsitedesc;
			}
		}

		if (isValidLength($inputadmsiteid, 1, 10) != "Y")
		{
			$passedErr = "1041";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmsiteid) . ") for inputadmsiteid: inputadmsiteid=$inputadmsiteid");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmsiteid)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid characters in inputadmsiteid($inputadmsiteid): inputadmsiteid=$inputadmsiteid"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmsiteid = $inputadmsiteid;
			}
		}

	
		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: ADDING SITE: admsite=$admsite admuid=$admuid ");
			mboLogError ("audit: valinputadmsiteid=$valinputadmsiteid");
			mboLogError ("audit: valinputadmsitedesc=$valinputadmsitedesc");

			$sql = "select 1 from mboSite "
			. " where siteId = '" . strtoupper($valinputadmsiteid) . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboAddSite.php: SQL Error: SQL=$sql");
				mboLogError ("DB error: " . mysql_error());
				mboFatalError();
			}

			$numRows = mysql_num_rows($results);
			//mboLogError ("debug: numrows=$numRows");
		
			if ($numRows != 0)
			{
				$passedErr = "1051";
			}
			else
			{	
				$sql = "insert into mboSite "
				. " (siteId, siteDesc) values ( "
				. "'" . strtoupper($valinputadmsiteid) . "'"
				. ",'" . strtoupper($valinputadmsitedesc) . "'"
				. ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddSite.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}
				
				$sql = "insert into mboSiteAuthMeth "
				. " (siteId, authMethId) values ( "
				. "'" . strtoupper($valinputadmsiteid) . "'"
				. ",'" . MBOFORM . "'"
				. ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddSite.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				$sql = "insert into mboSiteCourse "
				. " (siteId, courseId) values ( "
				. "'" . strtoupper($valinputadmsiteid) . "'"
				. ",'" . MBOBASIC . "'"
				. ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddSite.php: SQL Error: SQL=$sql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}


				$passedErr = "0002";
			}
			//mboLogError ("debug: passedErr=$passedErr");
		}

	}

	//mboLogError ("debug: passedErr=$passedErr");


	if ($passedErr == "0001")
	{
		echo "<h3><font color=blue>Please Enter all data below.</font></h3>";
		$passedErr = "1001";
	}
	else
	{ 
		if ($passedErr == "0002")
		{
			echo "<h3><font color=blue>Record added.</font></h3>";

			echo "<li>Site: $valinputadmsiteid";
			echo "<li>Description: $valinputadmsitedesc";
		}
		if ($passedErr == "1001")
		{
			echo "<h3><font color=red>Error: Invalid Data Was Entered.  Please double-check your input.</font></h3>";
		}
		if ($passedErr == "1011")
		{
			echo "<h3><font color=red>Error: Site Description is required.</font></h3>";
		}
		if ($passedErr == "1012")
		{
			echo "<h3><font color=red>Error: Invalid data in Site Description.</font></h3>";
		}
		if ($passedErr == "1041")
		{
			echo "<h3><font color=red>Error: Site ID is required.</font></h3>";
		}
		if ($passedErr == "1042")
		{
			echo "<h3><font color=red>Error: Invalid data in Site ID.</font></h3>";
		}
		if ($passedErr == "1051")
		{
			echo "<h3><font color=red>Error: Site Record already exists.</font></h3>";
		}
	}

	echo '<P><form action="mboAddSite.php?err=';
	echo "$passedErr";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboAddSiteGood.txt");
	}
	else
	{
		include("mboAddSiteBef.txt");
		include("mboAddSiteAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
