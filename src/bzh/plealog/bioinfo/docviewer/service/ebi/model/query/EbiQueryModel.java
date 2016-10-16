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
package bzh.plealog.bioinfo.docviewer.service.ebi.model.query;

import java.util.Enumeration;
import java.util.Hashtable;

import bzh.plealog.bioinfo.api.filter.BAccessorEntry;
import bzh.plealog.bioinfo.api.filter.BFilterFactory;
import bzh.plealog.bioinfo.api.filter.BRuleFactory;
import bzh.plealog.bioinfo.docviewer.api.QueryModel;
import bzh.plealog.bioinfo.filter.implem.BRuleFactoryImplem;
import bzh.plealog.hge.api.datamodel.DGMAttribute;

/**
 * This class can be used to instantiate a Filter data model suitable to deal
 * with EBI-Eye expressions.
 * 
 * @author Patrick G. Durand
 */
public class EbiQueryModel implements QueryModel {
  private Hashtable<String, BAccessorEntry> accessors_;
  private Hashtable<String, String> opeSymbolToTxt_;
  private Hashtable<String, String> opeTxtToSymbol_;

  private static final String[] EBI_OPE_FOR_STRING_1 = { OPE_Equal};
  private static final String[] EBI_OPE_FOR_STRING_2 = { OPE_MatchRegExp};
  private static final String[] EBI_OPE_FOR_DATE = { OPE_Equal, OPE_InRangeInclusive };
  private static final String[] EBI_OPE_FOR_NUMBERS = { OPE_Equal, OPE_InRangeInclusive };
  
  private static final String NP_TYPE = "NP"; //EbiBank.EMBL_REL.getType()+EbiBank.PROTEIN.getType()
  private static final String P_TYPE = "P"; //EbiBank.PROTEIN.getType()
  private static final String N_TYPE = "N"; //EbiBank.EMBL_REL.getType()
  
  
  public EbiQueryModel() {
    init();
  }

