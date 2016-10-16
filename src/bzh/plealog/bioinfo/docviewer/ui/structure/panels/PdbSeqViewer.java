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
package bzh.plealog.bioinfo.docviewer.ui.structure.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import bzh.plealog.bioinfo.api.data.feature.Feature;
import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.api.data.feature.utils.FeatureSelectionEvent;
import bzh.plealog.bioinfo.api.data.feature.utils.FeatureSelectionListener;
import bzh.plealog.bioinfo.api.data.sequence.DRulerModel;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceModel;
import bzh.plealog.bioinfo.api.data.sequence.DViewerSystem;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.structure.jmol.JMolCommander;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbModelEvent;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbModelListener;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbSequence;
import bzh.plealog.bioinfo.ui.feature.FeatureViewer;
import bzh.plealog.bioinfo.ui.feature.FeatureWebLinker;
import bzh.plealog.bioinfo.ui.hca.DDPanelHCA;
import bzh.plealog.bioinfo.ui.sequence.basic.DRulerViewer;
import bzh.plealog.bioinfo.ui.sequence.basic.DSequenceListViewer;
import bzh.plealog.bioinfo.ui.sequence.basic.DSequenceViewer;
import bzh.plealog.bioinfo.ui.sequence.basic.DViewerScroller;
import bzh.plealog.bioinfo.ui.sequence.event.DSelectionListenerSupport;
import bzh.plealog.bioinfo.ui.sequence.event.SelectSequenceAction;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.common.ContextMenuElement;
import com.plealog.genericapp.ui.common.ImageManagerAction;

