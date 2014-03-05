/*
 ----- BEGIN LICENSE BLOCK -----
 Version: MPL 1.1/GPL 2.0/LGPL 2.1

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with the
 License. You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is PharmGKB.

 The Initial Developer of the Original Code is PharmGKB (The Pharmacogenetics
 and Pharmacogenetics Knowledge Base, supported by NIH U01GM61374). Portions
 created by the Initial Developer are Copyright (C) 2014 the Initial Developer.
 All Rights Reserved.

 Contributor(s):

 Alternatively, the contents of this file may be used under the terms of
 either the GNU General Public License Version 2 or later (the "GPL"), or the
 GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in
 which case the provisions of the GPL or the LGPL are applicable instead of
 those above. If you wish to allow use of your version of this file only
 under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your
 decision by deleting the provisions above and replace them with the notice
 and other provisions required by the GPL or the LGPL. If you do not delete
 the provisions above, a recipient may use your version of this file under
 the terms of any one of the MPL, the GPL or the LGPL.

 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.pathvisio.debug.Logger;
import org.pathvisio.preferences.GlobalPreference;
import org.pharmgkb.exception.PgkbException;


/**
 * @author Mark Woon
 */
public class IoUtils {


	/**
	 * Saves a URL as a local file.  If file already exists locally, it will only be downloaded again if it has been
	 * modified on the server.
	 */
	public static File downloadFromUrl(String urlValue, String fileName) throws PgkbException {

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
			throw new PgkbException("Error processing '" + urlValue + "'", ex);
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
