/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

/**
 * @author Mark Woon
 */
public class PgkbPluginException extends Exception {

  public PgkbPluginException(String msg) {
    super(msg);
  }

  public PgkbPluginException(String msg, Exception ex) {
    super(msg, ex);
  }
}
