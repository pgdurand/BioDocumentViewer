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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import bzh.plealog.bioinfo.api.data.sequence.DLocation;
import bzh.plealog.bioinfo.api.data.sequence.DRulerModel;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbSequence;
import bzh.plealog.bioinfo.docviewer.ui.structure.panels.PdbSeqViewer;
import bzh.plealog.bioinfo.ui.sequence.event.DSelectionListenerSupport;
import bzh.plealog.bioinfo.ui.sequence.event.DSequenceSelectionEvent;
import bzh.plealog.bioinfo.ui.sequence.event.DSequenceSelectionListener;
import bzh.plealog.bioinfo.ui.sequence.event.IntervalBuilder;
import bzh.plealog.bioinfo.ui.util.RetractPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Setup the commands of the 3D Viewer. This panel is displayed on the right side of the viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class JMolCommander extends JPanel {
  private static final long serialVersionUID = -8587734455770215036L;
  private Hashtable<String, Command> commands_ = new Hashtable<String, Command>();
  private DDMolSimpleScriptActionListener scriptActListerner_ = new DDMolSimpleScriptActionListener();
  private SeqSelectionListener seqSelListener_ = new SeqSelectionListener();
  private DSelectionListenerSupport _lSupport;
  private JColorComboBox structColorChooser_;
  private JColorComboBox bkColorChooser_;
  private JTextField posSelector_;
  private JCheckBox selSideChainCheck_;
  private RetractPanel chainPanel_;
  private JMolPanel viewer_;
  private PdbSeqViewer seqViewer_;
  private String[] viewCommands_ = { "View: ", "a1", "a2", "a3", "a4", "a5", "a6" };
  private String[] renderCommands_ = { "Backbone: ", "b1", "b2", "b3", "b5", "b6", "b7" };
  private String[] sideChainCommands_ = { "Side chains: ", "f1", "f2" };
  private String[] sideChainOnSelCommands_ = { "Side chains: ", "g1", "g2" };
  private String[] waterCommands_ = { "Water: ", "c1", "c2" };
  private String[] axesCommands_ = { "3D axes: ", "e1", "e2" };
  private String[] pickCenterCommands_ = { "Pick center: ", "h1", "h2" };
  private String[] spinCommands_ = { "Spin: ", "i1", "i2" };
  private String[] delMeasuresCommands_ = { "Measures: ", "d1" };
  private String wireFrameThick_ = "0.25";
  private String ballThick_ = "0.4";
  private String bboneStyle_ = BBONE_ROCKET;
  private String chainCode_ = "a";
  private Command currentBBoneStyle_;
  private boolean sideChainsOn_;
  private boolean waterOn_;
  private boolean structure_;

  private static final String CPK_LBL = "CPK";
  private static final String AMINO_LBL = "amino";

  private static final String HEADER_1 = "Panel";
  private static final String HEADER_2 = "Structure";
  private static final String HEADER_3 = "Chain";
  private static final String HEADER_4 = "Toolbar";

  private static final ComboColor[] STRUCT_COLORS_TABLE = new ComboColor[] {
      // Possible colours: cf
      // http://chemapps.stolaf.edu/jmol/docs/examples/structure.htm
      new ComboColor(CPK_LBL, Color.white), new ComboColor(AMINO_LBL, Color.white),
      new ComboColor("white", Color.white), new ComboColor("light gray", Color.lightGray),
      new ComboColor("gray", Color.gray), new ComboColor("dark gray", Color.darkGray),
      new ComboColor("black", Color.black), new ComboColor("red", Color.red), new ComboColor("green", Color.green),
      new ComboColor("blue", Color.blue), new ComboColor("cyan", Color.cyan), new ComboColor("magenta", Color.magenta),
      new ComboColor("orange", Color.orange), new ComboColor("pink", Color.pink),
      new ComboColor("yellow", Color.yellow) };

  private static final ComboColor[] BK_COLORS_TABLE = new ComboColor[] { new ComboColor("black", Color.black),
      new ComboColor("white", Color.white), new ComboColor("light gray", Color.lightGray),
      new ComboColor("gray", Color.gray), new ComboColor("dark gray", Color.darkGray),
      new ComboColor("yellow", new Color(255, 255, 192)) };

  private static final Font CTRL_FNT = new Font("sans-serif", Font.PLAIN, 12);
  private static final Font HEADER_FNT = new Font("sans-serif", Font.PLAIN, 12);

  private static final String PROP_SIDECHAIN = "sideChainProp";
  private static final String PROP_WATER = "waterProp";
  private static final String PROP_BBONE_STYLE = "backBoneStyleProp";

  private static final String BBONE_SPACEFILL = "spaceFill";
  private static final String BBONE_BALSTICK = "ballAndStick";
  private static final String BBONE_STICK = "stick";
  private static final String BBONE_CARTOON = "cartoon";
  private static final String BBONE_ROCKET = "rocket";
  private static final String BBONE_TRACE = "trace";

  public static final String EMPTY_CMD = "exit; zap;";
  public static final String SSBONDS_CMD = "set bondmode or; select cys and sidechain; color yellow; wireframe WIREFRAME_THICK; ssbonds WIREFRAME_THICK;";

  public static final String INIT_SCRIPT_CMD = "restrict not selected;select not selected;cartoons off;set cartoonRockets true;"
      + "rockets on;color structure; SSBONDS_DISP;";
  // private static Logger logger_ = Logger.getLogger("kb."+"DDMolCommander");

  public JMolCommander() {
    commands_ = new Hashtable<String, Command>();
    scriptActListerner_ = new DDMolSimpleScriptActionListener();
    initCommands();
    structColorChooser_ = new JColorComboBox(STRUCT_COLORS_TABLE);
    bkColorChooser_ = new JColorComboBox(BK_COLORS_TABLE);
    posSelector_ = createJTextField();
    this.addPropertyChangeListener(new DDMolPropertyChangeListener());
  }

  public JMolCommander(JMolPanel viewer, PdbSeqViewer seqViewer) {
    this();
    viewer_ = viewer;
    seqViewer_ = seqViewer;
    this.setLayout(new BorderLayout());
    this.add(buildGUI(), BorderLayout.CENTER);
  }

  private static final String DEF_STYLE = "select all;spacefill off; wireframe off;cartoon;color cartoon chain;select ligand;wireframe 40;spacefill 120;select protein;";
  private static final String PROPS_STYLE = "SIDE_CHAIN; WATER_DISP; SSBONDS_DISP;";

  private void initCommands() {
    // commandes JMol: org.jmol.popup.PopupResourceBundle
    // ressources du menu:
    // org.openscience.jmol.Properties.Jmol-resources.properties
    // selecting particular residue: "select 10:a;"
    // changing color: "color [x00FF00];" -> s'applique a la selection
    // affichage sidechain: "set bondmode or; select 10:a and sidechain; color
    // green; wireframe 0.30;"
    // changementde styles sur selection de residus: $ set bondmode or; select
    // 1:a, 2:a and sidechain; wireframe;
    // JMol script reference: http://chemapps.stolaf.edu/jmol/docs/
    // JMol tutorial:
    // http://www.callutheran.edu/Academic_Programs/Departments/BioDev/omm/scripting/molmast.htm

    // TODO: mettre le texte des commandes dans un fichier de config
    // commands to modify the camera orientation
    commands_.put("a1", new Command("a1", "front", Box("moveto 2.0 front;delay 1"), null, null));
    commands_.put("a2", new Command("a2", "left", Box("moveto 1.0 front;moveto 2.0 left;delay 1"), null, null));
    commands_.put("a3", new Command("a3", "right", Box("moveto 1.0 front;moveto 2.0 right;delay 1"), null, null));
    commands_.put("a4", new Command("a4", "top", Box("moveto 1.0 front;moveto 2.0 top;delay 1"), null, null));
    commands_.put("a5", new Command("a5", "bottom", Box("moveto 1.0 front;moveto 2.0 bottom;delay 1"), null, null));
    commands_.put("a6", new Command("a6", "back", Box("moveto 1.0 front;moveto 2.0 back;delay 1"), null, null));
    // TODO: quid si pas de chaine ?
    // commands to modify the backbone style

    commands_.put("b1",
        new Command("b1", "Spacefill", DEF_STYLE
            + "restrict not selected;select not selected and not sidechain;spacefill 100%;color cpk; " + PROPS_STYLE,
        PROP_BBONE_STYLE, BBONE_SPACEFILL));
    commands_.put("b2",
        new Command("b2", "Ball & Stick",
            DEF_STYLE
                + "restrict not selected;select not selected and not sidechain;spacefill BALL_THICK;wireframe WIREFRAME_THICK;color cpk; "
                + PROPS_STYLE,
            PROP_BBONE_STYLE, BBONE_BALSTICK));
    commands_.put("b3",
        new Command("b3", "Sticks",
            DEF_STYLE
                + "restrict not selected;select not selected and not sidechain;wireframe WIREFRAME_THICK;color cpk; "
                + PROPS_STYLE,
            PROP_BBONE_STYLE, BBONE_STICK));
    commands_.put("b4",
        new Command("b4", "Wireframe",
            DEF_STYLE
                + "restrict not selected;select not selected and not sidechain;wireframe WIREFRAME_THICK;color cpk; "
                + PROPS_STYLE,
            PROP_BBONE_STYLE, BBONE_STICK));
    commands_.put("b5",
        new Command("b5", "Cartoons",
            DEF_STYLE
                + "restrict not selected;select not selected;set cartoonRockets false;cartoons on;color cartoon structure; "
                + PROPS_STYLE,
            PROP_BBONE_STYLE, BBONE_CARTOON));
    commands_
        .put("b6",
            new Command("b6", "Rockets",
                DEF_STYLE
                    + "restrict not selected;select not selected;set cartoonRockets true;rockets on;color structure; "
                    + PROPS_STYLE,
                PROP_BBONE_STYLE, BBONE_ROCKET));
    commands_.put("b7",
        new Command("b7", "Trace",
            DEF_STYLE + "restrict not selected;select not selected;trace on;color structure; " + PROPS_STYLE,
            PROP_BBONE_STYLE, BBONE_TRACE));
    // b6:select ligand;wireframe 40;spacefill 120;select protein; wireframe
    // off; spacefill off;cartoons;
    // b6: select ligand;wireframe 40;spacefill 120;select nucleic; spacefill
    // off; wireframe off;cartoon;select protein; spacefill off; wireframe
    // off;set cartoonRockets true;rockets on;cartoons off;color structure;
    // select all;spacefill off; wireframe off;cartoon;color cartoon
    // chain;select ligand;wireframe 40;spacefill 120;
    // select nucleic; wireframe WIREFRAME_THICK; color cpk;select protein;
    // restrict not selected;select not selected;set cartoonRockets true;rockets
    // on;color structure; SIDE_CHAIN; WATER_DISP; SSBONDS_DISP;
    // select nucleic; restrict not selected;select not selected;cartoons on;
    // color cpk;
    // test: 10mh

    commands_.put("c1",
        new Command("c1", "on", "select water; color cpk; spacefill 120; select not water;", PROP_WATER, Boolean.TRUE));
    commands_.put("c2",
        new Command("c2", "off", "select water; restrict not selected; select not water;", PROP_WATER, Boolean.FALSE));

    // Commands to delete user's measures
    commands_.put("d1", new Command("d1", "delete", "measures delete;", null, null));

    // commands to display 3D axes
    commands_.put("e1", new Command("e1", "on", "set showAxes true;", null, null));
    commands_.put("e2", new Command("e2", "off", "set showAxes false;", null, null));

    // commands to display side chains for an entire chain
    commands_.put("f1",
        new Command("f1", "on",
            "set bondmode or; select protein and sidechain; color CPK; SIDECHAIN_STYLE; SSBONDS_DISP;", PROP_SIDECHAIN,
            Boolean.TRUE));
    commands_.put("f2", new Command("f2", "off",
        "select protein and sidechain; wireframe off; spacefill off; SSBONDS_DISP;", PROP_SIDECHAIN, Boolean.FALSE));

    // commands to apply styles on selected positions
    commands_.put("g1",
        new Command("g1", "on",
            "set bondmode or; select USER_SELECTION; color cartoon USER_COLOR; color USER_COLOR; SIDECHAIN_STYLE;SSBONDS_DISP;",
            null, null));
    commands_.put("g2",
        new Command("g2", "off",
            "select USER_SELECTION; color cartoon USER_COLOR; color USER_COLOR; restrict not sidechain; SSBONDS_DISP;",
            null, null));

    // commands to pick a new center
    commands_.put("h1", new Command("h1", "on", "set picking center;", null, null));
    commands_.put("h2", new Command("h2", "off", "set picking off; ", null, null));

    // commands to spin structure
    commands_.put("i1", new Command("i1", "on", "spin on;", null, null));
    commands_.put("i2", new Command("i2", "off", "spin off; ", null, null));
  }

  /*
   * public Dimension getPreferredSize(){ return new Dimension(250,250); }
   * public Dimension getMinimumSize(){ return new Dimension(150,250); } public
   * Dimension getMaximumSize(){ return new Dimension(250,650); }
   */
  public synchronized void executeCmd(String command) {
    /*
     * if (viewer_.getViewer().isScriptExecuting()){ logger_.info(
     * "viewer is executing"); return; }
     */
    viewer_.getViewer().evalStringQuiet(prepareCmd(command));
  }

  public String getChainCode() {
    return chainCode_;
  }

  public void setChainCode(String chainCode) {
    chainCode_ = chainCode;
    if (chainCode_ != null) {
      chainPanel_.setTitle(HEADER_3 + " [" + chainCode_ + "]");
    } else {
      chainPanel_.setTitle(HEADER_3);
    }
  }

  public void applyDefaultBBoneStyle() {
    if (currentBBoneStyle_ != null) {
      currentBBoneStyle_.execute();
    } else {
      executeCmd(INIT_SCRIPT_CMD);
    }
  }

  public void resetView() {
    executeCmd(EMPTY_CMD);
  }

  public void registerSelectionListenerSupport(DSelectionListenerSupport lSupport) {
    _lSupport = lSupport;
    lSupport.addDSequenceSelectionListener(seqSelListener_);
  }

  public void setStructure(boolean val) {
    structure_ = val;
  }

  private JPanel buildGUI() {
    DefaultFormBuilder builder;
    FormLayout layout;
    JPanel formsPanel, mainPanel;
    RetractPanel rPanel1, rPanel2, rPanel3, rPanel4;
    JButton applyBtn;

    formsPanel = new JPanel();
    formsPanel.setLayout(new BoxLayout(formsPanel, BoxLayout.Y_AXIS));

    // panel 3 : Panel display
    layout = new FormLayout("right:60dlu, 2dlu, 40dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    buildSelector(builder, viewCommands_);
    builder.append(createJLabel("Background: "), bkColorChooser_);
    bkColorChooser_.addActionListener(new BKColorActionListener());
    buildBtn(builder, delMeasuresCommands_);
    buildOnOffCheck(builder, axesCommands_, true);
    buildOnOffCheck(builder, pickCenterCommands_, true);
    buildOnOffCheck(builder, spinCommands_, true);

    rPanel3 = new RetractPanel(HEADER_1, builder.getContainer(), true, true);
    rPanel3.setFont(HEADER_FNT);
    formsPanel.add(rPanel3);

    formsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

    // panel 1 : Structure display
    layout = new FormLayout("right:60dlu, 2dlu, 50dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    buildSelector(builder, renderCommands_);
    buildOnOffCheck(builder, sideChainCommands_, true);
    buildOnOffCheck(builder, waterCommands_, true);

    rPanel1 = new RetractPanel(HEADER_2, builder.getContainer(), true, true);
    rPanel1.setFont(HEADER_FNT);
    formsPanel.add(rPanel1);

    formsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

    // panel 2: Selection properties
    layout = new FormLayout("right:60dlu, 2dlu, 40dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    builder.append(createJLabel("Range: "), posSelector_);
    buildColorChooser(builder);
    selSideChainCheck_ = buildOnOffCheck(builder, sideChainOnSelCommands_, false);
    applyBtn = createJButton("Select!");
    applyBtn.addActionListener(new DDMolApplyGraphicsActionListener());
    builder.append(new JLabel(), applyBtn);
    rPanel2 = new RetractPanel(HEADER_3, builder.getContainer(), true, true);
    rPanel2.setFont(HEADER_FNT);
    formsPanel.add(rPanel2);
    chainPanel_ = rPanel2;

    rPanel4 = new RetractPanel(HEADER_4, getToolbar(), true, true);
    rPanel4.setFont(HEADER_FNT);
    formsPanel.add(rPanel4);
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(formsPanel, BorderLayout.NORTH);
    return mainPanel;
  }

  protected JPanel getToolbar() {
    JPanel pnl;

    pnl = new JPanel();
    if (viewer_.getOptionalCommands() != null)
      pnl.add(viewer_.getOptionalCommands());
    if (seqViewer_.getOptionalCommands() != null)
      pnl.add(seqViewer_.getOptionalCommands());
    return pnl;
  }

  private JLabel createJLabel(String msg) {
    JLabel lbl;

    lbl = new JLabel(msg);
    lbl.setFont(CTRL_FNT);
    return lbl;
  }

  /*
   * private JRadioButton createJRadioButton(String msg){ JRadioButton lbl;
   * 
   * lbl = new JRadioButton(msg); lbl.setFont(CTRL_FNT); return lbl; }
   */
  private JButton createJButton(String msg) {
    JButton lbl;

    lbl = new JButton(msg);
    lbl.setFont(CTRL_FNT);
    return lbl;
  }

  private JComboBox<Command> createJComboBox() {
    JComboBox<Command> lbl;

    lbl = new JComboBox<Command>();
    lbl.setFont(CTRL_FNT);
    return lbl;
  }

  private JTextField createJTextField() {
    JTextField jtf;

    jtf = new JTextField();
    jtf.setFont(CTRL_FNT);
    return jtf;
  }

  private void buildSelector(DefaultFormBuilder builder, String[] params) {
    JLabel lbl;
    JComboBox<Command> combo;

    lbl = createJLabel(params[0]);
    combo = createJComboBox();
    for (int i = 1; i < params.length; i++) {
      combo.addItem(commands_.get(params[i]));
    }
    combo.addActionListener(new DDMolActionListener());
    combo.setSelectedIndex(4);
    builder.append(lbl, combo);
  }

  private void buildColorChooser(DefaultFormBuilder builder) {
    JLabel lbl;

    lbl = createJLabel("Colour: ");
    structColorChooser_.setSelectedIndex(0);
    builder.append(lbl, structColorChooser_);
  }

  private JCheckBox buildOnOffCheck(DefaultFormBuilder builder, String[] params, boolean setAutoAction) {
    JLabel lbl;
    JCheckBox check;

    lbl = createJLabel(params[0]);
    check = new JCheckBox();
    if (setAutoAction) {
      check.addActionListener(
          new DDMolOnOffScriptActionListener(commands_.get(params[1]).getKey(), commands_.get(params[2]).getKey()));
    }
    builder.append(lbl, check);
    return check;
  }

  /*
   * private void buildOnOff(DefaultFormBuilder builder, String[] params){
   * JPanel btnPnl; JLabel lbl; JRadioButton onBtn, offBtn; ButtonGroup group;
   * 
   * lbl = createJLabel(params[0]);
   * 
   * onBtn = createJRadioButton(commands_.get(params[1]).getName());
   * onBtn.setActionCommand(commands_.get(params[1]).getKey());
   * onBtn.addActionListener(scriptActListerner_);
   * 
   * offBtn = createJRadioButton(commands_.get(params[2]).getName());
   * offBtn.setActionCommand(commands_.get(params[2]).getKey());
   * offBtn.addActionListener(scriptActListerner_);
   * 
   * btnPnl = new JPanel(new BorderLayout()); btnPnl.add(onBtn,
   * BorderLayout.WEST); btnPnl.add(offBtn, BorderLayout.EAST);
   * 
   * group = new ButtonGroup(); group.add(onBtn); group.add(offBtn);
   * offBtn.setSelected(true); builder.append(lbl, btnPnl); }
   */
  private void buildBtn(DefaultFormBuilder builder, String[] params) {
    JLabel lbl;
    JButton btn;

    lbl = createJLabel(params[0]);
    btn = createJButton(commands_.get(params[1]).getName());
    btn.setActionCommand(commands_.get(params[1]).getKey());
    btn.addActionListener(scriptActListerner_);
    builder.append(lbl, btn);
  }

  private static String Box(String cmd) {
    return "if not(showBoundBox);if not(showUnitcell);boundbox on;" + cmd + ";boundbox off;else;" + cmd
        + ";endif;endif;";
  }

  /**
   * Compile a particular command command before its execution by JMol.
   */
  private String prepareCmd(String cmd) {
    String newCmd = cmd;
    Command com;

    // System.out.println("before: "+cmd);
    if (newCmd.indexOf("SIDE_CHAIN") != -1) {
      if (sideChainsOn_)
        com = commands_.get("f1");
      else
        com = commands_.get("f2");
      newCmd = newCmd.replaceAll("SIDE_CHAIN", com.getCommand());
    }

    if (newCmd.indexOf("WATER_DISP") != -1) {
      if (waterOn_)
        com = commands_.get("c1");
      else
        com = commands_.get("c2");
      newCmd = newCmd.replaceAll("WATER_DISP", com.getCommand());
    }
    if (newCmd.indexOf("SIDECHAIN_STYLE") != -1) {
      if (bboneStyle_.equals(BBONE_SPACEFILL)) {
        newCmd = newCmd.replaceAll("SIDECHAIN_STYLE", "spacefill 100%");
      } else if (bboneStyle_.equals(BBONE_BALSTICK)) {
        newCmd = newCmd.replaceAll("SIDECHAIN_STYLE", "spacefill BALL_THICK;wireframe WIREFRAME_THICK");
      } else {
        newCmd = newCmd.replaceAll("SIDECHAIN_STYLE", "wireframe WIREFRAME_THICK");
      }
    }
    if (newCmd.indexOf("SSBONDS_DISP") != -1) {
      newCmd = newCmd.replaceAll("SSBONDS_DISP", SSBONDS_CMD);
    }
    if (chainCode_ != null)
      newCmd = newCmd.replaceAll("CHAIN_ID", ":" + chainCode_);
    newCmd = newCmd.replaceAll("BALL_THICK", ballThick_);
    newCmd = newCmd.replaceAll("WIREFRAME_THICK", wireFrameThick_);
    // System.out.println("after: "+newCmd);
    return newCmd;
  }

  private class DDMolPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent event) {
      String propName;

      propName = event.getPropertyName();
      if (PROP_SIDECHAIN.equals(propName)) {
        sideChainsOn_ = Boolean.TRUE.equals(event.getNewValue());
      } else if (PROP_WATER.equals(propName)) {
        waterOn_ = Boolean.TRUE.equals(event.getNewValue());
      } else if (PROP_BBONE_STYLE.equals(propName)) {
        bboneStyle_ = event.getNewValue().toString();
      }
    }
  }

  /**
   * Action used to apply a particular protein view style.
   */
  private class DDMolActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      JComboBox<?> box = (JComboBox<?>) event.getSource();
      Command command = (Command) box.getSelectedItem();
      if (structure_)
        command.execute();
      String key = command.getKey();
      if (key.startsWith("b")) {
        currentBBoneStyle_ = command;
      }
    }
  }

  /**
   * Action used to change the structure viewer background colour.
   */
  private class BKColorActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      JComboBox<?> box = (JComboBox<?>) event.getSource();
      ComboColor clr = (ComboColor) box.getSelectedItem();
      executeCmd("background " + clr.getJMolColor() + ";");
    }
  }

  /**
   * Execute a particular command.
   */
  private class DDMolSimpleScriptActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      Command com;

      com = commands_.get(event.getActionCommand());
      com.execute();
    }
  }

  /**
   * This action is used to activate or not boolean based commands.
   */
  private class DDMolOnOffScriptActionListener implements ActionListener {
    private String _onCommand;
    private String _offCommand;

    public DDMolOnOffScriptActionListener(String onCmd, String offCmd) {
      _onCommand = onCmd;
      _offCommand = offCmd;
    }

    public void actionPerformed(ActionEvent event) {
      JCheckBox check = (JCheckBox) event.getSource();
      Command com;

      if (check.isSelected())
        com = commands_.get(_onCommand);
      else
        com = commands_.get(_offCommand);
      com.execute();
    }
  }

  /**
   * Prepare the styles to use when selecting residues.
   */
  private String prepareStyles(String color, String cmd, int[] selResidues) {
    StringBuffer buf = new StringBuffer("");
    String pos, formattedCom;

    for (int i = 0; i < selResidues.length; i++) {
      buf.append(selResidues[i]);
      if (chainCode_ != null) {
        buf.append(":");
        buf.append(chainCode_);
      }
      if ((i + 1) < selResidues.length) {
        buf.append(",");
      }
    }
    pos = buf.toString();
    formattedCom = cmd.replaceAll("USER_SELECTION", pos);
    formattedCom = formattedCom.replaceAll("USER_COLOR", color);
    return formattedCom;
  }

  private int[] applySelection() {
    IntervalBuilder iBuilder;
    Command com;
    ComboColor clr;
    int[] values;

    clr = structColorChooser_.getSelectedColor();
    iBuilder = new IntervalBuilder();
    values = iBuilder.interpret(posSelector_.getText());
    if (values == null) {
      System.err.println(iBuilder.getErrorMessage());
      return null;
    }
    com = commands_.get(selSideChainCheck_.isSelected() ? "g1" : "g2");
    executeCmd(prepareStyles(clr.getJMolColor(), com.getCommand(), values));
    return values;
  }

  private List<DLocation> getLocation(DSequence seq, int[] values) {
    ArrayList<DLocation> locs;
    DRulerModel rModel;
    int idx;

    locs = new ArrayList<DLocation>();
    rModel = seq.getRulerModel();
    if (rModel == null) {
      locs.add(new DLocation(-1, -1));
    } else {
      for (int i = 0; i < values.length; i++) {
        idx = rModel.getRulerPos(values[i]);
        locs.add(new DLocation(idx, idx));
      }
    }
    return locs;
  }

  /**
   * Apply the graphic properties on a particular selection of residues.
   */
  private class DDMolApplyGraphicsActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      int[] values;
      PdbSequence seq;

      values = applySelection();
      if (values == null)
        return;
      seq = seqViewer_.getDisplayedSequence();
      if (_lSupport != null && seq != null) {
        _lSupport.setSelectionRanges(JMolCommander.this, seq.getSequence(), getLocation(seq.getSequence(), values));
      }
    }
  }

  /**
   * This class is used to store the Jmol scripting command used by the DDMol
   * viewer.
   */
  private class Command {
    String _key;
    String _name;
    String _command;
    String _property;
    Object _value;

    public Command(String key, String name, String command, String property, Object value) {
      super();
      this._name = name;
      this._key = key;
      this._command = command;
      this._property = property;
      this._value = value;
    }

    public String getCommand() {
      return _command;
    }

    public String getKey() {
      return _key;
    }

    public String getName() {
      return _name;
    }

    public void execute() {
      if (_property != null) {
        JMolCommander.this.firePropertyChange(_property, null, _value);
      }
      executeCmd(_command);
    }

    public String toString() {
      return _name;
    }
  }

  /**
   * The colour combobox.
   */
  @SuppressWarnings("serial")
  private class JColorComboBox extends JComboBox<Object> {
    public JColorComboBox() {
      super();
      this.setFont(CTRL_FNT);
      this.setRenderer(new ColorCellRenderer(CTRL_FNT));
    }

    public JColorComboBox(ComboColor[] table) {
      this();
      for (int i = 0; i < table.length; i++) {
        this.addItem(table[i]);
      }
    }

    public ComboColor getSelectedColor() {
      return (ComboColor) this.getSelectedItem();
    }
  }

  /**
   * The cell renderer used for the coulour combobox.
   */
  @SuppressWarnings("serial")
  private class ColorCellRenderer extends JPanel implements ListCellRenderer<Object> {
    private ComboColor clr_;

    public ColorCellRenderer(Font fnt) {
      this.setOpaque(true);
      this.setFont(fnt);
    }

    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      clr_ = (ComboColor) value;

      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      return this;
    }

    public Dimension getPreferredSize() {
      FontMetrics fm = this.getFontMetrics(this.getFont());
      return new Dimension(fm.stringWidth(clr_.getName()), fm.getHeight());
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);

      String name;

      Rectangle cell = this.getBounds();
      FontMetrics fm = this.getFontMetrics(this.getFont());
      name = clr_.getName();
      if (name.equals(CPK_LBL) || name.equals(AMINO_LBL)) {
        g.drawString(name, 4, (cell.height + fm.getHeight()) / 2 - 2);
      } else {
        cell.x = cell.y = 2;
        cell.height -= 4;
        cell.width -= 6;

        g.setColor(clr_.getClr());
        g.fillRect(cell.x, cell.y, cell.width, cell.height);
      }
    }
  }

  /**
   * An entry for the colour combox.
   */
  private static class ComboColor {
    private String _name;
    private Color _clr;
    private String _jmClr;

    public ComboColor(String name, Color clr) {
      super();
      setName(name);
      setClr(clr);
    }

    public Color getClr() {
      return _clr;
    }

    public void setClr(Color clr) {
      StringBuffer buf;

      this._clr = clr;
      buf = new StringBuffer();
      if (_name.equals(CPK_LBL) || _name.equals(AMINO_LBL)) {
        buf.append(_name);
      } else {
        Color c = getClr();
        buf.append("[");
        buf.append(c.getRed());
        buf.append(",");
        buf.append(c.getGreen());
        buf.append(",");
        buf.append(c.getBlue());
        buf.append("]");
      }
      _jmClr = buf.toString();
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      this._name = name;
    }

    public String getJMolColor() {
      return _jmClr;
    }

    public String toString() {
      return _name;
    }
  }

  /**
   * This class is a sequence viewer selection listener. It is used to answer
   * selection made on the sequence and transfer that selection to the structure
   * viewer.
   */
  private class SeqSelectionListener implements DSequenceSelectionListener {
    public void selectionChanged(DSequenceSelectionEvent event) {
      int seqFrom = event.getSelFrom();// abs values
      int seqTo = event.getSelTo();
      DSequence dseq = event.getEntireSequence();
      DRulerModel rModel = dseq.getRulerModel();

      if (event.getSource() == JMolCommander.this) {
        return;
      }

      if (rModel != null) {
        seqFrom = rModel.getSeqPos(seqFrom);
        seqTo = rModel.getSeqPos(seqTo);
      } else {
        seqFrom = seqTo = -1;
      }
      if (seqFrom == -1 || seqTo == -1) {
        posSelector_.setText("");
        if (currentBBoneStyle_ != null) {
          currentBBoneStyle_.execute();
        }
      } else {
        if (seqFrom != seqTo)
          posSelector_.setText(seqFrom + "-" + seqTo);
        else
          posSelector_.setText(String.valueOf(seqFrom));
        if (currentBBoneStyle_ != null) {
          currentBBoneStyle_.execute();
        }
        applySelection();
      }
    }
  }

}
