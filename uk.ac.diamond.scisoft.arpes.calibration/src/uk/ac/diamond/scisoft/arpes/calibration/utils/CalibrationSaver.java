package uk.ac.diamond.scisoft.arpes.calibration.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;
import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationWizard;

public class CalibrationSaver implements IRunnableWithProgress {

	private static final Logger logger = LoggerFactory.getLogger(CalibrationSaver.class);
	private DataMessageComponent calibrationData;
	private NexusFile nexus;

	public CalibrationSaver(DataMessageComponent calibrationData) {
		this.calibrationData = calibrationData;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Saving calibration data...", 8);
		try {
			String filePath = (String) calibrationData.getUserObject(GoldCalibrationWizard.SAVE_PATH);
			File file = new File(filePath);
			// check if overwite flag is true
			boolean isOverwrite = (boolean) calibrationData.getUserObject(GoldCalibrationWizard.OVERWRITE);
			if (!isOverwrite && file.exists()) {
				throw new InterruptedException("File already exist! please check the 'Overwrite' option.");
			} else if (isOverwrite && file.exists()) {
				file.delete();
			}
			nexus = ServiceHolder.getNexusFactory().newNexusFile(filePath);
			nexus.openToWrite(true);
			// save raw data
			IDataset data = (IDataset) calibrationData.getList(GoldCalibrationWizard.DATANAME);
			IDataset xaxis = (IDataset) calibrationData.getList(GoldCalibrationWizard.XAXIS_DATANAME);
			IDataset yaxis = (IDataset) calibrationData.getList(GoldCalibrationWizard.YAXIS_DATANAME);
			saveData(data, xaxis, yaxis, "/entry/calibration/analyser");
			monitor.worked(1);
			
			IDataset angleaxis = (IDataset) calibrationData.getList(GoldCalibrationWizard.ANGLE_AXIS);

			// save background
			IDataset backgroundData = (IDataset) calibrationData.getList(GoldCalibrationWizard.BACKGROUND);
			saveData(backgroundData, angleaxis, null, GoldCalibrationWizard.BACKGROUND);
			monitor.worked(1);
			// save background slope
			IDataset backgroundSlopeData = (IDataset) calibrationData.getList(GoldCalibrationWizard.BACKGROUND_SLOPE);
			saveData(backgroundSlopeData, angleaxis, null, GoldCalibrationWizard.BACKGROUND_SLOPE);
			monitor.worked(1);
			// save fermi_edge_step_height
			IDataset fermiEdgeStepHeightData = (IDataset) calibrationData.getList(GoldCalibrationWizard.FERMI_EDGE_STEP_HEIGHT);
			saveData(fermiEdgeStepHeightData, angleaxis, null, GoldCalibrationWizard.FERMI_EDGE_STEP_HEIGHT);
			monitor.worked(1);
			// save fitted
			IDataset fitteddata = (IDataset) calibrationData.getList(GoldCalibrationWizard.FITTED);
			IDataset energaxis = (IDataset) calibrationData.getList(GoldCalibrationWizard.ENERGY_AXIS);
			saveData(fitteddata, energaxis, angleaxis, GoldCalibrationWizard.FITTED);
			monitor.worked(1);
			// save fittedMu
			IDataset fittedMu = (IDataset) calibrationData.getList(GoldCalibrationWizard.FUNCTION_FITTEDMU_DATA);
			saveData(fittedMu, yaxis, null, GoldCalibrationWizard.FUNCTION_FITTEDMU_DATA);
			monitor.worked(1);
			// save fwhm
			IDataset fwhmData = (IDataset) calibrationData.getList(GoldCalibrationWizard.FWHM_DATA);
			saveData(fwhmData, angleaxis, null, GoldCalibrationWizard.FWHM_DATA);
			monitor.worked(1);
			// save mu
			IDataset muData = (IDataset) calibrationData.getList(GoldCalibrationWizard.MU_DATA);
			saveData(muData, angleaxis, null, GoldCalibrationWizard.MU_DATA);
			monitor.worked(1);
			// save residuals
			IDataset residualsData = (IDataset) calibrationData.getList(GoldCalibrationWizard.RESIDUALS_DATA);
			saveData(residualsData, angleaxis, null, GoldCalibrationWizard.RESIDUALS_DATA);
			monitor.worked(1);
			// save temperature
			IDataset temperatureData = (IDataset) calibrationData.getList(GoldCalibrationWizard.TEMPERATURE);
			saveData(temperatureData, angleaxis, null, GoldCalibrationWizard.TEMPERATURE);
			monitor.worked(1);
		} catch (NexusException e) {
			logger.error("Error writing to Nexus file:" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (nexus != null)
				try {
					nexus.close();
				} catch (NexusException e) {
					e.printStackTrace();
				}
		}
	}

	private void saveData(IDataset data, IDataset xaxisData, IDataset yaxisData, String dataNodePath) throws NexusException  {
		if (data == null)
			return;
		String[] nodes = dataNodePath.split("/");
		String nodePath = "";
		for (int i = 0; i < nodes.length; i++) {
			String node = nodes[i];
			if (!node.equals("")) {
				nodePath = nodePath.concat("/" + node);
				GroupNode group = nexus.getGroup(nodePath, true);
				if (i < nodes.length - 1) {
					nexus.addAttribute(group, new AttributeImpl(NexusTreeUtils.NX_CLASS, "NXEntry"));
				} else {
					nexus.addAttribute(group, new AttributeImpl(NexusTreeUtils.NX_CLASS, NexusTreeUtils.NX_DATA));
					data.setName(NexusTreeUtils.DATA);
					nexus.createData(group, data);
					if (yaxisData != null) {
						nexus.createData(group, yaxisData);
					}
					if (xaxisData != null) {
						nexus.createData(group, xaxisData);
					}
				}
			}
		}
	}
}
