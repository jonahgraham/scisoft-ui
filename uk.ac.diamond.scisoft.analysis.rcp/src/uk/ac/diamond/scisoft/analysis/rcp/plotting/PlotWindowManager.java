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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IIsBeingObserved;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.RMIServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.rpc.AnalysisRpcSyncExecDispatcher;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.rpc.IAnalysisRpcHandler;

public class PlotWindowManager implements IPlotWindowManager, IObservable, IIsBeingObserved {
	static private Logger logger = LoggerFactory.getLogger(PlotWindowManager.class);

	private static PlotWindowManager manager;

	/**
	 * Use this method to obtain a handle to the manager singleton.
	 * <p>
	 * This method isn't to be used outside of the PlotWindow framework. To obtain an {@link IPlotWindowManager} call
	 * {@link PlotWindow#getManager()}
	 * 
	 * @return PlotWindowManager
	 */
	public synchronized static PlotWindowManager getPrivateManager() {
		if (manager == null) {
			manager = new PlotWindowManager();

			// register as an RMI service
			try {
				RMIServerProvider.getInstance().exportAndRegisterObject(AbstractPlotWindow.RMI_SERVICE_NAME,
						new RMIPlotWindowManger());
			} catch (Exception e) {
				logger.warn("Unable to register PlotWindowManager for use over RMI - it might be disabled", e);
			}

			try {
				// register as an RPC service
				IAnalysisRpcHandler dispatcher = new AnalysisRpcSyncExecDispatcher(IPlotWindowManager.class, manager);
				AnalysisRpcServerProvider.getInstance().addHandler(AbstractPlotWindow.RPC_SERVICE_NAME, dispatcher);
			} catch (Exception e) {
				logger.warn("Not registered PlotWindowManager as RPC service - but might be disabled");
			}
		}
		return manager;
	}

	private Map<String, IPlotWindow> viewMap = new HashMap<String, IPlotWindow>();
	private Map<String, String> knownViews = new HashMap<String, String>();
	private Map<String, String> knownPlotViews = new HashMap<String, String>();
	private ObservableComponent observable = new ObservableComponent();

	private PlotWindowManager() {
		this(PlatformUI.getWorkbench().getViewRegistry().getViews());
	}

	/**
	 * Constructor is protected for testing purposes only
	 * 
	 * @param views
	 *            a list of view descriptors for available views in the registry
	 */
	protected PlotWindowManager(IViewDescriptor[] views) {
		// Register all view names that are explicit so we don't automatically
		// create a name with the same view
		if (views != null) {
			for (IViewDescriptor view : views) {
				// Register all views for duplicate name detection
				// This means we don't create new views with known names such
				// as Call Hierarchy or Search
				knownViews.put(view.getLabel(), view.getId());
				if (view.getId().startsWith(PlotView.ID)) {
					// Record views which we believe are normal plotting views
					knownPlotViews.put(view.getLabel(), view.getId());
				}
			}
		}
	}

	public void registerPlotWindow(IPlotWindow window) {
		viewMap.put(window.getName(), window);
		observable.notifyIObservers(this, null);
	}

	public void unregisterPlotWindow(IPlotWindow window) {
		viewMap.remove(window.getName());
		observable.notifyIObservers(this, null);
	}

	@Override
	public String openDuplicateView(IWorkbenchPage page, String viewName) {
		try {

			// Open the view, try to use the same page as the view
			// being duplicated
			if (page == null) {
				IPlotWindow window = viewMap.get(viewName);
				if (window != null) {
					page = window.getPage();
				}
			}

			// Create a new, unique name automatically
			String uniqueName = getUniqueName(viewName, page);

			// Perform the open
			openViewInternal(getPage(page), uniqueName);

			// Duplicate the data bean and (deeply) gui bean
			PlotServer plotServer = getPlotServer();
			GuiBean guiBean = plotServer.getGuiState(viewName);
			if (guiBean != null) {
				plotServer.updateGui(uniqueName, guiBean.copy());
			}
			DataBean dataBean = plotServer.getData(viewName);
			if (dataBean != null) {
				plotServer.setData(uniqueName, dataBean.copy());
			}

			return uniqueName;
		} catch (Exception e) {
			logger.error("Unable to duplicate plot view " + viewName, e);
			return null;
		}
	}

