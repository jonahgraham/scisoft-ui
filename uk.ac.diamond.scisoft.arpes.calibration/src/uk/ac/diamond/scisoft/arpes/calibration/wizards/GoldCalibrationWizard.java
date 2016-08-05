package uk.ac.diamond.scisoft.arpes.calibration.wizards;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class GoldCalibrationWizard extends Wizard implements INewWizard {

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
	public static final String TEMPERATURE = "/entry1/sample/temperature";
	public static final String PREVIOUS_PAGE = "previous";
	public static final String MU_DATA = "Mu";
	public static final String SAVE_PATH = "File save path";
	public static final String RESIDUALS_DATA = "residuals";
	public static final String FWHM_DATA = "fwhm";
	public static final String FITTED = "fitted";
	public static final String FUNCTION_FITTEDMU_DATA = "fitted data";
	protected GoldCalibrationPageOne one;
	protected GoldCalibrationPageTwo two;
	protected GoldCalibrationPageThree three;
	protected GoldCalibrationPageFour four;

	private DataMessageComponent calibrationData;
	private IPageChangedListener pageChangeListener;
	private IPageChangingListener pageChangingListener;
	private boolean isProcessOKToRun;
	private GoldCalibrationPageFive five;

	public GoldCalibrationWizard() {
		super();
		setNeedsProgressMonitor(true);
		calibrationData = new DataMessageComponent();
	}

	@Override
	public String getWindowTitle() {
		return "Gold Calibration Wizard";
	}

	@Override
	public boolean performFinish() {
		CalibrationWizardPage page = (CalibrationWizardPage) getContainer().getCurrentPage();
		// if last page
		if (page.getPageNumber() == 5) {
			if (page.runProcess())
				return true;
		}
		return false;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		IWizardContainer wd = getContainer();
		if (wd instanceof WizardDialog) {
			((WizardDialog) wd).addPageChangedListener(pageChangeListener);
			((WizardDialog) wd).addPageChangingListener(pageChangingListener);
		}
		getShell().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					event.doit = false;
				}
			}
		});
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		one = new GoldCalibrationPageOne(calibrationData);
		two = new GoldCalibrationPageTwo(calibrationData);
		three = new GoldCalibrationPageThree(calibrationData);
		four = new GoldCalibrationPageFour(calibrationData);
		five = new GoldCalibrationPageFive(calibrationData);
		pageChangeListener = new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {
				if (event.getSelectedPage() instanceof CalibrationWizardPage) {
					CalibrationWizardPage page = (CalibrationWizardPage) event.getSelectedPage();
					// not the last page
					if (isProcessOKToRun && page.getPageNumber() != 5)
						page.runProcess();
				}
			}
		};
		pageChangingListener = new IPageChangingListener() {
			@Override
			public void handlePageChanging(PageChangingEvent event) {
				CalibrationWizardPage currentpage = (CalibrationWizardPage) event.getCurrentPage();
				CalibrationWizardPage targetpage = (CalibrationWizardPage) event.getTargetPage();
				if (currentpage.getPageNumber() > targetpage.getPageNumber())
					isProcessOKToRun = false;
				else
					isProcessOKToRun = true;
			}
		};
		
		addPage(one);
		addPage(two);
		addPage(three);
		addPage(four);
		addPage(five);
		
		Object selected = selection.getFirstElement();
		String path = "";
		if (selected instanceof IFile) {
			IFile ifile = (IFile)selected;
			path = ifile.getLocation().toOSString();
		} else if (selected instanceof File) {
			File file = (File) selected;
			path = file.getPath();
		}
		setData(path);
	}

	private void setData(String path) {
		try {
			IDataHolder holder = LoaderFactory.getData(path);
//			String[] names = holder.getNames();
			ILazyDataset data = holder.getLazyDataset(DATANAME);
			IDataset slicedData = data.getSlice(new Slice(0, data.getShape()[0], data.getShape()[1])).squeeze();
			ILazyDataset xaxis = holder.getLazyDataset(XAXIS_DATANAME);
			IDataset slicedXaxis = xaxis.getSlice(new Slice(0, xaxis.getShape()[0], xaxis.getElementsPerItem())).squeeze();
			ILazyDataset yaxis = holder.getLazyDataset(YAXIS_DATANAME);
			IDataset slicedYaxis = yaxis.getSlice(new Slice(0, yaxis.getShape()[0], yaxis.getElementsPerItem())).squeeze();
			slicedXaxis.setName("energy");
			slicedYaxis.setName("angle");
			ILazyDataset temp = holder.getLazyDataset(TEMPERATURE);
			double temperature = temp.getSlice(new Slice(0, temp.getShape()[0], temp.getElementsPerItem())).getDouble(0);

			calibrationData.addList(DATANAME, slicedData);
			calibrationData.addList(XAXIS_DATANAME, slicedXaxis);
			calibrationData.addList(YAXIS_DATANAME, slicedYaxis);
			calibrationData.addUserObject(TEMPERATURE, temperature);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean canFinish() {
		CalibrationWizardPage page = (CalibrationWizardPage)getContainer().getCurrentPage();
		if (page.getPageNumber() == 5)
			return true;
		return false;
	}

}