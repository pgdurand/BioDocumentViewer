/* Copyright (C) 2006-2020 Patrick G. Durand
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
package bzh.plealog.bioinfo.docviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.log4j.BasicConfigurator;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.EZGenericApplication;
import com.plealog.genericapp.api.EZUIStarterListener;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.api.log.EZLoggerManager;
import com.plealog.genericapp.ui.desktop.CascadingWindowPositioner;
import com.plealog.genericapp.ui.desktop.GDesktopPane;
import com.plealog.genericapp.ui.desktop.GInternalFrame;
import com.plealog.genericapp.ui.desktop.JWindowsMenu;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.docviewer.api.BankProvider;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.conf.DirManager;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.actions.OpenFileAction;
import bzh.plealog.bioinfo.docviewer.ui.actions.OpenSampleFileAction;
import bzh.plealog.bioinfo.docviewer.ui.panels.DatabaseOpener;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.config.UISystemConfigurator;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.bioinfo.ui.util.MemoryMeter;

/**
 * Starter class of the DocumentViewer.
 * 
 * @author Patrick G. Durand
 * @since 2006
 */
public class DocumentViewer {
  /**
   * Start a UI-based application. Relies on the Java Generic Application
   * Framework. See https://github.com/pgdurand/jGAF
   */
  public static void main(String[] args) {
    // This has to be done at the very beginning, i.e. first method call within
    // main().
    EZGenericApplication.initialize("DocumentViewer");

    // Add application branding
    Properties props = DocViewerConfig.getVersionProperties();
    EZApplicationBranding.setAppName(props.getProperty("prg.app.name"));
    EZApplicationBranding.setAppVersion(props.getProperty("prg.version"));
    EZApplicationBranding.setCopyRight(props.getProperty("prg.copyright"));
    EZApplicationBranding.setProviderName(props.getProperty("prg.provider"));

    // We tell JRE where to locate resources... (always use a class located in
    // the same package as the resources)
    EZEnvironment.addResourceLocator(Messages.class);

    // setup the logger framework
    // turn off Java Logging standard console log
    EZLoggerManager.enableConsoleLogger(false);
    // turn on UI logger; text size limit is set to 2 million characters (when
    // content of UI logger reaches that limit, then UI text component simply
    // reset its content).
    EZLoggerManager.enableUILogger(true, 2);
    // delegate LogLevel to software config since JVM argument can be used
    // to modify standard behavior (see DocViewerConfig.JVM_ARG_DEBUG)
    DocViewerConfig.initLogLevel();
    // setup the logging system
    EZLoggerManager.initialize();

    // some third party libraries rely on log4j
    BasicConfigurator.configure();

    // Required to use Plealog Bioinformatics Core objects such as Features,
    // FeatureTables, Sequences
    CoreSystemConfigurator.initializeSystem();

    // Required to use the Plealog Bioinformatics UI library (e.g. CartoViewer
    // default graphics)
    UISystemConfigurator.initializeSystem();

    // we setup the Directory Manager
    ConfigManager.addConfig(new DirManager());
    
    /*
     * notice: FilterSystemConfigurator provides BFilter factories. However,
     * since this DocumentViewer software provides several different filtering
     * systems, we avoid using the central FilterSystem factory frameworks.
     * These two function calls are maintained commented here as a reminder: do
     * not call them to setup the standard BFilter system. (More here:
     * bzh.plealog.bioinfo.docviewer.api.QueryModel)
     */
    // FilterSystemConfigurator.initializeSystem();
    /* Comment out the following only if using the FilterSystem storage */
    // FilterSystemUI.initializeSystem();

    // this is to locate app resources (images, messages, etc)
    EZEnvironment.addResourceLocator(FilterMessages.class);

    // Add a listener to application startup cycle (see below)
    EZEnvironment.setUIStarterListener(new MyStarterListener());

    // Start the application
    EZGenericApplication.startApplication(args);
  }

  private static class MyStarterListener implements EZUIStarterListener {
    private GDesktopPane _desktop   = new GDesktopPane();
    private Component    _mainCompo = null;
    private JPanel       _btnPanel;

