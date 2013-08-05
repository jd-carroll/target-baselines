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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.model.ApiModelFactory;
import org.eclipse.pde.api.tools.internal.model.Messages;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiBaselineManager;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.search.SkippedComponent;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.eclipse.pde.api.tools.ui.internal.ApiToolsLabelProvider;
import org.eclipse.pde.api.tools.ui.internal.ApiUIPlugin;
import org.eclipse.pde.api.tools.ui.internal.preferences.ApiBaselinesConfigurationBlock;
import org.eclipse.pde.api.tools.ui.internal.preferences.PreferenceMessages;
import org.eclipse.pde.api.tools.ui.internal.wizards.ApiBaselineWizard;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEPreferencesManager;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.target.ExternalFileTargetHandle;
import org.eclipse.pde.internal.core.target.LocalTargetHandle;
import org.eclipse.pde.internal.core.target.TargetDefinition;
import org.eclipse.pde.internal.core.target.WorkspaceFileTargetHandle;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.IPreferenceConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.shared.target.StyledBundleLabelProvider;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.pde.internal.ui.wizards.target.EditTargetDefinitionWizard;
import org.eclipse.pde.internal.ui.wizards.target.MoveTargetDefinitionWizard;
import org.eclipse.pde.internal.ui.wizards.target.NewTargetDefinitionWizard2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import com.mansfield.pde.api.tools.internal.ui.SWTFactory;

