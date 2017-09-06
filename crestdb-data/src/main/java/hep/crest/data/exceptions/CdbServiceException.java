/**
 * 
 */
package hep.crest.data.exceptions;

/**
 * @author formica
 *
 */
public class CdbServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	public CdbServiceException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "CdbServiceException: " + super.getMessage();
	}

	
}
