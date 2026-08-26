package org.pharmgkb.pathvisio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.PropertyType;


/**
 * This defines an enumerated Property.
 * This should be paired with the ComboHandler.  When paired with the ComboHandler, it will not support the selection of
 * multiple values.
 *
 * @author Rebecca Tang
 */
public class EnumProperty extends SimpleProperty {
	private List<String> m_values;
	private Map<String, String> m_data;

	public EnumProperty(String id, String name, String desc, int order, @Nullable String defaultValue,
			PropertyType type, boolean isCollection) {
		super(id, name, desc, order, defaultValue, type, isCollection);
	}

	/**
	 * @return list of allowed values or null if none have been specified
	 */
	public List<String> getValues() {
		return m_values;
	}


	public @Nullable String getData(String value) {
		if (m_data == null) {
			return null;
		}
		return m_data.get(value);
	}

	public Double getDataDouble(String value) {
		return Double.parseDouble(getData(value));
	}


	public void addValue(String value) {
		if (value != null) {
			if (m_values == null) {
				m_values = new ArrayList<>();
			}
			m_values.add(value);
		}
	}


	public void addValue(@Nullable String value, @Nullable String data) {
		if (value != null) {
			addValue(value);
			if (data != null) {
				if (m_data == null) {
					m_data = new HashMap<>();
				}
				m_data.put(value, data);
			}
		}
	}

	@Override
	public String toString() {
		return "EnumProperty:" + getId();
	}
}
