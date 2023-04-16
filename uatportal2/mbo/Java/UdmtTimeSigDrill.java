import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtTimeSigDrill extends Applet
	implements MouseListener,  ActionListener
{
//------------------------------------------------------------------------------
// UdmtTimeSigDrill - Time signature drill and exam
//------------------------------------------------------------------------------
// PARAMS:
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
// 05/01/2004:  - changed to use UdmtURLBuilder class to build URL's for buttons,
// and new behavior to branch to "Sorry" page immediately when student fails exam.
// Started adding interface logic to UdmtExamLog.
// 09/15/2004:  - ver 0.02 - changed URLBuilder logic to set exam keys in order to work with Login servlet
// 01/17/2005:  - externalized exam keys in UdmtExamKey object
//------------------------------------------------------------------------------

	private	UdmtTsigNotationApi notation;
	private	UdmtTimeSigExercises exercises;
	private UdmtURLBuilder urlBuilder;
	private UdmtExamLog examLog;

	private UdmtImageButton btnLeftArrow, btnRightArrow;
	private UdmtImageButton btnWN, btnWNdot, btnHN, btnHNdot, btnQN, btnQNdot, btnEN,btnENdot, btnSN, btnSNdot;	
	private Button btnSimple, btnCompound, btnDuple, btnTriple, btnQuadruple, btnQuintuple, btnSextuple, btnSeptuple;

	private String udmtAppletName			=	"UdmtTimeSigDrill";
	private String udmtAppletVersion		=	"0.02"	;

	private Image imgAllSymbols;
	private Image imgTriplet;

//	private Sequencer sequencer;
//	private Sequence seq, progChgSequence;
	private int playStatus=-1;
	private boolean suppressingMIDI = true;

	private Rectangle rectScreen;
	private Image imageScreen;

	UdmtMusicSymbol muNoteheadFilled, muNoteheadUnfilled;
	UdmtMusicSymbol muTrebleClef, muAltoClef, muBassClef, muCommon, muLeger;
	UdmtMusicSymbol muWhole, muHalfD, muQuarterD, muEighthD, muSixteenthD, muThirtySecondD;
	UdmtMusicSymbol muStaff, muThickBar, muLeftRpt, muLeftDblBar, muRtDblBar, muBar, muRightRpt;
	UdmtMusicSymbol muHalfU, muQuarterU, muEighthU, muSixteenthU, muThirtySecondU;
	UdmtMusicSymbol muWholeR, muHalfR, muQuarterR, muEighthR, muSixteenthR, muThirtySecondR;
	UdmtMusicSymbol muDot, muDblDot, muSharp, muFlat, muNatural, muDblSharp, muDblFlat;
	UdmtMusicSymbol muNum0, muNum1, muNum2, muNum3, muNum4, muNum5, muNum6, muNum7, muNum8;
	UdmtMusicSymbol muNum9, muCut, muAccent, muMetronome;
	UdmtMusicSymbol muTriplet;

	// Copied in this instrument list because it is the only thing needed from UdmtMidiOutQueue.
    	private String[] GMImntList = {
"Piano","Bright Piano","Electric Grand","Honky Tonk Piano","Electric Piano 1","Electric Piano 2","Harpsichord","Clavinet",
"Celesta","Glockenspiel","Music Box","Vibraphone","Marimba","Xylophone","Tubular Bell","Dulcimer",
"Hammond Organ","Perc Organ","Rock Organ","Church Organ","Reed Organ","Accordion","Harmonica","Tango Accordion",
"Nylon Str Guitar","Steel String Guitar","Jazz Electric Gtr",
"Clean Guitar","Muted Guitar","Overdrive Guitar","Distortion Guitar","Guitar Harmonics",
"Acoustic Bass","Fingered Bass","Picked Bass","Fretless Bass","Slap Bass 1","Slap Bass 2","Syn Bass 1","Syn Bass 2",
"Violin","Viola","Cello","Contrabass","Tremolo Strings","Pizzicato Strings","Orchestral Harp","Timpani",
"Ensemble Strings","Slow Strings","Synth Strings 1","Synth Strings 2","Choir Aahs","Voice Oohs","Syn Choir","Orchestra Hit",
"Trumpet","Trombone","Tuba","Muted Trumpet","French Horn","Brass Ensemble","Syn Brass 1","Syn Brass 2",
"Soprano Sax","Alto Sax","Tenor Sax","Baritone Sax","Oboe","English Horn","Bassoon","Clarinet",
"Piccolo","Flute","Recorder","Pan Flute","Bottle Blow","Shakuhachi","Whistle","Ocinara",
"Syn Square Wave","Syn Saw Wave","Syn Calliope","Syn Chiff","Syn Charang","Syn Voice","Syn Fifths Saw","Syn Brass and Lead",
"Fantasia","Warm Pad","Polysynth","Space Vox","Bowed Glass","Metal Pad","Halo Pad","Sweep Pad",
"Ice Rain","Soundtrack","Crystal","Atmosphere","Brightness","Goblins","Echo Drops","Sci Fi",
"Sitar","Banjo","Shamisen","Koto","Kalimba","Bag Pipe","Fiddle","Shanai",
"Tinkle Bell","Agogo","Steel Drums","Woodblock","Taiko Drum","Melodic Tom","Syn Drum","Reverse Cymbal",
"Guitar Fret Noise","Breath Noise","Seashore","Bird","Telephone","Helicopter","Applause","Gunshot"
};

	int staffX, staffY;
	//int xLeft, xRight, yTop, yBottom;
	int saveX, saveY;
	
	private int   	numSymIDsOnStaff;
	private int[] 	symIDsOnStaff;
	private	int[]	staffXpos;
	private int[]	staffYpos;
	private Image[] imgs;

	private int	numBeamsOnStaff = 0;
	private	int[]	beamX1;
	private	int[]	beamY1;
	private	int[]	beamX2;
	private	int[]	beamY2;

	private int	numStemExtsOnStaff = 0;
	private int[]	stemExtX1;
	private int[]	stemExtY1;
	private int[]	stemExtX2;
	private int[]	stemExtY2;

	int currNumExercises;
	String currCmdStr;

	private int numCmdCharIx;
	private int[] cmdCharIx;
	private int[] noteSymIdIx;
		
	private int numMidiNotes;
	private int[] midiNotes;

	private int numNotesToDisplay=0;

	private Button checkButton, nextButton, playcorrButton, takeExamButton, showcorrButton;
	private Button shownotesButton, nextLessonButton, exitButton, hintButton;

	private Choice imntCombo;

	private int userImnt;

	private boolean examMode = false;

	//private PopupMenu popup;
	//private final int	MENU_HOR_OFFSET		=	18;
	//private final int	MENU_VER_OFFSET		=	179;

	private String strDrillMode			=	"Time Signature Drill";
	private String strExamMode			=	"Time Signature Exam";
	private String strInstruct 			= 	"Classify, then identify the beat note for the time signature shown below.";

	private int showAbendScreen 			= 	0;
	private String txtAbend 			= 	"";

	private int	numStudentBoxes;
	private String	studentBeatNote, studentSimpleCompound, studentNumBeats;

	private int boxBoundL, boxBoundR, prevBoxBoundR;	// for calculating x coordinates of midpoints between 
	private int[] boxMidpt;				// rhydict input boxes.

	private int cursorBoxNum = 1;			// the box number the cursor is on

	private int numArrowPts = 9;
	private int xArrow0[] =    {0, 5,   5,  10,   0, -10,  -5, -5, 0};
	private int yArrow0[] =    {0, 0, -15, -15, -25, -15, -15,  0, 0};
	private int xPolygon[] =   {0, 0,   0,   0,   0,   0,   0,  0, 0};
	private int yPolygon[] =   {0, 0,   0,   0,   0,   0,   0,  0, 0};

	private String parmSolfType			=	"";
	private String parmExamMode 			= 	"";

	private String parmNextURL 			= 	"";
	private URL nextURL;

	private String parmDrillURL 			= 	"";
	private URL drillURL;

	private String parmDrillExitURL 		= 	"";
	private URL drillExitURL;

	private int numtimeschecked 			= 	0;
	private int numquespresented 			= 	0;
	private int numquescorrect1sttime 		= 	0;


	private String strScore1 			= 	"Score: ";
	private int nScore1 				= 	0;
	private String strScore2 			= 	" out of ";
	private int nScore2 				= 	0;
	private String strScore3 			= 	" = ";
	private int nScorePercent 			= 	0;
	private String strScore4 			= 	"%";
	private String strScoreDisp;

	private String strCorrect 			= 	"Correct.";
	private String strIncorrect			= 	"Incorrect.  Please try again.";
	private String strIncorrectExam 		= 	"Incorrect.";
	private String strShowCorrect			= 	"The correct answer is being displayed.";

	private String strMaster1 			= 	"Congratulations.  You have passed the exam";
	private String strMaster2 			= 	"and may move on if you wish.";
	private String strFeedback 			= 	"";
	private String strExamFeedback1 		= 	"";
	private String strExamFeedback2 		= 	"";

	private String[] flatSyllables={ "do","ra","re","me","mi","fa","se","so","le","la","te","ti"};
	private String[] sharpSyllables={ "do","di","re","ri","mi","fa","fi","so","si","la","li","ti"};
	private String[] currSyllables;

	//int lBtn = 82;	// left coordinate of rhydict button set
	int lBtn = 145;	// left coordinate of rhydict button set
	int wBtn = 35;	// width of each button
	int hBtn = 50;	// height of each button
	int sBtn = 45;	// space between left edges of each button
	int xBtn;		// used for calculating x coordinate of each button
	int yBtn = 250;	// y coord of button set

	boolean playSeqAfterDraw=true;

	// DEFAULT VALUES WHICH MAY BE OVERRIDEN BY PARAMS:
	private String parmExamNumQues;
	private int num_exam_ques			= 	10;
	private String parmExamNumReqd;
	private int num_reqd_to_pass 			= 	9;

	private int[] shuffledQues;
	private int shuffledQuesIndex = 0;
	private int shuffledBase;
	private int majorMinor = +1;

	private String parmProbabDist;
	private int myKey;
	private char myKeySF;

	// amount to add to transposed sequence to get back to original - for calculating rhydict syllable
	// Major keys:               Cb Gb Db Ab Eb Bb   F C  G  D  A  E   B  F# C#
	private int[] keyShiftMaj = {+1,-6,-1,-8,-3,-10,-5,0,-7,-2,-9,-4,-11,-6,-1};

	// Minor keys:                ab eb  bb  f  c  g  d  a  e   b f# c# g# d#  a# (note: offsets adjusted by +4)
	private int[] keyShiftMin = { -8,-3,-10,-5, 0,-7,-2,-9,-4,-11,-6,-1,-8,-3,-10 };

	private int playCtr = 1;
	private int examPlayMax = 4;
	private String parmExamPlayMax;

	private int[] dotXpos = {97,122,147,172,197,222,247,272,297,322,347,372,397,422,447,472,497,522};


	private int tsignum = 0;
	private int tsigden = 0;

//------------------------------------------------------------------------------

	public void init()
	{
		// Display version and debugging info in Java Console
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		rectScreen = getBounds();

		notation = new UdmtTsigNotationApi();
		//System.out.println ("UdmtRhyNotationApi invoked: Version="+notation.getVersion());
		notation.initAPI();

		exercises = new UdmtTimeSigExercises();
				//05/01/2004- New procedure for building URLs requires object to be instantiated once
		urlBuilder = new UdmtURLBuilder();
		// End of 05/01/2004 change.		

		//05/01/2004- New procedure for Exam Log requires object to be instantiated once
		examLog = new UdmtExamLog(); 
		// End of 05/01/2004 change.		


		currNumExercises = exercises.getNumExercises ();


		currSyllables = new String[12];
		for (int i=0; i<12; i++)
		{
			 currSyllables[i]=flatSyllables[i];
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

//		if (examMode)
//		{
//			parmExamPlayMax = getParameter("examPlayMax");
//			if ( parmExamPlayMax != null)	{
//				examPlayMax = Integer.parseInt(parmExamPlayMax);
//				if (examPlayMax < 1)
//				{
//					examPlayMax = 1;
//				}
//			}
//		}

		//if (examMode)	// exam should only present last num_exam_ques exercises
		//{
		//	if (currNumExercises > num_exam_ques)
		//	{
		//		for (int i=0 ; i < (currNumExercises - num_exam_ques) ; i++)
		//		{
		//			currCmdStr = exercises.getExercise();
		//			//System.out.println ("currcmdstr="+currCmdStr);
		//		}
		//	}
		//}

		if (examMode)
		{
			// For TimeSig, want to shuffle whole deck - not just last 10 questions
			//shuffledBase = currNumExercises - num_exam_ques ;
			//shuffledQues = exercises.shuffleN (num_exam_ques);
			//for (int i=0 ; i < num_exam_ques ; i++)
			//{
			//	System.out.println("Shuffled i="+i+" ques="+shuffledQues[i]);
			//}
			shuffledBase = 0 ;
			shuffledQues = exercises.shuffleN (currNumExercises);
			//for (int i=0 ; i < currNumExercises ; i++)
			//{
			//	System.out.println("Shuffled i="+i+" ques="+shuffledQues[i]);
			//}
		}
		else
		{
			shuffledBase = 0 ;
			shuffledQues = exercises.shuffleN (currNumExercises);
			//for (int i=0 ; i < currNumExercises ; i++)
			//{
			//	System.out.println("Shuffled i="+i+" ques="+shuffledQues[i]);
			//}
		}
		
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
			//09/15/2004 - use UrlBuilder only if not in Exam Mode
			if (examMode)
			{
				UdmtExamKey objEk = new UdmtExamKey();
				String sExamKey = objEk.getExamKey("TSIGPASS");
				parmNextURL = parmNextURL + "?r="+sExamKey;
				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY!!!
			}
			else
			{
				parmNextURL = urlBuilder.buildURL (this.getCodeBase() , parmNextURL);
			}

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
				//09/15/2004 - use UrlBuilder only if not in Exam Mode
				if (examMode)
				{
					UdmtExamKey objEk = new UdmtExamKey();
					String sExamKey = objEk.getExamKey("TSIGFAIL");
					parmDrillURL = parmDrillURL + "?r="+sExamKey;
					//System.out.println ("parmDrillURL="+parmDrillURL); //FOR DEBUGGING ONLY!!!
				}
				else
				{
					parmDrillURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillURL);
				}

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
				//09/15/2004 - Note: drillExitURL is not used in Exam Mode
				parmDrillExitURL = urlBuilder.buildURL (this.getCodeBase() , parmDrillExitURL);

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

		// Triplet logic added 5/7/2004

		MediaTracker mt = new MediaTracker(this);
		URL url = getCodeBase();
		imgAllSymbols = getImage (url, "symbols-28pt-tr.gif");
		imgTriplet = getImage (url, "triplet-tr.gif");

		mt.addImage (imgAllSymbols, 1);
		mt.addImage (imgTriplet, 1);

		try 
		{
			mt.waitForAll();
			setUpSymbolObjects();
			//System.out.println("done setting up symbol objects");
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}


		staffX = 20;
		staffY = 120;

//		try	{
//			sequencer = MidiSystem.getSequencer();
//			sequencer.open();     
//			sequencer.addMetaEventListener(this);            
//			System.out.println("MIDI initialized successfully.");
//		}
//		catch ( Exception e )	{
//			System.out.println("ERROR: Exception when opening Java MIDI sequencer:");
//			e.printStackTrace();
//			showAbendScreen = 1;
//			txtAbend = "Exception when opening Java MIDI sequencer.";
//		}

		// 05-22-2004: For timesig drill - always use shuffled questions regardless of drill/exam

		//System.out.println ("getting exercise# : "+(shuffledBase + shuffledQues [ shuffledQuesIndex ]));
		currCmdStr = exercises.getExerciseNum ( shuffledBase + shuffledQues [ shuffledQuesIndex ] );
		shuffledQuesIndex++;
		if (shuffledQuesIndex == num_exam_ques)
		{
			shuffledQuesIndex = 0;
		}
		//old code for drill mode:
		//else
		//{
		//	currCmdStr = exercises.getExercise();
		//}

		//To test the questions in sequence:
		//currCmdStr = exercises.getExercise();

		//System.out.println ("Original cmdstr="+currCmdStr);


		notation.setCmdStr (currCmdStr);

		//seq = notation.getSequence();
		


		//System.out.println ("Getting symbol arrays:");
		numSymIDsOnStaff = notation.getNumSymIDsOnStaff();

 		symIDsOnStaff = new int[100];
		staffXpos = new int[100];
		staffYpos = new int[100];

		//05/10/04
		//System.out.println ("Getting timesig:");
		tsignum = notation.getTsigNumerator();
		tsigden = notation.getTsigDenominator();


		symIDsOnStaff = notation.getSymIDsOnStaff();
		staffXpos = notation.getStaffXPos();
		staffYpos = notation.getStaffYPos();

		numBeamsOnStaff = notation.getNumBeamsOnStaff();

		beamX1 = new int[50];
		beamY1 = new int[50];
		beamX2 = new int[50];
		beamY2 = new int[50];

		beamX1 = notation.getBeamX1();
		beamY1 = notation.getBeamY1();
		beamX2 = notation.getBeamX2();
		beamY2 = notation.getBeamY2();
		
		numStemExtsOnStaff = notation.getNumStemExtsOnStaff();

		stemExtX1 = new int[50];
		stemExtY1 = new int[50];
		stemExtX2 = new int[50];
		stemExtY2 = new int[50];

		stemExtX1 = notation.getStemExtX1();
		stemExtY1 = notation.getStemExtY1();
		stemExtX2 = notation.getStemExtX2();
		stemExtY2 = notation.getStemExtY2();

		numCmdCharIx = notation.getNumCmdCharIndexes();
		cmdCharIx = new int[50];
		cmdCharIx = notation.getCmdCharIndexes();
		noteSymIdIx = new int[50];
		noteSymIdIx = notation.getNoteSymIdIndexes();		

		numMidiNotes = notation.getNumMidiNotes();
		midiNotes = new int[50];
		midiNotes = notation.getMidiNotes();

		boxMidpt = new int[50];

		for (int n=0 ; n < numCmdCharIx ; n++)
		{
			boxBoundL = staffX + staffXpos[noteSymIdIx[n]] - 5 - 1 ;
			boxBoundR = boxBoundL + 22;
			if (n > 1) {
				boxMidpt[n-1] =  prevBoxBoundR + ((boxBoundL - prevBoxBoundR) / 2) ;
				//System.out.println ("Midpt["+(n-1)+"]="+boxMidpt[n-1]);
			}
			prevBoxBoundR = boxBoundR;
			//System.out.println ("n="+n+" boxBoundL="+boxBoundL+" boxBoundR="+boxBoundR);
		}
	
		resetStudentAnswer();

		//System.out.println ("Adding buttons to layout");

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

			playcorrButton = new Button("PLAY");
			playcorrButton.addActionListener(this);
		   	playcorrButton.setEnabled(false);
			playcorrButton.setBounds ( 60,350,110,30 );
			add(playcorrButton);

			hintButton = new Button("HINT");
     			hintButton.addActionListener(this);
			hintButton.setEnabled(false);
			hintButton.setBounds ( 60,300,110,30);
			add (hintButton);

			shownotesButton = new Button("SHOW NOTES");
			shownotesButton.setEnabled(false);        
			shownotesButton.addActionListener(this);
			shownotesButton.setBounds ( 180,350,110,30 );
			add(shownotesButton);

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


		btnSimple = new Button("Simple");
		btnSimple.addActionListener(this);	
		btnSimple.setVisible(true);
		btnSimple.setBounds ( 145,100,100,30);
		add(btnSimple);

		btnCompound = new Button("Compound");
		btnCompound.addActionListener(this);
		btnCompound.setVisible(true);
		btnCompound.setBounds ( 145,140,100,30);
		add(btnCompound);

		btnDuple = new Button("Duple");
		btnDuple.addActionListener(this);
		btnDuple.setVisible(true);
		btnDuple.setBounds ( 265,100,100,30);
		add(btnDuple);

		btnQuintuple = new Button("Quintuple");
		btnQuintuple.addActionListener(this);
		btnQuintuple.setVisible(true);
		btnQuintuple.setBounds ( 265,140,100,30);
		add(btnQuintuple);


		btnTriple = new Button("Triple");
		btnTriple.addActionListener(this);
		btnTriple.setVisible(true);
		btnTriple.setBounds ( 375,100,100,30);
		add(btnTriple);

		btnSextuple = new Button("Sextuple");
		btnSextuple.addActionListener(this);
		btnSextuple.setVisible(true);
		btnSextuple.setBounds ( 375,140,100,30);
		add(btnSextuple);


		btnQuadruple = new Button("Quadruple");
		btnQuadruple.addActionListener(this);
		btnQuadruple.setVisible(true);
		btnQuadruple.setBounds ( 485,100,100,30);
		add(btnQuadruple);

		btnSeptuple = new Button("Septuple");
		btnSeptuple.addActionListener(this);
		btnSeptuple.setVisible(true);
		btnSeptuple.setBounds ( 485,140,100,30);
		add(btnSeptuple);



		xBtn = lBtn;

		//btnLeftArrow = new Button("<");
		//btnLeftArrow.setEnabled(true);        
		//btnLeftArrow.addActionListener(this);
		//btnLeftArrow.setBounds ( xBtn,yBtn,wBtn,hBtn );
		//add(btnLeftArrow);

		//btnLeftArrow = new UdmtImageButton ("rhy-arrow-left-black-tr.gif", this);
		//btnLeftArrow.setActionCommand ("<");
		//btnLeftArrow.addActionListener(this);
		//btnLeftArrow.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		//add (btnLeftArrow);

		//xBtn += sBtn;
		btnWN = new UdmtImageButton ("wholenote-tr.gif", this);
		btnWN.setActionCommand ("WN");
		btnWN.addActionListener(this);
		btnWN.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnWN);

		xBtn += sBtn;
		btnWNdot = new UdmtImageButton ("wholenote-dot-tr.gif", this);
		btnWNdot.setActionCommand ("WD");
		btnWNdot.addActionListener(this);
		btnWNdot.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnWNdot);


		xBtn += sBtn;
		btnHN = new UdmtImageButton ("halfnote-tr.gif", this);
		btnHN.setActionCommand ("HN");
		btnHN.addActionListener(this);
		btnHN.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnHN);

		xBtn += sBtn;
		btnHNdot = new UdmtImageButton ("halfnote-dot-tr.gif", this);
		btnHNdot.setActionCommand ("HD");
		btnHNdot.addActionListener(this);
		btnHNdot.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnHNdot);

		xBtn += sBtn;
		btnQN = new UdmtImageButton ("qtrnote-tr.gif", this);
		btnQN.setActionCommand ("QN");
		btnQN.addActionListener(this);
		btnQN.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnQN);

		xBtn += sBtn;
		btnQNdot = new UdmtImageButton ("qtrnote-dot-tr.gif", this);
		btnQNdot.setActionCommand ("QD");
		btnQNdot.addActionListener(this);
		btnQNdot.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnQNdot);

		xBtn += sBtn;
		btnEN = new UdmtImageButton ("eighthnote-tr.gif", this);
		btnEN.setActionCommand ("EN");
		btnEN.addActionListener(this);
		btnEN.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnEN);

		xBtn += sBtn;
		btnENdot = new UdmtImageButton ("eighthnote-dot-tr.gif", this);
		btnENdot.setActionCommand ("ED");
		btnENdot.addActionListener(this);
		btnENdot.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnENdot);

		xBtn += sBtn;
		btnSN = new UdmtImageButton ("sixteenthnote-tr.gif", this);
		btnSN.setActionCommand ("SN");
		btnSN.addActionListener(this);
		btnSN.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnSN);

		xBtn += sBtn;
		btnSNdot= new UdmtImageButton ("sixteenthnote-dot-tr.gif", this);
		btnSNdot.setActionCommand ("SD");
		btnSNdot.addActionListener(this);
		btnSNdot.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
		add (btnSNdot);

		//xBtn += sBtn;
		//btnRightArrow = new UdmtImageButton ("rhy-arrow-right-black-tr.gif", this);
		//btnRightArrow.setActionCommand (">");
		//btnRightArrow.addActionListener(this);
		//btnRightArrow.setBounds ( xBtn - 1 , staffY + 80 - 2 ,wBtn,hBtn );
		//add (btnRightArrow);



		//System.out.println ("Adding instrument combobox");

		//imntCombo = new Choice();
		//for (int i=0 ; i <= 127 ; i++)	//{
			//imntCombo.add ( GMImntList[i] );
		//} 
		//if (examMode) {	
			//imntCombo.setBounds ( 60,350,110,30 );
		//}
		//else	{
			//imntCombo.setBounds ( 60,305,110,30);
		//}
		//add(imntCombo);
		//imntCombo.addItemListener(this);	

		// For rhythm, choose default patch that does not decay fast
		//imntCombo.select(11); // vibraphone
		//userImnt = 11;
		notation.reset();
		//notation.setSequencePatch (userImnt);
		notation.setCmdStr (currCmdStr);
		//seq = notation.getSequence();


		// For rhydict drill, popup menu always active.
		//xLeft = staffX + 50 ;
		//xRight = staffX + 547; // stop at double bar
		//yTop = staffY - 30 ;
		//yBottom = staffY + 130 ;
		//xLeft = 0;
		//xRight = rectScreen.width;
		//yTop = 0;
		//yBottom = rectScreen.height;

		//setupPopupMenu();
		//add ( popup );

		addMouseListener(this);


	}

