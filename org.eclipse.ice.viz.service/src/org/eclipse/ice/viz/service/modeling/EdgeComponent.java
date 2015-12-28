/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.modeling;

import java.util.HashMap;
import java.util.List;

import org.eclipse.ice.viz.service.datastructures.VizObject.IManagedVizUpdateableListener;
import org.eclipse.ice.viz.service.datastructures.VizObject.UpdateableSubscriptionType;

/**
 * A mesh component representing a line between two Vertices.
 * 
 * @author Robert Smith
 */
public class EdgeComponent extends AbstractMeshComponent {

	/**
	 * The edge's length.
	 */
	public double length;

	/**
	 * The default constructor.
	 */
	public EdgeComponent() {
		super();
	}

	/**
	 * The default constructor. It creates an edge between the two specified
	 * vertices.
	 * 
	 * @param start
	 * @param end
	 */
	public EdgeComponent(Vertex start, Vertex end) {
		super();

		// Add the vertices to the list of entities.
		addEntity(start);
		addEntity(end);
	}

	/**
	 * Gets the edge's length
	 * 
	 * @return The edge's length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Adds a new entity to the edge. Caps the edge at a maximum of two
	 * vertices, and recalculates the edge's length when a second vertex is
	 * added.
	 * 
	 * @param newEntity
	 *            The vertex to add to the edge.
	 */
	@Override
	public void addEntity(AbstractController newEntity) {

		// When a vertex is added, take action to ensure the edge maintains its
		// proper state
		if (newEntity instanceof Vertex) {

			// The number of vertices
			List<AbstractController> vertices = entities.get("Vertices");
			int verticesNum = (vertices != null ? vertices.size() : 0);

			// If the object is a vertex and the edge already has both vertices,
			// fail silently.
			if (verticesNum >= 2) {
				return;
			}

			// Add the entity
			super.addEntityByCategory(newEntity, "Vertices");

			// If this was the second vertex, calculate the edge's new length.
			if (verticesNum == 1) {
				length = calculateLength();
			}

			// For other entities, add them normally
		} else {
			super.addEntity(newEntity);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.modeling.AbstractMeshComponent#
	 * addEntityByCategory(org.eclipse.ice.viz.service.modeling.
	 * AbstractController, java.lang.String)
	 */
	@Override
	public void addEntityByCategory(AbstractController entity,
			String category) {

		// When a vertex is added, take action to ensure the edge maintains a
		// proper state
		if ("Vertices".equals(category)) {

			// The number of vertices
			List<AbstractController> vertices = entities.get("Vertices");
			int verticesNum = (vertices != null ? vertices.size() : 0);

			// If the object is a vertex and the edge already has both vertices,
			// fail silently.
			if (verticesNum >= 2) {
				return;
			}

			// Add the entity
			super.addEntityByCategory(entity, category);

			// If this was the second vertex, calculate the edge's new length.
			if (verticesNum == 1) {
				length = calculateLength();
			}
		}

		// Otherwise, add the entity normally
		else {
			super.addEntityByCategory(entity, category);
		}
	}

	/**
	 * Calculates the length of the edge. This method does nothing by default,
	 * and is intended to be overwritten by subclasses.
	 * 
	 * @return The edge's current length
	 */
	protected double calculateLength() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.modeling.AbstractMeshComponent#register(org.
	 * eclipse.ice.viz.service.datastructures.VizObject.IVizUpdateableListener)
	 */
	@Override
	public void register(IManagedVizUpdateableListener listener) {

		// Do not register the edge's vertices, as the edge will listen to them
		// instead
		List<AbstractController> vertices = entities.get("Vertices");
		if (vertices == null || !entities.get("Vertices").contains(listener)) {
			super.register(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.modeling.AbstractMeshComponent#setController(
	 * org.eclipse.ice.viz.service.modeling.AbstractController)
	 */
	@Override
	public void setController(AbstractController controller) {
		super.setController(controller);

		// Give a reference to the controller to the edge's vertices
		List<AbstractController> vertices = entities.get("Vertices");

		if (vertices != null) {
			for (AbstractController vertex : vertices) {
				vertex.addEntity(controller);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.modeling.AbstractMeshComponent#clone()
	 */
	@Override
	public Object clone() {

		// Create a new object
		EdgeComponent clone = new EdgeComponent();

		// Make it a copy of this and return it
		clone.copy(this);
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.modeling.AbstractMeshComponent#copy(org.
	 * eclipse.ice.viz.service.modeling.AbstractMeshComponent)
	 */
	@Override
	public void copy(AbstractMeshComponent otherObject) {

		// Queue messages from all the vertices being added
		updateManager.enqueue();

		// Add a copy of the vertices from the other edge
		for (AbstractController vertex : otherObject
				.getEntitiesByCategory("Vertices")) {
			addEntity((Vertex) vertex.clone());
		}

		// Copy the rest of the data members and fire an update
		// Copy each of the other component's data members
		type = otherObject.type;
		properties = new HashMap<String, String>(otherObject.properties);

		// Notify listeners of the change
		UpdateableSubscriptionType[] eventTypes = {
				UpdateableSubscriptionType.All };
		updateManager.notifyListeners(eventTypes);

		// Release all queued messages
		updateManager.flushQueue();
	}

	/**
	 * Gets the location for the edge's first vertex.
	 *
	 * @return A list of the vertex's 3D coordinates
	 */
	public double[] getStartLocation() {
		List<AbstractController> vertices = getEntitiesByCategory("Vertices");
		return (vertices != null && !vertices.isEmpty())
				? ((Vertex) vertices.get(0)).getLocation() : new double[3];
	}

	/**
	 * Gets the location for the edge's second vertex
	 * 
	 * @return A list of the vertex's 3D coordinates
	 */
	public double[] getEndLocation() {
		List<AbstractController> vertices = getEntitiesByCategory("Vertices");
		return (vertices != null && vertices.size() > 1)
				? ((Vertex) vertices.get(1)).getLocation() : new double[3];
	}
}
