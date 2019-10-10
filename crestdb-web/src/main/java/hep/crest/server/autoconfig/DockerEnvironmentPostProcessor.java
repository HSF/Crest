/**
 * 
 */
package hep.crest.server.autoconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author formica
 * This file requires a configuration file to be created in src/main/resource:
 * spring.factories
 * The content is something like this
 * org.springframework.boot.env.EnvironmentPostProcessor=hep.crest.server.autoconfig.DockerEnvironmentPostProcessor
 */
public class DockerEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "crestpassProperties";
	private static final Map<String, String> secretsMap;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	static {
		secretsMap = new HashMap<>();
		secretsMap.put("/run/secrets/vhfdb_password", "crest.db.password");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.boot.env.EnvironmentPostProcessor#postProcessEnvironment(
	 * org.springframework.core.env.ConfigurableEnvironment,
	 * org.springframework.boot.SpringApplication)
	 */
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		log.info("POSTPROCESS ENV is configuring {}",PROPERTY_SOURCE_NAME);
		Map<String, Object> map = new HashMap<>();
		try {
			for (Map.Entry<String, String> entry : secretsMap.entrySet()) {
				String springkey = entry.getValue();
				String respath = entry.getKey();
				loadSecret(respath, springkey, environment, map);
			}
		} catch (CdbServiceException e) {
			log.error("POSTPROCESS ENV Exception {}",e.getMessage());
		}
	}

	/**
	 * @param secpath
	 * @param springkey
	 * @param environment
	 * @param map
	 * @throws VhfServiceException
	 */
	private void loadSecret(String secpath, String springkey, ConfigurableEnvironment environment,
			Map<String, Object> map) throws CdbServiceException {
		Resource resource = new FileSystemResource(secpath);
		try {
			if (resource.exists()) {
				String mPassword = getStringFromInputStream(resource.getInputStream());
				mPassword = mPassword.replaceAll("\\n", "");
				map.put(springkey, mPassword);
				if (springkey.equals("store.password")) {
					System.setProperty("javax.net.ssl.trustStorePassword", mPassword);
					System.setProperty("javax.net.ssl.trustStore", "/run/secrets/truststore.jks");
				}
				addOrReplace(environment.getPropertySources(), map);
			} else {
				log.warn("CANNOT FIND secret in {} for {} ",secpath,PROPERTY_SOURCE_NAME);
			}
		} catch (IOException e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

	/**
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String getStringFromInputStream(InputStream input) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(input, writer, StandardCharsets.UTF_8);
		return writer.toString();
	}

	/**
	 * @param propertySources
	 * @param map
	 */
	private void addOrReplace(MutablePropertySources propertySources, Map<String, Object> map) {
		MapPropertySource target = null;
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
			if (source instanceof MapPropertySource) {
				target = (MapPropertySource) source;
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String key = entry.getKey();
					if (!target.containsProperty(key)) {
						target.getSource().put(key, entry.getValue());
					} else {
						log.debug("Key {} is already in {}",key ,target.getName());
					}
				}
			}
		}
		if (target == null) {
			target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
		}
		if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
			propertySources.addFirst(target);
		}
	}

	/**
	 * Utility method
	 * 
	 * @param propertySources
	 */
	@SuppressWarnings("unused")
	private void dumpAll(PropertySources propertySources) {
		for (PropertySource<?> propertySource : propertySources) {
			log.info("Found property source: {} : {}",propertySource.getName(), propertySource);
			if (propertySource instanceof MapPropertySource) {
				String[] keys = ((MapPropertySource) propertySource).getPropertyNames();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					Object val = ((MapPropertySource) propertySource).getProperty(key);
					log.info("   contains property: {} : {} ",key,val);
				}
			}
		}
	}
}

