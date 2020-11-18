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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.sequence.BankSequenceDescriptor;
import bzh.plealog.bioinfo.docviewer.conf.DirManager;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.panels.StatusBarHelperPanel;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.seqvertor.BiojavaUtils;
import bzh.plealog.bioinfo.seqvertor.SequenceDataBag;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.sequence.extended.CombinedAnnotatedSequenceViewer;

/**
 * This class implements the action to load a sample sequence file.
 * 
 * @author Patrick G. Durand
 */
public class OpenSampleFileAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private static final String NCBI_GP_SAMPLE = "p12263.gp";
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenSampleFileAction(String name) {
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
  public OpenSampleFileAction(String name, Icon icon) {
    super(name, icon);
  }

  private class Loader extends Thread {
    private boolean handleSequenceFile() {
      boolean isProteic=true;
      SequenceDataBag sdb;
      String sampleSeqFile = null;
      
      try {
        DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
        sampleSeqFile = dmgr.getDocumentDataPath() + NCBI_GP_SAMPLE;
      } catch (IOException e) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
            Messages.getString("OpenSampleFileAction.err1"));
        EZLogger.warn(e.toString());
        return false;
      }
      
      EZEnvironment.setWaitCursor();
      StatusBarHelperPanel.setHelperMessage(Messages
              .getString("OpenFileAction.msg1"));
      
      File f = new File(sampleSeqFile);
      
      if (f.exists() == false){
        InputStream  in = Messages.class
              .getResourceAsStream(NCBI_GP_SAMPLE);

          try (FileOutputStream fos= new FileOutputStream(f);
              BufferedInputStream bis = new BufferedInputStream(in)) {
            int n;
            byte[] buf = new byte[2048];
            while ((n = bis.read(buf)) != -1) {
              fos.write(buf, 0, n);
            }
            fos.flush();
          } catch (IOException e) {
            EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
                Messages.getString("OpenSampleFileAction.err2"));
            EZLogger.warn(e.toString());
            return false;
          }
        }
      
      // Read entry
      sdb = BiojavaUtils.readGenpeptEntry(new File(sampleSeqFile));
      
      // Display entry
      CombinedAnnotatedSequenceViewer viewer = new CombinedAnnotatedSequenceViewer();
      BankSequenceDescriptor descriptor = new BankSequenceDescriptor(sdb.getFeatTable(), sdb.getSeqInfo(),
          sdb.getDSequence());
      if (sdb.getFeatTable() != null) {
        sdb.getFeatTable().setSource(sampleSeqFile);
      }
      viewer.setData(descriptor);

      //open a new internal frame on the desktop
      StatusBarHelperPanel.displayInternalFrame(viewer, sdb.getSeqInfo().getId(), 
          isProteic?DocViewerConfig.PROTEIN_ICON:DocViewerConfig.DNA_ICON);
      return true;
    }
    
    public void run() {
      try {
        handleSequenceFile();
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
