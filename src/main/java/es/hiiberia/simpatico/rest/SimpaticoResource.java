package es.hiiberia.simpatico.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

@Path("/")
public class SimpaticoResource {

	public static String internalError = "Internal error";
	public static String badPOSTRequest = "Bad request. Post data must be json";
	public static String dataInsertedES = "Data inserted";
	
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(@Context HttpServletRequest request, @Context UriInfo uriInfo) {
    	
    	Response response;
    	
    	try {
	    	Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
	    	Logger.getLogger("clients").info("GET Method. IP Remote: " + request.getRemoteAddr() + ". Query: " + queryParams.toString());
	    	 	
	        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
	            String key = entry.getKey();
	            List<String> values = entry.getValue();	           
	        }
	        
	        // ElasticSearch
	        Settings settings = Settings.builder().put("cluster.name", "my-application").build(); 
	        TransportClient client = new PreBuiltTransportClient(settings); 
	        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

	        
	        SearchResponse responseQuery = client.prepareSearch("blog")
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.termQuery("user", "dilbert"))                 // Query
	                //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
	                //.setFrom(0).setSize(60).setExplain(true)
	                .get();
	        
	        
	        return Response.status(200).entity(responseQuery.toString()).build();
    	} catch (Exception e) {
    		return Response.status(400).entity("Exception").build();
    	}
    }
    
    @POST
    @Path("/put")
    @Produces(MediaType.APPLICATION_JSON)
    public Response putData(@Context HttpServletRequest request, String postData) {
    	try {
    		Response response;
    		
    		// Check valid data (must be a JSON)
    		if (!Utils.isJSONValid(postData)) {
    			response = Response.status(400).entity(badPOSTRequest).build();
    			Logger.getLogger("clients").warn("PUT Method BAD REQUEST. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
    		} else {
    			Logger.getLogger("clients").info("PUT Method. IP Remote: " + request.getRemoteAddr() + ". POST data: " + postData);
    			
    			ElasticSearchConnector connector = ElasticSearchConnector.getInstance();
    			
    			// Check if exist index
    			if (!connector.existsIndex(SimpaticoProperties.elasticSearchIndex)) {
    				connector.createIndex(SimpaticoProperties.elasticSearchIndex);
    			}
    			
    			// Insert data
    			connector.insertData(SimpaticoProperties.elasticSearchIndex, SimpaticoProperties.elasticSearchType, postData);
    			response = Response.status(200).entity(dataInsertedES).build();
    		}
    		
    		return response;
    	} catch (Exception e) {
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			Logger.getRootLogger().error(err.toString());
    		return Response.status(400).entity(internalError).build();
    	}
    }
    
	/** Test Method **/
    @GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject testIt() {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		return factory.createObjectBuilder()
			.add("message", "Welcome to SIMPATICO API!")
			.build();
	}
}
