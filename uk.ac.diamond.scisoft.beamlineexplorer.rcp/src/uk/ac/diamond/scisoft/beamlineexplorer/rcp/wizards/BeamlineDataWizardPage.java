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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.beamlineexplorer.rcp.icat.ICATDBClient;


public class BeamlineDataWizardPage extends WizardPage implements KeyListener {

	private static final String DEFAULT_BEAMLINE = "";
	private Text txtProject;
	private Text txtFedidValue;
	private Button btnCheckButton;
	private Combo beamlineListCombo;
	private Combo visitListCombo;
	private final String initProject;
	private final String initDirectory;
	private final String initFolder;
	
	// list of existing beamlines
	private final String[] beamlineList = {
			"", "b16", "b21", "b23", "i03", "i04-1",
			"i06", "i07", "i09-1", "i10-1", "i12",
			"i13-1", "i16", "i19", "i20-1", "i23",
			"p60", "b18", "b22", "i02", "i04",
			"i05", "i06-1", "i09", "i10", "i11", "i13",
			"i15", "i18", "i20", "i22", "i24", "p45", "p99"
	};
	
	List<VisitDetails> visitList = new ArrayList<VisitDetails>();
	
	private static Logger logger = LoggerFactory.getLogger(BeamlineDataWizardPage.class);

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
		this.initProject = prevProject != null ? prevProject : computeBeamline();
		this.initFolder = prevFolder != null ? prevFolder : computeDataFolder();
		this.initDirectory = prevDirectory != null ? prevDirectory : "";
		setTitle("Beamline Data Project Wizard - creates a link to a beamline data files");
		setDescription("Wizard to create a link to a set of beamline data files");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		
		// sort beamline list by name
		Arrays.sort(beamlineList);
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		String beamline = computeBeamline();
		Label lblBeamline = new Label(container, SWT.NULL);
		GridData gd_lblBeamline = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBeamline.widthHint = 123;
		gd_lblBeamline.heightHint = 26;
		lblBeamline.setLayoutData(gd_lblBeamline);
		lblBeamline.setText("Beamline");
				
