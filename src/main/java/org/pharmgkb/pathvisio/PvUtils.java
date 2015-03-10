/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio;

import javax.swing.JFrame;
import org.pathvisio.gui.swing.propertypanel.ComboHandler;
import org.pathvisio.gui.swing.propertypanel.PropertyDisplayManager;
import org.pathvisio.gui.swing.propertypanel.TypeHandler;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PropertyManager;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.util.Utils;
import org.pharmgkb.exception.PgkbException;


/**
 * Utility class for working with PathVisio codebase.
 *
 * @author Mark Woon
 */
public class PvUtils {


	/**
	 * Private constructor.
	 */
	private PvUtils() {
	}


  public static void registerProperty(ExtendedProperty prop, JFrame frame) throws PgkbException {

		// register type if it's not already registered
		PropertyType type = PropertyManager.getPropertyType(prop.getType().getId());
		if (type != null) {
			if (type != prop.getType()) {
				throw new PgkbException("Property type ID '" + prop.getType().getId() + "'  is already taken");
			}
		} else {
			PropertyManager.registerPropertyType(prop.getType());
		}
		PropertyManager.registerProperty(prop);
		PropertyDisplayManager.registerProperty(prop, false);
		PropertyDisplayManager.setPropertyOrder(prop, prop.getOrder());
		TypeHandler handler = null;
		if (prop instanceof EnumProperty) {
			handler = new ComboHandler(prop.getType(), ((EnumProperty)prop).getValues(), false);
		} else if (prop instanceof DictionaryProperty) {
			if (PropertyDisplayManager.getTypeHandler(prop.getType()) == null) {
				handler = new DictionaryHandler(frame, (DictionaryProperty)prop);
			}
		} else if (!(prop.getType() instanceof StaticPropertyType) &&
				PropertyDisplayManager.getTypeHandler(prop.getType()) == null) {
			throw new PgkbException("No handler defined for property " + prop.getId());
		}
		if (handler != null) {
			if (!prop.isEditable()) {
				handler = new ReadOnlyTypeHandler(handler);
			}
			PropertyDisplayManager.registerTypeHandler(handler);
		}
	}


	/**
	 * Customizes a PathwayElement based on PgkbType.
	 */
	public static void customizePathwayElement(PathwayElement elem, PgkbType type) {

		if (elem.getObjectType() == ObjectType.DATANODE) {
			if (Utils.isEmpty(elem.getDynamicProperty("pgkb.type"))) {
				elem.setDynamicProperty("pgkb.type", type.getDisplayName());
			}
			elem.setDataNodeType(type.getDataNodeType());
			if (type.getColor() != null) {
				elem.setColor(type.getColor());
			}
			elem.setFillColor(type.getFillColor());
			elem.setShapeType(type.getShapeType());
			elem.setLineStyle(type.getLineStyle());
		}
	}
}