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
package bzh.plealog.bioinfo.docviewer.service.ebi.model;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsDiagnostics;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsEntry;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsResult;

public class EbiSearchLoader {
  public static Search load(File xml) {
    String value;
    Search search;
    WsResult result;

    // We create a new search object.
    search = new Search();

    try {
      // read XML file into an appropriate data model
      result = JAXB.unmarshal(xml, WsResult.class);

      // first, we check if we have an error message
      value = getErrorMessage(result);
      if (value != null) {
        search.setError(value);
        return search;
      }

      ArrayList<String> ids = new ArrayList<>();

      for (WsEntry entry : result.getEntries().getEntry()) {
        ids.add(entry.getId());
      }
      search.setIds(ids);
      // Service provider should report current page of ids. This is not the
      // case for EBI, so we set from to zero, and this value will be updated
      // accordingly here: EbiQueryEngine.getIds(int from, int nb)
      search.setFrom(0);
      // Service provider should report the grand total number of documents
      // available in the DB. ok for EBI, so we retrieve that value.
      // Otherwise, we would have to update that value here:
      // EbiQueryEngine.getIds(int from, int nb)
      search.setTotal(result.getHitCount());
    } catch (Exception e) {
      search.setError(String.format("Unable to read EBI-Eye answer: %s", e.getMessage()));
    }

    return search;

  }

  private static String getErrorMessage(WsResult result) {
    WsDiagnostics diags = result.getDiagonostics();
    return diags != null ? diags.getMessage() : null;
  }
}
