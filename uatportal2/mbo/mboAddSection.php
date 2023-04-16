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
	$valinputadmsiteid = $inputadmsiteid;

	include("mboHeader.txt");
	echo "<h1>Add Section</h1><P>";

	$passedErr = $_GET['err'];


	if ($passedErr != "0001") 
	{
		$inputadmcourseid = htmlspecialchars($_POST['courseid']);
		$inputadmsectid = htmlspecialchars($_POST['sectid']);
		$inputadminstructorid = htmlspecialchars($_POST['instrid']);
		$inputadmsectdesc = htmlspecialchars($_POST['sectdesc']);

		// Validate user entered data
		$edits_ok = "Y";



		if (isValidLength($inputadmsectdesc, 1, 50) != "Y")
		{
			$passedErr = "1011";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmsectdesc) . ") for inputadmsectdesc: $inputadmsectdesc");
			$edits_ok = "N";
		}
		else
		{
			if (isValidDescription($inputadmsectdesc)!="Y") 
			{
				$passedErr = "1012";
				mboLogError ("badinput: Invalid characters in inputadmsectdesc($inputadmsectdesc): $inputadmsectdesc"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmsectdesc = $inputadmsectdesc;
			}
		}

		if (isValidLength($inputadminstructorid, 1, 20) != "Y")
		{
			$passedErr = "1041";
			mboLogError ("badinput: Invalid length (" . strlen($inputadminstructorid) . ") for inputadminstructorid: inputadmsectdesc=$inputadmsectdesc");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadminstructorid)!="Y") 
			{
				$passedErr = "1042";
				mboLogError ("badinput: Invalid characters in inputadminstructorid($inputadminstructorid): inputadmsectdesc=$inputadmsectdesc"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadminstructorid = $inputadminstructorid;
			}
		}

		if (isValidLength($inputadmsectid, 1, 20) != "Y")
		{
			$passedErr = "1031";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmsectid) . ") for inputadmsectid: inputadmsectdesc=$inputadmsectdesc");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmsectid)!="Y") 
			{
				$passedErr = "1032";
				mboLogError ("badinput: Invalid characters in inputadmsectid($inputadmsectid): inputadmsectdesc=$inputadmsectdesc"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmsectid = $inputadmsectid;
			}
		}

		if (isValidLength($inputadmcourseid, 1, 10) != "Y")
		{
			$passedErr = "1021";
			mboLogError ("badinput: Invalid length (" . strlen($inputadmcourseid) . ") for inputadmcourseid: inputadmsectdesc=$inputadmsectdesc");
			$edits_ok = "N";
		}
		else
		{
			if (isAlphaNumeric($inputadmcourseid)!="Y") 
			{
				$passedErr = "1022";
				mboLogError ("badinput: Invalid characters in inputadmcourseid($inputadmcourseid): inputadmsectdesc=$inputadmsectdesc"); 
				$edits_ok = "N";
			}
			else
			{
				$valinputadmcourseid = $inputadmcourseid;
			}
		}


	
		//mboLogError ("debug: EDITS_OK=$edits_ok");

		if ($edits_ok == "Y")
		{
			//mboLogError ("debug: EDITS_OK");

			mboLogError ("audit: ADDING SECTION: admsite=$admsite admuid=$admuid ");
			mboLogError ("audit: valinputadmsiteid=$valinputadmsiteid");
			mboLogError ("audit: valinputadmcourseid=$valinputadmcourseid");
			mboLogError ("audit: valinputadmsectid=$valinputadmsectid");
			mboLogError ("audit: valinputadminstructorid=$valinputadminstructorid");
			mboLogError ("audit: valinputadmsectdesc=$valinputadmsectdesc");

			$sql = "select 1 from mboSection "
			. " where siteId = '" . strtoupper($valinputadmsiteid) . "'"
			. " and courseId = '" . strtoupper($valinputadmcourseid) . "'"
			. " and sectionId = '" . strtoupper($valinputadmsectid) . "'";

			if (!($results = mysql_query($sql)))
			{
				mboLogError("mboAddSection.php: SQL Error: SQL=$sql");
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
				$sql = "insert into mboSection "
				. " (siteId, courseId, sectionId, instructorId, sectDescription) values ( "
				. "'" . strtoupper($valinputadmsiteid) . "'"
				. ",'" . strtoupper($valinputadmcourseid) . "'"
				. ",'" . strtoupper($valinputadmsectid) . "'"
				. ",'" . strtoupper($valinputadminstructorid) . "'"
				. ",'" . strtoupper($valinputadmsectdesc) . "'"
				. ")";

				if (!($results = mysql_query($sql)))
				{
					mboLogError("mboAddSection.php: SQL Error: SQL=$sql");
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
			echo "<li>Course: $valinputadmcourseid";
			echo "<li>Section: $valinputadmsectid";
			echo "<li>Instructor: $valinputadminstructorid";
			echo "<li>Description: $valinputadmsectdesc";
		}
		if ($passedErr == "1001")
		{
			echo "<h3><font color=red>Error: Invalid Data Was Entered.  Please double-check your input.</font></h3>";
		}
		if ($passedErr == "1011")
		{
			echo "<h3><font color=red>Error: Section Description is required.</font></h3>";
		}
		if ($passedErr == "1012")
		{
			echo "<h3><font color=red>Error: Invalid data in Section Description.</font></h3>";
		}
		if ($passedErr == "1021")
		{
			echo "<h3><font color=red>Error: Course ID is required.</font></h3>";
		}
		if ($passedErr == "1022")
		{
			echo "<h3><font color=red>Error: Invalid data in Course ID.</font></h3>";
		}
		if ($passedErr == "1031")
		{
			echo "<h3><font color=red>Error: Section ID is required.</font></h3>";
		}
		if ($passedErr == "1032")
		{
			echo "<h3><font color=red>Error: Invalid data in Section ID.</font></h3>";
		}
		if ($passedErr == "1041")
		{
			echo "<h3><font color=red>Error: Instructor ID is required.</font></h3>";
		}
		if ($passedErr == "1042")
		{
			echo "<h3><font color=red>Error: Invalid data in Instructor ID.</font></h3>";
		}
		if ($passedErr == "1051")
		{
			echo "<h3><font color=red>Error: Section Record already exists.</font></h3>";
		}
	}

	echo '<P><form action="mboAddSection.php?err=';
	echo "$passedErr";
	echo '" method=POST>';

	if ($passedErr == "0002")
	{
		include("mboAddSectionGood.txt");
	}
	else
	{
		include("mboAddSectionBefA.txt");

		//Populate dropdown for Course with courses offered at admin's site


		$sql = " select c.courseID, c.courseDesc "
			. " from mboCourse c, mboSiteCourse sc "
			. " where c.courseID = sc.courseId "
			. " and sc.siteId = '" . strtoupper($admsite) . "'";


		if (!($results = mysql_query($sql)))
		{
			mboLogError("admindex.php: SQL Error");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		$numRows = mysql_num_rows($results);
		//TODO: Enhance logic to check num rows: if 1, no dropdown, else dropdown

		echo "<tr><td class=units>Course:</td><td class=units>";		
		echo '<SELECT name="courseid">';
		echo '<option value="" SELECTED>Select course...</option>';

		while ($row = mysql_fetch_array($results))
		{	
			echo '<OPTION value="';
			echo "$row[0]";
			echo '">';
			echo "$row[0]: $row[1]</OPTION>";
		}
		echo "</SELECT></td></tr>";

		include("mboAddSectionBefB.txt");

		//Populate dropdown for Instructors at admin's site

		$sql = " select u.user_id, u.lastName, u.firstName "
			. " from mboAdmUser u, mboAdmRole r "
			. " where u.user_id = r.user_id "
			. " and u.siteId = r.site "
			. " and r.role_id = 'INSTRUCTOR' "
			. " and u.siteId = '" . strtoupper($admsite) . "'";


		if (!($results = mysql_query($sql)))
		{
			mboLogError("admindex.php: SQL Error");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		$numRows = mysql_num_rows($results);
		//TODO: Enhance logic to check num rows: if 1, no dropdown, else dropdown

		echo "<tr><td class=units>Instructor:</td><td class=units>";		
		echo '<SELECT name="instrid">';
		echo '<option value="" SELECTED>Select instructor...</option>';

		while ($row = mysql_fetch_array($results))
		{	
			echo '<OPTION value="';
			echo "$row[0]";
			echo '">';
			echo "$row[1], $row[2] </OPTION>";
		}
		echo "</SELECT></td></tr>";

		include("mboAddSectionAft.txt");
	}
	
	echo "</table>";
	echo "</html>";
}
?>
