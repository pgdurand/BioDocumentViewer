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
package bzh.plealog.bioinfo.docviewer.ui.actions;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class TableSelectorPanel extends JPanel {
  private static final long serialVersionUID = 6651437188966001969L;

  private JComboBox<String> _sequencePart;

  private static final String[] SEQ_PARTS = { "All sequences", "Current selection" };

  /**
   * Constructor.
   * 
   * @param selIsSelected
   *          specifies whether or not to activate the current selection
   */
  public TableSelectorPanel(boolean selIsSelected) {
    JPanel pnl1;

    pnl1 = new JPanel(new BorderLayout());

    pnl1.add(createSequencePart(selIsSelected), BorderLayout.NORTH);
    this.setLayout(new BorderLayout());
    this.add(pnl1, BorderLayout.CENTER);
  }

  /**
   * Returns true if one has to only process the selected part.
   */
  public boolean handleSelectionOnly() {
    return (_sequencePart.getSelectedItem().toString().indexOf("Current") != -1);
  }

  private JPanel createSequencePart(boolean selIsSelected) {
    DefaultFormBuilder builder;
    FormLayout layout;

    _sequencePart = new JComboBox<>();
    _sequencePart.addItem(SEQ_PARTS[0]);
    if (selIsSelected) {
      _sequencePart.addItem(SEQ_PARTS[1]);
      _sequencePart.setSelectedIndex(1);
    }
    layout = new FormLayout("170dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    builder.appendSeparator("Retrieve");
    builder.append(_sequencePart);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(builder.getContainer(), BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
    return panel;
  }
}
