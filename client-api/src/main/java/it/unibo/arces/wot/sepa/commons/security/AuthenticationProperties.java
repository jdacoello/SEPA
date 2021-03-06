package it.unibo.arces.wot.sepa.commons.security;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.JWTResponse;

/**
 * The set of properties used for client authentication
 * 
 * 
 * <pre>
	"oauth": {
		"enable" : true,
		"register": "https://localhost:8443/oauth/register",
		"tokenRequest": "https://localhost:8443/oauth/token",
		"client_id": "jaJBrmgtqgW9jTLHeVbzSCH6ZIN1Qaf3XthmwLxjhw3WuXtt7VELmfibRNvOdKLs",
		"client_secret": "fkITPTMsHUEb9gVVRMP5CAeIE1LrfBYtNLdqtlTVZ/CqgqcuzEw+ZcVegW5dMnIg",
		"jwt": "xabtQWoH8RJJk1FyKJ78J8h8i2PcWmAugfJ4J6nMd+1jVSoiipV4Pcv8bH+8wJLJ2yRaVage8/TzdZJiz2jdRP8bhkuNzFhGx6N1/1mgmvfKihLheMmcU0pLj5uKOYWFb+TB98n1IpNO4G69lia2YoR15LScBzibBPpmKWF+XAr5TeDDHDZQK4N3VBS/e3tFL/yOhkfC9Mw45s3mz83oydQazps2cFzookIhydKJWfvx34vSSnhpkfcdYbZ+7KDaK5uCw8It/0FKvsuW0MAboo4X49sDS+AHTOnVUf67wnnPqJ2M1thThv3dIr/WNn+8xJovJWkwcpGP4T7nH7MOCfZzVnKTHr4hN3q14VUWHYkfP7DEKe7LScGYaT4RcuIfNmywI4fAWabAI4zqedYbd5lXmYhbSmXviPTOQPKxhmZptZ6F5Q178nfK6Bik4/0PwUlgMsC6oVFeJtyPWvjfEP0nx9tGMOt+z9Rvbd7enGWRFspUQJS2zzmGlHW1m5QNFdtOCfTLUOKkyZV4JUQxI1CaP+QbIyIihuQDvIMbmNgbvDNBkj9VQOzg1WB7mj4nn4w7T8I9MpOxAXxnaPUvDk8QnL/5leQcUiFVTa1zlzambQ8xr/BojFB52fIz8LsrDRW/+/0CJJVTFYD6OZ/gepFyLK4yOu/rOiTLT5CF9H2NZQd7bi85zSmi50RHFa3358LvL50c4G84Gz7mkDTBV9JxBhlWVNvD5VR58rPcgESwlGEL2YmOQCZzYGWjTc5cyI/50ZX83sTlTbfs+Tab3pBlsRQu36iNznleeKPj6uVvql+3uvcjMEBqqXvj8TKxMi9tCfHA1vt9RijOap8ROHtnIe4iMovPzkOCMiHJPcwbnyi+6jHbrPI18WGghceZQT23qKHDUYQo2NiehLQG9MQZA1Ncx2w4evBTBX8lkBS4aLoCUoTZTlNFSDOohUHJCbeig9eV77JbLo0a4+PNH9bgM/icSnIG5TidBGyJpEkVtD7+/KphwM89izJam3OT",
		"expires": "04/5tRBT5n/VJ0XQASgs/w==",
		"type": "XPrHEX2xHy+5IuXHPHigMw=="
	}
}
 * </pre>
 */

public class AuthenticationProperties {
	/** The log4j2 logger. */
	private static final Logger logger = LogManager.getLogger();

	/** The properties file. */
	protected final File propertiesFile;

	private final Encryption encryption;

	private final boolean enabled;

	private final String registrationURL;
	private final String tokenRequestURL;

	private String clientId = null;
	private String clientSecret = null;

	private String jwt = null;
	private long expires = -1;
	private String type = null;

	public AuthenticationProperties(String jsapFileName, byte[] secret) throws SEPAPropertiesException {
		propertiesFile = new File(jsapFileName);

		try {
			FileReader in = new FileReader(propertiesFile);
			JsonObject jsap = new JsonParser().parse(in).getAsJsonObject();
			in.close();
			
			if (secret != null)
				encryption = new Encryption(secret);
			else
				encryption = new Encryption();

			if (jsap.has("oauth")) {
				JsonObject oauthJsonObject = jsap.getAsJsonObject("oauth");
				if (oauthJsonObject.has("enable"))
					enabled = oauthJsonObject.get("enable").getAsBoolean();
				else {
					enabled = false;
				}

				if (enabled) {
					registrationURL = oauthJsonObject.get("register").getAsString();
					tokenRequestURL = oauthJsonObject.get("tokenRequest").getAsString();

					if (oauthJsonObject.has("client_id"))
						clientId = encryption.decrypt(oauthJsonObject.get("client_id").getAsString());
					if (oauthJsonObject.has("client_secret"))
						clientSecret = encryption.decrypt(oauthJsonObject.get("client_secret").getAsString());
					if (oauthJsonObject.has("jwt"))
						jwt = encryption.decrypt(oauthJsonObject.get("jwt").getAsString());
					if (oauthJsonObject.has("expires"))
						expires = Long.decode(encryption.decrypt(oauthJsonObject.get("expires").getAsString()));
					if (oauthJsonObject.has("type"))
						type = encryption.decrypt(oauthJsonObject.get("type").getAsString());

				} else {
					registrationURL = null;
					tokenRequestURL = null;
				}
			} else {
				enabled = false;
				registrationURL = null;
				tokenRequestURL = null;
			}
		} catch (Exception e) {
			throw new SEPAPropertiesException(e.getMessage());
		}
	}

