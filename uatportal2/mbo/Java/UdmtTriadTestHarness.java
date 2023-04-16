import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import java.net.*;

//------------------------------------------------------------------------------

public class UdmtTriadTestHarness extends Applet
{
	private String udmtAppletName = "UdmtTriadDrill";
	private String udmtAppletVersion = "0.01";
	private Rectangle	rectScreen;

	public void init()
	{
		this.showStatus("Initializing "+udmtAppletName+" ver: "+udmtAppletVersion);
		System.out.println("init method called for Applet: "+udmtAppletName+" ver: "+udmtAppletVersion);

		rectScreen = getBounds();
		
		UdmtChord c = new UdmtChord();
		c.ConstructTriadFromRootMidi(60,'M');
		System.out.println ("C Major: 3rd="+c.getThirdMidi()+" 5th= "+c.getFifthMidi());
		c.ConstructTriadFromRootMidi(60,'m');
		System.out.println ("C Minor: 3rd="+c.getThirdMidi()+" 5th= "+c.getFifthMidi());
		c.ConstructTriadFromRootMidi(60,'+');
		System.out.println ("C Aug: 3rd="+c.getThirdMidi()+" 5th= "+c.getFifthMidi());
		c.ConstructTriadFromRootMidi(60,'-');
		System.out.println ("C Dim: 3rd="+c.getThirdMidi()+" 5th= "+c.getFifthMidi());
	}
	public void paint(Graphics g)
	{
		g.setColor(Color.red);
		g.fillRect(0, 0, rectScreen.width, rectScreen.height);
 	}
}
