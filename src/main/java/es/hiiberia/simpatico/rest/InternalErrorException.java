package es.hiiberia.simpatico.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InternalErrorException extends WebApplicationException {

	private static final long serialVersionUID = -3127477085550888636L;

	public InternalErrorException() {
		super(Response.status(500).build());
	}
	
	public InternalErrorException(String message) {
		super(Response.status(500).entity(message).build());
	}
}
