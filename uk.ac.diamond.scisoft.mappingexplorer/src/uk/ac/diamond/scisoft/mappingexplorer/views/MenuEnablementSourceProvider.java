/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.scisoft.mappingexplorer.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

/**
 * @author rsr31645
 * 
 */
public class MenuEnablementSourceProvider extends AbstractSourceProvider {

	public static final int MENU_ENABLED = 1;
	public static final String MENU_ENABLED_NAME = "uk.ac.diamond.scisoft.mappingexplorer.menusEnabled"; //$NON-NLS-1$
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { MENU_ENABLED_NAME };

	private Map<String, Boolean> partMenuEnablement = new HashMap<String, Boolean>();

	/**
	 * 
	 */
	public MenuEnablementSourceProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	@Override
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	@Override
	public Map<String, Object> getCurrentState() {
		Map<String, Object> m = new HashMap<String, Object>();
		// m.put(MENU_ENABLED, partMenuEnablement);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	public void enableMenu() {
		partMenuEnablement.put(MENU_ENABLED_NAME, true);
		fireSourceChanged(3, partMenuEnablement);
	}

	public void disableMenu() {
		partMenuEnablement.put(MENU_ENABLED_NAME, false);
		fireSourceChanged(3, partMenuEnablement);
	}
}
