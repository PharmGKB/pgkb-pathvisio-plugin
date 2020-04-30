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
