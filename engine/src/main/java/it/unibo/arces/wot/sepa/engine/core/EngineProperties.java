/* This class implements the configuration properties of the Semantic Event Processing Architecture (SEPA) Engine
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package it.unibo.arces.wot.sepa.engine.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;

import org.apache.logging.log4j.LogManager;

/**
 * <pre>
{
	"parameters": {
		"scheduler": {
			"queueSize": 100
		},
		"processor": {
			"updateTimeout": 60000,
			"queryTimeout": 60000,
			"maxConcurrentRequests": 5
		},
		"spu": {
			"timeout": 5000
		},
		"gates": {
			"security": {
				"enabled" : false,
				"ldap" : false
			},
			"paths": {
				"securePath": "/secure",
				"update": "/update",
				"query": "/query",
				"subscribe": "/subscribe",
				"register": "/oauth/register",
				"tokenRequest": "/oauth/token"
			},
			"ports": {
				"http": 8000,
				"https": 8443,
				"ws": 9000,
				"wss": 9443
			}
		}
	}
}
 * </pre>
 */
public class EngineProperties {
	private static final Logger logger = LogManager.getLogger();

	private String defaultsFileName = "engine.jpar";

	private JsonObject properties = new JsonObject();

	public EngineProperties(String propertiesFile) throws SEPAPropertiesException {

		if (propertiesFile == null) {
			throw new SEPAPropertiesException(new IllegalArgumentException("Properties file is null"));
		}

		FileReader in = null;
		try {
			in = new FileReader(propertiesFile);
			if (in != null) {
				properties = new JsonParser().parse(in).getAsJsonObject();
				if (properties.get("parameters") == null) {
					logger.warn("parameters key is missing");
					throw new SEPAPropertiesException(new NoSuchElementException("Parameters key is missing"));
				}
				properties = properties.get("parameters").getAsJsonObject();

				validate();
			}
			if (in != null)
				in.close();
		} catch (IOException e) {
			logger.warn(e.getMessage());

			defaults();

			try {
				storeProperties(defaultsFileName);
			} catch (IOException e1) {
				logger.error(e1.getMessage());
				throw new SEPAPropertiesException(e1);
			}

			logger.warn("USING DEFAULTS. Edit \"" + defaultsFileName + "\" (if needed) and run again the broker");
			// throw new SEPAPropertiesException(new FileNotFoundException(
			// "USING DEFAULTS. Edit \"" + defaultsFileName + "\" (if needed) and run again
			// the broker"));
		}
	}

	public EngineProperties(String propertiesFile, boolean secure) throws SEPAPropertiesException {
		this(propertiesFile);
		this.properties.get("gates").getAsJsonObject()
				.get("security").getAsJsonObject().addProperty("enabled", secure);
	}

	public String toString() {
		return properties.toString();
	}

	protected void defaults() {
		JsonObject parameters = new JsonObject();

		// Scheduler properties
		JsonObject scheduler = new JsonObject();
		scheduler.add("queueSize", new JsonPrimitive(100));
		scheduler.add("timeout", new JsonPrimitive(5000));
		parameters.add("scheduler", scheduler);

		// Processor properties
		JsonObject processor = new JsonObject();
		processor.add("updateTimeout", new JsonPrimitive(5000));
		processor.add("queryTimeout", new JsonPrimitive(5000));
		processor.add("maxConcurrentRequests", new JsonPrimitive(5));
		parameters.add("processor", processor);

		// SPU
		JsonObject spu = new JsonObject();
		spu.add("timeout", new JsonPrimitive(5000));
		parameters.add("spu", spu);

		// Gates
		JsonObject gates = new JsonObject();
		gates.add("secure", new JsonPrimitive(false));

		// Ports
		JsonObject ports = new JsonObject();
		ports.add("http", new JsonPrimitive(8000));
		ports.add("https", new JsonPrimitive(8443));
		ports.add("ws", new JsonPrimitive(9000));
		ports.add("wss", new JsonPrimitive(9443));

		// URI patterns
		JsonObject paths = new JsonObject();
		paths.add("securePath", new JsonPrimitive("/secure"));
		paths.add("update", new JsonPrimitive("/update"));
		paths.add("query", new JsonPrimitive("/query"));
		paths.add("subscribe", new JsonPrimitive("/subscribe"));
		paths.add("unsubscribe", new JsonPrimitive("/unsubscribe"));
		paths.add("register", new JsonPrimitive("/oauth/register"));
		paths.add("tokenRequest", new JsonPrimitive("/oauth/token"));

		gates.add("paths", paths);
		gates.add("ports", ports);
		parameters.add("gates", gates);

		properties.add("parameters", parameters);
	}

