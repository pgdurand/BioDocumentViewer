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
package bzh.plealog.bioinfo.docviewer.service.ensembl.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.query.EnsemblQueryExpressionBuilder;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.query.EnsemblQueryModel;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.SimpleStringExpressionBuilder;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;

import com.plealog.genericapp.api.log.EZLogger;

public class EnsemblQueryEngine implements QueryEngine {
  private EnsemblServerConfiguration _serverConfig;
  private BFilter _ensQuery;
  private BankType _dbName;
  private int _defPageSize = DocViewerConfig.PAGE_SIZE;

  //Ensembl API does not support pagination. So we support that feature here.
  //For now: everything in RAM.
  private Search _searchData;
  private Summary _summaryData;
  
  private Hashtable<String, String> _header_attrs;
  
  /**
   * No default constructor available.
   */
  private EnsemblQueryEngine() {
    _header_attrs = new Hashtable<>();
    _header_attrs.put("Accept", "text/xml");
  }

  /**
   * Constructor.
   *
   * @param dbName
   *          the database to query.
   * @param query
   *          the filter. Actually the constructor expects to have a
   *          EbiQueryExpressionBuilder object.
   *
   * @throws QueryEngineException
   *           if query is not an instance of EbiQueryExpressionBuilder, if dbName is
   *           null of if the configuration resource cannot be found or read.
   */
  public EnsemblQueryEngine(BankType dbName, BFilter query) throws QueryEngineException {
    this();

    if ((query instanceof EnsemblQueryExpressionBuilder == false) && (query instanceof SimpleStringExpressionBuilder == false))
      throw new QueryEngineException("invalid filter type");
    if (dbName == null)
      throw new QueryEngineException("invalid database name");

    _ensQuery = query;
    this._dbName = dbName;
    _serverConfig = new EnsemblServerConfiguration();
  }

  public Object clone() {
    EnsemblQueryEngine engine = new EnsemblQueryEngine();
    engine._dbName = this._dbName;
    engine._defPageSize = this._defPageSize;
    engine._ensQuery = (BFilter) this._ensQuery.clone();
    engine._serverConfig = new EnsemblServerConfiguration(this._serverConfig);
    return engine;
  }

  @Override
  public BankType getBankType() {
    return _dbName;
  }

  @Override
  public BFilter getQuery() {
    return _ensQuery;
  }

  @Override
  public Search getIds(int from, int nb) {
    prepareSearchData();
    return getSearchPage(from, nb);
  }

  @Override
  public Search getIds() {
    prepareSearchData();
    return getSearchPage(0, _defPageSize);
  }

  @Override
  public Summary getSummary() {
    prepareSummaryData();
    return getSummaryPage(0, _defPageSize);
  }

  @Override
  public Summary getSummary(int from, int nb) {
    prepareSummaryData();
    return getSummaryPage(from, nb);
  }

  @Override
  public ServerConfiguration getServerConfiguration() {
    return _serverConfig;
  }

  @Override
  public File load(String ids, String dbCode, boolean fullEntryFormat) {
    // not available for now
    return null;
  }

  private String getEnsemblGeneId(){
    String species="", gene_name="", key, value;
    StringTokenizer tokenizer, tokenizer2;
    Search res;
    
    // Step 1: get gene name and species from query
    // see EnsemblQueryExpressionBuilder.compile() to see how the query is created
    tokenizer = new StringTokenizer(_ensQuery.toString(),"|");
    while(tokenizer.hasMoreTokens()){
      tokenizer2 = new StringTokenizer(tokenizer.nextToken(),"=");
      key = tokenizer2.nextToken();
      value = tokenizer2.nextToken();
      if (key.equals(EnsemblQueryModel.GENE_NAME_KEY)){
        gene_name=value;
      }
      else if (key.equals(EnsemblQueryModel.SPECIES_KEY)){
        species=value;
      }
    }
    
    if (gene_name.isEmpty()){
      throw new QueryEngineException("gene name is missing");
    }
    if (species.isEmpty()){
      throw new QueryEngineException("species is missing");
    }
    
    EZLogger.debug("gene name: "+gene_name);
    EZLogger.debug("species: "+species);
    
    // using gene name and sepeices, query Ensembl to get Ensembl IDs
    res = _dbName.getSearch(HTTPBasicEngine.doGet(_serverConfig.getGene2EnsgIdUrl(species, gene_name), _header_attrs));
    if (res.getError() != null) {
      throw new QueryEngineException(res.getError());
    }
    if (res.getIds().isEmpty()){
      throw new QueryEngineException("unable to retrieve Ensembl ID for gene name:"+gene_name);
    }
    // we need an official ENSG ID...
    for(String id : res.getIds()){
      if (id.startsWith("ENSG")){
        return id;
      }
    }
    throw new QueryEngineException("unable to find Ensembl ID for gene name:"+gene_name);
  }
  
  private void prepareSearchData(){
    if (_searchData==null){
      String ensId = getEnsemblGeneId();
      Search res = _dbName.getSearch(HTTPBasicEngine.doGet(_serverConfig.getFetchVariationUrl(ensId), _header_attrs));
      if (res.getError() != null) {
        throw new QueryEngineException(res.getError());
      }
      _searchData = res;
    }
  }
  
  private void prepareSummaryData(){
    if (_summaryData==null){
      String ensId = getEnsemblGeneId();
      Summary res = _dbName.getSummary(HTTPBasicEngine.doGet(_serverConfig.getFetchVariationUrl(ensId), _header_attrs));
      if (res.getError() != null) {
        throw new QueryEngineException(res.getError());
      }
      _summaryData = res;
    }
  }
  
  private Search getSearchPage(int from, int nb){
    Search s = new Search();
    ArrayList<String> ids = new ArrayList<>();
    
    int total = from+nb;
    for(int i=from; i<total;i++){
      ids.add(_searchData.getId(i));
    }
    s.setIds(ids);
    s.setFrom(from);
    s.setTotal(_searchData.getTotal());
    return s;
  }
  
  private Summary getSummaryPage(int from, int nb){
    Summary s;
    s = new Summary();
    
    int total = from+nb;
    for(int i=from; i<total;i++){
      s.addDoc(_summaryData.getDoc(i));
    }
    
    s.setFrom(from);
    s.setTotal(_summaryData.getTotal());
    return s;
  }
}
