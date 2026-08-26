package org.pharmgkb.model;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;
import org.pharmgkb.common.util.UrlUtils;


/**
 * This is an enumeration of resources we get data from.
 *
 * <p>This is a trimmed copy of {@code pgkb-core}'s {@code LinkOutResource} - see
 * {@code pgkb-pathvisio-plugin/README.md}'s "Relationship to pgkb-core" section. Only the
 * properties this module's code actually reads are kept ({@code id}/{@code shortName}/
 * {@code displayName}/aliases for {@link #lookupByName(String)}, {@code validator} for
 * {@link #validateResourceId(String)}, {@code isOntology} for {@link #isOntology()}); the
 * description/URL/{@code DataSource}/editable/type-tag properties and the lookup/upgrade methods
 * that only serve them were dropped - if this module ever needs one back, restore it from
 * {@code pgkb-core}'s copy rather than reinventing it.
 *
 * @author Mark Woon
 */
public enum LinkOutResource implements ExtendedEnum {
  DBSNP(1, "dbSnp", "dbSNP", null, Pattern.compile("^[rs]s\\d+$"), false),
  GO(2, "GO", "GO", null, Pattern.compile("^GO:\\d{7}$"), true),
  /* Validation reference: https://www.ncbi.nlm.nih.gov/Sequin/acc.html. */
  GENBANK(3, "genbank", "GenBank", null,
      Pattern.compile(
          "^(" +
              "[A-Z]\\d{5}+|" +
              "[A-Z]{2}+\\d{6}+|" +
              "[A-Z]{3}+\\d{5}+|" +
              "[A-Z]{4}+\\d{8,10}+|" +
              "[A-Z]{5}+\\d{7}+" +
              ")" +
              "(?:\\.(\\d+))?$"),
      false),
  /* Validation reference: https://www.ncbi.nlm.nih.gov/Sequin/acc.html. */
  GENBANK_NUCLEOTIDE(80, "genbankNucleotide", "GenBank Nucleotide", null,
      Pattern.compile(
          "^(" +
              "[A-Z]\\d{5}+|" +
              "[A-Z]{2}+\\d{6}+" +
              ")" +
              "(?:\\.(\\d+))?$"),
      false),
  /* Validation reference: https://www.ncbi.nlm.nih.gov/Sequin/acc.html. */
  GENBANK_PROTEIN(81, "genbankProtein", "GenBank Protein", null,
      Pattern.compile(
          "^(" +
              "[A-Z]{3}+\\d{5}+" +
              ")" +
              "(?:\\.(\\d+))?$"),
      false),
  MESH(4, "MeSH", "MeSH", null, Pattern.compile("^[CDQMT]\\d{6,10}$"), true),
  ATC(5, "ATC", "ATC", null, Pattern.compile("^[ABCDGHJLMNPRSV](\\d{2}([A-Z]([A-Z](\\d{2})?)?)?)?$"), true),
  // the recommended name is "NCBI Gene" instead of "Entrez Gene"
  ENTREZ_GENE(6, "entrezGene", "NCBI Gene", null, Pattern.compile("^\\d+$"), false),
  SNOMEDCT(7, "SnoMedCT", "SnoMed CT", null, Pattern.compile("^(\\w+)?\\d+$"), true),
  OMIM(8, "omim", "OMIM", null, null, false),
  CLINPGX(9, "clinpgx", "ClinPGx", null, Pattern.compile("^P[AS]\\d+$"), false),
  PUBMED(10, "pubMed", "PubMed", new String[]{ "PMID" }, Pattern.compile("^\\d+$"), false),
  // RefSeq validation reference:
  //  - https://www.ncbi.nlm.nih.gov/books/NBK21091/table/ch18.T.refseq_accession_numbers_and_mole/
  //  - https://pdfs.semanticscholar.org/16b0/744424f02e01fe2f01b3ea03e2862f1359fc.pdf (Table 1)
  REFSEQ_RNA(11, "refSeqRna", "RefSeq RNA", null, Pattern.compile("(NM|NR|XM|XR)_\\d+(\\.\\d+)?"), false),
  REFSEQ_PROTEIN(12, "refSeqProtein", "RefSeq Protein", null, Pattern.compile("(NP|AP|WP|XP|YP|ZP)_\\d+(\\.\\d+)?"),
      false),
  REFSEQ_DNA(13, "refSeqDna", "RefSeq DNA", null,
      Pattern.compile("(NT|NG|NC|AC|NW|NS)_\\d+(\\.\\d+)?|(NZ)_[A-Z]{2,4}\\d+(\\.\\d+)?"), false),
  UCSC_GB(14, "ucscGenomeBrowser", "UCSC Genome Browser", null, null, false),
  UMLS(15, "UMLS", "UMLS", null, Pattern.compile("^C\\d+$"), true),
  MEDDRA(16, "MedDRA", "MedDRA", null, Pattern.compile("^\\d+$"), true),
  RXNORM(17, "RxNorm", "RxNorm", null, null, true),
  URL(18, "url", "URL", null,
      null, // uses UrlUtils.isValid()
      false),
  NDFRT(19, "NDFRT", "NDF-RT", null, null, true),
  UNIPROT(20, "uniProtKb", "UniProt", new String[]{ "UniProtKb" }, null, false),
  // Ensembl validator only supports human identifiers
  ENSEMBL(21, "ensembl", "Ensembl", null, Pattern.compile("^ENS(E|FM|G|GT|P|R|T)\\d{11}(\\.\\d+)?$"), false),
  GENATLAS(22, "genAtlas", "GenAtlas", null, null, false),
  GENECARD(23, "geneCard", "GeneCard", null, null, false),
  AND_OR(24, "andOr", "Collection Operator - And/Or", null, null, true),
  YES_NO(25, "yesNo", "Confirmation Operator - Yes/No", null, null, true),
  DRUG_NAME_BLACKLIST(26, "drugBlackList", "Drug Name Black List", null, null, true),