		beamlineListCombo = new Combo(container, SWT.READ_ONLY);
		beamlineListCombo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(visitListCombo != null &&  visitListCombo.getSelectionIndex() != -1){
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
				}
			}
		});
		beamlineListCombo.setItems(beamlineList);
		beamlineListCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		beamlineListCombo.select(Arrays.asList(beamlineList).indexOf(beamline));
		new Label(container, SWT.NONE);
		
		Label lblFedid = new Label(container, SWT.NONE);
		lblFedid.setText("Detected Fedid");
		setControl(container);
		
		txtFedidValue = new Text(container, SWT.BORDER);
		txtFedidValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(visitListCombo != null &&  visitListCombo.getSelectionIndex() != -1){
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
				}
			}
		});
		txtFedidValue.setText(computeFedid());
		txtFedidValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		Label lblVisitList = new Label(container, SWT.NULL);
		lblVisitList.setText("Visit List");
		
		visitListCombo = new Combo(container, SWT.READ_ONLY);
		visitListCombo.addModifyListener(new ModifyListener() {
			// change beamline value when visit_id changes
			@Override
			public void modifyText(ModifyEvent e) {
				String beamline = "";
				for (int counter=0; counter< visitList.size(); counter++){
					VisitDetails currentVisit = visitList.get(counter);
			
				if (currentVisit.getVisit_id().equalsIgnoreCase(getVisit())){
					beamline = currentVisit.getInstrument();
					break;
				 }
				}
				
				int index = beamlineListCombo.indexOf(beamline);
				beamlineListCombo.select(index);
			}
		});
		visitListCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		
		Button btnVisitList = new Button(container, SWT.NONE);
		btnVisitList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				// getting the list of visits
				String fedid = getFedid(); 
				String beamline = getBeamline();
				logger.debug("getting list of visits for beamline: " + beamline +" and user: " + fedid);
				
				if(fedid.trim().equals("") && beamline.trim().equals("")){
					logger.error("fedid and beamline cannot be BOTH null");
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec
						    (new Runnable() {
						        public void run() {
						            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						            		.getShell(),"Error","Please specify at least either FEDID OR BEAMLINE. \nThey cannot be both blank.");
						            txtFedidValue.setText(computeFedid());
						        }
						    });
													
				}else {	
					
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
					
					computeVisitList(getFedid(), getBeamline());
					
					String[] items = new String[visitList.size()];
					// populate list of VisitDetails
					for (int i=0; i< visitList.size(); i++){
						items[i] = visitList.get(i).getVisit_id().toLowerCase();
					}
					
					visitListCombo.setItems(items);
					if (visitListCombo.getItemCount() > 1)
						visitListCombo.select(0);
				}
			}
		});
		btnVisitList.setText("get visits");
		
		btnCheckButton = new Button(container, SWT.CHECK);
		btnCheckButton.setSelection(true);
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
		btnCheckButton.setText("Show files only");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		//
		dialogChanged() ;
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
	
		if (getBeamline().length() == 0 && getFedid().length() == 0) {
			updateStatus("Either fedid and/or beamline must specified.");
			return;
		}
		
		if (getBeamline().length() == 0 && getFedid().length() == 0) {
			updateStatus("Either fedid and/or beamline must specified.");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProject() {
		
 	    String beamline = null;
		for (int counter=0; counter< visitList.size(); counter++){
			VisitDetails currentVisit = visitList.get(counter);
	
		if (currentVisit.getVisit_id().equalsIgnoreCase(getVisit())){
			beamline = currentVisit.getInstrument();
			break;
		 }
		}
		// return the name of the created project
		return beamline + "-" + getVisit();
	}

	public String getDirectory() {
		// get following info from results not GUI as it can be blank
		String year = "";
 	    String beamline = "";
		for (int counter=0; counter< visitList.size(); counter++){
			VisitDetails currentVisit = visitList.get(counter);
	
		if (currentVisit.getVisit_id().equalsIgnoreCase(getVisit())){
			year = currentVisit.getStart_date().substring(0,4);
			beamline = currentVisit.getInstrument();
			logger.debug("retrieved year: " + year + " beamline: "+ beamline);
			break;
		 }
		}
		
		String directoryPath = "/dls/"+beamline+"/data/"+ year + "/" + getVisit();	
		return 	directoryPath; 
	}
	
	public String getFedid() {
		return txtFedidValue.getText();
	}

	public String getBeamline() {
		return beamlineListCombo.getText().toLowerCase();	
	}	
	
	public String getVisit() {
		return visitListCombo.getText().toLowerCase();	
	}


	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(txtProject)) {
			dialogChanged();
		}
		if (e.getSource().equals(txtFedidValue)) {
			dialogChanged();
		}
		if (e.getSource().equals(beamlineListCombo)) {
			dialogChanged();
		}

	}
	
	private String computeBeamline(){
		String hostname = "";
		String beamline = DEFAULT_BEAMLINE;

		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// display error message
			logger.error("Error getting hostname! " + e.getMessage());
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec
		    (new Runnable() {
		        public void run() {
		            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		            		.getShell(),"Error","Error getting hostname.\n");
		        }
		    });
		}
		
		logger.info("hostname: "+ hostname);

		int index = hostname.lastIndexOf("-");	

		if(index != -1 ){
			String tmpbeamline= hostname.substring(0, index);

			if (Arrays.asList(beamlineList).contains( tmpbeamline)){
				logger.info("hostname: "+ hostname + " beamline: "+ tmpbeamline);
				beamline =  tmpbeamline;
			}else{
				logger.error("hostname: "+ hostname + " is not a beamline");
			}
		}

		return beamline;
	}

	private String computeFedid(){
		return System.getProperty("user.name");
	}
	

	private String computeDataFolder() {
		return "/dls/"+computeBeamline() + "/data/";
	}
	
	private List<VisitDetails> computeVisitList(String fedid, String beamline) {	
		
		//clear visit list 
		visitList.clear();
		
		int rowCount = 0;
		int columnsCount = 0;
		List<String> columnNames = new ArrayList<String>();
		List<String> resultRows = new ArrayList<String>();
		
		String fedidsql = "";
		String beamlineSql = "";
		
		if ( !fedid.trim().equals("")){
			logger.debug("fedid-" + fedid +"-");
			fedidsql = " and federal_id='"+ fedid + "'";
		}
		if ( !beamline.trim().equals("")){
			logger.debug("beamline-" + beamline +"-");
			beamlineSql = " and instrument ='"+ beamline + "'";
		}
		
		// build sql query
		String sqlStatement = "select investigation.visit_id, investigation.instrument, investigation.inv_start_date, investigator.facility_user_id, federal_id from investigation, investigator, facility_user" +
				" where investigation.id = investigator.investigation_id and investigator.facility_user_id = facility_user.facility_user_id " + fedidsql + beamlineSql + " order by investigation.inv_start_date desc";
								
		   // getting the connection to the database
			Connection conn =  ICATDBClient.getConnection();
			if (conn != null) {
		    				try {

		    					// run the final query
		    					logger.info("\nsqlStatement: " + sqlStatement);
		    					Statement st = conn.createStatement();
		    					ResultSet rs = st.executeQuery(sqlStatement);

		    					ResultSetMetaData rsmd = rs.getMetaData();
		    					columnsCount = rsmd.getColumnCount();

		    					//logger.debug("\nNumber of Columns=" + columnsCount);

		    					// Get the column names; column indices start from 1
		    					for (int i = 1; i < columnsCount + 1; i++) {

		    						String columnName = rsmd.getColumnName(i);
		    						columnNames.add(columnName);
		    					}

		    					if (rs.next()) {
		    						// there is at least one record
		    						do {
		    							rowCount = rs.getRow();
		    							// populate rows with result set
		    							String resultRow = "";
		    							for (int i = 1; i <= columnsCount; i++) {
		    								resultRow = rs.getString(i) + "#SEP#" + resultRow;
		    							}
		    							resultRows.add(resultRow);
	    								// populate the result array
		    							String delims = "#SEP#";
		    							String[] tokens = resultRow.split(delims);
	    								visitList.add(new VisitDetails(tokens[0], tokens[4], tokens[3], tokens[2]));
		    							logger.info(resultRow);

		    						} while (rs.next());
		    					} else {
		    						// no record found
		    						String faultMessage = "no record found "
		    								+ this.getClass().getName() + "#computeVisitList";
		    						
		    						logger.error(faultMessage);
		    					}
		    					// rs.last();

		    					logger.info("Number of Rows= " + rowCount);
		    				
		    					// closing result set and statement
		    					rs.close();
		    					st.close();

		    				} catch (java.sql.SQLException e) {
		    					// catch any unexpected exception
		    					String faultMessage = "problem with sql query for db name: "
		    							+ ", and user:  "
		    							+ fedid
		    							+ " "
		    							+ this.getClass().getName()
		    							+ "#computeVisitList"
		    							+ e.getMessage();

		    					logger.error(faultMessage);
		    					// throw new BadDataExceptionException(faultMessage);
		    				}

		    			}
			return visitList;				
	}
}
