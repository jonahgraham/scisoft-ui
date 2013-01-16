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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDMappingView;

/**
 * @author rsr31645
 */
public class OpenColourMappingViewDefaultHandler extends AbstractHandler {

	private static final String COMMON_VIEW = "CommonView";
	private static final Logger logger = LoggerFactory.getLogger(OpenColourMappingViewDefaultHandler.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands. ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof TwoDMappingView) {

			TwoDMappingView twoDMappingView = (TwoDMappingView) activePart;
			String secondaryId = twoDMappingView.getViewSite().getSecondaryId();
			HistogramView histogramView = null;

			if (secondaryId != null) {
				try {
					histogramView = (HistogramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(HistogramView.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					logger.error("Problem opening colour mapping view {}", e);
				}
			} else {
				try {
					histogramView = (HistogramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(HistogramView.ID, COMMON_VIEW, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					logger.error("Problem opening colour mapping view {}", e);
				}
			}

			if (histogramView != null) {
				HistogramDataUpdate histogramDataUpdate = twoDMappingView.getHistogramDataUpdate();
				if (histogramDataUpdate != null) {
					histogramView.setData(histogramDataUpdate);
				}
				histogramView.addIObserver(twoDMappingView);

			}
		}
		return null;
	}
}
