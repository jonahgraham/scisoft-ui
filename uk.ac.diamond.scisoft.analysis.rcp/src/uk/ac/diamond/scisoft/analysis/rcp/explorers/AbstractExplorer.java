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

package uk.ac.diamond.scisoft.analysis.rcp.explorers;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * This is a template for data file explorers to fill out. A concrete subclass of this can be used in any (read-only) editor part,
 * view part or the compare files editor.
 * 
 * <p>Warning: do not invoke {@link IWorkbenchPartSite#setSelectionProvider(ISelectionProvider)} in constructor.
 * Leave this to the method that instantiates the subclass.
 */
abstract public class AbstractExplorer extends Composite implements ISelectionProvider {

	protected IWorkbenchPartSite site;
	protected ISelectionChangedListener metaValueListener;

	/**
	 * Prefix for a default axis dataset name
	 */
	public static final String DIM_PREFIX = "dim:";

	/**
	 * @param parent
	 * @param partSite
	 * @param valueSelect listener to be called when a value is selected (in context menu)
	 */
	public AbstractExplorer(Composite parent, IWorkbenchPartSite partSite, ISelectionChangedListener valueSelect) {
		super(parent, SWT.NONE);

		site = partSite;
		metaValueListener = valueSelect;
		
	}

	protected void initDragDrop(final Viewer viewer) {
		DragSource dragSource = new DragSource(viewer.getControl(), DND.DROP_COPY | DND.DROP_MOVE);
		dragSource.addDragListener(new DragSourceListener() {
			
			@Override
			public void dragStart(DragSourceEvent event) {
		        ISelection selection = viewer.getSelection();
		        LocalSelectionTransfer.getTransfer().setSelection(selection);
		        LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
		        event.doit = !selection.isEmpty();
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = LocalSelectionTransfer.getTransfer().getSelection().toString();
				} else {
			        event.data = LocalSelectionTransfer.getTransfer().getSelection();
				}
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
		        LocalSelectionTransfer.getTransfer().setSelection(null);
		        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
			}
		});
		dragSource.setTransfer(new Transfer[] {LocalSelectionTransfer.getTransfer(), org.eclipse.swt.dnd.TextTransfer.getInstance()});

		// No drop support as of yet
	}

	/**
	 * Load file
	 * @param fileName
	 * @param mon
	 * @return data holder
	 * @throws Exception
	 */
	abstract public IDataHolder loadFile(String fileName, IMonitor mon) throws Exception;

	/**
	 * Load file and display in explorer
	 * @param fileName
	 * @param mon
	 * @throws Exception
	 */
	abstract public void loadFileAndDisplay(String fileName, IMonitor mon) throws Exception;
}
