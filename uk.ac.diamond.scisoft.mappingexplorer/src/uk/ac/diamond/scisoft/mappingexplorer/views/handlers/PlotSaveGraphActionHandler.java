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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.PlotExportUtil;
import uk.ac.diamond.scisoft.mappingexplorer.views.IDatasetPlotterContainingView;

/**
 * @author rsr31645
 * 
 */
public class PlotSaveGraphActionHandler extends AbstractHandler {

	private String filename;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IDatasetPlotterContainingView) {
			IDatasetPlotterContainingView dv = (IDatasetPlotterContainingView) activePart;
			DataSetPlotter dataSetPlotter = dv.getDataSetPlotter();
			if (dataSetPlotter != null) {
				Shell shell = HandlerUtil.getActiveShell(event);
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);

				String[] filterExtensions = new String[] {
						"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps",
						"*.svg;*.SVG" };
				if (filename != null) {
					dialog.setFilterPath((new File(filename)).getParent());
				} else {
					String filterPath = "/";
					String platform = SWT.getPlatform();
					if (platform.equals("win32") || platform.equals("wpf")) {
						filterPath = "c:\\";
					}
					dialog.setFilterPath(filterPath);
				}
				dialog.setFilterNames(PlotExportUtil.FILE_TYPES);
				dialog.setFilterExtensions(filterExtensions);
				filename = dialog.open();
				if (filename == null) {
					return null;
				}

				dataSetPlotter.saveGraph(filename,
						PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		}
		return null;
	}

}
