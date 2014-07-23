/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.sda.navigator.views;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.util.io.FileUtils;
import org.dawnsci.io.h5.H5Loader;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.utils.OSUtils;
import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;
import uk.ac.diamond.sda.navigator.preference.FileNavigatorPreferenceConstants;
import uk.ac.diamond.sda.navigator.util.NIOUtils;
import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class FileLabelProvider extends ColumnLabelProvider {

	private int columnIndex;
	private SimpleDateFormat dateFormat;
	private IFileIconService service;
	private IPreferenceStore store;
	private boolean showComment;
	private boolean showScanCmd;

	public FileLabelProvider(final int column) throws Exception {
		this.columnIndex = column;
		this.dateFormat  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		this.service = (IFileIconService)ServiceManager.getService(IFileIconService.class);
		this.store =  NavigatorRCPActivator.getDefault().getPreferenceStore();
		
		this.showComment = store.getBoolean(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN);
		this.showScanCmd = store.getBoolean(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN);

	}

	@Override
	public Color getForeground(Object element) {
		if (columnIndex==0) return null;
		return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	}
	
	@Override
	public Image getImage(Object element) {
		
		if (element instanceof String) return null;
		final Path node   = (Path)element;
	
		switch(columnIndex) {
		case 0:
			try {
  			    return service.getIconForFile(node.toAbsolutePath().toString());
			} catch (Throwable ne) {
				return null;
			}

        default:
        	return null;
		}
	}

	/**
	 * { "Name", "Class", "Dims", "Type", "Size" };
	 */
	@Override
	public String getText(Object element) {
		

		if (element instanceof String) return (String)element;
		final Path node   = (Path)element;


		try {
			//if node is an hdf5 file, returns the file
			Map<Integer, String> attr = getH5Attributes(node);
	
			switch(columnIndex) {
			case 0:
				String name = NIOUtils.getRoots().contains(node)
				            ?  getRootLabel(node)
				            : node.getFileName().toString();
				return name;
			case 1:
				return dateFormat.format(Files.getLastModifiedTime(node).toMillis());
			case 2:
				return Files.isDirectory(node) ? "Directory" : FileUtils.getFileExtension(node.getFileName().toString());
			case 3:
				return formatSize(Files.size(node));
			case 4:
				return attr!=null&&showComment ? attr.get(4) : null;
			case 5:
				return attr!=null&&showScanCmd ? attr.get(5) : null;
			default:
				return null;
			
			}
		} catch (Exception ne) {
			return ne.getMessage();
		}
	}
	
	private Map<Path, Map<Integer, String>> attributes;

	
	private Map<Integer, String> getH5Attributes(Path node) throws Exception {
		
		if (Files.isDirectory(node))          return null;
		if (!H5Loader.isH5(node.toAbsolutePath().toString())) return null;
		
		if (attributes==null) attributes = new HashMap<Path, Map<Integer, String>>(89);
		if (attributes.containsKey(node)) return attributes.get(node);
		
		try (IHierarchicalDataFile h5File = HierarchicalDataFactory.getReader(node.toAbsolutePath().toString())) {
			
			final Map<Integer, String> attr = new HashMap<Integer,String>(3);
			attributes.put(node, attr);
			
			String comment;
			try {
				comment = NavigatorUtils.getHDF5Title(node.toAbsolutePath().toString(), h5File);
			} catch (Exception e) {
				comment = "N/A";
			}
			attr.put(4, comment);
			
			String scanCmd;
			try {
				scanCmd = NavigatorUtils.getHDF5ScanCommand(node.toAbsolutePath().toString(), h5File);
			} catch (Exception e) {
				e.printStackTrace();
				scanCmd = "N/A";
			}
			attr.put(5, scanCmd);

			return attr;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (attributes!=null) attributes.clear();
	}


	private String getRootLabel(Path node) {
    	if (OSUtils.isWindowsOS()) {
    		return	"("+node.toAbsolutePath().toString().substring(0, node.toAbsolutePath().toString().length()-1)+")";
    	}
		return "/";
    }
 
	private static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static String formatSize(long size) {
        if(size >= GB) {
            return df.format(size/GB) + " GB";
        }
        if(size >= MB) {
            return df.format(size/MB) + " MB";
        }
        if(size >= KB) {
            return df.format(size/KB) + " KB";
        }
        return "" + (int)size + " bytes";
    }
}
