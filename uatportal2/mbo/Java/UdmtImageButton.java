import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.image.*;

public class UdmtImageButton extends Canvas {

// Code from "A Java Gui Programmer's Primer" by Fintan Culwin, page 144-150.
// QA76.73.J38.C87 1998

private static final int BORDER_WIDTH = 2;
private static final Color DEFAULT_BORDER_COLOR
	= new Color (0x80, 0x80, 0x80);

private Image buttonImage = null;
private String imageSource = null;
private int buttonWidth = -1;
private int buttonHeight = -1;
private boolean pressed = false;
private Color borderColour;
private String actionCommand = null;
private ActionListener itsListener = null;
private Applet itsApplet;

public UdmtImageButton (String theSource, Applet applet)
{	this (theSource, applet, DEFAULT_BORDER_COLOR);
}

public UdmtImageButton (String theSource, Applet applet, Color colorForBorder)
{	super();
	imageSource = new String ( theSource);
	itsApplet = applet;
	this.setForeground (colorForBorder);
	this.enableEvents (AWTEvent.MOUSE_EVENT_MASK);
}

public void addNotify()
{	MediaTracker aTracker;
	super.addNotify();
	buttonImage = (itsApplet.getImage ( itsApplet.getCodeBase(), imageSource));
	aTracker = new MediaTracker (this);
	aTracker.addImage (buttonImage, 0);
	try	{
		aTracker.waitForID(0);
	} catch (InterruptedException exception)	{
	}
	if (buttonImage == null 
	||  buttonImage.getWidth(this) < 1
	||  buttonImage.getHeight(this) < 1 )
	{	System.err.println ("The image "+imageSource+" \n Could not be loaded.");
		System.exit(-1);
	}
	buttonWidth = buttonImage.getWidth(this) + BORDER_WIDTH * 2;
	buttonHeight = buttonImage.getHeight(this) + BORDER_WIDTH * 2;
	this.setSize (buttonWidth, buttonHeight);
}

public Dimension getMinimumSize() 
{	return ( new Dimension (buttonWidth, buttonHeight));
}

public Dimension getPreferredSize()
{	return this.getMinimumSize();
}

public void update (Graphics systemContext)
{	this.paint (systemContext);
}

public void paint ( Graphics systemContext)
{	int index;
	systemContext.drawImage (buttonImage, BORDER_WIDTH, BORDER_WIDTH, this);
	for (index=0; index < BORDER_WIDTH ; index++)
	{	systemContext.draw3DRect ( index, index, buttonWidth - index - 1, buttonHeight - index - 1, !pressed );
	}
}

protected void processMouseEvent (MouseEvent event)
{	switch (event.getID())
	{	case MouseEvent.MOUSE_EXITED:
			pressed = false;
			repaint();
			break;

		case MouseEvent.MOUSE_PRESSED:
			pressed = true;
			repaint();
			break;

		case MouseEvent.MOUSE_RELEASED:
			if ( ( pressed ) && ( itsListener != null ) )
			{	itsListener.actionPerformed ( new ActionEvent 
					( this, ActionEvent.ACTION_PERFORMED, this.getActionCommand() ) );
			}
			pressed = false;
			repaint();
			break;
	}
}

public void setActionCommand ( String command )
{	actionCommand = command;
}

public String getActionCommand () 
{	if (actionCommand == null)
	{	return "ImageButton";
	}
	else
	{	return actionCommand;
	}
}

public void addActionListener (ActionListener listener)
{	itsListener = AWTEventMulticaster.add ( itsListener, listener);
}

public void removeActionListener (ActionListener listener)
{	itsListener = AWTEventMulticaster.remove ( itsListener, listener);
}


} // class