/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.wizards;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.beamlineexplorer.rcp.icat.ICATDBClient;

public class BeamlineDataWizardPage extends WizardPage implements KeyListener {

	private static final String DEFAULT_BEAMLINE = "";
	private static final String DELIMITER = " - ";
	private static final String DEFAULT_PROJECT_NAME = "$Beamline-$VisitID";
	private static final String DEFAULT_LINK_NAME = "beamlineData";
	private String START_DATE;
	private String END_DATE;
	private Text txtFedidValue;
	private Button btnCheckButton;
	private Combo beamlineListCombo;
	private Combo visitListCombo;

	private ScrolledComposite sc;
	private Text txtProjectname;
	private Text txtLinkname;
	private Label lblDefaultProjectname;
	private DateTime dateFrom;
	private DateTime dateTo;
	private ExpansionAdapter expansionAdapter;
	private ExpandableComposite advancedOptionsExpander;

	// list of existing beamlines
	private final String[] beamlineList = { "", "b16", "b21", "b23", "i03", "i04-1", "i06", "i07", "i09-1", "i10-1",
			"i12", "i13-1", "i16", "i19", "i20-1", "i23", "p60", "b18", "b22", "i02", "i04", "i05", "i06-1", "i09",
			"i10", "i11", "i13", "i15", "i18", "i20", "i22", "i24", "p45", "p99" };

	List<VisitDetails> visitList = new ArrayList<VisitDetails>();

	private static Logger logger = LoggerFactory.getLogger(BeamlineDataWizardPage.class);

