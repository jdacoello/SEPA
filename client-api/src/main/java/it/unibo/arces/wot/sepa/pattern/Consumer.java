/* This class abstracts a consumer of the SEPA Application Design Pattern
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

import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTerm;
import it.unibo.arces.wot.sepa.api.SubscriptionProtocol;
import it.unibo.arces.wot.sepa.api.protocols.websocket.WebsocketSubscriptionProtocol;
import it.unibo.arces.wot.sepa.api.SPARQL11SEProtocol;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.request.SubscribeRequest;
import it.unibo.arces.wot.sepa.commons.request.UnsubscribeRequest;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.security.SEPASecurityManager;

public abstract class Consumer extends Client implements IConsumer {
	private static final Logger logger = LogManager.getLogger();

	protected String sparqlSubscribe = null;
	protected String subID = "";
	private Bindings forcedBindings;

	protected SPARQL11SEProtocol client;

	public Consumer(JSAP appProfile, String subscribeID, SEPASecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException {
		super(appProfile,sm);

		if (subscribeID == null) {
			logger.fatal("Subscribe ID is null");
			throw new SEPAProtocolException(new IllegalArgumentException("Subscribe ID is null"));
		}
		
		if (appProfile.getSPARQLQuery(subscribeID) == null) {
			logger.fatal("SUBSCRIBE ID [" + subscribeID + "] not found in " + appProfile.getFileName());
			throw new IllegalArgumentException(
					"SUBSCRIBE ID [" + subscribeID + "] not found in " + appProfile.getFileName());
		}

		subID = subscribeID;
		
		sparqlSubscribe = appProfile.getSPARQLQuery(subscribeID);

		forcedBindings = appProfile.getQueryBindings(subscribeID);

		if (sparqlSubscribe == null) {
			logger.fatal("SPARQL subscribe is null");
			throw new SEPAProtocolException(new IllegalArgumentException("SPARQL subscribe is null"));
		}

		// Subscription protocol
		SubscriptionProtocol protocol = null;
		protocol = new WebsocketSubscriptionProtocol(appProfile.getSubscribeHost(subscribeID),
				appProfile.getSubscribePort(subscribeID), appProfile.getSubscribePath(subscribeID));
		protocol.setHandler(this);
		if (appProfile.isSecure()) protocol.enableSecurity(sm);

		client = new SPARQL11SEProtocol(protocol,sm);
	}
	
	public final void setSubscribeBindingValue(String variable, RDFTerm value) throws SEPABindingsException {
		forcedBindings.setBindingValue(variable, value);
	}

	public final void subscribe(long timeout) throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		String authorizationHeader = null;
		
		if (isSecure()) authorizationHeader = sm.getAuthorizationHeader();
		
		client.subscribe(new SubscribeRequest(appProfile.addPrefixesAndReplaceBindings(sparqlSubscribe, addDefaultDatatype(forcedBindings,subID,true)), null, appProfile.getDefaultGraphURI(subID),
				appProfile.getNamedGraphURI(subID),
				authorizationHeader,timeout));
	}

	public final void unsubscribe(long timeout) throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException {
		logger.debug("UNSUBSCRIBE " + subID);

		String authorizationHeader = null;
		
		if (isSecure()) authorizationHeader = sm.getAuthorizationHeader();
		
		client.unsubscribe(
				new UnsubscribeRequest(subID, authorizationHeader,timeout));
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public void onSemanticEvent(Notification notify) {		
		ARBindingsResults results = notify.getARBindingsResults();

		BindingsResults added = results.getAddedBindings();
		BindingsResults removed = results.getRemovedBindings();

		logger.debug("onSemanticEvent: "+notify.getSpuid()+" "+notify.getSequence());
		
		if (notify.getSequence() == 0) {
			onFirstResults(added);
			return;
		}
		
		onResults(results);
		
		// Dispatch different notifications based on notify content
		if (!added.isEmpty())
			onAddedResults(added);
		if (!removed.isEmpty())
			onRemovedResults(removed);
	}
}
