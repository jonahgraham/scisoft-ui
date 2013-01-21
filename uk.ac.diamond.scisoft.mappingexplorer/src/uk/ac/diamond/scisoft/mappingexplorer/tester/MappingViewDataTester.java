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
package uk.ac.diamond.scisoft.mappingexplorer.tester;

import org.eclipse.core.expressions.PropertyTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rsr31645
 * 
 */
public class MappingViewDataTester extends PropertyTester {

	private static final Logger logger = LoggerFactory
			.getLogger(MappingViewDataTester.class);

	/**
	 * 
	 */
	public MappingViewDataTester() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		logger.info("Testing receiver {}", receiver);
		logger.info("Testing property {}", property);
		logger.info("Testing args {}", args);
		logger.info("Testing expectedValue {}", expectedValue);

		return false;
	}

}
