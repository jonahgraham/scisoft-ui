/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.system.info;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
/**
 * SystemInformation is a static class that holds all the current System information
 * of the system running the SDA Workbench. At least all the information it can
 * gather easily enough
 */
public class SystemInformation {

	private static HashMap<String, String> systemInfo = null;
	
	public final static String NUMCPUS = "NUMCPUS";
	public final static String JAVAVERSION = "JAVAVERSION";
	public final static String JAVAVMNAME = "JAVAVMNAME";
	public final static String TOTALMEMORY = "TOTALMEMORY";
	public final static String OSNAME = "OSNAME";
	public final static String OSVERSION = "OSVERSION";
	public final static String OSARCH = "OSARCH";
	public final static String SUPPORTOPENGL = "SUPPORTOPENGL";
	public final static String SUPPORTGLSL = "SUPPORTGLSL";
	public final static String GPUVENDOR = "GPUVENDOR";
	public final static String MAXTEXDIM = "MAXTEXDIM";

	public static void initialize() {
		systemInfo = new HashMap<String,String>();
		Runtime runtime = Runtime.getRuntime();
		systemInfo.put(OSNAME, System.getProperty("os.name"));
		systemInfo.put(OSVERSION, System.getProperty("os.version"));
		systemInfo.put(JAVAVERSION, System.getProperty("java.version"));
		systemInfo.put(JAVAVMNAME, System.getProperty("java.vm.name"));
		systemInfo.put(OSARCH, System.getProperty("os.arch"));
		systemInfo.put(TOTALMEMORY, ""+runtime.maxMemory());
		systemInfo.put(NUMCPUS,""+runtime.availableProcessors());		
	}
	
	public static void writeInfo(GC gc, 
								 int xpos,
								 Color red,
								 Color green,
								 Color yellow) {
		
		if (systemInfo.get(SUPPORTOPENGL) != null) {
			boolean hasJOGL = Boolean.parseBoolean(systemInfo.get(SUPPORTOPENGL));
			if (hasJOGL) {
				gc.setForeground(green);
				gc.drawText("PASSED",xpos,30);
			} else {
				gc.setForeground(red);
				gc.drawText("FAILED: Please upgrade your system to a OpenGL1.4 compatible graphics card",xpos,30);			
			}	
		}
		if (systemInfo.get(SUPPORTGLSL) != null) {
			boolean hasJOGLshaders = Boolean.parseBoolean(systemInfo.get(SUPPORTGLSL));
			if (hasJOGLshaders) {
				gc.setForeground(green);
				gc.drawText("PASSED",xpos,50);
			} else {
				gc.setForeground(red);
				gc.drawText("FAILED: GLSL isn't mandatory but recommended some features need it!",xpos,50);			
			}
		}
		float mBytes = Runtime.getRuntime().maxMemory()/(1024*1024);
		if (mBytes < 300) {
			gc.setForeground(red);
		} else if (mBytes < 1000) {
			gc.setForeground(yellow);
		} else
			gc.setForeground(green);
		String outputStr = ""+mBytes+" Mbyte(s)";
		if (mBytes < 1000) {
			outputStr+=" We recommend at least 1024Mbyte of Java heapspace";
		}
		gc.drawText(outputStr, xpos, 70);
		if (Runtime.getRuntime().availableProcessors() < 2) {
			gc.setForeground(yellow);
		} else
			gc.setForeground(green);
		gc.drawText(""+Runtime.getRuntime().availableProcessors(), xpos, 90);		
		
		String javaVersionStr = systemInfo.get(JAVAVERSION);
		outputStr = javaVersionStr;		
		int pos = javaVersionStr.indexOf('.');
		if (pos != -1) {
			String majorVersion = javaVersionStr.substring(0,pos);
			int pos2 = javaVersionStr.indexOf('.',pos+1);
			String minorVersion = javaVersionStr.substring(pos+1,pos2);
			int majorNr = Integer.parseInt(majorVersion);
			int minorNr = Integer.parseInt(minorVersion);
			if (majorNr == 1 && minorNr < 6) 
			{
				gc.setForeground(red);
				outputStr += " You need Java1.6";
			} else
				gc.setForeground(green);
		} else 
			gc.setForeground(red);		

		gc.drawText(outputStr,xpos,110);
	
	}

