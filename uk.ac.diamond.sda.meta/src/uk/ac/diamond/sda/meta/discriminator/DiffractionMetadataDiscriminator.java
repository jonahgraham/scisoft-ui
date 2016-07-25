/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.discriminator;

import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.january.metadata.IMetadata;

public class DiffractionMetadataDiscriminator implements IMetadataDiscriminator {

	public DiffractionMetadataDiscriminator() {

	}

	@Override
	public boolean isApplicableFor(IMetadata metadata) {
		if (metadata instanceof IDiffractionMetadata)
			return true;
		return false;
	}

}
