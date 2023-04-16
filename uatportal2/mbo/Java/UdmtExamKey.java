//---------------------------------------------------------------------------------
public class UdmtExamKey extends Object
//---------------------------------------------------------------------------------
{
	private String[] ExamID = {
	 "NoteIDTREBLESTAFF_ONLYPass"
	,"NoteIDBASSSTAFF_ONLYPass"
	,"NoteIDTREBLELEDGERS_ONLYPass"
	,"NoteIDBASSLEDGERS_ONLYPass"
	,"ScalesMajorAscPass"
	,"ScaleDegMAJORPass"
	,"SolfegeMajorScalarPass"
	,"KeysigMajorPass"
	,"IntervalsIDENTIFYPerfMaj1OctAscPass"
	,"IntervalsCONSTRUCTPerfMaj1OctAscPass"
	,"IntervalsIDENTIFYAll1OctAscPass"
	,"IntervalsCONSTRUCTAll1OctAscPass"
	,"IntervalsIDENTIFYAll2OctOnlyAscPass"
	,"IntervalsCONSTRUCTAll2OctOnlyAscPass"
	,"IntervalsCONSTRUCTAll1OctDescPass"
	,"IntervalsCONSTRUCTAll2OctOnlyDescPass"
	,"IntervalsINVERSIONSAll1OctAscPass"
	,"ScalesAllMinorAscMixedPass"
	,"KeysigMinorPass"
	,"SolfegeNaturalMinorScalarPass"
	,"SolfegeHarmonicMinorScalarPass"
	,"SolfegeMelodicMinorScalarPass"
	,"RhydictLevel1Pass"
	,"RhydictLevel2Pass"
	,"RhydictLevel3Pass"
        ,"TSIGPASS" 
	,"PianoKbdTREBLEALLPass"
	,"PianoKbdBASSALLPass"
	,"ScaleDegMINORPass"
	,"TriadIDMelodic MinorPass"
	,"TriadConstrMelodic MinorPass"
	,"TriadIDHarmonic MinorPass"
	,"TriadConstrHarmonic MinorPass"
	,"TriadIDNatural MinorPass"
	,"TriadConstrNatural MinorPass"
	,"TriadIDMajorPass"
	,"TriadConstrMajorPass"
	,"TriadChordIDPass"
	,"TriadChordConstrPass"
	,"IntvEarTrainMAJORPass"
	,"IntvEarTrainALLPass"
	,"TriadEarTrainPass"

	,"NoteIDTREBLESTAFF_ONLYFail"
	,"NoteIDBASSSTAFF_ONLYFail"
	,"NoteIDTREBLELEDGERS_ONLYFail"
	,"NoteIDBASSLEDGERS_ONLYFail"
	,"ScalesMajorAscFail"
	,"ScaleDegMAJORFail"
	,"SolfegeMajorScalarFail"
	,"KeysigMajorFail"
	,"IntervalsIDENTIFYPerfMaj1OctAscFail"
	,"IntervalsCONSTRUCTPerfMaj1OctAscFail"
	,"IntervalsIDENTIFYAll1OctAscFail"
	,"IntervalsCONSTRUCTAll1OctAscFail"
	,"IntervalsIDENTIFYAll2OctOnlyAscFail"
	,"IntervalsCONSTRUCTAll2OctOnlyAscFail"
	,"IntervalsCONSTRUCTAll1OctDescFail"
	,"IntervalsCONSTRUCTAll2OctOnlyDescFail"
	,"IntervalsINVERSIONSAll1OctAscFail"
	,"ScalesAllMinorAscMixedFail"
	,"KeysigMinorFail"
	,"SolfegeNaturalMinorScalarFail"
	,"SolfegeHarmonicMinorScalarFail"
	,"SolfegeMelodicMinorScalarFail"
	,"RhydictLevel1Fail"
	,"RhydictLevel2Fail"
	,"RhydictLevel3Fail"
	,"TSIGFAIL"	
	,"PianoKbdTREBLEALLFail"
	,"PianoKbdBASSALLFail"
	,"ScaleDegMINORFail"
	,"TriadIDMelodic MinorFail"
	,"TriadConstrMelodic MinorFail"
	,"TriadIDHarmonic MinorFail"
	,"TriadConstrHarmonic MinorFail"
	,"TriadIDNatural MinorFail"
	,"TriadConstrNatural MinorFail"
	,"TriadIDMajorFail"
	,"TriadConstrMajorFail"
	,"TriadChordIDFail"
	,"TriadChordConstrFail"
	,"IntvEarTrainMAJORFail"
	,"IntvEarTrainALLFail"
	,"TriadEarTrainFail"

	};	// 26 exams, 52 keys as of 1/23/05
	// 28 exams, 56 keys as of 6/22/06
	// 29 exams, 58 keys as of 6/22/06
	// 33 exams, 66 keys as of 6/22/06
	// 44 exams, 88 keys as of 10/19/08
	// 39 exams, 78 keys as of 10/28/08
	// 41 exams, 82 keys as of 10/28/08
	// 42 exams, 84 keys as of 11/23/08

//NOTE: EXAM KEYS NEED TO BE SYNCHRONIZED WITH mboscore.php

	private long[] lExamKey = {
	 1209991	//1097 x 1103
	,1238753	//1109 x 1117
	,1267867	//1123 x 1129
	,1327103	//1151 x 1153
	,1361873	//1163 x 1171
	,1401847	//1181 x 1187
	,1432793	//1193 x 1201
	,1476221	//1213 x 1217
	,1503067	//1223 x 1229
	,1522747	//1231 x 1237

	,1572491	//1249 x 1259
	,1633283	//1277 x 1279
	,1653787	//1283 x 1289
	,1674427	//1291 x 1297
	,1695203	//1301 x 1303
	,1723933	//1307 x 1319
	,1752967	//1321 x 1327
	,1860487	//1361 x 1367
	,1896113	//1373 x 1381
	,1971191	//1399 x 1409

	,2030621	//1423 x 1427
	,2047757	//1429 x 1433
	,2082233	//1439 x 1447
	,2108303	//1451 x 1453
	,2146189	//1459 x 1471
	,2196323	//1481 x 1483
	,2214143	//1487 X 1489
	,2238007	//1493 X 1499
	,2301253	//1511 x 1523
	,2362333	//1531 x 1543

	,2405597	//1549 x 1553
	,2442953	//1559 x 1567
	,2480609	//1571 x 1579
	,2528051	//1583 x 1597
	,2572807	//1601 x 1607
	,2595317	//1609 x 1613
	,2624399	//1619 x 1621
	,2663399	//1627 x 1637
	,2755591	//1657 x 1663
	,2782223	//1667 x 1669

	,2873021	//1693 x 1697
	,2903591	//1699 x 1709


	,27548069
	,29484487
	,27548069
	,29484487
	,32392103
	,33386387
	,35441557
	,37179521
	,38289659
	,39958447

	,41300411
	,43366789
	,44290271
	,45549703
	,47823679
	,48217283
	,49220603
	,48756791
	,50437319
	,52589219

	,56522057
	,56759971
	,58156921
	,58594927
	,59319569
	,59463541
	,59380697
	,58978939
	,61412287
	,58726249

	,59677063
	,59686351
	,59938031
	,56162017
	,56975717
	,57960509
	,57473279
	,58183777
	,57660389
	,55824271

	,54456217
	,53378957

	};


/* 60 PASS KEYS
1209991
1238753
1267867
1327103
1361873
1401847
1432793
1476221
1503067
1522747
1572491
1633283
1653787
1674427
1695203
1723933
1752967
1860487
1896113
1971191
2030621
2047757
2082233
2108303
2146189
2196323
2214143
2238007
2301253
2362333
2405597
2442953
2480609
2528051
2572807
2595317
2624399
2663399
2755591
2782223	//1667 x 1669
2873021	//1693 x 1697
2903591	//1699 x 1709

2965283	//1721 x 1723
3017153	//1733 x 1741
3062491	//1747 x 1753
3125743	//1759 x 1777
3186221	//1783 x 1787
3221989	//1789 x 1801
3301453	//1811 x 1823
3381857	//1831 x 1847
3474487	//1861 x 1867
3504383	//1871 x 1873
3526883	//1877 x 1879
3590989	//1889 x 1901
3648091	//1907 x 1913
3732623	//1931 x 1933
3802499	//1949 x 1951
3904567	//1973 x 1979
3960091	//1987 x 1993
3992003	//1997 x 1999
*/

/* 60 FAIL KEYS 
27548069
29484487
27548069
29484487
32392103
33386387
35441557
37179521
38289659
39958447
41300411
43366789
44290271
45549703
47823679
48217283
49220603
48756791
50437319
52589219
56522057
56759971
58156921
58594927
59319569
59463541
59380697
58978939
61412287
58726249
59677063
59686351
59938031
56162017
56975717
57960509
57473279
58183777
57660389
55824271
54456217
53378957

50047559
50812357
49322921
46167911
45897457
42638977
39348823
37430339
36536207
34097387
29408677
28250437
23137379
19612693
17895067
13952731
12127489
7894901
5810015
3577389
*/



	// Constructor:
	public void UdmtExamKey()
	{
	}

	//-----------------------------------------
	public String getExamKey( String s)
	{
		int[] rpm = {2,3,5,7,11,13,17,19,23,29
			,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};

		int rpmlen = rpm.length;
		//System.out.println ("rpmlen="+rpmlen);

		int rpix = (int)((Math.random() * rpmlen));
		//System.out.println ("rpix="+rpix);

		int rp = rpm[rpix];
		//System.out.println ("rp="+rp);

		long lRet = 0;
		String sRet = null;
		for (int x=0 ; x < ExamID.length ; x++)
		{
			if (ExamID[x].equals(s))
			{
				lRet = lExamKey[x];
			}
		}		
		//System.out.println ("lRet before randomizing="+lRet);

		lRet *= rp;
		//System.out.println ("lRet after randomizing="+lRet);

		sRet = ""+lRet;
		//System.out.println ("sRet="+sRet);
		
		return sRet;

	}
}
//---------------------------------------------------------------------------------
// END OF CLASS UdmtExamKey
//---------------------------------------------------------------------------------
