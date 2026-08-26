package org.pharmgkb.pathvisio;

import java.util.Set;
import com.google.common.collect.ImmutableSet;


/**
 * BioPax constants.
 *
 * @author Mark Woon
 */
public class BiopaxConstants {
  public static Set<PgkbType> PATHWAY_TYPES = ImmutableSet.of(
      PgkbType.PATHWAY,
      PgkbType.PHENOTYPE,
      PgkbType.PROCESS);
  public static Set<PgkbType> PROTEIN_TYPES = ImmutableSet.of(
      PgkbType.GENE,
      PgkbType.HAPLOTYPE,
      PgkbType.NONHUMAN_GENE,
      PgkbType.GENE_GROUP);
  public static Set<PgkbType> SMALL_MOLECULE_TYPES = ImmutableSet.of(
      PgkbType.BIOLOGICAL_INTERMEDIATE,
      PgkbType.DRUG,
      PgkbType.DRUG_CLASS,
      PgkbType.ION,
      PgkbType.METABOLITE);
  public static Set<PgkbType> COMPLEX_TYPES = ImmutableSet.of(
      PgkbType.GENE_COMPLEX,
      PgkbType.GENE_DRUG_COMPLEX,
      PgkbType.DRUG_COMPLEX);
  public static Set<PgkbType> NUCLEIC_ACID_TYPES = ImmutableSet.of(
      PgkbType.DNA_ENTITY,
      PgkbType.RNA_ENTITY);
  public static Set<PgkbType> PHYSICAL_ENTITY_TYPES = ImmutableSet.of(
      PgkbType.PHYSICAL_ENTITY);
  public static Set<PgkbType> FORCED_PHYSICAL_ENTITY_TYPES = ImmutableSet.<PgkbType>builder()
      .addAll(PHYSICAL_ENTITY_TYPES)
      .addAll(PATHWAY_TYPES)
      .build();
}
