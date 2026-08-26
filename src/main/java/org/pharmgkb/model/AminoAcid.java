package org.pharmgkb.model;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;


/**
 * Enum of amino acids. This stores their 1-character and 3-character codes.
 *
 * @author Ryan Whaley
 */
public enum AminoAcid implements ExtendedEnum {
  DEL (-1, "-", "del"),
  A(1, "A", "Ala"),
  B(2, "B", "Asx", true),
  C(3, "C", "Cys"),
  D(4, "D", "Asp"),
  E(5, "E", "Glu"),
  F(6, "F", "Phe"),
  G(7, "G", "Gly"),
  H(8, "H", "His"),
  I(9, "I", "Ile"),
  K(11, "K", "Lys"),
  L(12, "L", "Leu"),
  M(13, "M", "Met"),
  N(14, "N", "Asn"),
  P(16, "P", "Pro"),
  Q(17, "Q", "Gln"),
  R(18, "R", "Arg"),
  S(19, "S", "Ser"),
  T(20, "T", "Thr"),
  U(21, "U", "Sec"),
  V(22, "V", "Val"),
  W(23, "W", "Trp"),
  X(24, "X", "Xaa", true),
  Y(25, "Y", "Tyr"),
  Z(26, "Z", "Glx", true);

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<AminoAcid> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private final boolean m_representsMultiple;

  AminoAcid(int id, String shortName, String displayName) {
    this(id, shortName, displayName, false);
  }

  AminoAcid(int id, String shortName, String displayName, boolean multiple) {
    m_id = id;
    m_shortName = shortName;
    m_displayName = displayName;
    m_representsMultiple = multiple;
    init();
  }

  public boolean isRepresentsMultiple() {
    return m_representsMultiple;
  }


  //-- BEGIN ExtendedEnum methods --//
  private synchronized void init() {
    //noinspection ConstantValue
    if (s_extendedEnumHelper == null) {
      s_extendedEnumHelper = new ExtendedEnumHelper<>(getClass());
    }
    s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName);
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
  public static @Nullable AminoAcid lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable AminoAcid lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<AminoAcid> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<AminoAcid> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
