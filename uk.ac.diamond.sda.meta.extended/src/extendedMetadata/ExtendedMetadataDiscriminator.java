/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package extendedMetadata;

import org.eclipse.january.metadata.IExtendedMetadata;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.sda.meta.discriminator.IMetadataDiscriminator;

public class ExtendedMetadataDiscriminator implements IMetadataDiscriminator {

	public ExtendedMetadataDiscriminator() {
		// need a default constructor for extension point
	}

	@Override
	public boolean isApplicableFor(IMetadata metadata) {
		if (metadata instanceof IExtendedMetadata)
			return true;
		return false;
	}

}
