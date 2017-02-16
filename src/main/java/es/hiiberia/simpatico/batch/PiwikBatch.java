package es.hiiberia.simpatico.batch;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import es.hiiberia.simpatico.rest.SimpaticoResourceUtils;
import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

public class PiwikBatch {

	private static PiwikBatch INSTANCE = null;
	private Thread batchThread = null;
	
	private static final int INTERVAL_HOURS = 1; // Get new data every hour
	
	// Examples of methods
	private static String[] methods = {"VisitorInterest.getNumberOfVisitsPerVisitDuration",
									   "DevicesDetection.getBrowsers",
									   "Live.getCounters",
									   "UserCountry.getCountry",
									   "VisitsSummary.getUniqueVisitors"};
	private int currentMethod = -1;
	

	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Piwik;
	private static String ES_INDEX = SimpaticoProperties.elasticSearchPiwikIndex;
	private static String ES_TYPE =  SimpaticoProperties.elasticSearchPiwikType;
	
	// Singleton
	public static PiwikBatch getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PiwikBatch();
		}
		return INSTANCE;
	}
	
	// Private constructor
	private PiwikBatch() {
		batchThread = new Thread() {
			public void run() {
				ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
				Runnable periodicTask = new Runnable() {
					
					@Override
					public void run() {
						// Do the work
						String period = "day";
    					String date = "today";
    					String method = methods[getNextMethod()];
    					IndexResponse responseInsert;
    					
    					// Make piwik request
    					JSONArray piwikResponse = Utils.createJSONArrayIfValid(callPiwikAPI(method, period, date));
    					if (piwikResponse != null && piwikResponse.length() != 0) {
    						try {
	    						JSONObject piwikRes = new JSONObject()
	    												.put("method", method)
	    												.put("data", piwikResponse);
	    						// Elastic search connector
				    			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
				    			
				                // Check if exist index
				    			if (!connector.existsIndex(ES_INDEX)) {
				    				connector.createIndexWithDateField(ES_INDEX, ES_TYPE, SimpaticoProperties.elasticSearchCreatedFieldName);
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
				    			// Log response
				    			if (responseInsert.getResult() == Result.UPDATED) {
				    				Logger.getLogger(FILE_LOG).info("Piwik Thread: " + SimpaticoResourceUtils.dataUpdatedESResponse);
				    			} else {
				    				Logger.getLogger(FILE_LOG).info("Piwik Thread: " + SimpaticoResourceUtils.dataInsertedESResponse);
				    			}
    						} catch (Exception e) {
    							Logger.getLogger(FILE_LOG).error("Exception in Piwik Thread: " + e.getMessage());
    				    		Logger.getRootLogger().error("Exception in Piwik Thread: " + e.getMessage());
    							Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("Exception in Piwik Thread: " + e.getMessage() + "\n" + SimpaticoResourceUtils.exceptionStringifyStack(e));
    						}
    					}
					}
				};
				executor.scheduleAtFixedRate(periodicTask, 0, INTERVAL_HOURS, TimeUnit.HOURS);
			}
		};
		batchThread.start();
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
		Logger.getLogger(FILE_LOG).info("callPiwikApi: " + response.body());
		return response.body();
	}
	
	private int getNextMethod() {
		currentMethod++;
		if (currentMethod == 5) currentMethod = 0;
		
		return currentMethod;
	}
}
