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

package uk.ac.diamond.sda.navigator.decorator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.metadata.IExtendedMetadata;



public class LightweightSRSScanCmdDecoratorTest {
	
	private String srsFileName = "testfiles/230152.dat";
		
	@Test
	public void testSRSMetaDataLoader(){
		LightweightSRSScanCmdDecorator scd = new LightweightSRSScanCmdDecorator();
		IExtendedMetadata metaData = scd.srsMyMetaDataLoader(srsFileName);
		assertEquals(metaData.getScanCommand(),"scan chi 90 -90 -1 Waittime 0.5");
	}
}
