package uk.ac.diamond.scisoft.arpes.calibration;

import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class ServiceHolder {

	private static INexusFileFactory nexusFactory;

	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}

	public static void setNexusFactory(INexusFileFactory nexusFactory) {
		ServiceHolder.nexusFactory = nexusFactory;
	}
}
