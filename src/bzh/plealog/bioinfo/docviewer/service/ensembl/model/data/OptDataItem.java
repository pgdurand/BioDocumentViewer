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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.data;

import java.util.ArrayList;
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

  //undefined values: String->null ; numbers->-1 ; List<>->empty

  // error reported by Ensembl service
  @XmlAttribute(name = "error", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        error;
  
  // attributes when querying to get ENSG ids from gene names
  @XmlAttribute(name = "id", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        id;

  @XmlAttribute(name = "name", required = true)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        name;
  
  
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
  
  
  @XmlAttribute(name = "var_class", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        var_class;

  @XmlAttribute(name = "allele_string", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        allele_string;
  
  @XmlAttribute(name = "most_severe_consequence", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        most_severe_consequence;
  
  @XmlAttribute(name = "ambiguity", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        ambiguity;
  
  @XmlAttribute(name = "ancestral_allele", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        ancestral_allele;
  
  @XmlAttribute(name = "seq_region_name", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String        seq_region_name;
  @XmlAttribute(name = "start", required = false)
  protected int           start=-1;
  @XmlAttribute(name = "end", required = false)
  protected int           end=-1;
  @XmlAttribute(name = "strand", required = false)
  protected int           strand=-1;

  @XmlElement(name = "alleles", required = false)
  protected List<Alleles> alleles;
  
  @XmlElement(name = "clinical_significance", required = false)
  protected List<ClinicalSignificance> clinical_significance;
  
  @XmlElement(name = "evidence", required = false)
  protected List<Evidence> evidence;
  
  @XmlElement(name = "synonyms", required = false)
  protected List<Synonyms> synonyms;

  @XmlElement(name = "mappings", required = false)
  protected List<Mappings> mappings;

  @XmlElement(name = "transcript_consequences", required = false)
  protected List<TranscriptConsequences> transcript_consequences;

  public String getError() {
    return error;
  }

  public String getId() {
    return id;
  }

  public String getName(){
    return name;
  }
  
  public String getType() {
    return type;
  }

  public String getAssembly_name() {
    return assembly_name;
  }

  public String getConsequence_type() {
    return consequence_type;
  }

  public String getFeature_type() {
    return feature_type;
  }

  public String getSource() {
    return source;
  }

  public String getVariantClass() {
    return var_class;
  }
  
  public String getAllele_string() {
    return allele_string;
  }

  public String getMost_severe_consequence() {
    return most_severe_consequence;
  }

  public String getAmbiguity() {
    return ambiguity;
  }

  public String getAncestral_allele() {
    return ancestral_allele;
  }

  public String getSeq_region_name() {
    return seq_region_name;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getStrand() {
    return strand;
  }

  public List<Alleles> getAlleles() {
    if (alleles == null) {
      alleles = new ArrayList<Alleles>();
    }
    return alleles;
  }

  public List<ClinicalSignificance> getClinical_significance() {
    if (clinical_significance == null) {
      clinical_significance = new ArrayList<ClinicalSignificance>();
    }
    return clinical_significance;
  }

  public List<Evidence> getEvidence() {
    if (evidence == null) {
      evidence = new ArrayList<Evidence>();
    }
    return evidence;
  }

  public List<Synonyms> getSynonyms() {
    if (synonyms == null) {
      synonyms = new ArrayList<Synonyms>();
    }
    return synonyms;
  }

  public List<Mappings> getMappings() {
    if (mappings == null) {
      mappings = new ArrayList<Mappings>();
    }
    return mappings;
  }
  
  public List<TranscriptConsequences> getTranscriptConsequences() {
    if (transcript_consequences == null) {
      transcript_consequences = new ArrayList<TranscriptConsequences>();
    }
    return transcript_consequences;
  }
  
}
