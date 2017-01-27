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

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.EZGenericApplication;
import com.plealog.genericapp.api.EZUIStarterListener;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.docviewer.ui.variation.SAXTreeUtil;
import bzh.plealog.bioinfo.ui.config.UISystemConfigurator;

/**
 * A sample application to illustrate how to create a SNP Tree Viewer.
 * 
 * @author Patrick G. Durand
 */
public class BasicSNPViewerTest {

  // We will force the Font so that we have a nice sequence viewer
  private static File _snpFile = new File("./data/ensembl_dbsnp_vep.xml");

  /**
   * Start application. Relies on the Java Generic Application Framework. See
   * https://github.com/pgdurand/jGAF
   */
  public static void main(String[] args) {
    // This has to be done at the very beginning, i.e. first method call within
    // main().
    EZGenericApplication.initialize("BasicSNPViewerTest");
    // Add application branding
    EZApplicationBranding.setAppName("Basic SNP Viewer");
    EZApplicationBranding.setAppVersion("1.0");
    EZApplicationBranding.setCopyRight("P. Durand");
    EZApplicationBranding.setProviderName("Inria - Genscale Team");

    // Add a listener to application startup cycle (see below)
    EZEnvironment.setUIStarterListener(new MyStarterListener());

    // Required to use Plealog Bioinformatics Core objects such as Features,
    // FeatureTables, Sequences
    CoreSystemConfigurator.initializeSystem();

    // Required to use the Plealog Bioinformatics UI library (CartoViewer
    // default graphics)
    UISystemConfigurator.initializeSystem();

    // this is required to enable application finding resources (messages,
    // images, etc)
    EZEnvironment.addResourceLocator(Messages.class);
    
    // Start the application
    EZGenericApplication.startApplication(args);
  }

  /**
   * Implementation of the jGAF API.
   */
  private static class MyStarterListener implements EZUIStarterListener {

    @Override
    public Component getApplicationComponent() {
      JTree tree;
      try {
        tree = new JTree(SAXTreeUtil.loadXMLDocument("Variant", _snpFile, "opt"));
      } catch (Exception ex) {
        return new JLabel("Error: "+ex.toString());
      }

      JScrollPane scrollPane = new JScrollPane(tree);
      SAXTreeUtil.setVariationTreeRenderer(tree);
      SAXTreeUtil.expandAll(tree);
      return scrollPane;
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
    }

    @Override
    public void preStart() {
      // This method is called by the framework at the very beginning of
      // application startup.
    }

  }

}
