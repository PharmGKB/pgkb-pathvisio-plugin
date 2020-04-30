package org.pharmgkb.pathvisio.plugin;

/**
 * Exception for network problems.
 *
 * @author Mark Woon
 */
public class NetworkException extends PgkbPluginException {


  public NetworkException(String msg) {
    super(msg);
  }

  public NetworkException(String msg, Exception ex) {
    super(msg, ex);
  }
}
