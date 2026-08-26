package org.pharmgkb.exception;


import org.jspecify.annotations.Nullable;


/**
 * This is a generic runtime exception for PharmGKB code.
 *
 * @author Mark Woon
 */
public class PgkbRuntimeException extends RuntimeException {
  private @Nullable String m_data;
  private boolean m_fatal;


  public PgkbRuntimeException(String msg) {
    super(msg);
  }

  public PgkbRuntimeException(Exception ex, boolean isFatal) {
    super(ex);
    m_fatal = isFatal;
  }

  public PgkbRuntimeException(String msg, Exception ex) {
    super(msg, ex);
  }

  public PgkbRuntimeException(String msg, Exception ex, boolean isFatal) {
    super(msg, ex);
    m_fatal = isFatal;
  }


  /**
   * Gets whether this exception is fatal.
   */
  public boolean isFatal() {
    return m_fatal;
  }

  public @Nullable String getData() {
    return m_data;
  }

  public void setData(@Nullable String data) {
    m_data = data;
  }
}