/**
 * Setup sequence viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class PdbSeqViewer extends JPanel implements PdbModelListener {
  private static final long serialVersionUID = 5133996349893352499L;
  private DSequenceViewer			_qViewer;
	private DViewerScroller			_scroller;
	private DDPanelHCA				_hcaViewer;
	private FeatureViewer			_featViewer;
	private JMolCommander			_commander;
	private DSequenceModel			_hSM;
	private JComboBox<PdbSequence>	_chainSelector;
	private Font					_fnt			= new Font("Arial", Font.PLAIN, 12);
	private FontMetrics				_fm;
	private JTabbedPane				_jtp;
	private PdbSequence			_curSeq;
	private SelectSequenceAction	_selectAllMnu;
	private SelectSequenceAction	_selectNoneMnu;

	private static final int		SCROLL_BORDER	= 2;
	//private static final int		SELECTED_SEQ	= 1;
	//private static final int		FULL_SEQ		= 2;

	public PdbSeqViewer() {
		DSequenceListViewer master;
		DRulerViewer drv;
		JPanel seqViewer, chainSelector;
		Box box2;
		Dimension dim;
		JSplitPane jsp;
		int cellH;

		_fm = this.getFontMetrics(_fnt);
		//assemble the viewer
		_hcaViewer = new DDPanelHCA();
		seqViewer = new JPanel();
		seqViewer.setLayout(new BoxLayout(seqViewer, BoxLayout.Y_AXIS));

		master = createViewer("");
		drv = new DRulerViewer(((DSequenceModel) master.getModel()).getSequence().createRulerModel(1, 1), 15, SwingConstants.HORIZONTAL, SwingConstants.TOP);
		_qViewer = new DSequenceViewer(master, drv, false);
		_qViewer.setAlignmentX(0);
		drv.setBoxSize(_fm.getHeight());
		_hSM = (DSequenceModel) master.getModel();
		seqViewer.add(_qViewer);

		seqViewer.add(Box.createVerticalGlue());
		seqViewer.setOpaque(true);
		seqViewer.setBackground(Color.white);

		//prepare the scroller
		_scroller = new DViewerScroller(seqViewer);
		_scroller.setCellWidth(_qViewer.getSequenceList().getFixedCellWidth());
		_scroller.setCellHeight(_qViewer.getSequenceList().getFixedCellHeight());
		cellH = _qViewer.getSequenceList().getFixedCellWidth();
		dim = new Dimension(120, _hcaViewer.getPreferredHeight()/*3*cellH+scrollWidth*/);
		_scroller.getHorizontalScrollBar().setBlockIncrement(50 * cellH);
		_scroller.getHorizontalScrollBar().setUnitIncrement(cellH);
		_scroller.setPreferredSize(dim);
		_scroller.setMinimumSize(dim);
		_scroller.setOpaque(true);
		_scroller.setBackground(Color.white);

		_featViewer = new FeatureViewer(new FeatureWebLinker(), false);
		_featViewer.setAutoSelectFirstFeature(false);
		_featViewer.addFeatureSelectionListener(new SeqFeatureSelectionListener());

		_jtp = new JTabbedPane();
		_jtp.setFocusable(false);
		_jtp.setTabPlacement(JTabbedPane.RIGHT);
		_jtp.add("1D", _scroller);
		_jtp.add("HCA", new JScrollPane(_hcaViewer));

		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _jtp, _featViewer);
		jsp.setPreferredSize(dim);
		jsp.setMaximumSize(dim);
		jsp.setResizeWeight(1.0);
		jsp.setOneTouchExpandable(true);
		_chainSelector = new JComboBox<>();
		_chainSelector.addActionListener(new ChainSelectorActionListener());
		box2 = Box.createVerticalBox();
		box2.add(new JLabel("Chain:"));
		box2.add(_chainSelector);

		chainSelector = new JPanel(new BorderLayout());
		chainSelector.add(box2, BorderLayout.NORTH);
		this.setLayout(new BorderLayout());
		this.add(chainSelector, BorderLayout.WEST);
		this.add(jsp, BorderLayout.CENTER);
	}

	public JToolBar getOptionalCommands() {
		return null;
	}

	public void registerSelectionListenerSupport(DSelectionListenerSupport lSupport) {
		_hcaViewer.registerSelectionListenerSupport(lSupport);
		_qViewer.registerSelectionListenerSupport(lSupport);
		ArrayList<ContextMenuElement> actions;

		_selectAllMnu = new SelectSequenceAction("Select all", SelectSequenceAction.SelectType.ALL);
		_selectAllMnu.setListenerSupport(lSupport);
		_selectNoneMnu = new SelectSequenceAction("Clear selection", SelectSequenceAction.SelectType.CLEAR);
		_selectNoneMnu.setListenerSupport(lSupport);
		actions = new ArrayList<ContextMenuElement>();
		actions.add(new ContextMenuElement(_selectAllMnu));
		actions.add(new ContextMenuElement(_selectNoneMnu));

		//_qViewer.setContextMenu(new ContextMenuManager(_qViewer, actions));
		//make a copy of standard commands, then add a save image action
		//for the HCA panel
		ArrayList<ContextMenuElement> actions2;
		actions2 = new ArrayList<ContextMenuElement>();
		for (ContextMenuElement cme : actions) {
			actions2.add(cme);
		}
		//save image 
		ImageManagerAction imager;
		imager = new ImageManagerAction(Messages.getString("ImageManagerAction.save.btn"), EZEnvironment.getImageIcon("imager_s.png"));
		imager.setComponent(_hcaViewer);
		actions2.add(null);
		actions2.add(new ContextMenuElement(imager));
		//_hcaViewer.setContextMenu(new ContextMenuManager(_hcaViewer, actions2));
	}

	public void setCommander(JMolCommander commander) {
		_commander = commander;
	}

	private DSequenceListViewer createViewer(String seq) {
		DSequenceListViewer example = new DSequenceListViewer();
		example.setFont(_fnt);

		DSequenceModel model = new DSequenceModel(DViewerSystem.getSequenceFactory().getSequence(new StringReader(seq), DViewerSystem.getIUPAC_Protein_Alphabet()));
		example.setModel(model);
		return (example);
	}

	private int compH() {
		Dimension dim;
		int h;

		dim = _scroller.getPreferredSize();
		h = dim.height;
		dim = _featViewer.getPreferredSize();
		h += dim.height;
		return h + 2 * SCROLL_BORDER;
	}

	public Dimension getPreferredSize() {
		return new Dimension(250, compH());
	}

	public Dimension getMinimumSize() {
		return new Dimension(150, compH());
	}

	public void cleanViewer() {
		_chainSelector.removeAllItems();
		_qViewer.setModel(_hSM, 0, 1);
		_featViewer.clear();
		_hcaViewer.setSequence((DSequence) null);
		_curSeq = null;
		_selectAllMnu.setSequence(null);
		_selectNoneMnu.setSequence(null);
	}

	protected void displaySequence(PdbSequence seq) {
		DSequenceModel model;
		FeatureTable fTable;

		model = new DSequenceModel(seq.getSequence());
		_featViewer.clear();
		_qViewer.setModel(model);
		_scroller.getHorizontalScrollBar().setValue(0);
		fTable = seq.getFTable();
		if (fTable != null) {
			_featViewer.setData(fTable);
		}
		_hcaViewer.setSequence(seq.getSequence());
		if (_commander != null)
			_commander.setChainCode(seq.getChainCode().toLowerCase());
		_selectAllMnu.setSequence(seq.getSequence());
		_selectNoneMnu.setSequence(seq.getSequence());
	}

	public void pdbModelChanged(PdbModelEvent event) {
		PdbSequence seq;
		if (event.getEventType() == PdbModelEvent.MODEL_CLEARED) {
			cleanViewer();
		} else if (event.getEventType() == PdbModelEvent.CHAIN_ADDED) {
			seq = event.getPdbSequence();
			_chainSelector.addItem(seq);
		}
	}

	public PdbSequence getDisplayedSequence() {
		return _curSeq;
	}

	private class ChainSelectorActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			PdbSequence seq;

			seq = (PdbSequence) _chainSelector.getSelectedItem();
			_curSeq = seq;
			if (seq != null)
				displaySequence(seq);
		}
	}

	private class SeqFeatureSelectionListener implements FeatureSelectionListener {
		public void featureSelected(FeatureSelectionEvent event) {
			Feature feature;
			JScrollBar sBar;
			DSequenceListViewer dlv;
			DRulerModel drm;
			DSequenceModel dsm;
			int val, max, sFrom, sTo;

			feature = event.getFeature();
			dlv = (DSequenceListViewer) _qViewer.getSequenceList();
			dsm = (DSequenceModel) dlv.getModel();
			drm = dsm.getSequence().getRulerModel();
			if (feature != null) {
				if (feature.getFrom() <= feature.getTo()) {
					sFrom = drm.getRulerPos(feature.getFrom());
					sTo = drm.getRulerPos(feature.getTo());
				} else {
					sFrom = drm.getRulerPos(feature.getTo());
					sTo = drm.getRulerPos(feature.getFrom());
				}
				dlv.setSelectionInterval(sFrom, sTo);
				_hcaViewer.setSelectedSequenceRange(sFrom, sTo);
				val = feature.getFrom();
				sBar = _scroller.getHorizontalScrollBar();
				val = Math.max(0, drm.getRulerPos(val) - 1) * dlv.getFixedCellWidth();
				sFrom = sBar.getValue();
				sTo = sFrom + sBar.getVisibleAmount() - 3;
				sFrom -= 3;
				if (val <= sFrom || val >= sTo) {
					max = sBar.getMaximum() - sBar.getVisibleAmount();
					if (val > max) {
						val = max;
					}
					sBar.setValue(val);
				}
			} else {
				dlv.clearSelection();
				_hcaViewer.setSelectedSequenceRange(-1, -1);
			}
		}

		public void featureTypesSelected(String[] types) {
		}

	}


}
