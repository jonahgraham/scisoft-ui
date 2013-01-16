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

import gda.observable.IObservable;
import gda.observable.IObserver;

import org.eclipse.ui.IViewPart;

import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 * Provider of histogram update data - medium for {@link HistogramView} and the {@link IViewPart} implementing this view
 * to talk to each other
 * 
 * @author rsr31645
 */
public interface IHistogramDataUpdateProvider {

	/**
	 * Data to be sent across to the histogram view so that the {@link HistogramView} and the {@link TwoDMappingView}
	 * can interact to show the appropriate colour scheme
	 */
	HistogramDataUpdate getHistogramDataUpdate();

	/**
	 * The {@link HistogramUpdate} object is provided by the {@link HistogramView} through the {@link IObserver} -
	 * {@link IObservable} interfaces.
	 * 
	 * @param histogramUpdate
	 */
	void setHistogramUpdate(HistogramUpdate histogramUpdate);

	/**
	 * Select the entire dataset in the plotter for histogramming.
	 */
	void selectAllForHistogram();

}
