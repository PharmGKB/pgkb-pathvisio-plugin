package org.pharmgkb.pathvisio.plugin;

import javax.annotation.Nonnull;
import com.google.common.base.Preconditions;
import org.pathvisio.core.model.PropertyType;
import org.pharmgkb.pathvisio.ExtendedProperty;


/**
 * This represents an dependent property that can have a custom default value.
 * This should <b>ONLY</b> be used with {@link ObjectPropertyManager} and not PathVisio's
 * {@link org.pathvisio.core.model.PropertyManager}.
 *
 * @author Mark Woon
 */
public class DependentProperty implements ExtendedProperty {
  private ExtendedProperty m_baseProperty;
  private String m_defaultValue;


  public DependentProperty(@Nonnull ExtendedProperty baseProperty) {
    Preconditions.checkNotNull(baseProperty);
    m_baseProperty = baseProperty;
    m_defaultValue = baseProperty.getDefaultValue();
  }

  public @Nonnull ExtendedProperty getBaseProperty() {
    return m_baseProperty;
  }


  @Override
  public int getOrder() {
    return m_baseProperty.getOrder();
  }

  @Override
  public String getDefaultValue() {
    return m_defaultValue;
  }

  public void setDefaultValue(@Nonnull String defaultValue) {
    Preconditions.checkNotNull(defaultValue);
    m_defaultValue = defaultValue;
  }


  @Override
  public String getId() {
    return m_baseProperty.getId();
  }

  @Override
  public String getName() {
    return m_baseProperty.getName();
  }

  @Override
  public String getDescription() {
    return m_baseProperty.getDescription();
  }

  @Override
  public PropertyType getType() {
    return m_baseProperty.getType();
  }

  @Override
  public boolean isCollection() {
    return m_baseProperty.isCollection();
  }
}
