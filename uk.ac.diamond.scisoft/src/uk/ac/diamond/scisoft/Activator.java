/*
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.plugin.PydevPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * Setup the logging facilities
	 */
	transient private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.diamond.scisoft.feedback";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		plugin = this;

		// First thing to do here is to try to set up the logging properly.
		// during this, System.out will be used for logging
		try {			
			String logloc = System.getProperty("log.folder");
			if (logloc == null || "".equals(logloc)) {
				System.out.println("Log folder property not set, setting this manually to the temp directory");
				String tmpDir = System.getProperty("user.home")+"/.dawn/";
				System.setProperty("log.folder", tmpDir);
			}

			System.out.println("log.folder java property set to '"+System.getProperty("log.folder")+"'");

			System.out.println("Starting to Configure Logger");
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.reset();
			
			System.out.println("Logger Context Reset");
			
			// now find the configuration file
			ProtectionDomain pd = Activator.class.getProtectionDomain();
			CodeSource cs = pd.getCodeSource();
			URL url = cs.getLocation();
			File file = new File(url.getFile(), "logging/log_configuration.xml");
			url = file.toURI().toURL();
			
			if (file.exists()) {
				System.out.println("Logging Configuration File found at '"+url+"'");
			} else {
				System.out.println("Logging Configuration File Not found at '"+url+"'");
			}

			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			String host = url.getHost(); // workaround Windows issue with local files
			if (host == null || host.length() == 0)
				configurator.doConfigure(file);
			else
				configurator.doConfigure(url);
			
			System.out.println("Logging Configuration complete");
			
		} catch (Exception e) {
			System.out.println("Could not set up logging properly, loggin to stdout for now, error follows");
			e.printStackTrace();
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.reset();
		} 
		
		
		// NOTE: Mark B advised that the python configuration should not be done here as there is now 
		// done in earlystartup extension point. Look at history if you want this code back.
	}


	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@SuppressWarnings("unused")
	private boolean isInterpreter(final IProgressMonitor monitor) {

		final InterpreterThread checkInterpreter = new InterpreterThread(monitor);
		checkInterpreter.start();

		int totalTimeWaited = 0;
		while (!checkInterpreter.isFinishedChecking()) {
			try {
				if (totalTimeWaited > 4000) {
					logger.error("Unable to call getInterpreterInfo() method on pydev, " +
							"assuming interpreter is already created.");
					return true;
				}
				Thread.sleep(100);
				totalTimeWaited += 100;
			} catch (InterruptedException ne) {
				break;
			}
		}

		if (checkInterpreter.isInterpreter())
			return true;
		return false;
	}

	private class InterpreterThread extends Thread {

		private IInterpreterInfo info = null;
		private IProgressMonitor monitor;
		private boolean finishedCheck = false;

		InterpreterThread(final IProgressMonitor monitor) {
			super("Interpreter Info");
			setDaemon(true);// This is not that important
			this.monitor = monitor;
		}

		@Override
		public void run() {
			// Might never return...
			try {
				info = PydevPlugin.getJythonInterpreterManager().getInterpreterInfo(JythonCreator.INTERPRETER_NAME, monitor);
			} catch (MisconfigurationException e) {
				logger.error("Jython is not configured properly", e);
			}
			finishedCheck = true;
		}

		public boolean isInterpreter() {
			return info != null;
		}

		public boolean isFinishedChecking() {
			return finishedCheck;
		}
	}
}