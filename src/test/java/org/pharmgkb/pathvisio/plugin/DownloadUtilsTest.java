package org.pharmgkb.pathvisio.plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  void testHasNewVersionWhenOffline() {
    NetworkException ex = assertThrows(NetworkException.class,
        () -> DownloadUtils.hasNewVersion("http://nonexistent.invalid/"));
    assertEquals("No network? Skipping version check.", ex.getMessage());
  }

  @Test
  void testHasNewVersionWhenNoReleasePublished() throws Exception {
    HttpServer server = startServer(exchange -> {
      exchange.sendResponseHeaders(404, -1);
      exchange.close();
    });
    try {
      assertFalse(DownloadUtils.hasNewVersion(url(server)));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testHasNewVersionWhenReleaseMissingTagName() throws Exception {
    HttpServer server = startServer(exchange -> {
      byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    try {
      IOException ex = assertThrows(IOException.class, () -> DownloadUtils.hasNewVersion(url(server)));
      assertTrue(ex.getMessage().contains("tag_name"));
    } finally {
      server.stop(0);
    }
  }


  private static HttpServer startServer(HttpHandler handler) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext("/", handler);
    server.start();
    return server;
  }

  private static String url(HttpServer server) {
    return "http://localhost:" + server.getAddress().getPort() + "/";
  }
}
