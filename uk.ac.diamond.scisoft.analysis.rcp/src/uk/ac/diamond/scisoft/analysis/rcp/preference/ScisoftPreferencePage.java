/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
