package it.unibo.arces.wot.sepa.apps.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.SEPASecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;
import it.unibo.arces.wot.sepa.timing.Timings;

public class Sender extends Producer {
	private static final Logger logger = LogManager.getLogger();
	
	private String sender;
	
	public Sender(JSAP jsap, String senderURI,SEPASecurityManager sm)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(jsap, "SEND",sm);
		
		this.setUpdateBindingValue("sender", new RDFTermURI(senderURI));
		this.sender = senderURI;
	}
	
	public boolean sendMessage(String receiverURI,String text) {
		logger.info("SEND From: "+sender+" To: "+receiverURI+" "+text);
		
		long start = Timings.getTime();
		boolean ret = false;
		try {
			this.setUpdateBindingValue("receiver", new RDFTermURI(receiverURI));
			this.setUpdateBindingValue("text", new RDFTermLiteral(text));
			
			ret = update().isUpdateResponse();
		} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException | SEPABindingsException e) {
			logger.error(e.getMessage());
		}
		long stop = Timings.getTime();
		String msg = sender+receiverURI+text;
		Timings.log(msg.replace(" ", "_"), start, stop);
		return ret;
	}
}
