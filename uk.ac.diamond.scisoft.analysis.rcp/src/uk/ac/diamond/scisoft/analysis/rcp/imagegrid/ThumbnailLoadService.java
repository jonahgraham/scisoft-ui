/*-
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.rcp.imagegrid;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.utils.ImageThumbnailLoader;

/**
 *
 */
public class ThumbnailLoadService implements IThumbnailLoadService {

	protected boolean terminate = false;
	protected ArrayDeque<AbstractGridEntry> highPriorityQueue;
	protected ArrayDeque<AbstractGridEntry> lowPriorityQueue;
	protected final Semaphore locker = new Semaphore(1);

	public ThumbnailLoadService() {
		highPriorityQueue = new ArrayDeque<AbstractGridEntry>();
		lowPriorityQueue = new ArrayDeque<AbstractGridEntry>();
	}

	@Override
	public void run() {
		while (!terminate) {

			AbstractGridEntry entry = null;
			synchronized (highPriorityQueue) {
				if (highPriorityQueue.size() > 0)
					entry = highPriorityQueue.pop();
			}
			if (entry != null)
				processJob(entry);
			else {
				synchronized (lowPriorityQueue) {
					if (lowPriorityQueue.size() > 0)
						entry = lowPriorityQueue.pop();
				}
				if (entry != null)
					processJob(entry);
				else {
					try {
						synchronized (this) {
							wait();
						}
					} catch (InterruptedException ex) {
					}
				}
			}
		}
	}

	protected void loadAndCreateThumbnailImage(AbstractGridEntry entry) {
//		IDataset ds = ImageThumbnailLoader.loadImage(entry.getFilename(), true, false);
		IDataset ds = ImageThumbnailLoader.getThumbnail(entry.getFilename(), (IDataset) entry.getAdditionalInfo());
		entry.createImage(ds);
		locker.release();
	}

	private void processJob(AbstractGridEntry entry) {
		try {
			locker.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (entry instanceof SWTGridEntry) {
			if (!((SWTGridEntry) entry).hasThumbnailImage()) {
				loadAndCreateThumbnailImage(entry);
			} else {
				((SWTGridEntry) entry).loadThumbImage();
				try {
					Thread.sleep(30);
				} catch (InterruptedException ex) {
				}
				locker.release();
			}
		}
	}

	@Override
	public synchronized void addLoadJob(AbstractGridEntry entry, boolean highPriority) {
		notify();
		if (highPriority) {
			synchronized (highPriorityQueue) {
				highPriorityQueue.add(entry);
			}
		} else {
			synchronized (lowPriorityQueue) {
				lowPriorityQueue.add(entry);
			}
		}
	}

	@Override
	public synchronized void clearLowPriorityQueue() {
		synchronized (lowPriorityQueue) {
			lowPriorityQueue.clear();
		}
	}

	@Override
	public synchronized void clearHighPriorityQueue() {
		synchronized (highPriorityQueue) {
			highPriorityQueue.clear();
		}
	}

	@Override
	public synchronized void shutdown() {
		notify();
		synchronized (highPriorityQueue) {
			highPriorityQueue.clear();
		}
		synchronized (lowPriorityQueue) {
			lowPriorityQueue.clear();
		}
		terminate = true;
	}
}
