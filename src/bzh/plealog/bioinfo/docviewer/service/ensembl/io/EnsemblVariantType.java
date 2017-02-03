/* Copyright (C) 2006-2017 Patrick G. Durand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/agpl-3.0.txt
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 */
package bzh.plealog.bioinfo.docviewer.service.ensembl.io;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Define types of variant sets available in the software.
 * 
 * Retrieving variant data from Ensembl Variant DB can be quite tricky. Indeed,
 * depending on teh variant type requested, URL has to to be formatted accordingly
 * and that format is not uniform. So, this enum contains the logic for that.
 * 
 * @author Patrick G. Durand
 * */
public enum EnsemblVariantType {
  CLINVAR  ("clinvar" , "variation;variant_set=ClinVar"), 
  PHENCODE ("phencode", "variation;variant_set=phencode"), 
  COSMIC   ("cosmic"  , "somatic_variation"), 
  ALL      ("all"     , "variation");

  private final String name;
  private final String format;

  private static final Map<String, EnsemblVariantType> BY_NAME_MAP = new LinkedHashMap<>();

  static {
    for (EnsemblVariantType rae : EnsemblVariantType.values()) {
      BY_NAME_MAP.put(rae.name, rae);
    }
  }

  /**
   * Constructor.
   * 
   * @param name
   *          name of the variant set. For internal use only, it has nothing to
   *          do with Ensembl variant set name. It is also the string to use
   *          with the lookup method {@link EnsemblVariantType#getName()}.
   * @param format
   *          string to use in URL to query Ensembl Variant DB
   */
  private EnsemblVariantType(String name, String format) {
    this.name = name;
    this.format = format;
  }

  /**
   * @return the variant set name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the format string to use in URL.
   */
  public String getFormatString() {
    return format;
  }

  /**
   * Lookup method to get an EnsemblVariantType by its name.
   * 
   * @param name
   *          name of the variant set.
   * @return the corresponding EnsemblVariantType or null if not found.
   */
  public static EnsemblVariantType byName(String name) {
    return BY_NAME_MAP.get(name);
  }
}
