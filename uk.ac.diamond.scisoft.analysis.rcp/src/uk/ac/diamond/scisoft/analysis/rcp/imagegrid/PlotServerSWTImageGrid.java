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
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.FileOperationBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.views.ImageExplorerView;
import uk.ac.diamond.scisoft.imagegrid.SWTImageGrid;
import uk.ac.diamond.scisoft.imagegrid.gridentry.GridEntryMonitor;

/**
 * An SWTImageGrid that uses the plot server/guibean mechanism
 */
public class PlotServerSWTImageGrid extends SWTImageGrid {

	private static final Logger logger = LoggerFactory.getLogger(PlotServerSWTImageGrid.class);

	private static final String DEFAULTPLOTVIEW = "Dataset Plot";
	private List<String> plotViews; 
	private String viewName = null;
	private boolean usePlotServer = true;

	public PlotServerSWTImageGrid(Canvas canvas, String viewname) {
		super(canvas);
		this.viewName = viewname;
	}

	public PlotServerSWTImageGrid(int width, int height,Canvas canvas, String viewname) {
		super(width, height, canvas);
		this.viewName = viewname;
	}

	@Override
	protected void setupGrid() {
		super.setupGrid();
		// Simple system property to get files selected opened in an editor rather than
				// send to the plot server and plotted.
				// NOTE This must be a JAVA property and not a GDA property as programs outside
				// GDA are setting the property.
		if (System.getProperty("uk.ac.diamond.scisoft.analysis.rcp.imagegrid.plotServer")!=null) {
			usePlotServer = false;
		}
		plotViews = ImageExplorerView.getRegisteredViews();
		if (plotViews.size() == 0)
			plotViews.add(DEFAULTPLOTVIEW);
		Iterator<String> iter = plotViews.iterator();
		while (iter.hasNext()) {
			String viewName = iter.next();
			MenuItem item = new MenuItem(popupMenu, SWT.PUSH);
			item.setText(viewName);
			item.addSelectionListener(this);
		}
	}

	@Override
	protected void setGridEntryMonitor() {
		Rectangle rect = canvas.getClientArea();
		int maxNumImagesInMemory = MAXMEMORYUSAGE / (MAXTHUMBWIDTH * MAXTHUMBHEIGHT * 4);
		int visWidth = rect.width / MINTHUMBWIDTH;
		int visHeight = rect.height / MINTHUMBHEIGHT;
		monitor = new GridEntryMonitor(this, visWidth, visHeight, maxNumImagesInMemory, new PlotServerThumbnailLoadService(viewName));
	}

	@Override
	protected void sendOffLoadRequest(List<String> files, String plotViewName) {
		if (usePlotServer) {
//			List<String> files = new ArrayList<String>();
//			files.add(filename);
			GuiBean fileLoadBean = null;
			try {
				fileLoadBean = PlotServerProvider.getPlotServer().getGuiState(viewName);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (fileLoadBean == null)
				fileLoadBean = new GuiBean();
			FileOperationBean fopBean = new FileOperationBean(FileOperationBean.GETIMAGEFILE);
			fopBean.setFiles(files);
			fileLoadBean.put(GuiParameters.FILEOPERATION, fopBean);
			fileLoadBean.put(GuiParameters.DISPLAYFILEONVIEW, plotViewName);
			try {
				PlotServerProvider.getPlotServer().updateGui(viewName, fileLoadBean);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else { // Just tell RCP to open the file, the editor should be there for it
			try {
				EclipseUtils.openExternalEditor(files.get(0));
			} catch (PartInitException e) {
				logger.error("Cannot open "+files.get(0), e);
			}
//			File fileToOpen = new File(files.get(0));
//			 
//			if (fileToOpen.exists() && fileToOpen.isFile()) {
//			    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
//			    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//			 
//			    try {
//			        IDE.openEditorOnFileStore( page, fileStore );
//			    } catch ( PartInitException e ) {
//			        //Put your exception handler here if you wish to
//					logger.error("Cannot open "+files.get(0), e);
//			    }
//			}
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (!overviewWindow) {
			String filenameToLoad = determineFileToLoad();
			if (filenameToLoad != null) {
				ArrayList<String> files = new ArrayList<String>();
				files.add(filenameToLoad);
				sendOffLoadRequest(files, plotViews.get(0));
//				System.err.println("Filename "+filenameToLoad);
			}
		}
	}
}
