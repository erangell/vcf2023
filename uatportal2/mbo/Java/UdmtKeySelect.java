//---------------------------------------------------------------------------------
public class UdmtKeySelect extends Object
//---------------------------------------------------------------------------------
{
	private int[] prob;
	private int probindex;
	private int testkey = -5;
	private int testtimes = 8;

	// Constructor:
	public void UdmtKeySelect()
	{
	}

	// default probab distrib to +/- 5 sharps/flats.
	// for advanced mode (all keys), administrator will override param in html.
	public void initKeySelect()
	{
		prob = new int[15];
		setProb (-7,0);
		setProb (-6,0);
		setProb (-5,9);
		setProb (-4,9);
		setProb (-3,9);
		setProb (-2,9);
		setProb (-1,9);
		setProb (+0,10);
		setProb (+1,9);
		setProb (+2,9);
		setProb (+3,9);
		setProb (+4,9);
		setProb (+5,9);
		setProb (+6,0);
		setProb (+7,0);
		//for (int x=0 ; x<=14 ; x++)
		//{	System.out.println ("x="+x+" prob="+prob[x]);
		//}
	}

	public void setProb (int k, int pct)
	{
		prob[k+7]=pct;
	}
	
	public int getRandKey()
	{	int k;
		int sum=0;
		int r= (int)((Math.random() * 100));
		//System.out.println ("r="+r);
		int ix=0;

		// make sure you start at first nonzero probability (so you dont get 7 flats when r=0)
		while ((prob[ix]==0) && (ix < 14))
		{
			ix++;
		}

		sum += prob[ix];
		while ((sum < r) && (ix < 14))
		{
			ix++;
			sum += prob[ix];
		}
		return ix-7;
	}
	public int getTestKey()
	{	int retval = testkey;
		testtimes--;
		if (testtimes <= 0)
		{
			testtimes = 8;
			testkey++;
			if (testkey > 5)
			{
				testkey = -5;
			}
		}
		return retval;
	}
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtKeySelect
//---------------------------------------------------------------------------------
