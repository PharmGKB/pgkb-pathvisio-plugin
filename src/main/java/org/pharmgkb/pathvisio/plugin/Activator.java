package org.pharmgkb.pathvisio.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;


/**
 * This is the OSGI {@link BundleActivator} for the PharmGKB PathVisio plugin.
 *
 * @author Mark Woon
 */
public class Activator implements BundleActivator {
  private PgkbPlugin m_plugin;

  @Override
  public void start(BundleContext context) throws Exception {
    m_plugin = new PgkbPlugin();
    context.registerService(Plugin.class.getName(), m_plugin, null);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin.done();
  }
}
