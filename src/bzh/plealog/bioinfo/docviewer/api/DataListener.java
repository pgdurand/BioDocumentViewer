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
package bzh.plealog.bioinfo.docviewer.api;

/**
 * Interface uses to monitor data transfer.
 * 
 * @author Patrick G. Durand
 */
public interface DataListener {
	/**
	 * Method called during stream reading.
	 * 
	 * @param bytes number of bytes read since last call
	 * @param totalBytes numer of bytes read since the beginning
	 */
	public void bytesRead(long bytes, long totalBytes);
	/**
	 * Method called when stream reading is about to start.
	 */
	public void startReading();
	/**
	 * Method called when stream reading is done.
	 */
	public void stopReading();
	/**
	 * This method is intended to be used by callers to figure out if they
	 * have to stop their job.
	 */
	public boolean interruptProcessing();
}
