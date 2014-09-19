/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.server;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import uk.ac.diamond.sda.polling.Activator;
import uk.ac.diamond.sda.polling.jobs.AbstractPollJob;
import uk.ac.diamond.sda.polling.jobs.JobParameters;
import uk.ac.diamond.sda.polling.monitor.IPollMonitor;
import uk.ac.diamond.sda.polling.monitor.NullPollMonitor;
import uk.ac.diamond.sda.polling.preferences.PreferenceConstants;

public class PollServer implements IPropertyChangeListener {

	private static final String CLASS = "Class";

	private static PollServer pollServer = null;

	private static Thread schedulerThread = null;

	private Collection<AbstractPollJob> pollJobs = new ArrayList<AbstractPollJob>();

	private File pollFileDirectory;

	private ArrayList<PollJobContribution> jobClassList = null;

	protected IPollMonitor pollMonitor = null;

	public static PollServer getInstance() {
		if (pollServer == null) {
			pollServer = new PollServer();
			pollServer.setDirectory(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PATH));
		}
		return pollServer;
	}

	public PollServer() {
		super();
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		pollMonitor = new NullPollMonitor();
		runSheduler();
	}

	private void setDirectory(String directoryName) {
		clearAllJobs();
		pollFileDirectory = new File(directoryName);
		addJobsFromDirectory();
	}

	public void refresh() {
		setDirectory(pollFileDirectory.getAbsolutePath());
	}

	private void clearAllJobs() {
		pollJobs.clear();
	}

	public void setPollMonitor(IPollMonitor pollMonitor) {
		this.pollMonitor = pollMonitor;
	}

	public void addJob(AbstractPollJob job) {
		pollMonitor.jobAdded(job);

		// need to associate this with a
		pollJobs.add(job);
	}

	public void runSheduler() {
		try {
			stopSheduler();
		} catch (InterruptedException e) {
			// Do nothing, it just means this may leave a thread lying around for a bit until it dies
		}
		schedulerThread = new Thread(new PollScheduler(this));
		PollScheduler.SCHEDULER_RUNNING = true;
		schedulerThread.start();
	}

	public void stopSheduler() throws InterruptedException {
		if (schedulerThread != null) {
			PollScheduler.SCHEDULER_RUNNING = false;
			schedulerThread.join();
		}

		for (AbstractPollJob job : pollJobs) {
			job.setStatus("Paused");
		}
	}

	public Collection<AbstractPollJob> getPollJobs() {
		return pollJobs;
	}

	public List<PollJobContribution> getPollJobClasses() {

		if (jobClassList != null) {
			return jobClassList;
		}

		jobClassList = new ArrayList<PollJobContribution>();

		IExtension[] extensions = getExtensions("uk.ac.diamond.sda.polling.pollTask");

		for (int i = 0; i < extensions.length; i++) {

			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();

			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement config = configElements[j];

				jobClassList.add(PollJobContribution.getPollJobContribution(config));

			}
		}

		return jobClassList;
	}

	private AbstractPollJob createJobFromFile(String fileName) throws IOException, CoreException,
			IllegalArgumentException {

		JobParameters jobParameters = new JobParameters(fileName);

		for (PollJobContribution jobContribution : getPollJobClasses()) {
			String contributionClassName = jobContribution.getClassName();
			String jobParametersClassName = jobParameters.get(CLASS);
			if (jobParametersClassName != null && jobParametersClassName.contains(contributionClassName)) {
				return jobContribution.getJob(jobParameters);
			}
		}

		return null;
	}

	private IExtension[] getExtensions(String extensionPointId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
		IExtension[] extensions = point.getExtensions();
		return extensions;
	}

	private void addJobsFromDirectory() {
		if (pollFileDirectory.isDirectory()) {

			for (String name : pollFileDirectory.list()) {

				try {
					File file = new File(pollFileDirectory, name);
					if (file.canRead() && file.isFile()) {
						AbstractPollJob job = createJobFromFile(file.getAbsolutePath());
						if (job != null) {
							addJob(job);
						}
					}
				} catch (Exception e) {
					// dont need to worry about this to much, best not to stop things working
					e.printStackTrace();
				}
			}
		}
	}

	public String getNewJobFileName(PollJobContribution pollJobContribution) throws IOException {

		// first check the directory exists, and if it doesn't create it
		if (!pollFileDirectory.exists()) {
			if (!pollFileDirectory.mkdirs()) {
				throw new IOException(
						"Failed to access poll folder, please check your polling configuration directory in the polling preferences (Window->Preferences->Polling preferences)");
			}
		}

		String classname = pollJobContribution.getClassName();

		String[] classnamechunks = classname.split("\\.");
		String name = classnamechunks[classnamechunks.length - 1];

		File file = new File(pollFileDirectory, name + ".txt");
		Integer number = 0;

		while (file.exists()) {
			file = new File(pollFileDirectory, name + number.toString() + ".txt");
			number += 1;
		}

		// now need to populate that file with some standard input
		FileOutputStream fout = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos));

		String lines = pollJobContribution.getExampleConfigText().replace("\\n", "\n");

		bw.write(lines);

		bw.flush();
		bw.close();

		return file.getAbsolutePath();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (property == PreferenceConstants.P_PATH) {
			String pollserverDirectory = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_PATH);
			setDirectory(pollserverDirectory);
		}

	}

	public void removeJob(AbstractPollJob job) {
		pollJobs.remove(job);
		deleteFile(job);
	}

	private void deleteFile(AbstractPollJob job) {
		File file = new File(job.getJobParametersFilename());
		file.delete();
	}

	public void removeAllJobs() {
		for (AbstractPollJob job : pollJobs) {
			deleteFile(job);
		}

		pollJobs.clear();
	}

	public void shutdown() throws InterruptedException {

		stopSheduler();

		boolean finished = false;
		while (!finished) {

			finished = true;

			for (AbstractPollJob job : pollJobs) {
				if (!job.cancel()) {
					finished = false;
				}
			}
		}

		pollJobs.clear();
	}
}
