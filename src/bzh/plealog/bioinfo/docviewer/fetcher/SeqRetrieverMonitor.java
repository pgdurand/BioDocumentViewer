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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import bzh.plealog.bioinfo.docviewer.api.DataListener;
import bzh.plealog.bioinfo.docviewer.conf.DocViewerConfig;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.ui.common.Utils;

/**
 * This class is used to graphically monitor the sequence retrieval process.
 *
 * @author Patrick G. Durand
 */
public abstract class SeqRetrieverMonitor extends JPanel implements DataListener {
  private static final long serialVersionUID = -8729975150506984769L;
  private JProgressBar _progressBar;
  private JLabel _msg;
  private JLabel _animIcon;
  private String errMsg;
  private JPanel mainPanel;
  private boolean jobRunning;
  private boolean cancelJob;
  private int totSteps;
  private int curSteps;
  private long totBytesRead;
  private boolean pauseJob = false;
  private long runningTime;
  
  private static final MessageFormat INFO_TXT = new MessageFormat(Messages.getString("SeqRetrieverMonitor.lbl2"));

  /**
   * Constructor.
   */
  public SeqRetrieverMonitor() {
    super();
    buildUI(totSteps);
  }

  /**
   * Reset this monitor so reuse it.
   */
  public void reset() {
    errMsg = null;
    jobRunning = false;
    cancelJob = false;
    totBytesRead = 0l;
    totSteps = curSteps = 0;
    _progressBar.setValue(0);
    _progressBar.setMaximum(0);
    _msg.setText("");
    _animIcon.setIcon(null);
  }

  /**
   * Call this method when a process monitored by this class is done. When no
   * error occurred during a process this method returns null. Otherwise it is
   * an error message.
   */
  public synchronized String getErrMsg() {
    return errMsg;
  }

  /**
   * Set an error message. This method is intended to be used by process tasks
   * to report an error.
   */
  public synchronized void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  /**
   * Set the running status of the monitor. This method is intended to be used
   * by process tasks monitored by this class.
   */
  private synchronized void setJobRunnig(boolean val) {
    jobRunning = val;
  }

  /**
   * Figures out if the process task monitored by this class is still running.
   */
  public synchronized boolean isJobRunning() {
    return jobRunning;
  }

  /**
   * This method can be called to cancel the process task monitored by this
   * class.
   */
  public void cancelJob() {
    _msg.setText(Messages.getString("SeqRetrieverMonitor.lbl1"));
    cancelJob = true;
  }

  /**
   * This method is called when the retrieve process can not access to the
   * server
   */
  public void pauseJob() {
    _msg.setText(_msg.getText() + " - " + Messages.getString("SeqRetrieverMonitor.pause"));
    pauseJob = true;
    _animIcon.setIcon(null);
  }

  public boolean isPaused() {
    return this.pauseJob;
  }

  /**
   * This method is called to restart a paused job
   */
  public void restartJob() {
    _msg.setText("");
    pauseJob = false;
    _animIcon.setIcon(DocViewerConfig.WORKING_ICON);
  }

  /**
   * This method is intended to be used by process tasks to figures out if they
   * have to stop their job.
   */
  public boolean interruptProcessing() {
    return cancelJob;
  }

  /**
   * Sets the steps to the progress bar of the monitor. This method is intended
   * to be used by process tasks monitored by this class.
   */
  public void setCurSteps(int curSteps) {
    this.curSteps = curSteps;
    _progressBar.setValue(curSteps);
    _msg.setText(INFO_TXT.format(new Object[] { 
        curSteps,
        getProgressPct(),
        formatInterval(System.currentTimeMillis()-runningTime),
        formatInterval(getRemainingTime()) }));
  }

  /**
   * Adds some steps to the progress bar of the monitor. This method is intended
   * to be used by process tasks monitored by this class.
   */
  public void addSteps(int steps) {
    this.curSteps = _progressBar.getValue() + steps;
    _progressBar.setValue(this.curSteps);
    _msg.setText(INFO_TXT.format(new Object[] { 
        this.curSteps,
        getProgressPct(),
        formatInterval(System.currentTimeMillis()-runningTime),
        formatInterval(getRemainingTime()) }));
  }

  /**
   * Sets a message to be displayed by the monitor. This method is intended to
   * be used by process tasks monitored by this class.
   */
  public void setMessage(String msg) {
    _msg.setText(msg);
  }

  public int getCurSteps() {
    return curSteps;
  }

  public int getTotSteps() {
    return totSteps;
  }

  /**
   * Sets the total number of steps to the progress bar of the monitor. This
   * method is intended to be used by process tasks monitored by this class.
   */
  public void setTotSteps(int tot) {
    totSteps = tot;
    _progressBar.setMaximum(tot);
  }

  /**
   * This method must be called by process tasks monitored by this class to
   * inform that have ended a job.
   */
  public void jobDone() {
    setJobRunnig(false);
    _animIcon.setIcon(null);
  }

  /**
   * This method must be called by process tasks monitored by this class to
   * inform that they are starting a job.
   */
  public void startJob() {
    setJobRunnig(true);
    _animIcon.setIcon(DocViewerConfig.WORKING_ICON);
    runningTime=System.currentTimeMillis();
  }

  public void setCmdBar(JComponent compo) {
    JPanel pnl = new JPanel(new BorderLayout());
    pnl.add(compo, BorderLayout.NORTH);
    pnl.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 4));
    mainPanel.add(pnl, BorderLayout.EAST);
  }

  private void buildUI(int max) {
    mainPanel = new JPanel(new BorderLayout());
    _animIcon = new JLabel();
    mainPanel.add(_animIcon, BorderLayout.WEST);
    mainPanel.add(createProgressPanel(max), BorderLayout.CENTER);
    mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
  }

  private Component createProgressPanel(int max) {
    DefaultFormBuilder builder;
    FormLayout layout;

    layout = new FormLayout("300dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
    // builder.appendSeparator("Task in progress");
    _progressBar = new JProgressBar(0, max);
    builder.append(_progressBar);
    _msg = new JLabel("");
    // _msg.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    builder.append(_msg);
    return builder.getContainer();
  }

  public void bytesRead(long bytes, long totalBytes) {
    totBytesRead += bytes;
    _msg.setText("Reading " + Utils.getBytes(totBytesRead) + "...");
  }

  public void startReading() {
  }

  public void stopReading() {
  }

  private long getRemainingTime(){
    return ((System.currentTimeMillis()-runningTime)*(totSteps-curSteps) / curSteps);
  }
  private String getProgressPct(){
    return String.format("%d", (curSteps*100)/totSteps);
  }
  private String formatInterval(final long l)
  {
      final long hr = TimeUnit.MILLISECONDS.toHours(l);
      final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
      final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
      return String.format("%02d:%02d:%02d", hr, min, sec);
  }

}