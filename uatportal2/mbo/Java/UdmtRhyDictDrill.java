import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.sound.midi.*;
//------------------------------------------------------------------------------

public class UdmtRhyDictDrill extends Applet
        implements MouseListener,  ActionListener, MetaEventListener, Runnable
{
//------------------------------------------------------------------------------
// UdmtRhydictDrill - Rhythm dictation drill and exam
//------------------------------------------------------------------------------
// PARAMS:
//
// level	= The level of exercises to present:
// 			Values: 1,2,3 
//				REQUIRED
//
// examPlayMax = used to change the default number of times a student may play the sequence
// 	in exam mode.	OPTIONAL: DEFAULT=4
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
// 01/20/2005: ver 0.04 - changes to support exam login servlet
// 03/20/2005: fix bug where failurl looked up Pass examkey
//------------------------------------------------------------------------------

	private	UdmtRhyNotationApi notation;
	private	UdmtRhyDictExercises exercises;
	private UdmtURLBuilder urlBuilder;
	private UdmtExamLog examLog;

	private UdmtImageButton btnLeftArrow, btnRightArrow;
	private UdmtImageButton btnP1, btnP2, btnP3, btnP4, btnP5, btnP6, btnP7;
	private UdmtImageButton btnP8, btnP9;

	private Image imgP0, imgP1, imgP2, imgP3, imgP4, imgP5, imgP6, imgP7, imgP8;

	private String udmtAppletName			=	"UdmtRhydictDrill";
	private String udmtAppletVersion		=	"0.04";

	private Image imgAllSymbols;
	private Image imgTriplet;

	private Sequencer sequencer;
	private Sequence seq, progChgSequence;
	private int playStatus=-1;
	private boolean suppressingMIDI = false;

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
	String currWavFile;
	int currAnswerA;
	int currAnswerB;

	private int numCmdCharIx;
	private int[] cmdCharIx;
	private int[] noteSymIdIx;
		
	private int numMidiNotes;
	private int[] midiNotes;
	private int numMidiDurs;
	private long[] midiDurs;
	private int[] deltaMidiDurs ;
	private long prevMidiDur=0;


	private int numNotesToDisplay=0;

	private Button checkButton, nextButton, playcorrButton, takeExamButton, showcorrButton;
	private Button shownotesButton, nextLessonButton, exitButton, hintButton;

	//private Choice imntCombo;

	private int userImnt;

	private boolean examMode = false;

	//private PopupMenu popup;
	//private final int	MENU_HOR_OFFSET		=	18;
	//private final int	MENU_VER_OFFSET		=	179;

	private String strDrillMode			=	"Rhydict Drill";
	private String strExamMode			=	"Rhydict Exam";
	private String strInstruct 			= 	"Identify the rhythm pattern for the ";

	private int showAbendScreen 			= 	0;
	private String txtAbend 			= 	"";

	private int	numStudentBoxes;
	private int	studentBox = 0;
	private int	studentBoxMax = 1;
	private int[] 	studentAnswer;
//	private int[]	studentDurVal;
	private boolean[] studentCorrect;

	private int boxBoundL, boxBoundR, prevBoxBoundR;	// for calculating x coordinates of midpoints between 
	private int[] boxMidpt;				// rhydict input boxes.

	private int cursorBoxNum = 1;			// the box number the cursor is on

	private int numArrowPts = 9;
	private int xArrow0[] =    {0, 5,   5,  10,   0, -10,  -5, -5, 0};
	private int yArrow0[] =    {0, 0, -15, -15, -25, -15, -15,  0, 0};
	private int xPolygon[] =   {0, 0,   0,   0,   0,   0,   0,  0, 0};
	private int yPolygon[] =   {0, 0,   0,   0,   0,   0,   0,  0, 0};

	private String parmLevel			=	"";
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
	private String strIncorrect			= 	"Incorrect.";
	private String strIncorrectExam 		= 	"Incorrect.";
	private String strShowCorrect			= 	"The correct answer is being displayed.";

	private String strMaster1 			= 	"Congratulations.  You have passed the exam";
	private String strMaster2 			= 	"and may move on if you wish.";
	private String strFeedback 			= 	"";
	private String strExamFeedback1 		= 	"";
	private String strExamFeedback2 		= 	"";
	private String strDotTrip="";

	private String[] flatdurtypes={ "do","ra","re","me","mi","fa","se","so","le","la","te","ti"};
	private String[] sharpdurtypes={ "do","di","re","ri","mi","fa","fi","so","si","la","li","ti"};
	private String[] currdurtypes;

	int lBtn = 320;	// left coordinate of rhydict button set
	int wBtn = 65;	// width of each button
	int hBtn = 55;	// height of each button
	int sBtn = 95;	// space between left edges of each button
	int xBtn;		// used for calculating x coordinate of each button
	int yBtn = 250;	// y coord of button set

	boolean playSeqAfterDraw=false;

	// DEFAULT VALUES WHICH MAY BE OVERRIDEN BY PARAMS:
	private String parmExamNumQues;
	private int num_exam_ques			= 	10;
	private String parmExamNumReqd;
	private int num_reqd_to_pass 			= 	9;

	private int[] shuffledQues;
	private int shuffledQuesIndex = 0;
	private int shuffledBase;
	private int sex;
	private int shufdeck;
	private int majorMinor = +1;

	private String parmProbabDist;
	private int myKey;
	private char myKeySF;

	// amount to add to transposed sequence to get back to original - for calculating rhydict durtype
	// Major keys:               Cb Gb Db Ab Eb Bb   F C  G  D  A  E   B  F# C#
	private int[] keyShiftMaj = {+1,-6,-1,-8,-3,-10,-5,0,-7,-2,-9,-4,-11,-6,-1};

	// Minor keys:                ab eb  bb  f  c  g  d  a  e   b f# c# g# d#  a# (note: offsets adjusted by +4)
	private int[] keyShiftMin = { -8,-3,-10,-5, 0,-7,-2,-9,-4,-11,-6,-1,-8,-3,-10 };

	private int playCtr = 1;
	private int examPlayMax = 4;
	private String parmExamPlayMax;

	private int[] dotXpos = {97,122,147,172,197,222,247,272,297,322,347,372,397,422,447,472,497,522};

	private int dotToggle=-1, tripletToggle=-1; 	//-1=no dot/trip +1=yes

	private String[] level1BtnFile = {
		"dot8-16-tr.gif"
	,	"16-dot8-tr.gif"
	,	"2eighths-tr.gif"
	,	"8-16-16-tr.gif"
	,	"16-16-8-tr.gif"
	,	"16-8-16-tr.gif"
	,	"qtr-tr.gif"
	,	"4sixteenths-tr.gif"
	};

	private int[] level1BtnWidth = {
		77,77,77,77,77,77,35,102
	};
	private int[] level1BtnHeight = {
		57,57,57,57,57,57,57,57
	};

	private String[] level2BtnFile = {
		"2eighths-tr.gif"
	,	"dot8-16-tr.gif"
	,	"16-dot8-tr.gif"
	,	"16-8-16-tr.gif"
	,	"16-16-8-tr.gif"
	,	"8-16-16-tr.gif"
	,	"4sixteenths-tr.gif"
	};

	private int[] level2BtnWidth = {
		77,77,77,77,77,77,102
	};
	private int[] level2BtnHeight = {
		57,57,57,57,57,57,57
	};

	private String[] level3BtnFile  = {
		"8-16-16-8-tr.gif"
	,	"3eighths-tr.gif"
	,	"16-8-dot8-tr.gif"
	,	"16-16-8-8-tr.gif"
	,	"8th-qtr-tr.gif"
	,	"8-dot8-16-tr.gif"
	,	"8-8-16-16-tr.gif"
	,	"qtr-8th-tr.gif"
	,	"dot8-16-8-tr.gif"
	};

	private int[] level3BtnWidth = {
		97,69,80,102,63,80,102,63,80
	};
	private int[] level3BtnHeight = {
		57,56,56,57,57,56,57,57,56
	};

	private int[] w;
	private int[] h;

	private String[] audioQueue;
    	private int[] audioDelays;
    	private int audioQHead = 0;
   	private int audioQTail = 0;
    	private int audioWaitTime = 0;
    	final int audioQueueLength = 100;

        private Thread looper;
        private boolean running;
        private long runSeconds = 0;
        private int runCount;
        private String auQtr = "qtr.wav";
        private String auDotQtr = "dotqtr.wav";
        private String[] level1wav = { "qtr.wav","two8.wav","dot816.wav","16dot8.wav"
		,"four16.wav","16816.wav","81616.wav","16168.wav" };
        private String[] level2wav = {"two8.wav","dot816.wav","16dot8.wav"
		,"16816.wav","16168.wav","81616.wav","four16.wav" };
        private String[] level3wav = {"three8.wav","dot8168.wav","168dot8.wav","8dot816.wav"
		,"t816168.wav","161688.wav","881616.wav"};

//------------------------------------------------------------------------------

	public void init()
	{
		// Display version and debugging info in Java Console
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		rectScreen = getBounds();

		notation = new UdmtRhyNotationApi();
		//System.out.println ("UdmtRhyNotationApi invoked: Version="+notation.getVersion());
		notation.initAPI();

		exercises = new UdmtRhyDictExercises();
				//05/01/2004- New procedure for building URLs requires object to be instantiated once
		urlBuilder = new UdmtURLBuilder();
		// End of 05/01/2004 change.		

		//05/01/2004- New procedure for Exam Log requires object to be instantiated once
		examLog = new UdmtExamLog(); 
		// End of 05/01/2004 change.		

		parmLevel = getParameter ("level");
		if (parmLevel == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: rhydictType is required";
			System.out.println ("The value for the parameter rhydictType must be one of the following:");
			System.out.println ("MajorScalar,MajorArp,NaturalMinorScalar,NaturalMinorArp,");
			System.out.println ("HarmonicMinorScalar,HarmonicMinorArp,MelodicMinorScalar,MelodicMinorArp");
		}
		else if (parmLevel.equals("1"))	{
			strDrillMode="Rhythm Dictation Practice: Level 1";
			strExamMode="Rhythm Dictation Exam: Level 1";
			exercises.setExerciseType ('1');	
			studentBoxMax = 1;
			strInstruct	+= "2nd beat.";
		}
		else if (parmLevel.equals("2"))	{
			strDrillMode="Rhythm Dictation Practice: Level 2";
			strExamMode="Rhythm Dictation Exam: Level 2";
			exercises.setExerciseType ('2');
			studentBoxMax = 2;
			strInstruct	+= "2nd and 4th beats.";
		}
		else if (parmLevel.equals("3"))	{
			strDrillMode="Rhythm Dictation Practice: Level 3";
			strExamMode="Rhythm Dictation Exam: Level 3";
			exercises.setExerciseType ('3');
			studentBoxMax = 1;
			strInstruct	+= "2nd beat.";
		}
//		// for testing only:
//		else if (parmLevel.equals("T"))	{
//			strDrillMode="Rhythm Dictation Practice: Test Mode";
//			strExamMode="Rhythm Dictation Exam: Test Mode";
//			exercises.setExerciseType ('T');
//		}
		else	{
			showAbendScreen = 1;
			txtAbend = "Param: rhydictType is invalid.";
			System.out.println ("The value for the parameter rhydictType must be one of the following:");
			System.out.println ("MajorScalar,MajorArp,NaturalMinorScalar,NaturalMinorArp,");
			System.out.println ("HarmonicMinorScalar,HarmonicMinorArp,MelodicMinorScalar,MelodicMinorArp");
		}


		currNumExercises = exercises.getNumExercises ();


		currdurtypes = new String[12];
		for (int i=0; i<12; i++)
		{
			 currdurtypes[i]=flatdurtypes[i];
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

		if (examMode)
		{
			parmExamPlayMax = getParameter("examPlayMax");
			if ( parmExamPlayMax != null)	{
				examPlayMax = Integer.parseInt(parmExamPlayMax);
				if (examPlayMax < 1)
				{
					examPlayMax = 1;
				}
			}
		}

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

		shuffledBase = currNumExercises - num_exam_ques ;
		shufdeck = num_exam_ques;
		if (shuffledBase <= 0)
		{
			shuffledBase = 0;
			shufdeck = currNumExercises;
		}
		//System.out.println ("shuffledBase="+shuffledBase);
		shuffledQues = exercises.shuffleN (shufdeck);
		//for (int i=0 ; i < shufdeck ; i++)
		//{
		//	System.out.println("Shuffled i="+i+" ques="+shuffledQues[i]);
		//}
		
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
  				String sExamKey = objEk.getExamKey("RhydictLevel"+parmLevel+"Pass");
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
				//old code: parmDrillURL = this.getCodeBase() + parmDrillURL;

	 			//01-20-2005: Use URLBUILDER only if not in exam mode
 				if (examMode)
 				{
 					UdmtExamKey objEk = new UdmtExamKey();
  					String sExamKey = objEk.getExamKey("RhydictLevel"+parmLevel+"Fail");
	  				parmDrillURL = parmDrillURL + "?r="+sExamKey;
  					//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY
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

		// Triplet logic added 5/7/2004

		MediaTracker mt = new MediaTracker(this);
		URL url = getCodeBase();
		imgAllSymbols = getImage (url, "symbols-28pt-tr.gif");
		mt.addImage (imgAllSymbols, 1);

//		imgTriplet = getImage (url, "triplet-tr.gif");
//		mt.addImage (imgTriplet, 1);

		w = new int[9];
		h = new int[9];


		if (parmLevel.equals("1"))	
		{
			imgP0 = getImage (url, level1BtnFile[0]);
			imgP1 = getImage (url, level1BtnFile[1]);
			imgP2 = getImage (url, level1BtnFile[2]);
			imgP3 = getImage (url, level1BtnFile[3]);
			imgP4 = getImage (url, level1BtnFile[4]);
			imgP5 = getImage (url, level1BtnFile[5]);
			imgP6 = getImage (url, level1BtnFile[6]);
			imgP7 = getImage (url, level1BtnFile[7]);
			mt.addImage (imgP0, 1);
			mt.addImage (imgP1, 1);
			mt.addImage (imgP2, 1);
			mt.addImage (imgP3, 1);
			mt.addImage (imgP4, 1);
			mt.addImage (imgP5, 1);
			mt.addImage (imgP6, 1);
 			mt.addImage (imgP7, 1);
			for (int i = 0; i <= 7 ; i++)
			{
				w[i] = level1BtnWidth[i];
				h[i] = level1BtnHeight[i];
				//System.out.println("i="+i+" w="+w[i]+" h="+h[i]);
			}
		}
		if (parmLevel.equals("2"))	
		{
			imgP0 = getImage (url, level2BtnFile[0]);
			imgP1 = getImage (url, level2BtnFile[1]);
			imgP2 = getImage (url, level2BtnFile[2]);
			imgP3 = getImage (url, level2BtnFile[3]);
			imgP4 = getImage (url, level2BtnFile[4]);
			imgP5 = getImage (url, level2BtnFile[5]);
			imgP6 = getImage (url, level2BtnFile[6]);
			mt.addImage (imgP0, 1);
			mt.addImage (imgP1, 1);
			mt.addImage (imgP2, 1);
			mt.addImage (imgP3, 1);
			mt.addImage (imgP4, 1);
			mt.addImage (imgP5, 1);
			mt.addImage (imgP6, 1);
			for (int i = 0; i <= 6 ; i++)
			{
				w[i] = level2BtnWidth[i];
				h[i] = level2BtnHeight[i];
				//System.out.println("i="+i+" w="+w[i]+" h="+h[i]);
			}
		}
		if (parmLevel.equals("3"))	
		{
			imgP0 = getImage (url, level3BtnFile[0]);
			imgP1 = getImage (url, level3BtnFile[1]);
			imgP2 = getImage (url, level3BtnFile[2]);
			imgP3 = getImage (url, level3BtnFile[3]);
			imgP4 = getImage (url, level3BtnFile[4]);
			imgP5 = getImage (url, level3BtnFile[5]);
			imgP6 = getImage (url, level3BtnFile[6]);
			imgP7 = getImage (url, level3BtnFile[7]);
			imgP8 = getImage (url, level3BtnFile[8]);
			mt.addImage (imgP0, 1);
			mt.addImage (imgP1, 1);
			mt.addImage (imgP2, 1);
			mt.addImage (imgP3, 1);
			mt.addImage (imgP4, 1);
			mt.addImage (imgP5, 1);
			mt.addImage (imgP6, 1);
 			mt.addImage (imgP7, 1);
 			mt.addImage (imgP8, 1);
			for (int i = 0; i <= 8 ; i++)
			{
				w[i] = level3BtnWidth[i];
				h[i] = level3BtnHeight[i];
				//System.out.println("i="+i+" w="+w[i]+" h="+h[i]);
			}
		}

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

		try	{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();     
			sequencer.addMetaEventListener(this);            
			System.out.println("MIDI initialized successfully.");
		}
		catch ( Exception e )	{
			System.out.println("ERROR: Exception when opening Java MIDI sequencer:");
			e.printStackTrace();
			showAbendScreen = 1;
			txtAbend = "Exception when opening Java MIDI sequencer.";
		}

		if (examMode)
		{
			sex = shuffledBase + shuffledQues [ shuffledQuesIndex ] ;
			//System.out.println ("getting exercise# : "+sex);
			currAnswerA = exercises.getAnswerNumA( sex );
			currAnswerB = exercises.getAnswerNumB( sex );			
			currCmdStr = exercises.getExerciseNum ( sex );
			currWavFile = exercises.getWavFileNum ( sex );

			shuffledQuesIndex++;
			if (shuffledQuesIndex >= shufdeck)
			{
				shuffledQuesIndex = 0;
			}
		}
		else
		{
			currAnswerA = exercises.getAnswerA();
			currAnswerB = exercises.getAnswerB();
			currCmdStr = exercises.getExercise();
			currWavFile = exercises.getWavFile ( );

			//System.out.println ("currCmdStr="+currCmdStr);
			//System.out.println ("Answer A = "+currAnswerA+" Answer B = "+currAnswerB);
		}

		//System.out.println ("Original cmdstr="+currCmdStr);


		notation.setCmdStr (currCmdStr);

		seq = notation.getSequence();
		
		//System.out.println ("Getting symbol arrays:");
		numSymIDsOnStaff = notation.getNumSymIDsOnStaff();

 		symIDsOnStaff = new int[100];
		staffXpos = new int[100];
		staffYpos = new int[100];

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

		numMidiDurs = numMidiNotes ;
		midiDurs = new long[50];
		midiDurs = notation.getMidiTimes();

		deltaMidiDurs = new int[50];
		prevMidiDur=0;

  		for (int i=0 ; i < numMidiDurs ; i++)
		{
			deltaMidiDurs[i] = (int)(midiDurs[i] - prevMidiDur) * 2;
			prevMidiDur = midiDurs[i] ;
//			System.out.println ("i="+i+" midiDurs[i]="+midiDurs[i]+" deltaMidiDurs[i]="+deltaMidiDurs[i]);
		}


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

		studentAnswer = new int[2];
		//studentAnswer = new String[25];
		//studentDurVal = new int[25];
		studentCorrect = new boolean[2];
	
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
		
		if (examMode)
		{
			playcorrButton = new Button("PLAY");
			playcorrButton.addActionListener(this);
		   	playcorrButton.setEnabled(true);
			playcorrButton.setBounds ( 60,300,110,30 );
			add(playcorrButton);
		}
		else
		{
			playcorrButton = new Button("PLAY");
			playcorrButton.addActionListener(this);
		   	playcorrButton.setEnabled(true);
			playcorrButton.setBounds ( 60,350,110,30 );
			add(playcorrButton);
		}

		if ( !examMode )	{


			hintButton = new Button("HINT");
			hintButton.setEnabled(false);        
			hintButton.addActionListener(this);
			hintButton.setBounds ( 60,300,110,30 );
			add(hintButton);


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


		// add buttons for level:



		if (parmLevel.equals("1"))	
		{
			xBtn = lBtn + 10;
			btnP1 = new UdmtImageButton (level1BtnFile[0], this);
			btnP1.setActionCommand ("P1");
			btnP1.addActionListener(this);
			btnP1.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP1);

			xBtn += sBtn - 8;
			btnP2 = new UdmtImageButton (level1BtnFile[1], this);
			btnP2.setActionCommand ("P2");
			btnP2.addActionListener(this);
			btnP2.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP2);

			xBtn += sBtn - 8;
			btnP3 = new UdmtImageButton (level1BtnFile[2], this);
			btnP3.setActionCommand ("P3");
			btnP3.addActionListener(this);
			btnP3.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP3);

			xBtn = lBtn + 10;
			btnP4 = new UdmtImageButton (level1BtnFile[3], this);
			btnP4.setActionCommand ("P4");
			btnP4.addActionListener(this);
			btnP4.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP4);
	
			xBtn += sBtn - 8;
			btnP5 = new UdmtImageButton (level1BtnFile[4], this);
			btnP5.setActionCommand ("P5");
			btnP5.addActionListener(this);
			btnP5.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP5);
	
			xBtn += sBtn - 8;
			btnP6 = new UdmtImageButton (level1BtnFile[5], this);
			btnP6.setActionCommand ("P6");
			btnP6.addActionListener(this);
			btnP6.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP6);
	
			xBtn = lBtn + 52;
			btnP7 = new UdmtImageButton (level1BtnFile[6], this);
			btnP7.setActionCommand ("P7");
			btnP7.addActionListener(this);
			btnP7.setBounds ( xBtn, staffY + 105 - 4  ,wBtn,hBtn );
			add (btnP7);

			xBtn += sBtn - 50;
			btnP8 = new UdmtImageButton (level1BtnFile[7], this);
			btnP8.setActionCommand ("P8");
			btnP8.addActionListener(this);
			btnP8.setBounds ( xBtn, staffY + 105 - 4 ,wBtn,hBtn );
			add (btnP8);
		}


		if (parmLevel.equals("2"))	
		{
			xBtn = lBtn + 10;
			btnP1 = new UdmtImageButton (level2BtnFile[0], this);
			btnP1.setActionCommand ("P1");
			btnP1.addActionListener(this);
			btnP1.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP1);

			xBtn += sBtn - 10;
			btnP2 = new UdmtImageButton (level2BtnFile[1], this);
			btnP2.setActionCommand ("P2");
			btnP2.addActionListener(this);
			btnP2.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP2);

			xBtn += sBtn - 10;
			btnP3 = new UdmtImageButton (level2BtnFile[2], this);
			btnP3.setActionCommand ("P3");
			btnP3.addActionListener(this);
			btnP3.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP3);

			xBtn = lBtn + 10;
			btnP4 = new UdmtImageButton (level2BtnFile[3], this);
			btnP4.setActionCommand ("P4");
			btnP4.addActionListener(this);
			btnP4.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP4);
	
			xBtn += sBtn - 10;
			btnP5 = new UdmtImageButton (level2BtnFile[4], this);
			btnP5.setActionCommand ("P5");
			btnP5.addActionListener(this);
			btnP5.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP5);
	
			xBtn += sBtn - 10;
			btnP6 = new UdmtImageButton (level2BtnFile[5], this);
			btnP6.setActionCommand ("P6");
			btnP6.addActionListener(this);
			btnP6.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP6);
	
			xBtn = lBtn + 85;
			btnP7 = new UdmtImageButton (level2BtnFile[6], this);
			btnP7.setActionCommand ("P7");
			btnP7.addActionListener(this);
			btnP7.setBounds ( xBtn, staffY + 105 - 4 ,wBtn,hBtn );
			add (btnP7);
		}


		if (parmLevel.equals("3"))	
		{
			btnP1 = new UdmtImageButton (level3BtnFile[0], this);
			btnP1.setActionCommand ("P1");
			btnP1.addActionListener(this);
			btnP1.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP1);

			xBtn += sBtn + 10;
			btnP2 = new UdmtImageButton (level3BtnFile[1], this);
			btnP2.setActionCommand ("P2");
			btnP2.addActionListener(this);
			btnP2.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP2);

			xBtn += sBtn - 17;
			btnP3 = new UdmtImageButton (level3BtnFile[2], this);
			btnP3.setActionCommand ("P3");
			btnP3.addActionListener(this);
			btnP3.setBounds ( xBtn, staffY - 25 - 4 ,wBtn,hBtn );
			add (btnP3);

			xBtn = lBtn;
			btnP4 = new UdmtImageButton (level3BtnFile[3], this);
			btnP4.setActionCommand ("P4");
			btnP4.addActionListener(this);
			btnP4.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP4);
	
			xBtn += sBtn + 16;
			btnP5 = new UdmtImageButton (level3BtnFile[4], this);
			btnP5.setActionCommand ("P5");
			btnP5.addActionListener(this);
			btnP5.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP5);
	
			xBtn += sBtn - 23;
			btnP6 = new UdmtImageButton (level3BtnFile[5], this);
			btnP6.setActionCommand ("P6");
			btnP6.addActionListener(this);
			btnP6.setBounds ( xBtn, staffY + 40 - 4 ,wBtn,hBtn );
			add (btnP6);
	
			xBtn = lBtn;
			btnP7 = new UdmtImageButton (level3BtnFile[6], this);
			btnP7.setActionCommand ("P7");
			btnP7.addActionListener(this);
			btnP7.setBounds ( xBtn, staffY + 105 - 4 ,wBtn,hBtn );
			add (btnP7);

			xBtn += sBtn + 16;
			btnP8 = new UdmtImageButton (level3BtnFile[7], this);
			btnP8.setActionCommand ("P8");
			btnP8.addActionListener(this);
			btnP8.setBounds ( xBtn, staffY + 105 - 4 ,wBtn,hBtn );
			add (btnP8);

			xBtn += sBtn - 23;
			btnP9 = new UdmtImageButton (level3BtnFile[8], this);
			btnP9.setActionCommand ("P9");
			btnP9.addActionListener(this);
			btnP9.setBounds ( xBtn, staffY + 105 - 4 ,wBtn,hBtn );
			add (btnP9);
		}


