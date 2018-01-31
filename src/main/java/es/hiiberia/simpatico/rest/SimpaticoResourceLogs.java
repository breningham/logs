package es.hiiberia.simpatico.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.UserKPIObject;
import es.hiiberia.simpatico.utils.Utils;

@Path("/logs")
public class SimpaticoResourceLogs {

	private static String ES_INDEX = SimpaticoProperties.elasticSearchHIIndex;
	private static String ES_TYPE =  "LOG";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "Logs";
	
	private static int numLinesPrintStackInternalError = 1;
	private static int MAX_NUM_RESULTS_REQUEST = 10000;
	
	/* KPIs */
	@POST
    @Path("/reduction-time-spent")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reductionTimeSpent(@Context HttpServletRequest request, String postData) {
    	try {
    		// IDs user using SIMPATICO
    		// IDS user without using simpatico
    		// Date
    		
    		JSONObject json = Utils.createJSONObjectIfValid(postData);
    		if (json != null) {
    			long timeSpentNoSimpaticoUsers = 0;
    			long timeSpentSimpaticoUsers = 0;
    			
    			// Testing mode
    			Boolean testing = json.optBoolean("testing");
    			if (testing) {
    				timeSpentSimpaticoUsers = json.optLong("averageTimeSpentUsersSimpatico", -1);
    				timeSpentNoSimpaticoUsers = json.optLong("averageTimeSpentUsersWithoutSimpatico", -1);
    				Logger.getRootLogger().debug("[LOGS] Testing Mode. averageTimeSpentUsersSimpatico: " + timeSpentNoSimpaticoUsers + ", averageTimeSpentUsersWithoutSimpatico: " + timeSpentSimpaticoUsers);
    				if (timeSpentNoSimpaticoUsers == -1 || timeSpentSimpaticoUsers == -1) {
    					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format. averageTimeSpentUsersSimpatico and averageTimeSpentUsersWithoutSimpatico keys dont found");
    				}  
    			} else {
	    			String dateStart = json.opt("dateStart").toString();
	    			String dateEnd = json.opt("dateEnd").toString();
	    			List<Object> usersSimpatico = Utils.toList(json.getJSONArray("usersSimpatico"));
	    			List<Object> usersWithoutSimpatico = Utils.toList(json.getJSONArray("usersWithoutSimpatico"));
	    			
	    			
	    			String usersSimpaticoString = new String();
	    			for (int i = 0; i < usersSimpatico.size(); i++) {
	    				if (i == usersSimpatico.size() - 1 ) { // Last iteration
	    					usersSimpaticoString += "\"" + usersSimpatico.get(i).toString() + "\"";
	    				} else {
	    					usersSimpaticoString += "\"" + usersSimpatico.get(i).toString() + "\",";
	    				}
	    			}
	    			
	    			String usersWithoutSimpaticoString = new String();
	    			for (int i = 0; i < usersWithoutSimpatico.size(); i++) {
	    				if (i == usersWithoutSimpatico.size() - 1 ) { // Last iteration
	    					usersWithoutSimpaticoString += "\"" + usersWithoutSimpatico.get(i).toString() + "\"";
	    				} else {
	    					usersWithoutSimpaticoString += "\"" + usersWithoutSimpatico.get(i).toString() + "\",";
	    				}
	    			}
	    			
	    			// Check params
	
	    			
	    			// Do first request
	    			// First query
	        		String query1 = new String(
	        						"{\n" + 
	        			    		"    \"query\": {\n" + 
	        						"        \"bool\" : {\n " +
	        						"            \"must\" : {\n" +
	        			    		"                \"range\" : {\n" + 
	        			    		"                    \"created\" : {\n" + 
	        			    		"                        \"gte\": \"" + dateStart + "\",\n" + 
	        			    		"                        \"lt\": \"" + dateEnd + "\"\n" +
	        			    		"                    }\n" + 
	        			    		"                }\n" + 
	        			    		"            },\n" + 
	        			    		"            \"filter\" : {\n" +
	        			    		"                \"terms\" : {\n" + 
	        			    		"                    \"userID\" : [" + usersSimpaticoString + "]\n" + 
	        			    		"                } \n" + 
	        			    		"            },\n" +
	        			    		"            \"must_not\" : [\n" +
	        			    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
	        			    		"            ]\n" + 
	        			    		"        }\n" + 
	        			    		"    },\n" + 
	        			    		"    \"sort\": {\n" +
	        			    		"      \"created\": { \"order\": \"asc\" }\n" +
	        			    		"    }\n" +
	        			    		"}\n");
	        		 
	
	        		URL url = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Content-Type", "application/json");
	                connection.setRequestProperty("Accept", "application/json");
	                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	                osw.write(query1);
	                osw.flush();
	                osw.close();
	                Logger.getRootLogger().debug("[LOGS] Sending first GET to url: " + url);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection.getResponseCode());
	
	        		BufferedReader in = new BufferedReader(
	        		        new InputStreamReader(connection.getInputStream()));
	        		String inputLine;
	        		StringBuffer response = new StringBuffer();
	
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	
	        		//print result
	        		Logger.getRootLogger().debug("[LOGS] Response: " + response.toString());
	 
	        		
	        		
	        		// Second request
	        		String queryNoSimpatico = new String(
							"{\n" + 
				    		"    \"query\": {\n" + 
							"        \"bool\" : {\n " +
							"            \"must\" : {\n" +
				    		"                \"range\" : {\n" + 
				    		"                    \"created\" : {\n" + 
				    		"                        \"gte\": \"" + dateStart + "\",\n" + 
				    		"                        \"lt\": \"" + dateEnd + "\"\n" +
				    		"                    }\n" + 
				    		"                }\n" + 
				    		"            },\n" + 
				    		"            \"filter\" : {\n" +
				    		"                \"terms\" : {\n" + 
				    		"                    \"userID\" : [" + usersWithoutSimpaticoString + "]\n" + 
				    		"                } \n" + 
				    		"            },\n" +
				    		"            \"must_not\" : [\n" +
				    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
				    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
				    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
				    		"            ]\n" + 
				    		"        }\n" + 
				    		"    },\n" + 
				    		"    \"sort\": {\n" +
				    		"      \"created\": { \"order\": \"asc\" }\n" +
				    		"    }\n" +
				    		"}\n");
			 
	
					URL url2 = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
			        HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
			        connection2.setRequestMethod("GET");
			        connection2.setDoOutput(true);
			        connection2.setRequestProperty("Content-Type", "application/json");
			        connection2.setRequestProperty("Accept", "application/json");
			        OutputStreamWriter osw2 = new OutputStreamWriter(connection2.getOutputStream());
			        osw2.write(queryNoSimpatico);
			        osw2.flush();
			        osw2.close();
			        
			        Logger.getRootLogger().debug("[LOGS] Sending second GET to url: " + url2);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection2.getResponseCode());
			
					BufferedReader in2 = new BufferedReader(
					        new InputStreamReader(connection2.getInputStream()));
					String inputLine2;
					StringBuffer response2 = new StringBuffer();
			
					while ((inputLine2 = in2.readLine()) != null) {
						response2.append(inputLine2);
					}
					in2.close();
			
					//print result
					Logger.getRootLogger().debug("[LOGS] Response: " + response2.toString());
					
					// Convert each response to json and calculate percentage
					JSONObject jsonUsersSimpatico = Utils.createJSONObjectIfValid(response.toString());
					JSONObject jsonUsersWithoutSimpatico = Utils.createJSONObjectIfValid(response2.toString());
					
					// Arrays to process data from elastic search and get how time users spent
					ArrayList<UserKPIObject> alUsersSimpatico = new ArrayList<>();   // ArrayList usersSimpatico
					ArrayList<UserKPIObject> alUsersWithoutSimpatico = new ArrayList<>(); // ArrayList usersWithoutSimpatico
					
					// Process Simpatico users
					JSONObject jsonUsersSimpaticoResults = jsonUsersSimpatico.getJSONObject("hits");
					if (jsonUsersSimpaticoResults.getInt("total") > 0) {
						JSONObject hitSource;
						String event;
						String userId;
						String timestamp;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsersSimpatico = jsonUsersSimpaticoResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsersSimpatico.length(); i++) {
							hitSource = arrayHitsUsersSimpatico.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (event.equalsIgnoreCase("form_start")) {
								alUsersSimpatico.add(0, new UserKPIObject(userId, timeDate));
							} else {
								// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
								for (UserKPIObject oUser : alUsersSimpatico) {
									if (oUser.getUser().equalsIgnoreCase(userId)) {
										oUser.addDate(timeDate, event);
									}
								}
							}
						}
					}
					
					// Process No Simpatico users
					JSONObject jsonUsersWithoutSimpaticoResults = jsonUsersWithoutSimpatico.getJSONObject("hits");
					if (jsonUsersWithoutSimpaticoResults.getInt("total") > 0) {
						JSONObject hitSource;
						String userId;
						String timestamp;
						String event;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsersNoSimpatico = jsonUsersWithoutSimpaticoResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsersNoSimpatico.length(); i++) {
							hitSource = arrayHitsUsersNoSimpatico.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (event.equalsIgnoreCase("form_start")) {
								alUsersWithoutSimpatico.add(0, new UserKPIObject(userId, timeDate));
							} else {
								Logger.getRootLogger().debug("[LOGS] Response: " + response2.toString());
								// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
								for (UserKPIObject oUser : alUsersWithoutSimpatico) {
									if (oUser.getUser().equalsIgnoreCase(userId)) {
										oUser.addDate(timeDate, event);
									}
								}
							}
						}
					}
					
