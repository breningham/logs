package es.hiiberia.simpatico.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import es.hiiberia.simpatico.utils.ElasticSearchConnector;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import es.hiiberia.simpatico.utils.SimpaticoProperties;
import es.hiiberia.simpatico.utils.Utils;


@Path("/twitter")
public class SimpaticoResourceTwitter {
	
	private static String ES_INDEX = "twitter";
	
	private static String ES_TYPE =  "TWT";
	private static String ES_FIELD_SEARCH = SimpaticoProperties.elasticSearchFieldSearch;
	private static String FILE_LOG = SimpaticoProperties.simpaticoLog_Logs;
	private static String THIS_RESOURCE = "TWT";
	
	//atributos que meto porque los usa
	private static int numLinesPrintStackInternalError = 1;
	
	//PREGUNTA SI PUEDES IMPRESION DE LAS COSAS MEDIANTE SYSTEM.OUT.PRINTLN()
	
	

    @GET
    @Path("/prueba_get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find_formend(@Context HttpServletRequest request, @Context UriInfo uriInfo) {

    	try {
    		// Copy map (it is unmodificable)
	    	Map<String, List<String>> queryParamsUnmodificable = uriInfo.getQueryParameters();
	    	Map<String, List<String>> queryParams = new MultivaluedHashMap<>();
	    	
	    	if (queryParamsUnmodificable != null){
	    		queryParams.putAll(queryParamsUnmodificable);
	    	}
	    	
	    	
	    	
	    	Logger.getRootLogger().info("Los parametros son \n"+ queryParamsUnmodificable);
	    	
	    	ElasticSearchConnector inst= ElasticSearchConnector.getInstance();
	    	List<String> busqueda= new ArrayList<String>();
	    	
	    	inst.createIndexWithDateField("borra2", "aux", "22-05-2010");
	    	
	    	
	    	return SimpaticoResourceUtils.findRequest(request, queryParams, "twitter", "tweet", "user", FILE_LOG, THIS_RESOURCE);
    		
	    	
	    	 
	    	/*for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
	             //String key = entry.getKey();
	             List<String> values = entry.getValue();
	             for(String aux: values){
	            	 busqueda.add(aux);
	             }
	    	 }
	    	
	    	Logger.getRootLogger().info("El array vale:"+ busqueda);
	    	*/
	    	
	    	
	    	/*SearchResponse responseES= inst.search("twitter", "msg", "user", busqueda, null, null, 4);
	    	Logger.getRootLogger().info(responseES);
	    	*/
	    	//return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.searchResponse2JSONResponse(responseES));
	    	
	    	//return SimpaticoResourceUtils.findRequest(request, queryParams, "twitter", "msg", "user", null, null);
	    	
    	} catch (Exception e) {
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
		
    }	
	

	
	
	
    @POST
    @Path("/prueba_post")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prueba(@Context HttpServletRequest request, String postData){
		
    	try {
    		
	
	    	
			ElasticSearchConnector connector= ElasticSearchConnector.getInstance();
			Logger.getRootLogger().info("Empiezo");
			
			if(!connector.existsIndex("borra")){
				Logger.getRootLogger().info("Creando indice\n");
				connector.createIndex("borra");
			}
			
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			
			Logger.getRootLogger().info("el json es el siguiente\n"+jsonObject.toString());
			  	
			return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), "twitter", "new", null, FILE_LOG, THIS_RESOURCE);
			//return null;
					
			
			//List <String> index= new ArrayList<String>();
			//index.add("fa");
			//index.add("dea");
			/*Logger.getRootLogger().info("Estoy aquiiiiiiiiiii3");
			Logger.getRootLogger().info(index);
			if(inst.existsIndex("twitter")){
				Logger.getRootLogger().info("EXISTEEEEE");
			}*/
			
			//inst.insertDocument("twitter", "aux", "2010-11-15T14:13:09");
			//DeleteResponse del= inst.deleteDocument ("twitter", "new", "AWBVNqwQFqYLB1TQZqTJ");
			//Logger.getRootLogger().info(del);
			/*SearchResponse prueba= inst.search("shared", 1);
			Logger.getRootLogger().info("primero");
			Logger.getRootLogger().info(prueba);
			Logger.getRootLogger().info("segundo");	
			SearchResponse aaa= inst.search("shared", "IFE", "averageTime", SortOrder.ASC, 4);
			Logger.getRootLogger().info(aaa);
			Logger.getRootLogger().info("tercero:");
			Logger.getRootLogger().info(postData);
			
			QueryBuilder qb= QueryBuilders.matchQuery("user", "James");*/
		
			
		} catch (Exception e) {
			e.printStackTrace();
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}
		
		
		
		
		
		//inst.sear
		//SearchResponse busqueda= inst.searchES("twitter", "msg", qb, null, null, 2);
		
		//Mirar esto para recibir body
		//OJOOOOOOOO con el puerto de ARC y el de la ruta...utilizar 9300 para conectar a traves de aqui!!!
		//uriInfo.
		
		
    }

    
    
    //PUEDES BORRAR ESTA
    @POST
    @Path("/prueba_post_no_indice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prueba2(@Context HttpServletRequest request, String postData){
		
    	try {
    		
			ElasticSearchConnector connector= ElasticSearchConnector.getInstance();
			Logger.getRootLogger().info("Empiezo");
			
			
			
			
			// Check parameters and generate event attribute
			JSONObject jsonObject = Utils.createJSONObjectIfValid(postData);
			
			  	
			return SimpaticoResourceUtils.insertRequest(request, jsonObject.toString(), "twitter", "new", null, FILE_LOG, THIS_RESOURCE);
			
		
			
		} catch (Exception e) {
			e.printStackTrace();
			SimpaticoResourceUtils.logException(e, FILE_LOG, THIS_RESOURCE);
    		return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverInternalServerErrorCode, SimpaticoResourceUtils.internalErrorResponse + ": " + SimpaticoResourceUtils.getInternalErrorMessageWithStackTrace(e, numLinesPrintStackInternalError));
    	}	
		
    }
    
    
    
    
    @DELETE
   	@Path("/del/")
   	@Produces(MediaType.APPLICATION_JSON)
   	public Response testDelete(@Context HttpServletRequest request) {
    	Logger.getLogger(FILE_LOG).warn("[TEST] IP Remote: " + request.getRemoteAddr() + ". IP Header Real: " + SimpaticoResourceUtils.getRealIPHeader(request));
       	return SimpaticoResourceUtils.createMessageResponse(SimpaticoResourceUtils.serverOkCode, "Welcome to SIMPATICO " + THIS_RESOURCE + " API! Method: DELETE");
   	}
    
    
}
    

