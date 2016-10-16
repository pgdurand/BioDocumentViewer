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
package bzh.plealog.bioinfo.docviewer.service.ncbi.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.Count;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.ERROR;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.ESearchResult;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.FieldNotFound;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.Id;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.IdList;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.OutputMessage;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.PhraseIgnored;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.PhraseNotFound;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.QuotedPhraseNotFound;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.RetStart;

/**
 * A convenient class to transform a ESearchResult object into a Search object.
 * The former is only used for XML serialization while the latter is used for
 * the business of this software.
 * 
 * @author Patrick G. Durand
 */
public class EntrezSearchLoader {
  public static Search load(File xml) {
    String value;
    Search search;
    ESearchResult result;

    // We create a new search object.
    search = new Search();

    try {
      // read XML file into an appropriate data model
      result = JAXB.unmarshal(xml, ESearchResult.class);

      // first, we check if we have an error message
      value = getErrorMessage(result);
      if (value != null) {
        search.setError(value);
        return search;
      }

      // otherwise, we get data
      for (Object o : result.getDataElements()) {
        if (o instanceof RetStart) {
          // Service provider should report current page of ids. This is of for
          // for NCBI, so we get the value.
          // Otherwise, we would have to update that value here:
          // NcbiQueryEngine.getIds(int from, int nb)
          search.setFrom(Integer.valueOf(((RetStart) o).getvalue()));
        } else if (o instanceof Count) {
          // Service provider should report the grand total number of documents
          // available in the DB. ok for NCBI, so we retrieve that value.
          // Otherwise, we would have to update that value here:
          // NcbiQueryEngine.getIds(int from, int nb)
          search.setTotal(Integer.valueOf(((Count) o).getvalue()));
        } else if (o instanceof IdList) {
          // first list of iDs
          ArrayList<String> ids = new ArrayList<>();
          for (Id id : ((IdList) o).getId()) {
            ids.add(id.getvalue());
          }
          search.setIds(ids);
        }
      }
    } catch (Exception e) {
      search.setError(String.format("Unable to read NCBI answer: %s", e.getMessage()));
    }

    return search;
  }

  private static String getErrorMessage(ESearchResult result) {
    StringBuffer buf;
    boolean bFound = false;

    // first, JAXB may read a wrong XML file, in such a case, result is empty
    if ((result.getDataElements() == null || result.getDataElements().isEmpty()) && result.getErrorList() == null
        && result.getWarningList() == null) {
      return "empty file or wrong format";
    }
    // second, check for error from ERROR
    for (Object o : result.getDataElements()) {
      if (o instanceof ERROR) {
        return ((ERROR) o).getvalue();
      }
    }
    buf = new StringBuffer();
    // third, check for error from ErrorList/WarningList
    if (result.getErrorList() != null) {
      Iterator<FieldNotFound> iter = result.getErrorList().getFieldNotFound().iterator();
      while (iter.hasNext()) {
        buf.append(iter.next().getvalue());
        if (iter.hasNext()) {
          buf.append(", ");
        }
      }
      Iterator<PhraseNotFound> iter2 = result.getErrorList().getPhraseNotFound().iterator();
      if (iter2.hasNext()) {
        if (buf.length() != 0)
          buf.append("; ");
        while (iter2.hasNext()) {
          buf.append(iter2.next().getvalue());
          if (iter2.hasNext()) {
            buf.append(", ");
          }
        }
      }
    }
    if (result.getWarningList() != null) {
      if (buf.length() != 0)
        buf.append(": ");
      Iterator<PhraseIgnored> iter = result.getWarningList().getPhraseIgnored().iterator();
      while (iter.hasNext()) {
        bFound = true;
        buf.append(iter.next().getvalue());
        if (iter.hasNext()) {
          buf.append(", ");
        }
      }
      Iterator<QuotedPhraseNotFound> iter2 = result.getWarningList().getQuotedPhraseNotFound().iterator();
      if (iter2.hasNext()) {
        if (bFound)
          buf.append("; ");
        bFound = false;
        while (iter2.hasNext()) {
          bFound = true;
          buf.append(iter2.next().getvalue());
          if (iter2.hasNext()) {
            buf.append(", ");
          }
        }
      }
      Iterator<OutputMessage> iter3 = result.getWarningList().getOutputMessage().iterator();
      if (iter3.hasNext()) {
        if (bFound)
          buf.append("; ");
        while (iter3.hasNext()) {
          buf.append(iter3.next().getvalue());
          if (iter3.hasNext()) {
            buf.append(", ");
          }
        }
      }
    }
    if (buf.length() != 0) {
      return buf.toString();
    } else {
      return null;
    }
  }
}
