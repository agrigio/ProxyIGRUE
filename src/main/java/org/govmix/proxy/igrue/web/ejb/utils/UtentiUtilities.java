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
package org.govmix.proxy.igrue.web.ejb.utils;

import it.mef.csp.webservices.dto.Credenziali;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.govmix.proxy.igrue.Configuration;
import org.govmix.proxy.igrue.web.ejb.IdUtente;
import org.govmix.proxy.igrue.web.ejb.Utente;
import org.govmix.proxy.igrue.web.ejb.dao.IUtenteServiceSearch;
import org.govmix.proxy.igrue.web.mbean.LoginBean;
import org.govmix.proxy.igrue.web.service.ServiceManagerFactory;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;

public class UtentiUtilities {

	public static String getInboxDir() {
		return _getDir(Configuration.INBOX, getIdUtente());
	}

	public static String getOutboxDir() {
		return _getDir(Configuration.OUTBOX, getIdUtente());
	}

	public static String getContestoDir() {
		return _getDir(Configuration.CONTESTO, getIdUtente());
	}

	public static String getInboxDir(Utente utente) {
		return _getDir(Configuration.INBOX, getIdUtente(utente));
	}

	public static String getOutboxDir(Utente utente) {
		return _getDir(Configuration.OUTBOX, getIdUtente(utente));
	}

	public static String getContestoDir(Utente utente) {
		return _getDir(Configuration.CONTESTO, getIdUtente(utente));
	}

	public static String getInboxDir(IdUtente utente) {
		return _getDir(Configuration.INBOX, utente);
	}

	public static String getOutboxDir(IdUtente utente) {
		return _getDir(Configuration.OUTBOX, utente);
	}

	public static String getContestoDir(IdUtente utente) {
		return _getDir(Configuration.CONTESTO, utente);
	}

	public static Boolean getStatus(String url) throws IOException {

		Boolean result = false;
		try {
			URL urlObj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
			con.setRequestMethod("GET");
			// Set connection timeout
			con.setConnectTimeout(3000);
			con.connect();

			int code = con.getResponseCode();
			if (code == 200) {
				result = true;
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
	public static Utente getUtente() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if(fc!= null){
			ExternalContext ec = fc.getExternalContext();
			LoginBean lb = (LoginBean)ec.getSessionMap().get("loginBean");

			if(lb!= null && lb.getIsLoggedIn()){
				return lb.getLoggedUtente();
			}
		}
		return null;
	}

	public static Credenziali getCredenziali() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if(fc != null){
			ExternalContext ec = fc.getExternalContext();
			LoginBean lb = (LoginBean)ec.getSessionMap().get("loginBean");

			if(lb!= null && lb.getIsLoggedIn() && lb.getLoggedUtente() != null){
				IdUtente id = new IdUtente();
				id.setIdAmministrazione(lb.getLoggedUtente().getIdAmministrazione());
				id.setIdSistema(lb.getLoggedUtente().getIdSistema());
				try {
					return getCredenziali(id);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public static Credenziali getCredenziali(IdUtente idUtente) throws ServiceException, NotFoundException, MultipleResultException, NotImplementedException, SQLException {
		Credenziali credenziali = new Credenziali();
		credenziali.setIdAmministrazione(idUtente.getIdAmministrazione());
		credenziali.setIdSistema(idUtente.getIdSistema());
		IUtenteServiceSearch utenteServiceSearch = ServiceManagerFactory.getServiceManager().getUtenteServiceSearch();
		Utente u = utenteServiceSearch.get(idUtente);
		credenziali.setPassword(u.getMefPassword());
		return credenziali;
	}

	private static String _getDir(String dir, IdUtente utente) {
		return Configuration.IGRUE + "/" + utente.getIdAmministrazione()  + "/" + utente.getIdSistema() + "/" + dir + "/";
	}

	public static IdUtente getIdUtente() {
		IdUtente id = new IdUtente();
		Utente utente = getUtente();
		id.setIdAmministrazione(utente.getIdAmministrazione());
		id.setIdSistema(utente.getIdSistema());
		return id;
	}

	public static IdUtente getIdUtente(Utente utente) {
		IdUtente id = new IdUtente();
		id.setIdAmministrazione(utente.getIdAmministrazione());
		id.setIdSistema(utente.getIdSistema());
		return id;
	}

	public static List<IdUtente> getLstUtenti(Logger log) throws ServiceException, NotImplementedException, SQLException {
		IUtenteServiceSearch utenteServiceSearch = ServiceManagerFactory.getServiceManager().getUtenteServiceSearch();
		return utenteServiceSearch.findAllIds(utenteServiceSearch.newPaginatedExpression());
	}
	
	public static boolean auth(String idAmministrazione, int idSistema, String password) throws NotFoundException, MultipleResultException, ExpressionNotImplementedException, ExpressionException, ServiceException, NotImplementedException, SQLException {
		IUtenteServiceSearch utenteServiceSearch = ServiceManagerFactory.getServiceManager().getUtenteServiceSearch();
		Utente u = utenteServiceSearch.find(utenteServiceSearch.newExpression()
				.equals(Utente.model().ID_AMMINISTRAZIONE, idAmministrazione).and()
				.equals(Utente.model().ID_SISTEMA, idSistema));
		return (u != null && u.getPassword().equals(password));
	}
}
