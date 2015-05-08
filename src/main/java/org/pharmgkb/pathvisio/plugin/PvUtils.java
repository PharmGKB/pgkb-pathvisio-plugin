/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import javax.swing.JFrame;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PropertyManager;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticPropertyType;
import org.pathvisio.core.util.Utils;
import org.pathvisio.gui.handler.ComboHandler;
import org.pathvisio.gui.handler.PropertyDisplayManager;
import org.pathvisio.gui.handler.TypeHandler;
import org.pharmgkb.pathvisio.EnumProperty;
import org.pharmgkb.pathvisio.ExtendedProperty;
import org.pharmgkb.pathvisio.PgkbType;
import org.pharmgkb.pathvisio.ReadOnlyPropertyType;


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


  public static void registerProperty(ExtendedProperty prop, JFrame frame) throws PgkbPluginException {

		// register type if it's not already registered
		PropertyType type = PropertyManager.getPropertyType(prop.getType().getId());
		if (type != null) {
			if (type != prop.getType()) {
				throw new PgkbPluginException("Property type ID '" + prop.getType().getId() + "'  is already taken");
			}
		} else {
			type = prop.getType();
			PropertyManager.registerPropertyType(type);
		}

		PropertyManager.registerProperty(prop);
		PropertyDisplayManager.registerProperty(prop, false);
		PropertyDisplayManager.setPropertyOrder(prop, prop.getOrder());
		TypeHandler handler = null;
		if (prop instanceof EnumProperty) {
			handler = new ComboHandler(type, ((EnumProperty)prop).getValues(), false);
			if (type instanceof ReadOnlyPropertyType) {
				handler = new ReadOnlyTypeHandler(type, handler);
			}

		} else if (prop instanceof DictionaryProperty) {
			if (PropertyDisplayManager.getTypeHandler(type) == null) {
				handler = new DictionaryHandler(frame, (DictionaryProperty)prop);
				if (type instanceof ReadOnlyPropertyType) {
					handler = new ReadOnlyTypeHandler(type, handler);
				}
			}

		} else if (type instanceof ReadOnlyPropertyType) {
			PropertyType baseType = ((ReadOnlyPropertyType)type).getBaseType();
			handler = PropertyDisplayManager.getTypeHandler(baseType);
			if (handler == null) {
				if (baseType == StaticPropertyType.STRING) {
					handler = new StringTypeHandler();
				} else {
					throw new PgkbPluginException("No handler defined for read-only property type " + baseType.getId());
				}
			}
			handler = new ReadOnlyTypeHandler(type, handler);

		} else if (!(type instanceof StaticPropertyType) &&
				PropertyDisplayManager.getTypeHandler(type) == null) {
			throw new PgkbPluginException("No handler defined for property " + prop.getId());
		}

		if (handler != null) {
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