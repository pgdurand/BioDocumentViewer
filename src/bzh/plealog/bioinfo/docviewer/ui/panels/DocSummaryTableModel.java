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
package bzh.plealog.bioinfo.docviewer.ui.panels;

import javax.swing.table.AbstractTableModel;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.api.SummaryDocPresentationModel;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

/**
 * This is the table model that can be used to wrap a Summary objects to be used
 * with a DocSummaryTable.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class DocSummaryTableModel extends AbstractTableModel {
  private static final long serialVersionUID = -2347427221891267223L;
  private SummaryDocPresentationModel _presModel;
  private TableHeaderColumnItem[] _columnIds;
  private TableHeaderColumnItem[] _refColumnIds;
  private Summary _data;

  private DocSummaryTableModel() {
    super();
  }

  public DocSummaryTableModel(SummaryDocPresentationModel presentationModel) {
    this();
    _presModel = presentationModel;
    _refColumnIds = _presModel.getPresentationModel();
    createStandardColHeaders();
  }

  public void updateColumnHeaders(TableHeaderColumnItem[] colH) {
    _columnIds = colH;
    _presModel.saveDefaultColumnModel(TableColumnManager.getDelColumns(colH));
  }

  public void setData(Summary tData) {
    _data = tData;
    fireTableDataChanged();
  }

  public Summary getData() {
    return _data;
  }

  private void createStandardColHeaders() {
    int i, n;

    n = 0;
    for (i = 0; i < _refColumnIds.length; i++) {
      if (_refColumnIds[i].isVisible())
        n++;
    }
    _columnIds = new TableHeaderColumnItem[n];
    n = 0;
    for (i = 0; i < _refColumnIds.length; i++) {
      if (_refColumnIds[i].isVisible()) {
        _columnIds[n] = _refColumnIds[i];
        n++;
      }
    }
  }

  protected TableHeaderColumnItem[] getReferenceColHeaders() {
    return _refColumnIds;
  }

  public String getColumnName(int column) {
    return _columnIds[column].getSID();
  }

  public int getColumnId(int column) {
    return _columnIds[column].getIID();
  }

  public int getColumnHorizontalAlignment(int column) {
    return _columnIds[column].getHorizontalAlignment();
  }

  public boolean isLargestColumn(int column) {
    return _columnIds[column].isLargest();
  }

  public int getColumnCount() {
    return _columnIds.length;
  }

  public void clear() {
    _data = null;
    this.fireTableDataChanged();
  }

  public int getRowCount() {
    return _data != null ? _data.nbDocs() : 0;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    SummaryDoc item;
    Object val = null;

    if (_data == null)
      return "-";

    item = _data.getDoc(rowIndex);

    if (columnIndex < 0)
      return item;

    // first column: always a number
    if (columnIndex == 0) {
      // getFrom(): zero-based value.
      val = _data.getFrom() + rowIndex + 1;
    } else {
      item = _data.getDoc(rowIndex);
      val = item.getValue(_columnIds[columnIndex].getSID());
      if (val == null)
        val = "-";
    }
    return val;
  }

}
