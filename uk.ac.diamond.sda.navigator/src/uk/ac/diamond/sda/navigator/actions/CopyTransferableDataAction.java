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

import org.dawb.common.services.ServiceManager;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.sda.navigator.srs.SRSTreeData;

public class CopyTransferableDataAction extends Action {

	private static final Logger logger = LoggerFactory.getLogger(CopyTransferableDataAction.class);
	@SuppressWarnings("unused")
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
	public CopyTransferableDataAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Copy Data"); //$NON-NLS-1$
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
				setText("Copy '"+data.getName()+"' it may then be paste in the 'Data' view");
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
				final String    path = data.getFile().getRawLocation().toOSString();
				final IDataHolder hd = LoaderFactory.getData(path, true, false, new IMonitor.Stub());
				final ITransferableDataService service = (ITransferableDataService)ServiceManager.getService(ITransferableDataService.class);
				final ITransferableDataObject  object  = service.createData(hd, hd.getMetadata(), data.getName());
				service.setBuffer(object);
			}
		} catch (Exception e) {
			logger.error("Cannot copy data as transferable.", e);
		}
	}
}
