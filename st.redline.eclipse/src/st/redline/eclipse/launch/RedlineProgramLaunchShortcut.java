/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import st.redline.eclipse.Activator;

public class RedlineProgramLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			// searchAndLaunch(((IStructuredSelection)selection).toArray(),
			// mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IJavaElement javaElement = (IJavaElement) input
				.getAdapter(IJavaElement.class);
		if (javaElement != null) {
			// searchAndLaunch(new Object[] {javaElement}, mode);
		}
	}

	protected void launch(IType type, String mode) {
		try {
			ILaunchConfiguration config = findLaunchConfiguration(type, mode);
			if (config != null) {
				config.launch(mode, null);
			}
		} catch (CoreException e) {
			Activator.getDefault().log("Unable to perform launch.", e);
		}
	}

	private ILaunchConfiguration findLaunchConfiguration(IType type, String mode) {
		return null;
	}
}
