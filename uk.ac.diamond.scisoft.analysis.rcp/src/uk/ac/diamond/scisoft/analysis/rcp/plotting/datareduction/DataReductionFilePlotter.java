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

import gda.analysis.io.ScanFileHolderException;

import java.util.ArrayList;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.util.DisplayUtils;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.datareduction.DataReductionSelectionType.DATA_TYPE;

public class DataReductionFilePlotter {
	private final static Logger logger = LoggerFactory.getLogger(DataReductionFilePlotter.class);

	/**
	 * Method that loads data given a file and plots that data using the LightWeight PlottingSystem
	 * @param plottingSystem
	 *             the LightWeight plotting system
	 * @param fileName
	 *             the name of the data
	 * @param fileExtension
	 *             the extension of the file
	 * @param dataPath
	 *             if a NXS file, the data path, otherwise can be null
	 * @param axesPaths
	 *             if a NXS file, the axes paths ([0] = x , [1] = y), otherwise can be null
	 * @param aspectRatio
	 *             keep the aspect ratio
	 * @param intensityScale
	 *             display the intensity scale side bar.
	 */
	public static void plotData(final AbstractPlottingSystem plottingSystem, 
								String fileName, 
								String fileExtension, 
								final String dataPath, 
								String[] axesPaths,
								final boolean aspectRatio, 
								final boolean intensityScale,
								DATA_TYPE type){
		if(fileExtension != null && fileExtension.equals("nxs") && type.equals(DATA_TYPE.DATA)){
//			if (ARPESFileDescriptor.isArpesFile(filename)) {
				try {
					DataHolder data = LoaderFactory.getData(fileName);
					Map<String, ILazyDataset> map = data.getMap();
					ILazyDataset value = map.get(dataPath).squeeze();

					ILazyDataset xAxis = axesPaths != null && axesPaths.length == 2 ? map.get(axesPaths[0]) : null;
					ILazyDataset yAxis = axesPaths != null && axesPaths.length == 2 ? map.get(axesPaths[1]) : null;
					plottingSystem.clear();
					if(value.getShape().length == 2) {
						AbstractDataset image = DatasetUtils.convertToAbstractDataset(value.getSlice(new Slice(null)));
						ArrayList<IDataset> axes = new ArrayList<IDataset>(2);
						if(xAxis != null && yAxis != null){
							axes.add(DatasetUtils.convertToAbstractDataset(xAxis.getSlice(new Slice(null))));
							axes.add(DatasetUtils.convertToAbstractDataset(yAxis.getSlice(new Slice(null))));
							axes.get(0).setName("");
							axes.get(1).setName("");
						}
						
						IImageTrace imageTrace = plottingSystem.createImageTrace(plottingSystem.getPlotName());

						if(axes.isEmpty())
							imageTrace.setData(image, null, true);
						else
							imageTrace.setData(image, axes, true);
						plottingSystem.addTrace(imageTrace);
						plottingSystem.repaint(true);
						plottingSystem.setKeepAspect(aspectRatio);
						plottingSystem.setShowIntensity(intensityScale);
					} else {
						logger.warn("Dataset not the right shape for showing in the preview");
					}
					
				} catch (Exception e) {
					logger.error("Something went wrong when creating a overview plot",e);
				}
//			}
		} else if(fileExtension != null && (fileExtension.equals("tif")||fileExtension.equals("tiff"))){
			final TIFFImageLoader tiffLoader = new TIFFImageLoader(fileName);
			Job tiffJob = new Job("Loading tiff image") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IMonitor mon = new ProgressMonitorWrapper(monitor);
					try {
						DataHolder data = tiffLoader.loadFile(mon);

						Map<String, ILazyDataset> map = data.getMap();
						String name = data.getName(0);
						ILazyDataset tmpvalue = map.get(name);
						ILazyDataset value = tmpvalue.squeeze();
						plottingSystem.clear();
						if(value.getShape().length == 2) {
							final AbstractDataset image = DatasetUtils.convertToAbstractDataset(value.getSlice(new Slice(null)));
							final IImageTrace imageTrace = plottingSystem.createImageTrace(plottingSystem.getPlotName());

							DisplayUtils.runInDisplayThread(true, plottingSystem.getPlotComposite(), new Runnable() {
								@Override
								public void run() {
									imageTrace.setData(image, null, true);
									plottingSystem.addTrace(imageTrace);
									plottingSystem.repaint(true);
									plottingSystem.setKeepAspect(aspectRatio);
									plottingSystem.setShowIntensity(intensityScale);
								}
							});
							
						} else {
							logger.warn("Dataset not the right shape for showing in the preview");
							return Status.CANCEL_STATUS;
						}
					} catch (ScanFileHolderException e) {
						logger.error("Error loading Tiff image:", e);
						return Status.CANCEL_STATUS;
					}

					return Status.OK_STATUS;
				}
			};
			tiffJob.schedule();
		} else if(fileExtension != null && fileExtension.equals("cbf")){
			
		} else if(fileExtension != null && fileExtension.equals("img")){
			
		} 
		// if a mask
		else if(fileExtension != null && fileExtension.equals("nxs") && type.equals(DATA_TYPE.MASK)){
			
		}

	}
	
}
