/**
 * 
 */
package hep.crest.server.exceptions;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author formica
 *
 */
public class AlreadyExistsPojoException extends CdbServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	public AlreadyExistsPojoException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "AlreadyExistsPojoException: " + super.getMessage();
	}

	
}
