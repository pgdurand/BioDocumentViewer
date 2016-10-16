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
package bzh.plealog.bioinfo.docviewer.service.ncbi;

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
import bzh.plealog.bioinfo.docviewer.service.ncbi.io.EntrezQueryEngine;
import bzh.plealog.bioinfo.docviewer.service.ncbi.io.EntrezServerConfiguration;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.EntrezSearchLoader;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.EntrezSummaryLoader;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.EntrezQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.NucleotideQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.ProteinQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.StructureQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.TaxonomyQueryModel;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * Describe the banks from NCBI Entrez that are available for querying with this software.
 * Right now, provide access to nucleotide, protein, 3D structure, Clinvar and taxonomy databanks. 
 * More can be added if needed: adapt this code accordingly.
 * 
 * @author Patrick G. Durand
 */
public enum EntrezBank implements BankType {
  //format: data code (one letter), bank name for user presentation, NCBI bank code, 
  // figure out whether or not this bank enables sequence retrieval, reader type
  PROTEIN   ("P", "Proteins",                 "protein",    true,  ReaderType.GENPEPT), 
  NUCLEOTIDE("N", "Nucleotides (All)",        "nucleotide", true,  ReaderType.GENBANK), 
  NUCCORE   ("N", "Genbank Core Nucleotides", "nuccore",    true,  ReaderType.GENBANK), 
  //EST       ("N", "EST only",                 "nucest",     true,  ReaderType.GENBANK), 
  //GSS       ("N", "GSS only",                 "nucgss",     true,  ReaderType.GENBANK), 
  STRUCTURE ("S", "3D Structures",            "structure",  false, ReaderType.PDB), 
  TAXONOMY  ("T", "Taxonomy",                 "taxonomy",   false, ReaderType.UNKNOWN);

  private final String type;
  private final ReaderType rType;
  private final String userName;
  private final String entrezName;
  private final SummaryDocPresentationModel pModel;
  private final EntrezQueryModel filterModel;
  private final boolean enableSeqRetrieval;
  
  EntrezBank(String type, String userName, String entrezName, boolean enableSeqRetrieval, ReaderType rType) {
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
    case "T":
      pModel = new EntrezDbSummaryDocPresentationModel("T", "entrez.tax.docSumTable.columns");
      filterModel = new TaxonomyQueryModel();
      break;
    case "N":
      pModel = new EntrezDbSummaryDocPresentationModel("N", "entrez.nucl.docSumTable.columns");
      filterModel = new NucleotideQueryModel();
      break;
    case "S":
      pModel = new EntrezDbSummaryDocPresentationModel("S", "entrez.stru.docSumTable.columns");
      filterModel = new StructureQueryModel();
      break;
    case "P":
    default:
      pModel = new EntrezDbSummaryDocPresentationModel("P", "entrez.prot.docSumTable.columns");
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
    return "NCBI";
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
    //This method is called by the EntrezQueryEngine when retrieving an answer
    //from the NCBI Entrez Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    
    //Similar for all NCBI sequence banks.
    return EntrezSummaryLoader.load(f);
  }
  
  public Search getSearch(File f){
    //This method is called by the EntrezQueryEngine when retrieving an answer
    //from the NCBI Entrez Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    //This method actually retrieves list of IDs (or an error). Similar for all NCBI banks.
    return EntrezSearchLoader.load(f);
  }
  
  public QueryEngine prepareQueryEngine(BFilter query){
    return new EntrezQueryEngine(this, query);
  }
  
  public ServerConfiguration getServerConfiguration(){
    return new EntrezServerConfiguration();
  }
  
  /**
   * Setup a presentation model to display Entree Summary Documents.
   */
  private static class EntrezDbSummaryDocPresentationModel implements SummaryDocPresentationModel {

    //this key is used to store displayed columns in Application Preferences 
    private String _defColPropKey = "entrez.docSumTable.columns";
    //this is used to setup the displayed columns
    private TableHeaderColumnItem[] _presModel;
    
    // We describe the general model for all NCBI Entrez DBs. If a field is missing, or
    // you need to add one more: add new constant and increase numbering, but never 
    // insert between existing values !
    private static final int NUM_HDR = 0;
    private static final int Accession_HDR = 1;
    private static final int Identifier_HDR = 2;
    private static final int Description_HDR = 3;
    private static final int Length_HDR = 4;
    private static final int TaxId_HDR = 5;
    private static final int Organism_HDR = 6;
    private static final int CreateDate_HDR = 7;
    private static final int UpdateDate_HDR = 8;
    // Structure specific - begin
    private static final int Experiment_HDR = 9;
    // Structure specific - end
    // Taxonomy specific - begin
    private static final int CommonName_HDR = 10;
    private static final int Rank_HDR = 11;
    private static final int Division_HDR = 12;
    // Taxonomy specific - end
    // ClinVar specific - begin
    private static final int ClinicalSignificance_HDR = 13;
    private static final int Position_HDR = 14;
    private static final int PositionGRCh37_HDR = 15;
    private static final int VariantType_HDR = 16;
    private static final int ClinicalReview_HDR = 17;
    private static final int ClinicalDate_HDR = 18;
    private static final int Trait_HDR = 19;
    // ClinVar specific - end

