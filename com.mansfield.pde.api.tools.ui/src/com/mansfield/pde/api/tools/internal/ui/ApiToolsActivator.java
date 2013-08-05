/*******************************************************************************
 * Copyright (c) 2013 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 ******************************************************************************/
package com.mansfield.pde.api.tools.internal.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;


/**
 * @author Joseph Carroll (921496)
 */
@SuppressWarnings("deprecation")
public class ApiToolsActivator implements BundleActivator
{

	/**
	 * The bundle symbolic name.
	 */
	public static final String PLUGIN_ID = "com.fedex.airops.desktop.material.parts"; //$NON-NLS-1$

	private static ApiToolsActivator apiToolsActivator;

	private BundleContext context;

	private ServiceTracker<Location, Location> locationTracker;
	private ServiceTracker<PackageAdmin, PackageAdmin> pkgAdminTracker;
	private ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker;

	private ServiceTracker<DebugOptions, DebugOptions> debugTracker;
	private ServiceTracker<LogService, LogService> logServiceTracker;
	private ServiceTracker<FrameworkLog, FrameworkLog> frameworkLogTracker;

	private ServiceTracker<IPreferencesService, IPreferencesService> preferencesTracker;

	private DebugTrace trace;

	/**
	 * Get the default workbenchActivator.
	 * 
	 * @return a BundleActivator
	 */
	public static ApiToolsActivator getDefault()
	{
		return apiToolsActivator;
	}

	/**
	 * @param option
	 * @param msg
	 * @param error
	 */
	public static void trace(String option, String msg, Throwable error)
	{
		final DebugOptions debugOptions = apiToolsActivator.getDebugOptions();
		if (debugOptions.isDebugEnabled() && debugOptions.getBooleanOption(PLUGIN_ID + option, false))
		{
			System.out.println(msg);
			if (error != null)
			{
				error.printStackTrace(System.out);
			}
		}
		apiToolsActivator.getTrace().trace(option, msg, error);
	}

	/**
	 * @param level
	 * @param message
	 */
	public static void log(int level, String message)
	{
		LogService logService = apiToolsActivator.getLogService();
		if (logService != null)
		{
			logService.log(level, message);
		}
	}

	/**
	 * @param level
	 * @param message
	 * @param exception
	 */
	public static void log(int level, String message, Throwable exception)
	{
		LogService logService = apiToolsActivator.getLogService();
		if (logService != null)
		{
			logService.log(level, message, exception);
		}
	}

	/**
	 * @return the bundle object
	 */
	public Bundle getBundle()
	{
		return context.getBundle();
	}

	/**
	 * @return the PackageAdmin service from this bundle
	 */
	public PackageAdmin getBundleAdmin()
	{
		if (pkgAdminTracker == null)
		{
			if (context == null)
			{
				return null;
			}
			pkgAdminTracker = new ServiceTracker<PackageAdmin, PackageAdmin>(context, PackageAdmin.class.getName(), null);
			pkgAdminTracker.open();
		}
		return pkgAdminTracker.getService();
	}

