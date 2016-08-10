package uk.ac.diamond.scisoft.arpes.calibration.functions;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.fitting.functions.FermiGauss;
import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationWizard;

public class PrepareFermiGaussianFunction {
	private static final Integer FIT_DIRECTION = 1;
	private FermiGauss fg;

	public PrepareFermiGaussianFunction(DataMessageComponent calibrationData) {
		Integer fitDim = FIT_DIRECTION;
		// get the required datasets
		Dataset dataDS = DatasetFactory.createFromObject(calibrationData.getList(GoldCalibrationWizard.REGION_DATANAME));
		int[] shape = dataDS.getShape();
		
		Dataset xAxisDS = DatasetFactory.createFromObject(calibrationData.getList(GoldCalibrationWizard.ENERGY_AXIS));
		if (xAxisDS == null)
			xAxisDS = DatasetFactory.createRange(shape[fitDim], 0, -1, Dataset.FLOAT64);

		Double temperatureValue = 10.0;
		try {
			temperatureValue = (Double) calibrationData.getUserObject(GoldCalibrationWizard.TEMPERATURE_PATH);
		} catch (Exception e) {
			// TODO: Should log something.
		}
		
		fg = new FermiGauss();
		
		// Mu
		double min = (Float)dataDS.min(true);
		double height = (Float)dataDS.max(true) - (Float)dataDS.min(true);
		int crossing = Maths.abs(Maths.subtract(dataDS, (min+(height/2.0)))).minPos()[0];

		fg.getParameter(0).setValue(xAxisDS.getDouble(crossing));
		fg.getParameter(0).setLowerLimit((Double)xAxisDS.min(true));
		fg.getParameter(0).setUpperLimit((Double)xAxisDS.max(true));
		
		// Temperature
		fg.getParameter(1).setValue(temperatureValue);
		fg.getParameter(1).setLowerLimit(0.0);
		fg.getParameter(1).setUpperLimit(300.0);
		fg.getParameter(1).setFixed(true);
		
		// BG Slope
		fg.getParameter(2).setValue(0.0);
		fg.getParameter(2).setLowerLimit(-10000.0);
		fg.getParameter(2).setUpperLimit(10000.0);
		
		// Step Height
		fg.getParameter(3).setValue(height);
		fg.getParameter(3).setLowerLimit(0.0);
		fg.getParameter(3).setUpperLimit(height*2);
		
		// Constant
		fg.getParameter(4).setValue(min);
		fg.getParameter(4).setLowerLimit(0.0);
		fg.getParameter(4).setUpperLimit((Float)dataDS.min(true)*2);
		
		// FWHM
		fg.getParameter(5).setValue(0.001);
		fg.getParameter(5).setLowerLimit(0.001);
		fg.getParameter(5).setUpperLimit(0.1);
		
		fg.setName("fermi");

		// Update the names of the axis so that plotting works more nicely later on
		// xAxisDS.setName("Energy");
		// dataDS.setName("Intensity");
		// result.addList(dataset, dataDS);
		// result.addList(xAxis, xAxisDS);
	}

	public IFunction getPreparedFunction() {
		return fg;
	}
}
