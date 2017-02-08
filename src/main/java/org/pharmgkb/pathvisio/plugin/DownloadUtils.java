/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import java.io.File;
import java.io.FileOutputStream;
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
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Request;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pharmgkb.util.StreamUtils;


/**
 * Utility methods for working with downloads.
 *
 * @author Mark Woon
 */
public class DownloadUtils {
  private static DateTimeFormatter sf_timestampFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz");


  public static void downloadAndUnpackDataFile() throws PgkbPluginException {

    String url = "https://s3.pgkb.org/data/pathvisio.zip";
    // download and unzip data file
    System.out.println("Checking " + url);
    Path downloadedFile = downloadFromUrl(url, "pathvisio.zip");
    if (downloadedFile == null) {
      return;
    }
    System.out.println("  done.");
    File dataFile = null;
    try {
      ZipFile zipFile = new ZipFile(downloadedFile.toFile());
      Enumeration entries = zipFile.entries();
      int fileCount = 0;
      while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();
        if (entry.isDirectory() || entry.getName().startsWith("CREATED_")) {
          continue;
        }
        // unpack file
        dataFile = new File(GlobalPreference.getDataDir(), entry.getName());
        try (InputStream in = zipFile.getInputStream(entry);
             OutputStream out = new FileOutputStream(dataFile)) {
          StreamUtils.copy(in, out);
        }
        fileCount += 1;
      }
      if (fileCount == 0) {
        throw new PgkbPluginException("Empty zip file '" + url + "'");
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



  /**
   * Saves a URL as a local file.  If file already exists locally, it will only be downloaded again if it has been
   * modified on the server.
   */
  private static @Nullable Path downloadFromUrl(String urlValue, String fileName) throws PgkbPluginException {

    Path dataFile = GlobalPreference.getDataDir().toPath().resolve(fileName);
    try {
      Request.Get(urlValue).execute()
          .saveContent(dataFile.toFile());
      return dataFile;
    } catch (UnknownHostException ex) {
      if (Files.exists(dataFile)) {
        throw new NetworkException("No internet?\n\nCould not download " + fileName +
            " from preview.  Using existing older version.\n\nProceed at your own risk.");
      } else {
        throw new PgkbPluginException("Could not download " + fileName + " from preview", ex);
      }

    } catch (Exception ex) {
      throw new PgkbPluginException("Error processing '" + urlValue + "'", ex);
    }
  }



  public static boolean hasNewVersion() throws IOException, NetworkException {
    return getLatestVersion().isAfter(getThisVersion());
  }


  private static Instant getThisVersion() throws IOException {

    try (Reader reader = new InputStreamReader(PgkbPlugin.class.getResourceAsStream("timestamp.txt"));
         StringWriter curVersionWriter = new StringWriter()) {
      StreamUtils.copy(reader, curVersionWriter);
      return ZonedDateTime.parse(curVersionWriter.toString(), sf_timestampFormatter).toInstant();
    }
  }

  private static Instant getLatestVersion() throws IOException, NetworkException {

    // download timestamp
    URL url = new URL("https://stanford.box.com/shared/static/l98dyxkmwciukz2c76rmoml8qai5rubw.txt");
    Path versionFile = GlobalPreference.getDataDir().toPath().resolve("pgkb-pathvisio.timestamp.txt");
    try {
      FileUtils.copyURLToFile(url, versionFile.toFile());
    } catch (UnknownHostException ex) {
      throw new NetworkException("No network?  Skipping version check", ex);
    }
    // read and parse
    try (Reader reader = Files.newBufferedReader(versionFile);
         StringWriter curVersionWriter = new StringWriter()) {
      StreamUtils.copy(reader, curVersionWriter);
      return ZonedDateTime.parse(curVersionWriter.toString(), sf_timestampFormatter).toInstant();
    }
  }
}
