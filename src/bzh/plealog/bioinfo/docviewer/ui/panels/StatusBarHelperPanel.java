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
package bzh.plealog.bioinfo.docviewer.ui.panels;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.desktop.GDesktopPane;
import com.plealog.genericapp.ui.desktop.GInternalFrame;

import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

public class StatusBarHelperPanel {
  private static JLabel _helperField;
  private static GDesktopPane _desktop;

  // use to be sure that at any time only one fetching process is authorized
  // using such a static variable is not so good, so for the future: enhance!
  protected static boolean _fetchInProgress;

  private static Color RUNNING_TASK_COLOR = Color.GREEN.darker();
  private static Color NOT_RUNNING_TASK_COLOR;

  /**
   * Return the help field.
   */
  public static JComponent getHelperField() {
    if (_helperField == null) {
      _helperField = new JLabel();
      _helperField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      _helperField.setOpaque(true);
      _helperField.setFocusable(false);
      NOT_RUNNING_TASK_COLOR = _helperField.getBackground();
    }
    return _helperField;
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

  /**
   * reset the content of the help field.
   */
  public static void cleanHelperMessage() {
    _helperField.setText("");
    _helperField.setIcon(null);
    _helperField.setBackground(NOT_RUNNING_TASK_COLOR);
  }

  public static void setDesktop(GDesktopPane desktop) {
    _desktop = desktop;
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
}
