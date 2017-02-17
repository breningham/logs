package es.hiiberia.simpatico.rest;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/sf")
public class SimpaticoResourceSF {

	private static String THIS_RESOURCE = "SF";
	
    @GET
    @Path("/selectform")
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectForm(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	
    	JSONObject formToUse = new JSONObject();
    	
    	String modal;
    	if (Math.random() > 0.5) {
    		modal = "a";
    	} else {
    		modal = "b";
    	}
    	
    	try {
			formToUse.put("modal", modal);
		} catch (JSONException e) {
			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
		}
    	
    	return SimpaticoResourceUtils.createMessageResponse(formToUse);
    }
    
    /** Test Method **/
    @GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test() {
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API!");
	}
    
    
    /** Method to redirect to web API **/
    @GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void index() {
    	URI uri = UriBuilder.fromUri("http://127.0.0.1:8080/simpatico/").build();
    	Response.seeOther(uri);
	}
    
}
