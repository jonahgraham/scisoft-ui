package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

/**
 * Gets the plot connection.
 * @author fcp94556
 *
 */
public class PlotConnectionFactory {

	/**
	 * Get the connection whilst sheilding the concrete implementation.
	 * @param plotMode
	 * @param plottingSystem
	 * @return
	 */
	public static AbstractPlotConnection getConnection(GuiPlotMode plotMode, IPlottingSystem plottingSystem) {
		
		AbstractPlotConnection plotUI=null;
		if (plotMode.equals(GuiPlotMode.ONED)) {
			plottingSystem.setPlotType(PlotType.XY);
			plotUI = new Plotting1DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {
			plottingSystem.setPlotType(PlotType.XY_STACKED_3D);
			plotUI = new Plotting1DStackUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.TWOD)) {
			plottingSystem.setPlotType(PlotType.IMAGE);
			plotUI = new Plotting2DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SURF2D)) {
			plottingSystem.setPlotType(PlotType.SURFACE);
			plotUI = new Plotting2DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
//			plottingSystem.setPlotType(PlotType.SCATTER2D);
			plotUI = new PlottingScatter2DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SCATTER3D)) {
			plottingSystem.setPlotType(PlotType.XY_SCATTER_3D);
			plotUI = new PlottingScatter3DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.MULTI2D)) {
			plottingSystem.setPlotType(PlotType.MULTI_IMAGE);
			plotUI = new Plotting2DMultiUI(plottingSystem);
		}
		return plotUI;
	}
}
