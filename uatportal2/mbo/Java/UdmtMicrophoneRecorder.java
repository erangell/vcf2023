import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.font.*;
import java.text.*;

/*** MODIFICATION HISTORY:

VERSION 0.002 - 2005-12-30: Changing recording time to 2 seconds

 ***/


/** ACKNOWLEDGMENTS:
 * 
 *  Capture/Playback sample.  Record audio in different formats
 *  and then playback the recorded audio.  The captured audio can 
 *  be saved either as a WAVE, AU or AIFF.  Or load an audio file
 *  for streaming playback.
 *
 * @version @(#)CapturePlayback.java	1.11	99/12/03
 * @author Brian Lichtenwalter  
 *
 *
 * Fast Fourier Transform routine adapted from:
 * http://sepwww.stanford.edu/oldsep/hale/FftLab.html
 * referenced in:
 * http://www.developer.com/java/other/article.php/3457251
 *
 */
public class UdmtMicrophoneRecorder extends Object	{
//JPanel implements ActionListener, UdmtControlContext {

    static String udmtVersion = "0.002";

    final int bufSize = 16384;

    FormatControls formatControls = new FormatControls();
    Capture capture = new Capture();
    Playback playback = new Playback();

    AudioInputStream audioInputStream;
    SamplingGraph samplingGraph = new SamplingGraph();

    JButton playB, captB, pausB, loadB;
    JButton auB, aiffB, waveB;
    JTextField textField;

    String fileName = "";
    String errStr;
    double duration, seconds;
    File file;

    //frequency magnitudes were experimentally determined
    int midibasenote = 39;

    static int[] freqmagn =    {-1,126
			,133,141,150,159,168,178,189
			,200,212,224,238,252,267,283,300,317,336,356,377
			,400,424,449,475,504,534,565,599,635,672,712,755
			,800,847,898,951,1007,1067,1131,1198,1269,1345,1425,1510
			,1599,65536};

    static String[] midinotename = {"ERROR","ERROR"
			,"F2","F#2","G2","G#2","A2","A#2","B2"
			,"C3","C#3","D3","D#3","E3","F3","F#3","G3","G#3","A3","A#3","B3"
			,"C4","C#4","D4","D#4","E4","F4","F#4","G4","G#4","A4","A#4","B4"
			,"C5","C#5","D5","D#5","E5","F5","F#5","G5","G#5","A5","A#5","B5"
			,"C6","ERROR"};

    public boolean savecsv = false;
    static boolean debugging = true;

    static String logfilename = "voicelog.txt";
	
    static FileWriter logfile;

    static int fileseq = 0;


    public boolean Recording = false;
    public boolean Playing = false;

    public String retMessage = "";
    public int retMidiNote = 0;
    public int retSharpOrFlat = 0;	//-1 = user sang flat, +1 = user sang sharp
    public int retRedGreenYellow = 0;	//-1 = red, 0 = green, 1 = yellow
    public int retPitchScore = 0;	// -100 to +100 cents (estimated)
    public String retMidiNoteName = "";

    public int numValidSamples = 0;
    public int[] validSamples = new int[88200];

    public int lowFreqFilter = 120;
    public int highFreqFilter = 2048;
    public double[] fftmagnitude = new double[65536];
    public int ixFundamental = 0;

    public int[] FlatRedLeft = {
	    0,  0
	,   0, 133, 141, 150, 159, 168, 178
	, 189, 200, 212, 224, 238, 252, 267, 283, 300, 317, 336, 356
	, 377, 400, 424, 449, 475, 504 ,534 ,565, 599, 635, 672, 712
	, 755, 800, 847, 898, 951,1007,1067,1131,1198,1269,1345,1425
	,1510
    };

    public int[] FlatRedRight = {0,0,
	125,133,141,150,159,168,178,189,201,213,225,239,253,268,284,301,318,338,358,379,403,426,452,478
	,508,537,569,604,639,677,717,761,806,854,905,959,1016,1076,1140,1208,1280,1357,1437,1523
    };

    public int[] FlatYellowLeft = {0,0,
	126,134,142,151,160,169,179,190,202,214,226,240,254,269,285,302,319,339,359,380,404,427,453,479
	,509,538,570,605,640,678,718,762,807,855,906,960,1017,1077,1141,1209,1281,1358,1438,1524
    };

