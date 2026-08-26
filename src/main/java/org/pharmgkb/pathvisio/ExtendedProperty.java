package org.pharmgkb.pathvisio;

import org.pathvisio.core.model.Property;

/**
 * Adds additional properties to {@link Property}.
 *
 * @author Mark Woon
 */
public interface ExtendedProperty extends Property {

	int getOrder();


	String getDefaultValue();
}
