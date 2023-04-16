<?php
require_once ("mbolib.php");
mboDBlogin();
session_start();
$valkey = validateMboStudentSession();
if (substr($valkey,0,12) != "SUCCS_VLDTN|")
{
	header ("Location: stuIndex.php");
}
else
{

	$stupart = explode('|',$valkey);
	$stusite = $stupart[1];
	$stuuid  = $stupart[2];

	$ex_triad_ear    = 1;
	$ex_intv_ear_min = 2;
	$ex_intv_ear_maj = 3;
	$ex_triad_c_ls = 4;
	$ex_triad_i_ls = 5;
	$ex_triad_c_maj = 6;
	$ex_triad_i_maj = 7;
	$ex_triad_c_nat = 8;
	$ex_triad_i_nat = 9;
	$ex_triad_c_har = 10;
	$ex_triad_i_har = 11;
	$ex_triad_c_mel = 12;
	$ex_triad_i_mel = 13;
	$ex_min_sc_deg = 14;
	$ex_kbd_bass = 15;	
	$ex_kbd_treble = 16;	
	$ex_note_treble = 17;
	$ex_note_bass = 18;
	$ex_leger_treble = 19;
	$ex_leger_bass = 20;
	$ex_maj_scales = 21;
	$ex_maj_sc_deg = 22;
	$ex_maj_solf = 23;
	$ex_maj_key = 24;
	$ex_int_i_pm = 25;
	$ex_int_c_pm = 26;
	$ex_int_i_1oct = 27;
	$ex_int_c_1oct = 28;
	$ex_int_i_comp = 29;
	$ex_int_c_comp = 30;
	$ex_int_c_1oct_d = 31;
	$ex_int_c_comp_d = 32;
	$ex_int_inv = 33;
	$ex_min_sc = 34;
	$ex_min_key = 35;
	$ex_solf_nat = 36;
	$ex_solf_har = 37;
	$ex_solf_mel = 38;
	$ex_rhy_1 = 39;
	$ex_rhy_2 = 40;
	$ex_rhy_3 = 41;
	$ex_time_sig = 42;


	include("mboHeader.txt");

	$sql = ' SELECT examID, examTitle '
	. ' FROM '
	. '  mboExam '
	. ' ORDER BY examID '
	;
	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboExamGrid.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}
	$numExams = mysql_num_rows($results);
	$examindx = 0;
	while ($row = mysql_fetch_array($results))
	{
		$examId[$examindx] = $row[0];
		$examTitle[$examindx] = $row[1];
		$examindx++;
	}

	$numExams = $numExams+3;
	$gridRow[numExams-1]="N";
	$gridRow[numExams-2]="N";
	$gridRow[numExams-3]="N";


	for ($ix=1 ; $ix <= $numExams ; $ix++)
	{
		$gridRow[$ix]="To Do";
	}


	$sql = ' SELECT u.lastName, u.firstName '
	. ' , el.updateTime , el.Result, ex.examTitle, ex.examID  '
	. ' FROM '
	. '   mboExamLog el '
	. ' , mboUser u '
	. ' , mboExam ex  '
	. ' WHERE ex.examId = el.examId '
	. ' and el.site_id = ' . "'" . $stusite . "'"
	. ' and el.result <> ' . "'STARTED'"
	. ' and u.user_id = el.user_id '
	. ' and u.siteId = el.site_id '
	. ' and u.user_id = ' . "'" . strtoupper($stuuid) . "'"
	. ' ORDER BY u.lastName, u.firstName, el.updateTime DESC '
	;


	if (!($results = mysql_query($sql)))
	{
		mboLogError("mboStuExlog.php: SQL Error: SQL=$sql");
		mboLogError ("DB error: " . mysql_error());
		mboFatalError();
	}


	#echo "Number of rows: ";
	$numrows = mysql_num_rows($results);

	if ($numrows == 0)
	{	
		#echo "<p><h3>No exam log data found</h3>";
	}
	else
	{
		echo "<p><h1>FUNDAMENTALS OF MUSIC: COURSE INDEX</h1>";

		$firstrow = 'Y';

		while ($row = mysql_fetch_array($results))
		{	
			if ($firstrow == 'Y')
			{
				$firstrow = 'N';
				echo "<h3>Student: $row[0], $row[1]</h3>";
				//echo "<p><table border=1 cellpadding=3>";
				//echo "<tr>";
				//echo "<td><b>Date/Time</b></td>";
				//echo "<td><b>Result</b></td>  ";
				//echo "<td><b>Exam</b></td>";
				//echo "</tr>";
			}
			//echo "<tr>";
			//echo "<td> $row[2] </td>  ";
			//echo "<td> $row[3] </td><td> $row[4] </td>";
			//echo "</tr>";

			$gridIndex = -1;
			for ($ix = 0 ; $ix < $numExams ; $ix++)
			{
				if ($row[5] == $examId[$ix])
				{
					$gridIndex = $ix+1;
				}
			}

			if ($gridIndex >= 0)
			{
				//If student has one PASSED record, mastery = Y
				//If student has FAILED record, and no PASSED record yet found, mastery = N

				if ($row[3] == "FAILED")
				{
					if ($gridRow[$gridIndex] != "Y")
					{
						$gridRow[$gridIndex] = "<b>N</b>";
					}
				}				
				else
				{	if ($row[3] == "PASSED")
					{
						$gridRow[$gridIndex] = "Y";
					}
				}
			}

		}

		//echo "</table>";
		echo "<p><table border=1 cellpadding=3><tr>";

		//for ($ix=1 ; $ix <= $numExams ; $ix++)
		//{
		//	echo "<td>$ix $gridRow[$ix]</td>";
		//}

		echo "</tr></table>";

	}
}
?>



