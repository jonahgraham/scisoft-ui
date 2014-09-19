/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.utils;

import junit.framework.Assert;

import org.dawnsci.plotting.jreality.util.JOGLChecker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Ignore;
import org.junit.Test;

public class JOGLCheckerTest {

	@Test
	@Ignore("2011/01/24 Test ignored since not passing in Hudson GDA-3665")
	public void testJOGLChecker() {
		Display display = new Display();
		Shell shell = new Shell(display);
		Assert.assertEquals(true, JOGLChecker.canUseJOGL_OpenGL(null, shell));
	}
}
