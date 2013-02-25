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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

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

		showDate = new BooleanFieldEditor(PreferenceConstants.SHOW_DATE_COLUMN,"Show date column",getFieldEditorParent());
		addField(showDate);

		showType = new BooleanFieldEditor(PreferenceConstants.SHOW_TYPE_COLUMN,"Show type column",getFieldEditorParent());
		addField(showType);

		showSize = new BooleanFieldEditor(PreferenceConstants.SHOW_SIZE_COLUMN, "Show size column",getFieldEditorParent());
		addField(showSize);

		showComment = new BooleanFieldEditor(PreferenceConstants.SHOW_COMMENT_COLUMN, "Show comment column",getFieldEditorParent());
		addField(showComment);

		showScanCmd = new BooleanFieldEditor(PreferenceConstants.SHOW_SCANCMD_COLUMN, "Show scan command column",getFieldEditorParent());
		addField(showScanCmd);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
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
