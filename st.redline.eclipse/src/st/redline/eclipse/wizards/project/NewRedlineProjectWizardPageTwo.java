package st.redline.eclipse.wizards.project;

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;

public class NewRedlineProjectWizardPageTwo extends NewJavaProjectWizardPageTwo {

	public NewRedlineProjectWizardPageTwo(NewJavaProjectWizardPageOne mainPage) {
		super(mainPage);
		
		setTitle("Java and Smalltalk Settings");
        setDescription("Define the Java and Smalltalk build settings.");
	}

}
