/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import java.util.StringTokenizer;

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
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;

public class Fitting1DPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static String STRING_DELIMITER = PreferenceInitializer.DELIMITER;
	private Combo algType;
	private FloatSpinner accuracy;
	private Spinner smoothing;
	private Button autoSmooth;
	private Button autoStopping;
	private Spinner threshold;
	private Combo thresholdingMeasure;
	private Combo peakType;
	private Spinner numPeaks;
	private Spinner decimalPlaces;

	public Fitting1DPreferencePage() {

	}

	/**
	 * @wbp.parser.constructor
	 */
	public Fitting1DPreferencePage(String title) {
		super(title);
	}

	public Fitting1DPreferencePage(String title, ImageDescriptor image) {
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

		Group peakControlGroup = new Group(comp, SWT.NONE);
		peakControlGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		peakControlGroup.setLayout(new GridLayout(2, false));
		peakControlGroup.setText("Peak Controls");

		Label labPeakName = new Label(peakControlGroup, SWT.NONE);
		labPeakName.setText("Peak type");
		labPeakName.setToolTipText("Choose the probability density function to be fitted");

		peakType = new Combo(peakControlGroup, SWT.NONE|SWT.READ_ONLY);

		Label labNumPeaks = new Label(peakControlGroup, SWT.NONE);
		labNumPeaks.setText("Number of peaks to be fitted");

		numPeaks = new Spinner(peakControlGroup, SWT.NONE);
		numPeaks.setDigits(0);
		numPeaks.setMinimum(-1);

		Group algGroup = new Group(comp, SWT.NONE);
		algGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		algGroup.setLayout(new GridLayout(2, false));
		algGroup.setText("Algorithm Controls");

		Label algLabel = new Label(algGroup, SWT.NONE);
		algLabel.setText("Fitting algorithm");

		algType = new Combo(algGroup, SWT.NONE|SWT.READ_ONLY);

		Label accuractlab = new Label(algGroup, SWT.NONE);
		accuractlab.setText("Accuracy");
		accuractlab.setToolTipText("This sets the accuracy of the optomisation. "
				+ "The lower the number to more accurate the calculation");

		accuracy = new FloatSpinner(algGroup, SWT.NONE, 6, 5);
		//accuracy.setMinimum(0.00001);

		Label smoothingLab = new Label(algGroup, SWT.NONE);
		smoothingLab.setText("Smoothing");
		smoothingLab.setToolTipText("Smoothing over that many data points will be applied by the peak searching algorithm");

		smoothing = new Spinner(algGroup, SWT.NONE);
		smoothing.setDigits(0);
		smoothing.setMinimum(0);
		smoothing.setMaximum(10000);
		smoothing.setEnabled(false);

		Label autoSmoothLab = new Label(algGroup, SWT.NONE);
		autoSmoothLab.setText("Auto Smooth");
		autoSmoothLab.setToolTipText("Sets the smoothing to 1% of the data size (not recommened)");

		autoSmooth = new Button(algGroup, SWT.CHECK);
		autoSmooth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				smoothing.setEnabled(!autoSmooth.getSelection());
			}
		});
		Group stopping = new Group(comp, SWT.NONE);
		stopping.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		stopping.setLayout(new GridLayout(2, false));
		stopping.setText("Stopping Criteria");

		Label autoStopLabel = new Label(stopping, SWT.NONE);
		autoStopLabel.setText("Auto Stopping");
		autoStopLabel.setToolTipText("Stops the fitting routine at a given criterium");

		autoStopping = new Button(stopping, SWT.CHECK);
		autoStopping.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controlAutoStoppingGUI();
			}
		});

		Label thresholdLabel = new Label(stopping, SWT.NONE);
		thresholdLabel.setText("Stopping Threshold");
		thresholdLabel.setToolTipText("Stops the fitting routine when the next peak is less than the indicated"
				+ " proportion in size of the largest peak.");

		threshold = new Spinner(stopping, SWT.NONE);
		threshold.setDigits(2);
		threshold.setMinimum(1);
		threshold.setMaximum(99);
		threshold.setEnabled(false);
		threshold.setToolTipText("Stops the fitting routine when the next peak is less than the indicated"
				+ " porportion in size of the largest peak.");

		Label thresholdingMeasureLabel = new Label(stopping, SWT.NONE);
		thresholdingMeasureLabel.setText("Thresholding Measure");

		thresholdingMeasure = new Combo(stopping, SWT.NONE|SWT.READ_ONLY);
		
		Group sF = new Group(comp, SWT.NONE);
		sF.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		sF.setLayout(new GridLayout(2, false));
		sF.setText("Significant Figures");
		sF.setToolTipText("Set the number of significant figures in the table of fitted peaks");
		
		Label lblDecimalPlaces = new Label(sF, SWT.NONE);
		lblDecimalPlaces.setText("Decimal Places");
		
		decimalPlaces = new Spinner(sF, SWT.NONE);
		decimalPlaces.setValues(2, 0, 10, 0, 1, 1);
		
		// initialize
		initializePage();
		return comp;
	}

	private void initializePage() {
		populateAlgList();
		populateThresholdingMeasureList();
		populatePeakNameList();
		accuracy.setDouble(getAccuracy());
		numPeaks.setSelection(getNumPeaks());
		smoothing.setSelection(getSmoothing());
		autoSmooth.setSelection(getDefaultAutoSmoothing());
		autoStopping.setSelection(getAutoFitting());
		threshold.setSelection(getThreshold());
		decimalPlaces.setSelection(getDecimalPlaces());
		controlAutoStoppingGUI();
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}

	@Override
	protected void performDefaults() {
		numPeaks.setSelection(getDefaultPeakNum());
		peakType.select(getDefaultPeak());
		accuracy.setDouble(getDefaultAccuracy());
		smoothing.setSelection(getDefaultSmoothing());
		autoSmooth.setSelection(getDefaultAutoSmoothing());
		autoStopping.setSelection(getDefaultAutoStopping());
		threshold.setSelection(getDefaultThreshold());
		algType.select(getDetaultAlg());
		thresholdingMeasure.select(getDefaultMeasure());
		decimalPlaces.setSelection(getDefaultDecimalPlaces());
		controlAutoStoppingGUI();
	}


	private void storePreferences() {
		setNumPeaks(numPeaks.getSelection());
		setPeakName(peakType.getText());
		setAccuracy(accuracy.getDouble());
		setSmoothing(smoothing.getSelection());
		setAlgType(algType.getText());
		setAutoFitting(autoStopping.getSelection());
		setThreshold(threshold.getSelection());
		setThresholdingMeasure(thresholdingMeasure.getText());
		setDecimalPlaces(decimalPlaces.getSelection());
	}


	private boolean getDefaultAutoStopping() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
	}

	private int getDefaultThreshold() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_THRESHOLD);
	}

	private boolean getDefaultAutoSmoothing() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING);
	}

	private int getDefaultSmoothing() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
	}

	private double getDefaultAccuracy() {
		return getPreferenceStore().getDefaultDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
	}

	private int getDefaultPeakNum() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_PEAK_NUM);
	}

	private int getDetaultAlg() {
		String temp = getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_ALG_TYPE);
		StringTokenizer st = new StringTokenizer(getAlgorithmTypeList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			if (st.nextToken().equalsIgnoreCase(temp))
				return i;
			i++;
		}
		return 0;
	}

	private void populateAlgList() {
		String temp = getAlgType();
		StringTokenizer st = new StringTokenizer(getAlgorithmTypeList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			algType.add(token);
			if (token.equalsIgnoreCase(temp))
				algType.select(i);
			i++;
		}
	}

	private int getDefaultMeasure() {
		String temp = getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE);
		StringTokenizer st = new StringTokenizer(getThresholdingMeasureList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			if (st.nextToken().equalsIgnoreCase(temp))
				return i;
			i++;
		}
		return 0;
	}

	private void populateThresholdingMeasureList() {
		String temp = getThresholdingMeasure();
		StringTokenizer st = new StringTokenizer(getThresholdingMeasureList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			thresholdingMeasure.add(token);
			if (token.equalsIgnoreCase(temp))
				thresholdingMeasure.select(i);
			i++;
		}
	}

	private int getDefaultPeak() {
		String temp = getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_PEAKTYPE);
		StringTokenizer st = new StringTokenizer(getThresholdingMeasureList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			if (st.nextToken().equalsIgnoreCase(temp))
				return i;
			i++;
		}
		return 0;
	}

	private void populatePeakNameList() {
		String temp = getPeakName();
		StringTokenizer st = new StringTokenizer(getPeakNameList(), PreferenceInitializer.DELIMITER);
		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			peakType.add(token);
			if (token.equalsIgnoreCase(temp))
				peakType.select(i);
			i++;
		}

	}

	protected void controlAutoStoppingGUI() {
		thresholdingMeasure.setEnabled(autoStopping.getSelection());
		threshold.setEnabled(autoStopping.getSelection());
		numPeaks.setEnabled(!autoStopping.getSelection());
	}

	// ///////////////////////////////////////////////
	// Gettes and setters //
	// ///////////////////////////////////////////////

	private String getPeakName() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_PEAKTYPE))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_PEAKTYPE);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_PEAKTYPE);
	}

	private void setPeakName(String name) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_PEAKTYPE, name);
	}

	public String getAlgType() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_ALG_TYPE))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_ALG_TYPE);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_ALG_TYPE);
	}

	public void setAlgType(String algName) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_ALG_TYPE, algName);
	}

	public int getSmoothing() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE))
			return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
		return getPreferenceStore().getInt(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE);
	}

	public void setSmoothing(int smooth) {
		getPreferenceStore().setDefault(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE, smooth);
	}

	public double getAccuracy() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_ALG_ACCURACY))
			return getPreferenceStore().getDefaultDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
		return getPreferenceStore().getDouble(PreferenceConstants.FITTING_1D_ALG_ACCURACY);
	}

	public void setAccuracy(double accuracy) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_ALG_ACCURACY, accuracy);
	}

	public boolean getAutoFitting() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_AUTO_STOPPING))
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
		return getPreferenceStore().getBoolean(PreferenceConstants.FITTING_1D_AUTO_STOPPING);
	}

	public void setAutoFitting(boolean selected) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_AUTO_STOPPING, selected);
	}

	private int getThreshold() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_THRESHOLD))
			return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_THRESHOLD);
		return getPreferenceStore().getInt(PreferenceConstants.FITTING_1D_THRESHOLD);
	}

	private void setThreshold(int value) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_THRESHOLD, value);
	}

	private String getThresholdingMeasure() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE);
	}

	private void setThresholdingMeasure(String value) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE, value);
	}

	private int getNumPeaks() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_PEAK_NUM))
			return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_PEAK_NUM);
		return getPreferenceStore().getInt(PreferenceConstants.FITTING_1D_PEAK_NUM);
	}

	private void setNumPeaks(int value) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_PEAK_NUM, value);
	}

	////////////////////////////////////////////////////////
	//                 Lists of variables                 //
	////////////////////////////////////////////////////////
	
	private String getThresholdingMeasureList() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE_LIST))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE_LIST);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE_LIST);
	}

	public String getAlgorithmTypeList() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_ALG_LIST))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_ALG_LIST);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_ALG_LIST);
	}

	private String getPeakNameList() {
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_PEAKLIST))
			return getPreferenceStore().getDefaultString(PreferenceConstants.FITTING_1D_PEAKLIST);
		return getPreferenceStore().getString(PreferenceConstants.FITTING_1D_PEAKLIST);
	}
	
	private int getDecimalPlaces(){
		if (getPreferenceStore().isDefault(PreferenceConstants.FITTING_1D_DECIMAL_PLACES))
			return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_DECIMAL_PLACES);
		return getPreferenceStore().getInt(PreferenceConstants.FITTING_1D_DECIMAL_PLACES);
	}
	

	private int getDefaultDecimalPlaces() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.FITTING_1D_DECIMAL_PLACES);
	}
	

	private void setDecimalPlaces(int selection) {
		getPreferenceStore().setValue(PreferenceConstants.FITTING_1D_DECIMAL_PLACES, selection);
		
	}
}
