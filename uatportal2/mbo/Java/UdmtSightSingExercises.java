//---------------------------------------------------------------------------------
public class UdmtSightSingExercises extends Object
//---------------------------------------------------------------------------------
{
	private String[] MajorScalarExercise = {
	 "LG K0S +5 T4/C +20 o3 c4 d4 e4 f4 | g4 a4 b4 o+ c4 X"
	,"LG K0S +5 T4/C +20 o4 c4 o- b4 a4 g4 | f4 e4 d4 c4 X"
	,"LG K0S +5 T4/C +20 o3 c4 d4 c2 X"
	,"LF K0S +5 T4/C +20 o2 c4 o- b4 o+ c2 X"
	,"LG K0S +5 T4/C +20 o3 c4 d4 c4 o- b4 o+ | +10 c1 X"
	,"LF K0S +5 T4/C +20 o2 c4 d4 e4 d4 | +10 c1 X"
	,"LG K0S +5 T4/C +20 o3 c4 o- b4 a4 b4 | o+ +10 c1 X"
	,"LF K0S +5 T4/C +20 o2 c4 o- b4 o+ c4 d4 | +10 c1 X"
	,"LG K0S +5 T4/C +20 o3 c4 d4 c4 d4 | +10 c1 X"
	,"LG K0S +5 T4/C +20 o3 c4 o- b4 o+ c4 o- b4 | +10 o+ c1 X"
	,"LG K0S +5 T4/C +20 o3 c4 d4 e4 d4 |  c4 o- b4 o+ c2 X"
	,"LF K0S +5 T4/C +20 o2 c4 d4 e4 f4 |  e4 d4 c2 X"
	,"LG K0S +5 T4/C +20 o3 c4 o- b4 a4 b4 |  o+ c4 d4 c2 X"
	,"LG K0S +5 T4/C +20 o3 c4 o- b4 a4 g4 |  a4 b4 o+ c2 X"
	,"LG K0S +5 T4/C +20 o3 c4 o- b4 o+ c4 d4 |  e4 d4 c2 X"
	,"LG K0S +5 T4/C +20 o3 c4 d4 c4 o- b4 |  a4 b4 o+ c2 X"
	};
	
	private String[] MajorArpeggiatedExercise = {
	 "LG K1F +5 T4/C +20 o3 f4 e4 f4 g4 | f4 e4 d4 e4 | +10 f1 X"
	,"LF K1S +5 T4/C +20 o2 g4 a4 g4 f4 | e4 f4 g4 f4 | +10 g1 X"
	,"LG K2F +5 T4/C +20 o3 b4 a4 g4 a4 | b4 o+ c4 d4 c4 o- | +10 b1 X"
	,"LF K2S +5 T4/C +20 o2 d4 e4 f4 g4 | f4 e4 d4 c4 | +10 d1 X"
	,"LG K3F +5 T4/C +20 o4 e4 d4 c4 o- b4 | a4 b4 o+ c4 d4 | +10 e1 X"
	,"LF K3S +5 T4/C +20 o1 a4 b4 o+ c4 d4 | c4 d4 c4 o- b4 | +10 a1 X"
	,"LG K1F +5 T4/C +20 o3 f4 e4 f4 g4 | a4 b4 a4 g4 | +10 f1 X"
	,"LF K2F +5 T4/C +20 o2 b4 a4 g4 a4 | b4 a4 b4 o+ c4 o- | +10 b1 X"
	};

	private String[] NaturalMinorScalarExercise = {
	 "LG +3 K0S +3 T4/C +20 O3 A4 B4 O+ C4 D4 | E4 F4 G4 A4 X"
	,"LG +3 K0S +3 T4/C +20 O4 A4 G4 F4 E4 | D4 C4 O- B4 A4 X"
	,"LG +3 K0S +3 T4/C +20 O3 A4 B4 o+ C4 D4 | C4 o- B4 A4 G4 | +10 A1 X"
	,"LF +3 K0S +3 T4/C +20 O2 A4 G4 F4 G4 | A4 B4 A4 G4 | +10 A1  X"
	,"LG +3 K1F +3 T4/C +20 O4 D4 C4 D4 E4 | F4 E4 D4 E4 | +10 D1  X"
	,"LF +3 K1S +3 T4/C +20 O2 E4 D4 E4 F4 | E4 D4 C4 D4 | +10 E1 X"
	};

	private String[] NaturalMinorArpeggiatedExercise = {
	 "LG +3 K3F +3 T4/C +20 o3 c4 e4 g4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 e4 c4 o- g4 | o+ +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- a4 o+ c4 f4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 a4 f4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- b4 o+ d4 g4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 d4 o- b4 g4 | o+ +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 d4 | e4 f4 e4 d4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 a4 g4 | e4 d4 o- b4 g4 | o+ +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 e4 | f4 a4 g4 o- b4 | o+ +10 c1 X"
	};

	private String[] HarmonicMinorScalarExercise = {
	 "LG +3 K0S +3 T4/C +20 O3 A4 B4 o+ C4 D4 | E4 F4 -8 G#4 A4 X"
	,"LG +3 K0S +3 T4/C +20 O4 A4 -8 G#4 F4 E4 | D4 C4 O- B4 A4 X"
	,"LG +3 K0S +3 T4/C +20 O3 A4 B4 O+ C4 O- B4 | A4 -8 G#4 A4 B4 | +10 A1 X"
	,"LF +3 K0S +3 T4/C +20 O2 A4 -8 G#4 A4 B4 | O+ C4 o- B4 A4 -8 G#4 | +10 A1 X"
	,"LG +3 K1F +3 T4/C +20 O4 D4 E4 D4 E4 | D4 -8 C#4 D4 E4 | +10 D1 X"
	,"LF +3 K1S +3 T4/C +20 O2 E4 F4 E4 -8 D#4 | E4 F4 G4 F4 | +10 E1 X"
	};

	private String[] HarmonicMinorArpeggiatedExercise = {
	 "LG +3 K3F +3 T4/C +20 o3 c4 e4 g4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o-  g4 o+ c4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 e4  c4 o- g4 o+  | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- a4  o+ c4  f4  | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 a4  f4  | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- -8 b%4 o+ d4  g4  | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 d4 o- -8 b%4  g4 o+ | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4  d4  |  e4 f4 e4 d4 |  +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 a4 g4  |  e4 d4 o- -8 b%4 g4 |  o+ +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 e4  | f4 a4 g4 o- -8 b%4 | o+  +10 c1 X"
	};

	private String[] MelodicMinorScalarExercise = {
	 "LG +3 K0S +3 T4/C +20 O3 A4 B4 O+ C4 D4 | E4 -8 F#4 -8 G#4 A4  X"
	,"LG +3 K0S +3 T4/C +20 O4 A4 -8 G#4 -8 F#4 E4 | D4 C4 O- B4 A4  X"
	,"LG +3 K0S +3 T4/C +20 O3 A4 -8 G#4 -8 F#4 E4 | -8 F#4 -8 G#4 A4 B4 | +10 A1  X"
	,"LF +3 K0S +3 T4/C +20 O2 A4 B4 O+ C4 D4 | E4 D4 C4 O- B4 | +10 A1  X"
	,"LG +3 K1F +3 T4/C +20 O4 D4 -8 C#4 O- -8 B%4 A4 | -8 B%4 O+ -8 C#4 D4 -8 C#4 | +10 D1  X"
	,"LF +3 K1S +3 T4/C +20 O2 E4 F4 E4 -8 D#4 | E4 -8 D#4 -8 C#4 -8 D#4 | +10 E1  X"
	};

	private String[] MelodicMinorArpeggiatedExercise = {
	 "LG +3 K3F +3 T4/C +20 o3 c4 e4 g4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 e4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 e4 c4 o- g4 o+ | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- -8 a%4 o+  c4 f4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 -8 a%4 f4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o-  -8 b%4 o+ d4 g4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 d4 o-  -8 b%4  g4 o+ | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o-  g4  o+ c4 d4 | e4 f4 e4 d4 | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 f4 -8 a%4 g4 | e4 d4 o- -8 b%4 g4 o+ | +10 c1 X"
	,"LG +3 K3F +3 T4/C +20 o3 c4 o- g4 o+ c4 e4 |  f4 -8 a%4 g4 o- b%4 | o+ +10 c1X"
	};


	private int currentKeyNumSF = 0;
	private char currentKeySF = 'S';	//S=sharps F=flats
	private char currentKeyMajMin = 'M'; // M=major m=minor
	private char currentMinorType = 'N';	// N=natural H=harmonic M=melodic
	private char currentExerciseType = 'S'; // S=scalar A=arpeggiated
	private int currentExerciseNum = 0;	// index to exercise array


	// Constructor:
	public void UdmtSightSingExercises()
	{
	}

	//-----------------------------------------
	// Methods:
	// setExerciseType (enum exTyp)
	// setLevel (enum levelId)
	// setMinMeasureLength ( int minMeasLen )
	// setKey ( int numAccid, char SorF )
	// getNumExercises
	// getExercise (int exNum)
	// getNextExercise
	//-----------------------------------------
	public String getExercise( )
	{	
		String sRet = null;
		if ( currentKeyMajMin == 'M')
		{
			if ( currentExerciseType == 'S'  )
			{
				sRet = MajorScalarExercise[currentExerciseNum];
				currentExerciseNum++;
				if (currentExerciseNum >= MajorScalarExercise.length)
				{
					currentExerciseNum=0;
				}
			}
			else if ( currentExerciseType == 'A'  )
			{
				sRet = MajorArpeggiatedExercise[currentExerciseNum];
				currentExerciseNum++;
				if (currentExerciseNum >= MajorArpeggiatedExercise.length)
				{
					currentExerciseNum=0;
				}
			}
		}
		else if ( currentKeyMajMin == 'm')
		{
			if (currentMinorType == 'N')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = NaturalMinorScalarExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= NaturalMinorScalarExercise.length)
					{
						currentExerciseNum=0;
					}
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = NaturalMinorArpeggiatedExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= NaturalMinorArpeggiatedExercise.length)
					{
						currentExerciseNum=0;
					}
				}
			}
			else if (currentMinorType == 'H')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = HarmonicMinorScalarExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= HarmonicMinorScalarExercise.length)
					{
						currentExerciseNum=0;
					}
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = HarmonicMinorArpeggiatedExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= HarmonicMinorArpeggiatedExercise.length)
					{
						currentExerciseNum=0;
					}
				}
			}
			else if (currentMinorType == 'M')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = MelodicMinorScalarExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= MelodicMinorScalarExercise.length)
					{
						currentExerciseNum=0;
					}
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = MelodicMinorArpeggiatedExercise[currentExerciseNum];
					currentExerciseNum++;
					if (currentExerciseNum >= MelodicMinorArpeggiatedExercise.length)
					{
						currentExerciseNum=0;
					}
				}
			}
		}
		return sRet;
	}	


	//-----------------------------------------
	public String getExerciseNum( int n )
	{	
		String sRet = null;
		if ( currentKeyMajMin == 'M')
		{
			if ( currentExerciseType == 'S'  )
			{
				sRet = MajorScalarExercise[n];
			}
			else if ( currentExerciseType == 'A'  )
			{
				sRet = MajorArpeggiatedExercise[n];
			}
		}
		else if ( currentKeyMajMin == 'm')
		{
			if (currentMinorType == 'N')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = NaturalMinorScalarExercise[n];
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = NaturalMinorArpeggiatedExercise[n];
				}
			}
			else if (currentMinorType == 'H')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = HarmonicMinorScalarExercise[n];
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = HarmonicMinorArpeggiatedExercise[n];
				}
			}
			else if (currentMinorType == 'M')
			{
				if ( currentExerciseType == 'S'  )
				{
					sRet = MelodicMinorScalarExercise[n];
				}
				else if ( currentExerciseType == 'A'  )
				{
					sRet = MelodicMinorArpeggiatedExercise[n];
				}
			}
		}
		return sRet;
	}	

	public void setExerciseType (char exTyp, char exSubType, char minorType)
	{
		currentKeyMajMin = exTyp;
		currentExerciseType  = exSubType;
		currentMinorType = minorType;
	}
	public int getNumExercises()
	{
		int iRet=0;
		if ( currentKeyMajMin == 'M')
		{
			if ( currentExerciseType == 'S'  )
			{
				iRet = MajorScalarExercise.length;
			}
			else if ( currentExerciseType == 'A'  )
			{
				iRet = MajorArpeggiatedExercise.length;
			}
		}
		else if ( currentKeyMajMin == 'm')
		{
			if (currentMinorType == 'N')
			{
				if ( currentExerciseType == 'S'  )
				{
					iRet = NaturalMinorScalarExercise.length;
				}
				else if ( currentExerciseType == 'A'  )
				{
					iRet = NaturalMinorArpeggiatedExercise.length;
				}
			}
			else if (currentMinorType == 'H')
			{
				if ( currentExerciseType == 'S'  )
				{
					iRet = HarmonicMinorScalarExercise.length;
				}
				else if ( currentExerciseType == 'A'  )
				{
					iRet = HarmonicMinorArpeggiatedExercise.length;
				}
			}
			else if (currentMinorType == 'M')
			{
				if ( currentExerciseType == 'S'  )
				{
					iRet = MelodicMinorScalarExercise.length;
				}
				else if ( currentExerciseType == 'A'  )
				{
					iRet = MelodicMinorArpeggiatedExercise.length;
				}
			}
		}
		return iRet;

	}


	public int[] shuffleN (int n)
	{	int q;
		boolean[] inuse;
		int[] shuf;

		inuse = new boolean[n];
		shuf = new int[n];
		for (int i=0 ; i<n ; i++)
		{
			inuse[i]=false;
		}

		for (int i=(n-1) ; i>=0 ; i--)
		{
			q = (int)((Math.random() * n));
	    		while (inuse[q])
			{
				q++;
				if (q >= n)
				{
					q=0;
				}
			} //while
			shuf[q]=i;
			inuse[q]=true;
		} //for
		return shuf;
	} // shuffleN
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtSightSingExercises
//---------------------------------------------------------------------------------
