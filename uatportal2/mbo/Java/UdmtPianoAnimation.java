import java.awt.*;
import java.applet.*;
import java.net.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.sound.midi.*;

//------------------------------------------------------------------------------
// UdmtPianoAnimation - displays a GIF image and plays a command string sequence
// Version 0.02 - 4/9/2004 - added support for chords by using duration of 0
// Version 0.03 - 4/21/2004 - debugging construction of image URL
//------------------------------------------------------------------------------

public class UdmtPianoAnimation extends Applet implements MouseListener, MetaEventListener
{	
	private long miditime = 0;	// used for tracking duration of sequence

	private String parmImageURL = "";
	private URL imageURL;
	private Image imgInput;

	// Duration values to use for MIDI sequencing:
	private int durWhole = 16, durHalf = 8, durQtr = 4, dur8th = 2, dur16th= 1, dur32nd = 1, durZero=0;

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
	private Sequence 	seq;
	private Track 		track;
	MidiEvent 		event;
	ShortMessage 		msg;

	private int noteoffctr = 0;
	private int[] noteoffs;

	private int cbindex;
	private int codeBaseLen;
	private String codebase;
	private char[] codeBaseChars;
	private char[] parmImageChars;

	private UdmtPianoKeyboard muPiano;


	public void init()
	{
		rectScreen = getBounds();

		noteoffs = new int[8] ; // allow up to 8 simultaneous notes may be played in one chord

		parmCmdStr = getParameter ("cmdStr");
		if (parmCmdStr == null)
		{
			showAbendScreen = 1;
			txtAbend = "Param: cmdStr is required";
			System.out.println ("This applet requires string of music drawing commands");
			System.out.println ("to be passed in the parameter: cmdStr");
		}
		else
		{
			//System.out.println ("Retrieved Parameter: cmdStr");
			//System.out.println (parmCmdStr);
			
			arrCmdStr = parmCmdStr.toCharArray();
			lenCmdStr = parmCmdStr.length();
			//System.out.println ("Number of characters:"+lenCmdStr);
			
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

 		muPiano 		= new UdmtPianoKeyboard();
		muPiano.CropImage 	( imgInput, 0,0,573,100 );

		System.out.println("Attempting to start MIDI...");
		try	{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();     
			sequencer.addMetaEventListener(this);            
			seq = new Sequence(Sequence.PPQ, 4);         
			track = seq.createTrack();            
		}
		catch ( Exception e )	{
			this.showStatus("ERROR STARTING MIDI");
			System.out.println("ERROR: Exception when opening Java MIDI sequencer:");
			e.printStackTrace();
		}
		System.out.println("MIDI initialized successfully.");

		try	{

			for (int i=0 ; i<lenCmdStr ; i++)
			{
				processCmdChar (arrCmdStr[i]);
			}

			addEndOfTrackEvent ( miditime );
		}
		catch ( Exception e )	{
			this.showStatus("ERROR CREATING MIDI SEQUENCE");
			System.out.println("ERROR: Exception when building sequence:");
			e.printStackTrace();
		}
		
		borderColor = Color.white;

		addMouseListener(this);
	}


	public void addNoteEvent ( long mtime, int stsbyte, int chnl, int notenum, int vel)
	{
		//System.out.println ("addNoteEvent: mtime="+mtime+" stsbyte="+stsbyte+" chnl="+chnl+" notenum="+notenum+" vel="+vel);
		
		try {		
			msg = new ShortMessage();
			msg.setMessage(stsbyte, chnl, notenum, vel);

			event = new MidiEvent(msg, mtime);
			track.add(event);

			//Add a "Lyric" meta event so we can synchronize the piano keyboard drawing

			byte researchbytes[] = { 2, 0, 0 };
			if (vel > 0)
			{
				researchbytes[1] = 35;
			}
			else
			{
				researchbytes[1] = 33;	
			}
			researchbytes[2] = (byte)notenum;

			MetaMessage mymsg = new MetaMessage();
			mymsg.setMessage(01, researchbytes, 3);
			MidiEvent myevent = new MidiEvent(mymsg, mtime);
			track.add(myevent);
		}
		catch ( Exception e )	{
			this.showStatus("ERROR ADDING MESSAGE TO TRACK");
			System.out.println("ERROR: in addNoteEvent - unable to add short message to track");
			e.printStackTrace();
		}
	}

	public void addEndOfTrackEvent (long mtime)
	{
		//System.out.println ("addEndOfTrackEvent");

		byte blankbytes[] = { 0 };

		try
		{
			MetaMessage mymsg = new MetaMessage();
			mymsg.setMessage(47, blankbytes, 0);
			MidiEvent myevent = new MidiEvent(mymsg, mtime);
			track.add(myevent);
		}
		catch (InvalidMidiDataException e)
		{   
			System.out.println("Invalid MIDI Data Exception"); 
	    		e.printStackTrace();
		}
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
		// Check for Research bytes and if found, update piano keyboard display
		if (msg.getType() == 1)
		{
			byte[] theMessage = msg.getData();
			if (theMessage[1] == 35)
			{
				//System.out.println("NOTE ON :"+theMessage[2]);
				int midinote = (int)theMessage[2];
				muPiano.setPianoDot(midinote,1);
			}
			else if (theMessage[1] == 33)
			{
				//System.out.println("NOTE OFF:"+theMessage[2]);
				int midinote = (int)theMessage[2];
				muPiano.setPianoDot(midinote,0);
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

		if (playStatus == +1)
		{
			stopMidiPlayback();
			borderColor = Color.blue;
		}
		else
		{
			startMidiPlayback();
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
		
		g.drawString("Please open the Java Console for more information",10,40);					
		g.drawString("and contact systems support.",10,55);		
	}

//------------------------------------------------------------------------------

	public void drawScreen( Graphics g )
	{
		g.setColor (borderColor);
		g.fillRect(0, 0, rectScreen.width, rectScreen.height);
		
		Image pianoImage = muPiano.getImage();
		int pianoLeft = borderX;
		int pianoTop = borderY;
		int pianoRight = pianoLeft + muPiano.getWidth();
		int pianoBottom = pianoRight + muPiano.getHeight();
		muPiano.setScreenCoords (pianoLeft, pianoTop, pianoRight, pianoBottom); 
		g.drawImage (pianoImage, pianoLeft, pianoTop, this);

		for (int midinote = 36 ; midinote <= 84 ; midinote++)
		{
			int midix = muPiano.getPianoDotX(midinote);
			int midiy = muPiano.getPianoDotY(midinote);
			int midiRadius = muPiano.getPianoDotRadius();

			if (muPiano.getPianoDot(midinote) == 1)
			{
				g.setColor (Color.red);
			}
			else
			{
				if (muPiano.getDefaultPianoKeyColor(midinote) == 1)
				{
					g.setColor (Color.white);
				}
				else
				{
					g.setColor (new Color(37,37,37));
				}
			}
			g.fillArc (midix - midiRadius, midiy - midiRadius, 2*midiRadius, 2*midiRadius, 0, 360);		
		}
		g.setColor (Color.black);

	}

//------------------------------------------------------------------------------

	private void processCmdChar ( char charIn )
	{

		char c;

		//System.out.println ("Processing Character: "+c);

		//Convert to Uppercase 
		c = Character.toUpperCase(charIn);
		
		if (mode == MODE_NONE)	{

			  switch (c)	{
				case 'C': case 'D': case 'E': case 'F': case 'G': case 'A': case 'B': {
					//System.out.println("Mode changed to: MODE_NOTE");
					mode = MODE_NOTE;
					savedNoteLetter = c;
					break;
				}
				case 'O': {
					//System.out.println("Mode changed to: MODE_OCT");
					mode = MODE_OCT;
					break;
				}
				case 'R': {
					//System.out.println("Mode changed to: MODE_REST");
					mode = MODE_REST;
					break;
				}
				case ' ': {
					// Ignore whitespace
					break;
				}
				case 'X': {
					// Dummy command - used if last command on line doesn't render
					break;
				}
				default : {
					System.out.println("ERROR: Unsupported command character: "+c);
					break;
				}
			  } // switch
		}
		else if (mode == MODE_NOTE) {
			//System.out.println("MODE_NOTE");
			modeNote (c);
		}
		else if (mode == MODE_OCT) {
			//System.out.println("MODE_OCT");
			modeOct (c);
		}
		else if (mode == MODE_REST) {
			//System.out.println("MODE_REST");
			modeRest (c);
		}


	} // processCmdChar

//------------------------------------------------------------------------------

	private void modeNote ( char c)
	{
		
		if (c == '1') {
			addToSeq (currOctave, savedNoteLetter, accid2draw, durWhole, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durHalf, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durQtr, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur8th, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur16th, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, dur32nd, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '0')	{
			addToSeq (currOctave, savedNoteLetter, accid2draw, durZero, numDots );
			accid2draw = 0;
			mode = MODE_NONE;
		}
		else if (c == '#')	{
			accid2draw=+1;
		}
		else if (c == '&')	{
			accid2draw=-1;
		}
		else if (c == '%')	{
			accid2draw=+0;
		}
		else if (c == '*')	{
			accid2draw=+2;
		}
		else if (c == '@')	{
			accid2draw=-2;
		}
		else if (c == '.')	{
			numDots = 1;
		}
		else if (c == ':')	{
			numDots = 2;
		}
		//else if (c == '~')	{
		//	beamDivision=3;
		//}
		else if (c == '^')	{
		}
		else if (c == '!')	{
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
	}

//------------------------------------------------------------------------------

	private void modeOct ( char c)
	{
		if ((c >= '1') && (c <= '5'))	{
			switch (c)
			{
				case '1': {  currOctave = 1; break;  }
				case '2': {  currOctave = 2; break;  }
				case '3': {  currOctave = 3; break;  }
				case '4': {  currOctave = 4; break;  }
				case '5': {  currOctave = 5; break;  }
			}
		}
		else if (c == '+')	{
			currOctave++;
			if (currOctave >= 5)	{
				currOctave = 5;
			}
		}
		else if (c == '-')	{
			currOctave--;
			if (currOctave <= 1)	{
				currOctave = 1;
			}
		}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
	}

//------------------------------------------------------------------------------

	private void modeRest ( char c)
	{
		if (c == '1') {
			addRestToSeq (durWhole, numDots );
			mode = MODE_NONE;
		}
		else if (c == '2')	{
			addRestToSeq (durHalf, numDots );
			mode = MODE_NONE;
		}
		else if (c == '4')	{
			addRestToSeq (durQtr, numDots );
			mode = MODE_NONE;
		}
		else if (c == '8')	{
			addRestToSeq (dur8th, numDots );
			mode = MODE_NONE;
		}
		else if (c == '6')	{
			addRestToSeq (dur16th, numDots );
			mode = MODE_NONE;
		}
		else if (c == '3')	{
			addRestToSeq (dur32nd, numDots );
			mode = MODE_NONE;
		}
		else if (c == '.')	{
			numDots = 1;
		}
		else if (c == ':')	{
			numDots = 2;
		}
		//Note: Triplets not required
		//else if (c == '~')	{
		//	beamDivision=3;
		//}
		else if (c == ' ')	{
			mode = MODE_NONE;
		}
	}

//------------------------------------------------------------------------------

	private void addToSeq (int oct, char noteLetter, int accid, int dur, int dots )
	{
		int midinotenum=0, mididur=0;
		int offset=0;

		//System.out.println ("Adding to sequence: "+noteLetter+" Accid="+accid+" Oct="+oct+" Dur="+dur+" dots="+dots);

		switch (noteLetter)	{
			case 'C': { offset = 0 ; break ; }
			case 'D': { offset = 2 ; break ; }
			case 'E': { offset = 4 ; break ; }
			case 'F': { offset = 5 ; break ; }
			case 'G': { offset = 7 ; break ; }
			case 'A': { offset = 9 ; break ; }
			case 'B': { offset = 11 ; break ; }
		}

		midinotenum = 36 + (12 * (oct-1)) + offset + accid ;

		switch (dots)  {
			case 0:	{  mididur = dur ; break ; }
			case 1:	{  mididur = dur + (dur/2) ; break ; }
			case 2:	{  mididur = dur + (dur/2) + (dur / 4) ; break ; }
		}

		//System.out.println ("Miditime="+miditime+" midinotenum="+midinotenum+" mididur="+mididur);

		addNoteEvent ( miditime, 144, 1, midinotenum, 100);
		if (dur == 0)
		{
			noteoffs[noteoffctr] = midinotenum;
			noteoffctr++;
			if (noteoffctr >= 7)
			{
				noteoffctr = 7;
				System.out.println ("ERROR: Attempt to play more than 8 simultaneous notes");
			}
		}
		else
		{
			addNoteEvent ( (long)(miditime + (mididur * legatoPercent )) , 128, 1, midinotenum, 0 );
			for (int i=0 ; i < noteoffctr ; i++)
			{
				addNoteEvent ( (long)(miditime + (mididur * legatoPercent )), 128, 1, noteoffs[i], 0 );
			}
			noteoffctr = 0;
		}
		miditime += dur;
	}

//------------------------------------------------------------------------------

	private void addRestToSeq (int dur, int dots)
	{
		long mididur=0;

		//System.out.println ("Adding Rest to sequence: Dur="+dur+" dots="+dots);

		switch (dots)  {
			case 0:	{  mididur = dur ; break ; }
			case 1:	{  mididur = dur + (dur/2) ; break ; }
			case 2:	{  mididur = dur + (dur/2) + (dur / 4) ; break ; }
		}
		//System.out.println ("mididur="+mididur);

		addNoteEvent ( mididur , 128, 1, 0, 0 );
	}

//------------------------------------------------------------------------------

	private void startMidiPlayback()
	{
		//System.out.println ("Starting MIDI Playback");

		try {
			sequencer.stop();			
			sequencer.setTempoInBPM( (float)240.0 );
			sequencer.setSequence(seq);
		}
		catch ( Exception e )	{
			this.showStatus("ERROR SETTING SEQUENCE");
			System.out.println("ERROR: in startMidiPlayback - unable to set sequence for sequencer");
			e.printStackTrace();
		}
		sequencer.start();			
		playStatus = +1;
	}

//------------------------------------------------------------------------------

	private void stopMidiPlayback()
	{
		//System.out.println ("Stopping MIDI Playback");
		sequencer.stop();			
		playStatus = -1;

		for (int midinote = 36 ; midinote <= 84 ; midinote++)
		{
			muPiano.setPianoDot(midinote,0);
		}
		repaint();
	}

//------------------------------------------------------------------------------

} // class
