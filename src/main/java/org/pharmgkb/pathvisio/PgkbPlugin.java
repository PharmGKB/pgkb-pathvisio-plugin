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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.jidesoft.icons.IconsFactory;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.CommonActions;
import org.pathvisio.gui.swing.MainPanel;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.propertypanel.PropertyDisplayManager;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Property;
import org.pathvisio.model.PropertyManager;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.ShapeType;
import org.pathvisio.model.StaticProperty;
import org.pathvisio.plugin.Plugin;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.util.Resources;
import org.pathvisio.util.Utils;
import org.pathvisio.view.DefaultTemplates;
import org.pharmgkb.exception.PgkbException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Entry point for the PharmGKB PathVisio plugin.
 *
 * @author Mark Woon
 * @author Rebecca Tang
 */
public class PgkbPlugin implements Plugin, ObjectPropertyListener, Engine.ApplicationEventListener {
	public static String TYPE_DICTIONARY = "DICTIONARY";
	public static String TYPE_ENUM = "ENUM";

	private PvDesktop m_desktop;
	private ObjectPropertyManager m_objectPropertiesManager;
	private Set<Action> m_actions = new HashSet<>();


	public void init(PvDesktop desktop) {

		try {
			System.out.println("Initializing PgkbPlugin");
/*			LookAndFeelFactory.setDefaultStyle(LookAndFeelFactory.ECLIPSE_STYLE);
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception ex) {
				throw new RuntimeException("Error setting look and feel", ex);
			}
			*/
			// customize UI behavior
			try {
				// need to set class manager (see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4155617)
				UIManager.put("ClassLoader", this.getClass().getClassLoader());
			    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				Logger.log.error("Unable to load native look and feel", ex);
			}
			// buttons respond to pressing "Enter"
			UIManager.put("Button.defaultButtonFollowFocus", Boolean.TRUE);

			m_desktop = desktop;
			m_desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
			m_objectPropertiesManager = new ObjectPropertyManager(desktop);
			m_objectPropertiesManager.addListener(this);

			initProperties();
			initActions();

			System.out.println("Done initializing PgkbPlugin");
		} catch (Exception ex) {
			Logger.log.error("Error initializing plugin", ex);
      showErrorMessage(ex);
			throw ex;
		}
	}

	public void done() {
		// should remove from objects it's listening to,
		// but not bothering because PV doesn't support disabling a plugin
	}


	private void addToToolbar(JToolBar toolbar, Action action, Border buttonBorder) {
		action.setEnabled(false);
		m_actions.add(action);
		JButton button = toolbar.add(action);
		button.setBorder(buttonBorder);
		button.setFocusable(false);
		disableToolbarItem(button);
	}

	private void addToToolbar(JToolBar toolbar, Component component) {
		toolbar.add(component);
		disableToolbarItem(component);
	}

	private void disableToolbarItem(Component component) {
		component.setEnabled(false);
		List<Component> gb = m_desktop.getSwingEngine().getApplicationPanel().getToolbarGroup(MainPanel.TB_GROUP_SHOW_IF_VPATHWAY);
		gb.add(component);
	}

