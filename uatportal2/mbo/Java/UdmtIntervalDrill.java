//------------------------------------------------------------------------------
// UdmtIntervalDrill Applet -  Drill and Exam for Intervals //------------------------------------------------------------------------------
// REMEMBER TO INCREMENT VERSION NUMBER FOR EACH RELEASE (udmtAppletVersion)
//------------------------------------------------------------------------------
// PARAMS:
//
// intvType		=	The type of scale to present:
//				Values: 	PerfMaj1OctAsc = only perfect and major: unison thru octave, ascending
//						All1OctAsc = : all interval types, unison thru octave, ascending
//						All1OctDesc = : all interval types, unison thru octave, Descending
//						All2OctOnlyAsc = : all interval types, 9th thru 15th, ascending
//						All2OctOnlyDesc = : all interval types, 9th thru 15th, Descending
//				REQUIRED
//
// exerciseType	=     Whether the student is Identifying or Constructing intervals:
//				Values:	IDENTIFY = Identifying intervals
//						CONSTRUCT = Constructing intervals
//						INVERSIONS = Constructing inversions
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
//				OPTIONAL: DEFAULT=YES (midi will be suppressed)
//
// drillExitURL	=     The URL to invoke when the student presses the EXIT button
//                      in Drill mode.
//				REQUIRED IF examMode = DRILL
//
// debugMODE	= 	OPTIONAL: 'TRUE' to print debugging info to Java Console.
//				and to present the questions in non-random order.
//
//------------------------------------------------------------------------------
// Modification History:
//
// 05/01/2004: Ver 0.06 - changed to use UdmtURLBuilder class to build URL's for buttons,
// and new behavior to branch to "Sorry" page immediately when student fails exam.
// Started adding interface logic to UdmtExamLog.
// 07/18/2004: Ver 0.07 - changed several level numbers based on sequence of lessons
// 07/22/2004: Ver 0.08 - removed diminished unisons
// 09/06/2004: Ver 0.09 - bugfix for array index problem
// 09/14/2004: Ver 0.10 - changed default for suppressMIDI, and fixed logic for debugMode
// 01/19/2005: Ver 0.11 - changes to support exam login servlet
//------------------------------------------------------------------------------

/* TO DO:

POSSIBLE ENHANCEMENTS:

KNOWN BUGS:

QUESTIONS:

CODED - TO BE TESTED:

TESTED:
*/

//------------------------------------------------------------------------------

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtIntervalDrill extends Applet
	implements MouseListener, MouseMotionListener, ActionListener, ItemListener

