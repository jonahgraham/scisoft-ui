/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.eclipse.dawnsci.analysis.api.ServerPortEvent;
import org.eclipse.dawnsci.analysis.api.ServerPortListener;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.rcp.preference.AnalysisRpcAndRmiPreferencePage;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;

import org.dawb.common.util.net.NetUtils;

public class InitPlotServer implements IStartup, ServerPortListener{
	
	private static final Logger logger = LoggerFactory.getLogger(InitPlotServer.class);


	@SuppressWarnings("rawtypes")
	private ServiceTracker plotServerTracker;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void earlyStartup() {

		if (AnalysisRCPActivator.isCorbaClient()) return;
		
		AnalysisRpcServerProvider.getInstance().addPortListener(this);

		plotServerTracker = new ServiceTracker(AnalysisRCPActivator.getDefault().getBundleContext(), PlotServer.class.getName(), null);
		plotServerTracker.open();
		PlotServer plotServer = (PlotServer)plotServerTracker.getService();
		if( plotServer != null) PlotServerProvider.setPlotServer(plotServer);			
		
		// if the rmi server has been vetoed, dont start it up, this also has issues
		if (Boolean.getBoolean("uk.ac.diamond.scisoft.analysis.analysisrpcserverprovider.disable") == false) {
			
			int analysisPort = AnalysisRpcAndRmiPreferencePage.getAnalysisRpcPort();
			if (analysisPort<1 || NetUtils.isPortFree(analysisPort)) AnalysisRpcServerProvider.getInstance().setPort(analysisPort);

			int rmiPort = AnalysisRpcAndRmiPreferencePage.getRmiPort();
			if (rmiPort<1 || NetUtils.isPortFree(rmiPort)) {
				int port = RMIServerProvider.getInstance().getPort();
				if (port<1) RMIServerProvider.getInstance().setPort(rmiPort);
			}
			
			FlatteningService.getFlattener().setTempLocation(AnalysisRpcAndRmiPreferencePage.getAnalysisRpcTempFileLocation());
		}
		
		// Close the plot server when we shut down.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					PlotServer plotServer = (PlotServer)plotServerTracker.getService();
					if( plotServer != null)
						PlotServerProvider.setPlotServer(null);
					plotServerTracker.close();		
				} catch (Throwable ne) {
					// Ignored we are shutting down.
				}
			}
			
		}));

		PlotServerProvider.getPlotServer(); // Creates plot server if required.


	}


	@Override
	public void portAssigned(ServerPortEvent evt) {
		logger.info("Setting "+PreferenceConstants.ANALYSIS_RPC_SERVER_PORT_AUTO+" to: " +  evt.getPort());
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode("uk.ac.diamond.scisoft.analysis.rpc");
		node.putInt(PreferenceConstants.ANALYSIS_RPC_SERVER_PORT_AUTO, evt.getPort());
		try {
			node.flush();
		} catch (BackingStoreException e) {
			logger.error("Error saving preference", e);
		}
	}

}
