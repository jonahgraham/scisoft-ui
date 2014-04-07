/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.imagegrid;

import java.awt.Dimension;
import java.io.IOException;

import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.services.util.SWTImageUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;

/**
 * SWT Image implementation of a ImageGridEntry
 */
public class SWTGridEntry extends AbstractGridEntry {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(SWTGridEntry.class);
	private double loThreshold = 0.0;
	private double hiThreshold = 0.98;
	private static Color green = null;
	private static Color red = null;
	private static Color blue = null;
	private static long lastREDRAWinMillis = 0; 
	
	private Image gridImage;
	private Dimension imageDim;
	private Canvas canvas;
	private String toolTipText = null;
	@SuppressWarnings("unused")
	private int colourMapChoice = 0;
	private PaletteData paletteData;
	/**
	 * palette service used to retrieved the colour scheme
	 */
	private IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);

	public SWTGridEntry(String filename) {
		super(filename);
		gridImage = null;

	}

	public SWTGridEntry(String filename, Object additional) {
		super(filename,additional);
		gridImage = null;
	}

	public SWTGridEntry(String filename, Object additional, Canvas canvas,
			            int colourMapChoice, double loThreshold, double hiThreshold) {
		this(filename,additional);
		this.canvas = canvas;
		this.colourMapChoice = colourMapChoice;
		this.loThreshold = loThreshold;
		this.hiThreshold = hiThreshold;
	}

	public SWTGridEntry(String filename, Object additional, Canvas canvas, String colorScheme, double loThreshold,
			double hiThreshold) {
		this(filename, additional);
		this.canvas = canvas;
		this.paletteData = pservice.getDirectPaletteData(colorScheme);
		this.loThreshold = loThreshold;
		this.hiThreshold = hiThreshold;
	}

	@Override
	public void setNewfilename(String newFilename) {
		this.filename = newFilename;
		this.additionalInfo = null;
		if (gridImage != null)
			gridImage.dispose();
		if (thumbnailFilename != null)
		{
        	java.io.File imageFile = new java.io.File(thumbnailFilename);
        	imageFile.delete();
			thumbnailFilename = null;
		}
	}

	@Override
	public void setStatus(int newStatus) {
		status = newStatus;

	}

	@Override
	public void deActivate() {
		if (gridImage != null) {
			try {
				if (thumbnailFilename == null) {
					java.io.File file = java.io.File.createTempFile("tmp_thumb", ".png");
					String workspaceDir = org.eclipse.core.runtime.Platform.getInstanceLocation().getURL().getPath();
					if (SWT.getPlatform().equals("win32") ||
						SWT.getPlatform().equals("wpf"))
						workspaceDir = workspaceDir.substring(1);
					thumbnailFilename = workspaceDir +file.getName();
					file.delete();
					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[]{gridImage.getImageData()};
					loader.save(thumbnailFilename, SWT.IMAGE_PNG);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			gridImage.dispose();
			gridImage = null;
		}
	}

	public void loadThumbImage() {
		if (gridImage == null) {
			canvas.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					gridImage = new Image(canvas.getDisplay(),thumbnailFilename);
					// make sure system doesn't get flooded with redraw requests
					if (System.currentTimeMillis()-lastREDRAWinMillis > 20) {
						canvas.redraw();
						lastREDRAWinMillis = System.currentTimeMillis();
					}
				}				
			});
		} else {
			logger.warn("Something is wrong");
		}
	}
	
	@Override
	public boolean isDeactivated() {
		return gridImage == null;
	}

	@Override
	public void createImage(final IDataset ids) {
		
		final AbstractDataset ds = DatasetUtils.convertToAbstractDataset(ids);
		
		canvas.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int[] shape = ds.getShape();
				try {
					if (shape.length == 2) {
						double[] m;
						if (hiThreshold < 1) {
							if (loThreshold > 0) {
								if (ds instanceof RGBDataset) {
									RGBDataset rgb = (RGBDataset) ds;
									m = Stats.quantile(rgb.createRedDataset(AbstractDataset.INT16), loThreshold, hiThreshold);
									double[] t = Stats.quantile(rgb.createGreenDataset(AbstractDataset.INT16), loThreshold, hiThreshold);
									if (m[0] > t[0])
										m[0] = t[0];
									if (m[1] < t[1])
										m[1] = t[1];
									t = Stats.quantile(rgb.createBlueDataset(AbstractDataset.INT16), loThreshold, hiThreshold);
									if (m[0] > t[0])
										m[0] = t[0];
									if (m[1] < t[1])
										m[1] = t[1];
								} else {
									m = Stats.quantile(ds, loThreshold, hiThreshold);
								}
							} else {
								if (ds instanceof RGBDataset) {
									RGBDataset rgb = (RGBDataset) ds;
									AbstractDataset c = rgb.createRedDataset(AbstractDataset.INT16);
									double min = c.min().doubleValue();
									double max = Stats.quantile(c, hiThreshold);
									double t;

									c = rgb.createGreenDataset(AbstractDataset.INT16);
									t = c.min().doubleValue();
									if (min > t)
										min = t;
									t = Stats.quantile(c, hiThreshold);
									if (max < t)
										max = t;

									c = rgb.createBlueDataset(AbstractDataset.INT16);
									t = c.min().doubleValue();
									if (min > t)
										min = t;
									t = Stats.quantile(c, hiThreshold);
									if (max < t)
										max = t;

									m = new double[] {min, max};
								} else {
									m = new double[] {ds.min().doubleValue(), Stats.quantile(ds, hiThreshold)};
								}
							}
						} else {
							if (loThreshold > 0) {
								if (ds instanceof RGBDataset) {
									RGBDataset rgb = (RGBDataset) ds;
									AbstractDataset c = rgb.createRedDataset(AbstractDataset.INT16);
									double min = Stats.quantile(c, loThreshold);
									double max = c.max().doubleValue();
									double t;

									c = rgb.createGreenDataset(AbstractDataset.INT16);
									t = Stats.quantile(c, loThreshold);
									if (min > t)
										min = t;
									t = c.max().doubleValue();
									if (max < t)
										max = t;

									c = rgb.createBlueDataset(AbstractDataset.INT16);
									t = Stats.quantile(c, loThreshold);
									if (min > t)
										min = t;
									t = c.max().doubleValue();
									if (max < t)
										max = t;
									m = new double[] {min, max};
								} else {
									m = new double[] {Stats.quantile(ds, loThreshold), ds.max().doubleValue()};
								}
							} else {
								if (ds instanceof RGBDataset) {
									RGBDataset rgb = (RGBDataset) ds;
									AbstractDataset c = rgb.createRedDataset(AbstractDataset.INT16);
									double min = c.min().doubleValue();
									double max = c.max().doubleValue();
									double t;

									c = rgb.createGreenDataset(AbstractDataset.INT16);
									t = c.min().doubleValue();
									if (min > t)
										min = t;
									t = c.max().doubleValue();
									if (max < t)
										max = t;

									c = rgb.createBlueDataset(AbstractDataset.INT16);
									t = c.min().doubleValue();
									if (min > t)
										min = t;
									t = c.max().doubleValue();
									if (max < t)
										max = t;
									m = new double[] {min, max};
									
								} else {
									m = new double[] {ds.min().doubleValue(), ds.max().doubleValue()};
								}
							}
						}
						// Old mapping not necessary anymore 
//						int redSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4);
//						int greenSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 1);
//						int blueSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 2);
//						AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(redSelect));
//						AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(greenSelect));
//						AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(blueSelect));
//						ImageData imgD = SWTImageUtils.createImageData(ds, m[0], m[1], redFunc, greenFunc, blueFunc,
//								(redSelect < 0), (greenSelect < 0), (blueSelect < 0));
						ImageData imgD = SWTImageUtils.createImageData(ds, m[0], m[1], paletteData);
						gridImage = new Image(canvas.getDisplay(), imgD);
						imageDim = new Dimension(shape[1], shape[0]);
						canvas.redraw();
					} else {
						setStatus(INVALIDSTATUS);
					}
				} catch (Exception e) {
					setStatus(INVALIDSTATUS);
					logger.debug(e.getMessage());
				}
			}
			
		});
	}

	public void paint(GC gc, int posX, int posY, int xSize, int ySize)
	{
		if (gridImage != null &&
			!gridImage.isDisposed()) {
			int w, h, x, y;
			if (imageDim.width > imageDim.height) {
				w = xSize;
				x = 0;
				h = ySize * imageDim.height / imageDim.width;
				y = (ySize - h)/2;
			} else {
				h = ySize;
				y = 0;
				w = xSize * imageDim.width / imageDim.height;
				x = (xSize - w)/2;
			}
			gc.drawImage(gridImage, 0, 0, imageDim.width, imageDim.height, posX+x, posY+y, w, h);
			switch(status) {
				case 1:
				{
					if (green == null) {
						green = new Color(canvas.getDisplay(),new RGB(0,255,0));						
					}
					gc.setForeground(green);
					gc.drawRectangle(posX, posY, xSize, ySize);
				}
				break;
				case 2:
				{
					if (red == null) {
						red = new Color(canvas.getDisplay(),new RGB(255,0,0));						
					}
					gc.setForeground(red);
					gc.drawRectangle(posX, posY, xSize, ySize);
				}
				break;
				case SELECTEDSTATUS:
				{
					if (blue == null) {
						blue = new Color(canvas.getDisplay(),new RGB(64,64,255));						
					}
					gc.setAlpha(128);
					gc.setBackground(blue);
					gc.fillRectangle(posX, posY, xSize, ySize);
					gc.setAlpha(255);
				}
				break;
			}
		} else {
			if (red == null) {
				red = new Color(canvas.getDisplay(),new RGB(255,0,0));						
			}			
			gc.setForeground(red);
			gc.drawLine(posX+8,posY+8, posX+xSize-8, posY+ySize-8);
			gc.drawLine(posX+xSize-8, posY+8, posX+8, posY+ySize-8);
		}
	}
	
	public Dimension getImageDimension() {
		return imageDim;
	}

	public boolean hasThumbnailImage() {
		return thumbnailFilename != null;
	}
	
	public boolean hasImage() {
		return (gridImage != null && !gridImage.isDisposed());
	}

	@Override
	public void dispose() {
		if (gridImage != null &&
			!gridImage.isDisposed())
			gridImage.dispose();
		
        if (thumbnailFilename != null)	
        {
        	java.io.File imageFile = new java.io.File(thumbnailFilename);
        	imageFile.delete();
        }
	}

	@Override
	public String getToolTipText() {
		if (additionalInfo == null) {
			if (toolTipText == null) {
				toolTipText = (new java.io.File(filename)).getName();
			}
		} else {
			// TODO
		}
		return toolTipText;
	}
}