  // PathVisio resources
  CL(27, "CL", "CL", null, Pattern.compile("^CL:\\d{7}$"), true),
  CELLULAR_LOCATION(28, "cellularLocation", "Cellular Location", null, null, true),
  PATHWAY_MODIFICATION(29, "pathwayEnzymaticModification", "Enzymatic Modification", null, null, true),
  PATHWAY_ENTITIES(30, "pathwayEntities", "Pathway Entities", null, null, true),
  PATHWAY_PROCESSES(31, "pathwayProcesses", "Pathway Processes", null, null, true),
  PATHVISIO_STATE(32, "PvState", "State Ontology", null, null, true),
  TRANSMEMBRANE_TYPES(33, "transmembraneTypes", "Transmembrane Type", null, null, true),

  // VIP resources
  CHEMICAL_ROLES(34, "chemicalRole", "Chemical Role", null, null, true),

  NDC(35, "ndc", "NDC", null, Pattern.compile("^\\d+-\\d+-\\d+$"), false),
  DRUGBANK(36, "drugBank", "DrugBank", null, Pattern.compile("^DB\\d{5}$"), false),
  DRUGBANK_METABOLITE(82, "drugBankMetabolite", "DrugBank Metabolite", null, Pattern.compile("^DBMET\\d{5}$"), false),
  PDB(37, "pdb", "PDB", null, Pattern.compile("^[0-9][A-Za-z0-9]{3}$"), false),

  // Clinical Annotation resources
  LEVEL_OF_EVIDENCE(38, "levelsOfEvidence", "Level of Evidence", null, null, true),

  CTD_GENE(39, "ctd", "CTD", null, Pattern.compile("^\\d+$"), false),
  /* Details at http://en.wikipedia.org/wiki/CAS_registry_number */
  CAS(40, "cas", "CAS", new String[]{"Chemical Abstracts Service"}, Pattern.compile("^\\d+(?:-\\d+)*$"), false),
  MODBASE(41, "modBase", "ModBase", null, null, false),

