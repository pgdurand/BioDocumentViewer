/* Copyright (C) 2017 Inria
 * Author: Patrick G. Durand
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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.format;

import java.util.Iterator;
import java.util.List;

import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.SimpleElement;

/**
 * Utility methods to handle data model.
 * 
 * @author Patrick G. Durand
 */
public class Utilities {
  /**
   * Make a string from a list of SimpleElement.
   * 
   * @param list
   *          list of element data
   * @param separator
   *          string used to separate each element in the returned string
   * 
   * @return a string or '-' (without quotes) if list is null or empty
   */
  public static String makeString(List<? extends SimpleElement> list, String separator) {
    if (list == null || list.isEmpty()) {
      return "-";
    }
    StringBuffer buf = new StringBuffer();
    Iterator<? extends SimpleElement> iter = list.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().getvalue());
      if (iter.hasNext()) {
        buf.append(separator);
      }
    }
    return buf.toString();
  }

}
