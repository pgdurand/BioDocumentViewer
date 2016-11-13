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

public class EnsemblOptVariantTest {

  private static HashSet<String> ids;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ids = new HashSet<>();
    ids.add("rs565779474");
    ids.add("rs761919219");
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
    
    Opt o = JAXB.unmarshal(new File("./data/ensembl_opt_variants.xml"), Opt.class);
    
    assertTrue(o.getItem().size()==2);
    
    for(OptDataItem item : o.getItem()){
      assertTrue(ids.contains(item.getId()));
    }
    
    OptDataItem item = o.getItem().get(0);
    assertTrue(item.getAlleles().get(0).getvalue().equals("G"));
    assertTrue(item.getAlleles().get(1).getvalue().equals("A"));
    assertTrue(item.getAllelesStr().equals("G/A"));
    assertTrue(item.getAssembly_name().equals("GRCh38"));
    assertTrue(item.getConsequence_type().equals("3_prime_UTR_variant"));
    assertTrue(item.getFeature_type().equals("variation"));
    assertTrue(item.getSource().equals("dbSNP"));
    assertTrue(item.getSeq_region_name().equals("7"));
    assertTrue(item.getStart()==140719364);
    assertTrue(item.getEnd()==140719364);
    assertTrue(item.getStrand()==1);

    item = o.getItem().get(1);
    assertTrue(item.getAlleles().get(0).getvalue().equals("A"));
    assertTrue(item.getAlleles().get(1).getvalue().equals("C"));
    assertTrue(item.getAllelesStr().equals("A/C"));
    assertTrue(item.getAssembly_name().equals("GRCh38"));
    assertTrue(item.getConsequence_type().equals("3_prime_UTR_variant"));
    assertTrue(item.getFeature_type().equals("variation"));
    assertTrue(item.getSource().equals("dbSNP"));
    assertTrue(item.getSeq_region_name().equals("7"));
    assertTrue(item.getStart()==140719366);
    assertTrue(item.getEnd()==140719366);
    assertTrue(item.getStrand()==1);

  }

  @Test
  public void test2() {
    
    Opt o = JAXB.unmarshal(new File("./data/ensembl_tp53_variants.xml"), Opt.class);
    
    System.out.println(o.getItem().size());
    
    assertTrue(o.getItem().size()==411);
    
  }
}
