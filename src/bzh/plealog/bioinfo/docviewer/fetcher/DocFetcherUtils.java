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
package bzh.plealog.bioinfo.docviewer.fetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;

import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.api.log.EZLoggerManager;
import com.plealog.genericapp.api.log.EZLoggerManager.LogLevel;
import com.plealog.genericapp.ui.common.Utils;

/**
 * This class contains some utility methods used by the sequence retrieval
 * processor.
 * 
 * @author Patrick G. Durand
 */
public class DocFetcherUtils {

  private static final String ERR_1 = "unable to retrieve sequences";
  private static final String ERR_2 = "Server does not return expected number of sequences:\n%d instead of %d.";
  private static final String ERR_3 = "Unable to append files.";
  
  protected static final String ERR_4 = "retry (%s): %d/%d";
  protected static final int RETRY = 3;
  protected static final int SLEEP_TIME_RETRY = 5000;

  /**
   * Sometimes the NCBI server returns an error message within an HTML page.
   * Detect this and return false in that case. We also check that we have the
   * valid number of sequences.
   */
  private static int getNbSequenceInFile(File f, QueryEngine sLoader, boolean fullEntryFormat) {

    String line, seed;
    int counter = 0;

    if (f.length() < 5l) {// empty file (5: takes into account some space/return
                          // chars)
      return 0;
    }
    if (fullEntryFormat) {
      switch (sLoader.getBankType().getReaderType()) {
      case EMBL:
      case UNIPROT:
        seed = "ID";
        break;
      case GENPEPT:
      case GENBANK:
        seed = "LOCUS";
        break;
      case FASTADNA:
      case FASTAPROT:
        seed = ">";
        break;
      default:
        return 0;
      }
    } else {
      seed = ">";
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
      while ((line = reader.readLine()) != null) {
        if (line.length() == 0)
          continue;
        if (line.startsWith(seed)) {
          counter++;
        }
      }
    } catch (Exception e) {
      EZLogger.warn(e.toString());
      return 0;
    }

    return counter;
  }

  private static void deleteTempFile(File tmpSeqFile){
    if(EZLoggerManager.getLevel()!=LogLevel.debug){
      // no debug mode: do not keep temp file, even if the underlying code that generated
      // that file (HTTPBasicEngine actually) has set deleteOnExit flag.
      tmpSeqFile.delete();
    }
  }
  /**
   * Utility method. Check content of tmpSeqFile to ensure that we have indeed ndocs sequences. 
   * If ok then merge tmpSeqFile with seqFile and return null. Return a not null string if error.
   */
  private static String processFile(File tmpSeqFile, File seqFile, QueryEngine sLoader, int ndocs,
      boolean fullEntryFormat) {
    String errMsg = null;
    
    int counter = getNbSequenceInFile(tmpSeqFile, sLoader, fullEntryFormat);
    if (counter != ndocs) {
      deleteTempFile(tmpSeqFile);
      return String.format(ERR_2, counter, ndocs);
    }
    if (!Utils.appendFiles(tmpSeqFile.getAbsolutePath(), seqFile.getAbsolutePath(), true)){
      errMsg = ERR_3;
    }
    deleteTempFile(tmpSeqFile);
    return errMsg;
  }

  /**
   * Utility method. Download a set of sequences.
   */
  private static void processLoading(LoadingShuttle shuttle, String ids, String dbCode, QueryEngine sLoader,
      SeqRetrieverMonitor monitor, boolean fullEntryFormat) {
    boolean bError = false;
    int counter = 0;
    File tmpSeqFile = null;

    shuttle.tmpSeqFile = null;
    shuttle.status = LoadingShuttle.STATUS.PROGRESS;

    while (counter < RETRY) {
      bError = false;
      try {
        tmpSeqFile = sLoader.load(ids, dbCode, fullEntryFormat);
        shuttle.tmpSeqFile = tmpSeqFile;
        shuttle.status = LoadingShuttle.STATUS.OK;
        break;
      } catch (Exception e1) {
        bError = true;
        EZLogger.warn(e1.toString());
      }
      if (monitor.interruptProcessing()) {
        shuttle.status = LoadingShuttle.STATUS.INTERRUPTED;
        break;
      }
      // if error, retry at most three times to get some data
      counter++;
      EZLogger.info(String.format(ERR_4, "F", counter, RETRY));
      try {
        Thread.sleep(SLEEP_TIME_RETRY);
      } catch (InterruptedException e) {
      }
    }
    if (bError) {
      shuttle.status = LoadingShuttle.STATUS.ERROR;
    }
  }


