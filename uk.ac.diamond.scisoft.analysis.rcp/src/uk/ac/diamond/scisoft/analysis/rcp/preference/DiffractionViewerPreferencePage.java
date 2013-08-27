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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

public class DiffractionViewerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static String STRING_DELIMITER = PreferenceInitializer.DELIMITER;
	private Combo pullDownPeaks;
	private Spinner maxNumPeaks;
	private Spinner spnPixeloverloadThreshold;
	private Button autoStopping;
	private Spinner stoppingThreshold;
	private Button mxImageGlobal;

	static private String[] peaknames = { "Gaussian", "Lorentzian", "Pearson VII", "PseudoVoigt" };

	public DiffractionViewerPreferencePage() {

	}

	public DiffractionViewerPreferencePage(String title) {
		super(title);
	}

	public DiffractionViewerPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		TabFolder tabfolder = new TabFolder(parent, SWT.NONE);

		TabItem peakTab = new TabItem(tabfolder, SWT.NONE);
		peakTab.setText("Peak Fitting");

		Composite comp = new Composite(tabfolder, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);

		Group peakType = new Group(comp, SWT.NONE);
		peakType.setText("Peak distribution Selection");
		peakType.setLayout(new GridLayout(2, false));
		GridData ptgd = new GridData(SWT.FILL, SWT.FILL, true, false);
		peakType.setLayoutData(ptgd);

		Label peakLabel = new Label(peakType, SWT.NONE);
		peakLabel.setText("Peak Type");
		pullDownPeaks = new Combo(peakType, SWT.NONE|SWT.READ_ONLY);
		for (int i = 0; i < peaknames.length; i++) {
			pullDownPeaks.add(peaknames[i]);
		}

		Group numnPeakGroup = new Group(comp, SWT.NONE);
		numnPeakGroup.setText("Maximum number of peaks fitted");
		numnPeakGroup.setLayout(new GridLayout(4, false));
		GridData mpgd = new GridData(SWT.FILL, SWT.FILL, true, false);
		numnPeakGroup.setLayoutData(mpgd);

		Label labautofitting = new Label(numnPeakGroup, SWT.NONE);
		labautofitting.setText("Auto Stopping");
		labautofitting.setToolTipText("Stops the fitting once all the peaks above a threshold are found.");

		autoStopping = new Button(numnPeakGroup, SWT.CHECK);
		autoStopping.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAutoStopping(autoStopping.getSelection());
				controlAutoStoppingGUI();
			}
		});

		Label numPeaksLabel = new Label(numnPeakGroup, SWT.NONE);
		numPeaksLabel.setText("Number of peaks ");

		maxNumPeaks = new Spinner(numnPeakGroup, SWT.NONE);
		maxNumPeaks.setDigits(0);
		maxNumPeaks.setMaximum(100);
		maxNumPeaks.setMinimum(0);
		maxNumPeaks.setIncrement(1);

		Label labStopping = new Label(numnPeakGroup, SWT.NONE);
		labStopping.setText("Stopping threshold");
		labStopping.setToolTipText("Sets the stopping threshold for the fitting algorithm");

		stoppingThreshold = new Spinner(numnPeakGroup, SWT.BORDER);
		stoppingThreshold.setDigits(2);
		stoppingThreshold.setMinimum(1);
		stoppingThreshold.setMaximum(99);
		stoppingThreshold.setIncrement(1);

		peakTab.setControl(comp);

