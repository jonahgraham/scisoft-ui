/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.RMIServerProvider;
import uk.ac.diamond.scisoft.analysis.ServerPortEvent;
import uk.ac.diamond.scisoft.analysis.ServerPortListener;
import uk.ac.diamond.scisoft.analysis.rcp.preference.AnalysisRpcAndRmiPreferencePage;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;

public class InitPlotServer implements IStartup, ServerPortListener{
	
	private static final Logger logger = LoggerFactory.getLogger(InitPlotServer.class);


	@SuppressWarnings("rawtypes")
	private ServiceTracker plotServerTracker;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void earlyStartup() {

		AnalysisRpcServerProvider.getInstance().addPortListener(this);

		plotServerTracker = new ServiceTracker(AnalysisRCPActivator.getDefault().getBundleContext(), PlotServer.class.getName(), null);
		plotServerTracker.open();
		PlotServer plotServer = (PlotServer)plotServerTracker.getService();
		if( plotServer != null) PlotServerProvider.setPlotServer(plotServer);			
		
		// if the rmi server has been vetoed, dont start it up, this also has issues
		if (Boolean.getBoolean("uk.ac.diamond.scisoft.analysis.analysisrpcserverprovider.disable") == false) {
			AnalysisRpcServerProvider.getInstance().setPort(AnalysisRpcAndRmiPreferencePage.getAnalysisRpcPort());
			RMIServerProvider.getInstance().setPort(AnalysisRpcAndRmiPreferencePage.getRmiPort());
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
