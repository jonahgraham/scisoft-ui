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

import org.eclipse.dawnsci.analysis.api.metadata.IExtendedMetadata;
import org.junit.Test;



public class LightweightSRSScanCmdDecoratorTest {
	
	private String srsFileName = "testfiles/230152.dat";
		
	@Test
	public void testSRSMetaDataLoader(){
		LightweightSRSScanCmdDecorator scd = new LightweightSRSScanCmdDecorator();
		IExtendedMetadata metaData = scd.srsMyMetaDataLoader(srsFileName);
		assertEquals(metaData.getScanCommand(),"scan chi 90 -90 -1 Waittime 0.5");
	}
}
