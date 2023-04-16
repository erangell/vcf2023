//------------------------------------------------------------------------------

import java.util.*;
import java.lang.System;

//------------------------------------------------------------------------------
// UdmtStopWatch Class
//------------------------------------------------------------------------------

public class UdmtStopWatch extends Object
{
	private	long startTime;			// time when stopwatch was started
	private long stopTime;			// time when stopwatch was halted
	private long elapsedTime;		// last differential between start and stop times	

//------------------------------------------------------------------------------

	public void UdmtStopWatch()
	{
		startTime		=	0L;
		stopTime		=	0L;
		elapsedTime	=	0L;	
	}
	

//------------------------------------------------------------------------------

	public void	StartWatch()
	{
		stopTime		=	0L;
		elapsedTime	=	0L;
		startTime		=	System.currentTimeMillis();
	}

//------------------------------------------------------------------------------

	public	void HaltWatch()
	{
		stopTime		=	System.currentTimeMillis();
		elapsedTime	=	stopTime - startTime;

	}

//------------------------------------------------------------------------------

	public long	GetElapsedTime()
	{
		return( elapsedTime );
	}
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
