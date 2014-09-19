/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.polling;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PollingPerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView("uk.ac.diamond.sda.polling.views.PollingView", IPageLayout.BOTTOM, 0.80f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", IPageLayout.RIGHT, 0.6f, IPageLayout.ID_EDITOR_AREA);
		
	}

}
