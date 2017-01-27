/* Copyright (C) 2017 Inria
 * Author: Patrick G. Durand
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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class Mappings {

  /*
   * <mappings 
   *   allele_string = "C/A/G/T" 
   *   assembly_name = "GRCh38" 
   *   coord_system = "chromosome" 
   *   end = "7669671" 
   *   location = "17:7669671-7669671"
   *   seq_region_name = "17" 
   *   start = "7669671" 
   *   strand = "1" 
   * />
   */

  @XmlAttribute(name = "allele_string", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String allele_string;

  @XmlAttribute(name = "assembly_name", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String assembly_name;

  @XmlAttribute(name = "coord_system", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String coord_system;

  @XmlAttribute(name = "location", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String location;

  @XmlAttribute(name = "start", required = true)
  protected int start;
  @XmlAttribute(name = "end", required = true)
  protected int end;
  @XmlAttribute(name = "seq_region_name", required = true)
  protected int seq_region_name;

  public String getAllele_string() {
    return allele_string;
  }

  public String getAssembly_name() {
    return assembly_name;
  }

  public String getCoord_system() {
    return coord_system;
  }

  public String getLocation() {
    return location;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getSeq_region_name() {
    return seq_region_name;
  }

}
