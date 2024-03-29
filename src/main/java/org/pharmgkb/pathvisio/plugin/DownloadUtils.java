package org.pharmgkb.pathvisio.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pharmgkb.common.util.StreamUtils;


/**
 * Utility methods for working with downloads.
 *
 * @author Mark Woon
 */
public class DownloadUtils {
  private static final DateTimeFormatter sf_timestampFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz");


  public static void downloadAndUnpackDataFile() throws PgkbPluginException {
    String url = "https://api.pharmgkb.org/v1/download/file/data/pathvisio.zip?ref=pathvisio";
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
      StreamUtils.copyUrlToFile(url, dataFile);
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
    return getLatestVersion().isAfter(getThisVersion());
  }


  private static Instant getThisVersion() throws IOException {

    InputStream in = PgkbPlugin.class.getResourceAsStream("/org/pharmgkb/pathvisio/plugin/timestamp.txt");
    if (in == null) {
      throw new IOException("Cannot find timestamp.txt");
    }
    try (Reader reader = new InputStreamReader(in);
         StringWriter curVersionWriter = new StringWriter()) {
      IOUtils.copy(reader, curVersionWriter);
      return ZonedDateTime.parse(curVersionWriter.toString(), sf_timestampFormatter).toInstant();
    }
  }

  private static Instant getLatestVersion() throws IOException, NetworkException {

    // download timestamp
    URL url = new URL("https://drive.google.com/uc?export=download&id=1qhAvUJhAEYJgqAFUSc0BLf80Bt28QH1z");
    Path versionFile = GlobalPreference.getDataDir().toPath().resolve("pgkb-pathvisio.timestamp.txt");
    try {
      IOUtils.copy(url, versionFile.toFile());
    } catch (UnknownHostException ex) {
      throw new NetworkException("No network?  Skipping version check", ex);
    }
    // read and parse
    try (Reader reader = Files.newBufferedReader(versionFile);
         StringWriter curVersionWriter = new StringWriter()) {
      IOUtils.copy(reader, curVersionWriter);
      return ZonedDateTime.parse(curVersionWriter.toString(), sf_timestampFormatter).toInstant();
    }
  }
}
