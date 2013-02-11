/*
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.wizards;

import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class BeamlineDataWizardPage extends WizardPage implements KeyListener {

	private static final String DEFAULT_HOSTNAME = "i13-1-control.diamond.ac.uk";
	private Text txtDirectory;
	private Text txtProject;
	private Button btnCheckButton;
	private final String initProject;
	private final String initDirectory;
	private final String initFolder;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param prevDirectory
	 * @param prevFolder
	 * @param prevProject
	 */
	public BeamlineDataWizardPage(ISelection selection, String prevProject,
			String prevFolder, String prevDirectory) {
		super("BeamlineDataWizardPage");
		this.initProject = prevProject != null ? prevProject : getBeamline();
		this.initFolder = prevFolder != null ? prevFolder : getDataFolder();
		this.initDirectory = prevDirectory != null ? prevDirectory : "";
		setTitle("Beamline Data Project Wizard - creates a link to a beamline data files");
		setDescription("Wizard to create a link to a set of beamline data files");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label lblProjectName = new Label(container, SWT.NULL);
		lblProjectName.setText("&Beamline:");
		txtProject = new Text(container, SWT.BORDER);
		//txtProject.setText(initProject);
		txtProject.setText(getBeamline());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtProject.setLayoutData(gd);
		txtProject.addKeyListener(this);
				new Label(container, SWT.NONE);
		
				Label lbldataRootDirectory = new Label(container, SWT.NULL);
				lbldataRootDirectory.setText("&Data Root Directory:");
		txtDirectory = new Text(container, SWT.BORDER);
		//txtDirectory.setText(initDirectory);
		txtDirectory.setText(getDataFolder());
		txtDirectory.setEditable(true);
		txtDirectory.setEnabled(true);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtDirectory.setLayoutData(gd);
		Composite composite = new Composite(container, SWT.NULL);
		
				Button button = new Button(composite, SWT.PUSH);
				button.setBounds(0, 10, 71, 29);
				button.setText("Browse...");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						handleBrowse();
					}
				});
		dialogChanged();
		setControl(container);
		
		Label lblFedid = new Label(container, SWT.NONE);
		lblFedid.setText("D&etected Fedid:");
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setText(getFedid());
		new Label(container, SWT.NONE);
		
		btnCheckButton = new Button(container, SWT.CHECK);
		btnCheckButton.setEnabled(false);
		btnCheckButton.setGrayed(true);
		btnCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// set global variable to the check box selection
				BeamlineDataWizard.RECURSIVE_BROWSING = btnCheckButton.getSelection();
			}
		});
		btnCheckButton.setText("Recursive browsing");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for the container field.
	 */

	private void handleBrowse() {
		DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.OPEN);
		dirDialog.setFilterPath(getDirectory());
		final String filepath = dirDialog.open();
		if (filepath != null) {
			txtDirectory.setText(filepath);
			dialogChanged();
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		if (getProject().length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}

//		if (getFolder().length() == 0) {
//			updateStatus("Folder name must be specified. e.g. data");
//			return;
//		}

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
		return txtDirectory.getText();
	}
	

	public void setDataLocation(String selectedPath) {
		txtDirectory.setText(selectedPath);
	}

//	public String getFolder() {
//		return txtFolder.getText();
//	}

	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(txtProject)) {
			dialogChanged();
		}
//		if (e.getSource().equals(txtFolder)) {
//			dialogChanged();
//		}

	}
	
	// TODO get beamline name from hostname - linux
	private String getBeamline(){
		String hostname = null;
		String beamline = null;
		
		try {
			 hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// display error message
			System.out.println("Error getting hostname! " + e.getMessage());
		};
		
		if (hostname.contains("-control")){
			System.out.println("current hostname is beamline control machine: " + beamline);
		}else{
			System.out.println("hostname is NOT a beamline: " + hostname);
			// TODO to modify after testing
			hostname = DEFAULT_HOSTNAME;
		}
		
		int index = hostname.indexOf("-control");
		beamline = hostname.substring(0,index);
		
		System.out.println("beamline: " + beamline);
		
		return beamline;		
	}
	
	// TODO get current fedid from current linux session
	private String getFedid(){
		return System.getProperty("user.name");
	}
	

	private String getDataFolder() {
		
		return "/dls/"+getBeamline() + "/data/";
	}
}
