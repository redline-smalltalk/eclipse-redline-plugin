/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.preferences;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import st.redline.eclipse.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class RedlinePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/*
	 * The following constants should be changed/augmented if Redline Smalltalk runtime dependencies change.
	 */
	private static final String REDLINE_BIN_JAR_PREFIX = "redline-";
	private static final String ASM_BIN_JAR_PREFIX = "asm-";
	private static final String ANTLR_BIN_JAR_PREFIX = "antlr-runtime-";
	
	
	protected static final String JAR_FILE_EXTENSION = ".JAR";
	private static final String LIB_DIR_NAME = "lib";

	public RedlinePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Redline Smalltalk Preferences");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(
				PreferenceConstants.P_REDLINE_ROOT_PATH,
				"&Redline Smalltalk Home Directory:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceConstants.P_SOURCE_ROOT_PATH,
				"Source root", getFieldEditorParent()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performApply() {
		super.performApply();

		String redlineDistPath = getPreferenceStore().getString(
				PreferenceConstants.P_REDLINE_ROOT_PATH);

		if (redlineDistPath == null) {
			setErrorMessage("Redline Smalltalk Home Directory is invalid.");
			return;
		}

		File redlineDistDir = new File(redlineDistPath);

		if (!redlineDistDir.exists() || !redlineDistDir.isDirectory()) {
			setErrorMessage("Redline Smalltalk Home Directory is invalid.");
			return;
		}

		List<File> children = new ArrayList<File>(Arrays.asList(redlineDistDir
				.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String arg1) {

						return arg1.toUpperCase().endsWith(JAR_FILE_EXTENSION);
					}
				})));

		File redlineLibDir = new File(redlineDistPath, LIB_DIR_NAME);
		if (redlineLibDir.exists() && redlineLibDir.isDirectory()) {
			children.addAll(Arrays.asList(redlineLibDir
					.listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File arg0, String arg1) {

							return arg1.toUpperCase().endsWith(JAR_FILE_EXTENSION);
						}
					})));
		}

		File redlineJarPath = null;
		File asmJarPath = null;
		File antlrJarPath = null;

		for (File child : children) {
			if (redlineJarPath != null && asmJarPath != null
					&& antlrJarPath != null) {
				break;
			}
			if (child.getName().startsWith(REDLINE_BIN_JAR_PREFIX)) {
				redlineJarPath = child;
			} else if (child.getName().startsWith(ASM_BIN_JAR_PREFIX)) {
				asmJarPath = child;
			} else if (child.getName().startsWith(ANTLR_BIN_JAR_PREFIX)) {
				antlrJarPath = child;
			}
		}

		if (redlineJarPath == null || asmJarPath == null
				|| antlrJarPath == null) {
			setErrorMessage("Unable to find a required jar in the Redline home directory.");
			return;
		}

		getPreferenceStore().setValue(PreferenceConstants.REDLINE_JAR_PATH,
				redlineJarPath.toString());
		getPreferenceStore().setValue(PreferenceConstants.ASM_JAR_PATH,
				asmJarPath.toString());
		getPreferenceStore().setValue(PreferenceConstants.ANTLR_JAR_PATH,
				antlrJarPath.toString());

		setErrorMessage(null);
	}
}