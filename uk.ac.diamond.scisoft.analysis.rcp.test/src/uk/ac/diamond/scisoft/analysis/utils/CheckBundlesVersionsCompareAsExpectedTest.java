/*-
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * This class tests that bundle versions compare as expected 
 * so that we can use qualifiers to reduce change of wrong
 * PyDev.
 */
public class CheckBundlesVersionsCompareAsExpectedTest {

	@Test
	public void testComares() {
		String pydev_2_5_release = "2.5.0.2012040618";
		String pydev_2_5_diamond_fork_rev_1 = "2.5.0.2012060623-opengda-c9f36e1";
		String pydev_2_5_diamond_fork_rev_2 = "2.5.0.2012060719-opengda-7bab4d4";
		String pydev_2_5_diamond_fork_rev_2_or_later = "2.5.0.2012060719";
		String pydev_2_5_diamond_fork_later_than_rev_2 = "2.5.0.2012060720";

		List<Version> all = new ArrayList<Version>();
		// put them all in in a random order
		all.add(new Version(pydev_2_5_diamond_fork_rev_2_or_later));
		all.add(new Version(pydev_2_5_diamond_fork_rev_1));
		all.add(new Version(pydev_2_5_release));
		all.add(new Version(pydev_2_5_diamond_fork_later_than_rev_2));
		all.add(new Version(pydev_2_5_diamond_fork_rev_2));
		Collections.sort(all);
		
		Assert.assertTrue(all.get(0).toString().equals(pydev_2_5_release));
		Assert.assertTrue(all.get(1).toString().equals(pydev_2_5_diamond_fork_rev_1));
		Assert.assertTrue(all.get(2).toString().equals(pydev_2_5_diamond_fork_rev_2_or_later));
		Assert.assertTrue(all.get(3).toString().equals(pydev_2_5_diamond_fork_rev_2));
		Assert.assertTrue(all.get(4).toString().equals(pydev_2_5_diamond_fork_later_than_rev_2));
	}
}
