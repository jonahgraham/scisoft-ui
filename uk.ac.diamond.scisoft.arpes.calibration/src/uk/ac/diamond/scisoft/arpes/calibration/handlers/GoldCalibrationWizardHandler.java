/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.arpes.calibration.views.ARPESFilePreview;
import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationWizard;

public class GoldCalibrationWizardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		openWizard(HandlerUtil.getActiveShell(event), selection);
		return Boolean.FALSE;
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

	@Override
	public boolean isEnabled() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object selected = selection.getFirstElement();
			String path = "";
			if (selected instanceof IFile) {
				IFile ifile = (IFile) selected;
				path = ifile.getLocation().toOSString();
			} else if (selected instanceof File) {
				File file = (File) selected;
				path = file.getPath();
			}
			return ARPESFilePreview.isArpesFile(path, null);
		}

		return false;
	}

	@Override
	public boolean isHandled() {
		return true;
	}
}
