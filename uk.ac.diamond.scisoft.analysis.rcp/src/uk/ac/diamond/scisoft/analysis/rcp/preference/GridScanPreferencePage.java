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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;

/**
 * Preference page for gridscan sideplot preference settings
 */
public class GridScanPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text pixelResx;
	private Text pixelResy;
	private FloatSpinner xBeamCentre;
	private FloatSpinner yBeamCentre;

	public GridScanPreferencePage() {
		setDescription("Preferences for default grid scan.");
	}

	public GridScanPreferencePage(String title) {
		super(title);
	}

	public GridScanPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);
		
		Group measurementGroup = new Group(comp, SWT.NONE);
		measurementGroup.setText("Image Resolution Calibration");
		measurementGroup.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		measurementGroup.setLayoutData(gd);
	
		Label xRes = new Label(measurementGroup, SWT.LEFT);
		xRes.setText("X Axis: 1mm maps to ");		
		pixelResx = new Text(measurementGroup, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		pixelResx.setToolTipText("Set number of pixels on x axis used to display 1mm for plot view image");
		GridData gd2 = new GridData();
		gd2.widthHint = 50;
		pixelResx.setLayoutData(gd2);
		Label pixelsx = new Label(measurementGroup, SWT.LEFT);
		pixelsx.setText("pixels.");
			
		Label yRes = new Label(measurementGroup, SWT.LEFT);
		yRes.setText("Y Axis: 1mm maps to ");	
		pixelResy = new Text(measurementGroup, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		pixelResy.setToolTipText("Set number of pixels on y axis used to display 1mm for plot view image");
		pixelResy.setLayoutData(gd2);		
		Label pixels = new Label(measurementGroup, SWT.LEFT);
		pixels.setText("pixels.");		
		
		pixelResx.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();				
			}
		});
		
		pixelResy.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();				
			}
		});
		
		Group beamGroup = new Group(comp, SWT.NONE);
		beamGroup.setText("Beam Centre Settings");
		beamGroup.setLayout(new GridLayout(3, false));
		GridData bgd = new GridData(SWT.FILL, SWT.FILL, true, true);
		beamGroup.setLayoutData(bgd);		
		
		Label xBeam = new Label(beamGroup, SWT.LEFT);
		xBeam.setText("Beam centre x axis position: ");	
		xBeamCentre = new FloatSpinner(beamGroup, SWT.SINGLE | SWT.BORDER | SWT.RIGHT, 6, 0);	
		xBeamCentre.setMinimum(0);
		Label pixels1 = new Label(beamGroup, SWT.LEFT);
		pixels1.setText("pixels.");	
		
		Label yBeam = new Label(beamGroup, SWT.LEFT);
		yBeam.setText("Beam centre y axis position: ");	
		yBeamCentre = new FloatSpinner(beamGroup, SWT.SINGLE | SWT.BORDER | SWT.RIGHT, 6, 0);
		yBeamCentre.setMinimum(0);
		Label pixels2 = new Label(beamGroup, SWT.LEFT);
		pixels2.setText("pixels.");	
		
		xBeamCentre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}
		});

		yBeamCentre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}
		});

		//initialize
		initializePage();
		
		return comp;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
	}
	
	@Override
	protected void performDefaults() {
		loadDefaultPreferences();
	}

	
	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}
	
	/**
	 * Load the default resolution value
	 */
	private void loadDefaultPreferences() {
		String string = Double.toString(getDefaultXResolutionPreference());
		pixelResx.setText(string);
		string = Double.toString(getDefaultYResolutionPreference());
		pixelResy.setText(string);
		xBeamCentre.setDouble(getDefaultXBeamCentrePreference());
		yBeamCentre.setDouble(getDefaultYBeamCentrePreference());
	} 
	
	/**
	 * Load the resolution value
	 */
	private void initializePage() {
		String xRes = Double.toString(getXResPreference());
		pixelResx.setText(xRes);
		
		String yRes = Double.toString(getYResPreference());
		pixelResy.setText(yRes);
		
		xBeamCentre.setDouble(getXBeamCentrePreference());
		yBeamCentre.setDouble(getYBeamCentrePreference());
	} 
	
	/**
	 * Store the resolution value
	 */
	private void storePreferences() {
		String resX = pixelResx.getText().trim();
		String resY = pixelResy.getText().trim();
		try {
			setXResPreference(Double.parseDouble(resX));
			setYResPreference(Double.parseDouble(resY));
		} catch (NumberFormatException e) {
			// validation code prevents us reaching here
		}
		setXBeamCentrePreference(xBeamCentre.getDouble());
		setYBeamCentrePreference(yBeamCentre.getDouble());
	}
	
	public void validatePage() {
		boolean isValid = true;
		setErrorMessage(null);
		
		String resX = pixelResx.getText().trim();
		String resY = pixelResy.getText().trim();
		try {
			Double.parseDouble(resX);
			Double.parseDouble(resY);
		} catch (NumberFormatException e) {
			setErrorMessage("Please enter a valid value " + e.getMessage());
			isValid = false;
		}
		setValid(isValid);
	}
	
	/**
	 * Sets the x-axis image resolution value in the preference store
	 * @param value double 
	 */
	public void setXResPreference(double value) {
		getPreferenceStore().setValue(PreferenceConstants.GRIDSCAN_RESOLUTION_X, value);
	}
	
	/**
	 * Sets the x-axis image resolution value in the preference store
	 * @param value double 
	 */
	public void setYResPreference(double value) {
		getPreferenceStore().setValue(PreferenceConstants.GRIDSCAN_RESOLUTION_Y, value);
	}	
	
	/**
	 * Sets the x-axis value of the beam centre
	 * @param value double 
	 */
	public void setXBeamCentrePreference(double value) {
		getPreferenceStore().setValue(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX, value);
	}
	
	/**
	 * Sets the y-axis value of the beam centre
	 * @param value double 
	 */
	public void setYBeamCentrePreference(double value) {
		getPreferenceStore().setValue(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY, value);
	}		
	
	/**
	 * Return the x axis resolution setting from the preference store
	 */
	public double getXResPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_X)){
			return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X);
		}
		return getPreferenceStore().getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X);
	}
	
	/**
	 * Return the y axis resolution setting from the preference store
	 */
	public double getYResPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_Y)){
			return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y);
		}
		return getPreferenceStore().getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y);
	}	
	
	/**
	 * Return the beam centre x value
	 */
	public double getXBeamCentrePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX)){
			return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX);
		}
		return getPreferenceStore().getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX);
	}	
	
	/**
	 * Return the beam centre y value
	 */
	public double getYBeamCentrePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY)){
			return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY);
		}
		return getPreferenceStore().getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY);
	}		
	
	/**
	 * Return the x resolution default setting
	 */
	public double getDefaultXResolutionPreference() {
		return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X);
	}
	
	/**
	 * Return the y resolution default setting
	 */
	public double getDefaultYResolutionPreference() {
		return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y);
	}	
	
	/**
	 * Return the beam centre x default
	 */
	public double getDefaultXBeamCentrePreference() {
		return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX);
	}	
	
	/**
	 * Return the beam centre y default
	 */
	public double getDefaultYBeamCentrePreference() {
		return getPreferenceStore().getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY);
	}		
}
