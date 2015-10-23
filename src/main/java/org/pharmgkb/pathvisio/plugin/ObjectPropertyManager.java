/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.model.PathwayEvent;
import org.pathvisio.core.model.PathwayListener;
import org.pathvisio.core.model.Property;
import org.pathvisio.core.model.PropertyManager;
import org.pathvisio.core.util.Utils;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.gui.handler.PathwayTableModel;
import org.pharmgkb.pathvisio.BiopaxInteractionType;
import org.pharmgkb.pathvisio.DataConstants;
import org.pharmgkb.pathvisio.EnumProperty;
import org.pharmgkb.pathvisio.ExtendedProperty;
import org.pharmgkb.pathvisio.PgkbType;


/**
 * This class handles the association between dynamic properties and ObjectTypes.
 * It also supports dependent properties.
 *
 * @author Mark Woon
 */
public class ObjectPropertyManager implements Engine.ApplicationEventListener, PathwayListener, PathwayElementListener {
	private PvDesktop m_desktop;
	private PathwayTableModel m_tableModel;
	private Map<ObjectType, ObjectProperties> m_objectProperties = new HashMap<>();
	private Set<ObjectPropertyListener> m_listeners = new HashSet<>();


	public ObjectPropertyManager(PvDesktop desktop) {
		m_desktop = desktop;
		m_tableModel = desktop.getSwingEngine().getApplicationPanel().getModel();
		m_desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
	}


	public void addListener(ObjectPropertyListener listener) {
		m_listeners.add(listener);
	}

	private void fireEvent(int type, PathwayElement elem) {

		ObjectPropertyEvent event = new ObjectPropertyEvent(type, elem);
		for (ObjectPropertyListener listener : m_listeners) {
			listener.objectModified(event);
		}
	}


	/**
	 * Associates an property with an object type.
	 */
	public void registerProperty(ObjectType type, Property prop) {

		ObjectProperties objProps = m_objectProperties.get(type);
		if (objProps == null) {
			objProps = new ObjectProperties();
			m_objectProperties.put(type, objProps);
		}
		objProps.addProperty(prop);
	}

	/**
	 * Associates a dependent property with an object type.
	 */
	public void registerDependentProperty(@Nonnull ObjectType pvType, @Nonnull Property propControl,
      @Nonnull String condition, @Nonnull Property dependentProp) {

		ObjectProperties objProps = m_objectProperties.get(pvType);
		if (objProps == null) {
			objProps = new ObjectProperties();
			m_objectProperties.put(pvType, objProps);
		}
		objProps.addDependentProperty(propControl, condition, dependentProp);
	}


	/**
	 * Initializes the properties of a {@link PathwayElement} based on ObjectProperties when it is first added to the
	 * pathway.
	 */
	private void initializeProperties(PathwayElement elem) {

		ObjectProperties objProps = m_objectProperties.get(elem.getObjectType());
		// only update
		if (objProps != null) {
			if (!objProps.getDependentPropertyControls().isEmpty()) {
				// listen to handle dependent properties
				elem.addListener(this);
				Property typeProp = PgkbType.getProperty();
				// type property is often already set, in which case we should check if dependent props exist for it
				if (elem.getDynamicProperty(typeProp.getId()) != null &&
						objProps.getDependentPropertyControls().contains(typeProp)) {
					updateDependentProperty(elem, typeProp, objProps);
				}
			} else if (elem.getObjectType() == ObjectType.LINE) {
				elem.addListener(this);
			}
			if (objProps.getProperties() != null && !objProps.getProperties().isEmpty()) {
				for (Property p : objProps.getProperties()) {
					if (Utils.isEmpty(elem.getDynamicProperty(p.getId()))) {
						// add property to PathwayElement
						String value = getDefaultValue(p);
						elem.setDynamicProperty(p.getId(), value);
					}
				}
			}
		}
	}

