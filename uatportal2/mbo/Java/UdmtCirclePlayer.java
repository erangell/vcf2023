import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.sound.midi.*;

//------------------------------------------------------------------------------
// Circle of 5ths player
// Ver 0.02 - 4/30/04 - modified code for finding image URL
//------------------------------------------------------------------------------

public class UdmtCirclePlayer extends Applet implements MouseListener, MouseMotionListener, MetaEventListener
{	
	private String parmImageURL = "";
	private URL imageURL;
	private Image imgInput;

	// Duration values to use for MIDI sequencing:
	private int durWhole = 16, durHalf = 8, durQtr = 4, dur8th = 2, dur16th= 1, dur32nd = 1;

	private double legatoPercent = 0.75;	// percentage of duration to keep the note on.

	private int 		borderX = 2;	// border between applet and image
	private int 		borderY = 2;
	private Color		borderColor;

	private Color		arrowColor;
	private Color		knobColor;

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
	private Sequence 	seq;
	private Track 		track;
	private MidiEvent 	event1, event2;
	private ShortMessage 	msg1, msg2;
	private MetaMessage 	mymsg;
	private MidiEvent 	myevent;

	private long 		miditime;

	private int		cursorX, cursorY;
	private String 		param = "";
	private int		xMid=50,yMid=50,xMin=0,yMin=0,xMax=100,yMax=100;
	private int[]		Angle,midiNote;
	private int		AngleCount=0,midiNoteCount=0;
	private int		midiTop=61,midiBot=60;
	private int 		midi=60;
	private int		userMidiNote=60;

	private boolean		bShowGrid = false;

	private int		currNoteIndex = 0;

	private int knobRadius = 8;
	private int numArrowPts = 9;

	private int xArrow0[] =    {0, 5,   5,  10,   0, -10,  -5, -5, 0};
	private int yArrow0[] =    {0, 0, -45, -45, -55, -45, -45,  0, 0};

	private int xArrow30[] =   {0, -3,  21,  12,  28,  37,  28,  3, 0};
	private int yArrow30[] =   {0, -3, -41, -47, -48, -33, -37,  3, 0};

	private int xArrow60[] =   {0, 3,   42,  48,  48,  33,  37, -3, 0};
	private int yArrow60[] =   {0, 3,  -21, -12, -28, -37, -28, -3, 0};

	private int xArrow90[] =   {0, 0,  45,  45, 55, 45, 45, 0, 0};
	private int yArrow90[] =   {0, -5, -5, -10,  0, 10,  5, 5, 0};

	private int xArrow120[] =  {0, 3,   42,  48,  48,  33,  37, -3, 0};
	private int yArrow120[] =  {0, -3,  21, 12, 28, 37, 28, 3, 0};

	private int xArrow150[] =  {0, -3,  21,  12,  28,  37,  28,  3, 0};
	private int yArrow150[] =  {0, 3, 41, 47, 48, 33, 37,  -3, 0};

	private int xArrow180[] =  {0, -5, -5, -10,  0, 10,  5, 5, 0};
	private int yArrow180[] =  {0,  0, 45,  45, 55, 45, 45, 0, 0};

	private int xArrow210[] =  {0, 3,  -21, -12, -28, -37, -28, -3, 0};
	private int yArrow210[] =  {0, 3, 41, 47, 48, 33, 37,  -3, 0};

	private int xArrow240[] =  {0, -3, -42, -48, -48, -33, -37, 3, 0};
	private int yArrow240[] =  {0, -3,  21,  12,  28,  37,  28, 3, 0};

	private int xArrow270[] =  {0, 0, -45, -45, -55, -45, -45,  0, 0};
	private int yArrow270[] =  {0, 5,   5,  10,   0, -10,  -5, -5, 0};

	private int xArrow300[] =  {0, -3, -42, -48, -48, -33, -37,  3, 0};
	private int yArrow300[] =  {0, 3,  -21, -12, -28, -37, -28, -3, 0};

