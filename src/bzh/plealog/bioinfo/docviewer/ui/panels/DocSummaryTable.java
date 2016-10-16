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

import java.awt.Color;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import bzh.plealog.bioinfo.ui.util.JKTable;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;

/**
 * This is the table capable of displaying the content of a Summary
 * objects.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class DocSummaryTable extends JKTable {
  private static final long serialVersionUID = -6192917064010450955L;
  public static final Color QUERY_CELL_BK_COLOR = new Color(178, 215, 255);// 184,207,229

  /**
   * Constructor.
   * 
   * @param dm
   *          only an instance of DocSummaryTableModel is authorized. Even if
   *          the constructor does not test that, do not try to pass in another
   *          kind of table model.
   */
  public DocSummaryTable(TableModel dm) {
    super(dm);
    // force display of grid (to solve a MacOS X problem)
    this.setGridColor(Color.LIGHT_GRAY);
    this.getTableHeader().setReorderingAllowed(false);
  }

  public TableCellRenderer getCellRenderer(int row, int column) {
    TableCellRenderer tcr;
    DocSummaryTableModel tModel;

    tcr = super.getCellRenderer(row, column);

    tModel = (DocSummaryTableModel) this.getModel();
    if (tcr instanceof JLabel) {
      JLabel lbl;
      lbl = (JLabel) tcr;
      lbl.setHorizontalAlignment(tModel.getColumnHorizontalAlignment(column));
      if (row % 2 == 0) {
        lbl.setBackground(QUERY_CELL_BK_COLOR);
      } else {
        lbl.setBackground(Color.WHITE);
      }
    }
    return tcr;
  }

  public void initColumnSize(int width, int[] cols) {
    FontMetrics fm;
    TableColumnModel tcm;
    DocSummaryTableModel tModel;
    TableColumn tc, lastTc = null;
    String header;
    int i, size, tot, val;

    fm = this.getFontMetrics(this.getFont());
    tcm = this.getColumnModel();
    tModel = (DocSummaryTableModel) this.getModel();
    size = tcm.getColumnCount();
    tot = 0;
    if (cols != null && cols.length == size) {
      for (i = 0; i < size; i++) {
        tcm.getColumn(i).setPreferredWidth(cols[i]);
      }
    } else {
      // second pass: set the column width
      for (i = 0; i < size; i++) {
        tc = tcm.getColumn(i);
        header = tc.getHeaderValue().toString();
        if (!tModel.isLargestColumn(i)) {
          val = fm.stringWidth(header) + 20;
          tc.setPreferredWidth(val);
          tot += val;
        } else {
          lastTc = tc;
        }
      }
      if (lastTc != null) {
        lastTc.setPreferredWidth(width - tot - 2);
      }
    }
  }

  public void updateColumnHeaders(TableHeaderColumnItem[] colH) {
    ((DocSummaryTableModel) this.getModel()).updateColumnHeaders(colH);
  }

}
