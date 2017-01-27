/* Copyright (C) 2017 Inria
 * Author: Patrick G. Durand
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
package test.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import bzh.plealog.bioinfo.docviewer.ui.web.SwingFXWebViewer;

public class SwingFXWebViewerTest extends JFrame {
  private static final long serialVersionUID = 7892569846915738554L;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        SwingFXWebViewer browser = new SwingFXWebViewer();
        browser.setVisible(true);
        browser.load("http://inria.fr");
        // browser.load("file://"+System.getProperty("user.dir")+"/data/render/index_test.html");
        SwingFXWebViewerTest frame = new SwingFXWebViewerTest();
        frame.getContentPane().add(browser);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
      }
    });
  }
}
