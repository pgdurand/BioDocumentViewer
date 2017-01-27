/* Copyright (C) 2006-2017 Patrick G. Durand
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

import static org.junit.Assert.assertTrue;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.Ostermiller.util.Browser;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.configuration.DirectoryManager;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.sequence.BankSequenceDescriptor;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.config.DocViewerDirectoryType;
import bzh.plealog.bioinfo.docviewer.format.DataFormatter;
import bzh.plealog.bioinfo.docviewer.format.DataFormatter.FORMAT;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.format.DIWrapper;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.panels.DatabaseOpener;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.structure.ThreeDStructureViewer;
import bzh.plealog.bioinfo.docviewer.ui.variation.SAXTreeUtil;
import bzh.plealog.bioinfo.seqvertor.BiojavaUtils;
import bzh.plealog.bioinfo.seqvertor.SequenceDataBag;
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
  private VAR_VIEWER_TYPE _varViewType = VAR_VIEWER_TYPE.WEB_BROWSER;

  private static final MessageFormat MF = new MessageFormat(Messages.getString("FetchFastaAction.dwnld.msg"));

  private enum VAR_VIEWER_TYPE {
    TREE, FX_WEB, WEB_BROWSER
  }

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
   * Display variant data using a Tree Viewer. Actually, that viewer displays
   * the structure of the content of tmpFile which is XML formated.
   * 
   * @param varID
   *          variant ID
   * @param tmpFile
   *          file containing the data to display. Should be XML formated.
   */
  private void handleVariationInTreeViewer(String varID, File tmpFile) {
    // Create a Tree viewer
    JTree tree;
    try {
      tree = new JTree(SAXTreeUtil.loadXMLDocument("Variant: " + varID, tmpFile, "opt"));
    } catch (Exception ex) {
      EZLogger.warn(ex.toString());
      MessageFormat mf = new MessageFormat(Messages.getString("DisplayEntryAction.msg3"));
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), mf.format(new Object[] { varID }));
      return;
    }
    JScrollPane scrollPane = new JScrollPane(tree);
    SAXTreeUtil.setVariationTreeRenderer(tree);
    SAXTreeUtil.expandAll(tree);
    DatabaseOpener.displayInternalFrame(scrollPane, varID, DocViewerConfig.VAR_DNA_ICON);
  }

  /**
   * Display variant data using a Web Viewer. That viewer can be either an
   * embedded JavaFX based viewer or an external browser; it depends upon the
   * value of _varViewType class variable. Default is WEB_BROWSER, i.e. an
   * external browser.
   * 
   * @param varID
   *          variant ID
   * @param tmpFile
   *          file containing the data to display. Should be HTML formated.
   */
  private void handleVariationInWebViewer(String varID, File tmpFile) {
    File wFile;
    try {
      wFile = File.createTempFile("var_", ".html",
          new File(DirectoryManager.getPath(DocViewerDirectoryType.WEB_TEMPLATE)));
    } catch (IOException ex) {
      EZLogger.warn(ex.toString());
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), Messages.getString("DisplayEntryAction.err1"));
      return;
    }

    try (FileWriter fw = new FileWriter(wFile)) {
      DIWrapper diw = new DIWrapper(tmpFile);
      assertTrue(DataFormatter.dump(fw, diw, FORMAT.ENSEMBL_VAR_HTML));
    } catch (Exception ex) {
      EZLogger.warn(ex.toString());
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), Messages.getString("DisplayEntryAction.err2"));
      return;
    } finally {
      wFile.deleteOnExit();

    }

    if (_varViewType.equals(VAR_VIEWER_TYPE.WEB_BROWSER)) {
      try {
        Browser.init();
        EZEnvironment.setWaitCursor();
        Browser.displayURL("file://" + wFile.getAbsolutePath());
        EZEnvironment.setDefaultCursor();
      } catch (Exception ex) {
        EZLogger.warn(ex.toString());
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), Messages.getString("DisplayEntryAction.err3"));
      }
    } else {
      DatabaseOpener.displayWebDocument(wFile.getAbsolutePath(), varID, DocViewerConfig.VAR_DNA_ICON);
    }

  }

  /**
   * Fetch a variation entry from a remote server then display a VariantViewer.
   * For now, only handles Ensembl Variation data.
   */
  private void handleVariation() {
    String varID;
    File tmpFile = null;

    // Load entry
    varID = _curDoc.getValue(_qEngine.getBankType().getPresentationModel().getAccessionFieldKey());
    try {
      // varID = "rs879254030";// simple snp
      // varID = "rs587781858";// snp with polyphen and sift scores
      // ClinVar entry (NCBI view:
      // https://www.ncbi.nlm.nih.gov/clinvar/variation/245980/)
      // accessing CLinVar directly at NCBI: https://www.biostars.org/p/137256/
      // Possible pipeline:
      // dnSNP at NCBI:
      // https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=snp&id=879254030&report=XML
      // from dbSNP to clinvar at NCBI:
      // https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=snp&db=clinvar&id=879254030
      // clinvar at NCBI:
      // https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=clinvar&id=245980&retmode=xml
      tmpFile = _qEngine.load(varID, _qEngine.getBankType().getCode(), true);
    } catch (Exception ex) {
      tmpFile.delete();
      EZLogger.warn(ex.toString());
      MessageFormat mf = new MessageFormat(Messages.getString("DisplayEntryAction.msg1"));
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
          mf.format(new Object[] { varID, _qEngine.getBankType().getUserName() }));
      return;
    }

    // ensure deletion of temp file
    tmpFile.deleteOnExit();

    switch (_varViewType) {
    case FX_WEB:
    case WEB_BROWSER:
      handleVariationInWebViewer(varID, tmpFile);
      break;
    case TREE:
    default:
      handleVariationInTreeViewer(varID, tmpFile);
      break;
    }
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
    DatabaseOpener.setHelperMessage(MF.format(new Object[] { seqId }));

    try {
      tmpFile = _qEngine.load(seqId, _qEngine.getBankType().getCode(), true);
    } catch (Exception ex) {
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

    tmpFile.delete();

    // Display entry
    CombinedAnnotatedSequenceViewer viewer = new CombinedAnnotatedSequenceViewer();
    BankSequenceDescriptor descriptor = new BankSequenceDescriptor(sdb.getFeatTable(), sdb.getSeqInfo(),
        sdb.getDSequence());
    if (sdb.getFeatTable() != null) {
      sdb.getFeatTable()
          .setSource(_qEngine.getBankType().getProviderName() + " - " + _qEngine.getBankType().getUserName());
    }
    viewer.setData(descriptor);

    // open a new internal frame on the desktop
    DatabaseOpener.displayInternalFrame(viewer, seqId,
        isProteic ? DocViewerConfig.PROTEIN_ICON : DocViewerConfig.DNA_ICON);
  }

  /**
   * Fetch a structure entry from a remote server then display a
   * StructureVuiewer.
   */
  private void handle3DStructureEntry() {
    // Locate the FeatureWebLinker Resource
    FeatureWebLinker linker = new FeatureWebLinker();

    // Get the PDB id
    String pdbCode = _curDoc.getValue(_qEngine.getBankType().getPresentationModel().getAccessionFieldKey());

    DatabaseOpener.setHelperMessage("Downloading " + pdbCode);
    // Format URL and download PDB entry from remote server
    String url = linker.getURL("PDBgz", pdbCode);

    if (url == null) {
      EZLogger.warn("Unable to format PDB URL");
      return;
    }

    File pdbFile_gz = HTTPBasicEngine.doGet(url);

    if (pdbFile_gz == null) {
      EZLogger.warn("Unable to retrieve PDB entry from server");
      return;
    }

    // downloaded file is compressed (gzip). We need to uncompress it (3D viewer
    // provides
    // a PDB text viewer, so we need the uncompressed file)
    File pdbFile = new File(pdbFile_gz.getAbsoluteFile() + ".txt");
    // notice: gunzipFile() logs errors if any
    if (!EZFileUtils.gunzipFile(pdbFile_gz.getAbsolutePath(), pdbFile.getAbsolutePath())) {
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
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }

    // open a new internal frame on the desktop
    DatabaseOpener.displayInternalFrame(viewer, pdbCode, DocViewerConfig.STRUCT_ICON);
  }

  /**
   * Avoid dead-locking UI event thread by running a separate thread.
   */
  private class DisplayEntryThread extends Thread {
    @Override
    public void run() {
      _loadInProgress = true;
      DisplayEntryAction.this.setEnabled(false);
      EZEnvironment.setWaitCursor();
      try {
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
          break;
        case VARIATION:
          handleVariation();
        default:
        }
      } catch (Exception e) {
        // JRE 1.8/OSX: from time to time, got a weird exception from swing
        // for now: monitor it. Did not find what happens...
        EZLogger.debug(e.toString());
      } finally {
      }
      DisplayEntryAction.this.setEnabled(true);
      EZEnvironment.setDefaultCursor();
      DatabaseOpener.cleanHelperMessage();
      _loadInProgress = false;
    }
  }
}