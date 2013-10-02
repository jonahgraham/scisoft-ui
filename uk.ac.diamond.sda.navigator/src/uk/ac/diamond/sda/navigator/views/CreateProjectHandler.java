/*
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.sda.navigator.views;

import java.io.File;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

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
