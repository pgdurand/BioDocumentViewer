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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import com.plealog.genericapp.api.configuration.DirectoryManager;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.config.DocViewerDirectoryType;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;

/**
 * Setup the configuration to query Ensembl RESTful API. This class loads its
 * configuration from a file called <i>ensemblQuery.cfg</i>. By default that file
 * is loaded either from directory
 * <i>${userHome}/.${EZApplicationBranding.getAppName()}/conf</i> if it exists,
 * of from the file located next to this class, otherwise. Configuration file
 * lookup is done following this order: <i>conf</i> directory first, Java
 * package, otherwise.
 * 
 * @author Patrick G. Durand
 */
public class EnsemblServerConfiguration implements ServerConfiguration {
  //adding new fields imply updating copy constructor
  private Hashtable<String, String> _summaryUrls;
  private int _sleepTimePerRun = 1000; // use milliseconds internally
  private boolean _useH37assembly;
  
  //  ==== Resource containing URL templates
  private static final String CONF_RESOURCE = "ensemblQuery.cfg";
  
  //  ==== Server address
  // Standard Ensembl RESTful server. Can be used for all species.
  private static final String SERVER_URL = "ensembl.server.address";
  // Ensembl RESTful server to only target GRCh37 release of the human genome.
  private static final String H37_SERVER_URL = "h37.ensembl.server.address";
  
  //  ==== Service for Variants
  // get an Ensembl ID by gene name
  private static final String GENE_TO_ENSG_SERVICE = "gene2ensg.service";
  // get Variants by Ensembl ID
  private static final String FETCH_VAR_SERVICE_BY_ENSID = "fetch.var.ensid.service";
  // get Variants by chromosomic region
  private static final String FETCH_VAR_SERVICE_BY_REGION = "fetch.var.region.service";
  // get Variant full entry
  private static final String LOAD_VAR_SERVICE = "load.var.service";
  // get VEP data
  private static final String LOAD_VEP_SERVICE = "loag.vep.service";

  
  //  ==== Service for Transcripts
  // get Variants by chromosomic region
  private static final String FETCH_RNA_SERVICE_BY_ENSID = "fetch.rna.ensid.service";
  private static final String FETCH_RNA_SERVICE_BY_REGION = "fetch.rna.region.service";

  
  // ==== For internal use
  /* These are keys located in above URLs. They are replaced at runtime with
   * appropriate values. */
  private static final String SPECIES_KEY = "@SPECIES@";
  private static final String GENE_NAME_KEY = "@GENE_NAME@";
  private static final String REGION_KEY = "@REGION@";
  private static final String ENSG_ID_KEY = "@ENSG_ID@";
  private static final String VAR_ID_KEY = "@VAR_ID@";
  private static final String VARIANT_TYPE_KEY = "@VARIANT_TYPE@";
  
  private static final String SEQ_SLEEP_KEY = "sleep.per.run"; // use seconds in
  
  private static final String LOAD_ERR = "Load Ensembl configuration from: %s";
  private static final String CONF_ERR = "Ensembl configuration resource not found: %s";

  /**
   * Constructor.
   */
  public EnsemblServerConfiguration(){
    this(false);
    //_defaultServer = SERVER_URL;
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
    _useH37assembly = useGRCh37;
    prepareConfiguration(CONF_RESOURCE);
  }
  
  /**
   * Copy constructor.
   */
  public EnsemblServerConfiguration(EnsemblServerConfiguration srcConfig){
    _summaryUrls = new Hashtable<String, String>();
    for (String key : srcConfig._summaryUrls.keySet()){
      _summaryUrls.put(key, srcConfig._summaryUrls.get(key));
    }
    _sleepTimePerRun = srcConfig._sleepTimePerRun;
    _useH37assembly = srcConfig._useH37assembly;
  }

