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
package bzh.plealog.bioinfo.docviewer.service.ebi;

import java.util.ArrayList;
import java.util.List;

import bzh.plealog.bioinfo.docviewer.api.BankProvider;
import bzh.plealog.bioinfo.docviewer.api.BankType;

/**
 * Defines the EBI data banks provider.
 * 
 * @author Patrick G. Durand
 */
public class EbiProvider implements BankProvider {

  @Override
  public String getProviderName() {
    return "EBI";
  }

  @Override
  public List<BankType> getBanks() {
    ArrayList<BankType> banks = new ArrayList<>();
    for (BankType bt : EbiBank.values()) {
      banks.add(bt);
    }
    return banks;
  }
}
