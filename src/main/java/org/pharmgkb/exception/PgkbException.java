package org.pharmgkb.exception;

/**
 * PgkbException is the generic exception for ClinPGx code.
 *
 * @author Mark Woon
 */
public class PgkbException extends Exception {
  private boolean m_isFatal = true;


  /**
   * Constructs a PgkbException with no detail message.
   */
  public PgkbException() {
    super();
  }


  /**
   * Constructs a PgkbException with the specified detail message.
   */
  public PgkbException(String s) {
    super(s);
  }

  /**
   * Constructs a PgkbException with the specified detail message.
   */
  public PgkbException(String s, boolean isFatal) {
    super(s);
    m_isFatal = isFatal;
  }

  /**
   * Constructs a PgkbException with the detail message specified by the exception.
   */
  public PgkbException(Throwable cause) {
    super(cause);
  }


  /**
   * Constructs a PgkbException with the specified detail message and backs it up with additional data from another
   * Throwable.
   */
  public PgkbException(String s, Throwable cause) {
    super(s, cause);
  }


  /**
   * Constructs a PgkbException with the specified detail message and
   * backs it up with additional data from another Throwable.
   */
  public PgkbException(String s, Throwable cause, boolean isFatal) {
    super(s, cause);
    m_isFatal = isFatal;
  }


  /**
   * Gets whether this exception is fatal.
   */
  public boolean isFatal() {
    return m_isFatal;
  }
}
