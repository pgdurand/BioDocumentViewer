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
package bzh.plealog.bioinfo.docviewer.service.ebi.io;

import java.io.File;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.query.EbiQueryExpressionBuilder;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.SimpleStringExpressionBuilder;

/**
 * This is the engine capable of querying the EBI-Eye server to fetch some
 * sequence documents. Server query relies on a BFilter that
 * contain the expression used to select the requested data. As the class name
 * implies it is designed to use the EBE-Eye system. 
 *
 * @author Patrick G. Durand
 */
public class EbiQueryEngine implements QueryEngine {

  private EbiServerConfiguration _serverConfig;
  private BFilter _ebiQuery;
  private BankType _dbName;
  private int _defPageSize = DocViewerConfig.PAGE_SIZE;

  /**
   * No default constructor available.
   */
  private EbiQueryEngine() {
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
  public EbiQueryEngine(BankType dbName, BFilter query) throws QueryEngineException {
    this();

    if ((query instanceof EbiQueryExpressionBuilder == false) && (query instanceof SimpleStringExpressionBuilder == false))
      throw new QueryEngineException("invalid filter type");
    if (dbName == null)
      throw new QueryEngineException("invalid database name");

    _ebiQuery = query;
    this._dbName = dbName;
    _serverConfig = new EbiServerConfiguration();
  }

  public Object clone() {
    EbiQueryEngine engine = new EbiQueryEngine();
    engine._dbName = this._dbName;
    engine._defPageSize = this._defPageSize;
    engine._ebiQuery = (BFilter) this._ebiQuery.clone();
    engine._serverConfig = new EbiServerConfiguration(this._serverConfig);
    return engine;
  }

  /**
   * Returns the number of documents retrieved on each call of methods
   * getIds and getSummary. Default value is DocViewerConfig.PAGE_SIZE.
   */
  public int getDefaultPageSize() {
    return _defPageSize;
  }

  /**
   * Sets the number of documents retrieved on each call of methods
   * getIds and getSummary.
   */
  public void setDefaultPageSize(int defPageSize) {
    this._defPageSize = defPageSize;
  }
  /**
   * Prepare the URL to query the remote server for IDs.
   */
  private String prepareExecQuery(String query, int from, int nb) {
    String url;
    
    url = _serverConfig.getSummaryUrl(query, _dbName.getCode(), from, nb);
    
    if (url==null)
      throw new QueryEngineException("No URL found to query: "+_dbName.getCode());
    
    return url;
  }

  public Search getIds(int from, int nb) {
    Search res;
    String q;

    _ebiQuery.compile();
    q = _ebiQuery.toString();
    EZLogger.debug(String.format("getIds: %s ", q));
    EZLogger.debug(String.format("getIds: from %d, page: %d ", from, nb));
    
    res = _dbName.getSearch(HTTPBasicEngine.doGet(prepareExecQuery(q, from, nb)));
    if (res.getError() != null) {
      throw new QueryEngineException(res.getError());
    }

    return res;
  }

  public Summary getSummary(int from, int nb) {
    Summary sum;
    String  q;
    
    _ebiQuery.compile();
    q = _ebiQuery.toString();
    EZLogger.debug(String.format("getSummary: from %d, page: %d ", from, nb));
    sum = _dbName.getSummary(HTTPBasicEngine.doGet(prepareExecQuery(q, from, nb)));
    if (sum.getError() != null) {
      throw new QueryEngineException(sum.getError());
    }
    //adjust value since WsResult does not provide data as NCBI does (i.e. current page of ids)
    sum.setFrom(from);
    return sum;
  }

  public Search getIds() {
    return getIds(0, _defPageSize);
  }

  public Summary getSummary() {
    return getSummary(0, _defPageSize);
  }

  public BFilter getQuery() {
    return _ebiQuery;
  }

  public BankType getBankType(){
    return _dbName;
  }
  public File load(String ids, String dbCode, boolean fullEntryFormat) {
    String url;
    File fTmp;

    url = _serverConfig.getFetchUrl(dbCode, ids, !fullEntryFormat);
    if (url == null)
      throw new RuntimeException( "URL not found to query: " + dbCode);

    fTmp = HTTPBasicEngine.doGet(url);

    return fTmp;
  }
  public ServerConfiguration getServerConfiguration(){
    return _serverConfig;
  }

}
