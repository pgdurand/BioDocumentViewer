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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the list of Summary Documents obtained from a query. Be
 * very careful when updating this class since it is used to serialize objects
 * of this class using an XML framework.
 * 
 * @author Patrick G. Durand
 */
public class Summary {
  // list of documents
  private List<SummaryDoc> docs;
  // from: index of the first doc contained here. Between 0 and total.
  private int from;
  // total number of IDs validated by the query
  private int total;

  private String error;
  
  public Summary() {
    docs = new ArrayList<SummaryDoc>();
  }

  public void addDoc(SummaryDoc doc) {
    docs.add(doc);
  }

  public int nbDocs() {
    return docs.size();
  }

  public SummaryDoc getDoc(int idx) {
    return docs.get(idx);
  }

  public List<SummaryDoc> getDocs() {
    return docs;
  }

  public void setDocs(List<SummaryDoc> docs) {
    this.docs = docs;
  }

  public int getFrom() {
    return from;
  }

  public int getTotal() {
    return total;
  }

  public void setFrom(int from) {
    this.from = from;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
  
}