  private void init() {
    
    // To really know what are the fields available to create a query:
    // Use (in a browser): http://www.ebi.ac.uk/ebisearch/ws/rest
    //  to know list of available DBs
    // For instance, take 'emblrelease'. 
    // Then, use: http://www.ebi.ac.uk/ebisearch/ws/rest/emblrelease
    // to list fieldInfo elements, and locate those with attribute searchable=true
    // ... this how to complete code below... 

    // You can also use: http://www.ebi.ac.uk/ebisearch/querybuilder.ebi
    //   to retrieve fields that can be queried for particular DB and see how
    // to write a valid expression.

    // Then update method formatEbiField() in class EbiQueryExpressionBuilder to 
    // setup EBI query expression appropriately
    
    
    BAccessorEntry entry;
    accessors_ = new Hashtable<>();
    
    // It is worth noting that BAccessorEntry class and its relatives were designed to be the data filtering
    // system to handle BLAST result XML files (NCBI format). See BLAST-Filter-Tool on github/pgdurand.
    // However, the design of this system was not too bad to "reuse" it and apply it to a different scheme:
    // enabling the preparation of EBE-Eye URL-based queries.
    
    // To achieve that, we simply have to setup the appropriate entries, as follows:
    // ( notice: third argument of the constructor has been diverted to specify for which kind of EBI
    //   database the entry is available: (N)ucleotide, (P)protein, etc.
    //   It avoids duplicating entries that are similar to many databanks... probably a bad design,
    //   but I did not have enough time to deeply update BAccessorEntry framework. 
    // )
    
    // Have also a look at class EbiQueyExpressionBuilder: it uses BAccessorEntry to
    // create EBI-Eye expression.

    // I only describe first entry, all other entries have similar constructor
    entry = new BAccessorEntry(
        "Accession", // query field as displayed to the user in the editor
        "acc",      // query field to use in the EBI-Eye query
        NP_TYPE,  // field available for (N)nucleotide and (P) protein banks (see EbiBank class)
        EBI_OPE_FOR_STRING_1, //this field relies on string operators
        DGMAttribute.DT_STRING); //argument is a string
    // field is not case sensitive (use this method with 'false' to disable case sensitive
    // check box; not used to query remote server)
    entry.setAllowCaseSensitive(false); 
    entry.setHelpMsg(
        "Represents the unique accession number of a sequence or structure entry.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //id" name="id" description="Identifier"
    entry = new BAccessorEntry("Primary identifier", "id", NP_TYPE, EBI_OPE_FOR_STRING_1, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Primary identifier (ID line of the flast file). ");
    //All fields
    entry = new BAccessorEntry("All Fields", "ALL", NP_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents all terms from all searchable fields (e.g. description) in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //Create date
    entry = new BAccessorEntry("Creation Date", "creation_date", NP_TYPE, EBI_OPE_FOR_DATE, DGMAttribute.DT_DATE);
    entry.setHelpMsg(
        "Represents the date of creation of an entry in databank. " + DATE_HLP_MSG);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //Update date
    entry = new BAccessorEntry("Modification Date", "last_modification_date", NP_TYPE, EBI_OPE_FOR_DATE, DGMAttribute.DT_DATE);
    entry.setHelpMsg(
        "Represents the date of the most recent modification to an entry in databank. " + DATE_HLP_MSG);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //length
    entry = new BAccessorEntry("Sequence Length", "length", NP_TYPE, EBI_OPE_FOR_NUMBERS, DGMAttribute.DT_LONG);
    entry.setHelpMsg("Sequence Length. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //description" name="description" description="Description (DE line of the flat file)
    entry = new BAccessorEntry("Description", "description", NP_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Description (DE line of the flat file). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //organism_classification" name="organism_classification" description="Organism classification (OC line of the flat file)">
    entry = new BAccessorEntry("Organism classification", "organism_classification", NP_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Organism classification (OC line of the flat file). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //organism_species" name="organism_species" description="Organism species (OS line of the flat file)">
    entry = new BAccessorEntry("Organism: species", "organism_species", NP_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Organism species (OS line of the flat file). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);

    /** START: specific to uniprot*/
    //descRecName" name="descRecName" description="Recommended name (full and short names)
    entry = new BAccessorEntry("Recommended name", "descRecName", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Recommended name (full and short names). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //descAltName" name="descAltName" description="Alternative name"
    entry = new BAccessorEntry("Alternative name", "descAltName", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Alternative name. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //disease_name" name="disease_name" description="Disease name"
    entry = new BAccessorEntry("Disease name", "disease_name", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Disease name. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //gene_primary_name" name="gene_primary_name" description="Primary gene's name"
    entry = new BAccessorEntry("Primary gene's name", "gene_primary_name", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Primary gene's name. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //gene_synonym_name" name="gene_synonym_name" description="Gene's synonym name">
    entry = new BAccessorEntry("Gene's synonym name", "gene_synonym_name", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Gene's synonym name. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //keywords" name="Keywords" description="Keywords">
    entry = new BAccessorEntry("Keywords", "keywords", NP_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Keywords. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //organism_scientific_name" name="organism_scientific_name" description="Scientific name of the organism">
    entry = new BAccessorEntry("Organism: scientific name", "organism_scientific_name", P_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Scientific name of the organism. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //status" name="Status" description="Status">
    entry = new BAccessorEntry("Status", "status", P_TYPE, EBI_OPE_FOR_STRING_1, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Accept only one of: Reviewed, Unreviewed (Swissprot vs. TrEMBL). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    /** END: specific to uniprot*/
    
    
    /** START: specific to emblrelease*/
    //gene" name="gene" description="Symbol of the gene corresponding to a sequence region (Feature qualifier '/gene')">
    entry = new BAccessorEntry("Gene symbol", "gene", N_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Symbol of the gene corresponding to a sequence region (Feature qualifier '/gene'). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);

    //organism" name="organism" description="Scientific name of the organism that provided the sequenced genetic material (Feature qualifier '/organism')">
    entry = new BAccessorEntry("Organism", "organism", N_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Scientific name of the organism that provided the sequenced genetic material (Feature qualifier '/organism'). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);

    //topology" name="topology" description="Topology">
    entry = new BAccessorEntry("Topology", "topology", N_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Topology. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);

    //organelle" name="organelle" description="Sub-cellular location of non-nuclear sequences (OG lines)">"
    entry = new BAccessorEntry("Organelle", "organelle", N_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Sub-cellular location of non-nuclear sequences (OG lines). ");
    accessors_.put(entry.getAccessorVisibleName(), entry);

    //locus_tag" name="locus_tag" description="Locus tag">
    entry = new BAccessorEntry("Locus tag", "locus_tag", N_TYPE, EBI_OPE_FOR_STRING_2, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Locus tag. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    /** END: specific to emblrelease*/

    //setup operators mapping (operator sign vs. its human-readable counterpart)
    opeSymbolToTxt_ = new Hashtable<>();
    opeSymbolToTxt_.put(OPE_Equal, "is equal to");
    opeSymbolToTxt_.put(OPE_MatchRegExp, "contains");
    opeSymbolToTxt_.put(OPE_InRangeInclusive, "is in the range (inclusive)");

    opeTxtToSymbol_ = new Hashtable<>();
    opeTxtToSymbol_.put("is equal to", OPE_Equal);
    opeTxtToSymbol_.put("contains", OPE_MatchRegExp);
    opeTxtToSymbol_.put("is in the range (inclusive)", OPE_InRangeInclusive);

  }

  public BAccessorEntry getAccessorEntry(String visibleName) {
    return (BAccessorEntry) accessors_.get(visibleName);
  }

  public Enumeration<String> getAccessorVisibleNames() {
    return accessors_.keys();
  }

  public String getOperatorForText(String txtSymbol) {
    return opeTxtToSymbol_.get(txtSymbol);
  }

  public String getTextForOperator(String opeSymbol) {
    return opeSymbolToTxt_.get(opeSymbol);
  }

  protected Enumeration<BAccessorEntry> getAccessors(final String type) {
    return new Enumeration<BAccessorEntry>() {
      Enumeration<String> enume;
      BAccessorEntry entry = null;
      boolean bFirst = true;

      private void initialize() {
        enume = accessors_.keys();
        bFirst = false;
      }

      public synchronized boolean hasMoreElements() {
        if (bFirst)
          initialize();
        if (entry != null)
          return true;
        while (enume.hasMoreElements()) {
          entry = accessors_.get(enume.nextElement());
          if (entry.getObjectType().indexOf(type) != -1)
            return true;
        }
        entry = null;
        return (false);
      }

      public synchronized BAccessorEntry nextElement() {
        BAccessorEntry o = entry;
        entry = null;
        return o;
      }
    };
  }

  protected Enumeration<String> getAccessorVisibleNames(final String type) {
    return new Enumeration<String>() {
      Enumeration<String> enume;
      BAccessorEntry entry = null;
      boolean bFirst = true;

      private void initialize() {
        enume = accessors_.keys();
        bFirst = false;
      }

      public synchronized boolean hasMoreElements() {
        if (bFirst)
          initialize();
        if (entry != null)
          return true;
        while (enume.hasMoreElements()) {
          entry = accessors_.get(enume.nextElement());
          if (entry.getObjectType().indexOf(type) != -1)
            return true;
        }
        entry = null;
        return (false);
      }

      public synchronized String nextElement() {
        BAccessorEntry o = entry;
        entry = null;
        return o.getAccessorVisibleName();
      }
    };
  }

  public BFilterFactory getFilterFactory() {
    return new EbiQueryFactory();
  }

  public BRuleFactory getRuleFactory() {
    return new BRuleFactoryImplem();
  }
}
