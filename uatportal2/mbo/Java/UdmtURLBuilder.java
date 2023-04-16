import java.net.*;
//------------------------------------------------------------------------------
// UdmtURLBuilder -constructs URL given codebase and relative path 
// 	If the path begins with ../ then the URL is changed to one directory
//    level above, and the remainder of the path is concatenated to the end.
//------------------------------------------------------------------------------

public class UdmtURLBuilder extends Object
{	
	private String filepath = "";
	private String codebase;
	private int cbindex;
	private int codeBaseLen;
	private char[] codeBaseChars;
	private char[] parmImageChars;

	public String buildURL ( URL codebaseURL, String filepath)
	{
		codebase = codebaseURL.toString();
	
		//System.out.println("Codebase="+ codebase);					
		//System.out.println("filepath="+filepath);
			
		codeBaseLen = codebase.length();
		codeBaseChars = codebase.toCharArray();
		parmImageChars = filepath.toCharArray();
			
		//System.out.println ("First char of filepath="+parmImageChars[0]);
		//System.out.println ("Second char of filepath="+parmImageChars[1]);
		//System.out.println ("Third char of filepath="+parmImageChars[2]);
			
		if ((parmImageChars[0] == '.') && (parmImageChars[1] == '.') && (parmImageChars[2] == '/'))
		{
			//System.out.println ("Last char of codebase="+codeBaseChars[codeBaseLen-1]);
		
			if (codeBaseChars[codeBaseLen-1] == '/')
			{
				cbindex = codeBaseLen - 2;
			}
			else
			{
				cbindex = codeBaseLen - 1;
			}
			while (codeBaseChars[cbindex] != '/')
			{
				cbindex--;
			}
		
			//System.out.println ("Adjusted codebase="+codebase.substring(0,cbindex+1));
			//System.out.println ("Adjusted filepath="+filepath.substring(3,filepath.length()) );

			filepath = codebase.substring(0,cbindex+1) + filepath.substring(3,filepath.length()) ;
		}
		else
		{
			filepath = codebase+ filepath;
		}

		//REMOVED 3/20/2005 - FOR DEBUGGING ONLY
		//System.out.println ("(In URLBuilder: filepath="+filepath+")");
		return filepath;
	}
//------------------------------------------------------------------------------
} // class
//------------------------------------------------------------------------------
