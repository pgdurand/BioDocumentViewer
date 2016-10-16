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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class contains a summary document. Be very careful when updating this
 * class since it is used to serialize objects of this class using an XML
 * framework.
 * 
 * @author Patrick G. Durand
 */
public class SummaryDoc {
  /** Document ID */
  private String id;
  
  /** DocSum XML documents produced by NCBI-EUtils "esummary" have variable content. So,
   * we use a key/value storage system to adapt easily that DocSum content to this 
   * software. 
   */
  private Hashtable<String, String> values;

  public SummaryDoc() {
    values = new Hashtable<String, String>();
  }

  public int size() {
    return values.size();
  }

  public String getValue(String key) {
    return values.get(key);
  }

  public Enumeration<String> getKeys() {
    return values.keys();
  }

  public void clear() {
    values.clear();
  }

  public void add(String key, String value) {
    values.put(key, value);
  }

  public String getId() {
    return id;
  }

  public Hashtable<String, String> getValues() {
    return values;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setValues(Hashtable<String, String> values) {
    this.values = values;
  }

}
