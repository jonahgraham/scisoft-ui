/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.page;

import java.util.EventObject;

public class DiffractionMetadataCompositeEvent extends EventObject {
	protected static enum EventType {
		BEAM_CENTRE,
	}

	private EventType type;

	public DiffractionMetadataCompositeEvent(Object source, EventType propertyType) {
		super(source);
		type = propertyType;
	}

	public boolean hasBeamCentreChanged() {
		return type == EventType.BEAM_CENTRE;
	}
}
