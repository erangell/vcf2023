//------------------------------------------------------------------------------
// UdmtModeDrill Applet -  Scale Drill and Exam for Major and Minor Scales
//------------------------------------------------------------------------------
// REMEMBER TO INCREMENT VERSION NUMBER FOR EACH RELEASE (udmtAppletVersion)
//------------------------------------------------------------------------------
// PARAMS:
//
// scaleType	=	The type of scale to present:
//				Values: 	
//				 IonianAsc
//				 IonianDesc
//				 DorianAsc
//				 DorianDesc
//				 PhrygianAsc
//				 PhrygianDesc
//				 LydianAsc
//				 LydianDesc
//				 MixolydianAsc
//				 MixolydianDesc
//				 AeolianAsc
//				 AeolianDesc
//				 LocrianAsc
//				 LocrianDesc
//						
//				REQUIRED
//
// examMode		=	Whether student is practicing or taking exam.
//				Values:	DRILL, EXAM
//				OPTIONAL: DEFAULT=DRILL
//
// nextURL		=	The URL to invoke when student either:
//				1. Clicks TAKE EXAM when in DRILL mode.
//				2. Passes the exam when in EXAM mode.
//				REQUIRED
//
// drillURL		=	The URL to invoke when the student fails the exam
//				when in EXAM mode.
//				REQUIRED IF examMode = EXAM
//
// examNumQues	=	The number of questions to administer in EXAM mode.
//				OPTIONAL. ONLY USED FOR EXAM MODE. DEFAULT=10
//
// examNumReqd	=	The number of questions required to pass the exam.
//				OPTIONAL. ONLY USED FOR EXAM MODE. DEFAULT=9
//
// suppressMIDI	=	'YES'=Suppress all MIDI logic (used for browsers that don't
//				have a version of Java that supports MIDI playback)
//				OPTIONAL: DEFAULT=NO (No suppression)
//
// drillExitURL	=     The URL to invoke when the student presses the EXIT button
//                      in Drill mode.
//				REQUIRED IF examMode = DRILL
//
// NOTE: Future enhancements for grading will require retrieval of param for 
// encrypted Student ID, and either storing exam score in database via JDBC
// or passing query string to nextURL when exam is passed.  Additional work
// might be required if it is a requirement to log all attempts made at
// passing the exam.
//
//------------------------------------------------------------------------------
// Modification History:
//
// 12/25/03 - Ver 0.08 - Fix selection process for minor scales to use 
// minor circle of 5ths, and added exit buttons to both Drill and Exam mode.
// When in Exam Mode, the Exit Button always returns to the Drill (drillURL).
// When in Drill Mode, the Exit button returns to the drillExitURL.
// Also removed re-presentation of missed questions after first round of 15.
//
// 05/01/2004: Ver0.09  - changed to use UdmtURLBuilder class to build URL's for buttons,
// and new behavior to branch to "Sorry" page immediately when student fails exam.
// Started adding interface logic to UdmtExamLog.
//
// 01/20/2005: ver 0.10 - changes to support exam login servlet
//
// 11/27/2005: ver 0.11 - cloned UdmtScaleDrill to UdmtModeDrill and modified to test modes
//
// 01/14/2006: ver 0.12 - integrating piano keyboard
// 12/26/2008: ver 0.14 - removed examlog dependency, changed instruction text
//------------------------------------------------------------------------------

/* TO DO:

POSSIBLE ENHANCEMENTS:
- debug keyboard processing logic
- setup accelerator keys for buttons
- Use graphic popup menu instead of Java's popup menu behavior. 
- Use the open notehead as the cursor when the mouse is within the bounds of the staff.
- handle missing param for nextURL - need to decide approach for handling all types of errors
- handle midiunavailable
- handle graphics file not found
- handle malformed URL in nextURL
- dispose of objects in applet Stop method
- Identify common modules for Drill and Test applets - use abstract classes to redesign common classes.
- Robot test cases

KNOWN BUGS:

- when midi unavailable in browser, applet refuses to load in drill mode but test mode works
(if browser only supports 1.3.1, error on metaevent listener-is this 1.4 only?)

QUESTIONS:

CODED - TO BE TESTED:

TESTED:
- Replace "Next Unit" button with "Take Exam" button.  This button should always be active.  It will confirm the user's intention to take the exam, and link to the URL passed in the NextURL param.
- Remove "9 out of previous 10" scoring logic
- score jumps when moving between questions
- Make the octave note enterable and ensure that the Play Correct and Play Mine functions play it.
- when Db treble clef is starting note, octave is above 2nd leger line and you get array out of bounds.
- move buttons to bottom 
-"Exam" mode, where 10 questions are asked with minimal feedback, and the student's answer is graded at the end.
- Bug fixed: Play Correct button may play previous scale if pressed immediately after Next button
- Bug fixed: missed ques may be capturing  next ques after missed ques
-allow student to select instrument for playback.
-improve performance - function instead of loop for cursor positioning
- add param DrillUrl in both HTML pages to be used to branch back to drill mode when student fails exam
- make sure when reshuffling the questions for the next iteration that first ques doesn't equal last one of prev set
-displays put in to test: missed ques may be capturing  next ques after missed ques
-after student passes or fails exam, they should not be allowed to continue answering questions
-mouse moved:  loop that determiines cursor position should be replaced with a mathematical function to derive row/col from x,y
-fixed bug where play correct plays ascending for descending scale
-fixed bug where show correct button doesn't work for descending scale
-multi-mode drills
- generalize exercise for any scale - major, natural minor, harmonic minor, melodic minor.
- ??? add parm for ascending/descending and adjust starting note range accordingly.  Add a graphic arrow pointing up or down depending on whether the student needs to write it ascending or descending?


*/

//------------------------------------------------------------------------------

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtModeDrill extends Applet
	implements MouseListener, MouseMotionListener, ActionListener, ItemListener, Runnable

