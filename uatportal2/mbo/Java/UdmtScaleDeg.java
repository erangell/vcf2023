//------------------------------------------------------------------------------
// UdmtScaleDegree Applet -  Scale Degree Dril & Exam for Major and Minor Scales
//------------------------------------------------------------------------------
// PARAMS:
//
// scaleType		=	The type of scale to select degrees from:
//								Values: 	MAJOR
//													MINOR
//								REQUIRED
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
	*	Generate questions in a randomized deck
	MODIFICATION HISTORY:
	03/20/2005: ver 0.51: Adding logic for automatic redirection upon exam pass/fail
			also removed use of ExamLog object - no longer needed
	03/20/2005: ver 0.52: modified to use Exam keys - for servlets or php
	06/29/2006: ver 0.53: enhacements for minor scale degrees
	12/26/2008: ver 0.54: changes to text
*/

//------------------------------------------------------------------------------

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;

//------------------------------------------------------------------------------

public class UdmtScaleDeg extends Applet implements ActionListener, ItemListener
{
	private String udmtAppletName			=	"UdmtScaleDegree";
	private String udmtAppletVersion	=	"0.54"	;

	private	UdmtURLBuilder	urlBuilder;

	private String parmScaleType			=	"MAJOR";					//	default = major scale only
	private String dispScaleType			=	"";
	
	private String parmExamMode 			=	"";

	private String parmNextURL 				=	"";
	private URL		 nextURL;

	private String parmDrillURL 			=	"";
	private URL drillURL;

	private String parmDrillExitURL		=	"";
	private URL drillExitURL;

	private boolean showErrorScreen		=	false;
	private String txtError						=	"";

	private UdmtMidiOutQueue	midiOut;
	private UdmtMidiInQueue		midiIn;

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

	// These are half steps above C for the starting note of each major key (0-7 sharps, 0-7 flats)
	private int cyc5shOffsetMaj[] = {0,7,2,9,4,11,6,1};
	private int cyc5flOffsetMaj[] = {0,5,10,3,8,1,6,11}; 

	// Accidentals for starting note of each major key
	private char cyc5shAccMaj[]	=	{' ',' ',' ',' ',' ',' ','s','s'};
	private char cyc5flAccMaj[]	=	{' ',' ','f','f','f','f','f','f'};	

	// Staff Y positions on treble/bass clefs for sharps and flats for starting note of each key for Ascending scale
	private int cyc5shTrebMaj[]	=	{14,10,13, 9,12, 8,11,14};
	private int cyc5shBassMaj[]	=	{16,12,15,11,14,10,13, 9}; 
	private int cyc5flTrebMaj[]	=	{14,11,15,12, 9,13,10,14}; 
	private int cyc5flBassMaj[]	=	{16,13,10,14,11,15,12, 9}; 

	// Half steps above C for the starting note of each minor key (0-7 sharps, 0-7 flats)
	private int cyc5shOffsetMin[]	=	{9,4,11,6,1,8,3,10};
	private int cyc5flOffsetMin[]	=	{9,2,7,0,5,10,3,8}; 

	// Accidentals for starting note of each minor key
	private char cyc5shAccMin[]	=	{' ',' ',' ','s','s','s','s','s'};
	private char cyc5flAccMin[]	=	{' ',' ',' ',' ',' ','f','f','f',};	

	// Staff Y positions on treble and bass clefs for sharps and flats for starting note of each key
	private int cyc5shTrebMin[]	=	{9,12,15,11,14,10,13,9};
	private int cyc5shBassMin[]	= {11,14,10,13,16,12,15,11}; 
	private int cyc5flTrebMin[]	=	{9,13,10,14,11,15,12,9}; 
	private int cyc5flBassMin[]	= {11,15,12,16,13,10,14,11}; 

	private int MidiTreble[]	=	{84,83,81,79,77,76,74,72,71,69,67,65,64,62,60,59,57};
	private int MidiBass[]		=	{64,62,60,59,57,55,53,52,50,48,47,45,43,41,40,38,36};
	private int ScaleBeingTested[];

	private int MajorScaleAsc[] 				=	{2,2,1,2,2,2,1};
	private int NaturalMinorScaleAsc[]	=	{2,1,2,2,1,2,2};
	private int HarmonicMinorScaleAsc[]			= 	{2,1,2,2,1,3,1};
	private int MelodicMinorScaleAsc[]			= 	{2,1,2,2,2,2,1};

	//----------------------------------------------------------------------

	private boolean	userTestQuesInUse[];
	private int			userTestQuestions[];
	private int			userTestClefs[];
	private int			userTestMinorType[];
	
	private int			gotThisQuesCorrect;

	private int uData[];
	private int uAcc[];
	private int uMIDI;
	private int progCh	=	0; 		// imnt# will be param.  0=general midi grand piano

