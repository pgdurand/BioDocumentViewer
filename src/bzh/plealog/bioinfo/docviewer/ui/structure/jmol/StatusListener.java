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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.popup.JmolPopup;
import org.jmol.viewer.Viewer;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.feature.Feature;
import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.api.data.feature.utils.FeatureSystem;
import bzh.plealog.bioinfo.api.data.sequence.DRulerModel;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbModel;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbModelListener;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbSequence;

/**
 * Setup an in-house JMol Status Listener suitable for this viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class StatusListener implements JmolStatusListener {
  private JmolViewer viewer_;
  // private JmolPopup jmolpopup_;
  private PdbModel seqModel_;
  private String chain_;
  private FeatureTable feat_;

  public StatusListener(JmolViewer viewer, JmolPopup jmolpopup) {
    viewer_ = viewer;
    // jmolpopup_ = jmolpopup;
    seqModel_ = new PdbModel();
  }

  public void createImage(String arg0, Object arg1, int arg2) {
  }

  public String eval(String arg0) {
    return null;
  }

  public float[][] functionXY(String arg0, int arg1, int arg2) {
    return null;
  }

  public void handlePopupMenu(int x, int y) {
    // jmolpopup_.show(x, y);
  }

  public void notifyAtomHovered(int arg0, String arg1) {
  }

  public void notifyAtomPicked(int arg0, String arg1) {
    int idx = arg1.indexOf('#');
    if (idx != -1)
      EZLogger.info("Atom picked: " + arg1.substring(0, idx));
    else
      EZLogger.info("Atom picked: " + arg1);
  }

  @SuppressWarnings("rawtypes")
  private void lookForChain(Vector models, boolean proteic) {
    PdbSequence seqChain;
    FeatureTable fTable;
    Hashtable model, chain;
    Vector chains, residues;
    int j, from, to;

    // models: used by NMR, so get only first model in all cases
    if (!models.isEmpty()) {
      model = (Hashtable) models.get(0);
      chains = (Vector) model.get("chains");
      for (j = 0; j < chains.size(); j++) {
        chain = (Hashtable) chains.get(j);
        residues = (Vector) chain.get("residues");
        seqChain = JMolTranscoder.createSequenceFromChain(residues, proteic);
        if (seqChain != null) {
          if (proteic) {
            fTable = analyseStructure(seqChain);
            if (fTable != null && fTable.features() != 0) {
              if (seqChain.getChainCode().equals(chain_)) {
                DRulerModel rModel = seqChain.getSequence().getRulerModel();
                Feature ft;
                Enumeration enu;
                if (feat_ != null) {
                  enu = feat_.enumFeatures();
                  while (enu.hasMoreElements()) {
                    ft = (Feature) enu.nextElement();
                    if (rModel != null) {
                      // to map sequence coordinates (contained in the feat_
                      // object when not null)
                      // to PDB chain, we can consider that seq coord are
                      // absolute index ranging
                      // from 1 to seq size. As a consequence, we just have to
                      // map these seq coord
                      // useing the DRulerModel of the PDB Chain.
                      // be sure we are in the chain coord range
                      from = Math.max(0, ft.getFrom() - 1);
                      to = Math.min(seqChain.getSequence().size() - 1, ft.getTo() - 1);
                      // remap coord
                      ft.setFrom(rModel.getSeqPos(from));
                      ft.setTo(rModel.getSeqPos(to));
                    }
                    fTable.addFeature(ft);
                  }
                }
                fTable.sort(FeatureTable.POS_SORTER);
              }
              seqChain.setFTable(fTable);
            }
          }
          seqModel_.add(seqChain);
        }
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public void notifyFileLoaded(String arg0, String arg1, String arg2, Object arg3, String arg4) {
    Hashtable ht;

    if (arg0 == null)
      return;
    if (arg0.equals("string"))
      return;
    /*
     * UserUIMessenger.setInfoStatusMessage("PDB file loaded: "+arg0);
     * UserUIMessenger.setInfoStatusMessage("ModelCount: "
     * +viewer_.getModelCount()); UserUIMessenger.setInfoStatusMessage(
     * "ChainCount: "+viewer_.getChainCount());
     * UserUIMessenger.setInfoStatusMessage("PoymerCount: "
     * +viewer_.getPolymerCount());
     */

    // possible expression of interest here: protein, dna, rna
    seqModel_.clear();
    try {
      ht = ((Viewer) viewer_).getAllChainInfo("protein");
      lookForChain((Vector) ht.get("models"), true);
      ht = ((Viewer) viewer_).getAllChainInfo("dna");
      lookForChain((Vector) ht.get("models"), false);
      ht = ((Viewer) viewer_).getAllChainInfo("rna");
      lookForChain((Vector) ht.get("models"), false);
    } catch (Exception e) {

    }
  }

  @SuppressWarnings("rawtypes")
  private void analyseStructure(PdbSequence chain, FeatureTable fTable, String type) {
    Hashtable structure, model, chainHT;
    Vector models, chains, residues;
    int j;

    // possible expression: protein, backbone, helix, sheet, turn
    structure = ((Viewer) viewer_).getAllChainInfo(type);
    models = (Vector) structure.get("models");
    if (models == null || models.isEmpty())
      return;
    // models: used for NMR, only get first model in all case
    model = (Hashtable) models.get(0);
    chains = (Vector) model.get("chains");
    for (j = 0; j < chains.size(); j++) {
      chainHT = (Hashtable) chains.get(j);
      residues = (Vector) chainHT.get("residues");
      JMolTranscoder.createFeatureTableFromChain(chain, type, fTable, residues, true);
    }
  }

  private FeatureTable analyseStructure(PdbSequence chain) {
    FeatureTable fTable;

    fTable = FeatureSystem.getFeatureTableFactory().getFTInstance();
    analyseStructure(chain, fTable, "helix");
    analyseStructure(chain, fTable, "sheet");
    fTable.sort(FeatureTable.POS_SORTER);
    return fTable;
  }

  public void notifyFrameChanged(int arg0, int arg1, int arg2, int arg3, int arg4) {
  }

  public void notifyNewDefaultModeMeasurement(int arg0, String arg1) {
  }

  public void notifyNewPickingModeMeasurement(int arg0, String arg1) {
  }

  public void notifyResized(int arg0, int arg1) {
  }

  public void notifyScriptStart(String arg0, String arg1) {
  }

  public void notifyScriptTermination(String arg0, int arg1) {
  }

  public void sendConsoleEcho(String arg0) {
  }

  public void sendConsoleMessage(String arg0) {
  }

  public void sendSyncScript(String arg0, String arg1) {
  }

  public void setCallbackFunction(String arg0, String arg1) {
  }

  public void showConsole(boolean arg0) {
  }

  public void showUrl(String arg0) {
  }

  @SuppressWarnings("rawtypes")
  public Hashtable getRegistryInfo() {
    return null;
  }

  public void addPdbModelListener(PdbModelListener l) {
    seqModel_.addPdbModelListener(l);
  }

  public void removePdbModelListener(PdbModelListener l) {
    seqModel_.removePdbModelListener(l);
  }

  public void setOpeningInfo(String chain, FeatureTable feat) {
    chain_ = chain;
    feat_ = feat;
  }
}
