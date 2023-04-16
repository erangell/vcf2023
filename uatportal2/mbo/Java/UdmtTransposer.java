import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;

//------------------------------------------------------------------------------
// UdmtTransposer - Transposes command strings for solfege exercises (assumes all exercises written in C major or C minor)
//
//------------------------------------------------------------------------------


public class UdmtTransposer extends Object
{
	private String UdmtAppletVersion = "0.01";

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

	int majKeySharpXOffset[] = {30,40,50,60,70,80,90};
	int majKeySharpYOffset[] = {-6,9,-11,4,18,0,13};
	int majKeyFlatXOffset[] = {30,40,50,60,70,80,90};
	int majKeyFlatYOffset[] = {13,0,18,4,22,9,27};

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

	private int playStatus = -1 ; 	// -1 = stopped, +1 = playing

	private long miditime = 0;	// used for tracking duration of sequence

	private double legatoPercent = 0.75;	// percentage of duration to keep the note on.

	// Duration values to use for stringing:
	private int durWhole = 1, durHalf = 2, durQtr = 4, dur8th = 8, dur16th= 6, dur32nd = 3;


	private int kSharps[] = {0,0,0,0,0,0,0};	// adjustments for C-G when key signature changes
	private int kFlats[] = {0,0,0,0,0,0,0};
	private int mSharps[] = {0,0,0,0,0,0,0};	// adjustments for C-G for current measure
	private int mFlats[] = {0,0,0,0,0,0,0};
	private int tSharps[] = {0,0,0,0,0,0,0};	// adjustments for C-G for transposed sequence
	private int tFlats[] = {0,0,0,0,0,0,0};


	private String parmsuppressMIDI;
	private boolean suppressingMIDI = false;

	private int seqPatch = 0;
	private String transposed="";

	private int transpNumSF = 0;
	private char transpSorF = ' ';

	private String[] majKeyS0 = {
		"C","D","E","F","G","A","B","A%","B%"
	};
	private String[] majKeyS1 = {
		"G","A","B","C","D","E","F","C#","D#"
	};
	private String[] majKeyS2 = {
		"D","E","F","G","A","B","C","G#","A#"
	};
	private String[] majKeyS3 = {
		"A","B","C","D","E","F","G","D#","E#"
	};
	private String[] majKeyS4 = {
		"E","F","G","A","B","C","D","A#","B#"
	};
	private String[] majKeyS5 = {
		"B","C","D","E","F","G","A","E#","F*"
	};
	private String[] majKeyS6 = {
		"F","G","A","B","C","D","E","B#","C*"
	};
	private String[] majKeyS7 = {
		"C","D","E","F","G","A","B","F*","G*"
	};
	private String[] majKeyF0 = {
		"C","D","E","F","G","A","B","F#","G#"
	};
	private String[] majKeyF1 = {
		"F","G","A","B","C","D","E","B%","C#"
	};
	private String[] majKeyF2 = {
		"B","C","D","E","F","G","A","E%","F#"
	};
	private String[] majKeyF3 = {
		"E","F","G","A","B","C","D","A%","B%"
	};
	private String[] majKeyF4 = {
		"A","B","C","D","E","F","G","D%","E%"
	};
	private String[] majKeyF5 = {
		"D","E","F","G","A","B","C","G%","A%"
	};
	private String[] majKeyF6 = {
		"G","A","B","C","D","E","F","C%","D%"
	};
	private String[] majKeyF7 = {
		"C","D","E","F","G","A","B","F%","G%"
	};

	private String[] minKeyS0 = {
		"A","B","C","D","E","F","G","F#","G#"
	};
	private String[] minKeyS1 = {
		"E","F","G","A","B","C","D","C#","D#"
	};
	private String[] minKeyS2 = {
		"B","C","D","E","F","G","A","G#","A#"
	};
	private String[] minKeyS3 = {
		"F","G","A","B","C","D","E","D#","E#"
	};
	private String[] minKeyS4 = {
		"C","D","E","F","G","A","B","A#","B#"
	};
	private String[] minKeyS5 = {
		"G","A","B","C","D","E","F","E#","F*"
	};
	private String[] minKeyS6 = {
		"D","E","F","G","A","B","C","B#","C*"
	};
	private String[] minKeyS7 = {
		"A","B","C","D","E","F","G","F*","G*"
	};

