import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.sound.midi.*;
import javax.swing.*;
import java.io.*;
import java.security.*;
//------------------------------------------------------------------------------

// THIS APPLET RUNS AS AN APPLICATION

public class UdmtMicTestApp extends Applet
	implements MouseListener,  ActionListener, ItemListener, MetaEventListener, Runnable
{

	private String tempParmSightSingType = "MajorScalar";
	private String tempParmExamMode = "DRILL";
	

	private static JFrame mainFrame;
	private static int threadCount = 0;
	private static boolean _running = false;	//used for Runnable interface
	private static Thread _looper;


	private static int scrnWidth = 600;
	private static int scrnHeight = 600;


	private static UdmtMicrophoneRecorder udmtMicrophoneRecorder = new UdmtMicrophoneRecorder();
	private static String _VRMessage = "";
        private static int _VRMidiNote = 0;
 	private static int _VRSharpOrFlat = 0;	//-1 = user sang flat, +1 = user sang sharp
	private static int _VRRedGreenYellow = 0;	//-1 = red, 0 = green, 1 = yellow
	private static int _VRPitchScore = 0;	// -100 to +100 cents (estimated)
	private static String _VRMidiNoteName = "";

	private static int _VRNumValidSamples = 0;
	private static int _VRLowFreqFilter = 0;
	private static int _VRHighFreqFilter = 0;
			
	private static boolean waitingForRecordingToFinish = false;
	private static boolean waitingForPlaybackToFinish = false;

	private static boolean waitingToCalculateScore = false;


	private String udmtAppletName			=	"UdmtMicTest";
	private String udmtAppletVersion		=	"0.12"	;

	private Image imgAllSymbols;
	private Sequencer sequencer;
	private Sequence seq, progChgSequence;

	private MidiEvent 	event;
	private ShortMessage 	msg;

	private int playStatus=-1;
	private boolean suppressingMIDI = false;

	private Rectangle rectScreen;
	private Image imageScreen;

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

	private int prevExerciseIndex = 0;
    	private String[] ExerciseList = {
"C Major","Other Major Keys","Natural Minor","Harmonic Minor","Melodic Minor"
};

    	private String[] ExerciseMap = {
"MajorScalar","MajorArp","NaturalMinorScalar","HarmonicMinorScalar","MelodicMinorScalar"
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
	private int[] boxColor;

	private int numNotesToDisplay = 1;

	private Button checkButton, nextButton, playcorrButton, takeExamButton, showcorrButton;
	private Button shownotesButton, nextLessonButton, exitButton;
	private Button btnDo, btnRe, btnMi, btnFa, btnSo, btnLa, btnTi;
	private Button btnRa, btnMe, btnSe, btnLe, btnTe;
	private Button btnDi, btnRi, btnFi, btnSi, btnLi;
	private Button btnLeftArrow, btnRightArrow;
	private Choice imntCombo;
	//private Choice exerciseCombo;
	private Button btnPlayVoice;

	private Choice userSiteCombo;
	private int userSiteSel = -1;
	private Button btnUserLogin;
	private TextField txtUserId;
	private TextField txtUserPw;

	private Checkbox chkSaveCSV;

	private int userImnt;

	private static boolean examMode = false;

	//private PopupMenu popup;
	//private final int	MENU_HOR_OFFSET		=	18;
	//private final int	MENU_VER_OFFSET		=	179;

	private String strDrillMode			=	"Microphone Test";
	private String strExamMode			=	"Sight Singing Exam";
	private String strInstruct 			= 	"Start singing into your microphone, press Record, and hold the note for 2 seconds.";

	private String strConnectionStatus		=	"";
	private String strBottomTitle			=	"PLEASE LOGIN TO TAKE EXAM";

	private int showAbendScreen 			= 	0;
	private String txtAbend 			= 	"";

	private int	numStudentBoxes;
	private String[] 	studentAnswer;
	private int[]	studentSolfVal;
	private boolean[] studentCorrect;

	private int boxBoundL, boxBoundR, prevBoxBoundR;	// for calculating x coordinates of midpoints between 
	private int[] boxMidpt;				// SightSing input boxes.

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

	private Image imgPiano;
	private final int	PIANO_KBD_Y_OFFSET	=	275;

	private static boolean hintvisible = false;
	private static boolean thisIsTheLastQuestion = false;
	

	private String[] siteCodes;
	private String[] siteDescs;
	private String examSessionIdNum;

//------------------------------------------------------------------------------

	public void init()
	{
		// Display version and debugging info in Java Console
		//this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		//rectScreen = getBounds();
		rectScreen = new Rectangle(0,0,scrnWidth, scrnHeight);



		setLayout(null);

		checkButton = new Button("RECORD");
	      	checkButton.addActionListener(this);
		checkButton.setEnabled(true);
		checkButton.setBounds ( 75,650,100,30  );
		add (checkButton);

		chkSaveCSV = new Checkbox("Save");
		chkSaveCSV.setBounds ( 15,650,50,30  );
		add(chkSaveCSV);

		// 0.08 - made exit button always visible 
		exitButton = new Button("EXIT");
		exitButton.addActionListener(this);
	
		exitButton.setVisible(true);
		exitButton.setBounds ( 395,650,100,30 );
		add(exitButton);

		xBtn = lBtn;
		yBtn = staffY + 80;
				

		btnPlayVoice = new Button("PLAY RECORDED VOICE");
		btnPlayVoice.setEnabled(false);        
		btnPlayVoice.addActionListener(this);
		btnPlayVoice.setBounds (  185,650,200,30  );
		add(btnPlayVoice);


		addMouseListener(this);


		repaint();
	}

//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
// GRAPHICS ROUTINES
//------------------------------------------------------------------------------

	public void update( Graphics g ) 
	{
		//System.out.println("CALLING update");

		imageScreen = createImage (rectScreen.width, rectScreen.height);
		paint (imageScreen.getGraphics());
		g.drawImage (imageScreen, 0, 0, null);
        }
   
//------------------------------------------------------------------------------

	public void paint( Graphics g )
	{
		//System.out.println("CALLING paint");

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

			//System.out.println("W="+rectScreen.width+"H="+rectScreen.height);
			drawScreen (g);

			if (playSeqAfterDraw)
			{
				playSeqAfterDraw=false;
				startMidiPlayback();
			}
		}
		mainFrame.pack();
		mainFrame.show();
	}
//------------------------------------------------------------------------------

	public void drawAbendScreen ( Graphics g )
	{
		Font txtfont;
		txtfont = new Font ("Arial", Font.BOLD, 14);
		g.setFont (txtfont);

		g.drawString("THE FOLLOWING ERROR HAS OCCURRED:", 50, 150);
		g.drawString(txtAbend, 50, 200);
		
		g.drawString("Please open the Java Console for more information",50,250);								
		g.drawString("and contact systems support.",50,270);		
	}
//------------------------------------------------------------------------------

	private void drawScreen( Graphics g )
	{
		//System.out.println("CALLING drawScreen");
		Font headerfont, txtfont;

		g.setColor ( Color.white );
		g.fillRect(0,0,rectScreen.width,rectScreen.height);
		g.setColor ( Color.black );


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


		strFeedback = "";

		if (waitingToCalculateScore)
		{
			waitingToCalculateScore = false;

			_VRMessage = udmtMicrophoneRecorder.retMessage;
			_VRMidiNote = udmtMicrophoneRecorder.retMidiNote;
 			_VRSharpOrFlat = udmtMicrophoneRecorder.retSharpOrFlat;	//-1 = user sang flat, +1 = user sang sharp
			_VRRedGreenYellow = udmtMicrophoneRecorder.retRedGreenYellow;	//-1 = red, 0 = green, 1 = yellow
			_VRPitchScore = udmtMicrophoneRecorder.retPitchScore;	// -100 to +100 cents (estimated)
	    		_VRMidiNoteName = udmtMicrophoneRecorder.retMidiNoteName;

			_VRNumValidSamples = udmtMicrophoneRecorder.numValidSamples;
			_VRLowFreqFilter = udmtMicrophoneRecorder.lowFreqFilter;
			_VRHighFreqFilter = udmtMicrophoneRecorder.highFreqFilter;
			
			//public int[] validSamples = new int[88200];
			//public double[] fftmagnitude = new double[65536];

			//System.out.println(_VRMessage);

			//strFeedback = _VRMessage;

			String testerr = _VRMessage.substring(0,5);

			if ((_VRMidiNote >= 36) && (_VRMidiNote <= 84))
			{
				if (testerr.compareTo("ERROR") != 0)
				{
					strFeedback += "You sang "+_VRMidiNoteName;
	
					if (_VRSharpOrFlat == -1)
					{
						strFeedback += " (it was flat)";
					}
					if (_VRSharpOrFlat == +1)
					{
						strFeedback += " (it was sharp)";
					}

					if (_VRRedGreenYellow == -1)
					{
						strFeedback += " out of tune (approx. " + _VRPitchScore +" cents)";
					}
					if (_VRRedGreenYellow  == +1)
					{
						strFeedback += " a little out of tune (approx. " + _VRPitchScore +" cents)";
					}
					if (_VRRedGreenYellow  == 0)
					{
						strFeedback += " in tune (approx. " + _VRPitchScore +" cents)";
					}
				}
				else
				{
					strFeedback += "The computer was unable to determine the pitch you sang.  Please try again.";
				}
			}		
			else
			{
				strFeedback += "The computer was unable to determine the pitch you sang.  Please try again.";
			}



			//Determine if user has all green/yellow for every note
			boolean hasNonGreenYellow = false;
			for (int n=0 ; n < numCmdCharIx ; n++)
			{
				//System.out.println("boxcolor["+n+"]="+boxColor[n]);
				if (!((boxColor[n] == 2) || (boxColor[n] == 3)))
				{
					hasNonGreenYellow = true;
				}
			}
	

			g.setColor (Color.white);
			g.fillRect (10,70,570,520);

			g.setColor (Color.black);

			int xorigin = 54;
			int yorigin = 72+128;

			g.drawRect(10,70,570,260);
			g.drawLine(xorigin,yorigin-128,xorigin,yorigin+128);
			g.drawLine(xorigin,yorigin,xorigin+512,yorigin);

			int xfft = 54;
			int yfft = 72+260+256;

			g.drawRect(10,70+260,570,260);
			g.drawLine(xfft,yfft-256,xfft,yfft);
			g.drawLine(xfft,yfft,xfft+512,yfft);
	

			int[] samples = new int[512];
			int sampmax = -65536;
			int sampmin = 65536;

			if (_VRNumValidSamples > 0)
			{
				int xtick = 0;
				for (int i = 64 ; i < _VRNumValidSamples ; i+=172 )						{
					int ysamp = udmtMicrophoneRecorder.validSamples[i] ;
					samples[xtick] = ysamp;
					if (ysamp > sampmax) {sampmax = ysamp;}
					if (ysamp < sampmin) {sampmin = ysamp;}
					if (xtick < 511) {xtick++;}
				}
	
				int range = sampmax - sampmin;
				for (int x=0 ; x < 512; x++)
				{
					int scaledy = (int)((double)((samples[x]*128)/range));

					if (scaledy < -128) {scaledy = -128;}
					if (scaledy > 128) {scaledy = 128;}
					
					//System.out.println(x+","+samples[x]+","+scaledy);
	
					g.drawRect(xorigin+x,yorigin-scaledy,1,1);
	
				}
				//System.out.println("sampmin="+sampmin);
				//System.out.println("sampmax="+sampmax);
				//System.out.println("range="+range);

				int fftmax = 0;
				int fftsamp[] = new int[512];
				for (int i = 0 ; i <= 511 ; i++)
				{
					double a = udmtMicrophoneRecorder.fftmagnitude[_VRLowFreqFilter + i*3];
					double b = udmtMicrophoneRecorder.fftmagnitude[_VRLowFreqFilter + i*3+1];
					double c = udmtMicrophoneRecorder.fftmagnitude[_VRLowFreqFilter + i*3+2];
					int avg = (int)(a+b+c)/3;
					//System.out.println("a="+a+" b="+b+" c="+c+" avg="+avg);

					if (avg > fftmax) {fftmax = avg;}
					fftsamp[i] = avg;	
				}


				g.setColor(Color.red);
				g.fillRect(xfft,yfft-256,512,256);
				for (int j=0 ; j <= 44 ; j++)
				{
					//g.setColor(Color.red);
					//int frl = udmtMicrophoneRecorder.FlatRedLeft[j];
					//int frr = udmtMicrophoneRecorder.FlatRedRight[j];
					//int srl = udmtMicrophoneRecorder.SharpRedLeft[j];
					//int srr = udmtMicrophoneRecorder.SharpRedRight[j];

					//g.fillRect( xfft+frl/3,yfft-256,(frr/3)-(frl/3),256);
					//g.fillRect( xfft+srl/3,yfft-256,(srr/3)-(srl/3),256);

					g.setColor(Color.yellow);
					int fyl = udmtMicrophoneRecorder.FlatYellowLeft[j];
					int fyr = udmtMicrophoneRecorder.FlatYellowRight[j];
					int syl = udmtMicrophoneRecorder.SharpYellowLeft[j];
					int syr = udmtMicrophoneRecorder.SharpYellowRight[j];

					g.fillRect( xfft+fyl/3,yfft-256,(fyr/3)-(fyl/3),256);
					g.fillRect( xfft+syl/3,yfft-256,(syr/3)-(syl/3),256);

					int gl = fyr+1;
					int gr = syl-1;
					if (gr < gl) {gr = gl;}
					
					g.setColor(Color.green);
					g.fillRect( xfft+gl/3,yfft-256,(gr/3)-(gl/3),256);					

				}
				for (int x=0 ; x < 512; x++)
				{
					int scaledy=0;
					if (fftmax > 0)
					{
						scaledy = (int)((double)((fftsamp[x]*256)/fftmax));
					}
					if (scaledy > 256) {scaledy = 256;}
					
					//System.out.println(x+","+fftsamp[x]+","+scaledy);

					if ( ((_VRLowFreqFilter + x*3) <= udmtMicrophoneRecorder.ixFundamental )
					  && (udmtMicrophoneRecorder.ixFundamental <= (_VRLowFreqFilter + x*3+2)) )
					{
						g.setColor(Color.black);
						g.drawLine(xfft+x,yfft-scaledy,xfft+x,yfft);
						g.setColor(Color.blue);
						for (int z=0; z<=10 ; z++)
						{
							g.drawLine(xfft+x-z,yfft+z,xfft+x+z,yfft+z);
						}

					}
					else
					{
						g.setColor(Color.black);
						g.drawLine(xfft+x,yfft-scaledy,xfft+x,yfft);

					}
	
	
				}

			}
	
		}



		g.setColor (Color.black);


		nScore1 = currExerciseIndex;
		nScore2 = currNumExercises;	
		strScoreDisp = strScore1 + nScore1 + strScore2 + nScore2 ;

		//centerString (g, strScoreDisp, txtfont, 85);

		centerString (g, strFeedback, txtfont, 631);
		//centerString (g, strExamFeedback1, txtfont, 350);
		//centerString (g, strExamFeedback2, txtfont, 370);




		

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
			//System.out.println ("End of track event found");								
			stopMidiPlayback();			
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
				//this.showStatus("ERROR SETTING SEQUENCE");
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
	}

//------------------------------------------------------------------------------
	public void itemStateChanged (ItemEvent ie)
	{
		userImnt 	= imntCombo.getSelectedIndex();

		//REMOVED 6/24/06
		//Check if exercisetype was changed

		userSiteSel     = userSiteCombo.getSelectedIndex();

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

		//System.out.println("mousePressed: x="+xcoord+" y="+ycoord);


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
			cursorBoxNum=1;
		}
	} 
	public void cursorBack()
	{
		//System.out.println("CursorBack: cursorboxnum="+cursorBoxNum);
		cursorBoxNum--;
		if (cursorBoxNum < 1)
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



		if (e.getActionCommand().equals ( "RECORD" ) )
		{
			strFeedback = "Listening for 2 seconds...";
			repaint();

			//***INVOKE RECORDING LOGIC
			//System.out.println("Starting recording");
			udmtMicrophoneRecorder.savecsv = chkSaveCSV.getState();
			udmtMicrophoneRecorder.StartRecording();
			checkButton.setEnabled(false);
			
			btnPlayVoice.setEnabled(false);
			waitingForRecordingToFinish = true;

		}//check

		if (e.getActionCommand().equals ( "NEXT" ) )
		{
			//System.out.println("Next button pressed");
			//System.out.println("DEBUG: currExerciseIndex="+currExerciseIndex);
			//System.out.println("DEBUG: currNumExercises="+currNumExercises);
			//System.out.println("DEBUG: thisIsTheLastQuestion="+thisIsTheLastQuestion);

			stopMidiPlayback();

			if (examMode)
			{
				if (thisIsTheLastQuestion)
				{
					System.exit(0);
				}


				shuffledQuesIndex++;
				if (shuffledQuesIndex == num_exam_ques)
				{					
					shuffledQuesIndex = 0;

				}
			}
			else
			{
				strFeedback = "When recording: State your name, "+strDrillType+", Exercise #"+currExerciseIndex+", then begin singing.";
			}

			//System.out.println ("Original cmdstr="+currCmdStr);


			//transp.reset();
			//transp.setTranspKey (Math.abs(myKey), myKeySF ,majorMinor);
			//transp.setCmdStr (currCmdStr);
			//currCmdStr = transp.getTransposed();

			//System.out.println ("myKey="+myKey+" Transposed cmdstr="+currCmdStr);


			//checkButton.setEnabled(false);
			checkButton.setEnabled(true);
			
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
			cursorBoxNum = 1;
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

			for (int n=0 ; n < numCmdCharIx ; n++)
			{
				boxColor[n] = 0;
			}

			

			repaint();

		}

		if (e.getActionCommand().equals ( "PLAY NOTE" ) )
		{
			try 
			{

				seq = new Sequence(Sequence.PPQ, 4);         
				Track track = seq.createTrack();                        

				msg = new ShortMessage();
				msg.setMessage(0xC0, 1, userImnt, 0);

				event = new MidiEvent(msg, 0);
				track.add(event);

				msg = new ShortMessage();
				msg.setMessage(0x90, 1, midiNotes[cursorBoxNum - 1], 100);
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
			//Removed playback limit for sightsinging exam
			//if ((examMode) && (playCtr >= examPlayMax ))
			//{
			//	playcorrButton.setEnabled(false);
			//}
		}
		if (e.getActionCommand().equals ( "SHOW HINT" ) )
		{
			//System.out.println("Show Notes button pressed");
			numNotesToDisplay = numCmdCharIx;		// to display whole sequence
			shownotesButton.setLabel ("HIDE HINT");
			hintvisible = true;
			repaint();
		}
		if (e.getActionCommand().equals ( "HIDE HINT" ) )
		{
			//System.out.println("Show Notes button pressed");
			numNotesToDisplay = 1;		// to display whole sequence
			shownotesButton.setLabel ("SHOW HINT");
			hintvisible = false;
			repaint();
		}

		if (e.getActionCommand().equals ( "PLAY ALL" ) )
		{
			//7-5-2005: Resetting program change seq seems to fix bug
			userImnt = imntCombo.getSelectedIndex();
			
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



		if (e.getActionCommand().equals ( "PLAY RECORDED VOICE" ) )
		{
			//System.out.println("Starting playback");
			udmtMicrophoneRecorder.StartPlayback();
			btnPlayVoice.setEnabled(false);
			checkButton.setEnabled(false);
			waitingForPlaybackToFinish = true;

		}

		if (e.getActionCommand().equals ( "LOGIN" ) )
		{
		}


		if (e.getActionCommand().equals ( "TAKE EXAM" ) )
		{
			//05/02/2004: added for debugging:
			System.out.println("Take Exam button pressed");
			System.out.println("nextURL="+nextURL);
			//System.out.println("appletcontext="+this.getAppletContext());

			stopMidiPlayback();
			if (nextURL != null)
			{	
				//this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "NEXT LESSON" ) )
		{
			//System.out.println("Next Lesson button pressed");
			stopMidiPlayback();
			if (nextURL != null)
			{	
				//this.getAppletContext().showDocument(nextURL);
			}
		}
		if (e.getActionCommand().equals ( "EXIT" ) )
		{
			//System.out.println("Exit button pressed");
			boolean userWantsOut = false;
			stopMidiPlayback();
			if (examMode)
			{
			}
			else
			{
				userWantsOut = true;
			}

			//FOR APPLICATION:
			if (userWantsOut)
			{
            			System.exit(0);
			}
		}
	} // ActionPerformed
//------------------------------------------------------------------------------

    //Sample of running an applet as an application - from Java Cookbook p. 347


    public static void main(String arg[]) {
        mainFrame = new JFrame("University of Delaware - Music Basics Online - Sight Singing Practice");

	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //enableEvents(AWTEvent.WINDOW_EVENT_MASK);

	UdmtMicTestApp myApplet = new UdmtMicTestApp();

	myApplet.init();
	myApplet.start();	

	mainFrame.getContentPane().add(myApplet);
	scrnHeight += 150;
	mainFrame.setSize(scrnWidth, scrnHeight);
	mainFrame.setVisible(true);
	mainFrame.repaint();
	mainFrame.setState(Frame.ICONIFIED);
    }

//    public void processWindowEvent(WindowEvent event) {
//        if(event.getID() == WindowEvent.WINDOW_ACTIVATED) {
//            System.out.println("ACTIVATED");
//        } else if(event.getID() == WindowEvent.WINDOW_CLOSED) {
//            System.out.println("CLOSED");
//        } else if(event.getID() == WindowEvent.WINDOW_CLOSING) {
//            System.out.println("CLOSING");
//            System.exit(0);
//        } else if(event.getID() == WindowEvent.WINDOW_DEACTIVATED) {
//            System.out.println("DEACTIVATED");
//        } else if(event.getID() == WindowEvent.WINDOW_DEICONIFIED) {
//            System.out.println("DEICONIFIED");
//        } else if(event.getID() == WindowEvent.WINDOW_ICONIFIED) {
//            System.out.println("ICONIFIED");
//        } else if(event.getID() == WindowEvent.WINDOW_OPENED) {
//            System.out.println("OPENED");
//        }
//    }


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


		if (waitingForRecordingToFinish && (!udmtMicrophoneRecorder.Recording))
		{
			checkButton.setEnabled(true);
			waitingForRecordingToFinish = false;
			btnPlayVoice.setEnabled(true);
			waitingToCalculateScore = true;
			mainFrame.setState(Frame.ICONIFIED);
		}
		if (waitingForPlaybackToFinish && (!udmtMicrophoneRecorder.Playing))
		{
			waitingForPlaybackToFinish = false;
			btnPlayVoice.setEnabled(true);
			checkButton.setEnabled(true);
		}


		threadCount++;
		if (threadCount > 2)
		{
			mainFrame.setState(Frame.NORMAL);
		}

                _looper.sleep(500);		
            }
        } catch(InterruptedException e) {
            _running = false;
        }
    }

}

