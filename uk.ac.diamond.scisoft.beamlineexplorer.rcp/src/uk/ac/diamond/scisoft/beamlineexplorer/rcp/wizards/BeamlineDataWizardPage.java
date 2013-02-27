/*
 * Copyright 2013 Diamond Light Source Ltd. Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.wizards;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.beamlineexplorer.rcp.icat.ICATDBClient;

public class BeamlineDataWizardPage extends WizardPage implements KeyListener {

	private static final String DEFAULT_BEAMLINE = "";
	private static final String DELIMITER = " - ";
	private String START_DATE;
	private String END_DATE;
	private Text txtProject;
	private Text txtFedidValue;
	private Button btnCheckButton;
	private Combo beamlineListCombo;
	private Combo visitListCombo;
	private final String initProject;
	private final String initDirectory;
	private final String initFolder;

	private ScrolledComposite sc;
	private Text txtProjectname;
	private Text txtLinkname;
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
	private Text text;
	private Label lblNewLabel;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

	/**
	 * Constructor for BeamlineDataWizardPage.
	 * 
	 * @param prevDirectory
	 * @param prevFolder
	 * @param prevProject
	 */
	public BeamlineDataWizardPage(ISelection selection, String prevProject, String prevFolder, String prevDirectory) {
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

		/*
		 * sort beamline list by name
		 */
		Arrays.sort(beamlineList);
		String beamline = computeBeamline();

		// Set up the composite to hold all the information
		sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
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
			}
		});
		beamlineListCombo.setItems(beamlineList);
		beamlineListCombo.select(Arrays.asList(beamlineList).indexOf(beamline));
		new Label(composite, SWT.NONE);

		Label lblFedid = new Label(composite, SWT.NONE);
		lblFedid.setText("Detected Fedid:");

		txtFedidValue = new Text(composite, SWT.NONE);
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
				String beamline = "";
				for (int counter = 0; counter < visitList.size(); counter++) {
					VisitDetails currentVisit = visitList.get(counter);

					String visitItemText = getVisit();
					String[] splits = visitItemText.split(DELIMITER);
					String visitidText = splits[0].trim();
					if (currentVisit.getVisit_id().equalsIgnoreCase(visitidText)) {
						beamline = currentVisit.getInstrument();
						break;
					}
				}

				int index = beamlineListCombo.indexOf(beamline);
				beamlineListCombo.select(index);
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
					if (visitListCombo.getItemCount() > 1)
						visitListCombo.select(0);
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
				composite.layout();
				sc.notifyListeners(SWT.Resize, null);
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
		// set default FROM date 1 year backward
		Calendar calA = Calendar.getInstance();
		int lastYear = calA.get(Calendar.YEAR) - 1;

		advancedOptionsExpander = new ExpandableComposite(composite, SWT.NONE);
		GridData gd_advancedOptionsExpander = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_advancedOptionsExpander.widthHint = 242;
		advancedOptionsExpander.setLayoutData(gd_advancedOptionsExpander);
		advancedOptionsExpander.setLayout(new GridLayout(1, false));
		advancedOptionsExpander.setText("Advanced Options");
		advancedOptionsExpander.setExpanded(false);

		Composite optionsComposite = new Composite(advancedOptionsExpander, SWT.NONE);
		optionsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		optionsComposite.setLayout(new GridLayout(2, false));

		Label lblProjectname = new Label(optionsComposite, SWT.NONE);
		lblProjectname.setText("Project Name:");
		txtProjectname = new Text(optionsComposite, SWT.FILL);

		Label lblLinkname = new Label(optionsComposite, SWT.NONE);
		lblLinkname.setText("Link Name:");
		txtLinkname = new Text(optionsComposite, SWT.NONE);

		Label lblFrom = new Label(optionsComposite, SWT.NONE);
		lblFrom.setText("From Date:");
		dateFrom = new DateTime(optionsComposite, SWT.NONE | SWT.CALENDAR | SWT.DROP_DOWN);
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
		// begin from last year same date
		dateFrom.setYear(lastYear);
		dateFrom.setMonth(calA.get(Calendar.MONTH));
		dateFrom.setDay(calA.get(Calendar.DAY_OF_YEAR));

		Label lblTo = new Label(optionsComposite, SWT.NONE);
		lblTo.setText("To Date:");
		dateTo = new DateTime(optionsComposite, SWT.NONE | SWT.CALENDAR | SWT.DROP_DOWN);
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
		// end with current date + 1 month
		dateTo.setYear(calA.get(Calendar.YEAR));
		dateTo.setMonth(calA.get(Calendar.MONTH) + 1);
		dateTo.setDay(calA.get(Calendar.DAY_OF_YEAR));

		advancedOptionsExpander.setClient(optionsComposite);
		new Label(optionsComposite, SWT.NONE);
		new Label(optionsComposite, SWT.NONE);
		advancedOptionsExpander.addExpansionListener(expansionAdapter);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		// sc.setMinSize(new Point(400, 600));
		setControl(composite);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		// dialogChanged() ;
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
			return "beamlineData";
		}
		return txtLinkname.getText();
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
		List<String> resultRows = new ArrayList<String>();

		String fedidsql = "";
		String beamlineSql = "";

		if (!fedid.trim().equals("")) {
			fedidsql = " and federal_id='" + fedid + "'";
		}
		if (!beamline.trim().equals("")) {
			beamlineSql = " and instrument ='" + beamline + "'";
		}

		// build sql query
		// ignore visits that are more than 1 month in the future
		String sqlStatement = "select investigation.visit_id, investigation.instrument, investigation.inv_start_date, investigator.facility_user_id, federal_id from investigation, investigator, facility_user"
				+ " where investigation.id = investigator.investigation_id and investigator.facility_user_id = facility_user.facility_user_id "
				+ fedidsql
				+ beamlineSql
				+ " and investigation.inv_start_date >= to_date('"
				+ START_DATE
				+ "', 'DD-MM-YYYY') and investigation.inv_end_date <= to_date('"
				+ END_DATE
				+ "', 'DD-MM-YYYY') order by investigation.inv_start_date desc";

		// getting the connection to the database
		if (ICATDBClient.getConnection() != null) {
			try {

				// run the final query
				logger.info("-------------------------");
				logger.info(sqlStatement);
				logger.info("-------------------------");
				Statement st = ICATDBClient.getConnection().createStatement();
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
						// populate the result array
						String delims = "#SEP#";
						String[] tokens = resultRow.split(delims);
						visitList.add(new VisitDetails(tokens[0], tokens[4], tokens[3], tokens[2]));
						logger.info(resultRow);

					} while (rs.next());
				} else {
					// no record found
					String faultMessage = "no record found " + this.getClass().getName() + "#computeVisitList";

					logger.error(faultMessage);
				}
				// rs.last();

				logger.info("Number of Rows= " + rowCount);

				// closing result set and statement
				rs.close();
				st.close();

			} catch (java.sql.SQLException e) {
				// catch any unexpected exception
				String faultMessage = "problem with sql query for db name: " + ", and user:  " + fedid + " "
						+ this.getClass().getName() + "#computeVisitList" + e.getMessage();

				logger.error(faultMessage);
				// throw new BadDataExceptionException(faultMessage);
			}

		}

		try {
			ICATDBClient.getConnection().close();
			logger.debug("ICAT DB connection closed!");
		} catch (SQLException e) {
			logger.error("Error closing ICAT database connection");
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

}
