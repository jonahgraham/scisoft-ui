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

package uk.ac.diamond.scisoft.mappingexplorer.views.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.mappingexplorer.views.IDatasetPlotterContainingView;

/**
 * Action handler for "Monitor img position"
 * 
 * @author rsr31645
 */
public class MonitorImagePosDefaultHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(executionEvent);
		if (activePart instanceof IDatasetPlotterContainingView) {

			IDatasetPlotterContainingView dv = (IDatasetPlotterContainingView) activePart;

			// gives the toggled state of the button
			boolean isEnabled = ((ToolItem) ((Event) executionEvent.getTrigger()).widget).getSelection();

			DataSetPlotter dataSetPlotter = dv.getDataSetPlotter();
			if (dataSetPlotter != null) {
				dataSetPlotter.setPlotActionEnabled(isEnabled);
			} else {
				if (isEnabled) {
					((ToolItem) ((Event) executionEvent.getTrigger()).widget).setSelection(false);
				}
			}
		}
		return null;
	}

}
