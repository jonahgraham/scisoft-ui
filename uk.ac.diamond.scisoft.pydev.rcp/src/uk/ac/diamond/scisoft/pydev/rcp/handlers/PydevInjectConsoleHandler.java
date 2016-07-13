/*-
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.pydev.rcp.handlers;

import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.python.rpc.action.InjectPyDevConsole;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

public class PydevInjectConsoleHandler extends AbstractHandler {

	protected String viewname;

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get viewname
		IWorkbenchPage page = EclipseUtils.getActivePage();
		IWorkbenchPart part = page.getActivePart();
		viewname = part.getTitle();
		// get parameters
		Map<String, String> params = getParameters();
		if (params == null)
			params = event.getParameters();
		InjectPyDevConsole connection = new InjectPyDevConsole(params);
		// inject data if needed
		injectData(connection, part);
		connection.open(false);
		return null;
	}

	/**
	 * Inject Data if needed : to be overridden
	 */
	public void injectData(InjectPyDevConsole console, IWorkbenchPart part) {
		
	}

	/**
	 * To be overridden to set particular parameters when running InjectPydevConsole handler
	 * @return
	 */
	public Map<String, String> getParameters() {
		return null;
	}

}
