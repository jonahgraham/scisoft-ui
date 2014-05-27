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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.radio.RadioGroupWidget;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
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

import uk.ac.diamond.scisoft.feedback.attachment.AttachedFile;
import uk.ac.diamond.scisoft.feedback.attachment.AttachedFileContentProvider;
import uk.ac.diamond.scisoft.feedback.attachment.AttachedFileEditingSupport;
import uk.ac.diamond.scisoft.feedback.attachment.AttachedFileLabelProvider;
import uk.ac.diamond.scisoft.feedback.jobs.FeedbackJob;
import uk.ac.diamond.scisoft.feedback.utils.FeedbackConstants;
import uk.ac.diamond.scisoft.feedback.utils.FeedbackUtils;

public class FeedbackView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(FeedbackView.class);

	// this is the default to the java property "org.dawnsci.feedbackmail"
	private String destinationEmail = System.getProperty(FeedbackConstants.RECIPIENT_PROPERTY, FeedbackConstants.MAIL_TO);
	private String destinationName = "DAWN developers";

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

	private FeedbackJob feedbackJob;

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

		final ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite content = new Composite(scrollComposite, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		makeActions();
		{
			Composite radioSelection = new Composite(content, SWT.NONE);
			radioSelection.setLayout(new GridLayout(3, false));
			Label label = new Label(radioSelection, SWT.NONE);
			label.setText("Send Feedback to :");

			RadioGroupWidget mailToRadios = new RadioGroupWidget(radioSelection);
			mailToRadios.setActions(createEmailRadioActions());
		}
		{
			Label lblEmailAddress = new Label(content, SWT.NONE);
			lblEmailAddress.setText("Your email address for Feedback");
		}
		{
			emailAddress = new Text(content, SWT.BORDER | SWT.SINGLE);
			emailAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			final String email = Activator.getDefault() != null ? Activator.getDefault().getPreferenceStore()
					.getString(FeedbackConstants.FROM_PREF) : null;
			if (email != null && !"".equals(email))
				emailAddress.setText(email);
		}
		{
			Label lblSubject = new Label(content, SWT.NONE);
			lblSubject.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblSubject.setText("Summary");
		}
		{
			subjectText = new Text(content, SWT.BORDER | SWT.SINGLE);
			subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			final String subject = Activator.getDefault() != null ? Activator.getDefault().getPreferenceStore()
					.getString(FeedbackConstants.SUBJ_PREF) : null;
			if (subject != null && !"".equals(subject))
				subjectText.setText(subject);
		}
		{
			Label lblComment = new Label(content, SWT.NONE);
			lblComment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblComment.setText("Comment");
		}
		{
			messageText = new Text(content, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumHeight = 200;
			messageText.setLayoutData(gd);
		}
		{
			Label attachLabel = new Label(content, SWT.NONE);
			attachLabel.setText("Attached Files");

			tableViewer = new TableViewer(content, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
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
			Composite actionComp = new Composite(content, SWT.NONE);
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

		scrollComposite.setContent(content);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrollComposite.getClientArea();
				int height = content.computeSize(r.width, SWT.DEFAULT).y;
				scrollComposite.setMinHeight(height);
				scrollComposite.setMinWidth(content.computeSize(SWT.DEFAULT, r.height).x);
			}
		});

		hookContextMenu();
		contributeToActionBars();
	}

	private List<Action> createEmailRadioActions() {
		List<Action> radioActions = new ArrayList<Action>();
		Action sendToMailingListAction = new Action() {
			@Override
			public void run() {
				destinationEmail = FeedbackConstants.DAWN_MAILING_LIST;
				destinationName = "DAWN mailing list";
			}
		};
		sendToMailingListAction.setText("DAWN mailing list");
		sendToMailingListAction.setToolTipText("Send feedback to the DAWN mailing list");

		Action sendToDevelopersAction = new Action() {
			@Override
			public void run() {
				destinationEmail = System.getProperty(FeedbackConstants.RECIPIENT_PROPERTY, FeedbackConstants.MAIL_TO);
				destinationName = "DAWN developers";
			}
		};
		sendToDevelopersAction.setText("DAWN developers");
		sendToDevelopersAction.setToolTipText("Send feedback to DAWN developers");

		radioActions.add(sendToDevelopersAction);
		radioActions.add(sendToMailingListAction);
		return radioActions;
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

			private String fromvalue;
			private String subjectvalue;
			private String messagevalue;
			private String emailvalue;

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
						feedbackJob = new FeedbackJob("Sending feedback to " + destinationName, 
								fromvalue, subjectvalue, messagevalue, emailvalue, destinationEmail,
								attachedFilesList);
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
				fd.setFilterNames(new String [] {"Log Files (.log)", "Text Files (.txt)", "All Files (*.*)"});
				fd.setFilterExtensions(new String [] {"*?.log", "*?.txt", "*?.*"}); 
				fd.setText("Attach selected file to your feedback message");
				String fileName = fd.open();
				if (fileName != null) {
					AttachedFile attachedfile = new AttachedFile();
					attachedfile.path = fileName;
					attachedfile.name = fileName.substring((fileName.lastIndexOf(File.separator)+1));
					File file = new File(fileName);
					attachedfile.size = FeedbackUtils.getValueWithUnit(file.length());
					attachedFilesList.add(attachedfile);
					tableViewer.refresh();
				}
			}
		};
		attachAction.setText("Attach files");
		attachAction.setToolTipText("Attach file(s) to your feedback message");
		attachAction.setImageDescriptor(Activator.getImageDescriptor("icons/attach.png"));
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

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		messageText.setFocus();
	}
}