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

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.io.IExtendedMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.util.OSUtils;

public class FileLabelProvider extends ColumnLabelProvider {

	private int columnIndex;
	private SimpleDateFormat dateFormat;
	private IFileIconService service;

	public FileLabelProvider(final int column) throws Exception {
		this.columnIndex = column;
		this.dateFormat  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		this.service = (IFileIconService)ServiceManager.getService(IFileIconService.class);

	}
	
	@Override
	public Color getForeground(Object element) {
		if (columnIndex==0) return null;
		return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	}
	
	@Override
	public Image getImage(Object element) {
		
		if (element instanceof String) return null;
		final File node   = (File)element;
	
		switch(columnIndex) {
		case 0:
			try {
  			    return service.getIconForFile(node);
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
		final File node   = (File)element;
	
		switch(columnIndex) {
		case 0:
			return "".equals(node.getName())
				   ? getRootLabel(node)
				   : node.getName();
		case 1:
			return dateFormat.format(new Date(node.lastModified()));
		case 2:
			return node.isDirectory() ? "Directory" : FileUtils.getFileExtension(node);
		case 3:
			return formatSize(node.length());
		case 4:
			return node.isDirectory() ? "" : getASCIIScanCommand(node);
		default:
			return null;
		}
	}

    private String getASCIIScanCommand(File node) {
    	// make it work just for srs files for now
    	if(!FileUtils.getFileExtension(node).equals("dat") && !FileUtils.getFileExtension(node).equals("srs")) return "";
    	String result = "N/A";
    	IExtendedMetadata metaData = null;
    	try {
			IMetaData metaDataTest=LoaderFactory.getMetaData(node.getAbsolutePath(), null);
			if(metaDataTest instanceof IExtendedMetadata){
				metaData = (IExtendedMetadata)LoaderFactory.getMetaData(node.getAbsolutePath(), null);
				if(metaData == null) return result;
				result = metaData.getScanCommand();
			}
		} catch (Exception ne) {
			ne.printStackTrace();
			return result;
		}
		return result;
	}

	private String getRootLabel(File node) {
    	if (OSUtils.isWindowsOS()) {
    		return	"("+node.getAbsolutePath().substring(0, node.getAbsolutePath().length()-1)+")";
    	}
		return "/";
    }
 
	private static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static String formatSize(double size) {
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
