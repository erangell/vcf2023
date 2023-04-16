import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.sound.midi.*;

//------------------------------------------------------------------------------
// UdmtNotationApi - Interprets command strings of notation rendering commands
// and returns either arrays of symbol positioning data or a MIDI sequence.
//
//	public String getVersion()			returns version number of API
//
//	public void initAPI()				initializes object
//
//	public void reset()				resets variables - call between multiple command strings
//
//	public void setCmdStr (String newCmdStr)	sets command string
//
//	public Sequence getSequence()			returns MIDI sequence
//
//	public int getNumSymIDsOnStaff()		number of symbols to draw
//
//	public int[] getSymIDsOnStaff()			indexes of symbol images
//
//	public int[] getStaffXPos()			x coords of symbols to draw
//
//	public int[] getStaffYPos()			y coords of symbols to draw
//
//	public int getNumBeamsOnStaff()			number of beams to draw
//
//	public int[] getBeamX1()			left x coord of beam
//
//	public int[] getBeamY1()			left y coord of beam
//
//	public int[] getBeamX2()			right x coord of beam
//
//	public int[] getBeamY2()			right y coord of beam
//
//	public int getNumStemExtsOnStaff()		number of stem extensions to draw
//
//	public int[] getStemExtX1()			left x coord of stem extension
//
//	public int[] getStemExtY1()			left y coord of stem extension
//
//	public int[] getStemExtX2()			right x coord of stem extension
//
//	public int[] getStemExtY2()			right y coord of stem extension
//
//	public int	getNumCmdCharIndexes()		number of command character indexes in array
//
//	public int[] getCmdCharIndexes()		array of indexes to notes in command char array
//
//	public int[] getNoteSymIdIndexes()		array of indexes to notes in symbol id array
//
//	public int	 getNumMidiNotes()		number of midi notes in sequence
//
//	public int[] getMidiNotes()			array of midi note numbers for sequence
//
//	public long[] getMidiTimes()			array of midi durations for sequence
//
//	public void setSequencePatch (int patch)	sets program change event at beginning of sequence
//------------------------------------------------------------------------------


public class UdmtNotationApi extends Object
{
	private String UdmtAppletVersion = "0.05";

	private Image imgAllSymbols;

	int muNoteheadFilled, muNoteheadUnfilled;
	int muTrebleClef, muAltoClef, muBassClef, muCommon, muLeger;
	int muWhole, muHalfD, muQuarterD, muEighthD, muSixteenthD, muThirtySecondD;
	int muStaff, muThickBar, muLeftRpt, muLeftDblBar, muRtDblBar, muBar, muRightRpt;
	int muHalfU, muQuarterU, muEighthU, muSixteenthU, muThirtySecondU;
	int muWholeR, muHalfR, muQuarterR, muEighthR, muSixteenthR, muThirtySecondR;
	int muDot, muDblDot, muSharp, muFlat, muNatural, muDblSharp, muDblFlat;
	int muNum0, muNum1, muNum2, muNum3, muNum4, muNum5, muNum6, muNum7, muNum8;
	int muNum9, muCut, muAccent, muMetronome;

	int staffX, staffY;

	int gClefXOffset = 0;
	int gClefYOffset = -17;
	int fClefXOffset = 3;
	int fClefYOffset = -15;

	int keySharpXOffset[] = {30,40,50,60,70,80,90};
	int keySharpYOffset[] = {-6,9,-11,4,18,0,13};
	int keyFlatXOffset[] = {30,40,50,60,70,80,90};
	int keyFlatYOffset[] = {13,0,18,4,22,9,27};

	int staffNoteYPos[] = {-5,0,5,8,13,17,21,26,31,35,40,44,49,53,58,63,68,73,78};
	int staffNoteXPos[] = {140,165,190,215,240,265,290,315
				,340,365,390,415,440,465,490,515};

	private Image 		imageScreen;

	private int 		showAbendScreen;
	private String 		txtAbend;

	private String 		parmCmdStr;
	private char[] 		arrCmdStr;
	private int 		lenCmdStr;
	private int 		cmdCharIndex;
	private int		numCmdCharIndex = 0;
	private int[] 		cmdCharIndexes;
	private int[] 		noteSymIdIndexes;


	private int		currStaffXOffset = 0;

	private int		numSymsOnStaff = 0;
	private int[] symsOnStaff;
	private int[] symIDsOnStaff;

	private	int[]		staffXpos;
	private int[]		staffYpos;

	private int		beamLevel = 0;
	private int		numBeamsOnStaff = 0;
	private	int[]		beamX1;
	private	int[]		beamY1;
	private	int[]		beamX2;
	private	int[]		beamY2;

	private int		numStemExtsOnStaff = 0;
	private int[]	stemExtX1;
	private int[]	stemExtY1;
	private int[]	stemExtX2;
	private int[]	stemExtY2;

	private int		numBeamPoints = 0;
	private	int[]		beamPointX;
	private	int[]		beamPointY;
	private	int[]		beamStemDir;		//saves stem direction for each point
	private	int[] 		yMainBeam;	// ycoord of stem on main beam for group

	private int 		beamStackPtr=0;		// for tracking nested beams
	private int[] 		beamStack;

	private int		innerBeamPtr=0;
	private int[] 		innerBeamLeft;
	private int[] 		innerBeamRight;
	private int[] 		innerBeamLevel;	

	private int		innerFlagPtr=0;
	private int[] 		innerFlagIndex;
	private int[] 		innerFlagDirection;
	private int[] 		innerFlagLevel;	
	private int		innerFlagWidth=10;


	private int		midiNoteCount = 0;
	private int[]	midiNotes;
	private long[]	midiTimes;

	private int 		mode = 0;
	final int 		MODE_NONE=0;
	final int 		MODE_CLEF=1;
	final int 		MODE_TIME=2;
	final int 		MODE_KEY=3;
	final int 		MODE_NOTE=4;
	final int 		MODE_OCT=5;
	final int 		MODE_REST=6;
	final int 		MODE_BAR =7;
	final int 		MODE_PLUS =8;
	final int 		MODE_MINUS =9;

	private char 		currClef=' ';
	private int 		currKeyNumSF = 0;
	private char 		currKeySF = 'S';
	private int 		currOctave = 3;		// Octaves run from C to B.  Octave 3 has Middle C.
	private int		tsigNum = 0;
	private int		tsigDen = 0;

	private int 		currStemDir = 0; // 0=based on staff position, 1=up, -1=down
	private char		savedNoteLetter = ' ';
	private int 		currAccid = 0; // value of current adjustment to base note (#=1, b=-1, x=2, bb=-2)


	private int wholeNoteYNudge[] = {0,0,-1,-1,-2,-1,0,0,0,0,-1,-1,-2,0,0,0,0,0,0};

	private int numDots=0;
	private int beamDivision=0;
	private int accid2draw=0;

	private int currDigit=-1;
	private int plusVal=0;
	private int minusVal=0;

	private char backslash = '\\';

	// Objects required for MIDI Playback:
	private Sequencer 	sequencer;
	private Sequence 	seq;
	private Track 		track;
	MidiEvent 		event;
	ShortMessage 		msg;

	private int playStatus = -1 ; 	// -1 = stopped, +1 = playing

	private long miditime = 0;	// used for tracking duration of sequence

	private double legatoPercent = 0.75;	// percentage of duration to keep the note on.

	// Duration values to use for MIDI sequencing:
	private int durWhole = 16, durHalf = 8, durQtr = 4, dur8th = 2, dur16th= 1, dur32nd = 1;


