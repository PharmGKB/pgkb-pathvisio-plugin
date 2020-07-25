package org.pharmgkb.pathvisio.plugin.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import com.jidesoft.icons.IconsFactory;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.dialogs.PublicationXRefDialog;
import org.pharmgkb.pathvisio.plugin.PgkbPlugin;


/**
 * Action to add a literature reference to multiple elements add once.
 *
 * @author Mark Woon
 */
public class AddLiteratureAction extends AbstractAction {
  private final SwingEngine m_engine;

  public AddLiteratureAction(SwingEngine engine) {

    m_engine = engine;
    putValue(NAME, "Add literature");
    putValue(SHORT_DESCRIPTION, "Add literature reference to selected elements");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_J);

    ImageIcon icon = IconsFactory.getImageIcon(PgkbPlugin.class, "page_add.png");
    if (icon != null) {
      putValue(Action.SMALL_ICON, icon);
      putValue(Action.LARGE_ICON_KEY, icon);
    }
  }


  public void actionPerformed(ActionEvent e) {

    List<Graphics> elems = m_engine.getEngine().getActiveVPathway().getSelectedGraphics();
    if (elems.size() == 0) {
      JOptionPane.showMessageDialog(m_engine.getFrame(),
          "Must select something before adding literature.",
          "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    PublicationXref xref = new PublicationXref();
    PublicationXRefDialog d = new PublicationXRefDialog(xref, m_engine.getFrame(), m_engine.getApplicationPanel());
    d.setVisible(true);
    if(d.getExitCode().equals(PublicationXRefDialog.OK)) {
      for (Graphics elem : elems) {
        elem.getPathwayElement().getBiopaxReferenceManager().addElementReference(xref);
      }
    }
  }
}
