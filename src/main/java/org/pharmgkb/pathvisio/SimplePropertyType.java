package org.pharmgkb.pathvisio;

import com.google.common.base.Preconditions;
import org.pathvisio.core.model.PropertyType;


/**
 * Simple name-based {@link PropertyType}.
 *
 * @author Mark Woon
 */
public class SimplePropertyType implements PropertyType {
  private final String m_id;

  public SimplePropertyType(String id) {
    Preconditions.checkNotNull(id);
    m_id = id;
  }

  @Override
  public String getId() {
    return m_id;
  }


  @Override
  public String toString() {
    return m_id;
  }
}