	/**
	 * Gets the default value of the property.
	 */
	private String getDefaultValue(Property prop) {

		String defaultValue = "";
		if (prop instanceof ExtendedProperty && ((ExtendedProperty)prop).getDefaultValue() != null) {
			defaultValue = ((ExtendedProperty)prop).getDefaultValue();
		}
		return defaultValue;
	}


	//-- ApplicationEventListener methods --//

	public void applicationEvent(ApplicationEvent e) {

		if (e.getType() == ApplicationEvent.Type.PATHWAY_NEW || e.getType() == ApplicationEvent.Type.PATHWAY_OPENED) {
			Pathway p = m_desktop.getSwingEngine().getEngine().getActivePathway();
			p.addListener(this);
			// mappInfo is the property holder for the pathway
			initializeProperties(p.getMappInfo());
			if (e.getType() == ApplicationEvent.Type.PATHWAY_OPENED) {
				for (PathwayElement elem : p.getDataObjects()) {
					initializeProperties(elem);
				}
				fireEvent(ObjectPropertyEvent.PATHWAY_OPENED, p.getMappInfo());
			} else {
				fireEvent(ObjectPropertyEvent.PATHWAY_NEW, p.getMappInfo());
			}
		}
	}


	//-- PathwayListener methods --//

	public void pathwayModified(PathwayEvent e) {

		if (e.getType() == PathwayEvent.ADDED) {
			PathwayElement elem = e.getAffectedData();
			if (elem.getGraphId() == null) {
				elem.setGeneratedGraphId();
			}
			initializeProperties(elem);
			fireEvent(ObjectPropertyEvent.ELEMENT_ADDED, elem);
		}
	}

	//-- PathwayElementListener methods --//

	public void gmmlObjectModified(PathwayElementEvent e) {

		if (!e.isCoordinateChange()) {
			// check if dependent property has changed
			PathwayElement elem = e.getModifiedPathwayElement();
			ObjectProperties objProps = m_objectProperties.get(elem.getObjectType());
			boolean doRefresh = false;
			for (Property p : objProps.getDependentPropertyControls()) {
				if (e.affectsProperty(p.getId())) {
					doRefresh = true;
					updateDependentProperty(elem, p, objProps);
				}
			}
			if (elem.getObjectType() == ObjectType.LINE) {
				if (e.affectsProperty(DataConstants.PGKB_LINE_STRENGTH)) {
					EnumProperty prop = (EnumProperty)PropertyManager.getProperty(DataConstants.PGKB_LINE_STRENGTH);
					elem.setLineThickness(prop.getDataDouble(elem.getDynamicProperty(prop.getId())));
				} else if (e.affectsProperty(DataConstants.PGKB_IS_REVERSIBLE)) {
					if (Boolean.parseBoolean(elem.getDynamicProperty(DataConstants.PGKB_IS_REVERSIBLE))) {
						elem.setStartLineType(LineType.ARROW);
					} else {
						elem.setStartLineType(LineType.LINE);
					}
				} else if (e.affectsProperty(BiopaxInteractionType.getProperty().getId())) {
					String value = elem.getDynamicProperty(BiopaxInteractionType.getProperty().getId());
					if (!Utils.isEmpty(value)) {
						BiopaxInteractionType interactionType = BiopaxInteractionType.lookupByName(value);
						elem.setLineStyle(interactionType.getLineStyle());
						elem.setStartLineType(interactionType.getStartLineType());
						elem.setEndLineType(interactionType.getEndLineType());
						elem.setColor(interactionType.getColor());
					}
				}
			}
			if (doRefresh) {
				m_tableModel.refresh(true);
			}
			fireEvent(ObjectPropertyEvent.ELEMENT_MODIFIED, elem);
		}
	}


