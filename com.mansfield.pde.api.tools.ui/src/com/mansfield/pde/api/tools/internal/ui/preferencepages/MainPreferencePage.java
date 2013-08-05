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
package com.mansfield.pde.api.tools.internal.ui.preferencepages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.mansfield.pde.api.tools.internal.ui.ApiToolsMessages;

public class MainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public MainPreferencePage() {
		setDescription(ApiToolsMessages.Preferences_MainPage_Description);
	}

	public void init(IWorkbench workbench) {
		// do nothing
	}

	@Override
	protected Control createContents(Composite parent) {
		// something to fill the space
		Composite contentArea = new Composite(parent, SWT.None);
		return contentArea;
	}
	
}
