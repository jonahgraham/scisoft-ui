package uk.ac.diamond.scisoft.qstatmonitor.preferences;

public class QStatMonitorPreferenceConstants {

	public static final String P_SLEEP = "uk.ac.diamond.scisoft.qstatmonitor.preferences.sleep";
	public static final float DEF_SLEEP = 4.5f;

	public static final String P_REFRESH = "uk.ac.diamond.scisoft.qstatmonitor.preferences.refresh";
	public static final boolean DEF_REFRESH = false;
	
	//TODO: Do still need 'preference' constant for plot option
	/*
	public static final String P_PLOT = "uk.ac.diamond.scisoft.qstatmonitor.preferences.plot";
	public static final boolean DEF_PLOT = false;
	*/

	public static final String P_QUERY = "uk.ac.diamond.scisoft.qstatmonitor.preferences.query";
	public static final String DEF_QUERY = "qstat";

	public static final String P_USER = "uk.ac.diamond.scisoft.qstatmonitor.preferences.user";
	public static final String DEF_USER = "*";

	public static final String[] LIST_OF_QUERIES = {"qstat", "qstat -l tesla",
			"qstat -l tesla64", "qstat", "qstat -l tesla", "qstat -l tesla64"};

}