	private boolean updateDependentProperty(PathwayElement elem, Property controlProperty, ObjectProperties objProps) {

		String value = StringUtils.stripToEmpty(elem.getDynamicProperty(controlProperty.getId()));
    boolean hasDependents = false;

    // unset irrelevant properties
    m_tableModel.updatePropertyCounts(elem, true);
    for (Property ip : objProps.getIrrelevantProperties(controlProperty, value)) {
      elem.setDynamicProperty(ip.getId(), null);
      hasDependents = true;
    }
    // add dependent properties
    List<Property> props = objProps.getDependentProperties(controlProperty, value);
    if (props != null) {
      props.stream()
          .filter(dp -> elem.getDynamicProperty(dp.getId()) == null)
          .forEach(dp -> elem.setDynamicProperty(dp.getId(), getDefaultValue(dp)));
      hasDependents = true;
    }
    m_tableModel.updatePropertyCounts(elem, false);

    return hasDependents;
	}


	/**
	 * This class tracks what properties are associated with an object type.
	 */
	public static class ObjectProperties {
		private List<Property> m_properties = new ArrayList<>();
		private Map<Property, Map<String, List<Property>>> m_dependentPropertiesMap =
				new HashMap<>();
		private Map<Property, Map<String, Set<Property>>> m_dependentPropertiesReverseMap =
				new HashMap<>();
		private boolean m_isDependentPropsDirty = true;


		/**
		 * Gets all top-level properties associated with this object.  There may be other properties associated with
		 * this object that depend on the value of one of these top-level typed properties.
		 */
		public List<Property> getProperties() {
			return m_properties;
		}

		public boolean containsProperty(Property prop) {
			return m_properties.contains(prop);
		}

		/**
		 * Associates a Property to this object.
		 */
		public void addProperty(Property prop) {
			m_properties.add(prop);
		}


		/**
		 * Gets all the control properties that have dependent properties for this object.
		 */
		public Set<Property> getDependentPropertyControls() {
			return m_dependentPropertiesMap.keySet();
		}


		/**
		 * Gets all dependent properties based a control property's value.
		 *
		 * @return depependent properties or null if none exists
		 */
		public @Nullable List<Property> getDependentProperties(@Nonnull Property propControl, @Nonnull String value) {

			Map<String, List<Property>> dependentProps = m_dependentPropertiesMap.get(propControl);
			if (dependentProps != null) {
				return dependentProps.get(value);
			}
			return null;
		}

		/**
		 * Gets all properties that should no longer be associated with the object based on a control property's value.
		 */
		public @Nonnull Set<Property> getIrrelevantProperties(@Nonnull Property propControl, @Nonnull String value) {

			if (m_isDependentPropsDirty) {
				m_dependentPropertiesReverseMap.clear();
				m_isDependentPropsDirty = false;
			}
			Map<String, Set<Property>> reverseProps = m_dependentPropertiesReverseMap.get(propControl);
			if (reverseProps == null) {
				reverseProps = new HashMap<>();
				m_dependentPropertiesReverseMap.put(propControl, reverseProps);
			}
			Set<Property> irrelevantProps = reverseProps.get(value);
			if (irrelevantProps == null) {
				irrelevantProps = new HashSet<>();
				reverseProps.put(value, irrelevantProps);

				Map<String, List<Property>> dependentProps = m_dependentPropertiesMap.get(propControl);
				for (String key : dependentProps.keySet()) {
					if (!key.equals(value)) {
						irrelevantProps.addAll(dependentProps.get(key));
					}
				}
				List<Property> relevantProps = dependentProps.get(value);
				if (relevantProps != null) {
					irrelevantProps.removeAll(relevantProps);
				}
			}
			return irrelevantProps;
		}


		/**
		 * Adds a dependent Property.
		 *
		 * @param propControl   the control property
		 * @param condition	 the value of the property under which the sub-property is visible
		 * @param dependentProp the dependent Property
		 */
		public void addDependentProperty(Property propControl, String condition, Property dependentProp) {

			m_isDependentPropsDirty = true;
			Map<String, List<Property>> dependentProps = m_dependentPropertiesMap.get(propControl);
			if (dependentProps == null) {
				dependentProps = new HashMap<>();
				m_dependentPropertiesMap.put(propControl, dependentProps);
			}
			List<Property> props = dependentProps.get(condition);
			if (props == null) {
				props = new ArrayList<>();
				dependentProps.put(condition, props);
			}
			props.add(dependentProp);
		}
	}
}