//------------------------------------------------------------------------------
// UdmtIntvEarTrain Applet -  Triad Identification Drill and Exam
//------------------------------------------------------------------------------
// PARAMS:
//
// clefParm		=	The type of scale to select degrees from:
//					Values: 	TREBLE
//								BASS
//								BOTH
//					REQUIRED
//
// intervalTypeParm	=	Interval types being tested:
//					Values:	MAJOR
//						ALL
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

public class UdmtIntvEarTrain extends Applet implements ActionListener, ItemListener, MouseListener, MouseMotionListener
{
	private boolean _debug = false;	

	private boolean _running = false;	//used for Runnable interface
	private boolean _gameInProgress = false;
	private boolean _playMidiNotes = true;	

	private Thread _looper;

	private String udmtAppletName			=	"UdmtIntvEarTrain";
	private String udmtAppletVersion	=	"0.05"	;

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

	private Button checkButton;
	private Button nextButton;
	private Button playTriadButton;

	private Button playNotesButton,	 takeExamButton,	showCorrButton;
	private Button nextLessonButton, exitButton;
	
	private	Button P1;
	private	Button m2A1;
	private	Button M2d3;
	private	Button m3A2;
	private	Button M3d4;
	private	Button P4A3;
	private	Button A4d5;
	private	Button P5;
	private	Button m6A5;
	private	Button M6;
	private	Button m7A6;
	private	Button M7d8;
	private	Button P8;

	private	Button btnMajTriad;
	private	Button btnMinTriad;
	private	Button btnSharpTriad;
	private	Button btnFlatTriad;
	private	Button btnNaturalTriad;
	private	Button btnAugTriad;
	private	Button btnDimTriad;

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

	private int csnumeral = -1;
	private	int csacc = 0;
	private	int csqual = 0;
	private String currentStudentNoteLetter = "";
	private String currentStudentNoteAccid = "";

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
	
	private final	int	Q_DECK_MAX =	88;				//	maximum # of questons in deck	
	private				int	qClefs[];								//	clef used in question
	private				int	qNote[];								//	display note of question
	private				int	qAnswer[];							//	correct answer;
	private				int	qKeySig[];
	private				int	qScaleTone[];
	private				int	qIntervalRowSubtract[];

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

	private int[] useThisScale   = {0,2,4,5,7,9,11,12};
	private int[] majorScaleHalfSteps = {0,2,4,5,7,9,11,12};
	private int[] naturalMinorScaleHalfSteps = {0,2,3,5,7,8,10,12};
	private int[] harmonicMinorScaleHalfSteps = {0,2,3,5,7,8,11,12};
	private int[] melodicMinorScaleHalfSteps = {0,2,3,5,7,9,11,12};

	private boolean testAllIntervals = false;
	private int[] intvScaleLines = {1,2,2,3,3,4,4,5,6,6,7,7,8};
	private int[] intvMajScaleLines = {1,2,2,3,3,4,4,5,6,6,7,7,8};
	
	private boolean thirdVisible = false;

	private boolean _ShowKeysig = false;

//------------------------------------------------------------------------------

