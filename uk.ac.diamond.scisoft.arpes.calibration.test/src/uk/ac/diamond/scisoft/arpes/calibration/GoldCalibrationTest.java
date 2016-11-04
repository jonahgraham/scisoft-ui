package uk.ac.diamond.scisoft.arpes.calibration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.FermiGauss;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.arpes.calibration.functions.FermiGaussianFitter;
import uk.ac.diamond.scisoft.arpes.calibration.utils.ARPESCalibrationConstants;
import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationPageOne;

public class GoldCalibrationTest {
	private DataMessageComponent calibrationData;

	@Before
	public void prepare() {
		try {
			// load data
			IDataHolder holder = LoaderFactory.getData(getTestFilePath("i05-4856.nxs"));
			ILazyDataset data = holder.getLazyDataset(ARPESCalibrationConstants.DATANAME);
			IDataset slicedData = data.getSlice(new Slice(0, data.getShape()[0], data.getShape()[1])).squeeze();
			ILazyDataset xaxis = holder.getLazyDataset(ARPESCalibrationConstants.XAXIS_DATANAME);
			IDataset slicedXaxis = xaxis.getSlice(new Slice(0, xaxis.getShape()[0], xaxis.getElementsPerItem())).squeeze();
			ILazyDataset yaxis = holder.getLazyDataset(ARPESCalibrationConstants.YAXIS_DATANAME);
			IDataset slicedYaxis = yaxis.getSlice(new Slice(0, yaxis.getShape()[0], yaxis.getElementsPerItem())).squeeze();
			slicedXaxis.setName("energy");
			slicedYaxis.setName("angle");
			ILazyDataset temp = holder.getLazyDataset(ARPESCalibrationConstants.TEMPERATURE_PATH);
			double temperature = temp.getSlice(new Slice(0, temp.getShape()[0], temp.getElementsPerItem())).getDouble(0);
			// set message component
			calibrationData = new DataMessageComponent();
			calibrationData.addList(ARPESCalibrationConstants.DATANAME, slicedData);
			calibrationData.addList(ARPESCalibrationConstants.XAXIS_DATANAME, slicedXaxis);
			calibrationData.addList(ARPESCalibrationConstants.YAXIS_DATANAME, slicedYaxis);
			calibrationData.addUserObject(ARPESCalibrationConstants.TEMPERATURE_PATH, temperature);
			// * page one *//
			// set roi
			RectangularROI roi = new RectangularROI(new double[] { 538, 143 }, new double[] { 705, 815 });
			calibrationData.addROI(ARPESCalibrationConstants.REGION_NAME, roi);

			// set axes
			List<IDataset> dataAxes = Arrays.asList(new IDataset[] { slicedXaxis, slicedYaxis });
			List<IDataset> axes = GoldCalibrationPageOne.getSliceAxes(dataAxes, (RectangularROI)roi);
			IDataset xaxisData = axes.get(0);
			IDataset yaxisData = axes.get(1);
			calibrationData.addList(ARPESCalibrationConstants.ENERGY_AXIS, xaxisData);
			calibrationData.addList(ARPESCalibrationConstants.ANGLE_AXIS, yaxisData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void fitTest() {
		// set average
		IDataset slicedData = (IDataset) calibrationData.getList(ARPESCalibrationConstants.DATANAME);
		Dataset id = DatasetUtils.convertToDataset(slicedData);
		IROI roi = calibrationData.getROI(ARPESCalibrationConstants.REGION_NAME);
		IDataset averageData = ROIProfile.boxMean(id, null, (RectangularROI) roi, true)[0];
		averageData.setName("Intensity");
		assertEquals("average data has the expected shape", 167, averageData.getShape()[0]);

		calibrationData.addList(ARPESCalibrationConstants.AVERAGE_DATANAME, averageData);

		// set region data
		IDataset regionDataset = GoldCalibrationPageOne.getRegionData((RectangularROI) roi, calibrationData);
		assertEquals("Region dataset has the expected starting value", 1915.0, regionDataset.getDouble(0, 0), 0);
		assertEquals("Region dataset has the expected ending value", 295.0, regionDataset.getDouble(671, 166), 0);

		calibrationData.addList(ARPESCalibrationConstants.REGION_DATANAME, regionDataset);

		// set function with best fit for the ROI selected
		FermiGauss fg = new FermiGauss();
		// mu=65.67865
		fg.getParameter(0).setValue(65.67865258521087);
		fg.getParameter(0).setLowerLimit(65.5298122);
		fg.getParameter(0).setUpperLimit(65.79738760000001);
		// T=5.3737
		// Temperature
		fg.getParameter(1).setValue(5.3737);
		fg.getParameter(1).setLowerLimit(0.0);
		fg.getParameter(1).setUpperLimit(300.0);
		fg.getParameter(1).setFixed(true);
		// scaleM=-1960.80665
		// BG Slope
		fg.getParameter(2).setValue(-1960.2268711190447);
		fg.getParameter(2).setLowerLimit(-10000.0);
		fg.getParameter(2).setUpperLimit(10000.0);
		// scaleC=1222.65067
		// Step Height
		fg.getParameter(3).setValue(1222.7029957432776);
		fg.getParameter(3).setLowerLimit(0.0);
		fg.getParameter(3).setUpperLimit(4854.0);
		// C=301.3171
		// Constant
		fg.getParameter(4).setValue(301.3171);
		fg.getParameter(4).setLowerLimit(0.0);
		fg.getParameter(4).setUpperLimit(398.0);
		// fwhm=0.03019538
		// FWHM
		fg.getParameter(5).setValue(0.030196261265094036);
		fg.getParameter(5).setLowerLimit(0.001);
		fg.getParameter(5).setUpperLimit(0.1);
		fg.setName("fermi");
		calibrationData.addFunction(fg.getName(), fg);
		// run the fermi fitter
		FermiGaussianFitter fitter = new FermiGaussianFitter(calibrationData);
		fitter.fit(null);

		IDataset muData = (IDataset) calibrationData.getList(ARPESCalibrationConstants.MU_DATA);
		assertThat("mu data has been created", muData != null);
//		assertEquals("Mu data has the expected starting value", 65.65554, muData.getDouble(0), 0.00001);
//		assertEquals("Mu data has the expected ending value", 65.66521, muData.getDouble(671), 0.00001);
		// temperature
		IDataset tempData = (IDataset) calibrationData.getList(ARPESCalibrationConstants.TEMPERATURE);
		assertThat("tempData data has been created", tempData != null);
		assertEquals("Temp data has the expected starting value", 5.3737, tempData.getDouble(0), 0);
		assertEquals("Temp data has the expected ending value", 5.3737, tempData.getDouble(671), 0);
		// background slope
		IDataset bngSlope = (IDataset) calibrationData.getList(ARPESCalibrationConstants.BACKGROUND_SLOPE);
		assertThat("bngSlope data has been created", bngSlope != null);
//		assertEquals("Background slope data has the expected starting value", -2129.20444, bngSlope.getDouble(0), 0.00001);
//		assertEquals("Background slope data has the expected ending value", -2264.33415, bngSlope.getDouble(671), 0.00001);
		// fermi edge step height
		IDataset fermiEdge = (IDataset) calibrationData.getList(ARPESCalibrationConstants.FERMI_EDGE_STEP_HEIGHT);
		assertThat("fermiEdge data has been created", fermiEdge != null);
//		assertEquals("fermiEdge data has the expected starting value", 1127.93651, fermiEdge.getDouble(0), 0.00001);
//		assertEquals("fermiEdge data has the expected ending value", 1063.13730, fermiEdge.getDouble(671), 0.00001);

		// background
		IDataset bng = (IDataset) calibrationData.getList(ARPESCalibrationConstants.BACKGROUND);
		assertThat("bng data has been created", bng != null);
//		assertEquals("Background data has the expected starting value", 315.39261, bng.getDouble(0), 0.00001);
//		assertEquals("Background data has the expected ending value", 315.10838, bng.getDouble(671), 0.00001);

		IDataset fwhm = (IDataset) calibrationData.getList(ARPESCalibrationConstants.FWHM_DATA);
		assertThat("fwhm data has been created", fwhm != null);
//		assertEquals("Background slope data has the expected starting value", 0.004061, fwhm.getDouble(0), 0.00001);
//		assertEquals("Background slope data has the expected ending value", 0.0044011, fwhm.getDouble(671), 0.00001);

		IDataset residuals = (IDataset) calibrationData.getList(ARPESCalibrationConstants.RESIDUALS_DATA);
		assertThat("residuals data has been created", residuals != null);
//		assertEquals("fwhm data has the expected starting value", 4282014.76809, residuals.getDouble(0), 0.00001);
//		assertEquals("fwhm data has the expected ending value", 1024781.35223, residuals.getDouble(671), 0.00001);

		IDataset fitted = (IDataset) calibrationData.getList(ARPESCalibrationConstants.FITTED);
		assertThat("fitted data has been created", fitted != null);
//		assertEquals("fitted data has the expected starting value", 1749.2519, fitted.getDouble(0, 0), 0.0001);
//		assertEquals("fitted data has the expected ending value", 315.10838, fitted.getDouble(671, 166), 0.0001);

		IDataset fitImage = (IDataset) calibrationData.getList(ARPESCalibrationConstants.FIT_IMAGE);
		assertThat("fitImage data has been created", fitImage != null);
//		assertEquals("fitImage data has the expected starting value", 1709.677016, fitImage.getDouble(0, 0), 0.0001);
//		assertEquals("fitImage data has the expected ending value", 315.10838, fitImage.getDouble(671, 166), 0.0001);

		IDataset fitResiduals = (IDataset) calibrationData.getList(ARPESCalibrationConstants.FIT_RESIDUALS);
		assertThat("fitResiduals data has been created", fitResiduals != null);
//		assertEquals("fitResiduals data has the expected starting value", 4282014.7680, fitResiduals.getDouble(0), 0.00001);
//		assertEquals("fitResiduals data has the expected ending value", 1024781.35223, fitResiduals.getDouble(671), 0.00001);

	}

	private String getTestFilePath(String fileName) {
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	}
}
