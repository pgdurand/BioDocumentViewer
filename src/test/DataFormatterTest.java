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
package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.docviewer.format.DataFormatter;
import bzh.plealog.bioinfo.docviewer.format.DataFormatter.FORMAT;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Opt;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.format.DIWrapper;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

public class DataFormatterTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    EZEnvironment.addResourceLocator(Messages.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test1() {
    
    Opt o = JAXB.unmarshal(new File("./data/ensembl_dbsnp_varvep.xml"), Opt.class);
    try(FileWriter fw = new FileWriter("./data/render/index_test.html")){
     assertTrue(DataFormatter.dump(fw, new DIWrapper(o.getItem().get(0)), FORMAT.ENSEMBL_VAR_HTML)); 
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