	public void init()
	{
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		strDrillMode	="Interval Ear Training ";
		strExamMode		="Interval Ear Training Exam ";

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
		//Retrieve Parameter:	 intervalType
		//
		parmContent	=	getParameter("intervalType");

		testAllIntervals = parmContent.equals("ALL");
		
		//System.out.println ("parmContent="+parmContent);
		
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
  				String sExamKey = objEk.getExamKey("IntvEarTrain"+parmContent+"Pass");
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
  					String sExamKey = objEk.getExamKey("IntvEarTrain"+parmContent+"Fail");
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
		qIntervalRowSubtract = new int[ Q_DECK_MAX ];

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
//	PLAY INTERVAL button setup
//
		playTriadButton = new Button("PLAY INTERVAL");
		playTriadButton.addActionListener(this);
		playTriadButton.setBounds(btncol1,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		if (suppressingMIDI)
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
		if (suppressingMIDI)
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
		P1 = new Button("P1");
		P1.addActionListener(this);
		P1.setEnabled(true);
		P1.setBounds(350, staffY-10 - 50, 100, 20);
		add(P1);

		if (testAllIntervals)
		{
			M2d3 = new Button("M2 d3");

			m2A1 = new Button("m2 A1");
			m2A1.addActionListener(this);
			m2A1.setEnabled(true);
			m2A1.setBounds(460, staffY-10 - 25 - 13, 100, 20);
			add(m2A1);
		}
		else
		{
			M2d3 = new Button("M2");
		}
		M2d3.addActionListener(this);
		M2d3.setEnabled(true);
		M2d3.setBounds(350, staffY-10 - 25, 100, 20);
		add(M2d3);

		if (testAllIntervals)
		{
			M3d4 = new Button("M3 d4");

			m3A2 = new Button("m3 A2");
			m3A2.addActionListener(this);
			m3A2.setEnabled(true);
			m3A2.setBounds(460, staffY-10 - 13, 100, 20);
			add(m3A2);
		}
		else
		{
			M3d4 = new Button("M3");
		}
		M3d4.addActionListener(this);
		M3d4.setEnabled(true);
		M3d4.setBounds(350, staffY-10 , 100, 20);
		add(M3d4);

		if (testAllIntervals)
		{
			P4A3 = new Button("P4 A3");

			A4d5 = new Button("A4 d5");
			A4d5.addActionListener(this);
			A4d5.setEnabled(true);
			A4d5.setBounds(460, staffY-10 + 2*25 - 13, 100, 20);
			add(A4d5);

			m6A5 = new Button("m6 A5");
			m6A5.addActionListener(this);
			m6A5.setEnabled(true);
			m6A5.setBounds(460, staffY-10 + 3*25 - 13, 100, 20);
			add(m6A5);
		}
		else
		{
			P4A3 = new Button("P4");
		}
		P4A3.addActionListener(this);
		P4A3.setEnabled(true);
		P4A3.setBounds(350, staffY-10 + 1*25, 100, 20);
		add(P4A3);

		P5 = new Button("P5");
		P5.addActionListener(this);
		P5.setEnabled(true);
		P5.setBounds(350, staffY-10 + 2*25, 100, 20);
		add(P5);

		M6 = new Button("M6");
		M6.addActionListener(this);
		M6.setEnabled(true);
		M6.setBounds(350, staffY-10 + 3*25, 100, 20);
		add(M6);

		if (testAllIntervals)
		{
			M7d8 = new Button("M7 d8");

			m7A6 = new Button("m7 A6");
			m7A6.addActionListener(this);
			m7A6.setEnabled(true);
			m7A6.setBounds(460, staffY-10 + 4*25 - 13, 100, 20);
			add(m7A6);
		}
		else
		{
			M7d8 = new Button("M7");
		}
		M7d8.addActionListener(this);
		M7d8.setEnabled(true);
		M7d8.setBounds(350, staffY-10 + 4*25, 100, 20);
		add(M7d8);

		P8 = new Button("P8");
		P8.addActionListener(this);
		P8.setEnabled(true);
		P8.setBounds(350, staffY-10 + 5*25, 100, 20);
		add(P8);

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
		if (ac == 0)
		{
			ac = 3; // natural sign
		}
	    }

	    if (_debug)
	    {
		System.out.println("MidiNoteNumToRowAcClef: final ac="+ac);
	    }
	    return (forceline + (ac+2) * 100 );
	}

//------------------------------------------------------------------------------

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
	
		if (_debug) {
			System.out.println("qAnswer="+qAnswer[currQues]+" currentAnswer="+currentAnswer);
 		}

