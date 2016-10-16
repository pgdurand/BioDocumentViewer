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
 * A bank type.
 * 
 * @author Patrick G. Durand
 * */
public interface BankType {
  /**
   * Return the type of the bank. Must be an upper-case single-letter string.
   */
  public String getType();

  /**
   * Return the reader type to use read entries coming from this bank.
   */
  public ReaderType getReaderType();
  
  /**
   * Return a human friendly bank name.
   */
  public String getUserName();

  /**
   * Return the bank code that has to be used to query the remote server.
   */
  public String getCode();
  
  /**
   * Return the name of the provider.
   */
  public String getProviderName();
  
  /**
   * Return the query data model that fits to that bank.
   */
  public QueryModel getQueryModel();
  
  /**
   * Return the data model used for presentation purpose.
   * */
  public SummaryDocPresentationModel getPresentationModel();
  
  /**
   * Figures out whether or not this bank can be queried to retrieve sequences.
   */
  public boolean enableSequenceRetrieval();
  
  /**
   * Return the summary of a result. QueryEngine is responsible to query the remote server
   * to get some data. However, this BankType is responsible for reading the data coming from the
   * server and prepare a data model. Method should throws an Exception if something wrong occurs.
   * 
   */
  public Summary getSummary(File f);
  
  /**
   * Result the list of IDs of a result. QueryEngine is responsible to query the remote server
   * to get some data. However, this BankType is responsible for reading the data coming from the
   * server and prepare a data model. Method should throws an Exception if something wrong occurs.
   * 
   */
  public Search getSearch(File f);
  
  /**
   * Prepare a query engine given a query.
   */
  public QueryEngine prepareQueryEngine(BFilter query);
  
  /**
   * Returns the configuration of the server used to run queries and retrieve data.
   */
  public ServerConfiguration getServerConfiguration();
  
}
