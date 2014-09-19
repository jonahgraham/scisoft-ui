/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.feedback;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedbackAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(FeedbackAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FeedbackView.ID);
		} catch (PartInitException e) {
			logger.error("Could not open Feedback view:", e);
		}
		return null;
	}

	@Override
	public void run(IAction action) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FeedbackView.ID);
		} catch (PartInitException e) {
			logger.error("Could not open Feedback view:", e);
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Not implemented

	}

	@Override
	public void dispose() {
		// Not implemented

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// Not implemented

	}

}
