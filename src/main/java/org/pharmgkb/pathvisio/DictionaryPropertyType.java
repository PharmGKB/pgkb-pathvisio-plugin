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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.pathvisio.model.PropertyType;
import org.pharmgkb.exception.PgkbException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class implements PropertyType for a dictionary.
 *
 * @author Mark Woon
 */
public class DictionaryPropertyType implements PropertyType {
	public static final String GENE_DICTIONARY_ID = "pgkb.geneDictionary";
	public static final String DRUG_DICTIONARY_ID = "pgkb.drugDictionary";
	public static final String DRUG_ONLY_DICTIONARY_ID = "pgkb.drugOnlyDictionary";
	public static final String DRUG_CLASS_DICTIONARY_ID = "pgkb.drugClassDictionary";
	public static final String DISEASE_DICTIONARY_ID = "pgkb.diseaseDictionary";
	public static final String ATC_DICTIONARY_ID = "pgkb.atcDictionary";
	public static final String CL_DICTIONARY_ID = "pgkb.clDictionary";
	public static final String PGKB_PATHWAY_DICTIONARY_ID = "pgkb.pgkbPathwayDictionary";
	public static final String PGKB_HAPLOTYPE_DICTIONARY_ID = "pgkb.pgkbHaplotypeDictionary";
	private String m_id;
	private SortedMap<String, String> m_idNameMap = new TreeMap<String, String>();
	private SortedMap<String, String> m_nameIdMap = new TreeMap<String, String>();
	private Set<String> m_duplicateNames = new HashSet<String>();


	public DictionaryPropertyType(String id) {
		m_id = id;
	}

	public String getId() {
		return m_id;
	}


	public SortedMap<String, String> getEntries() {
		return m_idNameMap;
	}

	public SortedMap<String, String> getReverseEnteries() {
		return m_nameIdMap;
	}

	public void addEntry(String id, String name) {

		if (id != null && name != null) {
			if (m_duplicateNames.contains(name)) {
				name = name + " (" + id + ")";
			} else if (m_nameIdMap.containsKey(name)) {
				m_duplicateNames.add(name);
				// update previous entry
				String oldId = m_nameIdMap.get(name);
				String oldName = name + " (" + oldId + ")";
				m_nameIdMap.remove(name);
				m_nameIdMap.put(oldName, oldId);
				m_idNameMap.put(oldId, oldName);
				name = name + " (" + id + ")";
			}
			m_idNameMap.put(id, name);
			m_nameIdMap.put(name, id);
		}
	}


	/**
	 * Given an XML file, parse out the entries.
	 */
	public void readXml(File file) throws PgkbException {

		if (file != null && file.length() > 0) {
			InputStream xmlStream = null;
			try {
				xmlStream = new FileInputStream(file);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);

				NodeList roots = doc.getElementsByTagName("dictionary");
				if (roots.getLength() == 0) {
					throw new PgkbException("No dictionary root found");
				}
				if (roots.getLength() > 1) {
					throw new PgkbException("More than one dictionary roots found");
				}
				Element rootElement = (Element)roots.item(0);
				NodeList entryNL = rootElement.getElementsByTagName("entry");
				for (int j = 0; j < entryNL.getLength(); j++) {
					Element entryElem = (Element)entryNL.item(j);
					String id = entryElem.getAttribute("id");
					String name = entryElem.getAttribute("name");
					if (org.pathvisio.util.Utils.isEmpty(name)) {
						name = id;
					}
					addEntry(id, name);
				}
			} catch (Exception ex) {
				throw new PgkbException("Error parsing XML file '" + file + "'");
			} finally {
				if (xmlStream != null) {
					try {
						xmlStream.close();
					} catch (IOException ex) { /* ignore */ }
				}
			}
		}
	}

	/**
	 * Reads .tsv files.
	 * Expects 2 columns: ID and Name.
	 * Assumes first line is header and skips it.
	 */
	public void readTsv(File dataFile, String filterValue) throws PgkbException {

		try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
			// skip header
			reader.readLine();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split("\t");
				if (filterValue != null) {
          if (data.length != 3){
            System.out.println("grr");
          }
					if (!data[2].equals(filterValue)) {
						continue;
					}
				}
				addEntry(data[0], data[1]);
			}

		} catch (IOException ex) {
			throw new PgkbException("Error parsing tsv file '" + dataFile + "'", ex);
		}
	}
}
