/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class QStatMonitorPreferenceIntializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {		
		final IPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.qstatMonitor");
		store.setDefault(QStatMonitorPreferencePage.SLEEP, "4.5");
		store.setDefault(QStatMonitorPreferencePage.DISABLE_AUTO_REFRESH, false);
		store.setDefault(QStatMonitorPreferencePage.DISABLE_AUTO_PLOT, false);
		store.setDefault(QStatMonitorPreferencePage.QUERY, "qstat");
		store.setDefault(QStatMonitorPreferencePage.USER, "*");		
	}

}