<table border=1 cellpadding=3>
<tr>
<td colspan=3><b><u>Unit</u></b></td>
<td colspan=3><b><u>Activities</u></b></td>
<td><b><u>Mastered</u></b></td>
</tr>



<tr>
<td colspan=7><b><i>0.0 Computer Preparation</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>0.1</b></td>
<td><b>Sound Check</b></td>
<td><a href="mboSoundCheck.htm">Tutorial</a></td>
<td>&nbsp;
<td>&nbsp;
<td>&nbsp;
</td>
</tr>




<tr>
<td colspan=7><b><i>1.0 Note Reading</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.1</b></td>
<td><b>Treble Clef</b></td>
<td><a href="note1.htm">Tutorial</a></td>
<td><a href="note4.htm">Practice</a></td>
<td><a href="note13.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_note_treble]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.2</b></td>
<td><b>Bass Clef</b></td>
<td><a href="note5.htm">Tutorial</a></td>
<td><a href="note7.htm">Practice</a></td>
<td><a href="note17.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_note_bass]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.3</b></td>
<td><b>Treble Clef Leger Lines</b></td>
<td><a href="note9.htm">Tutorial</a></td>
<td><a href="note10.htm">Practice</a></td>
<td><a href="note20.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_leger_treble]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.4</b></td>
<td><b>Bass Clef Leger Lines</b></td>
<td><a href="note11.htm">Tutorial</a></td>
<td><a href="note12.htm">Practice</a></td>
<td><a href="note23.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_leger_bass]</b></center>"
?>
</td>
</tr>



<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.5</b></td>
<td><b>Musical Keyboard - Treble Clef</b>
<br>(Including also Sharps and Flats)</td>
</td>
<td><a href="note8.htm">Tutorial</a></td>
<td><a href="pianokey2.htm">Practice</a></td>
<td><a href="pianokey5.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_kbd_treble]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>1.6</b></td>
<td><b>Musical Keyboard - Bass Clef</b></td>
<td><a href="pianokey3.htm">Tutorial</a></td>
<td><a href="pianokey4.htm">Practice</a></td>
<td><a href="pianokey9.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_kbd_bass]</b></center>"
?>
</td>
</tr>






<tr>
<td colspan=7><b><i>2.0 Major Scales</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>2.1</b></td>
<td><b>Major Scale Construction</b></td>
<td><a href="majscales1.htm">Tutorial</a></td>
<td><a href="majscales5.htm">Practice</a></td>
<td><a href="majscales8.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_maj_scales]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>2.2</b></td>
<td><b>Major Scale Degrees and Functions</b></td>
<td><a href="majscales6.htm">Tutorial</a></td>
<td><a href="majscales7.htm">Practice</a></td>
<td><a href="majscales14.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_maj_sc_deg]</b></center>"
?>
</td>
</tr>

<tr>
<td colspan=7><b><i>3.0 Major Scales Solfege</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>3.1</b></td>
<td><b>Major Scales Solfege</b></td>
<td><a href="majsolfege1.htm">Tutorial</a></td>
<td><a href="majsolfege2.htm">Practice</a></td>
<td><a href="majsolfege3.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_maj_solf]</b></center>"
?>
</td>
</tr>

