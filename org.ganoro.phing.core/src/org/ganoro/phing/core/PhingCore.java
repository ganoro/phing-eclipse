package org.ganoro.phing.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class PhingCore extends Plugin {

	/**
	 * Unique identifier constant (value <code>"org.eclipse.ant.core"</code>)
	 * for the Ant Core plug-in.
	 */
	public static final String PI_ANTCORE = "org.eclipse.ant.core"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"antTasks"</code>)
	 * for the Ant tasks extension point.
	 */
	public static final String PT_TASKS = "antTasks"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"extraClasspathEntries"</code>)
	 * for the extra classpath entries extension point.
	 */
	public static final String PT_EXTRA_CLASSPATH = "extraClasspathEntries"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"antTypes"</code>)
	 * for the Ant types extension point.
	 */
	public static final String PT_TYPES = "antTypes"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"antProperties"</code>)
	 * for the Ant properties extension point.
	 * 
	 * @since 3.0
	 */
	public static final String PT_PROPERTIES = "antProperties"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"org.eclipse.ant.core.antBuildFile"</code>)
	 * for the content type of an Ant BuildFile
	 * 
	 * @since 3.0
	 */
	public static final String ANT_BUILDFILE_CONTENT_TYPE = PI_ANTCORE + ".antBuildFile"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"class"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String CLASS = "class"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"name"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String NAME = "name"; //$NON-NLS-1$

	/**
	 * Simple identifier constant (value <code>"library"</code>)
	 * of a tag that appears in Ant extensions.
	 */
	public static final String LIBRARY = "library"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"headless"</code>) of a tag
	 * that appears in Ant extensions.
	 * @since 2.1
	 */
	public static final String HEADLESS = "headless"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"eclipseRuntime"</code>) of a tag
	 * that appears in Ant extensions.
	 * @since 3.0
	 */
	public static final String ECLIPSE_RUNTIME = "eclipseRuntime"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"uri"</code>) of a tag
	 * that appears in Ant extensions.
	 * @since 3.2
	 */
	public static final String URI = "uri"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"value"</code>) of a tag
	 * that appears in Ant extensions.
	 * @since 3.0
	 */
	public static final String VALUE = "value"; //$NON-NLS-1$

	/**
	 * Key to access the <code>IProgressMonitor</code> reference. When a
	 * progress monitor is passed to the <code>AntRunner.run(IProgressMonitor)</code>
	 * method, the object is available as a reference for the current
	 * Ant project.
	 */
	public static final String ECLIPSE_PROGRESS_MONITOR = "eclipse.progress.monitor"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an error occurred running a build.
	 * @since 2.1
	 */
	public static final int ERROR_RUNNING_BUILD = 1;
	
	/**
	 * Status code indicating an error occurred due to a malformed URL.
	 * @since 2.1
	 */
	public static final int ERROR_MALFORMED_URL = 2;
	
	/**
	 * Status code indicating an error occurred as a library was not specified
	 * @since 2.1
	 */
	public static final int ERROR_LIBRARY_NOT_SPECIFIED = 3;
	
	
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private static PhingCore plugin;

	/** 
	 * Constructs an instance of this plug-in runtime class.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by the Ant Core plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * @since 3.1
	 * 
	 */
	public PhingCore() {
		plugin = this;
	}

	/**
	 * @return the plugin
	 */
	public static PhingCore getPlugin() {
		return plugin;
	}
	
	
	/**
	 * Given an extension point name, extract its extensions and return them
	 * as a List.
	 * @param pointName The name of the extension point
	 * @return The list of the extensions
	 */
	private List extractExtensions(String pointName) {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(PhingCore.PI_ANTCORE, pointName);
		if (extensionPoint == null) {
			return null;
		}
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		return Arrays.asList(extensions);
	}

	/**
	 * The preferences class for this plug-in.
	 */
	private AntCorePreferences preferences;	
	
	/**
	 * Returns an object representing this plug-in's preferences.
	 * 
	 * @return the Ant core object representing the preferences for this plug-in.
	 */
	public AntCorePreferences getPreferences() {
		if (preferences == null) {
			preferences = new AntCorePreferences(extractExtensions(PT_TASKS), extractExtensions(PT_EXTRA_CLASSPATH), extractExtensions(PT_TYPES), extractExtensions(PT_PROPERTIES), false);
		}
		return preferences;
	}
	
	/**
	 * Set this plug-in's preferences for running headless based on the 
	 * headless parameter.
	 * This method is public for testing purposes only. It should not
	 * be called outside of the Ant integration framework.
	 * @param headless Whether or not to mark that the plug-in is running headless or not
	 */
	public void setRunningHeadless(boolean headless) {
		preferences = new AntCorePreferences(extractExtensions(PT_TASKS), extractExtensions(PT_EXTRA_CLASSPATH), extractExtensions(PT_TYPES), extractExtensions(PT_PROPERTIES), headless);
	}

	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 * @since 2.1
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, PI_ANTCORE, 201, "Error logged from Ant Core: ", t); //$NON-NLS-1$
		getPlugin().getLog().log(status);
	}
	
	
	/**
	 * Returns a new class loader to use when executing Ant builds.
	 * 
	 * @return the new class loader
	 */
	public ClassLoader getNewClassLoader() {
	    AntCorePreferences corePreferences = getPreferences();
		return getNewClassLoader(false, corePreferences.getURLs());
	}
	
	/**
	 * Returns a new class loader to use when executing Ant builds.
	 * @param urls the URLs that define the classpath of the new classloader
	 * @return the new class loader
	 * @since 3.1
	 */
	public URLClassLoader getNewClassLoader(URL[] urls) {
		return getNewClassLoader(false, urls);
	}
	
	/**
	 * Returns a new class loader to use when executing Ant builds or 
	 * other applications such as parsing or code proposal determination.
	 * 
	 * @param allowLoading whether to allow plug-in classloaders associated 
	 * with the new classloader to load Apache Ant classes or resources.
	 * @return the new class loader
	 */
	public ClassLoader getNewClassLoader(boolean allowLoading) {
		AntCorePreferences corePreferences = getPreferences();
		URL[] urls = corePreferences.getURLs();
		return getNewClassLoader(allowLoading, urls);
	}
		
	/**
	 * Returns a new class loader to use when executing Ant builds or 
	 * other applications such as parsing or code proposal determination.
	 *  
	 * @param allowLoading whether to allow plug-in classloaders associated 
	 * with the new classloader to load Apache Ant classes or resources.
	 * @param urls the URLs that define the classpath of the new classloader
	 * @return the new class loader
	 * @since 3.1
	 */
	public URLClassLoader getNewClassLoader(boolean allowLoading, URL[] urls) {
		return (URLClassLoader) this.getClass().getClassLoader();
	}	
	
	
	
	
	
	
}
