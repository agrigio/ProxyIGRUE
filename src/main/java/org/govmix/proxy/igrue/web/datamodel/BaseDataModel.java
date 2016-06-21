/*
 * ProxyIgrue - Reimplementazione free del Sender IGRUE del MEF 
 * http://igrue.gov4j.it
 * 
 * Copyright (c) 2009-2015 Link.it srl (http://link.it). 
 * Copyright (c) 2009 Provincia Autonoma di Bolzano (http://www.provincia.bz.it/). 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.govmix.proxy.igrue.web.datamodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SerializableDataModel;
import org.apache.log4j.Logger;

/**
 * 
 * @author corallo
 *
 * @param <K> Il tipo della chiave (Integer, Long ...)
 * @param <T> Il tipo di dato che rappresenta una righa della tabella
 * @param <D> Il tipo del dataprovider
 */
public abstract class BaseDataModel<K, T , D> extends SerializableDataModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8413193446969383169L;
	private static Logger log = Logger.getLogger(BaseDataModel.class);
	private D dataProvider;
	protected boolean detached = false;
	protected K currentPk;
	protected Map<K,T> wrappedData = new HashMap<K,T>();
    protected List<K> wrappedKeys = null;
    protected Integer rowCount;
    
    /**
     * The boolean field, detached, starts as false.  
     * It will be set to “true” when SerializableDataModel getSerializableModel(Range range) is called, and back to false when public void update() is called.  
     * In this manner, the model will not be updated when the jsf component is rebuilt on postback, but rather when the new model is being built.
     */
	//@Override
	public void update() {
		//nothing to do
		detached = false;
	}

	 /**
     * This method never called from framework.
     * (non-Javadoc)
     * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
     */
    //@Override
    public Object getRowKey() {
        return currentPk;
    }
    /**
     * This method normally called by Visitor before request Data Row.
     */
    @SuppressWarnings("unchecked")
	//@Override
    public void setRowKey(Object key) {
        this.currentPk = (K) key;
    }

    /**
     * This is main part of Visitor pattern. Method called by framework many times during request processing. 
     * 
     * We have two checks to see if we should return cached data, or if we should fetch new data.  
     * The boolean field, detached, starts as false.  
     * It will be set to “true” when SerializableDataModel getSerializableModel(Range range) is called, and back to false when public void update() is called.  
     * In this manner, the model will not be updated when the jsf component is rebuilt on postback, but rather when the new model is being built.
     */
	//@Override
	public abstract void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException;

	/**
     * This method must return actual data rows count from the Data Provider. It is used by pagination control
     * to determine total number of data items.
     */
	//@Override
	public abstract int getRowCount();

	/**
     * This is main way to obtain data row. It is intensively used by framework. 
     * We strongly recommend use of local cache in that method. 
     * 
     * <pre>
      <code>if (currentPk==null) {
            return null;
        } else {
            User ret = wrappedData.get(currentPk);
            if (ret==null) {
                ret = getDataProvider().getAuctionItemByPk(currentPk);
                wrappedData.put(currentPk, ret);
                return ret;
            } else {
                return ret;
            }
        }</code>
     	</pre>
     * 
     * 
     */
	//@Override
	public abstract Object getRowData();
	 
	/**
     * Unused rudiment from old JSF staff.
     */
	//@Override
	public int getRowIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
     * Unused rudiment from old JSF staff.
     */
	//@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}
	
	//@Override
	public boolean isRowAvailable() {
		if(this.wrappedData.isEmpty()){
			return false;
		}else{
			boolean isAvailable = this.wrappedData.containsKey(this.currentPk);
			return isAvailable;
		}
	}
	/**
     * Unused rudiment from old JSF staff.
     */
	//@Override
	public void setRowIndex(int arg0) {
		// ignore
		log.debug("called setRowIndex "+arg0);
	}
	/**
     * Unused rudiment from old JSF staff.
     */
	//@Override
	public void setWrappedData(Object arg0) {
		throw new UnsupportedOperationException();
	}

	public D getDataProvider(){
		return dataProvider; 
	}

    public void setDataProvider(D dataProvider){
    	this.dataProvider = dataProvider;
    }
    
    /**
     * This method suppose to produce SerializableDataModel that will be serialized into View State and used on a post-back.
     * In current implementation we just mark current model as serialized. In more complicated cases we may need to 
     * transform data to actually serialized form.
     */
    public  SerializableDataModel getSerializableModel(Range range) {
        if (wrappedKeys!=null) {
        	detached = true;
            return this; 
        } else {
            return null;
        }
    }
}