    private JToolBar getToolbar() {
      JToolBar tBar;
      ImageIcon icon;
      Action act;
      JButton btn;

      tBar = new JToolBar();
      tBar.setFloatable(false);

      icon = EZEnvironment.getImageIcon("openTest.png");
      if (icon != null) {
        act = new OpenSampleFileAction("", icon);
      } else {
        act = new OpenSampleFileAction(Messages.getString("OpenBlastList.test.name"));
      }
      act.setEnabled(true);
      btn = tBar.add(act);
      btn.setToolTipText(Messages.getString("OpenBlastList.test.tip"));
      btn.setText(Messages.getString("OpenBlastList.test.name"));
      btn.setHorizontalTextPosition(SwingConstants.RIGHT);
      
      tBar.addSeparator();
      
      icon = EZEnvironment.getImageIcon("open.png");
      if (icon != null) {
        act = new OpenFileAction("", icon);
      } else {
        act = new OpenFileAction(Messages.getString("OpenFileList.open.name"));
      }
      act.setEnabled(true);
      btn = tBar.add(act);
      btn.setToolTipText(Messages.getString("OpenFileList.open.tip"));
      btn.setText(Messages.getString("OpenFileList.open.name"));
      btn.setHorizontalTextPosition(SwingConstants.RIGHT);
      /*
      icon = EZEnvironment.getImageIcon("download.png");
      if (icon != null) {
        act = new FetchFromNcbiAction("", icon);
      } else {
        act = new FetchFromNcbiAction(
            BVMessages.getString("OpenBlastList.openrid.name"));
      }
      act.setEnabled(true);
      btn = tBar.add(act);
      btn.setToolTipText(BVMessages.getString("OpenBlastList.openrid.tip"));
      btn.setText(BVMessages.getString("OpenBlastList.openrid.name"));
      */
      return tBar;
    }
    private Component prepareDesktop() {
      JPanel dpanel, mnuPnl;
      JButton logBtn;

      dpanel = new JPanel(new BorderLayout());
      mnuPnl = new JPanel(new BorderLayout());
      _btnPanel = new JPanel(new BorderLayout());

      JMenuBar menuBar = new JMenuBar();
      JWindowsMenu windowsMenu = new JWindowsMenu(
          Messages.getString("DocumentViewer.docs.mnu"),
          _desktop.getDesktopPane());
      windowsMenu.setWindowPositioner(new CascadingWindowPositioner(_desktop
          .getDesktopPane()));
      menuBar.add(windowsMenu);

      logBtn = new JButton(EZEnvironment.getImageIcon("logger.png"));
      //logBtn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
      logBtn.addActionListener(new ShowLoggerFrame());

      mnuPnl.add(menuBar, BorderLayout.WEST);
      mnuPnl.add(logBtn, BorderLayout.EAST);
      _btnPanel.add(mnuPnl, BorderLayout.EAST);
      _btnPanel.add(getToolbar(), BorderLayout.WEST);
      dpanel.add(_btnPanel, BorderLayout.NORTH);
      dpanel.add(_desktop, BorderLayout.CENTER);

      JPanel statusBar = new JPanel(new BorderLayout());
      JPanel hlpPnl = new JPanel(new BorderLayout());
      hlpPnl.add(DatabaseOpener.getHelperField(), BorderLayout.WEST);
      statusBar.add(new MemoryMeter(), BorderLayout.WEST);
      statusBar.add(hlpPnl, BorderLayout.CENTER);
      
      dpanel.add(statusBar, BorderLayout.SOUTH);
      
      return dpanel;
    }

    @Override
    public Component getApplicationComponent() {
      if (_mainCompo != null)
        return _mainCompo;

      // prepare the desktop viewer system
      _mainCompo = prepareDesktop();

      return _mainCompo;
    }

    @Override
    public boolean isAboutToQuit() {
      // You can add some code to figure out if application can exit.

      // Return false to prevent application from exiting (e.g. a background
      // task is still running).
      // Return true otherwise.

      // Do not add a Quit dialogue box to ask user confirmation: the framework
      // already does that for you.
      return true;
    }

    @Override
    public void postStart() {
      // This method is called by the framework just before displaying UI
      // (main frame).

      // Prepare the list of banks
      BankProvider bp = DocViewerConfig.getBankProvider();
      if (bp!=null){
        List<BankType> bts = bp.getBanks();
        // prepare the query system
        DatabaseOpener dop = new DatabaseOpener(bp.getBanks());
        dop.setDesktop(_desktop);

        GInternalFrame iFrame = new GInternalFrame(dop, bts.get(0)
            .getProviderName(), true, false, false, false);
        iFrame.setFrameIcon(DocViewerConfig.DBXPLR_ICON);
        Dimension dim = new Dimension(dop.getPreferredSize().width + 50,
            dop.getPreferredSize().height + 50);
        iFrame.setVisible(false);
        _desktop.addGInternalFrame(iFrame);
        // set size and pos have to be set after adding the frame in the desktop
        iFrame.setSize(dim);
        iFrame.setBounds(1, 1, dim.width, dim.height);
        iFrame.setVisible(true);
        iFrame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
      }
      else{
        EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), "Unable to find a BankProvider.");
      }

      DocViewerConfig.dumpApplicationProperties();

      EZLogger.info(String.format("%s - %s",
          EZApplicationBranding.getAppName(),
          EZApplicationBranding.getAppVersion()));
      EZLogger.info(EZApplicationBranding.getCopyRight());
    }

    @Override
    public void preStart() {
      // This method is called by the framework at the very beginning of
      // application startup.
    }

    @Override
    public void frameDisplayed() {
      
      
    }
  }

  /**
   * Utility class to show the UI Logger component.
   */
  private static class ShowLoggerFrame implements ActionListener {
    private JFrame frame;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (frame == null) {
        makeFrame();
      }
      frame.setVisible(!frame.isVisible());
    }

    private void makeFrame() {
      int delta = 50;
      frame = new JFrame(Messages.getString("DocumentViewer.docs.tab2"));
      frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      Rectangle rect = EZEnvironment.getParentFrame().getBounds();
      rect.x += delta;
      rect.y += delta;
      rect.width -= 2 * delta;
      rect.height -= 2 * delta;
      frame.getContentPane().add(EZLoggerManager.getUILogger());
      frame.setBounds(rect);
    }
  }
}
