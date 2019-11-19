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
 * Post processor to retrieve secrets from Docker. This file requires a
 * configuration file to be created in src/main/resource: spring.factories The
 * content is something like this:
 * org.springframework.boot.env.EnvironmentPostProcessor=
 * hep.crest.server.autoconfig.DockerEnvironmentPostProcessor
 *
 * @author formica
 */
public class DockerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * The property source.
     */
    private static final String PROPERTY_SOURCE_NAME = "crestpassProperties";

    /**
     * The secret map.
     */
    private static final Map<String, String> SECRETS_MAP;

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static {
        SECRETS_MAP = new HashMap<>();
        SECRETS_MAP.put("/run/secrets/vhfdb_password", "crest.db.password");
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
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            SpringApplication application) {
        log.info("POSTPROCESS ENV is configuring {}", PROPERTY_SOURCE_NAME);
        final Map<String, Object> map = new HashMap<>();
        try {
            for (final Map.Entry<String, String> entry : SECRETS_MAP.entrySet()) {
                final String springkey = entry.getValue();
                final String respath = entry.getKey();
                loadSecret(respath, springkey, environment, map);
            }
        }
        catch (final CdbServiceException e) {
            log.error("POSTPROCESS ENV Exception {}", e.getMessage());
        }
    }

    /**
     * @param secpath
     *            the String
     * @param springkey
     *            the String
     * @param environment
     *            the ConfigurableEnvironment
     * @param map
     *            the Map<String,Object>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    private void loadSecret(String secpath, String springkey, ConfigurableEnvironment environment,
            Map<String, Object> map) throws CdbServiceException {
        final Resource resource = new FileSystemResource(secpath);
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
            }
            else {
                log.warn("CANNOT FIND secret in {} for {} ", secpath, PROPERTY_SOURCE_NAME);
            }
        }
        catch (final IOException e) {
            throw new CdbServiceException(e.getMessage());
        }
    }

    /**
     * @param input
     *            the InputStream
     * @return String
     * @throws IOException
     *             If an Exception occurred
     */
    private String getStringFromInputStream(InputStream input) throws IOException {
        final StringWriter writer = new StringWriter();
        IOUtils.copy(input, writer, StandardCharsets.UTF_8);
        return writer.toString();
    }

    /**
     * @param propertySources
     *            the MutablePropertySources
     * @param map
     *            Map<String, Object>
     */
    private void addOrReplace(MutablePropertySources propertySources, Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            final PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (final Map.Entry<String, Object> entry : map.entrySet()) {
                    final String key = entry.getKey();
                    if (!target.containsProperty(key)) {
                        target.getSource().put(key, entry.getValue());
                    }
                    else {
                        log.debug("Key {} is already in {}", key, target.getName());
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
     * Utility method.
     *
     * @param propertySources
     *            the PropertySources
     */
    @SuppressWarnings("unused")
    private void dumpAll(PropertySources propertySources) {
        for (final PropertySource<?> propertySource : propertySources) {
            log.info("Found property source: {} : {}", propertySource.getName(), propertySource);
            if (propertySource instanceof MapPropertySource) {
                final String[] keys = ((MapPropertySource) propertySource).getPropertyNames();
                for (int i = 0; i < keys.length; i++) {
                    final String key = keys[i];
                    final Object val = ((MapPropertySource) propertySource).getProperty(key);
                    log.info("   contains property: {} : {} ", key, val);
                }
            }
        }
    }
}
