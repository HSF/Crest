package hep.crest.server.swagger.api;

/**
 * The exception that can be used to store the HTTP status code returned by an API response.
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen")
public class ApiException extends Exception {
    /** The HTTP status code. */
    private int code;

    /**
     * Constructor.
     *
     * @param code The HTTP status code.
     * @param msg The error message.
     */
    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Get the HTTP status code.
     *
     * @return The HTTP status code.
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "ApiException{" +
               "code=" + code +
               '}';
    }
}
