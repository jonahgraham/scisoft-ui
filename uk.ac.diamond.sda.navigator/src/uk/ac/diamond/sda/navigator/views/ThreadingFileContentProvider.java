/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.views;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;
import uk.ac.diamond.sda.navigator.preference.FileNavigatorPreferenceConstants;
import uk.ac.diamond.sda.navigator.util.NIOUtils;

class ThreadingFileContentProvider implements IFileContentProvider {
	

	private TreeViewer treeViewer;
	private FileSortType sort = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;
	private boolean collapseDatacollections;
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
	 * fast. This used to be a soft reference cache but the 
	 * file browsing does not really work if you start making the
	 * cached
	 */  
	private Map<Path, List<Path>>    cachedSorting;
	private Map<Path, Set<String>>   cachedStubs;
	private Map<Path, ReentrantLock> cachedLocks;
	

	public ThreadingFileContentProvider() {
		this.cachedSorting = new ConcurrentHashMap<Path, List<Path>>(89);
		this.cachedStubs   = new ConcurrentHashMap<Path, Set<String>>(89);
		this.cachedLocks   = new ConcurrentHashMap<Path, ReentrantLock>(89);
		
		this.elementQueue  = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
		this.childQueue    = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
		
		final IPreferenceStore store = NavigatorRCPActivator.getDefault().getPreferenceStore();
		collapseDatacollections = store.getBoolean(FileNavigatorPreferenceConstants.SHOW_COLLAPSED_FILES);
	}

	
	@Override
	public void dispose() {
		clearAndStop(null, true);
		elementQueue = null;
		childQueue = null;
	}
	
	@Override
	public void clearAll() {
		clearAndStop(null, false);
	}
	private void clearAndStop(Path path, boolean blankQueue) {
		
		if (blankQueue) {
			elementQueue.offerFirst(new BlankUpdateRequest()); // break the queue
			updateElementThread = null;
			
			childQueue.offerFirst(new BlankUpdateRequest()); // break the queue
			updateChildThread = null;
		}

		if (path!=null) {
			if (elementQueue!=null)  elementQueue.clear();
			if (childQueue!=null)    childQueue.clear();
			removeCachedPath(cachedStubs, path);
			removeCachedPath(cachedLocks, path);
			Object old = removeCachedPath(cachedSorting, path);
			if (old==null) {
				System.err.println("Unexpected clear in "+getClass().getSimpleName());
				clear();
			}
		} else {
		    clear();
		}
	}

	@Override
	public void clear(Path... paths) {
		if (paths==null || paths.length<1) {
			if (elementQueue!=null)  elementQueue.clear();
			if (childQueue!=null)    childQueue.clear();
			if (cachedSorting!=null) cachedSorting.clear();
			if (cachedStubs!=null)   cachedStubs.clear();
			if (cachedLocks!=null)   cachedLocks.clear();

		} else {
			for (int i = 0; i < paths.length; i++) {
				if (paths[i]==null) continue;
				removeCachedPath(cachedStubs,   paths[i].getParent());
				removeCachedPath(cachedLocks,   paths[i].getParent());
				removeCachedPath(cachedSorting, paths[i].getParent());
			}
		}
	}

