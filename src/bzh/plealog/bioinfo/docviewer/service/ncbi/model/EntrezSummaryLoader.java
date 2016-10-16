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
package bzh.plealog.bioinfo.docviewer.service.ncbi.model;

import java.io.File;

import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esearch.ERROR;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esummary.DocSum;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esummary.ESummaryResult;
import bzh.plealog.bioinfo.docviewer.service.ncbi.model.esummary.Item;

/**
 * A convenient class to transform a ESummaryResult object into a Summary object. The former is only
 * used for XML serialization while the latter is used for the business of this software.
 * 
 * @author Patrick G. Durand
 */
public class EntrezSummaryLoader {
  public static Summary load(File xml) {
    String         value;
    Summary        summary;
    SummaryDoc     doc;
    ESummaryResult result;
    DocSum         dsum;
    int            idx1, idx2;
    
    // We create a new summary doc. Note: do not worry about using
    // summary.setFrom() and summary.setTotal() methods on the 
    // new summary object: these are used by the caller of this 
    // load(File) method
    summary = new Summary();

    try {
      // read XML file into an appropriate data model
      result = JAXB.unmarshal(xml, ESummaryResult.class);
      // first, we check if we have an error message
      value = getErrorMessage(result);
      if (value != null) {
        summary.setError(value);
        return summary;
      }
      // then, we read data
      for (Object o : result.getDataElements()) {
        if (o instanceof DocSum) {
          dsum = (DocSum) o;
          doc = new SummaryDoc();
          summary.addDoc(doc);
          for(Item item : dsum.getItem()){
            //all NCBI sequence banks
            if (item.getName().equals("Caption")){//NCBI Entrez key
              doc.add("Accession", item.getvalue());//see: EntrezBank/SummaryPresentationModel keys
            }
            else if (item.getName().equals("Title")){
              value = item.getvalue();
              idx1 = value.lastIndexOf("[");
              idx2 = value.lastIndexOf("]");
              if (idx1!=-1 && idx2!=-1){
                doc.add("Description", item.getvalue().substring(0, idx1));
                doc.add("Organism", item.getvalue().substring(idx1+1, idx2));
              }
              else{
                doc.add("Description", item.getvalue());
              }
            }
            else if (item.getName().equals("Extra")){
              value = item.getvalue();
              idx1 = value.lastIndexOf("[");
              idx2 = value.lastIndexOf("]");
              if (idx1!=-1 && idx2!=-1){
                doc.add("Identifier", item.getvalue().substring(idx1+1, idx2));
              }
              else{
                doc.add("Identifier", item.getvalue());
              }
              //this is required for the UI of DocViewer
              doc.setId(doc.getValue("Identifier"));
            }
            else if (item.getName().equals("CreateDate")){
              doc.add("CreateDate", item.getvalue());
            }
            else if (item.getName().equals("UpdateDate")){
              doc.add("UpdateDate", item.getvalue());
            }
            else if (item.getName().equals("TaxId")){
              doc.add("TaxId", item.getvalue());
            }
            else if (item.getName().equals("Length")){
              doc.add("Length", item.getvalue());
            }
            //NCBI Structure bank
            else if (item.getName().equals("PdbAcc")){
              doc.add("Accession", item.getvalue());
            }
            else if (item.getName().equals("PdbDescr")){
              doc.add("Description", item.getvalue());
            }
            else if (item.getName().equals("PdbDepositDate")){
              doc.add("CreateDate", item.getvalue());
            }
            else if (item.getName().equals("OrganismList")){
              doc.add("Organism", item.getvalue());
            }
            else if (item.getName().equals("ExpMethod")){
              doc.add("Experiment", item.getvalue());
            }
            //NCBI Taxonomy bank
            else if (item.getName().equals("ScientificName")){
              doc.add("Organism", item.getvalue());
            }
            else if (item.getName().equals("CommonName")){
              doc.add("CommonName", item.getvalue());
            }
            else if (item.getName().equals("Rank")){
              doc.add("Rank", item.getvalue());
            }
            else if (item.getName().equals("Division")){
              doc.add("Division", item.getvalue());
            }
          }
        }
      }
    } catch (Exception e) {
      summary.setError(String.format("Unable to read NCBI summary: %s", e.getMessage()));
    }

    return summary;
  }

  private static String getErrorMessage(ESummaryResult result) {
    // first, JAXB may read a wrong XML file, in such a case, result is empty
    if ((result.getDataElements() == null || result.getDataElements().isEmpty())) {
      return "empty file or wrong format";
    }
    // second, check for error from ERROR
    for (Object o : result.getDataElements()) {
      if (o instanceof ERROR) {
        return ((ERROR) o).getvalue();
      }
    }
    return null;
  }
}
