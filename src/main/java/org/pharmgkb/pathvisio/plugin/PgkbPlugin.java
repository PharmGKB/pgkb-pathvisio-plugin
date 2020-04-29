package org.pharmgkb.pathvisio.plugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.jidesoft.icons.IconsFactory;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideLabel;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.Property;
import org.pathvisio.core.model.PropertyManager;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.util.Resources;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gui.CommonActions;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.handler.PropertyDisplayManager;
import org.pharmgkb.pathvisio.BiopaxInteractionType;
import org.pharmgkb.pathvisio.EnumProperty;
import org.pharmgkb.pathvisio.ExtendedProperty;
import org.pharmgkb.pathvisio.GpmlValidator;
import org.pharmgkb.pathvisio.PgkbType;
import org.pharmgkb.pathvisio.ReadOnlyPropertyType;
import org.pharmgkb.pathvisio.SimpleProperty;
import org.pharmgkb.pathvisio.SimplePropertyType;
import org.pharmgkb.pathvisio.plugin.action.AddLiteratureAction;
import org.pharmgkb.pathvisio.plugin.action.EditLiteratureAction;
import org.pharmgkb.pathvisio.plugin.swing.PopupDialogBuilder;
import org.pharmgkb.pathvisio.plugin.swing.WrapLayout;
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
  public static final String TYPE_DICTIONARY = "DICTIONARY";
  public static final String TYPE_ENUM = "ENUM";

  private static final Border sf_buttonBorder = new EmptyBorder(4, 4, 4, 4);

  private PvDesktop m_desktop;
  private ObjectPropertyManager m_objectPropertiesManager;
  private Set<Action> m_actions = new HashSet<>();
  private Set<Component> m_toolbarComponents = new HashSet<>();


  private Component getSpacer() {
    return Box.createRigidArea(new Dimension(4, 0));
  }

  private Component getBorderSpacer() {
    return Box.createRigidArea(new Dimension(8, 0));
  }

  @Override
  public void init(PvDesktop desktop) {

    try {
      System.out.println("Initializing PgkbPlugin");
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
      desktop.getFrame().setTitle("PGKB PathVisio");

      initProperties();
      initActions();
      if (desktop.getSwingEngine().getEngine().hasVPathway()) {
        enableActions();
      }

      System.out.println("Done initializing PgkbPlugin");


    } catch (Exception ex) {
      Logger.log.error("Error initializing plugin", ex);
      showErrorMessage(ex);
      throw ex;
    }
  }

  @Override
  public void done() {
    // should remove from objects it's listening to,
    // but not bothering because PV doesn't support disabling a plugin
  }


  private JPanel addPanelToToolbar(JToolBar toolbar, @Nullable String title) {

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    if (title != null) {
      panel.add(new JideLabel(title, JLabel.LEFT));
    }
    toolbar.add(panel);
    return panel;
  }

  private void addToPanelInToolbar(JPanel panel, Action action, boolean hideText) {

    action.setEnabled(false);
    m_actions.add(action);

    JideButton button = new JideButton(action);
    button.setBorder(sf_buttonBorder);
    button.setFocusable(false);
    button.setHideActionText(hideText);
    disableToolbarItem(button);

    panel.add(button);
  }

  private void endPadToolbarPanel(JPanel panel) {
    panel.add(getBorderSpacer());
  }

  private void addToPanelInToolbar(JPanel panel, Component component) {
    m_toolbarComponents.add(component);
    panel.add(component);
    disableToolbarItem(component);
  }

  private void disableToolbarItem(Component component) {
    component.setEnabled(false);
    Set<Component> gb = m_desktop.getSwingEngine().getApplicationPanel().getToolbarGroup(MainPanel.TB_GROUP_SHOW_IF_VPATHWAY);
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
    toolbar.setLayout(new WrapLayout(FlowLayout.LEFT));

    toolbar.removeAll();
    swingEngine.getApplicationPanel().getToolbarGroup(MainPanel.TB_GROUP_SHOW_IF_VPATHWAY).clear();

    // construct comboboxes
    JComboBox<Action> interactionCombo = new JComboBox<>();
    for (BiopaxInteractionType interactionType : BiopaxInteractionType.getAllSortedByName()) {
      // TODO(markwoon): filter out deprecated types until they're gone for good
      if (interactionType == BiopaxInteractionType.INFO_LABEL_LINE ||
          interactionType == BiopaxInteractionType.TEMPLATE_REACTION_REGULATION ||
          interactionType == BiopaxInteractionType.SUBINTERACTION) {
        continue;
      }
      Action action = new CommonActions.NewElementAction(swingEngine.getEngine(), new LineTemplate(interactionType));
      interactionCombo.addItem(action);
    }
    JComboBox<Action> nodeCombo = new JComboBox<>();
    JComboBox<Action> shapeCombo = new JComboBox<>();
    for (PgkbType type : PgkbType.getAllSortedByName()) {
      String dictPropTypeId = null;
      switch (type) {
        case GENE:
        case GENE_COLLECTION:
        case DRUG:
        case BIOLOGICAL_INTERMEDIATE:
        case BLACK_BOX:
          // TODO(markwoon): filter out BLACK_BOX until they're gone for good
          continue;
        case HAPLOTYPE:
          dictPropTypeId = DictionaryPropertyType.HAPLOTYPE_DICTIONARY_ID;
          break;
        case METABOLITE:
        case ION:
          dictPropTypeId = DictionaryPropertyType.CHEMICAL_DICTIONARY_ID;
          break;
        case DRUG_CLASS:
          dictPropTypeId = DictionaryPropertyType.DRUG_CLASS_DICTIONARY_ID;
          break;
        case PHENOTYPE:
          dictPropTypeId = DictionaryPropertyType.PHENOTYPE_DICTIONARY_ID;
          break;
        case PATHWAY:
          dictPropTypeId = DictionaryPropertyType.PATHWAY_DICTIONARY_ID;
          break;
        case PHYSICAL_ENTITY:
          dictPropTypeId = DictionaryPropertyType.PHYSICAL_ENTITY_DICTIONARY_ID;
          break;
        case PROCESS:
          dictPropTypeId = DictionaryPropertyType.PROCESS_DICTIONARY_ID;
          break;
        default:
          // fall out
      }
      Action action = newNodeAction(type, dictPropTypeId, true, -1);
      if (type.isDrawingOnly()) {
        // TODO(markwoon): filter out deprecated types until they're gone for good
        switch (type) {
          case DNA:
          case RNA:
            break;
          default:
            shapeCombo.addItem(action);
        }
      } else {
        nodeCombo.addItem(action);
      }
    }


    // zoom
    JPanel panel = addPanelToToolbar(toolbar, "Zoom: ");
    JComboBox<Action> zoomCombo = new JComboBox<>(swingEngine.getActions().zoomActions);
    zoomCombo.setMaximumSize(zoomCombo.getPreferredSize());
    zoomCombo.setEditable(true);
    zoomCombo.setSelectedIndex(5); // 100%
    zoomCombo.addActionListener(new ZoomComboListener());
    addToPanelInToolbar(panel, zoomCombo);

    endPadToolbarPanel(panel);

    // interactions
    panel = addPanelToToolbar(toolbar, "Interactions: ");
    // combobox
    addToPanelInToolbar(panel, optimizeComboBox(interactionCombo));
    // button
    addToPanelInToolbar(panel, createComboButton("Add interaction", "newarrow.gif", interactionCombo));

    endPadToolbarPanel(panel);

    // nodes
    panel = addPanelToToolbar(toolbar, "Nodes: ");
    // quick object buttons
    addAction(panel, newNodeAction(PgkbType.GENE, DictionaryPropertyType.GENE_DICTIONARY_ID, true, KeyEvent.VK_1),
        PgkbType.GENE);
    addAction(panel, newNodeAction(PgkbType.GENE_COLLECTION, KeyEvent.VK_2),
        PgkbType.GENE_COLLECTION);
    addAction(panel, newNodeAction(PgkbType.DRUG, DictionaryPropertyType.DRUG_DICTIONARY_ID, true, KeyEvent.VK_3),
        PgkbType.DRUG);
    addAction(panel, newNodeAction(PgkbType.BIOLOGICAL_INTERMEDIATE, DictionaryPropertyType.BIOLOGICAL_INTERMEDIATE_DICTIONARY_ID, true, KeyEvent.VK_4),
        PgkbType.BIOLOGICAL_INTERMEDIATE);

    panel.add(getSpacer());

    // node dropdown
    panel = addPanelToToolbar(toolbar, null);
    // combobox
    addToPanelInToolbar(panel, optimizeComboBox(nodeCombo));
    // button
    addToPanelInToolbar(panel, createComboButton("Add node", "newrectangle.gif", nodeCombo));

    endPadToolbarPanel(panel);

    // drawing only nodes
    panel = addPanelToToolbar(toolbar, "Drawing Only: ");
    // combobox
    addToPanelInToolbar(panel, optimizeComboBox(shapeCombo));
    // button
    addToPanelInToolbar(panel, createComboButton("Add shape", "newmitochondria.gif", shapeCombo));

    endPadToolbarPanel(panel);

    // add default layout actions to toolbar
    panel = addPanelToToolbar(toolbar, "Layout: ");
    for (Action layoutAction : swingEngine.getActions().layoutActions) {
      addToPanelInToolbar(panel, layoutAction, true);
    }

    endPadToolbarPanel(panel);

    // add hotkey for literature action
    panel = addPanelToToolbar(toolbar, "Literature: ");
    addToPanelInToolbar(panel, new AddLiteratureAction(m_desktop.getSwingEngine()), true);
    addToPanelInToolbar(panel, new EditLiteratureAction(m_desktop.getSwingEngine()), true);

    // done with toolbar
    toolbar.add(Box.createHorizontalGlue());
    toolbar.updateUI();

    System.out.println("  done initializing actions");
    checkForNewVersion();
  }

  private JideButton createComboButton(String tooltip, String icon, JComboBox comboBox) {
    JideButton button = new JideButton(new ImageIcon(Resources.getResourceURL(icon)));
    button.setBorder(sf_buttonBorder);
    button.setToolTipText(tooltip);
    button.setHideActionText(true);
    button.addActionListener(new ActionComboListener(comboBox));
    return button;
  }

  private JComboBox optimizeComboBox(JComboBox<Action> comboBox) {

    String longestValue = "";
    Action longestAction = null;
    for (int x = 0; x < comboBox.getItemCount(); x += 1) {
      Action action = comboBox.getItemAt(x);
      String name = ((String)action.getValue(Action.NAME));
      if (longestValue.length() < name.length()) {
        longestValue = name;
        longestAction = action;
      }
    }
    comboBox.setPrototypeDisplayValue(longestAction);
    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.setMaximumRowCount(comboBox.getItemCount());
    return comboBox;
  }


  private void addAction(JPanel panel, Action action, PgkbType type) {

    ImageIcon icon = IconsFactory.getImageIcon(PgkbPlugin.class, type.getShortName() + ".png");
    if (icon != null) {
      action.putValue(Action.SMALL_ICON, icon);
      action.putValue(Action.LARGE_ICON_KEY, icon);
    }
    addToPanelInToolbar(panel, action, true);
  }

  private Action newNodeAction(PgkbType type, int keyStroke) {
    return newNodeAction(type, null, false, keyStroke);
  }

  private Action newNodeAction(PgkbType type, @Nullable String dictPropTypeId, boolean dictValueRequired,
      int keyStroke) {

    DictionaryPropertyType dictPropType = null;
    if (dictPropTypeId != null) {
      dictPropType = (DictionaryPropertyType)PropertyManager.getPropertyType(dictPropTypeId);
    }
    Action action = new CommonActions.NewElementAction(m_desktop.getSwingEngine().getEngine(),
        new NodeTemplate(type, m_desktop, dictPropType, dictValueRequired));
    if (keyStroke != -1) {
      action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke,
          Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      action.putValue(Action.MNEMONIC_KEY, keyStroke);
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
    for (Component comp : m_toolbarComponents) {
      comp.setEnabled(true);
    }
    JToolBar toolbar = m_desktop.getSwingEngine().getApplicationPanel().getToolBar();
    Component[] components = toolbar.getComponents();
    if (components[components.length - 1] instanceof JComboBox) {
      toolbar.remove(components.length - 1);
      toolbar.updateUI();
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

      try {
        DownloadUtils.downloadAndUnpackDataFile();
      } catch (NetworkException ex) {
        new PopupDialogBuilder(m_desktop).error()
            .title("No Network")
            .message("Can't connect to the network: " + ex.getMessage())
            .show();
      }
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
        errBuilder.append("<p>Caused by ");
      }
      errBuilder.append(t.getClass().getSimpleName())
          .append(":<br />  ")
          .append(t.getMessage());
      t = t.getCause();
    }
    new PopupDialogBuilder(m_desktop).error()
        .title("Error Initializing Plugin")
        .htmlMessage("<p>Uh-oh.  Something went horribly, horribly wrong." +
            "<p><b>DO NOT CONTINUE USING PATHVISIO!</b>" +
            "<p>Please go yell at Mark and tell him:" +
            errBuilder.toString())
        .show();
  }


  /**
   * Initialize properties based on properties.xml.
   */
  private void initPropertyFile() throws PgkbPluginException {

    try (InputStream in = PgkbPlugin.class.getResourceAsStream("properties.xml")) {
      DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dbfac.newDocumentBuilder();

      Document doc = docBuilder.parse(in);
      NodeList roots = doc.getElementsByTagName("pathvisio");
      for (int i = 0; i < roots.getLength(); i++) {
        Element rootElement = (Element)roots.item(i);
        processTypes(rootElement.getElementsByTagName("types"));
        processProperties(rootElement.getElementsByTagName("properties"));
        processObjectProperties(rootElement.getElementsByTagName("objects"));
      }

    } catch (PgkbPluginException ex) {
      throw ex;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new PgkbPluginException("Error initializing properties", ex);
    }
  }

  /**
   * Parse types.
   */
  private void processTypes(NodeList propsNL) throws PgkbPluginException {

    for (int i = 0; i < propsNL.getLength(); i++) {
      Element propsElem = (Element)propsNL.item(i);
      NodeList defNL = propsElem.getElementsByTagName("dictionary");
      for (int j = 0; j < defNL.getLength(); j++) {
        Element propElem = (Element)defNL.item(j);
        String id = propElem.getAttribute("id");
        String file = StringUtils.stripToNull(propElem.getAttribute("file"));

        DictionaryPropertyType dictType;
        if (file == null) {
          dictType = new DictionaryPropertyType(id);
          NodeList optionNL = propElem.getElementsByTagName("option");
          if (optionNL.getLength() == 0) {
            throw new PgkbPluginException("No file or options specified for dictionary " + id);
          }
          for (int m = 0; m < optionNL.getLength(); m++) {
            Element optionElem = (Element)optionNL.item(m);
            String entryId = optionElem.getAttribute("id");
            String optionName = optionElem.getAttribute("name");
            dictType.addEntry(entryId, optionName);
          }

        } else {
          String format = StringUtils.stripToNull(propElem.getAttribute("format"));
          String filter = StringUtils.stripToNull(propElem.getAttribute("filter"));
          String[] filters = null;
          if (filter != null) {
            filters = filter.split(",");
          }

          File dataFile = new File(GlobalPreference.getDataDir(), file + ".tsv");
          System.out.println("Parsing " + file + " dictionary");
          dictType = new DictionaryPropertyType(id);
          dictType.readTsv(dataFile, format, filters);
          System.out.println("  done parsing");
        }
        PropertyManager.registerPropertyType(dictType);
      }
    }
  }

  /**
   * Parse properties.
   */
  private void processProperties(NodeList propsNL) throws PgkbPluginException {

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
        String defaultValue = StringUtils.stripToNull(propElem.getAttribute("defaultValue"));
        boolean isEditable = true;
        if (propElem.hasAttribute("editable")) {
          isEditable = Boolean.parseBoolean(propElem.getAttribute("editable"));
        }

        ExtendedProperty prop;
        if (TYPE_DICTIONARY.equals(type)) {
          String typeId = propElem.getAttribute("typeId");
          PropertyType dictType = PropertyManager.getPropertyType(typeId);
          if (dictType == null) {
            throw new PgkbPluginException("Unknown dictionary property type '" + typeId + "' for property '" + id + "'");
          }
          if (!isEditable) {
            dictType = getReadOnlyPropertyType(typeId, dictType);
          }
          if (!isCollection && ((DictionaryPropertyType)dictType).getEntries().size() < 20) {
            // if not collection and there are less than 20 entries, allow selection via dropdown
            EnumProperty enumProp = new EnumProperty(id, name, desc, order, defaultValue, dictType, false);
            for (Map.Entry<String, String> entry : ((DictionaryPropertyType)dictType).getEntries().entrySet()) {
              enumProp.addValue(entry.getValue(), entry.getKey());
            }
            if (defaultValue != null) {
              if (!enumProp.getValues().contains(defaultValue)) {
                throw new PgkbPluginException(id + " property does not have expected default value of '" + defaultValue + "'");
              }
            } else {
              // support empty value
              enumProp.addValue("", "");
            }
            prop = enumProp;
          } else {
            prop = new DictionaryProperty(id, name, desc, order, defaultValue, isCollection, dictType);
          }

        } else if (TYPE_ENUM.equals(type)) {
          if (isCollection) {
            throw new PgkbPluginException("Enum property '" + id + "' does not support multiselect, use dictionary instead");
          }
          PropertyType propType = new SimplePropertyType(id);
          if (!isEditable) {
            propType = getReadOnlyPropertyType(id, propType);
          }
          EnumProperty enumProp = new EnumProperty(id, name, desc, order, defaultValue, propType, false);
          prop = enumProp;
          NodeList optionNL = propElem.getElementsByTagName("option");
          for (int m = 0; m < optionNL.getLength(); m++) {
            Element optionElem = (Element)optionNL.item(m);
            if (!optionElem.hasAttribute("name")) {
              throw new PgkbPluginException("Enum option for " + prop.getId() + " is missing name");
            }
            String optionName = optionElem.getAttribute("name");
            String optionData = null;
            if (optionElem.hasAttribute("data")) {
              optionData = optionElem.getAttribute("data");
            }
            enumProp.addValue(optionName, optionData);
          }
          if (defaultValue != null) {
            if (!enumProp.getValues().contains(defaultValue)) {
              throw new PgkbPluginException(id + " property does not have expected default value of '" + defaultValue + "'");
            }
          }

        } else {
          // must be a known property type
          PropertyType propType = PropertyManager.getPropertyType(type);
          if (propType == null) {
            throw new PgkbPluginException("Unknown property type '" + type + "' for property '" + id + "'");
          }
          if (!isEditable) {
            propType = getReadOnlyPropertyType(type, propType);
          }
          prop = new SimpleProperty(id, name, desc, order, defaultValue, propType, isCollection);
        }

        PvUtils.registerProperty(prop, m_desktop.getFrame());
      }
    }
  }

  private PropertyType getReadOnlyPropertyType(String typeId, PropertyType type) {

    if (PropertyManager.getPropertyType(typeId + ".readOnly") == null) {
      return new ReadOnlyPropertyType(type);
    }
    return type;
  }

  /**
   * Parse object properties.  Should be called after parsing properties.
   */
  private void processObjectProperties(NodeList objsNL) throws PgkbPluginException {
    // objects
    for (int i = 0; i < objsNL.getLength(); i++) {
      Element rootElement = (Element)objsNL.item(i);
      //objects
      NodeList objNL = rootElement.getElementsByTagName("object");
      for (int j = 0; j < objNL.getLength(); j++) {
        Element objElem = (Element)objNL.item(j);
        ObjectType pvType = ObjectType.valueOf(objElem.getAttribute("type"));
        // properties, add each property to objProperties
        NodeList propNL = objElem.getElementsByTagName("property");
        for (int m = 0; m < propNL.getLength(); m++) {
          Element propElem = (Element)propNL.item(m);
          String propId = propElem.getAttribute("id");
          Property prop = PropertyManager.getProperty(propId);
          if (prop == null) {
            throw new PgkbPluginException("Unknown property: '" + propId + "'");
          }
          m_objectPropertiesManager.registerProperty(pvType, prop);
        }
        // control properties
        NodeList controlPropertyNL = objElem.getElementsByTagName("controlProperty");
        for (int n = 0; n < controlPropertyNL.getLength(); n++) {
          Element controlPropElem = (Element)controlPropertyNL.item(n);
          Property controlProp = PropertyManager.getProperty(controlPropElem.getAttribute("id"));
          if (controlProp == null) {
            throw new PgkbPluginException("Unknown control property: '" + controlPropElem.getAttribute("id") + "'");
          }
          String condition = controlPropElem.getAttribute("condition");
          // dependent properties
          NodeList dependentPropertyNL = controlPropElem.getElementsByTagName("dependentProperty");
          for (int p = 0; p < dependentPropertyNL.getLength(); p++) {
            Element dependentPropElem = (Element)dependentPropertyNL.item(p);
            String dependentPropId = dependentPropElem.getAttribute("id");
            Property dependentProp = PropertyManager.getProperty(dependentPropId);
            if (dependentProp == null) {
              throw new PgkbPluginException("Unknown dependent property: '" + dependentPropId + "'");
            }
            if (!(dependentProp instanceof ExtendedProperty)) {
              throw new PgkbPluginException("Dependent property '" + dependentPropId + "' is not an ExtendedProperty");
            }
            DependentProperty objProp = new DependentProperty((ExtendedProperty)dependentProp);
            String defaultValue = StringUtils.stripToNull(dependentPropElem.getAttribute("defaultValue"));
            if (defaultValue != null) {
              objProp.setDefaultValue(defaultValue);
            }
            m_objectPropertiesManager.registerDependentProperty(pvType, controlProp, condition, objProp);
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

    switch (e.getType()) {
      case PATHWAY_NEW:
      case PATHWAY_OPENED:
        enableActions();
        break;
      case PATHWAY_SAVE:
        try {
          GpmlValidator validator = new GpmlValidator.Builder().forPathway((Pathway)e.getSource()).build();
          if (!validator.validate()) {
            StringBuilder msgBuilder = new StringBuilder("<p>The file was saved.");

            if (!validator.getWarnings().isEmpty()) {
              msgBuilder.append("<p>But there are a few things you might want to fix:<ul>");
              for (String warn : validator.getWarnings()) {
                msgBuilder.append("<li>")
                    .append(warn)
                    .append("</li>");
              }
              msgBuilder.append("</ul>");
            }

            if (!validator.getErrors().isEmpty()) {
              msgBuilder.append("<p>The following errors were found:<ul>");
              for (String err : validator.getErrors()) {
                msgBuilder.append("<li>")
                    .append(err)
                    .append("</li>");
              }
              msgBuilder.append("</ul>");
            }
            new PopupDialogBuilder(m_desktop).error()
                .title("Validation Problems")
                .htmlMessage(msgBuilder.toString())
                .show();
          }
        } catch (Exception ex) {
          Logger.log.error("Validation error", ex);
          new PopupDialogBuilder(m_desktop).error()
              .title("Validation Problems")
              .htmlMessage("<p>Something went horribly wrong (" + ex.getMessage() + ")." +
                  "<p>Please contact a developer.")
              .show();
        }
        break;
      default:
        // fall out
    }
  }



  private void checkForNewVersion() {

    try {
      if (DownloadUtils.hasNewVersion()) {
        StringBuilder msgBuilder = new StringBuilder()
            .append("<p>There is a new version of PathVisio available.<p><a href=\"");
        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
          msgBuilder.append("https://stanford.box.com/shared/static/e7dzeopmu7tw7d82gnr2kjdb095pkqr7.zip");
        } else {
          msgBuilder.append("https://stanford.box.com/shared/static/1zru5xs42gsscyb2p3w857sptsaa7nlr.zip");
        }
        msgBuilder.append("\">Please update as soon as possible.</a>");
        new PopupDialogBuilder(m_desktop).info()
            .title("Time To Upgrade!")
            .htmlMessage(msgBuilder.toString())
            .show();
      }
    } catch (Exception ex) {
      Logger.log.error("Error checking version", ex);
      new PopupDialogBuilder(m_desktop)
          .title("Version Check Error")
          .error()
          .message("Error checking for newer version: " + ex.getMessage())
          .show();
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

    private ActionComboListener(JComboBox interactionComboBox) {
      m_interactionComboBox = interactionComboBox;
    }

    public void actionPerformed(ActionEvent e){
      Action action = (Action)m_interactionComboBox.getSelectedItem();
      action.actionPerformed(e);
    }
  }
}
