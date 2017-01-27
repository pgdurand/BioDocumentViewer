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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import bzh.plealog.bioinfo.docviewer.service.ensembl.model.format.Utilities;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class TranscriptConsequences {
/*  <transcript_consequences 
 *    amino_acids="G/R" 
 *    biotype="protein_coding" 
 *    cdna_end="1310" 
 *    cdna_start="1310" 
 *    cds_end="1120" 
 *    cds_start="1120" 
 *    codons="Ggt/Cgt" 
 *    gene_id="ENSG00000141510" 
 *    gene_symbol="TP53" 
 *    gene_symbol_source="HGNC" 
 *    hgnc_id="HGNC:11998" 
 *    impact="MODERATE" 
 *    polyphen_prediction="benign" 
 *    polyphen_score="0.157" 
 *    protein_end="374" 
 *    protein_start="374" 
 *    sift_prediction="tolerated" 
 *    sift_score="0.16" 
 *    strand="-1" 
 *    transcript_id="ENST00000269305" 
 *    variant_allele="G">
 */

  //undefined values: String->null ; numbers->-1
  
  @XmlAttribute(name = "cdna_end", required = false)
  protected int cdna_end=-1;
  @XmlAttribute(name = "cdna_start", required = false)
  protected int cdna_start=-1;
  @XmlAttribute(name = "cds_end", required = false)
  protected int cds_end=-1;
  @XmlAttribute(name = "cds_start", required = false)
  protected int cds_start=-1;
  @XmlAttribute(name = "protein_end", required = false)
  protected int protein_end=-1;
  @XmlAttribute(name = "protein_start", required = false)
  protected int protein_start=-1;
  @XmlAttribute(name = "strand", required = false)
  protected int strand=-1;

  @XmlAttribute(name = "polyphen_score", required = false)
  protected double polyphen_score=-1;
  @XmlAttribute(name = "sift_score", required = false)
  protected double sift_score=-1;
  
  @XmlAttribute(name = "amino_acids", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String amino_acids;
  @XmlAttribute(name = "biotype", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String biotype;
  @XmlAttribute(name = "codons", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String codons;
  @XmlAttribute(name = "gene_id", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String gene_id;
  @XmlAttribute(name = "gene_symbol", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String gene_symbol;
  @XmlAttribute(name = "impact", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String impact;
  @XmlAttribute(name = "polyphen_prediction", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String polyphen_prediction;
  @XmlAttribute(name = "sift_prediction", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String sift_prediction;
  @XmlAttribute(name = "transcript_id", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String transcript_id;
  @XmlAttribute(name = "variant_allele", required = false)
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected String variant_allele;
  
  @XmlElement(name = "consequence_terms", required = false)
  protected List<ConsequenceTerms> consequence_terms;
  private String consequencesTermsStr;
  
  public int getCdna_end() {
    return cdna_end;
  }
  public int getCdna_start() {
    return cdna_start;
  }
  public int getCds_end() {
    return cds_end;
  }
  public int getCds_start() {
    return cds_start;
  }
  public int getProtein_end() {
    return protein_end;
  }
  public int getProtein_start() {
    return protein_start;
  }
  public int getStrand() {
    return strand;
  }
  public double getPolyphen_score() {
    return polyphen_score;
  }
  public double getSift_score() {
    return sift_score;
  }
  public String getAmino_acids() {
    return amino_acids;
  }
  public String getBiotype() {
    return biotype;
  }
  public String getCodons() {
    return codons;
  }
  public String getGene_id() {
    return gene_id;
  }
  public String getGene_symbol() {
    return gene_symbol;
  }
  public String getImpact() {
    return impact;
  }
  public String getPolyphen_prediction() {
    return polyphen_prediction;
  }
  public String getSift_prediction() {
    return sift_prediction;
  }
  public String getTranscript_id() {
    return transcript_id;
  }
  public String getVariant_allele() {
    return variant_allele;
  }

  public List<ConsequenceTerms> getConsequenceTerms() {
    if (consequence_terms == null) {
      consequence_terms = new ArrayList<ConsequenceTerms>();
    }
    return consequence_terms;
  }
  public String getConsequenceTermsStr() {
    if (consequencesTermsStr != null) {
      return consequencesTermsStr;
    }
    consequencesTermsStr = Utilities.makeString(consequence_terms, "; ");
    return consequencesTermsStr;
  }

}
