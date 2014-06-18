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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {


	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.diamond.scisoft";

	// The shared instance
	private static Activator plugin;

	private static BundleContext bundleContext;

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
		plugin = this;
		bundleContext = context;

		// First thing to do here is to try to set up the logging properly.
		// during this, System.out will be used for logging
		try {			
			String logloc = System.getProperty("log.folder");
			if (logloc == null || "".equals(logloc)) {
				System.out.println("Log folder property not set, setting this manually to the temp directory");
				String tmpDir = System.getProperty("user.home")+"/.dawn/";
				File tmpDirFile = new File(tmpDir);
				if (tmpDirFile.exists() != true) {
					tmpDirFile.mkdirs();
				}
				
				System.setProperty("log.folder", tmpDir);
				
				// Redirect standard out away from console as javaw swallows it
				if (isWindowsOS()) {
					final File fout = new File(System.getProperty("user.home")+"/.dawn/dawn_std_out.txt");
					fout.mkdirs();
					if (fout.exists()) fout.delete();
					fout.createNewFile();
					
					final File ferr = new File(System.getProperty("user.home")+"/.dawn/dawn_std_err.txt");
					if (ferr.exists()) ferr.delete();
					ferr.createNewFile();
					
					MultiOutputStream out = new MultiOutputStream(System.out, new BufferedOutputStream(new FileOutputStream(fout)));
					MultiOutputStream err = new MultiOutputStream(System.err, new BufferedOutputStream(new FileOutputStream(ferr)));
					
					System.setOut(new PrintStream(out));
					System.setErr(new PrintStream(err));
				}
			}

			System.out.println("log.folder java property set to '"+System.getProperty("log.folder")+"'");

			System.out.println("Starting to Configure Logger");
			Object object = org.slf4j.LoggerFactory.getILoggerFactory();
			LoggerContext loggerContext = (LoggerContext) object;
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
			
		} catch (Throwable e) {
			System.out.println("Could not set up logging properly, loggin to stdout for now, error follows");
			e.printStackTrace();
			LoggerContext loggerContext = (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
			loggerContext.reset();
		} 
	}
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

	public static File getBundleLocation(final String bundle_id) throws IOException {
		
        // Just in case...
        final String eclipseDir = getEclipseHome();
  
        final File   plugins = new File(eclipseDir, "plugins");
        if (plugins.exists()) {
	        final File[] fa = plugins.listFiles();
	        for (int i = 0; i < fa.length; i++) {
				final File file = fa[i];
				if (file.getName().equals(bundle_id)) return file;
				if (file.getName().startsWith(bundle_id+"_")) return file;
			}
        }
		final Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if (bundle != null)
	        return FileLocator.getBundleFile(bundle);

		ProtectionDomain pd = Activator.class.getProtectionDomain();
		URL url = pd.getCodeSource().getLocation();
		return new File(url.getFile());
	}

	/**
	 * Gets eclipse home in debug and in deployed application mode.
	 * @return eclipseHome
	 */
	public static String getEclipseHome() {
		File hDirectory;
		try {
			URI u = new URI(System.getProperty("eclipse.home.location"));
			hDirectory = new File(u);
		} catch (URISyntaxException e) {
			return null;
		}

		String path = hDirectory.getName();
		if (path.equals("plugins") || path.equals("bundles")) {
			path = hDirectory.getParentFile().getParentFile().getAbsolutePath();
		} else{
			path = hDirectory.getAbsolutePath();
		}
        return path;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundleContext = context;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static Object getService(final Class<?> serviceClass) {
		if (bundleContext == null) return null;
		ServiceReference<?> ref = bundleContext.getServiceReference(serviceClass);
		if (ref==null) return null;
		return bundleContext.getService(ref);
	}
}
