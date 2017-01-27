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
package bzh.plealog.bioinfo.docviewer.service.ncbi.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.configuration.DirectoryManager;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.Utils;

import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.config.DocViewerDirectoryType;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.util.CoreUtil;

/**
 * This class stores the Sequence server configuration to be used by a client of
 * a Sequence server. This one is dedicated to use the NCBI server
 * (configuration is stored in file &apos;ncbiSequenceServer.config&apos;.
 *
 * @author Patrick G. Durand
 */
public class EntrezServerConfiguration implements ServerConfiguration{
  private static final String FASTA_SERVICE = "fetch_fasta.url";
  private static final String ENTRY_SERVICE = "fetch_full.url";
  private static final String QUERY_URL_KEY = "query.url";
  private static final String SUM_URL_KEY = "summary.url";
  private static final String SEQ_RUN_KEY = "sequences.per.run";
  private static final String LETTERS_SIZE_KEY = "letters.per.run";
  private static final String SEQ_SLEEP_KEY = "sleep.per.run"; // use seconds in

  private static final String CONF_RESOURCE = "ncbiQuery.cfg";

  private static final String LOAD_ERR = "Load NCBI configuration from: %s";
  private static final String CONF_ERR = "NCBI configuration resource not found: %s";

  //adding new fields imply updating copy constructor
  private String _fastaService;
  private String _entryService;
  private String _queryService;
  private String _summaryService;
  private int _seqPerRun = 100;
  private int _sleepTimePerRun = 1000; // use milliseconds internally
  private int _maxLetterPerRun = 5000000;

  /**
   * Constructor. Load automatically resource file called ncbiQuery.conf located
   * next to this class. 
   */
  public EntrezServerConfiguration() {
    Map<String, Object> conf;

    try {
      conf = readConfiguration();
      _fastaService = conf.get(FASTA_SERVICE).toString();
      EZLogger.debug(String.format("%s = %s", FASTA_SERVICE, _fastaService));
      _entryService = conf.get(ENTRY_SERVICE).toString();
      EZLogger.debug(String.format("%s = %s", ENTRY_SERVICE, _entryService));
      _queryService = conf.get(QUERY_URL_KEY).toString();
      EZLogger.debug(String.format("%s = %s", QUERY_URL_KEY, _queryService));
      _summaryService = conf.get(SUM_URL_KEY).toString();
      EZLogger.debug(String.format("%s = %s", SUM_URL_KEY, _summaryService));
      _seqPerRun = ((Integer) conf.get(SEQ_RUN_KEY)).intValue();
      EZLogger.debug(String.format("%s = %d", SEQ_RUN_KEY, _seqPerRun));
      _sleepTimePerRun = ((Integer) conf.get(SEQ_SLEEP_KEY)).intValue();
      EZLogger.debug(String.format("%s = %d", SEQ_SLEEP_KEY, _sleepTimePerRun));
      _maxLetterPerRun = ((Integer) conf.get(LETTERS_SIZE_KEY)).intValue();
      EZLogger.debug(String.format("%s = %d", LETTERS_SIZE_KEY, _maxLetterPerRun));
    } catch (Exception ex) {
      EZLogger.warn(ex.toString());
    }
  }

  /**
   * Copy constructor.
   */
  public EntrezServerConfiguration(EntrezServerConfiguration srcConfig) {
    this._entryService = srcConfig._entryService;
    this._fastaService = srcConfig._fastaService;
    this._entryService = srcConfig._entryService;
    this._queryService = srcConfig._queryService;
    this._summaryService = srcConfig._summaryService;
    this._seqPerRun = srcConfig._seqPerRun;
    this._sleepTimePerRun = srcConfig._sleepTimePerRun;
    this._maxLetterPerRun = srcConfig._maxLetterPerRun;
  }
  
  /**
   * Load and control values provided by resource file.
   */
  private Map<String, Object> readConfiguration() {
    Hashtable<String, Object> conf;
    Object value;
    ResourceBundle rb;
    InputStream in=null;
    String str;
    File f;
    
    conf = new Hashtable<>();
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
        EZLogger.debug(String.format(LOAD_ERR, EntrezQueryEngine.class.getResource(CONF_RESOURCE).toString()));
        in = EntrezQueryEngine.class.getResourceAsStream(CONF_RESOURCE);
      }
      //not good
      if (in == null)
        throw new Exception(String.format(CONF_ERR,CONF_RESOURCE));
      
