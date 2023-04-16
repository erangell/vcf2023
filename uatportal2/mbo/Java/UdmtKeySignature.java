//---------------------------------------------------------------------------------
public class UdmtKeySignature extends Object
//---------------------------------------------------------------------------------
{
	// noteRow = Staff Y positions:
	// ------------------
	// 0 = 2nd leger line above staff
	// 1 = space above 1st leger line above staff
	// 2 = first leger line above staff
	// 3 = space above staff line 5
	// 4 = staff line 5
	// 5 = staff space 4
	// 6 = staff line 4
	// 7 = staff space 3
	// 8 = staff line 3
	// 9 = staff space 2
	// 10 = staff line 2
	// 11 = staff space 1
	// 12 = staff line 1
	// 13 = first space below staff line 1
	// 14 = first leger line below staff
	// 15 = space below 1st leger line below staff
	// 16 = 2nd leger line below staff

	private int savedKeySig;
	private int[] rowAccidTreble;
	private int[] rowAccidBass;

	private String _KeySigNameMajor;
	private String _KeySigNameMinor;

	// Constructor:
	public UdmtKeySignature()
	{
		rowAccidTreble = new int[17];
		rowAccidBass = new int[17];
		_KeySigNameMajor = "C";
		_KeySigNameMinor = "Am";
	}

	public void setKeySignature(int keysig)
	{
		savedKeySig = keysig;
		for (int i = 0; i < 17; i++)
		{
			rowAccidTreble[i] = 0;
			rowAccidBass[i] = 0;
		}

		_KeySigNameMajor = "C";
		_KeySigNameMinor = "Am";

		if (keysig < 0) // flats
		{
			// F
			_KeySigNameMajor = "F";
			_KeySigNameMinor = "Dm";

			rowAccidTreble[15] = +1;
			rowAccidTreble[8] = +1;
			rowAccidTreble[1] = +1;
			
			rowAccidBass[10] = +1;
			rowAccidBass[3] = +1;
			
			if (keysig < -1)	//Bb
			{
				_KeySigNameMajor = "Bb";
				_KeySigNameMinor = "Gm";

				rowAccidTreble[12] = +1;
				rowAccidTreble[5] = +1;
			
				rowAccidBass[14] = +1;
				rowAccidBass[7] = +1;			
				rowAccidBass[0] = +1;			
			}
			if (keysig < -2)	//Eb
			{
				_KeySigNameMajor = "Eb";
				_KeySigNameMinor = "Cm";

				rowAccidTreble[16] = +1;
				rowAccidTreble[9] = +1;
				rowAccidTreble[2] = +1;
			
				rowAccidBass[11] = +1;
				rowAccidBass[4] = +1;			
			}
			if (keysig < -3)	//Ab
			{
				_KeySigNameMajor = "Ab";
				_KeySigNameMinor = "Fm";

				rowAccidTreble[13] = +1;
				rowAccidTreble[6] = +1;
			
				rowAccidBass[15] = +1;
				rowAccidBass[8] = +1;	
				rowAccidBass[1] = +1;			
			}
			if (keysig < -4)	//Db
			{
				_KeySigNameMajor = "Db";
				_KeySigNameMinor = "Bbm";

				rowAccidTreble[10] = +1;
				rowAccidTreble[3] = +1;
			
				rowAccidBass[12] = +1;
				rowAccidBass[5] = +1;			
			}
			if (keysig < -5)	//Gb
			{
				_KeySigNameMajor = "Gb";
				_KeySigNameMinor = "Ebm";

				rowAccidTreble[14] = +1;
				rowAccidTreble[7] = +1;
				rowAccidTreble[0] = +1;
			
				rowAccidBass[16] = +1;
				rowAccidBass[9] = +1;			
				rowAccidBass[2] = +1;			

			}
			if (keysig < -6)	//Cb
			{
				_KeySigNameMajor = "Cb";
				_KeySigNameMinor = "Abm";

				rowAccidTreble[11] = +1;
				rowAccidTreble[4] = +1;
			
				rowAccidBass[13] = +1;
				rowAccidBass[6] = +1;			
			}
		}
		else if (keysig > 0) // sharps
		{
			//G
			_KeySigNameMajor = "G";
			_KeySigNameMinor = "Em";

			rowAccidTreble[11] = -1;
			rowAccidTreble[4] = -1;

			rowAccidBass[13] = -1;
			rowAccidBass[6] = -1;

			if (keysig > 1)		//D
			{
				_KeySigNameMajor = "D";
				_KeySigNameMinor = "Bm";

				rowAccidTreble[14] = -1;
				rowAccidTreble[7] = -1;
				rowAccidTreble[0] = -1;

				rowAccidBass[16] = -1;
				rowAccidBass[9] = -1;			
				rowAccidBass[2] = -1;			
			}
			if (keysig > 2)		//A
			{
				_KeySigNameMajor = "A";
				_KeySigNameMinor = "F#m";

				rowAccidTreble[10] = -1;
				rowAccidTreble[3] = -1;
			
				rowAccidBass[12] = -1;
				rowAccidBass[5] = -1;			
			}
			if (keysig > 3)		//E
			{
				_KeySigNameMajor = "E";
				_KeySigNameMinor = "C#m";

				rowAccidTreble[13] = -1;
				rowAccidTreble[6] = -1;
			
				rowAccidBass[15] = -1;
				rowAccidBass[8] = -1;	
				rowAccidBass[1] = -1;			
			}
			if (keysig > 4)		//B
			{
				_KeySigNameMajor = "B";
				_KeySigNameMinor = "G#m";

				rowAccidTreble[16] = -1;
				rowAccidTreble[9] = -1;
				rowAccidTreble[2] = -1;
			
				rowAccidBass[11] = -1;
				rowAccidBass[4] = -1;			
			}
			if (keysig > 5)		//F#
			{
				_KeySigNameMajor = "F#";
				_KeySigNameMinor = "D#m";

				rowAccidTreble[12] = -1;
				rowAccidTreble[5] = -1;
			
				rowAccidBass[14] = -1;
				rowAccidBass[7] = -1;			
				rowAccidBass[0] = -1;		
			}
			if (keysig > 6)		//C#
			{
				_KeySigNameMajor = "C#";
				_KeySigNameMinor = "A#m";

				rowAccidTreble[15] = -1;
				rowAccidTreble[8] = -1;
				rowAccidTreble[1] = -1;
			
				rowAccidBass[10] = -1;
				rowAccidBass[3] = -1;
			}

		}
	}

	public int getKeySigAccid (int clef, int noteRow)
	{
		//System.out.println("getKeySigAccid: clef="+clef+" keysig="+savedKeySig+" noteRow="+noteRow);
		int retval = 0;
		if (clef > 0)
		{
			retval = rowAccidTreble[noteRow];
		}
		else if (clef < 0)
		{
			retval = rowAccidBass[noteRow];
		}
		//System.out.println("getKeySigAccid: retval="+retval);
		return retval;
	}

	public String getKeySigNameMajor ()
	{
		return _KeySigNameMajor;
	}
	public String getKeySigNameMinor ()
	{
		return _KeySigNameMinor;
	}
	
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtKeySignature
//---------------------------------------------------------------------------------
