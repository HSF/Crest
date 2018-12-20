/**
 * 
 */
package hep.crest.server.autoconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
 *
 */
public class DockerEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "crestpassProperties";
	private static final Map<String, String> secretsMap;
	static {
		secretsMap = new HashMap<>();
		secretsMap.put("/run/secrets/vhfdb_password", "svom.service.postgres_password");
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
		System.out.println("POSTPROCESS ENV is configuring " + PROPERTY_SOURCE_NAME);
		Map<String, Object> map = new HashMap<>();
		try {
			for (String respath : secretsMap.keySet()) {
				String springkey = secretsMap.get(respath);
				loadSecret(respath, springkey, environment, map);
			}
		} catch (CdbServiceException e) {
			System.err.println("POSTPROCESS ENV Exception "+e.getMessage());
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
				System.out.println("CANNOT FIND secret in " + secpath + " for " + PROPERTY_SOURCE_NAME);
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
		IOUtils.copy(input, writer, "UTF-8");
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
				for (String key : map.keySet()) {
					if (!target.containsProperty(key)) {
						target.getSource().put(key, map.get(key));
					} else {
						System.out.println("Key " + key + " is already in " + target.getName());
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
	private void dumpAll(PropertySources propertySources) {
		for (PropertySource<?> propertySource : propertySources) {
			System.out
					.println("Found property source: " + propertySource.getName() + " : " + propertySource.toString());
			if (propertySource instanceof MapPropertySource) {
				String keys[] = ((MapPropertySource) propertySource).getPropertyNames();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					Object val = ((MapPropertySource) propertySource).getProperty(key);
					System.out.println("   contains property: " + key + " : " + val);
				}
			}
		}
	}
}
