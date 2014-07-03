/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.sda.navigator.views;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.dawb.common.util.io.SortingUtils;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.sda.navigator.util.NIOUtils;

public class FileContentProvider implements ILazyTreeContentProvider {

	public enum FileSortType {
		ALPHA_NUMERIC, ALPHA_NUMERIC_DIRS_FIRST;
	}
	
	private TreeViewer treeViewer;
	private FileSortType sort = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;
	private LinkedBlockingDeque<UpdateRequest> elementQueue;
	private LinkedBlockingDeque<UpdateRequest> childQueue;
	
	/**
	 * Two threads with different priorities used.
	 */
	private Thread updateElementThread;

	/**
	 * Two threads with different priorities used.
	 */
	private Thread updateChildThread;

	/**
	 * Caching seems to be needed to keep the path sorting
	 * fast. 
	 * NOTE Is there a better way of doing this.
	 */
	private Map<Path, List<Path>> cachedSorting;
	@SuppressWarnings("unused")
	private IStatusLineManager statusManager;

	public FileContentProvider(final IStatusLineManager statusManager) {
		this.statusManager = statusManager;
		this.cachedSorting = new WeakHashMap<Path, List<Path>>(89);
		this.elementQueue  = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
		this.childQueue    = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
	}

	
	@Override
	public void dispose() {
		clearAndStop();
		elementQueue = null;
		childQueue = null;
	}

	private void clear() {
		if (elementQueue!=null)  elementQueue.clear();
		if (childQueue!=null)    childQueue.clear();
		if (cachedSorting!=null) cachedSorting.clear();
	}
	
	public void clearAndStop() {
		clear();
		elementQueue.offerFirst(new BlankUpdateRequest()); // break the queue
		updateElementThread = null;
		
		childQueue.offerFirst(new BlankUpdateRequest()); // break the queue
		updateChildThread = null;
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;
		treeViewer.refresh();
	}

	@Override
	public void updateElement(Object parent, int index) {

		if (elementQueue==null) return;
		if (PlatformUI.isWorkbenchRunning()) {
			if (updateElementThread==null) updateElementThread = createUpdateThread(elementQueue, 9, "Update directory contents");
			elementQueue.offerFirst(new ElementUpdateRequest(parent, index));
		} else {
			final Path node = (Path) parent;
			final List<Path> fa = getFileList(node);
			updateElementInternal(node, index, fa);
		}
	}

	public void updateElementInternal(Object parent, int index, List<Path> fa) {
		
		
		if (fa!=null && index < fa.size()) {
			Path element = fa.get(index);
			treeViewer.replace(parent, index, element);
			
			// We correct when they expand, listFiles() could be slow.
			if (Files.isDirectory(element)) {
				treeViewer.setChildCount(element, 1); // 1 for now
				if (updateChildThread==null) updateChildThread = createUpdateThread(childQueue, 2, "Update child size");
				childQueue.offerFirst(new ChildUpdateRequest(element, false)); // process size from queue
			} else {
				treeViewer.setChildCount(element, 0);
			}
		}
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		
		if (childQueue==null) return;
		
		if (element instanceof Path && !Files.isDirectory((Path)element)) {
			treeViewer.setChildCount(element, 0);
			return;
		}
		
		if (PlatformUI.isWorkbenchRunning()) {
			if (updateChildThread==null) updateChildThread = createUpdateThread(childQueue, 2, "Update child size");
			childQueue.offerFirst(new ChildUpdateRequest(element, true));
		} else {
			updateChildCountInternal(element, currentChildCount);
		}
				
	}
	
	private void updateChildCountInternal(Object element, int size) {
		
		if (element==null) return;
		
		if (element instanceof Path && !Files.isDirectory((Path)element)) {
			treeViewer.setChildCount(element, size);
		} else {
			treeViewer.setChildCount(element, 0);
		}
		
	}


	private List<Path> getFileList(Path node) {
		
		if (!Files.isDirectory(node)) return null;
		if (cachedSorting==null) return null;
				
		if (cachedSorting.containsKey(node)) {
			return cachedSorting.get(node);
		}
		
		List<Path> sorted=null;
		try {
			if (sort==FileSortType.ALPHA_NUMERIC) {
				sorted = SortingUtils.getSortedPathList(node, false);
			} else {
				sorted = SortingUtils.getSortedPathList(node, true);
			}
			cachedSorting.put(node, sorted);
		} catch (IOException ne) {
			cachedSorting.put(node, null);
		}
		return sorted;
	}


	@Override
	public Object getParent(Object element) {
		if (element==null || !(element instanceof Path)) {
			return null;
		}
		final Path node = ((Path) element);
		return node.getParent();
	}