	//----------------------------------------------------------------------
	
	private int		saveX, saveY;
	
	private int		currentClef;
	private int		startCursorRow, startCursorCol;
	private int		cursorRow, cursorCol;
	private int		cursorVisible =	0 ;
	private int		saveCursorRow, saveCursorCol ;

	private int		numNoteheads =	0;			//	number of noteheads currently in the array
	private int 	noteheadInUse[];				//	whether this slot in the array is in use or not
	private int 	noteheadRow[];					//	which of the rows the notehead is in
	private int 	noteheadCol[];					//	which of the columns the notehead is in 
	private char	noteheadAccid[];				//	chromatic modifier for the notehead
	private int 	noteheadColor[];				//	0=black  1=red

	private Button checkButton,				nextButton,			playmineButton;
	private Button playcorrButton,		takeExamButton,	showCorrButton;
	private Button nextLessonButton,	exitButton;

	private int currQues;												// # of current question being presented
	private	int	currentAnswer;
	private int numTimesChecked 			= 	0;
	private int numQuesPresented 			= 	0;
	private int numQuesCorrect1stTime	= 	0;

	private String strDrillMode	=	"Major Scale Function Practice";
	private String strExamMode	=	"Major Scale Function Exam";

	private String strInstruct	=	"";
	private String strInstruct1	=	"Select the correct scale function name ";
	private String strInstruct2 =	"";
	private String strInstruct3	=	" for the second note.";
	private String strMinorType =	"";

	private String strScore1 	= 	"Score: ";
	private int nScore1 			= 	0;
	private String strScore2 	= 	" out of ";
	private int nScore2 			= 	0;
	private String strScore3 	= 	" = ";
	private int nScorePercent	= 	0;
	private String strScore4 	= 	"%";
	private String strScoreDisp;

	private String strCorrect 			=	"Correct.";
	private String strIncorrect 		=	"Incorrect.  Please select a different scale function name.";
	private String strIncorrectExam	=	"Incorrect.";

	private String strMaster1 			= "Congratulations.  You have provided 9 out of 10";
	private String strMaster2 			= "correct answers and may  move on if you wish.";
	private String strFeedback 			= "";
	private String strExamFeedback1	= "";
	private String strExamFeedback2	= "";

	private int saveCursorX;
	private int	saveCursorY;

	private boolean examMode;

	private int prevQues	= 	100; 	// 100 indicates first time - no prev ques

	private Choice	imntCombo;
	private int			userInstrument	=	0 ;

	private Choice	degreeAnswer;

	private int mixedMinorAscMode	=	0;
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
	private final int	BASS_CLEF				=	(-1);
	
	private final	int	BLACK_NOTE			=	 0;
	private final int	RED_NOTE				=	 1;

	private final int	MAX_NOTES				=	50;
	
	private	final	int	TONIC						=	 0;
	private final int	SUPER_TONIC			=	 1;
	private final int	MEDIANT					=	 2;
	private final int	SUBDOMINANT			=	 3;
	private final int	DOMINANAT				=	 4;
	private final int	SUBMEDIANT			=	 5;
	private final int	SUBTONIC				=	 6;
	private	final int	LEADING_TONE		=	 7;
	
	private final int	MAJOR_SCALE			=	 1;
	private final int	MINOR_SCALE			=	 2;

	private	String degreeNames[]	=	{"Tonic",   "Supertonic", "Mediant",  "Subdominant",
	                                 "Dominant", "Submediant", "Subtonic", "Leading Tone"};
	
	private int majScaleGen[][]	=	{
				//        T ST ME SD DO SM LT
					{-1,-1,-1,-1,-1,-1,-1},					// Cb Major
					{-1,-1,-1,-1,-1,-1, 0},					// Gb Major
					{-1,-1, 0,-1,-1,-1, 0},					// Db Major
					{-1,-1, 0,-1,-1, 0, 0},					// Ab Major
					{-1, 0, 0,-1,-1, 0, 0},					// Eb Major
					{-1, 0, 0,-1, 0, 0, 0},					// Bb Major
					{ 0, 0, 0,-1, 0, 0, 0},					// F  Major
					{ 0, 0, 0, 0, 0, 0, 0},         // C  Major
					{ 0, 0, 0, 0, 0, 0, 1},					// G  Major
					{ 0, 0, 1, 0, 0, 0, 1},					// D  Major
					{ 0, 0, 1, 0, 0, 1, 1},					// A  Major
					{ 0, 1, 1, 0, 0, 1, 1},					// E  Major
					{ 0, 1, 1, 0, 1, 1, 1},					// B  Major
					{ 1, 1, 1, 0, 1, 1, 1},					// F# Major
					{ 1, 1, 1, 1, 1, 1, 1}					// C# Major
	                             	 };
	
