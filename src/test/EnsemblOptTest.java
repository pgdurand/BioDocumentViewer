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
import java.util.HashSet;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Opt;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.OptDataItem;

public class EnsemblOptTest {

  private static HashSet<String> ids;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ids = new HashSet<>();
    ids.add("ENSG00000141510");
    ids.add("ENSG00000067369");
    ids.add("LRG_321");
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
  public void test1() {
    
    Opt o = JAXB.unmarshal(new File("./data/ensembl_opt.xml"), Opt.class);
    
    assertTrue(o.getItem().size()==3);
    
    for(OptDataItem item : o.getItem()){
      assertTrue(ids.contains(item.getId()));
      assertTrue(item.getType().equals("gene"));
    }
    
  }

}
