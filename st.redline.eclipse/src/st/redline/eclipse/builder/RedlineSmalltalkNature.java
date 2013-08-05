/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;

import st.redline.eclipse.Activator;
import st.redline.eclipse.preferences.PreferenceConstants;

public class RedlineSmalltalkNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "st.redline.eclipse.redlineSmalltalkNature";

	
	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		// TODO: validate that user preferences have been updated with real location to REDLINE HOME before proceeding, otherwise present model dialog and abort.
		
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(SmallTalkSourceFileBuilder.BUILDER_ID)) {
				return;
			}
		}
		
		if (!SmallTalkSourceFileBuilder.isJDTProject(project)) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Project must be a Java project before the Redline nature can be added."));
		}
		
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(SmallTalkSourceFileBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
		
		IFolder folder = project.getFolder(getRedlineSourceRootDirectory());
		
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
		
		IJavaProject javaProject = JavaCore.create(project);
		
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);
		String[] redlineClasspathPaths = getRedlineClasspathEntries();
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1 + redlineClasspathPaths.length];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
		
		int offset = oldEntries.length + 1;
		
		for (String rlcpi : redlineClasspathPaths) {
			newEntries[offset] = JavaCore.newLibraryEntry(
					new Path(rlcpi), null, null);
			offset++;
		}
		
		javaProject.setRawClasspath(newEntries, null);
	}

	public static String getRedlineSourceRootDirectory() {
		String sourceRoot = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_SOURCE_ROOT_PATH);
		if (sourceRoot == null) {
			sourceRoot = Activator.REDLINE_DEFAULT_SOURCE_DIR;
		}
		
		return sourceRoot;
	}

	private String[] getRedlineClasspathEntries() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		return new String[] {
				prefs.getString(PreferenceConstants.REDLINE_JAR_PATH),
				prefs.getString(PreferenceConstants.ASM_JAR_PATH),
				prefs.getString(PreferenceConstants.ANTLR_JAR_PATH)
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(SmallTalkSourceFileBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);			
				return;
			}
		}
		
		IFolder folder = project.getFolder(getRedlineSourceRootDirectory());
		IJavaProject javaProject = JavaCore.create(project);
		
		List<String> redlineClasspathPaths = Arrays.asList(getRedlineClasspathEntries());
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		List<IClasspathEntry> cleanEntries = new ArrayList<IClasspathEntry>();
		
		for (IClasspathEntry e : oldEntries) {
			if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (e.getPath().equals(folder)) {
					continue;
				}
			}
			if (e.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				if (redlineClasspathPaths.contains(e.getPath().toOSString())) {
					continue;
				}
			}
			
			cleanEntries.add(e);
		}
		
		javaProject.setRawClasspath(cleanEntries.toArray(new IClasspathEntry[cleanEntries.size()]), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
