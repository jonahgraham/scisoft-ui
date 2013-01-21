/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.scisoft.mappingexplorer.views.actions;

import org.eclipse.jface.action.Action;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;

/**
 * @author rsr31645
 * 
 */
public class ZoomAction extends Action {

	private DataSetPlotter dataSetPlotter;

	public ZoomAction() {
		super("Zoom");
		setToolTipText("Zoom action");
		setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("/icons/minify.png"));
	}

	public void setDataSetPlotter(DataSetPlotter dataSetPlotter) {
		this.dataSetPlotter = dataSetPlotter;
	}

	@Override
	public void run() {
		super.run();
		dataSetPlotter.undoZoom();
	}

}