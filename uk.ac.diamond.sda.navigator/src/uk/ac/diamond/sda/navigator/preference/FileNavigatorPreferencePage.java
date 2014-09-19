/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;

public class FileNavigatorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor showDate;
	private BooleanFieldEditor showType;
	private BooleanFieldEditor showSize;
	private BooleanFieldEditor showScanCmd;
	private BooleanFieldEditor showComment;

	public FileNavigatorPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {

		showDate = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_DATE_COLUMN,"Show date column",getFieldEditorParent());
		addField(showDate);

		showType = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_TYPE_COLUMN,"Show type column",getFieldEditorParent());
		addField(showType);

		showSize = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_SIZE_COLUMN, "Show size column",getFieldEditorParent());
		addField(showSize);

		showComment = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN, "Show comment column",getFieldEditorParent());
		addField(showComment);

		showScanCmd = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN, "Show scan command column",getFieldEditorParent());
		addField(showScanCmd);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = NavigatorRCPActivator.getDefault().getPreferenceStore();

		setPreferenceStore(store);
		setDescription("Preferences for viewing a file system using the File Navigator:");
	}

	/**
	 * Adjust the layout of the field editors so that
	 * they are properly aligned.
	 */
	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
	}

	@Override
	protected void checkState() {
		super.checkState();
		setErrorMessage(null);
		setValid(true);
	}
}
