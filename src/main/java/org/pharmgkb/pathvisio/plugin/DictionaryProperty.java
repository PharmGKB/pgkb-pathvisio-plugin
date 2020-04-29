package org.pharmgkb.pathvisio.plugin;

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
  private final String m_id;
  private final String m_name;
  private final String m_description;
  private final int m_order;
  private final String m_defaultValue;
  private final boolean m_isCollection;
  private final PropertyType m_type;


  public DictionaryProperty(String id, String name, String desc, int order, String defaultValue, boolean isCollection,
      PropertyType type) {
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
