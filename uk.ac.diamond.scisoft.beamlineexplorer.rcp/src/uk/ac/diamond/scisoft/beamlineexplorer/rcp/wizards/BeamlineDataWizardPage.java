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
import java.util.Arrays;

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
import org.eclipse.swt.widgets.Combo;


public class BeamlineDataWizardPage extends WizardPage implements KeyListener {

	private static final String DEFAULT_HOSTNAME = "i16-control.diamond.ac.uk";
	private Text txtDataRootDirectory;
	private Text txtProject;
	private Text txtProjectName;
	private Button btnCheckButton;
	private Combo beamlineListCombo;
	private final String initProject;
	private final String initDirectory;
	private final String initFolder;
	
	// list of existing beamlines
	private final String[] beamlineList = {
			"b16", "b21", "b23", "i03", "i04-1",
			"i06", "i07", "i09-1", "i10-1", "i12",
			"i13-1", "i16", "i19", "i20-1", "i23",
			"p60", "b18", "b22", "i02", "i04",
			"i05", "i06-1", "i09", "i10", "i11", "i13",
			"i15", "i18", "i20", "i22", "i24", "p45", "p99"
	};

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
		
		Label lblProjectName = new Label(container, SWT.NONE);
		lblProjectName.setText("&Project Name:");
		String beamline = getBeamline();
		
		txtProjectName = new Text(container, SWT.BORDER);
		txtProjectName.setText(getBeamline() + "-project");
		GridData gd_txtProjectName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtProjectName.widthHint = 328;
		txtProjectName.setLayoutData(gd_txtProjectName);
		new Label(container, SWT.NONE);
		Label lblBeamline = new Label(container, SWT.NULL);
		GridData gd_lblBeamline = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBeamline.widthHint = 123;
		gd_lblBeamline.heightHint = 26;
		lblBeamline.setLayoutData(gd_lblBeamline);
		lblBeamline.setText("&Beamline:");
		
		beamlineListCombo = new Combo(container, SWT.NONE);
		beamlineListCombo.setItems(beamlineList);
		beamlineListCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		beamlineListCombo.select(Arrays.asList(beamlineList).indexOf(beamline));
		setControl(container);
		new Label(container, SWT.NONE);
		
		Label lblDataRootDirectory = new Label(container, SWT.NULL);
		lblDataRootDirectory.setText("&Data Root Directory:");
		txtDataRootDirectory = new Text(container, SWT.BORDER);
		GridData gd_txtDataRootDirectory = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtDataRootDirectory.widthHint = 343;
		txtDataRootDirectory.setLayoutData(gd_txtDataRootDirectory);
		txtDataRootDirectory.setText(getDataFolder());
		txtDataRootDirectory.setEditable(true);
		txtDataRootDirectory.setEnabled(true);
		dialogChanged();
		
				Button button = new Button(container, SWT.PUSH);
				button.setText("Browse...");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						handleBrowse();
					}
				});
		
		Label lblFedid = new Label(container, SWT.NONE);
		lblFedid.setText("D&etected Fedid:");
		
		Label lblFedidValue = new Label(container, SWT.NONE);
		lblFedidValue.setText(getFedid());
		new Label(container, SWT.NONE);
		
		btnCheckButton = new Button(container, SWT.CHECK);
		GridData gd_btnCheckButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCheckButton.heightHint = 45;
		btnCheckButton.setLayoutData(gd_btnCheckButton);
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
			txtDataRootDirectory.setText(filepath);
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
		return txtProjectName.getText();
	}

	public String getDirectory() {
		return txtDataRootDirectory.getText();
	}
	

	public void setDataLocation(String selectedPath) {
		txtDataRootDirectory.setText(selectedPath);
	}


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
