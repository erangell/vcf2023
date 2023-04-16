/* Midi Out Queue */

//2006-04-14: Added functionality to synchronize piano keyboard display

import javax.sound.midi.*;

public class UdmtMidiOutQueue 
            implements MetaEventListener
{
    private int numDevs;
    private String[] allDevices;
    private boolean[] allDevInput, allDevOutput;
    private int numIn, numOut;
    private String[] midiInDevices, midiOutDevices;
    private int[] midiInDeviceNums, midiOutDeviceNums;
    private int currentMidiInDeviceNum = 0, currentMidiOutDeviceNum = 0;
    private int prevMidiInDeviceNum, prevMidiOutDeviceNum;

    private MidiDevice.Info[] myMidiDeviceInfo;
    private MidiDevice myMidiInDevice, myMidiOutDevice;
    private Sequencer mySequencer;
    private Synthesizer mySynth ;
    private Receiver myReceiver;
    private Transmitter myTransmitter, myMidiInTransmitter;
    private boolean midiInActive = false, midiOutActive = false;

    public  UdmtMidiInQueue myMidiInReceiver;

    public  boolean syncPianoKeyboard = false;
    public  UdmtPianoKeyboard udmtPianoKeyboard;

    private Sequence userSequence;
    private Track[] userTracks;
//---------------------------------------------------------------------------------------------
    public String[] GMImntList = {
"Piano","Bright Piano","Electric Grand","Honky Tonk Piano","Electric Piano 1","Electric Piano 2","Harpsichord","Clavinet",
"Celesta","Glockenspiel","Music Box","Vibraphone","Marimba","Xylophone","Tubular Bell","Dulcimer",
"Hammond Organ","Perc Organ","Rock Organ","Church Organ","Reed Organ","Accordion","Harmonica","Tango Accordion",
"Nylon Str Guitar","Steel String Guitar","Jazz Electric Gtr",
"Clean Guitar","Muted Guitar","Overdrive Guitar","Distortion Guitar","Guitar Harmonics",
"Acoustic Bass","Fingered Bass","Picked Bass","Fretless Bass","Slap Bass 1","Slap Bass 2","Syn Bass 1","Syn Bass 2",
"Violin","Viola","Cello","Contrabass","Tremolo Strings","Pizzicato Strings","Orchestral Harp","Timpani",
"Ensemble Strings","Slow Strings","Synth Strings 1","Synth Strings 2","Choir Aahs","Voice Oohs","Syn Choir","Orchestra Hit",
"Trumpet","Trombone","Tuba","Muted Trumpet","French Horn","Brass Ensemble","Syn Brass 1","Syn Brass 2",
"Soprano Sax","Alto Sax","Tenor Sax","Baritone Sax","Oboe","English Horn","Bassoon","Clarinet",
"Piccolo","Flute","Recorder","Pan Flute","Bottle Blow","Shakuhachi","Whistle","Ocinara",
"Syn Square Wave","Syn Saw Wave","Syn Calliope","Syn Chiff","Syn Charang","Syn Voice","Syn Fifths Saw","Syn Brass and Lead",
"Fantasia","Warm Pad","Polysynth","Space Vox","Bowed Glass","Metal Pad","Halo Pad","Sweep Pad",
"Ice Rain","Soundtrack","Crystal","Atmosphere","Brightness","Goblins","Echo Drops","Sci Fi",
"Sitar","Banjo","Shamisen","Koto","Kalimba","Bag Pipe","Fiddle","Shanai",
"Tinkle Bell","Agogo","Steel Drums","Woodblock","Taiko Drum","Melodic Tom","Syn Drum","Reverse Cymbal",
"Guitar Fret Noise","Breath Noise","Seashore","Bird","Telephone","Helicopter","Applause","Gunshot"
};
//---------------------------------------------------------------------------------------------
    public UdmtMidiOutQueue()
    {    getMidiDevices();
	 setMidiOutDevice( 0 );	//default to first device (Java 1.4 always uses internal soundcard regardless)
	 initializeMidiOut();

	 if (midiInDeviceNums.length > 2)
	 {    setMidiInDevice( midiInDeviceNums[2] );   // default to first non-Java midi In device found
	 }
         else
	 {    setMidiInDevice( 0 );
	 }
	 initializeMidiIn();
    }

//---------------------------------------------------------------------------------------------
// Determine all installed midi devices and populate the arrays that contain the descriptions 
// and device numbers of the MIDI input and output devices.  
//---------------------------------------------------------------------------------------------
    private void getMidiDevices()
    {	
	int ixIn, ixOut;

	myMidiDeviceInfo = MidiSystem.getMidiDeviceInfo();
	numDevs = myMidiDeviceInfo.length;
	allDevices = new String[numDevs];
	allDevInput = new boolean[numDevs];
	allDevOutput = new boolean[numDevs];
	numIn = 0;   numOut = 0;
	for (int i = 0; i < numDevs; i++)
	{	try
		{	MidiDevice device = MidiSystem.getMidiDevice(myMidiDeviceInfo[i]);
			allDevices[i] = myMidiDeviceInfo[i].getName();
			if (device.getMaxTransmitters() != 0)
			{   allDevInput[i] = true;
			    numIn++;
			}
			if (device.getMaxReceivers() != 0)
			{   allDevOutput[i] =  true;	
			    numOut++;
			}
		}
		catch (MidiUnavailableException e)
		{	e.printStackTrace();
			numDevs = 0;
		}
	}
	midiInDevices = new String[numIn];
	midiOutDevices = new String[numOut];
	midiInDeviceNums = new int[numIn];
	midiOutDeviceNums = new int[numOut];
	ixIn = 0;  ixOut = 0;  	
	for (int j = 0; j < numDevs; j++)
	{
		if (allDevInput[j])
		{    midiInDevices[ixIn] = allDevices[j];
		     midiInDeviceNums[ixIn] = j;
		     ixIn++;
		}
		if (allDevOutput[j])
		{    midiOutDevices[ixOut] = allDevices[j];
		     midiOutDeviceNums[ixOut] = j;
		     ixOut++;
		}				
	} // for
    } // method getMidiDevices

//---------------------------------------------------------------------------------------------
    public String[] getMidiOutDevices()
    {    return midiOutDevices;
    }
//---------------------------------------------------------------------------------------------
    public int[] getMidiOutDeviceNums()
    {	 return midiOutDeviceNums;
    }
//---------------------------------------------------------------------------------------------
    public int getCurrMidiOutDeviceNum()
    {    return currentMidiOutDeviceNum;
    }
//--------------------------------------------------------------------------------------------- 
    public String getCurrMidiOutDeviceName()
    {    return allDevices[currentMidiOutDeviceNum];
    }
//---------------------------------------------------------------------------------------------
    public void setMidiOutDevice(int device)
    {    currentMidiOutDeviceNum = device;
    }
//---------------------------------------------------------------------------------------------
    public String[] getMidiInDevices()
    {    return midiInDevices;
    }
//---------------------------------------------------------------------------------------------
    public int[] getMidiInDeviceNums()
    {	 return midiOutDeviceNums;
    }
//---------------------------------------------------------------------------------------------
    public int getCurrMidiInDeviceNum()
    {    return currentMidiInDeviceNum;
    }
//--------------------------------------------------------------------------------------------- 
    public String getCurrMidiInDeviceName()
    {    return allDevices[currentMidiInDeviceNum];
    }
//--------------------------------------------------------------------------------------------- 
    public void setMidiInDevice(int device)
    {    currentMidiInDeviceNum = device;
    }

//---------------------------------------------------------------------------------------------
//  Use selected device numbers to initialize MIDI In and Out.
//  Midi Out Device # = midiOutDeviceNums[midiOutComboBox.getSelectedIndex()])
//  Midi IN Device # = midiInDeviceNums[midiInComboBox.getSelectedIndex()])
//---------------------------------------------------------------------------------------------
    public void initializeMidiOut()
    {	
	try 
	{   
	    if ((currentMidiOutDeviceNum >= 0) && (midiOutActive))
	    {   
		//System.out.println("Closing myMidiOutDevice: "+prevMidiOutDeviceNum);
		mySequencer.close();
		myMidiOutDevice.close();
	    }	    
	    myMidiOutDevice = MidiSystem.getMidiDevice( myMidiDeviceInfo[currentMidiOutDeviceNum] );
	    myMidiOutDevice.open();
	    mySequencer = MidiSystem.getSequencer();
	    myTransmitter = mySequencer.getTransmitter();
            mySequencer.open();
	    mySequencer.addMetaEventListener(this);
	    myTransmitter.setReceiver(myMidiOutDevice.getReceiver());

	    //System.out.println("Opened myMidiOutDevice: "+currentMidiOutDeviceNum);
	    midiOutActive = true;
	    prevMidiOutDeviceNum = currentMidiOutDeviceNum;

	}
	catch (MidiUnavailableException e)	    
	{   
	    System.out.println("MidiUnavailableException encountered");	    
	}	 

    }

//---------------------------------------------------------------------------------------------
    private void initializeMidiIn()
    {	
	try 
	{   
	    if ((currentMidiInDeviceNum >= 0)  && (midiInActive))
	    {	
		//System.out.println("Closing myMidiInDevice: "+prevMidiInDeviceNum);
		myMidiInDevice.close();
	    }
	    myMidiInDevice = MidiSystem.getMidiDevice( myMidiDeviceInfo[currentMidiInDeviceNum] );
	    myMidiInDevice.open();
	    myMidiInTransmitter = myMidiInDevice.getTransmitter();
	    
	    myMidiInReceiver = new UdmtMidiInQueue();
	    myMidiInTransmitter.setReceiver(myMidiInReceiver);

	    //System.out.println("Opened myMidiInDevice: "+currentMidiInDeviceNum);
	    midiInActive = true;
	    prevMidiInDeviceNum = currentMidiInDeviceNum;
	}
	catch (MidiUnavailableException e)	    
	{   
	    System.out.println("MidiUnavailableException encountered");	    
	}	 

    }

//---------------------------------------------------------------------------------------------
    public void playMetronome(float myTempo)
    {
	Sequence mySequence;
    	Track[] myTracks;
    	byte blankbytes[] = { 0 };

	System.out.println("testMidiOut");

	try
	{
	    mySequence = new Sequence(Sequence.PPQ, 24, 1);
	    myTracks = mySequence.getTracks();

    	    ShortMessage drummsg = new ShortMessage();
	    drummsg.setMessage (0x99, 42, 0x7F); // metronome click on channel 10
	    for (int t = 96; t <= 9600; t+=24)
	    {
	        myTracks[0].add(new MidiEvent(drummsg, t));
	    }

	    MetaMessage mymsg5 = new MetaMessage();
	    mymsg5.setMessage(0x2f, blankbytes, 0);
	    MidiEvent myevent5 = new MidiEvent(mymsg5,96);
	    myTracks[0].add(myevent5);

    	    mySequencer.setTempoInBPM(myTempo);
  	    mySequencer.setSequence(mySequence);
	    mySequencer.start();
   
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception in playMetronome"); 
	    e.printStackTrace();
	}
    }

//---------------------------------------------------------------------------------------------
    public void playSequence(Sequence mySequence, float myTempo)
    {
	try
	{
	    stopSequence();
	    mySequencer.setTempoInBPM(myTempo);
  	    mySequencer.setSequence(mySequence);
	    mySequencer.start();  
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception in playSequence"); 
	    e.printStackTrace();
	}	
    }

//---------------------------------------------------------------------------------------------
    public void playSequence(float myTempo)
    {
	playSequence ( userSequence, myTempo );
    }

//---------------------------------------------------------------------------------------------
    public void stopSequence()
    {
	if (mySequencer.isRunning())
	{
            mySequencer.stop();      
	}
    }

//---------------------------------------------------------------------------------------------
// The following method is called whenever a Meta event is processed by the sequencer.
// We are checking for meta event type 47 to determine if the end of the sequence has been reached.
//---------------------------------------------------------------------------------------------
    public void meta(MetaMessage msg)
    {
       if (msg.getType() == 47)
       {
	   if (mySequencer.isRunning())
	   {
               mySequencer.stop();      
	   }
       }
       else if (msg.getType() == 1)
       {   
	       udmtPianoKeyboard.ProcessMetaEvent(msg);
       }
    }

//---------------------------------------------------------------------------------------------
    public void createSequence()
    {
	try
	{
	    userSequence = new Sequence(Sequence.PPQ, 24, 1);
	    userTracks = userSequence.getTracks();
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception"); 
	    e.printStackTrace();
	}
    }
//---------------------------------------------------------------------------------------------
    public void addMidiMessage(long tick, int status, int data1, int data2)
    {
	try 
	{
	    ShortMessage mymsg = new ShortMessage();
	    mymsg.setMessage(status, data1, data2);    	    
	    MidiEvent myevent = new MidiEvent(mymsg, tick);	
	    userTracks[0].add(myevent);
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception"); 
	    e.printStackTrace();
	}
    }
//---------------------------------------------------------------------------------------------
    public void addNoteEvent(long tick, int channel, int notenum, int velocity)
    {
	// channel must be 0-15, notenum and velocity must be 0-127
	if (  (channel >= 0) && (channel <= 15)
           && (notenum >= 0) && (notenum <= 127)
           && (velocity >= 0) && (velocity <= 127)
           )
	{
	    addMidiMessage ( tick, 144 + channel, notenum, velocity);
	}
    }
//---------------------------------------------------------------------------------------------
    public void addNoteOn(long tick, int notenum)
    {
	addNoteEvent ( tick, 0, notenum, 64);
	if (syncPianoKeyboard)
	{
		addPianoDisplayMetaEvent(tick, notenum, 1);
	}
    }
//---------------------------------------------------------------------------------------------
    public void addNoteOff(long tick, int notenum)
    {
	addNoteEvent ( tick, 0, notenum, 0);
	if (syncPianoKeyboard)
	{
		addPianoDisplayMetaEvent(tick, notenum, 0);
	}
    }
//---------------------------------------------------------------------------------------------
    public void addProgChg(long tick, int prognum)
    {
	addMidiMessage (0, 0xC0, prognum, 0);
    }
//------------------------------------------------------------------------------------------
    public void addEndOfTrack(long tick)
    {
    	byte blankbytes[] = { 0 };

	try
	{
            MetaMessage mymsg = new MetaMessage();
	    mymsg.setMessage(47, blankbytes, 0);
	    MidiEvent myevent = new MidiEvent(mymsg, tick);
	    userTracks[0].add(myevent);
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception"); 
	    e.printStackTrace();
	}
    }
//------------------------------------------------------------------------------------------
    public void addPianoDisplayMetaEvent(long tick, int notenum, int vel)
    {

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

	try
	{
	    MetaMessage mymsg = new MetaMessage();
	    mymsg.setMessage(01, researchbytes, 3);
	    MidiEvent myevent = new MidiEvent(mymsg, tick);
	    userTracks[0].add(myevent);
	}
	catch (InvalidMidiDataException e)
	{   System.out.println("Invalid MIDI Data Exception"); 
	    e.printStackTrace();
	}
     }
//---------------------------------------------------------------------------------------------
} // END OF CLASS UdmtMidiOutQueue
//---------------------------------------------------------------------------------------------
