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
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.sequence.BankSequenceDescriptor;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.conf.DirManager;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.ui.panels.DatabaseOpener;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.structure.ThreeDStructureViewer;
import bzh.plealog.bioinfo.seqvertor.BiojavaUtils;
import bzh.plealog.bioinfo.seqvertor.SequenceDataBag;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.feature.FeatureWebLinker;
import bzh.plealog.bioinfo.ui.sequence.extended.CombinedAnnotatedSequenceViewer;

/**
 * This class manages an action to display a sequence or a structure in the
 * DocViewer.
 * 
 * @author Patrick G. Durand
 */
public class DisplayEntryAction extends AbstractAction {
  private static final long serialVersionUID = -1193984590737070988L;
  private SummaryDoc _curDoc;
  private QueryEngine _qEngine;
  private boolean _loadInProgress = false;

  private static final MessageFormat MF = new MessageFormat(Messages.getString("FetchFastaAction.dwnld.msg"));

  /**
   * Action Constructor.
   */
  public DisplayEntryAction(String name) {
    super(name);
  }

  /**
   * Action Constructor.
   */
  public DisplayEntryAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Action method.
   */
  public void actionPerformed(ActionEvent event) {
    if (_loadInProgress || _curDoc == null || _qEngine == null)
      return;
    new DisplayEntryThread().start();
  }

  /**
   * Provide this action with the selected document.
   */
  public void setDocument(SummaryDoc doc) {
    _curDoc = doc;
  }

  /**
   * Provide this action with the QueryEngine.
   */
  public void setQueryEngine(QueryEngine qe) {
    _qEngine = qe;
  }

  /**
   * Reset current selected document and query engine.
   */
  public void resetAction() {
    _curDoc = null;
    _qEngine = null;
  }

  /**
   * Fetch a sequence entry from a remote server then display a SequenceViewer.
   */
  private void handleSequenceEntry(boolean isProteic) {
    String seqId;
    File tmpFile = null;
    SequenceDataBag sdb;

    // Load entry
    seqId = _curDoc.getValue(_qEngine.getBankType().getPresentationModel().getAccessionFieldKey());
    DatabaseOpener.setHelperMessage(MF.format(new Object[]{seqId}));

    try{
      tmpFile = _qEngine.load(seqId, _qEngine.getBankType().getCode(), true);
    }
    catch(Exception ex){
      tmpFile.delete();
      EZLogger.warn(ex.toString());
      MessageFormat mf = new MessageFormat(Messages.getString("DisplayEntryAction.msg1"));
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
          mf.format(new Object[] { seqId, _qEngine.getBankType().getUserName() }));
      return;
    }

