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
package uk.ac.diamond.scisoft.mappingexplorer.views.twod;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.jreality.overlay.Overlay2DConsumer;
import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider2;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayType;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overlay consumer
 */
public class TwoDViewOverlayConsumer implements Overlay2DConsumer {
	protected double oTransparency = 0.8;

	private static final Logger logger = LoggerFactory.getLogger(TwoDViewOverlayConsumer.class);
	private Overlay2DProvider2 provider;

	private int[] startPosition;

	private int[] endPosition;

	private boolean listeningStarted;

	private int verticalLine;

	private int horizontalLine;

	private int dragRectanglePrimitive;

	private boolean newDragStarted;

	private int finalRectPrimitive;

	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider2) provider;
	}

	public void clearOverlays() {
		clearCrosswire();
		clearRectangleOverlays(finalRectPrimitive);
		startPosition = null;
		endPosition = null;

		clearRectangleOverlays(dragRectanglePrimitive);
	}

	@Override
	public void unregisterProvider() {
		clearOverlays();
		this.provider = null;
	}

	@Override
	public void removePrimitives() {
		logger.info("Remove primitives");
		clearOverlays();
	}

	@Override
	public void hideOverlays() {
		logger.info("Hide overlays");
	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		// Control will reach here only when the image is dragged. For the circle
		// overlay, there is no drag involved.
		clearRectangleOverlays(finalRectPrimitive);
		newDragStarted = true;

		endPosition = event.getImagePosition();
		provider.begin(OverlayType.VECTOR2D);
		provider.setColour(dragRectanglePrimitive, java.awt.Color.RED);
		provider.drawBox(dragRectanglePrimitive, startPosition[0], startPosition[1], endPosition[0], endPosition[1]);
		provider.setTransparency(dragRectanglePrimitive, oTransparency);
		provider.end(OverlayType.VECTOR2D);
	}

	private List<IConsumerListener> consumerListener = new ArrayList<TwoDViewOverlayConsumer.IConsumerListener>();

	public void addConsumerListener(IConsumerListener listener) {
		consumerListener.add(listener);
	}

	public void removeConsumerListener(IConsumerListener listener) {
		consumerListener.remove(listener);
	}

	public interface IConsumerListener {
		void areaSelected(Point[] rectangleCoordinates);

		void pixelSelected(int[] pixel);
	}

	protected void fireAreaSelected() {
		for (IConsumerListener l : consumerListener) {
			l.areaSelected(getRectangleCoordinates());
		}
	}

	protected void firePixelSelected(final int[] pixelPosition) {
		for (IConsumerListener l : consumerListener) {
			l.pixelSelected(pixelPosition);
		}
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		if (listeningStarted) {
			logger.info("Image Finished : {}", event.getImagePosition());
			listeningStarted = false;
			endPosition = event.getImagePosition();

			if (endPosition[0] == startPosition[0] && endPosition[1] == startPosition[1]) {
				firePixelSelected(endPosition);
			} else {
				// don't fire notify changed
				clearRectangleOverlays(dragRectanglePrimitive);
				try {
					endPosition = event.getImagePosition();
					finalRectPrimitive = provider.registerPrimitive(PrimitiveType.BOX);
					provider.begin(OverlayType.VECTOR2D);
					provider.setColour(finalRectPrimitive, java.awt.Color.BLUE);
					provider.drawBox(finalRectPrimitive, startPosition[0], startPosition[1], endPosition[0],
							endPosition[1]);
					provider.setTransparency(finalRectPrimitive, oTransparency);
					provider.end(OverlayType.VECTOR2D);
					newDragStarted = false;
					fireAreaSelected();
				} catch (Exception ex) {
					logger.error("Error while drawing box:{}", ex);
				}
			}

		}
	}

	public Point[] getRectangleCoordinates() {
		int x1;
		int x2;
		int y1;
		int y2;
		if (newDragStarted) {
			return null;
		}
		if (startPosition == null) {
			return null;
		}
		if (endPosition == null) {
			return null;
		}
		if (startPosition[0] == endPosition[0]) {
			return null;
		}
		if (startPosition[1] == endPosition[1]) {
			return null;
		}
		Point[] points = new Point[2];
		if (startPosition[0] > endPosition[0]) {
			x1 = endPosition[0];
			x2 = startPosition[0];
		} else {
			x1 = startPosition[0];
			x2 = endPosition[0];
		}

		if (startPosition[1] > endPosition[1]) {
			y1 = endPosition[1];
			y2 = startPosition[1];
		} else {
			y1 = startPosition[1];
			y2 = endPosition[1];
		}
		points[0] = new Point(x1, y1);
		points[1] = new Point(x2, y2);
		return points;
	}

	protected void clearCrosswire() {
		if (verticalLine != -1) {
			provider.begin(OverlayType.VECTOR2D);
			provider.unregisterPrimitive(verticalLine);
			provider.end(OverlayType.VECTOR2D);
		}
		if (horizontalLine != -1) {
			provider.begin(OverlayType.VECTOR2D);
			provider.unregisterPrimitive(horizontalLine);
			provider.end(OverlayType.VECTOR2D);
		}
	}

	protected void clearRectangleOverlays(int primitive) {
		if (primitive != -1) {
			provider.begin(OverlayType.VECTOR2D);
			provider.unregisterPrimitive(primitive);
			provider.end(OverlayType.VECTOR2D);

		}
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		listeningStarted = true;
		dragRectanglePrimitive = provider.registerPrimitive(PrimitiveType.BOX);
		logger.info("Image Start : {}", event.getImagePosition());
		startPosition = event.getImagePosition();
	}

	@Override
	public void showOverlays() {
		logger.info("Show overlays");

	}

	public void drawHighlighterCrossWire(int pos1, int pos2) {
		provider.begin(OverlayType.VECTOR2D);
		verticalLine = provider.registerPrimitive(PrimitiveType.LINE);
		provider.setColour(verticalLine, java.awt.Color.blue);
		provider.drawLine(verticalLine, pos1, pos2 - 5, pos1, pos2 + 5);
		provider.end(OverlayType.VECTOR2D);

		provider.begin(OverlayType.VECTOR2D);
		horizontalLine = provider.registerPrimitive(PrimitiveType.LINE);
		provider.setColour(horizontalLine, java.awt.Color.blue);
		// provider.drawCircle(verticalLine, pos1, pos2,0.05);
		provider.drawLine(horizontalLine, pos1 - 5, pos2, pos1 + 5, pos2);
		provider.end(OverlayType.VECTOR2D);

	}

	public void drawAreaSelection(int endX, int endY) {
		logger.info("Area selection called");
		startPosition = new int[] { 0, 0 };
		endPosition = new int[] { endX, endY };

		provider.begin(OverlayType.VECTOR2D);
		provider.setColour(finalRectPrimitive, java.awt.Color.BLUE);
		provider.drawBox(finalRectPrimitive, startPosition[0], startPosition[1], endPosition[0], endPosition[1]);
		provider.setTransparency(finalRectPrimitive, oTransparency);
		provider.end(OverlayType.VECTOR2D);
		newDragStarted = false;
		fireAreaSelected();
	}

	public void clearAreaOverlay() {
		if (finalRectPrimitive != -1) {
			startPosition = null;
			endPosition = null;
			provider.begin(OverlayType.VECTOR2D);
			provider.unregisterPrimitive(finalRectPrimitive);
			provider.end(OverlayType.VECTOR2D);

		}
	}
}