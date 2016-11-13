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
package bzh.plealog.bioinfo.docviewer.service.ensembl;

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
import bzh.plealog.bioinfo.docviewer.service.ensembl.io.EnsemblQueryEngine;
import bzh.plealog.bioinfo.docviewer.service.ensembl.io.EnsemblServerConfiguration;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.EnsemblSearchLoader;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.EnsemblSummaryLoader;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.query.EnsemblQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.query.VariationQueryModel;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * Describe the banks from Ensembl that are available for querying with this software.
 * Right now, provide access to Variation DB. 
 * More can be added if needed: adapt this code accordingly.
 * 
 * @author Patrick G. Durand
 */
public enum EnsemblBank implements BankType {
  //format: data code (one letter), bank name for user presentation, ENSEMBL bank code, 
  // figure out whether or not this bank enables sequence retrieval, reader type
  VARIATION  ("V", "Variation", "variation", false, ReaderType.UNKNOWN)
  ; 
  
  // By region: 
  //   http://grch37.rest.ensembl.org/documentation/info/overlap_region
  //   http://grch37.rest.ensembl.org/overlap/region/human/7:140424943-140624564?feature=gene;feature=transcript;feature=cds;feature=exon;content-type=application/json
  // by gene name:
  // gene name - > ensembl id: 
  //   http://rest.ensembl.org/xrefs/symbol/human/BRAF?object_type=gene
  // enesembl id -> variations:
  //   http://rest.ensembl.org/overlap/id/ENSG00000157764?feature=variation;variant_set=ClinVar
  // To get XML: set Header with "Accept: text/xml"
  
  //ena_sequence
  private final String type;
  private final ReaderType rType;
  private final String userName;
  private final String dbName;
  private final SummaryDocPresentationModel pModel;
  private final EnsemblQueryModel filterModel;
  private final boolean enableSeqRetrieval;
  