//------------------------------------------------------------------------------
{
	private boolean _running = false;	//used for Runnable interface
	private Thread _looper;

	private String udmtAppletName			=	"UdmtModeDrill";
	private String udmtAppletVersion		=	"0.14"	;

	//05/01/2004:
	private UdmtURLBuilder urlBuilder;
	// End of 05/01/2004 change.		

	private String parmScaleType 			= 	"IonianAsc"; //default = Ionian scale ascending.

	private String dispScaleType			=	"";
	private int upDown 				= 	+1;  // +1=ascending , -1=descending

	private String parmExamMode 			= 	"";

	private String parmNextURL 			= 	"";
	private URL nextURL;

	private String parmDrillURL 			= 	"";
	private URL drillURL;

	private String parmDrillExitURL 		= 	"";
	private URL drillExitURL;

	private int showAbendScreen 			= 	0;
	private String txtAbend 			= 	"";

	// Dependent classes:
	private UdmtMidiOutQueue midiout;

	private Rectangle rectScreen;
	private Image imageScreen;

	private Image imgAllSymbols;
	private Image imgPiano;

	private UdmtPianoKeyboard muPiano;

	private UdmtMusicSymbol muStaff, muTrebleClef, muBassClef, muDot, muBar, muLeger;
	private UdmtMusicSymbol muNoteheadFilled, muNoteheadUnfilled, muNoteheadRed, muRtDblBar;
	private UdmtMusicSymbol muSharp, muFlat, muNatural, muDblSharp, muDblFlat;

	private int staffX, staffY;

	private int gClefXOffset 			= 	0;
	private int gClefYOffset 			= 	-20;
	private int fClefXOffset 			= 	0;
	private int fClefYOffset 			= 	-17;

	private int keySharpXOffset[] 		= 	{38,51,64,77,90,103,116};
	private int keySharpYOffset[] 		= 	{1,25,-5,18,41,6,34};
	private int keyFlatXOffset[] 			= 	{38,51,64,77,90,103,116};
	private int keyFlatYOffset[] 			= 	{34,8,41,18,50,25,57};

	private int staffNoteYPos[] 			= 	{-17,-9,-1,7,15,23,31,39,47
									,55,63,71,79,87,95,103,111};
	private int staffNoteXPos[] 			= 	{140,167,194,221,248,275,302,329
									,356,383,410,437,464,491,518,545};

	// Staff Y positions:
	// ------------------
	// 0 = 2nd leger line above staff
	// 1 = space above 1st leger line above staff
	// 2 = first leger line above staff
	// 3 = space above staff line 5
	// 4 = staff line 5
	// 5 = staff space 4
	// 6 = staff line 4
	// 7 = staff space 3
	// 8 = staff line 3
	// 9 = staff space 2
	// 10 = staff line 2
	// 11 = staff space 1
	// 12 = staff line 1
	// 13 = first space below staff line 1
	// 14 = first leger line below staff
	// 15 = space below 1st leger line below staff
	// 16 = 2nd leger line below staff

	// ver0.08 - changes made to support minor cycle of 5ths

	// Keys are chosen from the cycle of fifths:
	// Major Sharp keys: 	C  G  D  A  E  B  F# C#
	// Major Flat keys: 	C  F  Bb Eb Ab Db Gb Cb
	// Minor Sharp keys: 	A  E  B  F# C# G# D# A# 
	// Minor Flat keys: 	A  D  G  C  F  Bb Eb Ab 

	// ver0.08 - The following array is not being used:
	//private String strDisplayKeys[] 		= 	{"Cb","Gb","Db","Ab","Eb","Bb","F","C "
	//								,"G ","D ","A ","E ","B ","F#","C#"};

	// For generation of test questions - map number of sharps/flats to octave offsets (half steps above C) 
	// and staff Y positions

	// These are half steps above C for the starting note of each major key (0-7 sharps, 0-7 flats)
	private int cyc5shOffsetMaj[]			= 	{0,7,2,9,4,11,6,1};
	private int cyc5flOffsetMaj[]			= 	{0,5,10,3,8,1,6,11}; 

	// accidentals for starting note of each major key
	private char cyc5shAccMaj[] 			= 	{' ',' ',' ',' ',' ',' ','s','s'};
	private char cyc5flAccMaj[] 			= 	{' ',' ','f','f','f','f','f','f'};	

	// Staff Y positions on treble and bass clefs for sharps and flats for starting note of each key for Ascending scale
	private int cyc5shTrebMaj[] 			=  	{14,10,13,9,12,8,11,14};
	private int cyc5shBassMaj[] 			= 	{16,12,15,11,14,10,13,9}; 
	private int cyc5flTrebMaj[] 			=	{14,11,15,12,9,13,10,14}; 
	private int cyc5flBassMaj[] 			= 	{16,13,10,14,11,15,12,9}; 

	private int cyc5shTrebDescMaj[]		=  	{7,3,6,2,5,1,4,7}; 
	private int cyc5shBassDescMaj[]		= 	{9,5,8,4,7,3,6,2}; 
	private int cyc5flTrebDescMaj[]		= 	{7,4,1,5,2,6,3,7}; 
	private int cyc5flBassDescMaj[]		= 	{9,6,3,7,4,8,5,2}; 

	// ver0.08 - new arrays for minor keys

	// These are half steps above C for the starting note of each minor key (0-7 sharps, 0-7 flats)
	private int cyc5shOffsetMin[]			= 	{9,4,11,6,1,8,3,10};
	private int cyc5flOffsetMin[]			= 	{9,2,7,0,5,10,3,8}; 

	// accidentals for starting note of each minor key
	private char cyc5shAccMin[] 			= 	{' ',' ',' ','s','s','s','s','s'};
	private char cyc5flAccMin[] 			= 	{' ',' ',' ',' ',' ','f','f','f',};	

	// Staff Y positions on treble and bass clefs for sharps and flats for starting note of each key
	private int cyc5shTrebMin[] 			=  	{9,12,15,11,14,10,13,9};
	private int cyc5shBassMin[] 			= 	{11,14,10,13,16,12,15,11}; 
	private int cyc5flTrebMin[] 			=	{9,13,10,14,11,15,12,9}; 
	private int cyc5flBassMin[] 			= 	{11,15,12,16,13,10,14,11}; 

	private int cyc5shTrebDescMin[]		=  	{2,5,1,4,7,3,6,2}; 
	private int cyc5shBassDescMin[]		= 	{4,7,3,6,2,5,1,4}; 
	private int cyc5flTrebDescMin[]		= 	{2,6,3,7,4,1,5,2}; 
	private int cyc5flBassDescMin[]		= 	{4,1,5,2,6,3,7,4}; 


	private int MidiTreble[]			=	{84,83,81,79,77,76,74,72
									,71,69,67,65,64,62,60
									,59,57};

	private int MidiBass[]				=	{64,62,60
									,59,57,55,53,52,50,48
									,47,45,43,41,40,38,36
									};
	private int ScaleBeingTested[];

	private int IonianAsc[] 			= 	{2,2,1,2,2,2,1};
	private int IonianDesc[]			= 	{1,2,2,2,1,2,2};

	private int AltIonianAsc[] 			= 	{2,2,2,1,2,2,1};
	private int AltIonianDesc[]			= 	{1,2,2,1,2,2,2};

	private int DorianAsc[] 			= 	{2,1,2,2,2,1,2};
	private int DorianDesc[]			= 	{2,1,2,2,2,1,2};

	private int PhrygianAsc[] 			= 	{1,2,2,2,1,2,2};
	private int PhrygianDesc[]			= 	{2,2,1,2,2,2,1};

	private int LydianAsc[] 			= 	{2,2,2,1,2,2,1};
	private int LydianDesc[]			= 	{1,2,2,1,2,2,2};

	private int MixolydianAsc[] 			= 	{2,2,1,2,2,1,2};
	private int MixolydianDesc[]			= 	{2,1,2,2,1,2,2};

	private int AltMixolydianAsc[] 			= 	{2,2,2,1,2,1,2};
	private int AltMixolydianDesc[]			= 	{2,1,2,1,2,2,2};

	private int AeolianAsc[] 			= 	{2,1,2,2,1,2,2};
	private int AeolianDesc[]			= 	{2,2,1,2,2,1,2};

	private int LocrianAsc[] 			= 	{1,2,2,1,2,2,2};
	private int LocrianDesc[]			= 	{2,2,2,1,2,2,1};

	private int AltLocrianAsc[] 			= 	{2,1,2,1,2,2,2};
	private int AltLocrianDesc[]			= 	{2,2,2,1,2,1,2};

	private int NaturalMinorAsc[] 			= 	{2,1,2,2,1,2,2};
	private int NaturalMinorDesc[]			= 	{2,2,1,2,2,1,2};
	
	private int HarmonicMinorAsc[]			= 	{2,1,2,2,1,3,1};
	private int HarmonicMinorDesc[]			= 	{1,3,1,2,2,1,2};

	private int MelodicMinorAsc[]			= 	{2,1,2,2,2,2,1};
	private int MelodicMinorDesc[]			= 	{2,2,1,2,2,1,2};

	private int HungarianAsc[] 			= 	{2,1,3,1,1,3,1};
	private int HungarianDesc[]			= 	{1,3,1,1,3,1,2};

	private int MajNeapolitanAsc[] 			= 	{1,2,2,2,2,2,1};
	private int MajNeapolitanDesc[]			= 	{1,2,2,2,2,2,1};

	private int MinNeapolitanAsc[] 			= 	{1,2,2,2,1,3,1};
	private int MinNeapolitanDesc[]			= 	{1,3,1,2,2,2,1};

	private int EnigmaticAsc[] 			= 	{1,3,2,2,2,1,1};
	private int EnigmaticDesc[]			= 	{1,1,2,2,2,3,1};

	//----------------------------------------------------------------------

	private int userTestQuesInUse[];
	private int userTestQuestions[];
	private int userTestClefs[];
	private int userTestMinorType[];
	private int userTestQuesHead 			= 	0; //queue head
	private int userTestQuesTail 			= 	0; // queue tail

	private int gotThisQuesCorrect;
	//private int missedQuestions[];
	//private int missedClefs[];
	//private int missct;

	private int Udata[];
	private int Uacc[];
	private int Umidi;
	private int progch				=	0; 	// imnt# will be param.  0=general midi grand piano

	//----------------------------------------------------------------------
	
	private PopupMenu popup;
	private final int	MENU_HOR_OFFSET		=	18;
	private final int	MENU_VER_OFFSET		=	54;

	private int saveX, saveY;
	private int xLeft, yTop, xRight, yBottom; 		// area in which cursor becomes active for entering notes

	private int currentClef;
	private int startcursorrow, startcursorcol;
	private int cursorrow, cursorcol;
	private int cursorvisible			=	0 ;
	private int savecursorrow, savecursorcol ;

	private int 	numNoteheads 		= 	0;	// number of noteheads currently in the array
	private int 	noteheadInUse[];				// whether this slot in the array is in use or not
	private int 	noteheadRow[];				// which of the rows the notehead is in
	private int 	noteheadCol[];				// which of the columns the notehead is in 
	private char 	noteheadAccid[];				// accidental for the notehead
	private int 	noteheadColor[];				// 0=black  1=red

	private Button checkButton, nextButton, playmineButton, playcorrButton, takeExamButton, showcorrButton;
	private Button nextLessonButton, exitButton;

	private int currques;						// current question number being presented

	private int numtimeschecked 			= 	0;
	private int numquespresented 			= 	0;
	private int numquescorrect1sttime 		= 	0;

	private String strDrillMode			=	"Major Scale Practice";
	private String strExamMode			=	"Major Scale Exam";

	private String strInstruct			=	"";
	private String strInstruct1			=	"Write the ";
	private String strInstruct2 			= 	"diatonic major";
	private String strInstruct3 			= 	" scale beginning on the given note.";

	private String strScore1 			= 	"Score: ";
	private int nScore1 				= 	0;
	private String strScore2 			= 	" out of ";
	private int nScore2 				= 	0;
	private String strScore3 			= 	" = ";
	private int nScorePercent 			= 	0;
	private String strScore4 			= 	"%";
	private String strScoreDisp;

	private String strCorrect 			= 	"Correct.";
	private String strIncorrect 			= 	"Incorrect.  Please fix notes in red and try again.";
	private String strIncorrectExam 		= 	"Incorrect.";

	private String strMaster1 			= 	"Congratulations.  You have provided 9 out of 10";
	private String strMaster2 			= 	"correct answers and may  move on if you wish.";
	private String strFeedback 			= 	"";
	private String strExamFeedback1 		= 	"";
	private String strExamFeedback2 		= 	"";

	private int savecursorx, savecursory;

	private boolean examMode;

	private int prevques 				= 	100; 	// 100 indicates first time - no prev ques

	private Choice imntCombo;
	private int userImnt				=	0 ;

	private int mixedMinorAscMode 		=	0;

	// DEFAULT VALUES WHICH MAY BE OVERRIDEN BY PARAMS:
	private String parmExamNumQues;
	private int num_exam_ques			= 	10;
	private String parmExamNumReqd;
	private int num_reqd_to_pass 			= 	9;
	private String parmSuppressMIDI		=	"NO";
	private boolean suppressingMIDI 		= 	false;

	private int majorOrMinor			= 	0;
	// Determines whether Major or Minor scales are being tested.  1=Major, -1=Minor.

	private int trebleorbass = 1;
	// alternates between 1 and -1 as questions generated and assigned to random slots
	// This ensures the pattern of clefs doesn't repeat until after 30 questions.

	private int minorType = 0;
	// alternates between 0 and 2 as questions generated and assigned to random slots
	// this allows even distribution of all 3 types of minor scales.

	//FOR PIANO DISPLAY
	private int		cursorX, cursorY;
	private String 		param = "";
	private int		xMin=0,xMax=100,yMin=0,yMax=100,yMid=50;
	private int[]		xTop,xBot;
	private int		xTopCount=0,xBotCount=0;
	private int		midiTop=61,midiBot=60;
	private int		userMidiNote=60;
	private boolean		bShowGrid = false;


//------------------------------------------------------------------------------

	public void init()
	{
		// Display version and debugging info in Java Console
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		//System.out.println("AppletInfo: "+this.getAppletInfo());
		//System.out.println("ParameterInfo: "+this.getParameterInfo());
		//System.out.println("CodeBase: "+this.getCodeBase());
		//System.out.println("DocumentBase:" +this.getDocumentBase());

		//05/01/2004- New procedure for building URLs requires object to be instantiated once
		urlBuilder = new UdmtURLBuilder();
		// End of 05/01/2004 change.		

		// Retrieve Parm for Scale Type
		ScaleBeingTested = new int[7];
	
		//ver0.08 - added logic to set value of majorOrMinor in order to determine
		//          which cycle of 5ths to use when generating test questions.  Also fixed abend logic.

		parmScaleType = getParameter ("scaleType");
		if (parmScaleType == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: ScaleType is required";
			System.out.println ("The value for the parameter ScaleType must be one of the following:");
			System.out.println ("MajorAsc, MajorDesc, NaturalMinorAsc, NaturalMinorDesc, HarmonicMinorAsc,");
			System.out.println ("HarmonicMinorDesc, MelodicMinorAsc, MelodicMinorDesc, AllMinorAscMixed");
		}
		else if (parmScaleType.equals("IonianAsc"))	{
			for (int i=0 ; i<=6 ; i++)		{
				ScaleBeingTested[i] = IonianAsc[i];
			}
			dispScaleType="Ionian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = +1;
		}
		else if (parmScaleType.equals("MajorAsc"))	{
			for (int i=0 ; i<=6 ; i++)		{
				ScaleBeingTested[i] = IonianAsc[i];
			}
			dispScaleType="Major Ascending";
			upDown = +1;
			strDrillMode="Major Scale Practice";
			strExamMode="Major Scale Exam";
			majorOrMinor = +1;
		}
		else if (parmScaleType.equals("AltIonianAsc"))	{
			for (int i=0 ; i<=6 ; i++)		{
				ScaleBeingTested[i] = AltIonianAsc[i];
			}
			dispScaleType="Alt Ionian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = +1;
		}
		else if  (parmScaleType.equals("IonianDesc"))	{
			for (int i=0 ; i<=6 ; i++)			{
				ScaleBeingTested[i] = IonianDesc[i];
			}
			dispScaleType="Ionian Descending";
			upDown = -1;
			strDrillMode="Mode Scale Practice";
			strExamMode="Mode Scale Exam";
			majorOrMinor = +1;
		}
		else if  (parmScaleType.equals("AltIonianDesc"))	{
			for (int i=0 ; i<=6 ; i++)			{
				ScaleBeingTested[i] = AltIonianDesc[i];
			}
			dispScaleType="Alt Ionian Descending";
			upDown = -1;
			strDrillMode="Mode Scale Practice";
			strExamMode="Mode Scale Exam";
			majorOrMinor = +1;
		}
		else if  (parmScaleType.equals("DorianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = DorianAsc[i];
			}
			dispScaleType="Dorian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("DorianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = DorianDesc[i];
			}
			dispScaleType="Dorian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("PhrygianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = PhrygianAsc[i];
			}
			dispScaleType="Phrygian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("PhrygianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = PhrygianDesc[i];
			}
			dispScaleType="Phrygian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("LydianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = LydianAsc[i];
			}
			dispScaleType="Lydian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("LydianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = LydianDesc[i];
			}
			dispScaleType="Lydian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MixolydianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MixolydianAsc[i];
			}
			dispScaleType="Mixolydian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MixolydianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MixolydianDesc[i];
			}
			dispScaleType="Mixolydian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AltMixolydianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AltMixolydianAsc[i];
			}
			dispScaleType="Alt Mixolydian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AltMixolydianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AltMixolydianDesc[i];
			}
			dispScaleType="Alt Mixolydian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AeolianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AeolianAsc[i];
			}
			dispScaleType="Aeolian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AeolianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AeolianDesc[i];
			}
			dispScaleType="Aeolian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("LocrianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = LocrianAsc[i];
			}
			dispScaleType="Locrian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("LocrianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = LocrianDesc[i];
			}
			dispScaleType="Locrian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AltLocrianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AltLocrianAsc[i];
			}
			dispScaleType="Alt Locrian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AltLocrianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = AltLocrianDesc[i];
			}
			dispScaleType="Alt Locrian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("HarmonicMinorAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = HarmonicMinorAsc[i];
			}
			dispScaleType="Harmonic Minor Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("HarmonicMinorDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = HarmonicMinorDesc[i];
			}
			dispScaleType="Harmonic Minor Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MelodicMinorAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MelodicMinorAsc[i];
			}
			dispScaleType="Melodic Minor Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MelodicMinorDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MelodicMinorDesc[i];
			}
			dispScaleType="Melodic Minor Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("HungarianAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = HungarianAsc[i];
			}
			dispScaleType="Hungarian Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("HungarianDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = HungarianDesc[i];
			}
			dispScaleType="Hungarian Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("EnigmaticAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = EnigmaticAsc[i];
			}
			dispScaleType="Enigmatic Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("EnigmaticDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = EnigmaticDesc[i];
			}
			dispScaleType="Enigmatic Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MajNeapolitanAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MajNeapolitanAsc[i];
			}
			dispScaleType="Major Neapolitan Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MajNeapolitanDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MajNeapolitanDesc[i];
			}
			dispScaleType="Major Neapolitan Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MinNeapolitanAsc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MinNeapolitanAsc[i];
			}
			dispScaleType="Minor Neapolitan Ascending";
			upDown = +1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("MinNeapolitanDesc"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MinNeapolitanDesc[i];
			}
			dispScaleType="Minor Neapolitan Descending";
			upDown = -1;
			strDrillMode="Mode Practice";
			strExamMode="Mode Exam";
			majorOrMinor = -1;
		}
		else if  (parmScaleType.equals("AllMinorAscMixed"))	{
			mixedMinorAscMode = 1;
			upDown=+1;
			strDrillMode="Minor Scale Practice";
			strExamMode="Minor Scale Exam";
			majorOrMinor = -1;
		}
		else 	{
			showAbendScreen = 1;
			txtAbend = "Invalid value for param: ScaleType";
			System.out.println ("The value for the parameter ScaleType must be one of the following:");
			System.out.println ("MajorAsc, MajorDesc, NaturalMinorAsc, NaturalMinorDesc, HarmonicMinorAsc,");
			System.out.println ("HarmonicMinorDesc, MelodicMinorAsc, MelodicMinorDesc, AllMinorAscMixed");
		}

		//Retrieve param for examMode
		parmExamMode = getParameter ("examMode");
		examMode = parmExamMode.equals("EXAM");

		//Retrieve param for next URL		
		// ver0.08 - fixed logic for handling null parameters whenever a param is required.

		parmNextURL = getParameter ("nextURL");
		if (parmNextURL == null)
		{
			showAbendScreen = 1;
			txtAbend = "Parameter nextURL is required";
			System.out.println ("The parameter: nextURL needs to be added to the APPLET tag");
			System.out.println ("of the HTML file that is invoking this applet.");
		}
		else
		{
			//05/01/2004- new procedure for building url
			//old code: parmNextURL = this.getCodeBase() + parmNextURL;

			//01-20-2005: Use URLBUILDER only if not in exam mode
			if (examMode)
			{
 				UdmtExamKey objEk = new UdmtExamKey();
  				String sExamKey = objEk.getExamKey("Scales"+parmScaleType+"Pass");
  				parmNextURL = parmNextURL + "?r="+sExamKey;
  				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY
			}
			else
			{
				parmNextURL = urlBuilder.buildURL (this.getCodeBase() , parmNextURL);
			}


			//End of 05/01/2004 change

			try	{
				nextURL = new URL(parmNextURL);
			}
			catch ( MalformedURLException e )	{
				showAbendScreen = 1;
				txtAbend = "Malformed URL Exception when creating URL from parmNextURL";
				e.printStackTrace();
			}
			catch ( Exception e )	{
				showAbendScreen = 1;
				txtAbend = "Exception when creating URL from parmNextURL";
				e.printStackTrace();
			}
		}
		
		//System.out.println ("Next URL: "+parmNextURL);
		//System.out.println ("Retrieving URL param for Exit button");

		if (examMode)
		{
			//Retrieve param for Drill URL	
			parmDrillURL = getParameter ("drillURL");

			if (parmDrillURL == null)
			{
				showAbendScreen = 1;
				txtAbend = "Parameter drillURL is required for Exam Mode";
				System.out.println ("The parameter: drillURL needs to be added to the APPLET tag");
				System.out.println ("of the HTML file that is invoking this applet.");
			}
			else
			{
				//05/01/2004- new procedure for building url
				//old code: parmDrillURL = this.getCodeBase() + parmDrillURL ;

				//01-20-2005: use URLBUILDER only if not in exam mode
				if (examMode)
				{
 					UdmtExamKey objEk = new UdmtExamKey();
  					String sExamKey = objEk.getExamKey("Scales"+parmScaleType+"Fail");
  					parmDrillURL = parmDrillURL + "?r="+sExamKey;
  					//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY
				}
				else
				{
					parmDrillURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillURL );
				}

				//End of 05/01/2004 change

				try	{
					drillURL = new URL(parmDrillURL);
				}
				catch ( MalformedURLException e )	{
					showAbendScreen = 1;
					txtAbend = "Malformed URL Exception when creating URL from parmDrillURL";
					e.printStackTrace();
				}
				catch ( Exception e )	{
					showAbendScreen = 1;
					txtAbend = "Exception when creating URL from parmDrillURL";
					e.printStackTrace();
				}
			}

			//System.out.println ("Drill URL: "+parmDrillURL);
		}
		else
		{
			//Retrieve param for Drill Exit URL	
			parmDrillExitURL = getParameter ("drillExitURL");

			if (parmDrillExitURL == null)
			{
				showAbendScreen = 1;
				txtAbend = "Parameter drillExitURL is required for Drill Mode";
				System.out.println ("The parameter: drillExitURL needs to be added to the APPLET tag");
				System.out.println ("of the HTML file that is invoking this applet.");
			}
			else
			{
				//05/01/2004- new procedure for building url
				//old code: parmDrillExitURL = this.getCodeBase() + parmDrillExitURL;
				parmDrillExitURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillExitURL);
				//End of 05/01/2004 change


				try	{
					drillExitURL = new URL(parmDrillExitURL);
				}
				catch ( MalformedURLException e )	{
					showAbendScreen = 1;
					txtAbend = "Malformed URL Exception when creating URL from parmDrillExitURL";
					e.printStackTrace();
				}
				catch ( Exception e )	{
					showAbendScreen = 1;
					txtAbend = "Exception when creating URL from parmDrillExitURL";
					e.printStackTrace();
				}
			}

			//System.out.println ("Drill Exit URL: "+parmDrillExitURL);
		}


		//Retrieve param for examNumQues	
		
		if (examMode)
		{
			parmExamNumQues = getParameter("examNumQues");
			if ( parmExamNumQues != null)	{
				num_exam_ques = Integer.parseInt(parmExamNumQues);
			}
			//System.out.println ("num_exam_ques: "+num_exam_ques);
		}

		//Retrieve param for examNumReqd
		if (examMode)
		{
			parmExamNumReqd = getParameter("examNumReqd");
			if ( parmExamNumReqd != null )	{
				num_reqd_to_pass = Integer.parseInt(parmExamNumReqd);
			}
			//System.out.println ("num_reqd_to_pass: "+num_reqd_to_pass);
		}

		//System.out.println("Getting suppressMIDI parm...");

		//Retrieve param for suppressMIDI
		parmSuppressMIDI = getParameter ("suppressMIDI");
		if ( parmSuppressMIDI != null )	{
			if (parmSuppressMIDI.equals("YES"))	{
				suppressingMIDI = true ;
				System.out.println ("MIDI suppression is in effect.");
			}
		}

		//System.out.println("Got suppressMIDI parm...");


		//PARAMS FOR KEYBOARD DISPLAY