  private void prepareConfiguration(String resName) throws QueryEngineException {
    InputStream         in = null;
    Properties          props;
    String              key,  str;
    Enumeration<Object> keys;
    File                f;
    
    try {
      // first, try to locate the file in the user conf dir
      str = DirectoryManager.getPath(DocViewerDirectoryType.CONF);
      if (str!=null){
        str += CONF_RESOURCE;
        f = new File(str);
        if (f.exists()){
          EZLogger.debug(String.format(LOAD_ERR, f.getAbsolutePath()));
          in = new FileInputStream(f);
        }
      }
      //try from software resource
      if (in==null){
        EZLogger.debug(String.format(LOAD_ERR, EnsemblQueryEngine.class.getResource(resName).toString()));
        in = EnsemblQueryEngine.class.getResourceAsStream(resName);
      }
      //not good
      if (in == null)
        throw new Exception(String.format(CONF_ERR,CONF_RESOURCE));
      
      //ok, load the configuration
      props = new Properties();
      props.load(in);
      _summaryUrls = new Hashtable<>();
      
      String serverAddr;
      
      if (_useH37assembly){
        serverAddr = props.getProperty(H37_SERVER_URL);
      }
      else{
        serverAddr = props.getProperty(SERVER_URL);
      }
      _summaryUrls.put(SERVER_URL, serverAddr);
      serverAddr = EZFileUtils.terminateURL(serverAddr);
      keys = props.keys();
      while(keys.hasMoreElements()){
        key = keys.nextElement().toString();
        if (key.equals(GENE_TO_ENSG_SERVICE)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(FETCH_VAR_SERVICE_BY_ENSID)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(FETCH_VAR_SERVICE_BY_REGION)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(LOAD_VAR_SERVICE)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(LOAD_VEP_SERVICE)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(FETCH_RNA_SERVICE_BY_ENSID)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(FETCH_RNA_SERVICE_BY_REGION)){
          manageServiceUrl(props, key, serverAddr);
        }
        else if (key.equals(SEQ_SLEEP_KEY)){
          str = props.getProperty(key);
          EZLogger.debug(String.format("%s = %s", key, str));
          _sleepTimePerRun = Integer.valueOf(str);
        }
      }
      if (_summaryUrls.isEmpty())
        throw new Exception("summary URLs not found");
    } catch (Exception ex) {
      throw new QueryEngineException("unable to init Ensembl Query System: " + resName + ": " + ex);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ex) {
        }
      }
    }
  }

  private void manageServiceUrl(Properties props, String key, String serverAddr){
    String str = props.getProperty(key).trim();
    if (str.charAt(0)=='/'){
      str = str.substring(1);
    }
    EZLogger.debug(String.format("%s = %s", key, str));
    _summaryUrls.put(key, serverAddr+str);
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
    return HTTPBasicEngine.isServerAvailable(_summaryUrls.get(SERVER_URL));
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
    
    str = _summaryUrls.get(GENE_TO_ENSG_SERVICE);
    
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
    
    str = _summaryUrls.get(FETCH_VAR_SERVICE_BY_ENSID);
    
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
    
    str = _summaryUrls.get(FETCH_VAR_SERVICE_BY_REGION);
    
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
    
    str = _summaryUrls.get(LOAD_VAR_SERVICE);
    
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
    
    str = _summaryUrls.get(LOAD_VEP_SERVICE);
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(VAR_ID_KEY, id);
    return str;
  }

  /**
   * Return a URL to retrieve known Transcripts given an Ensembl Gene ID.
   * 
   * @param ensg_id
   *          official Ensembl Gene ID
   * 
   * @return a URL properly formatted to query Ensembl DB.
   */
  public String getFetchTranscriptUrl(String ensg_id){
    String str;
    
    str = _summaryUrls.get(FETCH_RNA_SERVICE_BY_ENSID);
    
    str = str.replaceAll(ENSG_ID_KEY, ensg_id);
    return str;
  }

  /**
   * Return a URL to retrieve known Transcripts within a particular region.
   * 
   * @param species
   *          the species name
   * @param region
   *          the region for which to retrieve known variants. Expected format is
   *          A:B-C with A, B and C being chromosome number, start location and
   *          stop location, respectively.
   * 
   * @return a URL properly formatted to query Ensembl DB.
   */
  public String getFetchTranscriptUrl(String species, String region){
    String str;
    
    str = _summaryUrls.get(FETCH_VAR_SERVICE_BY_REGION);
    
    str = str.replaceAll(SPECIES_KEY, species);
    str = str.replaceAll(REGION_KEY, region);
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