  // Dosing Guideline resources
  RX_CHANGE(42, "rxChange", "Prescribing Change", null, null, true),
  // Drug Label resources
  SPL_SECTION(43, "SPL", "SPL Section", null, null, true),
  RETIREMENT_REASON(44, "retirement", "Retirement Reason", null, null, true),
  GENE_TEST_LEVEL(45, "geneTestLevel", "Genetic Testing Level", null, null, true),

  CHEBI(46, "chebi", "ChEBI", null, Pattern.compile("^CHEBI:\\d{1,10}+$"), false),
  KEGG_COMPOUND(47, "keggCompound", "KEGG Compound", null, Pattern.compile("^C\\d+$"), false),
  KEGG_DRUG(48, "keggDrug", "KEGG Drug", null, Pattern.compile("^D\\d+$"), false),
  KEGG_PATHWAY(78, "keggPathway", "KEGG Pathway", null, Pattern.compile("^\\w{2,4}\\d{5}$"), false),

  PUBCHEM_COMPOUND(49, "pubchemCompound", "PubChem Compound", null, Pattern.compile("^\\d{1,10}+$"), false),
  PUBCHEM_SUBSTANCE(50, "pubchemSubstance", "PubChem Substance", null, Pattern.compile("^\\d{1,10}+$"), false),
  PUBCHEM_BIOASSAY(64, "pubchemBioAssay", "PubChem BioAssay", null, Pattern.compile("^\\d+$"), false),

  // CPIC resources
  GUIDELINE_STRENGTH(51, "guidelineStrength", "Guideline Strength", null, null, true),

  HUMANCYC_GENE(52, "HumanCycGene", "HumanCyc Gene", null, null, false),
  IUPHAR_LIGAND(53, "iupharLigand", "IUPHAR Ligand", null, Pattern.compile("^\\d+$"), false),
  IUPHAR_RECEPTOR(54, "iupharReceptor", "IUPHAR Receptor", null, Pattern.compile("^\\d+$"), false),
  HGNC(55, "hgnc", "HGNC", null, Pattern.compile("^HGNC:\\d{1,5}$"), false),
  /**
   * This is used as a well-defined reaction reference for our pathway drawing
   * (e.g. glycolysis and krebs cycle in pathway).
   */
  REACTOME_REACTION(56, "reactomeReaction", "Reactome Reaction", null, Pattern.compile("^REACT_\\d+(\\.\\d+)?$"),
      false),
  GENE_ROLE(57, "geneRole", "Gene Role", null, null, true),

  // direct links to DPD are super inconsistent, so don't link directly to them
  DPD(58, "dpd", "DPD", null, Pattern.compile("^\\d{8}$"), false),
  BINDINGDB(59, "bindingDb", "BindingDB", null, null, false),
  CHEMSPIDER(60, "chemSpider", "ChemSpider", null, Pattern.compile("^\\d+$"), false),
  ALLELE_FUNCTION(61, "alleleFunction", "Allele Function", null, null, true),
  TTD(62, "TTD", "Therapeutic Targets Database", null, Pattern.compile("^D[A-Z]{2}\\d+$"), false),
  DAILYMED(63, "DailyMed", "FDA Drug Label at DailyMed", null, Pattern.compile("^[A-Za-z0-9-]+"), false),
  // 64 - PUBCHEM_BIOASSAY above
  PATHWAY_CATEGORIES(65, "pathwayCategories", "Pathway Categories", null, null, true),
  GUIDELINE_TAGS(66, "guidelineTags", "Guideline Tags", null, null, true),
  CLINICAL_TRIALS(67, "clinicalTrials", "ClinicalTrials.gov", null, Pattern.compile("^NCT\\d{8}$"), false),
  ISRCTN(68, "isrctn", "ISRCTN", null, Pattern.compile("^ISRCTN\\d+$"), false),
  GEO(69, "geo", "GEO", null, Pattern.compile("(GDS|GSE|GPL|GSM)\\d+"), false),
  DOI(70, "doi", "DOI", null, Pattern.compile("^10.\\d{4,}(?:\\.\\d+)*/.+$"), false),
  PII(71, "pii", "Publisher Item Identifiers", null, null, false),
  PMC(72, "pmc", "PubMed Central", new String[]{ "PMCID" }, Pattern.compile("^PMC\\d+$"), false),
  /* Details at http://en.wikipedia.org/wiki/Enzyme_Commission_number */
  EC(73, "ec", "Enzyme Commission", null,
      Pattern.compile("^(\\d+(\\.-\\.-\\.-)?)|(\\d+\\.\\d+(\\.-\\.-)?)|(\\d+\\.\\d+\\.\\d+(\\.-)?)|(\\d+\\.\\d+\\.\\d+\\.(n)?\\d+)$"),
      false),
  HCSC(75, "hcsc", "HC-SC", null, Pattern.compile("^\\d+$"), false),
  // 78 - KEGG_PATHWAY above
  HMDB(79, "hmdb", "HMDB", null, Pattern.compile("^HMDB\\d{5,7}$"), false),
  // 80 - GENBANK_NUCLEOTIDE above
  // 81 - GENBANK_PROTEIN above
  // 82 - DRUGBANK_METABOLITE
  // ISBN id=83 deleted
  CLINVAR(84, "clinvar", "ClinVar", null, Pattern.compile("^\\d+|SCV\\d+(\\.\\d+)?$"), false),

