/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.ac.diamond.scisoft.qstatmonitor.views.QStatMonitorView;

public class QStatMonitorPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.diamond.qstatMonitor.QStatMonitorPreferencePage";

	private Combo queryDropDown;
	private StringFieldEditor sleepSecondsField;
	private StringFieldEditor queryField;
	private StringFieldEditor userField;
	private BooleanFieldEditor disableAutoRefresh;
	private BooleanFieldEditor disableAutoPlot;

	private final String[] listOfQueries = { "qstat", "qstat -l tesla",
			"qstat -l tesla64", "qstat", "qstat -l tesla", "qstat -l tesla64" };

	public QStatMonitorPreferencePage() {
		super(GRID);
		final IPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.qstatMonitor");
		setPreferenceStore(store);
		setDescription("Preferences for QStat Monitor.");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {

		sleepSecondsField = new StringFieldEditor(QStatMonitorConstants.SLEEP,
				"Seconds between refresh", getFieldEditorParent());
		addField(sleepSecondsField);

		disableAutoRefresh = new BooleanFieldEditor(QStatMonitorConstants.DISABLE_AUTO_REFRESH,
				"Disable automatic refreshing", getFieldEditorParent());
		addField(disableAutoRefresh);

		disableAutoPlot = new BooleanFieldEditor(QStatMonitorConstants.DISABLE_AUTO_PLOT,
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
				queryField.setStringValue(listOfQueries[queryDropDown
						.getSelectionIndex()]);
				if (queryDropDown.getSelectionIndex() > 2) {
					userField.setStringValue("*");
				} else {
					userField.setStringValue("");
				}
			}
		});

		queryField = new StringFieldEditor(QStatMonitorConstants.QUERY, "Query",
				getFieldEditorParent());
		addField(queryField);
		userField = new StringFieldEditor(QStatMonitorConstants.USER, "Show tasks by this user",
				getFieldEditorParent());
		addField(userField);
	}

	@Override
	public boolean performOk() {
		super.performOk();
		// update qstat view variables for new query and sleep time
		QStatMonitorView view = (QStatMonitorView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.findView(QStatMonitorView.ID);
		if (view != null) { // then view is open
			if (sleepSecondsField.getStringValue() != null
					&& sleepSecondsField.getStringValue() != ""
					&& !sleepSecondsField.getStringValue().isEmpty()) {
				view.setSleepTimeSecs(Double.parseDouble(sleepSecondsField
						.getStringValue()));
			}
			view.setQuery(queryField.getStringValue());
			view.setUserArg(userField.getStringValue());
			if (disableAutoRefresh.getBooleanValue()) {
				view.stopRefreshing();
			} else {
				view.updateTable();
				view.startRefreshing();
			}

			view.setPlotOption(!disableAutoPlot.getBooleanValue());
			view.resetPlot();

		}
		return true;
	}

}
