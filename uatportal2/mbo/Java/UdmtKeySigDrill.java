//------------------------------------------------------------------------------
// UdmtKeySigDrill Applet -  Key Signature Drill and Exam for Major and Minor Keys
//------------------------------------------------------------------------------
// REMEMBER TO INCREMENT VERSION NUMBER FOR EACH RELEASE (udmtAppletVersion)
//------------------------------------------------------------------------------
// PARAMS:
//
// scaleType	=	The type of scale to present:
//				Values: 	Major, Minor		
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
// drillExitURL	=     The URL to invoke when the student presses the EXIT button
//                      in Drill mode.
//				REQUIRED IF examMode = DRILL
//
//------------------------------------------------------------------------------
// MODIFICATION HISTORY:
// 05/01/2004: Ver0.03  - changed to use UdmtURLBuilder class to build URL's for buttons,
// and new behavior to branch to "Sorry" page immediately when student fails exam.
// Started adding interface logic to UdmtExamLog.
// 05/02/2004: Ver 0.03 - fixed null reference problem in NEXT button
// 05/16/2004: Ver 0.04 - fixed problem with exam not finishing after student passes 
// this was caused by hintButton code being executed in Exam mode (when hint button not drawn)
// 01/20/2005: ver 0.05 - changes to support exam login servlet
//------------------------------------------------------------------------------


import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtKeySigDrill extends Applet
	implements MouseListener, MouseMotionListener, ActionListener, ItemListener