//------------------------------------------------------------------------------

	private void resetStudentAnswer()
	{
		studentBeatNote = "";
		studentSimpleCompound = "";
		studentNumBeats = "";
		numNotesToDisplay = numCmdCharIx;		// to display whole sequence
		repaint();
	}

	private void resetNotation()
	{
		//05/10/04
		//System.out.println ("Getting timesig:");
		tsignum = notation.getTsigNumerator();
		tsigden = notation.getTsigDenominator();

		numSymIDsOnStaff = notation.getNumSymIDsOnStaff();
		symIDsOnStaff = notation.getSymIDsOnStaff();

		staffXpos = notation.getStaffXPos();
		staffYpos = notation.getStaffYPos();

		numBeamsOnStaff = notation.getNumBeamsOnStaff();
		beamX1 = notation.getBeamX1();
		beamY1 = notation.getBeamY1();
		beamX2 = notation.getBeamX2();
		beamY2 = notation.getBeamY2();

		numStemExtsOnStaff = notation.getNumStemExtsOnStaff();
		stemExtX1 = notation.getStemExtX1();
		stemExtY1 = notation.getStemExtY1();
		stemExtX2 = notation.getStemExtX2();
		stemExtY2 = notation.getStemExtY2();

		numCmdCharIx = notation.getNumCmdCharIndexes();
		cmdCharIx = notation.getCmdCharIndexes();
		noteSymIdIx = notation.getNoteSymIdIndexes();		

		numMidiNotes = notation.getNumMidiNotes();
		midiNotes = notation.getMidiNotes();

		//System.out.println ("In resetNotation()");
		//for (int i=0 ; i < numMidiNotes ; i++)
		//{
		//	System.out.println ("midiNotes["+i+"]="+midiNotes[i]);
		//}

		for (int n=0 ; n < numCmdCharIx ; n++)
		{
			boxBoundL = staffX + staffXpos[noteSymIdIx[n]] - 5 - 1 ;
			boxBoundR = boxBoundL + 22;
			if (n > 1) {
				boxMidpt[n-1] =  prevBoxBoundR + ((boxBoundL - prevBoxBoundR) / 2) ;
				//System.out.println ("Midpt["+(n-1)+"]="+boxMidpt[n-1]);
			}
			prevBoxBoundR = boxBoundR;
			//System.out.println ("n="+n+" boxBoundL="+boxBoundL+" boxBoundR="+boxBoundR);
		}
	}

