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

import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BFilterFactory;
import bzh.plealog.bioinfo.api.filter.BRuleFactory;

/**
 * As a reminder the query system of the Document Viewer relies on the BFilter
 * framework originally designed to handle SROutput objects. Such objects are
 * annotated BLAST results.<br>
 * <br>
 * To avoid creating a new framework providing a filter editor we have diverted
 * the original BFilter API and reuse it in a slightly different manner. A
 * BFilter is not used to filter data. Instead it is used to setup a
 * databank query relying on some URL API.<br>
 * <br>
 * A simple example: NCBI Entrez eUtils API. So, a QueryModel consists of a
 * BFilter made of BRules and their concrete implementations are responsible for
 * making an Entrez expression.
 * 
 * @author Patrick G. Durand
 */
public interface QueryModel extends BDataAccessors {
  // TODO: need improvement: we should avoid extending BDataAccessors as this one contains
  // fields to handle SROutput data objects (aka BLAST/PLAST results)

  /**
   * Returns a BFilter factory.
   */
  public BFilterFactory getFilterFactory();

  /**
   * Returns a BRule factory.
   */
  public BRuleFactory getRuleFactory();
}
