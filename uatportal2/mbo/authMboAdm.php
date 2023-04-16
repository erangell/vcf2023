<?php
session_unset();

$passedSite = $_GET['site'];
$hiddenSite = $passedSite;

session_start();

$passedErr = $_GET['err'];

require_once ("mbolib.php");
require_once ("mboedits.php");

$tracedebug = 0; // set to 1 for debugging messages to be written to log

if ($passedErr != "0001") 
{

	mboDBlogin();

	$admid = htmlspecialchars($_POST['sid']);
	$admpw = htmlspecialchars($_POST['spn']);

	$btnSubmit = $_POST['btnSubmit'];

	// Validate user entered data
	// Max length = 20
	// alphanumeric only

	$edits_ok = "Y";

	if (isValidLength($admid, 1, 20) != "Y")
	{
		$passedErr = "1003";
		mboLogError ("badinput: Invalid length (" . strlen($admid) . ") for admid: $admid");
		$edits_ok = "N";
	}
	if (isValidLength($admpw, 1, 20) != "Y")
	{
		$passedErr = "1003";
		mboLogError ("badinput: Invalid length (" . strlen($admpw) . ") for admpw: admid=$admid");
		$edits_ok = "N";
	}
	if (isAlphaNumeric($admid)!="Y") 
	{
		$passedErr = "1004";
		mboLogError ("badinput: Invalid characters in admid: $admid"); 
		$edits_ok = "N";
	}
	if (isValidAdminPw($admpw)!="Y") 
	{
		$passedErr = "1004";
		mboLogError ("badinput: Invalid characters in admpw: admid=$admid"); 
		$edits_ok = "N";
	}
	
	//mboLogError ("debug: EDITS_OK=$edits_ok");

	if ($edits_ok == "Y")
	{
		//mboLogError ("debug: EDITS_OK");

		$sql = ' SELECT pw_salt, disabled, attempts_left, force_new_pw  ' 
			. ' FROM mboAdmSecurity '
			. ' WHERE site = "' . $passedSite . '"'
			. ' AND user_id = "' . strtoupper($admid) . '"'
			. ' AND active = "Y"'
		;

		if ($tracedebug == 1)
		{
			mboLogError ("debug: sql=$sql");
		}

		if (!($results = mysql_query($sql)))
		{
			mboLogError("authMboAdm.php: SQL Error: SQL=$sql");
			mboLogError ("DB error: " . mysql_error());

			mboFatalError();
		}

		$numRows = mysql_num_rows($results);

		if ($tracedebug == 1)
		{
			mboLogError ("debug: numrows=$numRows");
		}

		if ($numRows != 1)
		{	
			$passedErr = "1001";
			mboLogError ("badlogin: UNAUTHORIZED USER LOGIN ATTEMPT: site=$passedSite admid=$admid");		
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
				mboLogError ("access: ADMIN WITH DISABLED ACCOUNT ATTEMPTED LOGIN: site=$passedSite admid=$admid");
			}
			else
			{		
	
				$sql = ' SELECT 1 ' 
					. ' FROM mboAdmSecurity '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($admid) . '"'
					. ' AND password = '
					. '     PASSWORD("' . $salt . strtoupper($admpw) . $passedSite . strtoupper($admid) . '")'
					. ' AND active = "Y"'
				;
				$safesql = ' SELECT 1 ' 
					. ' FROM mboAdmSecurity '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($admid) . '"'
					. ' AND password = '
					. '     PASSWORD("' . $salt . '********' . $passedSite . strtoupper($admid) . '")'
					. ' AND active = "Y"'
				;

				//DO NOT PRINT THIS SQL TO THE LOG!
				if ($tracedebug == 1)
				{
					mboLogError ("debug: sql=$safesql");
				}

		
				if (!($results = mysql_query($sql)))
				{
					mboLogError("authMboAdm.php: SQL Error: SQL=$safesql");
					mboLogError ("DB error: " . mysql_error());
					mboFatalError();
				}

				$numRows = mysql_num_rows($results);
				if ($tracedebug == 1)
				{
					mboLogError ("debug: numrows=$numRows");
				}
	

				if ($numRows == 1)
				{
					$passedErr = "0002";

					mboLogError ("access: ADMIN LOGGED IN: site=$passedSite admid=$admid");	


					$sql = 'UPDATE mboAdmSecurity '
					. ' SET attempts_left = 3 '
					. ' , last_login = NOW() '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($admid) . '"'
					. ' AND active = "Y"';


					if ($tracedebug == 1)
					{
						mboLogError ("debug: sql=$sql");
					}

					if (!($results = mysql_query($sql)))
					{
						mboLogError("authMboAdm.php: SQL Error: SQL=$sql");
						mboLogError ("DB error: " . mysql_error());
						mboFatalError();
					}


					createMboAdminSession( $admid, $passedSite );

					if ($btnSubmit == "Change Password")
					{
						header("Location:mboChgAdminPw.php?err=0001");
					}
					else if ($forcepw == "Y")
					{
						header("Location:mboChgAdminPw.php?err=0003");
					}				
				}
				else
				{
					$passedErr = "1002";	
					mboLogError ("badlogin: INVALID PASSWORD ENTERED BY USER: site=$passedSite admid=$admid");		

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

					$sql = 'UPDATE mboAdmSecurity '
					. ' SET attempts_left = ' . $attemptsleft
					. ' , disabled = "' . $disabled . '"'
					. ' , last_failed_attempt = NOW() '
					. ' WHERE site = "' . $passedSite . '"'
					. ' AND user_id = "' . strtoupper($admid) . '"'
					. ' AND active = "Y"';


					if ($tracedebug == 1)
					{
						mboLogError ("debug: sql=$sql");
					}

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
echo "<h1>Administrator Login</h1><P>";

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
		echo "<h3><font color=red>Error: Your account is disabled.  Please contact your administrator.</font></h3>";
	}
	if ($passedErr == "1006")
	{
		echo "<h3><font color=red>Error: Your account has been disabled.  Please contact your administrator.</font></h3>";
	}

}

echo '<P><form action="authMboAdm.php?site=';
echo "$passedSite";
echo "&err=";
echo "$passedErr";
echo '" method=POST>';

if ($passedErr == "0002")
{
	echo '<a href="mboAdmMenu.php">Click here to go to the Main Menu</a>';
}
else
{
	include("authMboAdmAft.txt");
}

echo "</table>";
echo "</html>";


?>
