import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

//------------------------------------------------------------------------------
// UdmtAudioPlayer - displays image and plays an audio file when clicked
//------------------------------------------------------------------------------
// version 0.02 - 4/30/04 - added code to adjust codebase for finding the image URL
//------------------------------------------------------------------------------

public class UdmtAudioPlayer extends Applet implements MouseListener
{	
	private String parmImageURL = "";
	private URL imageURL;
	private Image imgInput;

	private int 		borderX = 2;	// border between applet and image
	private int 		borderY = 2;
	private Color		borderColor;

	private Rectangle 	rectScreen;
	private Image 		imageScreen;

	private int 		showAbendScreen;
	private String 		txtAbend;

	private String 		parmAuFile;
	private String 		codebase;
	private String 		auFilePath;
	private URL		auURL;

	private int playStatus = -1 ; 	// -1 = stopped, +1 = playing


	private int cbindex;
	private int codeBaseLen;
	private char[] codeBaseChars;
	private char[] parmImageChars;


	public void init()
	{
		rectScreen = getBounds();

		parmAuFile = getParameter ("aufile");
		if (parmAuFile == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: aufile is required";
			System.out.println ("This applet requires a .au filename");
			System.out.println ("to be passed in the parameter: aufile");
		}
		else
		{			
//			codebase = getCodeBase();
//			System.out.println ("codebase="+codebase);

//			auFilePath = codebase + parmAuFile;
// 			System.out.println ("auFilePath="+auFilePath);

//			try	{
//				auURL = new URL(auFilePath);
//			}
//			catch ( MalformedURLException e )	{
//				showAbendScreen = 1;
//				txtAbend = "Malformed URL Exception when creating URL from parmAuFile";
//				e.printStackTrace();
//			}
//			catch ( Exception e )	{
//				showAbendScreen = 1;
//				txtAbend = "Exception when creating URL from parmAuFile";
//				e.printStackTrace();
//			}
			
		}

		parmImageURL = getParameter ("imageURL");
		if (parmImageURL == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: imageURL is required";
			System.out.println ("This applet requires an URL containing the image to be drawn");
			System.out.println ("to be passed in the parameter: imageURL");
		}
		else
		{
			// 4/30/04 - Replaced following line with code below to adjust codebase URL to find image
			//parmImageURL = this.getCodeBase() + parmImageURL;

			//System.out.println("Codebase="+this.getCodeBase());
			//System.out.println("parmImageUrl="+parmImageURL);
			
			codebase = this.getCodeBase().toString();
			codeBaseLen = codebase.length();
			codeBaseChars = codebase.toCharArray();
			parmImageChars = parmImageURL.toCharArray();
			
			//System.out.println ("First char of parmImageUrl="+parmImageChars[0]);
			//System.out.println ("Second char of parmImageUrl="+parmImageChars[1]);
			//System.out.println ("Third char of parmImageUrl="+parmImageChars[2]);
			
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
				//System.out.println ("Adjusted parmimage="+parmImageURL.substring(3,parmImageURL.length()) );

				parmImageURL = codebase.substring(0,cbindex+1) + parmImageURL.substring(3,parmImageURL.length()) ;
			}
			else
			{
				parmImageURL = this.getCodeBase() + parmImageURL;
			}

			//System.out.println ("parmImageURL="+parmImageURL);

			try	{
				imageURL = new URL(parmImageURL);
			}
			catch ( MalformedURLException e )	{
				showAbendScreen = 1;
				txtAbend = "Malformed URL Exception when creating URL from parmImageURL";
				e.printStackTrace();
			}
			catch ( Exception e )	{
				showAbendScreen = 1;
				txtAbend = "Exception when creating URL from parmImageURL";
				e.printStackTrace();
			}
		}

		MediaTracker mt = new MediaTracker(this);
		URL url = getCodeBase();
		imgInput = getImage (imageURL);

		mt.addImage (imgInput, 1);
		
		try 
		{
			mt.waitForAll();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		borderColor = Color.white;

		addMouseListener(this);
	}


//------------------------------------------------------------------------------
// BEGIN: Implementation of MouseListener:
//------------------------------------------------------------------------------

	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("mouseClicked");
 	}

//------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) 
	{
		//System.out.println("mousePressed");

		if (playStatus == +1)
		{
			stopPlayback();
			borderColor = Color.blue;
		}
		else
		{
			startPlayback();
			borderColor = Color.green;
		}
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseReleased(MouseEvent e) 
	{
		//System.out.println("mouseReleased");
    	}

//------------------------------------------------------------------------------

    	public void mouseEntered(MouseEvent e) 
	{	
		//System.out.println("mouseEntered");

		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        	setCursor(cursor);

		stopPlayback();

		borderColor = Color.blue;
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseExited(MouseEvent e) 
	{
		//System.out.println("mouseExited");
		
		stopPlayback();
	
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        	setCursor(cursor);

		borderColor = Color.white;
		repaint();
    	}

//------------------------------------------------------------------------------
// END: Implementation of MouseListener
//------------------------------------------------------------------------------

	public void paint( Graphics g )
	{
		if (showAbendScreen == 1)
		{
			g.setColor (Color.red);
			g.fillRect(0, 0, rectScreen.width, rectScreen.height);

			g.setColor (Color.black);
			drawAbendScreen (g);
		}
		else
		{			
			drawScreen (g);
		}
	}

//------------------------------------------------------------------------------

	public void update( Graphics g ) 
	{
		imageScreen = createImage (rectScreen.width, rectScreen.height);
		paint (imageScreen.getGraphics());
		g.drawImage (imageScreen, 0, 0, null);
        }
 
//------------------------------------------------------------------------------

	public void drawAbendScreen ( Graphics g )
	{
		Font txtfont;
		txtfont = new Font ("Arial", Font.BOLD, 14);
		g.setFont (txtfont);

		g.drawString("THE FOLLOWING ERROR HAS OCCURRED:", 10, 10);
		g.drawString(txtAbend, 10, 25);
		
		g.drawString("Please open the Java Console for more information",10,40);					g.drawString("and contact systems support.",10,55);		
	}

//------------------------------------------------------------------------------

	public void drawScreen( Graphics g )
	{
		g.setColor (borderColor);
		g.fillRect(0, 0, rectScreen.width, rectScreen.height);
		g.drawImage (imgInput, borderX, borderY, this);
	}

//------------------------------------------------------------------------------

	private void startPlayback()
	{
		//System.out.println ("Starting Audio Playback");
		playStatus = +1;
		play(getCodeBase(), parmAuFile);
	}

//------------------------------------------------------------------------------

	private void stopPlayback()
	{
		//System.out.println ("Stopping Audio Playback");
		playStatus = -1;
	}

//------------------------------------------------------------------------------

} // class
