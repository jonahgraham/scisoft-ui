package uk.ac.diamond.scisoft.qstatmonitor;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Logger {
	

	public static void log(String id, String msg) {
		ILog logger = Activator.getDefault().getLog();
		logger.log(new Status(IStatus.INFO, id, msg));
	}

}
