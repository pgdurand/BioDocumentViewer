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

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileFilter;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.file.EZFileTypes;
import com.plealog.genericapp.implem.file.EZFileExtDescriptor;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.fetcher.DBAutoFetcher;
import bzh.plealog.bioinfo.docviewer.fetcher.DBSimpleFetcher;
import bzh.plealog.bioinfo.docviewer.fetcher.DocFetcherUtils;
import bzh.plealog.bioinfo.docviewer.fetcher.SeqRetrieverMonitor;
import bzh.plealog.bioinfo.docviewer.ui.panels.StatusBarHelperPanel;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

/**
 * This class manages an action to fetch sequences using a SequenceLoader
 * facility.
 * 
 * @author Patrick G. Durand
 */
public class FetchSequenceAction extends AbstractAction {
  private static final long serialVersionUID = 7277590317926372405L;
  private QueryEngine _engine;
  private List<SummaryDoc> _curDocs;
  private String _dbCode;
  private SeqRetrieverMonitor _monitor;
  @SuppressWarnings("unused")
  private int _totDocs;
  private boolean _emptySel = true;

  private static final MessageFormat INFO_TXT = new MessageFormat(Messages.getString("FetchFastaAction.lbl4"));

  public FetchSequenceAction(String name) {
    super(name);
  }

  public FetchSequenceAction(String name, Icon icon) {
    super(name, icon);
  }

  public void actionPerformed(ActionEvent event) {
    if (_monitor.isJobRunning() || StatusBarHelperPanel.isFetchingProcessRunning()) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), Messages.getString("FetchFastaAction.lbl1"));
      return;
    }
    if (_dbCode == null)
      return;
    TableSelectorDialog tsd = new TableSelectorDialog(EZEnvironment.getParentFrame(),
        Messages.getString("FetchFastaAction.lbl3"), !_emptySel);
    tsd.showDialog();
    if (tsd.dlgCancelled())
      return;
    File f = chooseFile();
    if (f == null)
      return;
    fetchSeqFromCurDocs(f, tsd.handleSelectionOnly());
  }

  public void setMonitor(SeqRetrieverMonitor monitor) {
    _monitor = monitor;
  }

  public void setDocuments(List<SummaryDoc> docs) {
    _curDocs = docs;
  }

  public void setTotDocs(int tot) {
    _totDocs = tot;
  }

  public void selectionIsEmpty(boolean empty) {
    _emptySel = empty;
  }

  public void setDatabaseCode(String db) {
    _dbCode = db;
  }

  public void setEngine(QueryEngine engine) {
    _engine = engine;
  }

  public void resetAction() {
    _curDocs = null;
    _dbCode = null;
  }

  private void fetchSeqFromCurDocs(File seqFile, boolean selectionOnly) {
    boolean retrieveFullEntry;

    _monitor.reset();
    retrieveFullEntry = seqFile.getAbsolutePath().endsWith(DocViewerConfig.DAT_FEXT);

    if (selectionOnly) {
      if (_curDocs == null || _curDocs.isEmpty())
        return;
      /*if (_curDocs.size() > 100) {
        if (!displayFetchMsg(_curDocs.size()))
          return;
      }*/

      new DBSimpleFetcher(seqFile, _engine, copyDocs(), _dbCode, _monitor, retrieveFullEntry).start();
    } else {
      /*if (_totDocs > 100) {
        if (!displayFetchMsg(_totDocs))
          return;
      }*/
      new DBAutoFetcher(seqFile, _engine, _dbCode, _monitor, retrieveFullEntry).start();
    }
  }

  /**
   * Allows the user to choose a file name.
   */
  private File chooseFile() {
    
    //return EZFileManager.chooseFileForSaveAction(Messages.getString("DDFileTypes.fas.dlg.header"));
    
    ArrayList<EZFileExtDescriptor> types = new ArrayList<EZFileExtDescriptor>();

    types.add(new EZFileExtDescriptor(DocViewerConfig.FAS_FEXT,
        EZFileTypes.getFileFilter(DocViewerConfig.FAS_FEXT).getDescription()));
    types.add(new EZFileExtDescriptor(DocViewerConfig.DAT_FEXT,
        EZFileTypes.getFileFilter(DocViewerConfig.DAT_FEXT).getDescription()));

    EZFileManager.useOSNativeFileDialog(false);
    return EZFileManager.chooseFileForSaveAction(
        EZEnvironment.getParentFrame(),
        Messages.getString("FetchFastaAction.dlg.header"), 
        new EZFileFilter(new String[]{DocViewerConfig.FAS_FEXT,DocViewerConfig.DAT_FEXT}, Messages.getString("FetchFastaAction.dlg.fext")), 
        types);
  }

  @SuppressWarnings("unused")
  private boolean displayFetchMsg(int nDocs) {
    StringBuffer buf = new StringBuffer();
    ServerConfiguration sConf;
    int time, val, sleepTime, seqPerRun;

    sConf = _engine.getServerConfiguration();
    //sConf.getSleepTimeBetweenRun(): milliseconds, so convert to seconds
    sleepTime = Math.max(1, sConf.getSleepTimeBetweenRun()/1000);
    seqPerRun = sConf.getSequencesPerRun();
    time = (nDocs*sleepTime/seqPerRun);
    
    if (time>86400){//days
      val = time/86400;
      if ((time%86400)!=0)
        val++;
      buf.append(val);
      buf.append(" "+Messages.getString("FetchFastaAction.lbl5"));
    }
    else if (time>3600){//hours
      val = time/3600;
      if ((time%3600)!=0)
        val++;
      buf.append(val);
      buf.append(" "+Messages.getString("FetchFastaAction.lbl6"));
    }
    else if (time>60){//minutes
      val = time/60;
      if ((time%60)!=0)
        val++;
      buf.append(val);
      buf.append(" "+Messages.getString("FetchFastaAction.lbl7"));
    }
    else{//seconds
      val = time;
      buf.append(val);
      buf.append(" "+Messages.getString("FetchFastaAction.lbl8"));
    }
    if(val>1)
      buf.append("s");

    return EZEnvironment.confirmMessage(EZEnvironment.getParentFrame(), 
        INFO_TXT.format(new Object[]{_engine.getBankType().getProviderName(), EZApplicationBranding.getAppName(), nDocs, buf.toString()}));
  }

  private List<DocFetcherUtils.DocSum> copyDocs() {
    ArrayList<DocFetcherUtils.DocSum> docs = new ArrayList<DocFetcherUtils.DocSum>();
    String data;

    for (SummaryDoc doc : _curDocs) {
      data = doc.getValue(_engine.getBankType().getPresentationModel().getLengthFieldKey());
      docs.add(new DocFetcherUtils.DocSum(doc.getId(), (data != null ? Integer.valueOf(data) : 0)));
    }
    return docs;
  }
}
