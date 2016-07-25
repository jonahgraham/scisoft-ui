/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.page;

import org.eclipse.january.metadata.IMetadata;
import org.eclipse.swt.widgets.Composite;

public interface IMetadataPage {

	/**
	 * This is a setter that will allow the page to process the metadata.
	 * 
	 * @param metadata
	 */
	public void setMetaData(IMetadata metadata);

	/**
	 * Each IMetadata Page should be capable of returning a composite containing the GUI elements
	 * 
	 * @param parent
	 * @return a composite containing a gui
	 */
	public Composite createComposite(Composite parent);

}
