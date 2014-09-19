/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

/**
 * Contains the constants used by the Jython Terminal configuration 
 */
public class PlotViewConstants {

	protected static final String CONFIGURATION_EXTENSION_POINT_ID = AnalysisRCPActivator.PLUGIN_ID + ".plotView"; //$NON-NLS-1$
	protected static final String PLOT_CONFIG = "plot_config"; //$NON-NLS-1$
	protected static final String ID = "id"; //$NON-NLS-1$
	protected static final String NAME = "name"; //$NON-NLS-1$
}
