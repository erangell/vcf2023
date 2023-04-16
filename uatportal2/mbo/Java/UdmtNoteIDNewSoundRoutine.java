//------------------------------------------------------------------------------
// UdmtNoteID Applet -  Note Identification Drill and Exam
//------------------------------------------------------------------------------
// PARAMS:
//
// clefParm		=	The type of scale to select degrees from:
//								Values: 	TREBLE
//													BASS
//													BOTH
//								REQUIRED
//
// contentParm	=	Staff positions to be included in the drill/exam
//								Values:	STAFF_ONLY
//												LEDGERS_ONLY
//												LINES_ONLY
//												SPACES_ONLY
//												ALL
//								REQUIRED
//
// timeParm			=	Time (in seconds) for the correct answer to be given
//								Values:		positive integer
//								Default:	3 seconds
//								OPTIONAL
//
//
// examMode			=	Whether student is practicing or taking exam.
//								Values:	DRILL   (Default Value)
//												EXAM
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
//													have a version of Java that supports MIDI playback)
//								OPTIONAL: DEFAULT=NO (No suppression)
//
// drillExitURL	=	The URL to invoke when the student presses the EXIT button
//                      in Drill mode.
//								REQUIRED IF examMode = DRILL
//
//------------------------------------------------------------------------------

/*
	TO DO:
	*	Seed the random number generator from the clock

Modification History:

02/05/2005 - Changes to support exam login servlet

04/14/2006 - added piano keyboard and animation to make it more like a game

02/04/2007 - ver 0.96 - replaced sound routine
*/

//------------------------------------------------------------------------------

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import java.net.*;
import javax.sound.sampled.*;

//------------------------------------------------------------------------------

public class UdmtNoteID extends Applet implements ActionListener, ItemListener, Runnable
{
	private boolean _running = false;	//used for Runnable interface
	private boolean _gameInProgress = false;
	private boolean _playMidiNotes = false;

	private Thread _looper;

	private String udmtAppletName			=	"UdmtNoteID";
	private String udmtAppletVersion	=	"0.96"	;

	private	UdmtURLBuilder	urlBuilder;

	private String parmClefType		=	"TREBLE";		//	default = treble clef only
	
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
	private int		cursorVisible =	0 ;
	private int		saveCursorRow, saveCursorCol ;

	private int		numNoteheads =	0;			//	number of noteheads currently in the array
	private int 	noteheadInUse[];				//	whether this slot in the array is in use or not
	private int 	noteheadRow[];					//	which of the rows the notehead is in
	private int 	noteheadCol[];					//	which of the columns the notehead is in 
	private char	noteheadAccid[];				//	chromatic modifier for the notehead
	private int 	noteheadColor[];				//	0=black  1=red

	private Button checkButton;
	private Button nextButton;
	private Button beginButton;
	private Button playNoteButton,	 takeExamButton,	showCorrButton;
	private Button nextLessonButton, exitButton;
	
	private	Button A_Pitch;
	private	Button B_Pitch;
	private	Button C_Pitch;
	private	Button D_Pitch;
	private	Button E_Pitch;
	private	Button F_Pitch;
	private	Button G_Pitch;

	private final int	PITCH_BOX_Y1			=	290;
	private final int	PITCH_BOX_X1			=	112;
	private final int PITCH_BOX_WIDTH		=	 45;
	private final int	PITCH_BOX_HEIGHT	=	 28;
	
	private int currQues;												// # of current question being presented
	private	int	currentAnswer;
	private int numTimesChecked 			= 	0;
	private int numQuesPresented 			= 	0;
	private int numQuesCorrect1stTime	= 	0;

	private String strDrillMode;
	private String strExamMode;

	private String strInstruct	=	"";
	private String strInstruct1	=	"Identify the given note.";
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

//	private String strCorrect 			=	"Correct.";
	private String strCorrect 			=	"Correct";
	private String strIncorrect 		=	"Incorrect.  Please select a different pitch name.";
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