    public int[] FlatYellowRight = {0,0,
	126,134,142,151,160,170,180,191,203,215,227,241,255,271,287,304,321,341,361,383,407,430,456,483
	,513,542,575,610,645,684,724,768,814,862,914,968,1026,1087,1151,1220,1293,1370,1451,1538
    };

    public int[] SharpYellowLeft = {0,0,
	131,139,148,157,166,176,186,197,209,221,234,248,263,279,295,312,330,350,371,393,417,441,467,495
	,525,555,589,624,660,700,741,786,832,882,934,989,1048,1111,1176,1246,1321,1399,1482,1570
    };

    public int[] SharpYellowRight = {0,0,
	131,139,148,157,166,176,187,198,209,222,235,249,264,280,297,314,332,353,373,396,420,444,471,499
	,529,559,593,629,665,705,747,792,839,889,942,998,1057,1120,1186,1257,1332,1412,1495,1584
    };

    public int[] SharpRedLeft = {0,0,
	132,140,149,158,167,177,188,199,210,223,236,250,265,281,298,315,333,354,374,397,421,445,472,500
	,530,560,594,630,666,706,748,793,840,890,943,999,1058,1121,1187,1258,1333,1413,1496,1585
    };

    public int[] SharpRedRight = {0,0,
	132,140,149,158,167,177,188,199,211,223,237,251,266,282,299,316,335,355,376,399,423,448,474,503
	,533,564,598,634,671,711,754,799,846,897,950,1006,1066,1130,1197,1268,1344,1424,1509,2048
    };


    public UdmtMicrophoneRecorder() {

    }


    public void open() { }


    public void close() {
        if (playback.thread != null) {
            playB.doClick(0);
        }
        if (capture.thread != null) {
            captB.doClick(0);
        }
    }

    public void StartRecording()
    {
                capture.start();
                samplingGraph.start();
		Recording = true;
    }
    public void StopRecording()
    {
                capture.stop();
                samplingGraph.stop();
		//Recording = false;
    }
    public void StartPlayback()
    {
                playback.start();
                samplingGraph.start();
		Playing = true;
    }
    public void StopPlayback()
    {
                playback.stop();
                samplingGraph.stop();
		Playing = false;
    }



    public void createAudioInputStream(File file, boolean updateComponents) {
        if (file != null && file.isFile()) {
            try {
                this.file = file;
                errStr = null;
                audioInputStream = AudioSystem.getAudioInputStream(file);
                playB.setEnabled(true);
                fileName = file.getName() ;

                long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
                duration = milliseconds / 1000.0;
                auB.setEnabled(true);
                aiffB.setEnabled(true);
                waveB.setEnabled(true);
                if (updateComponents) {
                    formatControls.setFormat(audioInputStream.getFormat());
                    samplingGraph.createWaveForm(null);
                }
            } catch (Exception ex) { 
                reportStatus(ex.toString());
            }
        } else {
            reportStatus("Audio file required.");
        }
    }


