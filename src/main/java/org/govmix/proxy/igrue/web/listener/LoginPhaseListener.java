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
package org.govmix.proxy.igrue.web.listener;



import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.govmix.proxy.igrue.web.mbean.LoginBean;

public class LoginPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -7545768911858875216L;
	private static Logger log = Logger.getLogger(LoginPhaseListener.class);
	
	private static String[] allowedPages = {"login","timeout", "form/utente", "list/utenti"};
		
	public void afterPhase(PhaseEvent event) {
		try{

			FacesContext fc = event.getFacesContext();
			
			ExternalContext ec = fc.getExternalContext();
			LoginBean lb = (LoginBean)ec.getSessionMap().get("loginBean");
	        //controllo se sono nella pagina di login
			boolean isLogged = lb == null ? false : lb.getIsLoggedIn();
			
			UIViewRoot vr = fc.getViewRoot();
			//se il viewroot e' == null allora vuol dire ke ho perso tutte le informazioni sulla
			//sullo stato jsf quindi devo riautenticarmi
	        boolean allowedPage = vr!=null ? isAllowedPage(vr.getViewId()) : false;
	        //se non sono nella pagina di login e se non sono loggato
	        if (!allowedPage && !isLogged) {
	        	addError(fc, "Autenticazione richiesta.");
	            NavigationHandler nh = fc.getApplication().getNavigationHandler();
	            nh.handleNavigation(fc, null, "login");
	        }
		}catch (Exception e) {
			log.error(e,e);
		}
	}

	private boolean isAllowedPage(String viewId){
		
		for (int i = 0; i < allowedPages.length; i++) {
			String page = allowedPages[i];
			//controllo se la pagina richiesta e' tra quelle permesse
			if(StringUtils.contains(viewId, page))
				return true;
		}
		return false;
	}
	
	public void beforePhase(PhaseEvent event) {
		
	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

	private void addError(FacesContext context, String message)  
	{  
		FacesMessage fMessage = new FacesMessage(message);  
		if (fMessage != null)  
		{  
			FacesContext facesContext = FacesContext.getCurrentInstance();  
			fMessage.setSeverity(FacesMessage.SEVERITY_ERROR);  
			facesContext.addMessage(null, fMessage);  
		}  
	}
	
}