@SuppressWarnings("restriction")
public class TargetBaselinePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String TARGET_BASELINE_EXTENSION = "@target"; //$NON-NLS-1$

	/**
	 * Constant key value used to store data in table items if they are moved to a new location
	 */
	private final static String DATA_KEY_MOVED_LOCATION = "movedLocation"; //$NON-NLS-1$
	
	private static final int NULL_DEFINITION = -1;
	
	// This is a candidate to be added to the ApiModelFactory
	private static IApiComponent[] addComponents(IApiBaseline baseline, ITargetDefinition targetDefinition, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.configuring_baseline, 50);
		IApiComponent[] result = null;
		try {
			// Acquire the service
			ITargetPlatformService service = (ITargetPlatformService) ApiPlugin.getDefault().acquireService(ITargetPlatformService.class.getName());
			IApiBaselineManager manager = ApiPlugin.getDefault().getApiBaselineManager();

			// Since this method is specific to Target's and the target would have to resolve,
			// if OSGi is not running then the environment is not in a valid state and we cannot 
			// proceed.
			if (service == null || manager == null) {
				return null;
			}

			Util.updateMonitor(subMonitor, 1);
			subMonitor.subTask(Messages.resolving_target_definition);

			ITargetLocation[] containers = targetDefinition.getTargetLocations();
			if (containers == null) {
				return null;
			}

			subMonitor.setWorkRemaining(30 * containers.length);
			List<TargetBundle> targetBundles = new ArrayList<TargetBundle>(79);
			for (int i = 0; i < containers.length; i++) {
				containers[i].resolve(targetDefinition, subMonitor.newChild(15));
				targetBundles.addAll(Arrays.asList(containers[i].getBundles()));
			}

			List<IApiComponent> components = new ArrayList<IApiComponent>(targetBundles.size());
			if (targetBundles.size() > 0) {
				subMonitor.setWorkRemaining(targetBundles.size());
				for (int i = 0; i < targetBundles.size(); i++) {
					Util.updateMonitor(subMonitor, 1);
					TargetBundle bundle = targetBundles.get(i);
					if (!bundle.isSourceBundle()) {
						IApiComponent component = ApiModelFactory.newApiComponent(baseline, URIUtil.toFile(bundle.getBundleInfo().getLocation()).getAbsolutePath());
						if (component != null) {
							subMonitor.subTask(NLS.bind(Messages.adding_component__0, component.getSymbolicName()));
							components.add(component);
						}
					}
				}
			}

			IApiBaseline existing = manager.getApiBaseline(baseline.getName());
			if (existing != null) {
				manager.removeApiBaseline(existing.getName());
			}
			manager.addApiBaseline(baseline);

			result = components.toArray(new IApiComponent[components.size()]);
			if (result != null) {
				baseline.addApiComponents(result);
				return result;
			}
			
			return new IApiComponent[0];
		}
		finally {
			subMonitor.done();
		}
	}
	
	// This is a candidate to be added to the Util
	public static final Comparator<IApiElement> componentSorter = new Comparator<IApiElement>() {
		public int compare(IApiElement o1, IApiElement o2) {
			if(o1 instanceof IApiComponent && o2 instanceof IApiComponent) {
				int eql = ((IApiComponent)o1).getSymbolicName().compareTo(((IApiComponent)o2).getSymbolicName());
				if (eql != 0) {
					return eql;
				}
				
				return ((IApiComponent)o1).getVersion().compareTo(((IApiComponent)o2).getVersion());				
			}
			if(o1 instanceof SkippedComponent && o2 instanceof SkippedComponent) {
				int eql = ((SkippedComponent)o1).getComponentId().compareTo(((SkippedComponent)o2).getComponentId());
				if (eql != 0) {
					return eql;
				}
				
				return ((SkippedComponent)o1).getVersion().compareTo(((SkippedComponent)o2).getVersion());
			}
			return -1;
		}
	};
	
	// This is a candidate to be added to the Util
	private static boolean areEqual(IApiComponent comp1, IApiComponent comp2) {
		if (comp1 == null) {
			return comp2 == null;
		}
		if (comp2 == null) {
			return false;
		}
		
		if (comp1.getClass() != comp2.getClass()) {
			return false;
		}
		
		boolean isEqual = Util.equalsOrNull(comp1.getName(), comp2.getName());
		isEqual |= Util.equalsOrNull(comp1.getSymbolicName(), comp2.getSymbolicName());
		isEqual |= Util.equalsOrNull(comp1.getVersion(), comp2.getVersion());
		if (!isEqual) {
			return false;
		}
		
		if (comp1.getType() != comp2.getType()) {
			return false;
		}
		
		String[] envs1;
		String[] envs2;
		try {
			envs1 = comp1.getExecutionEnvironments();
			envs2 = comp2.getExecutionEnvironments();
		} catch (CoreException e) {
			return false;
		}
		
		if (envs1.length != envs2.length) {
			// CODE_REVIEW
			// ... Is this true?
			return false;
		}
		
		for (int i = 0; i < envs1.length && isEqual; i++) {
			isEqual &= envs1[i].equals(envs2[i]);
		}
		
		if (!isEqual) {
			return false;
		}
		
		return true;
	}
	
	private class DefinitionLocationLabelProvider extends StyledBundleLabelProvider {

		ApiToolsLabelProvider apiToolsProvider;
		
		public DefinitionLocationLabelProvider(boolean showVersion, boolean appendResolvedVariables) {
			super(showVersion, appendResolvedVariables);
			
			apiToolsProvider = new ApiToolsLabelProvider() {
				protected boolean isDefaultBaseline(Object element) {
					return isDefault(element);
				}
			};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
		 */
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			cell.setText(getText(element));
			cell.setImage(getImage(element));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.pde.internal.ui.shared.target.StyledBundleLabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			ILabelProvider provider = (ILabelProvider) Platform.getAdapterManager().getAdapter(element, ILabelProvider.class);
			if (provider != null) {
				return provider.getImage(element);
			}

			Image image = super.getImage(element);
			if (image == null) {
				image = apiToolsProvider.getImage(element);
			}

			return image;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.pde.internal.ui.shared.target.StyledBundleLabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			ILabelProvider provider = (ILabelProvider) Platform.getAdapterManager().getAdapter(element, ILabelProvider.class);
			if (provider != null) {
				return provider.getText(element);
			}

			String text = super.getText(element);
			if (text == null || text.length() == 0) {
				apiToolsProvider.getText(element);
			}

			return text;
		}

	}
	
	private class TargetNameLabelProvider extends StyledCellLabelProvider {

		// Definition corresponding to running host
		private TargetDefinition fRunningHost;
		
		private Font fTextFont;
		ApiToolsLabelProvider apiToolsProvider;
		
		public TargetNameLabelProvider() {
			PDEPlugin.getDefault().getLabelProvider().connect(this);
			
			ITargetPlatformService service = getTargetService();
			if (service != null) {
				fRunningHost = (TargetDefinition) service.newDefaultTarget();
			}
			
			apiToolsProvider = new ApiToolsLabelProvider() {
				protected boolean isDefaultBaseline(Object element) {
					return isDefault(element);
				}
			};
		}

		/**
		 * @return a bold dialog font
		 */
		private Font getBoldFont() {
			if (fTextFont == null) {
				Font dialogFont = JFaceResources.getDialogFont();
				FontData[] fontData = dialogFont.getFontData();
				for (int i = 0; i < fontData.length; i++) {
					FontData data = fontData[i];
					data.setStyle(SWT.BOLD);
				}
				Display display = getShell().getDisplay();
				fTextFont = new Font(display, fontData);
			}
			return fTextFont;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
		 */
		public void update(ViewerCell cell) {
			final Object definition = cell.getElement();
			Styler style = new Styler() {
				public void applyStyles(TextStyle textStyle) {
					int identifier = getIdentifier(definition);
					if (newDefinitionId == identifier) {
						textStyle.font = getBoldFont();
					}
				}
			};

			String name = getDefinitionName(definition);

			int identifier = getIdentifier(definition);
			if (newDefinitionId == identifier) {
				name = name + NLS.bind(org.eclipse.pde.api.tools.ui.internal.Messages.ApiToolsLabelProvider_default_baseline_place_holder, 
						org.eclipse.pde.api.tools.ui.internal.Messages.ApiToolsLabelProvider_default_baseline);
			}

			StyledString styledString = new StyledString(name, style);
			if (definition instanceof IApiBaseline) {
				// do nothing
			} else if (definition instanceof ITargetDefinition) {
				ITargetHandle targetHandle = ((ITargetDefinition) definition).getHandle();
				if (targetHandle instanceof WorkspaceFileTargetHandle) {
					IFile file = ((WorkspaceFileTargetHandle) targetHandle).getTargetFile();
					String location = " - " + file.getFullPath(); //$NON-NLS-1$
					styledString.append(location, StyledString.DECORATIONS_STYLER);
				} else if (targetHandle instanceof ExternalFileTargetHandle) {
					URI uri = ((ExternalFileTargetHandle) targetHandle).getLocation();
					String location = " - " + uri.toASCIIString(); //$NON-NLS-1$
					styledString.append(location, StyledString.DECORATIONS_STYLER);
				} else {
					String location = (String) cell.getItem().getData(DATA_KEY_MOVED_LOCATION);
					if (location != null) {
						location = " - " + location; //$NON-NLS-1$
						styledString = new StyledString(name, style);
						styledString.append(location, StyledString.QUALIFIER_STYLER);
					}
				}
			}
			
			cell.setText(styledString.toString());
			cell.setStyleRanges(styledString.getStyleRanges());
			cell.setImage(getImage(definition));
			super.update(cell);
		}

		private Image getImage(Object definition) {
			if (definition instanceof IApiBaseline) {
				return apiToolsProvider.getImage(definition);
			}
			
			if (definition instanceof ITargetDefinition) {
				int flag = 0;
				if (fRunningHost != null && fRunningHost.isContentEquivalent((ITargetDefinition) definition)) {
					return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PRODUCT_BRANDING, flag);
				}
				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_TARGET_DEFINITION, flag);
			}
			
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.StyledCellLabelProvider#dispose()
		 */
		public void dispose() {
			PDEPlugin.getDefault().getLabelProvider().disconnect(this);
			if (fTextFont != null) {
				fTextFont.dispose();
				fTextFont = null;
			}
			super.dispose();
		}
	}

	// Table viewer
	private CheckboxTableViewer fTableViewer;

	// Buttons
	private Button fReloadButton;
	private Button fAddButton;
	private Button fEditButton;
	private Button fRemoveButton;
	private Button fMoveButton;

	// Text displaying additional information
	private TableViewer fDetails;
	
	//
	private ApiBaselinesConfigurationBlock compilers_ConfigBlock;

	// Initial collection of baselines, each object is realized into working copies
	private List<Object> allDefinitions;
	private Map<Integer, Object> identifiers;

	private List<ITargetDefinition> targetDefinitions;
	private List<IApiBaseline> baselineDefinitions;

	/**
	 * Modified definitions (to be modified on apply) 
	 */
	private List<Object> removed_Definitions;
	private Map<ITargetDefinition, IPath> moved_TargetDefinitions;

	// -1 is a special value indicating undefined
	private int activeDefinitionId = NULL_DEFINITION;
	private int newDefinitionId = NULL_DEFINITION;

	private int rebuild_Count;
	private boolean is_Dirty;

	private boolean active_ContentChanged;
	// boolean flag describing whether or not the most recent edit wizard changed the content
	// this is separate from active_ContnetChanged which describes if the content has ever changed
	private boolean wizard_ContentChanged;
	
	private ITargetPlatformService fTargetPlatformService;
	private IApiBaselineManager baseline_Manager;

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// ensures default targets are created when page is opened (if not created yet)
		PluginModelManager manager = PDECore.getDefault().getModelManager();
		if (!manager.isInitialized()) {
			manager.getExternalModelManager();
		}

		allDefinitions = new ArrayList<Object>();
		identifiers = new HashMap<Integer, Object>();
		// reserve -1 as a special value defining undefined
		identifiers.put(NULL_DEFINITION, new Object());

		targetDefinitions = new ArrayList<ITargetDefinition>();
		baselineDefinitions = new ArrayList<IApiBaseline>();

		removed_Definitions = new ArrayList<Object>();
		moved_TargetDefinitions = new HashMap<ITargetDefinition, IPath>(1);

		baseline_Manager = ApiPlugin.getDefault().getApiBaselineManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		Composite contentArea = SWTFactory.createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(contentArea);

		createTargetProfilesGroup(contentArea);

		Dialog.applyDialogFont(contentArea);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.TARGET_PLATFORM_PREFERENCE_PAGE);

		return contentArea;
	}

	private void createTargetProfilesGroup(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(comp);
		GridDataFactory.fillDefaults().grab(true, true).hint(350, SWT.DEFAULT).applyTo(comp);

		Label message = SWTFactory.createLabel(comp, SWT.WRAP, PDEUIMessages.TargetPlatformPreferencePage2_0);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(message);

		Label spacer = SWTFactory.createLabel(comp);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, 1).applyTo(spacer);

		Composite tableComposite = SWTFactory.createComposite(comp);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(tableComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);

		Label tableTitle = SWTFactory.createLabel(tableComposite, PDEUIMessages.TargetPlatformPreferencePage2_2);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(tableTitle);

		fTableViewer = CheckboxTableViewer.newCheckList(tableComposite, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(250, SWT.DEFAULT).grab(true, true).applyTo(fTableViewer.getTable());

		fTableViewer.setLabelProvider(new TargetNameLabelProvider());
		fTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object definition = event.getElement();
				if(event.getChecked()) {
					fTableViewer.setCheckedElements(new Object[] {definition});
					int id = getIdentifier(definition);
					newDefinitionId = id;
				} else {
					fTableViewer.setChecked(definition, event.getChecked());
					newDefinitionId = NULL_DEFINITION;
				}
				rebuild_Count = 0;
				fTableViewer.refresh(true);
				is_Dirty = true;
			}
		});
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
				updateDetails();
			}
		});
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				handleEdit(ss.getFirstElement());
			}
		});
		fTableViewer.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.NONE && e.keyCode == SWT.DEL) {
					handleRemove();
				}
			}
		});

		fTableViewer.setComparator(new ViewerComparator() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				String name1 = getDefinitionName(e1);
				String name2 = getDefinitionName(e2);
				if (name1 == null) {
					return -1;
				}
				if (name2 == null) {
					return 1;
				}
				return name1.compareToIgnoreCase(name2);
			}
		});

		Composite buttonComposite = SWTFactory.createComposite(tableComposite);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonComposite);
		GridDataFactory.fillDefaults().applyTo(buttonComposite);

		fReloadButton = SWTFactory.createPushButton(buttonComposite, PDEUIMessages.TargetPlatformPreferencePage2_16);
		int width = SWTFactory.getButtonWidthHint(fReloadButton);
		GridDataFactory.fillDefaults().hint(width, SWT.DEFAULT).applyTo(fReloadButton);
		fReloadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleReload();
			}
		});

		spacer = SWTFactory.createLabel(buttonComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, 1).applyTo(spacer);

		fAddButton = SWTFactory.createPushButton(buttonComposite, PDEUIMessages.TargetPlatformPreferencePage2_3);
		GridDataFactory.fillDefaults().applyTo(fAddButton);
		fAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAdd();
			}
		});

		fEditButton = SWTFactory.createPushButton(buttonComposite, PDEUIMessages.TargetPlatformPreferencePage2_5);
		GridDataFactory.fillDefaults().applyTo(fEditButton);
		fEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleEdit(getCurrentSelection().get(0));
			}
		});

		fRemoveButton = SWTFactory.createPushButton(buttonComposite, PDEUIMessages.TargetPlatformPreferencePage2_7);
		GridDataFactory.fillDefaults().applyTo(fRemoveButton);
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});

		// TODO: Bad Label
		fMoveButton = SWTFactory.createPushButton(buttonComposite, PDEUIMessages.TargetPlatformPreferencePage2_13, null);
		GridDataFactory.fillDefaults().applyTo(fMoveButton);
		fMoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMove();
			}
		});

		Composite detailsComposite = SWTFactory.createComposite(comp);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(detailsComposite);
		GridDataFactory.fillDefaults().applyTo(detailsComposite);

		Label locations = SWTFactory.createLabel(detailsComposite, PDEUIMessages.TargetPlatformPreferencePage2_25);
		GridDataFactory.fillDefaults().applyTo(locations);

		fDetails = new TableViewer(detailsComposite);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 50).grab(true, false).applyTo(fDetails.getTable());

		fDetails.setLabelProvider(new DefinitionLocationLabelProvider(true, true));
		fDetails.setContentProvider(new ArrayContentProvider());

		compilers_ConfigBlock = new ApiBaselinesConfigurationBlock((IWorkbenchPreferenceContainer) getContainer());
		compilers_ConfigBlock.createControl(comp, null);

		// Initialization steps

		// Bootstrap the buttons with their initial state
		updateButtons();

		// Add the existing API Baseline definitions
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IApiBaseline[] existingBaselines = baseline_Manager.getApiBaselines();
				List<IApiBaseline> baselines = new ArrayList<IApiBaseline>(existingBaselines.length);
				Collections.addAll(baselines, existingBaselines);
				// Find any persisted (active?) baselines with the @target extension
				// and remove them from the list of baselines since they should be
				// associated with a target
				for (int i = 0; i < baselines.size(); i++) {
					IApiBaseline baseline = baselines.get(i);
					if (baseline.getName().endsWith(TARGET_BASELINE_EXTENSION)) {
						baselines.remove(i--);
					}
				}
				
				buildIdentifiers(baselines);
				baselineDefinitions.addAll(baselines);
				allDefinitions.addAll(baselines);
			}
		});

		// Add the existing Target definitions
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				ITargetPlatformService service = getTargetService();
				if (service != null) {
					ITargetHandle[] targets = service.getTargets(null);
					for (int i = 0; i < targets.length; i++) {
						try {
							ITargetDefinition target = targets[i].getTargetDefinition();
							buildIdentifier(target);
							targetDefinitions.add(target);
							allDefinitions.add(target);
						} catch (CoreException e) {
							PDECore.log(e);
							setErrorMessage(e.getMessage());
						}
					}
				}
			}
		});

		// Load the definitions into the table
		fTableViewer.setInput(allDefinitions);

		// Set the active baseline, if any
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IApiBaseline activeBaseline = baseline_Manager.getDefaultApiBaseline();
				if (activeBaseline != null) {
					if (!activeBaseline.getName().endsWith(TARGET_BASELINE_EXTENSION)) {
						fTableViewer.setCheckedElements(new Object[] {activeBaseline});
						activeDefinitionId = getIdentifier(activeBaseline);
						newDefinitionId = activeDefinitionId;
					} else {
						int index = activeBaseline.getName().lastIndexOf(TARGET_BASELINE_EXTENSION);
						String name = activeBaseline.getName().substring(0, index);
						for (int i = 0; i < targetDefinitions.size(); i++) {
							ITargetDefinition target = targetDefinitions.get(i);
							if (target.getName().equals(name)) {
								try {
									IApiBaseline tempBaseline = ApiModelFactory.newApiBaseline("local_temp"); //$NON-NLS-1$
									try {
										addComponents(tempBaseline, target, new NullProgressMonitor());
									} catch (CoreException ex) {
										// do nothing, assume they do not match
										continue;
									}
									
									// Even though they are supposed to be returned by lookup order,
									// the order is non-deterministic (provable, bug?) thus we need to
									// sort the arrays
									IApiComponent[] activeComponents = activeBaseline.getApiComponents();
									Arrays.sort(activeComponents, componentSorter);
									IApiComponent[] tempComponents = tempBaseline.getApiComponents();
									Arrays.sort(tempComponents, componentSorter);
									
									if (activeComponents.length != tempComponents.length) {
										continue;
									}

									boolean equal = true;
									for (int j = 0; j < activeComponents.length && equal; j++) {
										// Since we are only interested in the bundles and not any SystemLibraryComponents
										// we can use the overridden equals() method in BundleComponent for the evaluation
										// If there is a system library then this will fail, as it should since Targets
										// cannot have system libraries
										equal &= areEqual(activeComponents[j], tempComponents[j]);
									}

									if (equal) {
										fTableViewer.setCheckedElements(new Object[] {target});
										activeDefinitionId = getIdentifier(target);
										newDefinitionId = activeDefinitionId;
										break;
									}
								} finally {
									baseline_Manager.removeApiBaseline("local_temp"); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
		});
		
		fTableViewer.refresh(true);
	}
	
	/**
	 * Returns if the specified definition is the default profile or
	 * not
	 * 
	 * @param definition
	 * @return if the profile is the default or not
	 */
	protected boolean isActive(Object definition) {
		int identifier = getIdentifier(definition);
		return activeDefinitionId == identifier;
	}

	/**
	 * Returns if the specified definition is the default profile or
	 * not
	 * 
	 * @param definition
	 * @return if the profile is the default or not
	 */
	protected boolean isDefault(Object definition) {
		int identifier = getIdentifier(definition);
		return newDefinitionId == identifier;
	}

	/**
	 * @return the current selection from the table viewer
	 */
	@SuppressWarnings("unchecked")
	protected List<Object> getCurrentSelection() {
		IStructuredSelection ss = (IStructuredSelection) fTableViewer.getSelection();
		if (ss.isEmpty()) {
			return new ArrayList<Object>();
		}
		return (List<Object>) ss.toList(); 
	}

	private void handleReload() {
		Object definition = identifiers.get(activeDefinitionId);
		
		if (definition instanceof IApiBaseline) {
			internalReloadBaselineDefinition((IApiBaseline) definition);
		} else if (definition instanceof ITargetDefinition) {
			internalReloadTargetDefinition((ITargetDefinition) definition);
		}
	}
	
	/**
	 * Open the new target platform wizard
	 */
	private void handleAdd() {
		NewTargetDefinitionWizard2 wizard = new NewTargetDefinitionWizard2();
		wizard.setWindowTitle(PDEUIMessages.TargetPlatformPreferencePage2_4);
		WizardDialog dialog = new WizardDialog(fAddButton.getShell(), wizard);
		if (dialog.open() == IDialogConstants.OK_ID) {
			ITargetDefinition definition = wizard.getTargetDefinition();
			if(definition != null) {
				buildIdentifier(definition);
				targetDefinitions.add(definition);
				allDefinitions.add(definition);
				fTableViewer.refresh();
				fTableViewer.setSelection(new StructuredSelection(definition), true);
				if(allDefinitions.size() == 1) {
					int identifier = getIdentifier(definition);
					newDefinitionId = identifier;
					fTableViewer.setCheckedElements(new Object[] {definition});
					fTableViewer.refresh(definition);
					rebuild_Count = 0;
				}
				is_Dirty = true;
			}
		}
	}

	/**
	 * Opens the selected target for editing
	 */
	private void handleEdit(Object definition) {
		Object modifiedDefinition = null;

		wizard_ContentChanged = false;
		if (definition instanceof IApiBaseline) {
			modifiedDefinition = doEditApiBaseline((IApiBaseline) definition);
		} else if (definition instanceof ITargetDefinition) {
			modifiedDefinition = doEditTarget((ITargetDefinition) definition);
		}

		if (modifiedDefinition != null) {
			// clear any pending edit updates
			removed_Definitions.add(modifiedDefinition);
			// Replace all references to the original with the new definition
			internalRemoveDefinition(definition);
			allDefinitions.remove(definition);
			setIdentifierFor(modifiedDefinition, definition);
			internalAddDefinition(modifiedDefinition);
			allDefinitions.add(modifiedDefinition);
			fTableViewer.refresh();
			if (isActive(definition)) {
				active_ContentChanged |= wizard_ContentChanged;
			}
			if (isDefault(definition)) {
				fTableViewer.setSelection(new StructuredSelection(modifiedDefinition), true);
				fTableViewer.setCheckedElements(new Object[] {modifiedDefinition});
				rebuild_Count = 0;
				fTableViewer.refresh(true);
			}
			is_Dirty = true;
		}
	}

	/**
	 * 
	 * @param definition
	 * @return
	 */
	private IApiBaseline doEditApiBaseline(IApiBaseline definition) {
		ApiBaselineWizard wizard = new ApiBaselineWizard(definition);
		WizardDialog dialog = new WizardDialog(ApiUIPlugin.getShell(), wizard);
		if (dialog.open() == IDialogConstants.OK_ID) {
			wizard_ContentChanged = wizard.contentChanged();
			return wizard.getProfile();
		}

		return null;		
	}

	/**
	 * 
	 * @param definition
	 * @return
	 */
	private ITargetDefinition doEditTarget(ITargetDefinition definition) {
		EditTargetDefinitionWizard wizard = new EditTargetDefinitionWizard(definition, true);
		wizard.setWindowTitle(PDEUIMessages.TargetPlatformPreferencePage2_6);
		WizardDialog dialog = new WizardDialog(fEditButton.getShell(), wizard);
		if (dialog.open() == IDialogConstants.OK_ID) {
			// wizard_ContentChanged = wizard.contentChanged();

			ITargetDefinition modifiedDefinition = wizard.getTargetDefinition();

			if (moved_TargetDefinitions.containsKey(definition)) {
				IPath moveLocation = moved_TargetDefinitions.remove(definition);
				moved_TargetDefinitions.put(modifiedDefinition, moveLocation);
			}

			return modifiedDefinition;
		}

		return null;		
	}

	/**
	 * Removes the selected targets
	 */
	private void handleRemove() {
		List<Object> definitions = getCurrentSelection();
		
		// If we are going to remove a workspace file, prompt to ask the user first
		boolean isWorkspace = false;
		for (Iterator<Object> iterator = definitions.iterator(); iterator.hasNext();) {
			Object definition = iterator.next();
			if (definition instanceof ITargetDefinition) {
				ITargetDefinition targetDefinition = (ITargetDefinition) definition;
				if (targetDefinition.getHandle() instanceof WorkspaceFileTargetHandle) {
					isWorkspace = true;
					break;
				}
			}
		}
		
		if (isWorkspace) {
			PDEPreferencesManager preferences = new PDEPreferencesManager(IPDEUIConstants.PLUGIN_ID);
			String choice = preferences.getString(IPreferenceConstants.PROP_PROMPT_REMOVE_TARGET);
			if (!MessageDialogWithToggle.ALWAYS.equalsIgnoreCase(choice)) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getShell(), PDEUIMessages.TargetPlatformPreferencePage2_19, PDEUIMessages.TargetPlatformPreferencePage2_20, PDEUIMessages.TargetPlatformPreferencePage2_21, false, PDEPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.PROP_PROMPT_REMOVE_TARGET);
				preferences.savePluginPreferences();
				if (dialog.getReturnCode() != IDialogConstants.YES_ID) {
					return;
				}
			}
		}
		
		for (Object definition : definitions) {
			// The only thing we wont be able to tell is if they delete the active definition
			// and then do not select a new one.  To address this, if the newDefinitionId == NULL_DEFINITION
			// always set the active baseline to null to clear it out.
			if (isActive(definition)) {
				active_ContentChanged = true;
			}
			if (isDefault(definition)) {
				newDefinitionId = NULL_DEFINITION;
				rebuild_Count = 0;
			}
			
			removed_Definitions.add(definition);
			internalRemoveDefinition(definition);
			allDefinitions.remove(definition);
			setIdentifierFor(null, definition);
		}
		fTableViewer.refresh();
		
		// ... ?
		// // Quick hack because the first refresh loses the checkedState, which is being used to bold the active target
		// fTableViewer.refresh(false);
		// fTableViewer.refresh(true);
	}

	/**
	 * Move the selected target to a workspace location
	 */
	private void handleMove() {
		MoveTargetDefinitionWizard wizard = new MoveTargetDefinitionWizard(moved_TargetDefinitions.values());
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		SWTUtil.setDialogSize(dialog, 400, 450);
		if (dialog.open() == IDialogConstants.OK_ID) {
			TableItem ti = fTableViewer.getTable().getItem(fTableViewer.getTable().getSelectionIndex());
			IPath newTargetLoc = wizard.getTargetFileLocation();
			IFile file = PDECore.getWorkspace().getRoot().getFile(newTargetLoc);
			ti.setData(DATA_KEY_MOVED_LOCATION, file.getFullPath().toString());
			IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
			moved_TargetDefinitions.put((ITargetDefinition) selection.getFirstElement(), wizard.getTargetFileLocation());
			fTableViewer.refresh(true);
		}
	}

	/**
	 * Update enabled state of buttons
	 */
	protected void updateButtons() {
		List<Object> selection = getCurrentSelection();
		int size = selection.size();
		fEditButton.setEnabled(size == 1);
		fRemoveButton.setEnabled(size > 0);
		if (size == 1 && selection.get(0) instanceof ITargetDefinition) {
			fMoveButton.setEnabled(((ITargetDefinition) selection.get(0)).getHandle() instanceof LocalTargetHandle);			
		} else {
			fMoveButton.setEnabled(false);
		}
		if (size == 1) {
			fReloadButton.setEnabled(isActive(selection.get(0)));
		} else {
			fReloadButton.setEnabled(false);
		}
	}

	/**
	 * Updates the details text box with information about the currently selected target 
	 */
	protected void updateDetails() {
		List<Object> selection = getCurrentSelection();
		if (selection.size() == 1) {
			fDetails.setInput(getDefinitionLocations(selection.get(0)));
		} else {
			fDetails.setInput(null);
		}
	}

	/**
	 * Returns the target platform service or <code>null</code> if the service could
	 * not be acquired.
	 * 
	 * @return target platform service or <code>null</code>
	 */
	private ITargetPlatformService getTargetService() {
		// CODE_REVIEW:
		// I'm not certain what the best approach is here.  From a performance standpoint,
		// I feel as though we should cache the service once it is retrieved. However, by
		// caching it locally we violate in internal implementation of PDECore.acquireService()
		// since it immediately calls bundleContext.ungetService (bug???). Yet when retrieving
		// the same service from APIPlugin.acquireService() caching it locally would not violate
		// anything since APIPlugin does not call bundleContext.ungetService.
		
		if (fTargetPlatformService == null) {
			fTargetPlatformService = (ITargetPlatformService) ApiPlugin.getDefault().acquireService(ITargetPlatformService.class.getName());
		}
	
		// return (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
		
		return fTargetPlatformService;
	}

	public void performDefaults() {
		performOk();
	}

	public boolean performOk() {
		// Check state conditions
		if (!is_Dirty) {
			return true;
		}
		
		ITargetPlatformService service = getTargetService();
		if (service == null) {
			return false;
		}
		
		// perform preload changes
		if (!applyChanges()) {
			return false;
		}
		
		compilers_ConfigBlock.performOK();
		
		// Since it is only possible to create new targets and not baselines
		// retrieve the baseline from the new definition (if any) and add
		// only that to the baseline manager
		IApiBaseline activeBaseline = null;
		try {
			activeBaseline = getBaselineFromDefinition(newDefinitionId);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		}

		if (activeDefinitionId != newDefinitionId || active_ContentChanged) {
			baseline_Manager.setDefaultApiBaseline(activeBaseline.getName());
			
			if (rebuild_Count < 1) {
				rebuild_Count++;
				IProject[] projects = Util.getApiProjects();
				// do not even ask if there are no projects to build
				if (projects != null) {
					if (MessageDialog.openQuestion(getShell(),
							PreferenceMessages.ApiProfilesPreferencePage_6,
							PreferenceMessages.ApiProfilesPreferencePage_7)) {
						Util.getBuildJob(projects).schedule();
					}
				}
			}
		}
		
		activeDefinitionId = newDefinitionId;
		is_Dirty = false;
		active_ContentChanged = false;
		
		return super.performOk();
	}
	
	private boolean applyChanges() {
		internalMoveDefinitions();
		
		try {
			internalRemoveDefinitions();
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), PDEUIMessages.TargetPlatformPreferencePage2_8, PDEUIMessages.TargetPlatformPreferencePage2_11, e.getStatus());
			return false;
		}
		
		try {
			internalSaveDefinitions();
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private void internalMoveDefinitions() {
		ITargetPlatformService service = getTargetService();
		
		// Move the marked definitions to workspace
		if (moved_TargetDefinitions.size() > 0) {
			Iterator<ITargetDefinition> iterator = moved_TargetDefinitions.keySet().iterator();
			while (iterator.hasNext()) {
				try {
					ITargetDefinition target = iterator.next();
					IPath path = moved_TargetDefinitions.get(target);
					IFile targetFile = PDECore.getWorkspace().getRoot().getFile(path);

					WorkspaceFileTargetHandle wrkspcTargetHandle = new WorkspaceFileTargetHandle(targetFile);
					ITargetDefinition newTarget = service.newTarget();
					service.copyTargetDefinition(target, newTarget);
					wrkspcTargetHandle.save(newTarget);
					
					// clear any pending edit updates
					removed_Definitions.add(target);
					// Replace all references to the original with the new definition
					internalRemoveDefinition(target);
					allDefinitions.remove(target);
					
					ITargetDefinition workspaceTarget = wrkspcTargetHandle.getTargetDefinition();
					setIdentifierFor(workspaceTarget, target);
					internalAddDefinition(workspaceTarget);
					allDefinitions.add(workspaceTarget);

					fTableViewer.refresh(false);
					// if (target == fActiveTarget) {
					// 	load = true;
					// 	toLoad = workspaceTarget;
					// }
				} catch (CoreException e) {
					PDEPlugin.log(e);
				}
			}
		}
		
		moved_TargetDefinitions.clear();
	}
	
	private void internalRemoveDefinitions() throws CoreException {
		// Remove any definitions that have been removed
		Iterator<Object> iterator = removed_Definitions.iterator();
		while (iterator.hasNext()) {
			Object definition = iterator.next();
			
			if (definition instanceof IApiBaseline) {
				String baselineName = ((IApiBaseline) definition).getName();
				baseline_Manager.removeApiBaseline(baselineName);
			} else if (definition instanceof ITargetDefinition) {
				ITargetPlatformService service = getTargetService();
				ITargetDefinition target = (ITargetDefinition) definition;
				service.deleteTarget(target.getHandle());
			}
			
			throw new IllegalStateException("Remove Definitions - Unknown definition type: " + definition.toString());
		}
		
		removed_Definitions.clear();
	}
	
	private void internalSaveDefinitions() throws CoreException {
		ITargetPlatformService service = getTargetService();
		
		// save others that are dirty
		Iterator<ITargetDefinition> iterator = targetDefinitions.iterator();
		while (iterator.hasNext()) {
			ITargetDefinition def = iterator.next();
			boolean save = true;
			if (def.getHandle().exists()) {
				ITargetDefinition original = def.getHandle().getTargetDefinition();
				if (((TargetDefinition) original).isContentEqual(def)) {
					save = false;
				}
			}
			if (save) {
				service.saveTargetDefinition(def);
			}
		}
	}
	
	private void internalReloadTargetDefinition(final ITargetDefinition target) {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell()) {
			protected void configureShell(Shell shell) {
				super.configureShell(shell);
				shell.setText(PDEUIMessages.TargetPlatformPreferencePage2_12);
			}
		};
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
					// Resolve the target
					target.resolve(monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			});
		} catch (InvocationTargetException e) {
			PDEPlugin.log(e);
			setErrorMessage(e.getMessage());
		} catch (InterruptedException e) {
			// Do nothing, resolve will happen when user presses ok
		}

		if (target.isResolved()) {
			// Check if the bundle resolution has errors
			IStatus bundleStatus = target.getStatus();
			if (bundleStatus.getSeverity() == IStatus.ERROR) {
				ErrorDialog.openError(getShell(), PDEUIMessages.TargetPlatformPreferencePage2_14, PDEUIMessages.TargetPlatformPreferencePage2_15, bundleStatus, IStatus.ERROR);
			}
		}
		fTableViewer.refresh(true);
	}
	
	private void internalReloadBaselineDefinition(IApiBaseline baseline) {
		
	}
	
	private IApiBaseline getBaselineFromDefinition(int identifier) throws CoreException {
		Object definition = identifiers.get(identifier);
		
		if (definition instanceof IApiBaseline) {
			return (IApiBaseline) definition;
		}
		
		if (!(definition instanceof ITargetDefinition)) {
			throw new IllegalStateException("Unknown definition type: " + definition.toString());
		}
		
		ITargetDefinition targetDefinition = (ITargetDefinition) definition;
		
		// Create the ApiBaseline to be used for the target
		IApiBaseline baseline = ApiModelFactory.newApiBaseline(targetDefinition.getName() + "@target");	
		
		IProgressMonitor monitor = new NullProgressMonitor();
		addComponents(baseline, targetDefinition, monitor);

		return baseline;
	}

	/**
	 * 
	 * @param definitions
	 */
	private void buildIdentifiers(List<? extends Object> definitions) {
		for (int i = 0; i < definitions.size(); i++) {
			buildIdentifier(definitions.get(i));
		}
	}

	/**
	 * 
	 * @param definition
	 */
	private void buildIdentifier(Object definition) {
		int hash = definition.hashCode();
		Object collision = identifiers.get(hash);
		while (collision != null) {
			collision = identifiers.get(++hash);
		}

		identifiers.put(hash, definition);
	}

	/**
	 * 
	 * @param definition
	 * @return
	 */
	private int getIdentifier(Object definition) {
		Iterator<Entry<Integer, Object>> entries = identifiers.entrySet().iterator();
		while(entries.hasNext()) {
			Entry<Integer, Object> entry = entries.next();
			if (definition == entry.getValue()) {
				return entry.getKey();
			}
		}

		throw new IllegalStateException("The specified definition is not in the list of existing definitions! " + definition.toString());
	}

	/**
	 * 
	 * @param newDefinition
	 * @param oldDefinition
	 */
	private void setIdentifierFor(Object newDefinition, Object oldDefinition) {
		int identifier = getIdentifier(oldDefinition);
		
		if (newDefinition == null) {
			identifiers.remove(identifier);
		} else {
			identifiers.put(identifier, newDefinition);
		}
	}

	/**
	 * 
	 * @param definition
	 * @return
	 */
	private String getDefinitionName(Object definition) {
		if (definition instanceof IApiBaseline) {
			return ((IApiBaseline) definition).getName();
		} 
		
		if (definition instanceof ITargetDefinition) {
			String name = ((ITargetDefinition) definition).getName();
			if (name == null || name.length() == 0) {
				ITargetHandle targetHandle = ((ITargetDefinition) definition).getHandle();	
				name = targetHandle.toString();
			}
			return name;
		}

		return null;
	}
	
	/**
	 * 
	 * @param definition
	 * @return
	 */
	private Object[] getDefinitionLocations(Object definition) {
		if (definition instanceof IApiBaseline) {
			String location =  ((IApiBaseline) definition).getLocation();
			return new String[] {location};			
		} 
		
		if (definition instanceof ITargetDefinition) {
			return ((ITargetDefinition) definition).getTargetLocations();
		}

		return null;
	}

	/**
	 * 
	 * @param definition
	 */
	private void internalRemoveDefinition(Object definition) {
		if (definition instanceof IApiBaseline) {
			baselineDefinitions.remove((IApiBaseline) definition);
		} else if (definition instanceof ITargetDefinition) {
			targetDefinitions.remove((ITargetDefinition) definition);
		}
	}
	
	/**
	 * 
	 * @param definition
	 */
	private void internalAddDefinition(Object definition) {
		if (definition instanceof IApiBaseline) {
			baselineDefinitions.add((IApiBaseline) definition);
		} else if (definition instanceof ITargetDefinition) {
			targetDefinitions.add((ITargetDefinition) definition);
		}
	}

}
