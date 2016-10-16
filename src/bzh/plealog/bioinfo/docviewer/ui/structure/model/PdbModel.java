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
package bzh.plealog.bioinfo.docviewer.ui.structure.model;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.event.EventListenerList;

/**
 * Setup a PDB data model suitable for the viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class PdbModel {
	private Hashtable<String, PdbSequence> _models;
	private EventListenerList                _listenerList;
	
	//** mettre un mecanisme de listener/notification de modifs **
	public PdbModel(){
		_models = new Hashtable<String, PdbSequence>();
		_listenerList = new EventListenerList();
	}
	
	public void add(PdbSequence seq){
		_models.put(seq.getChainCode(), seq);
		firePdbModelEvent(new PdbModelEvent(this, PdbModelEvent.CHAIN_ADDED, seq));
	}
	
	public int size(){
		return _models.size();
	}
	
	public void clear(){
		_models.clear();
		firePdbModelEvent(new PdbModelEvent(this, PdbModelEvent.MODEL_CLEARED));
	}
	
	public PdbSequence getPdbSequence(String chainCode){
		return _models.get(chainCode);
	}
	
	public Enumeration<String> getChainCodes(){
		return _models.keys();
	}
	
	public void addPdbModelListener(PdbModelListener l) {
		_listenerList.add(PdbModelListener.class, l);
	}

	public void removePdbModelListener(PdbModelListener l) {
		_listenerList.remove(PdbModelListener.class, l);
	}
	protected void firePdbModelEvent(PdbModelEvent event) {
	     Object[] listeners = _listenerList.getListenerList();
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==PdbModelListener.class) {
	             ((PdbModelListener)listeners[i+1]).pdbModelChanged(event);
	         }
	     }
	 }
}
