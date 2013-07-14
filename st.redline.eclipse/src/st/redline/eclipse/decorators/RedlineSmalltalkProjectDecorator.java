/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.decorators;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import st.redline.eclipse.Activator;
import st.redline.eclipse.builder.RedlineSmalltalkNature;

public class RedlineSmalltalkProjectDecorator implements
		ILightweightLabelDecorator {

	private ImageDescriptor decoratorImage;

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject) {
			try {
				if (((IProject) element).isNatureEnabled(RedlineSmalltalkNature.NATURE_ID)) {
					if (decoratorImage == null) {
						decoratorImage = Activator.getImageDescriptor("icons/redline_decorator.png");
					}
					
					if (decoratorImage != null) {
						decoration.addOverlay(decoratorImage);
					}
				}
			} catch (CoreException e) {
				//Intentionally ignore exception to prevent log spam.
				return;
			}
		}
	}

}