		if ( qAnswer[currQues] == currentAnswer ) {
					
			if ( numTimesChecked == 0)	{
				numQuesCorrect1stTime++;
				gotThisQuesCorrect = 1;
			}	

			strFeedback = strCorrect;
			
			nextButton.setEnabled(true);
	
			numTimesChecked++;
			thirdVisible = true;
			repaint();
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
			thirdVisible = false;
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

			csnumeral = -1;
			currentAnswer = 0;
			currentStudentNoteLetter = "";


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

		if ( e.getActionCommand().equals("P1") ) {
			csnumeral = 0;
			currentStudentNoteLetter = "P1";
			repaint();
		}
		else if ( e.getActionCommand().equals("m2 A1") ) {
			csnumeral = 1;
			currentStudentNoteLetter = "m2 A1";
			repaint();
		}
		else if ( e.getActionCommand().equals("M2") ) {
			csnumeral = 2;
			currentStudentNoteLetter = "M2";
			repaint();
		}
		else if ( e.getActionCommand().equals("M2 d3") ) {
			csnumeral = 2;
			currentStudentNoteLetter = "M2 d3";
			repaint();
		}
		else if ( e.getActionCommand().equals("m3 A2") ) {
			csnumeral = 3;
			currentStudentNoteLetter = "m3 A2";
			repaint();
		}
		else if ( e.getActionCommand().equals("M3") ) {
			csnumeral = 4;
			currentStudentNoteLetter = "M3";
			repaint();
		}
		else if ( e.getActionCommand().equals("M3 d4") ) {
			csnumeral = 4;
			currentStudentNoteLetter = "M3 d4";
			repaint();
		}
		else if ( e.getActionCommand().equals("P4") ) {
			csnumeral = 5;
			currentStudentNoteLetter = "P4";
			repaint();
		}
		else if ( e.getActionCommand().equals("P4 A3") ) {
			csnumeral = 5;
			currentStudentNoteLetter = "P4 A3";
			repaint();
		}
		else if ( e.getActionCommand().equals("A4 d5") ) {
			csnumeral = 6;
			currentStudentNoteLetter = "A4 d5";
			repaint();
		}
		else if ( e.getActionCommand().equals("P5") ) {
			csnumeral = 7;
			currentStudentNoteLetter = "P5";
			repaint();
		}
		else if ( e.getActionCommand().equals("m6 A5") ) {
			csnumeral = 8;
			currentStudentNoteLetter = "m6 A5";
			repaint();
		}
		else if ( e.getActionCommand().equals("M6") ) {
			csnumeral = 9;
			currentStudentNoteLetter = "M6";
			repaint();
		}
		else if ( e.getActionCommand().equals("m7 A6") ) {
			csnumeral = 10;
			currentStudentNoteLetter = "m7 A6";
			repaint();
		}
		else if ( e.getActionCommand().equals("M7 d8") ) {
			csnumeral = 11;
			currentStudentNoteLetter = "M7 d8";
			repaint();
		}
		else if ( e.getActionCommand().equals("M7") ) {
			csnumeral = 11;
			currentStudentNoteLetter = "M7";
			repaint();
		}
		else if ( e.getActionCommand().equals("P8") ) {
			csnumeral = 12;
			currentStudentNoteLetter = "P8";
			repaint();
		}

		if (csnumeral < 0) 
		{
			checkButton.setEnabled(false);
		}
		else
		{
			currentAnswer = Math.abs(csnumeral);

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
		//	PLAY INTERVAL BUTTON Servicing
		//
		if ( e.getActionCommand().equals("PLAY INTERVAL") )	{
			if ( !suppressingMIDI )	{
				midiOut.createSequence();
				midiOut.addProgChg(0, userInstrument );
				midiOut.addNoteOn(0, rootMidi);
				midiOut.addNoteOn(0, thirdMidi);
				midiOut.addNoteOff(46, rootMidi );
				midiOut.addNoteOff(46, thirdMidi );
				midiOut.addEndOfTrack(48);
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
				midiOut.addEndOfTrack(48);
				playMIDI( 120,0 );
			}
		}

		//
		//	SHOW CORRECT Servicing
		//
		if ( e.getActionCommand().equals("SHOW CORRECT") )	{
			
			thirdVisible = true;
			currentStudentNoteLetter = "";
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
		int quality = answer / 10000;
		String qual = "Maj";
		int capital = 1;
		if (quality == 1)
		{
			qual = "min";
			capital = 0;
		}
		if (quality == 2)
		{
			qual = "Aug";
			capital = 1;
		}
		if (quality == 3)
		{
			qual = "dim";
			capital = 0;
		}
		int accid = (answer - quality * 10000) / 1000;
		if (accid == 1)
		{
			qual = "# "+qual;
		}
		else if (accid == 3)
		{
			qual = "b "+qual;
		}
		return qual;
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

	public void AddNoteheadToStaff(int col, int row, char accid )
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
	    int		incrScaleToneM = 0;
			
	    for (q = 0; q < Q_DECK_MAX; q++) 
	    {
		qClefs[q]  = ChooseClef(parmClefType);  // Select clef

		//select key sig
		if (_debug) {
			qKeySig[q] = keySelect.getTestKey();
		} else {
			qKeySig[q] = keySelect.getRandKey();
		}
		int intvsubtract = 0;

		if (testAllIntervals)
		{
			//select scale tone: rotate to make sure all get tested
			qScaleTone[q] = incrScaleTone;
			intvsubtract = intvScaleLines[incrScaleTone];
			incrScaleTone++;
			if (incrScaleTone > 12)
			{
				incrScaleTone = 0;
			}
		}
		else
		{
			//select scale tone: rotate to make sure all get tested
			qScaleTone[q] = useThisScale[incrScaleTone];

			intvsubtract = intvMajScaleLines[qScaleTone[q]];
			incrScaleTone++;
			if (incrScaleTone > 7)
			{
				incrScaleTone = 0;
			}
		}

		//Determine Note Row from scale tone and key sig

		noteRow = randInt(8,16);

		//Get Root Note Accidental for scale tone ( from key sig)

		UdmtKeySignature rootuks = new UdmtKeySignature();
   	    	rootuks.setKeySignature(qKeySig[q]);
		int noteAccid = rootuks.getKeySigAccid (qClefs[q], noteRow);  //+1 = flat -1 = sharp
		if (noteAccid != 0)
		{
			noteAccid += 2;	// sharp = 1 flat = 3
		}


		//For Treble clef, no Bx C# or Cx
		//For Bass Clef, no Cb or Cbb

		if (intvsubtract == 0)
		{
			qIntervalRowSubtract[q] = 7; // octave
		}
		else
		{
			qIntervalRowSubtract[q] = intvsubtract - 1;
		}
		
		if (_debug)
		{ 		
			System.out.println ("Before CreateTriadRep: q="+q
			+" clef="+qClefs[q]
			+" keysig="+ qKeySig[q]
			+" scale tone="+qScaleTone[q]
			+" noterow="+noteRow
			+" key="+qKeySig[q]
			+" triadQuality="+qIntervalRowSubtract[q]
			);
		}

		qNote[q] = CreateNoteRowRep(noteRow, qKeySig[q]);	

		rootMidi = RowToMidiNoteNum(noteRow, qClefs[q], qKeySig[q]);
	
		qAnswer[q] = qScaleTone[q];

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

		//if (_debug)	{ return; }

		for (i = 0; i < sTimes; i++) {
			n = randInt(0, totalQ - 1);
			
			tempClef	 = qClefs[i];						
			tempNote	 = qNote[i];
			tempAnswer = qAnswer[i];
			tempKeySig	= qKeySig[i];
			tempScaleTone	= qScaleTone[i];
			tempTriadQuality = qIntervalRowSubtract[i];

			qClefs[i]  	= qClefs[n];
			qNote[i]	= qNote[n];
			qAnswer[i] 	= qAnswer[n];
			qKeySig[i]	= qKeySig[n];
			qScaleTone[i]	= qScaleTone[n];
			qIntervalRowSubtract[i] = qIntervalRowSubtract[n];
			
			qClefs[n]	= tempClef;
			qNote[n]	= tempNote;
			qAnswer[n] 	= tempAnswer;
			qKeySig[n]	= tempKeySig;
			qScaleTone[n]	= tempScaleTone;
			qIntervalRowSubtract[n] = tempTriadQuality;
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
			+" IntervalRowSubtract="+qIntervalRowSubtract[q]
			+" qnote="+qNote[q]
			+" scaleTone="+qScaleTone[q]
			+" answer="+qAnswer[q]
			);
		}


		startNote = GetNoteRepRow(qNote[q]);		

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: startnote="+startNote);
		}

		//Derive midi note from clef, startNote, ac:

		rootMidi = RowToMidiNoteNum(startNote, currentClef, currentKeySig);

		currentQuesOctaveMidi = rootMidi / 12;

		int rootrow = startNote; //GetRootNoteRowFromClefKey(currentClef, currentKeySig);
		int rootScaleMidi = RowToMidiNoteNum(rootrow, currentClef, currentKeySig);
		
		int achange = (rootScaleMidi) - rootMidi;

		UdmtKeySignature rootuks = new UdmtKeySignature();
   	    	rootuks.setKeySignature(currentKeySig);
		
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
			System.out.println("PresentCurrentQuestion:  rootrow="+rootrow+" rootScaleMidi="+rootScaleMidi+" achange="+achange);
			System.out.println("PresentCurrentQuestion: currentQuesOctaveMidi="+currentQuesOctaveMidi+" acnum="+acnum);
		}	

		ac = accidToChar(acnum);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: startnote="+startNote+" ac="+ac+" acnum=["+acnum+"]");
		}
	
