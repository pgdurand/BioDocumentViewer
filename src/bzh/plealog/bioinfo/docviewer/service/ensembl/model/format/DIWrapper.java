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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.format.DataFormatter;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.ClinicalSignificance;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Evidence;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Opt;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.OptDataItem;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Synonyms;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.TranscriptConsequences;
import bzh.plealog.bioinfo.util.CoreUtil;

/**
 * This is a data wrapper class used for the purpose of data formatting. It
 * avoids exhibiting XML/Java serialization objects and protect underlying
 * formatting engine against null or undefined values.
 * 
 * @author Patrick G. Durand
 */
public class DIWrapper {

  protected static final String UNKNWON = "-";

  private OptDataItem _item;
  private String _locationStr;
  private String _allelesStr;
  private String _clinSignStr;
  private String _evidenceStr;

  @SuppressWarnings("unused")
  private DIWrapper() {
  }

  /**
   * Constructor.
   * 
   * @param item
   *          the data to wrap in this object
   */
  public DIWrapper(OptDataItem item) {
    _item = item;
  }

  /**
   * Constructor.
   * 
   * @param file
   *          file containing data to load. Mut target an XML data file relying
   *          on the Opt object. From that object, only first OptDataItem is
   *          retained and wrapped by this DIWrapper instance.
   */
  public DIWrapper(File file) {
    Opt o = JAXB.unmarshal(file, Opt.class);
    _item = o.getItem().get(0);
  }

  /**
   * @return an ID
   */
  public String getId() {
    if (_item.getId() != null)
      return _item.getId();
    else if (_item.getName() != null)
      return _item.getName();
    else
      return UNKNWON;
  }

  public String getSourceURL(){
    String id = getId();
    if (id.startsWith("rs")){
      return CoreUtil.replaceFirst("http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs=@ID@", "@ID@", id);
    }
    else{
      return "#";
    }
  }
  /**
   * @return most severe consequence
   */
  public String getMost_severe_consequence() {
    return _item.getMost_severe_consequence() != null ? _item.getMost_severe_consequence() : UNKNWON;
  }

  /**
   * @return allele string
   */
  public String getAllele_string() {
    return _item.getAllele_string() != null ? _item.getAllele_string() : UNKNWON;
  }

  /**
   * @return ambiguity
   */
  public String getAmbiguity() {
    return _item.getAmbiguity() != null ? _item.getAmbiguity() : UNKNWON;
  }

  /**
   * @return ancestral allele
   */
  public String getAncestral_allele() {
    return _item.getAncestral_allele() != null ? _item.getAncestral_allele() : UNKNWON;
  }

  /**
   * @return list of synonym IDs
   */
  public List<String> getSynonyms() {
    ArrayList<String> synonyms;

    synonyms = new ArrayList<>();

    for (Synonyms sim : _item.getSynonyms()) {
      synonyms.add(sim.getvalue());
    }

    return synonyms;
  }

  /**
   * @return source databank of OptDataItem
   */
  public String getSource() {
    return _item.getSource();
  }

  /**
   * @return assembly name
   */
  public String getAssembly_name() {
    return _item.getAssembly_name();
  }

  /**
   * @return location formatted as "chr:start-end".
   */
  public String getLocation() {
    if (_locationStr != null) {
      return _locationStr;
    }
    StringBuffer buf = new StringBuffer();
    buf.append(_item.getSeq_region_name());
    buf.append(":");
    buf.append(_item.getStart());
    if (_item.getStart() != _item.getEnd()) {
      buf.append("-");
      buf.append(_item.getEnd());
    }
    buf.append(" (");
    buf.append(_item.getStrand() > 0 ? "+" : "-");
    buf.append(")");
    _locationStr = buf.toString();
    return _locationStr;
  }

  /**
   * @return all alleles as a string.
   */
  public String getAllelesStr() {
    if (_allelesStr != null) {
      return _allelesStr;
    }
    _allelesStr = Utilities.makeString(_item.getAlleles(), "/");
    return _allelesStr;
  }

  /**
   * @return all clinical significance as a string
   */
  public String getClinical_significanceStr() {
    if (_clinSignStr != null) {
      return _clinSignStr;
    }
    _clinSignStr = Utilities.makeString(_item.getClinical_significance(), "; ");
    return _clinSignStr;
  }

  /**
   * @return list of clinical significances
   */
  public List<String> getClinical_significance() {
    ArrayList<String> lst;

    lst = new ArrayList<>();

    for (ClinicalSignificance cs : _item.getClinical_significance()) {
      lst.add(cs.getvalue());
    }

    return lst;
  }

  /**
   * @return the icon representing a clinical significance
   */
  public String getClinical_significanceImageName(String clinical_significance) {
    ImageIcon icon;

    icon = DataFormatter.getIcon(clinical_significance);

    return icon != null ? icon.getDescription() : DIWrapper.UNKNWON;
  }

  /**
   * @return an evidences as a string
   */
  public String getEvidenceStr() {
    if (_evidenceStr != null) {
      return _evidenceStr;
    }
    _evidenceStr = Utilities.makeString(_item.getEvidence(), "; ");
    return _evidenceStr;
  }

  /**
   * @return list of evidences
   */
  public List<String> getEvidence() {
    ArrayList<String> lst;

    lst = new ArrayList<>();

    for (Evidence ev : _item.getEvidence()) {
      lst.add(ev.getvalue());
    }

    return lst;
  }

  /**
   * @return the icon representing an evidence
   */
  public String getEvidenceImageName(String evidence) {
    ImageIcon icon;

    icon = DataFormatter.getIcon(evidence);

    return icon != null ? icon.getDescription() : DIWrapper.UNKNWON;
  }

  /**
   * @return consequence type
   */
  public String getConsequence_type() {
    return _item.getConsequence_type();
  }

  /**
   * @return list of TranscriptConsequences within dedicated wrapper objects.
   */
  public List<TCWrapper> getTranscriptConsequences() {
    ArrayList<TCWrapper> tcs;

    tcs = new ArrayList<>();

    for (TranscriptConsequences tc : _item.getTranscriptConsequences()) {
      tcs.add(new TCWrapper(tc));
    }

    return tcs;
  }
}
