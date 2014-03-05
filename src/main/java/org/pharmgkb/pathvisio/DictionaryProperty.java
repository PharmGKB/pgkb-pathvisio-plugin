/*
 ----- BEGIN LICENSE BLOCK -----
 Version: MPL 1.1/GPL 2.0/LGPL 2.1

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with the
 License. You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is PharmGKB.

 The Initial Developer of the Original Code is PharmGKB (The Pharmacogenetics
 and Pharmacogenetics Knowledge Base, supported by NIH U01GM61374). Portions
 created by the Initial Developer are Copyright (C) 2014 the Initial Developer.
 All Rights Reserved.

 Contributor(s):

 Alternatively, the contents of this file may be used under the terms of
 either the GNU General Public License Version 2 or later (the "GPL"), or the
 GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in
 which case the provisions of the GPL or the LGPL are applicable instead of
 those above. If you wish to allow use of your version of this file only
 under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your
 decision by deleting the provisions above and replace them with the notice
 and other provisions required by the GPL or the LGPL. If you do not delete
 the provisions above, a recipient may use your version of this file under
 the terms of any one of the MPL, the GPL or the LGPL.

 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio;

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
