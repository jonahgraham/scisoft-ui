/*-
 * Copyright 2015 Diamond Light Source Ltd.
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

import static org.eclipse.dawnsci.analysis.api.RMIServerProvider.UNSET_PORT;

import java.io.IOException;

import org.dawb.common.util.net.NetUtils;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.eclipse.ui.IStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.preference.AnalysisRpcAndRmiPreferencePage;

/**
 * Initialiser to instantiate the RMI server from the UI preferences and/or system properties
 *
 * @author Simon Berriman
 */
public class InitRMIServer implements IStartup {
	private static final Logger LOGGER = LoggerFactory.getLogger(InitRMIServer.class);

	@Override
	public void earlyStartup() {
		final RMIServerProvider rmiServerProvider = RMIServerProvider.getInstance();

		int rmiPort = AnalysisRpcAndRmiPreferencePage.getRmiPort();
		if (rmiPort == UNSET_PORT) {
			try {
				LOGGER.info("Local RMI server port has not been specified - looking for a free port...");
				rmiPort = NetUtils.getFreePort();
				LOGGER.debug("Local RMI server port is now " + rmiPort);

			} catch (IOException e) {
				LOGGER.error("Unable to find a free local port automatically - RMI server cannot be initialised", e);
			}
		}

		if (rmiPort > UNSET_PORT && !NetUtils.isPortFree(rmiPort)) {
			LOGGER.error("Local server port " + rmiPort + " is not available - RMI server cannot be initialised. Please reconfigure.");

		} else {
			rmiServerProvider.setPort(rmiPort);

			try {
				rmiServerProvider.initServer();

			} catch (Exception e) {
				LOGGER.error("RMI Server failed to initialise. See nested exception", e);
			}
		}
	}
}