//		TabItem cal = new TabItem(tabfolder, SWT.NONE);
//		cal.setText("Calibration");
//
//		Composite calComp = new Composite(tabfolder, SWT.NONE);
//		calComp.setLayout(new GridLayout(1, false));
//		GridData calgd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		calComp.setLayoutData(calgd);
//
//		Group calibrationStandard = new Group(calComp, SWT.NONE);
//		calibrationStandard.setText("Detector Calibration Samples:");
//		GridData csgd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		calibrationStandard.setLayoutData(csgd);
//
//		GridData data = new GridData(GridData.FILL_BOTH);
//		standardType = new List(calibrationStandard, SWT.BORDER);
//		standardType.setLayoutData(data);
//		standardType.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				sfeNewCal.setStringValue(standardNames.get(standardType.getSelectionIndex()));
//				sfeNewCalDSpacing.setStringValue(standardDistances.get(standardType.getSelectionIndex()));
//
//			}
//		});
//
//		Group addNewCalibrant = new Group(calComp, SWT.NONE);
//		addNewCalibrant.setText("Add New Calibrant");
//
//		calibrationStandard.setLayout(new GridLayout(2, false));
//		GridData ncsgd = new GridData(SWT.FILL, SWT.FILL, true, false);
//		addNewCalibrant.setLayoutData(ncsgd);
//
//		sfeNewCal = new StringFieldEditor(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME, "Calibrant Name: ",
//				addNewCalibrant);
//		sfeNewCalDSpacing = new StringFieldEditor(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES,
//				"Calibrant d spacing", addNewCalibrant);
//		sfeNewCalDSpacing.setStringValue("Separated by commas");
//
//		add = new Button(addNewCalibrant, SWT.NONE);
//		add.setText("Add");
//		add.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				validateStandard();
//			}
//		});
//
//		remove = new Button(addNewCalibrant, SWT.NONE);
//		remove.setText("Remove");
//		remove.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				standardDistances.remove(standardType.getSelectionIndex());
//				standardNames.remove(standardType.getSelectionIndex());
//				updateGUIList();
//			}
//		});
//
//		cal.setControl(calComp);

		TabItem other = new TabItem(tabfolder, SWT.NONE);
		other.setText("Other");
		Composite otherComp = new Composite(tabfolder, SWT.NONE);
		otherComp.setLayout(new GridLayout(1, false));
		gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		otherComp.setLayoutData(gdc);
		other.setControl(otherComp);
		Group pixelOverloadGroup = new Group(otherComp, SWT.NONE);
		pixelOverloadGroup.setText("Detector pixel overload");
		pixelOverloadGroup.setLayout(new GridLayout(2, false));
		mpgd = new GridData(SWT.FILL, SWT.FILL, true, false);
		pixelOverloadGroup.setLayoutData(mpgd);

		Label pixelOverloadLabel = new Label(pixelOverloadGroup, SWT.NONE);
		pixelOverloadLabel.setText("Threshold value ");

		spnPixeloverloadThreshold = new Spinner(pixelOverloadGroup, SWT.BORDER);
		spnPixeloverloadThreshold.setDigits(0);
		spnPixeloverloadThreshold.setMaximum(100000);
		spnPixeloverloadThreshold.setMinimum(0);
		spnPixeloverloadThreshold.setIncrement(1);

		Group mxImageGlobalGroup = new Group(otherComp, SWT.NONE);
		mxImageGlobalGroup.setText("The MX image editor");
		mxImageGlobalGroup.setLayout(new GridLayout(2, false));
		mpgd = new GridData(SWT.FILL, SWT.FILL, true, false);
		mxImageGlobalGroup.setLayoutData(mpgd);

		Label mxImageGlobalLabel = new Label(mxImageGlobalGroup, SWT.NONE);
		mxImageGlobalLabel.setText("Editor available in all perspectives");
		mxImageGlobal = new Button(mxImageGlobalGroup, SWT.CHECK);

		// initialize
		initializePage();
		return tabfolder;
	}

	private void initializePage() {
		maxNumPeaks.setSelection(getMaxNumPeaks());
		pullDownPeaks.select(getPeakNumber(getPeakType()));
		spnPixeloverloadThreshold.setSelection(getPixelOverloadThreshold());
		mxImageGlobal.setSelection(getMXImageGlobal());
		stoppingThreshold.setSelection(getStoppingThreshold());
		autoStopping.setSelection(getAutoStopping());
		controlAutoStoppingGUI();
	}

	protected void controlAutoStoppingGUI() {
		maxNumPeaks.setEnabled(!getAutoStopping());
		stoppingThreshold.setEnabled(getAutoStopping());
	}

	@Override
	protected void performDefaults() {
		loadDefaultPreferences();
	}

	private int getPeakNumber(String s) {
		if (s.compareToIgnoreCase("Gaussian") == 0) {
			return 0;
		} else if (s.compareToIgnoreCase("Lorentzian") == 0) {
			return 1;
		} else if (s.compareToIgnoreCase("Pearson VII") == 0) {
			return 2;
		} else if (s.compareToIgnoreCase("PseudoVoigt") == 0) {
			return 3;
		} else {
			return 0;
		}
	}

	private void loadDefaultPreferences() {
		pullDownPeaks.select(getPeakNumber(getDefaultPeakType()));
		maxNumPeaks.setSelection(getDefaultMaxNumPeaks());
		stoppingThreshold.setSelection(getDefaultStoppingThreshold());
	}

	// Standard Name
	public String getDefaultStandardName() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
	}

	public String getStandardName() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME)) {
			return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
		}
		return getPreferenceStore().getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
	}

	public void setStandardName(String standardName) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME, standardName);
	}

	// Standard Distance
	public String getDefaultStandardDistance() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
	}

	public String getStandardDistance() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES)) {
			return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
		}
		return getPreferenceStore().getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
	}

	public void setStandardDistance(String standardDistances) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES, standardDistances);
	}

	// Standard Distance List
	public String getDefaultStandardDistanceList() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST);
	}

	public String getStandardDistanceList() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST)) {
			return getPreferenceStore()
					.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST);
		}
		return getPreferenceStore().getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST);
	}

	public void setStandardDistanceList(String standardNameList) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST, standardNameList);
	}

	// Standard Names List
	public String getDefaultStandardList() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST);
	}

	public String getStandardList() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST)) {
			return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST);
		}
		return getPreferenceStore().getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST);
	}

	public void setStandardList(String standardNameList) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST, standardNameList);
	}

	// Peak Names
	public String getDefaultPeakType() {
		return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);
	}

	public String getPeakType() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE)) {
			return getPreferenceStore().getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);
		}
		return getPreferenceStore().getString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);
	}

	public void setPeakType(String peakType) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE, peakType);
	}

	// max num peaks

	public int getDefaultMaxNumPeaks() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);
	}

	public int getMaxNumPeaks() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);
		}
		return getPreferenceStore().getInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);
	}

	public void setMaxNumPeaks(int numPeaks) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM, numPeaks);
	}

	// Auto Stopping
	public boolean getDefaultAutoStopping() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);
	}

	public boolean getAutoStopping() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING))
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);
		return getPreferenceStore().getBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);
	}

	public void setAutoStopping(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING, value);
	}

	// Stopping Threshold
	public int getDefaultStoppingThreshold() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
	}

	public int getStoppingThreshold() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
		}
		return getPreferenceStore().getInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
	}

	public void setStoppingThreshold(int value) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD, value);
	}

	// pixel overload value
	public int getDefaultPixelOverloadThreshold() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
	}

	public int getPixelOverloadThreshold() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
		}
		return getPreferenceStore().getInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
	}

	public void setPixelOverloadThreshold(int threshold) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD, threshold);
	}

	// MX image global value
	public boolean getDefaultMXImageGlobal() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.DIFFRACTION_VIEWER_MX_IMAGE_GLOBAL);
	}
	
	public boolean getMXImageGlobal() {
		if (getPreferenceStore().isDefault(PreferenceConstants.DIFFRACTION_VIEWER_MX_IMAGE_GLOBAL)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.DIFFRACTION_VIEWER_MX_IMAGE_GLOBAL);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.DIFFRACTION_VIEWER_MX_IMAGE_GLOBAL);
	}
	
	public void setMXImageGlobal(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.DIFFRACTION_VIEWER_MX_IMAGE_GLOBAL, value);
	}
	
	
	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}

	private void storePreferences() {
		setMaxNumPeaks(maxNumPeaks.getSelection());
		setPeakType(peaknames[pullDownPeaks.getSelectionIndex()]);
		setPixelOverloadThreshold(spnPixeloverloadThreshold.getSelection());
		setMXImageGlobal(mxImageGlobal.getSelection());
		setAutoStopping(autoStopping.getSelection());
		setStoppingThreshold(stoppingThreshold.getSelection());
	}


}
