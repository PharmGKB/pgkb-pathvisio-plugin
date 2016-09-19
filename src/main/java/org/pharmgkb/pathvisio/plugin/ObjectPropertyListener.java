/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

/**
 * This listener interface alerts implementors of significant events in ObjectPropertyManager.
 * <p>
 * If you want to be notified of changes to the pathway or elements <i>after</i> they've been updated by the
 * ObjectPropertyManager, you should implement this instead of {@link org.pathvisio.core.model.PathwayListener} and
 * {@link org.pathvisio.core.model.PathwayElementListener}, because there's no way to guarantee that your listener will
 * be notified after ObjectPropertyManager instead of before it.
 *
 * @author Mark Woon
 */
public interface ObjectPropertyListener {

  void objectModified(ObjectPropertyEvent event);
}