  EnsemblBank(String type, String userName, String entrezName, boolean enableSeqRetrieval, ReaderType rType) {
    //just in case: we add some control on the type
    if (type.length()!=1)
      throw new RuntimeException("Type BankType must be a single letter");
    this.type = type.toUpperCase();
    this.userName = userName;
    this.dbName = entrezName;
    this.enableSeqRetrieval = enableSeqRetrieval;
    this.rType = rType;
    
    switch (type) {
    // for each bank type, we setup the filter model (used to prepare Query Dialogue Box)
    // and the presentation model (used to display results).
    case "V":
    default:
      pModel = new EnsemblDbSummaryDocPresentationModel("V", "ens.var.docSumTable.columns");
      filterModel = new VariationQueryModel();
      break;
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
    return dbName;
  }

  public QueryModel getQueryModel() {
    return filterModel;
  }
  
  public String getProviderName(){
    return "Ensembl";
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
    //This method is called by the EnsemblQueryEngine when retrieving an answer
    //from the Ensembl Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    //All databanks have a similar processing
    return EnsemblSummaryLoader.load(f);
  }
  
  public Search getSearch(File f){
    //This method is called by the EnsemblQueryEngine when retrieving an answer
    //from the Ensembl Server. That answer is stored in a file: we read that
    //file here and prepare an appropriate data model
    
    //This method actually retrieves list of IDs (or an error). Similar for all Ensembl banks.
    return EnsemblSearchLoader.load(f);
  }
  
  public QueryEngine prepareQueryEngine(BFilter query){
    return new EnsemblQueryEngine(this, query);
  }

  public ServerConfiguration getServerConfiguration(){
    return new EnsemblServerConfiguration();
  }
  
  /**
   * Setup a presentation model to display Ensembl Documents.
   */
  public static class EnsemblDbSummaryDocPresentationModel implements SummaryDocPresentationModel {

    //this key is used to store displayed columns in Application Preferences 
    private String _defColPropKey = "ens.docSumTable.columns";
    //this is used to setup the displayed columns
    private TableHeaderColumnItem[] _presModel;

    // We describe the general model for all Ensembl DBs. If a field is missing, or
    // you need to add one more: add new constant and increase numbering, but never 
    // insert between existing values !
    public static final int NUM_HDR = 0;
    public static final int Identifier_HDR = 1;
    public static final int ASSEMBLY_HDR = 2; //e.g. GRCh38
    public static final int LOCATION_HDR = 3;//chr:start-end (+/-)
    public static final int VARIATION_HDR = 4;// e.g. A/C for SNP
    public static final int CONSEQUENCE_HDR = 5;// e.g. 3_prime_UTR_variant
    public static final int SOURCE_HDR = 6;//e.g. dbSNP

    // Then, we define the corresponding names. Again, you can add new entries,
    // but never insert.
    // Be aware that these strings also serve as key in the class Summary
    // for Ensembl DocSum preparation: see EensemblSummaryLoader class.
    public static final String[] RES_HEADERS = { "Num", "Identifier", 
        "Assembly", "Location", "Variation", "Consequence", "Source"};

    /**
     * Constructor.
     */
    private EnsemblDbSummaryDocPresentationModel(String type, String propertyKey) {
      _defColPropKey = propertyKey;
      switch (type) {
      case "V":
      default:
        _presModel = getVariationPresentationModel();
      }
    }
     private TableHeaderColumnItem[] getVariationPresentationModel() {
      String defColIDs;
      List<Integer> idSet;
      TableHeaderColumnItem[] refColumnIds;

      defColIDs = EZEnvironment.getApplicationProperty(_defColPropKey);
      if (defColIDs == null)
        defColIDs = "0,1,2,6,3,4,5,6";//XXX_HDR values
      idSet = TableColumnManager.getDefColumns(defColIDs);
      refColumnIds = new TableHeaderColumnItem[7];
      refColumnIds[0] = new TableHeaderColumnItem(RES_HEADERS[NUM_HDR], NUM_HDR, true, idSet.contains(NUM_HDR));
      refColumnIds[1] = new TableHeaderColumnItem(RES_HEADERS[Identifier_HDR], Identifier_HDR, false,
          idSet.contains(Identifier_HDR));
      refColumnIds[2] = new TableHeaderColumnItem(RES_HEADERS[ASSEMBLY_HDR], ASSEMBLY_HDR, false,
          idSet.contains(ASSEMBLY_HDR));
      refColumnIds[3] = new TableHeaderColumnItem(RES_HEADERS[LOCATION_HDR], LOCATION_HDR, false,
          idSet.contains(LOCATION_HDR));
      refColumnIds[4] = new TableHeaderColumnItem(RES_HEADERS[VARIATION_HDR], VARIATION_HDR, false,
          idSet.contains(VARIATION_HDR));
      refColumnIds[5] = new TableHeaderColumnItem(RES_HEADERS[CONSEQUENCE_HDR], CONSEQUENCE_HDR, false,
          idSet.contains(CONSEQUENCE_HDR));
      refColumnIds[5].setLargest(true);
      refColumnIds[5].setHorizontalAlignment(SwingConstants.LEFT);
      refColumnIds[6] = new TableHeaderColumnItem(RES_HEADERS[SOURCE_HDR], SOURCE_HDR, false,
          idSet.contains(SOURCE_HDR));
      refColumnIds[6].setHorizontalAlignment(SwingConstants.LEFT);
      return refColumnIds;
    }
    @Override
    public TableHeaderColumnItem[] getPresentationModel() {
      return _presModel;
    }
    @Override
    public String getLengthFieldKey() {
      return null;
    }
    @Override
    public String getAccessionFieldKey() {
      return RES_HEADERS[Identifier_HDR];
    }
    @Override
    public void saveDefaultColumnModel(String prop) {
      //EZEnvironment.setApplicationProperty(_defColPropKey, prop);
    }
  }

}
