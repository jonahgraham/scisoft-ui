/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
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
	
	private Button btnAutoRefresh;
	private Spinner spnInterval;
	
	public QStatMonitorPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group grpRefresh = new Group(mainComposite, SWT.NONE);
		grpRefresh.setText("Refresh Settings");
		grpRefresh.setLayout(new GridLayout(2, false));
		grpRefresh.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		(new Label(grpRefresh, SWT.NONE)).setText("Enable automatic refreshing");
		btnAutoRefresh = new Button(grpRefresh, SWT.CHECK | SWT.NO_FOCUS);
		
		(new Label(grpRefresh, SWT.NONE)).setText("Interval (seconds)");
		spnInterval = new Spinner(grpRefresh, SWT.READ_ONLY | SWT.BORDER);
		spnInterval.setDigits(1);
		spnInterval.setIncrement(5);
		spnInterval.setMinimum(20);
		spnInterval.setMaximum(1000);
		spnInterval.setSelection((int) (Double.parseDouble(preferences
				.getString(QStatMonitorPreferenceConstants.P_SLEEP)) * 10));
		
		return mainComposite;
	}
	

	@Override
	public boolean performOk() {
		return super.performOk();
	}

}