//------------------------------------------------------------------------------
{
	private String udmtAppletName			=	"UdmtIntervalDrill";
	private String udmtAppletVersion		=	"0.11"	;

	private String parmIntvType 			= 	"MajorAsc"; //default = major scale ascending.

	private String dispIntvType			=	"";
	private int upDown 				= 	+1;  // +1=ascending , -1=descending

	private String parmExerciseType		=	"";
	private char exerciseMode 			=	'I';

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

	//05/01/2004:
	private UdmtURLBuilder urlBuilder;
	private UdmtExamLog examLog;
	// End of 05/01/2004 change.		



	private UdmtMusicSymbol muStaff, muTrebleClef, muBassClef, muDot, muBar, muLeger;
	private UdmtMusicSymbol muNoteheadFilled, muNoteheadUnfilled, muNoteheadRed, muRtDblBar;
	private UdmtMusicSymbol muSharp, muFlat, muNatural, muDblSharp, muDblFlat;

	private Rectangle rectScreen;
	private Image imageScreen;

	private Image imgAllSymbols;

	private int staffX, staffY;

	private int gClefXOffset 			= 	0;
	private int gClefYOffset 			= 	-20;
	private int fClefXOffset 			= 	0;
	private int fClefYOffset 			= 	-17;

	private int keySharpXOffset[] 		= 	{42,58,73,88,103,118,133};
	private int keySharpYOffset[] 		= 	{1,25,-5,18,41,6,34};
	private int keyFlatXOffset[] 			= 	{42,58,73,88,103,118,133};
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
	private char cyc5shAccMaj2Oct[] 			= 	{' ',' ',' ',' ',' ',' ',' ',' '};
	private char cyc5flAccMaj2Oct[] 			= 	{' ',' ',' ',' ',' ',' ',' ',' '};

	// Staff Y positions on treble and bass clefs for sharps and flats for starting note of each key for Ascending scale
	private int cyc5shTrebMaj[] 			=  	{14,10,13,9,12,8,11,14};
	private int cyc5shBassMaj[] 			= 	{16,12,15,11,14,10,13,9}; 
	private int cyc5flTrebMaj[] 			=	{14,11,15,12,9,13,10,14}; 
	private int cyc5flBassMaj[] 			= 	{16,13,10,14,11,15,12,9}; 

	private int cyc5shTrebDescMaj[]		=  	{7,3,6,2,5,1,4,7}; 
	private int cyc5shBassDescMaj[]		= 	{9,5,8,4,7,3,6,2}; 
	private int cyc5flTrebDescMaj[]		= 	{7,4,1,5,2,6,3,7}; 
	private int cyc5flBassDescMaj[]		= 	{9,6,3,7,4,8,5,2}; 

	private int cyc5shTrebMaj2Oct[] 			=  	{14,14,14,16,14,15,14,14};
	private int cyc5shBassMaj2Oct[] 			= 	{16,16,15,16,14,16,16,16}; 
	private int cyc5flTrebMaj2Oct[] 			=	{14,14,15,14,16,14,14,14}; 
	private int cyc5flBassMaj2Oct[] 			= 	{16,16,16,14,16,15,16,16}; 

	private int cyc5shTrebDescMaj2Oct[]		=  	{0,0,0,2,0,1,0,0}; 
	private int cyc5shBassDescMaj2Oct[]		= 	{2,2,2,2,2,2,2,2}; 
	private int cyc5flTrebDescMaj2Oct[]		= 	{0,0,1,0,2,0,0,0}; 
	private int cyc5flBassDescMaj2Oct[]		= 	{2,2,2,2,2,2,2,2}; 




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
	private int IntervalsBeingTested[];
	private int IntervalDistanceBeingTested[];
	private char IntervalQualityBeingTested[];

	private int NumItemsBeingTested;

	//----------------------------------------------------------------------

	private int userTestQuesInUse[];
	private int userTestQuestion1[];
	private int userTestQuestion2[];
	private char userTestQuality[];
	private int userTestClefs[];
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
	private Button playnotesButton, nextLessonButton, exitButton, hintButton;

	private Button perfButton, majrButton, minrButton, augButton, dimButton;
	private Button unisButton, i2ndButton, i3rdButton, i4thButton, i5thButton;
	private Button i6thButton, i7thButton, octButton, i9thButton, i10thButton;
	private Button i11thButton, i12thButton, i13thButton, i14thButton, i15thButton;


	private int currques;						// current question number being presented
	private char currentqual;

	private int numtimeschecked 			= 	0;
	private int numquespresented 			= 	0;
	private int numquescorrect1sttime 		= 	0;

	private String strDrillMode			=	"Interval Identification: Practice";
	private String strExamMode			=	"Interval Identification: Exam";

	private String strDrillModeIdent			=	"Interval Identification: Practice";
	private String strExamModeIdent			=	"Interval Identification: Exam";
	private String strDrillModeConstruct		=	"Interval Construction: Practice";
	private String strExamModeConstruction		=	"Interval Construction: Exam";

	private String strInstruct			=	"";
	private String strInstruct1			=	"Identify the ";
	private String strInstruct2 			= 	"";
	private String strInstruct3 			= 	"interval for the two notes shown below.";

	private String strScore1 			= 	"Score: ";
	private int nScore1 				= 	0;
	private String strScore2 			= 	" out of ";
	private int nScore2 				= 	0;
	private String strScore3 			= 	" = ";
	private int nScorePercent 			= 	0;
	private String strScore4 			= 	"%";
	private String strScoreDisp;

	private String strCorrect 			= 	"Correct.";
	private String strIncorrectIdent		= 	"Incorrect.  ";
	private String strIncorrectConstr		= 	"Incorrect.  Please fix note in red and try again.";

	private String strIncorrectExam 		= 	"Incorrect.";

	private String strMaster1 			= 	"Congratulations.  You have provided 9 out of 10";
	private String strMaster2 			= 	"correct answers and may  move on if you wish.";
	private String strFeedback 			= 	"";
	private String strExamFeedback1 		= 	"";
	private String strExamFeedback2 		= 	"";

	private int savecursorx, savecursory;

	private boolean examMode;

	private int prevques1 				= 	100; 	// 100 indicates first time - no prev ques
	private int prevques2 				= 	100; 	// 100 indicates first time - no prev ques

	private Choice imntCombo;
	private int userImnt				=	0 ;

	private int mixedMinorAscMode 		=	0;

	// DEFAULT VALUES WHICH MAY BE OVERRIDEN BY PARAMS:
	private String parmExamNumQues;
	private int num_exam_ques			= 	10;
	private String parmExamNumReqd;
	private int num_reqd_to_pass 			= 	9;
	private String parmSuppressMIDI		=	"YES";	// changed 09-14-2004 to prevent browser problems with midi

	private boolean suppressingMIDI 		= 	true;	// changed 09-14-2004

	private int majorOrMinor			= 	0;
	// Determines whether Major or Minor scales are being tested.  1=Major, -1=Minor.

	private int trebleorbass = 1;
	// alternates between 1 and -1 as questions generated and assigned to random slots
	// This ensures the pattern of clefs doesn't repeat until after 30 questions.

	private int[] PerfMaj1OctAsc 			= {0,2,4,5,7,9,11,12};
	private int[] PerfMaj1OctAscDistance 	= {0,1,2,3,4,5, 6, 7};
	private char[] PerfMaj1OctAscQuality	 = {'P','M','M','P','P','M','M','P'};

	private int[] All1OctAsc			= {	0,1,2,1,3,0,4,3,5
									,2,5,6,4,7,8,6,9,8,10
									,7,11,10,12,9,12,13,11};

	private int[] All1OctDistanceAsc 		= {	0,0,1,1,1,1,2,2,2
									,2,3,3,3,4,4,4,5,5,5
									,5,6,6,6,6,7,7,7};

	private char[] All1OctQualityAsc		= {	'P','A','M','m','A','d','M','m','A'
									,'d','P','A','d','P','A','d','M','m','A'
									,'d','M','m','A','d','P','A','d'};

	private int[] All1OctDesc			= {	-1,-2,-1,-3,0,-4,-3,-5,-2
									,-5,-6,-4,-7,-8,-6,-9,-8,-10,-7
									,-11,-10,-12,-9,-12,-13,-11};

	private int[] All1OctDistanceDesc 		= {	0,-1,-1,-1,-1,-2,-2,-2,-2
									,-3,-3,-3,-4,-4,-4,-5,-5,-5,-5
									,-6,-6,-6,-6,-7,-7,-7};

	private char[] All1OctQualityDesc		= {	'A','M','m','A','d','M','m','A','d'
									,'P','A','d','P','A','d','M','m','A','d'
									,'M','m','A','d','P','A','d'};


	private int[] All2OctOnlyAsc			= {	14,13,15,12,16,15,17,14,17,18
									,16,19,20,18,21,20,22,19,23,22
									,24,21,24,25,23};

	private int[] All2OctOnlyDistanceAsc 	= {	8,8,8,8,9,9,9,9,10,10
									,10,11,11,11,12,12,12,12,13,13
									,13,13,14,14,14};

	private char[] All2OctOnlyQualityAsc	= {	'M','m','A','d','M','m','A','d','P','A'
									,'d','P','A','d','M','m','A','d','M','m'
									,'A','d','P','A','d'};

	private int[] All2OctOnlyDesc			= {	-14,-13,-15,-12,-16,-15,-17,-14,-17,-18
									,-16,-19,-20,-18,-21,-20,-22,-19,-23,-22
									,-24,-21,-24,-25,-23};

	private int[] All2OctOnlyDistanceDesc 	= {	-8,-8,-8,-8,-9,-9,-9,-9,-10,-10
									,-10,-11,-11,-11,-12,-12,-12,-12,-13,-13
									,-13,-13,-14,-14,-14};

	private char[] All2OctOnlyQualityDesc	= {	'M','m','A','d','M','m','A','d','P','A'
									,'d','P','A','d','M','m','A','d','M','m'
									,'A','d','P','A','d'};

	private int[] Inv1OctAsc			= {	2,1,3,0,4,3,5,2,5,6
									,4,7,8,6,9,8,10,7,11,10
									,12};

	private int[] Inv1OctDistanceAsc 		= {	1,1,1,1,2,2,2,2,3,3
									,3,4,4,4,5,5,5,5,6,6
									,6};

	private char[] Inv1OctQualityAsc		= {	'M','m','A','d','M','m','A','d','P','A'
									,'d','P','A','d','M','m','A','d','M','m'
									,'A'};

	private int[] Inv1Oct2NoteDistanceAsc 	= {	6,6,6,6,5,5,5,5,4,4
									,4,3,3,3,2,2,2,2,1,1
									,1};

	private char[] Inv1Oct2NoteQualityAsc	= {	'm','M','d','A','m','M','d','A','P','d'
									,'A','P','d','A','m','M','d','A','m','M'
									,'d'};

    	private String[] IntervalTypes = {"Perfect","Major","Minor","Augmented","Diminished"};
    	private String[] IntervalSizes = {"Unison","2nd","3rd","4th","5th","6th","7th","Octave","9th","10th","11th","12th","13th","14th","15th"};

    	private int[] Perfect = { 0, -1, -1,  5,  7, -1, -1, 12,  -1, -1, 17, 19, -1, -1, 24 };
    	private int[] Major   = {  -1, 2,  4, -1, -1,  9, 11, -1, 14, 16, -1, -1, 21, 23, -1 };
    	private int[] Minor   = {  -1, 1,  3, -1, -1,  8, 10, -1, 13, 15, -1, -1, 20, 22, -1 };
    	private int[] Aug     = {  1, 3,  5,  6,  8, 10, 12 , 13, 15, 17, 18, 20, 22, 24, 25 };
    	private int[] Dim     = {  -1, 0,  2,  4,  6,  7,  9, 11, 12, 14, 16, 18, 19, 21, 23 };

    	private String[] DefaultIntvName = {"Perfect Unison", "Minor 2nd", "Major 2nd", "Minor 3rd", "Major 3rd", "Perfect 4th", "Augmented 4th", "Perfect 5th", "Minor 6th", "Major 6th", "Minor 7th", "Major 7th", "Perfect Octave", "Minor 9th", "Major 9th", "Minor 10th", "Major 10th", "Perfect 11th", "Augmented 11th", "Perfect 12th", "Minor 13th", "Major 13th", "Minor 14th", "Major 14th", "Perfect 15th"};

	private int	currentkey=0, currentintv=0;

	private int[] MidiToStaffTrebSharp = {16,16,15,14,14,13,13,12,11,11,10,10,9,9,8,7,7,6,6,5,4,4,3,3,2,2,1,0};
	private int[] MidiToStaffTrebFlat  = {16,15,15,14,13,13,12,12,11,10,10,9,9,8,8,7,6,6,5,5,4,3,3,2,2,1,1,0};
	private int[] MidiToStaffBassSharp = {16,16,15,15,14,13,13,12,12,11,11,10,9,9,8,8,7,6,6,5,5,4,4,3,2,2,1,1,0};
	private int[] MidiToStaffBassFlat  = {16,15,15,14,14,13,12,12,11,11,10,10,9,8,8,7,7,6,5,5,4,4,3,3,2,1,1,0,0};

	private String activeButtonSet;


	private String userIntvQuality=" ";
	private char userIntvType = ' ';
	private String userIntvDistance=" ";
	private int userStaffDistance = 0;

	private String strIntvQuality=" ";
	private String strIntvDistance=" ";
	private String strIntvAboveBelow=" ";

	private int currentmidi = 0, secondmidi = 0;

	private char currIntervalQualityBeingTested=' ';
	private int  currIntervalDistanceBeingTested=0;
	private String currHint="";
	private boolean testingInversions = false;
	private char invIntervalQualityBeingTested=' ';
	private int  invIntervalDistanceBeingTested=0;

	private boolean debugging = false;
	private String parmDebugMode		=	"FALSE";

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

		// Retrieve Parm for Scale Type
		IntervalsBeingTested = new int[256];
		IntervalDistanceBeingTested = new int[256];
		IntervalQualityBeingTested = new char[256];

		//05/01/2004- New procedure for building URLs requires object to be instantiated once
		urlBuilder = new UdmtURLBuilder();
		// End of 05/01/2004 change.		

		//05/01/2004- New procedure for Exam Log requires object to be instantiated once
		examLog = new UdmtExamLog(); 
		// End of 05/01/2004 change.		

		NumItemsBeingTested = 0;

		parmExerciseType = getParameter ("exerciseType");
		if (parmExerciseType == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: ExerciseType is required";
			System.out.println ("The value for the parameter ExerciseType must be one of the following:");
			System.out.println ("CONSTRUCT, IDENTIFY");
		}
		else if (parmExerciseType.equals("CONSTRUCT"))	{
			exerciseMode = 'C';
		}
		else if (parmExerciseType.equals("IDENTIFY")) 	{
			exerciseMode = 'I';
			testingInversions = false;
		}
		else if (parmExerciseType.equals("INVERSIONS"))	{
			exerciseMode = 'I';
			testingInversions = true;
		}
		else
		{
			showAbendScreen = 1;
			txtAbend = "Param: ExerciseType is invalid";
			System.out.println ("The value for the parameter ExerciseType must be one of the following:");
			System.out.println ("CONSTRUCT, IDENTIFY,INVERSIONS");
		}


		parmIntvType = getParameter ("intvType");
		if (parmIntvType == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: IntvType is required";
			System.out.println ("The value for the parameter IntvType must be one of the following:");
			System.out.println ("PerfMaj1OctAsc,All1OctAsc,All1OctDesc,All2OctOnlyAsc,All2OctOnlyDesc");
		}
		else if (parmIntvType.equals("PerfMaj1OctAsc"))	{
			for (int i=0 ; i<=7 ; i++)		{
				IntervalsBeingTested[i] = PerfMaj1OctAsc[i];
				IntervalDistanceBeingTested[i] = PerfMaj1OctAscDistance[i];
				IntervalQualityBeingTested[i] = PerfMaj1OctAscQuality[i];
			}
			NumItemsBeingTested = 8;
			dispIntvType="";
			upDown = +1;
			if (exerciseMode == 'I')	{
				strDrillMode="Interval Identification Practice: Level 1";
				strExamMode="Interval Identification Exam: Level 1";
			}
			else	{
				strDrillMode="Interval Construction Practice: Level 1";
				strExamMode="Interval Construction Exam: Level 1";
			}
			majorOrMinor = +1;
			activeButtonSet = "PM1Oct";
		}
		else if (parmIntvType.equals("All1OctAsc"))	{

			if (testingInversions)
			{
				for (int i=0 ; i<=20 ; i++)		{
					IntervalsBeingTested[i] = Inv1OctAsc[i];
					IntervalDistanceBeingTested[i] =  Inv1OctDistanceAsc[i];
					IntervalQualityBeingTested[i] =  Inv1OctQualityAsc[i];
				}
				NumItemsBeingTested = 21;
			}
			else
			{
				for (int i=0 ; i<=26 ; i++)		{
					IntervalsBeingTested[i] = All1OctAsc[i];
					IntervalDistanceBeingTested[i] =  All1OctDistanceAsc[i];
					IntervalQualityBeingTested[i] =  All1OctQualityAsc[i];
				}
				NumItemsBeingTested = 27;
			}

			dispIntvType="";
			upDown = +1;
			if (exerciseMode == 'I')	{
				if (testingInversions)
				{
					strDrillMode="Interval Inversion Practice";
					strExamMode="Interval Inversion Exam";
				}
				else
				{
					strDrillMode="Interval Identification Practice: Level 2";
					strExamMode="Interval Identification Exam: Level 2";
				}
			}
			else	{
				strDrillMode="Interval Construction Practice: Level 2";
				strExamMode="Interval Construction Exam: Level 2";
			}
			majorOrMinor = +1;
			activeButtonSet = "All1Oct"; 
		}
		else if (parmIntvType.equals("All1OctDesc"))	{
			for (int i=0 ; i<=25 ; i++)		{
				IntervalsBeingTested[i] = All1OctDesc[i];
				IntervalDistanceBeingTested[i] =  All1OctDistanceDesc[i];
				IntervalQualityBeingTested[i] =  All1OctQualityDesc[i];
			}
			NumItemsBeingTested = 26;
			dispIntvType="";
			upDown = -1;
			if (exerciseMode == 'I')	{
				strDrillMode="Interval Identification Practice: Level 4";	//CHGD 7/18/04 ER
				strExamMode="Interval Identification Exam: Level 4";
			}
			else	{
				strDrillMode="Interval Construction Practice: Level 4";	//CHGD 7/18/04 ER
				strExamMode="Interval Construction Exam: Level 4";
			}
			majorOrMinor = +1;
			activeButtonSet = "All1Oct";
		}
		else if (parmIntvType.equals("All2OctOnlyAsc"))	{
			for (int i=0 ; i<=24 ; i++)		{
				IntervalsBeingTested[i] = All2OctOnlyAsc[i];
				IntervalDistanceBeingTested[i] =  All2OctOnlyDistanceAsc[i];
				IntervalQualityBeingTested[i] =  All2OctOnlyQualityAsc[i];
			}
			NumItemsBeingTested = 25;
			dispIntvType="";
			upDown = +1;
			if (exerciseMode == 'I')	{
				strDrillMode="Interval Identification Practice: Level 3";
				strExamMode="Interval Identification Exam: Level 3";
			}
			else	{
				strDrillMode="Interval Construction Practice: Level 3";	//CHGD 7/18/04 ER
				strExamMode="Interval Construction Exam: Level 3";
			}
			majorOrMinor = +1;
			activeButtonSet = "All2Oct";
		}

		else if (parmIntvType.equals("All2OctOnlyDesc"))	{
			for (int i=0 ; i<=24 ; i++)		{
				IntervalsBeingTested[i] = All2OctOnlyDesc[i];
				IntervalDistanceBeingTested[i] =  All2OctOnlyDistanceDesc[i];
				IntervalQualityBeingTested[i] =  All2OctOnlyQualityDesc[i];
			}
			NumItemsBeingTested = 25;
			dispIntvType="";
			upDown = -1;
			if (exerciseMode == 'I')	{
				strDrillMode="Interval Identification Practice: Level 4";	//CHGD 7/18/04 ER
				strExamMode="Interval Identification Exam: Level 4";
			}
			else	{
				strDrillMode="Interval Construction Practice: Level 4";	//CHGD 7/18/04 ER
				strExamMode="Interval Construction Exam: Level 4";
			}
			majorOrMinor = +1;
			activeButtonSet = "All2Oct";
		}
		else 	{
			showAbendScreen = 1;
			txtAbend = "Invalid value for param: IntvType";
			System.out.println ("The value for the parameter IntvType must be one of the following:");
			System.out.println ("PerfMaj1OctAsc, All1OctAsc, All1OctDesc, All2OctOnlyAsc, All2OctOnlyDesc.");
			System.out.println ("");
		}

		//System.out.println ("parmIntvType="+parmIntvType);

		//Retrieve param for examMode
		parmExamMode = getParameter ("examMode");
		examMode = parmExamMode.equals("EXAM");
		
		//05/01/2004:
		if (examMode)
		{
			boolean foundCookie = examLog.getStudentIdFromCookie();
			// How should cookie not found error be handled?
			examLog.setExamId(strExamMode);
			examLog.logExamEntry();
		}
		// End of 05/01/2004 change.		

		//Retrieve param for next URL		

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

			//01-19-2005: Use URLBUILDER only if not in exam mode
			if (examMode)
			{
				UdmtExamKey objEk = new UdmtExamKey();
 				String sExamKey = objEk.getExamKey("Intervals"+parmExerciseType+parmIntvType+"Pass");
 				parmNextURL = parmNextURL + "?r="+sExamKey;
 				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY!
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
				//old code: parmDrillURL = this.getCodeBase() + parmDrillURL;

			//01-19-2005: Use URLBUILDER only if not in exam mode
				if (examMode)
				{
					UdmtExamKey objEk = new UdmtExamKey();
 					String sExamKey = objEk.getExamKey("Intervals"+parmExerciseType+parmIntvType+"Fail");
 					parmDrillURL = parmDrillURL + "?r="+sExamKey;
 					//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY!
				}
				else
				{
					parmDrillURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillURL);
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
			if (!(parmSuppressMIDI.equals("YES")))	{
				suppressingMIDI = false ;
				System.out.println ("MIDI suppression is OFF.");
			}
		}

		//Retrieve param for debugMODE
		parmDebugMode = getParameter ("debugMODE");
		if ( parmDebugMode != null )	{
			if (parmDebugMode.equals("TRUE"))	{
				debugging = true ;
				System.out.println ("Debugging Mode is in effect.");
			}
		}


		if (debugging)
		{
			System.out.println("NumItemsBeingTested="+NumItemsBeingTested);

		}

		//System.out.println("Got suppressMIDI parm...");
		
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

			mt.addImage (imgAllSymbols, 1);
		
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

		noteheadInUse = new int[500];
		noteheadRow = new int[500];
		noteheadCol = new int[500];
		noteheadAccid = new char[500];
		noteheadColor = new int[500];

		userTestQuesInUse = new int[256];
		userTestQuestion1 = new int[256];
		userTestQuestion2 = new int[256];
		userTestQuality = new char[256];

		userTestClefs=new int[256];

		Udata = new int[8];
		Uacc = new int[8];

		rectScreen = getBounds();
		staffX = 5;
		staffY = rectScreen.height / 2 - 60;

		currentClef = randint (1,2);

		userTestQuesHead=0;
		userTestQuesTail=0;
		generateQuestions(0);
		
		currques = 0;
		setCurrentQuestion (currques);
		
		xLeft = staffX + staffNoteXPos[2] - 5 ;
		xRight = staffX+317; // for interval drill - stop at double bar
		yTop = staffY + staffNoteYPos[0] - 20 - 5 ;
		yBottom = staffY + staffNoteYPos[16] - 20 + 20 ;

		setupPopupMenu();
		add ( popup );

		setLayout(null);

		checkButton = new Button("CHECK");
      	checkButton.addActionListener(this);
		checkButton.setEnabled(false);
		checkButton.setBounds ( 180,300,110,30);
		add (checkButton);

      	nextButton = new Button("NEXT");
      	nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		nextButton.setBounds ( 300,300,110,30);
		add(nextButton);

		if ( !examMode )	{

			//Note: removed "Play Mine" functionality because user can cheat by matching tone to "Play Correct"
			
			playnotesButton = new Button("PLAY NOTES");
			playnotesButton.addActionListener(this);
	      	if (suppressingMIDI)	{
				playnotesButton.setEnabled(false);
			} 
			else	{
				playnotesButton.setEnabled(true);
			}
			playnotesButton.setBounds (60,350,110,30);
			add(playnotesButton);

			playcorrButton = new Button("PLAY INTERVAL");
			playcorrButton.addActionListener(this);
	      	if (suppressingMIDI)	{
				playcorrButton.setEnabled(false);
			} 
			else	{
				playcorrButton.setEnabled(true);
			}
			playcorrButton.setBounds ( 180,350,110,30);
			add(playcorrButton);
		
			showcorrButton = new Button("SHOW CORRECT");
			showcorrButton.setEnabled(false);        
			showcorrButton.addActionListener(this);
			showcorrButton.setBounds ( 300,350,110,30);
			add(showcorrButton);

			takeExamButton = new Button("TAKE EXAM");
			takeExamButton.addActionListener(this);
			takeExamButton.setBounds ( 420,350,110,30);
			add(takeExamButton);
		}

		if ( examMode )	{

			nextLessonButton = new Button("NEXT LESSON");
			nextLessonButton.addActionListener(this);
	
			nextLessonButton.setVisible(false);
			nextLessonButton.setBounds ( 420,300,110,30);
			add(nextLessonButton);

		}

		// 0.08 - made exit button always visible 
		exitButton = new Button("EXIT");
		exitButton.addActionListener(this);
	
		exitButton.setVisible(true);
		exitButton.setBounds ( 420,300,110,30);
		add(exitButton);

		if ( !examMode )	{
			if (!suppressingMIDI)
			{
				imntCombo = new Choice();
				for (int i=0 ; i <= 127 ; i++)	{
					imntCombo.add ( midiout.GMImntList[i] );
				} 
				imntCombo.setBounds ( 60,305,110,30);
				add(imntCombo);
				imntCombo.addItemListener(this);
			}
			else
			{
				hintButton = new Button("HINT");
      				hintButton.addActionListener(this);
				hintButton.setEnabled(false);
				hintButton.setBounds ( 60,300,110,30);
				add (hintButton);
			}
		}

		perfButton = new Button("P");
		perfButton.addActionListener(this);
		perfButton.setVisible(true);
		perfButton.setBounds ( 350,120,45,20);
		if (exerciseMode != 'I') {
			perfButton.setEnabled(false);
		}
		add(perfButton);

		majrButton = new Button("M");
		majrButton.addActionListener(this);
		majrButton.setVisible(true);
		majrButton.setBounds ( 400,120,45,20 );
		if (exerciseMode != 'I') {
			majrButton.setEnabled(false);
		}
		add(majrButton);

		minrButton = new Button("m");
		minrButton.addActionListener(this);
		minrButton.setVisible(true);
		minrButton.setBounds ( 450,120,45,20 );
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("PM2Oct"))) {
			minrButton.setEnabled (false);
		}
		else	{
			minrButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			minrButton.setEnabled(false);
		}
		add(minrButton);

		augButton = new Button("A");
		augButton.addActionListener(this);
		augButton.setVisible(true);
		augButton.setBounds ( 500,120,45,20 );
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("PM2Oct"))) {
			augButton.setEnabled (false);
		}
		else	{
			augButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			augButton.setEnabled(false);
		}
		add(augButton);

		dimButton = new Button("d");
		dimButton.addActionListener(this);
		dimButton.setVisible(true);
		dimButton.setBounds ( 550,120,45,20 );
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("PM2Oct"))) {
			dimButton.setEnabled (false);
		}
		else	{
			dimButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			dimButton.setEnabled(false);
		}
		add(dimButton);


		unisButton = new Button("1");
		unisButton.addActionListener(this);
		unisButton.setBounds ( 350,145,45,20);
		unisButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			unisButton.setEnabled(false);
		}
		else {
			unisButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			unisButton.setEnabled(false);
		}
		add(unisButton);

		i2ndButton = new Button("2");
		i2ndButton.addActionListener(this);
		i2ndButton.setBounds ( 400,145,45,20);
		i2ndButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i2ndButton.setEnabled(false);
		}
		else {
			i2ndButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i2ndButton.setEnabled(false);
		}
		add(i2ndButton);

		i3rdButton = new Button("3");
		i3rdButton.addActionListener(this);
		i3rdButton.setBounds ( 450,145,45,20);
		i3rdButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i3rdButton.setEnabled(false);
		}
		else {
			i3rdButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i3rdButton.setEnabled(false);
		}
		add(i3rdButton);

		i4thButton = new Button("4");
		i4thButton.addActionListener(this);
		i4thButton.setBounds ( 500,145,45,20);
		i4thButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i4thButton.setEnabled(false);
		}
		else {
			i4thButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i4thButton.setEnabled(false);
		}
		add(i4thButton);

		i5thButton = new Button("5");
		i5thButton.addActionListener(this);
		i5thButton.setBounds ( 550,145,45,20);
		i5thButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i5thButton.setEnabled(false);
		}
		else {
			i5thButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i5thButton.setEnabled(false);
		}
		add(i5thButton);

		i6thButton = new Button("6");
		i6thButton.addActionListener(this);
		i6thButton.setBounds ( 350,170,45,20);
		i6thButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i6thButton.setEnabled(false);
		}
		else {
			i6thButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i6thButton.setEnabled(false);
		}
		add(i6thButton);

		i7thButton = new Button("7");
		i7thButton.addActionListener(this);
		i7thButton.setBounds ( 400,170,45,20);
		i7thButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			i7thButton.setEnabled(false);
		}
		else {
			i7thButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			i7thButton.setEnabled(false);
		}
		add(i7thButton);

		octButton = new Button("8");
		octButton.addActionListener(this);
		octButton.setBounds ( 450,170,45,20);
		octButton.setVisible(true);
		if ( (activeButtonSet.equals("All2Oct")) || (activeButtonSet.equals("PM2Oct")) ) {
			octButton.setEnabled(false);
		}
		else {
			octButton.setEnabled(true);
		}
		if (exerciseMode != 'I') {
			octButton.setEnabled(false);
		}
		add(octButton);

		i9thButton = new Button("9");
		i9thButton.addActionListener(this);
		i9thButton.setVisible(true);
		i9thButton.setBounds ( 500,170,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i9thButton.setEnabled (false);
		}
		else	{
			i9thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i9thButton.setEnabled(false);
		}
		add(i9thButton);

		i10thButton = new Button("10");
		i10thButton.addActionListener(this);
		i10thButton.setVisible(true);
		i10thButton.setBounds ( 550,170,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i10thButton.setEnabled (false);
		}
		else	{
			i10thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i10thButton.setEnabled(false);
		}
		add(i10thButton);

		i11thButton = new Button("11");
		i11thButton.addActionListener(this);
		i11thButton.setVisible(true);
		i11thButton.setBounds ( 350,195,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i11thButton.setEnabled (false);
		}
		else	{
			i11thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i11thButton.setEnabled(false);
		}
		add(i11thButton);

		i12thButton = new Button("12");
		i12thButton.addActionListener(this);
		i12thButton.setVisible(true);
		i12thButton.setBounds ( 400,195,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i12thButton.setEnabled (false);
		}
		else	{
			i12thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i12thButton.setEnabled(false);
		}
		add(i12thButton);

		i13thButton = new Button("13");
		i13thButton.addActionListener(this);
		i13thButton.setVisible(true);
		i13thButton.setBounds ( 450,195,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i13thButton.setEnabled (false);
		}
		else	{
			i13thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i13thButton.setEnabled(false);
		}
		add(i13thButton);

		i14thButton = new Button("14");
		i14thButton.addActionListener(this);
		i14thButton.setVisible(true);
		i14thButton.setBounds ( 500,195,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i14thButton.setEnabled (false);
		}
		else	{
			i14thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i14thButton.setEnabled(false);
		}
		add(i14thButton);

		i15thButton = new Button("15");
		i15thButton.addActionListener(this);
		i15thButton.setVisible(true);
		i15thButton.setBounds ( 550,195,45,20);
		if ((activeButtonSet.equals("PM1Oct")) || (activeButtonSet.equals("All1Oct"))) {
			i15thButton.setEnabled (false);
		}
		else	{
			i15thButton.setEnabled (true);
		}
		if (exerciseMode != 'I') {
			i15thButton.setEnabled(false);
		}
		add(i15thButton);

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
	
		popup.add ( mi = new MenuItem ( "Natural" ) );
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

		if (exerciseMode == 'C')
		{
			if ( (xcoord >= xLeft) && (xcoord <= xRight) 
				&& (ycoord >= yTop) && (ycoord <= yBottom) )
			{
				popup.show ( e.getComponent(), xcoord - MENU_HOR_OFFSET, ycoord - MENU_VER_OFFSET );
				saveX = xcoord ;
				saveY = ycoord ;
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

		if (exerciseMode == 'C')
		{
			if ( (xcoord >= (staffX + staffNoteXPos[3]) ) && (xcoord <= (staffX + staffNoteXPos[6] ) ) 
				&& (ycoord >= yTop) && (ycoord <= yBottom) ) 
			{
				savecursorx = xcoord;
				savecursory = ycoord;

				if ( (xcoord >= (staffX + staffNoteXPos[3] ) ) 
				&& (xcoord < (staffX + staffNoteXPos[5] ) )
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

				cursorvisible = 1;
			
			} // if x,y in active region
			else
			{
				//make cursor disappear when mouse exits range
				cursorvisible = 0;
				repaint();
			}
		}
	}

//------------------------------------------------------------------------------

	public void actionPerformed (ActionEvent e)
	{
		char ac = '?';
		int uaccval=0;

		//System.out.println ("Menu Item: " + e.getActionCommand() );
		//System.out.println ("Clicked at: " + saveX + " , " + saveY);

		if (e.getActionCommand().equals ( "Natural" ) )
		{
			ac = 'n';
		}
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

			if  (getNoteRowFromColumn ( 4 ) > -1)
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

			if (getNoteRowFromColumn ( 4 ) > -1)
			{
				checkButton.setEnabled(true);
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "CHECK" ) )
		{
			int Corrmidi = 0;
			int correctline = 0;
			int numcorrect = 0;

			//System.out.println("Check button pressed");
			
			Udata[0]=getNoteRowFromColumn ( 2 );
			if (exerciseMode == 'I')
			{
				Udata[2]=getNoteRowFromColumn ( 5 );
			}
			else
			{
				Udata[2]=getNoteRowFromColumn ( 4 );
			}

			Uacc[0]=getNoteAccidFromColumn ( 2 );
			if (exerciseMode == 'I')
			{
				Uacc[2]=getNoteAccidFromColumn ( 5 );
			}
			else
			{
				Uacc[2]=getNoteAccidFromColumn ( 4 );
			}

			//System.out.println("Udata[0]="+Udata[0]+" Uacc[0]="+Uacc[0]);
			//System.out.println("Udata[2]="+Udata[2]+" Uacc[2]="+Uacc[2]);

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

			int i=2;
			
			//System.out.println("Udata["+i+"]="+Udata[i]+" acc="+Uacc[i]);
			correctline = 0;


			if (Udata[i] == (Udata[0] - currIntervalDistanceBeingTested ))
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
				
				Corrmidi += currentintv ;

				if (exerciseMode == 'I')
				{
					//System.out.println ("userIntvType="+userIntvType);
					//System.out.println ("currIntervalQualityBeingTested="+currIntervalQualityBeingTested);
					//System.out.println ("userStaffDistance="+userStaffDistance);
					//System.out.println ("currIntervalDistanceBeingTested="+currIntervalDistanceBeingTested);
					//System.out.println ("invIntervalQualityBeingTested="+invIntervalQualityBeingTested);
					//System.out.println ("invIntervalDistanceBeingTested="+invIntervalDistanceBeingTested);
					
					char testQuality;
					int testDistance;

					if (testingInversions)
					{
						testQuality = invIntervalQualityBeingTested;
						testDistance = invIntervalDistanceBeingTested;
					}
					else
					{
						testQuality = currIntervalQualityBeingTested;
						testDistance = currIntervalDistanceBeingTested;
					}

					if (userIntvType == testQuality)
					{
						if (userStaffDistance == testDistance)
						{
							numcorrect++;
						}
						else
						{
							currHint = "The Interval General Name is incorrect.";
						}
					}
					else
					{					
						if (userStaffDistance == testDistance)
						{
							currHint = "The Interval Quality is incorrect.";
						}
						else
						{
							currHint = "Both the Interval Quality and General Name are incorrect.";
						}
					}
				}


				if (exerciseMode != 'I')
				{

					//System.out.println ("Umidi="+Umidi+" Corrmidi="+Corrmidi);
					if ((Umidi == Corrmidi) && (correctline == 1))
					{
						//System.out.println("correct midi");
						setNoteheadColor ( i*2, Udata[i], 0 );
						numcorrect ++ ;
					}
					else
					{
						//System.out.println("incorrect midi, i="+i+" Udata[i]="+Udata[i]);
						setNoteheadColor ( i*2, Udata[i], 1 );
					}
				}
			}
			

			gotThisQuesCorrect=0;

			if ( numtimeschecked == 0)	{
				numquespresented++;
			}

			if (numcorrect == 1)
			{
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
					if (exerciseMode == 'I')	{
						strFeedback = currHint;
					}
					else	{
						strFeedback = strIncorrectConstr;
					}
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

					//05/01/2004: Write to Exam Log and branch immediately to NEXT LESSON page.

					//exitButton.setVisible(false);
					//nextLessonButton.setVisible(true);
					//checkButton.setEnabled(false);
					//nextButton.setEnabled(false);

					examLog.logExamPass();

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

					
					//05/01/2004 - change to write Exam log and branch immediately to "SORRY" page.

					//exitButton.setVisible(true);
					//checkButton.setEnabled(false);
					//nextButton.setEnabled(false);

					examLog.logExamFail();
	
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
			prevques1=userTestQuestion1[currques];
			prevques2=userTestQuestion2[currques];

			userIntvQuality=" ";
			userIntvDistance=" ";
			
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
			if (currques >=  (NumItemsBeingTested*2))
			{	currques = 0;
			}

			numtimeschecked = 0;

			userTestQuesTail ++;
			if (userTestQuesTail >=  (NumItemsBeingTested*2))
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
				
				generateQuestions(userTestQuesHead);
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
			int miditime = 0, midinote1=0, midinote2=0;

			//System.out.println("Play Mine button pressed");
			Udata[0]=getNoteRowFromColumn ( 2 );
			Udata[1]=getNoteRowFromColumn ( 4 );

			Uacc[0]=getNoteAccidFromColumn ( 2 );
			Uacc[1]=getNoteAccidFromColumn ( 4 );

			//System.out.println("Udata[0]="+Udata[0]);

			if (Udata[0] != -1)
			{
				if (currentClef == 1) // treble
				{
					midinote1 = MidiTreble [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("treb U midi" + Umidi ); 
				}
				else //bass
				{
					midinote1 = MidiBass [ Udata[0] ]  + Uacc[0] ;
					//System.out.println("bass U midi" + Umidi );
				}

				//System.out.println("Udata["+i+"]="+Udata[i]+" acc="+Uacc[i]);

				currentClef = userTestClefs[currques];
				
				//System.out.println ("Current Clef: "+currentClef);
				//System.out.println ("AccVal: "+Uacc[i]);

				if (Udata[1] != -1)
				{
					if (currentClef == 1) // treble
					{
						midinote2 = MidiTreble [ Udata[1] ]  + Uacc[1] ;
						//System.out.println("i="+i+" treb U midi" + Umidi );
					}
					else //bass
					{
						midinote2 = MidiBass [ Udata[1] ]  + Uacc[1] ;
						//System.out.println("i="+i+" bass U midi"+ Umidi );
					}
				}

				if (!suppressingMIDI)
				{
					midiout.createSequence();
		
					midiout.addProgChg    ( 0,  userImnt  );
			
					midiout.addNoteOn     ( 0,   midinote1);	
					midiout.addNoteOn     ( 0,   midinote2);	
					midiout.addNoteOff    ( 22,  midinote1 );	
					midiout.addNoteOff    ( 22,  midinote2 );
					midiout.addEndOfTrack ( 24 );
					midiout.playSequence  ( 120 );

					if (debugging)
					{
						System.out.println("PLAY MINE button: userImnt="+userImnt);
						System.out.println("midinote1="+midinote1+" midinote2="+midinote2);
					}
				}

			}
		} //playmine

		if (e.getActionCommand().equals ( "PLAY CORRECT" ) )
		{
			//System.out.println("Play correct button pressed");
			if (!suppressingMIDI)
			{
				midiout.createSequence();
		
				midiout.addProgChg    ( 0,  userImnt  );
				midiout.addNoteOn     ( 0,  currentmidi );	
				midiout.addNoteOn     ( 0,  secondmidi);	
				midiout.addNoteOff    ( 22, currentmidi );	
				midiout.addNoteOff    ( 22,  secondmidi );

				midiout.addEndOfTrack ( 24 );
				midiout.playSequence  ( 120 );

				if (debugging)
				{
					System.out.println("PLAY CORRECT button: userImnt="+userImnt);
					System.out.println("currentmidi="+currentmidi+" secondmidi="+secondmidi);
				}
			}
		} //playnotes

		if (e.getActionCommand().equals ( "PLAY NOTES" ) )
		{
			//System.out.println("Play interval button pressed");
			if (!suppressingMIDI)
			{
				midiout.createSequence();
		
				midiout.addProgChg    ( 0,  userImnt  );
				midiout.addNoteOn     ( 0,  currentmidi );	
				midiout.addNoteOff    ( 22, currentmidi );	
				midiout.addNoteOn     ( 24,  secondmidi);	
				midiout.addNoteOff    ( 46,  secondmidi );
				midiout.addEndOfTrack ( 48 );
				midiout.playSequence  ( 120 );

				if (debugging)
				{
					System.out.println("PLAY NOTES button: userImnt="+userImnt);
					System.out.println("currentmidi="+currentmidi+" secondmidi="+secondmidi);
				}
			}
		} //play notes

		if (e.getActionCommand().equals ( "PLAY INTERVAL" ) )
		{
			//System.out.println("Play interval button pressed");
			if (!suppressingMIDI)
			{
				midiout.createSequence();
		
				midiout.addProgChg    ( 0,  userImnt  );
				midiout.addNoteOn     ( 0,  currentmidi );	
				midiout.addNoteOn     ( 0,  secondmidi);	
				midiout.addNoteOff    ( 22, currentmidi );	
				midiout.addNoteOff    ( 22,  secondmidi );
				midiout.addEndOfTrack ( 24 );
				midiout.playSequence  ( 120 );

				if (debugging)
				{
					System.out.println("PLAY INTERVAL button: userImnt="+userImnt);
					System.out.println("currentmidi="+currentmidi+" secondmidi="+secondmidi);
				}
			}
		} //play interval

		if (e.getActionCommand().equals ( "SHOW CORRECT" ) )
		{
			int Urow, Uacc, curaccid;
			int Corrmidi = 0;

			//System.out.println("Show Correct button pressed");

			if (exerciseMode == 'I')
			{
				strFeedback = "The correct answer is: "+ strIntvQuality + " " + strIntvDistance +".";
			}

			if (exerciseMode != 'I')
			{
				Urow=getNoteRowFromColumn ( 2 );
				Uacc=getNoteAccidFromColumn ( 2 );

				eraseAllNoteheadsInColumn (4);

				//System.out.println("Urow="+Urow+" Uacc="+Uacc);

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
					
				
					if (testingInversions)
					{
						Corrmidi += 12; 
						Urow = Urow - 7;
					}
					else
					{
						Corrmidi += currentintv; 
						Urow = Urow - currIntervalDistanceBeingTested;
					}


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
					{	ac = 'n';
					} else if (curaccid == 1)
					{	ac = 's';
					} else if (curaccid == -1)
					{	ac = 'f';
					} else if (curaccid == 2)
					{	ac = 'S';
					} else if (curaccid == -2)
					{	ac = 'F';
					}

					//System.out.println ("curaccid="+curaccid+" ac="+ac);					

					addNoteheadToStaff ( 4, Urow, ac);

				} // if urow not -1

				checkButton.setEnabled(false);
				strFeedback = "The correct notes are being displayed.";
			} 

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

		if (e.getActionCommand().equals ( "P" ) )
		{
			userIntvQuality="Perfect";
			userIntvType='P';
			repaint();
		}
		if (e.getActionCommand().equals ( "M" ) )
		{
			userIntvQuality="Major";
			userIntvType='M';
			repaint();
		}
		if (e.getActionCommand().equals ( "m" ) )
		{
			userIntvQuality="Minor";
			userIntvType='m';
			repaint();
		}
		if (e.getActionCommand().equals ( "A" ) )
		{
			userIntvQuality="Augmented";
			userIntvType='A';
			repaint();
		}
		if (e.getActionCommand().equals ( "d" ) )
		{
			userIntvQuality="Diminished";
			userIntvType='d';
			repaint();
		}
		if (e.getActionCommand().equals ( "1" ) )
		{
			userIntvDistance="Unison";
			userStaffDistance = 0;
			repaint();
		}
		if (e.getActionCommand().equals ( "2" ) )
		{
			userIntvDistance="2nd";
			userStaffDistance = 1;
			repaint();
		}
		if (e.getActionCommand().equals ( "3" ) )
		{
			userIntvDistance="3rd";
			userStaffDistance = 2;
			repaint();
		}
		if (e.getActionCommand().equals ( "4" ) )
		{
			userIntvDistance="4th";
			userStaffDistance = 3;
			repaint();
		}
		if (e.getActionCommand().equals ( "5" ) )
		{
			userIntvDistance="5th";
			userStaffDistance = 4;
			repaint();
		}
		if (e.getActionCommand().equals ( "6" ) )
		{
			userIntvDistance="6th";
			userStaffDistance = 5;
			repaint();
		}
		if (e.getActionCommand().equals ( "7" ) )
		{
			userIntvDistance="7th";
			userStaffDistance = 6;
			repaint();
		}
		if (e.getActionCommand().equals ( "8" ) )
		{
			userIntvDistance="Octave";
			userStaffDistance = 7;
			repaint();
		}
		if (e.getActionCommand().equals ( "9" ) )
		{
			userIntvDistance="9th";
			userStaffDistance = 8;
			repaint();
		}
		if (e.getActionCommand().equals ( "10" ) )
		{
			userIntvDistance="10th";
			userStaffDistance = 9;
			repaint();
		}
		if (e.getActionCommand().equals ( "11" ) )
		{
			userIntvDistance="11th";
			userStaffDistance = 10;
			repaint();
		}
		if (e.getActionCommand().equals ( "12" ) )
		{
			userIntvDistance="12th";
			userStaffDistance = 11;
			repaint();
		}
		if (e.getActionCommand().equals ( "13" ) )
		{
			userIntvDistance="13th";
			userStaffDistance = 12;
			repaint();
		}
		if (e.getActionCommand().equals ( "14" ) )
		{
			userIntvDistance="14th";
			userStaffDistance = 13;
			repaint();
		}
		if (e.getActionCommand().equals ( "15" ) )
		{
			userIntvDistance="15th";
			userStaffDistance = 14;
			repaint();
		}

		if (exerciseMode == 'I')
		{
			if (userIntvDistance.equals(" ") || userIntvQuality.equals(" "))
			{
				checkButton.setEnabled(false);
			}
			else
			{
				checkButton.setEnabled(true);
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

	public void generateQuestions (int startat)
	{
		int ix, questionNum, keyNum;
		int inuse[];
		int q[];
		int k[];
		char qual[];
		int clef[];

		int j, ct;
		int qtemp;

		inuse = new int[NumItemsBeingTested];
		q = new int[NumItemsBeingTested];
		qual = new char[NumItemsBeingTested];

		k = new int[NumItemsBeingTested];
		clef = new int[NumItemsBeingTested];
	
		//System.out.println ("generatequestions: startat="+startat);

		for (int i=0; i <= (NumItemsBeingTested-1); i++)
		{   
			inuse[i] = 0;
			q[i] = 0;
			qual[i]=' ';
		}

		for (ix=0; ix <= (NumItemsBeingTested-1) ; ix++)	// assign each interval to a random question #
		{
			if (debugging)
			{
				questionNum = (int)(ix % NumItemsBeingTested);
				keyNum = (int)( ( (ix) % 15) - 7);
			}
			else
			{
				questionNum = (int)((Math.random() * NumItemsBeingTested ));

				keyNum = (int)((Math.random() * 16 ) - 8 );
			}

			if (debugging)
		    	{
				System.out.println("ix="+ix+" questionNum="+questionNum+" keyNum="+keyNum+" Trebleorbass="+trebleorbass);
			}

		    	while (inuse[questionNum] > 0)	// skip any questions already populated - find a slot
	    		{
				questionNum++;
				if (questionNum >= NumItemsBeingTested)
				{
				    questionNum = 0;
				}
			} //while

			inuse[questionNum] = 1;	
			q[questionNum] = IntervalsBeingTested[ix];
			qual[questionNum] = IntervalQualityBeingTested[ix];

			k[questionNum] = keyNum;
			clef[questionNum]= trebleorbass;
	

			// Rotate among treble/bass 
			trebleorbass *= -1;

			if (debugging)
			{
			    	System.out.println("Assigned interval" + ix +" to question: "
					+questionNum);
			}

		}	//for

		if (debugging)
		{
			System.out.println ("End of For loop");
		}

		// if q[0]=prevques,swap q[0] and q[14]
		if ( q[0]==prevques1 )
		{
			qtemp=q[0];
			q[0]=q[NumItemsBeingTested-1];
			q[NumItemsBeingTested-1]=qtemp;
		}

		//System.out.println ("Assigning questions to global array");
		j=startat;
		ct=0;
		while (ct<NumItemsBeingTested)
		{
			//System.out.println ("ct="+ct);
			userTestQuesInUse[j] = inuse[ct];	
			userTestQuestion1[j] = k[ct];
			userTestQuestion2[j] = q[ct];
			userTestQuality[j] = qual[ct];

			userTestClefs[j]= clef[ct];
			
			j++;
			if (j >= (NumItemsBeingTested * 2) )
			{
				j=0;
			}
			ct++;
		} //while
		userTestQuesHead=j;
		//missct = 0;

		if (debugging)
		{
			for (int x=0 ; x<=((NumItemsBeingTested*2)-1) ; x++)
			{
				if (userTestQuesInUse[x] == 1)
				{
					System.out.println ("Ques:"+x+" Key:"+userTestQuestion1[x]
							+" Interval:"+userTestQuestion2[x]
							+" Quality:"+userTestQuality[x]
							+" Clef:"+userTestClefs[x]);
				}
			}
		}
	}

//------------------------------------------------------------------------------

	public void setCurrentQuestion( int q )
	{
		int startnote = 0, secondnote =0, startacnum=0, secondacnum = 0;
		char ac = ' ', secondac = ' ';
		int altnote = 0; char altac=' '; int altmidi=0;

		currentkey = userTestQuestion1[q];
		currentintv = userTestQuestion2[q];
		currentqual = userTestQuality[q];

		//System.out.println ("Current Key: "+currentkey);

		currentClef = userTestClefs[q];
		//System.out.println ("Current Clef: "+currentClef);

		if (currentClef == 1)		{	// treble
			if (currentkey >= 0)	{ 	// sharp key OR c major
				if (upDown == +1)	{
					startnote = cyc5shTrebMaj[Math.abs(currentkey)];
					altnote = cyc5shTrebMaj2Oct[Math.abs(currentkey)];
				}
				else	{
					startnote = cyc5shTrebDescMaj[Math.abs(currentkey)];
					altnote = cyc5shTrebDescMaj2Oct[Math.abs(currentkey)];
				}
				ac = cyc5shAccMaj[Math.abs(currentkey)];
				altac = cyc5shAccMaj2Oct[Math.abs(currentkey)];

			}
			else if (currentkey < 0)	{	 // flat key
				if (upDown == +1)	{
					startnote = cyc5flTrebMaj[Math.abs(currentkey)];
					altnote = cyc5flTrebMaj2Oct[Math.abs(currentkey)];
				}
				else	{
					startnote = cyc5flTrebDescMaj[Math.abs(currentkey)];
					altnote = cyc5flTrebDescMaj2Oct[Math.abs(currentkey)];

				}
				ac = cyc5flAccMaj[Math.abs(currentkey)];
				altac = cyc5flAccMaj2Oct[Math.abs(currentkey)];

			}
		}		//treble clef
		else 	{					// bass
			if (currentkey >= 0)	{	// sharp key OR c major
				if (upDown == +1)	{
					startnote = cyc5shBassMaj[Math.abs(currentkey)];
					altnote = cyc5shBassMaj2Oct[Math.abs(currentkey)];
				}
				else	{
					startnote = cyc5shBassDescMaj[Math.abs(currentkey)];
					altnote = cyc5shBassDescMaj2Oct[Math.abs(currentkey)];
				}
				ac = cyc5shAccMaj[Math.abs(currentkey)];
				altac = cyc5shAccMaj2Oct[Math.abs(currentkey)];

			}
			else if (currentkey < 0) {	// flat key
				if (upDown == +1)	 {
					startnote = cyc5flBassMaj[Math.abs(currentkey)];
					altnote = cyc5flBassMaj2Oct[Math.abs(currentkey)];

				}
				else	{
					startnote = cyc5flBassDescMaj[Math.abs(currentkey)];
					altnote = cyc5flBassDescMaj2Oct[Math.abs(currentkey)];
				}
				ac = cyc5flAccMaj[Math.abs(currentkey)];
				altac = cyc5flAccMaj2Oct[Math.abs(currentkey)];
			}
		}		// bass clef
		
		
		//System.out.println("startnote="+startnote);

		if (currentClef == 1) // treble
		{
			currentmidi = MidiTreble [ startnote ]  ;
			altmidi = MidiTreble [ altnote ]  ;

		}
		else //bass
		{
			currentmidi = MidiBass [ startnote ]   ;
			altmidi = MidiBass [ altnote ]  ;

		}

		if (ac == 's')
		{
			currentmidi++;
		}
		else if (ac == 'f')
		{
			currentmidi--;
		}
		if (altac == 's')
		{
			altmidi++;
		}
		else if (altac == 'f')
		{
			altmidi--;
		}


		//System.out.println ("altnote="+altnote+" altmidi="+altmidi+" altac="+altac);

		secondmidi = currentmidi + currentintv;

		//System.out.println ("startnote="+startnote);
		//System.out.println ("currentmidi="+currentmidi+" upDown="+upDown+" currentintv="+currentintv);
		//System.out.println ("secondmidi="+secondmidi);

		// For debugging:
		//strExamFeedback1 = "startnote="+startnote+" currentmidi="+currentmidi+" upDown="+upDown
		//	+" currentintv="+currentintv+" secondmidi="+secondmidi;

		int idist = 0;
		for (int indx = 0 ; indx < NumItemsBeingTested ; indx++ )
		{
			//System.out.println ("In for loop: indx="+indx);
			//System.out.println ("IntervalsBeingTested[indx]="+IntervalsBeingTested[indx]);
			//System.out.println (" currentintv="+currentintv);
			//System.out.println (" currentqual="+currentqual);
			//System.out.println ("IntervalQualityBeingTested[indx]="+IntervalQualityBeingTested[indx]);



			if ((IntervalsBeingTested[indx] == currentintv) && (IntervalQualityBeingTested[indx] == currentqual))
			{
				idist = indx;
				break;
			}
		}

		//System.out.println ("In SetCurrentQuestion:");
		//System.out.println ("idist="+idist+" IntervalDistanceBeingTested="+IntervalDistanceBeingTested[idist]);
		//System.out.println (" IntervalQualityBeingTested="+IntervalQualityBeingTested[idist]);

		currIntervalQualityBeingTested = IntervalQualityBeingTested[idist];
		currIntervalDistanceBeingTested = IntervalDistanceBeingTested[idist];

		if (testingInversions)
		{
			invIntervalQualityBeingTested = Inv1Oct2NoteQualityAsc[idist];
			invIntervalDistanceBeingTested = Inv1Oct2NoteDistanceAsc[idist];

			//System.out.println("invIntervalQualityBeingTested="+invIntervalQualityBeingTested);
			//System.out.println("invIntervalDistanceBeingTested="+invIntervalDistanceBeingTested);

			switch (invIntervalQualityBeingTested)
			{
				case 'P': {
					strIntvQuality="Perfect";
					break;
				}
				case 'M': {
					strIntvQuality="Major";
					break;
				}
				case 'm': {
					strIntvQuality="Minor";
					break;
				}
				case 'A': {
					strIntvQuality="Augmented";
					break;
				}
				case 'd': {
					strIntvQuality="Diminished";
					break;
				}
			}
			if ( upDown  >= 0 )
			{
				strIntvAboveBelow = "above";
			}
			else
			{
				strIntvAboveBelow = "below";
			}

			strIntvDistance=IntervalSizes [ Math.abs( invIntervalDistanceBeingTested ) ];
		}
		else
		{
			switch (currIntervalQualityBeingTested)
			{
				case 'P': {
					strIntvQuality="Perfect";
					break;
				}
				case 'M': {
					strIntvQuality="Major";
					break;
				}
				case 'm': {
					strIntvQuality="Minor";
					break;
				}	
				case 'A': {
					strIntvQuality="Augmented";
					break;
				}
				case 'd': {
					strIntvQuality="Diminished";
					break;
				}
			}
			if ( upDown  >= 0 )
			{	
				strIntvAboveBelow = "above";
			}
			else
			{
				strIntvAboveBelow = "below";
			}	
			strIntvDistance=IntervalSizes [ Math.abs( currIntervalDistanceBeingTested ) ];
		}

		if (currentClef == 1)	{		// treble
			if ((secondmidi >= 57) && (secondmidi <= 82))	{	// low A thru high Bb
				// Note: Upper range reduced from 84 to 82 to avoid potential negative array index
				secondnote = startnote - currIntervalDistanceBeingTested;
				if (secondnote < 0)
				{
					System.out.println ("UNEXPECTED CONDITION ENCOUNTERED: treble: secondnote < 0");
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: currentmidi="+currentmidi);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: idist="+idist);
					System.out.println ("DEBUG: currIntervalDistanceBeingTested="
						+currIntervalDistanceBeingTested);
					System.out.println ("DEBUG: currentkey="+currentkey);
				}
				secondacnum = secondmidi - MidiTreble[secondnote];

				//System.out.println (": startnote="+startnote);
				//System.out.println (": currentmidi="+currentmidi);
				//System.out.println (": ac="+ac);
				//System.out.println (": secondnote="+secondnote);
				//System.out.println (": secondmidi="+secondmidi);
				//System.out.println (": secondacnum="+secondacnum);
				//System.out.println (": idist="+idist);
				//System.out.println (": currIntervalDistanceBeingTested="+currIntervalDistanceBeingTested);
				//System.out.println (": currentkey="+currentkey);


			}
			else if (secondmidi < 57)		{
				startnote = altnote;
				ac = altac;
				secondmidi = altmidi + currentintv;
				secondnote = startnote - currIntervalDistanceBeingTested;
				secondacnum = secondmidi - MidiTreble[secondnote];
				currentmidi = altmidi;

				// recompute starting note:
				if (debugging)
				{
					System.out.println ("TREBLE-BELOW BOTTOM LEGER LINE");
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: ac="+ac);
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: secondacnum="+secondacnum);
				}

				//currentmidi = 57 + secondacnum + currentintv;
				//System.out.println("currentmidi="+currentmidi);
				//startnote = Math.abs(16 + currIntervalDistanceBeingTested);
				//System.out.println("startnote="+startnote);
				//startacnum = currentmidi - MidiTreble[startnote];			
				//System.out.println("startacnum="+startacnum);

				//if (startacnum == +1)	{
				//	ac = 's';
				//}
				//else if (startacnum == -1)	{
				//	ac = 'f';
				//}
				//else if (startacnum == +2)	{
				//	ac = 'S';
				//}
				//else if (startacnum == -2)	{
				//	ac = 'F';
				//}
				//else	{	// make sure accidental always drawn for Unisons
				//	if (Math.abs(currIntervalDistanceBeingTested) == 0)
				//	{
				//		ac = 'n';
				//	}
				//	else
				//	{
				//		ac = ' ';
				//	}
				//}

			}
			else if (secondmidi > 82)	{
				startnote = altnote;
				ac = altac;
				secondmidi = altmidi + currentintv;
				secondnote = startnote - currIntervalDistanceBeingTested;
				secondacnum = secondmidi - MidiTreble[secondnote];
				currentmidi = altmidi;

				//secondnote = 1;
				//secondacnum = secondmidi - MidiTreble[secondnote];

				// recompute starting note:
				if (debugging)
				{
					System.out.println ("TREBLE-ABOVE TOP LEGER LINE");
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: ac="+ac);
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: secondacnum="+secondacnum);
				}

				//currentmidi = 82 + secondacnum - currentintv;
				//System.out.println("currentmidi="+currentmidi);
				//startnote = Math.abs(1 - currIntervalDistanceBeingTested);
				//System.out.println("startnote="+startnote);
				//startacnum = currentmidi - MidiTreble[startnote];			
				//System.out.println("startacnum="+startacnum);

				//if (startacnum == +1)	{
				//	ac = 's';
				//}
				//else if (startacnum == -1)	{
				//	ac = 'f';
				//}
				//else if (startacnum == +2)	{
				//	ac = 'S';
				//}
				//else if (startacnum == -2)	{
				//	ac = 'F';
				//}
				//else	{	// make sure accidental always drawn for Unisons
				//	if (Math.abs(currIntervalDistanceBeingTested) == 0)
				//	{
				//		ac = 'n';
				//	}
				//	else
				//	{
				//		ac = ' ';
				//	}
				//}

			}
		}		//treble clef
		else 	{					// bass
			if ((secondmidi >= 36) && (secondmidi <= 62))	{	// low C thru middle D
				// Note: Upper range reduced from 64 to 62 to avoid potential negative array index
				secondnote = startnote - currIntervalDistanceBeingTested;
				if (secondnote < 0)
				{
					System.out.println ("UNEXPECTED CONDITION ENCOUNTERED: bass: secondnote < 0");
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: currentmidi="+currentmidi);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: idist="+idist);
					System.out.println ("DEBUG: currIntervalDistanceBeingTested="
						+currIntervalDistanceBeingTested);
					System.out.println ("DEBUG: currentkey="+currentkey);
				}
				secondacnum = secondmidi - MidiBass[secondnote];

				//System.out.println (": startnote="+startnote);
				//System.out.println (": currentmidi="+currentmidi);
				//System.out.println (": ac="+ac);
				//System.out.println (": secondnote="+secondnote);
				//System.out.println (": secondmidi="+secondmidi);
				//System.out.println (": secondacnum="+secondacnum);
				//System.out.println (": idist="+idist);
				//System.out.println (": currIntervalDistanceBeingTested="+currIntervalDistanceBeingTested);
				//System.out.println (": currentkey="+currentkey);

			}
			else if (secondmidi < 36)		{

				startnote = altnote;
				ac = altac;
				secondmidi = altmidi + currentintv;
				secondnote = startnote - currIntervalDistanceBeingTested;
				secondacnum = secondmidi - MidiBass[secondnote];
				currentmidi = altmidi;

				//secondnote = 16;
				//secondacnum = secondmidi - MidiBass[secondnote];

				// recompute starting note:
				if (debugging)
				{
					System.out.println ("BASS-BELOW BOTTOM LEGER LINE");
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: ac="+ac);
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: secondacnum="+secondacnum);
				}

				//currentmidi = 36 + secondacnum + currentintv;
				//System.out.println("currentmidi="+currentmidi);
				//startnote = Math.abs(16 + currIntervalDistanceBeingTested);
				//System.out.println("startnote="+startnote);
				//startacnum = currentmidi - MidiBass[startnote];			
				//System.out.println("startacnum="+startacnum);

				//if (startacnum == +1)	{
				//	ac = 's';
				//}
				//else if (startacnum == -1)	{
				//	ac = 'f';
				//}
				//else if (startacnum == +2)	{
				//	ac = 'S';
				//}
				//else if (startacnum == -2)	{
				//	ac = 'F';
				//}
				//else	{	// make sure accidental always drawn for Unisons
				//	if (Math.abs(currIntervalDistanceBeingTested) == 0)
				//	{
				//		ac = 'n';
				//	}
				//	else
				//	{
				//		ac = ' ';
				//	}
				//}

			}
			else if (secondmidi > 62)	{

				startnote = altnote;
				ac = altac;
				secondmidi = altmidi + currentintv;
				secondnote = startnote - currIntervalDistanceBeingTested;
				secondacnum = secondmidi - MidiBass[secondnote];
				currentmidi = altmidi;

				//secondnote = 1;
				//secondacnum = secondmidi - MidiBass[secondnote];

				// recompute starting note:
				if (debugging)
				{
					System.out.println ("BASS-ABOVE TOP LEGER LINE");
					System.out.println ("DEBUG: startnote="+startnote);
					System.out.println ("DEBUG: ac="+ac);
					System.out.println ("DEBUG: secondnote="+secondnote);
					System.out.println ("DEBUG: secondmidi="+secondmidi);
					System.out.println ("DEBUG: secondacnum="+secondacnum);
				}

				//currentmidi = 62 + secondacnum - currentintv;
				//System.out.println("currentmidi="+currentmidi);
				//startnote = Math.abs(1 - currIntervalDistanceBeingTested);
				//System.out.println("startnote="+startnote);
				//startacnum = currentmidi - MidiBass[startnote];			
				//System.out.println("startacnum="+startacnum);

				//if (startacnum == +1)	{
				//	ac = 's';
				//}
				//else if (startacnum == -1)	{
				//	ac = 'f';
				//}
				//else if (startacnum == +2)	{
				//	ac = 'S';
				//}
				//else if (startacnum == -2)	{
				//	ac = 'F';
				//}
				//else	{	// make sure accidental always drawn for Unisons
				//	if (Math.abs(currIntervalDistanceBeingTested) == 0)
				//	{
				//		ac = 'n';
				//	}
				//	else
				//	{
				//		ac = ' ';
				//	}
				//}

			}
		}		// bass clef

		//System.out.println ("secondnote = "+secondnote+" secondacnum="+secondacnum);

		// For debugging:
		//strExamFeedback2 = "secondnote = "+secondnote+" secondacnum="+secondacnum;


		if (secondacnum == +1)	{
			secondac = 's';
		}
		else if (secondacnum == -1)	{
			secondac = 'f';
		}
		else if (secondacnum == +2)	{
			secondac = 'S';
		}
		else if (secondacnum == -2)	{
			secondac = 'F';
		}
		else if (secondacnum == 0)	{	// make sure accidental always drawn for Unisons
			if (Math.abs(currIntervalDistanceBeingTested) == 0)
			{
				if (ac == ' ')	// 3/2/04: print natural sign only if first accidental not blank
				{
					secondac = ' ';
				}
				else
				{
					secondac = 'n';
				}
			}
			else
			{
				secondac = ' ';
			}
		}
		else // HANDLE UNWANTED INTERVALS (EX: TRIPLE FLAT)
		{
			if (debugging)
			{
				System.out.println("RARE CONDITION: Handling Unwanted Interval: "+secondacnum);
			}
			startnote = altnote;
			ac = altac;
			secondmidi = altmidi + currentintv;
			secondnote = startnote - currIntervalDistanceBeingTested;
			currentmidi = altmidi;

			if (currentClef == 1)	{		// treble
				secondacnum = secondmidi - MidiTreble[secondnote];
			}
			else	{
				secondacnum = secondmidi - MidiBass[secondnote];
			}
			if (secondacnum == +1)	{
				secondac = 's';
			}
			else if (secondacnum == -1)	{
				secondac = 'f';
			}
			else if (secondacnum == +2)	{
				secondac = 'S';
			}
			else if (secondacnum == -2)	{
				secondac = 'F';
			}
		}

		startcursorrow = startnote;
		startcursorcol = 4;

		cursorrow = startnote;
		cursorcol = 4;

		addNoteheadToStaff (2, startnote, ac);

		if (exerciseMode == 'I')
		{
			addNoteheadToStaff (5, secondnote, secondac); 
		}
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
		startcursorcol 	= 	4;

		cursorrow 		= 	startnote;
		cursorcol 		= 	4;

		addNoteheadToStaff (2, startnote, ac);
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
		

		if (exerciseMode == 'I')
		{
			if (testingInversions)
			{
				strInstruct1 = "Identify the ";
				strInstruct2 = "inversion of ";
				strInstruct3 = "the interval shown below.";
			}
			else
			{
				strInstruct1 = "Identify the ";
				strInstruct2 = "";
				strInstruct3 = "interval for the two notes shown below.";
			}
			g.drawString ("Your answer: "+userIntvQuality+" "+userIntvDistance,350,235);
		}
		else
		{
			strInstruct1 = "Construct the interval which is a";

			if (strIntvQuality.equals("Augmented"))	{
				strInstruct2 = "n ";
			}
			else	{
				strInstruct2 = " ";
			}
			strInstruct2 += strIntvQuality + " " +strIntvDistance + " " + strIntvAboveBelow;

			strInstruct3 = " the note shown below.";		
		}

		strInstruct = strInstruct1 + strInstruct2 + strInstruct3;

		centerString (g, strInstruct, txtfont, 60);

		//System.out.println("DrawScreen 1");

		for (int x=staffX ; x < staffX+325 ; x+=65 )	{
			g.drawImage (muStaff.getImage(), x, staffY, this );
		}

		//System.out.println("DrawScreen 2");

		g.drawImage (muBar.getImage(), staffX - 1, staffY, this);
		g.drawImage (muRtDblBar.getImage(), staffX+317, staffY, this);


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

		// Draw Key Signature:
		
//		if (currentClef == 1)	{	// Treble clef
//			if (currentkey > 0)	// Sharps
//			{
//				for (int i=0 ; i < (Math.abs(currentkey)) ; i++)
//				{
//					g.drawImage (muSharp.getImage()
//						, staffX + keySharpXOffset[i]
//						, staffY-20+keySharpYOffset[i]
//						, this);
//				}
//			}
//			else if (currentkey < 0)	// Flats
//			{
//				for (int i=0 ; i < (Math.abs(currentkey)) ; i++)
//				{
//					g.drawImage (muFlat.getImage()
//						, staffX + keyFlatXOffset[i]
//						, staffY-20+keyFlatYOffset[i]
//						, this);
//				}	
//			}
//		}
//
//		if (currentClef == -1)	{		// Bass Clef
//			if (currentkey > 0)		// Sharps
//			{
//				for (int i=0 ; i < (Math.abs(currentkey)) ; i++)
//				{
//					g.drawImage (muSharp.getImage()
//						, staffX + keySharpXOffset[i]+5
//						, staffY-20+keySharpYOffset[i]+16
//						, this);
//				}
//			}
//			else if (currentkey < 0)	// Flats
//			{
//				for (int i=0 ; i < (Math.abs(currentkey)) ; i++)
//				{
//					g.drawImage (muFlat.getImage()
//						, staffX + keyFlatXOffset[i]+5
//						, staffY-20+keyFlatYOffset[i]+16
//						, this);
//				}
//			}
//		}

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
					if ((noteheadCol[i] == 5) 
					&& (Math.abs(currIntervalDistanceBeingTested) > 1) 
					&& (exerciseMode == 'I'))
					{
						drawNotehead (g, 2, noteheadRow[i], 'F' );
					}
					else if ((noteheadCol[i] == 5) 
					&& (Math.abs(currIntervalDistanceBeingTested) == 1)
					&& (exerciseMode == 'I'))
					{
						drawNotehead (g, 3, noteheadRow[i], 'F' );
					}
					else if ((noteheadCol[i] == 5) 
					&& (Math.abs(currIntervalDistanceBeingTested) == 0)
					&& (exerciseMode == 'I'))
					{
						drawNotehead (g, 4, noteheadRow[i], 'F' );
					}
					else
					{
						drawNotehead (g, noteheadCol[i], noteheadRow[i], 'F' );
					}
				}
				else	{
					if ( examMode )	{
						drawNotehead (g, noteheadCol[i], noteheadRow[i], 'F' );
					}
					else	{
						drawNotehead (g, noteheadCol[i], noteheadRow[i], 'R' );
					}
				}
				if ((noteheadCol[i] == 5) 
				&& (Math.abs(currIntervalDistanceBeingTested) > 1)
				&& (exerciseMode == 'I') )
				{
					drawAccidental (g, 2, noteheadRow[i], noteheadAccid[i]);
				}
				else if ((noteheadCol[i] == 5) 
				&& (Math.abs(currIntervalDistanceBeingTested) == 1)
 				&& (exerciseMode == 'I') )
				{
					drawAccidental (g, 2, noteheadRow[i], noteheadAccid[i]);
				}
				else if ((noteheadCol[i] == 5) 
				&& (Math.abs(currIntervalDistanceBeingTested) == 0)
				&& (exerciseMode == 'I') )
				{
					drawAccidental (g, 4, noteheadRow[i], noteheadAccid[i]);
				}
				else if ((noteheadCol[i] == 5) 
				&& (Math.abs(currIntervalDistanceBeingTested) == 0)
				&& (exerciseMode == 'I') )
				{
					drawAccidental (g, 2, noteheadRow[i], noteheadAccid[i]);
				}
				else if (noteheadCol[i] == 4) 
				{
					drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i]);
				}
				else if (exerciseMode == 'I') 
				{
					drawAccidental (g, 1, noteheadRow[i], noteheadAccid[i]);
				}
				else
				{
					drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i]);
				}

			}
		} // for


		// Draw Dots for positions where user will enter notes
		if (exerciseMode == 'C') 
		{
			g.drawImage (muDot.getImage()
					, staffX + staffNoteXPos[4] + 7, staffY + staffNoteYPos[0] - 25, this );
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

}
//------------------------------------------------------------------------------
// END OF CLASS UdmtMajorScaleDrill
//------------------------------------------------------------------------------