  // Variant Annotation resources
  ASSOCIATION_SIGNIFICANCE(85, "significance", "Association Significance", null, null, true),
  ASSOCIATION_TYPE(86, "association", "Association Type", null, null, true),
  CELL_TYPE(87, "cellCat", "Cell Type", null, null, true),
  EQUALITY(88, "equality", "Equality Symbols", null, null, true),
  VAR_ANN_DRUG_CONNECT_WORDS1(89, "drug_ConnWords1", "Drug Connecting words 1", null, null, true),
  VAR_ANN_FA_CONNECT_WORDS1(90, "fa_ConnWords1", "Functional Assay Connecting words 1", null, null, true),
  METABOLIZER_OPTION(91, "metabolizer", "Metabolizer Option", null, null, true),
  VAR_ANN_PHENOTYPE_CATEGORIES(92, "phenoCat", "Phenotype Categories", null, null, true),
  VAR_ANN_PHENOTYPE_CONNECT_WORDS1(93, "connWords1", "Phenotype Connecting words 1", null, null, true),
  VAR_ANN_PHENOTYPE_CONNECT_WORDS2(94, "connWords2", "Phenotype or Gene to Drug Connecting Words", null, null, true),
  POLARITY(95, "polarity", "Polarity", null, null, true),
  VAR_ANN_POPULATION(96, "populationTerm", "Population", null, null, true),
  POPULATION_TYPE(97, "populationType", "Population Type", null, null, true),
  STAT_ANALYSIS(98, "statAnalysis", "Statistic Analysis", null, null, true),
  STATISTICAL_TEST_TAGS(99, "statTest", "Statistical Test Tags", null, null, true),


  FDA_APPLICATION(109, "fda", "FDA Application", null,
      Pattern.compile("^(NDA|ANDA|BLA)?\\d+(/[A-Z]+-[0-9]+)?$"), false),
  GTR(110, "gtr", "GTR", null, null, false),
  LOINC(112, "loinc", "LOINC", null, Pattern.compile("^MTHU\\d+|(?:L[AP])*\\d+-\\d+$"), true),

  WIKIDATA(116, "wikidata", "Wikidata", null, Pattern.compile("^Q\\d+$"), false),
  SWISSMEDIC(117, "swiss", "Swissmedic", null, Pattern.compile("\\d+"), false),
  PHARMVAR(118, "pharmVar", "PharmVar", null, Pattern.compile("^PV\\d+$"), false),
  MONDO(119, "MONDO", "MONDO", null, Pattern.compile("MONDO:\\d+"), true),
  LRG(120, "lrg", "LRG", null, Pattern.compile("^LRG_\\d+$"), false),
  PHARMVAR_GENE(121, "pharmVarGene", "PharmVar Gene", null, null, false),