	private int kSharps[] = {0,0,0,0,0,0,0};	// adjustments for C-G when key signature changes
	private int kFlats[] = {0,0,0,0,0,0,0};
	private int mSharps[] = {0,0,0,0,0,0,0};	// adjustments for C-G for current measure
	private int mFlats[] = {0,0,0,0,0,0,0};

	private String parmsuppressMIDI;
	private boolean suppressingMIDI = false;

	private int seqPatch = 0;

//------------------------------------------------------------------------------
// THE FOLLOWING IS THE API FOR CALLING THIS PROGRAM AS AN OBJECT FROM OTHER JAVA PROGRAMS
//------------------------------------------------------------------------------
	public void UdmtNotationApi()
	{
	}

	public void initAPI()
	{
		//System.out.println("Constructor UdmtNotation called");

		// Allocate arrays
		symIDsOnStaff = new int[500];	// when called from API use index instead of object
		staffXpos = new int[500];
		staffYpos = new int[500];

		beamX1 = new int[100];
		beamX2 = new int[100];
		beamY1 = new int[100];
		beamY2 = new int[100];

		stemExtX1 = new int[100];
		stemExtX2 = new int[100];
		stemExtY1 = new int[100];
		stemExtY2 = new int[100];

		beamPointX = new int[50];
		beamPointY = new int[50];
		beamStemDir = new int[50];

		yMainBeam = new int[50];

		beamStack = new int[50];
		beamStack[0] = 0;

		innerBeamLeft= new int[50];
		innerBeamRight= new int[50];
		innerBeamLevel= new int[50];

		innerFlagIndex= new int[50];
		innerFlagDirection= new int[50];
		innerFlagLevel= new int[50];		

		cmdCharIndexes = new int[100];
		noteSymIdIndexes = new int[100];

		midiNotes = new int[100];
		midiTimes = new long[100];

		setUpSymbolNumbers();

		try	{
			seq = new Sequence(Sequence.PPQ, 4);         
			track = seq.createTrack();            
			//System.out.println("MIDI sequence created successfully.");
		}
		catch ( Exception e )	{
			System.out.println("ERROR: Exception when creating MIDI sequence:");
			e.printStackTrace();
			showAbendScreen = 1;
			txtAbend = "ERROR: Exception when creating MIDI sequence.  See Java Console";
		}
	}



	public void reset()
	{
		//System.out.println("Reset method called");

		beamStack[0] = 0;
		lenCmdStr=0;
		cmdCharIndex=0;
		numCmdCharIndex = 0;
		currStaffXOffset = 0;
		numSymsOnStaff = 0;
		beamLevel = 0;
		numBeamsOnStaff = 0;
		numStemExtsOnStaff = 0;
		numBeamPoints = 0;
		beamStackPtr=0;
		innerBeamPtr=0;
		innerFlagPtr=0;
		innerFlagWidth=10;
		midiNoteCount = 0;
		mode = 0;
		currClef=' ';
		currKeyNumSF = 0;
		currKeySF = 'S';
		currOctave = 3;	
		tsigNum = 0;
		tsigDen = 0;
		currStemDir = 0; 
		savedNoteLetter = ' ';
		currAccid = 0; 
		numDots=0;
		beamDivision=0;
		accid2draw=0;
		currDigit=-1;
		playStatus = -1 ; 	
		miditime = 0;	

		// added 4/16/04 to reset measure key signatures - was bleeding into subsequent questions
		for (int i=0; i < 7 ; i++)
		{
			mSharps[i] = 0;
			mFlats[i] = 0;
			kSharps[i] = 0;
			kFlats[i] = 0;

		}

		try	{
			track = null;
			seq = null;
			seq = new Sequence(Sequence.PPQ, 4);         
			track = seq.createTrack();            
			//System.out.println("MIDI sequence created successfully.");
		}
		catch ( Exception e )	{
			System.out.println("ERROR: Exception when creating MIDI sequence:");
			e.printStackTrace();
			showAbendScreen = 1;
			txtAbend = "ERROR: Exception when creating MIDI sequence.  See Java Console";
		}
	}

	private void setUpSymbolNumbers()
	{
		muNoteheadFilled = 0;
		muNoteheadUnfilled = 1;
		muTrebleClef = 2;
		muAltoClef = 3;
		muBassClef = 4;
		muCommon = 5;
		muLeger = 6;
		muWhole = 7;
		muHalfD = 8;
		muQuarterD = 9;
		muEighthD = 10;
		muSixteenthD = 11;
		muThirtySecondD = 12;

		muStaff = 13;
		muThickBar = 14;
		muLeftRpt = 15;
		muLeftDblBar = 16;
		muRtDblBar = 17;
		muBar = 18;
		muRightRpt = 19;

		muHalfU = 20;
		muQuarterU = 21;
		muEighthU = 22;
		muSixteenthU = 23;
		muThirtySecondU = 24;

		muWholeR = 25;
		muHalfR = 26;
		muQuarterR = 27;
		muEighthR = 28;
		muSixteenthR = 29;
		muThirtySecondR = 30;

		muDot = 31;
		muDblDot = 32;
		muSharp = 33;
		muFlat = 34;
		muNatural = 35;
		muDblSharp = 36;
		muDblFlat = 37;

		muNum0 = 38;
		muNum1 = 39;
		muNum2 = 40;
		muNum3 = 41;
		muNum4 = 42;
		muNum5 = 43;
		muNum6 = 44;
		muNum7 = 45;
		muNum8 = 46;
		muNum9 = 47;

		muCut = 48;
		muAccent = 49;
		muMetronome = 50;
	}



	public String getVersion()
	{
		return UdmtAppletVersion;
	}

	public Sequence getSequence()
	{
		//System.out.println("notation: returning sequence");
		return seq;
	}

	public int getNumSymIDsOnStaff()
	{
		return numSymsOnStaff;
	}
	public int[] getSymIDsOnStaff()
	{
		return symIDsOnStaff;
	}
	public int[] getStaffXPos()
	{
		return staffXpos;
	}
	public int[] getStaffYPos()
	{
		return staffYpos;
	}
	public int getNumBeamsOnStaff()
	{
		return numBeamsOnStaff; 
	}
	public int[] getBeamX1()
	{
		return beamX1;
	}
	public int[] getBeamY1()
	{
		return beamY1;
	}	
	public int[] getBeamX2()
	{
		return beamX2;
	}
	public int[] getBeamY2()
	{
		return beamY2;
	}	
	public int getNumStemExtsOnStaff()
	{
		return numStemExtsOnStaff; 
	}
	public int[] getStemExtX1()
	{
		return stemExtX1;
	}
	public int[] getStemExtY1()
	{
		return stemExtY1;
	}	
	public int[] getStemExtX2()
	{
		return stemExtX2;
	}
	public int[] getStemExtY2()
	{
		return stemExtY2;
	}	
	public int getNumCmdCharIndexes()		
	{
		return numCmdCharIndex;
	}
	public int[] getCmdCharIndexes()		
	{
		return cmdCharIndexes;
	}
	public int[] getNoteSymIdIndexes()		
	{
		return noteSymIdIndexes;
	}
	public int	 getNumMidiNotes()
	{
		return midiNoteCount;
	}
	public int[] getMidiNotes()		
	{
		return midiNotes;
	}
	public long[] getMidiTimes()		
	{
		return midiTimes;
	}

	public void setSequencePatch (int patch)
	{
		seqPatch = patch;
	}

	public void setCmdStr (String newCmdStr)
	{
		addProgChgEvent ( (long)0, 1, seqPatch );
		//System.out.println("adding pgmchg event:"+ seqPatch);

		parmCmdStr = newCmdStr;
		arrCmdStr = parmCmdStr.toCharArray();
		lenCmdStr = parmCmdStr.length();

		for (int i=0 ; i<lenCmdStr ; i++)
		{
			//System.out.println("Pos:"+i+" Char:"+arrCmdStr[i]);
			cmdCharIndex = i;
			processCmdChar (arrCmdStr[i]);
		}
	}


//----------------------------------------------------------------------------

