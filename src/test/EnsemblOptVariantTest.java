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
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.format.DIWrapper;

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
    DIWrapper wrapper = new DIWrapper(item);
    assertTrue(item.getAlleles().get(0).getvalue().equals("G"));
    assertTrue(item.getAlleles().get(1).getvalue().equals("A"));
    assertTrue(wrapper.getAllelesStr().equals("G/A"));
    assertTrue(item.getAssembly_name().equals("GRCh38"));
    assertTrue(item.getConsequence_type().equals("3_prime_UTR_variant"));
    assertTrue(item.getFeature_type().equals("variation"));
    assertTrue(item.getSource().equals("dbSNP"));
    assertTrue(item.getSeq_region_name().equals("7"));
    assertTrue(item.getStart()==140719364);
    assertTrue(item.getEnd()==140719364);
    assertTrue(item.getStrand()==1);

    item = o.getItem().get(1);
    wrapper = new DIWrapper(item);
    assertTrue(item.getAlleles().get(0).getvalue().equals("A"));
    assertTrue(item.getAlleles().get(1).getvalue().equals("C"));
    assertTrue(wrapper.getAllelesStr().equals("A/C"));
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
    
    assertTrue(o.getItem().size()==6346);
    
  }

  @Test
  public void test3() {
    
    Opt o = JAXB.unmarshal(new File("./data/ensembl_dbsnp_vep.xml"), Opt.class);
    DIWrapper wrapper = new DIWrapper(o.getItem().get(0));
    
    // wa have two data pieces
    assertTrue(o.getItem().size()==2);
    
    //check first data piece: we have clinical significance data
    assertTrue(o.getItem().get(0).getClinical_significance().size()==2);
    assertTrue(o.getItem().get(0).getClinical_significance().get(0).getvalue().equals("uncertain significance"));
    assertTrue(o.getItem().get(0).getClinical_significance().get(1).getvalue().equals("likely benign"));
    assertTrue(wrapper.getClinical_significanceStr().equals("uncertain significance; likely benign"));

    //check first data piece: we have evidence data
    assertTrue(o.getItem().get(0).getEvidence().size()==2);
    assertTrue(o.getItem().get(0).getEvidence().get(0).getvalue().equals("Phenotype_or_Disease"));
    assertTrue(o.getItem().get(0).getEvidence().get(1).getvalue().equals("ExAC"));
    assertTrue(wrapper.getEvidenceStr().equals("Phenotype_or_Disease; ExAC"));

    //check first data piece: we have synonyms
    assertTrue(o.getItem().get(0).getSynonyms().size()==69);
    assertTrue(o.getItem().get(0).getSynonyms().get(0).getvalue().equals("NM_001126115.1:c.724G>A"));
    assertTrue(o.getItem().get(0).getSynonyms().get(68).getvalue().equals("RCV000130166"));
    
    //check first data piece: we have mappings
    assertTrue(o.getItem().get(0).getMappings().size()==1);
    assertTrue(o.getItem().get(0).getMappings().get(0).getAllele_string().equals("C/A/G/T"));
    assertTrue(o.getItem().get(0).getMappings().get(0).getAssembly_name().equals("GRCh38"));
    assertTrue(o.getItem().get(0).getMappings().get(0).getCoord_system().equals("chromosome"));
    assertTrue(o.getItem().get(0).getMappings().get(0).getLocation().equals("17:7669671-7669671"));
    assertTrue(o.getItem().get(0).getMappings().get(0).getStart()==7669671);
    assertTrue(o.getItem().get(0).getMappings().get(0).getEnd()==7669671);
    assertTrue(o.getItem().get(0).getMappings().get(0).getSeq_region_name()==17);

    //check second data piece: we have most_severe_consequence data
    assertTrue(o.getItem().get(1).getMost_severe_consequence().equals("missense_variant"));
    assertTrue(o.getItem().get(1).getAllele_string().equals("C/A/G/T"));
    
    //check second data piece: we have transcript_consequences data
    assertTrue(o.getItem().get(1).getTranscriptConsequences().size()==75);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getSift_score()==0.01);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getSift_prediction().equals("deleterious"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getPolyphen_score()==0.986);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getPolyphen_prediction().equals("probably_damaging"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getCdna_end()==1310);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getCdna_start()==1310);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getCds_end()==1120);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getCds_end()==1120);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getProtein_end()==374);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getProtein_start()==374);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getAmino_acids().equals("G/C"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getBiotype().equals("protein_coding"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getCodons().equals("Ggt/Tgt"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getGene_id().equals("ENSG00000141510"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getGene_symbol().equals("TP53"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getImpact().equals("MODERATE"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getTranscript_id().equals("ENST00000269305"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getVariant_allele().equals("A"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getConsequenceTerms().size()==1);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(0).getConsequenceTermsStr().equals("missense_variant"));
    
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(74).getConsequenceTerms().size()==2);
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(74).getConsequenceTerms().get(0).getvalue().equals("intron_variant"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(74).getConsequenceTerms().get(1).getvalue().equals("NMD_transcript_variant"));
    assertTrue(o.getItem().get(1).getTranscriptConsequences().get(74).getConsequenceTermsStr().equals("intron_variant;NMD_transcript_variant"));
  }

}
