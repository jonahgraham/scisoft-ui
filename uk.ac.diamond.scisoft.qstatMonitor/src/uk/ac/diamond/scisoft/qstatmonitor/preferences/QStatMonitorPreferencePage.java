/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.qstatmonitor.Activator;

public class QStatMonitorPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.diamond.scisoft.qstatmonitor.preferences.QStatMonitorPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(QStatMonitorPreferencePage.class);

	public QStatMonitorPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		return null;
	}
	

	@Override
	public boolean performOk() {
		return super.performOk();
	}

}
