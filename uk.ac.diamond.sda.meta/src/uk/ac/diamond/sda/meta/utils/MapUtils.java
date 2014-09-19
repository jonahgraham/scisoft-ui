/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

	/**
	 * @param value
	 * @return v
	 */
	public static String getString(final Map<String, String> value) {
		if (value == null)
			return null;
		if (value.isEmpty())
			return null;
		final String line = value.toString();
		return line.substring(1, line.length() - 1); // key=value, key1=value1, ...
	}

	/**
	 * @param value
	 * @return v
	 */
	public static Map<String, String> getMap(final String value) {

		if (value == null)
			return null;
		if ("".equals(value.trim()))
			return null;
		final List<String> lines = getList(value);
		if (lines == null)
			return null;
		if (lines.isEmpty())
			return Collections.emptyMap();

		final Map<String, String> ret = new LinkedHashMap<String, String>(lines.size());
		for (String line : lines) {
			final String[] kv = line.split("=");
			if (kv == null || kv.length != 2)
				continue;
			ret.put(kv[0].trim(), kv[1].trim());
		}
		return ret;
	}

	/**
	 * copied from uk.ac.gda.util.list and made private
	 * 
	 * @param value
	 * @return v
	 */
	private static List<String> getList(final String value) {
		if (value == null)
			return null;
		if ("".equals(value.trim()))
			return null;
		final String[] vals = value.split(",");
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++)
			ret.add(vals[i].trim());
		return ret;
	}
}
