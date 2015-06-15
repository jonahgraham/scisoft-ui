package uk.ac.diamond.scisoft.mrc.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dawnsci.commandserver.core.application.Consumer;
import org.dawnsci.commandserver.core.application.ConsumerProcess;
import org.dawnsci.commandserver.core.application.IConsumerExtension;
import org.dawnsci.commandserver.core.consumer.Constants;
import org.dawnsci.commandserver.core.consumer.ConsumerBean;
import org.dawnsci.commandserver.core.consumer.ConsumerStatus;
import org.dawnsci.commandserver.core.consumer.QueueReader;
import org.dawnsci.commandserver.core.util.JSONUtils;
import org.dawnsci.commandserver.ui.preference.CommandConstants;
import org.dawnsci.common.widgets.file.SelectorWidget;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view which starts and stops the consumers for the EM pipeline.
 * 
 * @author fcp94556
 *
 */
public class AnalysisControlView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisControlView.class);
	
	private static String MPATH = "uk.ac.diamond.scisoft.mrc.ui.monitorPath";
	private static String PPATH = "uk.ac.diamond.scisoft.mrc.ui.propertiesPath";
	private static String WPATH = "uk.ac.diamond.scisoft.mrc.ui.momlPath";
	
	private static String SEP_PROCESS = "uk.ac.diamond.scisoft.mrc.ui.separateProcess";
	private static String LCMD = "uk.ac.diamond.scisoft.mrc.ui.linuxCommand";
	private static String WCMD = "uk.ac.diamond.scisoft.mrc.ui.windowsCommand";

	
	private List<IConsumerExtension> consumerList;
		
	public AnalysisControlView() {
		Activator.getDefault().getPreferenceStore().setDefault(LCMD, "module load dawn/snapshot ; $DAWN_RELEASE_DIRECTORY/dawn");
		Activator.getDefault().getPreferenceStore().setDefault(WCMD, "C:/Users/fcp94556/Desktop/DawnMaster/dawn.exe");
		Activator.getDefault().getPreferenceStore().setDefault(SEP_PROCESS, true);
	}
	
	@Override
	public void createPartControl(Composite parent) { 
		
		parent.setLayout(new GridLayout(1,  false));
	
		final Composite content = new Composite(parent, SWT.BORDER);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label label = null;
		label = new Label(content, SWT.WRAP);
		label.setText("Monitor an EM collection at a give path and with specific properties.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		createSelector(content, "Monitor Directory    ",      MPATH, false, null, null);
		createSelector(content, "EM Properties         ",     PPATH, true, new String[]{"Properties"}, new String[]{"*.properties"});
		createSelector(content, "Pipeline Path          ",    WPATH, true, new String[]{"Workflow"},   new String[]{"*.moml"});	
		
		final Button useSeparateProcesses = new Button(content, SWT.CHECK);
		useSeparateProcesses.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		useSeparateProcesses.setText("Use separate processes");
		useSeparateProcesses.setToolTipText("Check on to start the consumers as standalone processes.\nThese will continue to process data collections even after the user interface has stopped.\nYou can also use 'consumer start XXX' from the command line.");
		useSeparateProcesses.setSelection(Activator.getDefault().getPreferenceStore().getBoolean(SEP_PROCESS));
        useSeparateProcesses.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		Activator.getDefault().getPreferenceStore().setValue(SEP_PROCESS, useSeparateProcesses.getSelection());
        	}       	
		});
		
		final Composite buttons = new Composite(content, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		final Button start = new Button(buttons, SWT.PUSH);
		start.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		start.setImage(Activator.getImage("icons/apply.gif"));
		start.setText("Start");
		final Button stop = new Button(buttons, SWT.PUSH);
		stop.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stop.setImage(Activator.getImage("icons/reset.gif"));
		stop.setText("Stop");

		start.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				start();
			}
		});
		
		stop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stop();
			}
		});

	}

	protected void stop()  {
		
		// We assume the consumers are going and send the terminate topic
		// if they are.
		if (consumerList==null || consumerList.isEmpty()) {
			final QueueReader<ConsumerBean>  reader    = new QueueReader<ConsumerBean>();
	        try {
				final Map<String, ConsumerBean>  consumers = reader.getHeartbeats(new URI(getURI()), Constants.ALIVE_TOPIC, ConsumerBean.class, Constants.NOTIFICATION_FREQUENCY+500);
				
				for (String name : consumers.keySet()) {
					
					if (!name.startsWith("EM ")) continue;
					ConsumerBean bean = consumers.get(name);
					
					bean.setStatus(ConsumerStatus.REQUEST_TERMINATE);
					bean.setMessage("Requesting a termination of "+bean.getName());
					try {
						JSONUtils.sendTopic(bean, Constants.TERMINATE_CONSUMER_TOPIC, new URI(getURI()));
					} catch (Exception e) {
						logger.error("Cannot terminate consumer "+bean.getName(), e);
					}
				}

	        } catch (Exception e1) {
				e1.printStackTrace();
			} 
	        
	    // If we started somne in the same VM, we can stop those directly.
		} else { // Same VM, do not broadcast exit or UI will stop.
			for (IConsumerExtension ext : consumerList) {
				try {
					ext.stop();
				} catch (Exception e) {
					logger.error("Cannot stop consumer", e);
				}
			}
			consumerList.clear();
		}
	}

	protected void start() {
		
		if (consumerList == null) consumerList = new ArrayList<IConsumerExtension>(3);
		startFolderMonitor(Activator.getDefault().getPreferenceStore().getString(MPATH),
				           Activator.getDefault().getPreferenceStore().getString(PPATH));
		
		startWorkflowRunner(Activator.getDefault().getPreferenceStore().getString(WPATH));
		
	}

	private void startWorkflowRunner(String momlPath) {
		
		final Map<String,String> conf = new HashMap<String,String>(13);
		conf.put("uri",          getURI());
		conf.put("submit",       "scisoft.diamond.FOLDER_QUEUE");
		conf.put("topic",        "scisoft.em.STATUS_TOPIC");
		conf.put("status",       "scisoft.em.STATUS_QUEUE");
		conf.put("bundle",       "org.dawnsci.commandserver.workflow");
		conf.put("consumer",     "org.dawnsci.commandserver.workflow.WorkflowConsumer");
		conf.put("consumerName", "EM Pipeline");
		conf.put("processName",  "em");
		conf.put("execLocation",    Activator.getDefault().getPreferenceStore().getString(LCMD));
		conf.put("winExecLocation", Activator.getDefault().getPreferenceStore().getString(WCMD));
		conf.put("momlLocation",  momlPath);
        start("Workflow Runner", conf);
	}

	private void startFolderMonitor(final String toMonitor, final String propertiesPath) {
		final Map<String,String> conf = new HashMap<String,String>(13);
		conf.put("uri",       getURI());
		conf.put("resursive", "false");
		conf.put("bundle",    "org.dawnsci.commandserver.foldermonitor");
		conf.put("consumer",  "org.dawnsci.commandserver.foldermonitor.Monitor");
		conf.put("topic",     "scisoft.diamond.FOLDER_TOPIC");
		conf.put("status",    "scisoft.diamond.FOLDER_QUEUE");
		conf.put("nio",        "false");
		conf.put("filePattern",".+\\.mrc");
		conf.put("extraProperties", propertiesPath);
		conf.put("location",   toMonitor);
		conf.put("consumerName", "EM File Monitor");
		conf.put("execLocation",    Activator.getDefault().getPreferenceStore().getString(LCMD));
		conf.put("winExecLocation", Activator.getDefault().getPreferenceStore().getString(WCMD));
        start("Folder Monitor", conf);
	}
	
	private String getURI() {
        final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.commandserver.ui");
        return store.getString(CommandConstants.JMS_URI);
	}

	private void start(final String name, final Map<String,String> props) {
		
		final Thread thread = new Thread(name) {
			public void run() {
				
				/*
				 *  Start consumer programmatically.
				 *  We write the properties to file in order to do this because
				 *  it is better than a long command line
				 */
				try {
					File tmp = File.createTempFile("consumer", ".properties");
					tmp.deleteOnExit();
					storeProperties(props, tmp);
					
					final Map<String, String> conf = new HashMap<String, String>(1);
					conf.put("properties", tmp.getAbsolutePath());
					
					boolean sep = Activator.getDefault().getPreferenceStore().getBoolean(SEP_PROCESS);
					if (sep) {
						ConsumerProcess process = new ConsumerProcess(tmp);
						process.start(); // We just leave it to run
						
					} else {
					    IConsumerExtension ext = Consumer.create(conf);
						consumerList.add(ext);
						ext.start(); // blocking! It will appear in the active consumer list.
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
 	}
	
	@SuppressWarnings("unchecked")
	private final static void storeProperties(@SuppressWarnings("rawtypes") final Map map, final File file) throws IOException {

		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) file.createNewFile();
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {			
			final Properties props = new Properties();
			props.putAll(map);
			props.store(out, "DAWN Consumer properties file");
			
		} finally {
			out.close();
		}
	}


	private void createSelector(Composite content, String label, final String propName, boolean resource, String[] types, String[] exts) {
		
		SelectorWidget selector = new SelectorWidget(content, exts==null||exts.length<1, resource, types, exts) {
			@Override
			public void pathChanged(String path, TypedEvent event) {				
				Activator.getDefault().getPreferenceStore().setValue(propName, path);
			}
		};
		selector.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		selector.setLabel(label);
		removeMargins(selector.getComposite());
		
		String path = Activator.getDefault().getPreferenceStore().getString(propName);
		if (path!=null && !"".equals(path)) selector.setText(path);
	}
	
	public static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
