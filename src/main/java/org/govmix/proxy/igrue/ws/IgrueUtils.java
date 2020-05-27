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
package org.govmix.proxy.igrue.ws;

import com.sun.org.apache.xpath.internal.operations.Bool;
import it.eng.csp.webservices.EsitoElaborazione;
import it.eng.csp.webservices.EsitoElaborazioneSoapBindingQSService;
import it.eng.csp.webservices.GestioneEventi;
import it.eng.csp.webservices.GestioneEventiSoapBindingQSService;
import it.eng.csp.webservices.Tipologie;
import it.eng.csp.webservices.TipologieSoapBindingQSService;
import it.eng.csp.webservices.TrasmissioneSoapBindingQSService;
import it.mef.csp.webservices.dto.EventWS;
import it.mef.csp.webservices.dto.Property;
import it.mef.system.event.EventType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.govmix.proxy.igrue.Configuration;
import org.govmix.proxy.igrue.managers.DBManager;
import org.govmix.proxy.igrue.managers.LogManager;
import org.govmix.proxy.igrue.managers.MailManager;
import org.govmix.proxy.igrue.managers.Trasmissione;
import org.govmix.proxy.igrue.web.ejb.Evento;
import org.govmix.proxy.igrue.web.ejb.IdTrasmissione;
import org.govmix.proxy.igrue.web.ejb.IdUtente;
import org.govmix.proxy.igrue.web.ejb.ProprietaEvento;
import org.govmix.proxy.igrue.web.ejb.Ticket;
import org.govmix.proxy.igrue.web.ejb.Tipievento;
import org.govmix.proxy.igrue.web.ejb.dao.IEventoService;
import org.govmix.proxy.igrue.web.ejb.dao.IProprietaEventoService;
import org.govmix.proxy.igrue.web.ejb.dao.ITicketServiceSearch;
import org.govmix.proxy.igrue.web.ejb.dao.ITipieventoService;
import org.govmix.proxy.igrue.web.ejb.dao.jdbc.JDBCServiceManager;
import org.govmix.proxy.igrue.web.ejb.utils.UtentiUtilities;
import org.govmix.proxy.igrue.web.service.ServiceManagerFactory;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;
import org.w3c.dom.Node;

public class IgrueUtils {


	private static Marshaller marshaller;
	private static EsitoElaborazione esitoElaborazioneClient;
	private static GestioneEventi gestioneEventiClient;
	private static Tipologie tipologieClient;
	private static it.eng.csp.webservices.Trasmissione trasmissioneClient;
	private static SwaRequestFixInterceptor swaInterceptor;
	private static ActionResolverInterceptor actionResolverInterceptor;
	private static boolean initialized = false;
	public IgrueUtils() throws Exception{
		if(!initialized){
			swaInterceptor = new SwaRequestFixInterceptor();
			actionResolverInterceptor = new ActionResolverInterceptor();
			
			esitoElaborazioneClient = new EsitoElaborazioneSoapBindingQSService().getEsitoElaborazioneSoapBindingQSPort();
			((BindingProvider)esitoElaborazioneClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Configuration.PDurl_EsitoElaborazione.toString());
			((BindingProvider)esitoElaborazioneClient).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, Configuration.PDusername);
			((BindingProvider)esitoElaborazioneClient).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, Configuration.PDpassword);
			Client esitoElaborazioneClientC = ClientProxy.getClient(esitoElaborazioneClient);
			esitoElaborazioneClientC.getOutInterceptors().add(swaInterceptor);
			esitoElaborazioneClientC.getOutInterceptors().add(actionResolverInterceptor);
			
