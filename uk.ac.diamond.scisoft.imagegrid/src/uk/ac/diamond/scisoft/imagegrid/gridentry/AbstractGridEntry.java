/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.imagegrid.gridentry;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * An abstract entry for the ImageGridTable
 */

public abstract class AbstractGridEntry {

	public static final int SELECTEDSTATUS = 666;
	public static final int INVALIDSTATUS = -1;

	protected String filename = null;
	protected String thumbnailFilename = null;
	protected int status = 0;
	protected Object additionalInfo = null;
	
	public AbstractGridEntry(String filename) {
		this.filename = filename;
	}

	public AbstractGridEntry(String filename, Object additionalInfo) {
		this.filename = filename;
		this.additionalInfo = additionalInfo;
	}

	public abstract void setNewfilename(String newFilename);

	public abstract void setStatus(int newStatus);

	public abstract void deActivate();

	public abstract boolean isDeactivated();
	
	public abstract void dispose();

	public abstract void createImage(IDataset ds);

	public abstract String getToolTipText();

	public String getFilename() {
		return filename;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}

	public Object getAdditionalInfo() {
		return additionalInfo;
	}

	public int getStatus() {
		return status;
	}
}
