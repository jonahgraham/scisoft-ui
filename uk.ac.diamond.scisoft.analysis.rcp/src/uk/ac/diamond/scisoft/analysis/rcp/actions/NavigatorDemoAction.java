/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class NavigatorDemoAction extends AbstractHandler implements IObjectActionDelegate {

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
			Object[] selObjects = sel.toArray();
			for (int i = 0; i < selObjects.length; i++)
				System.err.println(selObjects[i]+" "+selObjects[i].getClass().toString());
				
		} else
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
