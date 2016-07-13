/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.pydev.rcp.handlers;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.python.rpc.action.InjectPyDevConsole;

public class PydevInjectCreateNewConsoleHandler extends PydevInjectConsoleHandler {

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(InjectPyDevConsole.CREATE_NEW_CONSOLE_PARAM, Boolean.TRUE.toString());
		params.put(InjectPyDevConsole.VIEW_NAME_PARAM, viewname);
		params.put(InjectPyDevConsole.SETUP_SCISOFTPY_PARAM,
				InjectPyDevConsole.SetupScisoftpy.ALWAYS.toString());
		return params;
	}
}
