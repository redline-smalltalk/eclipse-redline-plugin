/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "st.redline.eclipse"; //$NON-NLS-1$

	public static final String REDLINE_SOURCEFILE_EXTENSION = "st";

	public static final String REDLINE_SMALLTALK_LAUNCH_CLASSNAME = "st.redline.Stic";

	public static final String REDLINE_DEFAULT_SOURCE_DIR = "smalltalk";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Log a message.
	 * @param msg
	 */
	public void log(String msg) {
		log(msg, null);
	}

	/**
	 * Lot an exception.
	 * 
	 * @param msg
	 * @param e
	 */
	public void log(String msg, Exception e) {
		getLog().log(new Status(Status.INFO, PLUGIN_ID, Status.OK, msg, e));
	}
}
