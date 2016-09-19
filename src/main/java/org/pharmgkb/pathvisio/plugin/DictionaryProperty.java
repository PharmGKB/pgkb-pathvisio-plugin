/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import javax.annotation.Nonnull;
import com.google.common.base.Preconditions;
import org.pathvisio.core.model.PropertyType;
import org.pharmgkb.pathvisio.ExtendedProperty;
import org.pharmgkb.pathvisio.ReadOnlyPropertyType;


/**
 * This defines an dictionary Property, which allows multiple selection on a specific vocabulary.
 * This should be paired with the {@link DictionaryHandler}.
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
  private PropertyType m_type;


  public DictionaryProperty(String id, String name, String desc, int order, String defaultValue, boolean isCollection,
      @Nonnull PropertyType type) {
    Preconditions.checkNotNull(type);
    Preconditions.checkArgument(type instanceof DictionaryPropertyType ||
        (type instanceof ReadOnlyPropertyType && ((ReadOnlyPropertyType)type).getBaseType() instanceof DictionaryPropertyType));
    m_id = id;
    m_name = name;
    m_description = desc;
    m_order = order;
    m_defaultValue = defaultValue;
    m_isCollection = isCollection;
    m_type = type;
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

  public PropertyType getType() {
    return m_type;
  }

  public DictionaryPropertyType getDictionaryType() {
    if (m_type instanceof DictionaryPropertyType) {
      return (DictionaryPropertyType)m_type;
    } else {
      return (DictionaryPropertyType)((ReadOnlyPropertyType)m_type).getBaseType();
    }
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


  @Override
  public String toString() {
    return "DictionaryProperty:" + m_id;
  }
}
