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

public interface ServerConfiguration {
  /**
   * Returns the number of sequences to retrieve in each batch connection.
   */
  public int getSequencesPerRun();

  /**
   * Returns the sleep time to use between two retrieval runs. Return value is
   * in milliseconds.
   */
  public int getSleepTimeBetweenRun();

  /**
   * Returns the number of letters to retrieve in each batch connection.
   */
  public int getLettersPerRun();

  /**
   * Figures out whether or not remote server is available.
   */
  public boolean isServerAvailable();

}
