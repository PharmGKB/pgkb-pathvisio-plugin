package org.pharmgkb.pathvisio.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test for {@link DownloadUtils}.
 *
 * @author Mark Woon
 */
class DownloadUtilsTest {

  @Test
  void testIsNewVersionWhenDifferent() {
    assertTrue(DownloadUtils.isNewVersion("v1.0.1", "v1.0.0"));
  }

  @Test
  void testIsNewVersionWhenSame() {
    assertFalse(DownloadUtils.isNewVersion("v1.0.0", "v1.0.0"));
  }
}
