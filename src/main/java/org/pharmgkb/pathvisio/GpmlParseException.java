package org.pharmgkb.pathvisio;

import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pharmgkb.exception.PgkbRuntimeException;


/**
 * This exception gets thrown when there's a problem while parsing GPML.
 *
 * @author Mark Woon
 */
public class GpmlParseException extends PgkbRuntimeException {
  private @Nullable PathwayElement m_pathwayElement;
  private final String m_message;
  private final boolean m_isFatal;


  /**
   * Creates a fatal parse exception (indicating that the user cannot fix the errors).
   */
  public GpmlParseException(String msg) {
    super(buildMessage(null, msg, true));
    m_message = msg;
    m_isFatal = true;
  }

  /**
   * Creates a fatal parse exception (indicating that the user cannot fix the errors).
   */
  public GpmlParseException(PathwayElement pvElem, String msg) {
    super(buildMessage(pvElem, msg, true));
    m_pathwayElement = pvElem;
    m_message = msg;
    m_isFatal = true;
  }


  public GpmlParseException(PathwayElement pvElem, String msg, boolean isFatal) {
    super(buildMessage(pvElem, msg, isFatal));
    m_pathwayElement = pvElem;
    m_message = msg;
    m_isFatal = isFatal;
  }

  private static String buildMessage(@Nullable PathwayElement pvElem, String msg, boolean isFatal) {
    StringBuilder builder = new StringBuilder();
    if (pvElem != null) {
      if (pvElem.getObjectType() == ObjectType.LINE) {
        builder.append(PathvisioUtils.buildLineNameReference(pvElem));
      } else {
        builder.append(PathvisioUtils.buildElementNameReference(pvElem));
      }
      builder.append(": ");
    }
    builder.append(msg);
    if (!msg.endsWith(".")) {
      builder.append(".");
    }
    if (isFatal) {
      builder.append(" (CONTACT DEV!)");
    }
    return builder.toString();
  }


  public @Nullable PathwayElement getPathwayElement() {
    return m_pathwayElement;
  }

  public String getBaseMessage() {
    return m_message;
  }

  public boolean isFatal() {
    return m_isFatal;
  }
}