					// Average time spent simpatico Users
					for (UserKPIObject oUser : alUsersSimpatico) {
						timeSpentSimpaticoUsers += oUser.getDiffDateMS();
					}
					if (alUsersSimpatico.size() > 0) {
						timeSpentSimpaticoUsers = timeSpentSimpaticoUsers / alUsersSimpatico.size();
					}
					
					// Average time spent NO simpatico Users
					for (UserKPIObject oUser : alUsersWithoutSimpatico) {
						timeSpentNoSimpaticoUsers += oUser.getDiffDateMS();
					}
					if (alUsersWithoutSimpatico.size() > 0) {
						timeSpentNoSimpaticoUsers = timeSpentNoSimpaticoUsers / alUsersWithoutSimpatico.size();
					}
    			}
				
				// Page to do percentage. Inverse because less time is better https://www.skillsyouneed.com/num/percent-change.html
				double percentage;
				boolean decrease = false;
				if (timeSpentNoSimpaticoUsers == 0) {
					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Infinity");
				} else { 
					double sup = timeSpentNoSimpaticoUsers - timeSpentSimpaticoUsers;
					double inf = timeSpentNoSimpaticoUsers;
					if (sup < 0) { // There is a decrease
						decrease = true;
						sup = timeSpentSimpaticoUsers - timeSpentNoSimpaticoUsers;
						inf = timeSpentSimpaticoUsers;
					}
					percentage = Math.round((sup/inf * 100) * 100.0) / 100.0; // Round to 2 decimals
				}

				String valueReturn;
				if (decrease) {
					valueReturn = "-" + String.valueOf(percentage) + "%";
				} else {
					valueReturn = String.valueOf(percentage) + "%";
				}
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, valueReturn);
    			
    			/*double percentage = timeSpentSimpaticoUsers / timeSpentNoSimpaticoUsers;
    			percentage = Math.round((percentage * 100) * 100.0) / 100.0;
    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, percentage + "%");*/
    		}   		
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format");
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }

    /* KPIs */
	@POST
    @Path("/reduction-time-spent-all-users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reductionTimeSpentAll(@Context HttpServletRequest request, String postData) {
    	try {
    		// IDs user using SIMPATICO
    		// IDS user without using simpatico
    		// Date
    		
    		JSONObject json = Utils.createJSONObjectIfValid(postData);
    		if (json != null) {
    			long timeSpentNoSimpaticoUsers = 0;
    			long timeSpentSimpaticoUsers = 0;
    			long averageSpentSimpaticoUsers = 0, averageSpentNoSimpaticoUsers = 0;
    			int numSimpaticoUsers = 0, numNoSimpaticoUsers = 0;
    			
    			// Testing mode
    			Boolean testing = json.optBoolean("testing");
    			if (testing) {
    				averageSpentSimpaticoUsers = json.optLong("averageTimeSpentUsersSimpatico", -1);
    				averageSpentSimpaticoUsers = json.optLong("averageTimeSpentUsersWithoutSimpatico", -1);
    				Logger.getRootLogger().debug("[LOGS] Testing Mode. averageTimeSpentUsersSimpatico: " + timeSpentNoSimpaticoUsers + ", averageTimeSpentUsersWithoutSimpatico: " + timeSpentSimpaticoUsers);
    				if (timeSpentNoSimpaticoUsers == -1 || timeSpentSimpaticoUsers == -1) {
    					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format. averageTimeSpentUsersSimpatico and averageTimeSpentUsersWithoutSimpatico keys dont found");
    				}  
    			} else {
	    			String dateStart = json.opt("dateStart").toString();
	    			String dateEnd = json.opt("dateEnd").toString();
	    			
	    			
	    			// Do request
	        		String query1 = new String(
	        						"{\n" + 
	        			    		"    \"query\": {\n" + 
	        						"        \"bool\" : {\n " +
	        						"            \"must\" : {\n" +
	        			    		"                \"range\" : {\n" + 
	        			    		"                    \"created\" : {\n" + 
	        			    		"                        \"gte\": \"" + dateStart + "\",\n" + 
	        			    		"                        \"lt\": \"" + dateEnd + "\"\n" +
	        			    		"                    }\n" + 
	        			    		"                }\n" + 
	        			    		"            },\n" + 
	        			    		"            \"must_not\" : [\n" +
	        			    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
	        			    		"            ]\n" + 
	        			    		"        }\n" + 
	        			    		"    },\n" + 
	        			    		"    \"sort\": {\n" +
	        			    		"      \"created\": { \"order\": \"asc\" }\n" +
	        			    		"    }\n" +
	        			    		"}\n");
	        		 
	
	        		URL url = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Content-Type", "application/json");
	                connection.setRequestProperty("Accept", "application/json");
	                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	                osw.write(query1);
	                osw.flush();
	                osw.close();
	                Logger.getRootLogger().debug("[LOGS] Sending first GET to url: " + url);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection.getResponseCode());
	
	        		BufferedReader in = new BufferedReader(
	        		        new InputStreamReader(connection.getInputStream()));
	        		String inputLine;
	        		StringBuffer response = new StringBuffer();
	
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	
	        		//print result
	        		Logger.getRootLogger().debug("[LOGS] Response: " + response.toString());
	 
	        		
					// Convert each response to json and calculate percentage
					JSONObject jsonUsers = Utils.createJSONObjectIfValid(response.toString());
					
					// Arrays to process data from elastic search and get how time users spent
					ArrayList<UserKPIObject> alUsersSimpatico = new ArrayList<>();   // ArrayList usersSimpatico
					ArrayList<UserKPIObject> alUsersWithoutSimpatico = new ArrayList<>(); // ArrayList usersWithoutSimpatico
					
					// Process users
					JSONObject jsonUsersResults = jsonUsers.getJSONObject("hits");
					if (jsonUsersResults.getInt("total") > 0) {
						JSONObject hitSource;
						String event;
						String userId;
						String timestamp;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsers = jsonUsersResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsers.length(); i++) {
							hitSource = arrayHitsUsers.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (userId.contains("no_user_logged_")) {  // User No Simpatico
								if (event.equalsIgnoreCase("form_start")) {
									alUsersWithoutSimpatico.add(0, new UserKPIObject(userId, timeDate));
								} else {
									// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
									for (UserKPIObject oUser : alUsersWithoutSimpatico) {
										if (oUser.getUser().equalsIgnoreCase(userId)) {
											oUser.addDate(timeDate, event);
										}
									}
								}
							} else {
								if (event.equalsIgnoreCase("form_start")) {
									alUsersSimpatico.add(0, new UserKPIObject(userId, timeDate));
								} else {
									// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
									for (UserKPIObject oUser : alUsersSimpatico) {
										if (oUser.getUser().equalsIgnoreCase(userId)) {
											oUser.addDate(timeDate, event);
										}
									}
								}
							}
						}
					}
					
					// Average time spent simpatico Users
					for (UserKPIObject oUser : alUsersSimpatico) {
						timeSpentSimpaticoUsers += oUser.getDiffDateMS();
					}
					if (alUsersSimpatico.size() > 0) {
						averageSpentSimpaticoUsers = timeSpentSimpaticoUsers / alUsersSimpatico.size();
					}
					
					// Average time spent NO simpatico Users
					for (UserKPIObject oUser : alUsersWithoutSimpatico) {
						timeSpentNoSimpaticoUsers += oUser.getDiffDateMS();
					}
					if (alUsersWithoutSimpatico.size() > 0) {
						averageSpentNoSimpaticoUsers = timeSpentNoSimpaticoUsers / alUsersWithoutSimpatico.size();
					}
					
					numSimpaticoUsers = alUsersSimpatico.size();
					numNoSimpaticoUsers = alUsersWithoutSimpatico.size();
							
    			}
				
				// Page to do percentage. Inverse because less time is better https://www.skillsyouneed.com/num/percent-change.html
				double percentage;
				boolean decrease = false;
				if (averageSpentNoSimpaticoUsers == 0) {
					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Infinity " + " -> average_time_spent_Simpatico: " + averageSpentSimpaticoUsers + ". average_time_spent_NoSimpatico: " + averageSpentNoSimpaticoUsers 
							+ ". total_time_Simpatico: " +  timeSpentSimpaticoUsers + " numSimpaticoUsers: " + numSimpaticoUsers
							+ ". total_time_NoSimpatico: " +  timeSpentNoSimpaticoUsers + " numNoSimpaticoUsers: " + numNoSimpaticoUsers);
				} else { 
					double sup = averageSpentNoSimpaticoUsers - averageSpentSimpaticoUsers;
					double inf = averageSpentNoSimpaticoUsers;
					if (sup < 0) { // There is a decrease
						decrease = true;
						sup = averageSpentSimpaticoUsers - averageSpentNoSimpaticoUsers;
						inf = averageSpentSimpaticoUsers;
					}
					percentage = Math.round((sup/inf * 100) * 100.0) / 100.0; // Round to 2 decimals
				}

				String valueReturn;
				if (decrease) {
					valueReturn = "-" + String.valueOf(percentage) + "%";
				} else {
					valueReturn = String.valueOf(percentage) + "%";
				}
				valueReturn += " -> average_time_spent_Simpatico: " + averageSpentSimpaticoUsers + ". average_time_spent_NoSimpatico: " + averageSpentNoSimpaticoUsers 
						+ ". total_time_Simpatico: " +  timeSpentSimpaticoUsers + " numSimpaticoUsers: " + numSimpaticoUsers
						+ ". total_time_NoSimpatico: " +  timeSpentNoSimpaticoUsers + " numNoSimpaticoUsers: " + numNoSimpaticoUsers;
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, valueReturn);
    			
    			/*double percentage = timeSpentSimpaticoUsers / timeSpentNoSimpaticoUsers;
    			percentage = Math.round((percentage * 100) * 100.0) / 100.0;
    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, percentage + "%");*/
    		}   		
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format");
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
	
	@POST
    @Path("/percentage-complete-autonomously")
    @Produces(MediaType.APPLICATION_JSON)
    public Response percentageCompleteAutonomously(@Context HttpServletRequest request, String postData) {
    	try {
    		// IDs user using SIMPATICO
    		// IDS user without using simpatico
    		// Date
    		
    		JSONObject json = Utils.createJSONObjectIfValid(postData);
    		if (json != null) {
    			int usersCompleteFormSimpatico = 0;
    			int usersCompleteFormWithoutSimpatico = 0;
    			    			
    			// Testing mode
    			Boolean testing = json.optBoolean("testing");
    			if (testing) {
    				usersCompleteFormSimpatico = json.optInt("usersSimpaticoComplete", -1);
    				usersCompleteFormWithoutSimpatico = json.optInt("usersWithoutSimpaticoComplete", -1);
    				Logger.getRootLogger().debug("[LOGS] Testing Mode. usersSimpaticoComplete: " + usersCompleteFormSimpatico + ", usersWithoutSimpatico: " + usersCompleteFormWithoutSimpatico);
    				if (usersCompleteFormSimpatico == -1 || usersCompleteFormWithoutSimpatico == -1) {
    					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format. usersSimpatico and usersWithoutSimpatico keys dont found");
    				}  
    			} else {	    			
    				String dateStart = json.opt("dateStart").toString();
        			String dateEnd = json.opt("dateEnd").toString();
        			List<Object> usersSimpatico = Utils.toList(json.getJSONArray("usersSimpatico"));
        			List<Object> usersWithoutSimpatico = Utils.toList(json.getJSONArray("usersWithoutSimpatico"));

	    			String usersSimpaticoString = new String();
	    			for (int i = 0; i < usersSimpatico.size(); i++) {
	    				if (i == usersSimpatico.size() - 1 ) { // Last iteration
	    					usersSimpaticoString += "\"" + usersSimpatico.get(i).toString() + "\"";
	    				} else {
	    					usersSimpaticoString += "\"" + usersSimpatico.get(i).toString() + "\",";
	    				}
	    			}
	    			
	    			String usersWithoutSimpaticoString = new String();
	    			for (int i = 0; i < usersWithoutSimpatico.size(); i++) {
	    				if (i == usersWithoutSimpatico.size() - 1 ) { // Last iteration
	    					usersWithoutSimpaticoString += "\"" + usersWithoutSimpatico.get(i).toString() + "\"";
	    				} else {
	    					usersWithoutSimpaticoString += "\"" + usersWithoutSimpatico.get(i).toString() + "\",";
	    				}
	    			}
	
	    			
	    			// Do first request
	    			// First query
	        		String query1 = new String(
	        						"{\n" + 
	        			    		"    \"query\": {\n" + 
	        						"        \"bool\" : {\n " +
	        						"            \"must\" : {\n" +
	        			    		"                \"range\" : {\n" + 
	        			    		"                    \"created\" : {\n" + 
	        			    		"                        \"gte\": \"" + dateStart + "\",\n" + 
	        			    		"                        \"lt\": \"" + dateEnd + "\"\n" +
	        			    		"                    }\n" + 
	        			    		"                }\n" + 
	        			    		"            },\n" + 
	        			    		"            \"filter\" : {\n" +
	        			    		"                \"terms\" : {\n" + 
	        			    		"                    \"userID\" : [" + usersSimpaticoString + "]\n" + 
	        			    		"                } \n" + 
	        			    		"            },\n" +
	        			    		"            \"must_not\" : [\n" +
	        			    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
	        			    		"            ]\n" + 
	        			    		"        }\n" + 
	        			    		"    },\n" + 
	        			    		"    \"sort\": {\n" +
	        			    		"      \"created\": { \"order\": \"asc\" }\n" +
	        			    		"    }\n" +
	        			    		"}\n");
	        		 
	
	        		URL url = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Content-Type", "application/json");
	                connection.setRequestProperty("Accept", "application/json");
	                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	                osw.write(query1);
	                osw.flush();
	                osw.close();
	                Logger.getRootLogger().debug("[LOGS] Sending first GET to url: " + url);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection.getResponseCode());
	
	        		BufferedReader in = new BufferedReader(
	        		        new InputStreamReader(connection.getInputStream()));
	        		String inputLine;
	        		StringBuffer response = new StringBuffer();
	
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	
	        		//print result
	        		Logger.getRootLogger().debug("[LOGS] Response: " + response.toString());
	 
	        		
	        		
	        		// Second request
	        		String queryNoSimpatico = new String(
							"{\n" + 
				    		"    \"query\": {\n" + 
							"        \"bool\" : {\n " +
							"            \"must\" : {\n" +
				    		"                \"range\" : {\n" + 
				    		"                    \"created\" : {\n" + 
				    		"                        \"gte\": \"" + dateStart + "\",\n" + 
				    		"                        \"lt\": \"" + dateEnd + "\"\n" +
				    		"                    }\n" + 
				    		"                }\n" + 
				    		"            },\n" + 
				    		"            \"filter\" : {\n" +
				    		"                \"terms\" : {\n" + 
				    		"                    \"userID\" : [" + usersWithoutSimpaticoString + "]\n" + 
				    		"                } \n" + 
				    		"            },\n" +
				    		"            \"must_not\" : [\n" +
				    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
				    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
				    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
				    		"            ]\n" + 
				    		"        }\n" + 
				    		"    },\n" + 
				    		"    \"sort\": {\n" +
				    		"      \"created\": { \"order\": \"asc\" }\n" +
				    		"    }\n" +
				    		"}\n");
			 
	
					URL url2 = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
			        HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
			        connection2.setRequestMethod("GET");
			        connection2.setDoOutput(true);
			        connection2.setRequestProperty("Content-Type", "application/json");
			        connection2.setRequestProperty("Accept", "application/json");
			        OutputStreamWriter osw2 = new OutputStreamWriter(connection2.getOutputStream());
			        osw2.write(queryNoSimpatico);
			        osw2.flush();
			        osw2.close();
			        
			        Logger.getRootLogger().debug("[LOGS] Sending second GET to url: " + url2);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection2.getResponseCode());
			
					BufferedReader in2 = new BufferedReader(
					        new InputStreamReader(connection2.getInputStream()));
					String inputLine2;
					StringBuffer response2 = new StringBuffer();
			
					while ((inputLine2 = in2.readLine()) != null) {
						response2.append(inputLine2);
					}
					in2.close();
			
					//print result
					Logger.getRootLogger().debug("[LOGS] Response: " + response2.toString());
					
					// Convert each response to json and calculate percentage
					JSONObject jsonUsersSimpatico = Utils.createJSONObjectIfValid(response.toString());
					JSONObject jsonUsersWithoutSimpatico = Utils.createJSONObjectIfValid(response2.toString());
					
					// Arrays to process data from elastic search and get how time users spent
					ArrayList<UserKPIObject> alUsersSimpatico = new ArrayList<>();
					ArrayList<UserKPIObject> alUsersWithoutSimpatico = new ArrayList<>();
					
					// Process Simpatico users
					JSONObject jsonUsersSimpaticoResults = jsonUsersSimpatico.getJSONObject("hits");
					if (jsonUsersSimpaticoResults.getInt("total") > 0) {
						JSONObject hitSource;
						String event;
						String userId;
						String timestamp;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsersSimpatico = jsonUsersSimpaticoResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsersSimpatico.length(); i++) {
							hitSource = arrayHitsUsersSimpatico.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (event.equalsIgnoreCase("form_start")) {
								alUsersSimpatico.add(0, new UserKPIObject(userId, timeDate));
							} else {
								// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
								for (UserKPIObject oUser : alUsersSimpatico) {
									if (oUser.getUser().equalsIgnoreCase(userId)) {
										oUser.addDate(timeDate, event);
									}
								}
							}
						}
					}
					
					// Process No Simpatico users
					JSONObject jsonUsersWithoutSimpaticoResults = jsonUsersWithoutSimpatico.getJSONObject("hits");
					if (jsonUsersWithoutSimpaticoResults.getInt("total") > 0) {
						JSONObject hitSource;
						String userId;
						String timestamp;
						String event;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsersNoSimpatico = jsonUsersWithoutSimpaticoResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsersNoSimpatico.length(); i++) {
							hitSource = arrayHitsUsersNoSimpatico.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (event.equalsIgnoreCase("form_start")) {
								alUsersWithoutSimpatico.add(0, new UserKPIObject(userId, timeDate));
							} else {
								Logger.getRootLogger().debug("[LOGS] Response: " + response2.toString());
								// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
								for (UserKPIObject oUser : alUsersWithoutSimpatico) {
									if (oUser.getUser().equalsIgnoreCase(userId)) {
										oUser.addDate(timeDate, event);
									}
								}
							}
						}
					}
					
					// Users with SIMPATICO that complete form
					for (UserKPIObject oUser : alUsersSimpatico) {
						if (oUser.isFormComplete()) {
							usersCompleteFormSimpatico++;
						}
					}
	
					// Users without SIMPATICO that complete form
					for (UserKPIObject oUser : alUsersWithoutSimpatico) {
						if (oUser.isFormComplete()) {
							usersCompleteFormWithoutSimpatico++;
						}
					}
    			}
    			
    			// Page to do percentage https://www.skillsyouneed.com/num/percent-change.html
				double percentage;
				boolean decrease = false;
				if (usersCompleteFormWithoutSimpatico == 0) {
					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Infinity");
				} else { 
					double sup = usersCompleteFormSimpatico - usersCompleteFormWithoutSimpatico;
					double inf = usersCompleteFormWithoutSimpatico;
					if (sup < 0) { // There is a decrease
						decrease = true;
						sup = usersCompleteFormWithoutSimpatico - usersCompleteFormSimpatico;
						inf = usersCompleteFormWithoutSimpatico;
					}
					percentage = Math.round((sup/inf * 100) * 100.0) / 100.0; // Round to 2 decimals
				}
				
				String valueReturn;
				if (decrease) {
					valueReturn = "-" + String.valueOf(percentage) + "%";
				} else {
					valueReturn = String.valueOf(percentage) + "%";
				}
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, valueReturn);
				
    			/*double percentage = usersCompleteFormSimpatico / usersCompleteFormWithoutSimpatico;
    			percentage = Math.round((percentage * 100) * 100.0) / 100.0;   // Round to 2 decimals
				
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, percentage + "%");*/
    		}   		
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format");
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }

    /* KPIs */
	@POST
    @Path("/percentage-complete-autonomously-all-users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response percentageCompleteAutonomouslyAll(@Context HttpServletRequest request, String postData) {
    	try {
    		// IDs user using SIMPATICO
    		// IDS user without using simpatico
    		// Date
    		
    		JSONObject json = Utils.createJSONObjectIfValid(postData);
    		if (json != null) {
    			int usersCompleteFormSimpatico = 0;
    			int usersCompleteFormWithoutSimpatico = 0;
    			int numSimpaticoUsers = 0, numNoSimpaticoUsers = 0;
    			
    			// Testing mode
    			Boolean testing = json.optBoolean("testing");
    			if (testing) {
    				usersCompleteFormSimpatico = json.optInt("usersSimpaticoComplete", -1);
    				usersCompleteFormWithoutSimpatico = json.optInt("usersWithoutSimpaticoComplete", -1);
    				Logger.getRootLogger().debug("[LOGS] Testing Mode. usersSimpaticoComplete: " + usersCompleteFormSimpatico + ", usersWithoutSimpatico: " + usersCompleteFormWithoutSimpatico);
    				if (usersCompleteFormSimpatico == -1 || usersCompleteFormWithoutSimpatico == -1) {
    					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format. usersSimpatico and usersWithoutSimpatico keys dont found");
    				}    
    			} else {
	    			String dateStart = json.opt("dateStart").toString();
	    			String dateEnd = json.opt("dateEnd").toString();
	    			
	    			
	    			// Do request
	        		String query1 = new String(
	        						"{\n" + 
	        			    		"    \"query\": {\n" + 
	        						"        \"bool\" : {\n " +
	        						"            \"must\" : {\n" +
	        			    		"                \"range\" : {\n" + 
	        			    		"                    \"created\" : {\n" + 
	        			    		"                        \"gte\": \"" + dateStart + "\",\n" + 
	        			    		"                        \"lt\": \"" + dateEnd + "\"\n" +
	        			    		"                    }\n" + 
	        			    		"                }\n" + 
	        			    		"            },\n" + 
	        			    		"            \"must_not\" : [\n" +
	        			    		"                {\"match\" : { \"event\": \"elements_clicks\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_start\"} }, \n" +
	        			    		"                {\"match\" : { \"event\": \"session_end\"} } \n" +
	        			    		"            ]\n" + 
	        			    		"        }\n" + 
	        			    		"    },\n" + 
	        			    		"    \"sort\": {\n" +
	        			    		"      \"created\": { \"order\": \"asc\" }\n" +
	        			    		"    }\n" +
	        			    		"}\n");
	        		 
	
	        		URL url = new URL("http://" + SimpaticoProperties.elasticSearchIp + ":9200/shared/IFE/_search?size=" + MAX_NUM_RESULTS_REQUEST);
	                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                connection.setRequestMethod("GET");
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Content-Type", "application/json");
	                connection.setRequestProperty("Accept", "application/json");
	                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	                osw.write(query1);
	                osw.flush();
	                osw.close();
	                Logger.getRootLogger().debug("[LOGS] Sending first GET to url: " + url);
	                Logger.getRootLogger().debug("[LOGS] Response code: " + connection.getResponseCode());
	
	        		BufferedReader in = new BufferedReader(
	        		        new InputStreamReader(connection.getInputStream()));
	        		String inputLine;
	        		StringBuffer response = new StringBuffer();
	
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	
	        		//print result
	        		Logger.getRootLogger().debug("[LOGS] Response: " + response.toString());
	 
	        		
					// Convert each response to json and calculate percentage
					JSONObject jsonUsers = Utils.createJSONObjectIfValid(response.toString());
					
					// Arrays to process data from elastic search and get how time users spent
					ArrayList<UserKPIObject> alUsersSimpatico = new ArrayList<>();   // ArrayList usersSimpatico
					ArrayList<UserKPIObject> alUsersWithoutSimpatico = new ArrayList<>(); // ArrayList usersWithoutSimpatico
					
					// Process users
					JSONObject jsonUsersResults = jsonUsers.getJSONObject("hits");
					if (jsonUsersResults.getInt("total") > 0) {
						JSONObject hitSource;
						String event;
						String userId;
						String timestamp;
						//String created;
						Date timeDate;
						JSONArray arrayHitsUsers = jsonUsersResults.getJSONArray("hits");
						for (int i = 0; i < arrayHitsUsers.length(); i++) {
							hitSource = arrayHitsUsers.getJSONObject(i).getJSONObject("_source");
							userId = hitSource.getString("userID");
							event = hitSource.getString("event");
							timestamp = hitSource.getString("timestamp");
							//created = hitSource.getString("created");
							//DateTime dateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(timestamp);
							//timeDate = dateTime.toDate();
							timeDate = new Date(Long.parseLong(timestamp));
							if (userId.contains("no_user_logged_")) {  // User No Simpatico
								if (event.equalsIgnoreCase("form_start")) {
									alUsersWithoutSimpatico.add(0, new UserKPIObject(userId, timeDate));
								} else {
									// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
									for (UserKPIObject oUser : alUsersWithoutSimpatico) {
										if (oUser.getUser().equalsIgnoreCase(userId)) {
											oUser.addDate(timeDate, event);
										}
									}
								}
							} else {
								if (event.equalsIgnoreCase("form_start")) {
									alUsersSimpatico.add(0, new UserKPIObject(userId, timeDate));
								} else {
									// Search first ocurrence with these ID and add it. If doesnt find any previous item, it seems that event form_start doesnt occurs, so we dont save 
									for (UserKPIObject oUser : alUsersSimpatico) {
										if (oUser.getUser().equalsIgnoreCase(userId)) {
											oUser.addDate(timeDate, event);
										}
									}
								}
							}
						}
					}
					
					// Users with SIMPATICO that complete form
					for (UserKPIObject oUser : alUsersSimpatico) {
						if (oUser.isFormComplete()) {
							usersCompleteFormSimpatico++;
						}
					}
	
					// Users without SIMPATICO that complete form
					for (UserKPIObject oUser : alUsersWithoutSimpatico) {
						if (oUser.isFormComplete()) {
							usersCompleteFormWithoutSimpatico++;
						}
					}
					
					numSimpaticoUsers = alUsersSimpatico.size();
					numNoSimpaticoUsers = alUsersWithoutSimpatico.size();
    			}
				
    			// Page to do percentage https://www.skillsyouneed.com/num/percent-change.html
				double percentage;
				boolean decrease = false;
				if (usersCompleteFormWithoutSimpatico == 0) {
					return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Infinity -> " + 
							". NumSimpaticoUsersComplete: " + usersCompleteFormSimpatico + ". NumSimpaticoUsers: " + numSimpaticoUsers 
							+ ". NumNoSimpaticoUsersComplete: " +  usersCompleteFormWithoutSimpatico + " NumNoSimpaticoUsers: " + numNoSimpaticoUsers);
				} else { 
					double sup = usersCompleteFormSimpatico - usersCompleteFormWithoutSimpatico;
					double inf = usersCompleteFormWithoutSimpatico;
					if (sup < 0) { // There is a decrease
						decrease = true;
						sup = usersCompleteFormWithoutSimpatico - usersCompleteFormSimpatico;
						inf = usersCompleteFormWithoutSimpatico;
					}
					percentage = Math.round((sup/inf * 100) * 100.0) / 100.0; // Round to 2 decimals
				}
				
				String valueReturn;
				if (decrease) {
					valueReturn = "-" + String.valueOf(percentage) + "%";
				} else {
					valueReturn = String.valueOf(percentage) + "%";
				}
				valueReturn += " -> NumSimpaticoUsersComplete: " + usersCompleteFormSimpatico + ". NumSimpaticoUsers: " + numSimpaticoUsers 
						+ ". NumNoSimpaticoUsersComplete: " +  usersCompleteFormWithoutSimpatico + " NumNoSimpaticoUsers: " + numNoSimpaticoUsers;
				
				return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, valueReturn);
				
    			
    			/*double percentage = timeSpentSimpaticoUsers / timeSpentNoSimpaticoUsers;
    			percentage = Math.round((percentage * 100) * 100.0) / 100.0;
    			return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, percentage + "%");*/
    		}   		
    		
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverBadRequestCode, "JSON in bad format");
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
	
    @GET
    @Path("/find/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
  
    	try {
			return SimpaticoResourceUtils.findRequest(request, uriInfo, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
        
    /**
     * Insert a json document. The postData must be a valid json (we store the full json)
     * @param request
     * @param postData
     * @return
     */
    @POST
    @Path("/insert/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.insertRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
        
    /**
     * Update a json document. The postData must be a valid json and format: {"id": "<id to update>", "content": "<json>"}. 
     * If json has the same keys that the old document, the document updates.
     * If the document does not exists, it is created.
     * @param request
     * @param postData
     * @return
     */
    @PUT
    @Path("/update/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.updateRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
    
    
    /**
     * Remove a json document. The postData must be a valid json and format: {"id": "<id to eliminate>"}
     * @param request
     * @param postData
     * @return
     */
    @DELETE
    @Path("/remove/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@Context HttpServletRequest request, String postData) {
    	try {
			return SimpaticoResourceUtils.removeRequest(request, postData, ES_INDEX, ES_TYPE, ES_FIELD_SEARCH, FILE_LOG, THIS_RESOURCE);
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
    }
       
    
    /** Test Method **/
    @GET
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testGet(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: GET");
	}
    
    @POST
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testPost(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: POST");
	}
    
    @PUT
	@Path("/test/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response testPut(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
    	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: PUT");
	}
    
    @DELETE
   	@Path("/test/")
   	@Produces(MediaType.APPLICATION_JSON)
   	public Response testDelete(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
       	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: DELETE");
   	}
}
