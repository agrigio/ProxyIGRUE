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

import org.openspcoop2.generic_project.dao.jdbc.IJDBCServiceSearchWithId;
import org.govmix.proxy.igrue.web.ejb.IdEsito;
import org.openspcoop2.generic_project.beans.InUse;
import org.openspcoop2.generic_project.beans.IField;
import org.openspcoop2.generic_project.beans.NonNegativeNumber;
import org.openspcoop2.generic_project.beans.UnionExpression;
import org.openspcoop2.generic_project.beans.Union;
import org.openspcoop2.generic_project.beans.FunctionField;
import org.openspcoop2.generic_project.dao.jdbc.JDBCServiceManagerProperties;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCPaginatedExpression;
import org.openspcoop2.generic_project.dao.jdbc.JDBCProperties;

import org.govmix.proxy.igrue.web.ejb.dao.jdbc.JDBCServiceManager;
import org.govmix.proxy.igrue.web.ejb.dao.jdbc.JDBCLimitedServiceManager;
import org.govmix.proxy.igrue.web.ejb.Esito;
import org.govmix.proxy.igrue.web.ejb.dao.IEsitoServiceSearch;
import org.govmix.proxy.igrue.web.ejb.utils.ProjectInfo;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openspcoop2.utils.sql.ISQLQueryObject;
import org.openspcoop2.utils.sql.SQLObjectFactory;

/**     
 * Service can be used to search for the backend objects of type {@link org.govmix.proxy.igrue.web.ejb.Esito} 
 *
 * @author Giovanni Bussu (bussu@link.it)
 * @author $Author$
 * @version $Rev$, $Date$
*/

public class JDBCEsitoServiceSearch implements IEsitoServiceSearch {


	protected JDBCServiceManagerProperties jdbcProperties = null;
	protected JDBCServiceManager jdbcServiceManager = null;
	protected Logger log = null;
	protected IJDBCServiceSearchWithId<Esito, IdEsito, JDBCServiceManager> serviceSearch = null;
	public JDBCEsitoServiceSearch(JDBCServiceManager jdbcServiceManager) throws ServiceException {
		this.jdbcServiceManager = jdbcServiceManager;
		this.jdbcProperties = jdbcServiceManager.getJdbcProperties();
		this.log = jdbcServiceManager.getLog();
		this.log.debug(JDBCEsitoServiceSearch.class.getName()+ " initialized");
		this.serviceSearch = JDBCProperties.getInstance(ProjectInfo.getInstance()).getServiceSearch("esito");
		this.serviceSearch.setServiceManager(new JDBCLimitedServiceManager(this.jdbcServiceManager));
	}
		