	/**
	 * Initialize all actions.
	 */
	private void initActions() {

		System.out.println("Initializing actions");
		SwingEngine swingEngine = m_desktop.getSwingEngine();
		// hiding objects pane
		swingEngine.getApplicationPanel().getSideBarTabbedPane().remove(swingEngine.getApplicationPanel().getObjectsPane());
		// customizing tool bar
		JToolBar toolbar = swingEngine.getApplicationPanel().getToolBar();
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
		toolbar.removeAll();
		swingEngine.getApplicationPanel().getToolbarGroup(MainPanel.TB_GROUP_SHOW_IF_VPATHWAY).clear();
		Dimension spacerSize = new Dimension(4, 0);
		Dimension borderSpacerSize = new Dimension(8, 0);
		Border buttonBorder = new EmptyBorder(4, 4, 4, 4);

		// zoom
		toolbar.add(Box.createRigidArea(spacerSize));
		JLabel label = new JLabel("Zoom:", JLabel.LEFT);
		addToToolbar(toolbar, label);
		label.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		JComboBox<Action> zoomCombo = new JComboBox<>(swingEngine.getActions().zoomActions);
		zoomCombo.setMaximumSize(zoomCombo.getPreferredSize());
		zoomCombo.setEditable(true);
		zoomCombo.setSelectedIndex(5); // 100%
		zoomCombo.addActionListener(new ZoomComboListener());
		addToToolbar(toolbar,zoomCombo);

		toolbar.add(Box.createRigidArea(borderSpacerSize));
		toolbar.addSeparator();
		toolbar.add(Box.createRigidArea(borderSpacerSize));

		// interactions
		JComboBox<Action> interactionCombo = new JComboBox<>();
		String longestValue = "";
    Action longestAction = null;
		for (BiopaxInteractionType interactionType : BiopaxInteractionType.values()) {
			Action action = new CommonActions.NewElementAction(swingEngine.getEngine(), new LineTemplate(interactionType));
			interactionCombo.addItem(action);
      if (longestValue.length() < interactionType.getDisplayName().length()) {
        longestValue = interactionType.getDisplayName();
        longestAction = action;
      }
		}
		interactionCombo.setPrototypeDisplayValue(longestAction);
		interactionCombo.setMaximumSize(interactionCombo.getPreferredSize());
		interactionCombo.setMaximumRowCount(interactionCombo.getItemCount());
		addToToolbar(toolbar, interactionCombo);
		toolbar.add(Box.createRigidArea(spacerSize));
		JButton lineButton = new JButton(new ImageIcon(Resources.getResourceURL("newlineshapemenu.gif")));
		lineButton.setBorder(buttonBorder);
		lineButton.setToolTipText("Add interaction");
		lineButton.addActionListener(new ActionComboListener(interactionCombo));
		addToToolbar(toolbar, lineButton);

		toolbar.add(Box.createRigidArea(borderSpacerSize));
		toolbar.addSeparator();
		toolbar.add(Box.createRigidArea(borderSpacerSize));

		// quick object buttons
		addNewNodeAction(toolbar, spacerSize, PgkbType.GENE, DictionaryPropertyType.GENE_DICTIONARY_ID, KeyEvent.VK_1, buttonBorder);
		addNewNodeAction(toolbar, spacerSize, PgkbType.GENE_COLLECTION, null, KeyEvent.VK_2, buttonBorder);
		addNewNodeAction(toolbar, spacerSize, PgkbType.DRUG, DictionaryPropertyType.DRUG_ONLY_DICTIONARY_ID, KeyEvent.VK_3, buttonBorder);
		addNewNodeAction(toolbar, spacerSize, PgkbType.BIOLOGICAL_INTERMEDIATE, null, KeyEvent.VK_4, buttonBorder);

		// other objects
		JComboBox<Action> objectCombo = new JComboBox<>();
		longestValue = "";
    longestAction = null;
		for (PgkbType type : PgkbType.values()) {
			String dictPropTypeId = null;
			switch (type) {
				case GENE:
				case GENE_COLLECTION:
				case DRUG:
				case BIOLOGICAL_INTERMEDIATE:
					continue;
				case HAPLOTYPE:
					dictPropTypeId = DictionaryPropertyType.PGKB_HAPLOTYPE_DICTIONARY_ID;
					break;
				case DRUG_CLASS:
					dictPropTypeId = DictionaryPropertyType.DRUG_CLASS_DICTIONARY_ID;
					break;
				case DISEASE:
					dictPropTypeId = DictionaryPropertyType.DISEASE_DICTIONARY_ID;
					break;
				case PATHWAY:
					dictPropTypeId = DictionaryPropertyType.PGKB_PATHWAY_DICTIONARY_ID;
					break;

			}
			if (type == PgkbType.GENE || type == PgkbType.GENE_COLLECTION || type == PgkbType.DRUG ||
					type == PgkbType.BIOLOGICAL_INTERMEDIATE) {
				continue;
			}
      Action action = addNewNodeAction(null, null, type, dictPropTypeId, -1, buttonBorder);
			objectCombo.addItem(action);
      if (longestValue.length() < type.getDisplayName().length()) {
        longestValue = type.getDisplayName();
        longestAction = action;
      }
		}
		objectCombo.setPrototypeDisplayValue(longestAction);
		objectCombo.setMaximumSize(objectCombo.getPreferredSize());
		objectCombo.setMaximumRowCount(objectCombo.getItemCount());
		addToToolbar(toolbar, objectCombo);
		toolbar.add(Box.createRigidArea(spacerSize));
		JButton elementButton = new JButton(new ImageIcon(Resources.getResourceURL("newrectangle.gif")));
		elementButton.setBorder(buttonBorder);
		elementButton.setToolTipText("Add element");
		elementButton.addActionListener(new ActionComboListener(objectCombo));
		addToToolbar(toolbar, elementButton);

		toolbar.add(Box.createRigidArea(spacerSize));
		toolbar.addSeparator();
		toolbar.add(Box.createRigidArea(borderSpacerSize));

		addToToolbar(toolbar, new CommonActions.NewElementAction(swingEngine.getEngine(),
				new DefaultTemplates.ShapeTemplate(ShapeType.ARC)), buttonBorder);
		addToToolbar(toolbar, new CommonActions.NewElementAction(swingEngine.getEngine(),
				new DefaultTemplates.ShapeTemplate(ShapeType.BRACE)), buttonBorder);

		toolbar.add(Box.createRigidArea(spacerSize));
		toolbar.addSeparator();
		toolbar.add(Box.createRigidArea(borderSpacerSize));

		// add default layout actions to toolbar
		for(Action layoutAction : swingEngine.getActions().layoutActions) {
			addToToolbar(toolbar, layoutAction, buttonBorder);
		}

		addToToolbar(toolbar, Box.createHorizontalGlue());
		System.out.println("  done initializing actions");
	}

