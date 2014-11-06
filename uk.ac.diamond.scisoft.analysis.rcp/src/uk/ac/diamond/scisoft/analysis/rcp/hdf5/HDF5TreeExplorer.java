/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import java.util.HashSet;
import java.util.Set;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.DataExplorationPerspective;
import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.explorers.MetadataSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;
import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView;

public class HDF5TreeExplorer extends AbstractExplorer implements ISelectionProvider {
	private static final Logger logger = LoggerFactory.getLogger(HDF5TreeExplorer.class);

	Tree tree = null;
	private HDF5TableTree tableTree = null;
	private Display display;

	private String filename;

	/**
	 * Separator between (full) file name and node path
	 */
	public static final String HDF5FILENAME_NODEPATH_SEPARATOR = "#";

	private HDF5Selection hdf5Selection;
	private Set<ISelectionChangedListener> cListeners;

	private Listener contextListener = null;

	private DataHolder holder;

	private HDF5Loader loader = null;

	public HDF5TreeExplorer(Composite parent, IWorkbenchPartSite partSite, ISelectionChangedListener valueSelect) {
		super(parent, partSite, valueSelect);

		display = parent.getDisplay();

		setLayout(new FillLayout());

		if (metaValueListener != null) {
			contextListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					handleContextClick();
				}
			};
		}
		
		Listener singleListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.button == 1)
					handleSingleClick();
			}
		};

		tableTree = new HDF5TableTree(this, singleListener, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.button == 1)
					handleDoubleClick();
			}
		}, contextListener);
		initDragDrop(tableTree.getViewer());
		
		cListeners = new HashSet<ISelectionChangedListener>();
	}

	/**
	 * Select a node and populate a selection object
	 * @param link node link
	 */
	public void selectHDF5Node(NodeLink link) {
		selectHDF5Node(link, null);
	}

	/**
	 * Select a node and populate a selection object
	 * @param link node link
	 * @param type
	 */
	public void selectHDF5Node(NodeLink link, InspectorType type) {
		if (link == null)
			return;

		if (processTextNode(link)) {
			return;
		}

		HDF5Selection s = HDF5Utils.createDatasetSelection(link, true);
		if (s == null) {
			logger.error("Could not process update of selected node: {}", link.getName());
			return;
		}

		// provide selection
		if (type != null)
			s.setType(type);
		setSelection(s);
	}

	/**
	 * Select a node and populate a selection object
	 * @param path path of node
	 */
	public void selectHDF5Node(String path) {
		selectHDF5Node(path, null);
	}

	/**
	 * Select a node and populate a selection object
	 * @param path path of node
	 * @param type
	 */
	public void selectHDF5Node(String path, InspectorType type) {
		NodeLink link = tree.findNodeLink(path);

		if (link != null) {
			selectHDF5Node(link, type);
		} else {
			logger.debug("Could not find selected node: {}", path);
		}
	}

	private void handleContextClick() {
		IStructuredSelection selection = tableTree.getSelection();

		try {
			// check if selection is valid for plotting
			if (selection != null) {
				Object obj = selection.getFirstElement();
				String metaName = null;
				if (obj instanceof NodeLink) {
					metaName = ((NodeLink) obj).getFullName();
				} else if (obj instanceof Attribute) {
					metaName = ((Attribute) obj).getFullName();
				}
				if (metaName != null) {
					SelectionChangedEvent ce = new SelectionChangedEvent(this, new MetadataSelection(metaName));
					metaValueListener.selectionChanged(ce);
				}
			}
		} catch (Exception e) {
			logger.error("Error processing selection: {}", e.getMessage());
		}
	}

	private void handleSingleClick() {
		// Single click passes the standard tree selection on.
		IStructuredSelection selection = tableTree.getSelection();
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener s : cListeners) s.selectionChanged(e);
	}
	
	private void handleDoubleClick() {
		checkDataExplorePerspective();
		final Cursor cursor = getCursor();
		Cursor tempCursor = getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		if (tempCursor != null) setCursor(tempCursor);

		IStructuredSelection selection = tableTree.getSelection();

		try {
			// check if selection is valid for plotting
			if (selection != null && selection.getFirstElement() instanceof NodeLink) {
				NodeLink link = (NodeLink) selection.getFirstElement();
				selectHDF5Node(link);
			}
		} catch (Exception e) {
			logger.error("Error processing selection: {}", e.getMessage());
		} finally {
			if (tempCursor != null)
				setCursor(cursor);
		}
	}

	private void checkDataExplorePerspective() {
		final IWorkbenchPage page = EclipseUtils.getPage();
		if (page != null) {
			IViewReference viewRef = page.findViewReference(DatasetInspectorView.ID);
			// Check whether the datasetInspector view is open or not
			if (viewRef == null) {
				boolean openDExplore = MessageDialog.openQuestion(page.getWorkbenchWindow().getShell(),
						"Open Data Exploration Perspective",
						"This kind of action is associated with the 'DExplore' Perspective.\n\nWould you like to switch to the DExplore perspective now?");
				if (openDExplore) {
					try {
						PlatformUI.getWorkbench().showPerspective(DataExplorationPerspective.ID,
								page.getWorkbenchWindow());
					} catch (Exception ne) {
						logger.error(ne.getMessage(), ne);
					}
				}
			}
		}
	}

	private boolean processTextNode(NodeLink link) {
		Node node = link.getDestination();
		if (!(node instanceof DataNode))
			return false;

		DataNode dataset = (DataNode) node;
		if (!dataset.isString())
			return false;

		try {
			getTextView().setData(dataset.getString());
			return true;
		} catch (Exception e) {
			logger.error("Error processing text node {}: {}", link.getName(), e);
		}
		return false;
	}

	@Override
	public void dispose() {
		if (loader != null)
			loader.stopAsyncLoading();

		cListeners.clear();
		tree = null;
		holder = null;
		loader = null;
		super.dispose();
	}

	private AsciiTextView getTextView() {
		AsciiTextView textView = null;
		// check if Dataset Table View is open
		try {
			textView = (AsciiTextView) site.getPage().showView(AsciiTextView.ID);
		} catch (PartInitException e) {
			logger.error("All over now! Cannot find ASCII text view: {} ", e);
		}

		return textView;
	}

	@Override
	public IDataHolder loadFile(String fileName, IMonitor mon) throws Exception {
		if (fileName == filename)
			return holder;

		return LoaderFactory.getData(HDF5Loader.class, fileName, true, mon);
	}

	private static final long REFRESH_PERIOD = 1000L; // time between refreshing in milliseconds

	@Override
	public void loadFileAndDisplay(String fileName, IMonitor mon) throws Exception {
		loader = new HDF5Loader(fileName);
		loader.setAsyncLoad(true);
		final Tree ltree = loader.loadTree(mon);
		if (ltree != null) {
			setFilename(fileName);

			setHDF5Tree(ltree);

			new Thread(new Runnable() { // wait till loader completion whilst periodically refreshing viewer
				@Override
				public void run() {
					while (loader.isLoading()) {
						try {
							Thread.sleep(REFRESH_PERIOD);
							refreshTree();
						} catch (InterruptedException e) {
						}
					}
					refreshTree();
					holder = HDF5Loader.createDataHolder(ltree, true);
				}
			}).start();
		}
	}

	private void refreshTree() {
		if (display == null)
			return;

		display.syncExec(new Runnable() {
			@Override
			public void run() {
				tableTree.refresh();
			}
		});
	}

	/**
	 * @return loaded tree or null
	 */
	public Tree getTree() {
		return tree;
	}

	public Tree getHDF5Tree() {
		return getTree();
	}

	public void setHDF5Tree(Tree htree) {
		setTree(htree);
	}

	public void setTree(Tree htree) {
		if (htree == null)
			return;

		tree = htree;

		if (display == null)
			return;

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				tableTree.setInput(tree.getNodeLink());
				display.update();
			}
		});
	}


	public void expandAll() {
		tableTree.expandAll();
	}
	
	public void expandToLevel(int level) {
		tableTree.expandToLevel(level);
	}
	
	public void expandToLevel(Object link, int level) {
		tableTree.expandToLevel(link, level);
	}
	
	public TreePath[] getExpandedTreePaths() {
		return tableTree.getExpandedTreePaths();
	}


	// selection provider interface
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (cListeners.add(listener)) {
			return;
		}
	}

	@Override
	public ISelection getSelection() {
		if (hdf5Selection == null)
			return new StructuredSelection(); // Eclipse requires that we do not return null
		return hdf5Selection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (cListeners.remove(listener))
			return;
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof HDF5Selection)
			hdf5Selection = (HDF5Selection) selection;
		else
			return;

		SelectionChangedEvent e = new SelectionChangedEvent(this, hdf5Selection);
		for (ISelectionChangedListener s : cListeners)
			s.selectionChanged(e);
	}

	/**
	 * Set full name for file (including path)
	 * @param fileName
	 */
	public void setFilename(String fileName) {
		filename = fileName;
	}

	public HDF5TableTree getTableTree(){
		return tableTree;
	}
}
