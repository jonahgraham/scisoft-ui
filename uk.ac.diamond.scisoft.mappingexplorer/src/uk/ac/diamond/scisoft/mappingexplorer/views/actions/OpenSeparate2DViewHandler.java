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
package uk.ac.diamond.scisoft.mappingexplorer.views.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDMappingView;

/**
 * @author rsr31645
 * 
 */
public class OpenSeparate2DViewHandler extends OpenSeparateViewHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(OpenSeparate2DViewHandler.class);

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected String getViewIdToOpen() {
		return TwoDMappingView.ID;
	}

}
