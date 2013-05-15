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
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DataReductionPlotter {
	private final static Logger logger = LoggerFactory.getLogger(DataReductionPlotter.class);

	/**
	 * Method that plots data to a LightWeight PlottingSystem
	 * @param plottingSystem
	 *             the LightWeight plotting system
	 * @param data
	 *             the data to plot
	 */
	public static void plotData(final AbstractPlottingSystem plottingSystem,
								final IDataset data){
		Job plotJob = new Job("Plotting data") {
			
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {

					plottingSystem.clear();
					String tmpTitle = plottingSystem.getPlotName();
					if(data == null) return Status.CANCEL_STATUS;
					plottingSystem.updatePlot2D(data, null, monitor);
					plottingSystem.setTitle(tmpTitle);
					plottingSystem.getAxes().get(0).setTitle("");
					plottingSystem.getAxes().get(1).setTitle("");
					plottingSystem.setKeepAspect(true);
					plottingSystem.setShowIntensity(false);
				} catch (Exception e) {
					logger.error("Error plotting data", e);
					return Status.CANCEL_STATUS;
				}
					return Status.OK_STATUS;
			}
		};
		plotJob.schedule();
	}

	/**
	 * Method that loads data given an IStructuredSelection
	 * @param selection
	 * @return IDataset
	 */
	public static IDataset loadData(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			String filename = ((IFile) item).getRawLocation().toOSString();
			return loadData(filename,
						"/entry1/instrument/analyser/data");
		}
		// if the selection is an hdf5 tree item
		else if (item instanceof HDF5NodeLink) {
			HDF5NodeLink link = (HDF5NodeLink)item;

			String filename = link.getFile().getFilename();
			return loadData(filename,
						link.getFullName());
		}
		return null;
	}

	/**
	 * Method that loads data given a filename and a data path
	 * @param fileName
	 *             the name of the data
	 * @param dataPath
	 *             if a NXS file, the data path, otherwise can be null
	 * @return the data loaded as an AbstractDataset, null if none or not found
	 */
	public static AbstractDataset loadData(final String fileName, final String dataPath){
		try {
			DataHolder data = LoaderFactory.getData(fileName, null);

			Map<String, ILazyDataset> map = data.getMap();
			ILazyDataset tmpvalue = map.get(dataPath);
			if(tmpvalue == null) tmpvalue = map.get(data.getName(0));

			ILazyDataset value = tmpvalue.squeeze();
			if(value.getShape().length == 2) {
				return DatasetUtils.convertToAbstractDataset(value.getSlice(new Slice(null)));
			}
			logger.warn("Dataset not the right shape for showing in the preview");
			return null;
		} catch (Exception e) {
			logger.error("Error loading data", e);
			return null;
		}
	}
}