	public FileSortType getSort() {
		return sort;
	}


	public void setSort(FileSortType sort) {
		this.sort = sort;
	}
	
	
	private static Cursor  busy;
	private static boolean isBusy = false;

	private abstract class UpdateRequest {
		
		protected Object element;
		protected int index;

		UpdateRequest() {
			element=null;
			index  =-1;
			if (busy ==null) busy   = treeViewer.getControl().getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		}

		UpdateRequest(final Object element, final int index) {
			this.element = element;
			this.index   = index;
		}

		public Object getElement() {
			return element;
		}

		public int getIndex() {
			return index;
		}

		public abstract boolean process() throws Exception;
		
		protected void updateBusy(final BlockingQueue<UpdateRequest> queue, boolean start) {
			
			if (start) {
				if (!isBusy) {
					isBusy = true;
					if (treeViewer.getControl().isDisposed()) return;
					treeViewer.getControl().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (treeViewer.getControl().isDisposed()) return;
							treeViewer.getControl().setCursor(busy);
						}
					});
	
				}
			}
			
			// They requested us to stop
			if (!start) {				
				if (queue.isEmpty()) { // Nothing more in queue
           			if (treeViewer.getControl().isDisposed()) return;
                    treeViewer.getControl().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (treeViewer.getControl().isDisposed()) return;
							treeViewer.getControl().setCursor(null);
							isBusy = false;
						}
        			});
            	}
			}
		}
	}
	
	private class BlankUpdateRequest extends UpdateRequest {

		@Override
		public boolean process() throws Exception {
			return false;
		}
		
	}
	
	private class ElementUpdateRequest extends UpdateRequest {

		public ElementUpdateRequest(Object element, int index) {
			super(element, index);
		}

		@Override
		public boolean process() throws Exception {
			
			try {
				updateBusy(elementQueue, true);
				
				final List<Path> fa;
				if (getElement() instanceof String) {
					fa = NIOUtils.getRoots();
				} else {
					final Path node = (Path) getElement();
					fa = getFileList(node);
					
				}
	

				if (treeViewer.getControl().isDisposed()) return false;
				treeViewer.getControl().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (treeViewer.getControl().isDisposed()) return;

						updateElementInternal(getElement(), getIndex(), fa);
					}
				});

				
			} finally {
				updateBusy(elementQueue, false);

			}
			
			return true;

		}
		
	}
	
	
	private class ChildUpdateRequest extends UpdateRequest {

		private boolean updateBusyRequired;


		public ChildUpdateRequest(Object element, boolean updateBusyRequired) {
			super();
			this.element = element;
			this.updateBusyRequired = updateBusyRequired;
		}


		@Override
		public boolean process() throws Exception {
			
			try {
				
				if (updateBusyRequired) updateBusy(childQueue, true);
				
				
				int count = 0;
				
				if (element instanceof Path) {
			        try (DirectoryStream<Path> ds = Files.newDirectoryStream((Path)element)) {
			        	for (@SuppressWarnings("unused") Path path : ds) count+=1; // Faster way that File.list() in theory
			        } catch (IOException ex) {}
				} else {
					for (@SuppressWarnings("unused")Path p : FileSystems.getDefault().getRootDirectories()) count+=1;
				}
				
				final int size = count;
		        
				if (treeViewer.getControl().isDisposed()) return false;
				treeViewer.getControl().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (treeViewer.getControl().isDisposed()) return;

						updateChildCountInternal(element, size);
					}
				});
				    
				
			} finally {
				
			    if (updateBusyRequired) updateBusy(childQueue, true);
			}
			
			return true;

		}
		
	}
	

	/**
	 * Method creates a thread to process a queue
	 */
	private Thread createUpdateThread(final BlockingQueue<UpdateRequest> queue, final int priority, String name) {

		final Thread thread = new Thread(name) {

			@Override
			public void run() {

				while(!treeViewer.getControl().isDisposed() && queue!=null) {
					try {

						final UpdateRequest req = queue.take();

						// Blank object added to break the queue
						if (req.getElement()==null && req.getIndex()==-1) return;
                        if (req instanceof BlankUpdateRequest) return;
                        
						final boolean ok = req.process();
						if (!ok) break;

					} catch (InterruptedException ne) {
						break;

					} catch (org.eclipse.swt.SWTException swtE) {
						queue.clear();
						break;

					} catch (Exception ne) {
						queue.clear();
						continue;
					}
				}
			}	
		};
		thread.setPriority(priority);
		thread.setDaemon(true);
		thread.start();

		return thread;
	}

}
