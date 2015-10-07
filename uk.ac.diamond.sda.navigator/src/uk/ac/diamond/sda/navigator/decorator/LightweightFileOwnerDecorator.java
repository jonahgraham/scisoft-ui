/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.decorator;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightweightFileOwnerDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "uk.ac.diamond.sda.navigator.fileOwnerDecorator";
	
	private static final Logger logger = LoggerFactory.getLogger(LightweightFileOwnerDecorator.class);
	
	public LightweightFileOwnerDecorator() {
		super();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile) {
			IFile ifile = (IFile) element;
			IPath path = ifile.getLocation();
			File file = path.toFile();

			decoration.addSuffix("  "+getFileOwner(file));
		}
	}

	public static String getFileOwner(File file){
		String owner="";
		// File owner only for Unix OS
		String os = System.getProperty("os.name").toLowerCase();
		String command = "";
		if((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0) || (os.indexOf("mac") >= 0)){
			//command = "stat -c%U "+file.getAbsolutePath();
			command = "ls -l "+file.getAbsolutePath();
			
			
			try {
				Process p = Runtime.getRuntime().exec(command);
				Scanner sc = new Scanner(p.getInputStream());
				if(sc.hasNext()){
					String[] tmp = sc.nextLine().split(" ");
					owner = tmp[2]; //-rw-rw---- 1 wqk87977 wqk87977 16224 Nov 15 11:21 226942.dat
				}
				sc.close();
			} catch (IOException e) {
				logger.warn("WARNING: could not get file owner:",e.getMessage());
			}
		}
		return owner;
	}
}