	private Action addNewNodeAction(JToolBar toolbar, Dimension spacerSize, PgkbType type, String dictPropTypeId,
			int keyStroke, Border buttonBorder) {

		DictionaryPropertyType dictPropType = null;
		if (dictPropTypeId != null) {
			dictPropType = (DictionaryPropertyType)PropertyManager.getPropertyType(dictPropTypeId);
		}
		Action action = new CommonActions.NewElementAction(m_desktop.getSwingEngine().getEngine(),
				new NodeTemplate(type, m_desktop, dictPropType));
		ImageIcon icon = IconsFactory.getImageIcon(PgkbPlugin.class, type.getShortName() + ".png");
		if (icon != null) {
			action.putValue(Action.SMALL_ICON, icon);
			action.putValue(Action.LARGE_ICON_KEY, icon);
		}
		if (keyStroke != -1) {
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			action.putValue(Action.MNEMONIC_KEY, keyStroke);
		}

		if (toolbar != null) {
			addToToolbar(toolbar, action, buttonBorder);
			if (spacerSize != null) {
				toolbar.add(Box.createRigidArea(spacerSize));
			}
		}

		return action;
	}

	/**
	 * Enable all actions.
	 */
	private void enableActions() {
		for (Action action : m_actions) {
			action.setEnabled(true);
		}
	}


	private void initProperties() {

    Logger.log.setDest("STDOUT");
    Logger.log.setLogLevel(false, false, true, true, true, true);
		System.out.println("Initializing properties");
		// hide static properties and only show ones that pertain to PharmGKB
		for (Property prop : StaticProperty.values()) {
			PropertyDisplayManager.setVisible(prop, false);
		}
		// for pathways
		PropertyDisplayManager.setVisible(StaticProperty.MAPINFONAME, true); //title
		PropertyDisplayManager.setPropertyOrder(StaticProperty.MAPINFONAME, 13);
		PropertyDisplayManager.setVisible(StaticProperty.ORGANISM, true);
		PropertyDisplayManager.setPropertyOrder(StaticProperty.ORGANISM, 14);
		// data nodes
		PropertyDisplayManager.setVisible(StaticProperty.TEXTLABEL, true);
		PropertyDisplayManager.setPropertyOrder(StaticProperty.TEXTLABEL, 11);
		PropertyDisplayManager.setVisible(StaticProperty.COMMENTS, true);
		PropertyDisplayManager.setPropertyOrder(StaticProperty.COMMENTS, 200);
		PropertyDisplayManager.setVisible(StaticProperty.GRAPHID, true);
		PropertyDisplayManager.setPropertyOrder(StaticProperty.GRAPHID, 201);

		try {
			// register PharmGKB Type property
			PvUtils.registerProperty(PgkbType.getProperty(), m_desktop.getFrame());
			PvUtils.registerProperty(BiopaxInteractionType.getProperty(), m_desktop.getFrame());
			// register dictionary types
			downloadAndUnpackFile();
			createDictionaryType(DictionaryPropertyType.GENE_DICTIONARY_ID, "genes");
			DictionaryPropertyType drugOnly = createDictionaryType(DictionaryPropertyType.DRUG_ONLY_DICTIONARY_ID, "drugs", "drug");
			DictionaryPropertyType drugClassOnly = createDictionaryType(DictionaryPropertyType.DRUG_CLASS_DICTIONARY_ID, "drugs", "drugClass");

			DictionaryPropertyType dictType = new DictionaryPropertyType(DictionaryPropertyType.DRUG_DICTIONARY_ID);
			for (String key : drugOnly.getEntries().keySet()) {
				dictType.addEntry(key, drugOnly.getEntries().get(key));
			}
			for (String key : drugClassOnly.getEntries().keySet()) {
				dictType.addEntry(key, "<i>" + drugClassOnly.getEntries().get(key) + "</i>");
			}
			PropertyManager.registerPropertyType(dictType);

			createDictionaryType(DictionaryPropertyType.DISEASE_DICTIONARY_ID, "diseases");
			createDictionaryType(DictionaryPropertyType.ATC_DICTIONARY_ID, "atc");
			createDictionaryType(DictionaryPropertyType.CL_DICTIONARY_ID, "cl");
			createDictionaryType(DictionaryPropertyType.PGKB_PATHWAY_DICTIONARY_ID, "pathways");
			createDictionaryType(DictionaryPropertyType.PGKB_HAPLOTYPE_DICTIONARY_ID, "haplotypes");
			// initialize properties from XML
			initPropertyFile();

		} catch (Exception ex) {
			Logger.log.error("Error initializing PharmGKB plugin", ex);
      showErrorMessage(ex);
		}
		System.out.println("  done initializing properties");
	}

