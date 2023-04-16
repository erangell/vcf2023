//---------------------------------------------------------------------------------
public class UdmtChord extends Object
//---------------------------------------------------------------------------------
{
	private int rootMidi;
	private int secondMidi;
	private int thirdMidi;
	private int fourthMidi;
	private int fifthMidi;
	private int sixthMidi;
	private int seventhMidi;
	private int ninthMidi;
	private int eleventhMidi;
	private int thirteenthMidi;		
	private char quality; // M, m, +, o
	private int keysig; // 0 thru 11 for C thru B

	//-----------------------------------------

	public UdmtChord ()
	{
		rootMidi = -1;
		thirdMidi = -1;
		fifthMidi = -1;
		seventhMidi = -1;
		ninthMidi = -1;
		eleventhMidi = -1;
		thirteenthMidi = -1;		
	}

	public void setRootMidi( int notenum )
	{
		rootMidi = notenum;
	}
	public int getRootMidi()
	{
		return rootMidi;
	}
	public void setThirdMidi( int notenum )
	{
		thirdMidi = notenum;
	}
	public int getThirdMidi()
	{
		return thirdMidi;
	}
	public void setFifthMidi( int notenum )
	{
		fifthMidi = notenum;
	}
	public int getFifthMidi()
	{
		return fifthMidi;
	}

	//-----------------------------------------------------------------


	public char getTriadQuality()
	{
		char retch = '?';
		int distRootThird = thirdMidi - rootMidi;
		int distThirdFifth = fifthMidi - thirdMidi;

		if ((distRootThird == 4) && (distThirdFifth == 3)) retch='M';
		if ((distRootThird == 3) && (distThirdFifth == 4)) retch='m';
		if ((distRootThird == 4) && (distThirdFifth == 4)) retch='+';
		if ((distRootThird == 3) && (distThirdFifth == 3)) retch='o';
		return retch;
	}

	public void ConstructTriadFromRootMidi(int rootnote, char quality)
	{
		rootMidi = rootnote;
		switch (quality)
		{
			case 'M': {thirdMidi = rootMidi + 4; fifthMidi = rootMidi+7; break; }
			case 'm': {thirdMidi = rootMidi + 3; fifthMidi = rootMidi+7; break; }
			case '+': {thirdMidi = rootMidi + 4; fifthMidi = rootMidi+8; break; }
			case '-': {thirdMidi = rootMidi + 3; fifthMidi = rootMidi+6; break; }
		}
	}

}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtChord
//---------------------------------------------------------------------------------
