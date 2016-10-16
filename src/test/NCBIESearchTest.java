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

import bzh.plealog.bioinfo.docviewer.api.Search;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.EntrezSearchLoader;

public class NCBIESearchTest {

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
  public void testBadFormat() {
    Search s = EntrezSearchLoader.load(new File("./data/clinvar_esummary_sample.xml"));
    System.out.println(s.getError());
  }

  @Test
  public void testErrorList() {
    Search s = EntrezSearchLoader.load(new File("./data/ncbi_error.xml"));
    assertTrue("fdsfdsf[ACCN]: No items found.".equals(s.getError()));
  }

  @Test
  public void testError() {
    Search s = EntrezSearchLoader.load(new File("./data/ncbi_error2.xml"));
    assertTrue("Invalid db name specified: krotein".equals(s.getError()));
  }

  @Test
  public void testIdListPage1() {
    Search s = EntrezSearchLoader.load(new File("./data/ncbi_idList_p1.xml"));
    assertTrue(s.getError() == null);
    assertTrue(s.getFrom()==0);
    assertTrue(s.getTotal()==37533);
    assertTrue(s.getIds().size()==100);
    assertTrue(s.getId(0).equals("506947600"));
    assertTrue(s.getId(99).equals("190194389"));
  }
  @Test
  public void testIdListPage2() {
    Search s = EntrezSearchLoader.load(new File("./data/ncbi_idList_p2.xml"));
    assertTrue(s.getError() == null);
    assertTrue(s.getFrom()==100);
    assertTrue(s.getTotal()==37533);
    assertTrue(s.getIds().size()==100);
    assertTrue(s.getId(0).equals("446868675"));
    assertTrue(s.getId(99).equals("742272596"));
  }

}
