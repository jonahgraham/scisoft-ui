/*
 * Copyright 2012 Diamond Light Source Ltd. Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package uk.ac.diamond.scisoft.feedback;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.system.info.SystemInformation;

public class FeedbackView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(FeedbackView.class);

	private static final String DAWN_FEEDBACK = "[DAWN-FEEDBACK]";
	// this is the default to the java property "org.dawnsci.feedbackmail"
	private static final String MAIL_TO = "dawnjira@diamond.ac.uk";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.scisoft.feedback.FeedbackView";
	private Action feedbackAction;
	private Action attachAction;
	private Text emailAddress;
	private Text messageText;
	private Text subjectText;

	private List<AttachedFile> attachedFilesList = new ArrayList<AttachedFile>();
	private Button btnSendFeedback;
	private TableViewer tableViewer;

	private Job feedbackJob;

	private static final String FROM_PREF = FeedbackView.class.getName() + ".emailAddress";
	private static final String SUBJ_PREF = FeedbackView.class.getName() + ".subject";

	private static final int MAX_SIZE = 10000 * 1024; // bytes
	private static final int MAX_TOTAL_SIZE = 10000 * 2048;

	/**
	 * The constructor.
	 */
	public FeedbackView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialise it.
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));
		makeActions();
		{
			Label lblEmailAddress = new Label(parent, SWT.NONE);
			lblEmailAddress.setText("Your email address for Feedback");
		}
		{
			emailAddress = new Text(parent, SWT.BORDER | SWT.SINGLE);
			emailAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			final String email = Activator.getDefault() != null ? Activator.getDefault().getPreferenceStore()
					.getString(FROM_PREF) : null;
			if (email != null && !"".equals(email))
				emailAddress.setText(email);
		}
		{
			Label lblSubject = new Label(parent, SWT.NONE);
			lblSubject.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblSubject.setText("Summary");
		}
		{
			subjectText = new Text(parent, SWT.BORDER | SWT.SINGLE);
			subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			final String subject = Activator.getDefault() != null ? Activator.getDefault().getPreferenceStore()
					.getString(SUBJ_PREF) : null;
			if (subject != null && !"".equals(subject))
				subjectText.setText(subject);
		}
		{
			Label lblComment = new Label(parent, SWT.NONE);
			lblComment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblComment.setText("Comment");
		}
		{
			messageText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			messageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		{
			Label attachLabel = new Label(parent, SWT.NONE);
			attachLabel.setText("Attached Files");

			tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			createColumns(tableViewer);
//			tableViewer.getTable().setLinesVisible(true);
			tableViewer.getTable().setToolTipText("Delete the file by clicking on the X");
			tableViewer.setContentProvider(new AttachedFileContentProvider());
			tableViewer.setLabelProvider(new AttachedFileLabelProvider());
			tableViewer.setInput(attachedFilesList);
			tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			tableViewer.refresh();
		}
		{
			Composite actionComp = new Composite(parent, SWT.NONE);
			actionComp.setLayout(new GridLayout(3, false));
			actionComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ActionContributionItem attachAci = new ActionContributionItem(attachAction);
			attachAci = new ActionContributionItem(attachAci.getAction());
			attachAci.fill(actionComp);
			Button btnBrowseFile = (Button) attachAci.getWidget();
			btnBrowseFile.setText("Attach Files");
			btnBrowseFile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

			Label fileNameLabel = new Label(actionComp, SWT.SINGLE);
			fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ActionContributionItem sendAci = new ActionContributionItem(feedbackAction);
			sendAci = new ActionContributionItem(sendAci.getAction());
			sendAci.fill(actionComp);
			btnSendFeedback = (Button) sendAci.getWidget();
			btnSendFeedback.setText("Send Feedback");
			btnSendFeedback.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}

		hookContextMenu();
		contributeToActionBars();
	}

	private void createColumns(TableViewer tv) {
		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new AttachedFileEditingSupport(tv, 0));
		TableColumn tc = tvc.getColumn();
		tc.setText("File Name");
		tc.setWidth(400);

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new AttachedFileEditingSupport(tv, 1));
		tc = tvc.getColumn();
		tc.setText("Size");
		tc.setWidth(60);

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new AttachedFileEditingSupport(tv, 2));
		tc = tvc.getColumn();
		tc.setText("Delete");
		tc.setWidth(40);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				FeedbackView.this.fillContextMenu(manager);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(attachAction);
		manager.add(feedbackAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(attachAction);
		manager.add(feedbackAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(attachAction);
		manager.add(feedbackAction);
	}

	private void makeActions() {
		feedbackAction = new Action() {
			@Override
			public void run() {
				UIJob formUIJob = new UIJob("Getting Form data") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						fromvalue = emailAddress.getText();
						subjectvalue = subjectText.getText();
						messagevalue = messageText.getText();
						emailvalue = emailAddress.getText();
						return Status.OK_STATUS;
					}
				};
				formUIJob.addJobChangeListener(new JobChangeAdapter(){
					@Override
					public void done(IJobChangeEvent event) {
						feedbackJob = createFeedbackJob();
						feedbackJob.addJobChangeListener(getJobChangeListener());
						feedbackJob.setUser(true);
						feedbackJob.schedule();
					}
				});
				formUIJob.setUser(true);
				formUIJob.schedule();
			}
		};
		feedbackAction.setText("Send Feedback");
		feedbackAction.setToolTipText("Send Feedback");
		feedbackAction.setImageDescriptor(Activator.getImageDescriptor("icons/mailedit.gif"));

		attachAction = new Action() {
			@Override
			public void run() {
				FileDialog fd = new FileDialog(Display.getDefault().getActiveShell());
				fd.setText("Attach selected file to your feedback message");
				// fd.setFilterPath("C:/");
				// String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
				// fd.setFilterExtensions(filterExt);
				String fileName = fd.open();
				if (fileName != null) {
					AttachedFile attachedfile = new AttachedFile();
					attachedfile.path = fileName;
					attachedfile.name = fileName.substring((fileName.lastIndexOf(File.separator)+1));
					File file = new File(fileName);
					attachedfile.size = getValueWithUnit(file.length());
					attachedFilesList.add(attachedfile);
					tableViewer.refresh();
				}
			}
		};
		attachAction.setText("Attach files");
		attachAction.setToolTipText("Attach file(s) to your feedback message");
		attachAction.setImageDescriptor(Activator.getImageDescriptor("icons/attach.png"));
	}

	private String fromvalue;
	private String subjectvalue;
	private String messagevalue;
	private String emailvalue;

	private Job createFeedbackJob() {
		return new Job("Sending feedback to DAWN developers") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					if (fromvalue == null || fromvalue.length() == 0) {
						fromvalue = "user";
					} else {
						if (Activator.getDefault() != null) {
							Activator.getDefault().getPreferenceStore().setValue(FROM_PREF, fromvalue);
						}
					}

					if (monitor.isCanceled()) return Status.CANCEL_STATUS;

					String from = fromvalue;
					String subject = DAWN_FEEDBACK + " - " + subjectvalue;
					if (subjectvalue != null && !"".equals(subjectvalue)) {
						if (Activator.getDefault() != null) {
							Activator.getDefault().getPreferenceStore()
									.setValue(SUBJ_PREF, subjectvalue);
						}
					}
					StringBuilder messageBody = new StringBuilder();
					String computerName = "Unknown";
					try {
						computerName = InetAddress.getLocalHost().getHostName();
					} finally {

					}
					messageBody.append("Machine is   : " + computerName + "\n");

					String versionNumber = "Unknown";
					try {
						versionNumber = BundleUtils.getDawnVersion();
					} catch (Exception e) {
						logger.debug("Could not retrieve product and system information:" + e);
					}

					if (monitor.isCanceled()) return Status.CANCEL_STATUS;

					messageBody.append("Version is   : " + versionNumber + "\n");
					messageBody.append(messagevalue);
					messageBody.append("\n\n\n");
					messageBody.append(SystemInformation.getSystemString());

					File logFile = new File(System.getProperty("user.home"), "dawnlog.html");

					// get the mail to address from the properties
					String mailTo = System.getProperty("uk.ac.diamond.scisoft.feedback.recipient", MAIL_TO);

					if (logFile.length() > MAX_SIZE) {
						logger.error("The log file size exceeds: " + MAX_SIZE);
						return new Status(IStatus.WARNING, "File Size Problem",
								"The log file attached to the feedback exceeds 10MB.");
					}

					if (monitor.isCanceled()) return Status.CANCEL_STATUS;

					// fill the list of files to attach
					List<File> attachmentFiles = new ArrayList<File>();
					int totalSize = 0;
					for (int i = 0; i < attachedFilesList.size(); i++) {
						attachmentFiles.add(new File(attachedFilesList.get(i).path));
						// check that the size does not exceed the maximum one
						if (attachmentFiles.get(i).length() > MAX_SIZE) {
							logger.error("The attachment file size exceeds: " + MAX_SIZE);
							return new Status(IStatus.WARNING, "File Size Problem",
									"The attachment file size exceeds 10MB. Please chose a smaller file to attach.");
						}
						totalSize += attachmentFiles.get(i).length();
					}
					if (totalSize > MAX_TOTAL_SIZE) {
						logger.error("The total size of your attachement files exceeds: " + MAX_TOTAL_SIZE);
						return new Status(IStatus.WARNING, "File Size Problem",
								"The total size of your attachement files exceeds 20MB. Please chose smaller files to attach.");
					}

					if (monitor.isCanceled()) return Status.CANCEL_STATUS;

					// Test that the message is correctly formatted (not empty) and Test the email format
					if (!messagevalue.equals("") && emailvalue.contains("@")) {
						return FeedbackRequest.doRequest(from, mailTo,
								System.getProperty("user.name", "Unknown User"), subject,
								messageBody.toString(), logFile, attachmentFiles, monitor);
					}
					return new Status(IStatus.WARNING, "Format Problem",
							"Please type in your email and/or the message body before sending the feedback.");
				} catch (Exception e) {
					logger.error("Feedback email not sent", e);
					return new Status(
							IStatus.WARNING,
							"Feedback not sent!",
							"Please check that you have an Internet connection. If the feedback is still not working, click on OK to submit your feedback using the online feedback form available at http://dawnsci-feedback.appspot.com/");
				}
			}
		};
	}

	private JobChangeAdapter getJobChangeListener(){
		return new JobChangeAdapter() {
			@Override
			public void running(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						btnSendFeedback.setText("Sending");
						feedbackAction.setEnabled(false);
					}
				});
			}

			@Override
			public void done(final IJobChangeEvent event) {
				final String message = event.getResult().getMessage();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (event.getResult().isOK()) {
							messageText.setText("");
							attachedFilesList.clear();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow().getShell(),
											"Feedback successfully sent", message);
								}
							});
						} else {
							MessageBox messageDialog = new MessageBox(PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING | SWT.OK
									| SWT.CANCEL);
							messageDialog.setText("Feedback not sent!");

							messageDialog.setMessage(message);
							int result = messageDialog.open();

							if (message.startsWith("Please check") && result == SWT.OK) {
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										try {
											PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
													.openURL(new URL(FeedbackRequest.SERVLET_URL));
										} catch (PartInitException e) {
											logger.error("Error opening browser:", e);
										} catch (MalformedURLException e) {
											logger.error("Error - Malformed URL:", e);
										}
									}
								});
							}
						}
						btnSendFeedback.setText("Send Feedback");
						feedbackAction.setEnabled(true);
					}
				});
			}

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						btnSendFeedback.setText("Sending");
						feedbackAction.setEnabled(false);
					}
				});
			}
		};
	}

	private String getValueWithUnit(long value){
		if (((value / 1000) > 1) && ((value / 1000) < 1000))
			return String.valueOf(value / 1000) + "KB";
		else if ((value / 1000) > 1000)
			return String.valueOf(value / 1000000) + "MB";
		else
			return String.valueOf(value) + "B";
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		messageText.setFocus();
	}

	/**
	 * item data of the tableviewer
	 */
	class AttachedFile {
		String path;
		String name;
		String size;
		boolean delete;
	}

	/**
	 * Content Provider of the Table Viewer
	 */
	class AttachedFileContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return null;
			}
			return ((List<?>) inputElement).toArray();
		}
	}

	private static final Image DELETE = Activator.getImageDescriptor("icons/delete_obj.png").createImage();

	/**
	 * Label Provider of the Table Viewer
	 */
	class AttachedFileLabelProvider implements ITableLabelProvider {
		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 2)
				return null;
			if (element == null)
				return null;
			return DELETE;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element == null)
				return null;
			AttachedFile file = (AttachedFile) element;
			if (columnIndex == 0) {
				return file.name;
			} else if (columnIndex == 1) {
				return file.size;
			} else if (columnIndex == 2) {
				return null;
			}
			return null;
		}
	}

	/**
	 * Editing Support of the table cells (the boolean delete icon)
	 */
	class AttachedFileEditingSupport extends EditingSupport {
		private TableViewer tv;
		private int column;

		public AttachedFileEditingSupport(TableViewer viewer, int col) {
			super(viewer);
			tv = viewer;
			this.column = col;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object element) {
			if(column == 2)
				return true;
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			return true;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (column == 2) {
				AttachedFile file = (AttachedFile) element;
				attachedFilesList.remove(file);
				tv.refresh();
			}
		}
	}

}