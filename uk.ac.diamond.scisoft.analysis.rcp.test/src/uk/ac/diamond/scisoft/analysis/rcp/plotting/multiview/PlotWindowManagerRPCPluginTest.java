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

import uk.ac.diamond.scisoft.analysis.AnalysisRpcClientProvider;
import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview.MultiPlotViewTestBase.ThreadRunner.ThreadRunnable;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

/**
 * Concrete class that tests RPC connection from within same JVM
 */
public class PlotWindowManagerRPCPluginTest extends PlotWindowManagerPluginTestAbstract {

	private static IPlotWindowManager manager;

	@BeforeClass
	public static void setupRMIClient() {

		manager = new IPlotWindowManager() {

			@Override
			public String openView(IWorkbenchPage page, String viewName) {
				try {
					return (String) AnalysisRpcClientProvider.getInstance().request(IPlotWindowManager.RPC_SERVICE_NAME,
							"openView", null, viewName);
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String openDuplicateView(IWorkbenchPage page, String viewName) {
				try {
					return (String) AnalysisRpcClientProvider.getInstance().request(IPlotWindowManager.RPC_SERVICE_NAME,
							"openDuplicateView", null, viewName);
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String[] getOpenViews() {
				try {
					return (String[]) AnalysisRpcClientProvider.getInstance().request(IPlotWindowManager.RPC_SERVICE_NAME,
							"getOpenViews");
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void clearPlottingSystem(IPlottingSystem plottingSystem, String viewName) {
				try{
					plottingSystem.reset();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
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
				return manager.openDuplicateView(null, viewName);
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
				return manager.openView(null, viewName);
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