<tr>
<td colspan=7><b><i>4.0 Major Scales Sight Singing</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>4.2</b></td>
<td><b>Sight Singing: C Major</b></td>
<td><a href="majsightsing0.htm">Tutorial</a></td>
<td><a href="majsightsing1.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>4.3</b></td>
<td><b>Sight Singing: Other Major Keys</b></td>
<td><a href="majsightsing0.htm">Tutorial</a></td>
<td><a href="majsightsing2.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>


<!-- NEW VERSION BACKED OUT 10/28/08 
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>4.2</b></td>
<td><b>Sight Singing: C Major</b></td>
<td><a href="ssingmaj1.htm">Tutorial</a></td>
<td><a href="ssingmaj2.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>4.3</b></td>
<td><b>Sight Singing: Other Major Keys</b></td>
<td><a href="ssingmaj1.htm">Tutorial</a></td>
<td><a href="ssingmaj3.htm">Practice</a></td>
<td><a href="ssingmaj3.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[1]</b></center>"
?>
</tr>
-->



<tr>
<td colspan=7><b><i>5.0 Major Key Signatures</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>5.1</b></td>
<td><b>Major Key Signatures</b></td>
<td><a href="majkey1.htm">Tutorial</a></td>
<td><a href="majkey11.htm">Practice</a></td>
<td><a href="majkey13.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_maj_key]</b></center>"
?>
</td>
</tr>

