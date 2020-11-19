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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.http.HTTPEngineException;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
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
  
  private JLabel _serviceField;
  private JComboBox<BankType> _dbList;
  private JButton _startEditor;

  private static final MessageFormat RES_HEADER = new MessageFormat(Messages.getString("DatabaseOpener.lbl5"));
  private static final MessageFormat RES_HEADER2 = new MessageFormat(Messages.getString("DatabaseOpener.lbl6"));

  /**
   * Constructor.
   */
  public DatabaseOpener(List<BankType> banks) {
    JPanel mainPanel = new JPanel(new BorderLayout());

    _serviceField = new JLabel();
    _serviceField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    _serviceField.setOpaque(true);
    _serviceField.setFocusable(false);
    
    mainPanel.add(createPanelID(banks), BorderLayout.CENTER);

    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.NORTH);
    
    //the following is used to check whether or not remote server is available
    //and provide that information on the UI
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

  private void showEditor(BankType db, BFilter query){
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

        StatusBarHelperPanel.setHelperMessage(Messages.getString("DatabaseOpener.lbl7"));
        EZEnvironment.setWaitCursor();
        QueryEngine engine = _db.prepareQueryEngine(_query);
        Summary res = engine.getSummary(0, DocViewerConfig.PAGE_SIZE);
        if (res.nbDocs()==0) {
          EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), Messages.getString("DatabaseOpener.lbl8"));
        }
        else{
          XplorDocNavigator navigator = new XplorDocNavigator(engine);
          navigator.setData(res);
          // displayFrame(_db.getUserName(), navigator);
          StatusBarHelperPanel.displayInternalFrame(navigator, RES_HEADER.format(new Object[] { _db.getUserName() }),
              DocViewerConfig.DBXPLR_ICON, navigator);
          bError = false;
        }
      }
      catch(QueryEngineException qee){
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(String.format("Query is: %s. Bank is: %s", _query.toString(), _db.getCode()));
        EZLogger.warn(qee.getMessage());
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), qee.getMessage(),
            Messages.getString("DatabaseOpener.err2"),
            JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      }
      catch(HTTPEngineException hbe){
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(String.format("Query is: %s. Bank is: %s", _query.toString(), _db.getCode()));
        EZLogger.warn(hbe.getUrl());
        EZLogger.warn(String.format("[%d] %s", hbe.getHttpCode(), hbe.getMessage()));
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), hbe.getMessage(),
            Messages.getString("DatabaseOpener.err2"),
            JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      }
      catch (BFilterException bfe) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(bfe.getMessage());
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), bfe.getMessage(),
            Messages.getString("DatabaseOpener.err1"),
            JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      } finally {
        EZEnvironment.setDefaultCursor();
        StatusBarHelperPanel.cleanHelperMessage();
      }
      //if error, display back the editor with the query
      if(bError){
        showEditor(_db, _query);
      }
    }
  }

  
  
  private class ServiceTimer implements ActionListener{
    private BankType bt;
    
    private ServiceTimer(BankType bt){
      this.bt = bt;
      String str = String.format("<html><span style='color:orange;'>Contacting %s server...</span></html>", bt.getProviderName());
      _serviceField.setText(str);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
     Thread t = new Thread() {
       @Override
       public void run() {
         if (bt.getServerConfiguration().isServerAvailable()){
           String str = String.format("<html><span style='color:green;'>%s is online</span></html>", bt.getProviderName());
           _serviceField.setText(str);
         }
         else{
           String str = String.format("<html><span style='color:red;'>%s is offline</span></html>", bt.getProviderName());
           _serviceField.setText(str);
         }
       }
     };
     t.start();
    }
  }
  
  private class DBListener implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent e) {
      //BankType bt = (BankType) ((JComboBox<?>)e.getSource()).getSelectedItem();
      
      //EZLogger.info("Filter path: "+DocViewerConfig.getFilterStoragePath(bt));
    }
    
  }
}
