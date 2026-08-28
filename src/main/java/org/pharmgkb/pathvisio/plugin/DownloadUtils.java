package org.pharmgkb.pathvisio.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;


/**
 * Utility methods for working with downloads.
 *
 * @author Mark Woon
 */
public class DownloadUtils {
  private static final String GITHUB_LATEST_RELEASE_URL =
      "https://api.github.com/repos/PharmGKB/pgkb-pathvisio-plugin/releases/latest";


  public static void downloadAndUnpackDataFile() throws PgkbPluginException {
    String url = "https://api.clinpgx.org/v1/download/file/data/pathvisio.zip?ref=pathvisio";
    Path downloadedFile = downloadFromUrl(url, "pathvisio.zip");
    unzipFile(downloadedFile);
  }


  /**
   * Saves a URL as a local file.  If the file already exists locally, it will only be downloaded again if it has been
   * modified on the server.
   */
  private static Path downloadFromUrl(String url, String fileName) throws PgkbPluginException {

    System.out.println("Checking " + url);
    Path dataFile = GlobalPreference.getDataDir().toPath().resolve(fileName);
    try {
      copyUrlToFile(url, dataFile);
      return dataFile;
    } catch (UnknownHostException ex) {
      if (Files.exists(dataFile)) {
        throw new NetworkException("No internet?\n\nCould not download " + fileName +
            ".  Using existing older version.\n\nProceed at your own risk.");
      } else {
        throw new PgkbPluginException("Could not download " + fileName, ex);
      }
    } catch (Exception ex) {
      throw new PgkbPluginException("Error processing '" + url + "'", ex);
    }
  }

  /**
   * Copies contents of a {@code url} to a {@code file}.  If {@code file} already exists, it will be overwritten.
   * <p>
   * Use this instead of {@link IOUtils#copy(URL, File)} when you need to follow redirects.
   */
  private static void copyUrlToFile(String url, Path file) throws IOException {

    if (url.startsWith("http://") || url.startsWith("https://")) {
      try (CloseableHttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()) {
        HttpGet httpget = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpget)) {
          // save to file even if there's an error, so we can see what the error is
          try (InputStream in = response.getEntity().getContent();
               OutputStream out = Files.newOutputStream(file)) {
            IOUtils.copy(in, out);
          }
          if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Error downloading " + url + ": " + response.getStatusLine());
          }
        }
      }
    } else {
      URL ftpUrl = new URL(url);
      URLConnection conn = ftpUrl.openConnection();
      try (InputStream in = conn.getInputStream();
           OutputStream out = Files.newOutputStream(file)) {
        IOUtils.copy(in, out);
      }
    }
  }


  private static void unzipFile(Path downloadedFile) throws PgkbPluginException {
    System.out.println("Unpacking " + downloadedFile);
    File dataFile = null;
    try (ZipFile zipFile = new ZipFile(downloadedFile.toFile())) {
      Enumeration entries = zipFile.entries();
      int fileCount = 0;
      while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();
        if (entry.isDirectory() || entry.getName().startsWith("CREATED_") ||
            entry.getName().startsWith("LICENSE.txt")) {
          continue;
        }
        // unpack file
        dataFile = new File(GlobalPreference.getDataDir(), entry.getName());
        try (InputStream in = zipFile.getInputStream(entry);
             OutputStream out = Files.newOutputStream(dataFile.toPath())) {
          IOUtils.copy(in, out);
        }
        fileCount += 1;
      }
      if (fileCount == 0) {
        throw new PgkbPluginException("Empty zip file '" + downloadedFile.getName(downloadedFile.getNameCount() - 1) + "'");
      }

    } catch (ZipException ex) {
      if (dataFile != null && !dataFile.delete()) {
        Logger.log.warn("Error deleting " + dataFile.getAbsolutePath());
      }
      throw new PgkbPluginException("Error opening zip file", ex);
    } catch (IOException ex) {
      throw new PgkbPluginException("Error unzipping data", ex);
    }
  }


  public static boolean hasNewVersion() throws IOException, NetworkException {
    return hasNewVersion(GITHUB_LATEST_RELEASE_URL);
  }

  static boolean hasNewVersion(String releaseUrl) throws IOException, NetworkException {
    String latestTag;
    try {
      latestTag = fetchLatestReleaseTag(releaseUrl);
    } catch (UnknownHostException ex) {
      throw new NetworkException("No network? Skipping version check.");
    }
    if (latestTag == null) {
      // no release published yet (e.g. before this repo's first release) - nothing to compare against
      return false;
    }
    return isNewVersion(latestTag, getThisVersion());
  }

  /**
   * Compares the latest published release tag to this build's own embedded version.
   */
  static boolean isNewVersion(String latestTag, String currentVersion) {
    int idx = currentVersion.indexOf("-");
    if (idx != -1) {
      // remove dev version suffix
      currentVersion = currentVersion.substring(0, idx);
    }
    return !latestTag.equals(currentVersion);
  }


  private static String getThisVersion() throws IOException {

    InputStream in = PgkbPlugin.class.getResourceAsStream("/org/pharmgkb/pathvisio/plugin/version.txt");
    if (in == null) {
      throw new IOException("Cannot find version.txt");
    }
    try (Reader reader = new InputStreamReader(in);
         StringWriter versionWriter = new StringWriter()) {
      IOUtils.copy(reader, versionWriter);
      return versionWriter.toString().trim();
    }
  }

  /**
   * Fetches the latest release's tag name from GitHub, or {@code null} if no release has been published yet.
   */
  private static @Nullable String fetchLatestReleaseTag(String url) throws IOException {
    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
      HttpGet httpGet = new HttpGet(url);
      httpGet.setHeader("Accept", "application/vnd.github+json");
      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        int status = response.getStatusLine().getStatusCode();
        if (status == 404) {
          return null;
        }
        if (status != 200) {
          throw new IOException("Error fetching latest release: " + response.getStatusLine());
        }
        String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonObject release = JsonParser.parseString(body).getAsJsonObject();
        if (!release.has("tag_name")) {
          throw new IOException("Malformed release response: missing 'tag_name'");
        }
        return release.get("tag_name").getAsString();
      }
    }
  }
}
