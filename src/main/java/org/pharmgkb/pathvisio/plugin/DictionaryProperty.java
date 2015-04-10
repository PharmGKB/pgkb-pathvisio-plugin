/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import org.pharmgkb.pathvisio.ExtendedProperty;


/**
 * This defines an dictionary Property, which allows multiple selection on a specific vocabulary.
 * This should be paired with the DictionaryHandler.
 *
 * @author Rebecca Tang
 */
public class DictionaryProperty implements ExtendedProperty {
	private String m_id;
	private String m_name;
	private String m_description;
	private int m_order;
	private String m_defaultValue;
	private boolean m_isCollection;
	private DictionaryPropertyType m_type;
	private boolean m_isEditable = true;


	public DictionaryProperty(String id, String name, String desc, int order, String defaultValue, boolean isCollection,
			DictionaryPropertyType type, boolean isEditable) {
		m_id = id;
		m_name = name;
		m_description = desc;
		m_order = order;
		m_defaultValue = defaultValue;
		m_isCollection = isCollection;
		m_type = type;
		m_isEditable = isEditable;
	}


	public String getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public String getDescription() {
		return m_description;
	}

	public DictionaryPropertyType getType() {
		return m_type;
	}

	public boolean isCollection() {
		return m_isCollection;
	}

	public int getOrder() {
		return m_order;
	}

	public String getDefaultValue() {
		return m_defaultValue;
	}

	public boolean isEditable() {
		return m_isEditable;
	}
}
