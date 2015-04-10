/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

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
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.util.Utils;
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
	public void readXml(File file) throws PgkbPluginException {

		if (file != null && file.length() > 0) {
			InputStream xmlStream = null;
			try {
				xmlStream = new FileInputStream(file);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);

				NodeList roots = doc.getElementsByTagName("dictionary");
				if (roots.getLength() == 0) {
					throw new PgkbPluginException("No dictionary root found");
				}
				if (roots.getLength() > 1) {
					throw new PgkbPluginException("More than one dictionary roots found");
				}
				Element rootElement = (Element)roots.item(0);
				NodeList entryNL = rootElement.getElementsByTagName("entry");
				for (int j = 0; j < entryNL.getLength(); j++) {
					Element entryElem = (Element)entryNL.item(j);
					String id = entryElem.getAttribute("id");
					String name = entryElem.getAttribute("name");
					if (Utils.isEmpty(name)) {
						name = id;
					}
					addEntry(id, name);
				}
			} catch (Exception ex) {
				throw new PgkbPluginException("Error parsing XML file '" + file + "'");
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
	public void readTsv(File dataFile, String filterValue) throws PgkbPluginException {

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
			throw new PgkbPluginException("Error parsing tsv file '" + dataFile + "'", ex);
		}
	}
}
