/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.interpreters.PythonInterpreterManager;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAWNSCI-6068: Update the PYTHONPATH for changing install locations.
 * <p>
 * The {@link JythonCreator} behaves much harsher and simply creates new configuration with the correct path. The Python
 * version tries to play nicer with the user configuring Python themselves.
 */
public class PythonUpdater {
	private static Logger logger = LoggerFactory.getLogger(PythonUpdater.class);

	public void updatePythonPath(NullProgressMonitor monitor) {
		final PythonInterpreterManager man = (PythonInterpreterManager) PydevPlugin.getPythonInterpreterManager();

		IInterpreterInfo[] infos = man.getInterpreterInfos();
		List<IInterpreterInfo> newInfos = new ArrayList<IInterpreterInfo>(infos.length);
		Set<String> interpreterNamesToRestore = new HashSet<>();
		for (IInterpreterInfo info : infos) {
			String name = info.getExecutableOrJar();
			logger.debug("Checking " + name + " if updates are needed");
			Optional<IInterpreterInfo> newInfo = updatePythonPath(info);
			if (newInfo.isPresent()) {
				logger.debug("Updates are needed for " + name);
				interpreterNamesToRestore.add(name);
				newInfos.add(newInfo.get());
			} else {
				newInfos.add(info);
			}
		}
		if (interpreterNamesToRestore.size() > 0) {
			logger.debug("Updating being applied for all of " + interpreterNamesToRestore);
			man.setInfos(newInfos.toArray(new IInterpreterInfo[newInfos.size()]), interpreterNamesToRestore, monitor);
		}
	}

	private Optional<IInterpreterInfo> updatePythonPath(IInterpreterInfo info) {
		PyDevAdditionalInterpreterSettings settings = new PyDevAdditionalInterpreterSettings();

		List<String> libs = info.getPythonPath();
		List<String> newLibs = new ArrayList<>(libs.size());

		// First try to replace existing entries that look like scisoftpy/fabio
		boolean scisoftPyAdded = false;
		boolean fabioPathAdded = false;
		for (String lib : libs) {
			if (settings.isScisoftPyPath(lib)) {
				settings.getScisoftPyPath().ifPresent(newLibs::add);
				scisoftPyAdded = true;
			} else if (settings.isFabioPath(lib)) {
				settings.getFabioPath().ifPresent(newLibs::add);
				fabioPathAdded = true;
			} else {
				newLibs.add(lib);
			}
		}

		// then add them if not present
		if (!scisoftPyAdded) {
			settings.getScisoftPyPath().ifPresent(newLibs::add);
		}
		if (!fabioPathAdded) {
			settings.getFabioPath().ifPresent(newLibs::add);
		}

		if (!newLibs.equals(libs)) {
			logger.debug("Libs before: " + libs);
			logger.debug("Libs after: " + newLibs);

			InterpreterInfo copy = (InterpreterInfo) info.makeCopy();
			copy.libs.clear();
			copy.libs.addAll(newLibs);

			return Optional.of(copy);
		}

		return Optional.empty();
	}
}
