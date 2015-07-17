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
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.qstatmonitor.Activator;

public class QStatMonitorPreferenceIntializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(QStatMonitorConstants.P_SLEEP,
				QStatMonitorConstants.DEF_SLEEP);
		store.setDefault(QStatMonitorConstants.P_REFRESH,
				QStatMonitorConstants.DEF_REFRESH);
		store.setDefault(QStatMonitorConstants.P_PLOT,
				QStatMonitorConstants.DEF_PLOT);
		store.setDefault(QStatMonitorConstants.P_QUERY,
				QStatMonitorConstants.DEF_QUERY);
		store.setDefault(QStatMonitorConstants.P_USER,
				QStatMonitorConstants.DEF_USER);
	}

}
