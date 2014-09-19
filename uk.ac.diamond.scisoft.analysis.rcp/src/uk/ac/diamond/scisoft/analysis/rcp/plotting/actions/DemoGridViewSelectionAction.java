/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.views.ImageExplorerView;


/**
 *
 */
public class DemoGridViewSelectionAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewID = event.getParameter("uk.ac.diamond.scisoft.analysis.command.sourceView");
		if (viewID != null)
		{
			ImageExplorerView view = (ImageExplorerView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
			ArrayList<String> selectionList = view.getSelection();
//			Iterator<String> iter = selectionList.iterator();
//			while (iter.hasNext()) {
//				System.out.println(iter.next());
//			}
			view.pushSelectedFiles(selectionList);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}
