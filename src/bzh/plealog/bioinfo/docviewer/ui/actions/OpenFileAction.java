/* Copyright (C) 2020 Patrick G. Durand
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

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.sequence.BankSequenceDescriptor;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.panels.StatusBarHelperPanel;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.seqvertor.BiojavaUtils;
import bzh.plealog.bioinfo.seqvertor.SeqIOUtils;
import bzh.plealog.bioinfo.seqvertor.SequenceDataBag;
import bzh.plealog.bioinfo.ui.sequence.extended.CombinedAnnotatedSequenceViewer;

/**
 * This class implements the action to load a sequence file.
 * 
 * @author Patrick G. Durand
 */
public class OpenFileAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenFileAction(String name) {
    super(name);
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public OpenFileAction(String name, Icon icon) {
    super(name, icon);
  }

  private class Loader extends Thread {
    private boolean handleSequenceFile(File seqFile) {
      boolean isProteic=false;
      SequenceDataBag sdb;

      // Check entry type
      int format = SeqIOUtils.guessFileFormat(seqFile.getAbsolutePath());
      // Read entry
      switch (format) {
      case SeqIOUtils.EMBL:
        sdb = BiojavaUtils.readEmblEntry(seqFile);
        isProteic=false;
        break;
      case SeqIOUtils.GENBANK:
        sdb = BiojavaUtils.readGenbankEntry(seqFile);
        isProteic=false;
        break;
      case SeqIOUtils.GENPEPT:
        sdb = BiojavaUtils.readGenpeptEntry(seqFile);
        break;
      case SeqIOUtils.SWISSPROT:
        sdb = BiojavaUtils.readUniProtEntry(seqFile);
        break;
      case SeqIOUtils.FASTARNA:
      case SeqIOUtils.FASTADNA:
        isProteic=false;
      case SeqIOUtils.FASTAPROT:
        sdb = BiojavaUtils.readFastaEntry(seqFile);
        break;
      default:
        MessageFormat mf = new MessageFormat(Messages.getString("OpenFileAction.msg4"));
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
            mf.format(new Object[] { seqFile.getName() }));
        return false;
      }

      if (sdb.getSequence().length()==0) {
        MessageFormat mf = new MessageFormat(Messages.getString("OpenFileAction.msg5"));
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
            mf.format(new Object[] { sdb.getSeqInfo().getId() }));
        return false;
      }
      
      // Display entry
      CombinedAnnotatedSequenceViewer viewer = new CombinedAnnotatedSequenceViewer();
      BankSequenceDescriptor descriptor = new BankSequenceDescriptor(sdb.getFeatTable(), sdb.getSeqInfo(),
          sdb.getDSequence());
      if (sdb.getFeatTable() != null) {
        sdb.getFeatTable().setSource(seqFile.getAbsolutePath());
      }
      viewer.setData(descriptor);

      //open a new internal frame on the desktop
      StatusBarHelperPanel.displayInternalFrame(viewer, sdb.getSeqInfo().getId(), 
          isProteic?DocViewerConfig.PROTEIN_ICON:DocViewerConfig.DNA_ICON);
      return true;
    }
    private void doAction() {
      //EZFileManager.useOSNativeFileDialog(true);
      File[] fs = EZFileManager.chooseFilesForOpenAction(Messages
          .getString("OpenFileAction.lbl"));
      if (fs == null)// user canceled dlg box
        return;

      EZEnvironment.setWaitCursor();

      EZLogger.info(Messages
          .getString("OpenFileAction.msg1"));
      int notLoadedFiles=0, ncount=0;
      for (File f:fs) {
        ncount++;
        StatusBarHelperPanel.setHelperMessage(Messages
            .getString("OpenFileAction.msg1")+ncount+"/"+fs.length);
        if(!handleSequenceFile(f)) {
          notLoadedFiles++;
        }
      }
      if(notLoadedFiles!=0) {
        EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), 
            Messages.getString("OpenFileAction.msg2") );
      }
      
      System.gc();
    }
    public void run() {
      try {
        doAction();
      } catch (Throwable t) {
        EZLogger.warn(
            Messages.getString("OpenFileAction.err") +
            t.toString());
      } finally {
        EZEnvironment.setDefaultCursor();
        StatusBarHelperPanel.cleanHelperMessage();
      }
    }
  }

  public void actionPerformed(ActionEvent event) {
    new Loader().start();
  }

}