	//@Override
	public Esito get(IdEsito id) throws ServiceException, NotFoundException,MultipleResultException, NotImplementedException {
    
		Connection connection = null;
		try{
			
			// check parameters
			if(id==null){
				throw new Exception("Parameter (type:"+IdEsito.class.getName()+") 'id' is null");
			}
			
			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();
		
			return this.serviceSearch.get(this.jdbcProperties,this.log,connection,sqlQueryObject,id);
		
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(MultipleResultException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Get not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
	
	}

	//@Override
	public boolean exists(IdEsito id) throws MultipleResultException,ServiceException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(id==null){
				throw new Exception("Parameter (type:"+IdEsito.class.getName()+") 'id' is null");
			}

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.exists(this.jdbcProperties,this.log,connection,sqlQueryObject,id);
	
		}catch(MultipleResultException e){
			this.log.error(e,e); throw e;
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Exists not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<IdEsito> findAllIds(IPaginatedExpression expression) throws ServiceException, NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCPaginatedExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCPaginatedExpression.class.getName());
			}
			JDBCPaginatedExpression jdbcPaginatedExpression = (JDBCPaginatedExpression) expression;
			this.log.debug("sql = "+jdbcPaginatedExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();
			
			return this.serviceSearch.findAllIds(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcPaginatedExpression);
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("FindAllIds not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}

	//@Override
	public List<Esito> findAll(IPaginatedExpression expression) throws ServiceException, NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCPaginatedExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCPaginatedExpression.class.getName());
			}
			JDBCPaginatedExpression jdbcPaginatedExpression = (JDBCPaginatedExpression) expression;
			this.log.debug("sql = "+jdbcPaginatedExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.findAll(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcPaginatedExpression);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("FindAll not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}

	//@Override
	public Esito find(IExpression expression) throws ServiceException, NotFoundException, MultipleResultException, NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCExpression.class.getName());
			}
			JDBCExpression jdbcExpression = (JDBCExpression) expression;
			this.log.debug("sql = "+jdbcExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.find(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcExpression);			

		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(MultipleResultException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Find not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}

	//@Override
	public NonNegativeNumber count(IExpression expression) throws ServiceException, NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCExpression.class.getName());
			}
			JDBCExpression jdbcExpression = (JDBCExpression) expression;
			this.log.debug("sql = "+jdbcExpression.toSql());
			
			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.count(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcExpression);
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Count not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}

	//@Override
	public InUse inUse(IdEsito id) throws ServiceException, NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(id==null){
				throw new Exception("Parameter (type:"+IdEsito.class.getName()+") 'id' is null");
			}
			
			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.inUse(this.jdbcProperties,this.log,connection,sqlQueryObject,id);	
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("InUse not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<Object> select(IPaginatedExpression paginatedExpression, IField field) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(paginatedExpression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'paginatedExpression' is null");
			}
			if( ! (paginatedExpression instanceof JDBCPaginatedExpression) ){
				throw new Exception("Parameter (type:"+paginatedExpression.getClass().getName()+") 'paginatedExpression' has wrong type, expect "+JDBCPaginatedExpression.class.getName());
			}
			JDBCPaginatedExpression jdbcPaginatedExpression = (JDBCPaginatedExpression) paginatedExpression;
			this.log.debug("sql = "+jdbcPaginatedExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.select(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcPaginatedExpression,field);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Select not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<Map<String,Object>> select(IPaginatedExpression paginatedExpression, IField ... field) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(paginatedExpression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'paginatedExpression' is null");
			}
			if( ! (paginatedExpression instanceof JDBCPaginatedExpression) ){
				throw new Exception("Parameter (type:"+paginatedExpression.getClass().getName()+") 'paginatedExpression' has wrong type, expect "+JDBCPaginatedExpression.class.getName());
			}
			JDBCPaginatedExpression jdbcPaginatedExpression = (JDBCPaginatedExpression) paginatedExpression;
			this.log.debug("sql = "+jdbcPaginatedExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.select(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcPaginatedExpression,field);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Select not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public Object aggregate(IExpression expression, FunctionField functionField) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCExpression.class.getName());
			}
			JDBCExpression jdbcExpression = (JDBCExpression) expression;
			this.log.debug("sql = "+jdbcExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.aggregate(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcExpression,functionField);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Aggregate not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public Map<String,Object> aggregate(IExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCExpression.class.getName());
			}
			JDBCExpression jdbcExpression = (JDBCExpression) expression;
			this.log.debug("sql = "+jdbcExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.aggregate(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcExpression,functionField);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Aggregate not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<Map<String,Object>> groupBy(IExpression expression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(expression==null){
				throw new Exception("Parameter (type:"+IExpression.class.getName()+") 'expression' is null");
			}
			if( ! (expression instanceof JDBCExpression) ){
				throw new Exception("Parameter (type:"+expression.getClass().getName()+") 'expression' has wrong type, expect "+JDBCExpression.class.getName());
			}
			JDBCExpression jdbcExpression = (JDBCExpression) expression;
			this.log.debug("sql = "+jdbcExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.groupBy(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcExpression,functionField);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("GroupBy not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<Map<String,Object>> groupBy(IPaginatedExpression paginatedExpression, FunctionField ... functionField) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(paginatedExpression==null){
				throw new Exception("Parameter (type:"+IPaginatedExpression.class.getName()+") 'paginatedExpression' is null");
			}
			if( ! (paginatedExpression instanceof JDBCPaginatedExpression) ){
				throw new Exception("Parameter (type:"+paginatedExpression.getClass().getName()+") 'paginatedExpression' has wrong type, expect "+JDBCPaginatedExpression.class.getName());
			}
			JDBCPaginatedExpression jdbcPaginatedExpression = (JDBCPaginatedExpression) paginatedExpression;
			this.log.debug("sql = "+jdbcPaginatedExpression.toSql());

			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.groupBy(this.jdbcProperties,this.log,connection,sqlQueryObject,jdbcPaginatedExpression,functionField);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("GroupBy not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public List<Map<String,Object>> union(Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(unionExpression==null){
				throw new Exception("Parameter (type:"+UnionExpression.class.getName()+") 'unionExpression' is null");
			}
			
			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.union(this.jdbcProperties,this.log,connection,sqlQueryObject,union,unionExpression);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("Union not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}
	
	//@Override
	public NonNegativeNumber unionCount(Union union, UnionExpression ... unionExpression) throws ServiceException,NotFoundException,NotImplementedException {

		Connection connection = null;
		try{
			
			// check parameters
			if(unionExpression==null){
				throw new Exception("Parameter (type:"+UnionExpression.class.getName()+") 'unionExpression' is null");
			}
			
			// ISQLQueryObject
			ISQLQueryObject sqlQueryObject = SQLObjectFactory.createSQLQueryObject(this.jdbcProperties.getDatabase());
			sqlQueryObject.setANDLogicOperator(true);
			// Connection sql
			connection = this.jdbcServiceManager.getConnection();

			return this.serviceSearch.unionCount(this.jdbcProperties,this.log,connection,sqlQueryObject,union,unionExpression);			
	
		}catch(ServiceException e){
			this.log.error(e,e); throw e;
		}catch(NotFoundException e){
			this.log.debug(e,e); throw e;
		}catch(NotImplementedException e){
			this.log.error(e,e); throw e;
		}catch(Exception e){
			this.log.error(e,e); throw new ServiceException("UnionCount not completed: "+e.getMessage(),e);
		}finally{
			if(connection!=null){
				this.jdbcServiceManager.closeConnection(connection);
			}
		}
		
	}

	//@Override
	public IExpression newExpression() throws ServiceException,NotImplementedException {

		return this.serviceSearch.newExpression(this.log);

	}

	//@Override
	public IPaginatedExpression newPaginatedExpression() throws ServiceException, NotImplementedException {

		return this.serviceSearch.newPaginatedExpression(this.log);

	}
	
	//@Override
	public IExpression toExpression(IPaginatedExpression paginatedExpression) throws ServiceException,NotImplementedException {

		return this.serviceSearch.toExpression((JDBCPaginatedExpression)paginatedExpression,this.log);

	}

	//@Override
	public IPaginatedExpression toPaginatedExpression(IExpression expression) throws ServiceException, NotImplementedException {

		return this.serviceSearch.toPaginatedExpression((JDBCExpression)expression,this.log);

	}
	

}
