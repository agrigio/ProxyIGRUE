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
package org.govmix.proxy.igrue.ws.skeleton;

import it.mef.csp.webservices.messages.trasmissione.AssegnazioneCodProcAttIn;
import it.mef.csp.webservices.messages.trasmissione.AssegnazioneCodProcAttOut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.govmix.proxy.igrue.moduli.Spedizione;
import org.govmix.proxy.igrue.web.ejb.Esito;
import org.govmix.proxy.igrue.web.ejb.Evento;
import org.govmix.proxy.igrue.web.ejb.IdEsito;
import org.govmix.proxy.igrue.web.ejb.IdTabellacontesto;
import org.govmix.proxy.igrue.web.ejb.IdTrasmissione;
import org.govmix.proxy.igrue.web.ejb.IdUtente;
import org.govmix.proxy.igrue.web.ejb.Tabellacontesto;
import org.govmix.proxy.igrue.web.ejb.dao.IEsitoServiceSearch;
import org.govmix.proxy.igrue.web.ejb.dao.IEventoServiceSearch;
import org.govmix.proxy.igrue.web.ejb.dao.ITabellacontestoServiceSearch;
import org.govmix.proxy.igrue.web.ejb.utils.UtentiUtilities;
import org.govmix.proxy.igrue.web.service.ServiceManagerFactory;
import org.govmix.proxy.igrue.web.service.Utils;
import org.govmix.proxy.igrue.ws.Credenziali;
import org.govmix.proxy.igrue.ws.GetEsiti;
import org.govmix.proxy.igrue.ws.GetEsitiResponse;
import org.govmix.proxy.igrue.ws.GetTabellaContesto;
import org.govmix.proxy.igrue.ws.GetTabellaContestoResponse;
import org.govmix.proxy.igrue.ws.Igrue;
import org.govmix.proxy.igrue.ws.IgrueUtils;
import org.govmix.proxy.igrue.ws.SendDatiAttuazione;
import org.govmix.proxy.igrue.ws.SendDatiAttuazioneResponse;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;

@javax.jws.WebService(serviceName = "IgrueService", portName = "Igrue", targetNamespace = "http://ws.igrue.proxy.govmix.org", wsdlLocation = "WEB-INF/Igrue.wsdl", endpointInterface = "org.govmix.proxy.igrue.ws.Igrue")
public class IgrueImpl implements Igrue {

