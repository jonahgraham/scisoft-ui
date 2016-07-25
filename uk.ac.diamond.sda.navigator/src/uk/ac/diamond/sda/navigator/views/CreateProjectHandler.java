/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.views;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;

//import uk.ac.diamond.scisoft.analysis.rcp.wizards.DataWizard;

public class CreateProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			// This handler is not used anywhere
			// it has been commented out in order to remove the analysis.rcp dependency
			/*
			final DataWizard wizard = (DataWizard)EclipseUtils.openWizard("uk.ac.diamond.scisoft.analysis.rcp.wizards.DataWizard", false);
			File selectedFile = null;
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection sel = (IStructuredSelection)selection;
				if (sel.getFirstElement() instanceof File) {
					selectedFile = (File)sel.getFirstElement();
					wizard.setDataLocation(selectedFile);
				}
			}
			
			final WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			wd.setTitle(wizard.getWindowTitle());
			wd.open();
			*/
			
			// Select project explorer
			EclipseUtils.getActivePage().showView("org.eclipse.ui.navigator.ProjectExplorer", null, IWorkbenchPage.VIEW_ACTIVATE);
			

			return Boolean.TRUE;
			
		} catch (Exception ne) {
			throw new ExecutionException("Cannot open uk.ac.diamond.scisoft.analysis.rcp.wizards.DataWizard!", ne);
		}
	}

}
