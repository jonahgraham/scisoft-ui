/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import gda.observable.IObserver;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindowManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.ShowPlotViewHandler.ShowPlotViewParameterValues;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

/**
 * Contribution for Show Plot View submenu. Contains a list of all items that
 * {@link ShowPlotViewParameterValues#getParameterValues()} returns.
 * <p>
 * {@link ShowPlotViewHandler} is used as the handler for all the menu items contributed.
 */
public class ShowPlotViewMenu extends ContributionItem {
	private static Collator collator;
	private Comparator<CommandContributionItemParameter> actionComparator = new Comparator<CommandContributionItemParameter>() {
		@Override
		public int compare(CommandContributionItemParameter o1, CommandContributionItemParameter o2) {
			if (collator == null) {
				collator = Collator.getInstance();
			}
			return collator.compare(o1.label, o2.label);
		}
	};

	protected boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};
	private IObserver markDirtyOnChange = new IObserver() {

		@Override
		public void update(Object source, Object arg) {
			dirty = true;
		}
	};
	private ImageDescriptor imageDescriptor;

	/**
	 * Creates a Show Plot View menu.
	 * 
	 * @param id
	 *            the id
	 */
	public ShowPlotViewMenu(String id) {
		super(id);
	}

	/**
	 * Creates a Show Plot View menu.
	 */
	public ShowPlotViewMenu() {
		super();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Fills the menu with Show View actions.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		// Get visible actions.
		ShowPlotViewParameterValues values = new ShowPlotViewHandler.ShowPlotViewParameterValues();
		Map<String, String> parameterValues = values.getParameterValues();

		List<CommandContributionItemParameter> actions = new ArrayList<CommandContributionItemParameter>(
				parameterValues.size());
		CommandContributionItemParameter newPlotCCIP = null;
		for (Entry<String, String> entry : parameterValues.entrySet()) {
			CommandContributionItemParameter item = getItem(entry);
			if (item != null) {
				if (entry.getValue() == null) {
					newPlotCCIP = item;
				} else {
					actions.add(item);
				}
			}
		}
		Collections.sort(actions, actionComparator);
		for (CommandContributionItemParameter ccip : actions) {
			CommandContributionItem item = new CommandContributionItem(ccip);
			innerMgr.add(item);
		}

		if (newPlotCCIP != null) {
			// We only want to add the separator if there are no views,
			// otherwise, there will be a separator and then the 'New Plot View' entry
			// and that looks weird as the separator is separating nothing
			if (!innerMgr.isEmpty()) {
				innerMgr.add(new Separator());
			}

			// Add 'New Plot View'
			innerMgr.add(new CommandContributionItem(newPlotCCIP));
		}
	}

	private CommandContributionItemParameter getItem(Entry<String, String> entry) {
		if (imageDescriptor == null) {
			IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
			IViewDescriptor desc = reg.find(PlotView.PLOT_VIEW_MULTIPLE_ID);
			imageDescriptor = desc.getImageDescriptor();
		}

		CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow(), null, ShowPlotViewHandler.COMMAND_ID, CommandContributionItem.STYLE_PUSH);
		ccip.label = entry.getKey();
		ccip.icon = imageDescriptor;
		Map<String, String> params = new HashMap<String, String>();
		params.put(ShowPlotViewHandler.VIEW_NAME_PARAM, entry.getValue());
		ccip.parameters = params;

		return ccip;
	}

	@Override
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}
		PlotServerProvider.getPlotServer().addIObserver(markDirtyOnChange);
		PlotWindowManager.getPrivateManager().addIObserver(markDirtyOnChange);

		if (!dirty) {
			return;
		}

		MenuManager manager = new MenuManager();
		fillMenu(manager);
		IContributionItem items[] = manager.getItems();

		for (IContributionItem item : items) {
			item.fill(menu, index++);
		}

		dirty = false;
	}

	@Override
	public void dispose() {
		PlotServerProvider.getPlotServer().deleteIObserver(markDirtyOnChange);
		PlotWindowManager.getPrivateManager().deleteIObserver(markDirtyOnChange);
		super.dispose();
	}

}
