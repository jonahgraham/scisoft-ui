/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.imagegrid;

import java.awt.Rectangle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class GridEntryMonitor {

	private AbstractImageGrid grid;
	private Rectangle innerCacheArea;
	private Rectangle fullCacheArea;
	private Rectangle totalGridArea;
	private ExecutorService execSvc = null;	
	private ThumbnailLoadService service = null;
	private final static int BUFFERWIDTH = 10;
	private int totalImagesNumber = 0;
	
	public GridEntryMonitor(AbstractImageGrid grid,
							int width, int height, int numImages,
							String viewName) {
		this.grid = grid;
		this.totalImagesNumber = numImages;
		innerCacheArea = new Rectangle(0,0,width,height);
		totalGridArea = new Rectangle(0,0,grid.getGridWidth(),grid.getGridHeight());
	    float aspect = (float)grid.getGridWidth() / (float)grid.getGridHeight();
		int dim = (int)Math.sqrt(numImages);
	    int xDim = (int)(dim * aspect);
	    int yDim = (int)(dim * (1.0f/aspect));
	    if (xDim < width+BUFFERWIDTH) {
	    	xDim = width+BUFFERWIDTH;
	    	yDim = numImages / xDim;
	    }
		fullCacheArea = new Rectangle(0,0,xDim,yDim);
		
		execSvc = Executors.newFixedThreadPool(2);
		service = new ThumbnailLoadService(viewName);
		execSvc.execute(service);		
	}
	
	public void resizeDisplayArea(int newWidth, int newHeight) {
		innerCacheArea.setBounds((int)innerCacheArea.getX(), 
								 (int)innerCacheArea.getY(), 
								 newWidth, newHeight);
		recomputeTotalCacheArea(grid.getGridWidth(),grid.getGridHeight());
	}
	
	public void addEntry(AbstractGridEntry newEntry, int x, int y) {
		if (innerCacheArea.contains(x,y)) {
			service.addLoadJob(newEntry, true);
		} else {
			if (fullCacheArea.contains(x,y)) {
				service.addLoadJob(newEntry, false);
			}
		}
	}
	
	private Rectangle[] subtraction(Rectangle a, Rectangle b) {
		Rectangle c = a.intersection(b);
		// now overlap so return the second rectangle
		if (c.width == -1 && c.height == -1) {
			return new Rectangle[]{b};
		} 
		// easy case we only have one rectangle
		if (c.height == b.height) {
			// check from which side we need to subtract
			if (b.x < a.x) {
				c.setLocation(b.x,b.y);
				c.setSize(b.width-c.width, b.height);
			} else {
				c.setLocation(a.x+a.width+1,b.y);
				c.setSize(b.width-c.width, b.height);				
			}
			return new Rectangle[]{c};
		} else	if (c.width == b.width) {
			if (b.y < a.y) {
				c.setLocation(b.x,b.y);
				c.setSize(b.width, b.height - c.height);
			} else {
				c.setLocation(b.x,a.y+a.height);
				c.setSize(b.width,b.height - c.height);
			}
			return new Rectangle[]{c};	
		// more complicated case we have two separate rectangles	
		} else {
			if (b.x < a.x) {
				Rectangle d = new Rectangle(b.x,b.y,c.x-b.x,b.height);
				Rectangle e = null;
				if (b.y < a.y) {
					e = new Rectangle(c.x,b.y,c.width,c.y-b.y);
				} else {
					e = new Rectangle(c.x,c.y+c.height,c.width,b.height-c.height);
				}
				return new Rectangle[]{d,e};
			} 
			Rectangle d = new Rectangle(c.x+c.width,b.y,b.width-c.width,b.height);
			Rectangle e = null;
			if (b.y < a.y) {
				e = new Rectangle(c.x,b.y,c.width,b.height-c.height);
			} else {
				e = new Rectangle(c.x,b.y+c.height,c.width,b.height-c.height);
			}
			return new Rectangle[]{d,e};
		}
	}
	
	private void checkOnReloadPrimary() {
		for (int y = innerCacheArea.y; y < innerCacheArea.y+innerCacheArea.height; y++)
			for (int x = innerCacheArea.x; x < innerCacheArea.x+innerCacheArea.width; x++) {
					AbstractGridEntry entry = grid.getGridEntry(x, y);
					if (entry != null && entry.isDeactivated() && entry.getStatus() != AbstractGridEntry.INVALIDSTATUS)
						service.addLoadJob(entry, true);
		}		
	}
	
	private void recomputeTotalCacheArea(int width, int height)
    {
	    float aspect = (float)width / (float)height;
		int dim = (int)Math.sqrt(totalImagesNumber);
	    int xDim = (int)(dim * aspect);
	    int yDim = (int)(dim * (1.0f/aspect));
	    int minWidth = Math.min(innerCacheArea.width, width);
	    if (xDim < minWidth+BUFFERWIDTH) {
	    	xDim = minWidth+BUFFERWIDTH;
	    	yDim = totalImagesNumber / xDim;
	    }
		fullCacheArea.setBounds(fullCacheArea.x,fullCacheArea.y,xDim,yDim);	
    }
 	public void gridResize(int newWidth, int newHeight)
	{
		recomputeTotalCacheArea(newWidth, newHeight);
		totalGridArea.setBounds(0,0,newWidth,newHeight);
	}
	
	public void updateMonitorPosition(int x, int y) {
		innerCacheArea.setLocation(x, y);
		// Check if we are outside the fullCacheArea
//		if (!fullCacheArea.contains(innerCacheArea)) {
		service.clearHighPriorityQueue();
		checkOnReloadPrimary();
		
	//	}
		int cacheDistX = Math.min((fullCacheArea.width >> 1), fullCacheArea.width-innerCacheArea.width-2);
		int cacheDistY = Math.min((fullCacheArea.height >> 1), fullCacheArea.height-innerCacheArea.height-2);
		int fcx = x-cacheDistX;
		int fcy = y-cacheDistY;
		if (fcx < 0) fcx = 0;
		if (fullCacheArea.width >= totalGridArea.width) fcx = 0;
		if (fcy < 0) fcy = 0;
		if (fullCacheArea.height >= totalGridArea.height) fcy = 0;
		Rectangle newFullCacheArea = new Rectangle(fcx,fcy,fullCacheArea.width,fullCacheArea.height);
		if (!fullCacheArea.contains(newFullCacheArea)) {
			service.clearLowPriorityQueue();
			// compute out of cache areas
			Rectangle[] outAreas = subtraction(newFullCacheArea, fullCacheArea);
			// purge old entries from memory
			for (int i = 0; i < outAreas.length; i++) {
				Rectangle purgeArea = outAreas[i];
				for (int ry = 0; ry < purgeArea.height; ry++) {
					for (int rx = 0; rx < purgeArea.width; rx++) {
						int xPos = rx + purgeArea.x;
						int yPos = ry + purgeArea.y;
						AbstractGridEntry entry = grid.getGridEntry(xPos, yPos);
						if (entry != null)
							entry.deActivate();
					}
				}					
			}
			// compute new need to cache areas and load
			// entries
			for (int ry = 0; ry < newFullCacheArea.height; ry++) {
				for (int rx = 0; rx < newFullCacheArea.width; rx++) {
					int xPos = rx + newFullCacheArea.x;
					int yPos = ry + newFullCacheArea.y;
					if (!innerCacheArea.contains(xPos, yPos)) {
						AbstractGridEntry entry = grid.getGridEntry(xPos, yPos);
						if (entry != null && entry.isDeactivated()) {
							service.addLoadJob(entry, false);
						}
					}
				}
			}
		}
		fullCacheArea = newFullCacheArea;
	}
	
	public Rectangle getPrimaryCacheArea() {
		return innerCacheArea;
	}
	
	public Rectangle getSecondaryCacheArea() {
		return fullCacheArea;
	}
	
	public void dispose() {
		service.shutdown();
	}

	public void stopLoading() {
		service.clearLowPriorityQueue();
		service.clearHighPriorityQueue();
	}
}