	private final int	MAX_NOTES				=	 5;
	
	private final	int	Q_DECK_MAX =	30;				//	maximum # of questons in deck	
	private				int	qClefs[];								//	clef used in question
	private				int	qNote[];								//	display note of question
	private				int	qAnswer[];							//	correct answer (0..6);

	private	final	int	SHUFFLE_TIMES	=	5;			//	# of times to shuffle question deck
	
	private	int	curQ	=	(-1);									//	number of current question
	
	private final boolean	NEVER_ACTIVE	=	false;
	
	private final	int EXAM_FB_LINE_1	=	270;			//	y-coord for exam feedback line #1
	private final int EXAM_FB_LINE_2	=	290;			//	y-coord for exam feedback line #2
	
	private final int	STAFF_ONLY			=	0;
	private final int	LEDGERS_ONLY		=	1;
	private final int	LINES_ONLY			=	2;
	private final int	SPACES_ONLY			=	3;
	private final int	ALL							= 4;
	
	private final	int	FIRST_SPACE			=	5;
	
	private final int	NO_SELECTION		=	0;
	
	private final int	NO_MODIFIER			=	0;

	private final int	STARTING_NOTE_SLOT				= 13;
	private final int	ENDING_NOTE_SLOT				= 1;

	private int	_NoteSlot				=	-1;
	private int	_CurrNote = 0; 
	private char	_CurrAc = ' ';
	
	private String treblePitchNames = "CBAGFEDCBAGFEDCBA";
	private String bassPitchNames		= "EDCBAGFEDCBAGFEDC";
	
	UdmtStopWatch	theStopWatch;
	
	private final int	TEST_PASSED		=	1;
	private final int	TEST_FAILED		=	2;
	private final int	TEST_CONTINUE	=	3;

	private Clip clipCorrect, clipWrong, clipOutOfTime;

//------------------------------------------------------------------------------

