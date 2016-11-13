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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model;

import java.io.File;

import javax.xml.bind.JAXB;

import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.service.ensembl.EnsemblBank;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.Opt;
import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.OptDataItem;

/**
 * A convenient class to convert Opt XML documents into Summary objects.
 * The former is only used for XML serialization while the latter is used for
 * the business of this software.
 * 
 * @author Patrick G. Durand
 * */
public class EnsemblSummaryLoader {
  public static Summary load(File xml) {
    String value;
    Summary summary;
    Opt result;
    SummaryDoc doc;

    // We create a new summary doc. Note: do not worry about using
    // summary.setFrom() and summary.setTotal() methods on the
    // new summary object: these are used by the caller of this
    // load(File) method
    summary = new Summary();

    try {
      // read XML file into an appropriate data model
      result = JAXB.unmarshal(xml, Opt.class);

      // first, we check if we have an error message
      value = getErrorMessage(result);
      if (value != null) {
        summary.setError(value);
        return summary;
      }

      // then, we get results
      for (OptDataItem entry : result.getItem()) {
        doc = new SummaryDoc();
        summary.addDoc(doc);
        doc.setId(entry.getId());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.Identifier_HDR], entry.getId());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.ASSEMBLY_HDR], entry.getAssembly_name());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.SOURCE_HDR], entry.getSource());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.LOCATION_HDR], entry.getLocation());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.CONSEQUENCE_HDR], entry.getConsequence_type());
        doc.add(EnsemblBank.EnsemblDbSummaryDocPresentationModel.RES_HEADERS[EnsemblBank.EnsemblDbSummaryDocPresentationModel.VARIATION_HDR], entry.getAllelesStr());
      }
      // Service provider should report current page of ids. This is not the
      // case for Ensembl, so we set from to zero, and this value will be updated
      // accordingly here: EnsemblQueryEngine.getIds/Summary(int from, int nb)
      summary.setFrom(0);
      // Service provider should report the grand total number of documents
      // available in the DB. ok for Ensembl, so we retrieve that value.
      // Otherwise, we would have to update that value here:
      // EnsemblQueryEngine.getIds/Summary(int from, int nb)
      summary.setTotal(result.getItem().size());
    } catch (Exception e) {
      summary.setError(String.format("Unable to read Ensembl answer: %s", e.getMessage()));
    }

    return summary;
  }

  private static String getErrorMessage(Opt result) {
    if (result.getItem().isEmpty()) {
      return "Server answer is: no entries";
    }
    for (OptDataItem entry : result.getItem()) {
      if (entry.getError()!=null){
        return entry.getError();
      }
    }
    return  null;
  }
}
