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

package uk.ac.diamond.sda.navigator.actions;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;
import uk.ac.diamond.sda.navigator.srs.SRSTreeData;

public class OpenSRSAction extends Action {

	private IWorkbenchPage page;
	private SRSTreeData data;
	private ISelectionProvider provider;

	/**
	 * Construct the OpenSRSAction with the given page.
	 * 
	 * @param p
	 *            The page to use as context to open the editor.
	 * @param selectionProvider
	 *            The selection provider
	 */
	public OpenSRSAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Open SRS Editor"); //$NON-NLS-1$
		page = p;
		provider = selectionProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1 && sSelection.getFirstElement() instanceof SRSTreeData) {
				data = ((SRSTreeData) sSelection.getFirstElement());
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		try {
			if (isEnabled()) {
				IFile srsFile = data.getFile();
				IDE.openEditor(page, srsFile);
				// we reactivate the ProjectExplorer view 
				EclipseUtils.activateView("org.eclipse.ui.navigator.ProjectExplorer");
			}
		} catch (PartInitException e) {
			NavigatorRCPActivator.logError(0, "Could not open SRS Editor!", e); //$NON-NLS-1$
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error Opening SRS Editor", //$NON-NLS-1$
					"Could not open SRS Editor!"); //$NON-NLS-1$
		}
	}
}
