/*
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

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

/**
 * This class is to represent global DAWN preferences.
 * It provides a root node for the other DAWN preference pages
 */
public class ScisoftPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public ScisoftPreferencePage() {
		super(GRID);
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
		setDescription("DAWN Preferences (see sub pages)");
	}
	
	@Override
	protected void createFieldEditors() {
		
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