<tr>
<td colspan=7><b><i>6.0 Intervals</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.1</b></td>
<td><b>
Simple Interval Identification 
<br>(Top Note is in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals1.htm">Tutorial</a></td>
<td><a href="intervals6.htm">Practice</a></td>
<td><a href="intervals26.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_i_pm]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.2</b></td>
<td><b>
Simple ascending Interval Construction 
<br>(Top Note is in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals7.htm">Tutorial</a></td>
<td><a href="intervals8.htm">Practice</a></td>
<td><a href="intervals29.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_c_pm]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.3</b></td>
<td><b>
Simple Interval Identification 
<br>(Top Note is NOT in Major Scale of Bottom Note) 
</b></td>
<td><a href="intervals9.htm">Tutorial</a></td>
<td><a href="intervals13.htm">Practice</a></td>
<td><a href="intervals32.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_i_1oct]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.4</b></td>
<td><b>
Simple Ascending Interval Construction 
<br>(Top Note is NOT in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals14.htm">Tutorial</a></td>
<td><a href="intervals15.htm">Practice</a></td>
<td><a href="intervals35.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_c_1oct]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.5</b></td>
<td><b>
Compound Interval Identification 
<br>(Top Note is NOT in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals16.htm">Tutorial</a></td>
<td><a href="intervals17.htm">Practice</a></td>
<td><a href="intervals38.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_i_comp]</b></center>"
?>
</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.6</b></td>
<td><b>
Compound Ascending Interval Construction 
<br>(Top Note is NOT in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals18.htm">Tutorial</a></td>
<td><a href="intervals19.htm">Practice</a></td>
<td><a href="intervals41.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_c_comp]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.7</b></td>
<td><b>Simple Descending Interval Construction 
<br>(Top Note is NOT in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals20.htm">Tutorial</a></td>
<td><a href="intervals21.htm">Practice</a></td>
<td><a href="intervals44.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_c_1oct_d]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.8</b></td>
<td><b>Compound Descending Interval Construction 
<br>(Top Note is NOT in Major Scale of Bottom Note)
</b></td>
<td><a href="intervals22.htm">Tutorial</a></td>
<td><a href="intervals23.htm">Practice</a></td>
<td><a href="intervals47.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_c_comp_d]</b></center>"
?>
</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>6.9</b></td>
<td><b>
Identifiying Inversion of Intervals
</b></td>
<td><a href="intervals24.htm">Tutorial</a></td>
<td><a href="intervals25.htm">Practice</a></td>
<td><a href="intervals50.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_int_inv]</b></center>"
?>
</td>
</tr>


<tr>
<td colspan=7><b><i>7.0 Minor Scales and Key Signatures</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>7.1</b></td>
<td><b>Minor Scales</b></td>
<td><a href="minscales1.htm">Tutorial</a></td>
<td><a href="minscales4.htm">Practice</a></td>
<td><a href="minscales8.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_min_sc]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>7.2</b></td>
<td><b>Minor Scale Degrees and Functions</b></td>
<td><a href="minscaledeg1.htm">Tutorial</a></td>
<td><a href="minscaledeg2.htm">Practice</a></td>
<td><a href="minscaledeg3.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_min_sc_deg]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>7.3</b></td>
<td><b>Minor Key Signatures</b></td>
<td><a href="minscales5.htm">Tutorial</a></td>
<td><a href="minscales7.htm">Practice</a></td>
<td><a href="minscales10.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_min_key]</b></center>"
?>
</td>
</tr>

<tr>
<td colspan=7><b><i>8.0 Minor Scales Solfege</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>8.1</b></td>
<td><b>Natural Minor</b></td>
<td><a href="minsolfege1.htm">Tutorial</a></td>
<td><a href="minsolfege2.htm">Practice</a></td>
<td><a href="minsolfege5.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_solf_nat]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>8.2</b></td>
<td><b>Harmonic Minor</b></td>
<td><a href="minsolfege1.htm">Tutorial</a></td>
<td><a href="minsolfege3.htm">Practice</a></td>
<td><a href="minsolfege6.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_solf_har]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>8.3</b></td>
<td><b>Melodic Minor</b></td>
<td><a href="minsolfege1.htm">Tutorial</a></td>
<td><a href="minsolfege4.htm">Practice</a></td>
<td><a href="minsolfege7.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_solf_mel]</b></center>"
?>
</td>
</tr>

<tr>
<td colspan=7><b><i>9.0 Minor Scales Sight Singing</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.1</b></td>
<td><b>Sight Singing: Natural Minor</b></td>
<td><a href="minsightsing0.htm">Tutorial</a></td>
<td><a href="minsightsing1.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.2</b></td>
<td><b>Sight Singing: Harmonic Minor</b></td>
<td><a href="minsightsing0.htm">Tutorial</a></td>
<td><a href="minsightsing2.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.3</b></td>
<td><b>Sight Singing: Melodic Minor</b></td>
<td><a href="minsightsing0.htm">Tutorial</a></td>
<td><a href="minsightsing3.htm">Practice</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>


<!-- NEW VERSION BACKED OUT 10/28/08
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.1</b></td>
<td><b>Sight Singing: Natural Minor</b></td>
<td><a href="ssingmin1.htm">Tutorial</a></td>
<td><a href="ssingmin2.htm">Practice</a></td>
<td><a href="ssingmin2.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[2]</b></center>"
?>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.2</b></td>
<td><b>Sight Singing: Harmonic Minor</b></td>
<td><a href="ssingmin1.htm">Tutorial</a></td>
<td><a href="ssingmin3.htm">Practice</a></td>
<td><a href="ssingmin3.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[3]</b></center>"
?>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>9.3</b></td>
<td><b>Sight Singing: Melodic Minor</b></td>
<td><a href="ssingmin1.htm">Tutorial</a></td>
<td><a href="ssingmin4.htm">Practice</a></td>
<td><a href="ssingmin4.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[4]</b></center>"
?>
</td>
</tr>
-->



<tr>
<td colspan=7><b><i>10.0 Rhythm</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>10.1</b></td>
<td><b>Simple Meter Pattern Identification – Level I</b></td>
<td><a href="rhythm1.htm">Tutorial</a></td>
<td><a href="rhythm17.htm">Practice</a></td>
<td><a href="rhythm20.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_rhy_1]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>10.2</b></td>
<td><b>Simple Meter Pattern Identification – Level II</b></td>
<td><a href="rhythm18.htm">Tutorial</a></td>
<td><a href="rhythm19.htm">Practice</a></td>
<td><a href="rhythm24.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_rhy_2]</b></center>"
?>
</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>10.3</b></td>
<td><b>Compound Meter Pattern Identification</b></td>
<td><a href="rhythm6.htm">Tutorial</a></td>
<td><a href="rhythm28.htm">Practice</a></td>
<td><a href="rhythm29.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_rhy_3]</b></center>"
?>
</td>
</tr>

<!--PLETS REMOVED 6/23/06
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>10.4</b></td>
<td><b>Information about Plets</b></td>
<td><a href="rhythm8.htm">Tutorial</a></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>
-->

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>10.4</b></td>
<td><b>Time Signatures</b></td>
<td><a href="rhythm13.htm">Tutorial</a></td>
<td><a href="rhythm33.htm">Practice</a></td>
<td><a href="rhythm34.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_time_sig]</b></center>"
?>
</td>
</tr>




<tr>
<td colspan=7><b><i>11.0 Chords</i></b></td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.1</b></td>
<td><b>Triad Construction from Lead Sheet Symbols</b></td>
<td><a href="triads1.htm">Tutorial</a></td>
<td><a href="triads5.htm">Practice</a></td>
<td><a href="triads5a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_c_ls]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.2</b></td>
<td><b>Triad Identification using Lead Sheet Symbols</b></td>
<td><a href="triads1a.htm">Tutorial</a></td>
<td><a href="triads6.htm">Practice</a></td>
<td><a href="triads6a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_i_ls]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.3</b></td>
<td><b>Triad Construction: Major</b></td>
<td><a href="triads7.htm">Tutorial</a></td>
<td><a href="triads9.htm">Practice</a></td>
<td><a href="triads9a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_c_maj]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.4</b></td>
<td><b>Triad Identification: Major</b></td>
<td><a href="triads7a.htm">Tutorial</a></td>
<td><a href="triads10.htm">Practice</a></td>
<td><a href="triads11a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_i_maj]</b></center>"
?>
</td>
</tr>


<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.5</b></td>
<td><b>Triad Construction: Natural Minor</b></td>
<td><a href="triads12.htm">Tutorial</a></td>
<td><a href="triads13.htm">Practice</a></td>
<td><a href="triads13a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_c_nat]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.6</b></td>
<td><b>Triad Identification: Natural Minor</b></td>
<td><a href="triads12a.htm">Tutorial</a></td>
<td><a href="triads14.htm">Practice</a></td>
<td><a href="triads15a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_i_nat]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.7</b></td>
<td><b>Triad Construction: Harmonic Minor</b></td>
<td><a href="triads16.htm">Tutorial</a></td>
<td><a href="triads17.htm">Practice</a></td>
<td><a href="triads17a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_c_har]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.8</b></td>
<td><b>Triad Identification: Harmonic Minor</b></td>
<td><a href="triads16a.htm">Tutorial</a></td>
<td><a href="triads18.htm">Practice</a></td>
<td><a href="triads19a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_i_har]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.9</b></td>
<td><b>Triad Construction: Melodic Minor</b></td>
<td><a href="triads20.htm">Tutorial</a></td>
<td><a href="triads21.htm">Practice</a></td>
<td><a href="triads21a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_c_mel]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>11.10</b></td>
<td><b>Triad Identification: Melodic Minor</b></td>
<td><a href="triads20a.htm">Tutorial</a></td>
<td><a href="triads22.htm">Practice</a></td>
<td><a href="triads23a.htm">Exam</a></td>
<td>
<?php
echo "<center><b>$gridRow[$ex_triad_i_mel]</b></center>"
?>
</td>
</tr>
<tr>
<td colspan=7><b><i>12.0 Ear Training</i></b></td>
</tr>
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>12.1</b></td>
<td><b>Major Scale Intervals</b></td>
<td><a href="intervaleartrain1.htm">Tutorial</a></td>
<td><a href="intervaleartrain1p.htm">Practice</a></td>
<td><a href="intervaleartrain1e.htm">Exam</a></td>

<td>
<?php
echo "<center><b>$gridRow[$ex_intv_ear_maj]</b></center>"
?>
</td>
</tr>
<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>12.2</b></td>
<td><b>Minor and Augmented Intervals</b></td>
<td><a href="intervaleartrain2.htm">Tutorial</a></td>
<td><a href="intervaleartrain2p.htm">Practice</a></td>
<td><a href="intervaleartrain2e.htm">Exam</a></td>

<td>
<?php
echo "<center><b>$gridRow[$ex_intv_ear_min]</b></center>"
?>
</td>
</tr>

<tr>
<td>&nbsp;&nbsp;&nbsp;</td>
<td><b>12.3</b></td>
<td><b>Triads</b></td>
<td><a href="triadeartrain1.htm">Tutorial</a></td>
<td><a href="TriadEarTrainPractice.htm">Practice</a></td>
<td><a href="TriadEarTrainPreExam.htm">Exam</a></td>

<td>
<?php
echo "<center><b>$gridRow[$ex_triad_ear]</b></center>"
?>
</td>
</tr>


</table>

<P><h3><a href="mboStuExLog.php">View Exam Log</a></h3>	

<P><h3><a href="mutheoryhome.php">Return to homepage</a></h3>	

</body>
</html>