	private String[] minKeyF0 = {
		"A","B","C","D","E","F","G","F#","G#"
	};
	private String[] minKeyF1 = {
		"D","E","F","G","A","B","C","B%","C#"
	};
	private String[] minKeyF2 = {
		"G","A","B","C","D","E","F","E%","F#"
	};
	private String[] minKeyF3 = {
		"C","D","E","F","G","A","B","A%","B%"
	};
	private String[] minKeyF4 = {
		"F","G","A","B","C","D","E","D%","E%"
	};
	private String[] minKeyF5 = {
		"B","C","D","E","F","G","A","G%","A%"
	};
	private String[] minKeyF6 = {
		"E","F","G","A","B","C","D","C%","D%"
	};
	private String[] minKeyF7 = {
		"A","B","C","D","E","F","G","F%","G%"
	};


	private String[]  lookupNote;
	private int prevTranspMidi=60;
	private int currTranspMidi=-1, currTranspMidiOct=-1;
	private int prevTranspMidiOct = -1;

	private char[] arrTranspNote;
	private int arrTranspNoteLen;
	private char currTranspNote;
	private int currTranspAccid=0;

	private int transpDistance;
	private int startingMidi;
	private int startingMidiOct;
	private int prevorigmidinotenum=-1;

	private int testOffset;

//------------------------------------------------------------------------------
// THE FOLLOWING IS THE API FOR CALLING THIS PROGRAM AS AN OBJECT FROM OTHER JAVA PROGRAMS
//------------------------------------------------------------------------------
	public void UdmtTransposer()
	{
	}

