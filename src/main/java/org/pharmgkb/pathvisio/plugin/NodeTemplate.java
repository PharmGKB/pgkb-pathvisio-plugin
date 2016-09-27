/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JDialog;
import org.pathvisio.core.gui.SwingMouseEvent;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.Utils;
import org.pathvisio.core.view.DefaultTemplates;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.gui.view.VPathwaySwing;
import org.pharmgkb.pathvisio.DynamicProperty;
import org.pharmgkb.pathvisio.PgkbType;
import org.pharmgkb.pathvisio.plugin.swing.NewDictionaryElementDialog;
import org.pharmgkb.pathvisio.plugin.swing.NewElementDialog;

/**
 * Template for creating a new data node.
 *
 * @author Mark Woon
 */
public class NodeTemplate extends DefaultTemplates.DataNodeTemplate {
  private PgkbType m_type;
  private String m_name;
  private NewElementDialog m_newElementDialog;
  private NewDictionaryElementDialog m_newDictionaryElementDialog;
  private PvDesktop m_desktop;

  public NodeTemplate(@Nonnull PgkbType type, @Nonnull PvDesktop desktop,
      @Nullable DictionaryPropertyType dictPropType, boolean dictValueRequired) {
    super(type.getDataNodeType());
    m_type = type;
    m_name = m_type.getDisplayName();
    if (m_type.getNotes() != null) {
      m_name = m_name + " " + m_type.getNotes();
    }
    m_desktop = desktop;
    if (dictPropType == null) {
      m_newElementDialog = new NewElementDialog(desktop, type);
    } else {
      m_newDictionaryElementDialog = new NewDictionaryElementDialog(desktop, type, dictPropType, dictValueRequired);
    }
  }


  @Override
  public String getName() {
    return m_name;
  }


  public @Nullable PathwayElement[] addElements(Pathway p, double mx, double my) {

    JDialog dialog = null;
    if (m_newElementDialog != null) {
      dialog = m_newElementDialog;
      m_newElementDialog.reset();
    } else if (m_newDictionaryElementDialog != null) {
      dialog = m_newDictionaryElementDialog;
      m_newDictionaryElementDialog.reset();
    }

    String name;
    String accId = null;
    if (dialog != null) {
      dialog.setLocationRelativeTo(m_desktop.getFrame());
      dialog.setVisible(true);

      if (m_newElementDialog != null) {
        name = m_newElementDialog.getName();
      } else {
        name = m_newDictionaryElementDialog.getSelectedName();
        accId = m_newDictionaryElementDialog.getSelectedAccessionId();
      }
    } else {
      name = m_name;
    }
    if (Utils.isEmpty(name)) {
      return null;
    }
    PathwayElement element = PathwayElement.createPathwayElement(ObjectType.DATANODE);
    element.setGraphId(p.getUniqueGraphId());
    PvUtils.customizePathwayElement(element, m_type);
    element.setTextLabel(name);

    element.setMCenterX(mx);
    element.setMCenterY(my);
    element.setRotation(0);
    element.setInitialSize();
    int fontSize = (int)(element.getMFontSize());
    int width = (int)(fontSize * element.getTextLabel().length() * 0.75);
    if (element.getMWidth() < width) {
      element.setMWidth(width);
    }

    // this will add the element to the pathway AND initialize all dynamic properties
    addElement(element, p);
    if (!Utils.isEmpty(accId)) {
      DynamicProperty.PGKB_ID.set(element, accId);
    }

    return new PathwayElement[] { element };
  }

  public void postInsert(PathwayElement[] newElements) {

    VPathway vPathway = m_desktop.getSwingEngine().getEngine().getActiveVPathway();
    // set (x, y) coordinates to greater than VPathway.MIN_DRAG_LENGTH so it doesn't reset size
    vPathway.mouseUp(new SwingMouseEvent(new MouseEvent((VPathwaySwing)vPathway.getWrapper(),
        MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 10, 10, 1, false)));
  }
}
