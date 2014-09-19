/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.editors;

import java.io.File;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.explorers.TextDataExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.navigator.srs.SRSTreeData;

public class TextDataEditor extends EditorPart {

	private TextDataExplorer srsxp;
	private File file;

	public TextDataEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		file = EclipseUtils.getFile(input);
		if (file == null || !file.exists()) {
			throw new PartInitException("Input is not a file or file does not exist");
		} else if (!file.canRead()) {
			throw new PartInitException("Cannot read file (are permissions correct?)");
		}

		setSite(site);
        setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		IWorkbenchPartSite site = getSite();
		srsxp = new TextDataExplorer(parent, site, null);
		try {
			srsxp.loadFileAndDisplay(file.getPath(), null);
		} catch (Exception e) {
			return;
		}

		site.setSelectionProvider(srsxp);
		// Register selection listener
		registerSelectionListener();
	}

	@Override
	public void setFocus() {
		srsxp.setFocus();
	}

	private ISelectionListener selectionListener;
	
	/* Setting up of the SRSEditor as a Selection listener on the navigator selectionProvider */
	private void registerSelectionListener() {

		final ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null)
			throw new IllegalStateException("Cannot acquire the selection service!");
		selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
				if (selection instanceof IStructuredSelection) {
					if (!selection.isEmpty()) {
						final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
						final Object element = structuredSelection.getFirstElement();
						if (element instanceof SRSTreeData) {
							SRSTreeData srsData = (SRSTreeData) element;
							String filename = srsData.getFile().getLocation().toString();
							String[] temp = filename.split("/");
							filename = temp[temp.length-1];
							//update only the relevant srseditor
							String editorName= getSite().getPart().getTitle();
							if(filename.equals(editorName))
								update(part, srsData);			
						}
					}

				}
			}
		};
		selectionService.addSelectionListener(selectionListener);
	}

	private void unregisterSelectionListener() {

		final ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null)
			throw new IllegalStateException("Cannot acquire the selection service!");

		selectionService.removeSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {

		// Unregister selection listener
		unregisterSelectionListener();

		if (srsxp != null && !srsxp.isDisposed())
			srsxp.dispose();
		super.dispose();
	}

	/**
	 * This editor uses an SRS explorer
	 * @return explorer class
	 */
	public static Class<? extends AbstractExplorer> getExplorerClass() {
		return TextDataExplorer.class;
	}

	public void update(final IWorkbenchPart original, final SRSTreeData srsData) {
		
		// Make Display to wait until current focus event is finish, and then execute new focus event	
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				while (Display.getDefault().readAndDispatch()) {
					//wait for events to finish before continue
				}
				srsxp.forceFocus();
			}
		});
		//EclipseUtils.getActivePage().activate(this);

		TableViewer viewer = srsxp.getViewer();
		for (int i = 0; i < viewer.getTable().getItemCount(); i++) {
			String name = viewer.getTable().getItem(i).getText();
			if (name.equals(srsData.getName())) {
				viewer.getTable().setSelection(i);
				//selectItemAction.run();
				srsxp.selectItemSelection();
				break;
			}
		}

		// new focus event
		EclipseUtils.getActivePage().activate(original);

	}

}
