package uk.ac.diamond.scisoft.arpes.calibration.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.arpes.calibration.Activator;

public class ArpesFileEnabled {
	
	private static final Logger logger = LoggerFactory.getLogger(ArpesFileEnabled.class);

	public static boolean isArpesFile(String filename, IMonitor monitor) {
		
		// Reading the whole file is inefficient. Although for ARPES files
		// this may not be too bad - there are other HDF5 files which DAWN supports!
		// These grind to a halt as expanding the tree forces them to read all their 
		// data. Therefore we do NOT USE LoaderFactory to get a complete DataHolder.
		// Instead we do something cheap and fast.
		ILoaderService service = Activator.getService(ILoaderService.class);
		try {
			IDataHolder holder = service.getData(filename, true, monitor);
			ILazyDataset lazy = holder.getLazyDataset("/entry1/instrument/analyser/data");
			return lazy!=null;
		} catch (Exception e1) {
			return false;

		}
//		IHierarchicalDataFile file=null;
//		try {
//			file = HierarchicalDataFactory.getReader(filename);
//			Object node = file.getData("/entry1/instrument/analyser/data");
//			return node!=null;		
//		
//		} catch (Exception e) {
//			return false;
//		} finally {
//			if (file!=null)
//				try {
//					file.close();
//				} catch (Exception e) {
//					logger.error("Cannot close HDF5 file "+filename, e);
//				}
//		}
	}
	
	static public boolean isArpesFile(Object object) {
		
		if (object instanceof IFile) {
			String fileExtension = ((IFile) object).getFileExtension();
			if(fileExtension != null && fileExtension.equals("nxs")){
				return ArpesFileEnabled.isArpesFile(((IFile) object).getRawLocation().toOSString());
			}
		}
			
		if (object instanceof IStructuredSelection) {
			Object file = ((IStructuredSelection) object).getFirstElement();
			if (file instanceof IFile) {
				String fileExtension = ((IFile) file).getFileExtension();
				if(fileExtension != null && fileExtension.equals("nxs")){
					return ArpesFileEnabled.isArpesFile(((IFile) file).getRawLocation().toOSString());
				}
			}
		}
		return false;
	}
	
}
