/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import st.redline.eclipse.Activator;

public class RedlinePropertyPage extends PropertyPage {

	private static final String SOURCE_PATH_TITLE = "&Source path:";
	public static final String SOURCE_PATH_PROPERTY = "st.redline.eclipse.source.path";
	public static final String SOURCE_PATH_DEFAULT = "/redline";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text sourcePath;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		Composite nc = createComposite(parent);

		Label ownerLabel = new Label(nc, SWT.NONE);
		ownerLabel.setText(SOURCE_PATH_TITLE);

		sourcePath = new Text(nc, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		sourcePath.setLayoutData(gd);

		String name;
		try {
			name = getProject().getPersistentProperty(new QualifiedName("", SOURCE_PATH_PROPERTY));
			
			if (name != null) {
				sourcePath.setText(name);
			} else {
				sourcePath.setText(SOURCE_PATH_DEFAULT);
			}
		} catch (CoreException e) {
			Activator.getDefault().log("Unable to create property page.", e);
		}

		return composite;
	}

	private Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();
		sourcePath.setText(SOURCE_PATH_DEFAULT);
	}
	
	public boolean performOk() {
		try {
			getProject().setPersistentProperty(new QualifiedName("", SOURCE_PATH_PROPERTY),
				sourcePath.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	private IProject getProject() {
		return ((IProject) getElement().getAdapter(IProject.class));
	}

}