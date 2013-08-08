package st.redline.eclipse.wizards.project;

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;

public class NewRedlineProjectWizardPageOne extends NewJavaProjectWizardPageOne {

	public NewRedlineProjectWizardPageOne() {
		super();
		setPageComplete(false);
		setTitle("New Smalltalk Project");
		setDescription("Create a new Redline Smalltalk project.");
	}
}
