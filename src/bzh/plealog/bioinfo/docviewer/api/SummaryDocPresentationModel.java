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

import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

/**
 * Defines the ay to present SummaryDoc information.
 * 
 * Patrick G. Durand
 */
public interface SummaryDocPresentationModel {
  /**
   * Returns the data model used for presentation purpose. Presentation means view of the MVC paradigm.
   */
  public TableHeaderColumnItem[] getPresentationModel();
  
  /**
   * Returns the data key enabling to access length of an entry contained
   * in a DbSummaryDoc. For sequence entries, expected field should return sequence
   * length, i.e. number of amino acids or nucleotides. For other entries, one can
   * return document size. Such information is used internally by the retrieval system
   * to monitor and/or paginate data retrieval by chunks
   */
  public String getLengthFieldKey();
  
  /**
   * Returns the data key enabling to access accession number of an entry contained
   * in a DbSummaryDoc.
   */
  public String getAccessionFieldKey();
  
  /**
   * This method is called when it is required to save default column model.
   */
  public void saveDefaultColumnModel(String prop);
}