	private int xArrow330[] =  {0,  3, -21, -12, -28, -37, -28, -3, 0};
	private int yArrow330[] =  {0, -3, -41, -47, -48, -33, -37,  3, 0};

	private int cbindex;
	private int codeBaseLen;
	private String codebase;
	private char[] codeBaseChars;
	private char[] parmImageChars;

	//--------------------------------------------------------------------------------------------------------

	public void init()
	{
		rectScreen = getBounds();

		System.out.println ("rectScreen: width="+rectScreen.width);

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
			// 4/30/04 - replaced following line with code below to find correct image URL
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

 		if((param = getParameter("angles")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			AngleCount = st.countTokens();           
 			Angle = new int[ AngleCount];            
			for(int i=0; i<AngleCount; i++) {            
				Angle[i] = Integer.parseInt(st.nextToken());     
			}
		}
 		if((param = getParameter("midiNotes")) != null) {
			java.util.StringTokenizer st =                   
				new java.util.StringTokenizer(param,",");
			midiNoteCount = st.countTokens();           
 			midiNote = new int[ midiNoteCount];            
			for(int i=0; i<midiNoteCount; i++) {            
				midiNote[i] = Integer.parseInt(st.nextToken());          
			}
		}
 		if((param = getParameter("xMid")) != null) {
			xMid = Integer.parseInt(param);
		}
 		if((param = getParameter("yMid")) != null) {
			yMid = Integer.parseInt(param);
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
		arrowColor = Color.blue;
		knobColor = Color.blue;

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

			currNoteIndex ++;
	
			if (currNoteIndex < midiNoteCount)
			{

				try {
					seq = null;
					track = null;
					msg1 = null;
					msg2 = null;
					event1 = null;
					event2 = null;
					mymsg = null;
					myevent = null;

					seq = new Sequence(Sequence.PPQ, 4);    
					track = seq.createTrack();  
	
					miditime = 0;
					msg1 = new ShortMessage();
					msg2 = new ShortMessage();

					msg1.setMessage (144, 1, midiNote[currNoteIndex], 100);
					event1 = new MidiEvent(msg1, 0);
					track.add(event1);
	
					miditime += 3;

					msg2.setMessage (128, 1, midiNote[currNoteIndex] , 0);
					event2 = new MidiEvent(msg2, miditime);
					track.add(event2);

					miditime += 1;

					byte blankbytes[] = { 0 };
					mymsg = new MetaMessage();
					mymsg.setMessage(47, blankbytes, 0);
					myevent = new MidiEvent(mymsg, 6);
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
				borderColor = Color.green;
			}
			else
			{
				borderColor = Color.blue;
				currNoteIndex = 0;
			}

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

		//System.out.println("mousePressed");

		cursorX = e.getX();
		cursorY = e.getY();

		if (playStatus == -1) // only start playback when stopped
		{
			if (    (cursorX >= xMin) && (cursorX <= xMax)
			     && (cursorY >= yMin) && (cursorY <= yMax)
			   )
			{		
				userMidiNote = calcMidiNote (cursorX,cursorY);
				//System.out.println("userMidiNote="+userMidiNote);
	
				//stopMidiPlayback();
	
				try {    
					seq = null;
					track = null;
					msg1 = null;
					msg2 = null;
					event1 = null;
					event2 = null;
					mymsg = null;
					myevent = null;
	
					seq = new Sequence(Sequence.PPQ, 4);    
					track = seq.createTrack();  
		
					miditime = 0;
					msg1 = new ShortMessage();
					msg2 = new ShortMessage();

					msg1.setMessage (144, 1, midiNote[currNoteIndex], 100);
					event1 = new MidiEvent(msg1, 0);
					track.add(event1);
	
					miditime += 3;
	
					msg2.setMessage (128, 1, midiNote[currNoteIndex] , 0);
					event2 = new MidiEvent(msg2, miditime);
					track.add(event2);

					miditime += 1;

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
				borderColor = Color.green;

			} // if cursor within range
		} // if not playing
		
		repaint();
    	} // mousepressed

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

		Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        	setCursor(cursor);

		//stopMidiPlayback();

		borderColor = Color.blue;
		repaint();
    	}

//------------------------------------------------------------------------------

    	public void mouseExited(MouseEvent e) 
	{
		//System.out.println("mouseExited");
		
		//stopMidiPlayback();
	
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        	setCursor(cursor);

		borderColor = Color.white;
		//currNoteIndex = 0;

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
		int xPolygon[];
		int yPolygon[];

		xPolygon = new int[numArrowPts];
		yPolygon = new int[numArrowPts];

		g.setColor (Color.white);
		g.fillRect(0, 0, rectScreen.width, rectScreen.height);

		g.setColor (borderColor);
		g.drawImage (imgInput, borderX, borderY, this);
		g.drawRect(0, 0, rectScreen.width-1, rectScreen.height-1);
		g.drawRect(1, 1, rectScreen.width-3, rectScreen.height-3);

		g.setColor (arrowColor);

		// Calculate actual points for polygon for arrow being drawn
		for (int p=0 ; p < numArrowPts ; p++)
		{

			switch ( Angle[currNoteIndex] )
			{
				case 0: {
					xPolygon[p] = xMid + xArrow0[p];
					yPolygon[p] = yMid + yArrow0[p];
					break;
				}
				case 30: {
					xPolygon[p] = xMid + xArrow30[p];
					yPolygon[p] = yMid + yArrow30[p];
					break;
				}
				case 60: {
					xPolygon[p] = xMid + xArrow60[p];
					yPolygon[p] = yMid + yArrow60[p];
					break;
				}
				case 90: {
					xPolygon[p] = xMid + xArrow90[p];
					yPolygon[p] = yMid + yArrow90[p];
					break;
				}
				case 120: {
					xPolygon[p] = xMid + xArrow120[p];
					yPolygon[p] = yMid + yArrow120[p];
					break;
				}
				case 150: {
					xPolygon[p] = xMid + xArrow150[p];
					yPolygon[p] = yMid + yArrow150[p];
					break;
				}
				case 180: {
					xPolygon[p] = xMid + xArrow180[p];
					yPolygon[p] = yMid + yArrow180[p];
					break;
				}
				case 210: {
					xPolygon[p] = xMid + xArrow210[p];
					yPolygon[p] = yMid + yArrow210[p];
					break;
				}
				case 240: {
					xPolygon[p] = xMid + xArrow240[p];
					yPolygon[p] = yMid + yArrow240[p];
					break;
				}
				case 270: {
					xPolygon[p] = xMid + xArrow270[p];
					yPolygon[p] = yMid + yArrow270[p];
					break;
				}
				case 300: {
					xPolygon[p] = xMid + xArrow300[p];
					yPolygon[p] = yMid + yArrow300[p];
					break;
				}
				case 330: {
					xPolygon[p] = xMid + xArrow330[p];
					yPolygon[p] = yMid + yArrow330[p];
					break;
				}
			}

			//for testing:
			//xPolygon[p] = xMid + xArrow150[p];
			//yPolygon[p] = yMid + yArrow150[p];

		}
		g.fillPolygon ( xPolygon, yPolygon, numArrowPts );

		//System.out.println ("xMid="+xMid+" yMid="+yMid);

		g.setColor (knobColor);
		g.fillOval (xMid - knobRadius , yMid - knobRadius, knobRadius * 2, knobRadius * 2);

		xMin=0 ; yMin=0; xMax=rectScreen.width; yMax=rectScreen.height;

		if (bShowGrid)
		{
			g.setColor (Color.green);
			g.drawLine (xMid, yMid, cursorX, cursorY);

			g.setColor (Color.red);
			g.drawString (Integer.toString(cursorX)+
				","+Integer.toString(cursorY), 10, 75);
			g.drawString (Integer.toString(userMidiNote), 100, 75);
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
