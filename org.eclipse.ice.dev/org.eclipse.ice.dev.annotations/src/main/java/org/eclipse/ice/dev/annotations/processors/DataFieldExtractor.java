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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.eclipse.ice.dev.annotations.DataField;

/**
 * Extractor for DataField annotated elements.
 * @author Daniel Bluhm
 */
public class DataFieldExtractor extends AbstractAnnotationExtractor<Field> {

	public DataFieldExtractor(Elements elementUtils) {
		super(elementUtils);
	}

	/**
	 * Set of Annotation names that extractAnnotations should filter out.
	 */
	private static final Set<String> ANNOTATION_CLASS_NAMES = Set.of(
		DataField.class,
		DataField.Default.class
	).stream()
		.map(Class::getCanonicalName)
		.collect(Collectors.toSet());

	@Override
	public Field extract(Element element) throws InvalidElementException {
		DataField fieldInfo = element.getAnnotation(DataField.class);
		if (fieldInfo == null) {
			throw new InvalidElementException(
				"DataField annotation not found on element."
			);
		}
		Field field = Field.builder()
			.name(extractFieldName(element))
			.type(extractFieldType(element))
			.defaultValue(extractDefaultValue(element))
			.docString(extractDocComment(element))
			.annotations(extractAnnotations(element))
			.modifiersToString(extractModifiers(element))
			.getter(fieldInfo.getter())
			.setter(fieldInfo.setter())
			.match(fieldInfo.match())
			.unique(fieldInfo.unique())
			.searchable(fieldInfo.searchable())
			.nullable(fieldInfo.nullable())
			.build();
		return field;
	}

	/**
	 * Return the set of access modifiers on this Field.
	 * @param element from which modifiers are extracted.
	 * @return extract field modifiers
	 * @see Modifier
	 */
	private Set<Modifier> extractModifiers(Element element) {
		return element.getModifiers();
	}

	/**
	 * Return the set of annotations on this DataField, excepting the DataField
	 * Annotation itself.
	 * @param element from which annotations are extracted.
	 * @return extracted annotations, excluding DataField related annotations
	 */
	private List<String> extractAnnotations(Element element) {
		return element.getAnnotationMirrors().stream()
			.filter(mirror -> !ANNOTATION_CLASS_NAMES.contains(
				mirror.getAnnotationType().toString()
			))
			.map(AnnotationMirror::toString)
			.collect(Collectors.toList());
	}

	/**
	 * Return the class of this Field.
	 * @param element from which type is extracted.
	 * @return extracted field type
	 */
	private TypeMirror extractFieldType(Element element) {
		return element.asType();
	}

	/**
	 * Return the name of this Field.
	 * @param element from which name is extracted.
	 * @return extracted field name
	 */
	private String extractFieldName(Element element) {
		return element.getSimpleName().toString();
	}

	/**
	 * Return the DocComment of this Field.
	 * @param element from which doc comment is extracted.
	 * @return extracted doc comment
	 */
	private String extractDocComment(Element element) {
		return this.elementUtils.getDocComment(element);
	}

	/**
	 * Extract the defaultValue of this Field. Checks for {@link DataField.Default}
	 * and if not present checks for a constant expression if the field is
	 * {@code final}.
	 * @param element from which the default value is extracted.
	 * @return extracted default value
	 */
	private String extractDefaultValue(Element element) {
		String retval = null;
		DataField.Default defaults = element.getAnnotation(DataField.Default.class);
		if (defaults != null) {
			if (defaults.isString()) {
				retval = this.elementUtils.getConstantExpression(defaults.value());
			} else {
				retval = defaults.value();
			}
		} else if (element.getModifiers().contains(Modifier.FINAL)) {
			retval = this.elementUtils.getConstantExpression(
				((VariableElement) element).getConstantValue()
			);
		}
		return retval;
	}
}
