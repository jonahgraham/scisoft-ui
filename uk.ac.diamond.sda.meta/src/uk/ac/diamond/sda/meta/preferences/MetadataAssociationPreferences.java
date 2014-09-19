/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.preferences;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.UIJob;

import uk.ac.diamond.sda.meta.Activator;
import uk.ac.diamond.sda.meta.utils.MapUtils;

public class MetadataAssociationPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	private List fileAssociations;
	private Text txtFileType;
	private Text txtFileAssociation;
	private Map<String, String> mapOfFileAssociations;
	private String defaultView;
	private Text defaultType;

	public MetadataAssociationPreferences() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		getCurrentSetting();
	}

	private void getCurrentSetting() {
		mapOfFileAssociations = MapUtils.getMap(getPreferenceStore().getString(
				PreferenceConstants.defaultMetadataAssociation));
		defaultView = getPreferenceStore().getString(PreferenceConstants.defaultPage);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));

		Group gpdefaultType = new Group(comp, SWT.NONE);
		gpdefaultType.setText("Default Metadata Page");
		gpdefaultType.setLayout(new GridLayout(2, true));

		new Label(gpdefaultType, SWT.NONE).setText("The default viewer is ");
		defaultType = new Text(gpdefaultType, SWT.READ_ONLY);
		defaultType.setBackground(gpdefaultType.getBackground());

		Group gpfileAssociationTable = new Group(comp, SWT.NONE);
		gpfileAssociationTable.setText("File Associations");
		gpfileAssociationTable.setLayout(new GridLayout(1, false));
		gpfileAssociationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridData data = new GridData(GridData.FILL_BOTH);
		fileAssociations = new List(gpfileAssociationTable, SWT.BORDER);
		fileAssociations.setLayoutData(data);
		fileAssociations.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				updateAssociationDetails(fileAssociations.getSelection());
			}
		});

		Group gpFileAssociationDetails = new Group(comp, SWT.NONE);
		gpFileAssociationDetails.setText("Association Details");
		gpFileAssociationDetails.setLayout(new GridLayout(2, false));
		gpFileAssociationDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		new Label(gpFileAssociationDetails, SWT.NONE).setText("File: ");
		txtFileType = new Text(gpFileAssociationDetails, SWT.READ_ONLY);
		txtFileType.setBackground(gpFileAssociationDetails.getBackground());
		txtFileType.setLayoutData(data);

		new Label(gpFileAssociationDetails, SWT.NONE).setText("Association: ");
		txtFileAssociation = new Text(gpFileAssociationDetails, SWT.NONE);
		txtFileAssociation.setBackground(gpFileAssociationDetails.getBackground());
		txtFileAssociation.setLayoutData(data);

		updateFileAssociationList();

		return comp;
	}

	protected void updateAssociationDetails(final String[] selection) {
		UIJob updateDetails = new UIJob("Updata details") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// there should only be a single selection.
				Map<String, String> map = mapOfFileAssociations;
				txtFileType.setText(selection[0]);
				txtFileAssociation.setText(map.get(selection[0]));
				return Status.OK_STATUS;
			}
		};
		updateDetails.schedule();
	}

	private void updateFileAssociationList() {
		final Set<String> set = mapOfFileAssociations.keySet();
		UIJob updateLists = new UIJob("Update the file lists") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fileAssociations.removeAll();
				for (String string : set) {
					fileAssociations.add(string);
				}
				defaultType.setText(defaultView);
				return Status.OK_STATUS;
			}
		};
		updateLists.schedule();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		mapOfFileAssociations = MapUtils.getMap(getPreferenceStore().getDefaultString(
				PreferenceConstants.defaultMetadataAssociation));
		defaultView = getPreferenceStore().getDefaultString(PreferenceConstants.defaultPage);
		updateFileAssociationList();

		// setDefaults
		getPreferenceStore().setValue(PreferenceConstants.defaultMetadataAssociation,
				MapUtils.getString(mapOfFileAssociations));
		getPreferenceStore().setValue(PreferenceConstants.defaultPage, defaultView);
	}

}
