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
package bzh.plealog.bioinfo.docviewer.service.ebi;

import java.io.File;
import java.util.List;

import javax.swing.SwingConstants;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryModel;
import bzh.plealog.bioinfo.docviewer.api.ReaderType;
import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDocPresentationModel;
import bzh.plealog.bioinfo.docviewer.service.ebi.io.EbiQueryEngine;
import bzh.plealog.bioinfo.docviewer.service.ebi.io.EbiServerConfiguration;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.EbiSearchLoader;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.EbiSummaryLoader;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.query.EbiQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.query.NucleotideQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.query.ProteinQueryModel;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * Describe the banks from EBI that are available for querying with this software.
 * Right now, provide access to UniprotKB and EMBL Nucleotide release. 
 * More can be added if needed: adapt this code accordingly.
 * 
 * @author Patrick G. Durand
 */
public enum EbiBank implements BankType {
  //format: data code (one letter), bank name for user presentation, EBI bank code, 
  // figure out whether or not this bank enables sequence retrieval, reader type
  PROTEIN  ("P", "Uniprot-KB",                 "uniprotkb",   true, ReaderType.UNIPROT),
  EMBL_REL ("N", "EMBL Nucleotide release",    "emblrelease", true, ReaderType.EMBL)
  ; 
  
  //ena_sequence
  private final String type;
  private final ReaderType rType;
  private final String userName;
  private final String entrezName;
  private final SummaryDocPresentationModel pModel;
  private final EbiQueryModel filterModel;
  private final boolean enableSeqRetrieval;
  
  EbiBank(String type, String userName, String entrezName, boolean enableSeqRetrieval, ReaderType rType) {
    //just in case: we add some control on the type
    if (type.length()!=1)
      throw new RuntimeException("Type BankType must be a single letter");
    this.type = type.toUpperCase();
    this.userName = userName;
    this.entrezName = entrezName;
    this.enableSeqRetrieval = enableSeqRetrieval;
    this.rType = rType;
    
    switch (type) {
    // for each bank type, we setup the filter model (used to prepare Query Dialogue Box)
    // and the presentation model (used to display results).
    case "N":
      pModel = new EbiDbSummaryDocPresentationModel("N", "ebi.nuc.docSumTable.columns");
      filterModel = new NucleotideQueryModel();
      break;
    case "P":
    default:
      pModel = new EbiDbSummaryDocPresentationModel("P", "ebi.prot.docSumTable.columns");
      filterModel = new ProteinQueryModel();
    }
  }

  public String getType() {
    return type;
  }

  public ReaderType getReaderType(){
    return rType;
  }
  public String getUserName() {
    return userName;
  }

  public String getCode() {
    return entrezName;
  }

  public QueryModel getQueryModel() {
    return filterModel;
  }
  
  public String getProviderName(){
    return "EBI";
  }

  @Override
  public String toString() {
    return userName;
  }

  public SummaryDocPresentationModel getPresentationModel() {
    return pModel;
  }

  public boolean enableSequenceRetrieval(){
    return enableSeqRetrieval;
  }
  
  public Summary getSummary(File f){
    //This method is called by the EbiQueryEngine when retrieving an answer
    //from the Ebi-Eye Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    //All databanks have a similar processing
    return EbiSummaryLoader.load(f);
  }
  
  public Search getSearch(File f){
    //This method is called by the EbiQueryEngine when retrieving an answer
    //from the Ebi-Eye Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    //This method actually retrieves list of IDs (or an error). Similar for all NCBI banks.
    return EbiSearchLoader.load(f);
  }
  
  public QueryEngine prepareQueryEngine(BFilter query){
    return new EbiQueryEngine(this, query);
  }

  public ServerConfiguration getServerConfiguration(){
    return new EbiServerConfiguration();
  }
  
  /**
   * Setup a presentation model to display Entree Summary Documents.
   */
  private static class EbiDbSummaryDocPresentationModel implements SummaryDocPresentationModel {

    //this key is used to store displayed columns in Application Preferences 
    private String _defColPropKey = "ebi.docSumTable.columns";
    //this is used to setup the displayed columns
    private TableHeaderColumnItem[] _presModel;

    // We describe the general model for all EBI DBs. If a field is missing, or
    // you need to add one more: add new constant and increase numbering, but never 
    // insert between existing values !
    private static final int NUM_HDR = 0;
    private static final int Accession_HDR = 1;
    private static final int Identifier_HDR = 2;
    private static final int Description_HDR = 3;
    private static final int Length_HDR = 4;
    private static final int Organism_HDR = 5;
    private static final int Status_HDR = 6;

    // Then, we define the corresponding names. Again, you can add new entries,
    // but never insert.
    // Be aware that these strings also serve as key in the class Summary
    // for EBI DocSum preparation: see EyeSummaryLoader class.
    private static final String[] RES_HEADERS = { "Num", "Accession", "Identifier", 
        "Description", "Length", "Organism", "Status"};

    /**
     * Constructor.
     */
    private EbiDbSummaryDocPresentationModel(String type, String propertyKey) {
      _defColPropKey = propertyKey;
      switch (type) {
      case "N":
        _presModel = getNucPresentationModel();
        break;
      case "P":
      default:
        _presModel = getProtPresentationModel();
      }
    }
    private TableHeaderColumnItem[] getNucPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,2,3,4,5";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[6];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[Accession_HDR], Accession_HDR, false,
          idSet.contains(Accession_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[Identifier_HDR], Identifier_HDR, false,
          idSet.contains(Identifier_HDR));
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[Description_HDR], Description_HDR, false,
          idSet.contains(Description_HDR));
      refColumnIds[3].setLargest(true);
      refColumnIds[3].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[Length_HDR], Length_HDR, false,
          idSet.contains(Length_HDR));
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[Organism_HDR], Organism_HDR, false,
          idSet.contains(Organism_HDR));
      refColumnIds[5].setHorizontalAlignment(SwingConstants.LEFT);
      return refColumnIds;
    }

    private TableHeaderColumnItem[] getProtPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,2,6,3,4,5";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[7];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[Accession_HDR], Accession_HDR, false,
          idSet.contains(Accession_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[Identifier_HDR], Identifier_HDR, false,
          idSet.contains(Identifier_HDR));
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[Status_HDR], Status_HDR, false,
          idSet.contains(Status_HDR));
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[Description_HDR], Description_HDR, false,
          idSet.contains(Description_HDR));
      refColumnIds[4].setLargest(true);
      refColumnIds[4].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[Length_HDR], Length_HDR, false,
          idSet.contains(Length_HDR));
      refColumnIds[6] = new TableHeaderColumnItem(RES_HEADERS[Organism_HDR], Organism_HDR, false,
          idSet.contains(Organism_HDR));
      refColumnIds[6].setHorizontalAlignment(SwingConstants.LEFT);
      return refColumnIds;
    }
    @Override
    public TableHeaderColumnItem[] getPresentationModel() {
      return _presModel;
    }
    @Override
    public String getLengthFieldKey() {
      return RES_HEADERS[Length_HDR];
    }
    @Override
    public String getAccessionFieldKey() {
      return RES_HEADERS[Accession_HDR];
    }
    @Override
    public void saveDefaultColumnModel(String prop) {
      //EZEnvironment.setApplicationProperty(_defColPropKey, prop);
    }
  }

}
