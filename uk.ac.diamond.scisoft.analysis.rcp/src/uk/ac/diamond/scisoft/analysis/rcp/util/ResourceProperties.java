/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Composite;

public class ResourceProperties {
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("sda_resource"); //$NON-NLS-1$

	/**
	 * Creates an instance of a ControlExample embedded inside the supplied parent Composite.
	 * 
	 * @param parent
	 *            the container of the example
	 */
	@SuppressWarnings("unused")
	public ResourceProperties(Composite parent) {
		initResources();
	}

	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String. Returns the key if
	 * not found.
	 */
	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gets a string from the resource bundle and binds it with the given arguments. If the key is not found, return the
	 * key.
	 */
	static String getResourceString(String key, Object[] args) {
		try {
			return MessageFormat.format(getResourceString(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Loads the resources
	 */
	void initResources() {
		String error = (resourceBundle != null) ? getResourceString("error.CouldNotLoadResources")
				: "Unable to load resources"; //$NON-NLS-1$
		throw new RuntimeException(error);
	}
}
