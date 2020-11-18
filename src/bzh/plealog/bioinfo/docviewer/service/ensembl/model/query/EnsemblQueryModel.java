/* Copyright (C) 2006-2020 Patrick G. Durand
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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.query;

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
 * with Ensembl expressions.
 * 
 * @author Patrick G. Durand
 */
public class EnsemblQueryModel implements QueryModel {
  private Hashtable<String, BAccessorEntry> accessors_;
  private Hashtable<String, String> opeSymbolToTxt_;
  private Hashtable<String, String> opeTxtToSymbol_;

  private static final String[] EBI_OPE_FOR_STRING_1 = { OPE_Equal};
  
  private static final String V_TYPE = "V";
  
  public static String GENE_NAME_KEY = "GN_K";
  public static String SPECIES_KEY = "S_K";
  
  public EnsemblQueryModel() {
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
        "Gene name", // query field as displayed to the user in the editor
        GENE_NAME_KEY,      // query field to use in the EBI-Eye query
        V_TYPE,  // field available for (N)nucleotide and (P) protein banks (see EbiBank class)
        EBI_OPE_FOR_STRING_1, //this field relies on string operators
        DGMAttribute.DT_STRING); //argument is a string
    // field is not case sensitive (use this method with 'false' to disable case sensitive
    // check box; not used to query remote server)
    entry.setAllowCaseSensitive(false); 
    entry.setHelpMsg(
        "Gene name.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    //id" name="id" description="Identifier"
    entry = new BAccessorEntry("Species", SPECIES_KEY, V_TYPE, EBI_OPE_FOR_STRING_1, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Species names. ");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    //setup operators mapping (operator sign vs. its human-readable counterpart)
    opeSymbolToTxt_ = new Hashtable<>();
    opeSymbolToTxt_.put(OPE_Equal, "is equal to");

    opeTxtToSymbol_ = new Hashtable<>();
    opeTxtToSymbol_.put("is equal to", OPE_Equal);

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
    return new EnsemblQueryFactory();
  }

  public BRuleFactory getRuleFactory() {
    return new BRuleFactoryImplem();
  }

  @Override
  public void addAccessorEntry(BAccessorEntry arg0) {
  }
}
