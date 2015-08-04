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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.qstatmonitor.Activator;

public class QStatMonitorPreferencePage extends FieldEditorPreferencePage
		implements
			IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.diamond.scisoft.qstatmonitor.preferences.QStatMonitorPreferencePage";

	private Spinner spnRefreshInterval;
	private StringFieldEditor queryField;
	private StringFieldEditor userField;

	public QStatMonitorPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();

		(new Label(getFieldEditorParent(), SWT.READ_ONLY))
				.setText("Refresh interval (seconds)");

		spnRefreshInterval = new Spinner(getFieldEditorParent(), SWT.READ_ONLY
				| SWT.BORDER);
		spnRefreshInterval.setDigits(1);
		spnRefreshInterval.setIncrement(5);
		spnRefreshInterval.setMinimum(20);
		spnRefreshInterval.setMaximum(Integer.MAX_VALUE);
		spnRefreshInterval.setSelection((int) (Double.parseDouble(preferences
				.getString(QStatMonitorPreferenceConstants.P_SLEEP)) * 10));

		addField(new BooleanFieldEditor(QStatMonitorPreferenceConstants.P_REFRESH,
				"Disable automatic refreshing", getFieldEditorParent()));

		(new Label(getFieldEditorParent(), SWT.WRAP)).setText("Example queries");

		final Combo queryDropDown = new Combo(getFieldEditorParent(), SWT.DROP_DOWN
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

		queryField = new StringFieldEditor(QStatMonitorPreferenceConstants.P_QUERY,
				"Query", getFieldEditorParent());
		addField(queryField);

		userField = new StringFieldEditor(QStatMonitorPreferenceConstants.P_USER,
				"Show tasks by this user", getFieldEditorParent());
		addField(userField);
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(QStatMonitorPreferenceConstants.P_SLEEP,
				Float.parseFloat(spnRefreshInterval.getText()));

		return super.performOk();
	}

}