/***
 		if((param = getParameter("xTop")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			xTopCount = st.countTokens();           
 			xTop = new int[ xTopCount];            
			for(int i=0; i<xTopCount; i++) {            
				xTop[i] = Integer.parseInt(st.nextToken());          
				//System.out.println("i="+i+"xTop="+ xTop[i] );
			}
		}
 		if((param = getParameter("xBot")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			xBotCount = st.countTokens();           
 			xBot = new int[ xBotCount];            
			for(int i=0; i<xBotCount; i++) {            
				xBot[i] = Integer.parseInt(st.nextToken());          
				//System.out.println("i="+i+"xBot="+ xBot[i] );
			}
		}
 		if((param = getParameter("yMid")) != null) {
			yMid = Integer.parseInt(param);
		}
 		if((param = getParameter("yMin")) != null) {
			yMin = Integer.parseInt(param);
		}
 		if((param = getParameter("yMax")) != null) {
			yMax = Integer.parseInt(param);
		}
 		if((param = getParameter("xMin")) != null) {
			xMin = Integer.parseInt(param);
		}
 		if((param = getParameter("xMax")) != null) {
			xMax = Integer.parseInt(param);
		}
 		if((param = getParameter("midiTop")) != null) {
			midiTop = Integer.parseInt(param);
		}
 		if((param = getParameter("midiBot")) != null) {
			midiBot = Integer.parseInt(param);
		}

 		if((param = getParameter("showGrid")) != null) {
			if (param.equals("YES"))
			{
				bShowGrid = true;
			}
		}
***/

		
		// Initialize MIDI by calling the constructor of UdmtMidiOutQueue
		if (!suppressingMIDI)
		{
			System.out.println("Attempting to start MIDI...");
			try	{
				midiout = new UdmtMidiOutQueue();
			}
			catch ( Exception e )	{
				this.showStatus("ERROR STARTING MIDI");
				System.out.println("ERROR: Exception when creating UdmtMidiOutQueue:");
				e.printStackTrace();
			}
			System.out.println("MIDI initialized successfully.");
		}

		// Load graphics file of music symbols
		try 	{
			MediaTracker mt = new MediaTracker(this);
			URL url = getCodeBase();
			imgAllSymbols = getImage (url, "symbols-48pt-tr.gif");
			imgPiano = getImage (url, "keyboard4oct.gif");

			mt.addImage (imgAllSymbols, 1);
			mt.addImage (imgPiano, 2);
		
			mt.waitForAll();
			setUpSymbolObjects();
		}
		catch ( Exception e )	{
			this.showStatus("ERROR LOADING GRAPHICS");
			showAbendScreen = 1;
			txtAbend = "Exception when loading symbols-48pt-tr.gif";
			System.out.println("ERROR: Exception when loading symbols-48pt-tr.gif:");
			e.printStackTrace();
		}

		//Attach Piano Display to Midi Out queue
		if (!suppressingMIDI)
		{
    			midiout.udmtPianoKeyboard = muPiano;
		}

		noteheadInUse = new int[500];
		noteheadRow = new int[500];
		noteheadCol = new int[500];
		noteheadAccid = new char[500];
		noteheadColor = new int[500];

		userTestQuesInUse = new int[30];
		userTestQuestions = new int[30];
		userTestClefs=new int[30];
		userTestMinorType=new int[30];

		//missedQuestions=new int[30];
		//missedClefs=new int[30];

		Udata = new int[8];
		Uacc = new int[8];

		rectScreen = getBounds();
		staffX = 5;
		staffY = 140;	//OLD: rectScreen.height / 2 - 60;

		currentClef = randint (1,2);

		userTestQuesHead=0;
		userTestQuesTail=0;
		generate15questions(0);
		
		currques = 0;
		setCurrentQuestion (currques);
		
		xLeft = staffX + staffNoteXPos[2] - 5 ;
		xRight = staffX + staffNoteXPos[14] + 20 + 5 ;
		yTop = staffY + staffNoteYPos[0] - 20 - 5 ;
		yBottom = staffY + staffNoteYPos[16] - 20 + 20 ;

		setupPopupMenu();
		add ( popup );

		setLayout(null);

		int btnrow1 = 300;
		int btnrow2 = 350;

		checkButton = new Button("CHECK");
      	checkButton.addActionListener(this);
		checkButton.setEnabled(false);
		checkButton.setBounds ( 180,btnrow1,110,30);
		add (checkButton);

      	nextButton = new Button("NEXT");
      	nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		nextButton.setBounds ( 300,btnrow1,110,30);
		add(nextButton);

		if ( !examMode )	{

			playmineButton = new Button("PLAY MINE");
			playmineButton.addActionListener(this);
			if (suppressingMIDI)	{
				playmineButton.setEnabled(false);
			}
			else	{
				playmineButton.setEnabled(true);
			}
			playmineButton.setBounds ( 60,btnrow2,110,30);
			add(playmineButton);

			playcorrButton = new Button("PLAY CORRECT");
			playcorrButton.addActionListener(this);
	      	if (suppressingMIDI)	{
				playcorrButton.setEnabled(false);
			} 
			else	{
				playcorrButton.setEnabled(true);
			}
			playcorrButton.setBounds ( 180,btnrow2,110,30);
			add(playcorrButton);

			showcorrButton = new Button("SHOW CORRECT");
			showcorrButton.setEnabled(false);        
			showcorrButton.addActionListener(this);
			showcorrButton.setBounds ( 300,btnrow2,110,30);
			add(showcorrButton);

			takeExamButton = new Button("TAKE EXAM");
			takeExamButton.addActionListener(this);
			takeExamButton.setBounds ( 420,btnrow2,110,30);
			add(takeExamButton);
		}

		if ( examMode )	{

			nextLessonButton = new Button("NEXT LESSON");
			nextLessonButton.addActionListener(this);
	
			nextLessonButton.setVisible(false);
			nextLessonButton.setBounds ( 420,btnrow1,110,30);
			add(nextLessonButton);

		}

		// 0.08 - made exit button always visible 
		exitButton = new Button("EXIT");
		exitButton.addActionListener(this);
	
		exitButton.setVisible(true);
		exitButton.setBounds ( 420,btnrow1,110,30);
		add(exitButton);

		if ( !examMode )	{
			if (!suppressingMIDI)
			{
				imntCombo = new Choice();
				for (int i=0 ; i <= 127 ; i++)	{
					imntCombo.add ( midiout.GMImntList[i] );
				} 
				imntCombo.setBounds ( 60,btnrow1+5,110,30);
				add(imntCombo);
				imntCombo.addItemListener(this);
			}
		}

		addMouseListener(this);
		addMouseMotionListener(this);
	}