	public AuthenticationProperties(String jsapFileName) throws SEPAPropertiesException, SEPASecurityException {
		this(jsapFileName, null);
	}

	public AuthenticationProperties() {
		enabled = false;
		registrationURL = null;
		tokenRequestURL = null;
		propertiesFile = null;
		encryption = new Encryption();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getRegisterUrl() {
		return registrationURL;
	}

	public String getTokenRequestUrl() {
		return tokenRequestURL;
	}

	/**
	 * Checks if is token expired.
	 *
	 * @return true, if is token expired
	 * @throws SEPASecurityException
	 * @throws NumberFormatException
	 */
	public synchronized boolean isTokenExpired() {
		return getExpiringTime() == 0;
	}

	/**
	 * Gets the expiring seconds.
	 *
	 * @return the expiring seconds
	 * @throws SEPASecurityException
	 * @throws NumberFormatException
	 */
	public synchronized long getExpiringTime() {
		try {
			long now = new Date().getTime();

			logger.trace("@getExpiringTime Diff:" + (expires - now) + " Now: " + now + " Expires: " + expires);

			if (expires - now < 0)
				return 0;
			return expires - now;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Gets the access token.
	 *
	 * @return the access token
	 */
	public synchronized String getBearerAuthorizationHeader() {
		if (jwt != null)
			return "Bearer " + jwt;
		else
			return null;
	}

	public synchronized String getToken() {
		return jwt;
	}

	/**
	 * Gets the token type.
	 *
	 * @return the token type
	 */
	public synchronized String getTokenType() {
		return type;
	}

	/**
	 * Gets the basic authorization.
	 *
	 * @return the basic authorization
	 * @throws SEPASecurityException
	 */
	public synchronized String getBasicAuthorizationHeader() throws SEPASecurityException {
		if (clientId != null && clientSecret != null) {
			String plainString = clientId + ":" + clientSecret;
			try {
				return "Basic " + new String(Base64.getEncoder().encode(plainString.getBytes("UTF-8")), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new SEPASecurityException(e);
			}
		} else
			return null;
	}

	/**
	 * Sets the credentials.
	 *
	 * @param id     the username
	 * @param secret the password
	 */
	public synchronized void setCredentials(String id, String secret)
			throws SEPAPropertiesException, SEPASecurityException {
		logger.debug("@setCredentials Id: " + id + " Secret:" + secret);

		clientId = id;
		clientSecret = secret;
	}

	/**
	 * Sets the JWT.
	 *
	 * @param jwt the JSON Web Token
	 * @throws SEPAPropertiesException
	 * @throws SEPASecurityException
	 *
	 */
	public void setJWT(JWTResponse jwt) throws SEPASecurityException, SEPAPropertiesException {
		logger.debug("@setJWT: " + jwt);

		this.expires = new Date().getTime() + 1000 * jwt.getExpiresIn();
		this.jwt = jwt.getAccessToken();
		this.type = jwt.getTokenType();
	}

	/**
	 * Store properties.
	 *
	 * @param propertiesFile the properties file
	 * @throws SEPAPropertiesException
	 * @throws SEPASecurityException
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	public void storeProperties() throws SEPAPropertiesException, SEPASecurityException {
		if (propertiesFile == null) return;
		
		try {	
			FileReader in = new FileReader(propertiesFile);
			JsonObject jsap = new JsonParser().parse(in).getAsJsonObject();
			in.close();
			
			jsap.add("oauth", new JsonObject());
			jsap.getAsJsonObject("oauth").add("enable", new JsonPrimitive(enabled));

			if (registrationURL != null)
				jsap.getAsJsonObject("oauth").add("register", new JsonPrimitive(registrationURL));
			if (tokenRequestURL != null)
				jsap.getAsJsonObject("oauth").add("tokenRequest", new JsonPrimitive(tokenRequestURL));
			if (clientId != null)
				jsap.getAsJsonObject("oauth").add("client_id", new JsonPrimitive(encryption.encrypt(clientId)));
			if (clientSecret != null)
				jsap.getAsJsonObject("oauth").add("client_secret", new JsonPrimitive(encryption.encrypt(clientSecret)));

			if (jwt != null)
				jsap.getAsJsonObject("oauth").add("jwt", new JsonPrimitive(encryption.encrypt(jwt)));
			if (expires != -1)
				jsap.getAsJsonObject("oauth").add("expires",
						new JsonPrimitive(encryption.encrypt(String.format("%d", expires))));
			if (type != null)
				jsap.getAsJsonObject("oauth").add("type", new JsonPrimitive(encryption.encrypt(type)));

			FileWriter out = new FileWriter(this.propertiesFile.getPath());
			out.write(jsap.toString());
			out.close();
		} catch (IOException e) {
			throw new SEPAPropertiesException(e);
		}
	}
}
