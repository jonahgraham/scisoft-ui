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
package uk.ac.diamond.scisoft.mappingexplorer.views;

/**
 * Interface to define the data that is to be passed around the mapping explorer framework.
 * 
 * @author rsr31645
 */
public interface IMappingView3dData extends IMappingView2dData {

	/**
	 * Dataset to be displayed on the view. This is believed to be a 3D dataset
	 */
	String getDimension3Label();

	/**
	 * @return dimension values that essentially showup on the slider. The length should be the same as the third
	 *         dimension.
	 */
	double[] getDimension3Values();

}