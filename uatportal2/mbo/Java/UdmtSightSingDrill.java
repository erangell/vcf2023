import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.sound.midi.*;
//------------------------------------------------------------------------------

public class UdmtSightSingDrill extends Applet
	implements MouseListener,  ActionListener, ItemListener, MetaEventListener
{
//------------------------------------------------------------------------------
// UdmtSightSingDrill - SightSing drill and exam
//------------------------------------------------------------------------------
// PARAMS:
//
// SightSingType	= The type of exercises to present:
// Values: 
//	MajorScalar, MajorArp
//	NaturalMinorScalar, NaturalMinorArp,
//	HarmonicMinorScalar, HarmonicMinorArp,
//	MelodicMinorScalar, MelodicMinorArp
//				REQUIRED
//
// probabDist =	overrides default probability distribution only if exactly 15 integers are
// 	entered separated by commas.  Each must be between 0 and 100 and the sum
// 	must equal 100.  The integers are assigned in order as the probabilities of selecting
//	keys from 7 flats to 7 sharps.
//				OPTIONAL
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
// 05/01/2004: Ver 0.06  - changed to use UdmtURLBuilder class to build URL's for buttons,
// and new behavior to branch to "Sorry" page immediately when student fails exam.
// Started adding interface logic to UdmtExamLog.
//------------------------------------------------------------------------------
// 05/22/2004: Ver 0.07  - removed automatic playback of sequence
// NOTE: Version 0.07 incorrectly displayed 0.06 as the version number
//------------------------------------------------------------------------------
// 07/18/2004: Ver 0.08  - removed display of Level Numbers
//------------------------------------------------------------------------------
// 01/17/2005: ver 0.09 - changes to support exam login
//------------------------------------------------------------------------------
// 07/05/2005: ver 0.10 - changes to fix playback bug in Java 1.5
//------------------------------------------------------------------------------

	private	UdmtNotationApi notation;
	private	UdmtSightSingExercises exercises;
	private UdmtKeySelect keySelect;
	private UdmtImageButton btnLeftArrow, btnRightArrow;

	//private	UdmtTransposer transp;

	//05/01/2004:
	private UdmtURLBuilder urlBuilder;
	private UdmtExamLog examLog;
	// End of 05/01/2004 change.		

	private String udmtAppletName			=	"UdmtSightSingDrill";
	private String udmtAppletVersion		=	"0.10"	;

	private Image imgAllSymbols;
	private Sequencer sequencer;
	private Sequence seq, progChgSequence;

	private MidiEvent 	event;
	private ShortMessage 	msg;

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

	private int numNotesToDisplay = 1;

	private Button checkButton, nextButton, playcorrButton, takeExamButton, showcorrButton;
	private Button shownotesButton, nextLessonButton, exitButton;
	private Button btnDo, btnRe, btnMi, btnFa, btnSo, btnLa, btnTi;
	private Button btnRa, btnMe, btnSe, btnLe, btnTe;
	private Button btnDi, btnRi, btnFi, btnSi, btnLi;
	//private Button btnLeftArrow, btnRightArrow;
	private Choice imntCombo;

	private int userImnt;

	private boolean examMode = false;

	//private PopupMenu popup;
	//private final int	MENU_HOR_OFFSET		=	18;
	//private final int	MENU_VER_OFFSET		=	179;

	private String strDrillMode			=	"Sight Singing Drill";
	private String strExamMode			=	"Sight Singing Exam";
	private String strInstruct 			= 	"Play the starting note, then sing and record the displayed sequence.";

	private int showAbendScreen 			= 	0;
	private String txtAbend 			= 	"";

	private int	numStudentBoxes;
	private String[] 	studentAnswer;
	private int[]	studentSolfVal;
	private boolean[] studentCorrect;

	private int boxBoundL, boxBoundR, prevBoxBoundR;	// for calculating x coordinates of midpoints between 
	private int[] boxMidpt;				// SightSing input boxes.

	private int cursorBoxNum = 2;			// the box number the cursor is on

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

	private String parmJarURL 		= 	"";
	private URL jarURL;

	private int numtimeschecked 			= 	0;
	private int numquespresented 			= 	0;
	private int numquescorrect1sttime 		= 	0;


	private String strScore1 			= 	"Exercise: ";
	private int nScore1 				= 	0;
	private String strScore2 			= 	" of ";
	private int nScore2 				= 	0;
//	private String strScore3 			= 	" = ";
//	private int nScorePercent 			= 	0;
//	private String strScore4 			= 	"%";
	private String strScoreDisp;

	private String strCorrect 			= 	"Please save this download, then launch UdmtRecorder.jar to record your answer.";
	private String strIncorrect			= 	"Incorrect.  Please fix items in red and try again.";
	private String strIncorrectExam 		= 	"Incorrect.";
	private String strShowCorrect			= 	"";

	private String strMaster1 			= 	"Congratulations.  You have passed the exam";
	private String strMaster2 			= 	"and may move on if you wish.";
	private String strFeedback 			= 	"";
	private String strExamFeedback1 		= 	"";
	private String strExamFeedback2 		= 	"";

	private String[] flatSyllables={ "do","ra","re","me","mi","fa","se","so","le","la","te","ti"};
	private String[] sharpSyllables={ "do","di","re","ri","mi","fa","fi","so","si","la","li","ti"};
	private String[] currSyllables;

	int lBtn = 20;	// left coordinate of SightSing button set
	int wBtn = 25;	// width of each button
	int hBtn = 20;	// height of each button
	int sBtn = 30;	// space between left edges of each button
	int xBtn;		// used for calculating x coordinate of each button
	int yBtn = 250;	// y coord of button set
	//05/22/2004 - changed from true to false
	boolean playSeqAfterDraw=false;

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

	// amount to add to transposed sequence to get back to original - for calculating SightSing syllable
	// Major keys:               Cb Gb Db Ab Eb Bb   F C  G  D  A  E   B  F# C#
	private int[] keyShiftMaj = {+1,-6,-1,-8,-3,-10,-5,0,-7,-2,-9,-4,-11,-6,-1};

	// Minor keys:                ab eb  bb  f  c  g  d  a  e   b f# c# g# d#  a# (note: offsets adjusted by +4)
	private int[] keyShiftMin = { -8,-3,-10,-5, 0,-7,-2,-9,-4,-11,-6,-1,-8,-3,-10 };

	private int playCtr = 1;
	private int examPlayMax = 4;
	private String parmExamPlayMax;

	private String strDrillType;
	private int currExerciseIndex;
//------------------------------------------------------------------------------

	public void init()
	{
		// Display version and debugging info in Java Console
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		rectScreen = getBounds();

		notation = new UdmtNotationApi();
		//System.out.println ("UdmtNotationApi invoked: Version="+notation.getVersion());
		notation.initAPI();

		exercises = new UdmtSightSingExercises();
		keySelect = new UdmtKeySelect();
		keySelect.initKeySelect();

		//transp = new UdmtTransposer();
		//transp.initAPI();

		//05/01/2004- New procedure for building URLs requires object to be instantiated once
		urlBuilder = new UdmtURLBuilder();
		// End of 05/01/2004 change.		

		//05/01/2004- New procedure for Exam Log requires object to be instantiated once
		examLog = new UdmtExamLog(); 
		// End of 05/01/2004 change.		

		parmSolfType = getParameter ("SightSingType");
		if (parmSolfType == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: SightSingType is required";
			System.out.println ("The value for the parameter SightSingType must be one of the following:");
			System.out.println ("MajorScalar,MajorArp,NaturalMinorScalar,NaturalMinorArp,");
			System.out.println ("HarmonicMinorScalar,HarmonicMinorArp,MelodicMinorScalar,MelodicMinorArp");
		}
		else if (parmSolfType.equals("MajorScalar"))	{
			strDrillMode="Sight Singing Practice: C Major";
			strDrillType="C Major";
			strExamMode="SightSing Exam: Major";
			exercises.setExerciseType ('M','S',' ');	
			majorMinor = +1;
		}
		else if (parmSolfType.equals("MajorArp"))	{

			strDrillMode="Sight Singing Practice: Other Major Keys";
			strDrillType="Other Major Keys";
			strExamMode="SightSing Exam: Major";
			exercises.setExerciseType ('M','A',' ');	
			majorMinor = +1;
		}
		else if (parmSolfType.equals("NaturalMinorScalar"))	{
			strDrillMode="Sight Singing Practice: Natural Minor";
			strDrillType="Natural Minor";
			strExamMode="SightSing Exam: Natural Minor";
			exercises.setExerciseType ('m','S','N');	
			majorMinor = -1;
		}
		else if (parmSolfType.equals("NaturalMinorArp"))	{
			strDrillMode="Sight Singing Practice: Natural Minor";
			strExamMode="SightSing Exam: Natural Minor";
			exercises.setExerciseType ('m','A','N');	
			majorMinor = -1;
		}
		else if (parmSolfType.equals("HarmonicMinorScalar"))	{
			strDrillMode="Sight Singing Practice: Harmonic Minor";
			strDrillType="Harmonic Minor";
			strExamMode="SightSing Exam: Harmonic Minor";
			exercises.setExerciseType ('m','S','H');	
			majorMinor = -1;
		}
		else if (parmSolfType.equals("HarmonicMinorArp"))	{
			strDrillMode="Sight Singing Practice: Harmonic Minor";
			strExamMode="SightSing Exam: Harmonic Minor";
			exercises.setExerciseType ('m','A','H');	
			majorMinor = -1;
		}
		else if (parmSolfType.equals("MelodicMinorScalar"))	{
			strDrillMode="Sight Singing Practice: Melodic Minor";
			strDrillType="Melodic Minor";
			strExamMode="SightSing Exam: Melodic Minor";
	 		exercises.setExerciseType ('m','S','M');	
			majorMinor = -1;
		}
		else if (parmSolfType.equals("MelodicMinorArp"))	{
			strDrillMode="Sight Singing Practice: Melodic Minor";
			strExamMode="SightSing Exam: Melodic Minor";
			exercises.setExerciseType ('m','A','M');	
			majorMinor = -1;
		}
		else	{
			showAbendScreen = 1;
			txtAbend = "Param: SightSingType is invalid.";
			System.out.println ("The value for the parameter SightSingType must be one of the following:");
			System.out.println ("MajorScalar,MajorArp,NaturalMinorScalar,NaturalMinorArp,");
			System.out.println ("HarmonicMinorScalar,HarmonicMinorArp,MelodicMinorScalar,MelodicMinorArp");
		}

		parmProbabDist = getParameter ("probabDist");
		if ( parmProbabDist != null)  {
			//override default probability distribution only if exactly 15 numbers were entered separated by commas
			boolean allProbabsValid = true;
			int sumProbabs =0, probCount=0;
			int[] probab = new int[15];
 			java.util.StringTokenizer st =                   
			new java.util.StringTokenizer( parmProbabDist ,",");
			probCount = st.countTokens();           
 			probab = new int[ probCount ];            
			if ( probCount == 15 ) {
				for(int i=0; i< probCount ; i++) {            
					probab[i] = Integer.parseInt(st.nextToken());          
					if ( ( probab[i] < 0 ) || ( probab[i] > 100 ) )
					{
						allProbabsValid = false;
						System.out.println("PARAM probabDist contains invalid value:"+probab[i]);
					}
					sumProbabs += probab[i] ;
				} //for
				if (allProbabsValid && (sumProbabs == 100) )  {
					for(int i=0; i<= 14 ; i++) {     
						keySelect.setProb (i-7, probab[i] );
						//System.out.println("probab of "+(i-7)+"="+ probab[i] );
					} // for
				} // if allProbabsValid & sumProbabs = 100
				else if (sumProbabs != 100)
				{
					System.out.println("PARAM probabDist: sum of probablilities must equal 100");
				}
			} // if probCount == 15 
			else {
				System.out.println("PARAM probabDist does not contain 15 integers");
			}
		} // if  parmProbabDist != null

		currNumExercises = exercises.getNumExercises ();
		currExerciseIndex = 1;

//		//Transposition test:
//		for (int x=0 ; x< currNumExercises; x++)
//		{
//			currCmdStr = exercises.getExercise();
//			System.out.println ();
//			System.out.println ();
//			System.out.println ("x="+x+" currcmdstr="+currCmdStr);
//			System.out.println ("Transposing to:");
//			for (int skey=0 ; skey <= 7 ; skey++)
//			{		
//				System.out.println ();
//				System.out.println (skey+" sharps:");
//				transp.reset();
//				transp.setTranspKey (skey, 'S',majorMinor);
//				transp.setCmdStr (currCmdStr);
//				System.out.println ( transp.getTransposed() );		
//			}		
//			for (int fkey=0 ; fkey <= 7 ; fkey++)
//			{
//				System.out.println (fkey+" flats:");
//				transp.reset();
//				transp.setTranspKey (fkey, 'F',majorMinor);
//				transp.setCmdStr (currCmdStr);
//				System.out.println ( transp.getTransposed() );
//			}
//		}


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
		shuffledQues = exercises.shuffleN (num_exam_ques);
		//for (int i=0 ; i < num_exam_ques ; i++)
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

			//01/17/2005 - use UrlBuilder only if not in Exam Mode
			if (examMode)
			{
				UdmtExamKey objEk = new UdmtExamKey();
				String sExamKey = objEk.getExamKey("SightSing"+parmSolfType+"Pass");
				parmNextURL = parmNextURL + "?r="+sExamKey;
				//System.out.println ("parmNextURL="+parmNextURL); //FOR DEBUGGING ONLY!!!
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


				//09/15/2004 - use UrlBuilder only if not in Exam Mode
				if (examMode)
				{
					UdmtExamKey objEk = new UdmtExamKey();
					String sExamKey = objEk.getExamKey("SightSing"+parmSolfType+"Fail");
					parmDrillURL = parmDrillURL + "?r="+sExamKey;
					//System.out.println ("parmDrillURL="+parmDrillURL); //FOR DEBUGGING ONLY!!!
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


		parmJarURL = urlBuilder.buildURL (this.getCodeBase() , "UdmtRecorder.jar");

		try	{
			jarURL = new URL(parmJarURL);
		}
		catch ( MalformedURLException e )	{
			showAbendScreen = 1;
			txtAbend = "Malformed URL Exception when creating URL from parmJarURL";
			e.printStackTrace();
		}
		catch ( Exception e )	{
			showAbendScreen = 1;
			txtAbend = "Exception when creating URL from parmJarURL";
			e.printStackTrace();
		}



		MediaTracker mt = new MediaTracker(this);
		URL url = getCodeBase();
		imgAllSymbols = getImage (url, "symbols-28pt-tr.gif");

		mt.addImage (imgAllSymbols, 1);

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
			currCmdStr = exercises.getExercise();
		}

		//System.out.println ("Original cmdstr="+currCmdStr);

		myKey = keySelect.getRandKey();
		if (myKey >= 0) {
			myKeySF = 'S';
		} else {
			myKeySF = 'F';
		}

		//transp.reset();
		//transp.setTranspKey (Math.abs(myKey), myKeySF ,majorMinor);
		//transp.setCmdStr (currCmdStr);
		//currCmdStr = transp.getTransposed();
		notation.setCmdStr (currCmdStr);

		//System.out.println ("myKey="+myKey+" Transposed cmdstr="+currCmdStr);

		//System.out.println("Testing getRandKey");
		//int[] probdist = new int[15];
		//for (int p=0;p<1000;p++)
		//{
		//	probdist[keySelect.getRandKey()+7]++;
		//}
		//for (int i=0 ; i < 15 ; i++)
		//{
		//	System.out.println("i="+i+" probdist="+probdist[i]);
		//}
	
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

		
		studentAnswer = new String[16];
		studentSolfVal = new int[16];
		studentCorrect = new boolean[16];
	
		resetStudentAnswer();

		//System.out.println ("Adding buttons to layout");

		setLayout(null);

		checkButton = new Button("RECORDER");
      	checkButton.addActionListener(this);
		checkButton.setEnabled(true);
		checkButton.setBounds ( 300,350,110,30 );
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

			shownotesButton = new Button("SHOW HINT");
			shownotesButton.setEnabled(true);        
			shownotesButton.addActionListener(this);
			shownotesButton.setBounds (  180,300,110,30 );
			add(shownotesButton);


			showcorrButton = new Button("PLAY ALL");
			showcorrButton.setEnabled(true);        
			showcorrButton.addActionListener(this);
			showcorrButton.setBounds ( 180,350,110,30);
			add(showcorrButton);

			takeExamButton = new Button("TAKE EXAM");
			takeExamButton.setEnabled(false);        
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

//		btnLeftArrow = new UdmtImageButton ("arrow-left-black-tr.gif", this);
//		btnLeftArrow.setActionCommand ("<");
//		btnLeftArrow.addActionListener(this);
//		btnLeftArrow.setBounds ( xBtn, staffY + 80 - 2 ,wBtn,hBtn );
//		add (btnLeftArrow);
//
//		xBtn += sBtn;
//		btnDo = new Button("do");
//		btnDo.setEnabled(true);        
//		btnDo.addActionListener(this);
//		btnDo.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnDo);
//
//		xBtn += sBtn;
//		btnDi = new Button("di");
//		btnDi.setEnabled(true);        
//		btnDi.addActionListener(this);
//		btnDi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnDi);
//
//		xBtn += sBtn;
//		btnRa = new Button("ra");
//		btnRa.setEnabled(true);        
//		btnRa.addActionListener(this);
//		btnRa.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnRa);
//
//
//		xBtn += sBtn;
//		btnRe = new Button("re");
//		btnRe.setEnabled(true);        
//		btnRe.addActionListener(this);
//		btnRe.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnRe);
//
//
//		xBtn += sBtn;
//		btnRi = new Button("ri");
//		btnRi.setEnabled(true);        
//		btnRi.addActionListener(this);
//		btnRi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnRi);
//
//
//		xBtn += sBtn;
//		btnMe = new Button("me");
//		btnMe.setEnabled(true);        
//		btnMe.addActionListener(this);
//		btnMe.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnMe);
//
//		xBtn += sBtn;
//		btnMi = new Button("mi");
//		btnMi.setEnabled(true);        
//		btnMi.addActionListener(this);
//		btnMi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnMi);
//
//		xBtn += sBtn;
//		btnFa = new Button("fa");
//		btnFa.setEnabled(true);        
//		btnFa.addActionListener(this);
//		btnFa.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnFa);
//
//		xBtn += sBtn;
//		btnFi = new Button("fi");
//		btnFi.setEnabled(true);        
//		btnFi.addActionListener(this);
//		btnFi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnFi);
//
//		xBtn += sBtn;
//		btnSe = new Button("se");
//		btnSe.setEnabled(true);        
//		btnSe.addActionListener(this);
//		btnSe.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnSe);
//
//
//
//		xBtn += sBtn;
//		btnSo = new Button("so");
//		btnSo.setEnabled(true);        
//		btnSo.addActionListener(this);
//		btnSo.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnSo);
//
//		xBtn += sBtn;
//		btnSi = new Button("si");
//		btnSi.setEnabled(true);        
//		btnSi.addActionListener(this);
//		btnSi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnSi);
//
//		xBtn += sBtn;
//		btnLe = new Button("le");
//		btnLe.setEnabled(true);        
//		btnLe.addActionListener(this);
//		btnLe.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnLe);
//
//		xBtn += sBtn;
//		btnLa = new Button("la");
//		btnLa.setEnabled(true);        
//		btnLa.addActionListener(this);
//		btnLa.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnLa);
//
//		xBtn += sBtn;
//		btnLi = new Button("li");
//		btnLi.setEnabled(true);        
//		btnLi.addActionListener(this);
//		btnLi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnLi);
//
//
//
//		xBtn += sBtn;
//		btnTe = new Button("te");
//		btnTe.setEnabled(true);        
//		btnTe.addActionListener(this);
//		btnTe.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnTe);
//
//		xBtn += sBtn;
//		btnTi = new Button("ti");
//		btnTi.setEnabled(true);        
//		btnTi.addActionListener(this);
//		btnTi.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		add(btnTi);
//
//		xBtn += sBtn;
//		//btnRightArrow = new Button(">");
//		//btnRightArrow.setEnabled(true);        
//		//btnRightArrow.addActionListener(this);
//		//btnRightArrow.setBounds ( xBtn,yBtn,wBtn,hBtn );
//		//add(btnRightArrow);
//
//		btnRightArrow = new UdmtImageButton ("arrow-right-black-tr.gif", this);
//		btnRightArrow.setActionCommand (">");
//		btnRightArrow.addActionListener(this);
//		btnRightArrow.setBounds ( xBtn - 1 , staffY + 80 - 2 ,wBtn,hBtn );
//		add (btnRightArrow);
//


		//System.out.println ("Adding instrument combobox");

		imntCombo = new Choice();
		for (int i=0 ; i <= 127 ; i++)	{
			imntCombo.add ( GMImntList[i] );
		} 
		if (examMode) {	
			imntCombo.setBounds ( 60,350,110,30 );
		}
		else	{
			imntCombo.setBounds ( 60,305,110,30);
		}
		add(imntCombo);
		imntCombo.addItemListener(this);	

		// For SightSing drill, popup menu always active.
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

		//Logic from "Show Correct" button
		int correctSolfVal;
		int delta;

		if (majorMinor > 0) {
			delta = keyShiftMaj[myKey+7] ;
		} else {
			delta = keyShiftMin[myKey+7] ;
		}

		delta = 0;
		delta = (midiNotes[0] % 12) * -1;

		//System.out.println ("delta="+delta);

		for (int q = 0 ; q < numMidiNotes ; q++ )
		{
			correctSolfVal = (midiNotes[q] + delta) %12;
			if ((correctSolfVal == 1) || (correctSolfVal == 3) 
			|| (correctSolfVal == 6) || (correctSolfVal == 8) || (correctSolfVal == 10))
			{
				correctSolfVal *= -1;	// assume flats are being used for black keys
			}

			studentAnswer[q]=currSyllables[ Math.abs(correctSolfVal) ];
			studentCorrect[q]=true;
			studentSolfVal[q] = correctSolfVal;
		}		
		
		//numNotesToDisplay = numCmdCharIx;		// to display whole sequence

		//shownotesButton.setLabel ("HIDE NOTES");
		//shownotesButton.setEnabled (true);

		//strFeedback = strShowCorrect;
		strFeedback = "When recording: State your name, "+strDrillType+", Exercise #1, then begin singing.";

		nextButton.setEnabled(true);

		repaint();
	}

//------------------------------------------------------------------------------

	private void resetStudentAnswer()
	{
		for (int i=0 ; i<16 ; i++)
		{
			studentAnswer[i] = "  ";
			studentSolfVal[i] = 0;
			studentCorrect[i] = true;
		}
		studentAnswer[0]="do";
		studentCorrect[0] = true;

		numStudentBoxes = numCmdCharIx ;
		numNotesToDisplay = 1;

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


		for (int x=staffX ; x < staffX+530 ; x+=37 )
		{
			g.drawImage (muStaff.getImage(), x, staffY, this );
		}


		g.drawImage (muBar.getImage(), staffX - 2 , staffY, this);

		g.drawImage (muRtDblBar.getImage(), staffX+550, staffY, this);


		// Draw the music symbols: This needs to be modified if individual notes are to be displayed
		// not necessarily in order.

		//for (int n=0 ; n <= (noteSymIdIx[numNotesToDisplay-1]) ; n++)
		for (int n=0 ; n <= (noteSymIdIx[numCmdCharIx-1]) ; n++)
		{
			g.drawImage 	( imgs[symIDsOnStaff[n]]
					, staffX + staffXpos[n]
					, staffY + staffYpos[n]
					, this
					);

		}

		// Draw dots above note positions and boxes below note positions
		for (int n=0 ; n < numCmdCharIx ; n++)
		{
			if (studentCorrect[n])	{
				g.setColor (Color.black);
			}
			else	{
				g.setColor (Color.red);
			}

			g.drawImage 	( imgs[31]
					, staffX + staffXpos[noteSymIdIx[n]] 
					, staffY - 30
					, this
					);

			if (n < (numNotesToDisplay))
			{
				g.drawRect 	( staffX + staffXpos[noteSymIdIx[n]] - 5 - 1
					, staffY + 80 - 1
					, 22
					, 22
					);
				g.drawRect 	( staffX + staffXpos[noteSymIdIx[n]] - 5 - 2
					, staffY + 80 - 2
					, 24
					, 24
					);

				centerStringAroundX (g, studentAnswer[n], txtfont, staffX + staffXpos[noteSymIdIx[n]] + 5 , staffY + 95);
			}

//			if (cursorBoxNum == (n+1))
//			{
//				g.setColor (Color.blue);
//				for (int p = 0 ; p < numArrowPts ; p++ )
//				{
//					xPolygon[p] =  xArrow0[p] + staffX + staffXpos[noteSymIdIx[n]] - 5 - 1 + 11;
//					yPolygon[p] =  yArrow0[p] + staffY + 128 - 1;
//				}
//				g.fillPolygon ( xPolygon, yPolygon, numArrowPts );
//			}

			g.setColor (Color.black);
		}
		g.setColor (Color.black);

		//System.out.println("numBeamsOnStaff="+numBeamsOnStaff);

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


		//nScore1=numquescorrect1sttime;
		//nScore2=numquespresented;

		//if (nScore2 == 0)	{
		//	nScorePercent  = 0;
		//}
		//else	{
		//	nScorePercent  = (int)(nScore1 * 100 / nScore2);
		//}

		//strScoreDisp = strScore1 + nScore1 + strScore2 + nScore2 + strScore3 + nScorePercent + strScore4;

		nScore1 = currExerciseIndex;
		nScore2 = currNumExercises;	
		strScoreDisp = strScore1 + nScore1 + strScore2 + nScore2 ;

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
				sequencer.setTempoInBPM( (float)240.0 );
				sequencer.setSequence(seq);
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
	public void itemStateChanged (ItemEvent ie)
	{
		userImnt 	= imntCombo.getSelectedIndex();

		notation.reset();
		notation.setSequencePatch (userImnt);
		notation.setCmdStr (currCmdStr);
		//seq = notation.getSequence();

		/* OLD CODE:
		Track[] progChgTracks;
	    	byte blankbytes[] = { 0 };

		try
		{
	    		progChgSequence = new Sequence(Sequence.PPQ, 24, 1);
	    		progChgTracks = progChgSequence.getTracks();
	    		ShortMessage mymsg = new ShortMessage();
	    		mymsg.setMessage(0xC0, userImnt, 0);    	    
			MidiEvent myevent = new MidiEvent(mymsg, 0);		
		      progChgTracks[0].add(myevent);
	            MetaMessage metamsg = new MetaMessage();
		      metamsg.setMessage(47, blankbytes, 0);
	    		MidiEvent metaevent = new MidiEvent(metamsg, 1);
	    		progChgTracks[0].add(metaevent);
			sequencer.setSequence(progChgSequence);
			sequencer.start();
			playStatus = +1;
		}
		catch (InvalidMidiDataException e)
		{   
			System.out.println("Invalid MIDI Data Exception"); 
	    		e.printStackTrace();
		}
		*/
	}
//------------------------------------------------------------------------------
//
//	public void setupPopupMenu()
//	{
//		MenuItem mi ;
//		popup = new PopupMenu ( "Select SightSing Syllable" );
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
		if (cursorBoxNum >= (numStudentBoxes + 1))
		{
			cursorBoxNum=2;
		}
	} 
	public void cursorBack()
	{
		//System.out.println("CursorBack: cursorboxnum="+cursorBoxNum);
		cursorBoxNum--;
		if (cursorBoxNum < 2)
		{
			cursorBoxNum=numStudentBoxes;
		}
	} 

	public void actionPerformed (ActionEvent e)
	{
		int solfval = 0;
		String syllable;

		//System.out.println ("Menu Item: " + e.getActionCommand() );
		//System.out.println ("Clicked at: " + saveX + " , " + saveY);

		syllable = "??";

		if (e.getActionCommand().equals ( "do" ) )
		{
			solfval = 0;
			syllable = "do";
		}
		else if (e.getActionCommand().equals ( "di" ) )
		{
			solfval = 1;
			syllable = "di";
		}
		else if (e.getActionCommand().equals ( "ra" ) )
		{
			solfval = -1;
			syllable = "ra";
		}
		else if (e.getActionCommand().equals ( "re" ) )
		{
			solfval = 2;
			syllable = "re";
		}
		else if (e.getActionCommand().equals ( "ri" ) )
		{
			solfval = 3;
			syllable = "ri";
		}
		else if (e.getActionCommand().equals ( "me" ) )
		{
			solfval = -3;
			syllable = "me";
		}
		else if (e.getActionCommand().equals ( "mi" ) )
		{
			solfval = 4;
			syllable = "mi";
		}
		else if (e.getActionCommand().equals ( "fa" ) )
		{
			solfval = 5;
			syllable = "fa";
		}
		else if (e.getActionCommand().equals ( "fi" ) )
		{
			solfval = 6;
			syllable = "fi";
		}
		else if (e.getActionCommand().equals ( "se" ) )
		{
			solfval = -6;
			syllable = "se";
		}
		else if (e.getActionCommand().equals ( "so" ) )
		{
			solfval = 7;
			syllable = "so";
		}
		else if (e.getActionCommand().equals ( "si" ) )
		{
			solfval = 8;
			syllable = "si";
		}
		else if (e.getActionCommand().equals ( "le" ) )
		{
			solfval = -8;
			syllable = "le";
		}
		else if (e.getActionCommand().equals ( "la" ) )
		{
			solfval = 9;
			syllable = "la";
		}
		else if (e.getActionCommand().equals ( "li" ) )
		{
			solfval = 10;
			syllable = "li";
		}
		else if (e.getActionCommand().equals ( "te" ) )
		{
			solfval = -10;
			syllable = "te";
		}
		else if (e.getActionCommand().equals ( "ti" ) )
		{
			solfval = 11;
			syllable = "ti";
		}

		if (syllable != "??")
		{
			//System.out.println("Syllable: "+syllable+" solfval="+solfval+" cursorboxnum="+cursorBoxNum);
			studentAnswer[cursorBoxNum - 1] = syllable;
			studentSolfVal[cursorBoxNum - 1] = solfval;
			cursorFwd();
			// Check if student put an answer in every box
			boolean enableCheck = true;
			for (int i=0 ; i < numStudentBoxes ; i++)	{
				if (studentAnswer[i].equals("  "))	{
					enableCheck = false;
				}
			}
			if (enableCheck)	{
				checkButton.setEnabled(true);
			}
			else	{
				checkButton.setEnabled(false);
			}
			syllable = "??";	// trying to prevent double triggering from one click
			repaint();
		}

		if (e.getActionCommand().equals ( "<" ) )
		{
			cursorBack();
			repaint();
		}
		else if (e.getActionCommand().equals ( ">" ) )
		{
			cursorFwd();
			repaint();
		}



		if (e.getActionCommand().equals ( "RECORDER" ) )
		{
			//System.out.println("Check button pressed");
			int correctSolfVal, delta;
			boolean studentGotAllNotesCorrect = true;

			if (majorMinor > 0) {
				delta = keyShiftMaj[myKey+7] ;
			} else {
				delta = keyShiftMin[myKey+7] ;
			}

			delta = 0;
			delta = (midiNotes[0] % 12) * -1;
			//System.out.println ("delta="+delta);

			for (int q = 0 ; q < numMidiNotes ; q++ )
			{
				correctSolfVal = ( midiNotes[q] + delta) % 12;

				if 	( 	(correctSolfVal == 10) 	// For SightSing exercises, all minor keys will use flats
					|| 	(correctSolfVal == 8)	// so we can get away with multiplying flat SightSings by -1
					||	(correctSolfVal == 6)
					||	(correctSolfVal == 3)
					||	(correctSolfVal == 1)
					)
				{
					correctSolfVal *= -1;
				}
				//System.out.println("midinotes["+q+"]="+midiNotes[q]+" correctSolfval = "+ correctSolfVal );
				//System.out.println("Student answer:"+studentAnswer[q]+" StudentSolfVal="+studentSolfVal[q]);

				studentCorrect[q] = true;
				if (correctSolfVal != studentSolfVal[q])
				{
					studentGotAllNotesCorrect = false;
					studentCorrect[q] = false;
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

			if (studentGotAllNotesCorrect)
			{
				//strFeedback = strCorrect;
				numNotesToDisplay = 1;		// to display whole sequence
				nextButton.setEnabled(true);
				if (!examMode)
				{
					//shownotesButton.setLabel ("HIDE NOTES");
					showcorrButton.setEnabled(true);
				}
			}
			else
			{
				if (examMode)
				{
					//strFeedback = strIncorrectExam;
					nextButton.setEnabled(true);
				}
				else
				{
					//strFeedback = strIncorrect;
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
					imntCombo.setVisible(false);

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
					imntCombo.setVisible(false);

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
			this.getAppletContext().showDocument(jarURL);

		}//check

		if (e.getActionCommand().equals ( "NEXT" ) )
		{
			//System.out.println("Next button pressed");
			stopMidiPlayback();

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
				currCmdStr = exercises.getExercise();
				currExerciseIndex++;
				if (currExerciseIndex > currNumExercises)
				{
					currExerciseIndex = 1;
				}

				strFeedback = "When recording: State your name, "+strDrillType+", Exercise #"+currExerciseIndex+", then begin singing.";
			}

			//System.out.println ("Original cmdstr="+currCmdStr);

			myKey = keySelect.getRandKey();
			if (myKey >= 0) {
				myKeySF = 'S';
			} else {
				myKeySF = 'F';
			}

			//transp.reset();
			//transp.setTranspKey (Math.abs(myKey), myKeySF ,majorMinor);
			//transp.setCmdStr (currCmdStr);
			//currCmdStr = transp.getTransposed();

			//System.out.println ("myKey="+myKey+" Transposed cmdstr="+currCmdStr);

			notation.reset();
			notation.setCmdStr (currCmdStr);
			//seq = notation.getSequence();
			resetNotation();
			resetStudentAnswer();

			//checkButton.setEnabled(false);
			checkButton.setEnabled(true);
			nextButton.setEnabled(false);
			playcorrButton.setEnabled(true);
			playCtr = 1;

			if (!examMode)
			{
				showcorrButton.setEnabled(true);
				//shownotesButton.setEnabled(false);
				shownotesButton.setLabel ("SHOW HINT");
			}

			numtimeschecked = 0;
			//strFeedback = "";

			//reset cursor position for new question
			cursorBoxNum = 2;
			// 05/22/2004: changed from true to false
			playSeqAfterDraw=false;
		
			//repaint();

			int correctSolfVal;
			int delta;

			if (majorMinor > 0) {
				delta = keyShiftMaj[myKey+7] ;
			} else {
				delta = keyShiftMin[myKey+7] ;
			}

			delta = 0; 
			delta = (midiNotes[0] % 12) * -1;	
			//System.out.println ("delta="+delta);

			for (int q = 0 ; q < numMidiNotes ; q++ )
			{
				correctSolfVal = (midiNotes[q] + delta) %12;
				if ((correctSolfVal == 1) || (correctSolfVal == 3) 
				|| (correctSolfVal == 6) || (correctSolfVal == 8) || (correctSolfVal == 10))
				{
					correctSolfVal *= -1;	// assume flats are being used for black keys
				}

				studentAnswer[q]=currSyllables[ Math.abs(correctSolfVal) ];
				studentCorrect[q]=true;
				studentSolfVal[q] = correctSolfVal;
			}		
			numNotesToDisplay = 1;		// to display whole sequence
			//shownotesButton.setLabel ("HIDE NOTES");
			//shownotesButton.setEnabled (true);

			//strFeedback = strShowCorrect;

			nextButton.setEnabled(true);

			repaint();

		}

		if (e.getActionCommand().equals ( "PLAY" ) )
		{
			//System.out.println("Play button pressed");
			try 
			{
				seq = new Sequence(Sequence.PPQ, 4);         
				Track track = seq.createTrack();                        

				msg = new ShortMessage();
				msg.setMessage(0xC0, 1, userImnt, 0);

				event = new MidiEvent(msg, 0);
				track.add(event);

				msg = new ShortMessage();
				msg.setMessage(0x90, 1, midiNotes[0], 100);
				event = new MidiEvent(msg, 1);


				track.add(event);

				byte blankbytes[] = { 0 };
				MetaMessage mymsg = new MetaMessage();
				mymsg.setMessage(47, blankbytes, 0);
				MidiEvent myevent = new MidiEvent(mymsg, 24);
				track.add(myevent);
			}
			catch (InvalidMidiDataException imex)
			{   
				System.out.println("Invalid MIDI Data Exception"); 
		    		imex.printStackTrace();
			}


		
			playCtr++;
			if ( (!examMode) || ( (examMode) && (playCtr <= examPlayMax) ) )
			{
				startMidiPlayback();
			}
			if ((examMode) && (playCtr >= examPlayMax ))
			{
				playcorrButton.setEnabled(false);
			}
		}
		if (e.getActionCommand().equals ( "SHOW HINT" ) )
		{
			//System.out.println("Show Notes button pressed");
			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
			shownotesButton.setLabel ("HIDE HINT");
			repaint();
		}
		if (e.getActionCommand().equals ( "HIDE HINT" ) )
		{
			//System.out.println("Show Notes button pressed");
			numNotesToDisplay = 1;		// to display whole sequence
			shownotesButton.setLabel ("SHOW HINT");
			repaint();
		}

		if (e.getActionCommand().equals ( "PLAY ALL" ) )
		{
			//7-5-2005: Resetting program change seq seems to fix bug
			userImnt = imntCombo.getSelectedIndex();
			notation.reset();
			notation.setSequencePatch (userImnt);
			notation.setCmdStr (currCmdStr);
			
			seq = notation.getSequence();

			startMidiPlayback();

//			//System.out.println("Show Correct button pressed");
//			stopMidiPlayback();
//			int correctSolfVal;
//			int delta;
//
//			if (majorMinor > 0) {
//				delta = keyShiftMaj[myKey+7] ;
//			} else {
//				delta = keyShiftMin[myKey+7] ;
//			}
//
//			//System.out.println ("delta="+delta);
//
//			for (int q = 0 ; q < numMidiNotes ; q++ )
//			{
//				correctSolfVal = (midiNotes[q] + delta) %12;
//
//				if ((correctSolfVal == 1) || (correctSolfVal == 3) 
//					|| (correctSolfVal == 6) || (correctSolfVal == 8) || (correctSolfVal == 10))
//				{
//					correctSolfVal *= -1;	// assume flats are being used for black keys
//				}
//
//				studentAnswer[q]=currSyllables[ Math.abs(correctSolfVal) ];
//				studentCorrect[q]=true;
//				studentSolfVal[q] = correctSolfVal;
//			}		
//			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
//			//shownotesButton.setLabel ("HIDE NOTES");
//
//			strFeedback = strShowCorrect;
//
//			nextButton.setEnabled(true);
//
//			repaint();
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

}
