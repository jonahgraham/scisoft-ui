/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;



/**
 * Contains all the configuration elements for a JythonTerminalView
 */
public class PlotViewConfig {
	private String name;
	
	/**
	 * Create a config for this Jython Terminal Configuration
	 * @param id The ID of the view that the config is for
	 */
	public PlotViewConfig(String id) {
		// cache the name as it isn't configurable by the preferences
		name = PlotViewRegistry.getDefault().getConfigs().get(id).name;
	
	}

	/**
	 * @return the name of the view
	 */
	public String getName() {
		return name;
	}


}
