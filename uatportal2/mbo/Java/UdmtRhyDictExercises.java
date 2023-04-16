//---------------------------------------------------------------------------------
public class UdmtRhyDictExercises extends Object
//---------------------------------------------------------------------------------
{

private String[] level1Exercise = {
	"LG K0S  +25 o3 f4 +10 f4 +10 f4 X"	
,	"LG K0S  +25 o3 f4 +10 [f8 f8] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [ f.8 \\f6 ] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [/ f6 f.8  ] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [[ f6 f6 f6 f6 ] ] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [/ f6 f8 \\f6  ] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [ f8 [f6 f6]  ] +10 f4 X"
,	"LG K0S  +25 o3 f4 +10 [ [f6 f6] f8  ] +10  f4 X"
};

private String[] level2Exercise = {
	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ f.8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ /f6 f.8  ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ [f6 f6 f6 f6]  ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ /f6 f8 \\f6  ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ [f6 f6] f8  ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 f8 ] f4 [ f8 [f6 f6]   ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ f8 f8    ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ /f6 f.8    ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ [f6 f6 f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ /f6 f8 \\f6  ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ [f6 f6] f8  ] f4 X"
,	"LG K0S +10 o3 f4 [ f.8 \\f6 ] f4 [ f8 [f6 f6]  ] f4 X"
,	"LG K0S +10 o3 f4 [/ f6 f.8 ] f4 [ f8 f8  ] f4 X"
,	"LG K0S +10 o3 f4 [/ f6 f.8 ] f4 [ f.8 \\f6  ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f.8 ] f4 [ [f6 f6 f6 f6]  ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f.8 ] f4 [ /f6 f8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f.8 ] f4 [ [f6 f6] f8 ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f.8 ] f4 [ f8 [f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 f6 f6 ]] f4 [ f8 f8 ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 f6 f6 ]] f4 [ f.8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 f6 f6 ]] f4 [ /f6 f.8 ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6 f6 f6 ]] f4 [ /f6 f8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 f6 f6] ] f4 [[ f6 f6] f8 ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 f6 f6] ] f4 [ f8[ f6 f6 ]] f4 X"

,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ f8 f8  ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ f.8 \\f6  ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ /f6 f.8 ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ [f6 f6 f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ [f6 f6] f8 ] f4 X"
,	"LG K0S +10 o3 f4 [ /f6 f8 \\f6 ] f4 [ f8 [f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [[ f6 f6 ] f8 ] f4 [ f8 f8  ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6] f8 ] f4 [ f.8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6] f8 ] f4 [ /f6 f.8 ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6] f8 ] f4 [ [f6 f6 f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6] f8 ] f4 [ /f6 f8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [ [f6 f6] f8 ] f4 [ f8 [f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [ f8 f8 ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [ f.8 \\f6 ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [/f6 f.8 ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [ [f6 f6 f6 f6] ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [ /f6 f8 \\f6  ] f4 X"
,	"LG K0S +10 o3 f4 [ f8 [f6 f6] ] f4 [ [f6 f6] f8  ] f4 X"
};

	private String[] level3Exercise = {
	"LG K0S  +25 o3 f.4 +10 [ f8 f8 f8 ] +10 f.4 X"	
,	"LG K0S  +25 o3 f.4 +10 [ f.8 \\f6 f8 ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10 [ //f6 f8 f.8 ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10 [ f8 f.8 \\f6 ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10 [ f8 [f6 f6] f8 ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10 [ [f6 f6] f8 f8 ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10 [ f8 f8 [f6 f6] ] +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10  f4  f8  +10 f.4 X"
,	"LG K0S  +25 o3 f.4 +10  f8 f4  +10 f.4 X"
};

	private int[] level1ans = {
		6, 2, 0, 1, 7, 5, 3, 4 
	};

	private int[] level2ansA = {
		0,0,0,0,0,0,1,1,1,1,1,1,2,2,2,2,2,2,6,6,6,6,6,6
		,3,3,3,3,3,3,4,4,4,4,4,4,5,5,5,5,5,5
	};

	private int[] level2ansB = {
		1,2,6,3,4,5,0,2,6,3,4,5,0,1,6,3,4,5,0,1,2,3,4,5,
		0,1,2,6,4,5,0,1,2,6,3,5,0,1,2,6,3,4
	};
	private int[] level3ans = {
		1,8,2,5,0,3,6,7,4
	};

	private String[] level1Wav = {
	  "a01.wav","a02.wav","a03.wav","a04.wav"
	, "a05.wav","a06.wav","a07.wav","a08.wav"
	};

	private String[] level2Wav = {
	  "b01.wav","b02.wav","b03.wav","b04.wav"
	, "b05.wav","b06.wav","b07.wav","b08.wav"
	, "b09.wav","b10.wav","b11.wav","b12.wav"
	, "b13.wav","b14.wav","b15.wav","b16.wav"
	, "b17.wav","b18.wav","b19.wav","b20.wav"
	, "b21.wav","b22.wav","b23.wav","b24.wav"
	, "b25.wav","b26.wav","b27.wav","b28.wav"
	, "b29.wav","b30.wav","b31.wav","b32.wav"
	, "b33.wav","b34.wav","b35.wav","b36.wav"
	, "b37.wav","b38.wav","b39.wav","b40.wav"
	,"b41.wav","b42.wav"
	};

	private String[] level3Wav = {
	  "c01.wav","c02.wav","c03.wav","c04.wav"
	, "c05.wav","c06.wav","c07.wav","c08.wav"
	, "c09.wav"
	};

	private String[] testExercise = {
	"LG K0S o3 +10 [ f8 f8 f8 ] +10 [ f.8 \\f6 f8 ] +10 [ //f6 f8 f.8 ] +10 [ f8 f.8 \\f6 ] X"	
,	"LG K0S o3  +10 [ f8 [f6 f6] f8 ]  +10 [ [f6 f6] f8 f8 ]  +10 [ f8 f8 [f6 f6] ] +10  f4  f8  +10  f8 f4  X"
,	"LG K0S  +15 o3 f4 +10 [f8 f8] +10 [ f.8 \\f6 ] +10 [/ f6 f.8  ] X"	
,	"LG K0S  +15 o3 f4 [[ f6 f6 f6 f6 ] ] +10 [/ f6 f8 \\f6  ] +10 [ f8 [f6 f6]  ] +10 [ [f6 f6] f8  ]  X"
};

/***OLD:
	private String[] oldSimpleMeterExercise = {
	"LG K0S +5 T4/4 +15 o3 f4 f4 r4 [f8 f8] X"	
	,"LG K0S +5 T4/4 +15 o3 [f8 f8] r8 f8 [f8 f8] f4 X"	
	,"LG K0S +5 TC/8 +15 o3 f.4 f.4 f4 f8 "
	,"LG K0S +5 T4/4 +15 o3 f4 f4 [f~8 f`~8 f~8] [f~8 f`~8 f~8] X"
	,"LG K0S +5 TC/8 +15 o3 f.4 [f8 f8 f8] f4 f8 f.4 X"
	,"LG K0S +5 T4/4 +15 o3 f8 f4 f8 f.4 f8 X"	
	,"LG K0S +5 T4/4 +15 o3 f4 [f8 f8] r8 f8 [[f6 f6 f6 f6]] X"
	,"LG K0S +5 T6/8 +15 o3 [f8 f8 f8] [[f6 f6 f6 f6 f6 f6]] X"
	,"LG K0S +5 T4/4 +15 o3 f4  [f~8 f`~8 f~8] [f~8 [f`~6 f~6] f~8] f4 X"
	,"LG K0S +5 T3/2 +15 o3 f2 f4 f4 [f8 f8 f8 f8] X"	
	,"LG K0S +5 T4/4 +15 o3 f4 [f.8 \\f6] [f.8 \\f6] f4 X"	//Note: backslash is an escape character
	};
		private String[] CompoundMeterExercise = {
	"LG K0S +5 T6/8 +20 o3 f6 f6 f6 f6 f6 f6 f6 f6 f6 f6 f6 f6 X"
	};
***/


	private int currentKeyNumSF = 0;
	private char currentKeySF = 'S';	//S=sharps F=flats
	private char currentKeyMajMin = 'M'; // M=major m=minor
	private char currentMinorType = 'N';	// N=natural H=harmonic M=melodic
	private char currentExerciseType = 'S'; // S=scalar A=arpeggiated
	private int currentExerciseNum = 0;	// index to exercise array
	private int currentWavNum = 0;	// index to exercise array


	// Constructor:
	public void UdmtRhydictExercises()
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
		if ( currentExerciseType == '1'  )
		{
			sRet = level1Exercise[currentExerciseNum];
			currentExerciseNum++;
			if (currentExerciseNum >= level1Exercise.length)
			{
				currentExerciseNum=0;
			}
		}
		else if ( currentExerciseType == '2'  )
		{
			sRet = level2Exercise[currentExerciseNum];
			currentExerciseNum++;
			if (currentExerciseNum >= level2Exercise.length)
			{
				currentExerciseNum=0;
			}
		}
		else if ( currentExerciseType == '3'  )
		{
			sRet = level3Exercise[currentExerciseNum];
			currentExerciseNum++;
			if (currentExerciseNum >= level3Exercise.length)
			{
				currentExerciseNum=0;
			}
		}
		else if ( currentExerciseType == 'T'  )
		{
			sRet = testExercise[currentExerciseNum];
			currentExerciseNum++;
			if (currentExerciseNum >= testExercise.length)
			{
				currentExerciseNum=0;
			}
		}


		return sRet;
	}	


	//-----------------------------------------
	public String getWavFile ( )
	{	
		String sRet = null;
		if ( currentExerciseType == '1'  )
		{
			sRet = level1Wav[currentWavNum];
			currentWavNum++;
			if (currentWavNum >= level1Wav.length)
			{
				currentWavNum=0;
			}
		}
		else if ( currentExerciseType == '2'  )
		{
			sRet = level2Wav[currentWavNum];
			currentWavNum++;
			if (currentWavNum >= level2Wav.length)
			{
				currentWavNum=0;
			}
		}
		else if ( currentExerciseType == '3'  )
		{
			sRet = level3Wav[currentWavNum];
			currentWavNum++;
			if (currentWavNum >= level3Wav.length)
			{
				currentWavNum=0;
			}
		}

		return sRet;
	}	


	//-----------------------------------------
	public String getWavFileNum ( int n )
	{	
		String sRet = null;
		if ( currentExerciseType == '1'  )
		{
			sRet = level1Wav[n];
		}
		else if ( currentExerciseType == '2'  )
		{
			sRet = level2Wav[n];
		}
		else if ( currentExerciseType == '3'  )
		{
			sRet = level3Wav[n];
		}

		return sRet;
	}	


	public int getAnswerA()
	{
		int iRet = -1;
		if ( currentExerciseType == '1'  )
		{
			iRet = level1ans[currentExerciseNum];
		}
		else if ( currentExerciseType == '2'  )
		{
			iRet = level2ansA[currentExerciseNum];
		}
		else if ( currentExerciseType == '3'  )
		{
			iRet = level3ans[currentExerciseNum];
		}
		return iRet;
	}
	public int getAnswerB()
	{
		int iRet = -1;
		if ( currentExerciseType == '2'  )
		{
			iRet = level2ansB[currentExerciseNum];
		}
		else
		{
			iRet = -1;
		}
		return iRet;
	}

	//-----------------------------------------
	public String getExerciseNum( int n )
	{	
		String sRet = null;
		if ( currentExerciseType == '1'  )
		{
			sRet = level1Exercise[n];
		}
		else if ( currentExerciseType == '2'  )
		{
			sRet = level2Exercise[n];
		}
		else if ( currentExerciseType == '3'  )
		{
			sRet = level3Exercise[n];
		}
		else if ( currentExerciseType == 'T'  )
		{
			sRet = testExercise[n];
		}

		return sRet;
	}	

	public int getAnswerNumA( int n )
	{
		int iRet = -1;
		if ( currentExerciseType == '1'  )
		{
			iRet = level1ans[n];
		}
		else if ( currentExerciseType == '2'  )
		{
			iRet = level2ansA[n];
		}
		else if ( currentExerciseType == '3'  )
		{
			iRet = level3ans[n];
		}
		return iRet;
	}
	public int getAnswerNumB( int n )
	{
		int iRet = -1;
		if ( currentExerciseType == '2'  )
		{
			iRet = level2ansB[n];
		}
		else
		{
			iRet = -1;
		}
		return iRet;
	}



	public void setExerciseType ( char exSubType)
	{
		currentExerciseType  = exSubType;
	}

	public int getNumExercises()
	{
		int iRet=0;
		if ( currentExerciseType == '1'  )
		{
			iRet =level1Exercise.length;
		}
		else if ( currentExerciseType == '2'  )
		{
			iRet = level2Exercise.length;
		}
		else if ( currentExerciseType == '3'  )
		{
			iRet = level3Exercise.length;
		}
		else if ( currentExerciseType == 'T'  )
		{
			iRet = testExercise.length;
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
// END OF CLASS UdmtRhydictExercises
//---------------------------------------------------------------------------------
