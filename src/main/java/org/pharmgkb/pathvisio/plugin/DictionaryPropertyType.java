package org.pharmgkb.pathvisio.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pathvisio.core.model.PropertyType;

/**
 * This class implements PropertyType for a dictionary.
 *
 * @author Mark Woon
 */
public class DictionaryPropertyType implements PropertyType {
  public static final String BIOLOGICAL_INTERMEDIATE_DICTIONARY_ID = "pgkb.dict.biologicalIntermediate";
  public static final String CL_DICTIONARY_ID = "pgkb.dict.cl";
  public static final String CELLULAR_LOCATION_DICTIONARY_ID = "pgkb.dict.cellularLocation";
  public static final String PHENOTYPE_DICTIONARY_ID = "pgkb.dict.phenotype";
  public static final String CHEMICAL_DICTIONARY_ID = "pgkb.dict.chemical";
  public static final String DRUG_DICTIONARY_ID = "pgkb.dict.drug";
  public static final String DRUG_CLASS_DICTIONARY_ID = "pgkb.dict.drugClass";
  public static final String GENE_DICTIONARY_ID = "pgkb.dict.gene";
  public static final String HAPLOTYPE_DICTIONARY_ID = "pgkb.dict.haplotype";
  public static final String PATHWAY_DICTIONARY_ID = "pgkb.dict.pathway";
  public static final String PHYSICAL_ENTITY_DICTIONARY_ID = "pgkb.dict.physicalEntity";
  public static final String PROCESS_DICTIONARY_ID = "pgkb.dict.process";
  private static final Splitter sf_commaSplitter = Splitter.on(",").trimResults().omitEmptyStrings();
  private String m_id;
  private SortedMap<String, String> m_idNameMap = new TreeMap<>();
  /** Maps lower-cased name to ID */
  private SortedMap<String, String> m_nameIdMap = new TreeMap<>();
  private Set<String> m_duplicateNames = new HashSet<>();


  public DictionaryPropertyType(String id) {
    m_id = id;
  }

  public String getId() {
    return m_id;
  }


  /**
   * Gets the map of ID's to names.
   */
  public SortedMap<String, String> getEntries() {
    return m_idNameMap;
  }

  /**
   * Gets the map of lower-cased names to IDs.
   */
  public SortedMap<String, String> getReverseEnteries() {
    return m_nameIdMap;
  }

  public void addEntry(String id, String name) {

    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(name);
    if (m_idNameMap.containsKey(id)) {
      return;
    }
    String lcName = name.toLowerCase();
    if (m_duplicateNames.contains(lcName)) {
      name = name + " (" + id + ")";
      lcName = lcName  + " (" + id + ")";

    } else if (m_nameIdMap.containsKey(lcName)) {
      m_duplicateNames.add(lcName);
      // update previous entry
      String oldId = m_nameIdMap.get(lcName);
      String oldName = m_idNameMap.get(oldId);

      m_nameIdMap.remove(lcName);
      m_nameIdMap.put(lcName + " (" + oldId + ")", oldId);
      m_idNameMap.put(oldId, oldName  + " (" + oldId + ")");

      name = name + " (" + id + ")";
      lcName = lcName  + " (" + id + ")";
    }
    m_idNameMap.put(id, name);
    m_nameIdMap.put(lcName, id);
  }


  /**
   * Reads .tsv files.
   * Expects 2 columns: ID and Name.
   * Assumes first line is header and skips it.
   */
  public void readTsv(File dataFile, @Nullable String format, @Nullable String... filterValue) throws PgkbPluginException {

    Set<String> filters = null;
    if (filterValue != null && filterValue.length > 0) {
      filters = ImmutableSet.copyOf(filterValue);
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
      // skip header
      reader.readLine();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] data = line.split("\t");
        if (filters != null && filters.size() > 0) {
          if (data.length != 3) {
            throw new PgkbPluginException("Expecting 3 columns, got [" + line + "], in " + dataFile +
                ", filtering for " + filters);
          }
          List<String> types = sf_commaSplitter.splitToList(data[2]);
          if (Collections.disjoint(types, filters)) {
            continue;
          }
        }
        String text = data[1];
        if (format != null) {
          text = "<" + format + ">" + text + "</" + format + ">";
        }
        addEntry(data[0], text);
      }

    } catch (IOException ex) {
      throw new PgkbPluginException("Error parsing tsv file '" + dataFile + "'", ex);
    }
  }


  @Override
  public String toString() {
    return "DictionaryType:" + m_id;
  }
}
