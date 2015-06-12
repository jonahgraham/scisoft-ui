/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.mrc.ui;

import org.dawnsci.commandserver.ui.view.ConsumerView;
import org.dawnsci.commandserver.ui.view.StatusQueueView;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class EMPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);

		String queueViewId = StatusQueueView.createId("org.dawnsci.commandserver.foldermonitor", "org.dawnsci.commandserver.foldermonitor.FolderEventBean", "scisoft.em.STATUS_QUEUE", "scisoft.em.STATUS_TOPIC", "scisoft.diamond.FOLDER_QUEUE");
		queueViewId = queueViewId+"partName=EM Queue";
		layout.addView(queueViewId, IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(ConsumerView.ID, IPageLayout.RIGHT, 0.5f, queueViewId);
		layout.addView("uk.ac.diamond.scisoft.mrc.ui.controlView", IPageLayout.TOP, 0.28f, queueViewId);
	}


}
