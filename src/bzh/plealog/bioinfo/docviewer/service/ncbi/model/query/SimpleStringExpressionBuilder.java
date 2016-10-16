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
package bzh.plealog.bioinfo.docviewer.service.ncbi.model.query;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.api.filter.BRule;
import bzh.plealog.bioinfo.util.CoreUtil;

/**
 * This is a concrete implementation of a BFilter capable of handling plain text queries.
 * 
 * @author Patrick G. Durand
 */
public class SimpleStringExpressionBuilder implements BFilter {

  private String query = null;
  private String name = null;
  private String compiledQuery = null;

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public void compile() throws BFilterException {
    if (StringUtils.isBlank(this.compiledQuery)) {
      if (StringUtils.isNotBlank(this.query)) {
        this.compiledQuery = CoreUtil.replaceAll(this.query, "'", "\"");
        this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, "\"", "\\\"");
        this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, "\n", " ");
        this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, "\r", " ");
        this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, "\t", " ");
        while (this.compiledQuery.contains("  ")) {
          this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, "  ", " ");
        }
        this.compiledQuery = CoreUtil.replaceAll(this.compiledQuery, " ", "+");
      }
    }
  }

  @Override
  public SROutput execute(SROutput bo) throws BFilterException {
    return null;
  }

  @Override
  public void add(BRule rule) throws BFilterException {

  }

  @Override
  public void remove(BRule rule) {

  }

  @Override
  public Iterator<BRule> getRules() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String filterName) {
    this.name = filterName;
  }

  /**
   * Used to store the query (idem as getQuery)
   */
  @Override
  public String getDescription() {
    return this.query;
  }

  /**
   * Used to store the query (idem as setQuery)
   */
  @Override
  public void setDescription(String filterDescription) {
    this.query = filterDescription;
  }

  @Override
  public boolean getExclusive() {
    return false;
  }

  @Override
  public void setExclusive(boolean val) {
  }

  @Override
  public String getHtmlString() {
    StringBuilder result = new StringBuilder();
    result.append("<html><body>");
    if (StringUtils.isNotBlank(this.query)) {
      String html = this.query.replaceAll("\\sAND\\s", "<br>AND ");
      html = html.replaceAll("\\sNOT\\s", "<br>NOT ");
      html = html.replaceAll("\\sOR\\s", "<br>OR ");
      result.append(html);
    }
    result.append("</body></html>");
    return result.toString();
  }

  @Override
  public String getTxtString() {
    // "cytochrome c oxidase subunit I"[all fields] OR "COI"[All Fields] OR
    // "COXI"[All Fields] AND 00000000500[SLEN] : 00000001000[SLEN] NOT
    // "metagenomic"[All Fields] NOT "environmental"[All Fields] NOT
    // "uncultured"[all fields] NOT "unverified"[All fields] NOT "EST"[All
    // fields] NOT "GSS"[All FIelds] NOT "Pseudo"[All Fields] NOT
    // "Tracheophyta"[All Fields]) NOT "mosses"[porgn]
    return this.query;
  }

  public String toString() {
    return this.compiledQuery;
  }

  @Override
  public Object clone() {
    SimpleStringExpressionBuilder filter = new SimpleStringExpressionBuilder();
    filter.setQuery(this.query);
    return filter;
  }

}
