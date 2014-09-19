/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.analysis.rcp.views.ImageExplorerView;

/**
 *
 */
public class StopImageLoadingAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ImageExplorerView view = (ImageExplorerView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ImageExplorerView.ID);
		if (view != null) {
			Command command = event.getCommand();
			boolean oldValue = HandlerUtil.toggleCommandState(command);
			view.stopLoading(!oldValue);
		}
		return null;
	}
}
