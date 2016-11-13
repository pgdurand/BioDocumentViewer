/* Copyright (C) 2006-2016 Patrick G. Durand
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data")
public class OptDataItem {

  // error reported by Ensembl service
  @XmlAttribute(name = "error", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        error;
  
  // attributes when querying to get ENSG ids from gene names
  @XmlAttribute(name = "id", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        id;

  @XmlAttribute(name = "type", required = false)
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  protected String        type;

  // attributes when querying to get variants
  @XmlAttribute(name = "assembly_name", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        assembly_name;

  @XmlAttribute(name = "consequence_type", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        consequence_type;

  @XmlAttribute(name = "feature_type", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        feature_type;

  @XmlAttribute(name = "source", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        source;

  @XmlAttribute(name = "seq_region_name", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        seq_region_name;
  @XmlAttribute(name = "start", required = false)
  protected int           start;
  @XmlAttribute(name = "end", required = false)
  protected int           end;
  @XmlAttribute(name = "strand", required = false)
  protected int           strand;

  @XmlElement(name = "alleles", required = false)
  protected List<Alleles> alleles;
  private String          allelesStr;
  private String          locationStr;

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    this.type = value;
  }

  public String getAssembly_name() {
    return assembly_name;
  }

  public void setAssembly_name(String assembly_name) {
    this.assembly_name = assembly_name;
  }

  public String getConsequence_type() {
    return consequence_type;
  }

  public void setConsequence_type(String consequence_type) {
    this.consequence_type = consequence_type;
  }

  public String getFeature_type() {
    return feature_type;
  }

  public void setFeature_type(String feature_type) {
    this.feature_type = feature_type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSeq_region_name() {
    return seq_region_name;
  }

  public void setSeq_region_name(String seq_region_name) {
    this.seq_region_name = seq_region_name;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int getStrand() {
    return strand;
  }

  public void setStrand(int strand) {
    this.strand = strand;
  }

  public String getLocation(){
    if (locationStr!=null){
      return locationStr;
    }
    StringBuffer buf = new StringBuffer();
    buf.append(seq_region_name);
    buf.append(":");
    buf.append(start);
    if (start!=end){
      buf.append("-");
      buf.append(end);
    }
    buf.append(" (");
    buf.append(strand>0?"+":"-");
    buf.append(")");
    locationStr = buf.toString();
    return locationStr;
  }
  public String getAllelesStr() {
    if (allelesStr != null) {
      return allelesStr;
    }
    if (alleles == null || alleles.isEmpty()) {
      return "";
    }
    StringBuffer buf = new StringBuffer();
    Iterator<Alleles> iter = alleles.iterator();
    while(iter.hasNext()){
      buf.append(iter.next().getvalue());
      if (iter.hasNext()){
        buf.append("/");
      }
    }
    allelesStr = buf.toString();
    return allelesStr;
  }

  public List<Alleles> getAlleles() {
    if (alleles == null) {
      alleles = new ArrayList<Alleles>();
    }
    return alleles;
  }

}
