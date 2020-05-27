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
package org.govmix.proxy.igrue.web.service;


import javax.xml.ws.BindingProvider;

import it.mef.csp.webservices.dto.CodiceProceduraAttivazione;
import it.mef.csp.webservices.dto.Credenziali;
import it.mef.csp.webservices.messages.trasmissione.*;

import org.apache.log4j.Logger;
import org.govmix.proxy.igrue.managers.LogManager;
import org.govmix.proxy.igrue.web.ejb.utils.UtentiUtilities;
import org.govmix.proxy.igrue.ws.IgrueUtils;

public class RichiestaCodiceAttivazioneImpl implements IRichiestaCodiceAttivazione {

	public CodiceProceduraAttivazione getCodiceProceduraAttivazione() throws Exception {
		Logger log = LogManager.getLoggerSpedizioneDati();
		Credenziali credenziali = UtentiUtilities.getCredenziali();
		log.info("[WebUI] Richiesto nuovo CodiceProceduraAttivazione per " + credenziali.getIdAmministrazione() + credenziali.getIdSistema());
		AssegnazioneCodProcAttIn in = new AssegnazioneCodProcAttIn();
		AssegnazioneCodProcAttOut out = null;
		it.eng.csp.webservices.Trasmissione client = null;
		IgrueUtils igrueUtils = new IgrueUtils();
		try {

			if (UtentiUtilities.getStatus("http://149.202.54.135/igrue"))
			{ in.setCredenziali(credenziali);
				in.setProtocollo(10);			}

			client = igrueUtils.getTrasmissioneClient();
			out = client.assegnazioneCodProcAtt(in);
		} catch (Exception e1) {
			log.error("[WebUI] Errore nella richiesta di un nuovo CodiceProceduraAttivazione.", e1);
			igrueUtils.logHttpResponse((BindingProvider)client, log);
			throw new Exception("Errore durante la richiesta di un nuovo codice di procedura di attivazione", e1);
		} 
		if(out.getEsitoOperazione().getCodiceEsito()!=0){
			log.error("[WebUI] Errore durante la richiesta di un nuovo codice di procedura di attivazione: " + out.getEsitoOperazione().getDescrizioneEsito());
			throw new Exception("Errore durante la richiesta di un nuovo codice di procedura di attivazione: " + out.getEsitoOperazione().getDescrizioneEsito());
		}
		log.info("[WebUI] Ottenuto nuovo CodiceProceduraAttivazione " + out.getCodiceProceduraAttivazione().getIdProceduraAttivazione());
		return out.getCodiceProceduraAttivazione();
	}
}
