package uk.ac.diamond.scisoft.arpes.calibration.utils;

public class ARPESCalibrationConstants {

	public static final String DATA_NODE = "/entry1/instrument/analyser/data";
	public static final String ENERGY_NODE = "/entry1/instrument/analyser/energies";
	public static final String ANGLE_NODE = "/entry1/instrument/analyser/angles";

	/**
	 * WIZARD constants
	 *
	 */
	public static final String DATANAME = "/entry1/analyser/data";
	public static final String XAXIS_DATANAME = "/entry1/analyser/energies";
	public static final String YAXIS_DATANAME = "/entry1/analyser/angles";
	public static final String AVERAGE_DATANAME = "average";
	public static final String ENERGY_AXIS = "energies_regionX";
	public static final String ANGLE_AXIS = "angles_regionY";
	public static final String REGION_DATANAME = "region_area_data";
	public static final String REGION_NAME = "roi";
	public static final String MEAN_DATANAME = "region_data_mean";
	public static final String FIT_UPDATE_SYSTEM = "FitUpdate System";
	public static final String MU_SYSTEM = "Mu System";
	public static final String RESOLUTION_SYSTEM = "Resolution System";
	public static final String RESIDUALS_SYSTEM = "residuals System";
	public static final String FIT_IMAGE = "fit_image";
	public static final String FIT_RESIDUALS = "fit_residuals";
	public static final String FIT_PARAMETER = "fit_parameter_";
	public static final String FUNCTION_NAME = "fermi";
	public static final String FUNCTION_FITTEDMU = "Fitted Mu";
	public static final String TEMPERATURE_PATH = "/entry1/sample/temperature";
	public static final String PREVIOUS_PAGE = "previous";
	public static final String SAVE_PATH = "File save path";
	public static final String OVERWRITE = "overwrite";
	
	/**
	 * saved data
	 */
	public static final String MU_DATA = "/entry/calibration/mu";
	public static final String RESIDUALS_DATA = "/entry/calibration/residuals";
	public static final String FWHM_DATA = "/entry/calibration/fwhm";
	public static final String FITTED = "/entry/calibration/fitted";
	public static final String FUNCTION_FITTEDMU_DATA = "/entry/calibration/fittedMu";
	public static final String BACKGROUND = "/entry/calibration/background";
	public static final String BACKGROUND_SLOPE = "/entry/calibration/backround_slope";
	public static final String FERMI_EDGE_STEP_HEIGHT = "/entry/calibration/fermi_edge_step_height";
	public static final String TEMPERATURE = "/entry/calibration/temperature";
}
