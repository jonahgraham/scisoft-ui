/*-
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
