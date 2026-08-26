package org.pharmgkb.pathvisio;

import com.google.common.base.Preconditions;
import org.pathvisio.core.model.PropertyType;


/**
 * Read-only {@link PropertyType}.
 *
 * @author Mark Woon
 */
public class ReadOnlyPropertyType implements PropertyType {
  private final PropertyType m_baseType;


  public ReadOnlyPropertyType(PropertyType baseType) {
    Preconditions.checkNotNull(baseType);
    m_baseType = baseType;
  }


  @Override
  public String getId() {
    return m_baseType.getId() + ".readOnly";
  }

  public PropertyType getBaseType() {
    return m_baseType;
  }


  @Override
  public String toString() {
    return "READ-ONLY:" + m_baseType;
  }
}
