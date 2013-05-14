/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.datareduction;

import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;

public class DataReductionPlotter {
	private final static Logger logger = LoggerFactory.getLogger(DataReductionPlotter.class);

	/**
	 * Method that plots data on a plottingSystem given an IStructured Selection
	 * @param plottingSystem
	 * @param selection
	 */
	public static void plotData(final AbstractPlottingSystem plottingSystem, 
			IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			String filename = ((IFile) item).getRawLocation().toOSString();
				plotData(plottingSystem, filename,
						"/entry1/instrument/analyser/data",
						false);
		}
		// if the selection is an hdf5 tree item
		else if (item instanceof HDF5NodeLink) {
			HDF5NodeLink link = (HDF5NodeLink)item;

			String filename = link.getFile().getFilename();
				plotData(plottingSystem, filename,
						link.getFullName(),
						false);
		}
	}
	
	/**
	 * Method that loads data given a file and plots that data using the LightWeight PlottingSystem
	 * @param plottingSystem
	 *             the LightWeight plotting system
	 * @param fileName
	 *             the name of the data
	 * @param dataPath
	 *             if a NXS file, the data path, otherwise can be null
	 * @param intensityScale
	 *             display the intensity scale side bar.
	 */
	public static void plotData(final AbstractPlottingSystem plottingSystem, 
								String fileName, 
								final String dataPath, 
								final boolean intensityScale){
		loadAndDisplayImage(plottingSystem, fileName, dataPath, "Loading image", intensityScale);
	}

	private static void loadAndDisplayImage(final AbstractPlottingSystem plottingSystem,
								final String fileName,
								final String dataPath,
								final String loadingName,
								final boolean intensityScale){
		Job loaderJob = new Job(loadingName) {
			
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				IMonitor mon = new ProgressMonitorWrapper(monitor);
				try {
					DataHolder data = LoaderFactory.getData(fileName, mon);

					if(monitor.isCanceled()) return Status.CANCEL_STATUS;

					Map<String, ILazyDataset> map = data.getMap();
					ILazyDataset tmpvalue = map.get(dataPath);
					if(tmpvalue == null) tmpvalue = map.get(data.getName(0));
					if(tmpvalue == null) return Status.CANCEL_STATUS;

					ILazyDataset value = tmpvalue.squeeze();
					plottingSystem.clear();
					if(value.getShape().length == 2) {
						final AbstractDataset image = DatasetUtils.convertToAbstractDataset(value.getSlice(new Slice(null)));
						String tmpTitle = plottingSystem.getPlotName();
						plottingSystem.updatePlot2D(image, null, monitor);
						plottingSystem.setTitle(tmpTitle);
						plottingSystem.getAxes().get(0).setTitle("");
						plottingSystem.getAxes().get(1).setTitle("");
						plottingSystem.setKeepAspect(true);
						plottingSystem.setShowIntensity(intensityScale);
					} 
					else {
						logger.warn("Dataset not the right shape for showing in the preview");
						return Status.CANCEL_STATUS;
					}
				} catch (Exception e) {
					logger.error("Error "+loadingName, e);
					return Status.CANCEL_STATUS;
				}
					return Status.OK_STATUS;
			}
		};
		loaderJob.schedule();
	}
}
