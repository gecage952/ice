/*******************************************************************************
 * Copyright (c) 2020- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Bluhm - Initial implementation
 *******************************************************************************/

package org.eclipse.ice.dev.annotations.processors;

import java.util.Optional;

import javax.lang.model.element.Element;

/**
 * Interface for classes acting as extractors of annotation info.
 * @author Daniel Bluhm
 *
 * @param <T> the type of the information extracted by this annotation
 *        extractor.
 */
public interface AnnotationExtractor<T> {

	/**
	 * Extract information from element and annotations found on or within
	 * element. The subclass of element is dependent on the annotation and
	 * implementation of the extractor.
	 * @param element from which information will be extracted.
	 * @return extracted information
	 * @throws InvalidElementException if element is not annotated as expected
	 *         for this annotation extractor.
	 */
	public T extract(Element element) throws InvalidElementException;

	/**
	 * Extract information from element and annotations found on or within
	 * element if possible, return empty otherwise.
	 * @param element from which information will be extracted.
	 * @return extracted information wrapped in optional or empty.
	 */
	public Optional<T> extractIfApplies(Element element);
}