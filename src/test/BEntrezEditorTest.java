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
package test;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.service.ncbi.EntrezBank;
import bzh.plealog.bioinfo.docviewer.service.ncbi.io.EntrezQueryEngine;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.EntrezQueryFactory;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.query.EntrezQueryModel;
import bzh.plealog.bioinfo.docviewer.ui.panels.DocNavigator;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.filter.implem.BRuleFactoryImplem;
import bzh.plealog.bioinfo.filter.implem.FilterSerializerImplem;
import bzh.plealog.bioinfo.io.filter.FilterSerializerException;
import bzh.plealog.bioinfo.ui.filter.BFilterEditor;
import bzh.plealog.bioinfo.ui.filter.BFilterEditorDialog;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.bioinfo.ui.modules.filter.FilterSystemUI;

@SuppressWarnings("serial")
public class BEntrezEditorTest extends JFrame {

  private static class MainWindowAdapter extends WindowAdapter{
    private BFilterEditor filter_;

    public MainWindowAdapter(BFilterEditor filter){
      filter_ = filter;
    }

    public void windowClosing(WindowEvent e){
      BFilter bf;

      if (filter_!=null){
        try {
          bf = filter_.getFilter();
          new FilterSerializerImplem().save(bf, new File("entrez.xml"));
        } catch (BFilterException e1) {
          System.err.println("Error: "+e1);
        } catch (FilterSerializerException e1) {
          System.err.println("Error: "+e1);
        }
      }
      System.exit(0);
    }
  }
  private static class FrameShower implements Runnable {
    final JFrame frame;
    public FrameShower(JFrame frame) {
      this.frame = frame;
    }
    public void run() {
      frame.setVisible(true);
    }
  }
  /**
   * @param args
   */
  public static void main(String[] args) {
    BEntrezEditorTest    frame;
    BFilter              filt;
    BFilterEditorDialog  filterDialog;

    // this is required to enable application finding resources (messages, images, etc)
    EZEnvironment.addResourceLocator(Messages.class);

    // Initialize Plealog Bioinformatics libraries
    CoreSystemConfigurator.initializeSystem();   // Core Data Model
    FilterSystemUI.initializeSystem();
    EZEnvironment.addResourceLocator(FilterMessages.class);
    
    // It is worth noting that this NCBI Entrez Query system relies on the 
    // BLAST Result Filtering system... However, we cannot use the official API
    // through calls to FilterSystemConfigurator.initializeSystem() since we 
    // are not going to rely on the SROutput data model (aka BLAST results).
    
    // now, we directly call the editor
    System.out.println("Calling editor...");
    filterDialog = new BFilterEditorDialog(
        (Frame)null, //no main Frame 
        "Filter Editor", // a simple title for the editor
        new EntrezQueryModel(), //this is the data model 
        (BFilter) null, //we do not start with an existing filter
        new EntrezQueryFactory(), //the factory used to created new Filters 
        new BRuleFactoryImplem(), // the factory used to create filter's rules 
        true); // only shows rules editor

    // get the filter from editor; null if dialogue box cancelled
    filt = filterDialog.getFilter();
    if (filt==null){
      System.out.println("no filter");
      System.exit(0);
    }
    // convert the Filter internal data model into an NCBI Entrez query
    filt.compile();
    System.out.println("Filter: "+filt.getTxtString());
    
    //now, we query NCBI Entrez; here, we target the "protein" databank
    EntrezQueryEngine engine = new EntrezQueryEngine(EntrezBank.PROTEIN, filt);
    //the query engine is only used to retrieve Document Summaries, if any
    Summary res = engine.getSummary(0, 100);
    
    if (res==null){
      System.out.println("no result");
      System.exit(0);
    }
    
    // ok, we hare results: query NCBI to get full documents
    System.out.println("Found: "+res.getTotal()+" document(s)");
    
    DocNavigator navigator = new DocNavigator(engine);
    navigator.setData(res);

    // finally, setup and start the Document navigator
    frame = new BEntrezEditorTest();
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new MainWindowAdapter(null));

    frame.getContentPane().add(navigator);
    frame.pack();
    frame.setSize(new Dimension(640, 480));
    Runnable runner = new FrameShower(frame);
    EventQueue.invokeLater(runner);
  }
}