	private void processCmdChar ( char charIn )
	{

		char c;

		//System.out.println ("Processing Character: "+c);

		//Convert to Uppercase 
		c = Character.toUpperCase(charIn);
		
		if (mode == MODE_NONE)	{
			//System.out.println("MODE_NONE");

			if (c == backslash)
			{
				//System.out.println(backslash + "Left Flag");
				if (beamLevel > 0)
				{
					innerFlagPtr++;
					innerFlagIndex[innerFlagPtr] = numBeamPoints;
					innerFlagLevel[innerFlagPtr] = beamLevel ;
					innerFlagDirection[innerFlagPtr] = -1;
				}
			}
			else
			{
			  switch (c)	{
				case 'L': {
					//System.out.println("Mode changed to: MODE_CLEF");
					mode = MODE_CLEF;
					break;
				}
				case 'T': {
					//System.out.println("Mode changed to: MODE_TIME");
					mode = MODE_TIME;
					tsigNum = 0;
					tsigDen = 0;
					break;
				}
				case 'K': {
					//System.out.println("Mode changed to: MODE_KEY");
					mode = MODE_KEY;
					break;
				}
				case 'C': case 'D': case 'E': case 'F': case 'G': case 'A': case 'B': {
					//System.out.println("Mode changed to: MODE_NOTE");
					mode = MODE_NOTE;
					savedNoteLetter = c;
					break;
				}
				case 'O': {
					//System.out.println("Mode changed to: MODE_OCT");
					mode = MODE_OCT;
					break;
				}
				case 'R': {
					//System.out.println("Mode changed to: MODE_REST");
					mode = MODE_REST;
					break;
				}
				case '|': {
					//System.out.println("Mode changed to: MODE_BAR");
					mode = MODE_BAR;
					break;
				}
				case '+': {
					//System.out.println("Mode changed to: MODE_PLUS");
					mode = MODE_PLUS;
					plusVal=0;
					break;
				}
				case '-': {
					//System.out.println("Mode changed to: MODE_MINUS");
					mode = MODE_MINUS;
					minusVal=0;
					break;
				}
				case '>': {
					//System.out.println("Stem direction changed to UP");
					currStemDir = +1;
					break;
				}
				case '<': {
					//System.out.println("Stem direction changed to DOWN");
					currStemDir = -1;
					break;
				}
				case '[': {
					//System.out.println("[Beam Begin");
					beamLevel++;
					if (beamLevel >= 3)
					{
						beamLevel = 3;
					}
					if (beamLevel == 0)
					{
						numBeamPoints = 0;
					}
					else
					{
						beamStackPtr++;
						beamStack[beamStackPtr] = numBeamPoints;
					}
					//System.out.println ("beamStackPtr="+beamStackPtr+" value="+beamStack[beamStackPtr]);
					break;
				}
				case ']': {
					//System.out.println("]Beam End");
					if (beamLevel > 0)
					{
						innerBeamPtr++;
						innerBeamLeft[innerBeamPtr] = beamStack[beamStackPtr];
						innerBeamRight[innerBeamPtr] = numBeamPoints-1;
						innerBeamLevel[innerBeamPtr] = beamLevel - 1;
						
						//System.out.println ("innerBeamPtr = "+innerBeamPtr);
						//System.out.println ("Left: "+innerBeamLeft[innerBeamPtr]);
						//System.out.println ("Right: "+innerBeamRight[innerBeamPtr]);
						//System.out.println ("Level: "+innerBeamLevel[innerBeamPtr]);

						beamStackPtr--;
					}

					beamLevel--;
					if (beamLevel <= 0)
					{
						beamLevel = 0;
					}
					if (beamLevel == 0)
					{
						processBeams();
						numBeamPoints = 0;
						beamStackPtr = 0;
					}
					break;
				}

				case '/': { // right pointing flag within beam

					//System.out.println("/Right Flag");

					if (beamLevel > 0)
					{
						innerFlagPtr++;
						innerFlagIndex[innerFlagPtr] = numBeamPoints;
						innerFlagLevel[innerFlagPtr] = beamLevel ;
						innerFlagDirection[innerFlagPtr] = +1;
					}
					break;
				}
				case ' ': {
					// Ignore whitespace
					break;
				}
				case 'X': {
					// Dummy command - used if last command on line doesn't render
					break;
				}
				default : {
					System.out.println("ERROR: Unsupported command character: "+c);
					break;
				}
			  } // switch
			} // not backslash
		}
		else if (mode == MODE_CLEF) {
			//System.out.println("MODE_CLEF");
			modeClef (c);
		}
		else if (mode == MODE_TIME) {
			//System.out.println("MODE_TIME");
			modeTime (c);
		}
		else if (mode == MODE_KEY) {
			//System.out.println("MODE_KEY");
			modeKey (c);
		}
		else if (mode == MODE_NOTE) {
			//System.out.println("MODE_NOTE");
			modeNote (c);
		}
		else if (mode == MODE_OCT) {
			//System.out.println("MODE_OCT");
			modeOct (c);
		}
		else if (mode == MODE_REST) {
			//System.out.println("MODE_REST");
			modeRest (c);
		}
		else if (mode == MODE_BAR) {
			//System.out.println("MODE_BAR");
			modeBar (c);
		}
		else if (mode == MODE_PLUS) {
			//System.out.println("MODE_PLUS");
			modePlus (c);
		}
		else if (mode == MODE_MINUS) {
			//System.out.println("MODE_MINUS");
			modeMinus (c);
		}


	} // processCmdChar


	private void modeClef ( char c)
	{
		if (c == 'G')
		{
			//System.out.println("gClefXOffset="+gClefXOffset);
			add2staff (muTrebleClef, gClefXOffset, gClefYOffset, 25 );
			currClef=c;
			mode = MODE_NONE;
		}
		else if (c == 'F')
		{
			add2staff (muBassClef, fClefXOffset, fClefYOffset, 28);
			currClef = c;
			mode = MODE_NONE;
		}
		else if (c == ' ') {
			mode = MODE_NONE;
		}
		else
		{
			System.out.println ("ERROR: Unsupported Clef Character: "+c);
			mode = MODE_NONE;
		}

	}

