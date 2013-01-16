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
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.mappingexplorer.views.IDatasetPlotterContainingView;

/**
 * @author rsr31645
 * 
 */
public class PlotRegionZoomActionHandler extends AbstractHandler {

	private static final String PLOT_ZOOM_AREA_COMMAND_ID = "uk.ac.diamond.scisoft.analysis.rcp.PlotAreaZoomAction";
	private static final Logger logger = LoggerFactory
			.getLogger(PlotRegionZoomActionHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent executionEvent)
			throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(executionEvent);
		if (activePart instanceof IDatasetPlotterContainingView) {

			IDatasetPlotterContainingView dv = (IDatasetPlotterContainingView) activePart;

			// gives the toggled state of the button
			boolean isEnabled = ((ToolItem) ((Event) executionEvent
					.getTrigger()).widget).getSelection();

			DataSetPlotter dataSetPlotter = dv.getDataSetPlotter();
			if (dataSetPlotter != null) {
				setPlotAreaDisabled(dv, isEnabled, executionEvent);

				dataSetPlotter.setZoomEnabled(isEnabled);
				dataSetPlotter.setZoomMode(true);
				dataSetPlotter.setPlotUpdateOperation(isEnabled);

			} else {
				if (isEnabled) {
					((ToolItem) ((Event) executionEvent.getTrigger()).widget)
							.setSelection(false);
				}
			}

		}
		return null;
	}

	/**
	 * @param dv
	 * @param isEnabled
	 * @param executionEvent
	 * @throws ExecutionException
	 */
	protected void setPlotAreaDisabled(IDatasetPlotterContainingView dv,
			boolean isEnabled, ExecutionEvent executionEvent)
			throws ExecutionException {
		if (isEnabled) {
			Object service = dv.getViewSite().getService(IHandlerService.class);
			ToolBar toolBar = ((ToolItem) ((Event) executionEvent.getTrigger()).widget)
					.getParent();
			ToolItem toolItemDisabled = null;
			for (ToolItem toolItem : toolBar.getItems()) {
				Object data = toolItem.getData();
				if (data instanceof CommandContributionItem) {
					CommandContributionItem cmdContribItem = (CommandContributionItem) data;
					if (PLOT_ZOOM_AREA_COMMAND_ID.equals(cmdContribItem
							.getCommand().getId())) {
						if (toolItem.getSelection()) {
							toolItem.setSelection(false);
							toolItemDisabled = toolItem;
						}
						break;
					}
				}
			}
			if (toolItemDisabled != null) {
				if (service instanceof IHandlerService) {
					IHandlerService hs = (IHandlerService) service;
					Event event = new Event();
					event.widget = toolItemDisabled;

					try {
						hs.executeCommand(PLOT_ZOOM_AREA_COMMAND_ID, event);
					} catch (NotDefinedException e) {
						logger.error("NotDefinedException", e);
					} catch (NotEnabledException e) {
						logger.error("NotEnabledException", e);
					} catch (NotHandledException e) {
						logger.error("NotHandledException", e);
					}
				}
			}
		}
	}
}
