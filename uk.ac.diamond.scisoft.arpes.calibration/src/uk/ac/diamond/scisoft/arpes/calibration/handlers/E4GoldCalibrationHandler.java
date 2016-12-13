package uk.ac.diamond.scisoft.arpes.calibration.handlers;

import java.io.File;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.arpes.calibration.views.ARPESFilePreview;
import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationWizard;

public class E4GoldCalibrationHandler {

	private SelectionEvent event;

	public E4GoldCalibrationHandler(SelectionEvent event) {
		this.event = event;
	}

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection) {
		ISelection iSelection = selection instanceof ISelection ? (ISelection) selection : null;

		if (iSelection == null) {
			iSelection = event instanceof ISelection ? (ISelection) event : null;
			if (iSelection == null)
				return;
		}
		if (iSelection instanceof IStructuredSelection) {
			Object file = ((IStructuredSelection) iSelection).getFirstElement();
			String filename = null;
			if (file instanceof IFile) {
				String fileExtension = ((IFile) file).getFileExtension();
				if (fileExtension != null && fileExtension.equals("nxs")) {
					filename = ((IFile) file).getRawLocation().toOSString();
				}
			} else if (file instanceof File) {
				if (!((File) file).isDirectory()) {
					filename = ((File) file).getAbsolutePath();
				}
			}
			if (filename != null) {
				if (ARPESFilePreview.isArpesFile(filename, null)) {
					openWizard(Display.getDefault().getActiveShell(), iSelection);
				}
			}
		}
	}

	private void openWizard(final Shell shell, ISelection selection) {
		GoldCalibrationWizard wizard = new GoldCalibrationWizard();
		if (selection instanceof IStructuredSelection) {
			wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.open();
		} else {
			//TODO
		}
	}

	@CanExecute
	public boolean canExecute (@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection) {
		if (selection instanceof IStructuredSelection) {
			Object file = ((IStructuredSelection) selection).getFirstElement();
			String filename = null;
			if (file instanceof IFile) {
				String fileExtension = ((IFile) file).getFileExtension();
				if (fileExtension != null && fileExtension.equals("nxs")) {
					filename = ((IFile) file).getRawLocation().toOSString();
				}
			} else if (file instanceof File) {
				if (!((File) file).isDirectory()) {
					filename = ((File) file).getAbsolutePath();
				}
			}
			if (filename != null) {
				if (ARPESFilePreview.isArpesFile(filename, null)) {
					return true;
				}
			}
		}
		return false;
	}
}
