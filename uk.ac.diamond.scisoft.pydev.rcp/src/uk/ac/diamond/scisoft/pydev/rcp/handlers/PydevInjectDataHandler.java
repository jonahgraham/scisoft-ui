/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.pydev.rcp.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawnsci.python.rpc.action.InjectPyDevConsole;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PydevInjectDataHandler extends PydevInjectConsoleHandler {

	private static final Logger logger = LoggerFactory.getLogger(PydevInjectDataHandler.class);
	private LinkedHashMap<String, IDataset> data;

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(InjectPyDevConsole.SETUP_SCISOFTPY_PARAM, InjectPyDevConsole.SetupScisoftpy.ALWAYS.toString());
		params.put(InjectPyDevConsole.VIEW_NAME_PARAM, viewname);
		return params;
	}

	@Override
	public void injectData(InjectPyDevConsole connection, IWorkbenchPart part) {
		IPlottingSystem<?> system = part.getAdapter(IPlottingSystem.class);
		if (system != null) {
			Collection<ITrace> traces = system.getTraces();
			ITrace trace = !traces.isEmpty() ? traces.iterator().next() : null;
			IDataset d = trace.getData();
			String name = d.getName();
			if (name == null)
				name = trace.getName();
			try {
				setData(InjectPyDevConsole.getLegalVarName(name), d);
			} catch (Exception e) {
				logger.error("Cannot set data to use with inject, using name 'x' instead", e);
				setData("x", d);
			}
			try {
				connection.inject(data);
			} catch (Exception e) {
				logger.error("Error injecting data:" + e.getMessage());
			}
		}
	}

	/**
	 * Call this method to manually set the dataset which we should use to 
	 * send to the console. This data is currently sent using flattening.
	 * 
	 * This will clear other datasets already sent. To set more than one
	 * dataset at once, call setData(Map).
	 * 
	 * @param name
	 * @param data
	 */
	private void setData(String name, IDataset value) {
		if (this.data == null)
			data = new LinkedHashMap<String, IDataset>();
		data.clear();
		data.put(name, value);
	}
}
