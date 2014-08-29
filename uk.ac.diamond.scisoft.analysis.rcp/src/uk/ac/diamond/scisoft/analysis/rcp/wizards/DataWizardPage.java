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

package uk.ac.diamond.scisoft.analysis.rcp.wizards;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.dawnsci.common.widgets.file.SelectorWidget;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one (png).
 */

public class DataWizardPage extends WizardPage implements KeyListener {

	private Text txtProject;
	private Text txtFolder;
	private final String initProject;
	private final String initDirectory;
	private final String initFolder;
	private SelectorWidget directorySelector;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param prevDirectory
	 * @param prevFolder
	 * @param prevProject
	 */
	public DataWizardPage(@SuppressWarnings("unused") ISelection selection, String prevProject,
			String prevFolder, String prevDirectory) {
		super("DataWizardPage");
		this.initProject = prevProject != null ? prevProject : "Data";
		this.initFolder = prevFolder != null ? prevFolder : "data";
		this.initDirectory = prevDirectory != null ? prevDirectory : "";
		setTitle("Data Project Wizard - creates a link to a directory of data files");
		setDescription("Wizard to create a link to a set of data files");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label lblProjectName = new Label(container, SWT.NULL);
		lblProjectName.setText("&Project:");
		txtProject = new Text(container, SWT.BORDER);
		txtProject.setText(initProject);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtProject.setLayoutData(gd);
		txtProject.addKeyListener(this);
		new Composite(container, SWT.NULL);

		Label lblFolderName = new Label(container, SWT.NULL);
		lblFolderName.setText("&Folder:");
		txtFolder = new Text(container, SWT.BORDER);
		txtFolder.setText(initFolder);
		txtFolder.setEditable(true);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtFolder.setLayoutData(gd);
		txtFolder.addKeyListener(this);
		new Composite(container, SWT.NULL);

		Label label = new Label(container, SWT.NULL);
		label.setText("&Directory:");
		directorySelector = new SelectorWidget(container, true, false) {
			@Override
			public void pathChanged(String path, TypedEvent event) {
				dialogChanged();
			}
		};
		directorySelector.setLabel("");
		directorySelector.setText(initDirectory);

		new Composite(container, SWT.NULL);
		dialogChanged();
		setControl(container);
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		if (getProject().length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}

		if (getFolder().length() == 0) {
			updateStatus("Folder name must be specified. e.g. data");
			return;
		}

		if (getDirectory().length() == 0) {
			updateStatus("Directory containing files must be specified.");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProject() {
		return txtProject.getText();
	}

	public String getDirectory() {
		return directorySelector.getText();
	}
	
	public String getFolder() {
		return txtFolder.getText();
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(txtProject)) {
			dialogChanged();
		}
		if (e.getSource().equals(txtFolder)) {
			dialogChanged();
		}
	}

}