//------------------------------------------------------------------------------

	public void setUpSymbolObjects()
	{
		Image imgTemp;

		muStaff 				= new UdmtMusicSymbol();
		muStaff.CropImage 		( imgAllSymbols, 15, 200, 85, 280 );

		muTrebleClef 			= new UdmtMusicSymbol();
		muTrebleClef.CropImage 		( imgAllSymbols, 110, 0, 155, 120);

		muBassClef 				= new UdmtMusicSymbol();
		muBassClef.CropImage 		( imgAllSymbols, 260, 0, 310, 110);

		muDot	 				= new UdmtMusicSymbol();
		muDot.CropImage 			( imgAllSymbols, 450, 450, 457, 457);

		muNoteheadFilled 			= new UdmtMusicSymbol();
		muNoteheadFilled.CropImage	( imgAllSymbols, 25, 80, 50, 105 );

		muNoteheadUnfilled 		= new UdmtMusicSymbol();
		muNoteheadUnfilled.CropImage	( imgAllSymbols, 63, 80, 85, 105 );

		muBar 				= new UdmtMusicSymbol();
		muBar.CropImage 			( imgAllSymbols, 345, 200, 350, 280);	

		muRtDblBar 				= new UdmtMusicSymbol();
		muRtDblBar.CropImage		( imgAllSymbols, 290,200,310,280);	

		muSharp 				= new UdmtMusicSymbol();
		muSharp.CropImage 		( imgAllSymbols, 490,425,510,475);	

		muFlat	 			= new UdmtMusicSymbol();
		muFlat.CropImage 			( imgAllSymbols, 550,425,575,475);	

		muNatural 				= new UdmtMusicSymbol();
		muNatural.CropImage 		( imgAllSymbols, 615,425,630,475);	

		muDblSharp 				= new UdmtMusicSymbol();
		muDblSharp.CropImage 		( imgAllSymbols, 655,425,677,475);	

		muDblFlat 				= new UdmtMusicSymbol();
		muDblFlat.CropImage 		( imgAllSymbols,712,425,740,475);	

		muLeger 				= new UdmtMusicSymbol();
		muLeger.CropImage 		( imgAllSymbols, 388, 50, 423, 205 );

		muPiano 				= new UdmtPianoKeyboard();
		muPiano.CropImage 		( imgPiano, 0,0,573,100 );

		// create red notehead by cloning notehead image and changing pixel colors
		try 	{
			// Pixelgrabber logic from p.445 of Java AWT Reference:

			int pixels[] 	= new int [25*25];
			int redpixels[] 	= new int [25*25];
 
			PixelGrabber pg = new PixelGrabber
					( muNoteheadFilled.getImage()
					, 0
					, 0
					, 25
					, 25
					, pixels
					, 0
					, 25
					) ;

			pg.grabPixels();

			if ( (pg.status() & ImageObserver.ALLBITS) != 0)	{

				for (int y=0 ; y<=24 ; y++)				{
					for (int x=0 ; x <=24 ; x++)			{
						if (pixels[y*25+x] > 0)			{
							//System.out.print ("1");
							redpixels[y*25+x] = pixels[y*25+x];
						}
						else						{
							//System.out.print ("0");
							redpixels[y*25+x] = Color.red.getRGB();
						}
					}		
					//System.out.println();
				}

				imgTemp = createImage
					( new MemoryImageSource( 25, 25, redpixels, 0, 25)
					);
		
				muNoteheadRed = new UdmtMusicSymbol();
				muNoteheadRed.CropImage	( imgTemp, 0, 0, 25, 25 );
			}
		}
		catch ( Exception e )	{
			e.printStackTrace();
		}
	}

//------------------------------------------------------------------------------

	public void setupPopupMenu()
	{
		MenuItem mi ;
		popup = new PopupMenu ( "Select Note Type" );

		popup.add ( mi = new MenuItem ( "x" ) )  ;
		mi.addActionListener (this) ;	

		popup.add ( mi = new MenuItem ( "#" ) );
		mi.addActionListener (this) ;
	
		popup.add ( mi = new MenuItem ( "None" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "b" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "bb" ) );
		mi.addActionListener (this) ;
	}

//------------------------------------------------------------------------------

	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("mouseClicked");
   	}

