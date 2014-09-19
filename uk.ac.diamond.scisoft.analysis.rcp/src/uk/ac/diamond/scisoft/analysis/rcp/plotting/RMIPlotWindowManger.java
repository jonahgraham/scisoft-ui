/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.rmi.RemoteException;

import org.eclipse.dawnsci.analysis.api.RMIClientProvider;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.IPlotWindowManagerRMI;

/**
 * Wrapper for PlotWindowManager for access over RMI
 * <p>
 * This class is generally intended to be called from Jython (such as in the interactive console) via import scisoftpy;
 * scisoftpy.plot.window_manager.*
 * <p>
 * No instance methods in this class should be called from the Eclipse UI thread.
 */
public class RMIPlotWindowManger implements IPlotWindowManagerRMI {
	private static final Logger logger = LoggerFactory.getLogger(RMIPlotWindowManger.class);

	private static IPlotWindowManagerRMI manager;

	/**
	 * Get the handle to the remote PlotWindowManager.
	 * <p>
	 * The returned manager should not be held, but refetched each time it is used to allow the port to be changed.
	 * 
	 * @return manager
	 */
	public static IPlotWindowManagerRMI getManager() {
		try {
			if (manager == null) {
				manager = (IPlotWindowManagerRMI) RMIClientProvider.getInstance().lookup(null, AbstractPlotWindow.RMI_SERVICE_NAME);
			}
			return manager;
		} catch (Exception e) {
			logger.error("Unable to obtain IPlotWindowManagerRMI manager", e);
			return null;
		}
	}
	
	/**
	 * Clear the cached manager so that it is refetched. Useful after changing the target port number.
	 */
	public static void clearManager() {
		manager = null;
	}
	

	@Override
	public String openDuplicateView(final String viewName) throws RemoteException {
		try {
			final String[] ret = new String[1];
			final Exception[] exp = new Exception[1];
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						ret[0] = PlotWindow.getManager().openDuplicateView(null, viewName);
					} catch (Exception e) {
						exp[0] = e;
					}
				}
			});

			if (exp[0] != null) {
				throw exp[0];
			}
			return ret[0];
		} catch (Exception e) {
			logger.error("Unexpected error during remote call", e);
			throw new RemoteException("Unexpected error during remote call", e);
		}
	}

	@Override
	public String openView(final String viewName) throws RemoteException {
		try {
			final String[] ret = new String[1];
			final Exception[] exp = new Exception[1];
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						ret[0] = PlotWindow.getManager().openView(null, viewName);
					} catch (Exception e) {
						exp[0] = e;
					}
				}
			});

			if (exp[0] != null) {
				throw exp[0];
			}
			return ret[0];
		} catch (Exception e) {
			logger.error("Unexpected error during remote call", e);
			throw new RemoteException("Unexpected error during remote call", e);
		}
	}

	@Override
	public String[] getOpenViews() throws RemoteException {
		try {
			final String[][] ret = new String[1][];
			final Exception[] exp = new Exception[1];
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						ret[0] = PlotWindow.getManager().getOpenViews();
					} catch (Exception e) {
						exp[0] = e;
					}
				}
			});

			if (exp[0] != null) {
				throw exp[0];
			}
			return ret[0];
		} catch (Exception e) {
			logger.error("Unexpected error during remote call", e);
			throw new RemoteException("Unexpected error during remote call", e);
		}
	}

}