		private int minScaleGen[][]	=	{
				//        T ST ME SD DO SM ST
					{-1,-1,-1,-1,-1,-1,-1},					// ab Minor
					{-1, 0,-1,-1,-1,-1,-1},					// eb Minor
					{-1, 0,-1,-1, 0,-1,-1},					// bb Minor
					{ 0, 0,-1,-1, 0,-1,-1},					// f  Minor
					{ 0, 0,-1, 0, 0,-1,-1},					// c  Minor
					{ 0, 0,-1, 0, 0,-1, 0},					// g  Minor
					{ 0, 0, 0, 0, 0,-1, 0},					// d  Minor
					{ 0, 0, 0, 0, 0, 0, 0},         // a  Minor
					{ 0, 1, 0, 0, 0, 0, 0},					// e  Minor
					{ 0, 1, 0, 0, 1, 0, 0},					// b  Minor
					{ 1, 1, 0, 0, 1, 0, 0},					// f# Minor
					{ 1, 1, 0, 1, 1, 0, 0},					// c# Minor
					{ 1, 1, 0, 1, 1, 0, 1},					// g# Minor
					{ 1, 1, 1, 1, 1, 0, 1},					// d# Minor
					{ 1, 1, 1, 1, 1, 1, 1},					// a# Minor
	                              };

		private int harmonicMinScaleGen[][]	=	{
				//        T ST ME SD DO SM LT
					{-1,-1,-1,-1,-1,-1, 0},					// ab Minor
					{-1, 0,-1,-1,-1,-1, 0},					// eb Minor
					{-1, 0,-1,-1, 0,-1, 0},					// bb Minor
					{ 0, 0,-1,-1, 0,-1, 0},					// f  Minor
					{ 0, 0,-1, 0, 0,-1, 0},					// c  Minor
					{ 0, 0,-1, 0, 0,-1, 1},					// g  Minor
					{ 0, 0, 0, 0, 0,-1, 1},					// d  Minor
					{ 0, 0, 0, 0, 0, 0, 1},         // a  Minor
					{ 0, 1, 0, 0, 0, 0, 1},					// e  Minor
					{ 0, 1, 0, 0, 1, 0, 1},					// b  Minor
					{ 1, 1, 0, 0, 1, 0, 1},					// f# Minor
					{ 1, 1, 0, 1, 1, 0, 1},					// c# Minor
					{ 1, 1, 0, 1, 1, 0, 2},					// g# Minor
					{ 1, 1, 1, 1, 1, 0, 2},					// d# Minor
					{ 1, 1, 1, 1, 1, 1, 2},					// a# Minor
	                              };
		private int melodicMinScaleGen[][]	=	{
				//        T ST ME SD DO SM LT
					{-1,-1,-1,-1,-1, 0, 0},					// ab Minor
					{-1, 0,-1,-1,-1, 0, 0},					// eb Minor
					{-1, 0,-1,-1, 0, 0, 0},					// bb Minor
					{ 0, 0,-1,-1, 0, 0, 0},					// f  Minor
					{ 0, 0,-1, 0, 0, 0, 0},					// c  Minor
					{ 0, 0,-1, 0, 0, 0, 1},					// g  Minor
					{ 0, 0, 0, 0, 0, 0, 1},					// d  Minor
					{ 0, 0, 0, 0, 0, 1, 1},         // a  Minor
					{ 0, 1, 0, 0, 0, 1, 1},					// e  Minor
					{ 0, 1, 0, 0, 1, 1, 1},					// b  Minor
					{ 1, 1, 0, 0, 1, 1, 1},					// f# Minor
					{ 1, 1, 0, 1, 1, 1, 1},					// c# Minor
					{ 1, 1, 0, 1, 1, 1, 2},					// g# Minor
					{ 1, 1, 1, 1, 1, 1, 2},					// d# Minor
					{ 1, 1, 1, 1, 1, 2, 2},					// a# Minor
	                              };
                
                                                
	private final	int	Q_DECK_MAX =	45;				//	maximum # of questons in deck	
	private				int	qMode[];								//	major or minor mode
	private				int	qKey[];									//	key for the question
	private				int	qClefs[];								//	clef used in question
	private				int	qNote1[];								//	1st display note of question
	private				int	qDeg1[];								//	scale degree # of 1st note
	private				int	qNote2[];								//	2nd display note of question
	private				int	qDeg2[];								//	scale degree # of 2nd note (answer)
	private				String  qDispMinorType[];							// minor type display

	private	int	curQ	=	(-1);									//	number of current question
	
	private final boolean	NEVER_ACTIVE	=	false;
	