	public List<org.govmix.proxy.igrue.ws.GetEventiResponse.Evento> getEventi( Credenziali credenziali, XMLGregorianCalendar startdate) {



		try {
			if(!UtentiUtilities.auth(credenziali.getIdAmministrazione(), credenziali.getIdSistema(), credenziali.getPassword())){
				throw new Exception("Utente non autenticato");
			}


			List<org.govmix.proxy.igrue.ws.GetEventiResponse.Evento> response = new ArrayList<org.govmix.proxy.igrue.ws.GetEventiResponse.Evento>();
			IEventoServiceSearch eventoSearch = ServiceManagerFactory.getServiceManager().getEventoServiceSearch();

			IPaginatedExpression exp = eventoSearch.newPaginatedExpression();
			exp.greaterEquals(Evento.model().STARTDATE, startdate.toGregorianCalendar().getTime())
			.and().equals(Evento.model().OWNER_IDAMMINISTRAZIONE, credenziali.getIdAmministrazione())
			.and().equals(Evento.model().OWNER_IDSISTEMA, credenziali.getIdSistema())
			;

			java.util.List<Evento> lst = eventoSearch.findAll(exp);
			GregorianCalendar c = null;

			for(Evento evento: lst) {
				org.govmix.proxy.igrue.ws.GetEventiResponse.Evento o = new org.govmix.proxy.igrue.ws.GetEventiResponse.Evento();

				if(evento.getEnddate() != null) {
					c = new GregorianCalendar();
					c.setTime(new Date(evento.getEnddate().getTime()));
					o.setEnddate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
				}

				o.setEventtypeDescription(evento.getEventtypeDescription());
				o.setEventtypeCode(evento.getEventtypeCode());
				o.setId(evento.getEventId());
				o.setOwnerDescription(evento.getOwnerDescription());
				o.setOwnerIdamministrazione(evento.getOwnerIdamministrazione());
				o.setOwnerIdsistema(evento.getOwnerIdsistema());
				o.setParameterid(evento.getParameterid());
				o.setReason(evento.getReason());

				if(evento.getStartdate() != null){
					c = new GregorianCalendar();
					c.setTime(new Date(evento.getStartdate().getTime()));
					o.setStartdate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
				}

				response.add(o);
			}
			return response;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void getEsiti(GetEsiti request, Holder<GetEsitiResponse> response,
			Holder<byte[]> statisticheelaborazioni,
			Holder<byte[]> statistichescarti,
			Holder<byte[]> esitoelaborazioneperanagraficadiriferimento,
			Holder<byte[]> logdeglierrori) {
		int file = request.getIdAssegnato();
		GetEsitiResponse esitiOut = new GetEsitiResponse();
		response.value = esitiOut;
		try {
			
			if(!UtentiUtilities.auth(request.getCredenziali().getIdAmministrazione(), request.getCredenziali().getIdSistema(), request.getCredenziali().getPassword())){
				throw new Exception("Utente non autenticato");
			}
			
			IEsitoServiceSearch esitoSearch = ServiceManagerFactory.getServiceManager().getEsitoServiceSearch();

			IdEsito id = new IdEsito();
			IdTrasmissione idTrasmissione = new IdTrasmissione();
			idTrasmissione.setFile(file);
			IdUtente utente = new IdUtente(); 
			utente.setIdAmministrazione(request.getCredenziali().getIdAmministrazione());
			utente.setIdSistema(request.getCredenziali().getIdSistema());
			idTrasmissione.setUtente(utente);
			id.setTrasmissione(idTrasmissione);

			Esito esito = null;
			try {
				esito = esitoSearch.get(id);
			} catch(NotFoundException e) {
				throw new RuntimeException("Trasmissione non trovata.");
			}

			if (esito != null) {
				esitiOut.setFile(esito.getFile());
				esitiOut.setStatisticheelaborazioni(esito.getStatisticheelaborazioni());
				esitiOut.setStatisticheelaborazionidescrizione(esito.getStatisticheelaborazionidescrizione());
				esitiOut.setStatistichescarti(esito.getStatistichescarti());
				esitiOut.setStatistichescartidescrizione(esito.getStatistichescartidescrizione());
				esitiOut.setEsitoelaborazioneperanagraficadiriferimento(esito.getEsitoelaborazioneperanagraficadiriferimento());
				esitiOut.setEsitoelaborazioneperanagraficadiriferimentodescrizione(esito.getEsitoelaborazioneperanagraficadiriferimentodescrizione());
				esitiOut.setLogdeglierrori(esito.getLogdeglierrori());
				esitiOut.setLogdeglierroridescrizione(esito.getLogdeglierroridescrizione());

				GregorianCalendar c = new GregorianCalendar();
				c.setTime(new Date(esito.getDataultimocontrollo()
						.getTime()));
				esitiOut.setDataultimocontrollo(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(c));
			}

			// Allego gli esiti (se esistono)
			// statisticheelaborazioni
			try {
				File fileEsito = new File(UtentiUtilities.getInboxDir(utente) + file
						+ "/RISULTATI/statisticheelaborazioni.zip");
				FileInputStream fis = new FileInputStream(fileEsito);
				byte[] contentValue = new byte[(int) fileEsito.length()];
				fis.read(contentValue);
				fis.close();
				statisticheelaborazioni.value = contentValue;
			} catch (Exception e) {
				statisticheelaborazioni.value = new byte[0];
			}
			// statistichescarti
			try {
				File fileEsito = new File(UtentiUtilities.getInboxDir(utente) + file
						+ "/RISULTATI/statistichescarti.zip");
				FileInputStream fis = new FileInputStream(fileEsito);
				byte[] contentValue = new byte[(int) fileEsito.length()];
				fis.read(contentValue);
				fis.close();
				statistichescarti.value = contentValue;
			} catch (Exception e) {
				statistichescarti.value = new byte[0];
			}
			// esitoelaborazioneperanagraficadiriferimento
			try {
				File fileEsito = new File(
						UtentiUtilities.getInboxDir(utente)
						+ file
						+ "/RISULTATI/esitoelaborazioneperanagraficadiriferimento.zip");
				FileInputStream fis = new FileInputStream(fileEsito);
				byte[] contentValue = new byte[(int) fileEsito.length()];
				fis.read(contentValue);
				fis.close();
				esitoelaborazioneperanagraficadiriferimento.value = contentValue;
			} catch (Exception e) {
				esitoelaborazioneperanagraficadiriferimento.value = new byte[0];
			}
			// logdeglierrori
			try {
				File fileEsito = new File(UtentiUtilities.getInboxDir(utente) + file
						+ "/RISULTATI/logdeglierrori.zip");
				FileInputStream fis = new FileInputStream(fileEsito);
				byte[] contentValue = new byte[(int) fileEsito.length()];
				fis.read(contentValue);
				fis.close();
				logdeglierrori.value = contentValue;
			} catch (Exception e) {
				logdeglierrori.value = new byte[0];
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	public void getTabellaContesto(GetTabellaContesto request, Holder<GetTabellaContestoResponse> response, Holder<byte[]> content) {
		String tabella = request.getNometabella();
		GregorianCalendar c = new GregorianCalendar();
		try {
			if(!UtentiUtilities.auth(request.getCredenziali().getIdAmministrazione(), request.getCredenziali().getIdSistema(), request.getCredenziali().getPassword())){
				throw new Exception("Utente non autenticato");
			}
			
			ITabellacontestoServiceSearch tabellaContestoSearch = ServiceManagerFactory.getServiceManager().getTabellacontestoServiceSearch();
			IdTabellacontesto id = new IdTabellacontesto();
			id.setNometabella(tabella);

			IdUtente utente = new IdUtente(); 
			utente.setIdAmministrazione(request.getCredenziali().getIdAmministrazione());
			utente.setIdSistema(request.getCredenziali().getIdSistema());

			id.setUtente(utente);

			Tabellacontesto tabellaContesto = null;
			try {
				tabellaContesto = tabellaContestoSearch.get(id);
			} catch(NotFoundException e) {
				throw new Exception("TabellaContesto ["+tabella+"] non esistente per l'utente ["+id.getUtente().getIdAmministrazione()+":"+id.getUtente().getIdSistema()+"]");
			}

			c.setTime(new Date(tabellaContesto.getDataaggiornamento().getTime()));

			GetTabellaContestoResponse responseValue = new GetTabellaContestoResponse();
			responseValue.setDataaggiornamento(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			response.value = responseValue;

			File file = new File(UtentiUtilities.getContestoDir(utente) + tabella + ".zip");
			FileInputStream fis = new FileInputStream(file);
			byte[] contentValue = new byte[(int) file.length()];
			fis.read(contentValue);
			fis.close();
			content.value = contentValue;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public SendDatiAttuazioneResponse sendDatiAttuazione(SendDatiAttuazione request, byte[] bytes) {

		try {

			if(!UtentiUtilities.auth(request.getCredenziali().getIdAmministrazione(), request.getCredenziali().getIdSistema(), request.getCredenziali().getPassword())){
				throw new Exception("Utente non autenticato");
			}
			
			IdUtente idUtente = new IdUtente(); 
			idUtente.setIdAmministrazione(request.getCredenziali().getIdAmministrazione());
			idUtente.setIdSistema(request.getCredenziali().getIdSistema());

			Spedizione s = new Spedizione(null, idUtente);
			
			org.govmix.proxy.igrue.managers.Trasmissione t = new org.govmix.proxy.igrue.managers.Trasmissione(s);
			
			//Creo le cartelle necessarie:
			int codice = t.getFileId();
	    	(new File(UtentiUtilities.getInboxDir(idUtente) + codice)).mkdir();
	    	(new File(UtentiUtilities.getInboxDir(idUtente) + codice + "/RISULTATI")).mkdir();
	    	(new File(UtentiUtilities.getInboxDir(idUtente) + codice + "/EVENTI")).mkdir();
	    	
	    	File newZip = new File(UtentiUtilities.getInboxDir(idUtente) + codice + "/" + codice + ".zip");
	    	FileOutputStream fos = null;
	    	newZip.createNewFile();
			fos = new FileOutputStream(newZip);
			if (bytes != null) {
				fos.write(bytes);
			}
			else {
				throw new Exception("File non allegato o non valido");
			}
			fos.close();
			
			SendDatiAttuazioneResponse response = new SendDatiAttuazioneResponse();
			response.setIdAssegnato(t.getFileId());
			return response;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}


	public void getCodiceProceduraAttivazione(Credenziali credenziali,
//			Holder<Integer> protocollo,
			Holder<XMLGregorianCalendar> dataAssegnazione,
			Holder<String> idAmministrazione,
			Holder<String> idProceduraAttivazione, Holder<Integer> idSistema) {

		try {
			
			if(!UtentiUtilities.auth(credenziali.getIdAmministrazione(), credenziali.getIdSistema(), credenziali.getPassword())){
				throw new Exception("Utente non autenticato");
			}
			
			AssegnazioneCodProcAttIn in = new AssegnazioneCodProcAttIn();
			it.mef.csp.webservices.dto.Credenziali c = new it.mef.csp.webservices.dto.Credenziali();
			c.setIdAmministrazione(credenziali.getIdAmministrazione());
			c.setIdSistema(credenziali.getIdSistema());
//			c.setPassword(credenziali.getPassword());
			if (UtentiUtilities.getStatus("http://149.202.54.135/igrue"))
			{
			in.setCredenziali(c);
				in.setProtocollo(10);
			}


			IgrueUtils igrueUtils = new IgrueUtils();
			it.eng.csp.webservices.Trasmissione client = igrueUtils.getTrasmissioneClient();
			AssegnazioneCodProcAttOut out = client.assegnazioneCodProcAtt(in);
			idAmministrazione.value = out.getCodiceProceduraAttivazione().getIdAmministrazione();
			dataAssegnazione.value = out.getCodiceProceduraAttivazione().getDataAssegnazione();
			idProceduraAttivazione.value = out.getCodiceProceduraAttivazione().getIdProceduraAttivazione();
			idSistema.value = out.getCodiceProceduraAttivazione().getIdSistema();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

}
