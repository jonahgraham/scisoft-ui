/*
 * Copyright 2013 Diamond Light Source Ltd.
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