//------------------------------------------------------------------------------
{
	private String udmtAppletName			=	"UdmtKeySigDrill";
	private String udmtAppletVersion		=	"0.05"	;

	//05/01/2004:
	private UdmtURLBuilder urlBuilder;
	private UdmtExamLog examLog;
	// End of 05/01/2004 change.	

	private String parmScaleType 			= 	"Major"; //default = major scale ascending.

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

	private Rectangle rectScreen;
	private Image imageScreen;

	private Image imgAllSymbols;

	private UdmtMusicSymbol muStaff, muTrebleClef, muBassClef, muDot, muBar, muLeger;
	private UdmtMusicSymbol muNoteheadFilled, muNoteheadUnfilled, muSharpRed, muFlatRed, muRtDblBar;
	private UdmtMusicSymbol muSharp, muFlat, muNatural, muDblSharp, muDblFlat;
	private UdmtMusicSymbol muWholeR, muHalfR, muBlueWholeR, muBlueHalfR;

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
	private int staffNoteXPos[] 			= 	{15,40,65,90,115,140,165,190
									,215,240,265,290,315,340,365,390};

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

	// Display strings for key signatures: -7 thru + 7
	private String strDisplayMajorKeys[] 	= 	{"Cb","Gb","Db","Ab","Eb","Bb","F","C"
									,"G","D","A","E","B","F#","C#"};
	private String strDisplayMinorKeys[] 	= 	{"Ab","Eb","Bb","F","C","G","D","A"
									,"E","B","F#","C#","G#","D#","A#"};

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

	private int MajorScaleAsc[] 			= 	{2,2,1,2,2,2,1};
	private int MajorScaleDesc[]			= 	{1,2,2,2,1,2,2};

	private int NaturalMinorScaleAsc[]		= 	{2,1,2,2,1,2,2};
	private int NaturalMinorScaleDesc[]		= 	{2,2,1,2,2,1,2};

	private int HarmonicMinorScaleAsc[]		= 	{2,1,2,2,1,3,1};
	private int HarmonicMinorScaleDesc[]	= 	{1,3,1,2,2,1,2};

	private int MelodicMinorScaleAsc[]		= 	{2,1,2,2,2,2,1};
	private int MelodicMinorScaleDesc[]		= 	{2,2,1,2,2,1,2};
	
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
	private final int	MENU_VER_OFFSET		=	34;

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
	private Button hintButton;

	private int currques;						// current question number being presented

	private int numtimeschecked 			= 	0;
	private int numquespresented 			= 	0;
	private int numquescorrect1sttime 		= 	0;

	private String strDrillMode			=	"Major Key Signature Practice";
	private String strExamMode			=	"Major Key Signature Exam";

	private String strInstruct			=	"";
	private String strInstruct1			=	"Create the key signature for: ";
	private String strInstructK			=	"X";
	private String strInstruct2 			= 	" major ";
	private String strInstruct3 			= 	".";

	private String strScore1 			= 	"Score: ";
	private int nScore1 				= 	0;
	private String strScore2 			= 	" out of ";
	private int nScore2 				= 	0;
	private String strScore3 			= 	" = ";
	private int nScorePercent 			= 	0;
	private String strScore4 			= 	"%";
	private String strScoreDisp;

	private String strCorrect 			= 	"Correct.";
	private String strIncorrect 			= 	"Incorrect.  Please try again.  Click HINT if you need help.";
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

	private int majorOrMinor			= 	0;
	// Determines whether Major or Minor scales are being tested.  1=Major, -1=Minor.

	private int trebleorbass = 1;
	// alternates between 1 and -1 as questions generated and assigned to random slots
	// This ensures the pattern of clefs doesn't repeat until after 30 questions.

	private int minorType = 0;
	// alternates between 0 and 2 as questions generated and assigned to random slots
	// this allows even distribution of all 3 types of minor scales.

	// Staff positions for key signatures of 1-7 sharps and flats for each clef type:
	private int keySharpYPosTreb[] 	= {4,7,3,6,9,5,8};	// F C G D A E B
	private int keyFlatYPosTreb[] 	= {8,5,9,6,10,7,11};	// B E A D G C F
	private int keySharpYPosBass[] 	= {6,9,5,8,11,7,10};	// F C G D A E B
	private int keyFlatYPosBass[] 	= {10,7,11,8,12,9,13};	// B E A D G C F

	private int hintStatus = 0;
	private String strHintFeedback		=	"";

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

		//05/01/2004- New procedure for Exam Log requires object to be instantiated once
		examLog = new UdmtExamLog(); 
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
			System.out.println ("Major, Minor");
		}
		else if (parmScaleType.equals("Major"))	{
			for (int i=0 ; i<=6 ; i++)		{
				ScaleBeingTested[i] = MajorScaleAsc[i];
			}
			dispScaleType=" Major";
			upDown = +1;
			strDrillMode="Major Key Signature Practice";
			strExamMode="Major Key Signature Exam";
			majorOrMinor = +1;
		}
		else if  (parmScaleType.equals("Minor"))	{
			for (int i=0 ; i<=6 ; i++)				{
				ScaleBeingTested[i] = NaturalMinorScaleAsc[i];
			}
			dispScaleType=" Minor";
			upDown = +1;
			strDrillMode="Minor Key Signature Practice";
			strExamMode="Minor Key Signature Exam";
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
  				String sExamKey = objEk.getExamKey("Keysig"+parmScaleType+"Pass");
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
		
		System.out.println ("Next URL: "+parmNextURL);
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

				//01-20-2005: use URLBUILDER only if not in exam mode
				if (examMode)
				{
 					UdmtExamKey objEk = new UdmtExamKey();
  					String sExamKey = objEk.getExamKey("Keysig"+parmScaleType+"Fail");
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
		staffY = rectScreen.height / 2 - 60;

		currentClef = randint (1,2);

		userTestQuesHead=0;
		userTestQuesTail=0;
		generate15questions(0);
		
		currques = 0;
		setCurrentQuestion (currques);
		
		xLeft = staffX + staffNoteXPos[1] - 5 + 20 ;
		xRight = staffX + staffNoteXPos[7] + 20 + 5 + 20;
		yTop = staffY + staffNoteYPos[5] - 20 - 5 ;
		yBottom = staffY + staffNoteYPos[12] - 20 + 20 ;

		setupPopupMenu();
		add ( popup );

		setLayout(null);

		if ( !examMode )	{
			hintButton = new Button("HINT");
	      	hintButton.addActionListener(this);
			hintButton.setEnabled(false);
			hintButton.setBounds ( 60,300,110,30);
			add (hintButton);
		}

		checkButton = new Button("CHECK");
      	checkButton.addActionListener(this);
		checkButton.setEnabled(true);
		checkButton.setBounds ( 180,300,110,30);
		add (checkButton);

      	nextButton = new Button("NEXT");
      	nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		nextButton.setBounds ( 300,300,110,30);
		add(nextButton);

		if ( !examMode )	{

			playmineButton = new Button("PLAY MINE");
			playmineButton.addActionListener(this);
			playmineButton.setEnabled(false);
			playmineButton.setBounds ( 60,350,110,30);
			add(playmineButton);

			playcorrButton = new Button("PLAY CORRECT");
			playcorrButton.addActionListener(this);
			playcorrButton.setEnabled(false);
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

		muWholeR 				= new UdmtMusicSymbol();
		muWholeR.CropImage 		( imgAllSymbols, 35, 430, 60, 455 );

		muHalfR 				= new UdmtMusicSymbol();
		muHalfR.CropImage 		( imgAllSymbols, 84, 432, 110, 455 );

		// create red notehead by cloning notehead image and changing pixel colors
		try 	{
			// Pixelgrabber logic from p.445 of Java AWT Reference:

			int pixels[] 	= new int [20*50];
			int redpixels[] 	= new int [20*50];
 
			PixelGrabber pg = new PixelGrabber
					( muSharp.getImage()
					, 0
					, 0
					, 20
					, 50
					, pixels
					, 0
					, 20
					) ;

			pg.grabPixels();

			if ( (pg.status() & ImageObserver.ALLBITS) != 0)	{

				for (int y=0 ; y<=49 ; y++)				{
					for (int x=0 ; x <=19 ; x++)			{
						if (pixels[y*20+x] > 0)			{
							//System.out.print ("1");
							redpixels[y*20+x] = pixels[y*20+x];
						}
						else						{
							//System.out.print ("0");
							redpixels[y*20+x] = Color.red.getRGB();
						}
					}		
					//System.out.println();
				}

				imgTemp = createImage
					( new MemoryImageSource( 20, 50, redpixels, 0, 20)
					);
		
				muSharpRed = new UdmtMusicSymbol();
				muSharpRed.CropImage	( imgTemp, 0, 0, 20, 50 );
			}
		}
		catch ( Exception e )	{
			e.printStackTrace();
		}

		try 	{
			// Pixelgrabber logic from p.445 of Java AWT Reference:

			int pixels[] 	= new int [25*50];
			int redpixels[] 	= new int [25*50];
 

			PixelGrabber pg = new PixelGrabber
					( muFlat.getImage()
					, 0
					, 0
					, 25
					, 50
					, pixels
					, 0
					, 25
					) ;

			pg.grabPixels();

			if ( (pg.status() & ImageObserver.ALLBITS) != 0)	{

				for (int y=0 ; y<=49 ; y++)				{
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
					( new MemoryImageSource( 25, 50, redpixels, 0, 25)
					);
		
				muFlatRed = new UdmtMusicSymbol();
				muFlatRed.CropImage( imgTemp, 0, 0, 25, 50 );
			}
		}
		catch ( Exception e )	{
			e.printStackTrace();
		}



		try 	{
			// Pixelgrabber logic from p.445 of Java AWT Reference:

			int pixels[] 	= new int [25*25];
			int bluepixels[] 	= new int [25*25];
 

			PixelGrabber pg = new PixelGrabber
					( muWholeR.getImage()
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
							bluepixels[y*25+x] = pixels[y*25+x];
						}
						else						{
							//System.out.print ("0");
							bluepixels[y*25+x] = Color.blue.getRGB();
						}
					}		
					//System.out.println();
				}

				imgTemp = createImage
					( new MemoryImageSource( 25, 25, bluepixels, 0, 25)
					);
		
				muBlueWholeR = new UdmtMusicSymbol();
				muBlueWholeR.CropImage( imgTemp, 0, 0, 25, 25 );
			}
		}
		catch ( Exception e )	{
			e.printStackTrace();
		}

		try 	{
			// Pixelgrabber logic from p.445 of Java AWT Reference:

			int pixels[] 	= new int [25*25];
			int bluepixels[] 	= new int [25*25];
 

			PixelGrabber pg = new PixelGrabber
					( muHalfR.getImage()
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
							bluepixels[y*25+x] = pixels[y*25+x];
						}
						else						{
							//System.out.print ("0");
							bluepixels[y*25+x] = Color.blue.getRGB();
						}
					}		
					//System.out.println();
				}

				imgTemp = createImage
					( new MemoryImageSource( 25, 25, bluepixels, 0, 25)
					);
		
				muBlueHalfR = new UdmtMusicSymbol();
				muBlueHalfR.CropImage( imgTemp, 0, 0, 25, 23 );
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

		popup.add ( mi = new MenuItem ( "#" ) );
		mi.addActionListener (this) ;
	
		popup.add ( mi = new MenuItem ( "None" ) );
		mi.addActionListener (this) ;

		popup.add ( mi = new MenuItem ( "b" ) );
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
			&& (xcoord < (staffX + staffNoteXPos[8] ) )
			)
			{
				int width = staffNoteXPos[2] - staffNoteXPos[1] ;
				// For keysig drill/exam: not multiplying by 2
				cursorcol =(int) ((xcoord - (staffX + staffNoteXPos[1] + 20 )) / width) + 2 ;
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
			eraseAllNoteheadsInColumn (savecursorcol);
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
		//else if (e.getActionCommand().equals ( "x" ) )
		//{
		//	ac = 'S';
		//}
		//else if (e.getActionCommand().equals ( "bb" ) )
		//{
		//	ac = 'F';
		//}

		if ((ac != '?') && (ac != ' ') && (isNoteheadOnStaff ( cursorcol, cursorrow ) == 0))
		{
			eraseAllNoteheadsInColumn (savecursorcol);
			addNoteheadToStaff (savecursorcol, savecursorrow, ac);

			// check if user entered something in all note columns. 
			// If yes, enable the Check button - NOT NEEDED FOR KEYSIG DRILL - ALWAYS ENABLE CHECK BUTTON

			//if ( (getNoteRowFromColumn ( 0 ) > -1)
			//&& (getNoteRowFromColumn ( 2 ) > -1)
			//&& (getNoteRowFromColumn ( 4 ) > -1)
			//&& (getNoteRowFromColumn ( 6 ) > -1)
			//&& (getNoteRowFromColumn ( 8 ) > -1)
			//&& (getNoteRowFromColumn ( 10 ) > -1)
			//&& (getNoteRowFromColumn ( 12 ) > -1)
			//&& (getNoteRowFromColumn ( 14 ) > -1)
			//
			//)
			//{
			//	checkButton.setEnabled(true);
			//}

			repaint();
		}

		// trying to fix problem where adding accid and cursor in menu below staff
		if ((ac != '?') && (ac != ' ') && (isNoteheadOnStaff ( cursorcol, cursorrow ) != 0))
		{
			eraseAllNoteheadsInColumn (savecursorcol);
			addNoteheadToStaff (savecursorcol, savecursorrow, ac);
			// check if user entered something in all note columns. 
			// If yes, enable the Check button

			if ( (getNoteRowFromColumn ( 2 ) > -1)
			&& (getNoteRowFromColumn ( 3 ) > -1)
			&& (getNoteRowFromColumn ( 4 ) > -1)
			&& (getNoteRowFromColumn ( 5 ) > -1)
			&& (getNoteRowFromColumn ( 6 ) > -1)
			&& (getNoteRowFromColumn ( 7 ) > -1)
			&& (getNoteRowFromColumn ( 8 ) > -1)
			&& (getNoteRowFromColumn ( 9 ) > -1)

			)
			{
				checkButton.setEnabled(true);
			}
			repaint();


		}
		if (e.getActionCommand().equals ( "HINT" ) )
		{
			//System.out.println("Hint button pressed: hintStatus="+hintStatus);
			int Corrmidi = 0;
			int correctline = 0;
			int numcorrect = 0;
			int numEntered = 0;

			int keynum, abskeynum;


			if (hintStatus == 2)
			{
				strHintFeedback = "Incorrect symbols are hilighted in red.";

				keynum = userTestQuestions[currques];
				abskeynum = Math.abs(keynum);

				Udata[0]=getNoteRowFromColumn ( 2 );
				Udata[1]=getNoteRowFromColumn ( 3 );
				Udata[2]=getNoteRowFromColumn ( 4 );
				Udata[3]=getNoteRowFromColumn ( 5 );
				Udata[4]=getNoteRowFromColumn ( 6 );
				Udata[5]=getNoteRowFromColumn ( 7 );
				Udata[6]=getNoteRowFromColumn ( 8 );
				Udata[7]=getNoteRowFromColumn ( 9 );

	
				Uacc[0]=getNoteAccidFromColumn ( 2 );
				Uacc[1]=getNoteAccidFromColumn ( 3 );
				Uacc[2]=getNoteAccidFromColumn ( 4 );
				Uacc[3]=getNoteAccidFromColumn ( 5 );
				Uacc[4]=getNoteAccidFromColumn ( 6 );
				Uacc[5]=getNoteAccidFromColumn ( 7 );
				Uacc[6]=getNoteAccidFromColumn ( 8 );
				Uacc[7]=getNoteAccidFromColumn ( 9 );

				numEntered = 0;

				for (int i=0 ; i <= 6 ; i++) 
				{
					//System.out.println("Udata["+i+"]="+Udata[i]+"Uacc["+i+"]="+Uacc[i]);

					if (Udata[i] > -1)
					{
						numEntered++;
					}

					if (keynum > 0) // sharp key
					{
						if (currentClef == 1) // treble
						{
							if (i <= (abskeynum - 1)) // user must match exact position and order
							{
								//System.out.println("keySharpYPosTreb["+i+"]="+keySharpYPosTreb[i]);
	
								if ( (keySharpYPosTreb[i] == Udata[i]) && (Uacc[i] == +1) )
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{	//don't display red unless user clicks hint
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							else // user must leave all fields after correct key positions blank
							{
								if ((Udata[i] == -1) && (Uacc[i] == 0))
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{	//don't display red unless user clicks hint
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							//System.out.println ("numcorrect="+numcorrect);
						}
						else //bass
						{
							if (i <= (abskeynum - 1)) // user must match exact position and order
							{
								//System.out.println("keySharpYPosBass["+i+"]="+keySharpYPosBass[i]);

								if ( (keySharpYPosBass[i] == Udata[i]) && (Uacc[i] == +1) )
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{ 	//don't display red unless user clicks hint
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );														
								}
							}
							else // user must leave all fields after correct key positions blank
							{	
								if ((Udata[i] == -1) && (Uacc[i] == 0))
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{ 	//don't display red unless user clicks hint
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );												
								}
							}
							//System.out.println ("numcorrect="+numcorrect);
						}
					}
					else if (keynum < 0) // flat key
					{
						if (currentClef == 1) // treble
						{
							//System.out.println("keyFlatYPosTreb["+i+"]="+keyFlatYPosTreb[i]);
							if (i <= (abskeynum - 1)) // user must match exact position and order
							{
								//System.out.println("keyFlatYPosTreb["+i+"]="+keyFlatYPosTreb[i]);

								if ( (keyFlatYPosTreb[i] == Udata[i]) && (Uacc[i] == -1) )
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							else // user must leave all fields after correct key positions blank
							{	
								if ((Udata[i] == -1) && (Uacc[i] == 0))
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							//System.out.println ("numcorrect="+numcorrect);
						}
						else //bass
						{
							//System.out.println("keyFlatYPosBass["+i+"]="+keyFlatYPosBass[i]);
							if (i <= (abskeynum - 1)) // user must match exact position and order
							{
								//System.out.println("keyFlatYPosBass["+i+"]="+keyFlatYPosBass[i]);

								if ( (keyFlatYPosBass[i] == Udata[i]) && (Uacc[i] == -1) )
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}	
								else
								{
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							else // user must leave all fields after correct key positions blank
							{
								if ((Udata[i] == -1) && (Uacc[i] == 0))
								{
									numcorrect++;
									setNoteheadColor ( i+2, Udata[i], 0 );
								}
								else
								{
									//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
									setNoteheadColor ( i+2, Udata[i], 1 );
								}
							}
							//System.out.println ("numcorrect="+numcorrect);
							//System.out.println(numEntered);

						}
					}
					else // C major or A minor
					{
						if ((Udata[i] == -1) && (Uacc[i] == 0))
						{
							numcorrect++;
							setNoteheadColor ( i+2, Udata[i], 0 );
						}
						else
						{
							//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
							setNoteheadColor ( i+2, Udata[i], 1 );
						}
					}

				} // for

				if (!examMode)
				{
					hintButton.setEnabled (false);
					hintStatus = 3;
				}
			}
			strFeedback = strHintFeedback;

			if (hintStatus == 0)
			{
				hintStatus = 1;
			}
			else if (hintStatus == 1)
			{
				hintStatus = 2;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "CHECK" ) )
		{
			int Corrmidi = 0;
			int correctline = 0;
			int numcorrect = 0;
			int numEntered = 0;

			int keynum, abskeynum;

			//System.out.println("Check button pressed");
			//System.out.println("currques="+currques+" userTestQuestions[currques]="+userTestQuestions[currques]);

			hintStatus = 0;
			strFeedback = "";
			strHintFeedback = "";

			keynum = userTestQuestions[currques];
			abskeynum = Math.abs(keynum);

			Udata[0]=getNoteRowFromColumn ( 2 );
			Udata[1]=getNoteRowFromColumn ( 3 );
			Udata[2]=getNoteRowFromColumn ( 4 );
			Udata[3]=getNoteRowFromColumn ( 5 );
			Udata[4]=getNoteRowFromColumn ( 6 );
			Udata[5]=getNoteRowFromColumn ( 7 );
			Udata[6]=getNoteRowFromColumn ( 8 );
			Udata[7]=getNoteRowFromColumn ( 9 );

	
			Uacc[0]=getNoteAccidFromColumn ( 2 );
			Uacc[1]=getNoteAccidFromColumn ( 3 );
			Uacc[2]=getNoteAccidFromColumn ( 4 );
			Uacc[3]=getNoteAccidFromColumn ( 5 );
			Uacc[4]=getNoteAccidFromColumn ( 6 );
			Uacc[5]=getNoteAccidFromColumn ( 7 );
			Uacc[6]=getNoteAccidFromColumn ( 8 );
			Uacc[7]=getNoteAccidFromColumn ( 9 );

			numEntered = 0;


			for (int i=0 ; i <= 6 ; i++) 
			{
				//System.out.println("Udata["+i+"]="+Udata[i]+"Uacc["+i+"]="+Uacc[i]);

				if (Udata[i] > -1)
				{
					numEntered++;
				}

				if (keynum > 0) // sharp key
				{
					if (currentClef == 1) // treble
					{
						if (i <= (abskeynum - 1)) // user must match exact position and order
						{
							//System.out.println("keySharpYPosTreb["+i+"]="+keySharpYPosTreb[i]);

							if ( (keySharpYPosTreb[i] == Udata[i]) && (Uacc[i] == +1) )
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{	//don't display red unless user clicks hint
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						else // user must leave all fields after correct key positions blank
						{
							if ((Udata[i] == -1) && (Uacc[i] == 0))
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{	//don't display red unless user clicks hint
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						//System.out.println ("numcorrect="+numcorrect);
					}
					else //bass
					{
						if (i <= (abskeynum - 1)) // user must match exact position and order
						{
							//System.out.println("keySharpYPosBass["+i+"]="+keySharpYPosBass[i]);

							if ( (keySharpYPosBass[i] == Udata[i]) && (Uacc[i] == +1) )
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{ 	//don't display red unless user clicks hint
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );														
							}
						}
						else // user must leave all fields after correct key positions blank
						{
							if ((Udata[i] == -1) && (Uacc[i] == 0))
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{ 	//don't display red unless user clicks hint
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );											
							}
						}
						//System.out.println ("numcorrect="+numcorrect);
					}
				}
				else if (keynum < 0) // flat key
				{
					if (currentClef == 1) // treble
					{
						//System.out.println("keyFlatYPosTreb["+i+"]="+keyFlatYPosTreb[i]);
						if (i <= (abskeynum - 1)) // user must match exact position and order
						{
							//System.out.println("keyFlatYPosTreb["+i+"]="+keyFlatYPosTreb[i]);

							if ( (keyFlatYPosTreb[i] == Udata[i]) && (Uacc[i] == -1) )
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						else // user must leave all fields after correct key positions blank
						{
							if ((Udata[i] == -1) && (Uacc[i] == 0))
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						//System.out.println ("numcorrect="+numcorrect);
					}
					else //bass
					{
						//System.out.println("keyFlatYPosBass["+i+"]="+keyFlatYPosBass[i]);
						if (i <= (abskeynum - 1)) // user must match exact position and order
						{
							//System.out.println("keyFlatYPosBass["+i+"]="+keyFlatYPosBass[i]);

							if ( (keyFlatYPosBass[i] == Udata[i]) && (Uacc[i] == -1) )
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						else // user must leave all fields after correct key positions blank
						{
							if ((Udata[i] == -1) && (Uacc[i] == 0))
							{
								numcorrect++;
								setNoteheadColor ( i+2, Udata[i], 0 );
							}
							else
							{
								//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
								setNoteheadColor ( i+2, Udata[i], 1 );
							}
						}
						//System.out.println ("numcorrect="+numcorrect);
						//System.out.println(numEntered);

					}
				}
				else // C major or A minor
				{
					if ((Udata[i] == -1) && (Uacc[i] == 0))
					{
						numcorrect++;
						setNoteheadColor ( i+2, Udata[i], 0 );
					}
					else
					{
						//System.out.println ("RED: i="+i+",Udata[i]="+Udata[i]);
						setNoteheadColor ( i+2, Udata[i], 1 );
					}
				}

			} // for

			gotThisQuesCorrect=0;

			if ( numtimeschecked == 0)	{
				numquespresented++;
			}

			if (numcorrect == 7)	{
				if ( numtimeschecked == 0)	{

					numquescorrect1sttime++;
					gotThisQuesCorrect = 1;

					//System.out.println ("Got this ques correct.  NumCorr1sttime="+numquescorrect1sttime);
				}	

				strFeedback = strCorrect;			
				nextButton.setEnabled(true);
				if (!examMode)
				{
					hintButton.setEnabled(false);
				}
				//need to prevent user from hitting Check multiple times after answer correct
				// and also: from changing a correct answer to a wrong and back to the correct answer
				// 		and checking again to falsely score points

				checkButton.setEnabled(true);
				numtimeschecked++;
			}
			else
			{
				//System.out.println ("Incorrect.  numcorrect="+numcorrect);

				if ( examMode )	{
					strFeedback = strIncorrectExam;
					checkButton.setEnabled(true);
					nextButton.setEnabled(true);
				}
				else	{
					strFeedback = strIncorrect;
					if (!examMode)
					{
						hintButton.setEnabled(true);
					}
					hintStatus = 1;
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

			if (numEntered < abskeynum)
			{
				strHintFeedback = "Too few symbols were entered.  Click HINT to see incorrect symbols.";
			}
			else if (numEntered > abskeynum)
			{
				strHintFeedback = "Too many symbols were entered.  Click HINT to see incorrect symbols.";
			}
			else 
			{
				strHintFeedback = "Incorrect symbol placement.  Click HINT to see incorrect symbols.";
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
					//checkButton.setEnabled(true);
					//nextButton.setEnabled(false);

					//05/16/04: disable check and next buttons
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);


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

                                        // 05/16/04 - disable check and exit buttons
                                        checkButton.setEnabled(false);
                                        nextButton.setEnabled(false);

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

			checkButton.setEnabled(true);
			nextButton.setEnabled(false);
			if ( !examMode )
			{
				showcorrButton.setEnabled(false);
			}
			strFeedback = "";
			
			//05/02/2004 - fix bug which caused null reference in exam mode
			if (!examMode)
			{
				hintButton.setEnabled(false);
			}

			repaint();
		}

		if (e.getActionCommand().equals ( "PLAY MINE" ) )
		{
			int miditime = 0;

			//System.out.println("Play Mine button pressed");
			Udata[0]=getNoteRowFromColumn ( 2 );
			Udata[1]=getNoteRowFromColumn ( 3 );
			Udata[2]=getNoteRowFromColumn ( 4 );
			Udata[3]=getNoteRowFromColumn ( 5 );
			Udata[4]=getNoteRowFromColumn ( 6 );
			Udata[5]=getNoteRowFromColumn ( 7 );
			Udata[6]=getNoteRowFromColumn ( 8 );
			Udata[7]=getNoteRowFromColumn ( 9 );


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
				}
				else
				{
					miditime += 24;
				}

			}//for

		} //playmine

		if (e.getActionCommand().equals ( "PLAY CORRECT" ) )
		{
			int Urow, Uacc, miditime = 0;
			int Corrmidi = 0;

			//System.out.println("Play Correct button pressed");

			Urow=getNoteRowFromColumn ( 2 );
			Uacc=getNoteAccidFromColumn ( 2 );

			//System.out.println("Urow="+Urow);
			//System.out.println("Uacc="+Urow);

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

				miditime= 24;

				for (int i=0; i<=6; i++)
				{
					Corrmidi += ScaleBeingTested[i] * upDown;
					miditime += 24;
				}
			}
		}

		if (e.getActionCommand().equals ( "SHOW CORRECT" ) )
		{
			int Urow, Uacc, curaccid;
			int Corrmidi = 0;
			int key2disp;

			//System.out.println("Show Correct button pressed");

			eraseAllNoteheadsInColumn (2);
			eraseAllNoteheadsInColumn (3);
			eraseAllNoteheadsInColumn (4);
			eraseAllNoteheadsInColumn (5);
			eraseAllNoteheadsInColumn (6);
			eraseAllNoteheadsInColumn (7);
			eraseAllNoteheadsInColumn (8);
			eraseAllNoteheadsInColumn (9);


			key2disp = userTestQuestions[currques];

			if (key2disp > 0) // sharps
			{
				if (currentClef == 1) // treble
				{
					for (int i=0 ; i < key2disp ; i++)
					{
						addNoteheadToStaff ( i+2, keySharpYPosTreb[i] , 's');
					}
				}
				else //bass
				{
					for (int i=0 ; i < key2disp ; i++)
					{
						addNoteheadToStaff ( i+2, keySharpYPosBass[i] , 's');
					}
				}			
			}
			else if (key2disp < 0) // flats
			{
				if (currentClef == 1) // treble
				{
					for (int i=0 ; i < (key2disp * -1) ; i++)
					{
						addNoteheadToStaff ( i+2, keyFlatYPosTreb[i] , 'f');
					}
				}
				else //bass
				{
					for (int i=0 ; i < (key2disp * -1) ; i++)
					{
						addNoteheadToStaff ( i+2, keyFlatYPosBass[i] , 'f');
					}
				}			
			}
			
			//checkButton.setEnabled(false);
			if (!examMode)
			{
				hintButton.setEnabled(false);
			}

			strFeedback = "The correct key signature is being displayed.";
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
			//System.out.println ("   i="+i+"noteheadColor[i]="+noteheadColor[i]);
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
					ScaleBeingTested[i] = NaturalMinorScaleAsc[i];
				}
				dispScaleType=" Minor";
			}
			else if ( currMinorType == 1)
			{
				for (int i=0 ; i<=6 ; i++)
				{
					ScaleBeingTested[i] = HarmonicMinorScaleAsc[i];
				}
				dispScaleType=" Minor";
			}
			else if ( currMinorType == 2)
			{
				for (int i=0 ; i<=6 ; i++)
				{
					ScaleBeingTested[i] = MelodicMinorScaleAsc[i];
				}
				dispScaleType=" Minor";
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
		
		if (majorOrMinor == +1)	{
			strInstructK = strDisplayMajorKeys [ currentkey + 7 ] ; 
		} else if (majorOrMinor == -1)	{
			strInstructK = strDisplayMinorKeys [ currentkey + 7 ] ; 
		}
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
		strInstruct = strInstruct1 + strInstructK + strInstruct2 + strInstruct3;

		centerString (g, strInstruct, txtfont, 60);

		//System.out.println("DrawScreen 1");

		for (int x=staffX ; x < staffX+560 ; x+=65 )	{
			g.drawImage (muStaff.getImage(), x, staffY, this );
		}

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
		// NEED TO BE ABLE TO DRAW BLACK OR RED ACCIDENTALS

		for (int i=0; i<numNoteheads;i++)	{
			if (noteheadInUse[i] == 1)	{
				if (noteheadColor[i] == 0)	{
					drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i],'B');
				}
				else	{
					if ( examMode )	{
						drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i],'B');
					}
					else	{

						//System.out.println ("drawing red: hintStatus="+hintStatus);

						if (hintStatus == 3)
						{
							drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i],'R');
						}
						else
						{
							drawAccidental (g, noteheadCol[i], noteheadRow[i], noteheadAccid[i],'B');
						}

						//System.out.println("RED: i="+i);

					}
				}
			}
		} // for


		// Draw Dots for positions where user will enter notes
		for (int i=2; i<=8 ; i+=1)
		{
			g.drawImage (muDot.getImage(), staffX + staffNoteXPos[i] + 7, staffY + staffNoteYPos[0] - 17, this );
		}


		//System.out.println("DrawScreen 4");

		//Draw notehead at cursor position
		if (cursorvisible == 1)
		{	
			drawCursorBox (g, cursorcol, cursorrow );

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

	public void drawCursorBox ( Graphics g, int x , int y )
	{
		int xNudge = -2;

		g.drawImage (muBlueWholeR.getImage()
				, staffX + staffNoteXPos[x] + xNudge
				, staffY-20+ staffNoteYPos[y]
				, this );
		g.drawImage (muBlueHalfR.getImage()
				, staffX + staffNoteXPos[x] + xNudge
				, staffY-20+ staffNoteYPos[y]
				, this );

		if (y <= 2)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7 + xNudge
			, staffY-50+ staffNoteYPos[2]
			, this );
		}
		if (y==0)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7 + xNudge
			, staffY-50+ staffNoteYPos[0]
			, this );
		}
		if (y >= 14)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7 + xNudge
			, staffY-50+ staffNoteYPos[14]
			, this );
		}
		if (y==16)
		{
			g.drawImage (muLeger.getImage()
			, staffX + staffNoteXPos[x] - 7 + xNudge
			, staffY-50+ staffNoteYPos[16]
			, this );
		}
	}

//------------------------------------------------------------------------------

	public void drawAccidental ( Graphics g, int x , int y, char accid, char blackOrRed )
	{
		// For keysig drill - removed adjustments from x coordinate

		if (accid == 's')
		{
			if (blackOrRed == 'B')	{
				g.drawImage (muSharp.getImage()
				, staffX + staffNoteXPos[x] 
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
			else {
				g.drawImage (muSharpRed.getImage()
				, staffX + staffNoteXPos[x] 
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
		}
		else if (accid == 'f')
		{
			if (blackOrRed == 'B')	{
				g.drawImage (muFlat.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
			else {
				g.drawImage (muFlatRed.getImage()
				, staffX + staffNoteXPos[x]
				, staffY-20+ staffNoteYPos[y] - 13
				, this );
			}
		}
	}
}
//------------------------------------------------------------------------------
// END OF CLASS UdmtMajorScaleDrill
//------------------------------------------------------------------------------

