package hep.crest.server.annotations;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation for cache control.
 *
 * @author formica
 *
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheControlCdb {

    /**
     * @return String
     */
    String value() default "public, must-revalidate";

}