			gestioneEventiClient = new GestioneEventiSoapBindingQSService().getGestioneEventiSoapBindingQSPort();
			((BindingProvider)gestioneEventiClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Configuration.PDurl_GestioneEventi.toString());
			((BindingProvider)gestioneEventiClient).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, Configuration.PDusername);
			((BindingProvider)gestioneEventiClient).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, Configuration.PDpassword);
			Client gestioneEventiClientC = ClientProxy.getClient(gestioneEventiClient);
			gestioneEventiClientC.getOutInterceptors().add(swaInterceptor);
			gestioneEventiClientC.getOutInterceptors().add(actionResolverInterceptor);
			
			tipologieClient = new TipologieSoapBindingQSService().getTipologieSoapBindingQSPort();
			((BindingProvider)tipologieClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Configuration.PDurl_Tipologie.toString());
			((BindingProvider)tipologieClient).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, Configuration.PDusername);
			((BindingProvider)tipologieClient).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, Configuration.PDpassword);
			Client tipologieClientC = ClientProxy.getClient(tipologieClient);
			tipologieClientC.getOutInterceptors().add(swaInterceptor);
			tipologieClientC.getOutInterceptors().add(actionResolverInterceptor);
			
			trasmissioneClient = new TrasmissioneSoapBindingQSService().getTrasmissioneSoapBindingQSPort();
			((BindingProvider)trasmissioneClient).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Configuration.PDurl_Trasmissione.toString());
			((BindingProvider)trasmissioneClient).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, Configuration.PDusername);
			((BindingProvider)trasmissioneClient).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, Configuration.PDpassword);
			Client trasmissioneClientC = ClientProxy.getClient(trasmissioneClient);
			trasmissioneClientC.getOutInterceptors().add(swaInterceptor);
			trasmissioneClientC.getOutInterceptors().add(actionResolverInterceptor);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(EventWS.class);
			marshaller = jaxbContext.createMarshaller();
			initialized=true;
		} 
	}
	
	public EsitoElaborazione getEsitoElaborazioneClient() {
		return esitoElaborazioneClient;
	}
	
	public GestioneEventi getGestioneEventiClient() {
		return gestioneEventiClient;
	}
	
	public it.eng.csp.webservices.Trasmissione getTrasmissioneClient() {
		return trasmissioneClient;
	}
	
	public Tipologie getTipologieClient() {
		return tipologieClient;
	}
	
	public void logHttpResponse(BindingProvider client, Logger log) {
		Map<String, Object> context = client.getResponseContext();
		TreeMap<?,?> headers = (TreeMap<?,?>) context.get(Message.PROTOCOL_HEADERS);
		if(headers != null) {
			log.error("Header HTTP:");
			for(Object i : headers.keySet()){
				log.error("\t"+ i + ": " + headers.get(i));
			}
		} else {
			log.error("Header HTTP non disponibili");
		}
			
		@SuppressWarnings("unchecked")
		ArrayList<SoapHeader> soapheaders = (ArrayList<SoapHeader>) ((BindingProvider) client).getResponseContext().get(org.apache.cxf.headers.Header.HEADER_LIST);
		if(soapheaders != null) {
			log.error("SoapHeader:");
			for(SoapHeader header : soapheaders){
				Node o = (Node) header.getObject();
				log.error("\t" + XMLUtils.toString(o));
			}
		} else {
			log.error("SoapHeader non disponibili");
		}
	}
	
	@SuppressWarnings("unchecked")
	public byte[] getAttachment(BindingProvider client, String id) throws IOException {
		if(id.startsWith("cid:")) id = id.replace("cid:", "");
		Collection<Attachment> attachments = (Collection<Attachment>) client.getResponseContext().get(Message.ATTACHMENTS);
		for (Attachment attachment : attachments) {
			if(!id.equals(attachment.getId())) continue;
		    
			DataHandler data = attachment.getDataHandler();
		    InputStream is = data.getInputStream();
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    IOUtils.copy(is, baos);
		    baos.close(); 
		    is.close();
		    return baos.toByteArray();
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean save(EventWS event, IdUtente utente){
		boolean sendMail=true;
		int file = Integer.MIN_VALUE;
    	DBManager dbManager = DBManager.getInstance();
		Connection con = null;
		
		try{
			con = dbManager.getConnection();
			con.setAutoCommit(false);
			JDBCServiceManager serviceManager = new JDBCServiceManager(con, ServiceManagerFactory.getJDBCProperties(false));
			IEventoService eventoService = serviceManager.getEventoService();
			Evento evento = null;
			try {
				evento = eventoService.find(eventoService.newExpression().equals(Evento.model().EVENT_ID, Long.valueOf(event.getId()).intValue()));
			} catch(NotFoundException e) {}
			if(evento != null){
				//LogManager.getLoggerRaccoltaEventi().debug("Evento gia' in database. In lista per la cancellazione.");
				return true;
			}
			LogManager.getLoggerRaccoltaEventi().debug("Ricevuto un nuovo evento [" + event.getId() + "]["+ event.getParameterId() +"].");
			evento = new Evento();
			if(event.getEndDate() != null)
				evento.setEnddate(new java.util.Date(event.getEndDate().toGregorianCalendar().getTimeInMillis()));
			evento.setEventtypeCode(event.getEventType().getCode());
			evento.setEventtypeDescription(event.getEventType().getDescription());
			evento.setEventId(Long.valueOf(event.getId()).intValue());
			evento.setOwnerDescription(event.getOwner().getDescription());
			evento.setOwnerIdamministrazione(event.getOwner().getIdAmministrazione());
			evento.setOwnerIdsistema(event.getOwner().getIdSistema());
			evento.setParameterid(event.getParameterId());
			evento.setReason(event.getReason());
			evento.setStartdate(new java.util.Date(event.getStartDate().toGregorianCalendar().getTimeInMillis()));
			eventoService.create(evento);
			if(event.getProperties() != null){
				Property[] proprieta = event.getProperties().getProperties().toArray(new Property[1]);
				for(int i=0; i<proprieta.length;i++){
					IProprietaEventoService propeventoService = serviceManager.getProprietaEventoService();
					ProprietaEvento prop = new ProprietaEvento();
					prop.setPropertyId(event.getParameterId());
					prop.setPropertyKey(proprieta[i].getKey());
					prop.setPropertyValue(proprieta[i].getValue());
					propeventoService.create(prop);
				}
			}
			//Salvo il tutto su txt
			File eventiInfo = null;
			if(event.getParameterId().startsWith("T")){
				eventiInfo = new File(UtentiUtilities.getContestoDir(utente) + "EVENTI/" + event.getId() + ".xml");
			}
			else{
				ITicketServiceSearch ticketServiceSearch = serviceManager.getTicketServiceSearch();
				IPaginatedExpression exp = ticketServiceSearch.newPaginatedExpression();
				exp.equals(Ticket.model().IDTICKET, event.getParameterId());
				List<Ticket> ticket = ticketServiceSearch.findAll(exp);
				if(ticket != null && ticket.size() > 0){
					file = ticket.get(0).getFile();
					eventiInfo = new File(UtentiUtilities.getInboxDir(utente) + file + "/EVENTI/" + event.getId() + ".xml");
				}
				else{
					LogManager.getLoggerRaccoltaEventi().error("Nessuna trasmissione associata al ticket " + event.getParameterId() + " dell'esito " + event.getId());
					eventiInfo = new File(UtentiUtilities.getInboxDir(utente) + "/EVENTI_ORFANI/" + event.getId() + ".xml");
				}
			}
			FileOutputStream fos = null;
			try {
				if(!eventiInfo.exists()) eventiInfo.createNewFile();
				fos = new FileOutputStream(eventiInfo,true);
				marshaller.marshal( new JAXBElement(new QName("","eventi"), EventWS.class, event ), fos );
				fos.close();
			} catch (Exception e) {
				LogManager.getLoggerRaccoltaEventi().error("Impossibile salvare su fs l'esito " + event.getParameterId(),e);
				con.rollback();
				return false;
			}
			con.commit();
		}
		catch (SQLException sqle){
			sendMail=false;
			try{con.rollback();} catch(SQLException e){}
			LogManager.getLoggerRaccoltaEventi().error("Errore nel salvataggio degli Eventi:" + sqle,sqle);
			
			return false;
		}
		catch (Exception e){
			sendMail=false;
			try{con.rollback();} catch(SQLException ee){}
			LogManager.getLoggerRaccoltaEventi().error("Errore nel salvataggio degli Eventi:" + e,e);
			return false;
		}
		finally{
			dbManager.releaseConnection(con);
			//Se devo inviare la mail e non e' un evento di TabelleContesto...
			if(sendMail && file != Integer.MIN_VALUE){
				//Invio la mail e imposto come notificato il file.
				MailManager.postMail(event,file);
				IdTrasmissione idTrasmissione = new IdTrasmissione();
				idTrasmissione.setFile(file);
				idTrasmissione.setUtente(utente);
				Trasmissione.notifica(idTrasmissione);
			}
		}
    	return true;
    }
	
	
	
	public static boolean save(EventType eventType){
		try{
			ITipieventoService tipieventoService = ServiceManagerFactory.getServiceManager().getTipieventoService();
			Tipievento tipievento = null;
			try {
				tipievento = tipieventoService.find(tipieventoService.newExpression().equals(Tipievento.model().CODE, eventType.getCode()));
			} catch(NotFoundException e) {}
			if(tipievento != null) {
				//LogManager.getLoggerRaccoltaEventi().debug("Tipo Evento gia' in database.");
				return true;
			}
			LogManager.getLoggerRaccoltaEventi().debug("Ricevuto un nuovo Tipo evento [" + eventType.getDescription() + "].");
			tipievento = new Tipievento();
			tipievento.setCode(eventType.getCode());
			tipievento.setDescription(eventType.getDescription());
			tipieventoService.create(tipievento);
		}
		catch (Exception sqle){
			LogManager.getLoggerRaccoltaEventi().error("Errore nel salvataggio degli Eventi:" + sqle,sqle);
			
			return false;
		}
    	return true;
    }
}
