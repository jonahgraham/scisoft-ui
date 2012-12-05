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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcClientProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview.MultiPlotViewTestBase.ThreadRunner;
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
					return (String) AnalysisRpcClientProvider.getInstance().request(PlotWindow.RPC_SERVICE_NAME,
							"openView", null, viewName);
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String openDuplicateView(IWorkbenchPage page, String viewName) {
				try {
					return (String) AnalysisRpcClientProvider.getInstance().request(PlotWindow.RPC_SERVICE_NAME,
							"openDuplicateView", null, viewName);
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String[] getOpenViews() {
				try {
					return (String[]) AnalysisRpcClientProvider.getInstance().request(PlotWindow.RPC_SERVICE_NAME,
							"getOpenViews");
				} catch (AnalysisRpcException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void clearPlottingSystem(AbstractPlottingSystem plottingSystem, String viewName) {
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
	public void clearPlottingSystem(final AbstractPlottingSystem plottingSystem, String viewName) {
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