	/**
	 * @param bundleName
	 *            the bundle id
	 * @return A bundle if found, or <code>null</code>
	 */
	public Bundle getBundleForName(String bundleName)
	{
		Bundle[] bundles = getBundleAdmin().getBundles(bundleName, null);
		if (bundles == null)
		{
			return null;
		}
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++)
		{
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0)
			{
				return bundles[i];
			}
		}
		return null;
	}

	/**
	 * @return this bundles context
	 */
	public BundleContext getContext()
	{
		return context;
	}

	/**
	 * @return the instance Location service
	 */
	public Location getInstanceLocation()
	{
		if (locationTracker == null)
		{
			Filter filter = null;
			try
			{
				filter = context.createFilter(Location.INSTANCE_FILTER);
			}
			catch (InvalidSyntaxException e)
			{
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			locationTracker = new ServiceTracker<Location, Location>(context, filter, null);
			locationTracker.open();
		}
		return locationTracker.getService();
	}

	public void start(BundleContext context) throws Exception
	{
		apiToolsActivator = this;
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception
	{
		if (pkgAdminTracker != null)
		{
			pkgAdminTracker.close();
			pkgAdminTracker = null;
		}
		if (locationTracker != null)
		{
			locationTracker.close();
			locationTracker = null;
		}
		if (eventAdminTracker != null)
		{
			eventAdminTracker.close();
			eventAdminTracker = null;
		}
		if (debugTracker != null)
		{
			trace = null;
			debugTracker.close();
			debugTracker = null;
		}
		if (logServiceTracker != null)
		{
			logServiceTracker.close();
			logServiceTracker = null;
		}
		if (frameworkLogTracker != null)
		{
			frameworkLogTracker.close();
			frameworkLogTracker = null;
		}
	}

	/**
	 * @return the event admin
	 */
	public EventAdmin getEventAdmin()
	{
		if (eventAdminTracker == null)
		{
			if (context == null)
			{
				return null;
			}
			eventAdminTracker = new ServiceTracker<EventAdmin, EventAdmin>(context, EventAdmin.class.getName(), null);
			eventAdminTracker.open();
		}
		return eventAdminTracker.getService();
	}

	/**
	 * Returns the location in the local file system of the 
	 * plug-in state area for this plug-in.
	 * If the plug-in state area did not exist prior to this call,
	 * it is created.
	 * <p>
	 * The plug-in state area is a file directory within the
	 * platform's metadata area where a plug-in is free to create files.
	 * The content and structure of this area is defined by the plug-in,
	 * and the particular plug-in is solely responsible for any files
	 * it puts there. It is recommended for plug-in preference settings and 
	 * other configuration parameters.
	 * </p>
	 * @throws IllegalStateException, when the system is running with no data area (-data @none),
	 * or when a data area has not been set yet.
	 * @return a local file system path
	 *  XXX Investigate the usage of a service factory (see also platform.getStateLocation)
	 */
	public final IPath getStateLocation() throws IllegalStateException {
		return Platform.getStateLocation(getBundle());
	}
	
	/**
	 * @return the debug options
	 */
	public DebugOptions getDebugOptions()
	{
		if (debugTracker == null)
		{
			if (context == null)
			{
				return null;
			}
			debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(context, DebugOptions.class.getName(), null);
			debugTracker.open();
		}
		return debugTracker.getService();
	}

	/**
	 * @return the debug trace
	 */
	public DebugTrace getTrace()
	{
		if (trace == null)
		{
			trace = getDebugOptions().newDebugTrace(PLUGIN_ID);
		}
		return trace;
	}

	/**
	 * Returns the log service for the bundle. If no log service is registered,
	 * a log service is created that prints to the standard console out.
	 * 
	 * @return the log service
	 */
	public LogService getLogService()
	{
		LogService logService = null;
		if (logServiceTracker != null)
		{
			logService = logServiceTracker.getService();
		}
		else
		{
			if (context != null)
			{
				logServiceTracker = new ServiceTracker<LogService, LogService>(context, LogService.class.getName(), null);
				logServiceTracker.open();
				logService = logServiceTracker.getService();
			}
		}
		if (logService == null)
		{
			logService = new LogService()
			{
				public void log(int level, String message)
				{
					log(null, level, message, null);
				}

				public void log(int level, String message, Throwable exception)
				{
					log(null, level, message, exception);
				}

				@SuppressWarnings("rawtypes")
				public void log(ServiceReference sr, int level, String message)
				{
					log(sr, level, message, null);
				}

				@SuppressWarnings("rawtypes")
				public void log(ServiceReference sr, int level, String message, Throwable exception)
				{
					if (level == LogService.LOG_ERROR)
					{
						System.err.print("ERROR: "); //$NON-NLS-1$
					}
					else if (level == LogService.LOG_WARNING)
					{
						System.err.print("WARNING: "); //$NON-NLS-1$
					}
					else if (level == LogService.LOG_INFO)
					{
						System.err.print("INFO: "); //$NON-NLS-1$
					}
					else if (level == LogService.LOG_DEBUG)
					{
						System.err.print("DEBUG: "); //$NON-NLS-1$
					}
					else
					{
						System.err.print("log level " + level + ": "); //$NON-NLS-1$ //$NON-NLS-2$
					}
					System.err.println(message);
					if (exception != null)
					{
						exception.printStackTrace(System.err);
					}
				}
			};
		}
		return logService;
	}

	/**
	 * @return the framework log
	 */
	public FrameworkLog getFrameworkLog()
	{
		if (frameworkLogTracker == null)
		{
			if (context == null)
			{
				return null;
			}
			frameworkLogTracker = new ServiceTracker<FrameworkLog, FrameworkLog>(context, FrameworkLog.class.getName(), null);
			frameworkLogTracker.open();
		}
		return frameworkLogTracker.getService();
	}

	/**
	 * @return the preference service
	 */
	public IPreferencesService getPreferencesService()
	{
		if (preferencesTracker == null)
		{
			if (context == null)
			{
				return null;
			}
			preferencesTracker = new ServiceTracker<IPreferencesService, IPreferencesService>(context,
					IPreferencesService.class.getName(), null);
			preferencesTracker.open();
		}
		return preferencesTracker.getService();
	}

}