	private Object removeCachedPath(Map<Path, ?> cache, Path delete) {
		Object ret = cache.remove(delete);
		for (Path path : cache.keySet()) {
			if (path.startsWith(delete) || !Files.exists(path)) {
				cache.remove(path);
			}
		}
		return ret;
	}

	
	private static final int ELEMENT_PRIORITY = Thread.MIN_PRIORITY;
	private static final int CHILD_PRIORITY   = Thread.MAX_PRIORITY;
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;
		treeViewer.refresh();
	}

	@Override
	public void updateElement(Object parent, int index) {

		if (elementQueue==null) return;
		if (PlatformUI.isWorkbenchRunning()) {
			if (updateElementThread==null) updateElementThread = createUpdateThread(elementQueue, ELEMENT_PRIORITY, "Update directory contents");
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
				updateChildCount(element, -1);
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
			if (updateChildThread==null) updateChildThread = createUpdateThread(childQueue, CHILD_PRIORITY, "Update child size");
			childQueue.offerFirst(new ChildUpdateRequest(element, true));
		} else {
			updateChildCountInternal(element, currentChildCount);
		}
				
	}
	
	private void updateChildCountInternal(Object element, int size) {
		
		if (element==null) return;
		
		if (element instanceof Path && Files.isDirectory((Path)element)) {
			treeViewer.setChildCount(element, size);
		} else if (element instanceof String){
			treeViewer.setChildCount(element, NIOUtils.getRoots().size());
		} else {
			treeViewer.setChildCount(element, 0);
		}
	}


	private List<Path> getFileList(Path node) {
		
		if (!Files.isDirectory(node)) return null;
		if (cachedSorting==null) return null;
				
		if (cachedSorting.containsKey(node)) {
			List<Path> sorted = cachedSorting.get(node);
			if (sorted!=null) return sorted;
		}
		
		return null;
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


	@Override
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
		
		protected synchronized void updateBusy(final BlockingQueue<UpdateRequest> queue, boolean start) {
			
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
			} else {				
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
					ReentrantLock lock = getLock(node);
					try {
						lock.lock();
						fa = getFileList(node);
					} finally {
						lock.unlock();
					}
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
				if (cachedSorting.containsKey(element)) return true;
				
				if (updateBusyRequired) updateBusy(childQueue, true);
				
				int count = 0;
				
				if (element instanceof Path) {
					final Path path = (Path)element;
					
					// We try to get the size but we ignore repeated scans in the same directory
					// Therefore as we find the number, we populate the cachedSorting as we go.
					if (Files.isDirectory(path)) {
						
						ILoaderService lservice=null;
						if (collapseDatacollections) {
						    lservice = NavigatorRCPActivator.getService(ILoaderService.class);
						}
						
		    		    final Map<String, Path> files = new TreeMap<String, Path>(new SortNatural<String>(false));
		    		    final Map<String, Path> dirs  = new TreeMap<String, Path>(new SortNatural<String>(false));

			        	// Faster way than File.list() in theory
			        	// see http://www.rgagnon.com/javadetails/java-get-directory-content-faster-with-many-files.html						
				        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
				        	
				        	Set<String> tmp = null;
				        	
							if (collapseDatacollections) {
								tmp = new HashSet<String>(31);
								cachedStubs.put(path, new HashSet<String>(31));
							}
			        	
							ReentrantLock lock = getLock(path);
							try {
								lock.lock();
					        	for (Path p : ds) {
					        		
					        		final boolean isDir = Files.isDirectory(p);
					        		final String  name  = p.getFileName().toString();
					        		
					        		if (!isDir) {
					        			if (lservice!=null) {
					        				Matcher matcher = lservice.getStackMatcher(name);
					        				if (matcher!=null && matcher.matches()) {
					        					String id = matcher.group(1);

					        					// If we already have an item for this scan:
					        					if (tmp!=null && tmp.contains(id)) {
					        						// We have more than one of them, so they get truncated
					        						cachedStubs.get(path).add(id);
					        						continue;
					        					}

					        					// Otherwise allows its index to be added.
					        					if (tmp!=null) tmp.add(id);
					        				}
					        			}
						        		files.put(name, p);
						        		
					        		} else if (isDir && sort==FileSortType.ALPHA_NUMERIC_DIRS_FIRST) { // dirs separate
					        			dirs.put(name, p);
					        		} else {
					        			files.put(name, p);
					        		}
					        		count+=1; 
					        	}
					        
				        	
				        		// We precache the directory contents now because we pared them down with the regexp
					    	    final List<Path> ret = new ArrayList<Path>(files.size()+dirs.size());
					    	    ret.addAll(dirs.values());
					    	    ret.addAll(files.values());
					    	    dirs.clear();
					    	    files.clear();
					    	    cachedSorting.put(path, ret);
					    	    
							} finally {
								lock.unlock();
							}

				        } catch (IOException ex) {
				        	// Nothing
				        }
					}
				} else {
					for (@SuppressWarnings("unused")Path p : NIOUtils.getRoots()) count+=1;
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
				
			    if (updateBusyRequired) updateBusy(childQueue, false);
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

	private ReentrantLock getLock(Path path) {
		ReentrantLock lock;
		if (cachedLocks.containsKey(path)) {
			lock = cachedLocks.get(path);
		} else {
			lock = new ReentrantLock();
			cachedLocks.put(path, lock);
		}
		return lock;
	}


	public boolean isCollapseDatacollections() {
		return collapseDatacollections;
	}


	@Override
	public void setCollapseDatacollections(boolean collapseDatacollections) {
		this.collapseDatacollections = collapseDatacollections;
	}


	@Override
	public Set<String> getStubs(Path folder) {
		return cachedStubs.get(folder);
	}

}
