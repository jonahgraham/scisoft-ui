package uk.ac.diamond.scisoft.arpes.calibration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.junit.Before;
import org.junit.Ignore;
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
		IDataHolder holder;
		try {
			// load data
			holder = LoaderFactory.getData(getTestFilePath("i05-4856.nxs"));
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
			// set average
			Dataset id = DatasetUtils.convertToDataset(slicedData);
			IDataset averageData = ROIProfile.boxMean(id, null, (RectangularROI) roi, true)[0];
			averageData.setName("Intensity");
			calibrationData.addList(ARPESCalibrationConstants.AVERAGE_DATANAME, averageData);
			// set region data
			IDataset regionDataset = GoldCalibrationPageOne.getRegionData((RectangularROI)roi, calibrationData);
			calibrationData.addList(ARPESCalibrationConstants.REGION_DATANAME, regionDataset);
			// set axes
			List<IDataset> dataAxes = Arrays.asList(new IDataset[] { slicedXaxis, slicedYaxis });
			List<IDataset> axes = GoldCalibrationPageOne.getSliceAxes(dataAxes, (RectangularROI)roi);
			IDataset xaxisData = axes.get(0);
			IDataset yaxisData = axes.get(1);
			calibrationData.addList(ARPESCalibrationConstants.ENERGY_AXIS, xaxisData);
			calibrationData.addList(ARPESCalibrationConstants.ANGLE_AXIS, yaxisData);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void fitTest() {
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
		final FermiGaussianFitter fitter = new FermiGaussianFitter(calibrationData);
		Job fitterJob = new Job("FitterJob") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				fitter.fit(new ProgressMonitorWrapper(monitor));
				return Status.OK_STATUS;
			}
		};
		fitterJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				IDataset muData = (IDataset)calibrationData.getList(ARPESCalibrationConstants.MU_DATA);
				// temperature
				IDataset tempData = (IDataset)calibrationData.getList(ARPESCalibrationConstants.TEMPERATURE);
				// background slope
				IDataset bngSlope = (IDataset)calibrationData.getList(ARPESCalibrationConstants.BACKGROUND_SLOPE);
				// fermi edge step height
				IDataset fermiEdge = (IDataset)calibrationData.getList(ARPESCalibrationConstants.FERMI_EDGE_STEP_HEIGHT);
				// background
				IDataset bng = (IDataset)calibrationData.getList(ARPESCalibrationConstants.BACKGROUND);
				IDataset fwhm = (IDataset)calibrationData.getList(ARPESCalibrationConstants.FWHM_DATA);

				IDataset residuals = (IDataset)calibrationData.getList(ARPESCalibrationConstants.RESIDUALS_DATA);
				IDataset fitted = (IDataset)calibrationData.getList(ARPESCalibrationConstants.FITTED);
				IDataset fitImage = (IDataset)calibrationData.getList(ARPESCalibrationConstants.FIT_IMAGE);
				IDataset fitResiduals = (IDataset)calibrationData.getList(ARPESCalibrationConstants.FIT_RESIDUALS);
				
				assertThat("fitter has finished", true);
				System.out.println();
			}
		});
		fitterJob.schedule();
	}

	private String getTestFilePath(String fileName) {
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	}
}
