/*
 ----- BEGIN LICENSE BLOCK -----
 Version: MPL 1.1/GPL 2.0/LGPL 2.1

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with the
 License. You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is PharmGKB.

 The Initial Developer of the Original Code is PharmGKB (The Pharmacogenetics
 and Pharmacogenetics Knowledge Base, supported by NIH U01GM61374). Portions
 created by the Initial Developer are Copyright (C) 2014 the Initial Developer.
 All Rights Reserved.

 Contributor(s):

 Alternatively, the contents of this file may be used under the terms of
 either the GNU General Public License Version 2 or later (the "GPL"), or the
 GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in
 which case the provisions of the GPL or the LGPL are applicable instead of
 those above. If you wish to allow use of your version of this file only
 under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your
 decision by deleting the provisions above and replace them with the notice
 and other provisions required by the GPL or the LGPL. If you do not delete
 the provisions above, a recipient may use your version of this file under
 the terms of any one of the MPL, the GPL or the LGPL.

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