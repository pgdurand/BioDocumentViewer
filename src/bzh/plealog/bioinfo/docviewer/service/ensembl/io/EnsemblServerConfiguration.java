/* Copyright (C) 2006-2017 Patrick G. Durand
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

import com.plealog.genericapp.api.file.EZFileUtils;

import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;

/**
 * Setup the configuration to query Ensembl RESTful API.
 * 
 * @author Patrick G. Durand
 */
public class EnsemblServerConfiguration implements ServerConfiguration {

  //  ==== Server address
  // Standard Ensembl RESTful server. Can be used for all species.
  private static final String SERVER_URL = "http://rest.ensembl.org";
  // Ensembl RESTful server to only target GRCh37 release of the human genome.
  private static final String H37_SERVER_URL = "http://grch37.rest.ensembl.org";
  
  //  ==== Service URLs
  // get an Ensembl ID by gene name
  private static final String GENE_TO_ENSG_SERVICE = "xrefs/symbol/@SPECIES@/@GENE_NAME@?object_type=gene";

  // note: in the following URLs, variant_type is not added here. It is done by
  // this.formatVariant() method
  // get Variants by Ensembl ID
  private static final String FETCH_VAR_SERVICE_BY_ENSID = "overlap/id/@ENSG_ID@?feature=@VARIANT_TYPE@";
  // get Variants by chromosomic region
  private static final String FETCH_VAR_SERVICE_BY_REGION = "overlap/region/@SPECIES@/@REGION@?feature=@VARIANT_TYPE@";
  // get Variant full entry
  private static final String LOAD_VAR_SERVICE = "variation/@SPECIES@/@VAR_ID@";
  // get VEP data
  private static final String LOAD_VEP_SERVICE = "vep/@SPECIES@/id/@VAR_ID@";

  // ==== For internal use
  /* These are keys located in above URLs. They are replaced at runtime with
   * appropriate values. */
  private static final String SPECIES_KEY = "@SPECIES@";
  private static final String GENE_NAME_KEY = "@GENE_NAME@";
  private static final String REGION_KEY = "@REGION@";
  private static final String ENSG_ID_KEY = "@ENSG_ID@";
  private static final String VAR_ID_KEY = "@VAR_ID@";
  private static final String VARIANT_TYPE_KEY = "@VARIANT_TYPE@";
  
  private String _defaultServer;
  
  /**
   * Constructor.
   */
  public EnsemblServerConfiguration(){
    _defaultServer = SERVER_URL;
  }
  
  /**
   * Constructor.
   * 
   * @param useGRCh37
   *          set to true to use GRCh37 release of the human genome. Of course,
   *          this constructor is only useful when playing with human genome
   *          data.
   */
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
    return 0;
  }

  @Override
  public int getSleepTimeBetweenRun() {
    return 5000;
  }

  @Override
  public int getLettersPerRun() {
    return 0;
  }

  @Override
  public boolean isServerAvailable() {
    return HTTPBasicEngine.isServerAvailable(_defaultServer);
  }

  /**
   * Return a URL to retrieve an Ensembl Gene ID by gene name.
   * 
   * @param species
   *          the species name
   * @param gene_name
   *          the gene name
   * @return a URL properly formatted to query Ensembl XRefs Service.
   */
  public String getGene2EnsgIdUrl(String species, String gene_name){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+GENE_TO_ENSG_SERVICE;
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(GENE_NAME_KEY, gene_name);
    return str;
  }
  
  /**
   * Return a URL to retrieve known Variants given an Ensembl Gene ID.
   * 
   * @param ensg_id
   *          official Ensembl Gene ID
   * @param type
   *          the Ensembl Variant Type to retrieve
   * 
   * @return a URL properly formatted to query Ensembl Variant DB.
   */
  public String getFetchVariationUrl(String ensg_id, EnsemblVariantType type){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+FETCH_VAR_SERVICE_BY_ENSID;
    
    str = str.replaceAll(ENSG_ID_KEY, ensg_id);
    str = formatVariant(str, type);
    return str;
  }

  /**
   * Return a URL to retrieve known Variants within a particular region.
   * 
   * @param species
   *          the species name
   * @param region
   *          the region for which to retrieve known variants. Expected format is
   *          A:B-C with A, B and C being chromosome number, start location and
   *          stop location, respectively.
   * @param type
   *          the Ensembl Variant Type to retrieve
   * 
   * @return a URL properly formatted to query Ensembl Variant DB.
   */
  public String getFetchVariationUrl(String species, String region, EnsemblVariantType type){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+FETCH_VAR_SERVICE_BY_REGION;
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(REGION_KEY, region);
    str = formatVariant(str, type);
    return str;
  }

  /**
   * Return a URL to retrieve data of a particular variant.
   * 
   * @param species the species name 
   * @param id the variant ID
   * 
   * @return a URL properly formatted to query Ensembl Variant DB.
   */
  public String getLoadVariantURL(String species, String id){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+LOAD_VAR_SERVICE;
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(VAR_ID_KEY, id);
    return str;
  }
  
  /**
   * Return a URL to retrieve VEP data.
   * 
   * @param species the species name 
   * @param id the variant ID
   * 
   * @return a URL properly formatted to query Ensembl VEP service
   */
  public String getLoadVepURL(String species, String id){
    String str;
    
    str = EZFileUtils.terminateURL(_defaultServer)+LOAD_VEP_SERVICE;
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(VAR_ID_KEY, id);
    return str;
  }

  /**
   * Format a URL to retrieve variants given a variant set type.
   * 
   * @param url the url to update. 
   * @param type type of the variant set.
   * 
   * @return an update URLproperly formatted to query Ensembl Variant DB
   */
  private String formatVariant(String url, EnsemblVariantType type){
    // Documentation to understand this code:
    //  http://rest.ensembl.org/documentation/info/overlap_id
    //  http://www.ensembl.org/info/genome/variation/data_description.html#variation_sets
    //  https://www.biostars.org/p/230261/
    
    return url.replaceAll(VARIANT_TYPE_KEY, type.getFormatString());
  }
}
