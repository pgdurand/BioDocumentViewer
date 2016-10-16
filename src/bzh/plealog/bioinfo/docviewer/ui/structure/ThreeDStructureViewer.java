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
package bzh.plealog.bioinfo.docviewer.ui.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.prefs4j.implem.ui.components.PatternSearchFacility;

import bzh.plealog.bioinfo.docviewer.ui.structure.jmol.JMolCommander;
import bzh.plealog.bioinfo.docviewer.ui.structure.jmol.JMolPanel;
import bzh.plealog.bioinfo.docviewer.ui.structure.panels.PdbSeqViewer;
import bzh.plealog.bioinfo.ui.sequence.event.DSelectionListenerSupport;
import bzh.plealog.bioinfo.ui.util.JHeadPanel;

/**
 * This the master 3D Viewer. It combines DDPdbSeqViewer, DDMolPanel and DDMolCommander
 * into a single component. Use this one to start the viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class ThreeDStructureViewer extends JPanel {
  private static final long serialVersionUID = 5170999580816145347L;
  private PdbSeqViewer _seqViewer;
  private JMolPanel _3dViewer;
  private JMolCommander _commander;
  private JTabbedPane _jtp;
  private File _pdbFile;
  private JTextArea _txtViewer;
  private DSelectionListenerSupport _lSupport;

  private static final Font HELP_FNT = new Font("sans-serif", Font.PLAIN, 10);

  public ThreeDStructureViewer() {
    JSplitPane jsp;
    JPanel tdPanel, txtPane, mainPnl, seqPnl;
    JHeadPanel hPanel1D;

    mainPnl = new JPanel(new BorderLayout());
    seqPnl = new JPanel(new BorderLayout());

    tdPanel = get3DViewer();
    txtPane = getEntryTxtViewer();
    _jtp = new JTabbedPane();
    _jtp.setFocusable(false);
    _jtp.add("3D Viewer", tdPanel);
    _jtp.add("Text Viewer", txtPane);
    _jtp.addChangeListener(new MyChangeListener());
    _seqViewer = new PdbSeqViewer();
    seqPnl.add(_seqViewer, BorderLayout.CENTER);
    _commander = new JMolCommander(_3dViewer, _seqViewer);
    _3dViewer.addPdbModelListener(_seqViewer);
    _lSupport = new DSelectionListenerSupport();
    _seqViewer.registerSelectionListenerSupport(_lSupport);
    _commander.registerSelectionListenerSupport(_lSupport);
    _seqViewer.setCommander(_commander);

    hPanel1D = new JHeadPanel(null, "Sequence", seqPnl, true, false);

    mainPnl.add(_jtp, BorderLayout.CENTER);
    mainPnl.add(hPanel1D, BorderLayout.SOUTH);
    _commander.setMinimumSize(_commander.getPreferredSize());
    jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPnl, _commander);
    jsp.setResizeWeight(1.0);
    jsp.setOneTouchExpandable(true);

    this.setLayout(new BorderLayout());
    this.add(jsp, BorderLayout.CENTER);
  }

  private JPanel get3DViewer() {
    JPanel pnl, pnl2, pnl3;
    JLabel lbl;

    pnl = new JPanel(new BorderLayout());
    pnl2 = new JPanel(new BorderLayout());
    pnl3 = new JPanel(new BorderLayout());

    _3dViewer = new JMolPanel();
    lbl = new JLabel(
        "<HTML><I>To rotate</I>: left click and drag. " + "<I>To zoom</I>: hold shift key, left click and drag. "
            + "<I>To pan</I>: hold ctrl key, right click and drag. "
            + "<I>To measure</I>: double click on an atom to start measuring.</HTML>");
    lbl.setFont(HELP_FNT);
    pnl3.add(lbl, BorderLayout.NORTH);
    pnl2.add(pnl3, BorderLayout.CENTER);
    pnl2.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    // pnl.add(getPDBQueryPanel(), BorderLayout.NORTH);
    pnl.add(_3dViewer, BorderLayout.CENTER);
    pnl.add(pnl2, BorderLayout.SOUTH);
    return pnl;
  }

  @SuppressWarnings("serial")
  private JPanel getEntryTxtViewer() {
    JPanel pnl;
    PatternSearchFacility sf;
    JScrollPane jsp;

    pnl = new JPanel(new BorderLayout());
    _txtViewer = new JTextArea() {
      public boolean getScrollableTracksViewportWidth() {
        return false; // force display of horizontal scroll bar
      }
    };
    _txtViewer.setFont(new Font("courier", Font.PLAIN, 12));
    _txtViewer.setEditable(false);
    _txtViewer.setOpaque(true);
    _txtViewer.setBackground(Color.WHITE);
    sf = new PatternSearchFacility(_txtViewer);
    pnl.add(sf.getSearchForm(), BorderLayout.SOUTH);
    jsp = new JScrollPane(_txtViewer);
    jsp.setOpaque(true);
    jsp.setBackground(Color.WHITE);
    pnl.add(jsp, BorderLayout.CENTER);
    pnl.setOpaque(true);
    pnl.setBackground(Color.WHITE);
    return pnl;
  }

  public JMolPanel getMolPanel() {
    return _3dViewer;
  }

  public JMolCommander getMolCommander() {
    return _commander;
  }

  public void cleanViewer() {
    _commander.setStructure(false);
    _commander.resetView();
    _pdbFile = null;
    _seqViewer.cleanViewer();
    _3dViewer.cleanViewer();
  }

  public void setOpenedPdbFile(File pdbFile) {
    _pdbFile = pdbFile;
  }

  public void updateTxtDisplay() {
    String unCompFile, entryContent = null;
    File txtFile;

    // Get current tab
    int sel = _jtp.getSelectedIndex();
    if (sel == 1 && _pdbFile != null) {
      unCompFile = EZFileUtils.gunzipFile(_pdbFile.getAbsolutePath());
      if (unCompFile != null) {
        txtFile = new File(unCompFile);
        try {
          entryContent = EZFileUtils.getFileContent(txtFile);
        } catch (IOException e) {
        }
      }
    }
    _txtViewer.setText(entryContent != null ? entryContent : "");
    _txtViewer.setCaretPosition(0);

  }

  private class MyChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent evt) {
      updateTxtDisplay();
    }
  }
}
