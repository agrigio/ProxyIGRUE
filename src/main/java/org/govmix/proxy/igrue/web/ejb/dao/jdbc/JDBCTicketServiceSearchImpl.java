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
package org.govmix.proxy.igrue.web.ejb.dao.jdbc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.openspcoop2.utils.sql.ISQLQueryObject;
import org.openspcoop2.generic_project.dao.jdbc.utils.JDBCObject;
import org.openspcoop2.generic_project.dao.jdbc.IJDBCServiceSearchWithId;
import org.govmix.proxy.igrue.web.ejb.IdTrasmissione;
import org.govmix.proxy.igrue.web.ejb.IdUtente;
import org.govmix.proxy.igrue.web.ejb.TicketId;
import org.openspcoop2.generic_project.beans.InUse;
import org.openspcoop2.generic_project.beans.NonNegativeNumber;
import org.openspcoop2.generic_project.beans.IField;
import org.openspcoop2.generic_project.beans.UnionExpression;
import org.openspcoop2.generic_project.beans.Union;
import org.openspcoop2.generic_project.beans.FunctionField;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCPaginatedExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCServiceManagerProperties;
import org.govmix.proxy.igrue.web.ejb.dao.jdbc.converter.TicketFieldConverter;
import org.govmix.proxy.igrue.web.ejb.dao.jdbc.fetch.TicketFetch;
import org.govmix.proxy.igrue.web.ejb.dao.jdbc.JDBCServiceManager;
import org.govmix.proxy.igrue.web.ejb.Ticket;


public class JDBCTicketServiceSearchImpl implements IJDBCServiceSearchWithId<Ticket, TicketId, JDBCServiceManager> {

	private TicketFieldConverter ticketFieldConverter = new TicketFieldConverter();
	public TicketFieldConverter getTicketFieldConverter() {
		return this.ticketFieldConverter;
	}
	
	private TicketFetch ticketFetch = new TicketFetch();
	public TicketFetch getTicketFetch() {
		return this.ticketFetch;
	}
	
	
	private JDBCServiceManager jdbcServiceManager = null;

	//@Override
	public void setServiceManager(JDBCServiceManager serviceManager) throws ServiceException{
		this.jdbcServiceManager = serviceManager;
	}
	
	//@Override
	public JDBCServiceManager getServiceManager() throws ServiceException{
		return this.jdbcServiceManager;
	}
	

