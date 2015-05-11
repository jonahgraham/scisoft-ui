package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.dawnsci.analysis.api.EventTracker;

/**
 * This class is used to inject the analytics service using OSGI and retrieve in ConvertWizard
 * @author wqk87977
 *
 */
public class AnalyticsTrackerServiceLoader {

	private static EventTracker service;

	/**
	 * Used for OSGI injection
	 */
	public AnalyticsTrackerServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * @param at
	 */
	public static void setService(EventTracker at) {
		service = at;
	}

	public static EventTracker getService() {
		return service;
	}

}