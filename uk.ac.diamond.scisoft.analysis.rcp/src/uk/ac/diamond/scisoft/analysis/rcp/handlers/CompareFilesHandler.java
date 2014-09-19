/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.editors.CompareFilesEditor;

/**
 * Handler to send selection to compare file editor
 */
public class CompareFilesHandler extends AbstractHandler implements IObjectActionDelegate {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doAction();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		doAction();
	}

	private Object doAction() {
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IStructuredSelection sel = (IStructuredSelection)page.getSelection();
		
		if (sel != null) {
			IEditorInput input = CompareFilesEditor.createComparesFilesEditorInput(sel);
			try {
				page.openEditor(input, CompareFilesEditor.ID);
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		} else
			return Boolean.FALSE;
		return Boolean.TRUE;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
