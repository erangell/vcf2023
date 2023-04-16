<?php

function isAlphaNumeric ( $inputstr )
{

	$retval = "Y";

	//mboLogError ("debug:isAlphaNumeric: inputstr=$inputstr");

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
	}
	return $retval;
}

function isValidLength($inputstr, $minchars, $maxchars)
{
	$retval = "Y";
	
	if ((strlen($inputstr) < $minchars) || (strlen($inputstr) > $maxchars))
	{
		$retval = "N";
	}
	return $retval;
}	

function isValidAdminPw ( $inputstr )
{

	$retval = "Y";

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@$*";
	$musthave1 = "0123456789";
	$mustct1 = 0;
	$musthave2 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	$mustct2 = 0;

	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
		if (strstr($musthave1, $inputstr[$i]))
		{
			$mustct1++;
		}
		if (strstr($musthave2, $inputstr[$i]))
		{
			$mustct2++;
		}
	}
	if (($mustct1 == 0) || ($mustct2 == 0))
	{
		$retval = "N";
	}
	return $retval;
}

function isValidSysAdminPw ( $inputstr )
{

	$retval = "Y";

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@$*";
	$musthave1 = "0123456789";
	$mustct1 = 0;
	$musthave2 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	$mustct2 = 0;
	$musthave3 = "@$*";
	$mustct3 = 0;

	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
		if (strstr($musthave1, $inputstr[$i]))
		{
			$mustct1++;
		}
		if (strstr($musthave2, $inputstr[$i]))
		{
			$mustct2++;
		}
		if (strstr($musthave3, $inputstr[$i]))
		{
			$mustct3++;
		}
	}
	if (($mustct1 == 0) || ($mustct2 == 0) || ($mustct3 == 0))
	{
		$retval = "N";
	}
	return $retval;
}


function isValidStudentPw ( $inputstr )
{

	$retval = "Y";

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	$musthave1 = "0123456789";
	$mustct1 = 0;
	$musthave2 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	$mustct2 = 0;

	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
		if (strstr($musthave1, $inputstr[$i]))
		{
			$mustct1++;
		}
		if (strstr($musthave2, $inputstr[$i]))
		{
			$mustct2++;
		}
	}
	if (($mustct1 == 0) || ($mustct2 == 0))
	{
		$retval = "N";
	}
	return $retval;
}

function isValidEmail ( $inputstr )
{

	$retval = "Y";

	//mboLogError ("debug:isValidEmail: inputstr=$inputstr");

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@._";

	$atCount=0;
	$dotCount=0;
	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
		if ($inputstr[$i] == "@")
		{
			$atCount++;
		}
		if ($inputstr[$i] == ".")
		{
			$dotCount++;
		}

	}
	if ($atCount <> 1)
	{
		$retval = "N";
	}
	if ($dotCount < 1)
	{
		$retval = "N";
	}	
	return $retval;
}

function isValidDescription ( $inputstr )
{

	$retval = "Y";

	//mboLogError ("debug:isAlphaNumeric: inputstr=$inputstr");

	$validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _";

	for ($i = 0 , $j = strlen($inputstr) ; $i < $j ; $i++ )
	{
		if (!(strstr($validchars, $inputstr[$i])))
		{
			$retval = "N";
		}
	}
	return $retval;
}

//References:
// 1. PHP Cookbook, O'Reilley Publishing, 2003.
?>