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

import java.io.File;
import java.util.List;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

/**
 * This class is designed to automatically retrieve a set of sequences.
 * 
 * @author Patrick G. Durand
 */
public class DBSimpleFetcher extends Thread {
  private List<DocFetcherUtils.DocSum> docs;
  private String dbCode;
  private File fasta;
  private QueryEngine sLoader;
  private SeqRetrieverMonitor monitor;
  private boolean fullEntryFormat;

  /**
   * Constructor. Use this sequence fetcher only to download a few documents since it will
   * do the job directly without any time delay, etc. If you have to download large set
   * of documents, consider using the DBAutoFetcher: this one will handle to delay many
   * requests to service provide (i.e. remote server) using their recommendations. 
   * 
   * All parameters are mandatory.
   * 
   * @param fasta
   *          where to save the retrieved fasta formatted sequences
   * @param sLoader
   *          the Sequence Loader system
   * @param docs
   *          the sequences to retrieve. Size of this list should not be greater than
   *          DocViewerConfig.PAGE_SIZE. Otherwise, use DBAutoFetcher.
   * @param dbCode
   *          the database code to query on the server side
   * @param monitor
   *          the object used to monitor the fetching process
   */
  public DBSimpleFetcher(File fasta, QueryEngine sLoader, List<DocFetcherUtils.DocSum> docs, String dbCode,
      SeqRetrieverMonitor monitor, boolean fullEntryFormat) {
    this.docs = docs;
    this.dbCode = dbCode;
    this.fasta = fasta;
    this.sLoader = sLoader;
    this.monitor = monitor;
    this.fullEntryFormat = fullEntryFormat;
  }

  public void run() {
    monitor.startJob();
    EZLogger.debug(String.format("Sequences per run: %d", sLoader.getServerConfiguration().getSequencesPerRun()));
    if (fasta.exists()) {
      fasta.delete();
    }
    monitor.setTotSteps(docs.size());
    String errMsg = DocFetcherUtils.doFetchJob(fasta, sLoader, docs, dbCode, monitor, fullEntryFormat);
    if (errMsg != null) {
      monitor.setErrMsg(errMsg);
    }
    // we provide user with some information and let messages displayed
    // for a few seconds
    monitor.setCurSteps(docs.size());
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    monitor.setMessage(Messages.getString("DDXplorDocNavigator.lbl2"));
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }
    monitor.jobDone();
  }
}