//Imntcombo removed for Rhythm Dictation - always use vibraphone
//		//System.out.println ("Adding instrument combobox");
//
//		imntCombo = new Choice();
//		for (int i=0 ; i <= 127 ; i++)	//{

//			imntCombo.add ( GMImntList[i] );
//		} 
//		if (examMode) {	
//			imntCombo.setBounds ( 60,350,110,30 );
//		}
//		else	{
//			imntCombo.setBounds ( 60,305,110,30);
//		}
//		add(imntCombo);
//		imntCombo.addItemListener(this);	
//		// For rhythm, choose default patch that does not decay fast
//		imntCombo.select(11); // vibraphone
		
		userImnt = 11;

		// tempo slider bar










		notation.reset();
		notation.setSequencePatch (userImnt);
		notation.setCmdStr (currCmdStr);
		seq = notation.getSequence();


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

		audioQueue = new String[audioQueueLength];
		audioDelays = new int[audioQueueLength];
	}

//------------------------------------------------------------------------------

	private void resetStudentAnswer()
	{
		for (int i=0 ; i < studentBoxMax ; i++)
		{
			studentAnswer[i] = -1;
			studentCorrect[i] = false;
			studentBox = 0;
		}

//		for (int i=0 ; i<25 ; i++)
//		{
//			studentAnswer[i] = "  ";
//			studentDurVal[i] = 0;
//			studentCorrect[i] = false;
//		}
//
//		numStudentBoxes = numCmdCharIx ;

		numNotesToDisplay = 0;

		repaint();
	}

	private void resetNotation()
	{
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

//		System.out.println ("In resetNotation()");
//		System.out.println ("midi notes");
//		for (int i=0 ; i < numMidiNotes ; i++)
//		{
//			System.out.println ("midiNotes["+i+"]="+midiNotes[i]);
//		}

		// 05/17/04: capture midi durations into arrays
//		System.out.println ("midi durations");

		numMidiDurs = numMidiNotes ;
		midiDurs = notation.getMidiTimes();
		prevMidiDur=0;
		for (int i=0 ; i < numMidiDurs ; i++)
		{
			deltaMidiDurs[i] = (int)(midiDurs[i] - prevMidiDur);
			prevMidiDur = midiDurs[i] ;
//			System.out.println ("i="+i+" midiDurs[i]="+midiDurs[i]+" deltaMidiDurs[i]="+deltaMidiDurs[i]);
		}



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

//		muTriplet			= new UdmtMusicSymbol();
//		muTriplet.CropImage 		( imgTriplet, 0, 0, 30, 40 );
//		imgs[51] = muTriplet.getImage();

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

			if (playSeqAfterDraw)
			{
				playSeqAfterDraw=false;
				startMidiPlayback();
			}
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
		int imgX, imgY;

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

		if (parmLevel.equals("T"))
		{
		}
		else
		{
			for (int x=staffX ; x < staffX+260 ; x+=37 )
			{
				g.drawImage (muStaff.getImage(), x, staffY, this );
			}
		}

		g.drawImage (muBar.getImage(), staffX - 2 , staffY, this);

		g.drawImage (muRtDblBar.getImage(), staffX+287, staffY, this);


		// Draw the music symbols: This needs to be modified if individual notes are to be displayed
		// not necessarily in order.

		//System.out.println("numnotestodisplay="+numNotesToDisplay);
		if (numNotesToDisplay > 0)
		{
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
			cursorBoxNum = 1;
		}
		else // just display all symbols before first note
		{
			for (int n=0 ; n < (noteSymIdIx[0]) ; n++)
			{
				g.drawImage 	( imgs[symIDsOnStaff[n]]
					, staffX + staffXpos[n]
					, staffY + staffYpos[n]
					, this
					);

			}
		}

///////////////////////////////////////////////////////////////////////////old
//		// Draw dots above note positions and boxes below note positions
//		for (int n=0 ; n < 18 ; n++)
//		{
// 		   if (numNotesToDisplay == 0)
//		   {
//			//g.drawImage 	( imgs[31]
//			//		, staffX + dotXpos[n] - 21
//			//		, staffY - 30
//			//		, this
//			//		);
//
//
//			// draw student answer
//			if ( (studentAnswer[n].equals("f2")) 
//			|| (studentAnswer[n].equals("f.2"))
//			|| (studentAnswer[n].equals("f~2"))
//			)
//			{
//				g.drawImage 	(muHalfU.getImage()					
//					, staffX + dotXpos[n] - 25
//					, staffY - 5
//					, this);
//				if (studentAnswer[n].equals("f.2"))
//				{
//					g.drawImage 	(muDot.getImage()					
//					, staffX + dotXpos[n] - 12
//					, staffY + 25
//					, this);
//				}
//				if (studentAnswer[n].equals("f~2"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 27
//					, staffY - 30
//					, this);
//				}
//			} // half note
//
//			// draw student answer
//			if ( (studentAnswer[n].equals("f4")) 
//			|| (studentAnswer[n].equals("f.4"))
//			|| (studentAnswer[n].equals("f~4"))
//			)
//			{
//				g.drawImage 	(muQuarterU.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 5
//					, this);
//				if (studentAnswer[n].equals("f.4"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 12
//					, staffY + 25
//					, this);
//				}
//				if (studentAnswer[n].equals("f~4"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 27
//					, staffY - 30
//					, this);
//				}
//			} //qtr note
//
//			if ( (studentAnswer[n].equals("f8")) 
//			|| (studentAnswer[n].equals("f.8"))
//			|| (studentAnswer[n].equals("f~8"))
//			)
//			{
//				g.drawImage 	(muEighthU.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 5
//					, this);
//				if (studentAnswer[n].equals("f.8"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 12
//					, staffY + 25
//					, this);
//				}
//				if (studentAnswer[n].equals("f~8"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 27
//					, staffY - 30
//					, this);
//				}
//			} //8th note
//
//			if ( (studentAnswer[n].equals("f6")) 
//			|| (studentAnswer[n].equals("f.6"))
//			|| (studentAnswer[n].equals("f~6"))
//			)
//			{
//				g.drawImage 	(muSixteenthU.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 5 
//					, this);
//				if (studentAnswer[n].equals("f.6"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 12
//					, staffY + 25
//					, this);
//				}
//				if (studentAnswer[n].equals("f~6"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 27
//					, staffY - 30
//					, this);
//				}
//			} //16th note
//
//			if ( (studentAnswer[n].equals("r2")) 
//			|| (studentAnswer[n].equals("r.2"))
//			|| (studentAnswer[n].equals("r~2"))
//			)
//			{
//				g.drawImage 	(muHalfR.getImage()					
//					, staffX + dotXpos[n] - 25
//					, staffY - 6
//					, this);
//				if (studentAnswer[n].equals("r.2"))
//				{
//					g.drawImage 	(muDot.getImage()					
//					, staffX + dotXpos[n] - 12
//					, staffY + 8
//					, this);
//				}
//				if (studentAnswer[n].equals("r~2"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 29
//					, staffY - 30
//					, this);
//				}
//			} // half rest
//
//			// draw student answer
//			if ( (studentAnswer[n].equals("r4")) 
//			|| (studentAnswer[n].equals("r.4"))
//			|| (studentAnswer[n].equals("r~4"))
//			)
//			{
//				g.drawImage 	(muQuarterR.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 6
//					, this);
//				if (studentAnswer[n].equals("r.4"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 12
//					, staffY + 8
//					, this);
//				}
//				if (studentAnswer[n].equals("r~4"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 29
//					, staffY - 30
//					, this);
//				}
//			} //qtr note
//
//			if ( (studentAnswer[n].equals("r8")) 
//			|| (studentAnswer[n].equals("r.8"))
//			|| (studentAnswer[n].equals("r~8"))
//			)
//			{
//				g.drawImage 	(muEighthR.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 6
//					, this);
//				if (studentAnswer[n].equals("r.8"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 12
//					, staffY + 8
//					, this);
//				}
//				if (studentAnswer[n].equals("r~8"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 29
//					, staffY - 30
//					, this);
//				}
//			} //8th note
//
//			if ( (studentAnswer[n].equals("r6")) 
//			|| (studentAnswer[n].equals("r.6"))
//			|| (studentAnswer[n].equals("r~6"))
//			)
//			{
//				g.drawImage 	(muSixteenthR.getImage()
//					, staffX + dotXpos[n] - 25
//					, staffY - 6 
//					, this);
//				if (studentAnswer[n].equals("r.6"))
//				{
//					g.drawImage 	(muDot.getImage()
//					, staffX + dotXpos[n] - 10
//					, staffY + 8
//					, this);
//				}
//				if (studentAnswer[n].equals("r~6"))
//				{
//					g.drawImage 	(muTriplet.getImage()
//					, staffX + dotXpos[n] - 29
//					, staffY - 30
//					, this);
//				}
//			} //16th note
//
//			
//		   } // if (numNotesToDisplay == 0)
//
//		   if (cursorBoxNum == (n+1))
//		   {
//				g.setColor (Color.blue);
//				for (int p = 0 ; p < numArrowPts ; p++ )
//				{
//					xPolygon[p] =  xArrow0[p] + staffX + dotXpos[n] - 18;
//					yPolygon[p] =  yArrow0[p] + staffY + 68 - 1;
//				}
//				g.fillPolygon ( xPolygon, yPolygon, numArrowPts );
//		   }
//		   g.setColor (Color.black);
//
//		} // for
///////////////////////////////////////////////////////////////////////////old

		g.setColor (Color.black);

//		System.out.println ("Paint routine:");
//		System.out.println ("studentBoxMax="+studentBoxMax);
//		System.out.println ("studentAnswer[0]="+studentAnswer[0]);
//		System.out.println ("studentAnswer[1]="+studentAnswer[1]);


		if (studentBoxMax == 2)
		{
			g.drawRect ( 40,190,120,80);
			g.drawRect ( 39,189,122,82);
			g.drawRect ( 170,190,120,80);
			g.drawRect ( 169,189,122,82);

			if (studentBox == 0)
			{
				centerStringAroundX ( g, ">> 2nd beat: <<", txtfont, 100, 183 );
				centerStringAroundX ( g, "4th beat:", txtfont, 230, 183 );
			}
			else
			{
				centerStringAroundX ( g, "2nd beat:", txtfont, 100, 183 );
				centerStringAroundX ( g, ">> 4th beat: <<", txtfont, 230, 183 );
			}

			if (studentAnswer[0] > -1)
			{
				imgX = 100;
				imgY = 230;
				switch (studentAnswer[0])
				{
					case 0:
					{
						g.drawImage (imgP0
						, imgX - (w[0] / 2)
						, imgY - (h[0] / 2)
						, this);
						break;
					}
					case 1:
					{
						g.drawImage (imgP1
						, imgX - (w[1] / 2)
						, imgY - (h[1] / 2)
						, this);
						break;
					}
					case 2:
					{
						g.drawImage (imgP2
						, imgX - (w[2] / 2)
						, imgY - (h[2] / 2) 
						, this);
						break;
					}
					case 3:
					{
						g.drawImage (imgP3
						, imgX - (w[3] / 2)
						, imgY - (h[3] / 2)
						, this);
						break;
					}
 					case 4:
					{
						g.drawImage (imgP4
						, imgX - (w[4] / 2)
						, imgY - (h[4] / 2) 
						, this);
						break;
					}
					case 5:
					{
						g.drawImage (imgP5
						, imgX - (w[5] / 2)
						, imgY - (h[5] / 2) 
						, this);
						break;
					}
					case 6:
					{
						g.drawImage (imgP6
						, imgX - (w[6] / 2)
						, imgY - (h[6] / 2) 
						, this);
						break;
					}
					case 7:
					{
						g.drawImage (imgP7
						, imgX - (w[7] / 2)
						, imgY - (h[7] / 2) 
						, this);
						break;
					}
					case 8:
					{
						g.drawImage (imgP8
						, imgX - (w[8] / 2)
						, imgY - (h[8] / 2) 
						, this);
						break;
					}
				} // switch
			} // studentanswer[0]

			if (studentAnswer[1] > -1)
			{
				imgX = 230;
				imgY = 230;
				switch (studentAnswer[1])
				{
					case 0:
					{
						g.drawImage (imgP0
						, imgX - (w[0] / 2)
						, imgY - (h[0] / 2) 
						, this);
						break;
					}
					case 1:
					{
						g.drawImage (imgP1
						, imgX - (w[1] / 2)
						, imgY - (h[1] / 2)
						, this);
						break;
					}
					case 2:
					{
						g.drawImage (imgP2
						, imgX - (w[2] / 2)
						, imgY - (h[2] / 2) 
						, this);
						break;
					}
					case 3:
					{
						g.drawImage (imgP3
						, imgX - (w[3] / 2)
						, imgY - (h[3] / 2) 
						, this);
						break;
					}
 					case 4:
					{
						g.drawImage (imgP4
						, imgX - (w[4] / 2)
						, imgY - (h[4] / 2) 
						, this);
						break;
					}
					case 5:
					{
						g.drawImage (imgP5
						, imgX - (w[5] / 2)
						, imgY - (h[5] / 2) 
						, this);
						break;
					}
					case 6:
					{
						g.drawImage (imgP6
						, imgX - (w[6] / 2)
						, imgY - (h[6] / 2) 
						, this);
						break;
					}
					case 7:
					{
						g.drawImage (imgP7
						, imgX - (w[7] / 2)
						, imgY - (h[7] / 2) 
						, this);
						break;
					}
					case 8:
					{
						g.drawImage (imgP8
						, imgX - (w[8] / 2)
						, imgY - (h[8] / 2) 
						, this);
						break;
					}
				} // switch
			} // studentanswer[1]
		}
		else // studentBoxMax = 1
		{
			g.drawRect ( 110,190,120,80);
			g.drawRect ( 109,189,122,82);
			centerStringAroundX ( g, "2nd beat:", txtfont, 170, 183 );

			imgX = 170;
			imgY = 230;
			switch (studentAnswer[0])
			{
				case 0:
				{
					g.drawImage (imgP0
					, imgX - (w[0] / 2)
					, imgY - (h[0] / 2)
					, this);
					break;
				}
				case 1:
				{
					g.drawImage (imgP1
					, imgX - (w[1] / 2)
					, imgY - (h[1] / 2) 
					, this);
					break;
				}
				case 2:
				{
					g.drawImage (imgP2
					, imgX - (w[2] / 2)
					, imgY - (h[2] / 2) 
					, this);
					break;
				}
				case 3:
				{
					g.drawImage (imgP3
					, imgX - (w[3] / 2)
					, imgY - (h[3] / 2) 
					, this);
					break;
				}
				case 4:
				{
					g.drawImage (imgP4
					, imgX - (w[4] / 2)
					, imgY - (h[4] / 2) 
					, this);
					break;
				}
				case 5:
				{
					g.drawImage (imgP5
					, imgX - (w[5] / 2)
					, imgY - (h[5] / 2) 
					, this);
					break;
				}
				case 6:
				{
					g.drawImage (imgP6
					, imgX - (w[6] / 2)
					, imgY - (h[6] / 2) 
					, this);
					break;
				}
				case 7:
				{
					g.drawImage (imgP7
					, imgX - (w[7] / 2)
					, imgY - (h[7] / 2) 
					, this);
					break;
				}
				case 8:
				{
					g.drawImage (imgP8
					, imgX - (w[8] / 2)
					, imgY - (h[8] / 2) 
					, this);
					break;
				}
			} // switch
		} // studentBoxMax = 1


		//System.out.println("numBeamsOnStaff="+numBeamsOnStaff);

		if (numNotesToDisplay > 0)
		{
			// Draw the beams
			for (int b=0 ; b < numBeamsOnStaff ; b++)
			{
				g.drawLine 	( beamX1[b]+1, staffY+beamY1[b] - 2, beamX2[b]+1, staffY+beamY2[b] - 2);
				g.drawLine 	( beamX1[b]+1, staffY+beamY1[b] - 1, beamX2[b]+1, staffY+beamY2[b] - 1);
				g.drawLine 	( beamX1[b]+1, staffY+beamY1[b], beamX2[b]+1,  staffY+beamY2[b]);
				g.drawLine 	( beamX1[b]+1, staffY+beamY1[b] + 1, beamX2[b]+1, staffY+beamY2[b] + 1);
				g.drawLine 	( beamX1[b]+1, staffY+beamY1[b] + 2, beamX2[b]+1, staffY+beamY2[b] + 2);
			}

			//System.out.println("numStemExtsOnStaff="+numStemExtsOnStaff);

			for (int se=0 ; se < numStemExtsOnStaff ; se++)
			{
				g.drawLine 	( stemExtX1[se]+1, staffY+stemExtY1[se] , stemExtX2[se]+1, staffY+stemExtY2[se]);
			}
		}

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
		centerString (g, strFeedback, txtfont, 295);


//		if (dotToggle==1) {
//			strDotTrip="Dotted";
//		}
//		else if (tripletToggle==1) {
//			strDotTrip="Triplet";
//		}
//		else  {
//			strDotTrip="";
//		}
//
//		g.drawString (strDotTrip, 440, 270 ) ;

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
	public void meta(MetaMessage msg)
	{
		if (msg.getType() == 47)
		{
			//System.out.println ("End of track event found");								stopMidiPlayback();			
			repaint();
		}
	}
//------------------------------------------------------------------------------
	private void startMidiPlayback()
	{
		//System.out.println ("Starting MIDI Playback");
		if (!suppressingMIDI)
		{
			try {
				sequencer.stop();			
				sequencer.setSequence(seq);
				sequencer.setTempoInBPM( (float)180.0 );
				sequencer.setTempoFactor( (float)1.5 );

			}
			catch ( Exception e )	{
				this.showStatus("ERROR SETTING SEQUENCE");
				System.out.println("ERROR: in startMidiPlayback - unable to set sequence for sequencer");
				e.printStackTrace();
			}
			sequencer.start();			
			playStatus = +1;
		}
	}

//------------------------------------------------------------------------------

	private void stopMidiPlayback()
	{
		//System.out.println ("Stopping MIDI Playback");
		if (!suppressingMIDI)
		{
			sequencer.stop();			
			playStatus = -1;
		}
	}

//------------------------------------------------------------------------------
//	public void itemStateChanged (ItemEvent ie)
//	{
//		userImnt 	= imntCombo.getSelectedIndex();
//
//		notation.reset();
//		notation.setSequencePatch (userImnt);
//		notation.setCmdStr (currCmdStr);
//		seq = notation.getSequence();
//
//		/* OLD CODE:
//		Track[] progChgTracks;
//	    	byte blankbytes[] = { 0 };
//
//		try
//		{
//	    		progChgSequence = new Sequence(Sequence.PPQ, 24, 1);
//	    		progChgTracks = progChgSequence.getTracks();
//	    		ShortMessage mymsg = new ShortMessage();
//	    		mymsg.setMessage(0xC0, userImnt, 0);    	    
//			MidiEvent myevent = new MidiEvent(mymsg, 0);		
//		      progChgTracks[0].add(myevent);
//	            MetaMessage metamsg = new MetaMessage();
//		      metamsg.setMessage(47, blankbytes, 0);
//	    		MidiEvent metaevent = new MidiEvent(metamsg, 1);
//	    		progChgTracks[0].add(metaevent);
//			sequencer.setSequence(progChgSequence);
//			sequencer.start();
//			playStatus = +1;
//		}
//		catch (InvalidMidiDataException e)
//		{   
//			System.out.println("Invalid MIDI Data Exception"); 
//	    		e.printStackTrace();
//		}
//		*/
//	}
//------------------------------------------------------------------------------
//
//	public void setupPopupMenu()
//	{
//		MenuItem mi ;
//		popup = new PopupMenu ( "Select Rhydict durtype" );
//
//		popup.add ( mi = new MenuItem ( "ti" ) )  ;
//		mi.addActionListener (this) ;	
//		popup.add ( mi = new MenuItem ( "te" ) )  ;
//		mi.addActionListener (this) ;	
//		popup.add ( mi = new MenuItem ( "li" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "la" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "le" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "si" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "so" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "se" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "fi" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "fa" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "mi" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "me" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "ri" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "re" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "ra" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "di" ) );
//		mi.addActionListener (this) ;
//		popup.add ( mi = new MenuItem ( "do" ) );
//		mi.addActionListener (this) ;
//	}
//
//------------------------------------------------------------------------------

	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("mouseClicked");

		if (studentBoxMax == 2)
		{
			int mouseX = e.getX();
			int mouseY = e.getY();

			//check if in rectangle: (40,190,120,80);
			if ((mouseX >= 40) && (mouseX <= 160) && (mouseY >= 190) && (mouseY <= 270))
			{
				studentBox = 0;
				repaint();
			}

			//check if in rectangle: ( 170,190,120,80);
			if ((mouseX >= 170) && (mouseX <= 290) && (mouseY >= 190) && (mouseY <= 270))
			{
				studentBox = 1;
				repaint();
			}
		}
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
		if (cursorBoxNum >= 19)
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

///////////////old code (when student entered durations):
//		int durval = 0;
//		String durtype;
//
//		//System.out.println ("Menu Item: " + e.getActionCommand() );
//		//System.out.println ("Clicked at: " + saveX + " , " + saveY);
//
//		durtype = "??";
//
//		if (e.getActionCommand().equals ( "HN" ) )
//		{
//			durval = 48;
//			durtype = "f2";
//
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "f.2";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "f~2";
//				tripletToggle = -1;
//			}
//		}
//		else if (e.getActionCommand().equals ( "QN" ) )
//		{
//			durval = 24;
//			durtype = "f4";
//
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "f.4";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "f~4";
//				tripletToggle = -1;
//			}
//		}
//		else if (e.getActionCommand().equals ( "EN" ) )
//		{
//			durval = 12;
//			durtype = "f8";
//
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "f.8";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "f~8";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "SN" ) )
//		{
//			durval = 6;
//			durtype = "f6";
//
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "f.6";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "f~6";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "HR" ) )
//		{
//			durval = -48;
//			durtype = "r2";
//
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "r.2";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "r~2";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "QR" ) )
//		{
//			durval = -24;
//			durtype = "r4";
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "r.4";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "r~4";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "ER" ) )
//		{
//			durval = -12;
//			durtype = "r8";
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "r.8";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "r~8";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "SR" ) )
//		{
//			durval = -6;
//			durtype = "r6";
//			if ( dotToggle == 1)
//			{
//				durval *= 3;
//				durval /= 2;
//				durtype = "r.6";
//				dotToggle = -1;
//			}
//			if (tripletToggle == 1)
//			{
//				durval *= 2;
//				durval /= 3;
//				durtype = "r~6";
//				tripletToggle = -1;
//			}
//
//		}
//		else if (e.getActionCommand().equals ( "OD" ) )
//		{
//			dotToggle *= -1 ;
//			tripletToggle = -1;
//			repaint();
//		}
//		else if (e.getActionCommand().equals ( "TP" ) )
//		{
//			tripletToggle *= -1;
//			dotToggle = -1;
//			repaint();
//		}
//		else if (e.getActionCommand().equals ( "Erase" ) )
//		{
//			durval = 0;
//			durtype = "";
//			tripletToggle = -1;
//			dotToggle = -1;
//			repaint();
//		}
//
//		if (durtype != "??")
//		{
//			//System.out.println("durtype: "+durtype+" durval="+durval+" cursorboxnum="+cursorBoxNum);
//			studentAnswer[cursorBoxNum - 1] = durtype;
//			studentDurVal[cursorBoxNum - 1] = durval;
//			cursorFwd();
//
//			durtype = "??";	// trying to prevent double triggering from one click
//			repaint();
//
//		}
//
//		if (e.getActionCommand().equals ( "<" ) )
//		{
//			cursorBack();
//			repaint();
//		}
//		else if (e.getActionCommand().equals ( ">" ) )
//		{
//			cursorFwd();
//			repaint();
//		}
///////////////end of old code 


		if (e.getActionCommand().equals ( "P1" ) )
		{
			studentAnswer[studentBox] = 0;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}
		if (e.getActionCommand().equals ( "P2" ) )
		{
			studentAnswer[studentBox] = 1;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}
		if (e.getActionCommand().equals ( "P3" ) )
		{
			studentAnswer[studentBox] = 2;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P4" ) )
		{
			studentAnswer[studentBox] = 3;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P5" ) )
		{
			studentAnswer[studentBox] = 4;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P6" ) )
		{
			studentAnswer[studentBox] = 5;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P7" ) )
		{
			studentAnswer[studentBox] = 6;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P8" ) )
		{
			studentAnswer[studentBox] = 7;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (e.getActionCommand().equals ( "P9" ) )
		{
			studentAnswer[studentBox] = 8;
			studentBox++;
			if (studentBox >= studentBoxMax)
			{
				studentBox = 0;
			}
			repaint();
		}

		if (studentBoxMax == 2)
		{
			if ((studentAnswer[0] >= 0) && (studentAnswer[1] >= 0))
			{
				checkButton.setEnabled(true);
			}
		}
		else
		{
			if (studentAnswer[0] >= 0)
			{
				checkButton.setEnabled(true);
			}
		}


		if (e.getActionCommand().equals ( "CHECK" ) )
		{
			//System.out.println("Check button pressed");


//			int correctdurval, delta;
//			//test:
//			System.out.println("Student answer");
//			for (int i=0 ; i<18 ; i++)
//			{
//				System.out.println("i="+i+"ans="+studentAnswer[i]+"val="+studentDurVal[i]);
//			}
//			for (int i=0 ; i < numMidiDurs ; i++)
//			{			
//				System.out.println ("i="+i
//				+"deltaMidiDurs[i]="+deltaMidiDurs[i]);
//			}
//
//			for (int q = 0 ; q < numMidiNotes ; q++ )
//			{
//				
//				System.out.println("midinotes["+q+"]="+midiNotes[q]);
//
//				studentCorrect[q] = true;
//
//				correctdurval = studentDurVal[q];
//				if (correctdurval != studentDurVal[q])
//				{
//					studentGotAllNotesCorrect = false;
//					studentCorrect[q] = false;
//				}
//			}


			boolean studentGotAllNotesCorrect = false;

			if (parmLevel.equals("2"))	
			{
				if ((currAnswerA == studentAnswer[0]) && (currAnswerB == studentAnswer[1]))
				{
					studentGotAllNotesCorrect = true;
				}
			}
			else
			{
				if (currAnswerA == studentAnswer[0])
				{
					studentGotAllNotesCorrect = true;
				}
			}


			if (!examMode)
			{
				//shownotesButton.setEnabled(true);
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

			nextButton.setEnabled(true);

			if (studentGotAllNotesCorrect)
			{
				strFeedback = strCorrect;
				numNotesToDisplay = numCmdCharIx;		// to display whole sequence
				nextButton.setEnabled(true);
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
			stopMidiPlayback();

			if (examMode)
			{
				sex = shuffledBase + shuffledQues [ shuffledQuesIndex ] ;
				//System.out.println ("getting exercise# : "+sex);
				currAnswerA = exercises.getAnswerNumA( sex );
				currAnswerB = exercises.getAnswerNumB( sex );			
				currCmdStr = exercises.getExerciseNum ( sex );
				currWavFile = exercises.getWavFileNum ( sex );

				shuffledQuesIndex++;
				if (shuffledQuesIndex >= shufdeck)
				{					
					shuffledQuesIndex = 0;
				}
			}
			else
			{
				currAnswerA = exercises.getAnswerA();
				currAnswerB = exercises.getAnswerB();
				currCmdStr = exercises.getExercise();
				currWavFile = exercises.getWavFile ();

				//System.out.println ("currCmdStr="+currCmdStr);
				//System.out.println ("Answer A = "+currAnswerA+" Answer B = "+currAnswerB);
			}

			//System.out.println ("Original cmdstr="+currCmdStr);



			//System.out.println ("myKey="+myKey+" Transposed cmdstr="+currCmdStr);

			notation.reset();
			notation.setCmdStr (currCmdStr);
			seq = notation.getSequence();
			resetNotation();
			resetStudentAnswer();

			checkButton.setEnabled(false);
			nextButton.setEnabled(false);
			playcorrButton.setEnabled(true);
			playCtr = 1;

			if (!examMode)
			{
				showcorrButton.setEnabled(false);
				shownotesButton.setEnabled(false);
				shownotesButton.setLabel ("SHOW NOTES");
			}

			numtimeschecked = 0;
			strFeedback = "";

			//reset cursor position for new question
			cursorBoxNum = 1;
			playSeqAfterDraw=false;
		
			repaint();
		}

		if (e.getActionCommand().equals ( "PLAY" ) )
		{
			//System.out.println("Play button pressed");
		
			playCtr++;
			if ( (!examMode) || ( (examMode) && (playCtr <= examPlayMax) ) )
			{
				//EVentually: check if configured for Audio Mode or MIDI mode:
				//startMidiPlayback();

				if (parmLevel.equals("1"))	
				{
			        	queueAudio ( currWavFile ,1);
			        }			
				if (parmLevel.equals("2"))	
				{
			                queueAudio ( currWavFile ,1);
			        }			
				if (parmLevel.equals("3"))	
				{
				            queueAudio ( currWavFile ,1);
			 	}			
			}
			if ((examMode) && (playCtr >= examPlayMax ))
			{
				playcorrButton.setEnabled(false);
			}
		}
		if (e.getActionCommand().equals ( "SHOW NOTES" ) )
		{
			//System.out.println("Show Notes button pressed");
			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
			//shownotesButton.setLabel ("HIDE NOTES");
			repaint();
		}
//		if (e.getActionCommand().equals ( "HIDE NOTES" ) )
//		{
//			//System.out.println("Show Notes button pressed");
//			numNotesToDisplay = 0;		// to display whole sequence
//			shownotesButton.setLabel ("SHOW NOTES");
//			repaint();
//		}

		if (e.getActionCommand().equals ( "SHOW CORRECT" ) )
		{
			//System.out.println("Show Correct button pressed");
			stopMidiPlayback();
			int correctdurval;
			int delta;

//			if (majorMinor > 0) {
//				delta = keyShiftMaj[myKey+7] ;
//			} else {
//				delta = keyShiftMin[myKey+7] ;
//			}

			//System.out.println ("delta="+delta);

//			for (int q = 0 ; q < numMidiNotes ; q++ )
//			{
//				correctdurval = (midiNotes[q] + delta) %12;
//
//				if ((correctdurval == 1) || (correctdurval == 3) 
//					|| (correctdurval == 6) || (correctdurval == 8) || (correctdurval == 10))
//				{
//					correctdurval *= -1;	// assume flats are being used for black keys
//				}
//
//				studentAnswer[q]=currdurtypes[ Math.abs(correctdurval) ];
//				studentCorrect[q]=true;
//				studentDurVal[q] = correctdurval;
//			}		

			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
			//shownotesButton.setLabel ("HIDE NOTES");

			strFeedback = strShowCorrect;

			studentAnswer[0] = currAnswerA;
			studentAnswer[1] = currAnswerB;

			nextButton.setEnabled(true);

			repaint();
		}
		if (e.getActionCommand().equals ( "TAKE EXAM" ) )
		{
			//05/02/2004: added for debugging:
			System.out.println("Take Exam button pressed");
			System.out.println("nextURL="+nextURL);
			System.out.println("appletcontext="+this.getAppletContext());

			stopMidiPlayback();
			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "NEXT LESSON" ) )
		{
			//System.out.println("Next Lesson button pressed");
			stopMidiPlayback();
			if (nextURL != null)
			{	
				this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "EXIT" ) )
		{
			//System.out.println("Exit button pressed");
			stopMidiPlayback();
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


    private void queueAudio(String audioFileName, int seconds)
    {
        audioQHead++;
	if (audioQHead == audioQueue.length)
	{
	    audioQHead = 0;
	}
	if (audioQHead == audioQTail) // queue full - ignore new message
	{
	    System.out.println("queueAudio: Audio Queue Is Full");
	    System.out.println("queueAudio: audioFileName = "+audioFileName);
	    audioQHead--; // remove item from queue if queue is full
	    if (audioQHead < 0)
	    {
	    	audioQHead = audioQueue.length - 1;
	    }

	}
	audioQueue[audioQHead] = audioFileName;
	audioDelays[audioQHead] = seconds;
    }


//---------------------------------------------------------------------------------------------
// Timing thread
//---------------------------------------------------------------------------------------------
    public void start() {
        if(!running) {
            running = true;
            looper = new Thread(this);
            looper.start();
        }
    }
    public void stop() {
        running = false;
    }
    public void run() {
        try {
            while(running) {
                looper.sleep(50);
                frameNext();
            }
        } catch(InterruptedException e) {
            running = false;
        }
    }

    private void frameNext() 
    {
	runCount++;

        if (audioWaitTime == 0)
	{
		if (audioQHead != audioQTail)
		{
		    audioQTail++;
		    if (audioQTail == audioQueue.length)
		    {
			audioQTail = 0;
		    }
		    play(getCodeBase(), audioQueue[audioQTail]);
		    audioWaitTime = audioDelays[audioQTail];
		} // if
	}


	if (runCount >= 20)
	{   runSeconds++;
	    runCount = 0;

	    // process audio queue
	    if (audioWaitTime > 0)
	    {
		audioWaitTime--;
	    }
	} // if

    } // frameNext

}