	public void init()
	{
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		strDrillMode	="Note Identification Practice";
		strExamMode		="Note Identification Exam";

		urlBuilder		= new UdmtURLBuilder();
		theStopWatch	=	new UdmtStopWatch();

		//
		//Retrieve Parameter:  clefType
		//
		parmClefType = getParameter("clefType");
		if ( parmClefType == null )	{
			parmClefType = "TREBLE";
		}
		//System.out.println ("parmClefType="+parmClefType);
		//
		//Retrieve Parameter:	 contentParm
		//
		parmContent	=	getParameter("contentParm");
		if ( parmContent.equals("STAFF_ONLY") ) {
			contentMode = STAFF_ONLY;
		}
		else if ( parmContent.equals("LEDGERS_ONLY") ) {
			contentMode = LEDGERS_ONLY;
		}
		else if ( parmContent.equals("LINES_ONLY") ) {
			contentMode = LINES_ONLY;
		}
		else if ( parmContent.equals("SPACES_ONLY") ) {
			contentMode = SPACES_ONLY;
		}
		else {
			contentMode = ALL;
		}
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
  				String sExamKey = objEk.getExamKey("NoteID"+parmClefType+parmContent+"Pass");
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
  					String sExamKey = objEk.getExamKey("NoteID"+parmClefType+parmContent+"Fail");
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

		System.out.println ("Loading sounds...");

		URL url1 = null;
		URL url2 = null;
		URL url3 = null;
		try
		{
			url1 = new URL (this.getCodeBase() + "correct.WAV");
			clipCorrect = loadWavFile(url1);
			url2 = new URL (this.getCodeBase() + "wrong.WAV");
			clipWrong = loadWavFile(url2);
			url3 = new URL (this.getCodeBase() + "outoftime.WAV");
			clipOutOfTime = loadWavFile(url3);
			System.out.println ("Loaded 3 sounds.");
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}

		//
		// Load graphics file of music symbols
		//
		try 	
		{
			MediaTracker mt	= new MediaTracker(this);
			URL url					= getCodeBase();
			imgAllSymbols		= getImage(url, "symbols-48pt-tr.gif");

			mt.addImage (imgAllSymbols, 1);
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

		noteheadInUse = new int[MAX_NOTES];
		noteheadRow		= new int[MAX_NOTES];
		noteheadCol		= new int[MAX_NOTES];
		noteheadAccid	= new char[MAX_NOTES];
		noteheadColor	= new int[MAX_NOTES];

		uData	= new int[8];
		uAcc	= new int[8];

		rectScreen = getBounds();
		staffX = 5;
		staffY = rectScreen.height / 2 - 60;

		qClefs	=	new int[ Q_DECK_MAX ];
		qNote		=	new int[ Q_DECK_MAX ];
		qAnswer	= new int[ Q_DECK_MAX ];

		GenerateQuestionDeck();
		ShuffleQuestionDeck(SHUFFLE_TIMES, Q_DECK_MAX);
		
		setLayout(null);
//
//	CHECK button setup
//
		checkButton = new Button("CHECK");
    checkButton.addActionListener(this);
		checkButton.setEnabled(NEVER_ACTIVE);
		checkButton.setBounds (180,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(checkButton);
//
//	NEXT button setup
//
    nextButton = new Button("NEXT");
    nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		nextButton.setBounds (300,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(nextButton);
//
//	BEGIN button setup
//
		beginButton = new Button("BEGIN");
		beginButton.addActionListener(this);
		beginButton.setBounds(60,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(beginButton);
//
//	PLAY NOTE button setup
//
		playNoteButton = new Button("PLAY NOTE");
		playNoteButton.addActionListener(this);

		//playNoteButton.setEnabled(!suppressingMIDI);
		playNoteButton.setEnabled(NEVER_ACTIVE);

		playNoteButton.setBounds ( 180,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(playNoteButton);
//
//	SHOW CORRECT button setup
//
		showCorrButton = new Button("SHOW CORRECT");
		showCorrButton.setEnabled(false);        
		showCorrButton.addActionListener(this);
		showCorrButton.setBounds(300,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(showCorrButton);
//
//	TAKE EXAM button setup
//
		takeExamButton = new Button("TAKE EXAM");
		takeExamButton.addActionListener(this);
		takeExamButton.setBounds ( 420,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
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
			nextLessonButton.setBounds(420,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
			add(nextLessonButton);
		}
//
//	EXIT button setup
//
		exitButton = new Button("EXIT");
		exitButton.addActionListener(this);	
		exitButton.setVisible(true);
		exitButton.setBounds(420,ROW1_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(exitButton);
//
//	INSTRUMENT button setup
//
		if ( (!examMode) && (!suppressingMIDI) )	{
			imntCombo = new Choice();
			for (int i = 0 ; i <= 127 ; i++)	{
				imntCombo.add( midiOut.GMImntList[i] );
			} 
			imntCombo.setBounds(60,ROW1_Y+1,BUTTON_WIDTH,BUTTON_HEIGHT);
			add(imntCombo);
			imntCombo.addItemListener(this);
		}
//
// Pitch name response buttons
//
		A_Pitch = new Button("A");
		A_Pitch.addActionListener(this);
		A_Pitch.setEnabled(true);
		A_Pitch.setBounds(PITCH_BOX_X1, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(A_Pitch);
		
		B_Pitch = new Button("B");
		B_Pitch.addActionListener(this);
		B_Pitch.setEnabled(true);
		B_Pitch.setBounds(PITCH_BOX_X1+50,PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(B_Pitch);
	
		C_Pitch = new Button("C");
		C_Pitch.addActionListener(this);
		C_Pitch.setEnabled(true);
		C_Pitch.setBounds(PITCH_BOX_X1+100,PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(C_Pitch);

		D_Pitch = new Button("D");
		D_Pitch.addActionListener(this);
		D_Pitch.setEnabled(true);
		D_Pitch.setBounds(PITCH_BOX_X1+150,PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(D_Pitch);
		
		E_Pitch = new Button("E");
		E_Pitch.addActionListener(this);
		E_Pitch.setEnabled(true);
		E_Pitch.setBounds(PITCH_BOX_X1+200,PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(E_Pitch);
	
		F_Pitch = new Button("F");
		F_Pitch.addActionListener(this);
		F_Pitch.setEnabled(true);
		F_Pitch.setBounds(PITCH_BOX_X1+250, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(F_Pitch);

		G_Pitch = new Button("G");
		G_Pitch.addActionListener(this);
		G_Pitch.setEnabled(true);
		G_Pitch.setBounds(PITCH_BOX_X1+300, PITCH_BOX_Y1, PITCH_BOX_WIDTH, PITCH_BOX_HEIGHT);
		add(G_Pitch);
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
		String	accString = "Ff sS";
		
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

	private int ChecktTestCompletionStatus()
	{
	
		//System.out.println("numQuesPresented      = " + numQuesPresented);
		//System.out.println("num_reqd_to_pass      = " + num_reqd_to_pass);
		//System.out.println("numQuesCorrect1stTime = " + numQuesCorrect1stTime);
		//System.out.println("num_exam_ques         = " + num_exam_ques);


		if ( numQuesCorrect1stTime == num_reqd_to_pass ) {
			//System.out.println("TEST_PASSED");
			return( TEST_PASSED );
		}
		else
		{	if ( (numQuesPresented - numQuesCorrect1stTime) <= (num_exam_ques - num_reqd_to_pass) ) {
				//System.out.println("TEST_CONTINUE");
				return( TEST_CONTINUE );
			}
			else {
				//System.out.println("TEST_FAILED");
				return( TEST_FAILED );
			}
		}
	}

//------------------------------------------------------------------------------

	private void DoJudging(ActionEvent e)
	{
		gotThisQuesCorrect=0;

		numQuesPresented++;
//		if ( numTimesChecked == 0)	{
//			numQuesPresented++;
//		}
	
		if ( qAnswer[currQues] == currentAnswer ) {
			double	floatTime;
				
			theStopWatch.HaltWatch();
	
			if ( numTimesChecked == 0)	{
				numQuesCorrect1stTime++;
				gotThisQuesCorrect = 1;
			}	

			floatTime = (double) ( theStopWatch.GetElapsedTime() / 1000.0 );
			strFeedback = strCorrect + " in " + floatTime + " seconds.";			
			
			playSound ("correct.wav",0);
			processNextButton();						

//			nextButton.setEnabled(true);
	
//			checkButton.setEnabled(false);					//	can only be clicked once
//			numTimesChecked++;
		}
		else {
			if ( examMode )	{
				strFeedback = strIncorrectExam;
				checkButton.setEnabled(false);
				nextButton.setEnabled(true);
			}
			else	{
				strFeedback = strIncorrect;
				EraseAllNoteheadsInColumn(_NoteSlot);
				playSound ("wrong.wav",0);
				processNextButton();						
			}

//			numTimesChecked++;

//			if (numTimesChecked >= 2)	{
//				if ( !examMode ) {
//					showCorrButton.setEnabled(true);
//				}
//				nextButton.setEnabled(true);
//			}

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

			EraseAllNoteheadsInColumn(_NoteSlot);
			_NoteSlot = -1;	
			PresentCurrentQuestion(currQues);

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			if ( !examMode )	{
				showCorrButton.setEnabled(false);
			}
			strFeedback = "";

			if ( examMode ) {
				switch ( ChecktTestCompletionStatus() ) {
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
			repaint();


	}

	public void actionPerformed(ActionEvent e)
	{
		char	ac			= '?';
		int		uAccVal	= 0;
		//
		//	PITCH answer button Servicing
		//
		if ( e.getActionCommand().equals("A") ) {
			currentAnswer = 0;
			DoJudging(e);
		}
		else if ( e.getActionCommand().equals("B") ) {
			currentAnswer = 1;
			DoJudging(e);
		} 
		else if ( e.getActionCommand().equals("C") ) {
			currentAnswer = 2;
			DoJudging(e);
		} 
		else if ( e.getActionCommand().equals("D") ) {
			currentAnswer = 3;
			DoJudging(e);
		} 
		else if ( e.getActionCommand().equals("E") ) {
			currentAnswer = 4;
			DoJudging(e);
		} 
		else if ( e.getActionCommand().equals("F") ) {
			currentAnswer = 5;
			DoJudging(e);
		} 
		else if ( e.getActionCommand().equals("G") ) {
			currentAnswer = 6;
			DoJudging(e);
		} 
	//
		//	CHECK BUTTON Servicing
		//
		if (e.getActionCommand().equals("CHECK") )	{
			DoJudging(e);
		}
		//
		//	NEXT BUTTON Servicing
		//
		if ( e.getActionCommand().equals("NEXT") )	{
			processNextButton();
		}
		//
		//	PLAY NOTE BUTTON Servicing
		//
		if ( e.getActionCommand().equals("PLAY NOTE") )	{
			int uRow;
			int	uAcc;
			int	midiTime = 0;
	
			uRow = GetNoteRepRow( qNote[currQues] );
			uAcc = GetNoteRepAcc( qNote[currQues] );

			if (  qClefs[currQues] == TREBLE_CLEF )	{
				uMIDI = MidiTreble[ uRow ] + uAcc ;
			}
			else	{
				uMIDI = MidiBass[ uRow ] + uAcc ;
			}

			if ( !suppressingMIDI )	{
				midiOut.createSequence();
				midiOut.addProgChg(0, userInstrument );
				midiOut.addNoteOn(0, uMIDI);	
				midiOut.addNoteOff(22, uMIDI );
			}
			midiTime= 24;

			if ( !suppressingMIDI ) {
				midiOut.addEndOfTrack(midiTime);
				playMIDI( 120,0 );
			}
		}
		//
		//	SHOW CORRECT Servicing
		//
		if ( e.getActionCommand().equals("SHOW CORRECT") )	{
			char 	pName;
			int	 	theRow;
			String	allPitchNames = "ABCDEFG";
			
			theRow = GetNoteRepRow(qNote[currQues]);
			pName  = RowToPitchChar(qNote[currQues], qClefs[currQues]);			
			strFeedback = "The correct pitch name is " + pName + ".";
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
		//	NEXT LESSON Servicing
		//
		if ( e.getActionCommand().equals("NEXT LESSON") )	{
			if (nextURL != null)	{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		//
		//	BEGIN BUTTON Servicing
		//
		if ( e.getActionCommand().equals("BEGIN") )	{
			_gameInProgress = true;
			beginButton.setEnabled(false);
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

	public int ChooseStaffPos(int dMode)
	{
		int	n	=	0;

		switch ( dMode ) {
			case STAFF_ONLY:
				n	= randInt(4, 12 );						//	choose staff line/space
				break;
				
			case LEDGERS_ONLY:														//	choose ledger line/space
				n =  randInt(0,3);
				if ( randInt(1,100) > 50 ) {
					n += 13;
				}
				break;
		
			case LINES_ONLY:
				n = ( (randInt(2,6) * 2) );							//	choose staff line
				break;
				
			case SPACES_ONLY:
				n = ( (randInt(0,3)  * 2) + FIRST_SPACE );	//	choose staff space
				break;
		
			case ALL:
				n = randInt(0, 16) ;							// choose any staff postion
		}
		
		return(n);
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
//	Generate a "deck" of questions to be presented
//------------------------------------------------------------------------------

	public void GenerateQuestionDeck()
	{
		int		q;
		int		noteRow;
		int		prevRow = (-1);
		char	r;
		
		for (q = 0; q < Q_DECK_MAX; q++) {
			qClefs[q]  = ChooseClef(parmClefType);							// Select clef
			
			do {	
				noteRow = ChooseStaffPos(contentMode);
			} while ( noteRow == prevRow );

			qNote[q]	 = CreateNoteRep(noteRow, NO_MODIFIER	);	// Select given note		
			r					 = RowToPitchChar(qNote[q], qClefs[q]);
			qAnswer[q] = PitchNameToNumber(r);									// Set correct answer
			
			prevRow = noteRow;
		}
	}

//------------------------------------------------------------------------------

	public void	ShuffleQuestionDeck(int sTimes, int totalQ)
	{
		int tempClef;					//	swap variable for question clef
		int tempNote;					//	swap variable for question note
		int tempAnswer;				//	swap variable for question answer
		int	i;								//	shuffle loop counter variable
		int	n;								//	shuffle slot variable

		for (i = 0; i < sTimes; i++) {
			n = randInt(0, totalQ - 1);
			
			tempClef	 = qClefs[i];						
			tempNote	 = qNote[i];
			tempAnswer = qAnswer[i];
			
			qClefs[i]  = qClefs[n];
			qNote[i]	 = qNote[n];
			qAnswer[i] = qAnswer[n];
			
			qClefs[n]	 = tempClef;
			qNote[n]	 = tempNote;
			qAnswer[n] = tempAnswer;
		}
	}

//------------------------------------------------------------------------------
	
	public void PresentCurrentQuestion(int q)
	{
		int		startNote	= 0;
		char	ac				= ' ';
		int		note			= 0;

		currentClef	= qClefs[q];
 		
		startNote = GetNoteRepRow(qNote[q]);
		ac				=	accidToChar( GetNoteRepAcc(qNote[q]) );

		EraseAllNoteheadsInColumn(_NoteSlot);						//	Display question note
		AddNoteheadToStaff(_NoteSlot, startNote, ac);
		_CurrNote = startNote;
		_CurrAc = ac;
		_NoteSlot = STARTING_NOTE_SLOT;

		theStopWatch.StartWatch();
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

		centerString(g, strScoreDisp,			txtFont,  85);
		centerString(g, strFeedback,			txtFont, 280);
		centerString(g, strExamFeedback1, txtFont, EXAM_FB_LINE_1);
		centerString(g, strExamFeedback2, txtFont, EXAM_FB_LINE_2);
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
		
		strInstruct	 = strInstruct1 + strInstruct2 + strInstruct3;
		centerString(g, strInstruct, txtFont, 60);
		
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
		if ( currentClef == TREBLE_CLEF )	{
			g.drawImage(muTrebleClef.getImage(),staffX+gClefXOffset,staffY+gClefYOffset,this);
		}
		else	{
			g.drawImage(muBassClef.getImage(),staffX+fClefXOffset,staffY+fClefYOffset,this);
		}
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
				drawAccidental(g, noteheadCol[i], noteheadRow[i], noteheadAccid[i]);
			}
		}
	
		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);			
		displayScore(g);					//	display current score


		for (int offset = 0 ; offset <= 24 ; offset += 3)
		{
			g.drawImage(muBar.getImage(),staffNoteXPos[1]+offset, staffY, this);
		}
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
//	Note Representation Methods
//------------------------------------------------------------------------------

	public int CreateNoteRep(int row, int acc)
	{
		return( ((acc + 2) * 100) + row );
	}

//------------------------------------------------------------------------------

	public int GetNoteRepRow(int n)
	{
		return( n % 100);
	}

//------------------------------------------------------------------------------

	public int GetNoteRepAcc(int n)
	{
		return( (n / 100) - 2 );
	}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

	public void drawNotehead(Graphics g, int x , int y, char nhtype )
	{	
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


//------------------------------------------------------------------------------
	

	public Clip loadWavFile(URL object) 
	{
		Object currentSound = null;
		Clip clip = null;
		try 
		{
			currentSound = AudioSystem.getAudioInputStream((URL) object);
		} 
		catch(Exception e) 
		{
			System.out.println(e.toString());
		}

		if (currentSound instanceof AudioInputStream) 
		{
			try 
			{
				AudioInputStream stream = (AudioInputStream) currentSound;
				AudioFormat format = stream.getFormat();

				/**
				 * we can't yet open the device for ALAW/ULAW playback,
				 * convert ALAW/ULAW to PCM
				 */
				if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
					(format.getEncoding() == AudioFormat.Encoding.ALAW)) 
				{
					AudioFormat tmp = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, 
						format.getSampleRate(),
						format.getSampleSizeInBits() * 2,
						format.getChannels(),
						format.getFrameSize() * 2,
						format.getFrameRate(),
						true);
					stream = AudioSystem.getAudioInputStream(tmp, stream);
					format = tmp;
				}
				DataLine.Info info = new DataLine.Info(
					Clip.class, 
					stream.getFormat(), 
					((int) stream.getFrameLength() *
					format.getFrameSize()));

				clip = (Clip) AudioSystem.getLine(info);
				clip.open(stream);
			} 
			catch (Exception ex) 
			{ 
				ex.printStackTrace(); 
				currentSound = null;
				return null;
			}
		} 
		return clip;
	}

	public void playClip(Clip currentSound) 
	{
		Clip clip = (Clip) currentSound;
		clip.start();
		while (clip.isActive()) 
		{
		}
		clip.stop();
		//clip.close();
	}
//------------------------------------------------------------------------------
	public void playSound(String soundFile, int delay)
	{
		if (soundFile.equals("correct.wav"))
		{
			System.out.println("CORRECT");
			playClip (clipCorrect);
		}
		else if (soundFile.equals("wrong.wav"))
		{
			System.out.println("WRONG");
			playClip (clipWrong);
		}
		else if (soundFile.equals("outoftime.wav"))
		{
			System.out.println("OUT OF TIME");
			playClip (clipOutOfTime);
		}
		
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

//------------------------- IMPLEMENTATION OF RUNNABLE INTERFACE ---------------

    public void start() {
        if(_looper == null) {
            _running = true; // this will be set by new button
            _looper = new Thread(this);
            _looper.start();
        }
    }
    public void stop() {
        _running = false;
	_looper.interrupt();
    }
    public void run() {
        try {
            while(_running) {
	     if (_gameInProgress)
	     {
		if (_NoteSlot > ENDING_NOTE_SLOT)
		{
			EraseAllNoteheadsInColumn(_NoteSlot);						
			_NoteSlot--;
			AddNoteheadToStaff(_NoteSlot, _CurrNote, _CurrAc);


			int uRow;
			int	uAcc;
			int	midiTime = 0;
	
			uRow = GetNoteRepRow( qNote[currQues] );
			uAcc = GetNoteRepAcc( qNote[currQues] );

			if (  qClefs[currQues] == TREBLE_CLEF )	{
				uMIDI = MidiTreble[ uRow ] + uAcc ;
			}
			else	{
				uMIDI = MidiBass[ uRow ] + uAcc ;
			}

			if ( !suppressingMIDI )	{
				if (_NoteSlot >= (STARTING_NOTE_SLOT - 1))
				{
					midiOut.createSequence();
					midiOut.addProgChg(0, userInstrument );
					midiOut.addNoteOn(8, uMIDI);	
					midiOut.addNoteOff(44, uMIDI );
					midiOut.addEndOfTrack(48);
					playMIDI( 120,0 );
				}
			}

		}
		else if (_NoteSlot == ENDING_NOTE_SLOT)
		{
			EraseAllNoteheadsInColumn(_NoteSlot);						
			_NoteSlot = -1;
			playSound ("outoftime.wav",2000);
			processNextButton();
			numQuesPresented++;

		}
                repaint();
	      }
              _looper.sleep(200);		 
            }
        } catch(InterruptedException e) {
            _running = false;
        }
    }
//------------------------------------------------------------------------------



}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

