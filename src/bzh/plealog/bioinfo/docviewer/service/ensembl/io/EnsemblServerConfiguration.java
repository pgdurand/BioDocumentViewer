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
package bzh.plealog.bioinfo.docviewer.service.ensembl.io;

import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;

import com.plealog.genericapp.api.file.EZFileUtils;

public class EnsemblServerConfiguration implements ServerConfiguration {

  private static final String SERVER_URL="http://rest.ensembl.org";
  private static final String H37_SERVER_URL="http://grch37.rest.ensembl.org";
  
  private static final String GENE_TO_ENSG_SERVICE="xrefs/symbol/@SPECIES@/@GENE_NAME@?object_type=gene";
  private static final String FETCH_VAR_SERVICE = "overlap/id/@ENSG_ID@?feature=variation";//;variant_set=ClinVar";
  
  private String _defaultServer;
  
  public EnsemblServerConfiguration(){
    _defaultServer = SERVER_URL;
  }
  
  public EnsemblServerConfiguration(boolean useGRCh37){
    if (useGRCh37){
      _defaultServer = H37_SERVER_URL;
    }
    else{
      _defaultServer = SERVER_URL;
    }
  }
  /**
   * Copy constructor.
   */
  public EnsemblServerConfiguration(EnsemblServerConfiguration srcConfig){
    _defaultServer = srcConfig._defaultServer;
  }

  @Override
  public int getSequencesPerRun() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getSleepTimeBetweenRun() {
    // TODO Auto-generated method stub
    return 5000;
  }

  @Override
  public int getLettersPerRun() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isServerAvailable() {
    return HTTPBasicEngine.isServerAvailable(_defaultServer);
  }

  public String getGene2EnsgIdUrl(String species, String gene_name){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+GENE_TO_ENSG_SERVICE;
    
    str = str.replaceAll("@SPECIES@", species);
    str = str.replaceAll("@GENE_NAME@", gene_name);
    return str;
  }
  
  public String getFetchVariationUrl(String ensg_id){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+FETCH_VAR_SERVICE;
    
    str = str.replaceAll("@ENSG_ID@", ensg_id);
    return str;
  }
}
