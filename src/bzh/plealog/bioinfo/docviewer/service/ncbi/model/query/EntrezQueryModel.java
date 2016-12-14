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
package bzh.plealog.bioinfo.docviewer.service.ncbi.model.query;

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
 * with NCBI Entrez expressions.
 * 
 * @author Patrick G. Durand
 */
public class EntrezQueryModel implements QueryModel {
  private Hashtable<String, BAccessorEntry> accessors_;
  private Hashtable<String, String> opeSymbolToTxt_;
  private Hashtable<String, String> opeTxtToSymbol_;

  private static final String[] ENTREZ_OPE_FOR_NUMBERS = {
      // Note: It seems that NOT does not work with Entrez URL, while it is cited in
      // the NCBI manual.
      // So, we remove the != and !: operators.
      OPE_Equal/* ,"!=" */, OPE_LessThanEqual, OPE_GreatherThanEqual, OPE_InRangeInclusive };
  private static final String[] ENTREZ_OPE_FOR_STRING = { OPE_MatchRegExp/* ,"!:" */ // basic
                                                                                     // string
                                                                                     // operators
  };

  private static final String[] ENTREZ_OPE_FOR_DATE = { OPE_Equal, OPE_InRangeInclusive };

  //shortcut to define fields common to Nuc/Prot banks
  private static final String NP_TYPE = "NP";//EntrezBank.NUCLEOTIDE.getType()+EntrezBank.PROTEIN.getType();
  //shortcut to define fields common to Nuc/Prot/struct banks
  private static final String NPS_TYPE = "NPS";//EntrezBank.NUCLEOTIDE.getType()+EntrezBank.PROTEIN.getType()+EntrezBank.STRUCTURE.getType();
  private static final String P_TYPE = "P"; //EntrezBank.PROTEIN.getType()
  private static final String N_TYPE = "N"; //EntrezBank.NUCLEOTIDE.getType()
  private static final String T_TYPE = "T"; //EntrezBank.TAXONOMY.getType()
  
  
  public EntrezQueryModel() {
    init();
  }

