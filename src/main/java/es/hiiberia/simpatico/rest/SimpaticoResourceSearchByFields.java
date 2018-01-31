package es.hiiberia.simpatico.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import es.hiiberia.simpatico.utils.SimpaticoProperties;

@Path("/search")
public class SimpaticoResourceSearchByFields {
	private String searchParam= "search";
	private static String ES_INDEX = SimpaticoProperties.elasticSearchHIIndex;
	private static String ES_TYPE =  "SF";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "SF";
	private static int numLinesPrintStackInternalError = 1;
	
	
	
	
	@GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_sbf(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
		
    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null)
	    		queryParams.putAll(queryParamsUnmodificable);
	    	
	    	
	    	//List<String> words = queryParams.get(searchParam);
			/*if (words == null) {
				words = new ArrayList<>();
				//words.add(EVENT_SESSION_FEEDBACK);
				queryParams.put(SimpaticoResourceUtils.wordsParam, words);
			} else {
				words.add(EVENT_SESSION_FEEDBACK);
			}*/
			
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
		
		
	}
	

}
