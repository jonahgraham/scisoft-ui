/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;

public class FileNavigatorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = NavigatorRCPActivator.getDefault().getPreferenceStore();

		//File navigator preferences
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_DATE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_TYPE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_SIZE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN, false);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN, false);

	}
}
