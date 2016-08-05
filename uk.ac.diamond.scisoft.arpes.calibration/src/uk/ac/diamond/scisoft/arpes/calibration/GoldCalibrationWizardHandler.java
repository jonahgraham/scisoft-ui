package uk.ac.diamond.scisoft.arpes.calibration;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.arpes.calibration.wizards.GoldCalibrationWizard;

public class GoldCalibrationWizardHandler extends AbstractHandler implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	private ISelection selection;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		openWizard(HandlerUtil.getActiveShell(event));
		return Boolean.FALSE;
	}

	private void openWizard(final Shell shell) {
		GoldCalibrationWizard wizard = new GoldCalibrationWizard();
		wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setPageSize(new Point(1200, 800));
		dialog.create();
		dialog.open();
	}

	@Override
	public void run(IAction action) {
		openWizard(targetPart.getSite().getShell());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