//------------------------------------------------------------------------------

	private void setUpSymbolObjects()
	{
		imgs = new Image[100];

		muNoteheadFilled 		= new UdmtMusicSymbol();
		muNoteheadFilled.CropImage	( imgAllSymbols, 20, 45, 40, 60 );
		imgs[0] = muNoteheadFilled.getImage();

		muNoteheadUnfilled 		= new UdmtMusicSymbol();
		muNoteheadUnfilled.CropImage	( imgAllSymbols, 49, 45, 70, 60 );
		imgs[1] = muNoteheadUnfilled.getImage();

		muTrebleClef 			= new UdmtMusicSymbol();
		muTrebleClef.CropImage 		( imgAllSymbols, 80, 0, 110, 70);
		imgs[2] = muTrebleClef.getImage();

		muAltoClef 			= new UdmtMusicSymbol();
		muAltoClef.CropImage 		( imgAllSymbols, 120, 0, 150, 70);
		imgs[3] = muAltoClef.getImage();

		muBassClef 			= new UdmtMusicSymbol();
		muBassClef.CropImage 		( imgAllSymbols, 165, 0, 190, 50);
		imgs[4] = muBassClef.getImage();

		muCommon 			= new UdmtMusicSymbol();
		muCommon.CropImage 		( imgAllSymbols, 205, 40, 225, 65 );
		imgs[5] = muCommon.getImage();

		muLeger 			= new UdmtMusicSymbol();
		muLeger.CropImage 		( imgAllSymbols, 235, 15, 265, 55 );
		imgs[6] = muLeger.getImage();

		muWhole	 			= new UdmtMusicSymbol();
		muWhole.CropImage 		( imgAllSymbols, 280, 45, 295, 90 );
		imgs[7] = muWhole.getImage();

		muHalfD	 			= new UdmtMusicSymbol();
		muHalfD.CropImage 		( imgAllSymbols, 310, 45, 325, 90 );
		imgs[8] = muHalfD.getImage();

		muQuarterD	 		= new UdmtMusicSymbol();
		muQuarterD.CropImage 		( imgAllSymbols, 340, 45, 355, 90 );
		imgs[9] = muQuarterD.getImage();

		muEighthD	 		= new UdmtMusicSymbol();
		muEighthD.CropImage 		( imgAllSymbols, 368, 45, 385, 90 );
		imgs[10] = muEighthD.getImage();

		muSixteenthD	 		= new UdmtMusicSymbol();
		muSixteenthD.CropImage 		( imgAllSymbols, 396, 45, 415, 90 );
		imgs[11] = muSixteenthD.getImage();

		muThirtySecondD	 		= new UdmtMusicSymbol();
		muThirtySecondD.CropImage 	( imgAllSymbols, 424, 45, 445, 90 );
		imgs[12] = muThirtySecondD.getImage();

		muStaff 			= new UdmtMusicSymbol();
		muStaff.CropImage 		( imgAllSymbols, 20, 120, 60, 160 );
		imgs[13] = muStaff.getImage();

		muThickBar 			= new UdmtMusicSymbol();
		muThickBar.CropImage 		( imgAllSymbols, 75, 120, 85, 160 );
		imgs[14] = muThickBar.getImage();

		muLeftRpt 			= new UdmtMusicSymbol();
		muLeftRpt.CropImage 		( imgAllSymbols, 95, 120, 120, 160 );
		imgs[15] = muLeftRpt.getImage();

		muLeftDblBar 			= new UdmtMusicSymbol();
		muLeftDblBar.CropImage 		( imgAllSymbols, 130, 120, 145, 160 );
		imgs[16] = muLeftDblBar.getImage();

		muRtDblBar 			= new UdmtMusicSymbol();
		muRtDblBar.CropImage		( imgAllSymbols, 160,120,170,160);	
		imgs[17] = muRtDblBar.getImage();

		muBar 				= new UdmtMusicSymbol();
		muBar.CropImage 		( imgAllSymbols, 190, 120, 195, 160);	
		imgs[18] = muBar.getImage();

		muRightRpt 			= new UdmtMusicSymbol();
		muRightRpt.CropImage 		( imgAllSymbols, 225, 120, 250, 160 );
		imgs[19] = muRightRpt.getImage();

		muHalfU 			= new UdmtMusicSymbol();
		muHalfU.CropImage 		( imgAllSymbols, 265, 120, 280, 165 );
		imgs[20] = muHalfU.getImage();

		muQuarterU 			= new UdmtMusicSymbol();
		muQuarterU.CropImage 		( imgAllSymbols, 300, 120, 315, 165 );
		imgs[21] = muQuarterU.getImage();

		muEighthU 			= new UdmtMusicSymbol();
		muEighthU.CropImage 		( imgAllSymbols, 328, 120, 350, 165 );
		imgs[22] = muEighthU.getImage();

		muSixteenthU 			= new UdmtMusicSymbol();
		muSixteenthU.CropImage 		( imgAllSymbols, 365, 120, 390, 165 );
		imgs[23] = muSixteenthU.getImage();

		muThirtySecondU			= new UdmtMusicSymbol();
		muThirtySecondU.CropImage 	( imgAllSymbols, 402, 120, 430, 165 );
		imgs[24] = muThirtySecondU.getImage();

		muWholeR 			= new UdmtMusicSymbol();
		muWholeR.CropImage 		( imgAllSymbols, 20, 235, 35, 275 );
		imgs[25] = muWholeR.getImage();

		muHalfR 			= new UdmtMusicSymbol();
		muHalfR.CropImage 		( imgAllSymbols, 55, 235, 70, 275 );
		imgs[26] = muHalfR.getImage();

		muQuarterR 			= new UdmtMusicSymbol();
		muQuarterR.CropImage 		( imgAllSymbols, 85, 235, 100, 275 );
		imgs[27] = muQuarterR.getImage();

		muEighthR 			= new UdmtMusicSymbol();
		muEighthR.CropImage 		( imgAllSymbols, 115, 240, 130, 275 );
		imgs[28] = muEighthR.getImage();

		muSixteenthR 			= new UdmtMusicSymbol();
		muSixteenthR.CropImage 		( imgAllSymbols, 140, 231, 160, 275 );
		imgs[29] = muSixteenthR.getImage();

		muThirtySecondR			= new UdmtMusicSymbol();
		muThirtySecondR.CropImage 	( imgAllSymbols, 170, 232, 190, 275 );
		imgs[30] = muThirtySecondR.getImage();

		muDot				= new UdmtMusicSymbol();
		muDot.CropImage		 	( imgAllSymbols, 206, 253, 215, 265 );
		imgs[31] = muDot.getImage();

		muDblDot			= new UdmtMusicSymbol();
		muDblDot.CropImage		( imgAllSymbols, 237, 253, 255, 265 );
		imgs[32] = muDblDot.getImage();

		muSharp 			= new UdmtMusicSymbol();
		muSharp.CropImage 		( imgAllSymbols, 275,235,290,275);	
		imgs[33] = muSharp.getImage();

		muFlat	 			= new UdmtMusicSymbol();
		muFlat.CropImage 		( imgAllSymbols, 310,235,325,275);	
		imgs[34] = muFlat.getImage();

		muNatural			= new UdmtMusicSymbol();
		muNatural.CropImage		( imgAllSymbols, 340, 235, 355, 275 );
		imgs[35] = muNatural.getImage();

		muDblSharp 			= new UdmtMusicSymbol();
		muDblSharp.CropImage 		( imgAllSymbols, 370, 235, 385, 275);	
		imgs[36] = muDblSharp.getImage();

		muDblFlat			= new UdmtMusicSymbol();
		muDblFlat.CropImage 		( imgAllSymbols, 398, 235 ,420 ,275);	
		imgs[37] = muDblFlat.getImage();

		muNum0 				= new UdmtMusicSymbol();
		muNum0.CropImage 		( imgAllSymbols, 20, 355, 37, 375 );
		imgs[38] = muNum0.getImage();

		muNum1 				= new UdmtMusicSymbol();
		muNum1.CropImage 		( imgAllSymbols, 50, 355, 67, 375 );
		imgs[39] = muNum1.getImage();

		muNum2 				= new UdmtMusicSymbol();
		muNum2.CropImage 		( imgAllSymbols, 80, 355, 97, 375 );
		imgs[40] = muNum2.getImage();

		muNum3 				= new UdmtMusicSymbol();
		muNum3.CropImage 		( imgAllSymbols, 110, 355, 127, 375 );
		imgs[41] = muNum3.getImage();

		muNum4 				= new UdmtMusicSymbol();
		muNum4.CropImage 		( imgAllSymbols, 140, 355, 157, 375 );
		imgs[42] = muNum4.getImage();

		muNum5 				= new UdmtMusicSymbol();
		muNum5.CropImage 		( imgAllSymbols, 170, 355, 187, 375 );
		imgs[43] = muNum5.getImage();

		muNum6 				= new UdmtMusicSymbol();
		muNum6.CropImage 		( imgAllSymbols, 200, 355, 217, 375 );
		imgs[44] = muNum6.getImage();

		muNum7 				= new UdmtMusicSymbol();
		muNum7.CropImage 		( imgAllSymbols, 230, 355, 247, 375 );
		imgs[45] = muNum7.getImage();

		muNum8 				= new UdmtMusicSymbol();
		muNum8.CropImage 		( imgAllSymbols, 262, 355, 277, 375 );
		imgs[46] = muNum8.getImage();

		muNum9 				= new UdmtMusicSymbol();
		muNum9.CropImage 		( imgAllSymbols, 292, 355, 307, 375 );
		imgs[47] = muNum9.getImage();

		muCut				= new UdmtMusicSymbol();
		muCut.CropImage 		( imgAllSymbols, 362, 350, 377, 380 );
		imgs[48] = muCut.getImage();

		muAccent			= new UdmtMusicSymbol();
		muAccent.CropImage 		( imgAllSymbols, 332, 355, 347, 375 );
		imgs[49] = muAccent.getImage();

		muMetronome			= new UdmtMusicSymbol();
		muMetronome.CropImage 		( imgAllSymbols, 400, 345, 420, 370 );
		imgs[50] = muMetronome.getImage();

		muTriplet			= new UdmtMusicSymbol();
		muTriplet.CropImage 		( imgTriplet, 0, 0, 30, 40 );
		imgs[51] = muTriplet.getImage();

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

	private void drawScreen( Graphics g )
	{
		Font headerfont, txtfont;

		g.setColor ( Color.white );
		g.fillRect(0,0,rectScreen.width,rectScreen.height);
		g.setColor ( Color.black );

		//Uncomment to see mouse input range for popup menu:
		//g.drawRect (xLeft, yTop, (xRight-xLeft), (yBottom-yTop));

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
		
		centerString (g, strInstruct, txtfont, 60);


		//for (int x=staffX ; x < staffX+530 ; x+=37 )
		//{
		//	g.drawImage (muStaff.getImage(), x, staffY, this );
		//}
		//g.drawImage (muBar.getImage(), staffX - 2 , staffY, this);
		//g.drawImage (muRtDblBar.getImage(), staffX+550, staffY, this);


		for (int x=staffX ; x < staffX+75 ; x+=35 )	{
			g.drawImage (muStaff.getImage(), x, staffY, this );
		}
		g.drawImage (muBar.getImage(), staffX - 1, staffY, this);
		g.drawImage (muRtDblBar.getImage(), staffX+99, staffY, this);

		// Draw the music symbols: This needs to be modified if individual notes are to be displayed
		// not necessarily in order.

		//System.out.println("numnotestodisplay="+numNotesToDisplay);
		//if (numNotesToDisplay > 0)
		//{
			//Replaced line below to fix bug where dot on last note not drawn
			//for (int n=0 ; n <= (noteSymIdIx[numNotesToDisplay-1]) ; n++)
			for (int n=0 ; n < numSymIDsOnStaff ; n++)
			{
				g.drawImage 	( imgs[symIDsOnStaff[n]]
					, staffX + staffXpos[n]
					, staffY + staffYpos[n]
					, this
					);

			}
		//}
		//else // just display all symbols before first note
		//{
			//for (int n=0 ; n < (noteSymIdIx[0]) ; n++)
			//{
				//g.drawImage 	( imgs[symIDsOnStaff[n]]
//					, //staffX + staffXpos[n]
//					, //staffY + staffYpos[n]
//					, this
//					);
//
//			}
//		}

		// Draw dots above note positions and boxes below note positions
//		for (int n=0 ; n < 18 ; n++)
//		{
//			if (studentCorrect[n])	//{
//				g.setColor //(Color.black);
//			}
//			else	{
//				g.setColor //(Color.red);
//			}
//
//			g.drawImage 	( //imgs[31]
//					, //staffX + dotXpos[n] - 21
//					, //staffY - 30
//					, this
//					);
//
//			if (cursorBoxNum == //(n+1))
//			{
//				g.setColor //(Color.blue);
//				for (int p = 0 ; //p < numArrowPts ; p++ )
//				{
//					//xPolygon[p] =  xArrow0[p] + staffX + //dotXpos[n] - 18;
//					//yPolygon[p] =  yArrow0[p] + staffY + 68 - 1;
//				}
//				g.fillPolygon ( //xPolygon, yPolygon, numArrowPts );
//			}
//
//			g.setColor //(Color.black);
//		}
//		g.setColor (Color.black);

		//System.out.println("numBeamsOnStaff="+numBeamsOnStaff);

		//5/13/04 removed drawing of beams and stem extensions

		// 5/18/04 - put boxes around buttons
		g.drawRect (138,93,115,85);
		g.drawRect (258,93,335,85);
		g.drawRect (138,187,455,76);

		// 5/19/04 - draw student answer
		g.drawString ( "Your answer:", 15, 200 ) ;
		g.drawString ( studentSimpleCompound, 15, 220 ) ;
		g.drawString ( studentNumBeats, 15, 240 ) ;
		g.drawString ( studentBeatNote, 15, 260 ) ;

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
		centerString (g, strFeedback, txtfont, 291);
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

	public void centerStringAroundX ( Graphics g, String strIn, Font fontIn, int Xcoord, int Ycoord )
	{
		FontMetrics fm = g.getFontMetrics(fontIn);
		g.drawString ( strIn, ( Xcoord - (fm.stringWidth(strIn) / 2) ), Ycoord ) ;
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

		int xcoord, ycoord;
		xcoord = e.getX();
		ycoord = e.getY();

		//if ( (xcoord >= xLeft) && (xcoord <= xRight) 
		//	&& (ycoord >= yTop) && (ycoord <= yBottom) )
		//{
		//	popup.show ( e.getComponent(), xcoord - MENU_HOR_OFFSET, ycoord - MENU_VER_OFFSET );
		//	saveX = xcoord ;
		//	saveY = ycoord ;
		//}

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

	public void cursorFwd()
	{
		//System.out.println("CursorFwd: cursorboxnum="+cursorBoxNum);
		cursorBoxNum++;
		if (cursorBoxNum >= 18)
		{
			cursorBoxNum=1;
		}
	} 
	public void cursorBack()
	{
		//System.out.println("CursorBack: cursorboxnum="+cursorBoxNum);
		cursorBoxNum--;
		if (cursorBoxNum < 1)
		{
			cursorBoxNum=18;
		}
	} 

	public void actionPerformed (ActionEvent e)
	{
		int solfval = 0;
		String syllable;

		//System.out.println ("Menu Item: " + e.getActionCommand() );
		//System.out.println ("Clicked at: " + saveX + " , " + saveY);

		if (e.getActionCommand().equals ( "WN" ) )
		{
			studentBeatNote = "Whole";
		}
		else if (e.getActionCommand().equals ( "WD" ) )
		{
			studentBeatNote = "Dotted Whole";
		}
		else if (e.getActionCommand().equals ( "HN" ) )
		{
			studentBeatNote = "Half";
		}
		else if (e.getActionCommand().equals ( "HD" ) )
		{
			studentBeatNote = "Dotted Half";
		}
		else if (e.getActionCommand().equals ( "QN" ) )
		{
			studentBeatNote = "Quarter";
		}
		else if (e.getActionCommand().equals ( "QD" ) )
		{
			studentBeatNote = "Dotted Quarter";
		}
		else if (e.getActionCommand().equals ( "EN" ) )
		{
			studentBeatNote = "Eighth";
		}
		else if (e.getActionCommand().equals ( "ED" ) )
		{
			studentBeatNote = "Dotted Eighth";
		}
		else if (e.getActionCommand().equals ( "SN" ) )
		{
			studentBeatNote = "Sixteenth";
		}
		else if (e.getActionCommand().equals ( "SD" ) )
		{
			studentBeatNote = "Dotted Sixteenth";
		}
		else if (e.getActionCommand().equals ( "Simple" ) )
		{
			studentSimpleCompound = "Simple";
		}
		else if (e.getActionCommand().equals ( "Compound" ) )
		{
			studentSimpleCompound = "Compound";
		}
		else if (e.getActionCommand().equals ( "Duple" ) )
		{
			studentNumBeats = "Duple";
		}
		else if (e.getActionCommand().equals ( "Triple" ) )
		{
			studentNumBeats = "Triple";
		}
		else if (e.getActionCommand().equals ( "Quadruple" ) )
		{
			studentNumBeats = "Quadruple";
		}
		else if (e.getActionCommand().equals ( "Quintuple" ) )
		{
			studentNumBeats = "Quintuple";
		}
		else if (e.getActionCommand().equals ( "Sextuple" ) )
		{
			studentNumBeats = "Sextuple";
		}
		else if (e.getActionCommand().equals ( "Septuple" ) )
		{
			studentNumBeats = "Septuple";
		}

		if ( (studentBeatNote.equals(""))
			|| (studentSimpleCompound.equals(""))
			|| (studentNumBeats.equals(""))
		   )
		{
			checkButton.setEnabled(false);
		}
		else
		{
			checkButton.setEnabled(true);
		}

		repaint();

		if (e.getActionCommand().equals ( "CHECK" ) )
		{
			//System.out.println("Check button pressed");
			//System.out.println("Time Sig="+tsignum+"/"+tsigden );

			//System.out.println("Student answer:" 
			//	+" " + studentSimpleCompound 
			//	+" " + studentNumBeats
			//	+" " +studentBeatNote
			//	);

			String corrSimpleCompound = getCorrectSimpleCompound (tsignum,tsigden );
			String corrNumBeats = getCorrectNumBeats (tsignum,tsigden );
			String corrBeatNote = getCorrectBeatNote (tsignum,tsigden );

			//System.out.println("Correct answer:" 
			//	+" " + corrSimpleCompound 
			//	+" " + corrNumBeats
			//	+" " + corrBeatNote
			//	);

			boolean studentGotAllNotesCorrect = false;

			if (  (studentSimpleCompound.equals(corrSimpleCompound))
			   && (studentNumBeats.equals(corrNumBeats))
			   && (studentBeatNote.equals(corrBeatNote))
			   )
			{
				studentGotAllNotesCorrect = true;
			}

			if ( numtimeschecked == 0)	{
				numquespresented++;
				if (studentGotAllNotesCorrect)
				{
					numquescorrect1sttime++;
				}
			}
		
			numtimeschecked++;

			if (numtimeschecked >= 2)
			{
				if (!examMode)
				{
					showcorrButton.setEnabled(true);
				}
			}

			if (studentGotAllNotesCorrect)
			{
				strFeedback = strCorrect;
				numNotesToDisplay = numCmdCharIx;		// to display whole sequence
				nextButton.setEnabled(true);
				checkButton.setEnabled(false);
				if (!examMode)
				{
					//shownotesButton.setLabel ("HIDE NOTES");
					showcorrButton.setEnabled(false);
				}
			}
			else
			{
				if (examMode)
				{
					strFeedback = strIncorrectExam;
					nextButton.setEnabled(true);
				}
				else
				{
					strFeedback = strIncorrect;
				}
			}


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
					//imntCombo.setVisible(false);

					//05/01/2004: Write to Exam Log and branch immediately to NEXT LESSON page.
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

					exitButton.setVisible(true);
					checkButton.setEnabled(false);
					nextButton.setEnabled(false);
					//imntCombo.setVisible(false);

					//05/01/2004 - change to write Exam log and branch immediately to "SORRY" page.
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
			//System.out.println("Next button pressed");
			//stopMidiPlayback();

			if (examMode)
			{
				//System.out.println ("getting exercise# : "+(shuffledBase + shuffledQues [ shuffledQuesIndex ]));
				currCmdStr = exercises.getExerciseNum ( shuffledBase + shuffledQues [ shuffledQuesIndex ] );
				shuffledQuesIndex++;
				if (shuffledQuesIndex == num_exam_ques)
				{					
					shuffledQuesIndex = 0;
				}
			}
			else
			{
				// Replaced line below with code below - so drill uses shuffled deck
				//currCmdStr = exercises.getExercise();

				//System.out.println ("getting exercise# : "+(shuffledBase + shuffledQues [ shuffledQuesIndex ]));
				currCmdStr = exercises.getExerciseNum ( shuffledBase + shuffledQues [ shuffledQuesIndex ] );
				shuffledQuesIndex++;
				if (shuffledQuesIndex == num_exam_ques)
				{					
					shuffledQuesIndex = 0;
				}
			}

			//System.out.println ("Original cmdstr="+currCmdStr);



			//System.out.println ("myKey="+myKey+" Transposed cmdstr="+currCmdStr);

			notation.reset();
			notation.setCmdStr (currCmdStr);
			//seq = notation.getSequence();
			resetNotation();
			resetStudentAnswer();

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			if (!examMode)
			{
				playcorrButton.setEnabled(false);
				playCtr = 1;

				showcorrButton.setEnabled(false);
				shownotesButton.setEnabled(false);
				shownotesButton.setLabel ("SHOW NOTES");
			}

			numtimeschecked = 0;
			strFeedback = "";

			//reset cursor position for new question
			cursorBoxNum = 1;
			playSeqAfterDraw=true;
		
			repaint();
		}

//		if (e.getActionCommand().equals ( "PLAY" ) )
//		{
//			//System.out.println("Play button pressed");
//		
//			playCtr++;
//			if ( (!examMode) || ( (examMode) && (playCtr <= examPlayMax) ) )
//			{
//				//startMidiPlayback();
//			}
//			if ((examMode) && (playCtr >= examPlayMax ))
//			{
//				playcorrButton.setEnabled(false);
//			}
//		}
//		if (e.getActionCommand().equals ( "SHOW NOTES" ) )
//		{
//			//System.out.println("Show Notes button pressed");
//			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
//			//shownotesButton.setLabel ("HIDE NOTES");
//			repaint();
//		}
//		if (e.getActionCommand().equals ( "HIDE NOTES" ) )
//		{
//			//System.out.println("Show Notes button pressed");
//			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
//
//			shownotesButton.setLabel ("SHOW NOTES");
//			repaint();
//		}

		if (e.getActionCommand().equals ( "SHOW CORRECT" ) )
		{
			//System.out.println("Show Correct button pressed");

			String corrSimpleCompound = getCorrectSimpleCompound (tsignum,tsigden );
			String corrNumBeats = getCorrectNumBeats (tsignum,tsigden );
			String corrBeatNote = getCorrectBeatNote (tsignum,tsigden );

			studentSimpleCompound = corrSimpleCompound;
			studentNumBeats = corrNumBeats;
			studentBeatNote = corrBeatNote;
			

			strFeedback = strShowCorrect;

			checkButton.setEnabled(false);
			nextButton.setEnabled(true);
			showcorrButton.setEnabled(false);

			repaint();
		}
		if (e.getActionCommand().equals ( "TAKE EXAM" ) )
		{
			//05/02/2004: added for debugging:
			System.out.println("Take Exam button pressed");
			System.out.println("nextURL="+nextURL);
			System.out.println("appletcontext="+this.getAppletContext());

			//stopMidiPlayback();
			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "NEXT LESSON" ) )
		{
			//System.out.println("Next Lesson button pressed");
			//stopMidiPlayback();
			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "EXIT" ) )
		{
			//System.out.println("Exit button pressed");
			//stopMidiPlayback();
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

	String getCorrectSimpleCompound (int tsignum,int tsigden )
	{
		String ret = "Simple";
		if ( ((tsignum % 3) == 0) && ((tsignum / 3) > 1) )
		{
			ret = "Compound";
		}
		return ret;
	}

	String getCorrectNumBeats (int tsignum, int tsigden )
	{
		int adjnum = tsignum;
		String ret = "";
		String simpcomp = getCorrectSimpleCompound(tsignum,tsigden);

		if ( simpcomp.equals("Compound"))
		{
			adjnum = tsignum / 3;
		}
	
		switch (adjnum)
		{
			case 2: { ret="Duple"; break; }
			case 3: { ret="Triple"; break; }
			case 4: { ret="Quadruple"; break; }
			case 5: { ret="Quintuple"; break; }
			case 6: { ret="Sextuple"; break; }
			case 7: { ret="Septuple"; break; }
		}
		return ret;
	}

	String getCorrectBeatNote (int tsignum, int tsigden )
	{
		String ret = "";
		int beatmult = 1, beatdur = 0;
		String simpcomp = getCorrectSimpleCompound(tsignum,tsigden);

		if ( simpcomp.equals("Compound") )
		{
			beatmult = 3;
		}

		switch (tsigden)
		{
			case 1: { beatdur=96 * beatmult ; break; }
			case 2: { beatdur=48 * beatmult ; break; }
			case 4: { beatdur=24 * beatmult ; break; }
			case 8: { beatdur=12 * beatmult ; break; }
			case 16: { beatdur=6 * beatmult ; break; }
		}

		switch (beatdur)
		{
			case 6:   {ret = "Sixteenth"; break; }
			case 12:  {ret = "Eighth" ; break; }
			case 24:  {ret = "Quarter" ; break; }
			case 48:  {ret = "Half" ; break; }
			case 96:  {ret = "Whole" ; break; }

			case 9:   {ret = "Dotted Sixteenth" ; break; }
			case 18:  {ret = "Dotted Eighth" ; break; }
			case 36:  {ret = "Dotted Quarter" ; break; }
			case 72:  {ret = "Dotted Half" ; break; }
			case 144: {ret = "Dotted Whole" ; break; }
		}

		return ret;
	}


//------------------------------------------------------------------------------
}
