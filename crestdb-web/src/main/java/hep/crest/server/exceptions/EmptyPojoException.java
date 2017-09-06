/**
 * 
 */
package hep.crest.server.exceptions;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author formica
 *
 */
public class EmptyPojoException extends CdbServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	public EmptyPojoException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "EmptyPojoException: " + super.getMessage();
	}

	
}
