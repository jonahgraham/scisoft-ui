/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.editors.describers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

public class SRSDescriber implements IContentDescriber, ITextContentDescriber {

	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException {
		final BufferedReader reader = new BufferedReader(contents);
		boolean foundSRS = false;
		boolean foundEND = false;
		try {
			String dataStr;
			while ((dataStr = reader.readLine()) != null) {

				dataStr = dataStr.trim();

				if (dataStr.equals("&SRS")) {
					foundSRS = true;
				}

				if (dataStr.equals("&END")) {
					foundEND = true;
					break;
				}
			}
		} finally {
			reader.close();
		}

		if (foundSRS && foundEND) {
			return IContentDescriber.VALID;
		}
		return IContentDescriber.INVALID;
	}

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		return describe(new InputStreamReader(contents, "UTF-8"), description);
	}

	@Override
	public QualifiedName[] getSupportedOptions() {
		return IContentDescription.ALL;
	}

}
