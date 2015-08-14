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

		store.setDefault(QStatMonitorPreferenceConstants.P_SLEEP,
				QStatMonitorPreferenceConstants.DEF_SLEEP);
		store.setDefault(QStatMonitorPreferenceConstants.P_REFRESH,
				QStatMonitorPreferenceConstants.DEF_REFRESH);
		store.setDefault(QStatMonitorPreferenceConstants.P_RESOURCES_ALL,
				QStatMonitorPreferenceConstants.DEF_RESOURCES_ALL);
		store.setDefault(QStatMonitorPreferenceConstants.P_RESOURCE,
				QStatMonitorPreferenceConstants.DEF_RESOURCE);
		store.setDefault(QStatMonitorPreferenceConstants.P_USER_ALL,
				QStatMonitorPreferenceConstants.DEF_USER_ALL);
		store.setDefault(QStatMonitorPreferenceConstants.P_USER_CURR,
				QStatMonitorPreferenceConstants.DEF_USER_CURR);
		store.setDefault(QStatMonitorPreferenceConstants.P_USER_CUST,
				QStatMonitorPreferenceConstants.DEF_USER_CUST);
		store.setDefault(QStatMonitorPreferenceConstants.P_USER,
				QStatMonitorPreferenceConstants.DEF_USER);
	}

}
