/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.views;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

public class OpenFileNavigatorHandler extends AbstractHandler {

	private static int secondaryId = 1;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
			//final FileView parent   = (FileView)EclipseUtils.getActivePage().getActivePart();	
			final IViewPart view = EclipseUtils.getActivePage().showView(FileView.ID, FileView.ID+secondaryId, IWorkbenchPage.VIEW_CREATE);
			final IFileView fileView = (IFileView)view;
			secondaryId++;
			//TODO fileView.setRoot(parent.getSelectedFile());
			EclipseUtils.getActivePage().activate(view);
	        return Boolean.TRUE;
        } catch (Exception ne) {
        	throw new ExecutionException("Cannot open file navigator part!", ne);
        }
	}

}
