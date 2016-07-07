/*-
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.imagegrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.FileOperationBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.utils.ImageThumbnailLoader;
import uk.ac.diamond.scisoft.imagegrid.gridentry.AbstractGridEntry;
import uk.ac.diamond.scisoft.imagegrid.thumbnail.ThumbnailLoadService;


/**
 * ThumbnailLoadService with plot server
 */
public class PlotServerThumbnailLoadService extends ThumbnailLoadService implements IObserver {
	transient private static final Logger logger = LoggerFactory.getLogger(PlotServerThumbnailLoadService.class);
	
	private boolean localProcessing;
	private PlotServer plotServer;
	private ArrayList<String> files = new ArrayList<String>();
	private AbstractGridEntry currentProcessEntry = null;
	private String viewName;
	
	public PlotServerThumbnailLoadService(String viewName) {
		super();
		plotServer = PlotServerProvider.getPlotServer();
		try {
			setLocalProcessing(plotServer.isServerLocal());
		} catch (Exception e) {
			// cannot happen but is needed for interface
		}
		this.viewName = viewName;
	}

	private void undoBlock() {
		locker.release();
	}

	@SuppressWarnings("unused")
	private void requestImageFromServer(AbstractGridEntry entry) {
		GuiBean bean = new GuiBean();
		files.clear();
		FileOperationBean fopBean = new FileOperationBean(FileOperationBean.GETIMAGEFILE_THUMB);
		files.add(entry.getFilename());
		fopBean.setFiles(files);
		bean.put(GuiParameters.FILEOPERATION, fopBean);
		try {
			plotServer.updateGui(viewName, bean);
		} catch (Exception e) {
			locker.release();
			e.printStackTrace();
		}
	}

	@Override
	protected void loadAndCreateThumbnailImage(AbstractGridEntry entry) {
		IDataset ds = ImageThumbnailLoader.loadImage(entry.getFilename(), entry.getAdditionalInfo(), true, false);
		entry.createImage(ds);
		locker.release();
	}

	@Override
	public void update(Object source, Object changeCode) {
		if (changeCode instanceof String && 
			changeCode.equals(viewName)) {
			DataBean dbPlot;
			try {
				dbPlot = plotServer.getData(viewName);
				Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
				Iterator<DatasetWithAxisInformation > iter = plotData.iterator();
				while (iter.hasNext())
				{
					DatasetWithAxisInformation dsAxis = iter.next();
					Dataset ds = dsAxis.getData();
					if (ds.getName().equals(currentProcessEntry.getFilename()))
					{
						currentProcessEntry.createImage(ds);
					} else {
						logger.error("Oops, no match between dataset {} and current entry {}", ds.getName(),
								currentProcessEntry.getFilename());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				undoBlock();
			}
		}
	}

	public boolean isLocalProcessing() {
		return localProcessing;
	}

	public void setLocalProcessing(boolean localProcessing) {
		this.localProcessing = localProcessing;
	}
}
