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

import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overlay consumer
 */
public class TwoDViewOverlayConsumer {

	private static final Logger logger = LoggerFactory.getLogger(TwoDViewOverlayConsumer.class);
	

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
			l.areaSelected(null);
		}
	}

	protected void firePixelSelected(final int[] pixelPosition) {
		for (IConsumerListener l : consumerListener) {
			l.pixelSelected(pixelPosition);
		}
	}

	public void clearOverlays() {
		//TODO
	}

	
}