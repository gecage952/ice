/*******************************************************************************
 * Copyright (c) 2012, 2014- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.client.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ice.client.common.PropertySource;
import org.eclipse.ice.client.widgets.viz.service.IPlot;
import org.eclipse.ice.client.widgets.viz.service.IVizService;
import org.eclipse.ice.client.widgets.viz.service.IVizServiceFactory;
import org.eclipse.ice.datastructures.ICEObject.IUpdateable;
import org.eclipse.ice.datastructures.ICEObject.IUpdateableListener;
import org.eclipse.ice.datastructures.form.ResourceComponent;
import org.eclipse.ice.datastructures.resource.ICEResource;
import org.eclipse.ice.datastructures.resource.VizResource;
import org.eclipse.ice.iclient.uiwidgets.ISimpleResourceProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class is a FormPage that creates a page with table and metadata viewing
 * area for an ICE ResourceComponent.
 * 
 * @authors Jay Jay Billings, Taylor Patterson, Anna Wojtowicz, Jordan Deyton
 */
public class ICEResourcePage extends ICEFormPage implements ISelectionListener,
		IUpdateableListener {
	/**
	 * The ResourceComponent drawn by this page.
	 */
	private ResourceComponent resourceComponent;

	/**
	 * The current resource being managed by this page.
	 */
	private ICEResource currentResource = null;

	/**
	 * The ICEResourceView that holds resources for this page to display.
	 */
	private ICEResourceView resourceView;

	/**
	 * The primary composite for rendering the page.
	 */
	private Composite pageComposite;

	/**
	 * The layout that stacks the plot and browser composites. We use a
	 * StackLayout because we only show either the {@link #browser} or the
	 * {@link #plotComposite}.
	 */
	private final StackLayout stackLayout = new StackLayout();

	/**
	 * A browser to display the files/images from resources.
	 */
	private Browser browser;

	/**
	 * The service factory for visualization tools. This can be queried for
	 * vizualization services.
	 */
	private IVizServiceFactory vizFactory;

	/**
	 * The map that holds any existing plots, keyed on the resource IDs.
	 */
	private final Map<String, IPlot> plots;
	/**
	 * The map that holds any drawn plots, keyed on the resource IDs. A
	 * Composite should only be added to this map if it already exists in
	 * {@link #plots}.
	 */
	private final Map<String, Composite> plotComposites;

	/**
	 * The default constructor.
	 * 
	 * @param editor
	 *            The FormEditor for which the Page should be constructed. This
	 *            should be an {@link ICEFormEditor}.
	 * @param id
	 *            The id of the page.
	 * @param title
	 *            The title of the page.
	 */
	public ICEResourcePage(FormEditor editor, String id, String title) {

		// Call the super constructor
		super(editor, id, title);

		// Set the ICEFormEditor
		if (!(editor instanceof ICEFormEditor)) {
			System.out.println("ICEResourcePage Message: Invalid FormEditor.");
		}

		// Setup the plot maps.
		plots = new HashMap<String, IPlot>();
		plotComposites = new HashMap<String, Composite>();

		return;
	}

	/**
	 * This operation overrides the default/abstract implementation of
	 * FormPage.createFormContents to create the contents of the
	 * ICEResourcePage.
	 * 
	 * @param managedForm
	 *            The Form widget on which the ICEResourcePage exists.
	 */
	protected void createFormContent(IManagedForm managedForm) {

		// Local Declarations
		final FormToolkit toolkit = managedForm.getToolkit();

		// Try to show the Resource View.
		try {
			getSite().getWorkbenchWindow().getActivePage()
					.showView(ICEResourceView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		// Get the Resource View and set its content to this page's
		// ResourceComponent.
		resourceView = (ICEResourceView) getSite().getWorkbenchWindow()
				.getActivePage().findView(ICEResourceView.ID);

		// Get the parent Composite for the Resource Page widgets and set its
		// layout to the StackLayout.
		pageComposite = managedForm.getForm().getBody();
		pageComposite.setLayout(stackLayout);

		// Register the page as a selection listener. The page returned by
		// getPage() is not the same as this page! There are some subtle UI
		// differences under the hood.
		getSite().getPage().addSelectionListener(this);
		// Add a dispose event listener on the parent. If disposed at any point,
		// remove it from the workbench's SelectionService listeners (or else it
		// will attempt to refresh disposed widgets).
		pageComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				getSite().getWorkbenchWindow().getSelectionService()
						.removeSelectionListener(ICEResourcePage.this);
			}
		});

		// Create the browser.
		browser = createBrowser(pageComposite, toolkit);
		stackLayout.topControl = browser;
		pageComposite.layout();

		return;
	}

	/**
	 * This operation draws a browser on the page that displays the selected
	 * ICEResource.
	 * 
	 * @param formToolkit
	 *            The FormToolkit that is used to create SWT widgets for this
	 *            page.
	 * @param form
	 *            The IManagedForm on which the table should be drawn.
	 */
	protected Browser createBrowser(Composite parent, FormToolkit toolkit) {

		Browser browser = null;

		// It is possible that constructing the Browser throws an SWTError.
		// Thus, to create the browser, we must use a try-catch block.
		try {
			// Initialize the browser and apply the layout. It should use a
			// FillLayout so its contents take up all available space.
			browser = new Browser(parent, SWT.NONE);
			toolkit.adapt(browser);
			browser.setLayout(new FillLayout());

			// Get the current selection from the Resource View and set the
			// current Resource to it.
			ISelection selection;
			selection = getSite().getPage().getSelection(ICEResourceView.ID);
			currentResource = getResourceFromSelection(selection);

			// Display the default-selected Resource from the Resource View in
			// the browser, or a message that no resource is available.
			if (currentResource != null
					&& !(currentResource instanceof VizResource)) {
				browser.setUrl(currentResource.getPath().toString());
			} else {
				browser.setText("<html><body><center>No resources available.</center></body></html>");
			}
		} catch (SWTError e) {
			System.out.println("Client Message: "
					+ "Could not instantiate Browser: " + e.getMessage());
		}

		return browser;
	}

	/**
	 * Attempts to determine the {@link ICEResource} from the selection. The
	 * selection is assumed to be from the {@link #resourceView}, and so its
	 * selection structure applies.
	 * 
	 * @param selection
	 *            The selection to convert.
	 * @return The Resource from the selection, or null if the selection was
	 *         invalid.
	 */
	private ICEResource getResourceFromSelection(ISelection selection) {
		ICEResource selectedResource = null;
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			// Strings must be looked up in the Resource View's map.
			if (element instanceof String) {
				selectedResource = resourceView.resourceChildMap.get(element);
			}
			// PropertySources should wrap ICEResources.
			else if (element instanceof PropertySource) {
				PropertySource source = (PropertySource) element;
				selectedResource = (ICEResource) source.getWrappedData();
			}
		}
		return selectedResource;
	}

	/**
	 * This operation overrides the default/abstract implementation of
	 * ISelectionListener.selectionChanged to display the resource selected in
	 * the ICEResourceView.
	 * 
	 * @param part
	 *            The IWorkbenchPart that called this function.
	 * @param selection
	 *            The ISelection chosen in the part parameter.
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		// If the selection comes from the Resource View, update the view.
		if (ICEResourceView.ID.equals(part.getSite().getId())) {
			ICEResource selectedResource = getResourceFromSelection(selection);

			// Refresh the page's widgets based on the selected resource.
			if (selectedResource != null) {
				setCurrentResource(selectedResource);
			}
		}

		return;
	}

	/**
	 * Updates the Resource Page's widgets to render the specified Resource.
	 * <p>
	 * <b>Note:</b> This method assumes it is called from the UI thread.
	 * </p>
	 * 
	 * @param resource
	 *            The resource to render. Assumed not to be {@code null}.
	 */
	private void setCurrentResource(ICEResource resource) {

		if (resource != currentResource) {
			currentResource = resource;

			// VizResources should not use the browser. However, if it cannot be
			// rendered with available VizResources, we should try using the
			// browser.
			boolean useBrowser = !(resource instanceof VizResource);
			if (!useBrowser) {
				VizResource vizResource = (VizResource) resource;
				Composite plotComposite = null;

				// Find the drawn plot for this resource. If it does not exist,
				// attempt to draw it.
				String key = getPlotKey(vizResource);
				plotComposite = plotComposites.get(key);
				if (plotComposite == null) {
					plotComposite = createPlotComposite(pageComposite,
							vizResource);
				}

				// If the plot and its drawn Composite could be found, update
				// the StackLayout. Otherwise, we should try using the browser.
				if (plotComposite != null) {
					stackLayout.topControl = plotComposite;
					pageComposite.layout();
				} else {
					useBrowser = true;
				}
			}

			// If the Resource is a regular Resource or cannot be rendered via
			// the VizServices, try to open it in the browser.
			if (useBrowser && browser != null && !browser.isDisposed()) {
				// Update the browser.
				browser.setUrl(resource.getPath().toString());
				stackLayout.topControl = browser;
				pageComposite.layout();
			}
		}

		return;
	}

	/**
	 * Gets the resource's key for use in the plot maps.
	 * 
	 * @param resource
	 *            The resource whose key should be determined. Assumed not to be
	 *            {@code null}.
	 * @return The resource's key in the plot maps.
	 */
	private String getPlotKey(ICEResource resource) {
		return resource.getPath().toString();
	}

	/**
	 * This method queries each {@code IVizService} from the {@link #vizFactory}
	 * until it finds the first one that can create an {@link IPlot} for the
	 * specified resource. If one could be created, it will be added to the map
	 * of {@link #plots}.
	 * 
	 * @param resource
	 *            The resource that needs an {@link IPlot}.
	 * @return The plot, or {@code null} if one could not be created.
	 */
	private IPlot createPlot(VizResource resource) {
		IPlot plot = null;

		String[] serviceNames = vizFactory.getServiceNames();
		for (String serviceName : serviceNames) {
			// Get the next IVizService.
			IVizService service = vizFactory.get(serviceName);
			if (service != null) {
				// Try to create a plot with the service. If one was created, it
				// will need to go into the map of plots.
				try {
					plot = service.createPlot(resource.getPath());
					if (plot != null) {
						plots.put(getPlotKey(resource), plot);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return plot;
	}

	/**
	 * This method attempts to create a plot {@code Composite} for the specified
	 * resource. If one could be created, it will be added to the map of
	 * {@link #plotComposites}.
	 * <p>
	 * <b>Note (1):</b> This method assumes it is called from the UI thread.
	 * </p>
	 * <p>
	 * <b>Note (2):</b> If there is not a corresponding {@link IPlot} in the map
	 * of {@link #plots}, no {@code Composite} will be created.
	 * </p>
	 * 
	 * @param parent
	 *            The parent {@code Composite} in which to draw the new plot
	 *            {@code Composite}.
	 * @param resource
	 *            The resource that needs a plot {@code Composite}.
	 * @return The plot {@code Composite}, or {@code null} if one could not be
	 *         created.
	 */
	private Composite createPlotComposite(Composite parent, VizResource resource) {
		Composite plotComposite = null;

		// First, check the map of plot Composites.
		String key = getPlotKey(resource);

		IPlot plot = plots.get(key);
		if (plot != null) {

			// Try to get the available categories and plot types, then try to
			// plot the first available one.
			try {
				Map<String, String[]> plotTypes = plot.getPlotTypes();

				// Find the first category and plot type.
				String category = null;
				String[] types = null;
				Iterator<Entry<String, String[]>> iter = plotTypes.entrySet()
						.iterator();
				while (iter.hasNext() && (types == null || types.length == 0)) {
					Entry<String, String[]> entry = iter.next();
					category = entry.getKey();
					types = entry.getValue();
				}

				// TODO We will want to expose the categories and plot types to
				// the user.

				// If there is an available category and plot type, try to
				// render the plot.
				if (types != null && types.length > 0) {
					String plotType = types[0];

					// Try to draw the contents of the plot Composite. If
					// successfully drawn, add it to the map of plot Composites.
					try {
						FormToolkit toolkit = getManagedForm().getToolkit();
						plotComposite = toolkit.createComposite(parent);
						plotComposite.setLayout(new FillLayout());
						plot.draw(category, plotType, plotComposite);
						plotComposites.put(key, plotComposite);
					}
					// If the plot could not be drawn, dispose the plot
					// Composite.
					catch (Exception drawException) {
						drawException.printStackTrace();
						plotComposite.dispose();
						plotComposite = null;
					}
				}
			} catch (Exception plotTypeException) {
				plotTypeException.printStackTrace();
			}
		}

		return plotComposite;
	}

	/**
	 * This operation sets the IVizServiceFactory that should be used to create
	 * plots.
	 * 
	 * @param service
	 *            The service factory that should be used
	 */
	public void setVizService(IVizServiceFactory factory) {
		vizFactory = factory;
	}

	/**
	 * This operation sets the ResourceComponent that should be used by the
	 * ICEResourcePage. It also registers the ICEResourcePage with the
	 * ResourceComponent so that it can be notified of state changes through the
	 * IUpdateableListener interface.
	 * 
	 * @param component
	 *            The ResourceComponent
	 */
	public void setResourceComponent(ResourceComponent component) {

		if (component != resourceComponent) {

			// Get the Display for updating the UI from the page's Composite.
			final Display display;
			if (pageComposite != null && !pageComposite.isDisposed()) {
				display = pageComposite.getDisplay();
			} else {
				display = null;
			}

			// If necessary, unregister from the old ResourceComponent.
			if (resourceComponent != null) {
				resourceComponent.unregister(this);
			}

			// ---- Clear the Resource Page widgets. ---- //
			// Clear out the old metadata that can be done from outside the UI
			// thread.
			plots.clear();
			currentResource = null;

			// Update the UI and dispose of any stale UI pieces.
			if (display != null) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						// Clear the browser and make it the top widget.
						browser.setText("<html><body><center>No resources available.</center></body></html>");
						stackLayout.topControl = browser;
						pageComposite.layout();

						// Dispose any plot Composites.
						for (Composite plotComposite : plotComposites.values()) {
							plotComposite.dispose();
						}
						plotComposites.clear();

						return;
					}
				});
			}
			// ------------------------------------------ //

			// Set the reference to the new ResourceComponent.
			this.resourceComponent = component;

			// If not null, register for updates from it and trigger an update
			// to sync the ResourceComponent.
			if (component != null) {
				resourceComponent.register(this);
				update(resourceComponent);

				// Set the default selection in the Resource View and update the
				// contents of the Resource Page's widgets. This must be done on
				// the UI thread.
				if (display != null) {
					// Get the current selection from the Resource View and set
					// the current Resource to it if possible.
					ISelection selection;
					selection = getSite().getPage().getSelection(
							ICEResourceView.ID);
					final ICEResource currentResource = getResourceFromSelection(selection);
					if (currentResource != null) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								setCurrentResource(currentResource);
							}
						});
					}
				}
			}
		}

		return;
	}

	/**
	 * This operation retrieves the ResourceComponent that has been rendered by
	 * the ICEResourcePage or null if the component does not exist.
	 * 
	 * @return The ResourceComponent or null if the component was not previously
	 *         set.
	 */
	public ResourceComponent getResourceComponent() {
		return resourceComponent;
	}

	/**
	 * This operation sets the ISimpleResource provider that should be used by
	 * the output page to load ICEResources.
	 * 
	 * @param provider
	 *            The ISimpleResourceProvider
	 */
	public void setResourceProvider(ISimpleResourceProvider provider) {

		// Set the provider if it is not null
		if (provider != null) {
			// TODO
		}
		return;
	}

	/**
	 * This method is called whenever the current {@link #resourceComponent} is
	 * updated. It adds any new plots to the map of {@link #plots} if possible.
	 */
	@Override
	public void update(IUpdateable component) {

		// TODO Remove any old plots.

		if (component != null && component == resourceComponent) {
			// Get a local copy of the ResouceComponent.
			ResourceComponent resourceComponent = (ResourceComponent) component;

			// Create plots for any VizResources in the ResourceComponent that
			// do not already have plots.
			for (ICEResource resource : resourceComponent.getResources()) {
				if (resource instanceof VizResource) {
					// Try to get the existing plot.
					IPlot plot = plots.get(getPlotKey(resource));
					// If there is no plot already, try to create one.
					if (plot == null) {
						plot = createPlot((VizResource) resource);
					}
				}
			}
		}

		return;
	}
}