//------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) 
	{
		//System.out.println("mousePressed");

		savecursorrow = cursorrow;
		savecursorcol = cursorcol;

		int xcoord, ycoord;
		xcoord = e.getX();
		ycoord = e.getY();

		if ( (xcoord >= xLeft) && (xcoord <= xRight) 
			&& (ycoord >= yTop) && (ycoord <= yBottom) )
		{
			popup.show ( e.getComponent(), xcoord - MENU_HOR_OFFSET, ycoord - MENU_VER_OFFSET );
			saveX = xcoord ;
			saveY = ycoord ;
		}
		if (!examMode && (muPiano.pointIsInsidePiano(xcoord,ycoord)))
		{
			int userpianokbdmidinote = muPiano.mousePressedInsidePiano(xcoord,ycoord);
			if (userpianokbdmidinote >= 0)
			{
				if (!suppressingMIDI)
				{
					midiout.createSequence();
					midiout.addProgChg    ( 0,  userImnt  );	
					midiout.addNoteOn     ( 0,   userpianokbdmidinote);	
					midiout.addNoteOff    ( 94,  userpianokbdmidinote );
					midiout.addEndOfTrack ( 96 );
					midiout.playSequence  ( 120 );
				}
			}
		}
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseReleased(MouseEvent e) 
	{
		//System.out.println("mouseReleased");
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseEntered(MouseEvent e) 
	{	
		//System.out.println("mouseEntered");

		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        	setCursor(cursor);
    	}

//------------------------------------------------------------------------------

    	public void mouseExited(MouseEvent e) 
	{
		//System.out.println("mouseExited");
	
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        	setCursor(cursor);
    	}

//------------------------------------------------------------------------------

    	public void mouseDragged(MouseEvent e) 
	{
		//System.out.println("mouseDragged");
	}

//------------------------------------------------------------------------------

	public void mouseMoved(MouseEvent e)
	{
		//System.out.println("mouseMoved");

		int xcoord, ycoord;
		xcoord = e.getX();
		ycoord = e.getY();
		cursorX = xcoord;
		cursorY = ycoord;

		if ( (xcoord >= xLeft) && (xcoord <= xRight) 
			&& (ycoord >= yTop) && (ycoord <= yBottom) ) 
		{
			//Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
     			//setCursor(cursor);

			savecursorx = xcoord;
			savecursory = ycoord;

			//Er102703-chgd 1-11 to 1-13

			// This loop should be replaced with a mathematical function to derive row/col from x,y
			
			if ( (xcoord >= (staffX + staffNoteXPos[1] ) ) 
			&& (xcoord < (staffX + staffNoteXPos[15] ) )
			)
			{
				int width = staffNoteXPos[3] - staffNoteXPos[1] ;
				cursorcol =(int) ((xcoord - (staffX + staffNoteXPos[1] )) / width) * 2 + 2 ;
			} 

			if  ( (ycoord >= (staffY - 20 + staffNoteYPos[0] + 8 ) )
			&& (ycoord < (staffY - 20 + staffNoteYPos[16] + 8  ) )
			)
			{
				int height = staffNoteYPos[1] - staffNoteYPos[0] ;
				cursorrow = (int) ((ycoord - (staffY + staffNoteYPos[0]-20+8 )) / height) ;
			}

			if ( ycoord >= (staffY - 20 + staffNoteYPos[16] + 8 ) )
			{
				cursorrow = 16;
			}

			repaint();

			//OLD CODE FOR CURSOR POSITIONING:
			//
			//for (int ix = 1 ; ix <=13 ; ix+=2)
			//{
			//    for (int iy = 0 ; iy <= 15; iy++)
			//    {
			//
			//	if ( (xcoord >= (staffX + staffNoteXPos[ix] ) ) 
			//		&& (xcoord < (staffX + staffNoteXPos[ix+2] ) ) 
			//		&& (ycoord >= (staffY - 20 + staffNoteYPos[iy] + 8 ) )
			//		&& (ycoord < (staffY - 20 + staffNoteYPos[iy+1] + 8  ) )
			//	   )
			//	{
			//		cursorrow = iy;
			//		cursorcol = ix+1;
			//		repaint();
			//	}//if
			//
			//	if ( (xcoord >= (staffX + staffNoteXPos[ix] ) ) 
			//		&& (xcoord < (staffX + staffNoteXPos[ix+2] ) ) 
			//		&& (ycoord >= (staffY - 20 + staffNoteYPos[16] + 8 ) )
			//	   )
			//	{
			//		cursorrow = 16;
			//		cursorcol = ix+1;
			//		repaint();
			//	}//if
			//
			//    }//for iy
			//}//for ix

			cursorvisible = 1;

		} // if x,y in active region
		else
		{
			//Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        		//setCursor(cursor);

			//make cursor disappear when mouse exits range
			cursorvisible = 0;
			repaint();
		}
	}

//------------------------------------------------------------------------------

	public void actionPerformed (ActionEvent e)
	{
		char ac = '?';
		int uaccval=0;

		//System.out.println ("Menu Item: " + e.getActionCommand() );
		//System.out.println ("Clicked at: " + saveX + " , " + saveY);

		if (e.getActionCommand().equals ( "None" ) )
		{
			ac = ' ';
		}
		//else if (e.getActionCommand().equals ( "Natural" ) )
		//{
		//	ac = 'n';
		//}
		else if (e.getActionCommand().equals ( "#" ) )
		{
			ac = 's';
		}
		else if (e.getActionCommand().equals ( "b" ) )
		{
			ac = 'f';
		}
		else if (e.getActionCommand().equals ( "x" ) )
		{
			ac = 'S';
		}
		else if (e.getActionCommand().equals ( "bb" ) )
		{
			ac = 'F';
		}

		if ((ac != '?') && (isNoteheadOnStaff ( cursorcol, cursorrow ) == 0))
		{
			eraseAllNoteheadsInColumn (savecursorcol);
			addNoteheadToStaff (savecursorcol, savecursorrow, ac);
			// check if user entered something in all note columns. 
			// If yes, enable the Check button

			if ( (getNoteRowFromColumn ( 0 ) > -1)
			&& (getNoteRowFromColumn ( 2 ) > -1)
			&& (getNoteRowFromColumn ( 4 ) > -1)
			&& (getNoteRowFromColumn ( 6 ) > -1)
			&& (getNoteRowFromColumn ( 8 ) > -1)
			&& (getNoteRowFromColumn ( 10 ) > -1)
			&& (getNoteRowFromColumn ( 12 ) > -1)
			&& (getNoteRowFromColumn ( 14 ) > -1)

			)
			{
				checkButton.setEnabled(true);
			}
			repaint();
		}

		// trying to fix problem where adding accid and cursor in menu below staff
		if ((ac != '?') && (isNoteheadOnStaff ( cursorcol, cursorrow ) != 0))
		{
			eraseAllNoteheadsInColumn (savecursorcol);
			addNoteheadToStaff (savecursorcol, savecursorrow, ac);
			// check if user entered something in all note columns. 
			// If yes, enable the Check button

			if ( (getNoteRowFromColumn ( 0 ) > -1)
			&& (getNoteRowFromColumn ( 2 ) > -1)
			&& (getNoteRowFromColumn ( 4 ) > -1)
			&& (getNoteRowFromColumn ( 6 ) > -1)
			&& (getNoteRowFromColumn ( 8 ) > -1)
			&& (getNoteRowFromColumn ( 10 ) > -1)
			&& (getNoteRowFromColumn ( 12 ) > -1)
			&& (getNoteRowFromColumn ( 14 ) > -1)

			)
			{
				checkButton.setEnabled(true);
			}
			repaint();


		}

		if (e.getActionCommand().equals ( "CHECK" ) )
		{
			int Corrmidi = 0;
			int correctline = 0;
			int numcorrect = 1;


			//System.out.println("Check button pressed");
			
			Udata[0]=getNoteRowFromColumn ( 0 );
			Udata[1]=getNoteRowFromColumn ( 2 );
			Udata[2]=getNoteRowFromColumn ( 4 );
			Udata[3]=getNoteRowFromColumn ( 6 );
			Udata[4]=getNoteRowFromColumn ( 8 );
			Udata[5]=getNoteRowFromColumn ( 10 );
			Udata[6]=getNoteRowFromColumn ( 12 );
			Udata[7]=getNoteRowFromColumn ( 14 );


			Uacc[0]=getNoteAccidFromColumn ( 0 );
			Uacc[1]=getNoteAccidFromColumn ( 2 );
			Uacc[2]=getNoteAccidFromColumn ( 4 );
			Uacc[3]=getNoteAccidFromColumn ( 6 );
			Uacc[4]=getNoteAccidFromColumn ( 8 );
			Uacc[5]=getNoteAccidFromColumn ( 10 );
			Uacc[6]=getNoteAccidFromColumn ( 12 );
			Uacc[7]=getNoteAccidFromColumn ( 14 );

			
			//System.out.println("Udata[0]="+Udata[0]);

			if (Udata[0] != -1)
			{
				if (currentClef == 1) // treble
				{
					Umidi = MidiTreble [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("treb U midi" + Umidi ); 
				}
				else //bass
				{
					Umidi = MidiBass [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("bass U midi" + Umidi );
				}
				Corrmidi = Umidi;
			}

			for (int i=1; i<=7; i++)
			{
				//System.out.println("Udata["+i+"]="+Udata[i]+" acc="+Uacc[i]);

				correctline = 0;
				//er111703- use upDown for testing if note is on correct line
				if (Udata[i] == (Udata[0] - ( i * upDown) ))
				{
					//System.out.println ("correct line");
					correctline = 1;
				}
				else
				{
					//System.out.println ("wrong line");
					correctline = 0;
				}
				currentClef = userTestClefs[currques];
				
				//System.out.println ("Current Clef: "+currentClef);
				//System.out.println ("AccVal: "+Uacc[i]);

				if (Udata[i] != -1)
				{
					if (currentClef == 1) // treble
					{
						Umidi = MidiTreble [ Udata[i] ]  + Uacc[i] ;
						//System.out.println("i="+i+" treb U midi" + Umidi );
					}
					else //bass
					{
						Umidi = MidiBass [ Udata[i] ]  + Uacc[i] ;
						//System.out.println("i="+i+" bass U midi" + Umidi );
					}
					
					//er111703- use upDown for scale direction testing 
					Corrmidi += ScaleBeingTested[i-1] * upDown;

					if ((Umidi == Corrmidi) && (correctline == 1))
					{
						//System.out.println("correct midi");
						setNoteheadColor ( i*2, Udata[i], 0 );
						numcorrect ++ ;
					}
					else
					{
						//System.out.println("incorrect midi");
						setNoteheadColor ( i*2, Udata[i], 1 );

					}
				}
			}//for

			gotThisQuesCorrect=0;

			if ( numtimeschecked == 0)	{
				numquespresented++;
			}

			if (numcorrect == 8)	{
				if ( numtimeschecked == 0)	{

					numquescorrect1sttime++;
					gotThisQuesCorrect = 1;

					//System.out.println ("Got this ques correct.");
				}	

				strFeedback = strCorrect;			
				nextButton.setEnabled(true);
	
				//need to prevent user from hitting Check multiple times after answer correct
				// and also: from changing a correct answer to a wrong and back to the correct answer
				// 		and checking again to falsely score points

				checkButton.setEnabled(false);
				numtimeschecked++;
			}
			else
			{
				//System.out.println ("Incorrect.  numcorrect="+numcorrect);

				if ( examMode )	{
					strFeedback = strIncorrectExam;
					checkButton.setEnabled(false);
					nextButton.setEnabled(true);
				}
				else	{
					strFeedback = strIncorrect;
				}
				numtimeschecked++;

				//System.out.println ("Num Times Checked:"+numtimeschecked);
				if (numtimeschecked >= 2)	{
					if ( !examMode )		{
						showcorrButton.setEnabled(true);
					}
					nextButton.setEnabled(true);
				}
			}

			//System.out.println("gotThisQuesCorrect = "+gotThisQuesCorrect);
			//System.out.println ("Num corr 1st time: "+numquescorrect1sttime);
			//System.out.println ("Num ques presented "+ numquespresented);

			if (examMode)	{

				if (  ((numquespresented == num_reqd_to_pass) && (numquescorrect1sttime == num_reqd_to_pass))
				   || ((numquespresented == num_exam_ques) && (numquescorrect1sttime == num_reqd_to_pass))
				   )
				{
					strExamFeedback1="Congratulations!";
					strExamFeedback2="You passed this exam and may move on to the next lesson.";
					exitButton.setVisible(false);
					nextLessonButton.setVisible(true);
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);


					if (nextURL != null)
					{	
						this.getAppletContext().showDocument(nextURL);
					}
					// End of 05/01/2004 change.		

				}
				
				if (  ((numquespresented == num_exam_ques) && (numquescorrect1sttime <  num_reqd_to_pass))
				   || (	(numquespresented < num_exam_ques) 
						&& (numquescorrect1sttime <  (numquespresented - (num_exam_ques - num_reqd_to_pass)))
					)
				   )
				{
					if ((num_exam_ques - num_reqd_to_pass) == 1)	{
						strExamFeedback1="You missed more than 1 question and will need to retake";
					}
					else	{
						strExamFeedback1="You missed more than "
							+ (num_exam_ques - num_reqd_to_pass)
							+ " questions and will need to retake";
					}

					strExamFeedback2="this exam before you may move on to the next lesson. ";
				
					exitButton.setVisible(true);
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);
	
					if (drillURL != null)
					{	
						this.getAppletContext().showDocument(drillURL);
					}
					//END of 05/01/2004 change

				}

			} // examMode

			repaint();

		}//check


		if (e.getActionCommand().equals ( "NEXT" ) )
		{
			int qix;
			
			//System.out.println("Next button pressed");
			prevques=userTestQuestions[currques];

			if (gotThisQuesCorrect == 0)
			{	
				//missedQuestions[missct] = userTestQuestions[currques];
				//missedClefs[missct] = userTestClefs[currques];
				//missct++;			

				//System.out.println ("missed"
				//+ missct
				//+ " ques= "
				//+ missedQuestions[missct-1] 
				//+ " clef= "
				//+ missedClefs[missct-1] );
			}

			currques++;

			// ver0.08 - added following to prevent array out of bounds
			if (currques >= 30)
			{	currques = 0;
			}

			numtimeschecked = 0;

			userTestQuesTail ++;
			if (userTestQuesTail >= 30)
			{
				userTestQuesTail = 0;
			}

			//System.out.println("currques="+currques);
			//System.out.println("missct="+missct);
			
			
			if ( userTestQuesTail == userTestQuesHead)
			{
				//System.out.println ("DONE WITH 1ST QUES SET: TAIL=HEAD");
				// go thru missed ques, then generate 15 new ones

				//assertion: should never have first Missed ques same as prev ques 
				//unless only 1 missed and it was the last question 

				// ver0.08 - removed re-presentation of missed questions
				//qix= currques;
				//for (int m=0 ; m < missct ; m++)
				//{
				//	qix++;
				//	if (qix >= 30)
				//	{
				//		qix = 0;
				//	}
				//	userTestQuesInUse[qix] = 1;
				//	userTestQuestions[qix] = missedQuestions[m] ;
				//	userTestClefs[qix] = missedClefs[m] ;
				//	userTestQuesHead++;
				//	if (userTestQuesHead >= 30)
				//	{
				//		userTestQuesHead = 0;
				//	}
				//
				//}
				//missct=0;
				
				generate15questions(userTestQuesHead);
			}
			
			for (int c=0 ; c< 16 ; c++)
			{
				eraseAllNoteheadsInColumn(c);
			}
			setCurrentQuestion (currques);

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			if ( !examMode )
			{
				showcorrButton.setEnabled(false);
			}
			strFeedback = "";

			repaint();
		}

		if (e.getActionCommand().equals ( "PLAY MINE" ) )
		{
			int miditime = 0;

			//System.out.println("Play Mine button pressed");
			Udata[0]=getNoteRowFromColumn ( 0 );
			Udata[1]=getNoteRowFromColumn ( 2 );
			Udata[2]=getNoteRowFromColumn ( 4 );
			Udata[3]=getNoteRowFromColumn ( 6 );
			Udata[4]=getNoteRowFromColumn ( 8 );
			Udata[5]=getNoteRowFromColumn ( 10 );
			Udata[6]=getNoteRowFromColumn ( 12 );
			Udata[7]=getNoteRowFromColumn ( 14 );


			Uacc[0]=getNoteAccidFromColumn ( 0 );
			Uacc[1]=getNoteAccidFromColumn ( 2 );
			Uacc[2]=getNoteAccidFromColumn ( 4 );
			Uacc[3]=getNoteAccidFromColumn ( 6 );
			Uacc[4]=getNoteAccidFromColumn ( 8 );
			Uacc[5]=getNoteAccidFromColumn ( 10 );
			Uacc[6]=getNoteAccidFromColumn ( 12 );
			Uacc[7]=getNoteAccidFromColumn ( 14 );
			
			//System.out.println("Udata[0]="+Udata[0]);

			if (!suppressingMIDI)
			{
				midiout.syncPianoKeyboard = true;
			}

			if (Udata[0] != -1)
			{
				if (currentClef == 1) // treble
				{
					Umidi = MidiTreble [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("treb U midi" + Umidi ); 
				}
				else //bass
				{
					Umidi = MidiBass [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("bass U midi" + Umidi );
				}

				if (!suppressingMIDI)
				{
					midiout.createSequence();
		
					midiout.addProgChg    ( 0,  userImnt  );
			
					midiout.addNoteOn     ( 0,   Umidi);	
					midiout.addNoteOff    ( 22,  Umidi );	
				}
				miditime= 24;
			}

			for (int i=1; i<=7; i++)
			{
				//System.out.println("Udata["+i+"]="+Udata[i]+" acc="+Uacc[i]);

				currentClef = userTestClefs[currques];
				
				//System.out.println ("Current Clef: "+currentClef);
				//System.out.println ("AccVal: "+Uacc[i]);

				if (Udata[i] != -1)
				{
					if (currentClef == 1) // treble
						{
						Umidi = MidiTreble [ Udata[i] ]  + Uacc[i] ;
						//System.out.println("i="+i+" treb U midi" + Umidi );
					}
					else //bass
					{
						Umidi = MidiBass [ Udata[i] ]  + Uacc[i] ;
						//System.out.println("i="+i+" bass U midi"+ Umidi );
					}

					if (!suppressingMIDI)
					{
						midiout.addNoteOn     ( miditime,   Umidi);	
						midiout.addPianoDisplayMetaEvent(miditime, Umidi, 1);
						midiout.addNoteOff    ( miditime+22,  Umidi );
						midiout.addPianoDisplayMetaEvent(miditime+22, Umidi, 0);
						miditime += 24;
					}
				}
				else
				{
					miditime += 24;
				}

			}//for

			if (!suppressingMIDI)
			{
				midiout.syncPianoKeyboard = false;

				midiout.addEndOfTrack ( miditime );
				midiout.playSequence  ( 120 );
			}
		} //playmine

		if (e.getActionCommand().equals ( "PLAY CORRECT" ) )
		{
			int Urow, Uacc, miditime = 0;
			int Corrmidi = 0;

			//System.out.println("Play Correct button pressed");

			Urow=getNoteRowFromColumn ( 0 );
			Uacc=getNoteAccidFromColumn ( 0 );

			//System.out.println("Urow="+Urow);
			//System.out.println("Uacc="+Urow);

			if (!suppressingMIDI)
			{
				midiout.syncPianoKeyboard = true;
			}

			if (Urow != -1)
			{
				if (currentClef == 1) // treble
				{
					Umidi = MidiTreble [ Urow ]  + Uacc ;
					//System.out.println("treb U midi" + Umidi ); 
				}
				else //bass
				{
					Umidi = MidiBass [ Urow ]   + Uacc ;
					//System.out.println("bass U midi" + Umidi );
				}

				//System.out.println("Umidi="+Umidi);


				Corrmidi = Umidi;

				if (!suppressingMIDI)
				{
					midiout.createSequence();
					midiout.addProgChg    ( 0,  userImnt );
					midiout.addNoteOn     ( 0,   Umidi);	
					midiout.addNoteOff    ( 22,  Umidi );
				}
				miditime= 24;

				for (int i=0; i<=6; i++)
				{
					Corrmidi += ScaleBeingTested[i] * upDown;

					if (!suppressingMIDI)
					{
						midiout.addNoteOn     ( miditime,   Corrmidi);	
						midiout.addNoteOff    ( miditime+22,  Corrmidi );
					}

					miditime += 24;
				}

				if (!suppressingMIDI)
				{
					midiout.syncPianoKeyboard = false;

					midiout.addEndOfTrack ( miditime );
					midiout.playSequence  ( 120 );
				}
			}
		}

		if (e.getActionCommand().equals ( "SHOW CORRECT" ) )
		{
			int Urow, Uacc, curaccid;
			int Corrmidi = 0;

			//System.out.println("Show Correct button pressed");

			eraseAllNoteheadsInColumn (2);
			eraseAllNoteheadsInColumn (4);
			eraseAllNoteheadsInColumn (6);
			eraseAllNoteheadsInColumn (8);
			eraseAllNoteheadsInColumn (10);
			eraseAllNoteheadsInColumn (12);
			eraseAllNoteheadsInColumn (14);

			Urow=getNoteRowFromColumn ( 0 );
			Uacc=getNoteAccidFromColumn ( 0 );

			if (Urow != -1)
			{
				if (currentClef == 1) // treble
				{
					Umidi = MidiTreble [ Urow ]  + Uacc ;
					//System.out.println("treb U midi" + Umidi ); 
				}
				else //bass
				{
					Umidi = MidiBass [ Urow ]  + Uacc ;
					//System.out.println("bass U midi" + Umidi );
				}
				Corrmidi = Umidi;

				//System.out.println ("Corrmidi="+Corrmidi);

				for (int i=0; i<=6; i++)
				{
					if (upDown == +1)
					{
						Urow -- ;
					}
					else
					{
						Urow ++ ;
					}

					Corrmidi += ScaleBeingTested[i] * upDown ;

					//System.out.println ("Corrmidi="+Corrmidi);					

					if (currentClef == 1) // treble
					{
						curaccid = Corrmidi - MidiTreble[Urow];

					}
					else
					{
						curaccid = Corrmidi - MidiBass[Urow];
					}
					// derive ac

					if (curaccid == 0)
					{	ac = ' ';
					} else if (curaccid == 1)
					{	ac = 's';
					} else if (curaccid == -1)
					{	ac = 'f';
					} else if (curaccid == 2)
					{	ac = 'S';
					} else if (curaccid == -2)
					{	ac = 'F';
					}

					addNoteheadToStaff ( (i+1)*2, Urow, ac);


				} //for

			} // if urow not -1

					
			checkButton.setEnabled(false);

			strFeedback = "The correct notes are being displayed.";
			repaint();

		} //show correct




		if (e.getActionCommand().equals ( "TAKE EXAM" ) )
		{
			//05/02/2004: added for debugging:
			System.out.println("Take Exam button pressed");
			System.out.println("nextURL="+nextURL);
			System.out.println("appletcontext="+this.getAppletContext());

			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}

		if (e.getActionCommand().equals ( "NEXT LESSON" ) )
		{
			//System.out.println("Next Lesson button pressed");

			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}

		if (e.getActionCommand().equals ( "EXIT" ) )
		{
			// ver0.08 - added test for either exit from Exam or Drill

			//System.out.println("Exit button pressed");
			if (examMode)
			{
				if (drillURL != null)
				{	
					this.getAppletContext().showDocument(drillURL);
				}
			}
			else
			{
				if (drillExitURL != null)
				{	
					this.getAppletContext().showDocument(drillExitURL);
				}
			}
		}


	} // ActionPerformed

//------------------------------------------------------------------------------
//IMPLEMENTATION OF ItemListener Interface:
//------------------------------------------------------------------------------

	public void itemStateChanged (ItemEvent ie)
	{
		userImnt 	= imntCombo.getSelectedIndex();
	}

//------------------------------------------------------------------------------

	public void addNoteheadToStaff ( int col, int row, char accid )
	{
		// Find first free slot for symbol

		int i = 0;
		int slot = -1;
		while (i < numNoteheads)
		{
			if (noteheadInUse[i] == 0)
			{	slot = i;
			}
			i++;
		}	
		if (slot == -1)
		{	slot = numNoteheads;
			numNoteheads++;
		}
		noteheadInUse[slot] = 1;	
		noteheadCol[slot] = col;
		noteheadRow[slot] = row;	
		noteheadAccid[slot] = accid;
		noteheadColor[slot] = 0; // black
	}

//------------------------------------------------------------------------------

	public void removeNoteheadFromStaff ( int col, int row )
	{	
		int i = 0;
		while (i < numNoteheads)
		{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )
			{	
				noteheadInUse[i] = 0;
				noteheadRow[i]=-1;	
				noteheadCol[i]=-1;	
				noteheadAccid[i]=' ';
				noteheadColor[i]=-1;
			}
			i++;
		}	
	}

//------------------------------------------------------------------------------

	public void setNoteheadColor ( int col, int row, int colr )
	{	
		int i = 0;

		//System.out.println ("in setNoteheadColor: col="+col+" row="+row+" colr="+colr);

		while (i < numNoteheads)
		{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )
			{	
				noteheadColor[i] = colr;
				//System.out.println ("updated array");
			}
			i++;
		}	
	}

//------------------------------------------------------------------------------

	public void eraseAllNoteheadsInColumn ( int col )
	{	
		int i = 0;
		while (i < numNoteheads)
		{
			if ( noteheadCol[i] == col )
			{	
				noteheadInUse[i] = 0;
				noteheadRow[i]=-1;	
				noteheadCol[i]=-1;	
				noteheadAccid[i]=' ';
				noteheadColor[i]=-1;

			}
			i++;
		}	
	}

//------------------------------------------------------------------------------

	public int getNoteRowFromColumn ( int col )
	{	
		int row  = -1;
		int i = 0;
		while (i < numNoteheads)
		{
			if ( noteheadCol[i] == col )
			{	
				row = noteheadRow[i];
			}
			i++;
		}	
		
		return row;
	}

//------------------------------------------------------------------------------

	public int getNoteAccidFromColumn ( int col )
	{	
		char ac='x';
		int uaccval = 0;
		int i = 0;

		while (i < numNoteheads)
		{
			if ( noteheadCol[i] == col )
			{	
				ac = noteheadAccid[i];
			}
			i++;
		}	

		switch (ac)
		{
			case 's':
			{
				uaccval=+1;
				break;
			}
			case 'f':
			{
				uaccval=-1;
				break;
			}
			case 'S':
			{
				uaccval=+2;
				break;
			}
			case 'F':
			{
				uaccval=-2;
				break;
			}
			default:
			{
				uaccval = 0;
				break;
			}
		} //switch 

		return uaccval;
	}

//------------------------------------------------------------------------------

	public int isNoteheadOnStaff ( int col, int row )
	{	
		int i = 0, rc = 0;
		while (i < numNoteheads)
		{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )
			{	
				rc=1;
			}
			i++;
		}	
		return rc;
	}

//------------------------------------------------------------------------------

	public void generate15questions (int startat)
	{
		int keynum, questionNum;
		int inuse[];
		int q[];
		int clef[];
		int mintype[];

		int j, ct;
		int qtemp;

		inuse = new int[15];
		q = new int[15];
		clef = new int[15];
		mintype = new int[15];
	
		//System.out.println ("generate15questions: startat="+startat);

		for (int i=0; i<=14; i++)
		{   
			inuse[i] = 0;
			q[i] = 0;
		}

		for (keynum=-7; keynum <= 7; keynum++)	// assign each key to a random question #
		{
			questionNum = (int)((Math.random() * 15));
	    		//System.out.println("keynum="+keynum+" questionNum="+questionNum);
		    	while (inuse[questionNum] > 0)	// skip any questions already populated - find a slot
	    		{
				questionNum++;
				if (questionNum >= 15)
				{
				    questionNum = 0;
				}
			} //while
		    
			inuse[questionNum] = 1;	
			q[questionNum] = keynum;
			clef[questionNum]= trebleorbass;
			mintype[questionNum] = minorType;

			// ver0.08 - Rotate among treble/bass and Minor Type values
			trebleorbass *= -1;

			minorType++;
			if (minorType > 2)	{
				minorType = 0;
			}


		    	//System.out.println("Assigned keynum" + keynum +" to question: "
			//	+questionNum+" Trebleorbass="+trebleorbass);
		}	//for


		// if q[0]=prevques,swap q[0] and q[14]
		if ( q[0]==prevques )
		{
			qtemp=q[0];
			q[0]=q[14];
			q[14]=qtemp;
		}

		j=startat;
		ct=0;
		while (ct<15)
		{
			userTestQuesInUse[j] = inuse[ct];	
			userTestQuestions[j] = q[ct];
			userTestClefs[j]= clef[ct];
			userTestMinorType[j] = mintype[ct];
			j++;
			if (j >= 30)
			{
				j=0;
			}
			ct++;
		} //while
		userTestQuesHead=j;
		//missct = 0;

		// Debugging:
		//for (int x=0 ; x<=29 ; x++)
		//{	System.out.println ("Ques:"+x+" Key:"+userTestQuestions[x]+" Clef:"+userTestClefs[x]);
		//}
	}

//------------------------------------------------------------------------------

	public void setCurrentQuestion( int q )
	{
		int startnote = 0;
		char ac = ' ';
		int currentkey;
		int currMinorType;

		if (mixedMinorAscMode == 1)
		{
			// ver0.08 - logic to more evenly distribute minor types across questions

			currMinorType = userTestMinorType[q];

			if ( currMinorType == 0)
			{
				for (int i=0 ; i<=6 ; i++)
				{
					ScaleBeingTested[i] = NaturalMinorAsc[i];
				}
				dispScaleType="diatonic ascending Natural Minor";
			}
			else if ( currMinorType == 1)
			{
				for (int i=0 ; i<=6 ; i++)
				{
					ScaleBeingTested[i] = HarmonicMinorAsc[i];
				}
				dispScaleType="diatonic ascending Harmonic Minor";
			}
			else if ( currMinorType == 2)
			{
				for (int i=0 ; i<=6 ; i++)
				{
					ScaleBeingTested[i] = MelodicMinorAsc[i];
				}
				dispScaleType="diatonic ascending Melodic Minor";
			}
		
		} //mixedMinorAscMode

		currentkey = userTestQuestions[q];
		//System.out.println ("Current Key: "+currentkey);

		currentClef = userTestClefs[q];
		//System.out.println ("Current Clef: "+currentClef);

		// ver0.08 - added logic to select appropriate cyc5 array for major/minor

		if (currentClef == 1)		{	// treble
			if (currentkey >= 0)	{ 	// sharp key OR c major
				if (upDown == +1)	{
					if (majorOrMinor == +1)	{
						startnote = cyc5shTrebMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5shTrebMin[Math.abs(currentkey)];
					}
				}
				else	{
					if (majorOrMinor == +1)	{
						startnote = cyc5shTrebDescMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5shTrebDescMin[Math.abs(currentkey)];
					}
				}
				if (majorOrMinor == +1)	{
					ac = cyc5shAccMaj[Math.abs(currentkey)];
				}
				else if (majorOrMinor == -1)	{
					ac = cyc5shAccMin[Math.abs(currentkey)];
				}
			}
			else if (currentkey < 0)	{	 // flat key
				if (upDown == +1)	{
					if (majorOrMinor == +1)	{
						startnote = cyc5flTrebMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5flTrebMin[Math.abs(currentkey)];
					}
				}
				else	{
					if (majorOrMinor == +1)	{
						startnote = cyc5flTrebDescMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5flTrebDescMin[Math.abs(currentkey)];
					}
				}
				if (majorOrMinor == +1)	{
					ac = cyc5flAccMaj[Math.abs(currentkey)];
				}
				else if (majorOrMinor == -1)	{
					ac = cyc5flAccMin[Math.abs(currentkey)];
				}
			}
		}		//treble clef
		else 	{					// bass
			if (currentkey >= 0)	{	// sharp key OR c major
				if (upDown == +1)	{
					if (majorOrMinor == +1)	{
						startnote = cyc5shBassMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5shBassMin[Math.abs(currentkey)];
					}
				}
				else	{
					if (majorOrMinor == +1)	{
						startnote = cyc5shBassDescMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5shBassDescMin[Math.abs(currentkey)];
					}
				}
				if (majorOrMinor == +1)	{
					ac = cyc5shAccMaj[Math.abs(currentkey)];
				}
				else if (majorOrMinor == -1)	{
					ac = cyc5shAccMin[Math.abs(currentkey)];
				}
			}
			else if (currentkey < 0) {	// flat key
				if (upDown == +1)	 {
					if (majorOrMinor == +1)	{
						startnote = cyc5flBassMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5flBassMin[Math.abs(currentkey)];
					}
				}
				else	{
					if (majorOrMinor == +1)	{
						startnote = cyc5flBassDescMaj[Math.abs(currentkey)];
					}
					else if (majorOrMinor == -1)	{
						startnote = cyc5flBassDescMin[Math.abs(currentkey)];
					}
				}
				if (majorOrMinor == +1)	{
					ac = cyc5flAccMaj[Math.abs(currentkey)];
				}
				else if (majorOrMinor == -1)	{
					ac = cyc5flAccMin[Math.abs(currentkey)];
				}
			}
		}		// bass clef

		startcursorrow = startnote;
		startcursorcol = 0;

		cursorrow = startnote;
		cursorcol = 0;

		addNoteheadToStaff (0, startnote, ac);
	}

//------------------------------------------------------------------------------

	public void generateRandomStartingNote()
	{
		int startnote 	= 	randint (7,16);
		int accid 		= 	randint (1,3);
		char ac 		= 	' ';

		if (accid == 1)
			ac = 'f';
		else if (accid == 2)
			ac = ' ';
		else if (accid == 3)
			ac = 's';

		startcursorrow 	= 	startnote;
		startcursorcol 	= 	0;

		cursorrow 		= 	startnote;
		cursorcol 		= 	0;

		addNoteheadToStaff (0, startnote, ac);
	}

//------------------------------------------------------------------------------

	public int randint(int low,int high) 
	{
      	return(((int)((high-low+1)*(Math.random()))) + low);
	}


//------------------------------------------------------------------------------
// GRAPHICS ROUTINES
//------------------------------------------------------------------------------

	public void update( Graphics g ) 
	{
		imageScreen = createImage (rectScreen.width, rectScreen.height);
		paint (imageScreen.getGraphics());
		g.drawImage (imageScreen, 0, 0, null);
        }
   
//------------------------------------------------------------------------------

	public void paint( Graphics g )
	{
		if (showAbendScreen == 1)
		{
			g.setColor (Color.red);
			g.fillRect(0, 0, rectScreen.width, rectScreen.height);

			g.setColor (Color.black);
			drawAbendScreen (g);
		}
		else
		{
			g.setColor (Color.white);
			g.fillRect(0, 0, rectScreen.width, rectScreen.height);

			g.setColor (Color.black);
			drawScreen (g);
		}
	}

//------------------------------------------------------------------------------

	public void drawAbendScreen ( Graphics g )
	{
		Font txtfont;
		txtfont = new Font ("Arial", Font.BOLD, 14);
		g.setFont (txtfont);

		g.drawString("THE FOLLOWING ERROR HAS OCCURRED:", 50, 150);
		g.drawString(txtAbend, 50, 200);
		
		g.drawString("Please open the Java Console for more information",50,250);								g.drawString("and contact systems support.",50,270);		
	}

//------------------------------------------------------------------------------

	public void drawScreen ( Graphics g )
	{
		Font txtfont, headerfont;

		headerfont = new Font ("Arial", Font.BOLD, 28);
		g.setFont (headerfont);


		if (examMode)	{
			centerString (g, strExamMode, headerfont, 30);
		}
		else	{
			centerString (g, strDrillMode, headerfont, 30);
		}

		txtfont = new Font ("Arial", Font.BOLD, 14);
		g.setFont (txtfont);
		
		strInstruct2 = dispScaleType;
		strInstruct = strInstruct1 + strInstruct2 + strInstruct3;

		centerString (g, strInstruct, txtfont, 60);

		//System.out.println("DrawScreen 1");

		for (int x=staffX ; x < staffX+560 ; x+=65 )	{
			g.drawImage (muStaff.getImage(), x, staffY, this );
		}

		// PIANO KEYBOARD

		Image pianoImage = muPiano.getImage();
		int pianoLeft = staffX - 1;
		int pianoTop = staffY + 250;
		int pianoRight = pianoLeft + muPiano.getWidth();
		int pianoBottom = pianoRight + muPiano.getHeight();
		muPiano.setScreenCoords (pianoLeft, pianoTop, pianoRight, pianoBottom); 
		if (!examMode)
		{
			g.drawImage (pianoImage, pianoLeft, pianoTop, this);
		}

		//FOR DEBUGGING:
		//bShowGrid = true;

		if (bShowGrid)
		{
			g.setColor (Color.red);
			g.drawLine (muPiano.xMin+muPiano.x1left, muPiano.yMid+muPiano.y1left, muPiano.xMax+muPiano.x1left, muPiano.yMid+muPiano.y1left);
			g.drawLine (muPiano.xMin+muPiano.x1left, muPiano.yMin+muPiano.y1left, muPiano.xMin+muPiano.x1left, muPiano.yMax+muPiano.y1left);

			int xTopCount = muPiano.xTop.length;
			int xBotCount = muPiano.xBot.length;
			for (int i=0 ; i < xTopCount; i++)
			{
				g.drawLine (muPiano.xTop[i]-muPiano.xcalib+muPiano.x1left+muPiano.x1left
				,muPiano.yMin+muPiano.y1left
				, muPiano.xTop[i]-muPiano.xcalib+muPiano.x1left+muPiano.x1left
				,muPiano.yMid+muPiano.y1left);
			}
			for (int i=0 ; i < xBotCount; i++)
			{
				g.drawLine (muPiano.xBot[i]-muPiano.xcalib+muPiano.x1left+muPiano.x1left
				,muPiano.yMid+muPiano.y1left
				, muPiano.xBot[i]-muPiano.xcalib+muPiano.x1left+muPiano.x1left
				,muPiano.yMax+muPiano.y1left);
			}
			g.setColor (Color.black);

			//Draw all midi dots

		}

		//DEBUGGING: SHOW ALL MIDI DOTS
		//for (int midinote = 36 ; midinote <= 84 ; midinote++)
		//{
		//	int midix = muPiano.getPianoDotX(midinote);
		//	int midiy = muPiano.getPianoDotY(midinote);
		//	int midiRadius = muPiano.getPianoDotRadius();		
		//	g.setColor (Color.red);
		//	g.fillArc (midix - midiRadius, midiy - midiRadius, 2*midiRadius, 2*midiRadius, 0, 360);		
		//}
		//g.setColor (Color.black);

		if (!examMode)
		{
			for (int midinote = 36 ; midinote <= 84 ; midinote++)
			{
				int midix = muPiano.getPianoDotX(midinote);
				int midiy = muPiano.getPianoDotY(midinote);
				int midiRadius = muPiano.getPianoDotRadius();

				if (muPiano.getPianoDot(midinote) == 1)
				{
					g.setColor (Color.red);
				}
				else
				{
					if (muPiano.getDefaultPianoKeyColor(midinote) == 1)
					{
						g.setColor (Color.white);
					}
					else
					{
						g.setColor (new Color(37,37,37));
					}
				}
				g.fillArc (midix - midiRadius, midiy - midiRadius, 2*midiRadius, 2*midiRadius, 0, 360);		
			}
		}

		g.setColor (Color.black);
		

		//System.out.println("DrawScreen 2");

		g.drawImage (muBar.getImage(), staffX - 1, staffY, this);
		g.drawImage (muRtDblBar.getImage(), staffX+567, staffY, this);


		if (currentClef == 1)	{
			g.drawImage (muTrebleClef.getImage()
				, staffX+gClefXOffset
				, staffY+gClefYOffset
				, this );
		}
		else	{
			g.drawImage (muBassClef.getImage()
				, staffX+fClefXOffset
				, staffY+fClefYOffset
				, this );
		}

		//System.out.println("DrawScreen 3");

		// Sharps in key signature for treble clef
		//for (int i=0 ; i<=6 ; i++)
		//{
		//	g.drawImage (muSharp.getImage()
		//			, staffX + keySharpXOffset[i]
		//			, staffY-20+keySharpYOffset[i]
		//			, this);
		//}

		// Sharps in key signature for bass clef
		//for (int i=0 ; i<=6 ; i++)
		//{
		//	g.drawImage (muSharp.getImage()
		//			, staffX + keySharpXOffset[i]+5
		//			, staffY-20+keySharpYOffset[i]+16
		//			, this);
		//}

		// Flats in key signature for treble clef
		//for (int i=0 ; i<=6 ; i++)
		//{
		//	g.drawImage (muFlat.getImage()
		//			, staffX + keyFlatXOffset[i]
		//			, staffY-20+keyFlatYOffset[i]
		//			, this);
		//}

		// Flats in key signature for bass clef
		//for (int i=0 ; i<=6 ; i++)
		//{
		//	g.drawImage (muFlat.getImage()
		//			, staffX + keyFlatXOffset[i]+5
		//			, staffY-20+keyFlatYOffset[i]+16
		//			, this);
		//}

		//For testing:
		//int y=0;
		//for (int x=0 ; x<=15; x++)
		//{
		//	drawNotehead ( g, x, y, 'F' );
		//	y++;
		//}



		//Draw all noteheads that are currently on the staff
		for (int i=0; i<numNoteheads;i++)	{
			if (noteheadInUse[i] == 1)	{
				if (noteheadColor[i] == 0)	{
					drawNotehead (g, noteheadCol[i], noteheadRow[i], 'F' );
				}
				else	{
					if ( examMode )	{
						drawNotehead (g, noteheadCol[i], noteheadRow[i], 'F' );
					}
					else	{
						drawNotehead (g, noteheadCol[i], noteheadRow[i], 'R' );
					}
				}
				drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i]);
			}
		} // for


		// Draw Dots for positions where user will enter notes
		for (int i=2; i<16 ; i+=2)
		{
			g.drawImage (muDot.getImage(), staffX + staffNoteXPos[i] + 7, staffY + staffNoteYPos[0] - 25, this );
		}


		//System.out.println("DrawScreen 4");

		//Draw notehead at cursor position
		if (cursorvisible == 1)
		{	
			drawNotehead (g, cursorcol, cursorrow, 'U' );

			//The following is an attempt to attach the notehead to the cursor
			//g.drawImage (muNoteheadUnfilled.getImage(), savecursorx-10, savecursory-20, this);

		}

		//System.out.println ("presented "+numquespresented );
		//System.out.println ("score "+numquescorrect1sttime );


		//System.out.println("DrawScreen 5");

		nScore1=numquescorrect1sttime;
		nScore2=numquespresented;

		if (nScore2 == 0)	{
			nScorePercent  = 0;
		}
		else	{
			nScorePercent  = (int)(nScore1 * 100 / nScore2);
		}

		strScoreDisp = strScore1 + nScore1 + strScore2 + nScore2 + strScore3 + nScorePercent + strScore4;

		centerString (g, strScoreDisp, txtfont, 85);
		centerString (g, strFeedback, txtfont, 280);
		centerString (g, strExamFeedback1, txtfont, 350);
		centerString (g, strExamFeedback2, txtfont, 370);
	}

