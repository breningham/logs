package es.hiiberia.simpatico.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class ElasticSearchConnector {

	// Singleton
	private static ElasticSearchConnector INSTANCE = null;
	
	// Attributes
	private TransportClient client = null;
	
	// Public
	public static String AllFieldsES = "_all";
		
		
	// Private constructor
	private ElasticSearchConnector() throws Exception {
		
		Logger.getRootLogger().info("Elastic search: Connecting to"
				+ "\n  Host: " + SimpaticoProperties.elasticSearchIp 
				+ "\n  Port: " + SimpaticoProperties.elasticSearchPort
				+ "\n  Cluster name: " + SimpaticoProperties.elasticSearchClusterName);
			
        Settings settings = Settings.builder().put("cluster.name", SimpaticoProperties.elasticSearchClusterName).build(); 
        client = new PreBuiltTransportClient(settings); 
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(SimpaticoProperties.elasticSearchIp), SimpaticoProperties.elasticSearchPort));
        
        if (this.isConnected(client)) {
        	Logger.getRootLogger().info("Elastic search: Connected!");
        } else {
        	Logger.getRootLogger().error("Elastic search: No nodes available. Verify ES is running!");
        	throw new Exception("Elastic search: No nodes available. Verify ES is running!");
        }
	}
	
	private boolean isConnected(TransportClient client) throws Exception {
        List<DiscoveryNode> nodes = client.connectedNodes();
        return !nodes.isEmpty();
    }
	
	public static ElasticSearchConnector getInstance() throws Exception {
		if (INSTANCE == null) INSTANCE = new ElasticSearchConnector();
		return INSTANCE;
	}
	
	public TransportClient getClient() {
		return client;
	}
	
	public boolean existsIndex (String index) {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}
	
	public void createIndex (String index) {
		Logger.getRootLogger().info("Elastic search: Creating index: " + index);		
		try {
			client.admin().indices().create(new CreateIndexRequest(index)).get();
		} catch (Exception e) {
			Logger.getRootLogger().error("Elastic search: Creating index.");
		}
	}
	
	public void createIndexWithDateField (String index, String type, String timestampFieldName) {
		Logger.getRootLogger().info("Elastic search: Creating index: " + index);
		try {
			client.admin().indices().create(new CreateIndexRequest(index)).get();
			client.admin().indices().prepareCreate(index).addMapping(type, createTimestampField(type, timestampFieldName)).get();
		} catch (Exception e) {
			Logger.getRootLogger().error("Elastic search: Creating index.");
		}
	}
	
	public static XContentBuilder createTimestampField(String type, String timestampFieldName) {
		try {
			XContentBuilder mapping = jsonBuilder()
					.startObject()
						.startObject("mappings")
							.startObject(type)
								.startObject("properties")
									.startObject(timestampFieldName)
										.field("type", "date")
										.field("format", "basic_date_time_no_millis")
									.endObject()
								.endObject()
						.endObject()
					.endObject();
			
			Logger.getRootLogger().info("Mappings: " + mapping.string());
			return mapping;
		} catch (Exception e) {
			Logger.getRootLogger().error("Elastic search: Creating timestamp field.");
			return null;
		}
	}
	
	public IndexResponse insertDocument (String index, String type, String data) throws IOException {
		Logger.getRootLogger().info("Elastic search: Inserting document (index: " + index + ", type: " + type + ", data: " + data + ")");
		return client.prepareIndex(index , type).setSource(data).get();
	}
	
	public IndexResponse insertDocument (String index, String type, String id, String data) throws IOException {
		Logger.getRootLogger().info("Elastic search: Inserting document (index: " + index + ", type: " + type + ", data: " + data + ")");
		return client.prepareIndex(index , type).setId(id).setSource(data).get();
	}

	public IndexResponse insertDocument (String index, String type, XContentBuilder data) throws IOException {
		Logger.getRootLogger().info("Elastic search: Inserting document (index: " + index + ", type: " + type + ", data(builder): " + data.string() + ")");
		return client.prepareIndex(index , type).setSource(data).get();
	}
	
	public DeleteResponse deleteDocument (String index, String type, String id) throws IOException {
		Logger.getRootLogger().info("Elastic search: Deleting document (index: " + index + ", type: " + type + ", id: " + id + ")");
		return client.prepareDelete(index , type, id).get();
	}
	
	public UpdateResponse update (String index, String type, String id, XContentBuilder data) throws IOException {
		Logger.getRootLogger().info("Elastic search: Updating document (index: " + index + ", type: " + type + ", id: " + id + ")");
		return client.prepareUpdate(index , type, id).setDoc(data).get();
	}
	
	public SearchResponse searchData (String index, String field, List<String> words, String fieldSort, SortOrder ord, int limit) throws IOException {
		
		try {
			Logger.getRootLogger().info("Elastic search: Searching (index: " + index + ", field: " + field + ", words: " + words + ")");		

			BoolQueryBuilder boolQuery = new BoolQueryBuilder();
			for (String word : words) {
				boolQuery.should(QueryBuilders.matchQuery(field, word));
			}
			
			SearchRequestBuilder searchRequest = client.prepareSearch(index)
												        .setQuery(boolQuery)
												        .setSize(limit);
			if (fieldSort != null && !fieldSort.isEmpty()) {
				searchRequest.addSort(fieldSort, ord);
			}
			SearchResponse response = searchRequest.get();
		
			return response;
		} catch (IndexNotFoundException e) {
			Logger.getRootLogger().warn("Elastic search: Searching and there are not index created previusly (index: " + index + ", field: " + field + ", words: " + words + ")");		
			return new SearchResponse(InternalSearchResponse.empty(), "", 0, 0, 0, new ShardSearchFailure[0]); // Empty response
		} 
	}
	
	public SearchResponse searchAllData (String index, String field, String fieldSort, SortOrder ord, int limit) throws IOException {
		
		try {
			Logger.getRootLogger().info("Elastic search: Searching all data (index: " + index + ", field: " + field + ")");		

			// Specific words   
			QueryBuilder qb = QueryBuilders.matchAllQuery();
	
			SearchRequestBuilder searchRequest = client.prepareSearch(index)
												        .setQuery(qb)
												        .setSize(limit);
			
			if (fieldSort != null && !fieldSort.isEmpty()) {
				searchRequest.addSort(fieldSort, ord);
			}
			
			SearchResponse response = searchRequest.get();
		
			return response;
		} catch (IndexNotFoundException e) {
			Logger.getRootLogger().warn("Elastic search: Searching and there are not index created previusly (index: " + index + ", field: " + field + ")");		
			return createEmptySearchResponse();
		} 
	}
	
	public static SearchResponse createEmptySearchResponse() {
		return new SearchResponse(InternalSearchResponse.empty(), "", 0, 0, 0, new ShardSearchFailure[0]); // Empty response
	}
}
