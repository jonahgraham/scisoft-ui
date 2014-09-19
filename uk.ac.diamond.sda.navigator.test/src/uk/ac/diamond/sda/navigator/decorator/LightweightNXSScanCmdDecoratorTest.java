/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.decorator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class LightweightNXSScanCmdDecoratorTest {
	
	private String nxsFileName = "testfiles/2.nxs";
	
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
