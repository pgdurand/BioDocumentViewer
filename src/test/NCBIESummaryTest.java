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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Enumeration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.EntrezSummaryLoader;

public class NCBIESummaryTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testProtein() {
    Summary s = EntrezSummaryLoader.load(new File("./data/ncbi_docsum_prot.xml"));
    assertTrue(s.getDocs().size()==1);
    SummaryDoc sdoc = s.getDoc(0);
    assertTrue(sdoc.getValue("Accession").equals("P12265"));
    assertTrue(sdoc.getValue("CreateDate").equals("1989/10/01"));
    assertTrue(sdoc.getValue("Length").equals("648"));
    assertTrue(sdoc.getValue("UpdateDate").equals("2016/09/07"));
    assertTrue(sdoc.getValue("Description").equals("RecName: Full=Beta-glucuronidase; Flags: Precursor"));
    assertTrue(sdoc.getValue("TaxId").equals("10090"));
    assertTrue(sdoc.getValue("Identifier").equals("408359984"));
  }

  @Test
  public void testNucleotide() {
    //two documents
    Summary s = EntrezSummaryLoader.load(new File("./data/ncbi_docsum_nuc.xml"));
    assertTrue(s.getDocs().size()==2);
    SummaryDoc sdoc = s.getDoc(0);
    assertTrue(sdoc.getValue("Accession").equals("CP006683"));
    assertTrue(sdoc.getValue("CreateDate").equals("2014/09/04"));
    assertTrue(sdoc.getValue("Length").equals("1896191"));
    assertTrue(sdoc.getValue("UpdateDate").equals("2016/09/14"));
    assertTrue(sdoc.getValue("Description").equals("Melissococcus plutonius S1, complete genome"));
    assertTrue(sdoc.getValue("TaxId").equals("1385937"));
    assertTrue(sdoc.getValue("Identifier").equals("676312358"));
  }

  @Test
  public void testStructure() {
    //many documents
    Summary s = EntrezSummaryLoader.load(new File("./data/ncbi_docsum_struct.xml"));
    System.out.println(s.getDocs().size());
    assertTrue(s.getDocs().size()==46);
    SummaryDoc sdoc = s.getDoc(0);
    Enumeration<String> e = sdoc.getKeys();
    
    while(e.hasMoreElements()){
      String key = e.nextElement();
      System.out.println(String.format("%s:%s",key, sdoc.getValue(key)));
    }
  }
  
  
}
