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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.fetcher.SeqRetrieverMonitor;
import bzh.plealog.bioinfo.docviewer.ui.actions.DisplayEntryAction;
import bzh.plealog.bioinfo.docviewer.ui.actions.FetchSequenceAction;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.ui.util.SaveTableToCSVFileAction;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.ContextMenuElement;

/**
 * This is the component that aims at displaying database search result in the
 * Document Viewer. Basically, it wraps a standard DocNavigator with some
 * additional actions and the batch sequence retrieval system.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class XplorDocNavigator extends JPanel {
  private static final long serialVersionUID = 2155427241340846043L;
  private QueryEngine _engine;
  private DocNavigator _docNavigator;
  private DisplayEntryAction _docViewerAction;
  private FetchSequenceAction _sequenceFetcher;
  private Summary _curResult;
  private String _dbCode;
  private ListSelectionListener _tableSelListener;
  private JPanel _tbar;
  private MySeqRetrieverMonitor _monitor;
  private JButton _cancelBtn;
  private SaveTableToCSVFileAction _saveCSVAction;
  private SaveTableToCSVFileAction _saveCSVActionMnu;
  
  public XplorDocNavigator(QueryEngine engine) {
    _engine = engine;
    _docNavigator = new DocNavigator(engine);
    _monitor = new MySeqRetrieverMonitor();
    _cancelBtn = new JButton(EZEnvironment.getImageIcon("stopScheduler.png"));
    _cancelBtn.setToolTipText(Messages.getString("SeqRetrieverMonitor.cancel.toolTip"));
    _cancelBtn.setBorder(null);
    _cancelBtn.addActionListener(_monitor);
    _monitor.setCmdBar(_cancelBtn);
    _monitor.setVisible(false);
    _tableSelListener = new DocTableSelectionListener();
    _docNavigator.addDocSelectionListener(_tableSelListener);
    _tbar = new JPanel(new BorderLayout());
    _tbar.add(getToolbar(), BorderLayout.EAST);
    _tbar.add(_monitor, BorderLayout.WEST);

    _docNavigator.setDoubleClickAction(_docViewerAction);
    updateContextMenu();

    this.setLayout(new BorderLayout());
    this.add(_docNavigator, BorderLayout.CENTER);
    this.add(_tbar, BorderLayout.SOUTH);
  }

  /**
   * @see DocNavigator#setData(DbSummaryResult, EntrezQueryEngine).
   */
  public void setData(Summary res) {
    _docNavigator.setData(res);
    _curResult = res;
    _dbCode = _engine.getBankType().getCode();
    _sequenceFetcher.setEngine(_engine);
    handleActionsDefaults();
  }

  private void updateContextMenu() {
    _saveCSVActionMnu = new SaveTableToCSVFileAction(Messages.getString("DDXplorDocNavigator.lbl1"));
    _saveCSVActionMnu.setResultTable(_docNavigator.getDataTable());
    _saveCSVActionMnu.setEnabled(false);
    ArrayList<ContextMenuElement> actions;
    actions = new ArrayList<ContextMenuElement>();
    actions.add(null);
    actions.add(new ContextMenuElement(_saveCSVActionMnu));
    _docNavigator.getContextMenu().addActions(actions);
  }

  private void handleActionsDefaults() {
    _sequenceFetcher.setDatabaseCode(_dbCode);
    _sequenceFetcher.setDocuments(null);
    _sequenceFetcher.setEnabled(_engine.getBankType().enableSequenceRetrieval());
    _sequenceFetcher.selectionIsEmpty(true);
    _sequenceFetcher.setTotDocs(_curResult.getTotal());
    _docViewerAction.setDocument(null);
    _docViewerAction.setQueryEngine(_engine);
    _docViewerAction.setEnabled(false);
    _saveCSVActionMnu.setEnabled(false);
    _saveCSVAction.setEnabled(false);
  }

  protected JToolBar getToolbar() {
    ImageIcon icon;
    JToolBar tBar;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    // Fasta fetcher (interactive)
    icon = EZEnvironment.getImageIcon("savefas.png");
    if (icon != null) {
      _sequenceFetcher = new FetchSequenceAction("", icon);
    } else {
      _sequenceFetcher = new FetchSequenceAction(Messages.getString("DDXplorDocNavigator.save.fas.btn"));
    }
    _sequenceFetcher.setEnabled(false);
    _sequenceFetcher.setMonitor(_monitor);
    btn = tBar.add(_sequenceFetcher);
    btn.setToolTipText(Messages.getString("DDXplorDocNavigator.save.fas.tip"));
    btn.setText(Messages.getString("DDXplorDocNavigator.save.fas.btn"));
    // CSV Export
    icon = EZEnvironment.getImageIcon("savecsv.png");
    if (icon != null) {
      _saveCSVAction = new SaveTableToCSVFileAction("", icon);
    } else {
      _saveCSVAction = new SaveTableToCSVFileAction(Messages.getString("DDXplorDocNavigator.save.csv.btn"));
    }
    _saveCSVAction.setEnabled(false);
    _saveCSVAction.setResultTable(_docNavigator.getDataTable());
    btn = tBar.add(_saveCSVAction);
    btn.setToolTipText(Messages.getString("DDXplorDocNavigator.save.csv.tip"));
    btn.setText(Messages.getString("DDXplorDocNavigator.save.csv.btn"));
    tBar.addSeparator();

    // doc viewer action
    icon = EZEnvironment.getImageIcon("docView.png");
    if (icon != null) {
      _docViewerAction = new DisplayEntryAction("", icon);
    } else {
      _docViewerAction = new DisplayEntryAction(Messages.getString("DDXplorDocNavigator.show.entry.btn"));
    }
    _docViewerAction.setEnabled(false);
    btn = tBar.add(_docViewerAction);
    btn.setToolTipText(Messages.getString("DDXplorDocNavigator.show.entry.tip"));
    btn.setText(Messages.getString("DDXplorDocNavigator.show.entry.btn"));
    return tBar;
  }

  protected void cancelJob() {
    _monitor.cancelJob();
  }

  /**
   * Handles the selection made by the user on the serach result table.
   */
  private class DocTableSelectionListener implements ListSelectionListener {
    private List<SummaryDoc> getDocs(ListSelectionModel selModel) {
      ArrayList<SummaryDoc> docs;

      docs = new ArrayList<SummaryDoc>();
      for (int i = selModel.getMinSelectionIndex(); i <= selModel.getMaxSelectionIndex(); i++) {
        if (selModel.isSelectedIndex(i)) {
          docs.add(_curResult.getDoc(i));
        }
      }
      return docs;
    }

    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting())
        return;
      ListSelectionModel selModel = (ListSelectionModel) e.getSource();
      if (selModel.isSelectionEmpty()) {
        handleActionsDefaults();
        return;
      }
      _sequenceFetcher.setDatabaseCode(_dbCode);
      _sequenceFetcher.setDocuments(getDocs(selModel));
      _sequenceFetcher.setEnabled(_engine.getBankType().enableSequenceRetrieval() && !_monitor.isJobRunning());
      _sequenceFetcher.selectionIsEmpty(false);
      _sequenceFetcher.setTotDocs(_curResult.getTotal());
      //for the document viewer, we can only handle a single doc. 
      _docViewerAction.setDocument(_curResult.getDoc(e.getFirstIndex()));
      _docViewerAction.setQueryEngine(_engine);
      _docViewerAction.setEnabled(selModel.getMinSelectionIndex() == selModel.getMaxSelectionIndex());
      _saveCSVActionMnu.setEnabled(true);
      _saveCSVAction.setEnabled(true);
    }
  }

  /**
   * Handles the monitor that listen to the batch sequence retrieval engine.
   */
  private class MySeqRetrieverMonitor extends SeqRetrieverMonitor implements ActionListener {
    private static final long serialVersionUID = 4167673501141957773L;

    public void jobDone() {
      super.jobDone();
      DatabaseOpener.setFetchingProcessRunning(false);
      this.setVisible(false);
      EZLogger.info(Messages.getString("DDXplorDocNavigator.lbl2"));
      if (this.getErrMsg() != null) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), this.getErrMsg());
      }
    }

    public void startJob() {
      super.startJob();
      _cancelBtn.setIcon(EZEnvironment.getImageIcon("stopScheduler.png"));
      _cancelBtn.setToolTipText(Messages.getString("SeqRetrieverMonitor.cancel.toolTip"));
      EZLogger.info(Messages.getString("DDXplorDocNavigator.lbl3"));
      DatabaseOpener.setFetchingProcessRunning(true);
      this.setMessage(Messages.getString("DatabaseOpener.lbl7"));
      _cancelBtn.setEnabled(true);
      this.setVisible(true);
    }

    @Override
    public void pauseJob() {
      super.pauseJob();

      _cancelBtn.setIcon(EZEnvironment.getImageIcon("startScheduler.png"));
      _cancelBtn.setToolTipText(Messages.getString("SeqRetrieverMonitor.restart.toolTip"));
    }

    @Override
    public void restartJob() {
      super.restartJob();

      _cancelBtn.setIcon(EZEnvironment.getImageIcon("stopScheduler.png"));
      _cancelBtn.setToolTipText(Messages.getString("SeqRetrieverMonitor.cancel.toolTip"));
    }

    public void actionPerformed(ActionEvent e) {
      if (_cancelBtn.getToolTipText().equals(Messages.getString("SeqRetrieverMonitor.cancel.toolTip"))) {
        if (EZEnvironment.confirmMessage(EZEnvironment.getParentFrame(),
            Messages.getString("DDXplorDocNavigator.msg1"))) {
          _cancelBtn.setEnabled(false);
          this.cancelJob();
        }
      } else {
        this.restartJob();
      }
    }
  }
}
