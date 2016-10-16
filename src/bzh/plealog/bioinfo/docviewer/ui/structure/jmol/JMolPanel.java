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
package bzh.plealog.bioinfo.docviewer.ui.structure.jmol;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.popup.JmolPopup;

import com.Ostermiller.util.Browser;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileFilter;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbModelListener;
import bzh.plealog.bioinfo.ui.feature.FeatureWebLinker;

/*
 * Note: loading a PDB file: http://www.rcsb.org/pdb/files/4hhb.pdb.gz
 * cf. http://www.pdb.org/pdb/static.do?p=home/faq.html#download
 * Tutorial: http://www.callutheran.edu/Academic_Programs/Departments/BioDev/omm/scripting/molmast.htm
 * Another tutorial: http://wiki.jmol.org:81/index.php/Selecting (cf link 'atom expression')
 * */
/**
 * Setup the 3D Viewer. Relies on JMol.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class JMolPanel extends JPanel {
  private static final long serialVersionUID = 4830932894709028573L;
  private JmolViewer viewer_;
  private JmolAdapter adapter_;
  private JmolPopup jmolpopup_;
  private StatusListener statusListener_;
  private ImageManagerAction ima_;
  private DisplayEntryAction dea_;
  private String pdbCode_;

  private final Dimension currentSize_ = new Dimension();
  private final Rectangle rectClip_ = new Rectangle();

  public JMolPanel() {
    org.jmol.util.Logger.setActiveLevel(org.jmol.util.Logger.LEVEL_INFO, false);
    adapter_ = new SmarterJmolAdapter();
    viewer_ = JmolViewer.allocateViewer(this, adapter_);
    // jmolpopup_ = JmolPopup.newJmolPopup(viewer_, false, null);
    statusListener_ = new StatusListener(viewer_, jmolpopup_);
    viewer_.setJmolStatusListener(statusListener_);

    initViewer();
  }

  public void cleanViewer() {
    pdbCode_ = null;
    enableControls(false);
  }

  /**
   * Reset the Jmol display.
   */
  public void reset() {
    viewer_.homePosition();
  }

  public JmolViewer getViewer() {
    return viewer_;
  }

  public boolean openFile(String absPath, String pdbCode, String chain, FeatureTable feat) {
    // viewer_.evalString("quit");
    statusListener_.setOpeningInfo(chain, feat);
    viewer_.openFile(absPath);
    String strError = viewer_.getOpenFileError();
    if (strError != null) {
      EZLogger.warn(strError);
      enableControls(false);
      pdbCode_ = null;
      return false;
    } else {
      enableControls(true);
      pdbCode_ = pdbCode;
      return true;
    }
  }

  private void enableControls(boolean e) {
    ima_.setEnabled(e);
    dea_.setEnabled(e);
  }

  private void initViewer() {
    // This code from Spice-3D code to solve some Jmol problems at startup.
    viewer_.evalStringQuiet("set scriptQueue on;");
    String pdb = "ATOM     63  CA  GLY     9      47.866  28.415   2.952 \n";
    viewer_.openStringInline(pdb);
    viewer_.evalStringQuiet("select *; spacefill off;");
  }

  public void paint(Graphics g) {
    getSize(currentSize_);
    g.getClipBounds(rectClip_);
    viewer_.renderScreenImage(g, currentSize_, rectClip_);
  }

  public void addPdbModelListener(PdbModelListener l) {
    statusListener_.addPdbModelListener(l);
  }

  public void removePdbModelListener(PdbModelListener l) {
    statusListener_.addPdbModelListener(l);
  }

  public Dimension getMinimumSize() {
    return new Dimension(300, 300);
  }

  protected JToolBar getOptionalCommands() {
    ImageIcon icon;
    JToolBar tBar;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    icon = EZEnvironment.getImageIcon("earth.png");
    if (icon != null) {
      dea_ = new DisplayEntryAction("", icon);
    } else {
      dea_ = new DisplayEntryAction(Messages.getString("StructureViewer.dispEntry.btn"));
    }
    btn = tBar.add(dea_);
    btn.setToolTipText(Messages.getString("StructureViewer.dispEntry.toolTip"));
    dea_.setEnabled(false);

    icon = EZEnvironment.getImageIcon("imager.png");
    if (icon != null) {
      ima_ = new ImageManagerAction("", icon);
    } else {
      ima_ = new ImageManagerAction(Messages.getString("StructureViewer.saveImg.btn"));
    }
    btn = tBar.add(ima_);
    btn.setToolTipText(Messages.getString("StructureViewer.saveImg.toolTip"));
    ima_.setEnabled(false);

    return tBar;
  }

  /**
   * This class is used to generate an image from the JMol viewer.
   */
  private class DisplayEntryAction extends AbstractAction {
    private static final long serialVersionUID = -222631081138953189L;
    private FeatureWebLinker _linker;

    /**
     * Action constructor.
     * 
     * @param name
     *          the name of the action.
     */
    public DisplayEntryAction(String name) {
      super(name);
      _linker = new FeatureWebLinker();
    }

    /**
     * Action constructor.
     * 
     * @param name
     *          the name of the action.
     * @param icon
     *          the icon of the action.
     */
    public DisplayEntryAction(String name, Icon icon) {
      super(name, icon);
      _linker = new FeatureWebLinker();
    }

    public void actionPerformed(ActionEvent event) {
      String url, msg;
      try {
        // http://ostermiller.org/utils/Browser.html
        Browser.init();
        EZEnvironment.setWaitCursor();
        url = _linker.getURL("PDBview", pdbCode_);
        if (url == null)
          throw new Exception(Messages.getString("DDMolPanel.msg.err1"));
        Browser.displayURL(url);
        EZEnvironment.setDefaultCursor();
      } catch (Exception ex) {
        msg = Messages.getString("FeatureViewer.7");
        EZLogger.warn(msg + ": " + ex);
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), msg + ".");
      }
    }
  }

  /**
   * This class is used to generate an image from the JMol viewer.
   */
  private class ImageManagerAction extends AbstractAction {
    private static final long serialVersionUID = 8613128908419083781L;

    /**
     * Action constructor.
     * 
     * @param name
     *          the name of the action.
     */
    public ImageManagerAction(String name) {
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
    public ImageManagerAction(String name, Icon icon) {
      super(name, icon);
    }

    private File chooseFile() {
      return EZFileManager.chooseFileForSaveAction(
          Messages.getString("StructureViewer.image.dlg.header"), 
          new EZFileFilter(DocViewerConfig.PNG_FEXT, Messages.getString("StructureViewer.image.type")));
    }

    public void actionPerformed(ActionEvent event) {
      File f = chooseFile();
      if (f == null)
        return;
      new ImagerThread(f).start();
    }

    private class ImagerThread extends Thread {
      private File _f;

      public ImagerThread(File f) {
        _f = f;
      }

      public void run() {
        Image im;
        BufferedImage bImage;

        // this was added to let Jmol redraw its screen after the display
        // of the choosefile dlg. Otherwise, there is an exception during
        // getScreenImage() call.
        try {
          sleep(1000);
        } catch (InterruptedException e1) {
        }
        im = viewer_.getScreenImage();
        bImage = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        bImage.getGraphics().drawImage(im, 0, 0, null);
        try {
          ImageIO.write(bImage, "PNG", _f);
        } catch (IOException e) {
          EZLogger.warn("Unable to save image: " + e);
        }
      }
    }
  }
}
