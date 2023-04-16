import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;


//------------------------------------------------------------------------------
// UdmtAudioPlayer - displays image and plays a wav file when clicked
//------------------------------------------------------------------------------

public class UdmtPlayWav extends Applet implements MouseListener
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

	private String 		parmWavFile;
	private String 		codebase;
	private String 		auFilePath;
	private URL		auURL;

	private int playStatus = -1 ; 	// -1 = stopped, +1 = playing


	private int cbindex;
	private int codeBaseLen;
	private char[] codeBaseChars;
	private char[] parmImageChars;

	private Object currentSound;
	private String currentName;

	public void init()
	{
		rectScreen = getBounds();

		parmWavFile = getParameter ("aufile");
		if (parmWavFile == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: aufile is required";
			System.out.println ("This applet requires a .WAV filename");
			System.out.println ("to be passed in the parameter: aufile");
		}
		else
		{
			System.out.println("Loading WAV file: "+parmWavFile);
			URL url = null;
			try
			{
				url = new URL (this.getCodeBase() + parmWavFile);
				loadWavFile(url);
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
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
			codebase = this.getCodeBase().toString();
			codeBaseLen = codebase.length();
			codeBaseChars = codebase.toCharArray();
			parmImageChars = parmImageURL.toCharArray();
						
			if ((parmImageChars[0] == '.') && (parmImageChars[1] == '.') && (parmImageChars[2] == '/'))
			{			
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
			
				parmImageURL = codebase.substring(0,cbindex+1) + parmImageURL.substring(3,parmImageURL.length()) ;
			}
			else
			{
				parmImageURL = this.getCodeBase() + parmImageURL;
			}

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


	public boolean loadWavFile(URL object) 
	{
		try 
		{
			currentSound = AudioSystem.getAudioInputStream((URL) object);
		} 
		catch(Exception e) 
		{
			System.out.println(e.toString());
		}

		if (currentSound instanceof AudioInputStream) 
		{
			try 
			{
				AudioInputStream stream = (AudioInputStream) currentSound;
				AudioFormat format = stream.getFormat();

				/**
				 * we can't yet open the device for ALAW/ULAW playback,
				 * convert ALAW/ULAW to PCM
				 */
				if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
					(format.getEncoding() == AudioFormat.Encoding.ALAW)) 
				{
					AudioFormat tmp = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, 
						format.getSampleRate(),
						format.getSampleSizeInBits() * 2,
						format.getChannels(),
						format.getFrameSize() * 2,
						format.getFrameRate(),
						true);
					stream = AudioSystem.getAudioInputStream(tmp, stream);
					format = tmp;
				}
				DataLine.Info info = new DataLine.Info(
					Clip.class, 
					stream.getFormat(), 
					((int) stream.getFrameLength() *
					format.getFrameSize()));

				Clip clip = (Clip) AudioSystem.getLine(info);
				//clip.addLineListener(this);
				clip.open(stream);
				currentSound = clip;
			} 
			catch (Exception ex) 
			{ 
				ex.printStackTrace(); 
				currentSound = null;
				return false;
			}
		} 
		
		return true;
	}


	public void playSound() 
	{
		Clip clip = (Clip) currentSound;
		clip.start();
		while (clip.isActive()) 
		{
		}
		clip.stop();
		//clip.close();
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
		System.out.println ("StartPlayback");
		playStatus = +1;
		//play(getCodeBase(), parmWavFile);
		playSound();
		
	}

//------------------------------------------------------------------------------

	private void stopPlayback()
	{
		//System.out.println ("Stopping Audio Playback");
		playStatus = -1;
	}

//------------------------------------------------------------------------------

} // class
