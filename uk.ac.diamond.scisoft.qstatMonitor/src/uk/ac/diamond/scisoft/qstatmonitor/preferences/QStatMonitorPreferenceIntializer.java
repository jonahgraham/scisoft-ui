/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
//import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.ac.diamond.scisoft.qstatmonitor.Activator;

public class QStatMonitorPreferenceIntializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		/*
		final IPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.qstatMonitor");
		*/
		store.setDefault(QStatMonitorConstants.SLEEP, "4.5");
		store.setDefault(QStatMonitorConstants.DISABLE_AUTO_REFRESH, false);
		store.setDefault(QStatMonitorConstants.DISABLE_AUTO_PLOT, false);
		store.setDefault(QStatMonitorConstants.QUERY, "qstat");
		store.setDefault(QStatMonitorConstants.USER, "*");		
	}

}
