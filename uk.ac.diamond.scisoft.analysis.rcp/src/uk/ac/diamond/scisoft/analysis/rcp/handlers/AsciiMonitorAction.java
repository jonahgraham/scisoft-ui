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
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;

/**
 *
 */
public class AsciiMonitorAction extends AbstractHandler {

	/**
	 * 
	 */
	public final static String ID = "uk.ac.diamond.scisoft.analysis.rcp.monitorAscii";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
		     final AsciiTextView txt = (AsciiTextView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(AsciiTextView.ID);
		     txt.toggleMonitor();
		     return Boolean.TRUE;
		} catch (Exception ne) {
			return Boolean.FALSE;
		}
	}

}
