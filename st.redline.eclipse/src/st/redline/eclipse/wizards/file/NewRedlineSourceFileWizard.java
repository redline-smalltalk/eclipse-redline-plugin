/*******************************************************************************
 * Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution.
 *******************************************************************************/
package st.redline.eclipse.wizards.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import st.redline.eclipse.Activator;
import st.redline.eclipse.builder.SmallTalkSourceFileBuilder;

public class NewRedlineSourceFileWizard extends Wizard implements INewWizard {
	private MainPage page;
	private ISelection selection;

	public NewRedlineSourceFileWizard() {
		super();
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/smalltalk_file_wizard.png"));
		//setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
		setWindowTitle("New Smalltalk File");
	}

	public void addPages() {
		page = new MainPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String className = page.getClassName();
		final String packageName = page.getPackageName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					createSourceFile(containerName, className, packageName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		return true;
	}

	private void createSourceFile(String containerName, String className, String packageName,
			IProgressMonitor monitor) throws CoreException {
		
		String fileName = className + "." + Activator.REDLINE_SOURCEFILE_EXTENSION;
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Folder \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file;
		
		if (packageName != null && packageName.trim().length() > 0) {
			String packagePath = packageName.replace('.', File.separatorChar);
			
			IFolder targetDir = container.getFolder(new Path(packagePath));
			
			if (!targetDir.exists()) {
				SmallTalkSourceFileBuilder.mkdirs(targetDir, monitor);
			}
			file = targetDir.getFile(new Path(fileName));
		} else {
			file = container.getFile(new Path(fileName));
		}
		
		try {
			InputStream stream = openContentStream(className);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private InputStream openContentStream(String classname) {
		String contents = "Object < #" + classname + ".";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "st.redline.ide",
				IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}