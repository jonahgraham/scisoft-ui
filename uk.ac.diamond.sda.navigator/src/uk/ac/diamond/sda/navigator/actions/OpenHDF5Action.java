/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.actions;

import org.dawb.common.ui.selection.SelectedTreeItemInfo;
import org.dawb.common.ui.selection.SelectionUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;

public class OpenHDF5Action extends Action {

	private IWorkbenchPage page;
	private String filePath;
	private ISelectionProvider provider;

	/**
	 * Construct the OpenHDF5Action with the given page.
	 * 
	 * @param p
	 *            The page to use as context to open the editor.
	 * @param selectionProvider
	 *            The selection provider
	 */
	public OpenHDF5Action(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Open HDF5 Editor"); //$NON-NLS-1$
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
		if (selection instanceof ITreeSelection) {
			if (((ITreeSelection) selection).size() == 1) {
				SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
				if (results.length == 1) {
					filePath = results[0].getFile();
					return filePath != null;
				}
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
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path("/"));
		fileStore = fileStore.getFileStore(new Path(filePath));
//		final IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
//		IDE.openEditor(page, (IFile)res);
//		EclipseUtils.openEditor((IFile)res);
		if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			try {
				if (isEnabled()) {
					IDE.openEditorOnFileStore(page, fileStore);
				}
			} catch (PartInitException e) {
				NavigatorRCPActivator.logError(0, "Could not open HDF5 Editor!", e); //$NON-NLS-1$
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error Opening HDF5 Editor", //$NON-NLS-1$
						"Could not open HDF5 Editor!"); //$NON-NLS-1$
			}
		}
	}
}
