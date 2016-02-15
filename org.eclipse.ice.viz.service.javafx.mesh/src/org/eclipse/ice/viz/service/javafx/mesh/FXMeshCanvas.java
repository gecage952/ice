/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tony McCrary (tmccrary@l33tlabs.com), Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.javafx.mesh;

import org.eclipse.ice.viz.service.javafx.canvas.AbstractViewer;
import org.eclipse.ice.viz.service.javafx.canvas.FXVizCanvas;
import org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas;
import org.eclipse.ice.viz.service.mesh.properties.MeshSelection;
import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ice.viz.service.javafx.canvas.AbstractAttachment;

/**
 * <p>
 * GeometryCanvas provides the ability to visualize and manipulate 3D geometry
 * data.
 * </p>
 * 
 * @author Tony McCrary (tmccrary@l33tlabs.com), Robert Smith
 * 
 */
public class FXMeshCanvas extends FXVizCanvas implements IMeshVizCanvas {

	/**
	 * <p>
	 * Creates a canvas for the supplied geometry.
	 * </p>
	 * 
	 * @param geometry
	 *            ICE Geometry instance to visualizer in the canvas.
	 */
	public FXMeshCanvas(AbstractController mesh) {
		super(mesh);

	}

	/**
	 * <p>
	 * Fix for Eclipse/PDE's wonky fragment support. Creates a GeometryViewer
	 * supplied by an implementation fragment.
	 * </p>
	 * 
	 * @param viewerParent
	 *            the parent widget
	 * 
	 * @return a concrete implementation of GeometryViewer
	 * 
	 * @throws Exception
	 *             throws an exception if a concrete implementation cannot be
	 *             found
	 */
	@Override
	protected AbstractViewer materializeViewer(Composite viewerParent)
			throws Exception {
		try {
			return new FXMeshViewer(viewerParent);

		} catch (Exception e) {
			throw new Exception("", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.javafx.internal.FXVizCanvas#createAttachment(
	 * )
	 */
	@Override
	protected void createAttachment() {
		rootAtachment = (AbstractAttachment) viewer.getRenderer()
				.createAttachment(FXMeshAttachment.class);
	}

	// /**
	// * <p>
	// * Listens for updates coming in from the geometry provider.
	// * </p>
	// *
	// * @see IVizUpdateable#update
	// */
	// @Override
	// public void update(final IVizUpdateable component) {
	//
	// // Invoke this on the JavaFX UI thread
	// javafx.application.Platform.runLater(new Runnable() {
	// @Override
	// public void run() {
	// if (component == mesh) {
	// // rootGeometry.addGeometry(geometry);
	// }
	// }
	// });
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * setEditMode(boolean)
	 */
	@Override
	public void setEditMode(boolean edit) {
		((FXMeshViewer) viewer).setEditSelectionHandeling(edit);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * setVisibleHUD(boolean)
	 */
	@Override
	public void setVisibleHUD(boolean on) {
		((FXMeshViewer) viewer).setHUDVisible(on);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * setVisibleAxis(boolean)
	 */
	@Override
	public void setVisibleAxis(boolean on) {
		((FXMeshViewer) viewer).setAxisVisible(on);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * HUDIsVisible()
	 */
	@Override
	public boolean HUDIsVisible() {
		return ((FXMeshViewer) viewer).isHUDVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * AxisAreVisible()
	 */
	@Override
	public boolean AxisAreVisible() {
		return ((FXMeshViewer) viewer).getAxisVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * deleteSelection()
	 */
	@Override
	public void deleteSelection() {

		// Check each polygon in the mesh to see if it should be deleted
		for (AbstractController polygon : root.getEntities()) {

			// Whether or not the polygon is completely selected
			boolean selected = true;

			// Check each of the polygon's vertices
			for (AbstractController vertex : polygon
					.getEntitiesByCategory("Vertices")) {

				// If any vertex is not selected, stop and move on to the next
				// polygon
				if (!"True".equals(vertex.getProperty("Selected"))) {
					selected = false;
					break;
				}
			}

			// If all the vertices were selected, remove the polygon form the
			// mesh
			if (selected) {
				root.removeEntity(polygon);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.mesh.datastructures.IMeshVizCanvas#
	 * setSelection(java.lang.Object[])
	 */
	@Override
	public void setSelection(Object[] selection) {

		// Set the viewer's internal data structures
		((FXMeshViewer) viewer).setInternalSelection(selection);

		// Start by deselecting everything
		for (AbstractController polygon : root.getEntities()) {
			polygon.setProperty("Selected", "False");
		}

		for (AbstractController polygon : root.getEntities()) {

			// If a polygon is in the selection, set it as selected. (This will
			// also select all its children).
			for (Object meshSelection : selection) {
				if (((MeshSelection) meshSelection).selectedMeshPart == polygon) {
					polygon.setProperty("Selected", "True");
				}

				// If the polygon wasn't selected, check each of its children to
				// see if they were.
				else {
					for (AbstractController part : polygon.getEntities()) {
						if (((MeshSelection) meshSelection).selectedMeshPart == part) {
							part.setProperty("Selected", "True");
						}
					}
				}
			}
		}

	}

}