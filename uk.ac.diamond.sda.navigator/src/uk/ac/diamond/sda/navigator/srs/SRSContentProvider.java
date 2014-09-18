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

package uk.ac.diamond.sda.navigator.srs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * Provides the properties contained in a *.dat file as children of that file in a Common Navigator.
 */
public class SRSContentProvider implements ITreeContentProvider, IResourceChangeListener, IResourceDeltaVisitor {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final String SRS_EXT = "dat"; //$NON-NLS-1$
	private final Map<IFile, SRSTreeData[]> cachedModelMap = new HashMap<IFile, SRSTreeData[]>();
	private static StructuredViewer viewer;
	protected static String fileName;
	private DataHolder data;
	private IMetadata metaData;
	private static final Logger logger = LoggerFactory.getLogger(SRSContentProvider.class);

	/**
	 * Create the SRSContentProvider instance. Adds the content provider as a resource change listener to track changes
	 * on disk.
	 */
	public SRSContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Return the model elements for a *.dat IFile or NO_CHILDREN for otherwise.
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;
		if (parentElement instanceof SRSTreeData) {
			children = NO_CHILDREN;
		} else if (parentElement instanceof IFile) {
			/* possible model file */
			IFile modelFile = (IFile) parentElement;
			if (SRS_EXT.equals(modelFile.getFileExtension())) {
				children = cachedModelMap.get(modelFile);
				if (children == null && updateModel(modelFile) != null) {
					children = cachedModelMap.get(modelFile);
				}
			}
		}
		return children != null ? children : NO_CHILDREN;
	}

	/**
	 * Method that calls the SRSLoader class to load a .dat file
	 * 
	 * @param file
	 *            The .dat file to open
	 */
	public void srsFileLoader(IFile file) {

		fileName = file.getLocation().toString();
		try {
			metaData = LoaderFactory.getMetadata(fileName, null);
		} catch (Exception ne) {
			logger.error("Cannot open dat file", ne);
		}
	}

	/**
	 * Load the model from the given file, if possible.
	 * 
	 * @param modelFile
	 *            The IFile which contains the persisted model
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized IMetadata updateModel(IFile modelFile) {
		srsFileLoader(modelFile);

		if (SRS_EXT.equals(modelFile.getFileExtension())) {
			if (modelFile.exists()) {
				List properties = new ArrayList();

				Collection<String> metaDataCollec = metaData.getDataNames();
				String[] names = new String[metaDataCollec.size()];int i=0;
				for (Iterator iterator = metaDataCollec.iterator(); iterator.hasNext();) {
					names[i] = (String) iterator.next();
					i++;
				}
				for (int j = 0; j < names.length; j++) {
						properties.add(new SRSTreeData(names[j].trim(), "", "", "", modelFile));
				}
				SRSTreeData[] srsTreeData = (SRSTreeData[]) properties.toArray(new SRSTreeData[properties.size()]);

				cachedModelMap.put(modelFile, srsTreeData);
				return metaData;
			} 
			if(!modelFile.exists())
				cachedModelMap.remove(modelFile);
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof SRSTreeData) {
			SRSTreeData data = (SRSTreeData) element;
			return data.getFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof SRSTreeData) {
			return false;
		} else if (element instanceof IFile) {
			return SRS_EXT.equals(((IFile) element).getFileExtension());
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		cachedModelMap.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		if (oldInput != null && !oldInput.equals(newInput))
			cachedModelMap.clear();
		viewer = (StructuredViewer) aViewer;
		viewer.setComparator(null);	// automatic sorting out of children disabled 
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
	 * .eclipse.core.resources.IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core .resources.IResourceDelta)
	 */
	@Override
	public boolean visit(IResourceDelta delta) {

		IResource source = delta.getResource();
		switch (source.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			final IFile file = (IFile) source;
			if (SRS_EXT.equals(file.getFileExtension())) {
				updateModel(file);
				new UIJob("Update SRS Model in CommonViewer") { //$NON-NLS-1$
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed()){
							viewer.refresh(file);
							viewer.setComparator(null); // automatic sorting out of children disabled
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
			return false;
		}
		return false;
	}

	public DataHolder getData() {
		return data;
	}

	public void setData(DataHolder data) {
		this.data= data;
	}

}
