/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.diamond.scisoft.analysis.plotclient.RMIPlotWindowManger;
import uk.ac.diamond.scisoft.analysis.plotserver.IPlotWindowManagerRMI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview.MultiPlotViewTestBase.ThreadRunner.ThreadRunnable;

/**
 * Concrete class that tests RMI connection from within same JVM
 */
public class PlotWindowManagerRMIPluginTest extends PlotWindowManagerPluginTestAbstract {

	private static IPlotWindowManagerRMI manager;

	@BeforeClass
	public static void setupRMIClient() {
		manager = RMIPlotWindowManger.getManager();
	}

	@AfterClass
	public static void dropReference() {
		manager = null;
	}

	@Override
	public String openDuplicateView(IWorkbenchPage page, final String viewName) {
		ThreadRunner threadRunner = new ThreadRunner(new ThreadRunnable() {

			@Override
			public Object run() throws Exception {
				return manager.openDuplicateView(viewName);
			}

		});
		return (String) threadRunner.run();
	}

	@Override
	public void clearPlottingSystem(final IPlottingSystem plottingSystem, String viewName) {
		ThreadRunner threadRunner = new ThreadRunner(new ThreadRunnable() {

			@Override
			public Object run() throws Exception {
				plottingSystem.reset();
				return null;
			}
		});
		threadRunner.run();
	}

	@Override
	public String openView(IWorkbenchPage page, final String viewName) {
		ThreadRunner threadRunner = new ThreadRunner(new ThreadRunnable() {

			@Override
			public Object run() throws Exception {
				return manager.openView(viewName);
			}

		});
		return (String) threadRunner.run();
	}

	@Override
	public String[] getOpenViews() {
		ThreadRunner threadRunner = new ThreadRunner(new ThreadRunnable() {

			@Override
			public Object run() throws Exception {
				return manager.getOpenViews();
			}

		});
		return (String[]) threadRunner.run();
	}

}
