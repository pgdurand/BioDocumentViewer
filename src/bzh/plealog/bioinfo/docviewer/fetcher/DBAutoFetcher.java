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
import java.util.ArrayList;
import java.util.List;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.docviewer.api.QueryEngine;
import bzh.plealog.bioinfo.docviewer.api.Summary;
import bzh.plealog.bioinfo.docviewer.api.SummaryDoc;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

/**
 * This class is designed to automatically retrieve a set of sequences in batch
 * mode.
 * 
 * @author Patrick G. Durand
 */
public class DBAutoFetcher extends Thread {
  private QueryEngine _engine;
  private File _targetFile;
  private String _dbCode;
  private SeqRetrieverMonitor _monitor;
  private boolean _fullEntryFormat;

  /**
   * Constructor.
   * 
   * @param targetFile
   *          where to save the retrieved fasta formatted sequences
   * @param engine
   *          the query engine used to get sequences using the user query.
   * @param dbCode
   *          the database code to query on the server side
   * @param sLoader
   *          the Sequence Loader system
   * @param monitor
   *          the object used to monitor the fetching process
   */
  public DBAutoFetcher(File targetFile, QueryEngine engine, String dbCode,
      SeqRetrieverMonitor monitor, boolean fullEntryFormat) {
    _engine = engine;
    _targetFile = targetFile;
    _dbCode = dbCode;
    _monitor = monitor;
    _fullEntryFormat = fullEntryFormat;
  }

  private List<DocFetcherUtils.DocSum> getDocs(Summary curDocs) {
    ArrayList<DocFetcherUtils.DocSum> docs = new ArrayList<DocFetcherUtils.DocSum>();
    String data;

    for (SummaryDoc doc : curDocs.getDocs()) {
      data = doc.getValue(_engine.getBankType().getPresentationModel().getLengthFieldKey());
      docs.add(new DocFetcherUtils.DocSum(doc.getId(), (data != null ? Integer.valueOf(data) : 0)));
    }
    return docs;
  }

  public void run() {
    Summary res;
    String errMsg;
    int pageSize, nbDocs, totDocs, counter;
    boolean bFirst = true;

    _monitor.startJob();
    pageSize = _engine.getServerConfiguration().getSequencesPerRun();
    EZLogger.debug(String.format("Sequences per run: %d", pageSize));

    nbDocs = 0;
    totDocs = 1;
    if (_targetFile.exists()) {
      _targetFile.delete();
    }
    while (nbDocs < totDocs) {
      if (_monitor.interruptProcessing())
        break;
      if (_monitor.isPaused()) {
        try {
          Thread.sleep(_engine.getServerConfiguration().getSleepTimeBetweenRun());
        } catch (InterruptedException e) {
        }
      } else {
        counter = 0;
        res = null;
        while (counter < DocFetcherUtils.RETRY) {
          try {
            res = _engine.getSummary(nbDocs, pageSize);
          } catch (Exception e1) {
            EZLogger.warn(e1.toString());
          }
          if (_monitor.interruptProcessing())
            break;
          // ok ?
          if (res != null)
            break;
          // if error, retry at most three times to get some data
          counter++;
          EZLogger.info(String.format(DocFetcherUtils.ERR_4, "B", counter, DocFetcherUtils.RETRY));
          try {
            //when we are retrying to connect server, do wait a little more than server configuration
            EZLogger.debug(String.format("syswait: %d ms", DocFetcherUtils.SLEEP_TIME_RETRY));
            Thread.sleep(DocFetcherUtils.SLEEP_TIME_RETRY);
          } catch (InterruptedException e) {
          }
        }
        if (_monitor.interruptProcessing())
          break;
        if (res == null) {
          // _monitor.setErrMsg(Messages.getString("DatabaseOpener.lbl8"));
          EZLogger.warn(Messages.getString("DatabaseOpener.lbl8"));
          _monitor.pauseJob();
        } else {
          if (bFirst) {
            bFirst = false;
            totDocs = res.getTotal();
            _monitor.setTotSteps(totDocs);
          }
          errMsg = DocFetcherUtils.doFetchJob(_targetFile, _engine, getDocs(res), _dbCode, _monitor, _fullEntryFormat);

          nbDocs += res.nbDocs();
          _monitor.setCurSteps(nbDocs);

          if (errMsg != null) {
            EZLogger.warn("Error while retrieving sequence : " + errMsg);
            _monitor.pauseJob();
          }
        }
      }
    }
    _monitor.setCurSteps(nbDocs);
    // we provide user with some information and let messages displayed
    // for a few seconds
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    _monitor.setMessage(Messages.getString("DDXplorDocNavigator.lbl2"));
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }

    _monitor.jobDone();
  }
}
