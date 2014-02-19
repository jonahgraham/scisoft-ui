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

package uk.ac.diamond.sda.navigator.fileview;

import java.io.File;
import java.io.FileFilter;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.sda.navigator.views.FileView;

/**
 * Run as junit plugin test
 */
public class FileViewPluginTest {
	
	private FileView fileView;

	@Before
	public void setup() throws Exception {
		final IWorkbenchPage page   = EclipseUtils.getPage();
	    this.fileView               = (FileView)page.showView("uk.ac.diamond.sda.navigator.views.FileView");
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
	}

	/**
	 * Very simple test to check default folder in clean workspace.
	 */
	@Test
	public void testUserHome() throws Exception {
		
		final File selected = fileView.getSelectedFile();
		final File uhome = new File(System.getProperty("user.home"));
		if (!selected.equals(uhome)) throw new Exception("Should select users home by default! "+selected);
	}
	
	/**
	 * Very simple test to open the part and select a few folders.
	 * The selection algorithm simply attempts to build up some kind of selection path
	 * similar to what would happen when a user does.
	 */
	@Test
	public void testSelectingSomeThings() throws Exception {
		
		final File root = uk.ac.gda.util.OSUtils.isWindowsOS() ? new File("C:/") : new File("/");
		File selected = root;
		
		int count = 10;
		while(selected!=null) {
		    selected = getTestFolder(selected, count);
		    if (selected==null) return;
		    fileView.setSelectedFile(selected.getAbsolutePath());
		    count-=3;
			EclipseUtils.delay(500);
		}
		EclipseUtils.delay(1000);

	}

	/**
	 * searches for a sub-folder with at least count sub-folders, with at least count contents.
	 * @param parent
	 * @param count
	 * @return a file
	 */
	private File getTestFolder(File parent, final int count) {
		
        final File[] dirs = parent.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File child) {
				if (!child.isDirectory()) return false;
				final File[] subdirs = child.listFiles(new FileFilter() {
					@Override
					public boolean accept(File c) {
						return c.isDirectory() && c.listFiles()!=null && c.listFiles().length>=count;
					}
				});
				if (subdirs==null || subdirs.length<count) return false;
				return true;
			}
		});
        
        if (dirs!=null && dirs.length>0) return dirs[0];
        
        return null;
	}

}
