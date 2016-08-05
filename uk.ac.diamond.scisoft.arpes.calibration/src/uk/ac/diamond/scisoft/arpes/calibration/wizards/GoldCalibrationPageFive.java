package uk.ac.diamond.scisoft.arpes.calibration.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.richbeans.widgets.content.FileContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.arpes.calibration.Activator;
import uk.ac.diamond.scisoft.arpes.calibration.CalibrationSaver;

public class GoldCalibrationPageFive extends CalibrationWizardPage {
	private DataMessageComponent calibrationData;
	private String path;
	private Label txtLabel;
	private Text txtPath;
	private Button resourceButton;
	private Button fileButton;
	private Button overwrite;
	protected boolean isOverwrite;
	private CalibrationSaver saveWithProgress;

	public GoldCalibrationPageFive(DataMessageComponent calibrationData) {
		super("Save calibrated data");
		setTitle("Save Calibration");
		setDescription("Select the location and name of file where to save calibrated data");
		this.calibrationData = calibrationData;
		saveWithProgress = new CalibrationSaver(calibrationData);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 4;
//		layout.verticalSpacing = 9;

		txtLabel = new Label(container, SWT.NULL);
		txtLabel.setText("Calibration file path and name to save:");
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		txtPath = new Text(container, SWT.BORDER);
		txtPath.setEditable(true);

		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(txtPath, new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		if (path != null)
			txtPath.setText(path);
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		txtPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				path = txtPath.getText();
			}
		});

		resourceButton = new Button(container, SWT.PUSH);
		resourceButton.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
		resourceButton.setToolTipText("Browse to file inside a project");
		resourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleResourceBrowse();
			}
		});
		resourceButton.setEnabled(true);

		fileButton = new Button(container, SWT.PUSH);
		fileButton.setImage(Activator.getImageDescriptor("icons/folder.png").createImage());
		fileButton.setToolTipText("Browse to an external file");
		fileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleFileBrowse();
			}
		});

		final Label filler = new Label(container, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		overwrite = new Button(container, SWT.CHECK);
		overwrite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		overwrite.setToolTipText("Overwrite existing file(s) of the same name during processing.");
		overwrite.setText("Overwrite file if it already exists");
		overwrite.setSelection(true);
		overwrite.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isOverwrite = overwrite.getSelection();
			}
		});

		setControl(container);

		setPageComplete(false);
	}

	private void handleResourceBrowse() {
		IResource[] res = null;
//		if (newFile) {
			final IResource cur = getIResource();
			final IPath path = cur != null ? cur.getFullPath() : null;
			IFile file = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"File location", "Please choose a location.", path, null);
			res = file != null ? new IResource[] { file } : null;
//		} else {
//			res = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
//					"File location", "Please choose a location.", false, new Object[] { getIResource() }, null);
//		}

		if (res != null && res.length > 0) {
			this.path = res[0].getFullPath().toOSString();
			txtPath.setText(this.path);
		}
		calibrationData.addUserObject(GoldCalibrationWizard.SAVE_PATH, getAbsoluteFilePath());
	}

	private IResource getIResource() {
		IResource res = null;
		if (path != null) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		}
		if (res == null && path != null) {
			final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			if (path.startsWith(workspace)) {
				String relPath = path.substring(workspace.length());
				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
			}
		}
		return res;
	}

	private void handleFileBrowse() {
		String path = null;
		final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
		dialog.setText("Choose file");
		final String filePath = getAbsoluteFilePath();
		if (filePath != null) {
			final File file = new File(filePath);
			if (file.exists()) {
				if (file.isDirectory()) {
					dialog.setFilterPath(file.getAbsolutePath());
				} else {
					dialog.setFilterPath(file.getParent());
					dialog.setFileName(file.getName());
				}
			}

		}
		path = dialog.open();
		if (path != null) {
			this.path = path;
			txtPath.setText(this.path);
			calibrationData.addUserObject(GoldCalibrationWizard.SAVE_PATH, getAbsoluteFilePath());
		}
	}

	/**
	 * 
	 * @return the output file path
	 */
	private String getAbsoluteFilePath() {
		try {
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res != null)
				return res.getLocation().toOSString();
			final File file = new File(path);
			String parDir = file.getParent();
			IContainer folder = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(parDir);
			if (folder != null) {
				final IFile newFile = folder.getFile(new Path(file.getName()));
				if (newFile.exists())
					newFile.touch(null);
				return newFile.getLocation().toOSString();
			}
			return path;
		} catch (Throwable ignored) {
			return null;
		}
	}
	// @Override
	// public void setVisible(boolean visible) {
	// if (visible) {
	//
	// }
	// super.setVisible(visible);
	// }

	@Override
	public boolean runProcess() {
		System.out.println("Page 5");
		try {
			getContainer().run(true, true, saveWithProgress);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public int getPageNumber() {
		return 5;
	}

}