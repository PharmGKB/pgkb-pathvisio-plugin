/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.DefaultTemplates;
import org.pharmgkb.pathvisio.BiopaxInteractionType;


/**
 * This template creates a line.
 *
 * @author Mark Woon
 */
public class LineTemplate extends DefaultTemplates.LineTemplate {
  private BiopaxInteractionType m_interactionType;


  public LineTemplate(BiopaxInteractionType type) {
    super(type.getDisplayName().toLowerCase(), type.getLineStyle(), type.getStartLineStyle(), type.getEndLineStyle(),
        ConnectorType.STRAIGHT);
    m_interactionType = type;
  }


  @Override
  public PathwayElement[] addElements(Pathway p, double mx, double my) {

    PathwayElement[] elems = super.addElements(p, mx, my);
    for (PathwayElement elem : elems) {
      elem.setDynamicProperty(BiopaxInteractionType.getProperty().getId(), m_interactionType.getDisplayName());
      elem.setColor(m_interactionType.getColor());
    }
    return elems;
  }

}
