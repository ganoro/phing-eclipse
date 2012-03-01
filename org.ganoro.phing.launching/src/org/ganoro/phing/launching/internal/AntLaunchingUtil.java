/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.ganoro.phing.launching.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.ganoro.phing.launching.IAntLaunchConstants;
import org.ganoro.phing.launching.PhingLaunching;

/**
 * General utility class dealing with Ant build files
 */
public final class AntLaunchingUtil {
	public static final String ATTRIBUTE_SEPARATOR = ","; //$NON-NLS-1$;
	public static final char ANT_CLASSPATH_DELIMITER = '*';
	public static final String ANT_HOME_CLASSPATH_PLACEHOLDER = "G"; //$NON-NLS-1$
	public static final String ANT_GLOBAL_USER_CLASSPATH_PLACEHOLDER = "UG"; //$NON-NLS-1$

	/**
	 * No instances allowed
	 */
	private AntLaunchingUtil() {
		super();
	}

	/**
	 * Returns a single-string of the strings for storage.
	 * 
	 * @param strings
	 *            the array of strings
	 * @return a single-string representation of the strings or
	 *         <code>null</code> if the array is empty.
	 */
	public static String combineStrings(String[] strings) {
		if (strings.length == 0)
			return null;

		if (strings.length == 1)
			return strings[0];

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strings.length - 1; i++) {
			buf.append(strings[i]);
			buf.append(ATTRIBUTE_SEPARATOR);
		}
		buf.append(strings[strings.length - 1]);
		return buf.toString();
	}

	/**
	 * Returns an array of targets to be run, or <code>null</code> if none are
	 * specified (indicating the default target or implicit target should be
	 * run).
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return array of target names, or <code>null</code>
	 * @throws CoreException
	 *             if unable to access the associated attribute
	 */
	public static String[] getTargetNames(ILaunchConfiguration configuration)
			throws CoreException {
		String attribute = null;
		if (IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE
				.equals(configuration.getType().getIdentifier())) {
			attribute = getTargetNamesForAntBuilder(configuration);
		}
		if (attribute == null) {
			attribute = configuration.getAttribute(
					IAntLaunchConstants.ATTR_ANT_TARGETS, (String) null);
			if (attribute == null) {
				return null;
			}
		}

		return AntLaunchingUtil.parseRunTargets(attribute);
	}

	private static String getTargetNamesForAntBuilder(
			ILaunchConfiguration configuration) throws CoreException {
		String buildType = ExternalToolBuilder.getBuildType();
		String targets = null;
		if (IExternalToolConstants.BUILD_TYPE_AUTO.equals(buildType)) {
			targets = configuration.getAttribute(
					IAntLaunchConstants.ATTR_ANT_AUTO_TARGETS, (String) null);
		} else if (IExternalToolConstants.BUILD_TYPE_CLEAN.equals(buildType)) {
			targets = configuration.getAttribute(
					IAntLaunchConstants.ATTR_ANT_CLEAN_TARGETS, (String) null);
		} else if (IExternalToolConstants.BUILD_TYPE_FULL.equals(buildType)) {
			targets = configuration.getAttribute(
					IAntLaunchConstants.ATTR_ANT_AFTER_CLEAN_TARGETS,
					(String) null);
		} else if (IExternalToolConstants.BUILD_TYPE_INCREMENTAL
				.equals(buildType)) {
			targets = configuration.getAttribute(
					IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, (String) null);
		}

		return targets;
	}

	/**
	 * Returns a map of properties to be defined for the build, or
	 * <code>null</code> if none are specified (indicating no additional
	 * properties specified for the build).
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return map of properties (name --> value), or <code>null</code>
	 * @throws CoreException
	 *             if unable to access the associated attribute
	 */
	public static Map getProperties(ILaunchConfiguration configuration)
			throws CoreException {
		Map map = configuration.getAttribute(
				IAntLaunchConstants.ATTR_ANT_PROPERTIES, (Map) null);
		return map;
	}

	/**
	 * Returns a String specifying the Ant home to use for the build.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return String specifying Ant home to use or <code>null</code>
	 * @throws CoreException
	 *             if unable to access the associated attribute
	 */
	public static String getAntHome(ILaunchConfiguration configuration)
			throws CoreException {

		return null;
	}

	/**
	 * Returns an array of property files to be used for the build, or
	 * <code>null</code> if none are specified (indicating no additional
	 * property files specified for the build).
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return array of property file names, or <code>null</code>
	 * @throws CoreException
	 *             if unable to access the associated attribute
	 */
	public static String[] getPropertyFiles(ILaunchConfiguration configuration)
			throws CoreException {
		String attribute = configuration.getAttribute(
				IAntLaunchConstants.ATTR_ANT_PROPERTY_FILES, (String) null);
		if (attribute == null) {
			return null;
		}
		String[] propertyFiles = AntLaunchingUtil.parseString(attribute, ","); //$NON-NLS-1$
		for (int i = 0; i < propertyFiles.length; i++) {
			String propertyFile = propertyFiles[i];
			propertyFile = expandVariableString(propertyFile, "AntUtil_6");
			propertyFiles[i] = propertyFile;
		}
		return propertyFiles;
	}

	/**
	 * Returns the list of URLs that define the custom classpath for the Ant
	 * build, or <code>null</code> if the global classpath is to be used.
	 * 
	 * @param config
	 *            launch configuration
	 * @return a list of <code>URL</code>
	 * 
	 * @throws CoreException
	 *             if file does not exist, IO problems, or invalid format.
	 */
	public static URL[] getCustomClasspath(ILaunchConfiguration config)
			throws CoreException {
		return null;
	}

	private static String expandVariableString(String variableString,
			String invalidMessage) throws CoreException {
		return null;
	}

	/**
	 * Returns the list of target names to run
	 * 
	 * @param extraAttibuteValue
	 *            the external tool's extra attribute value for the run targets
	 *            key.
	 * @return a list of target names
	 */
	public static String[] parseRunTargets(String extraAttibuteValue) {
		return parseString(extraAttibuteValue, ATTRIBUTE_SEPARATOR);
	}

	/**
	 * Returns the list of Strings that were delimiter separated.
	 * 
	 * @param delimString
	 *            the String to be tokenized based on the delimiter
	 * @return a list of Strings
	 */
	public static String[] parseString(String delimString, String delim) {
		if (delimString == null) {
			return new String[0];
		}

		// Need to handle case where separator character is
		// actually part of the target name!
		StringTokenizer tokenizer = new StringTokenizer(delimString, delim);
		String[] results = new String[tokenizer.countTokens()];
		for (int i = 0; i < results.length; i++) {
			results[i] = tokenizer.nextToken().trim();
		}

		return results;
	}

	/**
	 * Returns an IFile with the given fully qualified path (relative to the
	 * workspace root). The returned IFile may or may not exist.
	 */
	public static IFile getFile(String fullPath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getFile(new Path(fullPath));
	}

	/**
	 * Returns the workspace file associated with the given path in the local
	 * file system, or <code>null</code> if none. If the path happens to be a
	 * relative path, then the path is interpreted as relative to the specified
	 * parent file.
	 * 
	 * Attempts to handle linked files; the first found linked file with the
	 * correct path is returned.
	 * 
	 * @param path
	 * @param buildFileParent
	 * @return file or <code>null</code>
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findFilesForLocation(IPath)
	 */
	public static IFile getFileForLocation(String path, File buildFileParent) {
		if (path == null) {
			return null;
		}
		IPath filePath = new Path(path);
		IFile file = null;
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(filePath);
		if (files.length > 0) {
			file = files[0];
		}
		if (file == null) {
			// relative path
			File relativeFile = null;
			try {
				// this call is ok if buildFileParent is null
				relativeFile = FileUtils.getFileUtils().resolveFile(
						buildFileParent, path);
				filePath = new Path(relativeFile.getAbsolutePath());
				files = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocation(filePath);
				if (files.length > 0) {
					file = files[0];
				} else {
					return null;
				}
			} catch (BuildException be) {
				return null;
			}
		}

		if (file.exists()) {
			return file;
		}
		File ioFile = file.getLocation().toFile();
		if (ioFile.exists()) {// needs to handle case insensitivity on WINOS
			try {
				files = ResourcesPlugin
						.getWorkspace()
						.getRoot()
						.findFilesForLocation(
								new Path(ioFile.getCanonicalPath()));
				if (files.length > 0) {
					return files[0];
				}
			} catch (IOException e) {
			}
		}

		return null;
	}

	/**
	 * Returns whether the given configuration should be launched in the
	 * background. When unspecified, the default value for an Ant launch
	 * configuration is <code>true</code>.
	 * 
	 * @param configuration
	 *            the configuration
	 * @return whether the configuration is configured to launch in the
	 *         background
	 */
	public static boolean isLaunchInBackground(
			ILaunchConfiguration configuration) {
		boolean launchInBackground = true;
		try {
			launchInBackground = configuration.getAttribute(
					IExternalToolConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		} catch (CoreException ce) {
			PhingLaunching.log(ce);
		}
		return launchInBackground;
	}
}