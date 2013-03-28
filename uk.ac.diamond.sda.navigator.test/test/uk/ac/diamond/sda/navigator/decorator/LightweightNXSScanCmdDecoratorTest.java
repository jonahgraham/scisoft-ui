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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class LightweightNXSScanCmdDecoratorTest {
	
	private String nxsFileName = "testFiles/2.nxs";
	
	private static final Logger logger = LoggerFactory.getLogger(LightweightNXSScanCmdDecoratorTest.class);
	
	@Test
	public void testGetHDF5TitleAndScanCmd(){
		try {
			String[][] listTitlesAndScanCmd = NavigatorUtils.getHDF5TitlesAndScanCmds(nxsFileName);
			assertEquals("\nScanCmd1: scan DCMFPitch -0.12 0.12 0.0040 counter 1.0 BPM1IN", listTitlesAndScanCmd[1][0]);
			assertEquals("", listTitlesAndScanCmd[0][0]);
		} catch (Exception e) {
			logger.error("Could not load NXS Title/ScanCmd: ", e);
		}
	}
}