  /**
   * Save a set of sequences in a file. All parameters are mandatory.
   * 
   * @param seqFile
   *          the fasta file where to save the sequences
   * @param sLoader
   *          the SequenceLoader system object used to query a remote server
   * @param docs
   *          the list of documents to retrieve from the server
   * @param dbCode
   *          the database code to query on the remote server
   * @param monitor
   *          the monitor used to listener to the sequence retrieval engine
   * 
   * @return null if success, otherwise an error message.
   */
  public static String doFetchJob(File seqFile, QueryEngine sLoader, List<DocSum> docs, String dbCode,
      SeqRetrieverMonitor monitor, boolean fullEntryFormat) {
    LoadingShuttle shuttle;
    StringBuffer buf;
    String str;
    Iterator<DocSum> iter;
    DocSum ds;
    int totSize, steps, idProcessed, ndocs;

    shuttle = new LoadingShuttle();
    totSize = steps = idProcessed= 0;
    buf = new StringBuffer();
    iter = docs.iterator();

    EZLogger.debug(String.format("Letters per run: %d", sLoader.getServerConfiguration().getLettersPerRun()));
    // given a set of IDs, try to load set of sequences by set of
    // sLoader.getServerConfiguration().getLettersPerRun() letters max.
    // This was added to handle genomes: even a few IDs may correspond
    // to a huge amount of sequence data.
    while (iter.hasNext()) {
      if (monitor.interruptProcessing())
        return null;
      // append seqID to retrieve...
      ds = iter.next();
      buf.append(ds.id);
      totSize += ds.length;
      steps++;
      // ... and check whether or not these set of IDs refer to an amount of
      // sequence letters exceeding server configuration
      if (totSize >= sLoader.getServerConfiguration().getLettersPerRun()) {
        // if yes: load sequences
        str=buf.toString();
        ndocs=StringUtils.countMatches(str, ",")+1;
        EZLogger.debug("page: "+str);
        processLoading(shuttle, str, dbCode, sLoader, monitor, fullEntryFormat);
        // check what happened
        if (shuttle.status.equals(LoadingShuttle.STATUS.ERROR)) {
          return ERR_1;
        } else if (shuttle.status.equals(LoadingShuttle.STATUS.INTERRUPTED)) {
          return null;
        }
        // check tmpSeqFile content
        str = processFile(shuttle.tmpSeqFile, seqFile, sLoader, ndocs, fullEntryFormat);
        if (str != null) {
          // something wrong?
          return str;
        }
        idProcessed+=ndocs;
        // proceed with remaining seqIDs...
        monitor.addSteps(steps);
        buf = new StringBuffer();
        totSize = steps = 0;
        // since we have made a successful call to remote server: wait for next batch
        // to conform to recommendations of remote public service (NCBI, EBI, etc.)
        try {
          EZLogger.debug(String.format("wait: %d ms", sLoader.getServerConfiguration().getSleepTimeBetweenRun()));
          Thread.sleep(sLoader.getServerConfiguration().getSleepTimeBetweenRun());
        } catch (InterruptedException e) {
        }
      } else {
        if (iter.hasNext() && totSize != 0) {
          buf.append(",");
        }
      }
    }
    if (monitor.interruptProcessing())
      return null;
    // purge buffer if needed
    if (buf.length() != 0) {
      // if yes: load sequences
      str=buf.toString();
      ndocs=StringUtils.countMatches(str, ",")+1;
      EZLogger.debug("purge: "+str);
      processLoading(shuttle, str, dbCode, sLoader, monitor, fullEntryFormat);
      // check what happened
      if (shuttle.status.equals(LoadingShuttle.STATUS.ERROR)) {
        return ERR_1;
      } else if (shuttle.status.equals(LoadingShuttle.STATUS.INTERRUPTED)) {
        return null;
      }
      // check tmpSeqFile content
      str = processFile(shuttle.tmpSeqFile, seqFile, sLoader, ndocs, fullEntryFormat);
      if (str != null) {
        // something wrong?
        return str;
      }
      idProcessed+=ndocs;
    }
    //final check: do we have downloaded all requested sequences
    ndocs=docs.size();
    if(idProcessed!=ndocs){
      return String.format(ERR_2, idProcessed, ndocs);
    }
    else{
      return null;
    }
  }

  private static class LoadingShuttle {
    enum STATUS {
      OK, INTERRUPTED, ERROR, PROGRESS
    }
    File tmpSeqFile;
    STATUS status;
  }

  public static class DocSum {
    String id;
    int length;

    public DocSum(String id, int length) {
      super();
      this.id = id;
      this.length = length;
    }
  }

}
