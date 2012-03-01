/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.internal.preferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;
import org.ganoro.phing.ui.PhingUi;

public class FileFilter extends ViewerFilter {

	/**
	 * Objects to filter
	 */
	private List fFilter;
	
	/**
	 * Collection of files and containers to display
	 */
	private Set fFiles;
	
	private String fExtension;

    private boolean fConsiderExtension= true;

	/**
	 * Creates a new filter that filters the given 
	 * objects.
	 * Note: considerExtension must be called to initialize the filter
	 */
	public FileFilter(List objects, String extension) {
		fFilter = objects;
		fExtension= extension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return fFiles.contains(element) && !fFilter.contains(element);
	}
	
	/**
	 * Search for all the matching files in the workspace.
	 */
	private void init() {
		BusyIndicator.showWhile(PhingUi.getStandardDisplay(), new Runnable() {
			public void run() {
				fFiles = new HashSet();
				traverse(ResourcesPlugin.getWorkspace().getRoot(), fFiles);
			}
		});
	}

	/**
	 * Traverse the given container, adding files to the given set.
	 * Returns whether any files were added
	 */
	private boolean traverse(IContainer container, Set set) {
		boolean added = false;
		try {
			IResource[] resources = container.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String ext = file.getFileExtension();
					if (!fConsiderExtension || (ext != null && ext.equalsIgnoreCase(fExtension))) {
						set.add(file);
						added = true;
					}
				} else if (resource instanceof IContainer) {
					if (traverse((IContainer) resource, set)) {
						set.add(resource);
						added = true;
					}
				}
			}
		} catch (CoreException e) {
		}
		return added;
	}
	
	/**
	 * Sets whether this filter will filter based on extension.
	 * @param considerExtension whether to consider a file's extension when filtering
	 */
	public void considerExtension(boolean considerExtension) {
	    fConsiderExtension= considerExtension;
	    init();
	}
}