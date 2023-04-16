//---------------------------------------------------------------------------------
public class UdmtTimeSigExercises extends Object
//---------------------------------------------------------------------------------
{
	private String[] TsigExercise = {
       "LG +15 T2/2 X" 
	,"LG +15 T2/K X"	// K in denominator will make it draw Cut Time
	,"LG +15 T3/2 X"
	,"LG +15 T4/2 X"
	,"LG +15 T5/2 X"
	,"LG +15 T6/2 X"
	,"LG +15 T7/2 X"
	,"LG +15 T9/2 X"
	,"LG +15 TC/2 X"
	,"LG +15 TF/2 X"
	,"LG +15 T2/4 X"
	,"LG +15 T3/4 X"
	,"LG +15 T4/4 X"	
	,"LG +15 T4/K X"	// K in denominator will make it draw Common Time	
	,"LG +15 T5/4 X"
	,"LG +15 T6/4 X"
	,"LG +15 T7/4 X"
	,"LG +15 T9/4 X"
	,"LG +15 TC/4 X"
	,"LG +15 TF/4 X"
	,"LG +15 T2/8 X"
	,"LG +15 T3/8 X"
	,"LG +15 T4/8 X" 
	,"LG +15 T5/8 X"
	,"LG +15 T6/8 X"
	,"LG +15 T7/8 X"
	,"LG +15 T9/8 X"
	,"LG +15 TC/8 X"
	,"LG +15 TF/8 X"
	};

	private int currentKeyNumSF = 0;
	private char currentKeySF = 'S';	//S=sharps F=flats
	private char currentKeyMajMin = 'M'; // M=major m=minor
	private char currentMinorType = 'N';	// N=natural H=harmonic M=melodic
	private char currentExerciseType = 'S'; // S=scalar A=arpeggiated
	private int currentExerciseNum = 0;	// index to exercise array


	// Constructor:
	public void UdmtTimeSigExercises()
	{
	}

	//-----------------------------------------
	public String getExercise( )
	{	
		String sRet = null;
		sRet = TsigExercise[currentExerciseNum];
		currentExerciseNum++;
		if (currentExerciseNum >= TsigExercise.length)
		{
			currentExerciseNum=0;
		}
		return sRet;
	}	


	//-----------------------------------------
	public String getExerciseNum( int n )
	{	
		String sRet = TsigExercise[n];
		return sRet;
	}	

	//-----------------------------------------
	public int getNumExercises()
	{
		int iRet = TsigExercise.length;
		return iRet;

	}

	//-----------------------------------------
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
// END OF CLASS UdmtTimeSigExercises
//---------------------------------------------------------------------------------
