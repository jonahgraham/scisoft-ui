package uk.ac.diamond.scisoft.qstatmonitor.views;

import java.util.ArrayList;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.UIJob;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.qstatmonitor.Activator;
import uk.ac.diamond.scisoft.qstatmonitor.QStatMonitorPreferencePage;
import uk.ac.diamond.scisoft.qstatmonitor.api.Utils;

public class QStatMonitorView extends ViewPart {

	public static final String ID = "uk.ac.diamond.qstatmonitor.views.QStatMonitorView";

	private Table table;
	private final String[] titles = { "Job Number", "Priority", "Job Name",
			"Owner", "State", "Submission Time", "Queue Name", "Slots", "Tasks" };

	private ArrayList<String> jobNumberList = new ArrayList<String>();
	private ArrayList<String> priorityList = new ArrayList<String>();
	private ArrayList<String> jobNameList = new ArrayList<String>();
	private ArrayList<String> ownerList = new ArrayList<String>();
	private ArrayList<String> stateList = new ArrayList<String>();
	private ArrayList<String> submissionTimeList = new ArrayList<String>();
	private ArrayList<String> queueNameList = new ArrayList<String>();
	private ArrayList<String> slotsList = new ArrayList<String>();
	private ArrayList<String> tasksList = new ArrayList<String>();

	private ArrayList<Double> timeList = new ArrayList<Double>();
	private ArrayList<Integer> suspendedList = new ArrayList<Integer>();
	private ArrayList<Integer> runningList = new ArrayList<Integer>();
	private ArrayList<Integer> queuedList = new ArrayList<Integer>();

	private int sleepTimeMilli;
	private String qStatQuery;
	private String userArg;
	private boolean plotOption;

	long startTime = System.nanoTime();

	private final RefreshAction refreshAction = new RefreshAction();
	private final OpenPreferencesAction openPreferencesAction = new OpenPreferencesAction();
	private TableUpdaterThread tableUpdaterThread;

	/*
	 * Runs the Qstat query and stores the resulting items in relevant arrays,
	 * then calls the redrawing of the table.
	 */
	private final Job getQstatInfoJob = new Job("Fetching QStat Info") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ArrayList<String>[] lists = Utils.getTableLists(qStatQuery,
						userArg);
				jobNumberList = lists[0];
				priorityList = lists[1];
				jobNameList = lists[2];
				ownerList = lists[3];
				stateList = lists[4];
				submissionTimeList = lists[5];
				queueNameList = lists[6];
				slotsList = lists[7];
				tasksList = lists[8];

