package es.hiiberia.simpatico.rest;

import java.util.Date;

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
	
	private static int numLinesPrintStackInternalError = 1;
	
	
	@GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
        	
		try {
			return SimpaticoResourceUtils.findRequest(request, uriInfo, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
	
	
	@GET
	@Path("/test/visitsduration")
    @Produces(MediaType.APPLICATION_JSON)
	public Response testVisitsDuration(@Context HttpServletRequest request) {
		try {
			IndexResponse responseInsert;
    		Response response;
			
			JSONArray piwikResponse = Utils.createJSONArrayIfValid(callPiwikAPI(methods[0], "day", "today"));
			if (piwikResponse != null) {
				JSONObject piwikRes = new JSONObject().put("data", piwikResponse);
				
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
    			Logger.getLogger(FILE_LOG).warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request) + ". POST data: " + piwikResponse);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		}

			return response;
		} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
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
