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

import java.util.List;

/**
 * Describe a data banks provider.
 * 
 * @author Patrick G. Durand
 * */
public interface BankProvider {

  /**
   * Returns the name of this provider. In addition to giving a name, this value
   * is also the one to use on the command-line to specify the BankProvider to
   * use at application startup.
   * 
   * @return the provider name
   * */
  public String getProviderName();

  /**
   * Return the list of banks available for query.
   * 
   * @return list of banks
   */
  public List<BankType> getBanks();
}
