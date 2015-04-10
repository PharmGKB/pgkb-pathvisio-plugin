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
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.annotation.Nullable;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;


/**
 * @author Mark Woon
 */
public class IoUtils {


	/**
	 * Saves a URL as a local file.  If file already exists locally, it will only be downloaded again if it has been
	 * modified on the server.
	 */
	public static @Nullable File downloadFromUrl(String urlValue, String fileName) throws PgkbPluginException {

		try {
			URL url = new URL(urlValue);
			URLConnection conn = url.openConnection();

			File dataFile = new File(GlobalPreference.getDataDir(), fileName);
			if (dataFile.exists()) {
				if (conn.getLastModified() == 0 || dataFile.lastModified() < conn.getLastModified()) {
					// update file
					System.out.println("  downloading update");
					doDownload(conn, dataFile);
					return dataFile;
				} else {
					return null;
				}
			} else {
				System.out.println("  downloading ");
				doDownload(conn, dataFile);
				return dataFile;
			}

		} catch (Exception ex) {
			throw new PgkbPluginException("Error processing '" + urlValue + "'", ex);
		}
	}

	private static void doDownload(URLConnection conn, File file) throws IOException {

		if (file.exists()) {
			if (!file.delete()) {
				Logger.log.error("Couldn't delete " + file);
			}
		}
		System.out.println("  saving to " + file.getAbsolutePath());
		copyInputStream(conn.getInputStream(), new FileOutputStream(file));
	}


	/**
	 * Copies InputStream to OutputStream.  Will close streams when finished copying.
	 */
	public static void copyInputStream(InputStream in, OutputStream out) throws IOException {

		try {
			byte[] buf = new byte[1024];
			int read;
			while ((read = in.read(buf)) != -1) {
				out.write(buf, 0, read);
			}
		} finally {
			try {
				out.close();
			} catch (IOException ex) { /* ignore */ }
			try {
				in.close();
			} catch (IOException ex) { /* ignore */ }
		}
	}
}
