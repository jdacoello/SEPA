package it.unibo.arces.wot.sepa.engine.gates.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.ExceptionLogger;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.engine.bean.EngineBeans;
import it.unibo.arces.wot.sepa.engine.core.EngineProperties;
import it.unibo.arces.wot.sepa.engine.dependability.Dependability;
import it.unibo.arces.wot.sepa.engine.protocol.oauth.JWTRequestHandler;
import it.unibo.arces.wot.sepa.engine.protocol.oauth.RegisterHandler;
import it.unibo.arces.wot.sepa.engine.protocol.sparql11.SecureQueryHandler;
import it.unibo.arces.wot.sepa.engine.protocol.sparql11.SecureUpdateHandler;
import it.unibo.arces.wot.sepa.engine.scheduling.Scheduler;

public class HttpsGate {
	protected static final Logger logger = LogManager.getLogger();

	protected EngineProperties properties;
	protected Scheduler scheduler;

	protected String serverInfo = "SEPA Gate-HTTPS/1.1";
	protected HttpServer server = null;

	protected IOReactorConfig config = IOReactorConfig.custom().setTcpNoDelay(true).setSoReuseAddress(true).build();

	public HttpsGate(EngineProperties properties, Scheduler scheduler) throws SEPASecurityException, SEPAProtocolException {

		try {
			server = ServerBootstrap.bootstrap().setListenerPort(properties.getHttpsPort()).setServerInfo(serverInfo)
					.setIOReactorConfig(config).setSslContext(Dependability.getSSLContext())
					.setExceptionLogger(ExceptionLogger.STD_ERR)
					.registerHandler(properties.getRegisterPath(), new RegisterHandler())
					.registerHandler(properties.getSecurePath() + properties.getQueryPath(),
							new SecureQueryHandler(scheduler))
					.registerHandler(properties.getSecurePath() + properties.getUpdatePath(),
							new SecureUpdateHandler(scheduler))
					.registerHandler(properties.getTokenRequestPath(), new JWTRequestHandler())
					.registerHandler("/echo", new EchoHandler()).create();
		} catch (IllegalArgumentException e) {
			throw new SEPASecurityException(e);
		}
		
		try {
			server.start();
		} catch (IOException e) {
			throw new SEPAProtocolException(e);
		}

		System.out.println("SPARQL 1.1 SE Query  | " + EngineBeans.getSecureQueryURL());
		System.out.println("SPARQL 1.1 SE Update | " + EngineBeans.getSecureUpdateURL());
		System.out.println("Client registration  | " + EngineBeans.getRegistrationURL());
		System.out.println("Token request        | " + EngineBeans.getTokenRequestURL());
	}
	
	public void shutdown() {
		server.shutdown(5, TimeUnit.SECONDS);
		
		try {
			server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logger.debug(serverInfo+" interrupted: " + e.getMessage());
		}
	}
}
