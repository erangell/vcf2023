//------------------------------------------------------------------------------
// UdmtTriadID Applet -  Triad Identification Drill and Exam
//------------------------------------------------------------------------------
// PARAMS:
//
// clefParm		=	The type of scale to select degrees from:
//					Values: 	TREBLE
//								BASS
//								BOTH
//					REQUIRED
//
// scaleTypeParm	=	Scale type being tested:
//					Values:	MAJOR
//						NATURAL_MIN
//						HARMONIC_MIN
//						MELODIC_MIN
//					REQUIRED
//
// examMode			=	Whether student is practicing or taking exam.
//								Values:	DRILL   (Default Value)
//									EXAM
//								OPTIONAL
//
// nextURL			=	The URL to invoke when student either:
//									1. Clicks TAKE EXAM when in DRILL mode.
//									2. Passes the exam when in EXAM mode.
//								REQUIRED
//
// drillURL			=	The URL to invoke when the student fails the exam when in EXAM mode.
//								REQUIRED IF examMode = EXAM
//
// examNumQues	=	The number of questions to administer in EXAM mode.
//								OPTIONAL. ONLY USED FOR EXAM MODE. DEFAULT=10
//
// examNumReqd	=	The number of questions required to pass the exam.
//								OPTIONAL. ONLY USED FOR EXAM MODE. DEFAULT=9
//
// suppressMIDI	=	'YES'=Suppress all MIDI logic (used for browsers that don't
//			have a version of Java that supports MIDI playback)
//								OPTIONAL: DEFAULT=NO (No suppression)
//
// drillExitURL	=	The URL to invoke when the student presses the EXIT button
//                      in Drill mode.
//								REQUIRED IF examMode = DRILL
//
//------------------------------------------------------------------------------

/*
Modification History:
09/13/2008 - cloned from piano kbd id
*/

