/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.launch;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class RedlineProgramMainTab extends JavaLaunchTab {

	private Text launchClassname;
	private Text fProjText;
	private Button fProjButton;
	private WidgetListener fListener = new WidgetListener();

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			launchClassname.setText(config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, ""));
		} catch (CoreException ce) {
			launchClassname.setText("");
		}
		String projectName = "";
		try {
			projectName = config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fProjText.setText(projectName);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
				(String) launchClassname.getText());
		configuration.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				fProjText.getText());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			if (validateClassExists(launchClassname.getText())) {
				return true;
			}
		} catch (NumberFormatException nfe) {
		}

		setErrorMessage("Invalid launch class specified.");
		return false;
	}

	private boolean validateClassExists(String text) {
		// TODO implement. Using JDT or resource model, determine redline file
		// exists in target project.
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = createComposite(parent, parent.getFont(), 1, 1,
				GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createMainTypeEditor(comp, "Smalltalk Class");
		
		setControl(comp);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (launchClassname != null) {
			configuration.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					launchClassname.getText());
		} else {
			configuration.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					"");
		}
	}

	@Override
	public String getName() {
		return "Redline Smalltalk";
	}

	/**
	 * Updates the buttons and message in this page's launch configuration
	 * dialog.
	 */
	protected void updateLaunchConfigurationDialog() {
		if (getLaunchConfigurationDialog() != null) {
			// order is important here due to the call to
			// refresh the tab viewer in updateButtons()
			// which ensures that the messages are up to date
			getLaunchConfigurationDialog().updateButtons();
			getLaunchConfigurationDialog().updateMessage();
		}
	}

	/**
	 * chooses a project for the type of java launch config that it is
	 * 
	 * @return
	 */
	private IJavaProject chooseJavaProject() {
		ILabelProvider labelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Redline Smalltalk Launcher");
		dialog.setMessage("Launch a redline program with this dialog");
		try {
			dialog.setElements(JavaCore.create(getWorkspaceRoot())
					.getJavaProjects());
		} catch (JavaModelException jme) {
			jme.printStackTrace();
		}
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {
			return (IJavaProject) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Return the IJavaProject corresponding to the project name in the project
	 * name text field, or null if the text does not match a project name.
	 */
	protected IJavaProject getJavaProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getJavaModel().getJavaProject(projectName);
	}

	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createProjectEditor(Composite parent) {
		Group group = createGroup(parent,
				"Project", 2, 1,
				GridData.FILL_HORIZONTAL);
		fProjText = createSingleText(group, 1);
		fProjText.addModifyListener(fListener);
		ControlAccessibleListener.addListener(fProjText, group.getText());
		fProjButton = createPushButton(group,
				"Browse...", null);
		fProjButton.addSelectionListener(fListener);
	}
	
	protected void createMainTypeEditor(Composite parent, String text) {
		Group group = createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL); 
		launchClassname = createSingleText(group, 1);
		launchClassname.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		ControlAccessibleListener.addListener(launchClassname, group.getText());
	}

	/**
	 * Convenience method to get access to the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		IJavaProject project = chooseJavaProject();
		if (project == null) {
			return;
		}
		String projectName = project.getElementName();
		fProjText.setText(projectName);
	}

	private class WidgetListener implements ModifyListener, SelectionListener {

		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fProjButton) {
				handleProjectButtonSelected();
			} else {
				updateLaunchConfigurationDialog();
			}
		}
	}

	private static class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		public ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}

		public static void addListener(Control comp, String name) {
			// strip mnemonic
			String[] strs = name.split("&"); //$NON-NLS-1$
			StringBuffer stripped = new StringBuffer();
			for (int i = 0; i < strs.length; i++) {
				stripped.append(strs[i]);
			}
			comp.getAccessible().addAccessibleListener(
					new ControlAccessibleListener(stripped.toString()));
		}
	}

	public static Composite createComposite(Composite parent, Font font,
			int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(font);
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	public static Group createGroup(Composite parent, String text, int columns,
			int hspan, int fill) {
		Group g = new Group(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setText(text);
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	public static Text createSingleText(Composite parent, int hspan) {
		Text t = new Text(parent, SWT.SINGLE | SWT.BORDER);
		t.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		t.setLayoutData(gd);
		return t;
	}
	
	public static Text createText(Composite parent, int style, int hspan, String text) {
    	Text t = new Text(parent, style);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    	gd.horizontalSpan = hspan;
    	t.setLayoutData(gd);
    	t.setText(text);
    	return t;
    }
	
	public static Label createLabel(Composite parent, String text, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = false;
		l.setLayoutData(gd);
		return l;
	}
}
