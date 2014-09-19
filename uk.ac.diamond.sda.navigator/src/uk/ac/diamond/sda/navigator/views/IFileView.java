/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.views;

import org.dawb.common.util.io.IFileSelector;

/**
 * This class is the interface for FileView. 
 * @see uk.ac.diamond.sda.navigator.views.FileView
 */
public interface IFileView extends IFileSelector {
	public void collapseAll();
	public void showPreferences();
	public void refresh();
	public void openSelectedFile();
}
