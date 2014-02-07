/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.mappingexplorer.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView2dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.oned.OneDMappingView;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDMappingView;
import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5Helper.TYPE;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;

/**
 * ONLY USED FOR TESTING. Provider to the 2D mapping view
 */
public class MappingViewPageProvider extends EditorPart {
	private static final Logger logger = LoggerFactory.getLogger(MappingViewPageProvider.class);
	public final static String ID = "uk.ac.diamond.scisoft.mappingexplorer.test.TWODMappingViewPageProvider";

	// private HDF5Loader hdfxp;

	public MappingViewPageProvider() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.None);

		root.setLayout(new FillLayout());
		// hdfxp = new HDF5Loader();
		Button btnOpensep2DView = new Button(root, SWT.None);
		btnOpensep2DView.setText("Open Separate 2D View");
		btnOpensep2DView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(TwoDMappingView.ID, getPartName(), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		});
		Button btnOpenCommon2DView = new Button(root, SWT.None);
		btnOpenCommon2DView.setText("Open Common 2D View");
		btnOpenCommon2DView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(TwoDMappingView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		});

		Button btnOpensep1DView = new Button(root, SWT.None);
		btnOpensep1DView.setText("Open Separate 1D View");
		btnOpensep1DView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(OneDMappingView.ID, getPartName(), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		});
		Button btnOpenCommon1DView = new Button(root, SWT.None);
		btnOpenCommon1DView.setText("Open Common 1D View");
		btnOpenCommon1DView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(OneDMappingView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		});
	}

	@Override
	public void setFocus() {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		IEditorInput editorInput2 = getEditorInput();
		if (editorInput2 instanceof FileEditorInput) {
			FileEditorInput fep = (FileEditorInput) editorInput2;
			IFile file = fep.getFile();
			final String fileName = file.getLocation().toOSString();
			try {
				if (adapter == IMappingView3dData.class) {
					Hdf5HelperData readDataSet = Hdf5Helper.getInstance()
							.readDataSetAll(fileName, "entry1/excalibur_summary_ad", "data",true);
					Object data = readDataSet.data;
					if (data instanceof int[]) {
						int[] ds = (int[]) data;
						int[] dims = new int[readDataSet.dims.length];
						int count = 0;
						for (long dim : readDataSet.dims) {
							dims[count++] = (int) dim;
						}
						IntegerDataset integerDataset = new IntegerDataset(ds, dims);


						String dim0 = null;
						try {
							dim0 = Hdf5Helper.getInstance().readAttributeAsString(fileName, TYPE.GROUP,
									"entry1/instrument/excalibur_summary_ad", "dim0");
						} catch (Exception e) {
							logger.error(e1.getMessage(), e);
						}
						Object data0 = Hdf5Helper.getInstance().readDataSetAll(fileName, "entry1/excalibur_summary_ad", dim0, true).data;
						double[] d0 = getDoubleArray(data0);

						String dim1 = null;
						try {
							dim1 = Hdf5Helper.getInstance().readAttributeAsString(fileName, TYPE.GROUP,
									"entry1/instrument/excalibur_summary_ad", "dim1");
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
						Object data1 = Hdf5Helper.getInstance().readDataSetAll(fileName, "entry1/excalibur_summary_ad", dim1, true).data;
						double[] d1 = getDoubleArray(data1);

						String dim2 = null;
						try {
							dim2 = Hdf5Helper.getInstance().readAttributeAsString(fileName, TYPE.GROUP,
									"entry1/instrument/excalibur_summary_ad", "dim2");
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
						Object data2 = Hdf5Helper.getInstance().readDataSetAll(fileName, "entry1/excalibur_summary_ad", dim2, true).data;
						double[] d2 = getDoubleArray(data2);
						String[] nameLabels = { dim0, dim1, dim2};

						return new DataIn3D(integerDataset, nameLabels, d0, d1, d2);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return super.getAdapter(adapter);
	}

	private double[] getDoubleArray(Object data1) {
		if (data1 instanceof double[]) {
			return (double[]) data1;
		} else if (data1 instanceof int[]) {
			int[] is = (int[]) data1;
			double[] d = new double[is.length];
			for (int i = 0; i < is.length; i++) {
				d[i] = is[i];
			}
			return d;
		}
		return null;
	}

	private class DataIn2D implements IMappingView2dData {
		protected final String[] nameLabels;
		protected final ILazyDataset ds;
		private final double[] data1;
		private final double[] data2;

		public DataIn2D(ILazyDataset ds, String[] nameLabels, double[] data1, double[] data2) {
			this.ds = ds;
			this.nameLabels = nameLabels;
			this.data1 = data1;
			this.data2 = data2;
		}

		@Override
		public ILazyDataset getDataSet() {
			if (ds.getShape().length > 2) {
				int[] shape = ds.getShape();
				IDataset slice = ds.getSlice(new Slice(null), new Slice(null), new Slice(0, 1));
				slice.setShape(shape[0], shape[1]);
				return slice;
			} else if (ds.getShape().length == 2) {
				return ds;
			}

			return null;
		}

		@Override
		public String getDimension1Label() {
			return nameLabels[0];
		}

		@Override
		public String getDimension2Label() {
			return nameLabels[1];
		}

		@Override
		public double[] getDimension1Values() {
			return data1;
		}

		@Override
		public double[] getDimension2Values() {
			return data2;
		}

	}

	private class DataIn3D extends DataIn2D implements IMappingView3dData {

		private final double[] data3;

		public DataIn3D(ILazyDataset ds, String[] nameLabels, double[] data1, double[] data2, double[] data3) {
			super(ds, nameLabels, data1, data2);
			this.data3 = data3;
		}

		@Override
		public String getDimension3Label() {
			return nameLabels[2];
		}

		@Override
		public ILazyDataset getDataSet() {
			return ds;
		}

		@Override
		public double[] getDimension3Values() {
			return data3;
		}
	}

	@Override
	public String getPartName() {
		return getFileName();
	}

	private String getFileName() {
		return ((FileEditorInput) getEditorInput()).getFile().getName();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
}
