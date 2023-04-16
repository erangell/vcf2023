import java.util.*;
//----------------------------------------------------------------------------------
// UdmtExamLog: This is where code to log student attempts to take the exam will go
// TODO: Figure out how to get Student ID from Cookie and (use JDBC?) to store log file.
// 03/20/2005 - THIS CODE IS OBSOLETE - CAN EVENTUALLY BE REMOVED FROM EACH PROGRAM
// (Logging will be done using PHP scripts)
//----------------------------------------------------------------------------------

public class UdmtExamLog extends Object
{	
	private String mStudentId;
	private String mExamId;
	
	public void UdmtExamLog()
	{
		mStudentId = "INVALID";
		mExamId = "INVALID";
	}

	public void setStudentId ( String studentId )
	{
		mStudentId = studentId;
	}
	public void setExamId ( String examId )
	{
		mExamId = examId;
	}

	public boolean getStudentIdFromCookie ()
	{
		// This code will eventually look for a cookie created from a Udmt login screen
		// It will return false if the cookie cannot be found.

		setStudentId ("TestStudentID");
		return true;
	}

	public void logExamEntry ()
	{
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		String logDateTime = currDate.toString();
		//System.out.println (logDateTime+": Student: "+mStudentId+" began exam: "+mExamId);
	}

	public void logExamPass ()
	{
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		String logDateTime = currDate.toString();

		//System.out.println (logDateTime+": Student: "+mStudentId+" PASSED exam: "+mExamId);
	}

	public void logExamFail ()
	{
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		String logDateTime = currDate.toString();

		//System.out.println (logDateTime+": Student: "+mStudentId+" FAILED exam: "+mExamId);
	}

//------------------------------------------------------------------------------
} // class
//------------------------------------------------------------------------------
