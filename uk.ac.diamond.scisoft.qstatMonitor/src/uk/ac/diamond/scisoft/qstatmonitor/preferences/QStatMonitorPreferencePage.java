/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.qstatmonitor.Activator;
import uk.ac.diamond.scisoft.qstatmonitor.views.QStatMonitorView;

public class QStatMonitorPreferencePage extends FieldEditorPreferencePage
		implements
			IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.diamond.scisoft.qstatmonitor.preferences.QStatMonitorPreferencePage";

	private Combo queryDropDown;
	//private StringFieldEditor sleepSecondsField;
	private StringFieldEditor queryField;
	private StringFieldEditor userField;
	private BooleanFieldEditor disableAutoRefresh;
	private BooleanFieldEditor disableAutoPlot;
	
	private Spinner spnRefreshInterval;

	public QStatMonitorPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {		
		Label lblRefreshInterval = new Label(getFieldEditorParent(), SWT.READ_ONLY);
		lblRefreshInterval.setText("Refresh interval (seconds)");
		
		spnRefreshInterval = new Spinner(getFieldEditorParent(), SWT.READ_ONLY | SWT.BORDER);
		spnRefreshInterval.setDigits(1);
		spnRefreshInterval.setIncrement(5);
		spnRefreshInterval.setMinimum(20);
		spnRefreshInterval.setMaximum(Integer.MAX_VALUE);
		spnRefreshInterval.setSelection((int) (QStatMonitorPreferenceConstants.DEF_SLEEP * 10));
		
		//TODO: Temp fix to use preference store, better to set in performOk()
		spnRefreshInterval.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				store.setValue(QStatMonitorPreferenceConstants.P_SLEEP, spnRefreshInterval.getText());
			}
		});
		
		/*
		sleepSecondsField = new StringFieldEditor(
				QStatMonitorPreferenceConstants.P_SLEEP,
				"Seconds between refresh", getFieldEditorParent());
		addField(sleepSecondsField);
		*/

		disableAutoRefresh = new BooleanFieldEditor(
				QStatMonitorPreferenceConstants.P_REFRESH,
				"Disable automatic refreshing", getFieldEditorParent());
		addField(disableAutoRefresh);

		disableAutoPlot = new BooleanFieldEditor(
				QStatMonitorPreferenceConstants.P_PLOT,
				"Disable automatic plotting", getFieldEditorParent());
		addField(disableAutoPlot);

		Label axisLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		axisLabel.setText("Example queries");
		queryDropDown = new Combo(getFieldEditorParent(), SWT.DROP_DOWN
				| SWT.WRAP | SWT.READ_ONLY);
		queryDropDown.add("My jobs", 0);
		queryDropDown.add("My jobs on tesla", 1);
		queryDropDown.add("My jobs on tesla64", 2);
		queryDropDown.add("All jobs", 3);
		queryDropDown.add("All jobs on tesla", 4);
		queryDropDown.add("All jobs on tesla64", 5);
		queryDropDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				queryField
						.setStringValue(QStatMonitorPreferenceConstants.LIST_OF_QUERIES[queryDropDown
								.getSelectionIndex()]);
				if (queryDropDown.getSelectionIndex() > 2) {
					userField.setStringValue("*");
				} else {
					userField.setStringValue("");
				}
			}
		});

		queryField = new StringFieldEditor(
				QStatMonitorPreferenceConstants.P_QUERY, "Query",
				getFieldEditorParent());
		addField(queryField);
		userField = new StringFieldEditor(
				QStatMonitorPreferenceConstants.P_USER,
				"Show tasks by this user", getFieldEditorParent());
		addField(userField);
	}
	// TODO: Replace with PropertyChangeListener, use PreferenceStore
	@Override
	public boolean performOk() {
		super.performOk();
		// update qstat view variables for new query and sleep time
		QStatMonitorView view = (QStatMonitorView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.findView(QStatMonitorView.ID);
		if (view != null) { // then view is open
			view.setSleepTimeSecs(Double.parseDouble(spnRefreshInterval.getText()));
			
			/*
			if (sleepSecondsField.getStringValue() != null
					&& sleepSecondsField.getStringValue() != ""
					&& !sleepSecondsField.getStringValue().isEmpty()) {
				view.setSleepTimeSecs(Double.parseDouble(sleepSecondsField
						.getStringValue()));
			}
			*/
			
			view.setQuery(queryField.getStringValue());
			view.setUserArg(userField.getStringValue());
			
			view.setAutomaticRefresh(!disableAutoRefresh.getBooleanValue());
			view.updateTable();

			view.setPlotOption(!disableAutoPlot.getBooleanValue());
			view.resetPlot();
		}
		return true;
	}

}
