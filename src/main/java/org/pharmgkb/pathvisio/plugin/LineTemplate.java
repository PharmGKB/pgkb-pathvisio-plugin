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
