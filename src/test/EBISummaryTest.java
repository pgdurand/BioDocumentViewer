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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.EbiSummaryLoader;

public class EBISummaryTest {

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
  public void testBasicResult() {
    Summary s = EbiSummaryLoader.load(new File("./data/ebi_docsum_prot.xml"));
    assertTrue(s.getDocs().size()==1);
    SummaryDoc doc = s.getDoc(0);
    assertTrue(doc.getValue("Identifier").equals("BGLR_MOUSE"));
    assertTrue(doc.getValue("Accession").equals("P12265"));
    assertTrue(doc.getValue("Description").equals("Beta-glucuronidase"));
    assertTrue(doc.getValue("Organism").equals("Mus musculus"));
    assertTrue(doc.getValue("Length").equals("648"));
    assertTrue(doc.getValue("Status").equals("Reviewed"));
  }

}