  private void showErrorMessage(Throwable t) {

    StringBuilder errBuilder = new StringBuilder();
    while (t != null) {
      if (errBuilder.length() != 0) {
        errBuilder.append("\nCaused by ");
      }
      errBuilder.append(t.getClass().getSimpleName())
          .append(":\n  ")
          .append(t.getMessage());
      t = t.getCause();
    }
    JOptionPane.showMessageDialog(m_desktop.getFrame(),
        "Uh-oh.  Something went hideously wrong.\n\n" +
            errBuilder.toString() +
            "\n\nDO NOT CONTINUE USING PATHVISIO!\n\n" +
            "Please go yell at Mark.\n\n",
        "Error Initializing Plugin", JOptionPane.ERROR_MESSAGE);
  }

	/**
	 * Creates a dictionary property type that's based on PharmGKB data file.
	 *
	 * @param id		   the ID of the DictionaryPropertyType
	 * @param baseFilename the base filename of the PharmGKB data file
	 */
	private DictionaryPropertyType createDictionaryType(String id, String baseFilename) throws PgkbException {
		return createDictionaryType(id, baseFilename, null);
	}


	private DictionaryPropertyType createDictionaryType(String id, String baseFilename, String filterValue)
      throws PgkbException {

		File dataFile = new File(GlobalPreference.getDataDir(), baseFilename + ".tsv");
		System.out.println("Parsing dictionary");
		DictionaryPropertyType dictType = new DictionaryPropertyType(id);
		dictType.readTsv(dataFile, filterValue);
		System.out.println("  done parsing");
		PropertyManager.registerPropertyType(dictType);
		return dictType;
	}

