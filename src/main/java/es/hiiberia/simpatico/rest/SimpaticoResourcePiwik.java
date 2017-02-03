package es.hiiberia.simpatico.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	
	@GET
	@Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
	public Response test(@Context HttpServletRequest request) {
		try {
			IndexResponse responseInsert;
    		Response response;
			
			JSONArray piwikResponse = Utils.createJSONArrayIfValid(callPiwikAPI());
			if (piwikResponse != null) {
				JSONObject piwikRes = new JSONObject().put("result", piwikResponse);
				// Put it into ES
				
				// Elastic search connector
				ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
				
				// Check if exist index
				if (!connector.existsIndex(SimpaticoProperties.elasticSearchPiwikIndex)) {
					connector.createIndexWithDateField(SimpaticoProperties.elasticSearchPiwikIndex, SimpaticoProperties.elasticSearchPiwikType, 
														SimpaticoProperties.elasticSearchCreatedFieldName);
				}
				
				// Add created time in utc
				piwikRes.put(SimpaticoProperties.elasticSearchCreatedFieldName, new DateTime(new Date()).withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
				
				// Check if "_id" param inside
    			if (piwikRes.has(SimpaticoResourceUtils._idParam)) {
    				String id = piwikRes.getString(SimpaticoResourceUtils._idParam);
    				piwikRes.remove(SimpaticoResourceUtils._idParam);
    				// Insert data with id
    				responseInsert = connector.insertDocument(SimpaticoProperties.elasticSearchPiwikIndex, SimpaticoProperties.elasticSearchPiwikType, id, piwikRes.toString());
    			} else {
    				// Insert data without id
    				responseInsert = connector.insertDocument(SimpaticoProperties.elasticSearchPiwikIndex, SimpaticoProperties.elasticSearchPiwikType, piwikRes.toString());
    			}
    			
    			if (responseInsert.getResult() == Result.UPDATED) {
					response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, SimpaticoResourceUtils.dataUpdatedESResponse);
				} else {
					response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverCreatedCode, SimpaticoResourceUtils.dataInsertedESResponse);
				}
			} else {
    			Logger.getRootLogger().warn("[BAD REQUEST] Insert document. IP Remote: " + request.getRemoteAddr() + ". POST data: " + piwikResponse);
    			response = SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, SimpaticoResourceUtils.badPOSTRequestResponse);
    		}

			return response;
		} catch (Exception e) {
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			Logger.getRootLogger().error(err.toString());
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse);
		}
	}

	private String callPiwikAPI() {
		HttpRequest request = HttpRequest.get(SimpaticoProperties.piwikApiUrl)
								.query("method", "VisitorInterest.getNumberOfVisitsPerVisitDuration")
								.query("idSite", "1")
								.query("period", "day")
								.query("date", "yesterday")
								.query("format", "json")
								.query("token_auth", SimpaticoProperties.piwikAuthToken);
		HttpResponse response = request.send();
		Logger.getRootLogger().info(response.body());
		return response.body();
	}
}
