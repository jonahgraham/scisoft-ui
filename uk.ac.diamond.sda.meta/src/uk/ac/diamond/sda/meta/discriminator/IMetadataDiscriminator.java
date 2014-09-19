/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.discriminator;

import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;

public interface IMetadataDiscriminator {

	/**
	 * This method returns true if the metadata being presented can be processed by the page
	 * 
	 * @param metadata
	 * @return is the page can process the metadata
	 */
	public boolean isApplicableFor(IMetadata metadata);

}
