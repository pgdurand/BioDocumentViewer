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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.ContextMenuElement;
import com.plealog.genericapp.ui.common.ContextMenuManager;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.http.HTTPEngineException;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.ui.util.BasicSelectTableAction;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;

/**
 * This class can be used to display results contained in Summary objects. In
 * addition, the class is capable of handling queries to a remote server to
 * complete the results of a particular query. Indeed, each query to a server
 * may return a huge number of results, but a single Summary object only
 * contains a page of n results. Please, have a look at the method
 * setData(Summary).
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class DocNavigator extends JPanel {
  private static final long serialVersionUID = 1291759858887061466L;
  private DocSummaryTable _table;
  private JScrollPane _scroller;
  private QueryEngine _engine;
  private AntiAliasLabel _queryText;
  private JLabel _totDocs;
  private JButton _prevBtn;
  private JButton _nextBtn;
  // This component aims at displaying results from querying a remote databank
  // system (NCBI, EBI, ...). Such a result may be composed from several pages. 
  // This is the page currently displayed.
  private int _curPage;
  // And this is the page size used to query the remote server.
  private int _pageSize;
  // To avoid a query the remote server each time the user ask for a new page of
  // result, this table is used to store previously retrieved pages. Each page
  // is actually a Summary object. Key is a page number.
  private Hashtable<String, Summary> _docPages;
  private Action _doubleClickAct;
  private ImageIcon _animIcon;
  private ImageIcon _notAnimIcon;
  private JLabel _animLbl;
  private JTextField _curPageField;
  private JLabel _totPageLbl;
  private ContextMenuManager _contextMnu;

  /**
   * Constructor.
   * 
   * @param engine
   *          the engine that was used to create the passed in result. That
   *          engine can also be used in this component to navigate through more
   *          data.
   */
  public DocNavigator(QueryEngine engine) {
    TableColumnManager tcm;
    DocSummaryTableModel tModel;
    JPanel header, navigator;
    JTabbedPane jtp;

    _engine = engine;

    _docPages = new Hashtable<String, Summary>();

    tModel = new DocSummaryTableModel(engine.getBankType().getPresentationModel());
    _table = new DocSummaryTable(tModel);
    _table.addMouseListener(new TableMouseListener());
    _table.setAutoCreateRowSorter(true);
    
    _scroller = new JScrollPane(_table);
    _scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    tcm = new TableColumnManager(_table, tModel.getReferenceColHeaders());
    _scroller.setCorner(JScrollPane.UPPER_RIGHT_CORNER, tcm.getInvoker());

    header = new JPanel(new BorderLayout());
    header.add(getTotalPanel(), BorderLayout.WEST);
    header.add(getNavigatorPanel(), BorderLayout.EAST);

    navigator = new JPanel(new BorderLayout());
    navigator.add(header, BorderLayout.NORTH);
    navigator.add(_scroller, BorderLayout.CENTER);

    _totPageLbl.setFont(_curPageField.getFont());
    _totDocs.setFont(_curPageField.getFont());
    jtp = new JTabbedPane();
    jtp.setFocusable(false);
    jtp.add("Navigator", navigator);
    jtp.add("Query", getQueryPanel());

    this.setLayout(new BorderLayout());
    this.add(jtp, BorderLayout.CENTER);
    this.addComponentListener(new TableComponentAdapter());
  }

  private JPanel getTotalPanel() {
    JPanel pnl = new JPanel();

    pnl.add(new JLabel("Total number of available documents: "));
    _totDocs = new JLabel("");
    pnl.add(_totDocs);
    return pnl;
  }

  private JPanel getQueryPanel() {
    JPanel pnl1, pnl2;

    pnl1 = new JPanel(new BorderLayout());
    _queryText = new AntiAliasLabel();
    pnl1.add(_queryText, BorderLayout.WEST);
    pnl2 = new JPanel(new BorderLayout());
    pnl2.add(pnl1, BorderLayout.NORTH);
    pnl2.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));
    return pnl2;
  }

  private JPanel getNavigatorPanel() {
    NavigatorBtnActListener act = new NavigatorBtnActListener();
    DefaultFormBuilder builder;
    FormLayout layout;
    JPanel pnl = new JPanel();

    _prevBtn = new JButton("<");
    _prevBtn.addActionListener(act);
    _nextBtn = new JButton(">");
    _nextBtn.addActionListener(act);
    _prevBtn.setEnabled(false);
    _nextBtn.setEnabled(false);
    _animIcon = EZEnvironment.getImageIcon("circle_all.gif");
    _notAnimIcon = EZEnvironment.getImageIcon("circle_back.gif");

    layout = new FormLayout("30dlu, 2dlu, 30dlu", "");
    builder = new DefaultFormBuilder(layout);
    _animLbl = new JLabel(_notAnimIcon);
    _curPageField = new JTextField();
    _curPageField.addKeyListener(new MyKeyListener());
    _curPageField.setEnabled(false);
    _totPageLbl = new JLabel("/");
    builder.append(_curPageField, _totPageLbl);
    pnl.add(_animLbl);
    pnl.add(_prevBtn);
    pnl.add(builder.getContainer());
    pnl.add(_nextBtn);
    return pnl;
  }

  protected int[] getCurrentColumnSize(TableColumnModel tcm) {
    int[] cols;
    int i, size;

    size = tcm.getColumnCount();
    if (size == 0)
      return null;
    cols = new int[size];
    for (i = 0; i < size; i++) {
      cols[i] = tcm.getColumn(i).getPreferredWidth();
    }

    return cols;
  }

  /**
   * Sets the action to invoke when a double occurs on the table.
   */
  public void setDoubleClickAction(Action act) {
    _doubleClickAct = act;
  }

  /**
   * Sets the data to display in this table. When calling this method, it is
   * important to notice that parameter engine must have been used at least once
   * to produce a first instance of parameter res.
   * 
   * @param res
   *          a first result object produced by the engine.
   * @param engine
   *          the engine that was used to create the passed in result. That
   *          engine can also be used in this component to navigate through more
   *          data.
   */
  public void setData(Summary res) {

    _queryText.setText(_engine.getQuery().getHtmlString());

    setSummaryResultData(res);
    if (res.getDocs() == null || res.getDocs().isEmpty()) {
      resetDataModel();
    } else {
      int pageNum = res.getFrom() / res.nbDocs() + 1;
      _docPages.put(String.valueOf(pageNum), res);
      _curPage = pageNum;
      _pageSize = res.nbDocs();
      _prevBtn.setEnabled(pageNum != 1);
      _nextBtn.setEnabled(!((res.getFrom() + res.nbDocs()) >= res.getTotal()));
      _curPageField.setText(String.valueOf(_curPage));
      _curPageField.setEnabled(true);
      int totPages = res.getTotal() / _pageSize;
      if (res.getTotal() % _pageSize != 0) {
        totPages++;
      }
      _totPageLbl.setText("/ " + String.valueOf(totPages));
    }
  }

  private void setSummaryResultData(Summary res) {
    ((DocSummaryTableModel) _table.getModel()).setData(res);
    sortData();
    _scroller.getVerticalScrollBar().setValue(0);

    _totDocs.setText(DecimalFormat.getInstance().format((res.getTotal())));
  }

  private void sortData(){
    int column = 0;
    SortOrder order = SortOrder.ASCENDING;
    RowSorter<?> st;
    
    // we check if we already have sorted data using particular column/order
    st = _table.getRowSorter();
    if (st!=null && st.getSortKeys().isEmpty()==false){
      column = _table.getRowSorter().getSortKeys().get(0).getColumn();
      order = _table.getRowSorter().getSortKeys().get(0).getSortOrder();
    }
    
    // do sort!
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(_table.getModel());
    _table.setRowSorter(sorter);
    List<RowSorter.SortKey> sortKeys = new ArrayList<>();
    sortKeys.add(new RowSorter.SortKey(column, order));
    sorter.setSortKeys(sortKeys);
    sorter.sort();
  }

  /**
   * Adds a listener to follow the selection made on the document table.
   */
  public void addDocSelectionListener(ListSelectionListener listener) {
    _table.getSelectionModel().addListSelectionListener(listener);
  }

  /**
   * Removes a selection listener.
   */
  public void removeDocSelectionListener(ListSelectionListener listener) {
    _table.getSelectionModel().removeListSelectionListener(listener);
  }

  /**
   * Return the default context menu associated to the table.
   */
  public ContextMenuManager getContextMenu() {
    return _contextMnu;
  }

  /**
   * returns the data table. For read-only use.
   */
  public JTable getDataTable() {
    return _table;
  }

  /**
   * Resets the content of this panel.
   */
  public void resetDataModel() {
    _table.clearSelection();
    ((DocSummaryTableModel) _table.getModel()).clear();
    _scroller.getVerticalScrollBar().setValue(0);
    _engine = null;
    _totDocs.setText("");
    _docPages.clear();
    _curPage = 0;
    _prevBtn.setEnabled(false);
    _nextBtn.setEnabled(false);
    _curPageField.setText("");
    _curPageField.setEnabled(false);
    _totPageLbl.setText("");
  }

  private class MyKeyListener extends KeyAdapter {
    public void keyReleased(KeyEvent e) {
      super.keyReleased(e);
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        try {
          int page = Integer.valueOf(_curPageField.getText());
          // same page ?
          if (page == _curPage)
            return;
          Summary res = _docPages.get(String.valueOf(_curPage));
          int totPages = res.getTotal() / _pageSize;
          if (res.getTotal() % _pageSize != 0) {
            totPages++;
          }
          if (page >= 1 && page <= totPages) {
            new QueryRunner(page).start();
          }
        } catch (Exception e1) {
        }
      }
    }
  }

  private class TableComponentAdapter extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      Component parent;

      int width;
      parent = (Component) e.getSource();
      width = parent.getBounds().width;
      _table.initColumnSize(width, null);
    }
  }

  private class NavigatorBtnActListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (event.getSource() == _prevBtn) {
        new QueryRunner(_curPage - 1).start();
      } else {
        new QueryRunner(_curPage + 1).start();
      }
    }
  }

  private class QueryRunner extends Thread {
    private int newPage;

    public QueryRunner(int newPage) {
      this.newPage = newPage;
    }

    private void displayNewPage() {
      Summary res;
      int from;

      if (newPage < 1)
        return;

      // check if we have the requested page
      res = _docPages.get(String.valueOf(newPage));
      if (res != null) {
        setData(res);
        _curPage = newPage;
        _curPageField.setText(String.valueOf(_curPage));
        return;
      }
      // get the current page, then the new page
      res = _docPages.get(String.valueOf(_curPage));
      from = (newPage - 1) * _pageSize;// res.getFrom() + (delta*nb);
      if (from >= (res.getTotal() - 1))
        return;
      if (from < 0)
        from = 0;
      try {
        res = _engine.getSummary(from, _pageSize);
        _docPages.put(String.valueOf(newPage), res);
        setData(res);
        _curPage = newPage;
        _curPageField.setText(String.valueOf(_curPage));
      } catch (QueryEngineException qee) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(
            String.format("Query is: %s. Bank is: %s", _engine.getQuery().toString(), _engine.getBankType().getCode()));
        EZLogger.warn(qee.getMessage());
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), qee.getMessage(),
            Messages.getString("DatabaseOpener.err2"), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      } catch (HTTPEngineException hbe) {
        EZEnvironment.setDefaultCursor();
        EZLogger.warn(
            String.format("Query is: %s. Bank is: %s", _engine.getQuery().toString(), _engine.getBankType().getCode()));
        EZLogger.warn(hbe.getUrl());
        EZLogger.warn(String.format("[%d] %s", hbe.getHttpCode(), hbe.getMessage()));
        JOptionPane.showMessageDialog(EZEnvironment.getParentFrame(), hbe.getMessage(),
            Messages.getString("DatabaseOpener.err2"), JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION);
      }
    }

    private void handleBtn() {
      _prevBtn.setEnabled(_curPage != 1);
      Summary res = _docPages.get(String.valueOf(_curPage));
      _nextBtn.setEnabled(!((res.getFrom() + res.getDocs().size()) >= res.getTotal()));
    }

    public void run() {
      _animLbl.setIcon(_animIcon);
      _prevBtn.setEnabled(false);
      _nextBtn.setEnabled(false);
      _curPageField.setEnabled(false);
      displayNewPage();
      handleBtn();
      _animLbl.setIcon(_notAnimIcon);
      _curPageField.setEnabled(true);
    }
  }

  private class TableMouseListener extends MouseAdapter {

    public TableMouseListener() {
      BasicSelectTableAction act;

      ArrayList<ContextMenuElement> actions;
      actions = new ArrayList<ContextMenuElement>();
      act = new BasicSelectTableAction("Select all", BasicSelectTableAction.SelectType.ALL);
      act.setTable(_table);
      actions.add(new ContextMenuElement(act));
      act = new BasicSelectTableAction("Clear selection", BasicSelectTableAction.SelectType.CLEAR);
      act.setTable(_table);
      actions.add(new ContextMenuElement(act));
      act = new BasicSelectTableAction("Invert selection", BasicSelectTableAction.SelectType.INVERT);
      act.setTable(_table);
      actions.add(new ContextMenuElement(act));
      _contextMnu = new ContextMenuManager(_table, actions);
    }

    public void mouseReleased(MouseEvent e) {
      if (SwingUtilities.isRightMouseButton(e) && _contextMnu != null) {
        _contextMnu.showContextMenu(e.getX(), e.getY());
      } else if (e.getClickCount() == 2 && _doubleClickAct != null) {
        _doubleClickAct.actionPerformed(new ActionEvent(_table, ActionEvent.ACTION_PERFORMED, null));
      }
    }
  }

  @SuppressWarnings("serial")
  private class AntiAliasLabel extends JLabel {
    public void paintComponent(Graphics g) {
      Object o = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      super.paintComponent(g);
      // restore graphics
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, o);
    }
  }
}
