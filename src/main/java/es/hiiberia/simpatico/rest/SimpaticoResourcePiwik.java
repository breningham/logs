package es.hiiberia.simpatico.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

@Path("/piwik")
public class SimpaticoResourcePiwik {
	// Examples of methods
	private static String[] methods = {"VisitorInterest.getNumberOfVisitsPerVisitDuration",
									   "DevicesDetection.getBrowsers",
									   "Live.getCounters",
									   "UserCountry.getCountry",
									   "VisitsSummary.getUniqueVisitors"};
	
	private static String ES_INDEX = SimpaticoProperties.elasticSearchPiwikIndex;
	private static String ES_TYPE =  SimpaticoProperties.elasticSearchPiwikType;
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Piwik;
	private static String THIS_RESOURCE = "Piwik";
	
	@GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        	
    	ArrayList<String> literalWords = new ArrayList<>();
    	int limit = 0; 
    	String fieldSortName = "";
    	SortOrder sortOrder = SortOrder.ASC; // Inicialize. If fieldSort is empty dont sort
    	
    	try {
    		// Query params
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	Logger.getLogger(FILE_LOG).info("Find documents. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    	 	
	    	// Process query params
	        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
	            String key = entry.getKey();
	            List<String> values = entry.getValue();
	            
	            // literal words
	            if (key.contentEquals(SimpaticoResourceUtils.wordsParam)) {
	            	for (String word : values) {
	            		// Comma separated and add to array
	            		for (String splitWord : word.split(SimpaticoResourceUtils.separateParam)) {
	            			literalWords.add(splitWord);
	            		}
	            	}
	            // Limit	
	            } else if (key.contentEquals(SimpaticoResourceUtils.limitParam)) {
	            	if (!values.isEmpty() && Utils.isInteger(values.get(0))) {
	            		limit = Integer.parseInt(values.get(0));
	            	}
	            // Sort
	            } else if (key.contentEquals(SimpaticoResourceUtils.sortASCParam)) {
	            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
	            	sortOrder = SortOrder.ASC;
	            } else if (key.contentEquals(SimpaticoResourceUtils.sortDESCParam)) {
	            	fieldSortName = SimpaticoProperties.elasticSearchCreatedFieldName;
	            	sortOrder = SortOrder.DESC;
	            } else {
	            	// BAD PARAMS
	            	Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Find documents. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badParamsRequestResponse);
	            }
	        }
	        	        
	        SearchResponse responseES;
	        // No params, so empty request -> return full documents stored
	        if (literalWords.isEmpty()) {
	        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, fieldSortName, sortOrder, limit);
	        } else {
	        	responseES = ElasticSearchConnector.getInstance().search(ES_INDEX, ES_FIELD_SEARCH, literalWords, fieldSortName, sortOrder, limit);
	        }
	        
	        return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
    	} catch (Exception e) {
    		// Print the exception and its trace on log
    		Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
    	}
    }
	
	@GET
	@Path("/testVisitsDuration")
    @Produces(MediaType.APPLICATION_JSON)
	public Response testVisitsDuration(@Context HttpServletRequest request) {
		try {
			IndexResponse responseInsert;
    		Response response;
			
			JSONArray piwikResponse = Utils.createJSONArrayIfValid(callPiwikAPI(methods[0], "day", "today"));
			if (piwikResponse != null) {
				JSONObject piwikRes = new JSONObject().put("data", piwikResponse);
				// Put it into ES
				
				// Elastic search connector
				ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
				
				// Check if exist index
				if (!connector.existsIndex(ES_INDEX)) {
					connector.createIndexWithDateField(ES_INDEX, ES_TYPE, 
														SimpaticoProperties.elasticSearchCreatedFieldName);
				}
				
				// Add created time in utc
				piwikRes.put(SimpaticoProperties.elasticSearchCreatedFieldName, new DateTime(new Date()).withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
				
				// Check if "_id" param inside
    			if (piwikRes.has(SimpaticoResourceUtils._idParam)) {
    				String id = piwikRes.getString(SimpaticoResourceUtils._idParam);
    				piwikRes.remove(SimpaticoResourceUtils._idParam);
    				// Insert data with id
    				responseInsert = connector.insertDocument(ES_INDEX, ES_TYPE, id, piwikRes.toString());
    			} else {
    				// Insert data without id
    				responseInsert = connector.insertDocument(ES_INDEX, ES_TYPE, piwikRes.toString());
    			}
    			
    			if (responseInsert.getResult() == Result.UPDATED) {
					response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, SimpaticoResourceUtils.dataUpdatedESResponse);
				} else {
					response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverCreatedCode, SimpaticoResourceUtils.dataInsertedESResponse);
				}
			} else {
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + piwikResponse);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		}

			return response;
		} catch (Exception e) {
			Logger.getLogger(FILE_LOG).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
    		Logger.getRootLogger().error("Exception in " + THIS_RESOURCE + ": " + e.getMessage());
			Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in " + THIS_RESOURCE + ": " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
		}
	}

	/**
	 * Makes a GET request to Piwik REST API
	 * @param method For reference: http://developer.piwik.org/api-reference/reporting-api
	 * @param period Can be any of: day, week, month, year or range. If 'range', date param is mandatory
	 * @param date Format: YYYY-MM-DD. It also can be 'today' or 'yesterday'. If 'period' is range, supported keywords are: 'lastX', 'previousX' and
	 * YYYY-MM-DD,YYYY-MM-DD, or YYYY-MM-DD,today or YYYY-MM-DD,yesterday 
	 * @return Piwik's response
	 */
	private String callPiwikAPI(String method, String period, String date) {
		HttpRequest request = HttpRequest.get(SimpaticoProperties.piwikApiUrl)
								.query("method", method)
								.query("idSite", "1")
								.query("period", period)
								.query("date", date)
								.query("format", "json")
								.query("token_auth", SimpaticoProperties.piwikAuthToken);
		HttpResponse response = request.send();
		Logger.getLogger(FILE_LOG).info(response.body());
		return response.body();
	}
}