	public void setTranspKey(int numSF, char sOrF, int majorMinor)
	{
		if ((numSF >= 0) && (numSF <= 7))
		{
			transpNumSF = numSF;
		}
		else
		{
			System.out.println ("ERROR: invalid number of sharps/flats in setTranspKey");
		}
		if ((sOrF == 'S') || (sOrF == 'F'))
		{
			transpSorF = sOrF;
		}
		else
		{
			System.out.println ("ERROR: invalid S/F character in setTranspKey");
		}

		for (int i=0 ; i < 9 ; i++)
		{
			if (transpSorF == 'S')
			{
				switch (transpNumSF)
				{
					case 0:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF0[i];
							startingMidi = 60;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF0[i];
							startingMidi = 69;
							startingMidiOct = 3;
						}
						break;
					}

					case 1:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS1[i];
							startingMidi = 67;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS1[i];
							startingMidi = 64;
							startingMidiOct = 3;
						}
						break;
					}
					case 2:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS2[i];
							startingMidi = 62;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS2[i];
							startingMidi = 71;
							startingMidiOct = 3;
						}
						break;
					}
					case 3:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS3[i];
							startingMidi = 69;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS3[i];
							startingMidi = 66;
							startingMidiOct = 3;
						}
						break;
					}
					case 4:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS4[i];
							startingMidi = 64;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS4[i];
							startingMidi = 61;
							startingMidiOct = 3;
						}
						break;
					}
					case 5:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS5[i];
							startingMidi = 71;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS5[i];
							startingMidi = 68;
							startingMidiOct = 3;
						}
						break;
					}
					case 6:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS6[i];
							startingMidi = 66;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS6[i];
							startingMidi = 63;
							startingMidiOct = 3;
						}
						break;
					}
					case 7:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyS7[i];
							startingMidi = 61;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyS7[i];
							startingMidi = 70;
							startingMidiOct = 3;
						}
						break;
					}
				} // switch
			} // if S
			else if (transpSorF == 'F')
			{
				switch (transpNumSF)
				{
					case 0:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF0[i];
							startingMidi = 60;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF0[i];
							startingMidi = 69;
							startingMidiOct = 3;
						}
						break;
					}
					case 1:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF1[i];
							startingMidi = 65;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF1[i];
							startingMidi = 62;
							startingMidiOct = 3;
						}
						break;
					}
					case 2:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF2[i];
							startingMidi = 70;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF2[i];
							startingMidi = 67;
							startingMidiOct = 3;
						}
						break;
					}
					case 3:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF3[i];
							startingMidi = 63;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF3[i];
							startingMidi = 60;
							startingMidiOct = 3;
						}
						break;
					}
					case 4:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF4[i];
							startingMidi = 68;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF4[i];
							startingMidi = 65;
							startingMidiOct = 3;
						}
						break;
					}
					case 5:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF5[i];
							startingMidi = 61;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF5[i];
							startingMidi = 70;
							startingMidiOct = 3;
						}
						break;
					}
					case 6:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF6[i];
							startingMidi = 66;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF6[i];
							startingMidi = 63;
							startingMidiOct = 3;
						}
						break;
					}
					case 7:	{
						if (majorMinor > 0) {
							lookupNote[i] = majKeyF7[i];
							startingMidi = 59;
							startingMidiOct = 3;
						} else {
							lookupNote[i] = minKeyF7[i];
							startingMidi = 68;
							startingMidiOct = 3;
						}
						break;
					}
				} // switch
			} // else if F
		} // for		

		prevorigmidinotenum = -1;

	} // method setTranspKey


	public void initAPI()
	{
		lookupNote = new String[9];
	}



	public void reset()
	{
		//System.out.println("Reset method called");

		lenCmdStr=0;
		cmdCharIndex=0;
		numCmdCharIndex = 0;
		mode = 0;
		currClef=' ';
		currKeyNumSF = 0;
		currKeySF = 'S';
		currOctave = 3;	
		tsigNum = 0;
		tsigDen = 0;
		savedNoteLetter = ' ';
		currAccid = 0; 
		numDots=0;
		accid2draw=0;
		currDigit=-1;
		prevTranspMidi=60;
		currTranspMidi=-1;
		currTranspMidiOct=-1;
		prevTranspMidiOct = -1;
	}

	public String getVersion()
	{
		return UdmtAppletVersion;
	}
	public String getTransposed()
	{
		return transposed;
	}

	public void setCmdStr (String newCmdStr)
	{
		
		parmCmdStr = newCmdStr;
		arrCmdStr = parmCmdStr.toCharArray();
		lenCmdStr = parmCmdStr.length();

		transposed = "";

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
							}
			else
			{
			  switch (c)	{
				case 'L': {
					//System.out.println("Mode changed to: MODE_CLEF");
					mode = MODE_CLEF;
					transposed += c;
					break;
				}
				case 'T': {
					//System.out.println("Mode changed to: MODE_TIME");
					mode = MODE_TIME;
					tsigNum = 0;
					tsigDen = 0;
					transposed += c;
					break;
				}
				case 'K': {
					//System.out.println("Mode changed to: MODE_KEY");
					mode = MODE_KEY;
					transposed += c;
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
					transposed += c;
					break;
				}
				case '|': {
					//System.out.println("Mode changed to: MODE_BAR");
					mode = MODE_BAR;
					transposed += c;
					break;
				}
				case '+': {
					//System.out.println("Mode changed to: MODE_PLUS");
					mode = MODE_PLUS;
					plusVal=0;
					transposed += c;
					break;
				}
				case '-': {
					//System.out.println("Mode changed to: MODE_MINUS");
					mode = MODE_MINUS;
					minusVal=0;
					transposed += c;
					break;
				}
				case '>': {
					//System.out.println("Stem direction changed to UP");
					currStemDir = +1;
					transposed += c;
					break;
				}
				case '<': {
					//System.out.println("Stem direction changed to DOWN");
					currStemDir = -1;
					transposed += c;
					break;
				}
				case '[': {
					//System.out.println("[Beam Begin");
					transposed += c;
					break;
				}
				case ']': {
					//System.out.println("]Beam End");
					transposed += c;
					break;
				}

				case '/': { // right pointing flag within beam

					//System.out.println("/Right Flag");
					transposed += c;
					break;
				}
				case ' ': {
					// Ignore whitespace
					transposed += c;
					break;
				}
				case 'X': {
					// Dummy command - used if last command on line doesn't render					
					transposed += c;

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
			transposed += c;

			mode = MODE_NONE;
		}
		else if (c == 'F')
		{
			currClef = c;
			transposed += c;

			mode = MODE_NONE;
		}
		else if (c == ' ') {
			transposed += c;

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
				case '2': {  tsigNum = 2; transposed += c; break;  }
				case '3': {  tsigNum = 3; transposed += c; break;  }
				case '4': {  tsigNum = 4; transposed += c; break;  }
				case '6': {  tsigNum = 6; transposed += c; break;  }
			}
		}
		else if (tsigDen == 0)
		{
			switch (c)
			{
				case '4': {  
					tsigDen = 4; 
					//renderTimeSig (tsigNum, tsigDen);
					transposed += c; 
					break;  
				}
				case '8': {  
					tsigDen = 8; 
					//renderTimeSig (tsigNum, tsigDen);
					transposed += c; 
					break;  
				}
				case 'C': { 
					tsigDen = 2; 
					//renderTimeSig (tsigNum, tsigDen);
					transposed += c; 
					break; 
				}
				case '/': {
					transposed += c;
					break;
				}
			}
			
		}
		else if (c == '/')	{
			// no logic needed for now because only supporting 1 digit numerator/denominator
			transposed += c; 
		}
		else if (c == ' ')	{
			transposed += c; 
			mode = MODE_NONE;
		}
		else	{
			System.out.println ("ERROR: Unsupported TimeSig Character: "+c);
			mode = MODE_NONE;
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
			//renderKeySig (currKeySF, currKeyNumSF);

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
				tSharps[i] = kSharps[i];
			}

			//System.out.println ("Starting midi: "+startingMidi);

			// write new key signature to output string		
			transposed += transpNumSF;
			transposed += transpSorF;

			mode = MODE_NONE;
		}
		else if (c == 'F')	{
			currKeySF = c;
			//renderKeySig (currKeySF, currKeyNumSF);

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
				tFlats[i] = kFlats[i];
			}

			//System.out.println ("Starting midi: "+startingMidi);


			// write new key signature to output string
			transposed += transpNumSF;
			transposed += transpSorF;

			mode = MODE_NONE;
		}
		else if (c == ' ')	{
			transposed += c; 
			mode = MODE_NONE;
		}
		else	{
			System.out.println ("ERROR: Unsupported KeySig Character: "+c);
			mode = MODE_NONE;
		}
	}

	private void modeNote ( char c)
	{
		int ypos;
		
		if (c == '1') {
			addToSeq (currOctave, savedNoteLetter, accid2draw, durWhole, numDots );
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durHalf, numDots );
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durQtr, numDots );
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur8th, numDots );
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur16th, numDots );
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur32nd, numDots );
			mode = MODE_NONE;
		}
		else if (c == '#')	{
			accid2draw=+1;
			System.out.println ("Unsupported accidental for solfege transposition");
		}
		else if (c == '&')	{
			accid2draw=-1;
			System.out.println ("Unsupported accidental for solfege transposition");
		}
		else if (c == '%')	{
			accid2draw=+10;
		}
		else if (c == '*')	{
			accid2draw=+2;
			System.out.println ("Unsupported accidental for solfege transposition");
		}
		else if (c == '@')	{
			accid2draw=-2;
			System.out.println ("Unsupported accidental for solfege transposition");
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
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			transposed += c;
			mode = MODE_NONE;
		}
		else if (c == '.')	{
			transposed += c;
		}
		else if (c == ':')	{
			transposed += c;
		}
		else if (c == '~')	{
			transposed += c;
		}
		else if (c == ' ')	{
			transposed += c;
			mode = MODE_NONE;
		}
	}

	private void modeBar ( char c)
	{

		switch ( c )
		{
			case '|' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
			case '[' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
			case ']' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
			case 'L' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
			case 'R' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
			case 'T' : {
				transposed += c;
				mode = MODE_NONE;
				break; 
			}

			default : {
				transposed += c;
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
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
		}
		if (currDigit > -1)
		{
			transposed += c;
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
				transposed += c;
				mode = MODE_NONE;
				break; 
			}
		}
		if (currDigit > -1)
		{
			transposed += c;
		}
	}