	protected void validate() throws SEPAPropertiesException {
		try {
			properties.getAsJsonObject("scheduler").get("queueSize").getAsInt();
		} catch (Exception e) {
			logger.error("scheduler-queueSize is missing");
			throw new SEPAPropertiesException("scheduler-queueSize is missing");
		}

		try {
			properties.getAsJsonObject("scheduler").get("timeout").getAsInt();
		} catch (Exception e) {
			logger.error("scheduler-timeout is missing");
			throw new SEPAPropertiesException("scheduler-timeout is missing");
		}

		try {
			properties.getAsJsonObject("processor").get("updateTimeout").getAsInt();
		} catch (Exception e) {
			logger.error("processor-updateTimeout is missing");
			throw new SEPAPropertiesException("processor-updateTimeout is missing");
		}

		try {
			properties.getAsJsonObject("processor").get("queryTimeout").getAsInt();
		} catch (Exception e) {
			logger.error("processor-queryTimeout is missing");
			throw new SEPAPropertiesException("processor-queryTimeout is missing");
		}

		try {
			properties.getAsJsonObject("processor").get("maxConcurrentRequests").getAsInt();
		} catch (Exception e) {
			logger.error("processor-maxConcurrentRequests is missing");
			throw new SEPAPropertiesException("processor-maxConcurrentRequests is missing");
		}

		try {
			properties.getAsJsonObject("spu").get("timeout").getAsInt();
		} catch (Exception e) {
			logger.error("spu-timeout is missing");
			throw new SEPAPropertiesException("spu-timeout is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("security");
		} catch (Exception e) {
			logger.error("gates-security is missing");
			throw new SEPAPropertiesException("gates-security is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("security").get("enabled").getAsBoolean();
		} catch (Exception e) {
			logger.error("gates-security-enabled is missing");
			throw new SEPAPropertiesException("gates-security-enabled is missing");
		}

//		if (properties.getAsJsonObject("gates").getAsJsonObject("security").get("enabled").getAsBoolean()) {
//			try {
//				properties.getAsJsonObject("gates").getAsJsonObject("security").get("cacertificate");
//			} catch (Exception e) {
//				logger.error("gates-security-certificate is missing");
//				throw new SEPAPropertiesException("gates-security-certificate is missing");
//			}
//			
//			try {
//				properties.getAsJsonObject("gates").getAsJsonObject("security").get("ldap").getAsBoolean();
//			} catch (Exception e) {
//				logger.error("gates-security-ldap is missing");
//				throw new SEPAPropertiesException("gates-security-certificate is missing");
//			}
////			
////			if (!properties.getAsJsonObject("gates").getAsJsonObject("security").get("type").getAsString().equals("jks") && !properties.getAsJsonObject("gates").getAsJsonObject("security").get("type").getAsString().equals("pem")) {
////				throw new SEPAPropertiesException("gates-security-type MUST be jks or pem");
////			}
////			
////			try {
////				properties.getAsJsonObject("gates").getAsJsonObject("security").get("path").getAsString();
////			} catch (Exception e) {
////				logger.error("gates-security-pwd is missing");
////				throw new SEPAPropertiesException("gates-security-pwd is missing");
////			}
////
////			try {
////				properties.getAsJsonObject("gates").getAsJsonObject("security").get("pwd").getAsString();
////			} catch (Exception e) {
////				logger.error("gates-security-pwd is missing");
////				throw new SEPAPropertiesException("gates-security-pwd is missing");
////			}
//		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("ports").get("http").getAsInt();
		} catch (Exception e) {
			logger.error("gates-ports-http is missing");
			throw new SEPAPropertiesException("gates-ports-http is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("ports").get("https").getAsInt();
		} catch (Exception e) {
			logger.error("gates-ports-https is missing");
			throw new SEPAPropertiesException("gates-ports-https is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("ports").get("ws").getAsInt();
		} catch (Exception e) {
			logger.error("gates-ports-ws is missing");
			throw new SEPAPropertiesException("gates-ports-ws is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("ports").get("wss").getAsInt();
		} catch (Exception e) {
			logger.error("gates-ports-wss is missing");
			throw new SEPAPropertiesException("gates-ports-wss is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("securePath").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-securePath is missing");
			throw new SEPAPropertiesException("gates-paths-securePath is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("update").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-update is missing");
			throw new SEPAPropertiesException("gates-paths-update is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("query").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-query is missing");
			throw new SEPAPropertiesException("gates-paths-query is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("subscribe").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-subscribe is missing");
			throw new SEPAPropertiesException("gates-paths-subscribe is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("register").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-register is missing");
			throw new SEPAPropertiesException("gates-paths-register is missing");
		}

		try {
			properties.getAsJsonObject("gates").getAsJsonObject("paths").get("tokenRequest").getAsString();
		} catch (Exception e) {
			logger.error("gates-paths-tokenRequest is missing");
			throw new SEPAPropertiesException("gates-paths-tokenRequest is missing");
		}
	}

	private void storeProperties(String propertiesFile) throws IOException {
		FileWriter out = new FileWriter(propertiesFile);
		out.write(properties.toString());
		out.close();
	}

	public boolean isSecure() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("security").get("enabled").getAsBoolean();
		} catch (Exception e) {
			return false;
		}
	}

	public int getMaxConcurrentRequests() {
		try {
			return properties.getAsJsonObject("processor").get("maxConcurrentRequests").getAsInt();
		} catch (Exception e) {
			return 5;
		}
	}

	public int getUpdateTimeout() {
		try {
			return properties.getAsJsonObject("processor").get("updateTimeout").getAsInt();
		} catch (Exception e) {
			return 5000;
		}
	}

	public int getQueryTimeout() {
		try {
			return properties.getAsJsonObject("processor").get("queryTimeout").getAsInt();
		} catch (Exception e) {
			return 5000;
		}
	}

	public int getSchedulingQueueSize() {
		try {
			return properties.getAsJsonObject("scheduler").get("queueSize").getAsInt();
		} catch (Exception e) {
			return 1000;
		}
	}

	public int getWsPort() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("ports").get("ws").getAsInt();
		} catch (Exception e) {
			return 9000;
		}
	}

