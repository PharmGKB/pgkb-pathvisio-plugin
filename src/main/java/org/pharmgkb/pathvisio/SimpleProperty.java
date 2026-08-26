package org.pharmgkb.pathvisio;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.PropertyType;

/**
 * This is a simple implementation of a Property that can also be its own PropertyType.
 *
 * @author Mark Woon
 */
public class SimpleProperty implements ExtendedProperty {
	private final String m_id;
	private final String m_name;
	private final String m_description;
	private final PropertyType m_type;
	private final boolean m_isCollection;
	private final int m_order;
	private String m_defaultValue = "";


	/**
	 * Constructor for a Property with an external PropertyType.
	 *
	 * @param isCollection true if multiple values can be selected, otherwise only one value can be selected
	 */
	public SimpleProperty(String id, String name, String desc, int order, @Nullable String defaultValue,
			PropertyType type, boolean isCollection) {
		Preconditions.checkNotNull(type);
		m_id = id;
		m_name = name;
		m_description = desc;
		m_order = order;
		m_type = type;
		m_isCollection = isCollection;
		if (defaultValue != null) {
			m_defaultValue = defaultValue;
		}
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
		return "SimpleProperty:" + m_id;
	}
}
