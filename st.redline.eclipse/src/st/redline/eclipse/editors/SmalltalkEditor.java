/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class SmalltalkEditor extends TextEditor {

	private SmalltalkEditorColorManager colorManager;

	public SmalltalkEditor() {
		super();
		colorManager = new SmalltalkEditorColorManager();
		setSourceViewerConfiguration(new SmalltalkEditorConfiguration(colorManager));
		//setDocumentProvider(new SmalltalkDocumentProvider());
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
}
