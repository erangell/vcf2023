import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

import javax.sound.midi.*;

//---------------------------------------------------------------------------------
public class UdmtPianoKeyboard extends Component
//---------------------------------------------------------------------------------
{
	private Image imgSym;
	private int x1, y1, x2, y2;		

	public int x1left, y1left, x2right, y2right;		

	public int[] xTop = {38,48,62,71,84,97,107,118,129,139
				,151,163,175,185,199,208,221,234,244,256
				,265,277,287,300,312,322,335,345,358,371
				,381,392,402,414,424
				,437,448,460,471,483,496,506,517,528,538,550,562,574};

	public int[] xBot = {45,65,84,103,124,144,163,182,202,221
				,240,261,280,299,319,338,357,377,397,417,437
				,457,474,496,515,535,553,574};

	private int[] midiTopOrBottom = {1,0,1,0,1,1,0,1,0,1,0,1
				     ,1,0,1,0,1,1,0,1,0,1,0,1
				     ,1,0,1,0,1,1,0,1,0,1,0,1
				     ,1,0,1,0,1,1,0,1,0,1,0,1,1};	//1=bottom 0=top


	private int midiDotRadius = 4;

	private int[] xMidiDot = {42, 51, 62, 74, 82,102,110,122,132,141,154,161
				,180,188,200,211,219,239,247,259,269,279,291,298
				,317,325,337,348,356,376,384,396,406,415,428,435
				,454,462,474,485,493,513,521,533,543,552,565,572
				,592};

	private int[] yMidiDot = {83, 50, 83, 50, 83, 83, 50, 83, 50, 83, 50, 83
				 ,83, 50, 83, 50, 83, 83, 50, 83, 50, 83, 50, 83
				 ,83, 50, 83, 50, 83, 83, 50, 83, 50, 83, 50, 83
				 ,83, 50, 83, 50, 83, 83, 50, 83, 50, 83, 50, 83
				 ,83};

	private int[] pianoDotOnOrOff = {0,0,0,0,0,0,0,0,0,0,0,0
					,0,0,0,0,0,0,0,0,0,0,0,0
					,0,0,0,0,0,0,0,0,0,0,0,0
					,0,0,0,0,0,0,0,0,0,0,0,0,0};

	public int xMin=3;
	public int xMax=570;
	public int yMin=3;
	public int yMax=95;
	public int yMid=58;
	public int midiTop=36;
	public int midiBot=36;

	public int lastcsrx = 0;
	public int lastcsry = 0;

	public int xcalib = 26;
	public int ycalib = 0;

	//-----------------------------------------
	public void CropImage ( Image origImg, int newX1, int newY1, int newX2, int newY2 )
	{	
		x1 = newX1; y1 = newY1; x2 = newX2; y2 = newY2;

		// Image Cropping logic from p.461 of Java AWT Reference:
		// (PDF available at: http://www.oreilly.com/catalog/javaawt/book)

		imgSym = createImage
			( new FilteredImageSource 
				( origImg.getSource()
				, new CropImageFilter 
					( x1
					, y1
					, this.getWidth()
					, this.getHeight() 
					)
				)
			);
	}
	//-----------------------------------------
	public int getX1 ()
	{
		return x1;
	}
	//-----------------------------------------
	public int getY1 ()
	{
		return y1;
	}
	//-----------------------------------------
	public int getWidth()
	{
		return x2 - x1 + 1;
	}
	//-----------------------------------------
	public int getHeight()
	{
		return y2 - y1 + 1;
	}
	//-----------------------------------------
	public Image getImage()
	{
		return imgSym;
	}

	//-----------------------------------------
	public void setScreenCoords(int xl, int yl, int xr, int yr)
	{
		x1left = xl;
		y1left = yl;
		x2right = xr;
		y2right = yr;		
	}

	//-----------------------------------------
	public boolean pointIsInsidePiano(int x, int y)
	{
		boolean inside = false;
		if ((x >= x1left) && (x <= x2right) && (y >= y1left) && (y <= y2right))
		{
			inside = true;
		}
		return inside;
	}

	//-----------------------------------------
	public int mousePressedInsidePiano(int scrx, int scry)
	{
		int retmidinote = -1;
		lastcsrx = scrx;
		lastcsry = scry;
		int adjx = scrx - x1left;
		int adjy = scry - y1left;
		//System.out.println("MousePressedInsidePiano: "+scrx+","+scry+" adjusted:"+adjx+","+adjy);

		int cursorX = adjx;
		int cursorY = adjy;


		if (    (cursorX >= xMin) && (cursorX <= (xMax + xcalib))
		     && (cursorY >= yMin) && (cursorY <= (yMax + ycalib))
		   )
		{		
			retmidinote = calcMidiNote (cursorX - xMin + xcalib ,cursorY - yMin + ycalib);
		}
 

		return retmidinote;
	}

	//-----------------------------------------

	private int calcMidiNote (int cx, int cy)
	{
		int ix;
		int startingOctave, startingOffset, ixOctave, ixOffset;
		int[] whiteKeyOffset = {0,2,4,5,7,9,11};
		int whiteIndex;
		int midi;

		int xTopCount = xTop.length;
		int xBotCount = xBot.length;

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



	//-----------------------------------------

	public int getPianoDotRadius ()
	{
		return midiDotRadius;
	}

	//-----------------------------------------

	public int getPianoDotX (int midinote)
	{
		int midiIndex = midinote - midiBot;

		int xadj = x1left - xMin - xcalib;		

		return xMidiDot[midiIndex] + xadj;
	}

	//-----------------------------------------

	public int getPianoDotY (int midinote)
	{
		int midiIndex = midinote - midiBot;

		int yadj = y1left - yMin ;		

		return yMidiDot[midiIndex] + yadj;		
	}

	//-----------------------------------------
	public int getDefaultPianoKeyColor (int midinote)	//return 0 if key is normally black, 1 if white
	{
		int midiIndex = midinote - midiBot;
		return midiTopOrBottom[midiIndex];
	}

	//-----------------------------------------
	public int togglePianoDot (int midinote)	//return 0 if key is now unpressed, 1 if pressed
	{
		int midiIndex = midinote - midiBot;
		if (pianoDotOnOrOff[midiIndex] == 0)
		{

			pianoDotOnOrOff[midiIndex] = 1;
		}
		else
		{
			pianoDotOnOrOff[midiIndex] = 0;
		}
		return pianoDotOnOrOff[midiIndex];
	}
	//-----------------------------------------
	public void setPianoDot (int midinote, int status)	//pass 0 for note off, 1 for note on
	{
		int midiIndex = midinote - midiBot;
		pianoDotOnOrOff[midiIndex] = status;
	}
	//-----------------------------------------
	public int getPianoDot (int midinote)		//return 0 if key is now unpressed, 1 if pressed
	{
		int midiIndex = midinote - midiBot;
		return pianoDotOnOrOff[midiIndex];
	}
	//-----------------------------------------
	public void ProcessMetaEvent( MetaMessage msg )
	{
		byte[] theMessage = msg.getData();
		if (theMessage[1] == 35)
		{
			//System.out.println("UdmtPianoKeyboard.ProcessMetaEvent NOTE ON :"+theMessage[2]);
			int midinote = (int)theMessage[2];
			setPianoDot(midinote,1);
		}
		else if (theMessage[1] == 33)
		{
			//System.out.println("UdmtPianoKeyboard.ProcessMetaEvent NOTE OFF:"+theMessage[2]);
			int midinote = (int)theMessage[2];
			setPianoDot(midinote,0);
		}	
	}
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtPianoKeyboard
//---------------------------------------------------------------------------------
