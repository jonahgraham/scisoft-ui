/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

import org.dawb.common.ui.printing.PrintSettings;
import org.dawb.common.ui.printing.PrintSettings.Orientation;
import org.dawb.common.ui.printing.PrintSettings.Resolution;
import org.dawb.common.ui.printing.PrintSettings.Scale;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

public class PrintSettingsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Combo scaleCombo;
	private Combo printerListCombo;
	private Combo orientationCombo;
	private Combo resolutionCombo;
	private Button keepAspectRatio;
	private PrintSettings settings;
	
	public PrintSettingsPreferencePage() {
	}

	public PrintSettingsPreferencePage(String title) {
		super(title);
	}

	public PrintSettingsPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);
		Group printSettingsGroup = new Group(comp, SWT.NONE);
		printSettingsGroup.setText("Print Settings");
		printSettingsGroup.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		printSettingsGroup.setLayoutData(gd);

		Label printerNameLabel = new Label(printSettingsGroup, SWT.LEFT);
		printerNameLabel.setText("Printer Name: ");
		printerListCombo = new Combo(printSettingsGroup, SWT.RIGHT|SWT.READ_ONLY);
		printerListCombo.setToolTipText("List of all available printers");
		if(settings == null){
			settings = new PrintSettings();
		}

		PrinterData[] printerList = Printer.getPrinterList();
		for (int i = 0; i < printerList.length; i++) {
			printerListCombo.add(printerList[i].name);
		}

		Label scaleLabel = new Label(printSettingsGroup, SWT.LEFT);
		scaleLabel.setText("Scale: ");
		scaleCombo = new Combo(printSettingsGroup, SWT.RIGHT|SWT.READ_ONLY);
		scaleCombo.setToolTipText("Change the scale of the plot to be printed");
		Scale[] scaleList = Scale.values();
		for (int i = 0; i < scaleList.length; i++) {
			scaleCombo.add(scaleList[i].getName().toString());
		}

		Label resolutionLabel = new Label(printSettingsGroup, SWT.LEFT);
		resolutionLabel.setText("Resolution: ");
		resolutionCombo = new Combo(printSettingsGroup, SWT.RIGHT|SWT.READ_ONLY);
		resolutionCombo.setToolTipText("Change the resolution of the plot to be printed");
		Resolution[] resolutionList = Resolution.values();
		for (int i = 0; i < resolutionList.length; i++) {
			resolutionCombo.add(resolutionList[i].getName().toString());
		}

		Label orientationLabel = new Label(printSettingsGroup, SWT.LEFT);
		orientationLabel.setText("Orientation: ");
		orientationCombo = new Combo(printSettingsGroup, SWT.RIGHT|SWT.READ_ONLY);
		orientationCombo.setEnabled(false); //not yet fully functional
		orientationCombo.setToolTipText("Not yet fully functional");
		Orientation[] orientationList = Orientation.values();
		for (int i = 0; i < orientationList.length; i++) {
			orientationCombo.add(orientationList[i].getName().toString());
		}

		keepAspectRatio = new Button(printSettingsGroup, SWT.CHECK);
		keepAspectRatio.setText("Keep Aspect Ratio");
		keepAspectRatio.setToolTipText("Check to keep the aspect ratio of the plot");
		
		initializePage();

		return comp;
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

	/**
	 * Load the print settings value
	 */
	private void initializePage() {
		scaleCombo.select(getScalePreference());
		printerListCombo.select(getPrinterNamePreference());
		orientationCombo.select(getOrientationPreference());
		resolutionCombo.select(getResolutionPreference());
		keepAspectRatio.setSelection(getKeepAspectRatio());
	}

	/**
	 * Load the default print settings value
	 */
	private void loadDefaultPreferences() {
		scaleCombo.select(getDefaultScalePreference());
		printerListCombo.select(getDefaultPrinterNamePreference());
		orientationCombo.select(getDefaultOrientationPreference());
		resolutionCombo.select(getDefaultOrientationPreference());
	}

	/**
	 * Store the print settings value
	 */
	private void storePreferences() {
		setScalePreference(scaleCombo.getSelectionIndex());
		setPrinterNamePreference(printerListCombo.getSelectionIndex());
		setOrientationPreference(orientationCombo.getSelectionIndex());
		setResolutionPreference(resolutionCombo.getSelectionIndex());
	}

	public int getDefaultPrinterNamePreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME);
	}

	public int getDefaultOrientationPreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_ORIENTATION);
	}

	public int getDefaultScalePreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_SCALE);
	}
	
	public int getDefaultResolutionPreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_RESOLUTION);
	}

	public int getPrinterNamePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME);
	}

	public int getOrientationPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PRINTSETTINGS_ORIENTATION)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_ORIENTATION);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PRINTSETTINGS_ORIENTATION);
	}

	public int getScalePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PRINTSETTINGS_SCALE)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_SCALE);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PRINTSETTINGS_SCALE);
	}

	public int getResolutionPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PRINTSETTINGS_RESOLUTION)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PRINTSETTINGS_RESOLUTION);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PRINTSETTINGS_RESOLUTION);
	}

	public boolean getKeepAspectRatio() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PRINTSETTINGS_ASPECTRATIO)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PRINTSETTINGS_ASPECTRATIO);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.PRINTSETTINGS_ASPECTRATIO);
	}

	public void setScalePreference(int value) {
		settings.setScale(Scale.values()[value]);
		getPreferenceStore().setValue(PreferenceConstants.PRINTSETTINGS_SCALE, value);
	}

	public void setOrientationPreference(int value) {
		settings.setOrientation(Orientation.values()[value]);
		getPreferenceStore().setValue(PreferenceConstants.PRINTSETTINGS_ORIENTATION, value);
	}

	public void setKeepAspectRatioPreference(boolean value) {
		settings.setKeepAspectRatio(value);
		getPreferenceStore().setValue(PreferenceConstants.PRINTSETTINGS_ASPECTRATIO, value);
	}

	public void setPrinterNamePreference(int value) {
		settings.setPrinterData(Printer.getPrinterList()[value]);
		getPreferenceStore().setValue(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME, value);
	}

	public void setResolutionPreference(int value) {
		settings.setResolution(Resolution.values()[value]);
		getPreferenceStore().setValue(PreferenceConstants.PRINTSETTINGS_RESOLUTION, value);
	}
}

