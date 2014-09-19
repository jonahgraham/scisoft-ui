/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

public class SidePlotterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button chkUseLogY;

	public SidePlotterPreferencePage() {
	}

	public SidePlotterPreferencePage(String title) {
		super(title);
	}

	public SidePlotterPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);

		Group plot1DGroup = new Group(comp, SWT.NONE);
		plot1DGroup.setText("Plot 1D");
		plot1DGroup.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		plot1DGroup.setLayoutData(gd);
		Label lblLogY = new Label(plot1DGroup, SWT.LEFT);
		lblLogY.setText("Use logscale for Y (initial setting)");
		chkUseLogY = new Button(plot1DGroup, SWT.CHECK | SWT.RIGHT);

		initializePage();

		return comp;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}

	@Override
	protected void performDefaults() {
		loadDefaultPreferences();
	}

	private void initializePage() {
		chkUseLogY.setSelection(getUseLogYPreference());
	}

	private void loadDefaultPreferences() {
		chkUseLogY.setSelection(getDefaultUseLogYPreference());
	}

	private void storePreferences() {
		setUseLogYPreference(chkUseLogY.getSelection());
	}

	public boolean getDefaultUseLogYPreference() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y);
	}

	public boolean getUseLogYPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y);
	}

	public void setUseLogYPreference(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y, value);
	}

}
