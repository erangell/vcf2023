import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.sound.midi.*;

//------------------------------------------------------------------------------
// UdmtPianoPlayer - displays image and responds to mouse clicks to play notes according to piano keyboard map
// version 0.01 - 2/6/2004 - initial version
// version 0.02 - 4/21/2004 - modified to adjust image URL path if it contains a relative path with "../"
//------------------------------------------------------------------------------


public class UdmtPianoPlayer extends Applet implements MouseListener, MouseMotionListener, MetaEventListener
{	
	private long miditime = 0;	// used for tracking duration of sequence


	private String parmImageURL = "";
	private URL imageURL;
	private Image imgInput;

	// Duration values to use for MIDI sequencing:
	private int durWhole = 16, durHalf = 8, durQtr = 4, dur8th = 2, dur16th= 1, dur32nd = 1;

	private double legatoPercent = 0.75;	// percentage of duration to keep the note on.

	private int 		borderX = 2;	// border between applet and image
	private int 		borderY = 2;
	private Color		borderColor;

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

	private Rectangle 	rectScreen;
	private Image 		imageScreen;

	private int 		showAbendScreen;
	private String 		txtAbend;

	private String 		parmCmdStr;
	private char[] 		arrCmdStr;
	private int 		lenCmdStr;

	private int 		currOctave = 3;		// Octaves run from C to B.  Octave 3 has Middle C.
	private char		savedNoteLetter = ' ';

	private int numDots=0;
	private int accid2draw=0;

	private int playStatus = -1 ; 	// -1 = stopped, +1 = playing

	// Objects required for MIDI Playback:
	private Sequencer 	sequencer;



	private int		cursorX, cursorY;
	private String 		param = "";
	private int		xMin=0,xMax=100,yMin=0,yMax=100,yMid=50;
	private int[]		xTop,xBot;
	private int		xTopCount=0,xBotCount=0;
	private int		midiTop=61,midiBot=60;
	private int 		midi=60;
	private int		userMidiNote=60;

	private boolean		bShowGrid = false;

	private int cbindex;
	private int codeBaseLen;
	private String codebase;
	private char[] codeBaseChars;
	private char[] parmImageChars;


