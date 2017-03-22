/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.python.pydev.debug.newconsole.PydevConsoleConstants;

public class PyDevConfiguration {

	public void initialiseConsole() {
		// need to set some preferences to get the Pydev features working.
		IPreferenceStore pydevDebugPreferenceStore =  new ScopedPreferenceStore(InstanceScope.INSTANCE,"org.python.pydev.debug");

		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INITIAL_INTERPRETER_CMDS, "#Configuring Environment, please wait\nimport scisoftpy as dnp;import sys;sys.executable=''\n");
		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INTERACTIVE_CONSOLE_VM_ARGS, "-Xmx512m");
		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INTERACTIVE_CONSOLE_MAXIMUM_CONNECTION_ATTEMPTS, 4000);
	}


}