		thirdMidi = rootMidi + qScaleTone[q];

		int staffinfo3 = MidiNoteNumToRowAcClef(thirdMidi, currentClef, currentKeySig, startNote - qIntervalRowSubtract[q]);
		int staffinfo3note   = (staffinfo3 % 100);		
		int staffinfo3acnum = staffinfo3 / 100 - 2;
		char staffinfo3ac = accidToChar(staffinfo3acnum);

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: third="+thirdMidi+" ["+staffinfo3+"] staffinfo3acnum="+staffinfo3acnum+" staffinfo3ac="+staffinfo3ac);
		}

		
		EraseAllNoteheadsInColumn(STAFF_COLUMN_ROOT);
		EraseAllNoteheadsInColumn(STAFF_COLUMN_THIRD);

		AddNoteheadToStaff(STAFF_COLUMN_ROOT, startNote, ac);
		
		AddNoteheadToStaff(STAFF_COLUMN_THIRD, staffinfo3note, staffinfo3ac);
		

		if (_debug)
		{ 		
			System.out.println("PresentCurrentQuestion: root=="+rootMidi+"["+startNote+"] third="+thirdMidi+" ["+staffinfo3+"]");
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

		String currentStudentChord = currentStudentNoteLetter;

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
		for (int x = staffX; x < (staffX+295); x += 65)	{
			g.drawImage(muStaff.getImage(), x, staffY, this );
		}
		//
		//	draw the bar lines at staff edges
		//
		g.drawImage(muBar.getImage(),			 staffX - 1, staffY, this);
		g.drawImage(muRtDblBar.getImage(), staffX+310, staffY, this);
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

		//
		//	draw the notes
		//
		for (int i = 0; i  <numNoteheads; i++)	{
			if ( noteheadInUse[i] == 1 )	{
				if (!((!thirdVisible) && noteheadCol[i]==STAFF_COLUMN_THIRD))
				{				
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
					drawAccidental(g, noteheadCol[i], noteheadRow[i], noteheadAccid[i]);
				}
			}
		}

		// Draw Rectangles for positions where user will enter notes for Root, 3rd, 5th
		for (int i=1; i<=5 ; i+=4)
		{
			int extra = 15;
			g.drawRect(staffX + staffNoteXPos[i-1] + 7 - extra, staffY + staffNoteYPos[0] - 25,staffNoteXPos[i+1] - staffNoteXPos[i-1] + extra,175);
			//g.drawImage (muDot.getImage(), staffX + staffNoteXPos[i] + 7, staffY + staffNoteYPos[0] - 25, this );


			if (i==1)
			{
				g.drawString("Root:", staffX + staffNoteXPos[i-1] + 7, staffY + staffNoteYPos[0] - 35);
			}

			if (i==5)
			{
				g.drawString("Upper Note:", staffX + staffNoteXPos[i-1] -15 , staffY + staffNoteYPos[0] - 35);
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

	public int CreateNoteRowRep(int row, int keysig)
	{
		return( ((keysig + 14) * 1000) + row );
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

	public void drawAccidental(Graphics g, int x , int y, char accid )
	{
		//System.out.println("drawAccidental: x="+x+" y="+y+" accid="+accid);

		if ((x >= 0) && (y >= 0))
		{
			if (accid == 's')	{
				g.drawImage (muSharp.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 13
				, this );

			}
			else if (accid == 'f')	{
				g.drawImage (muFlat.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
			else if (accid == 'n')	{
				g.drawImage(muNatural.getImage(), 
									staffX + staffNoteXPos[x] - 18,
									staffY-20+ staffNoteYPos[y] - 13,
									this );
			}
	
			else if (accid == 'S')	{
				g.drawImage (muDblSharp.getImage()
				, staffX + staffNoteXPos[x] - 20
				, staffY-20+ staffNoteYPos[y] - 14
				, this );

			}
			else if (accid == 'F')	{
				g.drawImage (muDblFlat.getImage()
				, staffX + staffNoteXPos[x] - 25
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

