/*-
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.imagegrid;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.imagegrid.gridentry.AbstractGridEntry;
import uk.ac.diamond.scisoft.imagegrid.gridentry.GridEntryMonitor;
import uk.ac.diamond.scisoft.imagegrid.gridentry.SWTGridEntry;
import uk.ac.diamond.scisoft.imagegrid.thumbnail.ThumbnailLoadService;

/**
 *
 */
public class SWTImageGrid extends AbstractImageGrid
		implements PaintListener, Listener, SelectionListener, KeyListener, MouseListener, MouseMoveListener {

	private static final Logger logger = LoggerFactory.getLogger(SWTImageGrid.class);

	protected static final int GRIDXGAPINPIXELS = 5;
	protected static final int GRIDYGAPINPIXELS = 5;
	protected static final int CTRL_MASK = (1 << 2);
	protected static final int SHIFT_MASK = (1 << 3);
	protected Canvas canvas;
	protected ScrollBar vBar;
	protected ScrollBar hBar;
	protected Color background = null;
	protected Color white = null;
	protected Color green = null;
	protected Color red = null;
	protected Color blue = null;
	protected int scrollX = 0;
	protected int scrollY = 0;
	protected int mouseX = 0;
	protected int mouseY = 0;
	protected int currentTileWidth = 0;
	protected int currentTileHeight = 0;
	protected int thumbSize;
	protected int mouseButtonMode = 0;
	protected boolean visualizeCache = false;
	protected boolean overviewWindow = false;
	protected SWTGridEntry toolTipEntry = null;
	protected String imageFileToLoad = null;
	protected Menu popupMenu = null;

	public SWTImageGrid(Canvas canvas) {
		super();
		this.canvas = canvas;
		setupGrid();
		setGridEntryMonitor();
	}

	public SWTImageGrid(int width, int height, Canvas canvas) {
		super(width, height);
		this.canvas = canvas;
		setupGrid();
		setGridEntryMonitor();
	}

	protected void setupGrid() {
		this.canvas.addPaintListener(this);
		this.canvas.addListener(SWT.Resize, this);
		this.canvas.addKeyListener(this);
		this.canvas.addMouseListener(this);
		this.canvas.addMouseMoveListener(this);
		vBar = canvas.getVerticalBar();
		hBar = canvas.getHorizontalBar();
		vBar.addSelectionListener(this);
		hBar.addSelectionListener(this);
		popupMenu = new Menu(canvas.getShell(), SWT.POP_UP);
	}

	protected void setGridEntryMonitor() {
		Rectangle rect = canvas.getClientArea();
		int maxNumImagesInMemory = MAXMEMORYUSAGE / (MAXTHUMBWIDTH * MAXTHUMBHEIGHT * 4);
		int visWidth = rect.width / MINTHUMBWIDTH;
		int visHeight = rect.height / MINTHUMBHEIGHT;
		monitor = new GridEntryMonitor(this, visWidth, visHeight, maxNumImagesInMemory, new ThumbnailLoadService());
	}

	@Override
	public void displayGrid() {
		// Nothing to do

	}

	private void displayWindowOverview(GC gc) {
		Rectangle client = canvas.getClientArea();
		float unitWidth = (float) (client.width - vBar.getSize().x) / (float) gridWidth;
		float unitHeight = (float) (client.height - hBar.getSize().y) / (float) gridHeight;
		gc.setBackground(white);
		for (int y = 0; y < gridHeight; y++)
			for (int x = 0; x < gridWidth; x++)
				if (table[x + y * gridWidth] != null) {
					gc.drawRectangle((int) (x * unitWidth) - 1, (int) (y * unitHeight) - 1, 3, 3);
				}
		float numVisX = (float) (client.width) / (float) (currentTileWidth + GRIDXGAPINPIXELS);
		float numVisY = (float) (client.height) / (float) (currentTileHeight + GRIDYGAPINPIXELS);

		if (green == null) {
			green = new Color(canvas.getDisplay(), new RGB(0, 255, 0));
		}
		gc.setBackground(green);
		gc.setAlpha(128);
		gc.fillRectangle((int) ((-scrollX / currentTileWidth) * unitWidth),
				(int) ((-scrollY / currentTileHeight) * unitHeight), (int) ((numVisX) * unitWidth),
				(int) ((numVisY - 1) * unitHeight));

	}

	private void displayCaches(GC gc) {
		Rectangle client = canvas.getClientArea();

		java.awt.Rectangle primaryCache = null, secondaryCache = null;
		if (monitor != null) {
			primaryCache = monitor.getPrimaryCacheArea();
			secondaryCache = monitor.getSecondaryCacheArea();
		}
		float unitWidth = (float) client.width / (float) gridWidth;
		float unitHeight = (float) client.height / (float) gridHeight;
		if (green == null) {
			green = new Color(canvas.getDisplay(), new RGB(0, 255, 0));
		}
		if (red == null) {
			red = new Color(canvas.getDisplay(), new RGB(255, 0, 0));
		}
		gc.setAlpha(255);
		gc.setBackground(white);
		for (int y = 0; y < gridHeight; y++)
			for (int x = 0; x < gridWidth; x++)
				if (table[x + y * gridWidth] != null) {
					if (((SWTGridEntry) table[x + y * gridWidth]).hasImage())
						gc.fillOval((int) (x * unitWidth), (int) (y * unitHeight), 2, 2);
					else
						gc.drawOval((int) (x * unitWidth), (int) (y * unitHeight), 2, 2);
				}
		gc.setAlpha(128);

		gc.setBackground(green);

		if (secondaryCache != null) {
			gc.fillRectangle((int) (secondaryCache.x * unitWidth), (int) (secondaryCache.y * unitHeight),
					(int) (secondaryCache.width * unitWidth), (int) (secondaryCache.height * unitHeight));
		}
		gc.setBackground(red);

		if (primaryCache != null) {
			gc.fillRectangle((int) (primaryCache.x * unitWidth), (int) (primaryCache.y * unitHeight),
					(int) (primaryCache.width * unitWidth), (int) (primaryCache.height * unitHeight));
		}
	}

	@Override
	public void setThumbnailSize(int size) {
		thumbSize = size;
	}

	@Override
	protected void resizeGrid(int newWidth, int newHeight) {
		super.resizeGrid(newWidth, newHeight);
		canvas.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				Rectangle client = canvas.getClientArea();
				// int tileWidth = client.width / gridWidth;
				// int tileHeight = client.height / gridHeight;
				// thumbSize = Math.min(tileWidth, tileHeight);
				// tileWidth = Math.min(thumbSize,MAXTHUMBWIDTH);
				// tileHeight = Math.min(thumbSize,MAXTHUMBHEIGHT);
				currentTileWidth = Math.max(thumbSize, MINTHUMBWIDTH);
				currentTileHeight = Math.max(thumbSize, MINTHUMBHEIGHT);
				currentTileWidth = Math.min(currentTileWidth, MAXTHUMBWIDTH);
				currentTileHeight = Math.min(currentTileHeight, MAXTHUMBHEIGHT);
				vBar.setMinimum(0);
				hBar.setMinimum(0);
				vBar.setIncrement(currentTileHeight + GRIDYGAPINPIXELS);
				hBar.setIncrement(currentTileWidth + GRIDXGAPINPIXELS);
				int deltaX = (currentTileWidth + GRIDXGAPINPIXELS) * gridWidth - client.width;
				int deltaY = (currentTileHeight + GRIDYGAPINPIXELS) * gridHeight - client.height;
				hBar.setMaximum(Math.max(deltaX, 1));
				vBar.setMaximum(Math.max(deltaY, 1));
			}
		});
	}

	@Override
	public void paintControl(PaintEvent e) {
		GC canvasGc = e.gc;
		if (background == null) {
			background = new Color(canvas.getDisplay(), new RGB(64, 64, 64));
			white = new Color(canvas.getDisplay(), new RGB(255, 255, 255));
			blue = new Color(canvas.getDisplay(), new RGB(0, 0, 255));
		}
		Rectangle client = canvas.getClientArea();
		canvasGc.setAlpha(255);
		canvasGc.setForeground(white);
		canvasGc.setBackground(background);
		canvasGc.fillRectangle(client);
		if (currentTileWidth == 0 || currentTileHeight == 0) {
			// int tileWidth = client.width / gridWidth;
			// int tileHeight = client.height / gridHeight;
			// int thumbSize = Math.min(tileWidth, tileHeight);
			// tileWidth = Math.min(thumbSize,MAXTHUMBWIDTH);
			// tileHeight = Math.min(thumbSize,MAXTHUMBHEIGHT);

			currentTileWidth = Math.max(thumbSize, MINTHUMBWIDTH);
			currentTileHeight = Math.max(thumbSize, MINTHUMBHEIGHT);
			currentTileWidth = Math.min(currentTileWidth, MAXTHUMBWIDTH);
			currentTileHeight = Math.min(currentTileHeight, MAXTHUMBHEIGHT);
			vBar.setMinimum(0);
			hBar.setMinimum(0);
			vBar.setIncrement(currentTileHeight + GRIDYGAPINPIXELS);
			hBar.setIncrement(currentTileWidth + GRIDXGAPINPIXELS);
			int deltaX = (currentTileWidth + GRIDXGAPINPIXELS) * gridWidth - client.width;
			int deltaY = (currentTileHeight + GRIDYGAPINPIXELS) * gridHeight - client.height;
			hBar.setMaximum(Math.max(deltaX, 1));
			vBar.setMaximum(Math.max(deltaY, 1));
		}
		if (visualizeCache) {
			displayCaches(canvasGc);
			canvasGc.setBackground(background);
		} else if (overviewWindow) {
			displayWindowOverview(canvasGc);
		} else {
			int rectX = -1;
			int rectY = -1;
			toolTipEntry = null;
			synchronized (table) {
				for (int y = 0; y < gridHeight; y++) {
					for (int x = 0; x < gridWidth; x++) {
						if (table[x + y * gridWidth] != null) {
							currentTileWidth = Math.max(thumbSize, MINTHUMBWIDTH);
							currentTileHeight = Math.max(thumbSize, MINTHUMBHEIGHT);
							int xPos = scrollX + x * (currentTileWidth + GRIDXGAPINPIXELS);
							int yPos = scrollY + y * (currentTileHeight + GRIDYGAPINPIXELS);
							if (xPos > -currentTileWidth * 3 && xPos < canvas.getBounds().width + currentTileWidth * 3
									&& yPos > -currentTileHeight * 3
									&& yPos < canvas.getBounds().height + currentTileHeight * 3) {
								SWTGridEntry entry = (SWTGridEntry) table[x + y * gridWidth];
								if (mouseButtonMode == 1)
									entry.setStatus(0);

								if ((mouseButtonMode & SHIFT_MASK) == 0) {
									if (mouseX >= xPos && mouseX <= (xPos + currentTileWidth) && mouseY >= yPos
											&& mouseY <= (yPos + currentTileHeight)) {
										rectX = xPos;
										rectY = yPos;
										toolTipEntry = entry;
										if (mouseButtonMode > 0)
											entry.setStatus(AbstractGridEntry.SELECTEDSTATUS);
									}
								}

								if (entry != toolTipEntry) {
									entry.paint(canvasGc, xPos, yPos, currentTileWidth, currentTileHeight);
								}
							}
						}
					}
				}
			}
			if (toolTipEntry != null) {
				int width = currentTileWidth + ((currentTileWidth * 30) / 100);
				int height = currentTileHeight + ((currentTileHeight * 30) / 100);
				toolTipEntry.paint(canvasGc, rectX, rectY, width, height);
				canvasGc.setAlpha(128);
				canvasGc.setBackground(blue);
				canvasGc.fillRectangle(rectX, rectY, width, height);
			}
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Resize) {
			Rectangle rect = canvas.getClientArea();
			int visWidth = rect.width / MINTHUMBWIDTH;
			int visHeight = rect.height / MINTHUMBHEIGHT;
			if (monitor != null)
				monitor.resizeDisplayArea(visWidth, visHeight);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(vBar) || e.getSource().equals(hBar)) {
			scrollX = -hBar.getSelection();
			scrollY = -vBar.getSelection();
			int deltaX = Math.round((float) -scrollX / (float) currentTileWidth);
			int deltaY = Math.round((float) -scrollY / (float) currentTileHeight);
			if (monitor != null)
				monitor.updateMonitorPosition(deltaX, deltaY);
			canvas.redraw();
		} else if (e.getSource() instanceof MenuItem) {
			if (imageFileToLoad != null) {
				// check if there is a selection if yes use that
				// otherwise just the single selected file
				ArrayList<String> files = getSelection();
				if (files == null || files.size() == 0) {
					files = new ArrayList<String>();
					files.add(imageFileToLoad);
				}
				sendOffLoadRequest(files, ((MenuItem) e.getSource()).getText());
			}
		}

	}

	@Override
	public void dispose() {
		nextEntryX = nextEntryY = 0;
		for (int y = 0; y < gridHeight; y++)
			for (int x = 0; x < gridWidth; x++) {
				AbstractGridEntry entry = table[x + y * gridWidth];
				if (entry != null)
					entry.dispose();
				table[x + y * gridWidth] = null;
			}
		monitor.dispose();
		monitor = null;
		if (blue != null) {
			blue.dispose();
			blue = null;
		}
		if (green != null) {
			green.dispose();
			green = null;
		}
		if (red != null) {
			red.dispose();
			red = null;
		}
		if (background != null) {
			background.dispose();
			background = null;
		}
		if (white != null) {
			white.dispose();
			white = null;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Nothing to do

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.character == 'v' || e.character == 'V') {
			visualizeCache = !visualizeCache;
			canvas.redraw();
		}
		if (e.character == 'o' || e.character == 'O') {
			overviewWindow = !overviewWindow;
			canvas.redraw();
		}
	}

	protected String determineFileToLoad() {
		String filenameToLoad = null;
		synchronized (table) {
			for (int y = 0; y < gridHeight; y++) {
				for (int x = 0; x < gridWidth; x++) {
					if (table[x + y * gridWidth] != null) {
						SWTGridEntry entry = (SWTGridEntry) table[x + y * gridWidth];
						int xPos = scrollX + x * (currentTileWidth + GRIDXGAPINPIXELS);
						int yPos = scrollY + y * (currentTileHeight + GRIDYGAPINPIXELS);
						if (mouseButtonMode == 1)
							entry.setStatus(0);

						if ((mouseButtonMode & SHIFT_MASK) == 0) {
							if (mouseX >= xPos && mouseX <= (xPos + currentTileWidth) && mouseY >= yPos
									&& mouseY <= (yPos + currentTileHeight)) {
								filenameToLoad = entry.getFilename();
								break;
							}
						}
					}
				}
			}
		}
		return filenameToLoad;
	}

	protected void sendOffLoadRequest(List<String> files, String plotViewName) {
		try {
			EclipseUtils.openExternalEditor(files.get(0));
		} catch (PartInitException e) {
			logger.error("Cannot open " + files.get(0), e);
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (!overviewWindow) {
			String filenameToLoad = determineFileToLoad();
			if (filenameToLoad != null) {
				ArrayList<String> files = new ArrayList<String>();
				files.add(filenameToLoad);
			}
		}
	}

	private void shiftSelect() {
		int rectX = -1;
		int rectY = -1;
		int prevRectX = 0;
		int prevRectY = 0;
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (table[x + y * gridWidth] != null) {
					SWTGridEntry entry = (SWTGridEntry) table[x + y * gridWidth];
					int xPos = scrollX + x * (currentTileWidth + GRIDXGAPINPIXELS);
					int yPos = scrollY + y * (currentTileHeight + GRIDYGAPINPIXELS);
					if (mouseX >= xPos && mouseX <= (xPos + currentTileWidth) && mouseY >= yPos
							&& mouseY <= (yPos + currentTileHeight)) {
						rectX = x;
						rectY = y;
						entry.setStatus(AbstractGridEntry.SELECTEDSTATUS);
					} else {
						if (entry.getStatus() == AbstractGridEntry.SELECTEDSTATUS) {
							if ((rectX == -1 && rectY == -1) || (prevRectX == 0 && prevRectY == 0)) {
								prevRectX = x;
								prevRectY = y;
							}
						}
					}
				}
			}
		}
		int prevEntry = prevRectX + prevRectY * gridWidth;
		int maxEntry = rectX + rectY * gridWidth;
		if (maxEntry < prevEntry) {
			int temp = maxEntry;
			maxEntry = prevEntry;
			prevEntry = temp;
		}
		int endEntry = gridWidth * gridHeight;
		if (rectX != -1 && rectY != -1) {
			for (int listNr = prevEntry; listNr < maxEntry; listNr++)
				if (table[listNr] != null) {
					SWTGridEntry entry = (SWTGridEntry) table[listNr];
					entry.setStatus(AbstractGridEntry.SELECTEDSTATUS);
				}
			if ((mouseButtonMode & CTRL_MASK) != CTRL_MASK) {
				for (int listNr = 0; listNr < prevEntry; listNr++)
					if (table[listNr] != null) {
						SWTGridEntry entry = (SWTGridEntry) table[listNr];
						entry.setStatus(0);
					}

				for (int listNr = maxEntry + 1; listNr < endEntry; listNr++)
					if (table[listNr] != null) {
						SWTGridEntry entry = (SWTGridEntry) table[listNr];
						entry.setStatus(0);
					}
			}
		}
	}

	private void doRightMouseButton() {
		imageFileToLoad = determineFileToLoad();
		if (imageFileToLoad != null)
			popupMenu.setVisible(true);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (!overviewWindow) {
			if (e.button == 1) {
				mouseButtonMode = 1;
				if ((e.stateMask & SWT.CTRL) != 0) {
					mouseButtonMode += CTRL_MASK;
				}
				if ((e.stateMask & SWT.SHIFT) != 0) {
					mouseButtonMode += SHIFT_MASK;
					shiftSelect();
				}
			} else if (e.button == 3) {
				doRightMouseButton();
			}
		} else {
			Rectangle client = canvas.getClientArea();
			float unitWidth = (float) (client.width - vBar.getSize().x) / (float) gridWidth;
			float unitHeight = (float) (client.height - hBar.getSize().y) / (float) gridHeight;
			float numVisX = (float) (client.width) / (float) (currentTileWidth + GRIDXGAPINPIXELS);
			float numVisY = (float) (client.height - hBar.getSize().y) / (float) (currentTileHeight + GRIDYGAPINPIXELS);
			float posX = e.x / unitWidth;
			float posY = e.y / unitHeight;
			posX -= numVisX * 0.5f;
			posY -= numVisY * 0.5f;

			if (posX + numVisX >= gridWidth)
				posX -= (posX + numVisX) - gridWidth;
			if (posY + numVisY >= gridHeight)
				posY -= (posY + numVisY) - gridHeight;

			if (posX < 0.0)
				posX = 0.0f;
			if (posY < 0.0)
				posY = 0.0f;
			if (monitor != null)
				monitor.updateMonitorPosition(Math.round(posX), Math.round(posY));
			scrollX = -(int) (posX * (currentTileWidth + GRIDXGAPINPIXELS));
			scrollY = -(int) (posY * (currentTileHeight + GRIDYGAPINPIXELS));
			vBar.setSelection(-scrollY);
			hBar.setSelection(-scrollX);
		}
		canvas.redraw();
	}

	@Override
	public void mouseUp(MouseEvent e) {
		mouseButtonMode = 0;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		mouseX = e.x;
		mouseY = e.y;
		canvas.redraw();
		if (toolTipEntry != null && !overviewWindow) {
			canvas.setToolTipText(toolTipEntry.getToolTipText());
		} else
			canvas.setToolTipText("");
	}

	@Override
	public ArrayList<String> getSelection() {
		ArrayList<String> selection = new ArrayList<String>();
		synchronized (table) {
			for (int y = 0; y < gridHeight; y++)
				for (int x = 0; x < gridWidth; x++)
					if (table[x + y * gridWidth] != null) {
						SWTGridEntry entry = (SWTGridEntry) table[x + y * gridWidth];
						if (entry.getStatus() == AbstractGridEntry.SELECTEDSTATUS)
							selection.add(entry.getFilename());
					}
		}
		return selection;
	}

	@Override
	public void setOverviewMode(boolean overview) {
		overviewWindow = overview;

	}

	@Override
	public boolean getOverviewMode() {
		return overviewWindow;
	}

	@Override
	public void stopLoading() {
		if (monitor != null)
			monitor.stopLoading();
	}

}
