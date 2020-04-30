package org.pharmgkb.pathvisio.plugin;

import org.pathvisio.core.model.PathwayElement;

/**
 * This event is thrown by ObjectPropertyManager.
 *
 * @author Mark Woon
 */
public class ObjectPropertyEvent {
  public static final int PATHWAY_NEW = 1;
  public static final int PATHWAY_OPENED = 2;
  public static final int ELEMENT_ADDED = 3;
  public static final int ELEMENT_MODIFIED = 4;

  private int m_type;
  private PathwayElement m_element;

  ObjectPropertyEvent(int type, PathwayElement elem) {
    m_type = type;
    m_element = elem;
  }


  public int getType() {
    return m_type;
  }

  public PathwayElement getElement() {
    return m_element;
  }
}
