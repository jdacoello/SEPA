package it.unibo.arces.wot.sepa.pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unibo.arces.wot.sepa.ConfigurationProvider;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.SEPASecurityManager;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.commons.response.Response;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

public class ITPattern {

	protected final Logger logger = LogManager.getLogger();
	protected static JSAP app = null;
	protected static SEPASecurityManager sm = null;

	protected static ITConsumer consumerAll;
	protected static Producer randomProducer;
	protected static ITAggregator randomAggregator;
	protected static ITConsumer consumerRandom1;

	@BeforeClass
	public static void init() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		try {
			app = new ConfigurationProvider().getJsap();
		} catch (SEPAPropertiesException | SEPASecurityException e) {
			assertFalse("Configuration not found", false);
		}

		if (app.isSecure()) {
			sm = new ConfigurationProvider().buildSecurityManager();
			Response ret = sm.register("SEPATest");

			assertFalse(ret.isError());
		}

		consumerAll = new ITConsumer(app, "ALL", sm);
		randomProducer = new Producer(app, "RANDOM", sm);
		randomAggregator = new ITAggregator(app, "RANDOM", "RANDOM1", sm);
		consumerRandom1 = new ITConsumer(app, "RANDOM1", sm);
	}

	@Test(timeout = 5000)
	public void subscribe() throws InterruptedException, SEPASecurityException, IOException, SEPAPropertiesException,
			SEPAProtocolException, SEPABindingsException {
		consumerAll.subscribe();
	}

	@Test(timeout = 5000)
	public void produce() throws InterruptedException, SEPASecurityException, IOException, SEPAPropertiesException,
			SEPAProtocolException, SEPABindingsException {
		Response ret = randomProducer.update();
		assertFalse(ret.isError());
	}

	@Test(timeout = 15000)
	public void produceX50() throws InterruptedException, SEPASecurityException, IOException, SEPAPropertiesException,
			SEPAProtocolException, SEPABindingsException {
		for (int i = 0; i < 50; i++) {
			Response ret = randomProducer.update();
			assertFalse("Failed on update: "+i,ret.isError());
		}
	}

	@Test(timeout = 5000)
	public void subscribeAndResults() throws InterruptedException, SEPASecurityException, IOException,
			SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		consumerAll.subscribe();
		consumerAll.waitNotification();
	}

	@Test(timeout = 5000)
	public void notification() throws InterruptedException, SEPASecurityException, IOException, SEPAPropertiesException,
			SEPAProtocolException, SEPABindingsException {
		consumerAll.subscribe();
		consumerAll.waitNotification();

		randomProducer.update();

		consumerAll.waitNotification();
	}

	@Test(timeout = 5000)
	public void aggregation() throws InterruptedException, SEPASecurityException, IOException, SEPAPropertiesException,
			SEPAProtocolException, SEPABindingsException {
		consumerRandom1.subscribe();
		consumerRandom1.waitNotification();

		randomAggregator.subscribe();
		randomAggregator.waitNotification();

		randomProducer.update();

		randomAggregator.waitNotification();
		consumerRandom1.waitNotification();
	}

	@Test(timeout = 5000)
	public void aggregationX10() throws InterruptedException, SEPASecurityException, IOException,
			SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		consumerRandom1.subscribe();
		consumerRandom1.waitNotification();

		randomAggregator.subscribe();
		randomAggregator.waitNotification();

		for (int i = 0; i < 10; i++) {
			randomProducer.update();

			randomAggregator.waitNotification();
			consumerRandom1.waitNotification();
		}
	}
}