    public void saveToFile(String name, AudioFileFormat.Type fileType) {

        if (audioInputStream == null) {
            reportStatus("No loaded audio to save");
            return;
        } else if (file != null) {
            createAudioInputStream(file, false);
        }

        // reset to the beginnning of the captured data
        try {
            audioInputStream.reset();
        } catch (Exception e) { 
            reportStatus("Unable to reset stream " + e);
            return;
        }

        File file = new File(fileName = name);
        try {
            if (AudioSystem.write(audioInputStream, fileType, file) == -1) {
                throw new IOException("Problems writing to file");
            }
        } catch (Exception ex) { reportStatus(ex.toString()); }
        samplingGraph.repaint();
    }
        

    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.out.println(errStr);
            samplingGraph.repaint();
        }
    }


    /**
     * Write data to the OutputChannel.
     */
    public class Playback implements Runnable {

        SourceDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
            } 
        }

        public void run() {

            // reload the file if loaded by file
            if (file != null) {
                createAudioInputStream(file, false);
            }

            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream
            try {
                audioInputStream.reset();
            } catch (Exception e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }

            // get an AudioInputStream of the desired format for playback
            AudioFormat format = formatControls.getFormat();
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
                        
            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }

            // define the required attributes for our line, 
            // and make sure a compatible line is supported.

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, 
                format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the source data line for playback.

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) { 
                shutDown("Unable to open the line: " + ex);
                return;
            }

            // play back the captured audio data

            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead = 0;

            // start the source data line
            line.start();

            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0 ) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.  let the data play out, then
            // stop and close the line.
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
	    Playing = false;
        }
    } // End class Playback
        

    /** 
     * Reads data from the input channel and writes to the output stream
     */
    class Capture implements Runnable {

        TargetDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                samplingGraph.stop();
                loadB.setEnabled(true);
                playB.setEnabled(true);
                pausB.setEnabled(false);
                auB.setEnabled(true);
                aiffB.setEnabled(true);
                waveB.setEnabled(true);
                captB.setText("Record");
                System.err.println(errStr);
                samplingGraph.repaint();
            }
        }

        public void run() {

            duration = 0;
            audioInputStream = null;
            
            // define the required attributes for our line, 
            // and make sure a compatible line is supported.

            AudioFormat format = formatControls.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
                format);
                        
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the target data line for capture.

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) { 
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) { 
                shutDown(ex.toString());
                showInfoDialog();
                return;
            } catch (Exception ex) { 
                shutDown(ex.toString());
                return;
            }

            // play back the captured audio data
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
            
            line.start();

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;

            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // load bytes into the audio input stream for playback

            byte audioBytes[] = out.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

            long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;

            try {
                audioInputStream.reset();
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                return;
            }

            samplingGraph.createWaveForm(audioBytes);
        }
    } // End class Capture
 

    /**
     * Controls for the AudioFormat.
     */
    class FormatControls extends JPanel {
    
        Vector groups = new Vector();
        JToggleButton linrB, ulawB, alawB, rate8B, rate11B, rate16B, rate22B, rate44B;
        JToggleButton size8B, size16B, signB, unsignB, litB, bigB, monoB,sterB;
    
        public FormatControls() {
            setLayout(new GridLayout(0,1));
            EmptyBorder eb = new EmptyBorder(0,0,0,5);
            BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
            CompoundBorder cb = new CompoundBorder(eb, bb);
            setBorder(new CompoundBorder(cb, new EmptyBorder(8,5,5,5)));
            JPanel p1 = new JPanel();
            ButtonGroup encodingGroup = new ButtonGroup();
            linrB = addToggleButton(p1, encodingGroup, "linear", true);
            ulawB = addToggleButton(p1, encodingGroup, "ulaw", false);
            alawB = addToggleButton(p1, encodingGroup, "alaw", false);
            add(p1);
            groups.addElement(encodingGroup);
               
            JPanel p2 = new JPanel();
            JPanel p2b = new JPanel();
            ButtonGroup sampleRateGroup = new ButtonGroup();
            rate8B = addToggleButton(p2, sampleRateGroup, "8000", false);
            rate11B = addToggleButton(p2, sampleRateGroup, "11025", false);
            rate16B = addToggleButton(p2b, sampleRateGroup, "16000", false);
            rate22B = addToggleButton(p2b, sampleRateGroup, "22050", false);
            rate44B = addToggleButton(p2b, sampleRateGroup, "44100", true);
            add(p2);
	    add(p2b);
            groups.addElement(sampleRateGroup);
    
            JPanel p3 = new JPanel();
            ButtonGroup sampleSizeInBitsGroup = new ButtonGroup();
            size8B = addToggleButton(p3, sampleSizeInBitsGroup, "8", false);
            size16B = addToggleButton(p3, sampleSizeInBitsGroup, "16", true);
            add(p3);
            groups.addElement(sampleSizeInBitsGroup);
    
            JPanel p4 = new JPanel();
            ButtonGroup signGroup = new ButtonGroup();
            signB = addToggleButton(p4, signGroup, "signed", true);
            unsignB = addToggleButton(p4, signGroup, "unsigned", false);
            add(p4);
            groups.addElement(signGroup);

            JPanel p5 = new JPanel();
            ButtonGroup endianGroup = new ButtonGroup();
            litB = addToggleButton(p5, endianGroup, "little endian", false);
            bigB = addToggleButton(p5, endianGroup, "big endian", true);
            add(p5);
            groups.addElement(endianGroup);

            JPanel p6 = new JPanel();
            ButtonGroup channelsGroup = new ButtonGroup();
            monoB = addToggleButton(p6, channelsGroup, "mono", true);
            sterB = addToggleButton(p6, channelsGroup, "stereo", false);
            add(p6);
            groups.addElement(channelsGroup);
        }
    
        private JToggleButton addToggleButton(JPanel p, ButtonGroup g, 
                                     String name, boolean state) {
            JToggleButton b = new JToggleButton(name, state);
            p.add(b);
            g.add(b);
            return b;
        }

        public AudioFormat getFormat() {

            Vector v = new Vector(groups.size());
            for (int i = 0; i < groups.size(); i++) {
                ButtonGroup g = (ButtonGroup) groups.get(i);
                for (Enumeration e = g.getElements();e.hasMoreElements();) {
                    AbstractButton b = (AbstractButton) e.nextElement();
                    if (b.isSelected()) {
                        v.add(b.getText());
                        break;
                    }
                }
            }

            AudioFormat.Encoding encoding = AudioFormat.Encoding.ULAW;
            String encString = (String) v.get(0);
            float rate = Float.valueOf((String) v.get(1)).floatValue();
            int sampleSize = Integer.valueOf((String) v.get(2)).intValue();
            String signedString = (String) v.get(3);
            boolean bigEndian = ((String) v.get(4)).startsWith("big");
            int channels = ((String) v.get(5)).equals("mono") ? 1 : 2;

            if (encString.equals("linear")) {
                if (signedString.equals("signed")) {
                    encoding = AudioFormat.Encoding.PCM_SIGNED;
                } else {
                    encoding = AudioFormat.Encoding.PCM_UNSIGNED;
                }
            } else if (encString.equals("alaw")) {
                encoding = AudioFormat.Encoding.ALAW;
            }
            return new AudioFormat(encoding, rate, sampleSize, 
                          channels, (sampleSize/8)*channels, rate, bigEndian);
        }


        public void setFormat(AudioFormat format) {
            AudioFormat.Encoding type = format.getEncoding();
            if (type == AudioFormat.Encoding.ULAW) {
                ulawB.doClick();
            } else if (type == AudioFormat.Encoding.ALAW) {
                alawB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_SIGNED) {
                linrB.doClick(); signB.doClick(); 
            } else if (type == AudioFormat.Encoding.PCM_UNSIGNED) {
                linrB.doClick(); unsignB.doClick(); 
            }
            float rate = format.getFrameRate();
            if (rate == 8000) {
                rate8B.doClick();
            } else if (rate == 11025) {
                rate11B.doClick();
            } else if (rate == 16000) {
                rate16B.doClick();
            } else if (rate == 22050) {
                rate22B.doClick();
            } else if (rate == 44100) {
                rate44B.doClick();
            }
            switch (format.getSampleSizeInBits()) {
                case 8  : size8B.doClick(); break;
                case 16 : size16B.doClick(); break;
            }
            if (format.isBigEndian()) {
                bigB.doClick(); 
            } else { 
                litB.doClick();
            }
            if (format.getChannels() == 1) {
                monoB.doClick(); 
            } else { 
                sterB.doClick();
            }
        }
    } // End class FormatControls


    /**
     * Render a WaveForm.
     */
    class SamplingGraph extends JPanel implements Runnable {

        private Thread thread;
        private Font font10 = new Font("serif", Font.PLAIN, 10);
        private Font font12 = new Font("serif", Font.PLAIN, 12);
        Color jfcBlue = new Color(204, 204, 255);
        Color pink = new Color(255, 175, 175);
 

        public SamplingGraph() {
            setBackground(new Color(20, 20, 20));
        }


        public void createWaveForm(byte[] audioBytes) {

	    int numZeroCrossings = 0;
	    int prevAudioData = 0;
  	    int ixfft = 0;
	    float[] fftreal = new float[65536];
	    float[] fftimag = new float[65536];
	    //double[] fftmagnitude = new double[65536];

            //lines.removeAllElements();  // clear the old vector

            AudioFormat format = audioInputStream.getFormat();
            if (audioBytes == null) {
                try {
                    audioBytes = new byte[
                        (int) (audioInputStream.getFrameLength() 
                        * format.getFrameSize())];
                    audioInputStream.read(audioBytes);
                } catch (Exception ex) { 
                    reportStatus(ex.toString());
                    return; 
                }
            }

            //Dimension d = getSize();
            //int w = d.width;
            //int h = d.height-15;
            int[] audioData = null;
            if (format.getSampleSizeInBits() == 16) {
                 int nlengthInSamples = audioBytes.length / 2;
                 audioData = new int[nlengthInSamples];
                 if (format.isBigEndian()) {

      		    ixfft = 0;
		
  		    numValidSamples = 0;

                    for (int i = 0; i < nlengthInSamples; i++) 
		    {
                    	/* First byte is MSB (high order) */
                        int MSB = (int) audioBytes[2*i];
                        /* Second byte is LSB (low order) */
                        int LSB = (int) audioBytes[2*i+1];
                        audioData[i] = MSB << 8 | (255 & LSB);

			//APPLY HAMMING WINDOW

			double hamming = 0.53836 - 0.46164 * Math.cos(2 * Math.PI * numValidSamples / 65535);

			//Analyze Audio Data for 1st and 2nd seconds recorded
		        //Assuming 44100 samples per second

			validSamples[numValidSamples] = (int)(audioData[i] * hamming);
		
			if (((prevAudioData < 0) && (audioData[i] > 0)) || ((prevAudioData > 0) && (audioData[i] < 0)) || (audioData[i] == 0))
			{
				numZeroCrossings++;
		  	}

			if ((numValidSamples >= (44100-32768)) && (ixfft < 65536))
			{
				fftreal[ixfft] = validSamples[ixfft];
				fftimag[ixfft] = 0;
				ixfft++;
			}

			prevAudioData = audioData[i];
			numValidSamples++;
                     }


		     float freqGuess = numZeroCrossings / 4;		     
		     String msg="";     
		     //msg = numValidSamples+" samples\r\n" + numZeroCrossings+" zero crossings\r\nFrequency Guess: "+freqGuess+" Hz";
  		     //JOptionPane.showMessageDialog(null, msg, "Audio Data Collected", JOptionPane.INFORMATION_MESSAGE);
	     
		     if (savecsv)
		     {
			     System.out.println("======================================================================================");
	         	     try {

				 fileseq++;

				 String datafname = "dat"+fileseq+".csv";
	
			         //if (textField.getText().trim().equals(""))
		    		 //{
				 //      datafname = "voictest.csv";
		  	         //}
			         //else
		    		 //{
	          		 //	datafname = textField.getText().trim()+".csv";
		    		 //}

	
		  	         FileWriter datafile = new FileWriter(datafname);
	
				 //datafile.write(numZeroCrossings+","+freqGuess+"\r\n");
		                 for (int i=0 ; i < 44100 ; i++)
			         {
				     datafile.write(validSamples[i]+","+validSamples[i+44100]+"\r\n");
			         }
				 datafile.flush();
				 datafile.close();
	
			     } catch (Exception ex) { 
		              	reportStatus(ex.toString());
	            	     }
		     }


		     UdmtFFT.complexToComplex(1,65536,fftreal,fftimag);

		     double maxMagnitude = 0f;
		     double sumMagnitude = 0f;
		     int ixMaxMagnitude = 0;

		     int numMagnitudes = highFreqFilter - lowFreqFilter ;

		     //Only calculate magnitude for first 2048 points in order to avoid aliasing by high-frequency harmonics
                     for (int i=lowFreqFilter ; i < highFreqFilter ; i++)
		     {
			fftmagnitude[i]=Math.sqrt((fftreal[i]*fftreal[i])+(fftimag[i]*fftimag[i]));
			sumMagnitude += fftmagnitude[i];

    			if (fftmagnitude[i] > maxMagnitude)
			{
				maxMagnitude = fftmagnitude[i];
				ixMaxMagnitude = i;
			}
		     }

		     //To fix problems with strong overtones obscuring the fundamental,
		     //Look for the first local maximum whose magnitude is greater than
		     //one standard deviation from the mean of all the magnitudes.

		     double mean = sumMagnitude / numMagnitudes;

		     double variance = 0f;

		     //Calc std deviation
                     for (int i=lowFreqFilter ; i < highFreqFilter ; i++)
		     {
			 double deviation = fftmagnitude[i] - mean;
		         variance += deviation*deviation;
		     }
		     double stdev = Math.sqrt(variance/(numMagnitudes - 1));

		     ixFundamental = 0;

                     for (int i=lowFreqFilter ; i < highFreqFilter ; i++)
		     {
    			if (fftmagnitude[i] > (mean+stdev))
			{
				ixFundamental = i;
				break;
			}
		     }


		     //msg += "\r\n\r\nMax magnitude index="+ixMaxMagnitude+" value="+maxMagnitude;
		     //msg = "FFT Max magnitude index="+ixMaxMagnitude;
		     //msg += "\nMean = "+mean+"  Stdev="+stdev;
		     msg += "\nFFT Fundamental Freq Index="+ixFundamental;

  		     //JOptionPane.showMessageDialog(null, msg, "Pitch Detection Completed", JOptionPane.INFORMATION_MESSAGE);

		     //Choose either Max Magnitude or Fundamental to lookup here:
		     int ixLookup = ixFundamental;

		     //Lookup MIDI Note Number on calibrated magnitude table
		     int midinote = 0;
		     int midicolor = 0;
		     int midisharpflat = 0;
		     int calccents = 0;
		     for (int freqix = 1 ; freqix <= freqmagn.length ; freqix++)
		     {
			 int previx = freqix - 1;
		         if ((ixLookup >= (freqmagn[previx] + 1)) && (ixLookup <= freqmagn[freqix]))
			 {
				if (debugging)
				{
				  System.out.println ("ixlookup>=" + (freqmagn[previx] + 1) +" <= "+freqmagn[freqix]);
				  System.out.println ("FLAT RED: "+FlatRedLeft[freqix]+" thru "+FlatRedRight[freqix]);
				  System.out.println ("FLAT YELLOW: "+FlatYellowLeft[freqix]+" thru "+FlatYellowRight[freqix]);

				  System.out.println ("SHARP RED: "+SharpRedLeft[freqix]+" thru "+SharpRedRight[freqix]);
				  System.out.println ("SHARP YELLOW: "+SharpYellowLeft[freqix]+" thru "+SharpYellowRight[freqix]);
				}

				midinote = midibasenote+freqix;
				
				if ((ixLookup >= FlatRedLeft[freqix]) && (ixLookup <= FlatRedRight[freqix]))
				{
					midicolor = -1;
					midisharpflat = -1;		
				}
				else if ( (ixLookup >= FlatYellowLeft[freqix]) && (ixLookup <= FlatYellowRight[freqix]) )
				{
					midicolor = +1;
					midisharpflat = -1;	
				}
				else if ((ixLookup >= SharpRedLeft[freqix]) && (ixLookup <= SharpRedRight[freqix]))
				{
					midicolor = -1;
					midisharpflat = +1;		
				}
				else if ( (ixLookup >= SharpYellowLeft[freqix]) && (ixLookup <= SharpYellowRight[freqix]) )
				{
					midicolor = +1;
					midisharpflat = +1;	
				}

				int range = freqmagn[freqix] - freqmagn[previx];
				float midpt = ((freqmagn[freqix] + freqmagn[previx] + 1) / 2);
				float fcents = (((float)ixLookup - midpt) / ((float)range));
				calccents = (int)(fcents * 100);

				if (debugging)
				{
				  System.out.println ("ixLookup="+ixLookup);
				  System.out.println ("range="+range);
				  System.out.println ("midpt="+midpt);
				  System.out.println ("fcents="+fcents);
				  System.out.println ("calccents="+calccents);
				}

				break;
			 }
		     }

		     //msg += "\r\n\r\n(For the note name below, Middle C = Octave 4)";

		     msg += " MIDI="+midinote;

		     if (debugging)
		     {
		       System.out.println(msg);
		     }

		     msg = midinotename[midinote-midibasenote];


		     if (midicolor == -1)
		     {
			msg += " RED ";
		     }
		     else if (midicolor == +1)
		     {
			msg += " YELLOW ";
		     }
		     else
		     {
			msg += " GREEN ";
		     }

		     if (midisharpflat == -1)
		     {
			msg += " FLAT ";
		     }
		     else if (midisharpflat == +1)
		     {
			msg += " SHARP ";
		     }
		     else
		     {
			msg += " ";
		     }

		     msg += " APPROX CENTS OFF: "+calccents;

		     retMessage = msg;
		     retMidiNote = midinote;
    		     retSharpOrFlat = midisharpflat;
    		     retRedGreenYellow = midicolor;
		     retMidiNoteName = 	midinotename[midinote-midibasenote];
		     retPitchScore = calccents;

		     if (debugging)
		     {
		       System.out.println(msg);
		     }	
		     if (savecsv)	
		     {
			     System.out.println("SAVING CSV");

	         	     try {
				 String datafftname = "fft"+fileseq+".csv";
	
		  	         FileWriter fftfile = new FileWriter(datafftname);
	
		                 for (int i=0 ; i < 65536 ; i++)
			         {
				     if (i < numMagnitudes)
				     {
				         fftfile.write(fftreal[i]+","+fftimag[i]+","+fftmagnitude[i]+","+ixMaxMagnitude+","+maxMagnitude+"\r\n");
				     }
			             else
				     {
				         fftfile.write(fftreal[i]+","+fftimag[i]+"\r\n");
				     }
			         }
				 fftfile.flush();
				 fftfile.close();
	
			     } catch (Exception ex) { 
		              	reportStatus(ex.toString());
	            	     }


		     }


  		     //JOptionPane.showMessageDialog(null, msg, "Pitch Detection  Completed", JOptionPane.INFORMATION_MESSAGE);

		     //try {
		     //	logfile.write("\r\nPitch Detected: "+msg + "\r\n");
	   	     //	logfile.flush();
		     //} catch (Exception ex) { 
		     // 	reportStatus(ex.toString());
	             //}


                 } else {

		     //System.out.println("HERE");
                     for (int i = 0; i < nlengthInSamples; i++) {
                         /* First byte is LSB (low order) */
                         int LSB = (int) audioBytes[2*i];
                         /* Second byte is MSB (high order) */
                         int MSB = (int) audioBytes[2*i+1];

                         audioData[i] = MSB << 8 | (255 & LSB);
                     }
                 }
             } else if (format.getSampleSizeInBits() == 8) {
                 int nlengthInSamples = audioBytes.length;
                 audioData = new int[nlengthInSamples];
                 if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                     for (int i = 0; i < audioBytes.length; i++) {
                         audioData[i] = audioBytes[i];
                     }
                 } else {
                     for (int i = 0; i < audioBytes.length; i++) {
                         audioData[i] = audioBytes[i] - 128;
                     }
                 }
            }
               
//            int frames_per_pixel = audioBytes.length / format.getFrameSize()/w;
//            byte my_byte = 0;
//            double y_last = 0;
//            int numChannels = format.getChannels();
//            for (double x = 0; x < w && audioData != null; x++) {
//                int idx = (int) (frames_per_pixel * numChannels * x);
//                if (format.getSampleSizeInBits() == 8) {
//                     my_byte = (byte) audioData[idx];
//                } else {
//                     my_byte = (byte) (128 * audioData[idx] / 32768 );
//                }
//                double y_new = (double) (h * (128 - my_byte) / 256);
//                //lines.add(new Line2D.Double(x, y_last, x, y_new));
//                y_last = y_new;
//            }


	    if (debugging)
	    {
	      System.out.println("DONE RECORDING");
	    }
	    Recording = false;

            repaint();
        }


        public void paint(Graphics g) {

            Dimension d = getSize();
            int w = d.width;
            int h = d.height;
            int INFOPAD = 15;

            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
            g2.setColor(Color.white);
            g2.fillRect(0, h-INFOPAD, w, INFOPAD);

            if (errStr != null) {
                g2.setColor(jfcBlue);
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.drawString("ERROR", 5, 20);
                AttributedString as = new AttributedString(errStr);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                AttributedCharacterIterator aci = as.getIterator();
                FontRenderContext frc = g2.getFontRenderContext();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    TextLayout tl = lbm.nextLayout(w-x-5);
                    if (!tl.isLeftToRight()) {
                        x = w - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (capture.thread != null) {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Length: " + String.valueOf(seconds), 3, h-4);
            } else {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("File: " + fileName + "  Length: " + String.valueOf(duration) + "  Position: " + String.valueOf(seconds), 3, h-4);

                if (audioInputStream != null) {
                    // .. render sampling graph ..
                    g2.setColor(jfcBlue);
                    //for (int i = 1; i < lines.size(); i++) {
                    //    g2.draw((Line2D) lines.get(i));
                    //}

                    // .. draw current position ..
                    if (seconds != 0) {
                        double loc = seconds/duration*w;
                        g2.setColor(pink);
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(new Line2D.Double(loc, 0, loc, h-INFOPAD-2));
                    }
                }
            }
        }
    
        public void start() {
            thread = new Thread(this);
            thread.setName("SamplingGraph");
            thread.start();
            seconds = 0;
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
            seconds = 0;
            while (thread != null) {
                if ((playback.line != null) && (playback.line.isOpen()) ) {

                    long milliseconds = (long)(playback.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                } else if ( (capture.line != null) && (capture.line.isActive()) ) {

                    long milliseconds = (long)(capture.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;

		    // only record for 2 seconds - do not throw out any data
		    if (seconds > 1.75)
		    {
		    	StopRecording();
		    }
                }

                try { thread.sleep(100); } catch (Exception e) { break; }

                repaint();
                                
                while ((capture.line != null && !capture.line.isActive()) ||
                       (playback.line != null && !playback.line.isOpen())) 
                {
                    try { thread.sleep(10); } catch (Exception e) { break; }
                }
            }
            seconds = 0;
            repaint();
        }
    } // End class SamplingGraph


	public static void showInfoDialog() 
	{

		final String msg =
		  "When running the Java Sound demo as an applet these permissions\n" +

		  "are necessary in order to load/save files and record audio :  \n\n"+

		  "grant { \n" +

		  "  permission java.io.FilePermission \"<<ALL FILES>>\", \"read, write\";\n" +

		  "  permission javax.sound.sampled.AudioPermission \"record\"; \n" +

		  "  permission java.util.PropertyPermission \"user.dir\", \"read\";\n"+

		  "}; \n\n" +

		  "The permissions need to be added to the .java.policy file.";

		new Thread(new Runnable() 
		{

			public void run() 
			{

				JOptionPane.showMessageDialog(null, msg, "Applet Info", JOptionPane.INFORMATION_MESSAGE);

			}

		}
		).start();

	}



	public static void showSubmitDialog() 
	{

		final String msg =
		  "Your recording has been saved.\n";

		new Thread(new Runnable() 
		{

			public void run() 
			{

				JOptionPane.showMessageDialog(null, msg, "Recording Saved", JOptionPane.INFORMATION_MESSAGE);

			}

		}
		).start();

	}



    public static void main(String s[]) 
	{
		try { 
			logfile  = new FileWriter(logfilename);
			logfile.write("UdmtMicrophoneRecorder.java started.  Frequency Magnitudes:\r\n");

			for (int i=0 ; i <= freqmagn.length ; i++)
			{
				logfile.write(midinotename[i]+" : "+freqmagn[i]+"\r\n");
			}

		    	logfile.flush();

		} catch (Exception ex) { 
	      		System.out.println(ex.toString());
		}


        UdmtMicrophoneRecorder capturePlayback = new UdmtMicrophoneRecorder();
        capturePlayback.open();
        //JFrame f = new JFrame("MBO Sight Singing Voice Test ver:"+udmtVersion);
        //f.addWindowListener(new WindowAdapter() {
        //   public void windowClosing(WindowEvent e) {
	//	try { 
	//		logfile.close();
	//		System.exit(0); 
	//
	//	} catch (Exception ex) { 
	//      		System.out.println(ex.toString());
	//	}
	//    }
        //});
        //f.getContentPane().add("Center", capturePlayback);
        //f.pack();
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //int w = 400;
        //int h = 200;

	//If showing sampling graph:
        //int w = 1024;
        //int h = 768;

        //f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        //f.setSize(w, h);
        //f.show();
    }
}

// Simple Fast Fourier Transform.
class UdmtFFT {
    public static void complexToComplex(int sign, int n,
                                        float ar[], float ai[]) {
        float scale = (float)Math.sqrt(1.0f/n);

        int i,j;
        for (i=j=0; i<n; ++i) {
            if (j>=i) {
	            float tempr = ar[j]*scale;
	            float tempi = ai[j]*scale;
	            ar[j] = ar[i]*scale;
	            ai[j] = ai[i]*scale;
	            ar[i] = tempr;
	            ai[i] = tempi;
            }
            int m = n/2;
            while (m>=1 && j>=m) {
	            j -= m;
	            m /= 2;
            }
            j += m;
        }
    
        int mmax,istep;
        for (mmax=1,istep=2*mmax; mmax<n; mmax=istep,istep=2*mmax) {
            float delta = (float)sign*3.141592654f/(float)mmax;
            for (int m=0; m<mmax; ++m) {
	            float w = (float)m*delta;
	            float wr = (float)Math.cos(w);
	            float wi = (float)Math.sin(w);
	            for (i=m; i<n; i+=istep) {
	                j = i+mmax;
	                float tr = wr*ar[j]-wi*ai[j];
	                float ti = wr*ai[j]+wi*ar[j];
	                ar[j] = ar[i]-tr;
	                ai[j] = ai[i]-ti;
	                ar[i] += tr;
	                ai[i] += ti;
	            }
            }
            mmax = istep;
        }
    }
}
