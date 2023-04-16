<?php
session_unset();

$passedSite = $_GET['site'];
$hiddenSite = $passedSite;

session_start();


$passedErr = $_GET['err'];

require_once ("mbolib.php");
require_once ("mboedits.php");

if ($passedErr != "0001") 
{

	mboDBlogin();


	$stuid = htmlspecialchars($_POST['sid']);
	$stupw = htmlspecialchars($_POST['spn']);

	$btnSubmit = $_POST['btnSubmit'];

	// Validate user entered data
	// Max length = 20
	// alphanumeric only
	$edits_ok = "Y";

	if (isValidLength($stuid, 1, 20) != "Y")
	{
		$passedErr = "1003";
		mboLogError ("badinput: Invalid length (" . strlen($stuid) . ") for stuid: $stuid");
		$edits_ok = "N";
	}
	if (isValidLength($stupw, 1, 20) != "Y")
	{
		$passedErr = "1003";
		mboLogError ("badinput: Invalid length (" . strlen($stupw) . ") for stupw: stuid=$stuid");
		$edits_ok = "N";
	}
	if (isAlphaNumeric($stuid)!="Y") 
	{
		$passedErr = "1004";
		mboLogError ("badinput: Invalid characters in stuid: $stuid"); 
		$edits_ok = "N";
	}
	if (isValidStudentPw($stupw)!="Y") 
	{
		$passedErr = "1004";
		mboLogError ("badinput: Invalid characters in stupw: stuid=$stuid"); 
		$edits_ok = "N";
	}
	
	//mboLogError ("debug: EDITS_OK=$edits_ok");

	if ($edits_ok == "Y")
	{
		//mboLogError ("debug: EDITS_OK");


		$sql = ' SELECT pw_salt, disabled, attempts_left, force_new_pw  ' 
			. ' FROM mboUserSecurity '
			. ' WHERE site = "' . $passedSite . '"'
			. ' AND user_id = "' . strtoupper($stuid) . '"'
			. ' AND active = "Y"'
			;

		//mboLogError ("sql:$sql");

		if (!($results = mysql_query($sql)))
		{
			mboLogError("authMbo.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());
			mboFatalError();
		}

		$numRows = mysql_num_rows($results);
		//mboLogError ("debug: numrows=$numRows");
	
		if ($numRows != 1)
		{	
			$passedErr = "1001";
			mboLogError ("badlogin: UNAUTHORIZED STUDENT LOGIN ATTEMPT: site=$passedSite stuid=$stuid");		
		}
		else
		{
			$row = mysql_fetch_array($results);
			$salt = $row[0];
			$disabled = $row[1];
			$attemptsleft = $row[2];
			$forcepw = $row[3];

			if ($disabled == "Y")
			{
				$passedErr = "1005";
				mboLogError ("access: STUDENT WITH DISABLED ACCOUNT ATTEMPTED LOGIN: site=$passedSite stuid=$stuid");
			}
			else
			{		
				$sql = ' SELECT 1 ' 
					. ' FROM mboUserSecurity '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($stuid) . '"'
					. ' AND password = '
					. '     PASSWORD("' . $passedSite . strtoupper($stuid) . $salt . strtoupper($stupw) . '")'
					. ' AND active = "Y"'
				;
				$safesql = ' SELECT 1 ' 
					. ' FROM mboUserSecurity '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($stuid) . '"'
					. ' AND password = '
					. '     PASSWORD("' . $passedSite . strtoupper($stuid) . $salt . '********' . '")'
					. ' AND active = "Y"'
				;
	
				//DO NOT PRINT THIS SQL TO THE LOG!
			
				if (!($results = mysql_query($sql)))
				{
					mboLogError("authMbo.php: SQL Error: SQL=$safesql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}
	
				$numRows = mysql_num_rows($results);
				//mboLogError ("debug: numrows=$numRows");
		
	
				if ($numRows == 1)
				{
					$passedErr = "0002";
					mboLogError ("access: STUDENT LOGGED IN: site=$passedSite stuid=$stuid");	

					$sql = 'UPDATE mboUserSecurity '
					. ' SET attempts_left = 3 '
					. ' , last_login = NOW() '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($stuid) . '"'
					. ' AND active = "Y"';

					if (!($results = mysql_query($sql)))
					{
						mboLogError("authMbo.php: SQL Error: SQL=$sql");
						mboLogError ("DB error: " . mysql_error());
						mboFatalError();
					}
	
					createMboStudentSession( $stuid, $passedSite );

					//mboLogError ("debug: btnSubmit=$btnSubmit");
					//mboLogError ("debug: forcepw=$forcepw");
					
					if ($btnSubmit == "Change Password")
					{
						header("Location:mboChgStudentPw.php?err=0001");
					}
					else if ($forcepw == "Y")
					{
						header("Location:mboChgStudentPw.php?err=0003");
					}				
				}
				else
				{
					$passedErr = "1002";
					mboLogError ("badlogin: INVALID PASSWORD ENTERED BY STUDENT: site=$passedSite stuid=$stuid");	

					if ($attemptsleft > 0)
					{
						$attemptsleft --;
					}
					else
					{
						$attemptsleft = 0;
					}
					if ($attemptsleft <= 0 )
					{
						$disabled = "Y";
						$passedErr = "1006";
					}

					$sql = 'UPDATE mboUserSecurity '
					. ' SET attempts_left = ' . $attemptsleft
					. ' , disabled = "' . $disabled . '"'
					. ' , last_failed_attempt = NOW() '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($stuid) . '"'
					. ' AND active = "Y"';

					if (!($results = mysql_query($sql)))
					{
						mboLogError("authMbo.php: SQL Error: SQL=$sql");
						mboLogError ("DB error: " . mysql_error());
						mboFatalError();
					}
				}
			}
		}	
	}
}

//mboLogError ("debug: passedErr=$passedErr");

include("mboHeader.txt");
echo "<h1>Student Login</h1><P>";

if ($passedErr == "0001")
{
	echo "<h3><font color=blue>Please Login.</font></h3>";
	$passedErr = "1001";
}
else
{ 
	if ($passedErr == "0002")
	{
		echo "<h3><font color=blue>Successful Login.</font></h3>";
	}
	if ($passedErr == "1001")
	{
		echo "<h3><font color=red>Error: Invalid Login.</font></h3>";
	}
	if ($passedErr == "1002")
	{
		echo "<h3><font color=red>Error: Invalid Login.</font></h3>";
	}
	if ($passedErr == "1003")
	{
		echo "<h3><font color=red>Error: Invalid Login.</font></h3>";
	}
	if ($passedErr == "1004")
	{
		echo "<h3><font color=red>Error: Invalid Login.</font></h3>";
	}
	if ($passedErr == "1005")
	{
		echo "<h3><font color=red>Error: Your account is disabled.  Please contact your instructor.</font></h3>";
	}
	if ($passedErr == "1006")
	{
		echo "<h3><font color=red>Error: Your account has been disabled.  Please contact your instructor.</font></h3>";
	}
}

echo '<P><form action="authMbo.php?site=';
echo "$passedSite";
echo "&err=";
echo "$passedErr";
echo '" method=POST>';

if ($passedErr == "0002")
{
	include("authMboGood.txt");
}
else
{
	include("authMboAft.txt");
}

echo "</table>";
echo "</html>";

?>
