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

public class PydevInjectGetGuiBeanHandler extends PydevInjectConsoleHandler {

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(InjectPyDevConsole.INJECT_COMMANDS_PARAM, "bean=dnp.plot.getbean('"+ viewname +"')");
		return params;
	}
}
