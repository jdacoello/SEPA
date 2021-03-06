/* This class implements a SEPA producer
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.pattern;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.protocol.SPARQL11Protocol;
import it.unibo.arces.wot.sepa.commons.request.UpdateRequest;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.security.SEPASecurityManager;

public class Producer extends Client implements IProducer {
	private static final Logger logger = LogManager.getLogger();
	
	protected String sparqlUpdate = null;
	protected String SPARQL_ID = "";
	private Bindings forcedBindings;
	
	private SPARQL11Protocol client;
	
	public Producer(JSAP appProfile,String updateID,SEPASecurityManager sm) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException  {
		super(appProfile,sm);
		
		if (appProfile.getSPARQLUpdate(updateID) == null) {
			logger.fatal("UPDATE ID [" +updateID+"] not found in "+appProfile.getFileName());
			throw new IllegalArgumentException("UPDATE ID [" +updateID+"] not found in "+appProfile.getFileName());
		}
		
		SPARQL_ID = updateID;
		
		sparqlUpdate = appProfile.getSPARQLUpdate(updateID);
		
		forcedBindings = appProfile.getUpdateBindings(updateID);
		
		client = new SPARQL11Protocol(sm);
	}
	
	public final Response update() throws SEPASecurityException, SEPAProtocolException, SEPAPropertiesException, SEPABindingsException {
		return update(0);
	}
	
	public final Response update(int timeout) throws SEPASecurityException, SEPAPropertiesException, SEPABindingsException, SEPAProtocolException{	 
		String authorizationHeader = null;
		
		if (isSecure()) authorizationHeader = sm.getAuthorizationHeader();
		
		UpdateRequest req = new UpdateRequest(appProfile.getUpdateMethod(SPARQL_ID), appProfile.getUpdateProtocolScheme(SPARQL_ID),appProfile.getUpdateHost(SPARQL_ID), appProfile.getUpdatePort(SPARQL_ID),
					appProfile.getUpdatePath(SPARQL_ID), appProfile.addPrefixesAndReplaceBindings(sparqlUpdate, addDefaultDatatype(forcedBindings,SPARQL_ID,false)),
					appProfile.getUsingGraphURI(SPARQL_ID), appProfile.getUsingNamedGraphURI(SPARQL_ID),authorizationHeader,timeout);
		 
		 Response retResponse = client.update(req);
		 
		 if (retResponse.isError()) {
			 if (isSecure()) {
				 ErrorResponse errorResponse = (ErrorResponse) retResponse;
				 if (errorResponse.isTokenExpiredError()) {
					 try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new SEPAProtocolException(e);
					}
					 
				 }
				 
				 authorizationHeader = sm.getAuthorizationHeader();
					
				 req = new UpdateRequest(appProfile.getUpdateMethod(SPARQL_ID), appProfile.getUpdateProtocolScheme(SPARQL_ID),appProfile.getUpdateHost(SPARQL_ID), appProfile.getUpdatePort(SPARQL_ID),
								appProfile.getUpdatePath(SPARQL_ID), appProfile.addPrefixesAndReplaceBindings(sparqlUpdate, addDefaultDatatype(forcedBindings,SPARQL_ID,false)),
								appProfile.getUsingGraphURI(SPARQL_ID), appProfile.getUsingNamedGraphURI(SPARQL_ID),authorizationHeader,timeout);
					 
				 retResponse = client.update(req);
			 }
		 }
		 
		 return retResponse;
	 }

	@Override
	public void close() throws IOException {
		client.close();
	}

	public final void setUpdateBindingValue(String variable, RDFTerm value) throws SEPABindingsException {
		forcedBindings.setBindingValue(variable, value);
	}
}