	//@Override
	public Ticket get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, TicketId id) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException,Exception {
		Object id_ticket = this.findIdTicket(jdbcProperties, log, connection, sqlQueryObject, id, true);
		return this._get(jdbcProperties, log, connection, sqlQueryObject, id_ticket);
		
		
	}
	
	//@Override
	public boolean exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, TicketId id) throws MultipleResultException, NotImplementedException, ServiceException,Exception {

		Object id_ticket = this.findIdTicket(jdbcProperties, log, connection, sqlQueryObject, id, false);
		return id_ticket != null;	
		
	}
	
	//@Override
	public List<TicketId> findAllIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression expression) throws NotImplementedException, ServiceException,Exception {

		List<TicketId> list = new ArrayList<TicketId>();

		List<Object> ids = this._findAllObjectIds(jdbcProperties, log, connection, sqlQueryObject, expression);
        
        for(Object id: ids) {
        	TicketId idTicket = (TicketId)id;
        	list.add(idTicket);
        }

        return list;
		
	}
	
	//@Override
	public List<Ticket> findAll(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression expression) throws NotImplementedException, ServiceException,Exception {

        List<Ticket> list = new ArrayList<Ticket>();

		List<Object> ids = this._findAllObjectIds(jdbcProperties, log, connection, sqlQueryObject, expression);
        
        for(Object id: ids) {
        	list.add(this._get(jdbcProperties, log, connection, sqlQueryObject, id));
        }

        return list;      
		
	}
	
	//@Override
	public Ticket find(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) 
		throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException,Exception {

        Object id = this._findObjectId(jdbcProperties, log, connection, sqlQueryObject, expression);
        if(id!=null){
        	return this._get(jdbcProperties, log, connection, sqlQueryObject, id);
        }else{
        	throw new NotFoundException("Entry with id["+id+"] not found");
        }
		
	}
	
	//@Override
	public NonNegativeNumber count(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws NotImplementedException, ServiceException,Exception {
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareCount(jdbcProperties, log, connection, sqlQueryObject, expression,
												this.getTicketFieldConverter(), Ticket.model());
		
		sqlQueryObject.addSelectCountField("tot");
		
		_join(expression,sqlQueryObject);
		
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.count(jdbcProperties, log, connection, sqlQueryObject, expression,
																			this.getTicketFieldConverter(), Ticket.model(),listaQuery);
	}

	//@Override
	public InUse inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, TicketId id) throws NotFoundException, NotImplementedException, ServiceException,Exception {
		
		Object id_ticket = this.findIdTicket(jdbcProperties, log, connection, sqlQueryObject, id, true);
        return this._inUse(jdbcProperties, log, connection, sqlQueryObject, id_ticket);
		
	}

	//@Override
	public List<Object> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, IField field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		List<Map<String,Object>> map = 
			this.select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression, new IField[]{field});
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.selectSingleObject(map);
	}
	
	//@Override
	public List<Map<String,Object>> select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, IField ... field) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,paginatedExpression,field);
		try{
			return _select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,paginatedExpression,field);
		}
	}

	//@Override
	public Object aggregate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		Map<String,Object> map = 
			this.aggregate(jdbcProperties, log, connection, sqlQueryObject, expression, new FunctionField[]{functionField});
		return org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.selectAggregateObject(map,functionField);
	}
	
	//@Override
	public Map<String,Object> aggregate(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {													
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,expression,functionField);
		try{
			List<Map<String,Object>> list = _select(jdbcProperties, log, connection, sqlQueryObject, expression);
			return list.get(0);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,expression,functionField);
		}
	}

	//@Override
	public List<Map<String,Object>> groupBy(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		if(expression.getGroupByFields().size()<=0){
			throw new ServiceException("GroupBy conditions not found in expression");
		}
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,expression,functionField);
		try{
			return _select(jdbcProperties, log, connection, sqlQueryObject, expression);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,expression,functionField);
		}
	}
	

	//@Override
	public List<Map<String,Object>> groupBy(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
													JDBCPaginatedExpression paginatedExpression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		if(paginatedExpression.getGroupByFields().size()<=0){
			throw new ServiceException("GroupBy conditions not found in expression");
		}
		
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.setFields(sqlQueryObject,paginatedExpression,functionField);
		try{
			return _select(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression);
		}finally{
			org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.removeFields(sqlQueryObject,paginatedExpression,functionField);
		}
	}
	
	protected List<Map<String,Object>> _select(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												IExpression expression) throws ServiceException,NotFoundException,NotImplementedException,Exception {
		
		List<Object> listaQuery = new ArrayList<Object>();
		List<JDBCObject> listaParams = new ArrayList<JDBCObject>();
		List<Object> returnField = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareSelect(jdbcProperties, log, connection, sqlQueryObject, 
        						expression, this.getTicketFieldConverter(), Ticket.model(), 
        						listaQuery,listaParams);
		
		_join(expression,sqlQueryObject);
        
        List<Map<String,Object>> list = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.select(jdbcProperties, log, connection, sqlQueryObject, 
        								expression, this.getTicketFieldConverter(), Ticket.model(),
        								listaQuery,listaParams,returnField);
		if(list!=null && list.size()>0){
			return list;
		}
		else{
			throw new NotFoundException("Not Found");
		}
	}
		
	//@Override
	public List<Map<String,Object>> union(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException,Exception {		
		
		List<ISQLQueryObject> sqlQueryObjectInnerList = new ArrayList<ISQLQueryObject>();
		List<JDBCObject> jdbcObjects = new ArrayList<JDBCObject>();
		List<Class<?>> returnClassTypes = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareUnion(jdbcProperties, log, connection, sqlQueryObject, 
        						this.getTicketFieldConverter(), Ticket.model(), 
        						sqlQueryObjectInnerList, jdbcObjects, union, unionExpression);
		
		if(unionExpression!=null){
			for (int i = 0; i < unionExpression.length; i++) {
				UnionExpression ue = unionExpression[i];
				IExpression expression = ue.getExpression();
				_join(expression,sqlQueryObjectInnerList.get(i));
			}
		}
        
        List<Map<String,Object>> list = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.union(jdbcProperties, log, connection, sqlQueryObject, 
        								this.getTicketFieldConverter(), Ticket.model(), 
        								sqlQueryObjectInnerList, jdbcObjects, returnClassTypes, union, unionExpression);
        if(list!=null && list.size()>0){
			return list;
		}
		else{
			throw new NotFoundException("Not Found");
		}								
	}
	
	//@Override
	public NonNegativeNumber unionCount(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, 
												Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException,Exception {		
		
		List<ISQLQueryObject> sqlQueryObjectInnerList = new ArrayList<ISQLQueryObject>();
		List<JDBCObject> jdbcObjects = new ArrayList<JDBCObject>();
		List<Class<?>> returnClassTypes = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareUnionCount(jdbcProperties, log, connection, sqlQueryObject, 
        						this.getTicketFieldConverter(), Ticket.model(), 
        						sqlQueryObjectInnerList, jdbcObjects, union, unionExpression);
		
		if(unionExpression!=null){
			for (int i = 0; i < unionExpression.length; i++) {
				UnionExpression ue = unionExpression[i];
				IExpression expression = ue.getExpression();
				_join(expression,sqlQueryObjectInnerList.get(i));
			}
		}
        
        NonNegativeNumber number = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.unionCount(jdbcProperties, log, connection, sqlQueryObject, 
        								this.getTicketFieldConverter(), Ticket.model(), 
        								sqlQueryObjectInnerList, jdbcObjects, returnClassTypes, union, unionExpression);
        if(number!=null && number.longValue()>=0){
			return number;
		}
		else{
			throw new NotFoundException("Not Found");
		}
	}



	// -- ConstructorExpression	

	//@Override
	public JDBCExpression newExpression(Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCExpression(this.ticketFieldConverter);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}


	//@Override
	public JDBCPaginatedExpression newPaginatedExpression(Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCPaginatedExpression(this.ticketFieldConverter);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
	
	//@Override
	public JDBCExpression toExpression(JDBCPaginatedExpression paginatedExpression, Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCExpression(paginatedExpression);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}

	//@Override
	public JDBCPaginatedExpression toPaginatedExpression(JDBCExpression expression, Logger log) throws NotImplementedException, ServiceException {
		try{
			return new JDBCPaginatedExpression(expression);
		}catch(Exception e){
			throw new ServiceException(e);
		}
	}
	
	
	
	// -- DB
	
	//@Override
	public Ticket get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException, Exception {
		throw new NotImplementedException("Table without long id column PK");
	}
	
	protected Ticket _get(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Object objectId) throws NotFoundException, MultipleResultException, NotImplementedException, ServiceException, Exception {
	
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
					new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
				
		ISQLQueryObject sqlQueryObjectGet = sqlQueryObject.newSQLQueryObject();
				
		Ticket ticket = new Ticket();

		TicketId id = this.getIdFromObject(objectId);

		// Object ticket
		ISQLQueryObject sqlQueryObjectGet_ticket = sqlQueryObjectGet.newSQLQueryObject();
		sqlQueryObjectGet_ticket.setANDLogicOperator(true);
		sqlQueryObjectGet_ticket.addFromTable(this.getTicketFieldConverter().toTable(Ticket.model()));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_SISTEMA,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().FILE,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().DATAASSEGNAZIONE,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().DATAFINETRASMISSIONE,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().FILERICEVUTO,true));
		sqlQueryObjectGet_ticket.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().IDTICKET,true));

		sqlQueryObjectGet_ticket.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().FILE,true)+"=?");
		sqlQueryObjectGet_ticket.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true)+"=?");
		sqlQueryObjectGet_ticket.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().ID_SISTEMA,true)+"=?");

		// Get ticket
		ticket = (Ticket) jdbcUtilities.executeQuerySingleResult(sqlQueryObjectGet_ticket.createSQLQuery(), jdbcProperties.isShowSql(), Ticket.model(), this.getTicketFetch(),
			new JDBCObject(id.getTrasmissione().getFile(),id.getTrasmissione().getFile().getClass()),
			new JDBCObject(id.getTrasmissione().getUtente().getIdAmministrazione(),id.getTrasmissione().getUtente().getIdAmministrazione().getClass()),
			new JDBCObject(id.getTrasmissione().getUtente().getIdSistema(),id.getTrasmissione().getUtente().getIdSistema().getClass()));

        return ticket;  
	
	} 
	
	protected TicketId getIdFromObject(Object objectId) throws ServiceException {
		if(!(objectId instanceof TicketId)) throw new ServiceException("ID dovrebbe essere un TicketId");
		return (TicketId)objectId;
	}

	//@Override
	public boolean exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws MultipleResultException, NotImplementedException, ServiceException, Exception {
		throw new NotImplementedException("Table without long id column PK");
	}
	
	protected boolean _exists(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Object objectId) throws MultipleResultException, NotImplementedException, ServiceException, Exception {
	
		org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities jdbcUtilities = 
					new org.openspcoop2.generic_project.dao.jdbc.utils.JDBCPreparedStatementUtilities(sqlQueryObject.getTipoDatabaseOpenSPCoop2(), log, connection);
				
		boolean existsTicket = false;

		TicketId id = this.getIdFromObject(objectId);

		sqlQueryObject = sqlQueryObject.newSQLQueryObject();

		sqlQueryObject.setANDLogicOperator(true);
		sqlQueryObject.addFromTable(this.getTicketFieldConverter().toTable(Ticket.model()));
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true));

		sqlQueryObject.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().FILE,true)+"=?");
		sqlQueryObject.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true)+"=?");
		sqlQueryObject.addWhereCondition(this.getTicketFieldConverter().toColumn(Ticket.model().ID_SISTEMA,true)+"=?");

		// Exists ticket
		existsTicket = jdbcUtilities.exists(sqlQueryObject.createSQLQuery(), jdbcProperties.isShowSql(),
				new JDBCObject(id.getTrasmissione().getFile(),id.getTrasmissione().getFile().getClass()),
				new JDBCObject(id.getTrasmissione().getUtente().getIdAmministrazione(),id.getTrasmissione().getUtente().getIdAmministrazione().getClass()),
				new JDBCObject(id.getTrasmissione().getUtente().getIdSistema(),id.getTrasmissione().getUtente().getIdSistema().getClass()));

        return existsTicket;
	
	}
	
	private void _join(IExpression expression, ISQLQueryObject sqlQueryObject) throws NotImplementedException, ServiceException, Exception{
	
		if(expression.inUseModel(Ticket.model().TRASMISSIONE.UTENTE,false)){
			String tableName1 = this.getTicketFieldConverter().toTable(Ticket.model());
			String tableName2 = this.getTicketFieldConverter().toTable(Ticket.model().TRASMISSIONE.UTENTE);
			sqlQueryObject.addWhereCondition(tableName1+".id_amministratore="+tableName2+".id_amministratore");
			sqlQueryObject.addWhereCondition(tableName1+".id_sistema="+tableName2+".id_sistema");
		}
		
	}
	
	//@Override
	public List<Long> findAllTableIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression paginatedExpression) throws ServiceException, NotImplementedException, Exception {
		
		throw new NotImplementedException("Table without long id column PK");
		
	}
	public List<Object> _findAllObjectIds(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCPaginatedExpression paginatedExpression) throws ServiceException, NotImplementedException, Exception {
		
		sqlQueryObject.setANDLogicOperator(true);
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().FILE,true));
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true));
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_SISTEMA,true));

		
		List<Class<?>> objectIdClass = new ArrayList<Class<?>>();
		
		objectIdClass.add(Ticket.model().FILE.getFieldType());
		objectIdClass.add(Ticket.model().ID_AMMINISTRAZIONE.getFieldType());
		objectIdClass.add(Ticket.model().ID_SISTEMA.getFieldType());
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareFindAll(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression,
												this.getTicketFieldConverter(), Ticket.model());
		
		_join(paginatedExpression,sqlQueryObject);
		
		List<List<Object>> listListObjects = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.findAll(jdbcProperties, log, connection, sqlQueryObject, paginatedExpression,
																			this.getTicketFieldConverter(), Ticket.model(), objectIdClass, listaQuery);
        List<Object> listObjects = new ArrayList<Object>();
        for(List<Object> obj: listListObjects) {
        	listObjects.add(this.getIdFromListObjects(obj));
        }
		return listObjects;
		
	}
	
	private TicketId getIdFromListObjects(List<Object> obj) {
		TicketId id = new TicketId();
		IdTrasmissione idTrasmissione = new IdTrasmissione();
		
		idTrasmissione.setFile((Integer)obj.get(0));
		IdUtente utente = new IdUtente();
		utente.setIdAmministrazione((String)obj.get(1));
		utente.setIdSistema((Integer)obj.get(2));
		idTrasmissione.setUtente(utente);
		
		id.setTrasmissione(idTrasmissione);
		
		return id;
	}

	//@Override
	public long findTableId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws ServiceException, NotFoundException, MultipleResultException, NotImplementedException, Exception {
	
		throw new NotImplementedException("Table without long id column PK");
		
	}
	
	public Object _findObjectId(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, JDBCExpression expression) throws ServiceException, NotFoundException, MultipleResultException, NotImplementedException, Exception {
		
		sqlQueryObject.setANDLogicOperator(true);
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().FILE,true));
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_AMMINISTRAZIONE,true));
		sqlQueryObject.addSelectField(this.getTicketFieldConverter().toColumn(Ticket.model().ID_SISTEMA,true));

		
		List<Class<?>> objectIdClass = new ArrayList<Class<?>>();
		
		objectIdClass.add(Ticket.model().FILE.getFieldType());
		objectIdClass.add(Ticket.model().ID_AMMINISTRAZIONE.getFieldType());
		objectIdClass.add(Ticket.model().ID_SISTEMA.getFieldType());
		
		List<Object> listaQuery = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.prepareFind(jdbcProperties, log, connection, sqlQueryObject, expression,
												this.getTicketFieldConverter(), Ticket.model());
		
		_join(expression,sqlQueryObject);

		List<Object> res = org.openspcoop2.generic_project.dao.jdbc.utils.JDBCUtilities.find(jdbcProperties, log, connection, sqlQueryObject, expression,
														this.getTicketFieldConverter(), Ticket.model(), objectIdClass, listaQuery);
		if(res!=null){
			return this.getIdFromListObjects(res);
		}
		else{
			throw new NotFoundException("Not Found");
		}
		
	}

	//@Override
	public InUse inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, long tableId) throws ServiceException, NotFoundException, NotImplementedException, Exception {
		throw new NotImplementedException("Table without long id column PK");
	}

	protected InUse _inUse(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, Object objectId) throws ServiceException, NotFoundException, NotImplementedException, Exception {

		InUse inUse = new InUse();
		inUse.setInUse(false);
		
		/* 
         * TODO: implement code that checks whether the object identified by the id parameter is used by other objects
        */

        // Delete this line when you have implemented the method
        int throwNotImplemented = 1;
        if(throwNotImplemented==1){
                throw new NotImplementedException("NotImplemented");
        }
        // Delete this line when you have implemented the method

        return inUse;

	}
	
	protected Object findIdTicket(JDBCServiceManagerProperties jdbcProperties, Logger log, Connection connection, ISQLQueryObject sqlQueryObject, TicketId id, boolean throwNotFound) throws NotFoundException, ServiceException, NotImplementedException, Exception {

		if(!_exists(jdbcProperties, log, connection, sqlQueryObject, id)) {
			if(throwNotFound) {
				throw new NotFoundException("Not Found");
			} else {
				return null;
			}
		}

		return id;
	}
}