	private boolean downloadAndUnpackFile() throws PgkbException {

		String url = "http://www.pharmgkb.org/download.do?objId=pathvisio.zip&ref=pgkb-pathvisio";
		// download and unzip data file
		System.out.println("Checking " + url);
		File downloadedFile = IoUtils.downloadFromUrl(url, "pathvisio.zip");
		if (downloadedFile == null) {
			return false;
		}
		System.out.println("  done.");
		File dataFile = null;
		try {
			ZipFile zipFile = new ZipFile(downloadedFile);
			Enumeration entries = zipFile.entries();
			int fileCount = 0;
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				if (entry.isDirectory() || entry.getName().startsWith("CREATED_")) {
					continue;
				}
				// unpack file
				dataFile = new File(GlobalPreference.getDataDir(), entry.getName());
				IoUtils.copyInputStream(zipFile.getInputStream(entry), new FileOutputStream(dataFile));
				fileCount += 1;
			}
			if (fileCount == 0) {
				throw new PgkbException("Empty zip file '" + url + "'");
			}
			return true;

		} catch (ZipException ex) {
			if (dataFile != null && !dataFile.delete()) {
				Logger.log.warn("Error deleting " + dataFile.getAbsolutePath());
			}
			throw new PgkbException("Error opening zip file", ex);
		} catch (IOException ex) {
			throw new PgkbException("Error unzipping data", ex);
		}
	}

	/**
	 * Initialize properties based on properties.xml.
	 */
	private void initPropertyFile() throws PgkbException {

		InputStream in = null;
		try {
			in = PgkbPlugin.class.getResourceAsStream("properties.xml");
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();

			Document doc = docBuilder.parse(in);
			NodeList roots = doc.getElementsByTagName("pathvisio");
			for (int i = 0; i < roots.getLength(); i++) {
				Element rootElement = (Element)roots.item(i);
				processProperties(rootElement.getElementsByTagName("properties"));
				processObjectProperties(rootElement.getElementsByTagName("objects"));
			}

		} catch (Exception ex) {
			if (ex instanceof PgkbException) {
				throw (PgkbException)ex;
			}
			throw new PgkbException("Error initializing properties", ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	/**
	 * Parse properties.
	 */
	private void processProperties(NodeList propsNL) throws PgkbException {

		for (int i = 0; i < propsNL.getLength(); i++) {
			Element propsElem = (Element)propsNL.item(i);
			NodeList defNL = propsElem.getElementsByTagName("property");
			for (int j = 0; j < defNL.getLength(); j++) {
				Element propElem = (Element)defNL.item(j);
				String type = propElem.getAttribute("type");
				String id = propElem.getAttribute("id");
				String name = propElem.getAttribute("name");
				String desc = propElem.getAttribute("description");
				boolean isCollection = Boolean.parseBoolean(propElem.getAttribute("multiSelect"));
				int order = Integer.parseInt(propElem.getAttribute("order"));
				String defaultValue = propElem.getAttribute("defaultValue");
				boolean isEditable = true;
				if (propElem.hasAttribute("editable")) {
					isEditable = Boolean.parseBoolean(propElem.getAttribute("editable"));
				}
				ExtendedProperty prop;

				if (TYPE_DICTIONARY.equals(type)) {
					DictionaryPropertyType dictType;
					if (propElem.hasAttribute("typeId")) {
						String typeId = propElem.getAttribute("typeId");
						dictType = (DictionaryPropertyType)PropertyManager.getPropertyType(typeId);
						if (dictType == null) {
							throw new PgkbException("Unknown property type '" + typeId + "' for property '" + id + "'");
						}
					} else {
						dictType = new DictionaryPropertyType(id);
						String fileName = propElem.getAttribute("file");
						if (!Utils.isEmpty(fileName)) {
							if (fileName.endsWith(".xml")) {
								dictType.readXml(new File(fileName));
							} else {
								throw new PgkbException("Unsupported dictionary format '" + fileName + "'");
							}
						} else {
							NodeList optionNL = propElem.getElementsByTagName("option");
							for (int m = 0; m < optionNL.getLength(); m++) {
								Element optionElem = (Element)optionNL.item(m);
								String entryId = optionElem.getAttribute("id");
								String optionName = optionElem.getAttribute("name");
								dictType.addEntry(entryId, optionName);
							}
						}
					}
					prop = new DictionaryProperty(id, name, desc, order, defaultValue, isCollection, dictType, isEditable);

				} else if (TYPE_ENUM.equals(type)) {
					if (isCollection) {
						throw new PgkbException("Enum property '" + id + "' does not support multiselect, use dictionary instead");
					}
					EnumProperty enumProp = new EnumProperty(id, name, desc, order, defaultValue, isCollection, isEditable);
					prop = enumProp;
					NodeList optionNL = propElem.getElementsByTagName("option");
					for (int m = 0; m < optionNL.getLength(); m++) {
						Element optionElem = (Element)optionNL.item(m);
						if (!optionElem.hasAttribute("name")) {
							throw new PgkbException("Enum option for " + prop.getId() + " is missing name");
						}
						String optionName = optionElem.getAttribute("name");
						String optionData = null;
						if (optionElem.hasAttribute("data")) {
							optionData = optionElem.getAttribute("data");
						}
						enumProp.addValue(optionName, optionData);
					}

				} else {
					// must be a known property type
					PropertyType propType = PropertyManager.getPropertyType(type);
					if (propType == null) {
						throw new PgkbException("Unknown property type '" + type + "' for property '" + id + "'");
					}
					prop = new SimpleProperty(id, name, desc, order, defaultValue, propType, isCollection, isEditable);
				}

				PvUtils.registerProperty(prop, m_desktop.getFrame());
			}
		}
	}

	/**
	 * Parse object properties.  Should be called after parsing properties.
	 */
	private void processObjectProperties(NodeList objsNL) throws PgkbException {
		// objects
		for (int i = 0; i < objsNL.getLength(); i++) {
			Element rootElement = (Element)objsNL.item(i);
			//objects
			NodeList objNL = rootElement.getElementsByTagName("object");
			for (int j = 0; j < objNL.getLength(); j++) {
				Element objElem = (Element)objNL.item(j);
				ObjectType objType = ObjectType.valueOf(objElem.getAttribute("type"));
				// properties, add each property to objProperties
				NodeList propNL = objElem.getElementsByTagName("property");
				for (int m = 0; m < propNL.getLength(); m++) {
					Element propElem = (Element)propNL.item(m);
					String propId = propElem.getAttribute("id");
					Property prop = PropertyManager.getProperty(propId);
					if (prop == null) {
						throw new PgkbException("Unknown property: '" + propId + "'");
					}
					m_objectPropertiesManager.registerProperty(objType, prop);
				}
				// control properties
				NodeList controlPropertyNL = objElem.getElementsByTagName("controlProperty");
				for (int n = 0; n < controlPropertyNL.getLength(); n++) {
					Element controlPropElem = (Element)controlPropertyNL.item(n);
					String controlPropId = controlPropElem.getAttribute("id");
					Property controlProp = PropertyManager.getProperty(controlPropId);
					if (controlProp == null) {
						throw new PgkbException("Unknown control property: '" + controlPropId + "'");
					}
					String condition = controlPropElem.getAttribute("condition");
					// dependent properties
					NodeList dependentPropertyNL = controlPropElem.getElementsByTagName("dependentProperty");
					for (int p = 0; p < dependentPropertyNL.getLength(); p++) {
						Element dependentPropElem = (Element)dependentPropertyNL.item(p);
						String dependentPropId = dependentPropElem.getAttribute("id");
						Property dependentProp = PropertyManager.getProperty(dependentPropId);
						if (dependentProp == null) {
							throw new PgkbException("Unknown dependent property: '" + dependentPropId + "'");
						}
						m_objectPropertiesManager.registerDependentProperty(objType, controlProp, condition, dependentProp);
					}
				}
			}
		}
	}


	//-- ObjectPropertyListener  methods --//
	public void objectModified(ObjectPropertyEvent event) {

		PathwayElement elem = event.getElement();
		switch (event.getType()) {
			// catching this here instead of just handling it directly in ObjectPropertyManager
			// to allow ObjectPropertyManager to be more generic
			case ObjectPropertyEvent.PATHWAY_NEW:
				elem.setOrganism("Homo sapiens");
				break;
		}
	}


	//-- ApplicationEventListener  methods --//

	public void applicationEvent(ApplicationEvent e) {

		if (e.getType() == ApplicationEvent.PATHWAY_NEW || e.getType() == ApplicationEvent.PATHWAY_OPENED) {
			enableActions();
		}
	}




	//-- handler for zoom combo box in toolbar
	private class ZoomComboListener implements ActionListener {

		public void actionPerformed(ActionEvent e){
			JComboBox combo = (JComboBox) e.getSource();
			Object s = combo.getSelectedItem();
			if (s instanceof Action) {
				((Action) s).actionPerformed(e);
			} else if (s instanceof String) {
				String zs = (String) s;
				zs=zs.replace("%","");
				try {
					double zf = Double.parseDouble(zs);
					CommonActions.ZoomAction za = new CommonActions.ZoomAction(m_desktop.getSwingEngine().getEngine(), zf);
					za.setEnabled(true);
					za.actionPerformed(e);
				} catch (Exception ex) {
					// ignore bad input
				}
			}
		}
	}

	//-- handler for combo boxes in toolbar
	private class ActionComboListener implements ActionListener {
		private JComboBox m_interactionComboBox;

		public ActionComboListener(JComboBox interactionComboBox) {
			m_interactionComboBox = interactionComboBox;
		}

		public void actionPerformed(ActionEvent e){
			Action action = (Action)m_interactionComboBox.getSelectedItem();
			action.actionPerformed(e);
		}
	}
}