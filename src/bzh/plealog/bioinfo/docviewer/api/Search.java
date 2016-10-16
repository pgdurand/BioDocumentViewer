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
 * This class contains the result of a query. Be very careful when updating this
 * class since it is used to serialize objects of this class with an XML
 * framework.
 * 
 * @author Patrick G Durand
 */
public class Search {
  // from: index of the first ID contained here. Between 0 and total.
  private int from;
  // total number of IDs validated by the query
  private int total;
  // error: not null when an error occurred while querying the remote server.
  private String error;

  private List<String> ids;

  public Search() {
    ids = new ArrayList<String>();
  }

  public void addId(String id) {
    ids.add(id);
  }

  public int nbIds() {
    return ids.size();
  }

  public String getId(int idx) {
    return ids.get(idx);
  }

  public int getFrom() {
    return from;
  }

  public int getTotal() {
    return total;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setFrom(int from) {
    this.from = from;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

}