	public int getHttpPort() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("ports").get("http").getAsInt();
		} catch (Exception e) {
			return 8000;
		}
	}

	public int getHttpsPort() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("ports").get("https").getAsInt();
		} catch (Exception e) {
			return 8443;
		}
	}

	public int getWssPort() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("ports").get("wss").getAsInt();
		} catch (Exception e) {
			return 9443;
		}
	}

	public String getUpdatePath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("update").getAsString();
		} catch (Exception e) {
			return "/update";
		}
	}

	public String getSubscribePath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("subscribe").getAsString();
		} catch (Exception e) {
			return "/subscribe";
		}
	}

	public String getQueryPath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("query").getAsString();
		} catch (Exception e) {
			return "/query";
		}
	}

	public String getRegisterPath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("register").getAsString();
		} catch (Exception e) {
			return "/oauth/register";
		}
	}

	public String getTokenRequestPath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("tokenRequest").getAsString();
		} catch (Exception e) {
			return "/oauth/token";
		}
	}

	public String getSecurePath() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("paths").get("securePath").getAsString();
		} catch (Exception e) {
			return "/secure";
		}
	}

	public int getSPUProcessingTimeout() {
		try {
			return properties.getAsJsonObject("spu").get("timeout").getAsInt();
		} catch (Exception e) {
			return 2000;
		}
	}

	public boolean isUpdateReliable() {
		try {
			return properties.getAsJsonObject("processor").get("reliableUpdate").getAsBoolean();
		} catch (Exception e) {
			return true;
		}
	}

	public int getSchedulerTimeout() {
		try {
			return properties.getAsJsonObject("scheduler").get("timeout").getAsInt();
		} catch (Exception e) {
			return 60000;
		}
	}
	
	public boolean isLDAPEnabled() {
		try {
			return properties.getAsJsonObject("gates").getAsJsonObject("security").get("ldap").getAsBoolean();
		} catch (Exception e) {
			return false;
		}
	}
	
}
