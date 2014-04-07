/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.imagegrid;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

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