//------------------------------------------------------------------------------

	private void addToSeq (int oct, char noteLetter, int accid, int dur, int dots )
	{
		int origmidinotenum=-1;
		int offset=0, toffset=0;
		int deltamidi = 0;
		String transpNote = "";

		//System.out.println ("Adding to sequence: "+noteLetter+" Accid="+accid+" Oct="+oct+" Dur="+dur+" dots="+dots);

		// First, figure out the midi value of the note from the original sequence
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


		switch (noteLetter)	{
			case 'C': { 
				transpNote = lookupNote[0];
				break ; 
			}
			case 'D': { 
				transpNote = lookupNote[1];
				break ; 
			}
			case 'E': { 
				transpNote = lookupNote[2];
				break ; 
			}
			case 'F': { 
				transpNote = lookupNote[3];
				break ; 
			}
			case 'G': { 
				transpNote = lookupNote[4];
				break ; 
			}
			case 'A': { 
				if (accid == 10) {
					transpNote = lookupNote[7];
				}
				else	{
					transpNote = lookupNote[5];
				}
				break ; 
			}
			case 'B': { 
				if (accid == 10) {
					transpNote = lookupNote[8];
				}
				else	{
					transpNote = lookupNote[6];
				}
				break ; 
			}
		}

		// Figure out the midi value of the note from the transposed sequence

		//System.out.println ("transpNote="+transpNote);

		arrTranspNote = transpNote.toCharArray();
		arrTranspNoteLen = transpNote.length();
		currTranspNote = arrTranspNote[0];

		//System.out.println ("currTranspNote="+currTranspNote);


		origmidinotenum = 36 + (12 * (oct-1)) + offset ;

		//System.out.println ("prevorigmidinotenum="+prevorigmidinotenum+" origmidinotenum"+origmidinotenum);

		if (prevorigmidinotenum != -1)
		{
			deltamidi = origmidinotenum - prevorigmidinotenum;

		}
		else // first note - write octave
		{
			deltamidi = 0;
			prevTranspMidi = startingMidi;
			prevTranspMidiOct =  startingMidiOct;
		}

		//System.out.println ("deltamidi="+deltamidi);

		currTranspMidi = prevTranspMidi + deltamidi;
		currTranspMidiOct = (int)(currTranspMidi / 12) - 2;
		
		// adjust octave for cb, c@, b#, b*
		testOffset = currTranspMidi % 12;
		if ( (currTranspNote == 'C') && (testOffset == 11) )
		{
			currTranspMidiOct++;
		}
		if ( (currTranspNote == 'C') && (testOffset == 10) )
		{
			currTranspMidiOct++;
		}
		if ( (currTranspNote == 'B') && (testOffset == 0) )
		{
			currTranspMidiOct--;
		}
		if ( (currTranspNote == 'B') && (testOffset == 1) )
		{
			currTranspMidiOct--;
		}

		//System.out.println ("transpNote="+transpNote+ " currTranspMidi=" +currTranspMidi+	 "currTranspMidiOct=" +currTranspMidiOct);

		if (currTranspMidiOct != prevTranspMidiOct)
		{
			transposed += "O";
			transposed += currTranspMidiOct;
			transposed += " ";
		}

		transposed += transpNote;
		transposed += dur;
		transposed += " ";
		accid2draw=0;

		prevTranspMidi= currTranspMidi;
		prevTranspMidiOct = currTranspMidiOct;

		prevorigmidinotenum = origmidinotenum;
	}

//------------------------------------------------------------------------------
} // class


