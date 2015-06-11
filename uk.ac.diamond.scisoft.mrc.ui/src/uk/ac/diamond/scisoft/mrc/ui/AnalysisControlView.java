package uk.ac.diamond.scisoft.mrc.ui;

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
	
	private String smonitorPath, spropertiesPath, smomlPath;

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
		
		createSelector(content, "Monitor Directory    ",     MPATH, "smonitorPath", false);
		createSelector(content, "EM Properties         ",     PPATH, "spropertiesPath", true, new String[]{"Properties", "*.properties"});
		createSelector(content, "Pipeline Path          ",     WPATH, "smomlPath", true);		

		final Composite buttons = new Composite(content, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		final Button start = new Button(buttons, SWT.PUSH);
		start.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		start.setImage(Activator.getImage("icons/apply.gif"));
		start.setText("Start");
		start.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				start();
			}
		});
		
		final Button stop = new Button(buttons, SWT.PUSH);
		stop.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stop.setImage(Activator.getImage("icons/reset.gif"));
		stop.setText("Stop");
		stop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stop();
			}
		});

	}

	protected void stop() {
		// TODO Auto-generated method stub
		
	}

	protected void start() {
		
		
	}

	private void createSelector(Composite content, String label, final String propName, final String fieldName, boolean resource, String[]... exts) {
		
		SelectorWidget selector = new SelectorWidget(content, exts==null||exts.length<1, resource, exts) {
			@Override
			public void pathChanged(String path, TypedEvent event) {
				
				Activator.getDefault().getPreferenceStore().setValue(propName, path);
				
				// Try to set the value by reflection
				try {
					getClass().getField(fieldName).set(AnalysisControlView.this, path);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