    // Then, we define the corresponding names. Again, you can add new entries,
    // but never insert.
    // Be aware that these strings also serve as key in the class Summary
    // for NCBI Entrez DocSum preparation: see ESummaryLoader class.
    private static final String[] RES_HEADERS = { "Num", "Accession", "Identifier", "Description", "Length", "TaxId",
        "Organism", "CreateDate", "UpdateDate", "Experiment", "CommonName", "Rank", "Division", "Clinical Significance",
        "Position (GRCh38)","Position (GRCh37)","Variant Type","Last Reviewed", "Review status", "Trait"};


    /**
     * Constructor.
     */
    private EntrezDbSummaryDocPresentationModel(String type, String propertyKey) {
      _defColPropKey = propertyKey;
      switch (type) {
      case "T":
        _presModel = getTaxonomyPresentationModel();
        break;
      case "S":
        _presModel = getStructPresentationModel();
        break;
      case "C":
        _presModel = getClinVarPresentationModel();
        break;
      case "P":
      case "N":
      default:
        _presModel = getNucProtPresentationModel();
      }
    }

    @Override
    public TableHeaderColumnItem[] getPresentationModel() {
      return _presModel;
    }

    private TableHeaderColumnItem[] getClinVarPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,3,19,16,14,15,13,17,18";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[10];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[Accession_HDR], Accession_HDR, false,
          idSet.contains(Accession_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[Description_HDR], Description_HDR, false,
          idSet.contains(Description_HDR));
      refColumnIds[2].setLargest(true);
      refColumnIds[2].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[Trait_HDR], Trait_HDR, false,
          idSet.contains(Trait_HDR));
      refColumnIds[3].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[VariantType_HDR], VariantType_HDR, false,
          idSet.contains(VariantType_HDR));
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[Position_HDR], Position_HDR, false,
          idSet.contains(Position_HDR));
      refColumnIds[6] = new TableHeaderColumnItem(RES_HEADERS[PositionGRCh37_HDR], PositionGRCh37_HDR, false,
          idSet.contains(PositionGRCh37_HDR));
      refColumnIds[7] = new TableHeaderColumnItem(RES_HEADERS[ClinicalSignificance_HDR], ClinicalSignificance_HDR, false,
          idSet.contains(ClinicalSignificance_HDR));
      refColumnIds[7].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[8] = new TableHeaderColumnItem(RES_HEADERS[ClinicalReview_HDR], ClinicalReview_HDR, false,
          idSet.contains(ClinicalReview_HDR));
      refColumnIds[9] = new TableHeaderColumnItem(RES_HEADERS[ClinicalDate_HDR], ClinicalDate_HDR, false,
          idSet.contains(ClinicalDate_HDR));
      return refColumnIds;
    }
    
    private TableHeaderColumnItem[] getTaxonomyPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,5,6,10,11,12";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[6];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[TaxId_HDR], TaxId_HDR, false,
          idSet.contains(TaxId_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[Organism_HDR], Organism_HDR, false,
          idSet.contains(Organism_HDR));
      refColumnIds[2].setLargest(true);
      refColumnIds[2].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[CommonName_HDR], CommonName_HDR, false,
          idSet.contains(CommonName_HDR));
      refColumnIds[3].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[Rank_HDR], Rank_HDR, false,
          idSet.contains(Rank_HDR));
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[Division_HDR], Division_HDR, false,
          idSet.contains(Division_HDR));
      return refColumnIds;
    }
    
    private TableHeaderColumnItem[] getStructPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,3,9";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[6];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[Accession_HDR], Accession_HDR, false,
          idSet.contains(Accession_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[Description_HDR], Description_HDR, false,
          idSet.contains(Description_HDR));
      refColumnIds[2].setLargest(true);
      refColumnIds[2].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[Organism_HDR], Organism_HDR, false,
          idSet.contains(Organism_HDR));
      refColumnIds[3].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[CreateDate_HDR], CreateDate_HDR, false,
          idSet.contains(CreateDate_HDR));
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[Experiment_HDR], Experiment_HDR, false,
          idSet.contains(Experiment_HDR));
      return refColumnIds;
    }

    private TableHeaderColumnItem[] getNucProtPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,3,4,6,7";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[9];
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
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[TaxId_HDR], TaxId_HDR, false, idSet.contains(TaxId_HDR));
      refColumnIds[6] = new TableHeaderColumnItem(RES_HEADERS[Organism_HDR], Organism_HDR, false,
          idSet.contains(Organism_HDR));
      refColumnIds[6].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[7] = new TableHeaderColumnItem(RES_HEADERS[CreateDate_HDR], CreateDate_HDR, false,
          idSet.contains(CreateDate_HDR));
      refColumnIds[8] = new TableHeaderColumnItem(RES_HEADERS[UpdateDate_HDR], UpdateDate_HDR, false,
          idSet.contains(UpdateDate_HDR));
      return refColumnIds;
    }

    @Override
    public String getLengthFieldKey() {
      return RES_HEADERS[Length_HDR];
    }

    @Override
    public String getAccessionFieldKey(){
      return RES_HEADERS[Accession_HDR];
    }
    @Override
    public void saveDefaultColumnModel(String prop) {
      EZEnvironment.setApplicationProperty(_defColPropKey, prop);
    }
  }

}