  private void init() {
    // sources of inspiration:
    // see table 3 from this link to have the data model and field codes
    // http://www.ncbi.nlm.nih.gov/bookshelf/br.fcgi?book=helpentrez&part=EntrezHelp#EntrezHelp.Writing_Advanced_Sea
    // http://www.ncbi.nlm.nih.gov/books/NBK3837/
    // More on:
    // http://eutils.ncbi.nlm.nih.gov/entrez/query/static/esearch_help.html
    // http://cbsu.tc.cornell.edu/resources/seq_comp/PB607_introductory/entrez/NCBI_entrez.html

    BAccessorEntry entry;
    accessors_ = new Hashtable<>();
    
    // It is worth noting that BAccessorEntry class and its relatives were designed to be the data filtering
    // system to handle BLAST result XML files (NCBI format). See BLAST-Filter-Tool on github/pgdurand.
    // However, the design of this system was not too bad to "reuse" it and apply it to a different scheme:
    // enabling the preparation of NCBI Entrez E-Utils URL-based queries.
    
    // To achieve that, we simply have to setup the appropriate entries, as follows:
    // ( notice: third argument of the constructor has been diverted to specify for which kind of NCBI
    //   Entrez database the entry is available: (N)ucleotide, (P)protein, (S)tructure, (C)linVar, etc.
    //   It avoids duplicating entries that are similar to many databanks... probably a bad design,
    //   but I did not have enough time to deeply update BAccessorEntry framework. 
    // )
    
    // Have also a look at class EntrezQueyExpressionBuilder: it uses BAccessorEntry to
    // create NCBI Entrez expression.

    // I only describe first entry, all other entries have similar constructor
    entry = new BAccessorEntry(
        "Accession", // query field as displayed to the user in the editor
        "ACCN",      // query field to use in the Entrez query
        NP_TYPE,  // field available for (N)nucleotide and (P) protein banks (see EntrezBank class)
        ENTREZ_OPE_FOR_STRING, //this field relies on string operators
        DGMAttribute.DT_STRING); //argument is a string
    entry.setAllowCaseSensitive(false); // field is not case sensitive
    entry.setHelpMsg(
        "Represents the unique accession number of a sequence or structure entry. The 3D structure database accession is the PDB ID but not the MMDB ID.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("All Fields", "ALL", NPS_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents all terms from all searchable fields (e.g. description) in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Author", "AUTH", NPS_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents authors from all references in a database record. The format is last name space first initial(s), without punctuation (e.g., servat g)");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("EC/RN Number", "ECNO", P_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the number assigned by the Enzyme Commission or Chemical Abstract Service (CAS) to designate a particular enzyme or chemical, respectively.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Feature Key", "FKEY", N_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the biological features assigned or annotated to a nucleotide sequence as defined in the DDBJ/EMBL/GenBank Feature Table (http://www.ncbi.nlm.nih.gov/projects/collab/FT/index.html). Not available for the Protein and 3D structure databases.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Gene Name", "GENE", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the standard and common names of genes found in the database records. This field is not available in 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Keyword", "KYWD", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the terms from the controlled vocabularies associated with the GenBank, EMBL, DDBJ, SWISS-Prot, PIR, PRF, or PDB databases. Not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Journal Name", "JOUR", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the name of the journal (in abbreviated form; e.g., J Biol Chem) or ISSN in which the data were published.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Modification Date", "MDAT", NPS_TYPE, ENTREZ_OPE_FOR_DATE, DGMAttribute.DT_DATE);
    entry.setHelpMsg(
        "Represents the date of the most recent modification to an entry in NCBI Entrez databases. " + DATE_HLP_MSG);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Molecular Weight", "MOLWT", P_TYPE, ENTREZ_OPE_FOR_NUMBERS, DGMAttribute.DT_LONG);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Organism", "ORGN", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents the scientific and common names for the organisms associated with an entry.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Primary Accession", "PACC", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the primary accession number of an entry assigned by a sequence database builder. A Primary Accession index is not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Properties", "PROPS", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the properties of a sequence. For example, the Nucleotide database's Properties index includes molecule types, publication status, molecule locations, and GenBank divisions. Not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Protein Name", "PROT", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the standard names of proteins found in database records. Sometimes, it is best to also consider All Fields or Text Words. Not available in the Structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Publication Date", "PDAT", NPS_TYPE, ENTREZ_OPE_FOR_DATE, DGMAttribute.DT_DATE);
    entry.setHelpMsg("Represents the date a entry is released in NCBI Entrez databases. " + DATE_HLP_MSG);
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("SeqID String", "SQID", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the special string identifier, similar to a FASTA identifier, for a given sequence. A SeqID String index is not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Sequence Length", "SLEN", NP_TYPE, ENTREZ_OPE_FOR_NUMBERS, DGMAttribute.DT_LONG);
    entry.setHelpMsg("Sequence Length is not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Substance Name", "SUBS", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Represents the names of any chemicals associated with an entry from the CAS registry and the MEDLINE Name of Substance field. Not available in the Genome database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Text Word", "WORD", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents all of the free text associated with a database entry.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    
    
    entry = new BAccessorEntry("Title", "TITL", NP_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg(
        "Includes only the words found in the definition line of a record. Not available in the 3D structure database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);


    // BEGIN - NCBI Taxonomy only
    // Documentation about NCBI ENtrey Taxonomy queryable fields is not easily available.
    // I relied on this page: http://www.ncbi.nlm.nih.gov/taxonomy/advanced
    // using this advanced browser, one can see the fields of interest
    entry = new BAccessorEntry("All Names", "All Names", T_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents all names from all searchable fields in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    entry = new BAccessorEntry("Common Names", "Common Name", T_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents common names in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    entry = new BAccessorEntry("Scientific Names", "Scientific Name", T_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents only scientific names in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    entry = new BAccessorEntry("Division", "Division", T_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents division in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    entry = new BAccessorEntry("Rank", "Rank", T_TYPE, ENTREZ_OPE_FOR_STRING, DGMAttribute.DT_STRING);
    entry.setAllowCaseSensitive(false);
    entry.setHelpMsg("Represents rank in the database.");
    accessors_.put(entry.getAccessorVisibleName(), entry);
    // END - NCBI Taxonomy only
    
    //setup operators mapping (operator sign vs. its human-readable counterpart)
    opeSymbolToTxt_ = new Hashtable<>();
    opeSymbolToTxt_.put(OPE_Equal, "is equal to");
    opeSymbolToTxt_.put(OPE_NotEqual, "is not equal to");
    opeSymbolToTxt_.put(OPE_LessThanEqual, "is less than or equal to");
    opeSymbolToTxt_.put(OPE_GreatherThanEqual, "is greater than or equal to");
    opeSymbolToTxt_.put(OPE_MatchRegExp, "contains");
    opeSymbolToTxt_.put(OPE_NotMatchRegExp, "does not contain");
    opeSymbolToTxt_.put(OPE_InRangeInclusive, "is in the range (inclusive)");

    opeTxtToSymbol_ = new Hashtable<>();
    opeTxtToSymbol_.put("is equal to", OPE_Equal);
    opeTxtToSymbol_.put("is not equal to", OPE_NotEqual);
    opeTxtToSymbol_.put("is less than or equal to", OPE_LessThanEqual);
    opeTxtToSymbol_.put("is greater than or equal to", OPE_GreatherThanEqual);
    opeTxtToSymbol_.put("contains", OPE_MatchRegExp);
    opeTxtToSymbol_.put("does not contain", OPE_NotMatchRegExp);
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
    return new EntrezQueryFactory();
  }

  public BRuleFactory getRuleFactory() {
    return new BRuleFactoryImplem();
  }

  @Override
  public void addAccessorEntry(BAccessorEntry arg0) {
    // no need to do anything here
  }
}
