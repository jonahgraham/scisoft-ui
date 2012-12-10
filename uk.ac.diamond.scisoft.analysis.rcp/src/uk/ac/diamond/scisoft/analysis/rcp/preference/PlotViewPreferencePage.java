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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.GlobalColourMaps;

public class PlotViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Combo cmbColourMap;
	private Combo cmbColourScale;
	private Combo cmbCameraPerspective;
	private Button chkExpertMode;
	private Button chkAutoContrast;
	private Button chkScrollbars;
	private Spinner spnAutoLoThreshold;
	private Spinner spnAutoHiThreshold;
	private Combo cmbPlottingSystem;

	public PlotViewPreferencePage() {
	}

	public PlotViewPreferencePage(String title) {
		super(title);
	}

	public PlotViewPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));

		Group plottingSystemGroup = new Group(comp, SWT.NONE);
		plottingSystemGroup.setText("Plotting System");
		plottingSystemGroup.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		plottingSystemGroup.setLayoutData(gd);
		Label lblPlottingSys = new Label(plottingSystemGroup, SWT.LEFT);
		lblPlottingSys.setText("Default plotting system (restart to take effect): ");
		cmbPlottingSystem = new Combo(plottingSystemGroup, SWT.RIGHT | SWT.READ_ONLY);
		cmbPlottingSystem.add("Hardware Accelerated", 0);
		cmbPlottingSystem.add("Lightweight", 1);

		Group plotMulti1DGroup = new Group(comp, SWT.NONE);
		plotMulti1DGroup.setText("Plot 1DStack");
		plotMulti1DGroup.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		plotMulti1DGroup.setLayoutData(gd);

		Label lblCameraType = new Label(plotMulti1DGroup, SWT.LEFT);
		lblCameraType.setText("Camera projection: ");
		cmbCameraPerspective = new Combo(plotMulti1DGroup, SWT.RIGHT | SWT.READ_ONLY);
		cmbCameraPerspective.add("Orthographic");
		cmbCameraPerspective.add("Perspective");

		Group plot2DGroup = new Group(comp, SWT.NONE);
		plot2DGroup.setText("Plot 2D");
		plot2DGroup.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		plot2DGroup.setLayoutData(gd);

		Label lblColourMap = new Label(plot2DGroup, SWT.LEFT);
		lblColourMap.setText("Default colour mapping");
		cmbColourMap = new Combo(plot2DGroup, SWT.RIGHT | SWT.READ_ONLY);
		for (int i = 0; i < GlobalColourMaps.colourMapNames.length; i++)
			cmbColourMap.add(GlobalColourMaps.colourMapNames[i]);

		Label lblExpertMode = new Label(plot2DGroup, SWT.LEFT);
		lblExpertMode.setText("Colour map expert mode");
		chkExpertMode = new Button(plot2DGroup, SWT.CHECK | SWT.RIGHT);

		Label lblAutoHisto = new Label(plot2DGroup, SWT.LEFT);
		lblAutoHisto.setText("Auto contrast");
		chkAutoContrast = new Button(plot2DGroup, SWT.CHECK | SWT.RIGHT);

		Label lblLThreshold = new Label(plot2DGroup, SWT.LEFT);
		lblLThreshold.setText("Auto-contrast lower threshold (in %)");
		spnAutoLoThreshold = new Spinner(plot2DGroup, SWT.RIGHT);
		spnAutoLoThreshold.setMinimum(0);
		spnAutoLoThreshold.setMaximum(99);
		spnAutoLoThreshold.setIncrement(1);
		spnAutoLoThreshold.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int l = spnAutoLoThreshold.getSelection() + PreferenceConstants.MINIMUM_CONTRAST_DELTA;
				if (spnAutoHiThreshold.getSelection() < l)
					spnAutoHiThreshold.setSelection(l);
				spnAutoHiThreshold.setMinimum(l);
			}
		});

		Label lblHThreshold = new Label(plot2DGroup, SWT.LEFT);
		lblHThreshold.setText("Auto-contrast upper threshold (in %)");
		spnAutoHiThreshold = new Spinner(plot2DGroup, SWT.RIGHT);
		spnAutoHiThreshold.setMinimum(1);
		spnAutoHiThreshold.setMaximum(100);
		spnAutoHiThreshold.setIncrement(1);
		spnAutoHiThreshold.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int h = spnAutoHiThreshold.getSelection() - PreferenceConstants.MINIMUM_CONTRAST_DELTA;
				if (spnAutoLoThreshold.getSelection() > h)
					spnAutoLoThreshold.setSelection(h);
				spnAutoLoThreshold.setMaximum(h);
			}
		});

		Label lblScaling = new Label(plot2DGroup, SWT.LEFT);
		lblScaling.setText("Colour scaling");
		cmbColourScale = new Combo(plot2DGroup, SWT.RIGHT | SWT.READ_ONLY);
		cmbColourScale.add("Linear");
		cmbColourScale.add("Logarithmic");

		Label lblScrollbars = new Label(plot2DGroup, SWT.LEFT);
		lblScrollbars.setText("Show scrollbars");
		chkScrollbars = new Button(plot2DGroup, SWT.CHECK | SWT.RIGHT);
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

	/**
	 * Load the resolution value
	 */
	private void initializePage() {
		cmbColourMap.select(getColourMapChoicePreference());
		chkExpertMode.setSelection(getExpertModePreference());
		chkAutoContrast.setSelection(getAutoContrastPreference());
		spnAutoLoThreshold.setSelection(getAutoContrastLoPreference());
		spnAutoHiThreshold.setSelection(getAutoContrastHiPreference());
		cmbColourScale.select(getColourScaleChoicePreference());
		cmbCameraPerspective.select(getPerspectivePreference());
		chkScrollbars.setSelection(getScrollBarPreference());
		cmbPlottingSystem.select(getPlottingSystemPreference());
	}

	/**
	 * Load the default resolution value
	 */
	private void loadDefaultPreferences() {
		cmbColourMap.select(getDefaultColourMapChoicePreference());
		chkExpertMode.setSelection(getDefaultExpertModePreference());
		chkAutoContrast.setSelection(getDefaultAutoContrastPreference());
		spnAutoLoThreshold.setSelection(getDefaultAutoContrastLoPreference());
		spnAutoHiThreshold.setSelection(getDefaultAutoContrastHiPreference());
		cmbColourScale.select(getDefautColourScaleChoicePreference());
		cmbCameraPerspective.select(getDefaultPerspectivePreference());
		chkScrollbars.setSelection(getDefaultScrollBarPreference());
		cmbPlottingSystem.select(getDefaultPlottingSystemPreference());
	}

	/**
	 * Store the resolution value
	 */
	private void storePreferences() {
		setColourMapChoicePreference(cmbColourMap.getSelectionIndex());
		setExpertModePreference(chkExpertMode.getSelection());
		setAutoContrastPreference(chkAutoContrast.getSelection());
		setAutoContrastLoPreference(spnAutoLoThreshold.getSelection());
		setAutoContrastHiPreference(spnAutoHiThreshold.getSelection());
		setColourScaleChoicePreference(cmbColourScale.getSelectionIndex());
		setCameraPerspective(cmbCameraPerspective.getSelectionIndex());
		setScrollBarPreference(chkScrollbars.getSelection());
		setPlottingSystem(cmbPlottingSystem.getSelectionIndex());
	}

	private int getDefaultPerspectivePreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ);
	}

	private int getDefaultColourMapChoicePreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP);
	}

	private int getDefautColourScaleChoicePreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING);
	}

	private boolean getDefaultExpertModePreference() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT);
	}

	private boolean getDefaultAutoContrastPreference() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST);
	}

	private int getDefaultAutoContrastLoPreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD);
	}

	private int getDefaultAutoContrastHiPreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD);
	}

	private boolean getDefaultScrollBarPreference() {
		return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR);
	}

	private int getDefaultPlottingSystemPreference() {
		return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}

	private int getPerspectivePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ);
	}

	private int getColourMapChoicePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP);
	}

	private boolean getExpertModePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT);
	}

	private boolean getAutoContrastPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST);
	}

	private int getAutoContrastLoPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD);
	}

	private int getAutoContrastHiPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD);
	}

	private int getColourScaleChoicePreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING);
	}

	private boolean getScrollBarPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR)) {
			return getPreferenceStore().getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR);
		}
		return getPreferenceStore().getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR);
	}

	private int getPlottingSystemPreference() {
		if (getPreferenceStore().isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)) {
			return getPreferenceStore().getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
		}
		return getPreferenceStore().getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}

	private void setColourMapChoicePreference(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP, value);
	}

	private void setColourScaleChoicePreference(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING, value);
	}

	private void setExpertModePreference(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_CMAP_EXPERT, value);
	}

	private void setAutoContrastPreference(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST, value);
	}

	private void setAutoContrastLoPreference(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_LOTHRESHOLD, value);
	}

	private void setAutoContrastHiPreference(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST_HITHRESHOLD, value);
	}

	private void setCameraPerspective(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ, value);
	}

	private void setScrollBarPreference(boolean value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR, value);
	}

	private void setPlottingSystem(int value) {
		getPreferenceStore().setValue(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM, value);
	}
}