	private void modeTime ( char c)
	{

		if ((tsigNum == 0) && (tsigDen == 0))
		{
			switch (c)
			{
				case '2': {  tsigNum = 2; break;  }
				case '3': {  tsigNum = 3; break;  }
				case '4': {  tsigNum = 4; break;  }
				case '6': {  tsigNum = 6; break;  }
			}
		}
		else if (tsigDen == 0)
		{
			switch (c)
			{
				case '4': {  
					tsigDen = 4; 
					renderTimeSig (tsigNum, tsigDen);
					break;  
				}
				case '8': {  
					tsigDen = 8; 
					renderTimeSig (tsigNum, tsigDen);
					break;  
				}
				case 'C': { 
					tsigDen = 2; 
					renderTimeSig (tsigNum, tsigDen);
					break; 
				}
			}
			
		}
		else if (c == '/')	{
			// no logic needed for now because only supporting 1 digit numerator/denominator
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
		else	{
			System.out.println ("ERROR: Unsupported TimeSig Character: "+c);
			mode = MODE_NONE;
		}
	}

	private void renderTimeSig (int num, int den)
	{
		//System.out.println ("renderTimeSig: num="+num+" den="+den);

		if (den == 2) // common or cut time
		{
			switch (num)
			{
				case 2: {  
					add2staff (muCut, 0, 4, 20);
					break;
				}
				case 4: {  
					add2staff (muCommon, 0, 6, 20);
					break;
				}
			}
		}
		else 
		{
			switch (num)
			{
				case 2: {  
					add2staff (muNum2, 0, 0, 0);
					break;
				}
				case 3: {  
					add2staff (muNum3, 0, 0, 0);			
					break;
				}
				case 4: {  
					add2staff (muNum4, 0, 0, 0);					
					break;
				}
				case 6: {  
					add2staff (muNum6, 0, 0, 0);			
					break;
				}
			}	
			switch (den)
			{
				case 4: { 
					add2staff (muNum4, 0, 18, 25);			
					break;
				}

				case 8: { 
					add2staff (muNum8, 0, 18, 25);			
					break;
				}
			}
		}
	}

	private void modeKey ( char c)
	{
		if ((c >= '0') && (c <= '7'))	{
			switch (c)
			{
				case '0': {  currKeyNumSF = 0; break;  }
				case '1': {  currKeyNumSF = 1; break;  }
				case '2': {  currKeyNumSF = 2; break;  }
				case '3': {  currKeyNumSF = 3; break;  }
				case '4': {  currKeyNumSF = 4; break;  }
				case '5': {  currKeyNumSF = 5; break;  }
				case '6': {  currKeyNumSF = 6; break;  }
				case '7': {  currKeyNumSF = 7; break;  }
			}
		}
		else if (c == 'S')	{
			currKeySF = c;
			renderKeySig (currKeySF, currKeyNumSF);

			// Setup keysignature adjustment arrays for MIDI
			if (currKeyNumSF >= 1)	{	// F#
				kSharps[3] = +1;
			}
			if (currKeyNumSF >= 2)	{	// C#
				kSharps[0] = +1;
			}
			if (currKeyNumSF >= 3)	{	// G#
				kSharps[4] = +1;
			}
			if (currKeyNumSF >= 4)	{	// D#
				kSharps[1] = +1;
			}
			if (currKeyNumSF >= 5)	{	// A#
				kSharps[5] = +1;
			}
			if (currKeyNumSF >= 6)	{	// E#
				kSharps[2] = +1;
			}
			if (currKeyNumSF >= 7)	{	// B#
				kSharps[6] = +1;
			}

			for (int i=0; i < 7 ; i++)
			{
				mSharps[i] = kSharps[i];
			}

			mode = MODE_NONE;
		}
		else if (c == 'F')	{
			currKeySF = c;
			renderKeySig (currKeySF, currKeyNumSF);

			// Setup keysignature adjustment arrays for MIDI
			if (currKeyNumSF >= 1)	{	// Bb
				kFlats[6] = -1;
			}
			if (currKeyNumSF >= 2)	{	// Eb
				kFlats[2] = -1;
			}
			if (currKeyNumSF >= 3)	{	// Ab
				kFlats[5] = -1;
			}
			if (currKeyNumSF >= 4)	{	// Db
				kFlats[1] = -1;
			}
			if (currKeyNumSF >= 5)	{	// Gb
				kFlats[4] = -1;
			}
			if (currKeyNumSF >= 6)	{	// Cb
				kFlats[0] = -1;
			}
			if (currKeyNumSF >= 7)	{	// Fb
				kFlats[3] = -1;
			}

			for (int i=0; i < 7 ; i++)
			{
				mFlats[i] = kFlats[i];
			}

			mode = MODE_NONE;
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
		else	{
			System.out.println ("ERROR: Unsupported KeySig Character: "+c);
			mode = MODE_NONE;
		}
	}

	private void renderKeySig (char SorF, int n)
	{
		int i;

		if (n!=0)
		{
			if (currClef == 'G')
			{
				if (SorF == 'S')
				{
					// Sharps in key signature for treble clef
					for ( i=0 ; i<=(n-1) ; i++)
					{
						add2staff (muSharp
						, 0 
						, keySharpYOffset[i]-20
						, 9 );
					}
				}
				else if (SorF == 'F')
				{	
					// Flats in key signature for treble clef
					for ( i=0 ; i<=(n-1) ; i++)
					{
						add2staff (muFlat
						, 0 
						, keyFlatYOffset[i]-20
						, 9 );
					}
				} // flat
			} // g clef
			else if (currClef == 'F')
			{
				if (SorF == 'S')
				{
					// Sharps in key signature for bass clef
					for ( i=0 ; i<=(n-1) ; i++)
					{
						add2staff (muSharp
						,  0
						, keySharpYOffset[i] +9 -20
						, 9 );
					}
				}
				else if (SorF == 'F')
				{	
					// Flats in key signature for bass clef
					for ( i=0 ; i<=(n-1) ; i++)
					{
						add2staff (muFlat
						,  0
						, keyFlatYOffset[i] + 9 -20
						, 9 );
					}
				} // flat
			} // f clef
			nudge(8);
		} // n!=0		
	} //renderkeysig

	private void modeNote ( char c)
	{
		int ypos;
		
		if (c == '1') {
			addToSeq (currOctave, savedNoteLetter, accid2draw, durWhole, numDots );
			currStemDir = +1;
			ypos = calcStaffYPos (savedNoteLetter, currOctave);
			addNoteToStaff (muWhole, muWhole, 1, 30 + wholeNoteYNudge[ypos], -1);
			currStemDir = 0;
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durHalf, numDots );
			addNoteToStaff (muHalfU, muHalfD, 0, 0, -1);
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durQtr, numDots );
			addNoteToStaff (muQuarterU, muQuarterD, 2, 0, 0);
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur8th, numDots );
			addNoteToStaff (muEighthU, muEighthD, 2, 0, 0);
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur16th, numDots );
			addNoteToStaff (muSixteenthU, muSixteenthD, 2, 0, 0);
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur32nd, numDots );
			addNoteToStaff (muThirtySecondU, muThirtySecondD, 2, 0, 0);
			mode = MODE_NONE;
		}
		else if (c == '#')	{
			accid2draw=+1;
		}
		else if (c == '&')	{
			accid2draw=-1;
		}
		else if (c == '%')	{
			accid2draw=+10;
		}
		else if (c == '*')	{
			accid2draw=+2;
		}
		else if (c == '@')	{
			accid2draw=-2;
		}
		else if (c == '.')	{
			numDots = 1;
		}
		else if (c == ':')	{
			numDots = 2;
		}
		else if (c == '~')	{
			beamDivision=3;
		}
		else if (c == '^')	{
		}
		else if (c == '!')	{
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}

	}

	private void addNoteToStaff ( int muShapeU, int muShapeD
						, int xNudge, int yNudge, int lowerLegerNudge)
	{
		int ypos, yadjust = 0;
		int yShift = 0;
		int extraNudge = 0;
		int endOfStemX, endOfStemY;

		if (muShapeU == muWhole)
		{
			yShift = -29;
		}
		else
		{
			yShift = 0;
		}


		if (beamLevel > 0)
		{
			muShapeU = muQuarterU;
			muShapeD = muQuarterD;
		}

		ypos = calcStaffYPos (savedNoteLetter, currOctave);
			
		// calculate correct default stem direction
		if (currStemDir == 0)	{
			if (ypos >= 9) {
				currStemDir = +1;
			}
			else	{
				currStemDir = -1;
			}
		}

		//accidentals
		if (currStemDir == +1)	{
			yadjust = staffNoteYPos[ypos] - 49;

			if (accid2draw == +1)	{
				add2staff ( muSharp, 0 + xNudge, yadjust + yNudge + 11 + yShift, 15);
				accid2draw = 0;
			}
			else if (accid2draw == +2)	{
				add2staff ( muDblSharp, 0 + xNudge, yadjust + yNudge + 11 + yShift, 15);
				accid2draw = 0;
			}
			else if (accid2draw == -1)	{
				add2staff ( muFlat, 0 + xNudge, yadjust + yNudge + 11 + yShift, 15);	
				accid2draw = 0;
			}
			else if (accid2draw == -2)	{
				add2staff ( muDblFlat, 0 + xNudge, yadjust + yNudge + 11 + yShift , 19);	
				accid2draw = 0;
			}
			else if (accid2draw == +10)	{
				add2staff ( muNatural, 0 + xNudge, yadjust + yNudge+ 11 + yShift, 15);	
				accid2draw = 0;
			}
		}
		else if (currStemDir == -1)	{
			yadjust = staffNoteYPos[ypos] - 20;
			
			if (accid2draw == +1)	{
				add2staff ( muSharp, 0 + xNudge, yadjust + yNudge -18, 15);
				accid2draw = 0;
			}
			else if (accid2draw == +2)	{
				add2staff ( muDblSharp, 0 + xNudge, yadjust + yNudge -18, 15);
				accid2draw = 0;
			}
			else if (accid2draw == -1)	{
				add2staff ( muFlat, 0 + xNudge, yadjust + yNudge -18, 15);	
				accid2draw = 0;
			}
			else if (accid2draw == -2)	{
				add2staff ( muDblFlat, -2 + xNudge, yadjust + yNudge -18, 15);	
				accid2draw = 0;
			}
			else if (accid2draw == +10)	{
				add2staff ( muNatural, 0 + xNudge, yadjust + yNudge -18, 15);	
				accid2draw = 0;
			}

		} //stem down


		// draw leger lines

		if (ypos==0)
		{
			add2staff (muLeger, xNudge-36 +25 , -50+ staffNoteYPos[0], 0);

		}
		if (ypos <= 2)
		{
			add2staff (muLeger, xNudge-36 +25 , -50+ staffNoteYPos[2], 1);
		}		
		if (ypos==18)
		{
			add2staff (muLeger, xNudge-35 +25  + lowerLegerNudge  , -50+ staffNoteYPos[18], 0);
		}
		if (ypos >= 16)
		{
			add2staff (muLeger, xNudge-35  +25 + lowerLegerNudge  , -50+ staffNoteYPos[16], 0);
		}
		if (ypos >= 14)
		{
			add2staff (muLeger, xNudge-35 +25  + lowerLegerNudge , -50+ staffNoteYPos[14], 1);
		}


		// draw note shape

		if (currStemDir == +1)	{
			yadjust = staffNoteYPos[ypos] - 49;
			add2staff ( muShapeU, 0 + xNudge, yadjust + yNudge, 25);

			endOfStemX = currStaffXOffset + 8;
			endOfStemY = yadjust + yNudge + 34;

			//test
			if (beamLevel > 0)
			{
				beamPointX [numBeamPoints] = endOfStemX ;
				beamPointY [numBeamPoints] = endOfStemY + (staffY - 28) ; 
				//System.out.println("debug: endOfStemY ="+ endOfStemY + " staffy="+staffY);
				beamStemDir [numBeamPoints] = currStemDir ; 
				numBeamPoints++;
			}

		}
		else if (currStemDir == -1)	{
			yadjust = staffNoteYPos[ypos] - 20;
			add2staff ( muShapeD, 0 + xNudge, yadjust + yNudge, 25);

			endOfStemX = currStaffXOffset - 2;
			endOfStemY = yadjust + yNudge + 69;

			//test
			if (beamLevel > 0)
			{
				beamPointX [numBeamPoints] = endOfStemX - 1;
 				beamPointY [numBeamPoints] = endOfStemY + (staffY - 28) - 1; 
				//System.out.println("debug: endOfStemY ="+ endOfStemY + " staffy="+staffY);

				beamStemDir [numBeamPoints] = currStemDir ; 
				numBeamPoints++;
			}
		}


		// save indexes of notes in array so other programs can draw one note at a time.
		cmdCharIndexes[numCmdCharIndex]=cmdCharIndex;
		noteSymIdIndexes[numCmdCharIndex]= numSymsOnStaff - 1 ;
		numCmdCharIndex++;




		// process dots 
		if (numDots > 0)
		{
			//System.out.println ("Drawing dots:" + numDots);

			// nudge dots up if ypos even (for notes drawn on a line)
			if ((ypos % 2) == 0)
			{
				extraNudge = -2;
			}
			else
			{
				extraNudge =0;
			}

			if (currStemDir == +1)	{
				yadjust = staffNoteYPos[ypos] - 20 + yShift + extraNudge ;
				if (numDots == 1) {
					add2staff ( muDot, 0 + xNudge - 10, yadjust + yNudge + extraNudge , 2);
				}
				else if (numDots == 2) {
					add2staff ( muDblDot, 0 + xNudge - 10, yadjust + yNudge + extraNudge , 7);
				}
			}
			else if (currStemDir == -1)	{
				yadjust = staffNoteYPos[ypos] - 20 + yShift;
				if (numDots == 1) {
					add2staff ( muDot, 0 + xNudge - 10, yadjust + yNudge + extraNudge , 2);
				}
				else if (numDots == 2) {
					add2staff ( muDblDot, 0 + xNudge - 10, yadjust + yNudge + extraNudge , 7);
				}
			} // stem down
			numDots = 0;
		} // dots

		currStemDir = 0; 

	} // addNoteToStaff

	private int calcStaffYPos (char savedNoteLetter, int currOctave)
	{
		// Calculate index to StaffNoteYPos array (0-16) based on note letter and octave.
		// if note/octave out of staff range, return either 0 or 16

		//First convert the savedNoteLetter to a numeric offset value: C=0, B=6
		int staffOctOffset = 0;

		int calcpos = 0;	// calculated position to be returned

		switch (savedNoteLetter)
		{
			case 'C':	{
				staffOctOffset = 0;
				break;
			}
			case 'D':	{
				staffOctOffset = 1;
				break;
			}
			case 'E':	{
				staffOctOffset = 2;
				break;
			}
			case 'F':	{
				staffOctOffset = 3;
				break;
			}
			case 'G':	{
				staffOctOffset = 4;
				break;
			}
			case 'A':	{
				staffOctOffset = 5;
				break;
			}
			case 'B':	{
				staffOctOffset = 6;
				break;
			}
		}

		if (currClef == 'G')
		{
			if (currOctave == 5 )
			{
				if (staffOctOffset > 0)
				{
					System.out.println ("Invalid octave 5 note for Treble clef in calcStaffYPos:" +staffOctOffset );
				}
				calcpos = 0;
			}
			else if (currOctave < 2)
			{
				System.out.println ("Invalid currOctave value for Treble clef in calcStaffYPos:"+ currOctave );
				calcpos = 16;
			}
			else if (currOctave == 2 )
			{
				if (staffOctOffset < 3)
				{
					System.out.println ("Invalid octave 2 note for Treble clef in calcStaffYPos:"+ staffOctOffset );
					calcpos = 18;
				}
				else if (staffOctOffset == 3)
				{
					calcpos = 18;
				}
				else if (staffOctOffset == 4)
				{
					calcpos = 17;
				}
				else if (staffOctOffset == 5)
				{
					calcpos = 16;
				}
				else if (staffOctOffset == 6)
				{
					calcpos = 15;
				}
			}
			else
			{
				calcpos = 14 - ((currOctave - 3) * 7) - staffOctOffset ;
			}
		}
		else if (currClef == 'F')
		{
			//System.out.println ("Debug: Bass Clef - Curroctave=" +currOctave +"staffOctOffset="+staffOctOffset );

			if (currOctave > 3)
			{
				System.out.println ("Invalid currOctave value for Bass clef in calcStaffYPos:"+ currOctave );
				calcpos = 0;
			}
			else if (currOctave == 3)
			{
				if (staffOctOffset > 2)
				{
					System.out.println ("Invalid octave 3 note for Bass clef in calcStaffYPos:"+staffOctOffset );
					calcpos = 0;
				}
				else
				{			
					calcpos = 16 - (7 * (currOctave-1)) - staffOctOffset;				
					//System.out.println ("calcpos: "+ calcpos );
				}
			}
			else
			{
				calcpos = 16 - (7 * (currOctave-1)) - staffOctOffset;
				//System.out.println ("calcpos: "+ calcpos );
			}
		}
		else
		{
			System.out.println ("Invalid currClef value in calcStaffYPos:"+currClef);
			calcpos = 0;
		}

		return calcpos;
	}


	private void modeOct ( char c)
	{
		if ((c >= '1') && (c <= '5'))	{
			switch (c)
			{
				case '1': {  currOctave = 1; break;  }
				case '2': {  currOctave = 2; break;  }
				case '3': {  currOctave = 3; break;  }
				case '4': {  currOctave = 4; break;  }
				case '5': {  currOctave = 5; break;  }
			}
		}
		else if (c == '+')	{
			currOctave++;
			if (currOctave >= 5)	{
				currOctave = 5;
			}
		}
		else if (c == '-')	{
			currOctave--;
			if (currOctave <= 1)	{
				currOctave = 1;
			}
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
	}

	private void modeRest ( char c)
	{
		if (c == '1') {
			addRestToSeq (durWhole, numDots );
			add2staff (muWholeR, 0, -7, 20);
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			addRestToSeq (durHalf, numDots );
			add2staff (muHalfR, 0, -7, 20);
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			addRestToSeq (durQtr, numDots );
			add2staff (muQuarterR, 0, -7, 20);
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			addRestToSeq (dur8th, numDots );
			add2staff (muEighthR, 0, -7, 20);
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			addRestToSeq (dur16th, numDots );
			add2staff (muSixteenthR, 0, -7, 20);
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			addRestToSeq (dur32nd, numDots );
			add2staff (muThirtySecondR, 0, -7, 25);
			mode = MODE_NONE;
		}
		else if (c == '.')	{
			numDots = 1;
		}
		else if (c == ':')	{
			numDots = 2;
		}
		else if (c == '~')	{
			beamDivision=3;
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
	}

	private void modeBar ( char c)
	{

		switch ( c )
		{
			case '|' : {
				add2staff ( muBar, 0, 0, 5);
				add2staff ( muBar, 0, 0, 10);
				mode = MODE_NONE;
				break; 
			}
			case '[' : {
				add2staff ( muLeftDblBar, 0, 0, 15);
				mode = MODE_NONE;
				break; 
			}
			case ']' : {
				add2staff ( muRtDblBar, 0, 0, 15);
				mode = MODE_NONE;
				break; 
			}
			case 'L' : {
				add2staff ( muLeftRpt, -5, 0, 20);
				mode = MODE_NONE;
				break; 
			}
			case 'R' : {
				add2staff ( muRightRpt, -10, 0, 15);
				mode = MODE_NONE;
				break; 
			}
			case 'T' : {
				add2staff ( muThickBar, 0, 0, 15);
				mode = MODE_NONE;

				break; 
			}

			default : {
				add2staff ( muBar, 0, 0, 10);
				mode = MODE_NONE;
				break; 
			}
		} // switch
	
		for (int i=0; i < 7 ; i++)
		{
			mSharps[i] = kSharps[i];
			mFlats[i] = kFlats[i];
		}
	}

	private void modePlus ( char c)
	{
		currDigit = -1;
		switch ( c )
		{
			case '0': {  currDigit = 0; break;  }
			case '1': {  currDigit = 1; break;  }
			case '2': {  currDigit = 2; break;  }
			case '3': {  currDigit = 3; break;  }
			case '4': {  currDigit = 4; break;  }
			case '5': {  currDigit = 5; break;  }
			case '6': {  currDigit = 6; break;  }
			case '7': {  currDigit = 7; break;  }
			case '8': {  currDigit = 8; break;  }
			case '9': {  currDigit = 9; break;  }
			case ' ':  {  
				mode = MODE_NONE;
				currStaffXOffset += plusVal ;
				//System.out.println("plusval="+plusVal);
				break; 
			}
		}
		if (currDigit > -1)
		{
			plusVal *= 10;
			plusVal += currDigit;
		}
	}

	private void modeMinus ( char c)
	{
		currDigit = -1;
		switch ( c )
		{
			case '0': {  currDigit = 0; break;  }
			case '1': {  currDigit = 1; break;  }
			case '2': {  currDigit = 2; break;  }
			case '3': {  currDigit = 3; break;  }
			case '4': {  currDigit = 4; break;  }
			case '5': {  currDigit = 5; break;  }
			case '6': {  currDigit = 6; break;  }
			case '7': {  currDigit = 7; break;  }
			case '8': {  currDigit = 8; break;  }
			case '9': {  currDigit = 9; break;  }
			case ' ':  {  
				mode = MODE_NONE;
				currStaffXOffset -= minusVal ;
				break; 
			}
		}
		if (currDigit > -1)
		{
			minusVal *= 10;
			minusVal += currDigit;
		}
	}


	private void add2staff ( int newMuSym, int adjustX, int adjustY, int spaceX )
	{

		symIDsOnStaff[numSymsOnStaff] = newMuSym;
		staffXpos[numSymsOnStaff] = staffX + currStaffXOffset + adjustX;  
		staffYpos[numSymsOnStaff] = staffY + adjustY;

		//System.out.println ("Added symbol to staff:");
		//System.out.println ("X = "+staffXpos[numSymsOnStaff]+" Y= "+staffYpos[numSymsOnStaff]);

		numSymsOnStaff++;
		
		if (numSymsOnStaff >= 500)
		{
			// Arrays full
			showAbendScreen = 1;
			txtAbend = "Maximum 500 symbols may be drawn on staff";
		}

		currStaffXOffset += spaceX ;

		//System.out.println ("new X = "+currStaffXOffset);
		
	}

	private void addBeam ( int X1, int Y1, int X2, int Y2 )
	{

		//System.out.println ("addBeam: "+X1+","+Y1+" thru "+X2+","+Y2);

		beamX1[numBeamsOnStaff] = X1;
		beamY1[numBeamsOnStaff] = Y1;
		beamX2[numBeamsOnStaff] = X2;
		beamY2[numBeamsOnStaff] = Y2;

		numBeamsOnStaff++;
		if (numBeamsOnStaff >= 100)
		{
			// Arrays full
			showAbendScreen = 1;
			txtAbend = "Maximum 100 beams may be drawn on staff";
		}

	}


	private void addStemExt ( int X1, int Y1, int X2, int Y2 )
	{
		stemExtX1[numStemExtsOnStaff] = X1;
		stemExtY1[numStemExtsOnStaff] = Y1;
		stemExtX2[numStemExtsOnStaff] = X2;
		stemExtY2[numStemExtsOnStaff] = Y2;

		numStemExtsOnStaff++;
		if (numStemExtsOnStaff >= 100)
		{
			// Arrays full
			showAbendScreen = 1;
			txtAbend = "Maximum 100 stem extensions may be drawn on staff";
		}
	}


	private void nudge (int spaceX)
	{
		currStaffXOffset += spaceX ;
	}

	private void processBeams()
	{
		int x=0, y=0, maxY=0, minY=0, maxX=0, minX=0, maxIX=0, minIX=0;
		//int pmaxY=0, pminY=0, pmaxX=0, pminX=0, pmaxIX=0, pminIX=0;
		//int higherIX=0, lowerIX=0;

		int mainBeamLeftX, mainBeamLeftY, mainBeamRightX, mainBeamRightY;

		int deltaY, deltaX;
		double slope;
		double maxSlope = 0.1;
		double minSlope = -0.1;
	
		// determine max/min ycoord of all note stems in current beam
		
		//System.out.println("processBeams");

		for (int i=0; i <= (numBeamPoints-1) ; i++)
		{
			x = beamPointX[i];
			y = beamPointY[i];
			//System.out.println (i+": x="+x+" y="+y);

			if (i==0)
			{
				maxY = y;
				minY = y;
				maxX = x;
				minX = x;
				maxIX = 0;
				minIX = 0;

				//pmaxY = y;
				//pminY = y;
				//pmaxX = x;
				//pminX = x;
				//pmaxIX = 0;
				//pminIX = 0;
			}
			else
			{
				if (y >= maxY)	{
					//pmaxY = maxY;
					//pmaxX = maxX;
					//pmaxIX = maxIX;
					maxY = y;
					maxX = x;
					maxIX = i;
				}
				if (y <= minY)	{
					//pminY = minY;
					//pminX = minX;
					//pminIX = minIX;
					minY = y;
					minX = x;
					minIX = i;
				}
			} // else
		} // for

		//System.out.println ("maxIX="+maxIX+" maxX="+maxX+" maxY="+maxY);
		//System.out.println ("minIX="+minIX+" minX="+minX+" minY="+minY);
		//System.out.println ("pmaxIX="+pmaxIX+" pmaxX="+pmaxX+" pmaxY="+pmaxY);
		//System.out.println ("pminIX="+pminIX+" pminX="+pminX+" pminY="+pminY);

		// calc slope btw first and last note of main beam


		//if (beamStemDir[0] < 0) // stem up
		//{
		//	if (minIX <= pminIX)
		//	{
		//		lowerIX = minIX;
		//		higherIX = pminIX;
		//	}
		//	else
		//	{
		//		lowerIX = pminIX;
		//		higherIX = minIX;
		///	}
		//}
		//else
		//{
		//	if (maxIX <= pmaxIX)
		//	{
		//		lowerIX = maxIX;
		//		higherIX = pmaxIX;
		//	}
		//	else
		//	{
		//		lowerIX = pmaxIX;
		//		higherIX = maxIX;
		//	}
		//}
		
		// Attempting to fix bug where stem in middle sometimes protrudes thru beam
		//deltaY = beamPointY [ higherIX ] - beamPointY [ lowerIX ] ;
		//deltaX = beamPointX [ higherIX ] - beamPointX [ lowerIX ] ;
		//System.out.println ("lowerIX="+lowerIX +"higherIX="+higherIX +"deltaY="+deltaY+" deltaX="+deltaX);



		//Original delta calculations:
		deltaY = beamPointY [ numBeamPoints-1 ] - beamPointY [0] ;
		deltaX = beamPointX [ numBeamPoints-1 ] - beamPointX [0] ;

		slope = (double)deltaY / (double)deltaX ;

		//System.out.println ("deltaY="+deltaY+" deltaX="+deltaX+" slope="+slope);

		if (slope > maxSlope)
		{
			slope = maxSlope;
		}
		else if (slope < minSlope)
		{
			slope = minSlope;
		}

		//System.out.println ("adjusted slope="+slope);

		// recalc endpoints of main beam
		if (beamStemDir[0] < 0) // stem up
		{
			mainBeamRightX = beamPointX[ numBeamPoints-1 ];
			mainBeamRightY = (int) (slope * (double)( mainBeamRightX - maxX) + (double)maxY);
			mainBeamLeftX = beamPointX[ 0 ];
			mainBeamLeftY = (int) (slope * (double)(mainBeamLeftX - maxX ) + (double)maxY);
		}
		else // if (beamStemDir[0] > 0) // stem down
		{
			mainBeamRightX = beamPointX[ numBeamPoints-1 ];
			mainBeamRightY = (int) (slope * (double)( mainBeamRightX - minX) + (double)minY);
			mainBeamLeftX = beamPointX[ 0 ];
			mainBeamLeftY = (int) (slope * (double)(mainBeamLeftX - minX ) + (double)minY);
		}
		//System.out.println ("mainBeamRightX="+mainBeamRightX);
		//System.out.println ("mainBeamRightY="+mainBeamRightY);
		//System.out.println ("mainBeamLeftX="+mainBeamLeftX);
		//System.out.println ("mainBeamLeftY="+mainBeamLeftY);

		addBeam ( mainBeamLeftX, mainBeamLeftY, mainBeamRightX, mainBeamRightY);

		// Add stem extensions
		for (int i=0; i <= (numBeamPoints-1) ; i++)
		{
			x = beamPointX[i];
			y = beamPointY[i];
			
			// Interpolate Y coord of Main Beam at x
			yMainBeam[i] = mainBeamLeftY + (int) (slope * (double)( x - mainBeamLeftX ) );

			addStemExt ( x, y, x, yMainBeam[i]);
		}

		// draw nested beams
		for (int i=1; i <= (innerBeamPtr) ; i++)
		{
			//System.out.println("i="+i);

			if (beamStemDir[innerBeamLeft[i]] > 0)
			{
				//System.out.println("upstem: left="+innerBeamLeft[i]+" right="+innerBeamRight[i]+" level="+innerBeamLevel[i]);
				
				addBeam	(
			  	beamPointX[innerBeamLeft[i]] 
				, yMainBeam[innerBeamLeft[i]] + (7 * innerBeamLevel[i])
				, beamPointX[innerBeamRight[i]] 
				, yMainBeam[innerBeamRight[i]] + (7 * innerBeamLevel[i])
				);
			}
			else
			{
				//System.out.println("downstem");

				addBeam	(
			  	beamPointX[innerBeamLeft[i]] 
				, yMainBeam[innerBeamLeft[i]] - (7 * innerBeamLevel[i])
				, beamPointX[innerBeamRight[i]] 
				, yMainBeam[innerBeamRight[i]] - (7 * innerBeamLevel[i])
				);
			}
		} // for
		innerBeamPtr = 0;

		// draw flags
		for (int i=1; i <= (innerFlagPtr) ; i++)
		{
			int flagEndX, flagEndY;

			//System.out.println("i="+i);
			//System.out.println("innerFlagIndex[i]="+innerFlagIndex[i]);

			if (beamStemDir[innerFlagIndex[i]] > 0)
			{
				//System.out.println("upstem: Flag direction="+innerFlagDirection[i]
				//			+"  level="+innerFlagLevel[i]);

				if (innerFlagDirection[i] > 0) // right pointing
				{
					flagEndX = beamPointX[innerFlagIndex[i]] + innerFlagWidth;
					flagEndY = mainBeamLeftY 
							+ (int) (slope * (double)( flagEndX - mainBeamLeftX ) ) 
							+ (7 * innerFlagLevel[i]);
				}
				else // left pointing
				{
					flagEndX = beamPointX[innerFlagIndex[i]] - innerFlagWidth;
					flagEndY = mainBeamLeftY 
							+ (int) (slope * (double)( flagEndX - mainBeamLeftX ) ) 
							+ (7 * innerFlagLevel[i]);
				}

				addBeam	(
			  	beamPointX[innerFlagIndex[i]] 
				, yMainBeam[innerFlagIndex[i]] + (7 * innerFlagLevel[i])
				, flagEndX
				, flagEndY
				);
			}
			else
			{
				//System.out.println("downstem: Flag direction="+innerFlagDirection[i]
				//			+"  level="+innerFlagLevel[i]);

				if (innerFlagDirection[i] > 0) // right pointing
				{
					flagEndX = beamPointX[innerFlagIndex[i]] + innerFlagWidth;
					flagEndY = mainBeamLeftY 
							+ (int) (slope * (double)( flagEndX - mainBeamLeftX ) ) 
							- (7 * innerFlagLevel[i]);
				}
				else // left pointing
				{
					flagEndX = beamPointX[innerFlagIndex[i]] - innerFlagWidth;
					flagEndY = mainBeamLeftY 
							+ (int) (slope * (double)( flagEndX - mainBeamLeftX ) ) 
							- (7 * innerFlagLevel[i]);
				}

				addBeam	(
			  	beamPointX[innerFlagIndex[i]] 
				, yMainBeam[innerFlagIndex[i]] - (7 * innerFlagLevel[i])
				, flagEndX
				, flagEndY
				);
			}
		} // for
		innerFlagPtr = 0;


	} // processBeams


//------------------------------------------------------------------------------


	private void addNoteEvent ( long mtime, int stsbyte, int chnl, int notenum, int vel)
	{
		//System.out.println ("addNoteEvent: mtime="+mtime+" stsbyte="+stsbyte+" chnl="+chnl+" notenum="+notenum+" vel="+vel);
		
		try {		
			msg = new ShortMessage();
			msg.setMessage(stsbyte, chnl, notenum, vel);

			event = new MidiEvent(msg, mtime);
			track.add(event);
		}
		catch ( Exception e )	{
			System.out.println("ERROR: in addNoteEvent - unable to add short message to track");
			e.printStackTrace();
		}
	}

	private void addProgChgEvent ( long mtime, int chnl, int patchnum)
	{		
		try {		
			msg = new ShortMessage();
			msg.setMessage(0xC0, chnl, patchnum, 0);

			event = new MidiEvent(msg, mtime);
			track.add(event);
		}
		catch ( Exception e )	{
			System.out.println("ERROR: in addProgChgEvent - unable to add progchg message to track");
			e.printStackTrace();
		}
	}

	private void addEndOfTrackEvent (long mtime)
	{
		//System.out.println ("addEndOfTrackEvent");

		byte blankbytes[] = { 0 };

		try
		{
			MetaMessage mymsg = new MetaMessage();
			mymsg.setMessage(47, blankbytes, 0);
			MidiEvent myevent = new MidiEvent(mymsg, mtime);
			track.add(myevent);
		}
		catch (InvalidMidiDataException e)
		{   
			System.out.println("Invalid MIDI Data Exception"); 
	    		e.printStackTrace();
		}
	}

//------------------------------------------------------------------------------

	private void addToSeq (int oct, char noteLetter, int accid, int dur, int dots )
	{
		int midinotenum=0, mididur=0;
		int offset=0;

		//System.out.println ("Adding to sequence: "+noteLetter+" Accid="+accid+" Oct="+oct+" Dur="+dur+" dots="+dots);

		switch (noteLetter)	{
			case 'C': { 
				if (accid == 10) {
					mSharps[0] = 0; mFlats[0] = 0;
				}
				else if (accid > 0)	{
					mSharps[0] = accid;
				}
				else if (accid < 0)	{
					mFlats[0] = accid;
				}
				break ; 
			}
			case 'D': { 
				if (accid == 10) {
					mSharps[1] = 0; mFlats[1] = 0;
				}
				else if (accid > 0)	{
					mSharps[1] = accid;
				}
				else if (accid < 0)	{
					mFlats[1] = accid;
				}
				break ; 
			}
			case 'E': { 
				if (accid == 10) {
					mSharps[2] = 0; mFlats[2] = 0;
				}
				else if (accid > 0)	{
					mSharps[2] = accid;
				}
				else if (accid < 0)	{
					mFlats[2] = accid;
				}
				break ; 
			}
			case 'F': { 
				if (accid == 10) {
					mSharps[3] = 0; mFlats[3] = 0;
				}
				else if (accid > 0)	{
					mSharps[3] = accid;
				}
				else if (accid < 0)	{
					mFlats[3] = accid;
				}
				break ; 
			}
			case 'G': { 
				if (accid == 10) {
					mSharps[4] = 0; mFlats[4] = 0;
				}
				else if (accid > 0)	{
					mSharps[4] = accid;
				}
				else if (accid < 0)	{
					mFlats[4] = accid;
				}
				break ; 
			}
			case 'A': { 
				if (accid == 10) {
					mSharps[5] = 0; mFlats[5] = 0;
				}
				else if (accid > 0)	{
					mSharps[5] = accid;
				}
				else if (accid < 0)	{
					mFlats[5] = accid;
				}
				break ; 
			}
			case 'B': { 
				if (accid == 10) {
					mSharps[6] = 0; mFlats[6] = 0;
				}
				else if (accid > 0)	{
					mSharps[6] = accid;
				}
				else if (accid < 0)	{
					mFlats[6] = accid;
				}
				break ; 
			}
		}

		//for (int i=0 ; i<7 ; i++)
		//{
		//	System.out.println (i+": mSharps:"+mSharps[i]+" mFlats"+mFlats[i]);
		//}

		switch (noteLetter)	{
			case 'C': { offset = 0 + mSharps[0] + mFlats[0] ; break ; }
			case 'D': { offset = 2 + mSharps[1] + mFlats[1] ; break ; }
			case 'E': { offset = 4 + mSharps[2] + mFlats[2] ; break ; }
			case 'F': { offset = 5 + mSharps[3] + mFlats[3] ; break ; }
			case 'G': { offset = 7 + mSharps[4] + mFlats[4] ; break ; }
			case 'A': { offset = 9 + mSharps[5] + mFlats[5] ; break ; }
			case 'B': { offset = 11 + mSharps[6] + mFlats[6] ; break ; }
		}

		midinotenum = 36 + (12 * (oct-1)) + offset ;

		//System.out.println ("oct= "+oct+" offset="+offset+" accid="+accid+" midinotenum="+midinotenum);

		switch (dots)  {
			case 0:	{  mididur = dur ; break ; }
			case 1:	{  mididur = dur + (dur/2) ; break ; }
			case 2:	{  mididur = dur + (dur/2) + (dur / 4) ; break ; }
		}

		//System.out.println ("Miditime="+miditime+" midinotenum="+midinotenum+" dur="+dur+" mididur="+mididur);

		if (!suppressingMIDI)
		{
			addNoteEvent ( miditime, 144, 1, midinotenum, 100);
			addNoteEvent ( (long)(miditime + (mididur * legatoPercent )) , 128, 1, midinotenum, 0 );
			
			midiNotes [ midiNoteCount ] = midinotenum;
			midiTimes [ midiNoteCount ] = (long)(miditime + (mididur * legatoPercent ));
			midiNoteCount++;
		}
		miditime += dur;
	}

//------------------------------------------------------------------------------

	private void addRestToSeq (int dur, int dots)
	{
		long mididur=0;

		//System.out.println ("Adding Rest to sequence: Dur="+dur+" dots="+dots);

		switch (dots)  {
			case 0:	{  mididur = dur ; break ; }
			case 1:	{  mididur = dur + (dur/2) ; break ; }
			case 2:	{  mididur = dur + (dur/2) + (dur / 4) ; break ; }
		}
		//System.out.println ("mididur="+mididur);

		if (!suppressingMIDI)
		{
			addNoteEvent ( mididur , 128, 1, 0, 0 );
		}
	}

//------------------------------------------------------------------------------
} // class
