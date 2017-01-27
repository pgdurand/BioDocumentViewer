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
package bzh.plealog.bioinfo.docviewer.service.ncbi.io;

import java.io.File;
import java.util.Iterator;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.EntrezQueryExpressionBuilder;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.SimpleStringExpressionBuilder;

/**
 * This is the engine capable of querying the NCBI Entrez server to fetch some
 * sequence or structure documents. Server query relies on a BFilter that
 * contain the expression used to select the requested data. As the class name
 * implies it is designed to use the NCBI Entrez system. However, it could be
 * used with other servers as soon as they are capable of simulating the NCBI
 * server behavior.<br>
 * See:
 * http://www.ncbi.nlm.nih.gov/bookshelf/br.fcgi?book=coursework&part=eutils<br>
 * See: http://eutils.ncbi.nlm.nih.gov/entrez/query/static/esearch_help.html<br>
 *
 * @author Patrick G. Durand
 */
public class EntrezQueryEngine implements QueryEngine {
  private BFilter entrezQuery;
  private BankType dbName;
  private int defPageSize = QueryEngine.PAGE_SIZE;
  private EntrezServerConfiguration _serverConfig;
  
  /**
   * No default constructor available.
   */
  private EntrezQueryEngine() {
  }

  /**
   * Constructor.
   *
   * @param dbName
   *          the database to query.
   * @param query
   *          the filter. Actually the constructor expects to have a
   *          EntrezQueryExpressionBuilder object.
   *
   * @throws QueryEngineException
   *           if query is not an instance of EntrezQueryExpressionBuilder, if dbName is
   *           null of if the configuration resource cannot be found or read.
   */
  public EntrezQueryEngine(BankType dbName, BFilter query) throws QueryEngineException {
    this();

    if ((query instanceof EntrezQueryExpressionBuilder == false) && (query instanceof SimpleStringExpressionBuilder == false))
      throw new QueryEngineException("invalid filter type");
    if (dbName == null)
      throw new QueryEngineException("invalid database name");

    entrezQuery = query;
    this.dbName = dbName;
    _serverConfig = new EntrezServerConfiguration();
  }

  public Object clone() {
    EntrezQueryEngine engine = new EntrezQueryEngine();
    engine.dbName = this.dbName;
    engine.defPageSize = this.defPageSize;
    engine.entrezQuery = (BFilter) this.entrezQuery.clone();
    engine._serverConfig = new EntrezServerConfiguration(this._serverConfig);
    return engine;
  }

  /**
   * Returns the number of documents retrieved on each call of methods
   * getIds and getSummary. Default value is DocViewerConfig.PAGE_SIZE.
   */
  public int getDefaultPageSize() {
    return defPageSize;
  }

  /**
   * Sets the number of documents retrieved on each call of methods
   * getIds and getSummary.
   */
  public void setDefaultPageSize(int defPageSize) {
    this.defPageSize = defPageSize;
  }

  /**
   * Prepare the URL to query the remote server for IDs.
   */
  private String prepareExecQuery(String query, int from, int nb) {
    return _serverConfig.getQueryServiceUrl(query, dbName.getCode(), from, nb);
  }

  /**
   * Prepare the URL to query the remote server for document summaries.
   */
  private String prepareSummaryUrl(String ids) {
    return _serverConfig.getSummaryServiceUrl(dbName.getCode(), ids);
  }

  public Search getIds(int from, int nb) {
    Search res;
    String q;

    entrezQuery.compile();
    q = entrezQuery.toString();
    EZLogger.debug(String.format("getIds: %s ", q));
    EZLogger.debug(String.format("getIds: from %d, page: %d ", from, nb));
    res = dbName.getSearch(HTTPBasicEngine.doGet(prepareExecQuery(q, from, nb)));
    if (res.getError() != null) {
      throw new QueryEngineException(res.getError());
    }
    return res;
  }

  public Search getIds() {
    return getIds(0, defPageSize);
  }

  public Summary getSummary() {
    return getSummary(0, defPageSize);
  }

  public BFilter getQuery() {
    return entrezQuery;
  }

  public Summary getSummary(int from, int nb) {
    Search search;
    Summary sum;
    StringBuffer buf;
    Iterator<String> ids;
    String str;
    
    search = getIds(from, nb);
    
    buf = new StringBuffer();
    ids = search.getIds().iterator();
    while (ids.hasNext()) {
      buf.append(ids.next());
      if (ids.hasNext())
        buf.append(",");
    }
    str = buf.toString();
    EZLogger.debug(String.format("getSummary: %s ", str));
    sum = dbName.getSummary(HTTPBasicEngine.doGet(prepareSummaryUrl(str)));

    if (sum.getError() != null) {
      throw new QueryEngineException(sum.getError());
    }
    sum.setTotal(search.getTotal());
    sum.setFrom(search.getFrom());
    return sum;
  }

  public BankType getBankType(){
    return dbName;
  }

  public File load(String ids, String dbCode, boolean fullEntryFormat) {
    String  url;
    File    fTmp;

    if (fullEntryFormat) {
      url = _serverConfig.getEntryServiceURL(dbCode, ids);
    }
    else{
      url = _serverConfig.getFastaServiceURL(dbCode, ids);
    }

    fTmp = HTTPBasicEngine.doGet(url);
    
    return fTmp;
  }

  public ServerConfiguration getServerConfiguration(){
    return _serverConfig;
  }
}