	/**
	 * Method to generate content for a system information check (used a HTML content provider)
	 * 
	 * @param systemInfoCheck SystemInformation static fields
	 * @return outputStr[] a String array with 2 columns ([0]= style, [1]= content)
	 */
	public static String[] writeHTMLInfo(String systemInfoCheck) {
		String[] outputStr={"", ""};
		if(systemInfoCheck.equals(SUPPORTOPENGL)){
			if (systemInfo.get(SUPPORTOPENGL) != null) {
				boolean hasJOGL = Boolean.parseBoolean(systemInfo.get(SUPPORTOPENGL));
				if (hasJOGL) {
					outputStr[0]="green";
					outputStr[1]="PASSED";
				} else {
					outputStr[0]="red";
					outputStr[1]="FAILED: Please upgrade your system to a OpenGL1.4 compatible graphics card";
				}	
			}
		} else if(systemInfoCheck.equals(SUPPORTGLSL)){
			if (systemInfo.get(SUPPORTGLSL) != null) {
				boolean hasJOGLshaders = Boolean.parseBoolean(systemInfo.get(SUPPORTGLSL));
				if (hasJOGLshaders) {
					outputStr[0]="green";
					outputStr[1]="PASSED";
				} else {
					outputStr[0]="yellow";
					outputStr[1]="FAILED: GLSL isn't mandatory but recommended some features need it!";
				}
			}
		} else if(systemInfoCheck.equals(NUMCPUS)){
			if (Runtime.getRuntime().availableProcessors() < 2) {
				outputStr[0]="yellow";
			} else
				outputStr[0]="green";
			outputStr[1]=""+Runtime.getRuntime().availableProcessors();

		} else if(systemInfoCheck.equals(JAVAVERSION)){
			String javaVersionStr = systemInfo.get(JAVAVERSION);
			outputStr[1] = javaVersionStr;
			int pos = javaVersionStr.indexOf('.');
			if (pos != -1) {
				String majorVersion = javaVersionStr.substring(0,pos);
				int pos2 = javaVersionStr.indexOf('.',pos+1);
				String minorVersion = javaVersionStr.substring(pos+1,pos2);
				int majorNr = Integer.parseInt(majorVersion);
				int minorNr = Integer.parseInt(minorVersion);
				if (majorNr == 1 && minorNr < 6){
					outputStr[0]="red";
					outputStr[1] += " You need Java1.6";
				} else
					outputStr[0]="green";
			} else 
				outputStr[0]="red";
		} else if(systemInfoCheck.equals(TOTALMEMORY)){
			float mBytes = Runtime.getRuntime().maxMemory()/(1024*1024);
			if (mBytes < 300) {
				outputStr[0]="red";
			} else if (mBytes < 1000) {
				outputStr[0]="yellow";
			} else
				outputStr[0]="green";
			outputStr[1] = ""+mBytes+" Mbyte(s)";
			if (mBytes < 1000) {
				outputStr[1]+=" We recommend at least 1024Mbyte of Java heapspace";
			}
		}

		return outputStr;
	}

	public static void setOpenGLVendor(String vendor) {
		systemInfo.put(GPUVENDOR, vendor);
	}
	
	public static String getOpenGLVendor() {
		return systemInfo.get(GPUVENDOR);
	}
	
	public static void setOpenGLMaxTex(int maxTexdim) {
		systemInfo.put(MAXTEXDIM, ""+maxTexdim);
	}
	
	public static void setOpenGLSupport(boolean support) {
		systemInfo.put(SUPPORTOPENGL, ""+support);
	}
	
	public static void setGLSLSupport(boolean support) {
		systemInfo.put(SUPPORTGLSL, ""+support);
	}

	public static String getGPUVendor() {
		return systemInfo.get(GPUVENDOR);
	}
	
	public static boolean supportsOpenGL() {
		return Boolean.parseBoolean(systemInfo.get(SUPPORTOPENGL));
	}
	
	public static boolean supportsGLSL() {
		return Boolean.parseBoolean(systemInfo.get(SUPPORTGLSL));
	}
	
	public static int getNumCPUs() {
		return Integer.parseInt(systemInfo.get(NUMCPUS));
	}
	
	public static long getTotalMemory() {
		return Long.parseLong(systemInfo.get(TOTALMEMORY));
	}
	
	public static String getJAVAVMVersion() {
		return systemInfo.get(JAVAVERSION);
	}
	
	
	public static String getJAVAVMName() {
		return systemInfo.get(JAVAVMNAME);
	}
	
	public static String getOSVersion() {
		return systemInfo.get(OSVERSION);
	}
	
	public static String getOSArchitecture() {
		return systemInfo.get(OSARCH);
	}
	
	public static String getOSName() {
		return systemInfo.get(OSNAME);
	}
	

	public static String getSystemString() {
		if (systemInfo == null) {
			initialize();
		}
		String returnStr;
		returnStr = OSNAME+":"+systemInfo.get(OSNAME)+"\n"+
		            OSVERSION+":"+systemInfo.get(OSVERSION)+"\n"+
		            OSARCH+":"+systemInfo.get(OSARCH)+"\n"+
		            JAVAVMNAME+":"+systemInfo.get(JAVAVMNAME)+"\n"+
		            JAVAVERSION+":"+systemInfo.get(JAVAVERSION)+"\n"+
		            TOTALMEMORY+":"+systemInfo.get(TOTALMEMORY)+"\n";
		if (systemInfo.get(SUPPORTOPENGL) != null)
			returnStr+=SUPPORTOPENGL+":"+systemInfo.get(SUPPORTOPENGL)+"\n";

		if (systemInfo.get(SUPPORTGLSL) != null)
			returnStr+=SUPPORTGLSL+":"+systemInfo.get(SUPPORTGLSL)+"\n";
		
		if (systemInfo.get(GPUVENDOR) != null)
			returnStr+=GPUVENDOR+":"+systemInfo.get(GPUVENDOR)+"\n";
		
		if (systemInfo.get(MAXTEXDIM) != null)
			returnStr+=MAXTEXDIM+":"+systemInfo.get(MAXTEXDIM);
		return returnStr;
	}
}
