/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

import st.redline.eclipse.Activator;
import st.redline.eclipse.preferences.PreferenceConstants;

public class RedlineSmalltalkNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "st.redline.eclipse.redlineSmalltalkNature";


	private static final String MAVEN_SOURCE_STRUCTURE = "src/main/java";


	private static final String MAVEN_TEST_STRUCTURE = "src/test/java";

	
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
		
		IJavaProject javaProject = JavaCore.create(project);
		List<IPackageFragmentRoot> soucreRoots = new ArrayList<IPackageFragmentRoot>();
		for (String folderPath : getRedlineSourceRootDirectory(javaProject)) {
			IFolder folder = project.getFolder(folderPath);
			
			if (!folder.exists()) {
				folder.create(false, true, null);
			}
			
			soucreRoots.add(javaProject.getPackageFragmentRoot(folder));
		}
		
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		
		for (IPackageFragmentRoot fr : soucreRoots) {
			classpathEntries.add(JavaCore.newSourceEntry(fr.getPath()));
		}
		
		for (String rlcpi : getRedlineClasspathEntries()) {
			classpathEntries.add(JavaCore.newLibraryEntry(
					new Path(rlcpi), null, null));
		}
		
		javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
	}

	public static Iterable<String> getRedlineSourceRootDirectory(IJavaProject javaProject) throws JavaModelException {
		Set<String> paths = new HashSet<String>(1);
		
		if (javaProject != null && javaProject.getAllPackageFragmentRoots() != null) {
			
			for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					if (root.getPath().toOSString().endsWith(MAVEN_SOURCE_STRUCTURE)) {
						paths.add("src" + File.separator + "main" + File.separator + "smalltalk");
						continue;
					} else if (root.getPath().toOSString().endsWith(MAVEN_TEST_STRUCTURE)) {
						paths.add("src" + File.separator + "test" + File.separator + "smalltalk");
						continue;
					}
				} 
			}
			
			if (!paths.isEmpty()) {
				return paths;
			}
		}
		
		//If cannot access Java project source folder info, use configured via prefs.
		String sourceRoot = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_SOURCE_ROOT_PATH);
		if (sourceRoot != null) {
			paths.add(sourceRoot);
		} else {
			paths.add(Activator.REDLINE_DEFAULT_SOURCE_DIR);
		}
		
		return paths;
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
		
		IJavaProject javaProject = JavaCore.create(project);
		for (String folderPath : getRedlineSourceRootDirectory(javaProject)) {
			IFolder folder = project.getFolder(folderPath);
			
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