	@Override
	public void clearPlottingSystem(IPlottingSystem plottingSystem, String viewName) {
		try {
			plottingSystem.reset();
			plottingSystem.setTitle("");
		} catch (Exception e) {
			logger.error("Unable to clear plot view " + viewName, e);
		}
	}

	@Override
	public String openView(IWorkbenchPage page, String viewName) {
		try {
			if (viewName == null)
				viewName = getUniqueName("Plot 0", page);
			openViewInternal(page, viewName);
			return viewName;
		} catch (PartInitException e) {
			logger.error("Unable to open new plot view " + viewName, e);
			return null;
		}
	}

	protected void openViewInternal(IWorkbenchPage page, String viewName) throws PartInitException {
		if (knownViews.containsKey(viewName)) {
			getPage(page).showView(knownViews.get(viewName));
		} else {
			getPage(page).showView(PlotView.PLOT_VIEW_MULTIPLE_ID, viewName, IWorkbenchPage.VIEW_ACTIVATE);
		}
	}

	@Override
	public String[] getOpenViews() {
		Set<String> keys = viewMap.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	protected IWorkbenchPage getPage(IWorkbenchPage page) throws NullPointerException {
		if (page == null) {
			try {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			} catch (NullPointerException e) {
				Collection<IPlotWindow> values = viewMap.values();
				Iterator<IPlotWindow> iterator = values.iterator();
				if (iterator.hasNext())
					return iterator.next().getPage();
				throw new NullPointerException("Unable to obtain a workbench page to open view from");
			}
		}
		return page;
	}

	protected PlotServer getPlotServer() {
		return PlotServerProvider.getPlotServer();
	}

	private String getUniqueName(String base, IWorkbenchPage page) {
		try {
			Set<String> knownNames = new HashSet<String>(Arrays.asList(getAllPossibleViews(page)));
			int lastSpaceIndex = base.lastIndexOf(' ');
			if (lastSpaceIndex >= 0) {
				String numString = base.substring(lastSpaceIndex + 1);
				int viewNum = Integer.parseInt(numString);
				String prefix = base.substring(0, lastSpaceIndex + 1);
				String winner;
				do {
					viewNum++;
					winner = prefix + viewNum;
					if (viewNum > 1000000 || viewNum <= 0) {
						throw new NumberFormatException();
					}
				} while (knownViews.containsKey(winner) || knownNames.contains(winner));
				return winner;
			}
		} catch (NumberFormatException e) {
			// no number at end of string, fall through
		}
		return getUniqueName(base + " 0", page);
	}

	/**
	 * Return all the possible plot views that are either open, or can be opened because they are defined in plugin.xml
	 * or have data already in the Plot Server
	 * 
	 * @param page
	 *            workbench page to get list of view references from, can be <code>null</code> to automatically load
	 *            default page from Platform
	 * @return list of plot views
	 */
	public String[] getAllPossibleViews(IWorkbenchPage page) {
		Set<String> views = new HashSet<String>();
		views.addAll(Arrays.asList(getOpenViews()));
		views.addAll(knownPlotViews.keySet());
		if (page != null) {
			try {
				IViewReference[] viewReferences = getPage(page).getViewReferences();
				for (IViewReference ref : viewReferences) {
					if (PlotView.PLOT_VIEW_MULTIPLE_ID.equals(ref.getId())) {
						views.add(ref.getSecondaryId());
					}
				}
			} catch (NullPointerException e) {
				// Not a fatal error, but shouldn't happen
				logger.error("Failed to add list of view references", e);
			}
		}

		try {
			String[] names = getPlotServer().getGuiNames();
			if (names != null)
				views.addAll(Arrays.asList(names));
		} catch (Exception e) {
			logger.error("Failed to get list of GUI names from Plot Server", e);
		}

		return views.toArray(new String[views.size()]);
	}
	
	@Override
	public boolean IsBeingObserved() {
		return observable.IsBeingObserved();
	}

	@Override
	public void addIObserver(IObserver observer) {
		observable.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observable.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observable.deleteIObservers();
	}
}
