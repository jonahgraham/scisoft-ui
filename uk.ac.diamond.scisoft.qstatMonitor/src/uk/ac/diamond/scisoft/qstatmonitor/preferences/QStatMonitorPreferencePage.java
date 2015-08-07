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
import org.eclipse.swt.widgets.Combo;
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
	
	private static final int SPN_DEC_PLACES = 1;
	// Must not exceed SPN_DEC_PLACES number of decimal places
	private static final float REF_INC_VAL = 0.5f;
	private static final float REF_MIN_VAL = 2.0f;
	private static final float REF_MAX_VAL = 100.0f;
	
	/**
	 * Converts refresh interval value to spinner compatible value (integer)
	 * 
	 * @param 
	 * 		value
	 * @return
	 * 		spinner value
	 */
	private static int convertSpinnerValue(float value) {
		int multFactor = (int) Math.pow(10, SPN_DEC_PLACES);		
		return (int) (value * multFactor);
	}
	
	private Button btnAutoRefresh;
	private Spinner spnInterval;
	private Combo cboResource;
	private Combo cboUser;
	
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
		spnInterval.setDigits(SPN_DEC_PLACES);
		spnInterval.setIncrement(convertSpinnerValue(REF_INC_VAL));
		spnInterval.setMinimum(convertSpinnerValue(REF_MIN_VAL));
		spnInterval.setMaximum(convertSpinnerValue(REF_MAX_VAL));
		spnInterval.setSelection(convertSpinnerValue(preferences.getFloat(QStatMonitorPreferenceConstants.P_SLEEP)));
		
		Group grpQuery = new Group(mainComposite, SWT.NONE);
		grpQuery.setText("Query Configuration");
		grpQuery.setLayout(new GridLayout(2, false));
		grpQuery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		(new Label(grpQuery, SWT.NONE)).setText("Resource");
		cboResource = new Combo(grpQuery, SWT.DROP_DOWN);
		cboResource.setItems(new String[] {"tesla", "tesla64", "All"});
		cboResource.setSize(5, 0);
		
		(new Label(grpQuery, SWT.NONE)).setText("User");
		cboUser = new Combo(grpQuery, SWT.DROP_DOWN | SWT.WRAP);
		cboUser.setItems(new String[] {"Default", "All"});

		return mainComposite;
	}
	

	@Override
	public boolean performOk() {
		return super.performOk();
	}

}
