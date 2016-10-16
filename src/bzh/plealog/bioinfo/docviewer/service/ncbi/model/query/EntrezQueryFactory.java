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

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.api.filter.BFilterFactory;
import bzh.plealog.bioinfo.api.filter.BOperatorAccessors;
import bzh.plealog.bioinfo.io.filter.BFilterIO;

/**
 * This is an implementation of interface BFilterFactory to create NCBI Entrez
 * filters.
 * 
 * @author Patrick G. Durand
 */
public class EntrezQueryFactory implements BFilterFactory {

  // this is for special use: provide directly an Entrez Expression. Yet to be
  // implemented...
  public static final String QUERY_IS_AN_ENTREZ_EXPRESSION = "query as a string";

  public BFilter createFilter(BOperatorAccessors fModel, String filterName) throws BFilterException {
    if (filterName == null || filterName.length() == 0)
      throw new BFilterException("filter name is not defined.");
    if (filterName.equals(QUERY_IS_AN_ENTREZ_EXPRESSION)) {
      return new SimpleStringExpressionBuilder();
    } else {
      return new EntrezQueryExpressionBuilder((EntrezQueryModel) fModel, filterName);
    }
  }

  public BFilter createFilter(BOperatorAccessors fModel, BFilterIO filter) throws BFilterException {
    if (filter == null)
      throw new BFilterException("filter I/O object is not defined.");
    return new EntrezQueryExpressionBuilder((EntrezQueryModel) fModel, filter);
  }
}