	private final	int EXAM_FB_LINE_1	=	270;			//	y-coord for exam feedback line #1
	private final int EXAM_FB_LINE_2	=	290;			//	y-coord for exam feedback line #2
	
//------------------------------------------------------------------------------

	public void init()
	{
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

 		urlBuilder = new UdmtURLBuilder();

		//
		//Retrieve Parameter:  scaleType
		//
		ScaleBeingTested = new int[7];
	
		parmScaleType = getParameter ("scaleType");
		if (parmScaleType == null)	{
			showErrorScreen = true;
			txtError = "Param: ScaleType is required";
			System.out.println("The value for the parameter ScaleType must be one of the following:");
			System.out.println("MAJOR or MINOR");
		}
		else if (parmScaleType.equals("MAJOR"))	{
			for (int i=0 ; i<=6 ; i++)		{
				ScaleBeingTested[i] = MajorScaleAsc[i];
			}
			dispScaleType="";
			strDrillMode	="Major Scale Function Names Practice";
			strExamMode		="Major Scale Function Names Exam";
		}
		else if  (parmScaleType.equals("MINOR"))	{
			pickRandomMinorScale();
			strDrillMode	=	"Minor Scale Function Names Practice";
			strExamMode		=	"Minor Scale Function Names Exam";
		}
		//
		//Retrieve Parameter:  examMode
		//
		parmExamMode = getParameter("examMode");
		examMode		 = parmExamMode.equals("EXAM");
		//
		//Retrieve Parameter:  nextURL
		//
		parmNextURL = getParameter("nextURL");
		if ( parmNextURL == null )	{
			showErrorScreen = true;
			txtError = "Parameter nextURL is required";
		}
		else	{
 			//02-05-2005 - logic for exam keys
 			if (examMode)
  			{
  				UdmtExamKey objEk = new UdmtExamKey();
   				String sExamKey = objEk.getExamKey("ScaleDeg"+parmScaleType+"Pass");
   				parmNextURL = parmNextURL + "?r="+sExamKey;
   				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY
  			}
  			else
  			{
 				//Note 2/5/05: Previous version wasn't calling urlBuilder - retrofitted code here:
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
				showErrorScreen = true;
				txtError = "Parameter drillURL is required for Exam Mode";
			}
			else	{

 				//02-05-2005 - logic for exam keys
 				if (examMode)
  				{
  					UdmtExamKey objEk = new UdmtExamKey();
   					String sExamKey = objEk.getExamKey("ScaleDeg"+parmScaleType+"Fail");
 	  				parmDrillURL = parmDrillURL + "?r="+sExamKey;
   					//System.out.println ("parmDrillURL="+parmDrillURL); //FOR DEBUGGING ONLY
  				}
  				else
 	 			{
 					//Note 2/5/05: Previous version wasn't calling urlBuilder - retrofitted code here:
  					parmDrillURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillURL);
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

			if ( parmDrillExitURL == null )	{
				showErrorScreen	= true;
				txtError				= "Parameter drillExitURL is required for Drill Mode";
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
		parmSuppressMIDI = getParameter ("suppressMIDI");
		if ( parmSuppressMIDI != null )	{
			if (parmSuppressMIDI.equals("YES"))	{
				suppressingMIDI = true ;
				System.out.println ("MIDI suppression is in effect.");
			}
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

		userTestQuesInUse	= new boolean[Q_DECK_MAX];
		userTestQuestions	= new int[Q_DECK_MAX];
		userTestClefs			=	new int[Q_DECK_MAX];
		userTestMinorType	=	new int[Q_DECK_MAX];

		uData	= new int[8];
		uAcc	= new int[8];

		rectScreen = getBounds();
		staffX = 5;
		staffY = rectScreen.height / 2 - 60;

		qMode		=	new int[ Q_DECK_MAX ];
		qKey		=	new int[ Q_DECK_MAX ];
		qClefs	=	new int[ Q_DECK_MAX ];
		qNote1	=	new int[ Q_DECK_MAX ];
		qDeg1		=	new int[ Q_DECK_MAX ];
		qNote2	=	new int[ Q_DECK_MAX ];
		qDeg2		=	new int[ Q_DECK_MAX ];
		qDispMinorType  = new String[ Q_DECK_MAX ];

		GenerateQuestionDeck();
		
		setLayout(null);
//
//	CHECK button setup
//
		checkButton = new Button("CHECK");
    checkButton.addActionListener(this);
		checkButton.setEnabled(false);
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
//	PLAY MINE button setup
//
		playmineButton = new Button("PLAY MINE");
		playmineButton.addActionListener(this);
		playmineButton.setEnabled(NEVER_ACTIVE);
		playmineButton.setBounds(60,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(playmineButton);
//
//	PLAY NOTES button setup
//
		playcorrButton = new Button("PLAY NOTES");
		playcorrButton.addActionListener(this);
		playcorrButton.setEnabled(!suppressingMIDI);
		playcorrButton.setBounds ( 180,ROW2_Y,BUTTON_WIDTH,BUTTON_HEIGHT);
		add(playcorrButton);
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
//	DEGREE_ANSWER popup menu setup
//
		degreeAnswer = new Choice();
		degreeAnswer.add("Select a Function Name");
		
		for (int i = 0 ; i <= 7 ; i++)	{
			degreeAnswer.add( degreeNames[i] );
		}

		degreeAnswer.setBounds(310,290,BUTTON_WIDTH + 45,BUTTON_HEIGHT + 5);
		add(degreeAnswer);
		degreeAnswer.addItemListener(this);
		
		// generate a question deck
		
		currQues = 0;
		PresentCurrentQuestion(currQues);
	}

//------------------------------------------------------------------------------

	public void pickRandomMinorScale()
	{
		int rnd = randInt(1,3);
		if (rnd == 1)
		{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = NaturalMinorScaleAsc[i];
			}
			dispScaleType	=	"Natural Minor";
		}
		else if (rnd == 2)
		{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = HarmonicMinorScaleAsc[i];
			}
			dispScaleType	=	"Harmonic Minor";
		}
		else if (rnd == 3)
		{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = MelodicMinorScaleAsc[i];
			}
			dispScaleType	=	"Melodic Minor";
		}

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

	public void actionPerformed(ActionEvent e)
	{
		char	ac			= '?';
		int		uAccVal	= 0;
		//
		//	CHECK BUTTON Servicing
		//
		if (e.getActionCommand().equals("CHECK") )	{
			gotThisQuesCorrect=0;

			if ( numTimesChecked == 0)	{
				numQuesPresented++;
			}

			currentAnswer = degreeAnswer.getSelectedIndex() - 1;
			if ( qDeg2[currQues] == currentAnswer ) {
				if ( numTimesChecked == 0)	{
					numQuesCorrect1stTime++;
					gotThisQuesCorrect = 1;
				}	

				strFeedback = strCorrect;			
				nextButton.setEnabled(true);
	
				checkButton.setEnabled(false);					//	can only be clicked once
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
					if ( !examMode )		{
						showCorrButton.setEnabled(true);
					}
					nextButton.setEnabled(true);
				}
			}
			
			if ( examMode )	{
				if (  ((numQuesPresented == num_reqd_to_pass) && (numQuesCorrect1stTime == num_reqd_to_pass))
				   || ((numQuesPresented == num_exam_ques) && (numQuesCorrect1stTime == num_reqd_to_pass))
				   )
				{
					strExamFeedback1="Congratulations!";
					strExamFeedback2="You passed this exam and may move on to the next lesson.";
					exitButton.setVisible(false);
					nextLessonButton.setVisible(true);
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);
					this.getAppletContext().showDocument(nextURL);
				}
				
				if (  ((numQuesPresented == num_exam_ques) && (numQuesCorrect1stTime <  num_reqd_to_pass))
				   || (	(numQuesPresented < num_exam_ques) 
						&& (numQuesCorrect1stTime <  (numQuesPresented - (num_exam_ques - num_reqd_to_pass)))
					)
				   )
				{
					if ((num_exam_ques - num_reqd_to_pass) == 1)	{
						strExamFeedback1="You missed more than 1 question and will need to retake";
					}
					else	{
						strExamFeedback1="You missed more than " + (num_exam_ques - num_reqd_to_pass) + " questions and will need to retake";
					}

					strExamFeedback2="this exam before you may move on to the next lesson. ";
					exitButton.setVisible(true);
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);
					this.getAppletContext().showDocument(drillURL);
				}
			} // examMode
			repaint();
		}
		//
		//	NEXT BUTTON Servicing
		//
		if (e.getActionCommand().equals("NEXT"))	{
			currQues++;
			if ( currQues == Q_DECK_MAX ) {
				GenerateQuestionDeck();
				currQues = 0;
			}

			numTimesChecked = 0;

			EraseAllNoteheadsInColumn(0);
			EraseAllNoteheadsInColumn(8);	
			PresentCurrentQuestion(currQues);

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			if ( !examMode )	{
				showCorrButton.setEnabled(false);
			}
			strFeedback = "";
			

			repaint();
		}
		//
		//	PLAY NOTES BUTTON Servicing
		//
		if ( e.getActionCommand().equals("PLAY NOTES") )	{
			int uRow;
			int	uAcc;
			int	midiTime = 0;
	
			uRow = GetNoteRepRow( qNote1[currQues] );
			uAcc = GetNoteRepAcc( qNote1[currQues] );

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

			uRow = GetNoteRepRow( qNote2[currQues] );
			uAcc = GetNoteRepAcc( qNote2[currQues] );
			if ( qClefs[currQues] == TREBLE_CLEF )	{
				uMIDI = MidiTreble[ uRow ] + uAcc ;
			}
			else	{																								//bass
				uMIDI = MidiBass[ uRow ] + uAcc ;
			}

			if (!suppressingMIDI)	{
				midiOut.addNoteOn(midiTime,			uMIDI);	
				midiOut.addNoteOff(midiTime+22, uMIDI);
			}
			midiTime += 24;

			if ( !suppressingMIDI ) {
				midiOut.addEndOfTrack(midiTime);
				midiOut.playSequence( 120 );
			}
		}
		//
		//	SHOW CORRECT Serving
		//
		if ( e.getActionCommand().equals("SHOW CORRECT") )	{
			degreeAnswer.select( qDeg2[currQues] + 1 );
			strFeedback = "The correct scale function name is " +
			              degreeNames[qDeg2[currQues]]         + ".";
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
		//	EXIT BUTTON Servicing
		//
		if ( e.getActionCommand().equals("EXIT") )	{
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
		currentAnswer =	degreeAnswer.getSelectedIndex() - 1;    // compensate for directions
		if ( currentAnswer >= 0 ) {
			checkButton.setEnabled(true);
		}
		
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

	public void RemoveNoteheadFromStaff(int col, int row)
	{	
		int i = 0;
		
		while (i < numNoteheads)	{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )	{	
				noteheadInUse[i]	= 0;
				noteheadRow[i]		=	-1;	
				noteheadCol[i]		=	-1;	
				noteheadAccid[i]	=	' ';
				noteheadColor[i]	= -1;
			}
			i++;
		}	
	}

//------------------------------------------------------------------------------

	public void SetNoteheadColor( int col, int row, int colr )
	{	
		int i = 0;

		while (i < numNoteheads)	{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )	{	
				noteheadColor[i] = colr;
			}
			i++;
		}	
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

	public int GetNoteRowFromColumn(int col)
	{	
		int row  = -1;
		int i		 = 0;
		
		while (i < numNoteheads)	{
			if ( noteheadCol[i] == col )	{	
				row = noteheadRow[i];
			}
			i++;
		}	
		
		return( row );
	}

//------------------------------------------------------------------------------

	public int GetNoteAccidFromColumn(int col)
	{	
		char ac			='x';
		int uAccVal	= 0;
		int i				= 0;

		while (i < numNoteheads)	{
			if ( noteheadCol[i] == col )	{	
				ac = noteheadAccid[i];
			}
			i++;
		}	

		switch ( ac )	{
			case 's':
				uAccVal = +1;
				break;

			case 'f':
				uAccVal = -1;
				break;

			case 'S':
				uAccVal = +2;
				break;

			case 'F':
				uAccVal = -2;
				break;

			default:
				uAccVal = 0;
				break;
		}

		return( uAccVal );
	}

//------------------------------------------------------------------------------

	public int IsNoteheadOnStaff(int col, int row)
	{	
		int i  = 0;
		int	rc = 0;
		
		while (i < numNoteheads)	{
			if ( (noteheadRow[i] == row) && (noteheadCol[i] == col) )	{	
				rc = 1;
			}
			i++;
		}	
		
		return( rc );
	}

//------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
//	Question Generator
//------------------------------------------------------------------------------

	public void	GenerateMajorScaleQ(int q)
	{
		int	scaleMode	=	MAJOR_SCALE;								//	mode of the question to be generated
		int	tonicNote;															//	tonic note for the current key given the clef
		int	newNoteRow;
		int	newAccid;
		int	genDeg1;
		int	genDeg2;

		if ( qClefs[q] == TREBLE_CLEF ) {
			tonicNote	= GetTrebleNote(q);
		}
		else { 
			tonicNote = GetBassNote(q);
		}

		qDeg1[q]	=	GetRandFirstDegree();
		genDeg1		=	qDeg1[q];
		newAccid	= majScaleGen[qKey[q] + 7][qDeg1[q]];				
		qNote1[q] = CreateNoteRep(GetNoteRepRow(tonicNote) - qDeg1[q], newAccid);
					
		do {	
			genDeg2 =	GetRandSecondDegree();
		} while  ( genDeg1 == genDeg2 );
		qDeg2[q] = genDeg2;
	
		newNoteRow	=	GetNoteRepRow(tonicNote) - qDeg2[q];
		newAccid		= majScaleGen[qKey[q] + 7][qDeg2[q]];
		qNote2[q]		= CreateNoteRep(newNoteRow, newAccid);

		if ( genDeg1 == SUBTONIC ) {
			qDeg1[q] = LEADING_TONE;
		}
		else {
			if ( genDeg2 == SUBTONIC ) {
				qDeg2[q] = LEADING_TONE;
			}
		}	
	}

//------------------------------------------------------------------------------

	public void	GenerateMinorScaleQ(int q)
	{
		int	scaleMode	=	MINOR_SCALE;								//	mode of the question to be generated
		int	tonicNote;															//	tonic note for the current key given the clef
		int	newNoteRow;
		int	newAccid = 0;
		int	genDeg1, genDeg2;

		if ( qClefs[q] == TREBLE_CLEF ) {
			tonicNote	= GetTrebleNote(q);
		}
		else { 
			tonicNote = GetBassNote(q);
		}

		qDeg1[q]	=	GetRandFirstDegree();
		genDeg1		=	qDeg1[q];


		if (dispScaleType.equals("Natural Minor"))
		{
			newAccid	= minScaleGen[qKey[q] + 7][qDeg1[q]];		
		}
		else if (dispScaleType.equals("Harmonic Minor"))
		{
			newAccid	= harmonicMinScaleGen[qKey[q] + 7][qDeg1[q]];		
		}
		else if (dispScaleType.equals("Melodic Minor"))
		{
			newAccid	= melodicMinScaleGen[qKey[q] + 7][qDeg1[q]];		
		}

		
		qNote1[q] = CreateNoteRep(GetNoteRepRow(tonicNote) - qDeg1[q], newAccid);
					
		do {	
			genDeg2 =	GetRandSecondDegree();
		} while  ( genDeg1 == genDeg2 );
		qDeg2[q] = genDeg2;
	
		newNoteRow	=	GetNoteRepRow(tonicNote) - qDeg2[q];

		if (dispScaleType.equals("Natural Minor"))
		{
			newAccid	= minScaleGen[qKey[q] + 7][qDeg2[q]];
		}
		else if (dispScaleType.equals("Harmonic Minor"))
		{
			newAccid	= harmonicMinScaleGen[qKey[q] + 7][qDeg2[q]];
		}
		else if (dispScaleType.equals("Melodic Minor"))
		{
			newAccid	= melodicMinScaleGen[qKey[q] + 7][qDeg2[q]];
		}

		qNote2[q]		= CreateNoteRep(GetNoteRepRow(tonicNote) - qDeg2[q], newAccid);

		if ( (dispScaleType.equals("Harmonic Minor")) || (dispScaleType.equals("Melodic Minor")) )
		{
			if ( genDeg1 == SUBTONIC ) {
				qDeg1[q] = LEADING_TONE;
			}
			else {
				if ( genDeg2 == SUBTONIC ) {
					qDeg2[q] = LEADING_TONE;
				}
			}
		}	
	}

//------------------------------------------------------------------------------

	public void GenerateQuestionDeck()
	{
		int	scaleMode	=	MAJOR_SCALE;

		if ( parmScaleType.equals("MINOR") ) {
			scaleMode	=	MINOR_SCALE;
		}

		for (int q = 0; q < Q_DECK_MAX; q++) {
			qMode[q]	=	scaleMode;											//	Set the question mode	
			qClefs[q] = randInt(1,2);										//	Choose a clef		
			qKey[q]		= randInt(1,15) - 8;							//	Choose a key (-7 ... +7)
			
			if ( scaleMode == MAJOR_SCALE ) {
				GenerateMajorScaleQ(q);
			}
			else {
				pickRandomMinorScale();
				GenerateMinorScaleQ(q);
			}
			qDispMinorType[q] = dispScaleType;

		}

	}

//------------------------------------------------------------------------------

	public int	GetTrebleNote(int q)
	{
		int		startRow	= 0;
		char	cModifer	= ' ';

		if ( qKey[q] >= 0 )	{ 																		// C major or sharp keys
			if ( qMode[q] == MAJOR_SCALE )	{
				startRow = cyc5shTrebMaj[qKey[q]];
				cModifer = cyc5shAccMaj[qKey[q]];
			}
			else	{
				startRow	= cyc5shTrebMin[qKey[q]];
				cModifer	= cyc5shAccMin[qKey[q]];
			}
		}
		else	{																										// flat key
			if (  qMode[q] == MAJOR_SCALE )	{
				startRow	= cyc5flTrebMaj[Math.abs(qKey[q])];
				cModifer	= cyc5flAccMaj[Math.abs(qKey[q])];
			}
			else	{
				startRow	= cyc5flTrebMin[Math.abs(qKey[q])];
				cModifer	= cyc5flAccMin[Math.abs(qKey[q])];
			}
		}

		return ( CreateNoteRep(startRow, cModifer) );
	}

//------------------------------------------------------------------------------

	public int	GetBassNote(int q)
	{
		int		startRow	= 0;
		char	cModifer	= ' ';

		if ( qKey[q] >= 0 )	{ 																	// Sharp keys OR C Major
			if ( qMode[q] == MAJOR_SCALE )	{
				startRow = cyc5shBassMaj[qKey[q]];
				cModifer = cyc5shAccMaj[qKey[q]];
			}
			else	{
				startRow	= cyc5shBassMin[qKey[q]];
				cModifer	= cyc5shAccMin[qKey[q]];
			}
		}
		else	{																									// Flat keys
			if (  qMode[q] == MAJOR_SCALE )	{
				startRow	= cyc5flBassMaj[Math.abs(qKey[q])];
				cModifer	= cyc5flAccMaj[Math.abs(qKey[q])];
			}
			else	{
				startRow	= cyc5flBassMin[Math.abs(qKey[q])];
				cModifer	= cyc5flAccMin[Math.abs(qKey[q])];
			}
		}
		
		return ( CreateNoteRep(startRow, cModifer) );	
	}

//------------------------------------------------------------------------------

	public void PresentCurrentQuestion(int q)
	{
		int		startNote	= 0;
		char	ac				= ' ';
		int		note;
		int		currentKey;

		currentKey	= qKey[q];
		currentClef	= qClefs[q];
 
		if ( qClefs[q] == TREBLE_CLEF )	{
			note = GetTrebleNote(q);
		}
		else {
			note = GetBassNote(q);
		}
		
		startNote = GetNoteRepRow(note);
		ac				=	accidToChar( GetNoteRepAcc(note) );

		startCursorRow = startNote;
		startCursorCol = 0;

		cursorRow = startNote;
		cursorCol = 0;
		//
		//	Display two notes for current question
		//
		for (int i = 2; i <= 14; i += 2) {						//	erase all notes on staff
			EraseAllNoteheadsInColumn(i);
		} 

		AddNoteheadToStaff(0, GetNoteRepRow(qNote1[q]),
		                      accidToChar(GetNoteRepAcc(qNote1[q])));	// display 1st note
		AddNoteheadToStaff(8, GetNoteRepRow(qNote2[q]),
		                      accidToChar(GetNoteRepAcc(qNote2[q])));	//	display 2nd note
		degreeAnswer.select(TONIC);																		//	rest popup
	}

//------------------------------------------------------------------------------

	public int randInt(int low, int high)
	{
		return(((int)((high-low+1)*(Math.random()))) + low);
	}


//------------------------------------------------------------------------------

	public int GetRandFirstDegree()
	{
		int	x	=	randInt(0, 100);
		
		if ( x > 75 )								//	25% TONIC
			return( 0 );
		else if ( x > 65 )					//	10% SUPERTONIC
			return( 1);
		else if ( x > 50 )					//	15% MEDIANT
			return( 2 );
		else if ( x > 40 )					//	10% SUBDOMINANT
			return( 3 );
		else if ( x > 20 )					//	20% DOMINANT
			return( 4 );
		else if ( x > 10 )					//	10% SUBMEDIANT
			return ( 5 );
		else												//	10% LEADINGTONE / SUBTONIC
			return( 6 );
	}
	

//------------------------------------------------------------------------------

	public int GetRandSecondDegree()
	{
		int	x	=	randInt(0, 100);
		
		if ( x > 80 )								//	20% TONIC
			return( 0 );
		else if ( x > 70 )					//	10% SUPERTONIC
			return( 1);
		else if ( x > 55 )					//	15% MEDIANT
			return( 2 );
		else if ( x > 40 )					//	15% SUBDOMINANT
			return( 3 );
		else if ( x > 20 )					//	20% DOMINANT
			return( 4 );
		else if ( x > 10 )					//	10% SUBMEDIANT
			return ( 5 );
		else												//	10% LEADING_TONE / SUBTONIC
			return( 6);
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
//		centerString(g, strExamFeedback1, txtFont, 350);
//		centerString(g, strExamFeedback2, txtFont, 370);
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
		

		strInstruct2 = "";
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
	
		txtFont = new Font("Arial", Font.BOLD, 18);
		g.setFont(txtFont);

		strMinorType = qDispMinorType[currQues];
		if (!strMinorType.equals(""))
		{
			strMinorType += ": ";
		}

		centerStringAt(g, strMinorType+degreeNames[qDeg1[currQues]], txtFont, DEGREE_X, DEGREE_Y);

		txtFont = new Font("Arial", Font.BOLD, 14);
		g.setFont(txtFont);			
		displayScore(g);					//	display current score
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
//		g.drawString(s, ((rectScreen.width/2) - (fm.stringWidth(s) / 2)), yCoord);
	}

//------------------------------------------------------------------------------
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
		else if (accid == 'f')
		{
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
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