				redrawTable();
			} catch (StringIndexOutOfBoundsException e) {
				stopThreadAndJobs();
			} catch (NullPointerException npe) {
				stopThreadAndJobs();
				updateContentDescriptionError();
			}
			return Status.OK_STATUS;
		}
	};

	/*
	 * removes all current items from the table, then adds the contents of the
	 * arrays to the relevant columns, then packs the table
	 */
	private final UIJob redrawTableJob = new UIJob("Redrawing Table") {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				table.removeAll();
				for (int i = 0; i < jobNumberList.size(); i++) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, jobNumberList.get(i));
					item.setText(1, priorityList.get(i));
					item.setText(2, jobNameList.get(i));
					item.setText(3, ownerList.get(i));
					item.setText(4, stateList.get(i));
					item.setText(5, submissionTimeList.get(i));
					item.setText(6, queueNameList.get(i));
					item.setText(7, slotsList.get(i));
					item.setText(8, tasksList.get(i));
				}
				packTable();
				updateContentDescription();
			} catch (SWTException e) {
				stopThreadAndJobs();
			}
			return Status.OK_STATUS;
		}
	};

	private final UIJob replotJob = new UIJob("Replotting") {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			plotResults();
			return Status.OK_STATUS;
		}
	};

	private final Runnable setContentDescriptionOnError = new Runnable() {
		@Override
		public void run() {
			setContentDescription("Inalid Qstat query entered.");
		}
	};

	/*
	 * Runs a loop which updates the table
	 */
	private class TableUpdaterThread extends Thread {
		private boolean runCondition = true;

		@Override
		public void run() {
			while (runCondition) {
				try {
					sleep(sleepTimeMilli);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (runCondition) {
					updateTable();
					if (plotOption) {
						updateListsAndPlot();
					}
				}
			}
		}

		public void stopThread() {
			runCondition = false;
		}
	};

	/**
	 * Resets the time and clears the plot lists
	 */
	public void resetPlot() {
		startTime = System.nanoTime();
		timeList.clear();
		suspendedList.clear();
		runningList.clear();
		queuedList.clear();
	}

	/**
	 * Setter for plotOption If option is true; opens the plot view
	 * 
	 * @param option
	 */
	public void setPlotOption(boolean option) {
		this.plotOption = option;
		if (option) {
			try {
				final PlotView view = (PlotView) EclipseUtils.getPage()
						.showView(
								"uk.ac.diamond.scisoft.qstatMonitor.qstatPlot");
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Constructor, gets preferences from the preference store and stores them
	 * as variables within this object
	 */
	public QStatMonitorView() {

		final IPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.qstatMonitor");
		sleepTimeMilli = store.getInt(QStatMonitorPreferencePage.SLEEP) * 1000;
		qStatQuery = store.getString(QStatMonitorPreferencePage.QUERY);
		userArg = store.getString(QStatMonitorPreferencePage.USER);
		if (!store.getBoolean(QStatMonitorPreferencePage.DISABLE_AUTO_REFRESH)) {
			tableUpdaterThread = new TableUpdaterThread();
			tableUpdaterThread.start();
		}

		plotOption = !store
				.getBoolean(QStatMonitorPreferencePage.DISABLE_AUTO_PLOT);

	}

	/**
	 * Updates the plot lists
	 */
	private void updatePlotLists() {
		timeList.add(getElapsedMinutes());
		int suspended = 0;
		int running = 0;
		int queued = 0;
		for (int i = 0; i < jobNumberList.size(); i++) {
			if (stateList.get(i).equalsIgnoreCase("s")) {
				suspended += Integer.parseInt(slotsList.get(i));
			} else {
				if (stateList.get(i).equalsIgnoreCase("r")) {
					running += Integer.parseInt(slotsList.get(i));
				} else {
					if (stateList.get(i).contains("q")
							|| stateList.get(i).contains("Q")) {
						queued += Integer.parseInt(slotsList.get(i));
					}
				}
			}
		}
		suspendedList.add(suspended);
		runningList.add(running);
		queuedList.add(queued);
	}

	/**
	 * Gets the time in minutes since the time was last reset
	 * 
	 * @return
	 */
	private double getElapsedMinutes() {
		long estimatedTime = System.nanoTime() - startTime;
		return estimatedTime / 60000000000.0;
	}
	

	/**
	 * Calles updatePlotLists(), then schedules the replotJob
	 */
	private void updateListsAndPlot() {
		updatePlotLists();
		replotJob.cancel();
		replotJob.schedule();
	}

	/**
	 * Plots the plot list values to the plot view
	 */
	private void plotResults() {
		if (!timeList.isEmpty()) {

			PlotView view = null;
			try {
				view = (PlotView) PlatformUI
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.findView(
								"uk.ac.diamond.scisoft.qstatMonitor.qstatPlot");
			} catch (NullPointerException e) {
				stopThreadAndJobs();
			}

			DoubleDataset timeDataset = (DoubleDataset) DoubleDataset
					.createFromList(timeList);
			timeDataset.setName("Time (mins)");

			Dataset suspendedDataset = IntegerDataset
					.createFromList(suspendedList);
			suspendedDataset.setName("Suspended");

			Dataset queuedDataset = IntegerDataset
					.createFromList(queuedList);
			queuedDataset.setName("Queued");

			Dataset runningDataset = IntegerDataset
					.createFromList(runningList);
			runningDataset.setName("Running");
			
					
			Dataset[] datasetArr = { suspendedDataset, queuedDataset,
					runningDataset};
			
			//ArrayList<Dataset> list = new ArrayList<Dataset>();
			//list.add(suspendedDataset);
			//list.add(queuedDataset);
			//list.add(runningDataset);

			if (view != null) {
				try {
					SDAPlotter.plot("QStat Monitor Plot", timeDataset,	datasetArr);
					//SDAPlotter.plot("QStat Monitor Plot", timeDataset,	list.toArray(new Dataset[3]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("nulll");
			}

		}

	}

	@Override
	public void createPartControl(Composite parent) {

		// Create action bar and add actions to it
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(openPreferencesAction);
		bars.getToolBarManager().add(refreshAction);

		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}

		updateTable();
		redrawTable();

		if (plotOption) {
			try {
				final PlotView view = (PlotView) EclipseUtils.getPage()
						.showView(
								"uk.ac.diamond.scisoft.qstatMonitor.qstatPlot");
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			updateListsAndPlot();
		}

	}

	/**
	 * schedules the getQstatInfoJob, cancelling it if it is already running
	 */
	public void updateTable() {
		getQstatInfoJob.cancel();
		getQstatInfoJob.schedule();
	}

	/**
	 * schedules the redrawTableJob, cancelling it if it is already running
	 */
	private void redrawTable() {
		redrawTableJob.cancel();
		redrawTableJob.schedule();
	}

	/**
	 * Packs each column of the table so that all titles and items are visible
	 * without resizing
	 */
	private void packTable() {
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		stopThreadAndJobs();
		super.dispose();
	}

	/**
	 * stops the updaterThread, getQstatInfoJob and redrawTableJob
	 */
	private void stopThreadAndJobs() {
		if (tableUpdaterThread != null) {
			tableUpdaterThread.stopThread();
		}
		getQstatInfoJob.cancel();
		redrawTableJob.cancel();
	}

	/**
	 * Updates the content description to show the number of tasks been
	 * displayed in the table
	 */
	private void updateContentDescription() {
		int numItems = jobNumberList.size();
		if (numItems == 1) {
			setContentDescription("Showing 1 task.");
		} else {
			setContentDescription("Showing " + numItems + " tasks.");
		}
	}

	private void updateContentDescriptionError() {
		PlatformUI.getWorkbench().getDisplay()
				.asyncExec(setContentDescriptionOnError);
	}

	/**
	 * setter for sleepTimeMilli
	 * 
	 * @param seconds
	 *            new value in seconds
	 */
	public void setSleepTimeSecs(double seconds) {
		sleepTimeMilli = (int) Math.round(seconds * 1000);
	}

	/**
	 * setter for sleepTimeMilli
	 * 
	 * @param seconds
	 *            new value in milliseconds
	 */
	public void setSleepTimeMilli(int milliSeconds) {
		sleepTimeMilli = milliSeconds;
	}

	/**
	 * setter for qStatQuery
	 * 
	 * @param query
	 */
	public void setQuery(String query) {
		qStatQuery = query;
	}

	/**
	 * stops the updaterThread by setting its run condition to false
	 */
	public void stopRefreshing() {
		if (tableUpdaterThread != null) {
			tableUpdaterThread.stopThread();
		}
	}

	/**
	 * starts a new updaterThread, if one is already running it is stopped
	 */
	public void startRefreshing() {
		if (tableUpdaterThread != null && tableUpdaterThread.isAlive()) {
			tableUpdaterThread.stopThread();
		}
		tableUpdaterThread = new TableUpdaterThread();
		tableUpdaterThread.start();
	}

	/**
	 * setter for userArg
	 * 
	 * @param userID
	 */
	public void setUserArg(String userID) {
		userArg = userID;
	}

	/*
	 * **************************************** ACTION
	 * ********************************
	 */

	class RefreshAction extends Action {
		RefreshAction() {
			setText("Refresh table");
			setImageDescriptor(Activator.getDefault().getWorkbench()
					.getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		}

		public void run() {
			updateTable();
			redrawTable();
			updateListsAndPlot();
		}
	}

	class OpenPreferencesAction extends Action {
		OpenPreferencesAction() {
			setText("Preferences");
			setImageDescriptor(Activator.getDefault().getWorkbench()
					.getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		}

		public void run() {
			PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), QStatMonitorPreferencePage.ID, null,
					null);
			if (pref != null) {
				pref.open();
			}
		}
	}

}