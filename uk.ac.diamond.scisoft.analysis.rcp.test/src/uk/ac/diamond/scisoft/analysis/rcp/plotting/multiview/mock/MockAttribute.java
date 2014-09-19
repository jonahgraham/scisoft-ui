/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview.mock;

public class MockAttribute {

	private String fName;
	private String fValue;

	public MockAttribute(String name) {
		fName = name;
	}

	public MockAttribute(String name, String value) {
		this(name);
		fValue = value;
	}

	public String getName() {
		return fName;
	}

	public String getValue() {
		return fValue;
	}
}