//------------------------------------------------------------------------------

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtTriadID extends Applet implements ActionListener, ItemListener, MouseListener, MouseMotionListener
{
	private boolean _debug = false;	

	private boolean _running = false;	//used for Runnable interface
	private boolean _gameInProgress = false;
	private boolean _playMidiNotes = true;	

	private Thread _looper;

	private String udmtAppletName			=	"UdmtTriadID";
	private String udmtAppletVersion	=	"0.06"	;

	private	UdmtURLBuilder	urlBuilder;
	
	private UdmtKeySelect keySelect;

	private String parmClefType		=	"TREBLE";		//	default = treble clef only
	
	private String parmDebug 	=	"";
	private String parmExamMode 	=	"";
	private String parmContent		=	"";

	private String parmNextURL 		=	"";
	private URL nextURL;

	private String parmDrillURL 	=	"";
	private URL drillURL;

	private String parmDrillExitURL =	"";
	private URL drillExitURL;

	private boolean showErrorScreen		=	false;
	private String txtError						=	"";

	private UdmtMidiOutQueue	midiOut;
//	private UdmtMidiInQueue		midiIn;

	private Rectangle	rectScreen;
	private Image			imageScreen;

	private Image imgAllSymbols;
	private Image imgPiano;
	private Image imgTurtle;
	private Image imgRabbit;

	private UdmtMusicSymbol muStaff, muTrebleClef, muBassClef, muDot, muBar, muLeger;
	private UdmtMusicSymbol muNoteheadFilled, muNoteheadUnfilled, muNoteheadRed, muRtDblBar;
	private UdmtMusicSymbol muSharp, muFlat, muNatural, muDblSharp, muDblFlat;

	final	private	int	STAFF_WIDTH	=	320;
	
	private int staffX;
	private	int staffY;

	private int gClefXOffset	=	0;
	private int gClefYOffset 	=	-20;
	private int fClefXOffset 	=	0;
	private int fClefYOffset 	=	-17;

	private int keySharpXOffset[]	=	{38,51,64,77,90,103,116};
	private int keySharpYOffset[] =	{1,25,-5,18,41,6,34};
	private int keyFlatXOffset[] 	=	{38,51,64,77,90,103,116};
	private int keyFlatYOffset[] 	=	{34,8,41,18,50,25,57};


	private int staffNoteYPos[]		=	{-17,-9,-1,7,15,23,31,39,47,55,63,71,79,87,95,103,111};
	private	final int	YPOS_BASE		=	-17;
	private final int YPOS_GAP		=	8;
	
	private int staffNoteXPos[] 	=	{140,167,194,221,248,275,302,329,356,383,410,437,464,491,518,545};
	private final int	XPOS_BASE		=	140;
	private final int	XPOS_GAP		=	27;
	
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

	private int MidiTreble[]	=	{84,83,81,79,77,76,74,72,71,69,67,65,64,62,60,59,57};
	private int MidiBass[]		=	{64,62,60,59,57,55,53,52,50,48,47,45,43,41,40,38,36};
	private int ScaleBeingTested[];

	//----------------------------------------------------------------------
	
	private int	gotThisQuesCorrect;

	private int uData[];
	private int uAcc[];
	private int uMIDI;
	private int progCh	=	0; 		// imnt# will be param.  0=general midi grand piano

	//----------------------------------------------------------------------
	
	private int		currentClef;

	private PopupMenu popup;
	private final int	MENU_HOR_OFFSET		=	18;
	private final int	MENU_VER_OFFSET		=	54;

	private int saveX, saveY;
	private int xLeft, yTop, xRight, yBottom; 		// area in which cursor becomes active for entering notes

	private int startcursorrow, startcursorcol;
	private int cursorrow, cursorcol;
	private int cursorvisible			=	0 ;
	private int savecursorrow, savecursorcol ;
	private int savecursorx, savecursory;



	private int		numNoteheads =	0;			//	number of noteheads currently in the array
	private int 	noteheadInUse[];				//	whether this slot in the array is in use or not
	private int 	noteheadRow[];					//	which of the rows the notehead is in
	private int 	noteheadCol[];					//	which of the columns the notehead is in 
	private char	noteheadAccid[];				//	chromatic modifier for the notehead
									//	F=bb f=b s=# S=x"
	private int 	noteheadColor[];				//	0=black  1=red
	private int	noteheadAccidNudge[];				//	adjustment of X Coordinate of accidental

	private Button checkButton;
	private Button nextButton;
	private Button playTriadButton;

	private Button playNotesButton,	 takeExamButton,	showCorrButton;
	private Button nextLessonButton, exitButton;
	
	private	Button Roman1;
	private	Button Roman2;
	private	Button Roman3;
	private	Button Roman4;
	private	Button Roman5;
	private	Button Roman6;
	private	Button Roman7;

//	private	Button Roman1m;
//	private	Button Roman2m;
//	private	Button Roman3m;
//	private	Button Roman4m;
//	private	Button Roman5m;
//	private	Button Roman6m;
//	private	Button Roman7m;

	private	Button btnAugTriad;
	private	Button btnDimTriad;
	private	Button btnMajTriad;
	private	Button btnMinTriad;

	private final int	PITCH_BOX_Y1			=	270;
	private final int	PITCH_BOX_Y2			=	290;
	private final int	PITCH_BOX_X1			=	60;
	private final int PITCH_BOX_WIDTH		=	 35;
	private final int PITCH_BOX_SEP		=	 40;
	private final int	PITCH_BOX_HEIGHT	=	 25;

	private final int	PIANO_KBD_Y_OFFSET	=	210;
	
	private int currQues; // # of current question being presented

	private	int	currentQuesOctaveMidi;

	private	int	currentAnswer;
	private	int	currentAnswerAccid;
	private	int	alternateAnswer;
	private	int	alternateAnswerAccid;

	private int csnumeral = 0;
	private	int csacc = 0;
	private	int csqual = 0;
	private String currentStudentRomanNumeral = "";
	private String currentStudentChordQuality = "";
	private final int X_STUDENT_CHORD = 480;
	private final int Y_STUDENT_CHORD = 305;

	private int numTimesChecked 			= 	0;
	private int numQuesPresented 			= 	1;
	private int numQuesCorrect1stTime	= 	0;

	private String strDrillMode;
	private String strExamMode;

	private String strInstruct	=	"";
	private String strInstruct1	=	"Identify the given triad.";
	private String strInstruct2 =	"";
	private String strInstruct3	=	"";

	private String strScore1 	= 	"Score: ";
	private int nScore1 			= 	0;
	private String strScore2 	= 	" out of ";
	private int nScore2 			= 	0;
	private String strScore3 	= 	" = ";
	private int nScorePercent	= 	0;
	private String strScore4 	= 	"%";
	private String strScoreDisp;

	private String strCorrect 			=	"Correct";
	private String strIncorrect 		=	"Incorrect.";
	private String strIncorrectExam	=	"Incorrect.";

	private String strMaster1 			= "Congratulations.  You have provided 9 out of 10";
	private String strMaster2 			= "correct answers and may  move on if you wish.";
	private String strFeedback 			= "";
	private String strExamFeedback1	= "";
	private String strExamFeedback2	= "";

	private boolean	examMode;
	private int			contentMode;

	private Choice	imntCombo;
	private int			userInstrument	=	0 ;

	private Choice	degreeAnswer;

	//
	// DEFAULT VALUES WHICH MAY BE OVERRIDEN BY PARAMS:
	//
	private String	parmExamNumQues;
	private int			num_exam_ques			=	10;
	private String	parmExamNumReqd;
	private int			num_reqd_to_pass	=	 9;
	private String	parmSuppressMIDI	=	"NO";
	private boolean	suppressingMIDI		=	false;

	private int trebleOrBass = 1;

	private int minorType = 0;

	private final int	NO_ANSWER_YET		=	(-1);
	private final int	ANSWER_GIVEN		=	(+1);
	
	private final int	BUTTON_WIDTH		=	110;
	private final int	BUTTON_HEIGHT		=	 30;
	private	final int	ROW1_Y					=	325;
	private	final	int	ROW2_Y					=	365;
	
	private	final	int	DEGREE_X				=	155;
	private final int	DEGREE_Y				=	305;
	
	private final int	TREBLE_CLEF			=	(+1);
	private final int	BOTH_CLEFS			=	0;
	private final int	BASS_CLEF				=	(-1);
	
	private final	int	BLACK_NOTE			=	 0;
	private final int	RED_NOTE				=	 1;

	private final int	MAX_NOTES				=	 30;
	
	private final	int	Q_DECK_MAX =	30;				//	maximum # of questons in deck	
	private				int	qClefs[];								//	clef used in question
	private				int	qNote[];								//	display note of question
	private				int	qAnswer[];							//	correct answer;
	private				int	qKeySig[];
	private				int	qScaleTone[];
	private				int	qTriadQuality[];

	private				int	rootMidi;
	private				int	thirdMidi;
	private				int	fifthMidi;

	private	final	int	SHUFFLE_TIMES	=	5;			//	# of times to shuffle question deck
	
	private	int	curQ	=	(-1);									//	number of current question
	
	private final boolean	NEVER_ACTIVE	=	false;
	
	private final	int EXAM_FB_LINE_1	=	270;			//	y-coord for exam feedback line #1
	private final int EXAM_FB_LINE_2	=	290;			//	y-coord for exam feedback line #2
	
	private final int	MAJOR			=	0;
	private final int	NATURAL_MINOR		=	1;
	private final int	HARMONIC_MINOR		=	2;
	private final int	MELODIC_MINOR		=	3;
	
	private final	int	FIRST_SPACE			=	5;
	
	private final int	NO_SELECTION		=	0;
	
	private final int	NO_MODIFIER			=	0;

	private final int	STARTING_NOTE_SLOT				= 13;
	private final int	ENDING_NOTE_SLOT				= 1;
	
	private String treblePitchNames = "CBAGFEDCBAGFEDCBA";
	private String bassPitchNames		= "EDCBAGFEDCBAGFEDC";

	private int[] treblePitchMidis = {84,83,81,79,77,76,74,72,71,69,67,65,64,62,60,59,57};
	private int[] bassPitchMidis   = {64,62,60,59,57,55,53,52,50,48,47,45,43,41,40,38,36};
	
	//UdmtStopWatch	theStopWatch;
	
	private final int	TEST_PASSED		=	1;
	private final int	TEST_FAILED		=	2;
	private final int	TEST_CONTINUE	=	3;


	private final int	STAFF_COLUMN_ROOT =	1;
	private final int	STAFF_COLUMN_THIRD =	5;
	private final int	STAFF_COLUMN_FIFTH =	9;
	private final int	STAFF_COLUMN_TRIAD =	13;

	private int currentKeySig = 0;	

	// 0=maj 1=min 2=aug 3=dim
	private int[] useThisScaleToneQuality   = {0,1,1,0,0,1,3};
	private int[] majorScaleToneQual = {0,1,1,0,0,1,3};
	private int[] naturalMinorScaleToneQual = {1,3,0,1,1,0,0};
	private int[] harmonicMinorScaleToneQual = {1,3,2,1,0,0,3};
	private int[] melodicMinorScaleToneQual = {1,1,2,0,0,3,3};

	private int[] useThisScale   = {0,2,4,5,7,9,11};
	private int[] majorScaleHalfSteps = {0,2,4,5,7,9,11};
	private int[] naturalMinorScaleHalfSteps = {0,2,3,5,7,8,10};
	private int[] harmonicMinorScaleHalfSteps = {0,2,3,5,7,8,11};
	private int[] melodicMinorScaleHalfSteps = {0,2,3,5,7,9,11};

	private boolean _ShowKeysig = false;
	private String _KeySigNameDisp = "";

	private int NudgeLeftRoot = -20;
	private int NudgeLeftThird = -10;
	private int NudgeLeftFifth = 0;

//------------------------------------------------------------------------------

	public void init()
	{
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		strDrillMode	="Triad Identification: ";
		strExamMode		="Triad Identification Exam: ";

		urlBuilder		= new UdmtURLBuilder();

		keySelect		= new UdmtKeySelect();
		keySelect.initKeySelect();

		//theStopWatch	=	new UdmtStopWatch();

		parmDebug = getParameter("malhcs");
		if ( parmDebug != null ) {
			System.out.println("parmDebug="+parmDebug);
			_debug	 = ( parmDebug.equals("DRAKCAP") );
		}

		//
		//Retrieve Parameter:  clefType
		//
		parmClefType = getParameter("clefType");
		if ( parmClefType == null )	{
			parmClefType = "TREBLE";
		}
		//System.out.println ("parmClefType="+parmClefType);
		//
		//Retrieve Parameter:	 scaleType
		//
		parmContent	=	getParameter("scaleType");
		if ( parmContent.equals("Major") ) {
			contentMode = MAJOR;
			for (int i=0; i < 7; i++)
			{
				useThisScaleToneQuality[i] = majorScaleToneQual[i];
				useThisScale[i] = majorScaleHalfSteps[i];
			}
		}
		else if ( parmContent.equals("Natural Minor") ) {
			contentMode = NATURAL_MINOR;
			for (int i=0; i < 7; i++)
			{
				useThisScaleToneQuality[i] = naturalMinorScaleToneQual[i];
				useThisScale[i] = naturalMinorScaleHalfSteps[i];
			}
		}
		else if ( parmContent.equals("Harmonic Minor") ) {
			contentMode = HARMONIC_MINOR;
			for (int i=0; i < 7; i++)
			{
				useThisScaleToneQuality[i] = harmonicMinorScaleToneQual[i];
				useThisScale[i] = harmonicMinorScaleHalfSteps[i];
			}
		}
		else if ( parmContent.equals("Melodic Minor") ) {
			contentMode = MELODIC_MINOR;
			for (int i=0; i < 7; i++)
			{
				useThisScaleToneQuality[i] = melodicMinorScaleToneQual[i];
				useThisScale[i] = melodicMinorScaleHalfSteps[i];
			}
		}
		//System.out.println ("parmContent="+parmContent);
		strDrillMode = strDrillMode + parmContent;
		strExamMode = strExamMode + parmContent;

		//
		//Retrieve Parameter:  examMode
		//
		parmExamMode = getParameter("examMode");
		examMode		 = parmExamMode.equals("EXAM");
		//
		//Retrieve Parameter:  nextURL
		//
		parmNextURL = getParameter("nextURL");
		//System.out.println ("ORIGINAL parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY

		if ( parmNextURL == null )	{
			showErrorScreen = true;
			txtError = "Parameter nextURL is required";
		}
		else	{

 			//02-05-2005: Use URLBUILDER only if not in exam mode
 			if (examMode)
 			{
 				UdmtExamKey objEk = new UdmtExamKey();
  				String sExamKey = objEk.getExamKey("TriadID"+parmContent+"Pass");
  				parmNextURL = parmNextURL + "?r="+sExamKey;
  				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY
 			}
 			else
 			{
 				parmNextURL = urlBuilder.buildURL (this.getCodeBase() , parmNextURL);
 			}

			try	{
				nextURL = new URL(parmNextURL);
			}
			catch ( MalformedURLException e )	{
				showErrorScreen = true;
				txtError = "Malformed URL Exception when creating URL from parmNextURL";
				e.printStackTrace();
			}
			catch ( Exception e )	{
				showErrorScreen = true;
				txtError = "Exception when creating URL from parmNextURL";
				e.printStackTrace();
			}
		}
		
		if ( examMode )	{
			parmDrillURL = getParameter("drillURL");

			if ( parmDrillURL == null )	{
				showErrorScreen	= true;
				txtError				= "Parameter drillURL is required for Exam Mode";
			}
			else	{
	 			//02-05-2005: Use URLBUILDER only if not in exam mode
 				if (examMode)
 				{
 					UdmtExamKey objEk = new UdmtExamKey();
  					String sExamKey = objEk.getExamKey("TriadID"+parmContent+"Fail");
	  				parmDrillURL = parmDrillURL + "?r="+sExamKey;
  					//System.out.println ("parmDrillURL="+parmDrillURL); //FOR DEBUGGING ONLY
	 			}
 				else
 				{
					parmDrillURL = urlBuilder.buildURL(this.getCodeBase(), parmDrillURL);
	 			}


				try	{
					drillURL = new URL(parmDrillURL);
				}
				catch ( MalformedURLException e )	{
					showErrorScreen = true;
					txtError = "Malformed URL Exception when creating URL from parmDrillURL";
					e.printStackTrace();
				}
				catch ( Exception e )	{
					showErrorScreen = true;
					txtError = "Exception when creating URL from parmDrillURL";
					e.printStackTrace();
				}
			}
		}
		else	{
			parmDrillExitURL = getParameter("drillExitURL");

			if (parmDrillExitURL == null )	{
				showErrorScreen = true;
				txtError = "Parameter drillExitURL is required for Drill Mode";
			}
			else	{
				parmDrillExitURL = urlBuilder.buildURL(this.getCodeBase(), parmDrillExitURL);
				try	{
					drillExitURL = new URL(parmDrillExitURL);
				}
				catch ( MalformedURLException e )	{
					showErrorScreen = true;
					txtError = "Malformed URL Exception when creating URL from parmDrillExitURL";
					e.printStackTrace();
				}
				catch ( Exception e )	{
					showErrorScreen = true;
					txtError = "Exception when creating URL from parmDrillExitURL";
					e.printStackTrace();
				}
			}
		}
		//
		//	Retrieve param:		 examNumQues	
		//
		if ( examMode )	{
			parmExamNumQues = getParameter("examNumQues");
			if ( parmExamNumQues != null)	{
				num_exam_ques = Integer.parseInt(parmExamNumQues);
			}
		}
		//	
		//Retrieve Parameter:  examNumReqd
		//
		if ( examMode )	{
			parmExamNumReqd = getParameter("examNumReqd");
			if ( parmExamNumReqd != null )	{
				num_reqd_to_pass = Integer.parseInt(parmExamNumReqd);
			}
		}
		//
		//Retrieve Parameter:  suppressMIDI
		//
		parmSuppressMIDI = getParameter("suppressMIDI");
		if ( parmSuppressMIDI != null ) {
			suppressingMIDI	 = ( parmSuppressMIDI.equals("YES") );
		}
		else {
			suppressingMIDI = false;
		}

		// Initialize MIDI by calling the constructor of UdmtMidiOutQueue
		if (!suppressingMIDI)	{
			System.out.println("Attempting to start MIDI...");
			try	{
				midiOut = new UdmtMidiOutQueue();
			}
			catch ( Exception e )	{
				this.showStatus("ERROR STARTING MIDI");
				System.out.println("ERROR: Exception when creating UdmtMidiOutQueue:");
				e.printStackTrace();
			}
			System.out.println("MIDI initialized successfully.");
		}
		//
		// Load graphics file of music symbols
		//
		try 	{
			MediaTracker mt	= new MediaTracker(this);
			URL url					= getCodeBase();
			imgAllSymbols		= getImage(url, "symbols-48pt-tr.gif");
			//imgPiano = getImage (url, "bigpiano.gif");
			//imgTurtle = getImage(url, "turtle.gif");
			//imgRabbit = getImage(url, "rabbit.gif");

			mt.addImage (imgAllSymbols, 1);
			//mt.addImage (imgPiano, 2);
			//mt.addImage(imgTurtle, 3);
			//mt.addImage(imgRabbit, 4);
			mt.waitForAll();
			SetUpSymbolObjects();
		}
		catch ( Exception e )	{
			this.showStatus("ERROR LOADING GRAPHICS");
			showErrorScreen = true;
			txtError = "Exception when loading symbols-48pt-tr.gif";
			System.out.println("ERROR: Exception when loading symbols-48pt-tr.gif:");
			e.printStackTrace();
		}


		//Removed sync of MidiOut to piano keyboard for Big Piano
		//Attach Piano Display to Midi Out queue
		//if (!suppressingMIDI)
		//{
	    	//	midiOut.udmtPianoKeyboard = muPiano;
		//}


		noteheadInUse = new int[MAX_NOTES];
		noteheadRow		= new int[MAX_NOTES];
		noteheadCol		= new int[MAX_NOTES];
		noteheadAccid	= new char[MAX_NOTES];
		noteheadColor	= new int[MAX_NOTES];
		noteheadAccidNudge	= new int[MAX_NOTES];


		uData	= new int[8];
		uAcc	= new int[8];

		rectScreen = getBounds();
		staffX = 5;
		staffY = 130;

		qClefs	=	new int[ Q_DECK_MAX ];
		qNote		=	new int[ Q_DECK_MAX ];
		qAnswer	= new int[ Q_DECK_MAX ];
		qKeySig = new int[ Q_DECK_MAX ];
		qScaleTone = new int[ Q_DECK_MAX ];
		qTriadQuality = new int[ Q_DECK_MAX ];

		GenerateQuestionDeck();
		ShuffleQuestionDeck(SHUFFLE_TIMES, Q_DECK_MAX);
		
		setLayout(null);

// set up button positions

		int btncol0 = 1;
		int btncol1 = 60;
		int btncol2 = 180;
		int btncol3 = 300;
		int btncol4 = 420;
		int btncol5 = 587;

//	CHECK button setup
//
		checkButton = new Button("CHECK");
        checkButton.addActionListener(this);
		checkButton.setEnabled(false);
		checkButton.setBounds (btncol2,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(checkButton);
//
//	NEXT button setup
//
    nextButton = new Button("NEXT");
    nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		nextButton.setBounds (btncol3,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(nextButton);
//
//	PLAY TRIAD button setup
//
		playTriadButton = new Button("PLAY TRIAD");
		playTriadButton.addActionListener(this);
		playTriadButton.setBounds(btncol1,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		if (suppressingMIDI || examMode)
		{
			playTriadButton.setEnabled(false);
		}
		else
		{
			playTriadButton.setEnabled(true);
		}
		add(playTriadButton);
//
//	PLAY NOTES Button setup
//
		playNotesButton = new Button("PLAY NOTES");
		playNotesButton.addActionListener(this);
		playNotesButton.setBounds (btncol2,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		if (suppressingMIDI || examMode)
		{
			playNotesButton.setEnabled(false);
		}
		else
		{
			playNotesButton.setEnabled(true);
		}
		add(playNotesButton);
//
//	SHOW CORRECT button setup
//
		showCorrButton = new Button("SHOW CORRECT");
		showCorrButton.setEnabled(false);        
		showCorrButton.addActionListener(this);
		showCorrButton.setBounds(btncol3,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(showCorrButton);
//
//	TAKE EXAM button setup
//
		takeExamButton = new Button("TAKE EXAM");
		takeExamButton.addActionListener(this);
		takeExamButton.setBounds ( btncol4,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(takeExamButton);
		if ( examMode ) {
			takeExamButton.setEnabled(NEVER_ACTIVE);
		}
//
//	NEXT LESSON button setup
//
		if ( examMode )	{
			nextLessonButton = new Button("NEXT LESSON");
			nextLessonButton.addActionListener(this);
			nextLessonButton.setVisible(false);
			nextLessonButton.setBounds(btncol4,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
			add(nextLessonButton);
		}
//
//	EXIT button setup
//
		exitButton = new Button("EXIT");
		exitButton.addActionListener(this);	
		exitButton.setVisible(true);
		exitButton.setBounds(btncol4,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(exitButton);
//
//	INSTRUMENT button setup
//
		imntCombo = new Choice();
		for (int i = 0 ; i <= 127 ; i++)	{
			imntCombo.add( midiOut.GMImntList[i] );
		} 
		imntCombo.setBounds(btncol1,ROW1_Y+1,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(imntCombo);
		imntCombo.addItemListener(this);
		if ( (examMode) || (suppressingMIDI) )	{
			imntCombo.setEnabled(false);
		}
//
// Pitch name response buttons
//
		Roman1 = new Button("1");
		Roman1.addActionListener(this);
		Roman1.setEnabled(true);
		Roman1.setBounds(PITCH_BOX_X1+0*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman1);

		Roman2 = new Button("2");
		Roman2.addActionListener(this);
		Roman2.setEnabled(true);
		Roman2.setBounds(PITCH_BOX_X1+1*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman2);

		Roman3 = new Button("3");
		Roman3.addActionListener(this);
		Roman3.setEnabled(true);
		Roman3.setBounds(PITCH_BOX_X1+2*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman3);

		Roman4 = new Button("4");
		Roman4.addActionListener(this);
		Roman4.setEnabled(true);
		Roman4.setBounds(PITCH_BOX_X1+3*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman4);

		Roman5 = new Button("5");
		Roman5.addActionListener(this);
		Roman5.setEnabled(true);
		Roman5.setBounds(PITCH_BOX_X1+4*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman5);

		Roman6 = new Button("6");
		Roman6.addActionListener(this);
		Roman6.setEnabled(true);
		Roman6.setBounds(PITCH_BOX_X1+5*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman6);

		Roman7 = new Button("7");
		Roman7.addActionListener(this);
		Roman7.setEnabled(true);
		Roman7.setBounds(PITCH_BOX_X1+6*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(Roman7);

		btnMajTriad = new Button("M");
		btnMajTriad.addActionListener(this);
		btnMajTriad.setEnabled(true);
		btnMajTriad.setBounds(PITCH_BOX_X1+8*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(btnMajTriad);

		btnAugTriad = new Button("+");
		btnAugTriad.addActionListener(this);
		btnAugTriad.setEnabled(true);
		btnAugTriad.setBounds(PITCH_BOX_X1+9*PITCH_BOX_SEP, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(btnAugTriad);

//		Roman1m = new Button("i");
//		Roman1m.addActionListener(this);
//		Roman1m.setEnabled(true);
//		Roman1m.setBounds(PITCH_BOX_X1+0*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman1m);
//
//		Roman2m = new Button("ii");
//		Roman2m.addActionListener(this);
//		Roman2m.setEnabled(true);
//		Roman2m.setBounds(PITCH_BOX_X1+1*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman2m);
//
//		Roman3m = new Button("iii");
//		Roman3m.addActionListener(this);
//		Roman3m.setEnabled(true);
//		Roman3m.setBounds(PITCH_BOX_X1+2*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman3m);
//
//		Roman4m = new Button("iv");
//		Roman4m.addActionListener(this);
//		Roman4m.setEnabled(true);
//		Roman4m.setBounds(PITCH_BOX_X1+3*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman4m);
//
//		Roman5m = new Button("v");
//		Roman5m.addActionListener(this);
//		Roman5m.setEnabled(true);
//		Roman5m.setBounds(PITCH_BOX_X1+4*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman5m);
//
//		Roman6m = new Button("vi");
//		Roman6m.addActionListener(this);
//		Roman6m.setEnabled(true);
//		Roman6m.setBounds(PITCH_BOX_X1+5*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman6m);
//
//		Roman7m = new Button("vii");
//		Roman7m.addActionListener(this);
//		Roman7m.setEnabled(true);
//		Roman7m.setBounds(PITCH_BOX_X1+6*PITCH_BOX_SEP, PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
//		add(Roman7m);

		btnMinTriad = new Button("m");
		btnMinTriad.addActionListener(this);
		btnMinTriad.setEnabled(true);
		btnMinTriad.setBounds(PITCH_BOX_X1+8*PITCH_BOX_SEP,  PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(btnMinTriad);

		btnDimTriad = new Button("o");
		btnDimTriad.addActionListener(this);
		btnDimTriad.setEnabled(true);
		btnDimTriad.setBounds(PITCH_BOX_X1+9*PITCH_BOX_SEP,  PITCH_BOX_Y2, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(btnDimTriad);


		addMouseListener(this);


//		
// Present a question
//		
		currQues = 0;
		PresentCurrentQuestion(currQues);
	}

//------------------------------------------------------------------------------

	public void SetUpSymbolObjects()
	{
		Image imgTemp;

		muStaff	= new UdmtMusicSymbol();
		muStaff.CropImage(imgAllSymbols, 15, 200, 85, 280 );

		muTrebleClef	= new UdmtMusicSymbol();
		muTrebleClef.CropImage(imgAllSymbols, 110, 0, 155, 120);

		muBassClef	= new UdmtMusicSymbol();
		muBassClef.CropImage(imgAllSymbols, 260, 0, 310, 110);

		muDot	= new UdmtMusicSymbol();
		muDot.CropImage(imgAllSymbols, 450, 450, 457, 457);

		muNoteheadFilled	= new UdmtMusicSymbol();
		muNoteheadFilled.CropImage(imgAllSymbols, 25, 80, 50, 105 );

		muNoteheadUnfilled	= new UdmtMusicSymbol();
		muNoteheadUnfilled.CropImage( imgAllSymbols, 63, 80, 85, 105 );

		muBar	= new UdmtMusicSymbol();
		muBar.CropImage(imgAllSymbols, 345, 200, 350, 280);	

		muRtDblBar = new UdmtMusicSymbol();
		muRtDblBar.CropImage(imgAllSymbols, 290,200,310,280);	

		muSharp	= new UdmtMusicSymbol();
		muSharp.CropImage( imgAllSymbols, 490,425,510,475);	

		muFlat	= new UdmtMusicSymbol();
		muFlat.CropImage( imgAllSymbols, 550,425,575,475);	

		muNatural	= new UdmtMusicSymbol();
		muNatural.CropImage(imgAllSymbols, 615,425,630,475);	

		muDblSharp	= new UdmtMusicSymbol();
		muDblSharp.CropImage(imgAllSymbols, 655,425,677,475);	

		muDblFlat	= new UdmtMusicSymbol();
		muDblFlat.CropImage(imgAllSymbols,712,425,740,475);	

		muLeger	= new UdmtMusicSymbol();
		muLeger.CropImage(imgAllSymbols, 388, 50, 423, 205);

		// create red notehead by cloning notehead image and changing pixel colors
		
		try 	{
			int pixels[]		= new int [25*25];
			int redpixels[]	= new int [25*25];
 
			PixelGrabber pg = new PixelGrabber(muNoteheadFilled.getImage(), 0
																						, 0, 25, 25, pixels, 0, 25) ;
			pg.grabPixels();

			if ( (pg.status() & ImageObserver.ALLBITS) != 0)	{
				for (int y=0 ; y<=24 ; y++)				{
					for (int x=0 ; x <=24 ; x++)			{
						if (pixels[y*25+x] > 0)			{
							redpixels[y*25+x] = pixels[y*25+x];
						}
						else {
							redpixels[y*25+x] = Color.red.getRGB();
						}
					}		
				}

				imgTemp = createImage(new MemoryImageSource( 25, 25, redpixels, 0, 25));
		
				muNoteheadRed = new UdmtMusicSymbol();
				muNoteheadRed.CropImage( imgTemp, 0, 0, 25, 25 );
			}
		}
		catch ( Exception e )	{
			e.printStackTrace();
		}
	}

//------------------------------------------------------------------------------

	private	char accidToChar(int a)
	{
		char		ac				=	' ';
		String	accString = "Ff sSn";
		
		try {
			ac = accString.charAt(a + 2);
		}
		catch (Exception e) {
			//
		}

		return( ac );	
	}


//------------------------------------------------------------------------------
//	Convert a row number (staff position) to its pitch class name
//------------------------------------------------------------------------------
	private char RowToPitchChar(int aNote, int theClef)
	{
		int		theRow;
		char	pChar;

		theRow = GetNoteRepRow( aNote );
		if ( theClef == TREBLE_CLEF ) {
			pChar = treblePitchNames.charAt(theRow); 
		}
		else {		 		
			pChar = bassPitchNames.charAt(theRow);
		}
		
		return( pChar );
	}



//------------------------------------------------------------------------------
//	Convert a row number (staff position) to its midi note number
//------------------------------------------------------------------------------
	private int RowToMidiNoteNum(int theRow, int theClef, int theKeySig)
	{
		int pMidi;

		//System.out.println("RowToMidiNoteNum: theRow="+theRow+" theClef="+theClef+" theKeySig="+theKeySig);

		UdmtKeySignature uks = new UdmtKeySignature();
		uks.setKeySignature(currentKeySig);

		if ( theClef == TREBLE_CLEF ) {
			pMidi = treblePitchMidis[theRow] - uks.getKeySigAccid (theClef, theRow ); 
		}
		else {		 		
			pMidi = bassPitchMidis[theRow] - uks.getKeySigAccid (theClef, theRow );
		}

		//System.out.println("RowToMidiNoteNum: pMidi="+pMidi);
		
		return( pMidi );
	}


//------------------------------------------------------------------------------
//	Convert a midi note number to a staff position
//------------------------------------------------------------------------------
	private int MidiNoteNumToRowAcClef(int midinote, int clef, int keysig, int forceline)
	{
	    if (_debug)
	    {
		System.out.println("MidiNoteNumToRowAcClef: midinote="+midinote+" clef="+clef+" keysig="+keysig+" forceline="+forceline);
	    }

	    int ac = 0;
	    if (clef == TREBLE_CLEF)
	    {
		ac = midinote - treblePitchMidis[forceline];
   	    }
	    else
	    {
		ac = midinote - bassPitchMidis[forceline];
	    }

	    if (_debug)
	    {
		System.out.println("MidiNoteNumToRowAcClef: Initial ac="+ac);
	    }

	    UdmtKeySignature uks = new UdmtKeySignature();
   	    uks.setKeySignature(keysig);

	    int ksacc = uks.getKeySigAccid (clef, forceline);
	    if (_debug)
	    {
		System.out.println("MidiNoteNumToRowAcClef: keysig accidental="+ksacc);
	    }

	    if (ac == (-1 * ksacc))
	    {
		//ac = 0;
	    }
	    else if (ksacc != 0)
	    {
		//if (ac == 0)
		//{
		//	ac = 3; // natural sign
		//}
	    }

	    if (_debug)
	    {
		System.out.println("MidiNoteNumToRowAcClef: final ac="+ac);
	    }
	    return (forceline + (ac+2) * 100);
	}

//------------------------------------------------------------------------------

	private int CheckTestCompletionStatus()
	{
		int retval = TEST_CONTINUE;
		if (_debug)
		{	
			System.out.println("numQuesPresented      = " + numQuesPresented);
			System.out.println("num_reqd_to_pass      = " + num_reqd_to_pass);
			System.out.println("numQuesCorrect1stTime = " + numQuesCorrect1stTime);
			System.out.println("num_exam_ques         = " + num_exam_ques);
		}

		if ( numQuesCorrect1stTime == num_reqd_to_pass ) {
			retval =  TEST_PASSED;
		}
		else
		{	if ( (numQuesPresented - numQuesCorrect1stTime) <= (num_exam_ques - num_reqd_to_pass) ) {
				retval = TEST_CONTINUE;
			}
			else {
				retval = TEST_FAILED;
			}
		}
		
		if (_debug)
		{
			System.out.println("retval="+retval);
		}
		return retval;
	}

//------------------------------------------------------------------------------

	private void DoJudging(int currentAnswer)
	{
		gotThisQuesCorrect=0;
	
		//System.out.println("qAnswer="+qAnswer[currQues]+" currentAnswer="+currentAnswer);
 
		if ( qAnswer[currQues] == currentAnswer ) {
					
			if ( numTimesChecked == 0)	{
				numQuesCorrect1stTime++;
				gotThisQuesCorrect = 1;
			}	

			strFeedback = strCorrect;
			
			nextButton.setEnabled(true);
	
			numTimesChecked++;
		}
		else {
			if ( examMode )	{
				strFeedback = strIncorrectExam;
				checkButton.setEnabled(false);
				nextButton.setEnabled(true);
			}
			else	{
				strFeedback = strIncorrect;
			}

			numTimesChecked++;

			if (numTimesChecked >= 2)	{
				if ( !examMode ) {
					showCorrButton.setEnabled(true);
				}
				nextButton.setEnabled(true);
			}

		}
	}

//------------------------------------------------------------------------------

	public void processNextButton()
	{
			currQues++;
			if ( currQues == Q_DECK_MAX ) {
				GenerateQuestionDeck();
				ShuffleQuestionDeck(SHUFFLE_TIMES, Q_DECK_MAX);
				currQues = 0;
			}

			numTimesChecked = 0;

			PresentCurrentQuestion(currQues);

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			if ( !examMode )	{
				showCorrButton.setEnabled(false);
			}
			strFeedback = "";
 			currentStudentRomanNumeral = ""; 
			currentStudentChordQuality = "";
			currentAnswer = 0;

			if ( examMode ) {
				switch ( CheckTestCompletionStatus() ) {
				case TEST_PASSED:
					this.getAppletContext().showDocument(nextURL);
					break;
					
				case TEST_FAILED:
					this.getAppletContext().showDocument(drillURL);
					break;
					
				case TEST_CONTINUE:
					//	Nothing special to need to be done
					break;
				}
			}
			numQuesPresented++;
			repaint();

	}

	public void actionPerformed(ActionEvent e)
	{
		char ac = '?';
		int uaccval=0;

		//System.out.println ("Menu Item: " + e.getActionCommand() );
		//System.out.println ("Clicked at: " + saveX + " , " + saveY);

		if (e.getActionCommand().equals ( "None" ) )
		{
			ac = ' ';
		}
		else if (e.getActionCommand().equals ( "Natural" ) )
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

		//
		//	Triad answer button Servicing
		//
		
		if ( e.getActionCommand().equals("m") ) {
			currentStudentChordQuality = "*";
			currentStudentRomanNumeral = currentStudentRomanNumeral.toLowerCase();
			csqual=1;
			repaint();
		}
		else if ( e.getActionCommand().equals("M") ) {
			currentStudentChordQuality = "*";
			currentStudentRomanNumeral = currentStudentRomanNumeral.toUpperCase();
			csqual=0;
			repaint();
		}
		else if ( e.getActionCommand().equals("+") ) {
			currentStudentChordQuality = "+";
			currentStudentRomanNumeral = currentStudentRomanNumeral.toUpperCase();
			csqual=2;
			repaint();
		} 
		else if ( e.getActionCommand().equals("o") ) {
			currentStudentChordQuality = "o";
			currentStudentRomanNumeral = currentStudentRomanNumeral.toLowerCase();
			csqual=3;
			repaint();
		}
		 
		else if ( e.getActionCommand().equals("1") ) {
			csnumeral = 1;
			csqual = 0;
			currentStudentRomanNumeral = "I";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("i") ) {
			csnumeral = -1;
			csqual = 1;
			currentStudentRomanNumeral = "i";
			currentStudentChordQuality = "*";
			repaint();
		}

		else if ( e.getActionCommand().equals("2") ) {
			csnumeral = 2;
			csqual = 0;
			currentStudentRomanNumeral = "II";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("ii") ) {
			csnumeral = -2;
			csqual = 1;
			currentStudentRomanNumeral = "ii";
			currentStudentChordQuality = "*";
			repaint();
		}

		else if ( e.getActionCommand().equals("3") ) {
			csnumeral = 3;
			csqual = 0;
			currentStudentRomanNumeral = "III";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("iii") ) {
			csnumeral = -3;
			csqual = 1;
			currentStudentRomanNumeral = "iii";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("4") ) {
			csnumeral = 4;
			csqual = 0;
			currentStudentRomanNumeral = "IV";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("iv") ) {
			csnumeral = -4;
			csqual = 1;
			currentStudentRomanNumeral = "iv";
			currentStudentChordQuality = "*";
			repaint();
		}

		else if ( e.getActionCommand().equals("5") ) {
			csnumeral = 5;
			csqual = 0;
			currentStudentRomanNumeral = "V";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("v") ) {
			csnumeral = -5;
			csqual = 1;
			currentStudentRomanNumeral = "v";
			currentStudentChordQuality = "*";
			repaint();
		}

		else if ( e.getActionCommand().equals("6") ) {
			csnumeral = 6;
			csqual = 0;
			currentStudentRomanNumeral = "VI";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("vi") ) {
			csnumeral = -6;
			csqual = 1;
			currentStudentRomanNumeral = "vi";
			currentStudentChordQuality = "*";
			repaint();
		}

		else if ( e.getActionCommand().equals("7") ) {
			csnumeral = 7;
			csqual = 0;
			currentStudentRomanNumeral = "VII";
			currentStudentChordQuality = "*";
			repaint();
		}
		else if ( e.getActionCommand().equals("vii") ) {
			csnumeral = -7;
			csqual = 1;
			currentStudentRomanNumeral = "vii";
			currentStudentChordQuality = "*";
			repaint();
		}


		if ((currentStudentRomanNumeral == "") || (currentStudentChordQuality == "")) 
		{
			checkButton.setEnabled(false);
		}
		else
		{
			currentAnswer = Math.abs(csnumeral) + (csqual) * 10;

			checkButton.setEnabled(true);
		}

 
		//
		//	CHECK BUTTON Servicing
		//
		if (e.getActionCommand().equals("CHECK") )	{
			DoJudging(currentAnswer);
			repaint();
		}
		//
		//	NEXT BUTTON Servicing
		//
		if ( e.getActionCommand().equals("NEXT") )	{
			processNextButton();
			_ShowKeysig = false;
			repaint();
		}
		//
		//	PLAY Triad BUTTON Servicing
		//
		if ( e.getActionCommand().equals("PLAY TRIAD") )	{
			if ( !suppressingMIDI )	{
				midiOut.createSequence();
				midiOut.addProgChg(0, userInstrument );
				midiOut.addNoteOn(0, rootMidi);
				midiOut.addNoteOn(0, thirdMidi);
				midiOut.addNoteOn(0, fifthMidi);	
				midiOut.addNoteOff(70, rootMidi );
				midiOut.addNoteOff(70, thirdMidi );
				midiOut.addNoteOff(70, fifthMidi );
				midiOut.addEndOfTrack(72);
				playMIDI( 120,0 );
			}
		}
		if ( e.getActionCommand().equals("PLAY NOTES") )	{
			if ( !suppressingMIDI )	{
				midiOut.createSequence();
				midiOut.addProgChg(0, userInstrument );
				midiOut.addNoteOn(0, rootMidi);
				midiOut.addNoteOff(22, rootMidi );
				midiOut.addNoteOn(24, thirdMidi);
				midiOut.addNoteOff(46, thirdMidi );
				midiOut.addNoteOn(48, fifthMidi);	
				midiOut.addNoteOff(70, fifthMidi );
				midiOut.addEndOfTrack(72);
				playMIDI( 120,0 );
			}
		}

		//
		//	SHOW CORRECT Servicing
		//
		if ( e.getActionCommand().equals("SHOW CORRECT") )	{
			//char 	pName;
			//int	 	theRow;
			//String	allPitchNames = "ABCDEFG";
			
			//theRow = GetNoteRepRow(qNote[currQues]);
			//pName  = RowToPitchChar(qNote[currQues], qClefs[currQues]);			
			strFeedback = "Answer: "+DisplayAnswer(qAnswer[currQues]);
			_ShowKeysig = true;
			repaint();
		}
		//
		//	EXAM BUTTON Servicing
		//
		if ( e.getActionCommand().equals("TAKE EXAM") )	{
			if (nextURL != null)	{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		//
		//	EXIT BUTTON Servicing
		//
		if ( e.getActionCommand().equals("EXIT") )	{
			stop();
			if ( examMode )	{
				if (drillURL != null)	{	
					this.getAppletContext().showDocument(drillURL);
				}
			}
			else	{
				if (drillExitURL != null)	{	
					this.getAppletContext().showDocument(drillExitURL);
				}
			}
		}
	}
	private String DisplayAnswer(int answer)
	{
		int quality = answer / 10;
		String qual = "";
		int capital = 1;
		if (quality == 1)
		{
			qual = "";
			capital = 0;
		}
		if (quality == 2)
		{
			qual = "+";
			capital = 1;
		}
		if (quality == 3)
		{
			qual = "o";
			capital = 0;
		}
		int degree = answer % 10;
		return GetRomanNumeral(degree,capital)+" "+qual;
	}
//------------------------------------------------------------------------------
//IMPLEMENTATION OF ItemListener Interface:
//------------------------------------------------------------------------------

	public void itemStateChanged(ItemEvent ie)
	{
		if ( !examMode ) {
			userInstrument = imntCombo.getSelectedIndex();
		}
	}

//------------------------------------------------------------------------------

	public void AddNoteheadToStaff(int col, int row, char accid, int accidNudge )
	{
		int	i		 = 0;
		int slot = -1;
		
		while ( i < numNoteheads )	{
			if ( noteheadInUse[i] == 0 )	{
				slot = i;
			}
			i++;
		}	
		
		if (slot == -1)	{
			slot = numNoteheads;
			numNoteheads++;
		}
		noteheadInUse[slot] = 1;	
		noteheadCol[slot]		= col;
		noteheadRow[slot]		= row;	
		noteheadAccid[slot] = accid;
		noteheadColor[slot] = BLACK_NOTE;
		noteheadAccidNudge[slot] = accidNudge;

	}
	
//------------------------------------------------------------------------------

	public void EraseAllNoteheadsInColumn(int col)
	{	
		int i = 0;
	
		while (i < numNoteheads)	{
			if ( noteheadCol[i] == col )	{	
				noteheadInUse[i]	= 0;
				noteheadRow[i]		=	-1;	
				noteheadCol[i]		=	-1;	
				noteheadAccid[i]	=	' ';
				noteheadAccidNudge[i]   = 0;				
				noteheadColor[i]	=	-1;
			}
			i++;
		}	
	}

//------------------------------------------------------------------------------
//	Question Generator
//------------------------------------------------------------------------------

	public int ChooseClef(String cStr)
	{
		if ( cStr.equals("TREBLE") ) {
			return( TREBLE_CLEF );
		}
		else	{
			if ( cStr.equals("BASS") ) {
				return( BASS_CLEF );
			}
			else {
				if ( randInt(1,1000) > 500 ) {
					return( TREBLE_CLEF );
				}
				else {
					return( BASS_CLEF );
				}
			}
		}
	}
		
//------------------------------------------------------------------------------
//	Concert a Pitch Name to a Number
//------------------------------------------------------------------------------

	public int PitchNameToNumber(char pChar)
	{
		String pNames	=	"ABCDEFG";
		
		return( pNames.indexOf(pChar) );
	}
	
//------------------------------------------------------------------------------
// Determine Note Row of Root of scale from clef and key sig
//------------------------------------------------------------------------------

	public int GetRootNoteRowFromClefKey(int clef, int keysig)
	{
		int noteRow = -1;

		if (contentMode == MAJOR)
		{
		    if (clef == TREBLE_CLEF)
		    {
			switch(keysig)
			{
				case -7: { noteRow = 14 ; break ;}
				case -6: { noteRow = 10 ; break ;}
				case -5: { noteRow = 13 ; break ;}
				case -4: { noteRow = 16 ; break ;} 
				case -3: { noteRow = 12 ; break ;}
				case -2: { noteRow = 15 ; break ;}
				case -1: { noteRow = 11 ; break ;}
				case 0:  { noteRow = 14 ; break ;}
				case 1:  { noteRow = 10 ; break ;}
				case 2:  { noteRow = 13 ; break ;}
				case 3:  { noteRow = 16 ; break ;}
				case 4:  { noteRow = 12 ; break ;}
				case 5:  { noteRow = 15 ; break ;}
				case 6:  { noteRow = 11 ; break ;}
				case 7:  { noteRow = 14 ; break ;}
			}
		    }
		    else if (clef == BASS_CLEF)
		    {
			switch(keysig)
			{
				case -7: { noteRow = 16 ; break ;}
				case -6: { noteRow = 12 ; break ;}
				case -5: { noteRow = 15 ; break ;}
				case -4: { noteRow = 11 ; break ;} 
				case -3: { noteRow = 14 ; break ;}
				case -2: { noteRow = 10 ; break ;}
				case -1: { noteRow = 13 ; break ;}
				case 0:  { noteRow = 16 ; break ;}
				case 1:  { noteRow = 12 ; break ;}
				case 2:  { noteRow = 15 ; break ;}
				case 3:  { noteRow = 11 ; break ;}
				case 4:  { noteRow = 14 ; break ;}
				case 5:  { noteRow = 10 ; break ;}
				case 6:  { noteRow = 13 ; break ;}
				case 7:  { noteRow = 16 ; break ;}
			}
		    }
		}
		else if ((contentMode == NATURAL_MINOR) || (contentMode == HARMONIC_MINOR) || (contentMode == MELODIC_MINOR))
		{
		    if (clef == TREBLE_CLEF)
		    {
			switch(keysig)
			{
				case -7: { noteRow = 16 ; break ;}
				case -6: { noteRow = 12 ; break ;}
				case -5: { noteRow = 15 ; break ;}
				case -4: { noteRow = 11 ; break ;} 
				case -3: { noteRow = 14 ; break ;}
				case -2: { noteRow = 10 ; break ;}
				case -1: { noteRow = 13 ; break ;}
				case 0:  { noteRow = 16 ; break ;}
				case 1:  { noteRow = 12 ; break ;}
				case 2:  { noteRow = 15 ; break ;}
				case 3:  { noteRow = 11 ; break ;}
				case 4:  { noteRow = 14 ; break ;}
				case 5:  { noteRow = 10 ; break ;}
				case 6:  { noteRow = 13 ; break ;}
				case 7:  { noteRow = 16 ; break ;}
			}
		    }
		    else if (clef == BASS_CLEF)
		    {
			switch(keysig)
			{
				case -7: { noteRow = 11 ; break ;}
				case -6: { noteRow = 14 ; break ;}
				case -5: { noteRow = 10 ; break ;}
				case -4: { noteRow = 13 ; break ;} 
				case -3: { noteRow = 16 ; break ;}
				case -2: { noteRow = 12 ; break ;}
				case -1: { noteRow = 15 ; break ;}
				case 0:  { noteRow = 11 ; break ;}
				case 1:  { noteRow = 14 ; break ;}
				case 2:  { noteRow = 10 ; break ;}
				case 3:  { noteRow = 13 ; break ;}
				case 4:  { noteRow = 16 ; break ;}
				case 5:  { noteRow = 12 ; break ;}
				case 6:  { noteRow = 15 ; break ;}
				case 7:  { noteRow = 11 ; break ;}
			}
		    }
		}
		return noteRow;
	}

//------------------------------------------------------------------------------
//	Generate a "deck" of questions to be presented
//------------------------------------------------------------------------------

	public void GenerateQuestionDeck()
	{
	    int		q;
	    int		noteRow = 0;
	    int		incrScaleTone = 0;
			
	    for (q = 0; q < Q_DECK_MAX; q++) 
	    {
		qClefs[q]  = ChooseClef(parmClefType);  // Select clef

		//select key sig
		qKeySig[q] = keySelect.getRandKey();
		
		//select scale tone: rotate to make sure all get tested
		qScaleTone[q] = incrScaleTone;	//randInt(0,6);
		incrScaleTone++;
		if (incrScaleTone > 6)
		{
			incrScaleTone = 0;
		}

		//Determine Note Row from scale tone and key sig

		noteRow = GetRootNoteRowFromClefKey(qClefs[q], qKeySig[q]) - qScaleTone[q];

		//For Treble clef, no Bx C# or Cx
		//For Bass Clef, no Cb or Cbb

		//select quality
		qTriadQuality[q] = useThisScaleToneQuality[qScaleTone[q]];
		
		if (_debug)
		{ 		
			System.out.println ("Before CreateTriadRep: q="+q
			+" clef="+qClefs[q]
			+" keysig="+ qKeySig[q]
			+" scale tone="+qScaleTone[q]
			+" noterow="+noteRow
			+" key="+qKeySig[q]
			+" triadQuality="+qTriadQuality[q]
			);
		}

		qNote[q] = CreateTriadRep(noteRow, qKeySig[q], qTriadQuality[q]);	
	
		qAnswer[q] = (qScaleTone[q]+1)+qTriadQuality[q]*10;

		if (_debug)
		{
			System.out.println ("q="+q
			+" qnote="+qNote[q]
			+" scaleTone="+qScaleTone[q]
			+" answer="+qAnswer[q]
			);
		}
	    }
	}

//------------------------------------------------------------------------------

	public void	ShuffleQuestionDeck(int sTimes, int totalQ)
	{
		int tempClef;					//	swap variable for question clef
		int tempNote;					//	swap variable for question note
		int tempKeySig;
		int tempScaleTone;
		int tempTriadQuality;
		int tempAnswer;				//	swap variable for question answer
		int	i;								//	shuffle loop counter variable
		int	n;								//	shuffle slot variable

		if (_debug)	{ return; }

		for (i = 0; i < sTimes; i++) {
			n = randInt(0, totalQ - 1);
			
			tempClef	 = qClefs[i];						
			tempNote	 = qNote[i];
			tempAnswer = qAnswer[i];
			tempKeySig	= qKeySig[i];
			tempScaleTone	= qScaleTone[i];
			tempTriadQuality = qTriadQuality[i];

			qClefs[i]  	= qClefs[n];
			qNote[i]	= qNote[n];
			qAnswer[i] 	= qAnswer[n];
			qKeySig[i]	= qKeySig[n];
			qScaleTone[i]	= qScaleTone[n];
			qTriadQuality[i] = qTriadQuality[n];
			
			qClefs[n]	= tempClef;
			qNote[n]	= tempNote;
			qAnswer[n] 	= tempAnswer;
			qKeySig[n]	= tempKeySig;
			qScaleTone[n]	= tempScaleTone;
			qTriadQuality[n] = tempTriadQuality;
		}
	}

//------------------------------------------------------------------------------
	
	public void PresentCurrentQuestion(int q)
	{
		int	startNote	= 0;
		int     acnum = 0;
		char	ac				= ' ';
		int 	qual = 0;
		int		note			= 0;

		currentClef	= qClefs[q];
		currentKeySig = qKeySig[q];

		if (_debug)
		{ 		
			System.out.println ("PresentCurrentQuestion: q="+q
			+" clef="+qClefs[q]
			+" keysig="+ qKeySig[q]
			+" scale tone="+qScaleTone[q]
			+" triadQuality="+qTriadQuality[q]
			+" qnote="+qNote[q]
			+" scaleTone="+qScaleTone[q]
			+" answer="+qAnswer[q]
			);
		}


		startNote = GetNoteRepRow(qNote[q]);		
		qual = GetNoteRepTriadQuality(qNote[q]);
		char quality = 'M';

		if (qual == 1) 
			{ quality = 'm'; }
		else 
		{	if (qual == 2) { quality = '+'; }
			else { 
				if (qual == 3) { quality = '-'; } 
			}
		}
		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: startnote="+startNote+" qual="+qual+" quality="+quality);
		}

		//Derive midi note from clef, startNote, ac:

		rootMidi = RowToMidiNoteNum(startNote, currentClef, currentKeySig);

		currentQuesOctaveMidi = rootMidi / 12;

		int rootrow = GetRootNoteRowFromClefKey(currentClef, currentKeySig);
		int rootScaleMidi = RowToMidiNoteNum(rootrow, currentClef, currentKeySig);
		int usethis = useThisScale[qScaleTone[q]];
		int achange = (rootScaleMidi + usethis) - rootMidi;

		UdmtKeySignature rootuks = new UdmtKeySignature();
   	    	rootuks.setKeySignature(currentKeySig);

		if (contentMode == MAJOR)
		{
			_KeySigNameDisp = rootuks.getKeySigNameMajor();
		}
		else
		{
			_KeySigNameDisp = rootuks.getKeySigNameMinor();
		}

		acnum = -1 * rootuks.getKeySigAccid (currentClef, startNote);
		
		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: rootMidi="+rootMidi+"acnum="+acnum);
		}

		if ((acnum == -1) && (achange == +1))
		{
			acnum = 3;	// flat + 1 = natural
			rootMidi++;
		}
		else if ((acnum == 0) && (achange == +1))
		{
			acnum = 1;	// natural + 1 = sharp
			rootMidi++;
		}
		else if ((acnum == 1) && (achange == +1))
		{
			acnum = 2;	// sharp + 1 = double sharp
			rootMidi++;
		}
		//else
		//{
		//	acnum = 0;
		//}

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion:  rootrow="+rootrow+" rootScaleMidi="+rootScaleMidi+" usethis="+usethis+" achange="+achange);
			System.out.println("PresentCurrentQuestion: currentQuesOctaveMidi="+currentQuesOctaveMidi+" acnum="+acnum);
		}	

		ac = accidToChar(acnum);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: startnote="+startNote+" ac="+ac+" acnum=["+acnum+"] quality="+quality);
		}

		UdmtChord chord = new UdmtChord();
		chord.ConstructTriadFromRootMidi(rootMidi,quality);

		thirdMidi = chord.getThirdMidi();
		int staffinfo3 = MidiNoteNumToRowAcClef(thirdMidi, currentClef, currentKeySig, startNote - 2);
		int staffinfo3note   = (staffinfo3 % 100);		
		int staffinfo3acnum = staffinfo3 / 100 - 2;
		char staffinfo3ac = accidToChar(staffinfo3acnum);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: third="+thirdMidi+" ["+staffinfo3+"] staffinfo3acnum="+staffinfo3acnum+" staffinfo3ac="+staffinfo3ac);
		}

		fifthMidi = chord.getFifthMidi();
		int staffinfo5 = MidiNoteNumToRowAcClef(fifthMidi, currentClef, currentKeySig, startNote - 4);
		int staffinfo5note   = staffinfo5 % 100;
		int staffinfo5acnum = staffinfo5 / 100 - 2;
		char staffinfo5ac = accidToChar(staffinfo5acnum);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: fifth="+fifthMidi+" ["+staffinfo5+"] staffinfo5acnum="+staffinfo5acnum+" staffinfo5ac="+staffinfo5ac);
		}
		
		EraseAllNoteheadsInColumn(STAFF_COLUMN_ROOT);
		EraseAllNoteheadsInColumn(STAFF_COLUMN_THIRD);
		EraseAllNoteheadsInColumn(STAFF_COLUMN_FIFTH);
		EraseAllNoteheadsInColumn(STAFF_COLUMN_TRIAD);

		AddNoteheadToStaff(STAFF_COLUMN_ROOT, startNote, ac,0);
		AddNoteheadToStaff(STAFF_COLUMN_THIRD, staffinfo3note, staffinfo3ac,0);
		AddNoteheadToStaff(STAFF_COLUMN_FIFTH, staffinfo5note, staffinfo5ac,0);

		AddNoteheadToStaff(STAFF_COLUMN_TRIAD, startNote, ac, NudgeLeftRoot);
		AddNoteheadToStaff(STAFF_COLUMN_TRIAD, staffinfo3note, staffinfo3ac, NudgeLeftThird);
		AddNoteheadToStaff(STAFF_COLUMN_TRIAD, staffinfo5note, staffinfo5ac, NudgeLeftFifth);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: root=="+rootMidi+"["+startNote+"] third="+thirdMidi+" ["+staffinfo3+"] fifth="+fifthMidi+" ["+staffinfo5 +"]");
		}
	
		//TEST
		//AddNoteheadToStaff(13,16,'s');
		//AddNoteheadToStaff(13,14,'s');
		//AddNoteheadToStaff(13,12,'S');
		//AddNoteheadToStaff(1,16,'s');
		//AddNoteheadToStaff(5,14,'s');
		//AddNoteheadToStaff(9,12,'S');

		//theStopWatch.StartWatch();		
	}

//------------------------------------------------------------------------------

	public int randInt(int low, int high)
	{
		return(((int)((high-low+1)*(Math.random()))) + low);
	}

//------------------------------------------------------------------------------
// GRAPHICS ROUTINES
//------------------------------------------------------------------------------

	public void update(Graphics g) 
	{
		imageScreen = createImage(rectScreen.width, rectScreen.height);
		paint(imageScreen.getGraphics());
		g.drawImage(imageScreen, 0, 0, null);
	}
   
//------------------------------------------------------------------------------

	public void paint(Graphics g)
	{
		if ( showErrorScreen )	{
			g.setColor(Color.red);
			g.fillRect(0, 0, rectScreen.width, rectScreen.height);

			g.setColor (Color.black);
			drawErrorScreen(g);
		}
		else	{
			g.setColor(Color.white);
			g.fillRect(0, 0, rectScreen.width, rectScreen.height);

			g.setColor(Color.black);
			drawScreen(g);
		}
	}

//------------------------------------------------------------------------------

	public void drawErrorScreen( Graphics g )
	{
		Font txtFont;
		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);

		g.drawString("THE FOLLOWING ERROR HAS OCCURRED:", 50, 150);
		g.drawString(txtError, 50, 200);
		
		g.drawString("Please open the Java Console for more information",50,250);								g.drawString("and contact systems support.",50,270);		
	}

//------------------------------------------------------------------------------

	private void displayScore(Graphics g)
	{
		Font txtFont;

		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);

		nScorePercent	=	0;
		
		nScore1	=	numQuesCorrect1stTime;
		nScore2	=	numQuesPresented;
		if ( numQuesPresented > 0 ) {
			nScorePercent = (int)(nScore1 * 100 / nScore2);
		}

		strScoreDisp = strScore1 + nScore1 + strScore2 + nScore2 + strScore3 + nScorePercent + strScore4;

		//centerString(g, strScoreDisp,			txtFont,  85);
		centerString(g, strScoreDisp,			txtFont,  50);

		g.drawString(strFeedback, 5, Y_STUDENT_CHORD-50);
		
		centerString(g, strExamFeedback1, txtFont, EXAM_FB_LINE_1);
		centerString(g, strExamFeedback2, txtFont, EXAM_FB_LINE_2);

		String dispCurrentStudentChordQuality = currentStudentChordQuality;
		if (currentStudentChordQuality.equals("*"))
		{
			dispCurrentStudentChordQuality  = "";
		}
		String currentStudentChord = currentStudentRomanNumeral +" "+ dispCurrentStudentChordQuality;

		g.drawString(currentStudentChord, X_STUDENT_CHORD-10, Y_STUDENT_CHORD);
		g.drawRect(X_STUDENT_CHORD - 20 , Y_STUDENT_CHORD - 20, 70, 30);

}

//------------------------------------------------------------------------------

	public void drawScreen(Graphics g)
	{

		Font txtFont;
		Font headerFont;

		headerFont = new Font ("Arial", Font.BOLD, 28);
		g.setFont(headerFont);

		if ( examMode )	{
			centerString(g, strExamMode, headerFont, 30);
		}
		else	{
			centerString(g, strDrillMode, headerFont, 30);
		}

		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);
		
		//strInstruct	 = strInstruct1 + strInstruct2 + strInstruct3;
		//centerString(g, strInstruct, txtFont, 60);
		
		//
		//	draw the staff
		//
		for (int x = staffX; x < (staffX+560); x += 65)	{
			g.drawImage(muStaff.getImage(), x, staffY, this );
		}
		//
		//	draw the bar lines at staff edges
		//
		g.drawImage(muBar.getImage(),			 staffX - 1, staffY, this);
		g.drawImage(muRtDblBar.getImage(), staffX+567, staffY, this);
		//
		//	draw the clef
		//
		int clefYSharpOffset = 0;
		int clefYFlatOffset = 0;
		if ( currentClef == TREBLE_CLEF )	{
			clefYSharpOffset = -20;
			clefYFlatOffset = -20;
			g.drawImage(muTrebleClef.getImage(),staffX+gClefXOffset,staffY+gClefYOffset,this);
		}
		else	{
			clefYSharpOffset = -5;
			clefYFlatOffset = -5;
			g.drawImage(muBassClef.getImage(),staffX+fClefXOffset,staffY+fClefYOffset,this);
		}
	
		// draw the key signature
		if (_ShowKeysig)
		{
			if (currentKeySig < 0)
			{
				for (int i = 0 ; i < currentKeySig * -1 ; i++)
				{
					g.drawImage(muFlat.getImage(), keyFlatXOffset[i]+15, keyFlatYOffset[i]+staffY+clefYFlatOffset, this);

				}
			}
			else if (currentKeySig > 0)
			{
				for (int i = 0 ; i < currentKeySig ; i++)
				{
					g.drawImage(muSharp.getImage(), keySharpXOffset[i]+15, keySharpYOffset[i]+staffY+clefYSharpOffset, this);

				}
			}
		}
		//g.drawString("Key: "+_KeySigNameDisp, X_STUDENT_CHORD-10, Y_STUDENT_CHORD-25);
		g.drawString("Key: "+_KeySigNameDisp, keySharpXOffset[0]+15, staffY+clefYSharpOffset-25);


		//
		//	draw the notes
		//
		for (int i = 0; i  <numNoteheads; i++)	{
			if ( noteheadInUse[i] == 1 )	{
				if ( noteheadColor[i] == 0 )	{
					drawNotehead(g, noteheadCol[i], noteheadRow[i], 'U' );
				}
				else	{
					if ( examMode )	{
						drawNotehead(g, noteheadCol[i], noteheadRow[i], 'F' );
					}
					else	{
						drawNotehead(g, noteheadCol[i], noteheadRow[i], 'R' );
					}
				}
				drawAccidental(g, noteheadCol[i], noteheadRow[i], noteheadAccid[i], noteheadAccidNudge[i]);
			}
		}

		// Draw Rectangles for positions where user will enter notes for Root, 3rd, 5th
		for (int i=1; i<=13 ; i+=4)
		{
			int extra = 15;
			g.drawRect(staffX + staffNoteXPos[i-1] + 7 - extra, staffY + staffNoteYPos[0] - 25,staffNoteXPos[i+1] - staffNoteXPos[i-1] + extra,175);
			//g.drawImage (muDot.getImage(), staffX + staffNoteXPos[i] + 7, staffY + staffNoteYPos[0] - 25, this );


			if (i==1)
			{
				g.drawString("Root:", staffX + staffNoteXPos[i-1] + 7, staffY + staffNoteYPos[0] - 35);
			}
			else if (i==5)
			{
				g.drawString("Third:", staffX + staffNoteXPos[i-1] + 7, staffY + staffNoteYPos[0] - 35);
			}
			else if (i==9)
			{
				g.drawString("Fifth:", staffX + staffNoteXPos[i-1] + 7, staffY + staffNoteYPos[0] - 35);
			}
			else if (i==13)
			{
				g.drawString("Triad:", staffX + staffNoteXPos[i-1] + 7, staffY + staffNoteYPos[0] - 35);
			}

		}

		//Draw notehead at cursor position
		if (cursorvisible == 1)
		{	
			drawNotehead (g, cursorcol, cursorrow, 'U' );

			//The following is an attempt to attach the notehead to the cursor
			//g.drawImage (muNoteheadUnfilled.getImage(), savecursorx-10, savecursory-20, this);
		}
	
		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);			
		displayScore(g);					//	display current score
		g.setColor (Color.black);

		//This is the barrier for Note Game
		//for (int offset = 0 ; offset <= 24 ; offset += 3)
		//{
		//	g.drawImage(muBar.getImage(),staffNoteXPos[1]+offset, staffY, this);
		//}

	}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

	public void	centerStringAt(Graphics g, String s, Font f, int xDivide, int yCoord)
	{
		FontMetrics fm = g.getFontMetrics(f);
		
		g.drawString(s, (xDivide - (fm.stringWidth(s) / 2)), yCoord);
	}

//------------------------------------------------------------------------------

	public void centerString(Graphics g, String s, Font f, int yCoord)
	{
		FontMetrics fm = g.getFontMetrics(f);
		
		centerStringAt(g, s, f, (rectScreen.width / 2), yCoord);
	}

//------------------------------------------------------------------------------
//	Triad Representation Methods
//------------------------------------------------------------------------------

	public int CreateTriadRep(int row, int keysig, int triadquality)
	{
		return( (triadquality * 100) + ((keysig + 14) * 1000) + row );
	}

//------------------------------------------------------------------------------
	public int GetNoteRepKeySig(int n)
	{
		return (n / 1000) - 14;
	}
//------------------------------------------------------------------------------

	public int GetNoteRepRow(int n)
	{
		return( n - (n / 100)* 100 );
	}

//------------------------------------------------------------------------------
	public int GetNoteRepTriadQuality(int n)
	{

		return (n - ((n / 1000)* 1000)) / 100;
	}

//------------------------------------------------------------------------------
	public String GetRomanNumeral(int degree, int capital)
	{
		String retval = "";
		if (capital > 0)
		{
			switch (degree)
			{
				case 1: {retval = "I"; break;}
				case 2: {retval = "II"; break;}
				case 3: {retval = "III"; break;}
				case 4: {retval = "IV"; break;}
				case 5: {retval = "V"; break;}
				case 6: {retval = "VI"; break;}
				case 7: {retval = "VII"; break;}
			}
		}
		else
		{
			switch (degree)
			{
				case 1: {retval = "i"; break;}
				case 2: {retval = "ii"; break;}
				case 3: {retval = "iii"; break;}
				case 4: {retval = "iv"; break;}
				case 5: {retval = "v"; break;}
				case 6: {retval = "vi"; break;}
				case 7: {retval = "vii"; break;}
			}
		}
		return retval;
	}
//------------------------------------------------------------------------------

	public void drawNotehead(Graphics g, int x , int y, char nhtype )
	{	
		//System.out.println("drawNotehead: x="+x+" y="+y+" nhtype="+nhtype);
		if ((x < 0) || (y < 0))
		{
			return;
		}

		if (nhtype == 'F')	{																// filled note
			g.drawImage(muNoteheadFilled.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}
		else if (nhtype == 'U')	{													 // unfilled note
			g.drawImage(muNoteheadUnfilled.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}
		else if (nhtype == 'R')	{													// red note
			g.drawImage(muNoteheadRed.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y]
				, this );
		}

		if ( y <=  2)	{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[2]
			, this );
		}
		if ( y == 0 )	{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[0]
			, this );
		}
		if (y >= 14)	{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[14]
			, this );
		}
		if (y==16)	{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7
			, staffY-50+ staffNoteYPos[16]
			, this );
		}
	}

//------------------------------------------------------------------------------

	public void drawAccidental(Graphics g, int x , int y, char accid, int nudge )
	{
		//System.out.println("drawAccidental: x="+x+" y="+y+" accid="+accid);

		if ((x >= 0) && (y >= 0))
		{
			if (accid == 's')	{
				g.drawImage (muSharp.getImage()
				, staffX + staffNoteXPos[x] - 20 + nudge
				, staffY-20+ staffNoteYPos[y] - 13
				, this );

			}
			else if (accid == 'f')	{
				g.drawImage (muFlat.getImage()
				, staffX + staffNoteXPos[x] - 20 + nudge
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
			else if (accid == 'n')	{
				g.drawImage(muNatural.getImage(), 
									staffX + staffNoteXPos[x] - 18 + nudge,
									staffY-20+ staffNoteYPos[y] - 13,
									this );
			}
	
			else if (accid == 'S')	{
				g.drawImage (muDblSharp.getImage()
				, staffX + staffNoteXPos[x] - 20 + nudge
				, staffY-20+ staffNoteYPos[y] - 14
				, this );

			}
			else if (accid == 'F')	{
				g.drawImage (muDblFlat.getImage()
				, staffX + staffNoteXPos[x] - 25 + nudge
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
		}
	}


//------------------------------------------------------------------------------
//	public void playSound(String soundFile, int delay)
//	{
//		play (getCodeBase(),soundFile);
//		if (delay > 0)
//		{
//			try {
//				Thread.sleep(delay);
//			} catch (Exception ex)
//			{
//				System.out.println(ex.toString());
//			}
//		}
//	}
//------------------------------------------------------------------------------
	public void playMIDI( int tempo, int delay )
	{
		if (_playMidiNotes)
		{
			midiOut.playSequence( tempo );
			if (delay > 0)
			{
				try {
					Thread.sleep(delay);
				} catch (Exception ex)
				{
				System.out.println(ex.toString());
				}
			}
		}
	}
//------------------------------------------------------------------------------
// MOUSE LISTENER
//------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) 
	{				
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
	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("mouseClicked");
   	}

//------------------------------------------------------------------------------
// MouseMotionListener:
//------------------------------------------------------------------------------
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

	public void setupPopupMenu()
	{
		MenuItem mi ;
		popup = new PopupMenu ( "Select Note Type" );

		popup.add ( mi = new MenuItem ( "Double Sharp" ) )  ;
		mi.addActionListener (this) ;	

		popup.add ( mi = new MenuItem ( "Sharp" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "Natural" ) );
		mi.addActionListener (this) ;
	
		popup.add ( mi = new MenuItem ( "None" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "Flat" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "Double Flat" ) );
		mi.addActionListener (this) ;
	}

//------------------------------------------------------------------------------

}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

