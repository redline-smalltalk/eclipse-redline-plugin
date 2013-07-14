/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.builder;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import st.redline.eclipse.properties.RedlinePropertyPage;

public class SmallTalkSourceFileBuilder extends IncrementalProjectBuilder {
	
	public static final String BUILDER_ID = "st.redline.eclipse.sampleBuilder";

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				
				IPath sourcePath = getRedlineSourcePath();
				if (sourcePath.isPrefixOf(file.getProjectRelativePath())) {
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						// handle added resource
						
						copyToBin(file, getRedlineSourceRootPath());
						break;
					case IResourceDelta.REMOVED:
						removeFromBin(file);
						break;
					case IResourceDelta.CHANGED:
						// handle changed resource
						copyToBin(file, getRedlineSourceRootPath());
						break;
					}
				}
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				
 				if (isSmallTalkSourceFile(file)) {
					try {
						copyToBin(file, getRedlineSourcePath());
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					return false;
				}
			}
			
			//return true to continue visiting children.
			return true;
		}

		private boolean isSmallTalkSourceFile(IFile file) {
			return (file.getFileExtension() != null && file.getFileExtension().equals("st"));
		}
	}
	
	public IPath getRedlineSourceRootPath() throws CoreException {
		//Only look at files inside of the configured redline source dir.
		String redlineSourceDir = getProject().getPersistentProperty(new QualifiedName("", RedlinePropertyPage.SOURCE_PATH_PROPERTY));
		if (redlineSourceDir == null) {
			redlineSourceDir = getProject().getFolder(RedlinePropertyPage.SOURCE_PATH_DEFAULT).toString();
		}
		return Path.fromOSString(redlineSourceDir);
	}
	
	public IPath getRedlineSourcePath() throws CoreException {
		//Only look at files inside of the configured redline source dir.
		String redlineSourceDir = getProject().getPersistentProperty(new QualifiedName("", RedlinePropertyPage.SOURCE_PATH_PROPERTY));
		if (redlineSourceDir == null) {
			redlineSourceDir = RedlinePropertyPage.SOURCE_PATH_DEFAULT;
		}
		
		return Path.fromOSString(redlineSourceDir);
	}

	public void removeFromBin(IResource resource) {
		// TODO Auto-generated method stub
		
	}

	public void copyToBin(IFile file, IPath sourcePath) throws CoreException {
		//Strip everything from path before the redline source dir.
		IPath baseFile = file.getFullPath().removeFirstSegments(sourcePath.segmentCount() - 1);
		
		//Get the root of the bin dir
		IFolder root = getOutputFolder(file.getProject());
		
		//Copy target plus relative path
		IFile destFile = root.getFile(baseFile);
		
		if (root != null) {
			copyFile(file, destFile, null); 
		}
	}
	
	/**
	 * Copies the given file to the given destination file. Does nothing
	 * if the destination file already exists.
	 * 
	 * @param source the source file; must exist
	 * @param dest the destination file; may or may not exist; never 
	 *    overwritten
	 * @param monitor the progress monitor, or <code>null</code> if none
	 * @exception CoreException if something goes wrong
	 */
	private void copyFile(IFile source, IFile dest, IProgressMonitor monitor) 
			throws CoreException {

		if (dest.exists()) {
			dest.delete(true, monitor);
		}

		IContainer parent = dest.getParent();
		if (parent.getType() == IResource.FOLDER) {
			mkdirs((IFolder) parent, monitor);
		}
		dest.create(source.getContents(false), false, monitor);
		
	}

	/**
	 * Creates the given folder, and its containing folders, if required. 
	 * Does nothing if the given folder already exists.
	 * 
	 * @param folder the folder to create
	 * @param monitor the progress monitor, or <code>null</code> if none
	 * @exception CoreException if something goes wrong
	 */
	public static void mkdirs(IFolder folder, IProgressMonitor monitor) 
			throws CoreException {
		if (folder.exists()) {
			return;
		}
		IContainer parent = folder.getParent();
		if (!parent.exists() && parent.getType() == IResource.FOLDER) {
			mkdirs((IFolder) parent, monitor);
		}
		folder.create(false, true, monitor);
	}
	
	/**
	 * Get output path as configured by JDT.
	 * 
	 * @param project
	 * @return
	 * @throws JavaModelException
	 */
	private IFolder getOutputFolder(IProject project) throws JavaModelException {
		IJavaProject jproject = JavaCore.create(getProject());
		if (jproject == null) {
			// not a java project (anymore?)
			return null;
		}		
		
		IPath outputPath = jproject.getOutputLocation();
		
		IFolder outputFolder = getProject().getParent().getFolder(outputPath);
		
		return outputFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		if (!isJDTProject(getProject())) {
			return null;
		}
		
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	/**
	 * @param project
	 * @return true if project has JDT nature.
	 */
	public static boolean isJDTProject(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			//Intentionally ignored
		}
		
		return false;
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