  CLINPGX_TAGS(122, "cpgxTags", "ClinPGx Tags", null, null, true),
  /**
   * Tags for classifying curator-reviewed literature.
   */
  PGX_PAPER_TYPES(123, "pgxPaperTypes", "PGx Paper Types", null, null, true),

  CHEMBL(124, "chembl", "ChEMBL", null, Pattern.compile("^CHEMBL\\d+$"), false),
  FDA_UNII(125, "unii", "UNII", null, Pattern.compile("^[A-Z0-9]+$"), false),
  DRUGBANK_SALT(126, "drugBankSalt", "DrugBank Salt", null, Pattern.compile("^DBSALT\\d{6}$"), false),
  PDB_LIGAND(127, "pdbLigand", "PDB Ligand", null, Pattern.compile("^[A-Z\\d]{2,3}$"), false),
  AEMPS(128, "aemps", "AEMPS", null, Pattern.compile("^\\d+$"), false),
  HP(129, "HP", "HP", null, Pattern.compile("^HP:\\d{7}$"), true),
  CLINGEN_ALLELE(130, "clinGenAllele", "ClinGen Allele", null, Pattern.compile("^CA\\d+"), false),
  GNOMAD_V2(131, "gnomADv2", "GnomAD v2", null, null, false),
  GNOMAD_V3(132, "gnomADv3", "GnomAD v3", null, null, false),
  GNOMAD_V4(133, "gnomADv4", "GnomAD v4", null, null, false),
  GSI_OPTION(134, "gsiOptions", "GSI Allele Options", null, null, true),
  ;
  /* CURRENT ID MAX: 134
   * WE ARE USING THE SAME ID SEQUENCE FOR CrossReferenceResource AND OntologyResource AND LinkOutResource.
   * If you add to this enum, be sure to re-run the db-export make task.
   * Insert a corresponding row into PgkbComm.LinkOutResources.
   * Referential integrity FTW.
   *
   * If you need to link a resource to an ObjectType, use the LinkOutMapper.
   */

  /**
   * Pattern used to identify a {@link LinkOutResource} in markup.
   */
  public static final Pattern MARKUP_PATTERN = Pattern.compile("\\[\\s*(.+?)\\s*:\\s*(.+?)\\s*]");

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<LinkOutResource> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private final @Nullable Pattern m_validator;
  private final boolean m_isOntology;


  LinkOutResource(int id, String shortName, String displayName, String @Nullable [] aliases,
      @Nullable Pattern validator, boolean isOntology) {
    m_id = id;
    m_shortName = shortName;
    m_displayName = displayName;
    m_validator = validator;
    m_isOntology = isOntology;
    init(aliases);
  }


  public boolean validateResourceId(@Nullable String id) {
    if (StringUtils.stripToNull(id) == null) {
      return false;
    }
    if (this == URL) {
      return UrlUtils.isValid(id);
    }
    return (m_validator == null || m_validator.matcher(id).matches());
  }


  public boolean isOntology() {
    return m_isOntology;
  }


  //-- BEGIN ExtendedEnum methods --//
  private synchronized void init(String @Nullable [] aliases) {
    //noinspection ConstantValue
    if (s_extendedEnumHelper == null) {
      s_extendedEnumHelper = new ExtendedEnumHelper<>(getClass());
    }
    s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName, aliases);
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public String getShortName() {
    return m_shortName;
  }

  @Override
  public String getDisplayName() {
    if (m_displayName != null) {
      return m_displayName;
    }
    return m_shortName;
  }

  @Override
  public final String toString() {
    return getDisplayName();
  }
  //-- END ExtendedEnum methods --//

  //-- BEGIN ExtendedEnum statics --//
  public static @Nullable LinkOutResource lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }
  //-- END ExtendedEnum statics --//
}
