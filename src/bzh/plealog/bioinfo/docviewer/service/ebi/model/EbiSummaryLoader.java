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
package bzh.plealog.bioinfo.docviewer.service.ebi.model;

import java.io.File;

import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsDiagnostics;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsEntry;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsField;
import bzh.plealog.bioinfo.docviewer.service.ebi.model.search.WsResult;

public class EbiSummaryLoader {
  public static Summary load(File xml) {
    String value;
    Summary summary;
    WsResult result;
    SummaryDoc doc;

    // We create a new summary doc. Note: do not worry about using
    // summary.setFrom() and summary.setTotal() methods on the
    // new summary object: these are used by the caller of this
    // load(File) method
    summary = new Summary();

    try {
      // read XML file into an appropriate data model
      result = JAXB.unmarshal(xml, WsResult.class);

      // first, we check if we have an error message
      value = getErrorMessage(result);
      if (value != null) {
        summary.setError(value);
        return summary;
      }

      // then, we get results
      // note: wrong request may result in an emty answer, without any error
      // message.
      // So, we handle that here
      if (result.getEntries().getEntry().isEmpty()) {
        summary.setError("Server answer is: no entries");
        return summary;
      }
      for (WsEntry entry : result.getEntries().getEntry()) {
        doc = new SummaryDoc();
        summary.addDoc(doc);

        doc.setId(entry.getId());
        doc.add("Identifier", entry.getId());
        doc.add("Accession", entry.getAcc());
        // to know fields that are available for display, use
        // http://www.ebi.ac.uk/ebisearch/ws/rest/xxx
        // where xxx is a valid DB id (e.g. uniprotkb). In the XML answer of
        // that web service, have a look at the many fieldInfo elements,
        // especially those with 'id' in lower-case having 'retrievable' flag
        // set to true.
        for (WsField field : entry.getFields().getField()) {
          // Convert "EBI-Eye fields" to "DocViewer" ones.

          // ** Description
          // descRecName: to retrieve description from uniprotkb
          // description: to retrieve description from emblrelease
          if ((field.getId().equals("descRecName") || field.getId().equals("description"))
              && field.getValues().getValue().isEmpty() == false) {
            // Description: DocViewer key for EBI service as describe in
            // classEbiBank/SummaryPresentation
            doc.add("Description", field.getValues().getValue().get(0));

            // ** Organism
          } else if (field.getId().equals("organism_scientific_name")
              && field.getValues().getValue().isEmpty() == false) {
            // Note: uniprotkb does provide access to organism name. However,
            // emblrelease does not !!!
            // So, for the emblrelease db, an alternative is:
            // retrieve field 'TAXON" (you get a taxon ID), then use this URL to
            // get organism name:
            // http://www.ebi.ac.uk/ebisearch/ws/rest/taxonomy?query=id:(9605)&fields=name
            // Not implemented yet, and should be implemented in class EbiQueryEngine
            // because it is the one that handles HTTP Get connections. 
            // Of note: it's possible to get multiple names using:
            // http://www.ebi.ac.uk/ebisearch/ws/rest/taxonomy?query=id:(9605)+OR+id:(9606)&fields=name
            //   ('+' means space character in URL encoding)
            
            doc.add("Organism", field.getValues().getValue().get(0));

            // ** Length (uniprotkb only)
          } else if (field.getId().equals("length") && field.getValues().getValue().isEmpty() == false) {
            doc.add("Length", field.getValues().getValue().get(0));

            // ** Status (uniprotkb only)
          } else if (field.getId().equals("status") && field.getValues().getValue().isEmpty() == false) {
            doc.add("Status", field.getValues().getValue().get(0));
          }
        }
      }
      // Service provider should report current page of ids. This is not the
      // case for EBI, so we set from to zero, and this value will be updated
      // accordingly here: EbiQueryEngine.getIds/Summary(int from, int nb)
      summary.setFrom(0);
      // Service provider should report the grand total number of documents
      // available in the DB. ok for EBI, so we retrieve that value.
      // Otherwise, we would have to update that value here:
      // EbiQueryEngine.getIds/Summary(int from, int nb)
      summary.setTotal(result.getHitCount());
    } catch (Exception e) {
      summary.setError(String.format("Unable to read EBI-Eye answer: %s", e.getMessage()));
    }

    return summary;
  }

  private static String getErrorMessage(WsResult result) {
    WsDiagnostics diags = result.getDiagonostics();
    return diags != null ? diags.getMessage() : null;
  }
}
