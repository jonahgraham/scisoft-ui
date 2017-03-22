/*
 * Copyright (c) 2012 Diamond Light Source Ltd. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.ui.pythonpathconf.InterpreterNewCustomEntriesAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyDevAdditionalInterpreterSettings extends InterpreterNewCustomEntriesAdapter {
	private static Logger logger = LoggerFactory.getLogger(PyDevAdditionalInterpreterSettings.class);

	@Override
	public Collection<String> getAdditionalEnvVariables() {
		if (!PlatformUI.isWorkbenchRunning())
			return null;
		List<String> entriesToAdd = new ArrayList<String>();
		entriesToAdd.add("SCISOFT_RPC_PORT=${scisoft_rpc_port}");
		entriesToAdd.add("SCISOFT_RMI_PORT=${scisoft_rmi_port}");
		entriesToAdd.add("SCISOFT_RPC_TEMP=${scisoft_rpc_temp}");
		return entriesToAdd;
	}

	public boolean isScisoftPyPath(String lib) {
		// Is this the scisoftpy plug-in ...
		if (lib.contains("uk.ac.diamond.scisoft.python")) {
			// ... and the scisoftpy files are in here
			if (Files.exists(Paths.get(lib, "scisoftpy", "__init__.py"))) {
				return true;
			}
			// ... or in here in dev mode
			if (Files.exists(Paths.get(lib, "src", "scisoftpy", "__init__.py"))) {
				return true;
			}
			// ... else this is not under consideration
		}
		
		return false;
	}
	
	public Optional<String> getScisoftPyPath() {
		// Try to add the scisoftpy location when in dev
		URL scisoftpyInitURL = null;
		try {
			scisoftpyInitURL = FileLocator
					.find(new URL("platform:/plugin/uk.ac.diamond.scisoft.python/src/scisoftpy/__init__.py"));
		} catch (MalformedURLException e) {
			// unreachable as it is a constant string
		}

		// Try to add the scisoftpy location when deployed
		if (scisoftpyInitURL == null) {
			try {
				scisoftpyInitURL = FileLocator
						.find(new URL("platform:/plugin/uk.ac.diamond.scisoft.python/scisoftpy/__init__.py"));
			} catch (MalformedURLException e) {
				// unreachable as it is a constant string
			}
		}
		if (scisoftpyInitURL != null) {
			try {
				scisoftpyInitURL = FileLocator.toFileURL(scisoftpyInitURL);
				IPath scisoftpyInitPath = new Path(scisoftpyInitURL.getPath());
				IPath rootPath = scisoftpyInitPath.removeLastSegments(2); // remove scisoftpy and __init__.py
				IPath path = rootPath.removeTrailingSeparator();
				return Optional.of(path.toOSString());
			} catch (IOException e) {
				logger.debug("Failed to convert scisoft URL into a file URL", e);
			}
		} else {
			logger.debug("Failed to find location of scisfotpy to add the python path");
		}

		return Optional.empty();
	}
	
	public boolean isFabioPath(String lib) {
		// Best guess that this is Dawn root install for Fabio if dawn.ini is in it...
		if (Files.exists(Paths.get(lib, "dawn.ini"))) {
			return true;
		}
		// ...or it is the target platform directory when running in dev mode
		if (Files.exists(Paths.get(lib, "dynamic.target"))) {
			return true;
		}

		return false;
	}
	
	public Optional<String> getFabioPath() {
		// Add Fabio to the path
		try {
			IPath path = new Path(System.getProperty("eclipse.home.location").replace("file:", ""));
			// path = path.append("fabio");
			logger.debug("Fabio python path is : " + path.toOSString());
			if (path.toFile().exists()) {
				logger.debug("Fabio python path added");
				return Optional.of(path.toOSString());
			}
		} catch (Exception e) {
			logger.warn("Failed to add Fabio to add the python path");
		}
		
		return Optional.empty();
	}

	@Override
	public Collection<String> getAdditionalLibraries() {

		if (!PlatformUI.isWorkbenchRunning())
			return null; // Headless mode, for instance workflows!

		List<String> libraries = new ArrayList<String>();
		getScisoftPyPath().ifPresent(libraries::add);
		getFabioPath().ifPresent(libraries::add);
		return libraries;
	}

}
