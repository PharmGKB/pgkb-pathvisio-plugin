package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import javax.annotation.Nonnull;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import com.google.common.base.Preconditions;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.desktop.PvDesktop;


/**
 * This builder creates and displays pop-up dialogs.
 *
 * @author Mark Woon
 */
public class PopupDialogBuilder {
  private Component m_parent;
  private int m_type = JOptionPane.INFORMATION_MESSAGE;
  private String m_title = "Info";
  private String m_message;
  private boolean m_isHtml;


  public PopupDialogBuilder(@Nonnull PvDesktop desktop) {
    m_parent = desktop.getFrame();
  }


  public PopupDialogBuilder title(@Nonnull String title) {
    Preconditions.checkNotNull(title);
    m_title = title;
    return this;
  }

  public PopupDialogBuilder info() {
    m_type = JOptionPane.INFORMATION_MESSAGE;
    if (m_title == null) {
      m_title = "Info";
    }
    return this;
  }

  public PopupDialogBuilder warn() {
    m_type = JOptionPane.WARNING_MESSAGE;
    if (m_title == null) {
      m_title = "Warning";
    }
    return this;
  }

  public PopupDialogBuilder error() {
    m_type = JOptionPane.ERROR_MESSAGE;
    if (m_title == null) {
      m_title = "Error";
    }
    return this;
  }

  public PopupDialogBuilder message(@Nonnull String message) {
    Preconditions.checkNotNull(message);
    Preconditions.checkState(m_message == null, "Already provided message");
    m_message = message;
    return this;
  }

  public PopupDialogBuilder htmlMessage(@Nonnull String message) {
    Preconditions.checkNotNull(message);
    Preconditions.checkState(m_message == null, "Already provided message");
    m_message = message;
    m_isHtml = true;
    return this;
  }

  public void show() {

    Object display;
    if (!m_isHtml) {
      display = m_message;
    } else {
      JLabel label = new JLabel();
      // create some css from the label's font
      Font font = label.getFont();
      StringBuilder style = new StringBuilder()
          .append("font-family:")
          .append(font.getFamily())
          .append(";")
          .append("font-weight:")
          .append(font.isBold() ? "bold" : "normal")
          .append(";")
          .append("font-size:")
          .append(font.getSize())
          .append("pt;");

      JEditorPane editorPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + m_message + "</body></html>");

      editorPane.addHyperlinkListener(e -> {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
              Logger.log.error("Error opening link to " + e.getURL());
            }
          } else {
            Logger.log.error("Error opening link to " + e.getURL() + " (unsupported desktop)");
          }
        }
      });
      editorPane.setEditable(false);
      editorPane.setBackground(label.getBackground());
      display = editorPane;
    }

    JOptionPane.showMessageDialog(m_parent, display, m_title, m_type);
  }
}