      rb = new PropertyResourceBundle(in);
      value = Utils.getString(rb, FASTA_SERVICE);
      if (!value.equals(Utils.UNKNOWNSTRING))
        conf.put(FASTA_SERVICE, value);
      value = Utils.getString(rb, ENTRY_SERVICE);
      if (!value.equals(Utils.UNKNOWNSTRING))
        conf.put(ENTRY_SERVICE, value);
      value = Utils.getString(rb, QUERY_URL_KEY);
      if (!value.equals(Utils.UNKNOWNSTRING))
        conf.put(QUERY_URL_KEY, value);
      value = Utils.getString(rb, SUM_URL_KEY);
      if (!value.equals(Utils.UNKNOWNSTRING))
        conf.put(SUM_URL_KEY, value);
      value = Utils.getInteger(rb, SEQ_RUN_KEY);
      if (!value.equals(Utils.UNKNOWNINTEGER))
        conf.put(SEQ_RUN_KEY, value);
      value = Utils.getInteger(rb, SEQ_SLEEP_KEY);
      if (!value.equals(Utils.UNKNOWNINTEGER))
        conf.put(SEQ_SLEEP_KEY, value);
      value = Utils.getInteger(rb, LETTERS_SIZE_KEY);
      if (!value.equals(Utils.UNKNOWNINTEGER))
        conf.put(LETTERS_SIZE_KEY, value);

    } catch (Exception e) {
      //should not happen unless resource file is not available
      EZLogger.warn(Messages.getString("SequenceServerConfig.12") + ": " + e);
    }
    finally{
      try{if(in!=null){in.close();}}catch(Exception e){}
    }
    return conf;
  }

  /**
   * Returns the service URL to get IDs given an ENtrez query.
   *
   * @param query
   *          if the tag TERM exists in the URL and if query is not null, then the
   *          tag is replaced by the value of query.
   * @param dbCode
   *          if the tag DB exists in the URL and if dbCode is not null, then
   *          the tag is replaced by the value of dbCode.
   * @param from
   *          if the tag FROM exists in the URL, then
   *          the tag is replaced by the value of from.
   * @param nb
   *          if the tag NB exists in the URL, then
   *          the tag is replaced by the value of nb.
   */
  public String getQueryServiceUrl(String query, String dbCode, int from, int nb){
    String str;
    if (dbCode != null)
      str = CoreUtil.replaceAll(_queryService, "@DB@", dbCode);
    else
      str = _queryService;
    if (query != null)
      str = CoreUtil.replaceAll(str, "@TERM@", query);

    str = str.replaceAll("@FROM@", String.valueOf(from));
    str = str.replaceAll("@NB@", String.valueOf(nb));
    return addServiceRequirements(str);
  }

  /**
   * Returns the service URL to get document summaries.
   *
   * @param dbCode
   *          if the tag DB exists in the URL and if dbCode is not null, then
   *          the tag is replaced by the value of dbCode.
   * @param ids
   *          if the tag ID exists in the URL and if ids is not null, then the
   *          tag is replaced by the value of id.
   *
   */
  public String getSummaryServiceUrl(String dbCode, String ids){
    String str;
    if (dbCode != null)
      str = CoreUtil.replaceAll(_summaryService, "@DB@", dbCode);
    else
      str = _summaryService;
    if (ids != null)
      str = CoreUtil.replaceAll(str, "@ID@", ids);

    return addServiceRequirements(str);
  }

  
  /**
   * Returns the Sequence server URL for the Fasta service.
   *
   * @param dbCode
   *          if the tag DB exists in the URL and if dbCode is not null, then
   *          the tag is replaced by the value of dbCode.
   * @param id
   *          if the tag ID exists in the URL and if id is not null, then the
   *          tag is replaced by the value of id.
   *
   */
  public String getFastaServiceURL(String dbCode, String id) {
    String str;
    if (dbCode != null)
      str = CoreUtil.replaceAll(_fastaService, "@DB@", dbCode);
    else
      str = _fastaService;
    if (id != null)
      str = CoreUtil.replaceAll(str, "@ID@", id);
    return addServiceRequirements(str);
  }

  /**
   * Returns the Sequence server URL for the Entry service.
   *
   * @param dbCode
   *          if the tag DB exists in the URL and if dbCode is not null, then
   *          the tag is replaced by the value of dbCode.
   * @param id
   *          if the tag ID exists in the URL and if id is not null, then the
   *          tag is replaced by the value of id.
   *
   */
  public String getEntryServiceURL(String dbCode, String id) {
    String str;
    if (dbCode != null)
      str = CoreUtil.replaceAll(_entryService, "@DB@", dbCode);
    else
      str = _entryService;
    if (id != null)
      str = CoreUtil.replaceAll(str, "@ID@", id);
    
    str = CoreUtil.replaceAll(str, "@TYPE@", dbCode.equals("protein") ? "gp" : "gb");
      
    return addServiceRequirements(str);
  }

  /**
   * Figures out whether or not remote server is available.
   */
  public boolean isServerAvailable() {
    URL url=null;
    boolean available = false;
    try {
      //the following may throw a MalformedUrlException
      url = new URL(_queryService);
      url = new URL(String.format("%s://%s", url.getProtocol(), url.getHost()));
      available = HTTPBasicEngine.isServerAvailable(url.toString());
    } catch (Exception e) {
      EZLogger.warn(e.toString());
    }
    //so, if we hit this line, server is available
    return available;
  }

  /**
   * Returns the max number of sequences than can be retrieved for each run.
   */
  public int getSequencesPerRun() {
    return _seqPerRun;
  }

  public void setSequencesPerRun(int val) {
    _seqPerRun = val;
  }

  /**
   * Returns the sleep time to use between two retrieval runs. Return value is
   * in milliseconds.
   */
  public int getSleepTimeBetweenRun() {
    return _sleepTimePerRun;
  }

  public void setSleepTimeBetweenRun(int val) {
    _sleepTimePerRun = val;
  }

  /**
   * Returns the maximal number of letters to retrieve per run.
   */
  public int getLettersPerRun() {
    return _maxLetterPerRun;
  }

  public void setLettersPerRun(int letterPerRun) {
    _maxLetterPerRun = letterPerRun;
  }

  private String addServiceRequirements(String str) {
    StringBuffer buf = new StringBuffer(str);
    buf.append("&tool=");
    buf.append(EZApplicationBranding.getAppName()+"-"+EZApplicationBranding.getAppVersion());
    buf.append("&email=");
    buf.append(DocViewerConfig.getUserEmail());
    return buf.toString();
  }
}
