/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
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
