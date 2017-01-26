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
package bzh.plealog.bioinfo.docviewer.api;

import java.io.File;

import bzh.plealog.bioinfo.api.filter.BFilter;

/**
 * Defines the behavior of a component capable of querying a data provider.
 * 
 * @author Patrick G. Durand
 */
public interface QueryEngine {
  public static final int PAGE_SIZE            = 100;

  /**
   * Return the bank type associated to this query engine.
   */
  public BankType getBankType();
  
  /**
   * Returns the query associated to this query engine.
   */
  public BFilter getQuery();

  /**
   * Query the remote server using the filter and return a list of IDs. The two
   * parameters from and nb are used to get a page of IDs. Indeed, a query could
   * return a huge number of Ids in the range [0..n]. These two parameters tell
   * the server from which absolute index within [0..n] (from) it has to return
   * ID, and how many of them (nb).
   *
   * @param from
   *          see method comments. Zero-based value.
   * @param nb
   *          see method comments
   *
   * @return a DbSearchResult or null if the query failed.
   */
  public Search getIds(int from, int nb);
  /**
   * Query the remote server using the filter and return a list of IDs.
   *
   * @return DbSearchResult or null if the query failed.
   */
  public Search getIds();

  /**
   * Query the remote server using the filter and return a DbSummaryResult.
   *
   * @return a DbSummaryResult object or null if the query failed.
   */
  public Summary getSummary();
  /**
   * Query the remote server using the filter and return a DbSummaryResult. The
   * two parameters from and nb are used to get a page of Summary documents.
   * Indeed, a query could return a huge number of Summary documents in the
   * range [0..n]. These two parameters tell the server from which absolute
   * index within [0..n] (from) it has to return a Summary document, and how
   * many of them (nb).
   *
   * @param from
   *          see method comments. Zero-based value.
   * @param nb
   *          see method comments
   *
   * @return a DbSummaryResult object or null if the query failed.
   */
  public Summary getSummary(int from, int nb) ;
  
  /**
   * Returns the server configuration.
   */
  public ServerConfiguration getServerConfiguration();
  
  /**
   * Load sequences from the remote server.
   */
  public File load(String ids, String dbCode, boolean fullEntryFormat);
  
  /**
   * Returns data page size.
   * 
   * Default value is {@link QueryEngine#PAGE_SIZE}
   */
  public default int getPageSize(){
    return QueryEngine.PAGE_SIZE;
  }
  
  /**
   * Figures out whether or not a QueryEngine enables to provide Search and
   * Summary using pagination. Default is true .
   */
  public default boolean enablePagination(){
    return true;
  }
}
