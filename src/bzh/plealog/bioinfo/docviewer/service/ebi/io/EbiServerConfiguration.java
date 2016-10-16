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
package bzh.plealog.bioinfo.docviewer.service.ebi.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.docviewer.api.QueryEngineException;
import bzh.plealog.bioinfo.docviewer.api.ServerConfiguration;
import bzh.plealog.bioinfo.docviewer.http.HTTPBasicEngine;
import bzh.plealog.bioinfo.docviewer.ui.DocViewerConfig;

public class EbiServerConfiguration implements ServerConfiguration{
  //adding new fields imply updating copy constructor
  private Hashtable<String, String> _summaryUrls;
  private int _seqPerRun = 100;
  private int _sleepTimePerRun = 1000; // use milliseconds internally
  private int _maxLetterPerRun = 5000000;

  //name of resource containing URL templates used to address EBI REST services
  private static final String CONF_RESOURCE = "ebiQuery.cfg";
  private static final String URL_SUFFIX_TO_LOAD = ".url";
  private static final String SUM_URL_SUFFIX = ".summary.url";
  private static final String FETCH_FAS_URL = "fetch_fasta.url";
  private static final String FETCH_FULL_URL = "fetch_full.url";
  private static final String SEQ_RUN_KEY = "sequences.per.run";
  private static final String LETTERS_SIZE_KEY = "letters.per.run";
  private static final String SEQ_SLEEP_KEY = "sleep.per.run"; // use seconds in

  private static final String LOAD_ERR = "Load EBI configuration from: %s";
  private static final String CONF_ERR = "EBI configuration resource not found: %s";
      
  /**
   * Constructor. Load automatically resource file called ebiQuery.conf located
   * next to this class. 
   */
  public EbiServerConfiguration(){
    prepareConfiguration(CONF_RESOURCE);
  }

  /**
   * Copy constructor.
   */
  public EbiServerConfiguration(EbiServerConfiguration srcConfig){
    _summaryUrls = new Hashtable<String, String>();
    for (String key : srcConfig._summaryUrls.keySet()){
      _summaryUrls.put(key, srcConfig._summaryUrls.get(key));
    }
  }

  private void prepareConfiguration(String resName) throws QueryEngineException {
    InputStream         in = null;
    Properties          props;
    String              key,  str;
    Enumeration<Object> keys;
    File                f;
    
    try {
      // first, try to locate the file in the user conf dir
      str = DocViewerConfig.getConfigurationPath();
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
        EZLogger.debug(String.format(LOAD_ERR, EbiQueryEngine.class.getResource(resName).toString()));
        in = EbiQueryEngine.class.getResourceAsStream(resName);
      }
      //not good
      if (in == null)
        throw new Exception(String.format(CONF_ERR,CONF_RESOURCE));
      
      //ok, load the configuration
      props = new Properties();
      props.load(in);
      _summaryUrls = new Hashtable<>();
      keys = props.keys();
      while(keys.hasMoreElements()){
        key = keys.nextElement().toString();
        if (key.endsWith(URL_SUFFIX_TO_LOAD)){
          str = props.getProperty(key);
          EZLogger.debug(String.format("%s = %s", key, str));
          _summaryUrls.put(key, str);
        }
        else if (key.equals(SEQ_RUN_KEY)){
          str = props.getProperty(key);
          EZLogger.debug(String.format("%s = %s", key, str));
          _seqPerRun = Integer.valueOf(str);
        }
        else if (key.equals(LETTERS_SIZE_KEY)){
          str = props.getProperty(key);
          EZLogger.debug(String.format("%s = %s", key, str));
          _maxLetterPerRun = Integer.valueOf(str);
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
      throw new QueryEngineException("unable to init EBI Query System: " + resName + ": " + ex);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ex) {
        }
      }
    }
  }

  //public services usually require these fields. 
  private String addServiceRequirements(String url) {
    StringBuffer buf = new StringBuffer(url);

    buf.append("&tool=");
    buf.append(EZApplicationBranding.getAppName()+"-"+EZApplicationBranding.getAppVersion());
    buf.append("&email=");
    buf.append(DocViewerConfig.getUserEmail());
    return buf.toString();
  }

  public String getSummaryUrl(String query, String dbCode, int from, int nb){
    String str;
    str = _summaryUrls.get(dbCode+SUM_URL_SUFFIX);
    if(str==null)
      return null;
    str = str.replaceAll("@TERM@", query);
    str = str.replaceAll("@DB@", dbCode);
    str = str.replaceAll("@FROM@", String.valueOf(from));
    str = str.replaceAll("@NB@", String.valueOf(nb));
    return addServiceRequirements(str);
  }

  public String getFetchUrl(String dbCode, String ids, boolean fastaFormat){
    String str;
    if (fastaFormat)
      str = _summaryUrls.get(FETCH_FAS_URL);
    else
      str = _summaryUrls.get(FETCH_FULL_URL);
    if (str==null)
      return null;
    str = str.replaceAll("@DB@", dbCode);
    str = str.replaceAll("@ID@", ids.trim());
    return addServiceRequirements(str);

  }

  /**
   * Figures out whether or not remote server is available.
   */
  public boolean isServerAvailable() {
    String service=null;
    URL    url;
    boolean available = false;
    
    //locate a summary url
    for(String key : _summaryUrls.keySet()){
      if (key.endsWith(SUM_URL_SUFFIX)){
        service=_summaryUrls.get(key);
        break;
      }
    }
    //at least one summary service must be available
    if (service==null)
      return false;
    try {
      //the following may throw a MalformedUrlException
      url = new URL(service);
      url = new URL(String.format("%s://%s", url.getProtocol(), url.getHost()));
      available = HTTPBasicEngine.isServerAvailable(url.toString());
    } catch (Exception e) {
      EZLogger.warn(e.toString());
    }
    //so, if we hit this line, server is available
    return available;

  }

  @Override
  public int getSequencesPerRun() {
    return _seqPerRun;
  }

  @Override
  public int getSleepTimeBetweenRun() {
    return _sleepTimePerRun;
  }

  @Override
  public int getLettersPerRun() {
    return _maxLetterPerRun;
  }

}