	public void init()
	{
		rectScreen = getBounds();

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
				txtAbend = "Malformed URL Exception when creating URL from parmImageURL: "+parmImageURL;
				e.printStackTrace();
			}
			catch ( Exception e )	{
				showAbendScreen = 1;
				txtAbend = "Exception when creating URL from parmImageURL: "+parmImageURL;
				e.printStackTrace();
			}
		}

 		if((param = getParameter("xTop")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			xTopCount = st.countTokens();           
 			xTop = new int[ xTopCount];            
			for(int i=0; i<xTopCount; i++) {            
				xTop[i] = Integer.parseInt(st.nextToken());          
				//System.out.println("i="+i+"xTop="+ xTop[i] );
			}
		}
 		if((param = getParameter("xBot")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			xBotCount = st.countTokens();           
 			xBot = new int[ xBotCount];            
			for(int i=0; i<xBotCount; i++) {            
				xBot[i] = Integer.parseInt(st.nextToken());          
				//System.out.println("i="+i+"xBot="+ xBot[i] );
			}
		}
 		if((param = getParameter("yMid")) != null) {
			yMid = Integer.parseInt(param);
		}
 		if((param = getParameter("yMin")) != null) {
			yMin = Integer.parseInt(param);
		}
 		if((param = getParameter("yMax")) != null) {
			yMax = Integer.parseInt(param);
		}
 		if((param = getParameter("xMin")) != null) {
			xMin = Integer.parseInt(param);
		}
 		if((param = getParameter("xMax")) != null) {
			xMax = Integer.parseInt(param);
		}
 		if((param = getParameter("midiTop")) != null) {
			midiTop = Integer.parseInt(param);
		}
 		if((param = getParameter("midiBot")) != null) {
			midiBot = Integer.parseInt(param);
		}

 		if((param = getParameter("showGrid")) != null) {
			if (param.equals("YES"))
			{
				bShowGrid = true;
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


		System.out.println("Attempting to start MIDI...");
		try	{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();     
			sequencer.addMetaEventListener(this);            
			         
		}
		catch ( Exception e )	{
			this.showStatus("ERROR STARTING MIDI");
			System.out.println("ERROR: Exception when opening Java MIDI sequencer:");
			e.printStackTrace();
		}
		System.out.println("MIDI initialized successfully.");
		
		borderColor = Color.white;

		addMouseListener(this);
		addMouseMotionListener(this);
	}

//-----------------------------------------------------------------------------------------
// BEGIN: Implementation of MetaEventListener:
//
// The following method is called whenever a Meta event is processed by the sequencer.
// We are checking for meta event type 47 to determine if the end of the sequence has been reached.
//-----------------------------------------------------------------------------------------
	public void meta(MetaMessage msg)
	{
		if (msg.getType() == 47)
		{
			//System.out.println ("End of track event found");
			stopMidiPlayback();
			borderColor = Color.blue;
			repaint();
		}
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
		Sequence 	seq;
		Track 		track;
		MidiEvent 	event, event2;
		ShortMessage 	msg, msg2;

		//System.out.println("mousePressed");

		cursorX = e.getX();
		cursorY = e.getY();

		if (    (cursorX >= xMin) && (cursorX <= xMax)
		     && (cursorY >= yMin) && (cursorY <= yMax)
		   )
		{		
			userMidiNote = calcMidiNote (cursorX,cursorY);
			//System.out.println("userMidiNote="+userMidiNote);
	
			stopMidiPlayback();

			try {    
				seq = new Sequence(Sequence.PPQ, 4);    
				track = seq.createTrack();  
	
				msg = new ShortMessage();
				msg.setMessage (144, 1, userMidiNote , 100);
				event = new MidiEvent(msg, 0);
				track.add(event);
	
				msg2 = new ShortMessage();
				msg2.setMessage (128, 1, userMidiNote , 0);
				event2 = new MidiEvent(msg, 5);
				track.add(event);

				byte blankbytes[] = { 0 };
				MetaMessage mymsg = new MetaMessage();
				mymsg.setMessage(47, blankbytes, 0);
				MidiEvent myevent = new MidiEvent(mymsg, 6);
				track.add(myevent);

				sequencer.stop();			
				sequencer.setTempoInBPM( (float)240.0 );
				sequencer.setSequence(seq);

			}
			catch ( Exception ex )	{
				this.showStatus("ERROR BUILDING SEQUENCE");
				System.out.println("ERROR: unable to build sequence for sequencer");
				ex.printStackTrace();
			}

			sequencer.start();			
			playStatus = +1;
		
		}

		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseReleased(MouseEvent e) 
	{
		//System.out.println("mouseReleased");

		cursorX = e.getX();
		cursorY = e.getY();
		//System.out.println ("Cursor at: "+cursorX+","+cursorY);
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseEntered(MouseEvent e) 
	{	
		//System.out.println("mouseEntered");

		Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        	setCursor(cursor);

		stopMidiPlayback();

		borderColor = Color.blue;
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseExited(MouseEvent e) 
	{
		//System.out.println("mouseExited");
		
		stopMidiPlayback();
	
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        	setCursor(cursor);

		borderColor = Color.white;
		repaint();
    	}

//------------------------------------------------------------------------------
// END: Implementation of MouseListener
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// Implementation of MouseMotionListener
//------------------------------------------------------------------------------

    	public void mouseMoved (MouseEvent e) 
	{
		//System.out.println("mouseMoved");

		cursorX = e.getX();
		cursorY = e.getY();

		repaint();
    	}
//-----------------------------------------------

    	public void mouseDragged (MouseEvent e) 
	{
		//System.out.println("mouseDragged");

		cursorX = e.getX();
		cursorY = e.getY();

		repaint();
    	}

//------------------------------------------------------------------------------
// END: Implementation of MouseMotionListener
//------------------------------------------------------------------------------

	private int calcMidiNote (int cx, int cy)
	{
		int ix;
		int startingOctave, startingOffset, ixOctave, ixOffset;
		int[] whiteKeyOffset = {0,2,4,5,7,9,11};
		int whiteIndex;

		if (cy <= yMid)
		{
			// upper part of piano

			ix = 0;
			while ( (ix < xTopCount) && (cx > xTop[ix]) )
			{
				ix++;            
			}
			
			midi = midiTop + ix;
		}
		else
		{
			// lower part of piano

			ix = 0;
			while ( (ix < xBotCount) && (cx > xBot[ix]) )
			{
				ix++;            
			}
			//System.out.println ("ix="+ix);

			startingOctave = midiBot / 12;	
			startingOffset = midiBot % 12;

			//System.out.println ("startingOctave="+startingOctave+"startingOffset="+startingOffset);

			whiteIndex = 0;
			while ( (whiteIndex < 6) && ( startingOffset > whiteKeyOffset[whiteIndex] ) )
			{
				whiteIndex++;            
			}
			//System.out.println ("whiteIndex="+whiteIndex);
			
			ixOctave = (whiteIndex + ix) / 7;
			ixOffset = (whiteIndex + ix) % 7;

			//System.out.println ("ixOctave="+ixOctave+"ixOffset="+ixOffset);

			midi = (startingOctave + ixOctave) * 12 + whiteKeyOffset[ixOffset] ;

			//System.out.println ("midi="+midi);
		}
		return midi;
	}

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

	

		if (bShowGrid)
		{
			g.setColor (Color.green);
			g.drawLine (0,cursorY,rectScreen.width,cursorY);
			g.drawLine (cursorX, 0, cursorX, rectScreen.height);

			g.setColor (Color.red);
			g.drawString (Integer.toString(cursorX)+
				","+Integer.toString(cursorY), 10, 75);
			g.drawString (Integer.toString(userMidiNote), 100, 75);


			g.setColor (Color.red);
			g.drawLine (xMin, yMid, xMax, yMid);

			for (int i=0 ; i < xTopCount; i++)
			{
				g.drawLine (xTop[i],yMin, xTop[i],yMid);
			}
			for (int i=0 ; i < xBotCount; i++)
			{
				g.drawLine (xBot[i],yMid, xBot[i],yMax);
			}
		}
	}

//------------------------------------------------------------------------------

	private void stopMidiPlayback()
	{
		//System.out.println ("Stopping MIDI Playback");
		sequencer.stop();			
		playStatus = -1;
	}

//------------------------------------------------------------------------------

} // class
