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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.plealog.genericapp.ui.common.UIUtils;

/**
 * Display the selection manipulation dialogue box.
 * 
 * @author Patrick G. Durand
 */
public class TableSelectorDialog extends JDialog {

  private static final long serialVersionUID = 5726103930703558125L;
  private boolean _cancelled;
  private TableSelectorPanel _sPanel;

  /**
   * Constructor.
   * 
   * @param parent
   *          the parent of this dialogue
   * @param isProt
   *          specifies whether the sequence is a protein
   * @param hasSelectedParts
   *          specifies whether the sequence has selected regions on it
   */
  public TableSelectorDialog(Frame parent, String title, boolean hasSelectedParts) {
    super(parent, true);
    buildGUI(hasSelectedParts);
    this.setTitle(title);
    this.pack();
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new DBDialogAdapter());
  }

  /**
   * Creates the GUI.
   */
  private void buildGUI(boolean hasSelectedParts) {
    JPanel mainPnl, btnPnl;
    JButton okBtn, cancelBtn;

    mainPnl = new JPanel(new BorderLayout());
    _sPanel = new TableSelectorPanel(hasSelectedParts);
    mainPnl.add(_sPanel, BorderLayout.CENTER);
    mainPnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    okBtn = new JButton("Ok");
    cancelBtn = new JButton("Cancel");
    okBtn.addActionListener(new OkAction());
    cancelBtn.addActionListener(new CancelAction());
    btnPnl = new JPanel();
    btnPnl.setLayout(new BoxLayout(btnPnl, BoxLayout.X_AXIS));
    btnPnl.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    btnPnl.add(Box.createHorizontalGlue());
    btnPnl.add(okBtn);
    btnPnl.add(Box.createRigidArea(new Dimension(10, 0)));
    btnPnl.add(cancelBtn);
    btnPnl.add(Box.createHorizontalGlue());

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(mainPnl, BorderLayout.CENTER);
    getContentPane().add(btnPnl, BorderLayout.SOUTH);
  }

  /**
   * Shows the dialog box on screen.
   */
  public void showDialog() {
    UIUtils.centerOnScreen(this);
    setVisible(true);
  }

  /**
   * Returns true if the dialogue has been canceled. Call this method when the
   * dialogue has been closed.
   */
  public boolean dlgCancelled() {
    return _cancelled;
  }

  /**
   * Returns true if one has to only process the selected region of the
   * sequence. Call this method when the dialogue has been closed.
   */
  public boolean handleSelectionOnly() {
    return _sPanel.handleSelectionOnly();
  }

  /**
   * This inner class manages actions coming from the JButton Ok.
   */
  private class OkAction extends AbstractAction {
    private static final long serialVersionUID = 8703296009993601880L;

    /**
     * Manages JButton action
     */
    public void actionPerformed(ActionEvent e) {
      _cancelled = false;
      dispose();
    }
  }

  /**
   * This inner class manages actions coming from the JButton Cancel.
   */
  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = 8041115315309591436L;

    /**
     * Manages JButton action
     */
    public void actionPerformed(ActionEvent e) {
      _cancelled = true;
      dispose();
    }
  }

  /**
   * Listener of JDialog events.
   */
  private class DBDialogAdapter extends WindowAdapter {
    /**
     * Manages windowClosing event: hide the dialog.
     */
    public void windowClosing(WindowEvent e) {
      _cancelled = true;
      dispose();
    }
  }
}
