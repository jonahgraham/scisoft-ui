package uk.ac.diamond.scisoft.mrc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.commandserver.core.application.Consumer;
import org.dawnsci.commandserver.core.application.IConsumerExtension;
import org.dawnsci.common.widgets.file.SelectorWidget;
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

public class AnalysisControlView extends ViewPart {
	
	private static String MPATH = "uk.ac.diamond.scisoft.mrc.ui.monitorPath";
	private static String PPATH = "uk.ac.diamond.scisoft.mrc.ui.propertiesPath";
	private static String WPATH = "uk.ac.diamond.scisoft.mrc.ui.momlPath";
	
	private List<IConsumerExtension> consumerList;
	
	@Override
	public void createPartControl(Composite parent) { 
		
		parent.setLayout(new GridLayout(1,  false));
	
		final Composite content = new Composite(parent, SWT.BORDER);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label label = null;
		label = new Label(content, SWT.WRAP);
		label.setText("Monitor an EM collection at a give path and with specific properties.\nCurrently only starting the consumers with the UI process is supported.\nTODO start consumers as process...");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		createSelector(content, "Monitor Directory    ",      MPATH, false, null, null);
		createSelector(content, "EM Properties         ",     PPATH, true, new String[]{"Properties"}, new String[]{"*.properties"});
		createSelector(content, "Pipeline Path          ",    WPATH, true, new String[]{"Workflow"},   new String[]{"*.moml"});		

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
				start.setEnabled(false);
				stop.setEnabled(true);
			}
		});
		
		stop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stop();
				start.setEnabled(true);
				stop.setEnabled(false);
			}
		});

	}

	protected void stop()  {
					
		if (consumerList == null) return;
		for (IConsumerExtension ext : consumerList) {
			try {
				ext.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		consumerList.clear();
	}

	protected void start() {
		
		if (consumerList == null) consumerList = new ArrayList<IConsumerExtension>(3);
		startFolderMonitor(Activator.getDefault().getPreferenceStore().getString(MPATH),
				           Activator.getDefault().getPreferenceStore().getString(PPATH));
		
		startWorkflowRunner(Activator.getDefault().getPreferenceStore().getString(WPATH));
		
	}

	private void startWorkflowRunner(String momlPath) {
		
		final Map<String,String> conf = new HashMap<String,String>(13);
		conf.put("uri",       "tcp://sci-serv5.diamond.ac.uk:61616");
		conf.put("submit",    "scisoft.diamond.FOLDER_QUEUE");
		conf.put("topic",     "scisoft.em.STATUS_TOPIC");
		conf.put("status",    "scisoft.em.STATUS_QUEUE");
		conf.put("bundle",    "org.dawnsci.commandserver.mrc");
		conf.put("consumer",  "org.dawnsci.commandserver.mrc.consumer.MRCConsumer");
		conf.put("momlLocation",   momlPath);
        start("Workflow Runner", conf);
	}

	private void startFolderMonitor(final String toMonitor, final String propertiesPath) {
		final Map<String,String> conf = new HashMap<String,String>(13);
		conf.put("uri",       "tcp://sci-serv5.diamond.ac.uk:61616");
		conf.put("resursive", "false");
		conf.put("bundle",    "org.dawnsci.commandserver.foldermonitor");
		conf.put("consumer",  "org.dawnsci.commandserver.foldermonitor.Monitor");
		conf.put("topic",     "scisoft.diamond.FOLDER_TOPIC");
		conf.put("status",    "scisoft.diamond.FOLDER_QUEUE");
		conf.put("nio",        "false");
		conf.put("filePattern",".+\\.mrc");
		conf.put("properties", propertiesPath);
		conf.put("location",   toMonitor);
        start("Folder Monitor", conf);
	}
	
	private void start(final String name, final Map<String,String> conf) {
		
		final Thread thread = new Thread(name) {
			public void run() {
				
				/*
				 *  Start consumer programmatically.
				 */
				try {
				    IConsumerExtension ext = Consumer.create(conf);
					consumerList.add(ext);
					ext.start(); // blocking! It will appear in the active consumer list.
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
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
