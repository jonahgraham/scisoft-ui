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
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
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
	public static final String PLUGIN_ID = "uk.ac.diamond.scisoft";

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
			final File dir = getBundleLocation(PLUGIN_ID);
			File logDir = new File(dir, "logging");
			File file   = new File(logDir, "log_configuration.xml");
			
			if (file.exists()) {
				System.out.println("Logging Configuration File found at '"+file+"'");
			} else {
				System.out.println("Logging Configuration File Not found at '"+file+"'");
			}

			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(file);
			
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

	public static File getBundleLocation(final String bundle_id) throws IOException {
		

        // Just in case...
        final String eclipseDir = cleanPath(System.getProperty("eclipse.home.location"));
  
        final File   plugins = new File(eclipseDir+"/plugins/");
        if (plugins.exists()) {
	        final File[] fa = plugins.listFiles();
	        for (int i = 0; i < fa.length; i++) {
				final File file = fa[i];
				if (file.getName().equals(bundle_id)) return file;
				if (file.getName().startsWith(bundle_id+"_")) return file;
			}
        }
		final Bundle bundle = Platform.getBundle(PLUGIN_ID);
        return FileLocator.getBundleFile(bundle);
	}

	private static String cleanPath(String loc) {
		
		// Remove reference:file: from the start. TODO find a better way,
	    // and test that this works on windows (it might have ///)
        if (loc.startsWith("reference:file:")){
        	loc = loc.substring(15);
        } else if (loc.startsWith("file:")) {
        	loc = loc.substring(5);
        } else {
        	return loc;
        }
        
        loc = loc.replace("//", "/");
        loc = loc.replace("\\\\", "\\");

        return loc;
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