//------------------------------------------------------------------------------

	public void centerString ( Graphics g, String strIn, Font fontIn, int Ycoord )
	{
		FontMetrics fm = g.getFontMetrics(fontIn);
		g.drawString ( strIn, ( (rectScreen.width/2) - (fm.stringWidth(strIn) / 2) ), Ycoord ) ;
	}

//------------------------------------------------------------------------------

	public void drawNotehead ( Graphics g, int x , int y, char nhtype )
	{
		if (nhtype == 'F')	{ 						// filled
			g.drawImage (muNoteheadFilled.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}
		else if (nhtype == 'U')	{						 // unfilled
			g.drawImage (muNoteheadUnfilled.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}
		else if (nhtype == 'R')	{						// red
			g.drawImage (muNoteheadRed.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}

		if (y <= 2)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[2]
			, this );
		}
		if (y==0)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[0]
			, this );
		}
		if (y >= 14)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[14]
			, this );
		}
		if (y==16)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[16]
			, this );
		}
	}

//------------------------------------------------------------------------------

	public void drawAccidental ( Graphics g, int x , int y, char accid )
	{

		if (accid == 's')
		{
			g.drawImage (muSharp.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 13
				, this );

		}
		else if (accid == 'f')
		{
			g.drawImage (muFlat.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
		}
		else if (accid == 'n')
		{
			g.drawImage (muNatural.getImage()
				, staffX + staffNoteXPos[x] - 18
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
		}

		else if (accid == 'S')
		{
			g.drawImage (muDblSharp.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 14
				, this );

		}
		else if (accid == 'F')
		{
			g.drawImage (muDblFlat.getImage()
				, staffX + staffNoteXPos[x] - 25
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
		}
	}

//------------------------- IMPLEMENTATION OF RUNNABLE INTERFACE ---------------

    public void start() {
        if(_looper == null) {
            _running = true;
            _looper = new Thread(this);
            _looper.start();
        }
    }
    public void stop() {
        _running = false;
    }
    public void run() {
        try {
            while(_running) {
                repaint();
                _looper.sleep(20);
            }
        } catch(InterruptedException e) {
            _running = false;
        }
    }
}
//------------------------------------------------------------------------------
// END OF CLASS UdmtModeDrill
//------------------------------------------------------------------------------