	public BeamlineDataWizardPage(ISelection selection, String prevProject, String prevFolder, String prevDirectory) {
		super("BeamlineDataWizardPage");
//		this.initProject = prevProject != null ? prevProject : computeBeamline();
//		this.initFolder = prevFolder != null ? prevFolder : computeDataFolder();
//		this.initDirectory = prevDirectory != null ? prevDirectory : "";
		setTitle("Beamline Data Project Wizard - creates a link to a beamline data files");
		setDescription("Wizard to create a link to a set of beamline data files");

		if(isWindows()){
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
			.asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog
					.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Error",
							"Beamline projects cannot be created in Windows operating system. \nThey can be created on Diamond Light Source Linux OS only.");
					
				}
			});//end show dialog
		}
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		/*
		 * sort beamline list by name
		 */
		Arrays.sort(beamlineList);
		String beamline = computeBeamline();

		// Set up the composite to hold all the information
		sc = new ScrolledComposite(parent, SWT.BORDER);
		sc.setAlwaysShowScrollBars(true);
		sc.setLayout(new FillLayout());

		final Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// set-up the standard GUI elements
		Label lblBeamline = new Label(composite, SWT.NULL);
		lblBeamline.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblBeamline.setText("Beamline:");
		GridData gd_lblBeamline = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBeamline.widthHint = 72;
		lblBeamline.setLayoutData(gd_lblBeamline);

		beamlineListCombo = new Combo(composite, SWT.READ_ONLY);
		beamlineListCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// update project name
				String projectNameContent = "";
				try {
					beamlineListCombo.getText();
					projectNameContent = beamlineListCombo.getText();
				}catch(NullPointerException ex){}
				
				try{
					visitListCombo.getText();
					String[] splits = visitListCombo.getText().split(" - ");
					projectNameContent = projectNameContent + "-" + splits[0];
				}catch(NullPointerException ex){}
				
				// update default project name
				updateDefProjectName();
							
			}
		});
		GridData gd_beamlineListCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_beamlineListCombo.widthHint = 225;
		beamlineListCombo.setLayoutData(gd_beamlineListCombo);
		beamlineListCombo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (visitListCombo != null && visitListCombo.getSelectionIndex() != -1) {
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
				}
				dialogChanged();
				
				// update project name
				String projectNameContent = "";
				try {
					beamlineListCombo.getText();
					projectNameContent = beamlineListCombo.getText();
				}catch(NullPointerException ex){}
				
				try{
					visitListCombo.getText();
					String[] splits = visitListCombo.getText().split(" - ");
					projectNameContent = projectNameContent + "-" + splits[0];
				}catch(NullPointerException ex){}
				
				// update default project name
				updateDefProjectName();
			}
		});
		beamlineListCombo.setItems(beamlineList);
		beamlineListCombo.select(Arrays.asList(beamlineList).indexOf(beamline));
		new Label(composite, SWT.NONE);

		Label lblFedid = new Label(composite, SWT.NONE);
		lblFedid.setText("Detected Fedid:");

		txtFedidValue = new Text(composite, SWT.BORDER);
		GridData gd_txtFedidValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtFedidValue.widthHint = 217;
		txtFedidValue.setLayoutData(gd_txtFedidValue);
		txtFedidValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (visitListCombo != null && visitListCombo.getSelectionIndex() != -1) {
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
					visitListCombo.removeAll();
				}
				
				dialogChanged();
			}
		});
		txtFedidValue.setText(computeFedid());
		new Label(composite, SWT.NONE);

		Label lblVisitList = new Label(composite, SWT.NULL);
		GridData gd_lblVisitList = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblVisitList.widthHint = 63;
		lblVisitList.setLayoutData(gd_lblVisitList);
		lblVisitList.setText("Visit List:");

		visitListCombo = new Combo(composite, SWT.READ_ONLY);
		GridData gd_visitListCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_visitListCombo.widthHint = 223;
		visitListCombo.setLayoutData(gd_visitListCombo);
		visitListCombo.addModifyListener(new ModifyListener() {
			// change beamline value when visit_id changes
			@Override
			public void modifyText(ModifyEvent e) {
				System.out.println("visit name changed");

				String beamline = "";
				String visitItemText = getVisit();
				String[] splits = visitItemText.split(DELIMITER);
				String visitidText = splits[0].trim();
				for (int counter = 0; counter < visitList.size(); counter++) {
					VisitDetails currentVisit = visitList.get(counter);
					
					if (currentVisit.getVisit_id().equalsIgnoreCase(visitidText)) {
						beamline = currentVisit.getInstrument();
						break;
					}
				}

				int index = beamlineListCombo.indexOf(beamline);
				beamlineListCombo.select(index);
				
				// update default project name
				updateDefProjectName();
								
				dialogChanged();
				
			}

		});

		Button btnVisitList = new Button(composite, SWT.NONE);
		/**
		 * getVisits button clicked
		 */
		btnVisitList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				// getting the list of visits
				String fedid = getFedid();
				String beamline = getBeamline();
				logger.debug("getting list of visits for beamline: " + beamline + " and user: " + fedid);

				// initialise start_time and end_time
				setSqlFromDate();
				setSqlToDate();

				if (fedid.trim().equals("") && beamline.trim().equals("")) {
					logger.error("fedid and beamline cannot be BOTH null");
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
							.asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog
											.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
													"Error",
													"Please specify at least either FEDID OR BEAMLINE. \nThey cannot be both blank.");
									txtFedidValue.setText(computeFedid());
								}
							});

				} else {

					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
					visitListCombo.removeAll();

					computeVisitList(getFedid(), getBeamline());

					String[] items = new String[visitList.size()];
					// populate list of VisitDetails
					for (int i = 0; i < visitList.size(); i++) {
						items[i] = visitList.get(i).getVisit_id().toLowerCase() + DELIMITER
								+ visitList.get(i).getInstrument() + DELIMITER
								+ getDate(visitList.get(i).getStart_date());
					}

					visitListCombo.setItems(items);
					if (visitListCombo.getItemCount() >= 1)
						visitListCombo.select(0);
					
					//modify default project name
					if(visitListCombo.getItemCount() > 0){
						String[] splits = visitListCombo.getText().split(" - ");
						String projectNameContent = beamlineListCombo.getText() + "-" + splits[0];
						lblDefaultProjectname.setText("DEF: " + projectNameContent);
					}
				}

			}
		});
		btnVisitList.setText("get visits");
		new Label(composite, SWT.NONE);

		/**
		 * Set up the advanced options expandable GUI
		 * */
		
		// Specify the expansion Adapter
		expansionAdapter = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				// advanced options expanded, resize
				composite.layout();
				sc.notifyListeners(SWT.Resize, null);
			
				 // force shell resize
				Point size;
				if (e.getState())
					size = getShell().computeSize( 550, 870 );
				else
					size = getShell().computeSize( 550, 450 );

				 getShell().setSize( size );
			}
		};
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		btnCheckButton = new Button(composite, SWT.CHECK);
		btnCheckButton.setSelection(true);
		btnCheckButton.setText("Show files only");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		sc.setContent(composite);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		advancedOptionsExpander = new ExpandableComposite(composite, SWT.NONE);
		GridData gd_advancedOptionsExpander = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_advancedOptionsExpander.widthHint = 523;
		advancedOptionsExpander.setLayoutData(gd_advancedOptionsExpander);
		advancedOptionsExpander.setLayout(new GridLayout(1, false));
		advancedOptionsExpander.setText("Advanced Options");
		advancedOptionsExpander.setExpanded(false);

		Composite optionsComposite = new Composite(advancedOptionsExpander, SWT.NONE);
		optionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		optionsComposite.setLayout(new GridLayout(3, false));
				
		Label lblProjectname = new Label(optionsComposite, SWT.NONE);
		lblProjectname.setText("Project Name:");
		txtProjectname = new Text(optionsComposite, SWT.NONE | SWT.BORDER);
		GridData gd_txtProjectname = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtProjectname.widthHint = 174;
		txtProjectname.setLayoutData(gd_txtProjectname);
		lblDefaultProjectname = new Label(optionsComposite, SWT.NONE);
		GridData gd_lblDefaultProjectname = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblDefaultProjectname.widthHint = 140;
		lblDefaultProjectname.setLayoutData(gd_lblDefaultProjectname);
		lblDefaultProjectname.setText(DEFAULT_PROJECT_NAME);
		
		Label lblLinkname = new Label(optionsComposite, SWT.NONE);
		lblLinkname.setText("Link Name:");
		txtLinkname = new Text(optionsComposite, SWT.BORDER);
		GridData gd_txtLinkname = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtLinkname.widthHint = 175;
		txtLinkname.setLayoutData(gd_txtLinkname);
		Label lblDefaultLinkname = new Label(optionsComposite, SWT.NONE);
		lblDefaultLinkname.setText("DEF: " + DEFAULT_LINK_NAME);

		Label lblFrom = new Label(optionsComposite, SWT.NONE);
		lblFrom.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblFrom.setText("Visit StartDate from:");
		dateFrom = new DateTime(optionsComposite, SWT.NONE | SWT.CALENDAR | SWT.DROP_DOWN);
		dateFrom.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		dateFrom.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		logger.debug("dateFrom size: X: " + dateFrom.getBounds().x + " Y: " + dateFrom.getBounds().y + " width: " + dateFrom.getBounds().width);
		dateFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// clear visit list
				if (visitListCombo != null && visitListCombo.getSelectionIndex() != -1) {
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
					visitListCombo.removeAll();
				}
				// set from date
				setSqlFromDate();
			}

		});
		// set default FROM date to current date minus 1 month
		Calendar calA = Calendar.getInstance();
		calA.add(Calendar.MONTH, -1);
		dateFrom.setDate(calA.get(Calendar.YEAR), calA.get(Calendar.MONTH), calA.get(Calendar.DAY_OF_MONTH));
		Label lblFromPadding = new Label(optionsComposite, SWT.NONE);
		lblFromPadding.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		
		Label lblTo = new Label(optionsComposite, SWT.NONE);
		lblTo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblTo.setText("Visit StartDate to:");
		dateTo = new DateTime(optionsComposite, SWT.NONE | SWT.CALENDAR | SWT.DROP_DOWN);
		dateTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		dateTo.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		dateTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// clear visit list
				if (visitListCombo != null && visitListCombo.getSelectionIndex() != -1) {
					visitListCombo.deselectAll();
					visitListCombo.clearSelection();
					visitListCombo.removeAll();
				}
				// set to date
				setSqlToDate();
			}
		});
		// end with current date + 2 days forward
		Calendar calB = Calendar.getInstance();
		calB.add(Calendar.DATE, 2);
		dateTo.setDate(calB.get(Calendar.YEAR), calB.get(Calendar.MONTH), calB.get(Calendar.DAY_OF_MONTH));
		
		advancedOptionsExpander.setClient(optionsComposite);
		new Label(optionsComposite, SWT.NONE);
		new Label(optionsComposite, SWT.NONE);
		new Label(optionsComposite, SWT.NONE);
		new Label(optionsComposite, SWT.NONE);
		advancedOptionsExpander.addExpansionListener(expansionAdapter);
		setControl(composite);
		
		dialogChanged();
	}

	protected String getDate(String start_date) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_date);
		} catch (ParseException e) {
			logger.error("cannot parse date: " + start_date);
		}

		if (date != null)
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		
		return "";

	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {

		try{
			if (getVisit().length() == 0) {
				updateStatus("a visit must be specified in order to create the project");
				return;
			}
			updateStatus(null);
		}catch(Exception e){
			logger.error("dialogChanged: " + e.getMessage());
		}

		
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProject() {

		if (txtProjectname.getTextChars().length == 0) {
			String beamline = null;
			String visitText = "";
			for (int counter = 0; counter < visitList.size(); counter++) {
				VisitDetails currentVisit = visitList.get(counter);

				String[] splits = getVisit().split(DELIMITER);
				visitText = splits[0];

				if (currentVisit.getVisit_id().equalsIgnoreCase(visitText)) {
					beamline = currentVisit.getInstrument();
					break;
				}
			}
			// return the name of the created project
			return beamline + "-" + visitText;
		}
		// return project name from field
		return txtProjectname.getText();
	}

	public String getDirectory() {
		// get following info from results not GUI as it can be blank
		String year = "";
		String beamline = "";
		String visitText = "";
		for (int counter = 0; counter < visitList.size(); counter++) {
			VisitDetails currentVisit = visitList.get(counter);

			String[] splits = getVisit().split(DELIMITER);
			visitText = splits[0];

			if (currentVisit.getVisit_id().equalsIgnoreCase(visitText)) {
				year = currentVisit.getStart_date().substring(0, 4);
				beamline = currentVisit.getInstrument();
				logger.debug("retrieved year: " + year + " beamline: " + beamline);
				break;
			}
		}

		String directoryPath = "/dls/" + beamline + "/data/" + year + "/" + visitText;
		return directoryPath;
	}

	public String getLink() {
		if (txtLinkname.getTextChars().length == 0) {
			return DEFAULT_LINK_NAME;
		}
		return txtLinkname.getText();
	}

	public String getFedid() {
		try{
			String text = txtFedidValue.getText();
			return text.toLowerCase();
		}catch(NullPointerException npe){}
		
		return "";		
	}

	public String getBeamline() {
		try{
			String text = beamlineListCombo.getText();
			return text.toLowerCase();
		}catch(NullPointerException npe){}
		
		return "";
	}

	public String getVisit() {
		try{
			String text = visitListCombo.getText();
			return text.toLowerCase();
		}catch(NullPointerException npe){}
		
		return "";
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	private String computeBeamline() {
		String hostname = "";
		String beamline = DEFAULT_BEAMLINE;

		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// display error message
			logger.error("Error getting hostname! " + e.getMessage());
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
							"Error getting hostname.\n");
				}
			});
		}

		int index = hostname.lastIndexOf("-");

		if (index != -1) {
			String tmpbeamline = hostname.substring(0, index);

			if (Arrays.asList(beamlineList).contains(tmpbeamline)) {
				logger.info("hostname: " + hostname + " beamline: " + tmpbeamline);
				beamline = tmpbeamline;
			} else {
				logger.error("hostname: " + hostname + " is not a beamline");
			}
		}

		return beamline;
	}

	private String computeFedid() {
		return System.getProperty("user.name");
	}

	private String computeDataFolder() {
		return "/dls/" + computeBeamline() + "/data/";
	}

	private List<VisitDetails> computeVisitList(String fedid, String beamline) {

		// clear visit list
		visitList.clear();

		int rowCount = 0;
		int columnsCount = 0;
		List<String> columnNames = new ArrayList<String>();
		List<String> resultRows  = new ArrayList<String>();

		String fedidConstraintSql = "";
		String fedidTableSql  = "";
		String fedidWhereSql  = "";
		String beamlineConstraintSql = "";

		if (!fedid.trim().equals("")) {
			fedidConstraintSql = " and federal_id='" + fedid + "'";
			fedidTableSql = ", investigator.facility_user_id , federal_id ";
			fedidWhereSql = " AND investigator.facility_user_id = facility_user.facility_user_id ";
		}
		if (!beamline.trim().equals("")) {
			beamlineConstraintSql = " and instrument ='" + beamline + "'";
		}

		// build sql query
		String sqlStatement = "SELECT DISTINCT investigation.visit_id, investigation.instrument, investigation.inv_start_date " + fedidTableSql + " from investigation, investigator, facility_user"
				+ " WHERE investigation.id = investigator.investigation_id " + fedidWhereSql  
				+ fedidConstraintSql
				+ beamlineConstraintSql
				+ " AND investigation.inv_start_date >= to_date('"
				+ START_DATE
				+ "', 'DD-MM-YYYY') and investigation.inv_end_date <= to_date('"
				+ END_DATE
				+ "', 'DD-MM-YYYY') order by investigation.inv_start_date DESC";

		// getting the connection to the database
		Connection connection = null;
		try {
			connection =ICATDBClient.getConnection();
		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}
		if (connection != null) {
			try {

				// run the final query
				logger.info("-------------------------");
				logger.info(sqlStatement);
				logger.info("-------------------------");
				Statement st = connection.createStatement();
				ResultSet rs = st.executeQuery(sqlStatement);

				ResultSetMetaData rsmd = rs.getMetaData();
				columnsCount = rsmd.getColumnCount();

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
						//logger.info(resultRow);
						// populate the result array
						String delims = "#SEP#";
						String[] tokens = resultRow.split(delims);
						if(fedid.equals(null) || fedid.trim().equals("")){
							visitList.add(new VisitDetails("N/A", tokens[2], tokens[1], tokens[0]));
						}else{
							visitList.add(new VisitDetails(tokens[0], tokens[4], tokens[3], tokens[2]));

						}

					} while (rs.next());
				} else {
					// no record found
					String faultMessage = "no record found " + this.getClass().getName() + "#computeVisitList";

					logger.error(faultMessage);
				}
				logger.info("Number of Rows= " + rowCount);

				// closing result set and statement
				rs.close();
				st.close();

				connection.close();
				logger.debug("ICAT DB connection closed!");
			} catch (java.sql.SQLException e) {
				// catch any unexpected exception
				String faultMessage = "problem with sql query for db name: " + ", and user:  " + fedid + " "
						+ this.getClass().getName() + "#computeVisitList" + e.getMessage();

				logger.error(faultMessage);
			}

		}
		return visitList;
	}

	public boolean isShowFilesOnly() {
		return btnCheckButton.getSelection();
	}

	private void setSqlFromDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, dateFrom.getYear());
		cal.set(Calendar.MONTH, dateFrom.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dateFrom.getDay());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		START_DATE = sdf.format(cal.getTime());
		logger.debug("From date: " + START_DATE);
	}

	private void setSqlToDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, dateTo.getYear());
		cal.set(Calendar.MONTH, dateTo.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dateTo.getDay());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		END_DATE = sdf.format(cal.getTime());
		logger.debug("To date: " + END_DATE);
	}

	private void updateDefProjectName() {
		
		String visitText = getVisit(); 
		if(!visitText.isEmpty()){
			String beamline="";
			/*
			 * get beamline and visit id from selected text in visit combo -can make use of string split instead-
			 */
			for (int counter = 0; counter < visitList.size(); counter++) {
				VisitDetails currentVisit = visitList.get(counter);

				String[] splits = visitText.split(DELIMITER);
				visitText = splits[0];

				if (currentVisit.getVisit_id().equalsIgnoreCase(visitText)) {
					beamline = currentVisit.getInstrument();
					break;
				}
			}			
			lblDefaultProjectname.setText("DEF: "+beamline + "-" + visitText);
		}else{
			try{
				lblDefaultProjectname.setText(DEFAULT_PROJECT_NAME);
			}catch(NullPointerException npe){logger.error("default project name label not yet ready");}
		}
		
	}
	
	 public static String getOsName()
	   {
		 return System.getProperty("os.name");
	   }
	 
	   public static boolean isWindows()
	   {
	      return getOsName().startsWith("Windows");
	   }

}