    // Read entry
    switch (_qEngine.getBankType().getReaderType()) {
    case EMBL:
      sdb = BiojavaUtils.readEmblEntry(tmpFile);
      break;
    case GENBANK:
      sdb = BiojavaUtils.readGenbankEntry(tmpFile);
      break;
    case GENPEPT:
      sdb = BiojavaUtils.readGenpeptEntry(tmpFile);
      break;
    case UNIPROT:
      sdb = BiojavaUtils.readUniProtEntry(tmpFile);
      break;
    case FASTADNA:
    case FASTAPROT:
      sdb = BiojavaUtils.readFastaEntry(tmpFile);
      break;
    default:
      tmpFile.delete();
      MessageFormat mf = new MessageFormat(Messages.getString("DisplayEntryAction.msg2"));
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), mf.format(new Object[] { seqId }));
      return;
    }

    DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
    try {
      EZFileUtils.copyFile(tmpFile, new File(dmgr.getDocumentDataPath()+seqId+"."+_qEngine.getBankType().getReaderType()));
    } catch (IOException e) {
      //not bad: we won't have a local copy of the file
    }

    tmpFile.delete();

    if (sdb.getSequence().length()==0) {
      MessageFormat mf = new MessageFormat(Messages.getString("DisplayEntryAction.msg3"));
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), mf.format(new Object[] { seqId }));
      return;
    }
    
    // Display entry
    CombinedAnnotatedSequenceViewer viewer = new CombinedAnnotatedSequenceViewer();
    BankSequenceDescriptor descriptor = new BankSequenceDescriptor(sdb.getFeatTable(), sdb.getSeqInfo(),
        sdb.getDSequence());
    if (sdb.getFeatTable() != null) {
      sdb.getFeatTable()
          .setSource(_qEngine.getBankType().getProviderName() + " - " + _qEngine.getBankType().getUserName());
    }
    viewer.setData(descriptor);

    //open a new internal frame on the desktop
    DatabaseOpener.displayInternalFrame(viewer, seqId, isProteic?DocViewerConfig.PROTEIN_ICON:DocViewerConfig.DNA_ICON);
  }

  /**
   * Fetch a structure entry from a remote server then display a StructureVuiewer.
   */
  private void handle3DStructureEntry() {
    // Locate the FeatureWebLinker Resource
    FeatureWebLinker linker = new FeatureWebLinker();

    // Get the PDB id
    String pdbCode = _curDoc.getValue(_qEngine.getBankType().getPresentationModel().getAccessionFieldKey());

    DatabaseOpener.setHelperMessage("Downloading "+pdbCode);
    // Format URL and download PDB entry from remote server
    String url = linker.getURL("PDBgz", pdbCode);

    if (url == null){
      EZLogger.warn("Unable to format PDB URL");
      return;
    }

    File pdbFile_gz = HTTPBasicEngine.doGet(url);

    if (pdbFile_gz == null){
      EZLogger.warn("Unable to retrieve PDB entry from server");
      return;
    }

    //downloaded file is compressed (gzip). We need to uncompress it (3D viewer provides
    //a PDB text viewer, so we need the uncompressed file)
    File   pdbFile = new File(pdbFile_gz.getAbsoluteFile()+".txt");
    //notice: gunzipFile() logs errors if any
    if (!EZFileUtils.gunzipFile(pdbFile_gz.getAbsolutePath(), pdbFile.getAbsolutePath())){
      return;
    }
    
    // Everything ok? Start the 3D Viewer!
    ThreeDStructureViewer viewer = new ThreeDStructureViewer();
    if (viewer.getMolPanel().openFile(pdbFile.getAbsolutePath(), pdbCode, null, null)) {
      viewer.getMolCommander().setStructure(true);
      viewer.getMolCommander().applyDefaultBBoneStyle();
    } else {
      viewer.getMolCommander().setStructure(false);
      viewer.getMolCommander().resetView();
    }
    viewer.setOpenedPdbFile(pdbFile);
    viewer.updateTxtDisplay();
    try {Thread.sleep(2000);} catch (InterruptedException e) {}
    
    //open a new internal frame on the desktop
    DatabaseOpener.displayInternalFrame(viewer, pdbCode, DocViewerConfig.STRUCT_ICON);
  }

  /**
   * Avoid dead-locking UI event thread by running a separate thread.
   */
  private class DisplayEntryThread extends Thread{
    @Override
    public void run(){
      _loadInProgress = true;
      DisplayEntryAction.this.setEnabled(false);
      EZEnvironment.setWaitCursor();
      try{
        switch (_qEngine.getBankType().getReaderType()) {
        case PDB:
          handle3DStructureEntry();
          break;
        case EMBL:
        case GENBANK:
        case FASTADNA:
          handleSequenceEntry(false);
          break;
        case GENPEPT:
        case UNIPROT:
        case FASTAPROT:
          handleSequenceEntry(true);
        default:
        }
      }
      catch(Exception e){
        //JRE 1.8/OSX: from time to time, got a weird exception from swing
        //for now: monitor it. Did not find what happens...
        EZLogger.debug(e.toString());
      }
      finally{
      }
      DisplayEntryAction.this.setEnabled(true);
      EZEnvironment.setDefaultCursor();
      DatabaseOpener.cleanHelperMessage();
      _loadInProgress = false;
    }
  }
}