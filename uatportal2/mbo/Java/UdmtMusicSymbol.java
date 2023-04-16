import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

//---------------------------------------------------------------------------------
public class UdmtMusicSymbol extends Component
//---------------------------------------------------------------------------------
{
	private Image imgSym;
	private int x1, y1, x2, y2;		

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
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtMusicSymbol
//---------------------------------------------------------------------------------
