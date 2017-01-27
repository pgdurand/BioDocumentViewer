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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.desktop.GDesktopPane;
import com.plealog.genericapp.ui.desktop.GInternalFrame;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.http.HTTPEngineException;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.web.SwingFXWebViewer;
import bzh.plealog.bioinfo.ui.filter.BFilterEditorDialog;

/**
 * This panel is the entry point of the database explorer component. It allows a
 * user to choose a data provider, a database and start a search.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class DatabaseOpener extends JPanel {
  private static final long serialVersionUID = 4699430095111364352L;
  private static GDesktopPane _desktop;
  private static JLabel _helperField;
  private static JLabel _serviceField;
  private JComboBox<BankType> _dbList;
  private JButton _startEditor;

  private static Color RUNNING_TASK_COLOR = Color.GREEN.darker();
  private static Color NOT_RUNNING_TASK_COLOR;

  // use to be sure that at any time only one fetching process is authorized
  // using such a static variable is not so good, so for the future: enhance!
  protected static boolean _fetchInProgress;

  private static final MessageFormat RES_HEADER = new MessageFormat(Messages.getString("DatabaseOpener.lbl5"));
  private static final MessageFormat RES_HEADER2 = new MessageFormat(Messages.getString("DatabaseOpener.lbl6"));

  /**
   * Constructor.
   */
  public DatabaseOpener(List<BankType> banks) {
    JPanel mainPanel = new JPanel(new BorderLayout());

    _helperField = new JLabel();
    _helperField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    _helperField.setOpaque(true);
    _helperField.setFocusable(false);

    _serviceField = new JLabel();
    _serviceField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    _serviceField.setOpaque(true);
    _serviceField.setFocusable(false);

    NOT_RUNNING_TASK_COLOR = _helperField.getBackground();

    mainPanel.add(createPanelID(banks), BorderLayout.CENTER);

    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.NORTH);

    // the following is used to check whether or not remote server is available
    // and provide that information on the UI
    Timer timer = new Timer(2000, new ServiceTimer(banks.get(0)));
    timer.setRepeats(false);
    timer.start();
  }

  private Component createPanelID(List<BankType> banks) {
    DefaultFormBuilder builder;
    FormLayout layout;

    _dbList = new JComboBox<>();

    for (BankType bt : banks) {
      _dbList.addItem(bt);
    }

    _dbList.addActionListener(new DBListener());
    _startEditor = new JButton(Messages.getString("DatabaseOpener.lbl2"));
    _startEditor.addActionListener(new StartEditorAction());
    _startEditor.setToolTipText(Messages.getString("DatabaseOpener.lbl3"));
    layout = new FormLayout("120dlu, 4dlu, 30dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.appendSeparator(Messages.getString("DatabaseOpener.lbl1"));
    builder.setDefaultDialogBorder();
    builder.append(_dbList, _startEditor);
    builder.appendSeparator();
    builder.append(_serviceField);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(builder.getContainer(), BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
    return panel;
  }

  public void setDesktop(GDesktopPane desktop) {
    _desktop = desktop;
  }

  public JComponent getHelperField() {
    return _helperField;
  }

  public static void displayInternalFrame(JComponent viewer, String title, ImageIcon icon,
      final XplorDocNavigator navigator) {
    int delta = 20;

    GInternalFrame iFrame = new GInternalFrame(viewer, // the viewer
        title, // iFrame title will be the entry ID
        true, true, true, // resizable, closable, maximizable: allowed
        false);// does not allow iconifiable: not working with JRE1.7+ on OSX !
               // Known bug.
    if (icon != null)
      iFrame.setFrameIcon(icon);
    Dimension dim = _desktop.getSize();
    iFrame.setVisible(false);
    _desktop.addGInternalFrame(iFrame);
    iFrame.setSize(dim);
    iFrame.setBounds(delta, delta, dim.width - 2 * delta, dim.height - 2 * delta);
    iFrame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    iFrame.addInternalFrameListener(new IFrameListener(navigator));
    iFrame.setVisible(true);
  }

  public static void displayInternalFrame(JComponent viewer, String title, ImageIcon icon) {
    displayInternalFrame(viewer, title, icon, null);
  }

  public static void displayWebDocument(String htmlFile, String title, ImageIcon icon) {
    SwingFXWebViewer webViewer = new SwingFXWebViewer(null, true);
    DatabaseOpener.displayInternalFrame(webViewer, title, icon);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        webViewer.load("file://" + htmlFile);
      }
    });
  }

  /**
   * Returns true if a sequence fetching process is already running in the
   * system.
   */
  public static synchronized boolean isFetchingProcessRunning() {
    return _fetchInProgress;
  }

  /**
   * Sets the running status of the sequence fetching system.
   */
  public static synchronized void setFetchingProcessRunning(boolean running) {
    _fetchInProgress = running;
  }

  /**
   * Set a message during some operation.
   */
  public static void setHelperMessage(String msg) {
    if (msg == null) {
      cleanHelperMessage();
    }
    _helperField.setText(msg);
    _helperField.setIcon(DocViewerConfig.WORKING_ICON);
    _helperField.setBackground(RUNNING_TASK_COLOR);
  }

  public static void cleanHelperMessage() {
    _helperField.setText("");
    _helperField.setIcon(null);
    _helperField.setBackground(NOT_RUNNING_TASK_COLOR);
  }

  private void showEditor(BankType db, BFilter query) {
    BFilterEditorDialog filterDialog;
    BFilter filt;

    filterDialog = new BFilterEditorDialog(EZEnvironment.getParentFrame(),
        RES_HEADER2.format(new Object[] { db.getUserName() }), db.getQueryModel(), query,
        db.getQueryModel().getFilterFactory(), db.getQueryModel().getRuleFactory(), true);
    filt = filterDialog.getFilter();
    if (filt != null) {
      new FirstConnectionTask(filt, db).start();
    }
  }

  /**
   * Actions used to display the query editor.
   */
  private class StartEditorAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      showEditor((BankType) _dbList.getSelectedItem(), null);
    }
  }

  /**
   * Task used to start the query against the data provider server.
   */
  private class FirstConnectionTask extends Thread {
    private BFilter _query;
    private BankType _db;

    public FirstConnectionTask(BFilter q, BankType db) {
      _query = q;
      _db = db;
    }

    public void run() {
      boolean bError = true;
      try {
        _query.compile();

        setHelperMessage(Messages.getString("DatabaseOpener.lbl7"));
        EZEnvironment.setWaitCursor();
        QueryEngine engine = _db.prepareQueryEngine(_query);
        Summary res;
        if (engine.enablePagination()) {
          res = engine.getSummary(0, engine.getPageSize());
        } else {
          res = engine.getSummary();
        }
        if (res.nbDocs() == 0) {
          EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), Messages.getString("DatabaseOpener.lbl8"));
        } else {
          XplorDocNavigator navigator = new XplorDocNavigator(engine);
          navigator.setData(res);
          // displayFrame(_db.getUserName(), navigator);
          displayInternalFrame(navigator, RES_HEADER.format(new Object[] { _db.getUserName() }),
              DocViewerConfig.DBXPLR_ICON, navigator);
          bError = false;
        }
      } catch (QueryEngineException qee) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(String.format("Query is: %s. Bank is: %s", _query.toString(), _db.getCode()));
        EZLogger.warn(qee.getMessage());
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), qee.getMessage(),
            Messages.getString("DatabaseOpener.err2"), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      } catch (HTTPEngineException hbe) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(String.format("Query is: %s. Bank is: %s", _query.toString(), _db.getCode()));
        EZLogger.warn(hbe.getUrl());
        EZLogger.warn(String.format("[%d] %s", hbe.getHttpCode(), hbe.getMessage()));
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), hbe.getMessage(),
            Messages.getString("DatabaseOpener.err2"), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      } catch (BFilterException bfe) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(bfe.getMessage());
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), bfe.getMessage(),
            Messages.getString("DatabaseOpener.err1"), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      } finally {
        EZEnvironment.setDefaultCursor();
        cleanHelperMessage();
      }
      // if error, display back the editor with the query
      if (bError) {
        showEditor(_db, _query);
      }
    }
  }

  private static class IFrameListener implements InternalFrameListener {
    XplorDocNavigator navigator;

    public IFrameListener(XplorDocNavigator navigator) {
      this.navigator = navigator;
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
      if (EZEnvironment.confirmMessage(EZEnvironment.getParentFrame(), Messages.getString("DatabaseOpener.msg1"))) {
        if (navigator != null)
          navigator.cancelJob();
        e.getInternalFrame().dispose();
      }
    }

    public void internalFrameClosed(InternalFrameEvent e) {
      if (navigator != null)
        navigator.cancelJob();
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }
  }

  private class ServiceTimer implements ActionListener {
    private BankType bt;

    private ServiceTimer(BankType bt) {
      this.bt = bt;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (bt.getServerConfiguration().isServerAvailable()) {
        String str = String.format("<html><span style='color:green;'>%s is online</span></html>", bt.getProviderName());
        _serviceField.setText(str);
      } else {
        String str = String.format("<html><span style='color:red;'>%s is offline</span></html>", bt.getProviderName());
        _serviceField.setText(str);
      }
    }
  }

  private class DBListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      // BankType bt = (BankType)
      // ((JComboBox<?>)e.getSource()).getSelectedItem();

      // EZLogger.info("Filter path:
      // "+DocViewerConfig.getFilterStoragePath(bt));
    }

  }
}
