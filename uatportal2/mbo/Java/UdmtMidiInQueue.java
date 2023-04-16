import javax.sound.midi.*;

public class UdmtMidiInQueue
implements Receiver
{
	private long[] msgTime;
	private int[]  msgChnl;
	private int[]  msgNote;
	private int[]  msgVel;
	private int queueSize = 1000;	// circular buffer holds 1000 note events
	private int msgIndex = -1;	// -1 if no msgs since queue created
	
	private boolean			bRecording = false;

	public UdmtMidiInQueue()
	{	msgTime = new long[queueSize];
		msgChnl = new int[queueSize];
		msgNote = new int[queueSize];
		msgVel  = new int[queueSize];
	}

	public void close()
	{
	}

	public void startRecording()
	{	bRecording = true;
	}

	public void stopRecording()
	{	bRecording = false;
	}

	public int getMidiInQueueSize()
	{	return(queueSize);
	}

	public int getMidiInQueuePtr()
	{	return(msgIndex);		
	}

	public long getMidiInQueueTime(int index)
	{	return(msgTime[index]);
	}

	public int getMidiInQueueChnl(int index)
	{	return(msgChnl[index]);
	}

	public int getMidiInQueueNote(int index)
	{	return(msgNote[index]);
	}

	public int getMidiInQueueVel(int index)
	{	return(msgVel[index]);
	}

	private void bumpIndex()
	{
		msgIndex++;
		if (msgIndex == 1000)
		{    msgIndex = 0;
		}
	}
	public void send(MidiMessage message, long lTimeStamp)
	{
		if (bRecording && (message instanceof ShortMessage))
		{
			ShortMessage myShortMessage = (ShortMessage) message;
			if (myShortMessage.getCommand() == 0x90)
			{
			    bumpIndex();
  			    msgTime[msgIndex] = lTimeStamp;
			    msgChnl[msgIndex] = myShortMessage.getChannel();
			    msgNote[msgIndex] = myShortMessage.getData1();
			    msgVel[msgIndex]  = myShortMessage.getData2();
			}
			else if (myShortMessage.getCommand() == 0x80)
			{
			    bumpIndex();
  			    msgTime[msgIndex] = lTimeStamp;
			    msgChnl[msgIndex] = myShortMessage.getChannel();
			    msgNote[msgIndex] = myShortMessage.getData1();
			    msgVel[msgIndex]  = 0;
			}
		}
	}
}
