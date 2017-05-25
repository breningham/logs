package es.hiiberia.simpatico.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
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
		
	private static int DEFAULT_SIZE_LIMIT_ES = 10000;
		
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
	
	
	/* Indices */
	
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
	
	public List<String> getAllIndex () {
		
		String[] index = client.admin()
							.indices()
							.getIndex(new GetIndexRequest())
							.actionGet()
							.getIndices();
		
		return Arrays.asList(index);
	}
	
	public boolean existsIndex (String index) {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}
	
	/** Insert **/
	
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
	
	/** Delete **/
	public DeleteResponse deleteDocument (String index, String type, String id) throws IOException {
		Logger.getRootLogger().info("Elastic search: Deleting document (index: " + index + ", type: " + type + ", id: " + id + ")");
		return client.prepareDelete(index , type, id).get();
	}
	
	/** Update **/
	public UpdateResponse update (String index, String type, String id, XContentBuilder data) throws IOException {
		Logger.getRootLogger().info("Elastic search: Updating document (index: " + index + ", type: " + type + ", id: " + id + ")");
		return client.prepareUpdate(index , type, id).setDoc(data).get();
	}
	
	/** Search **/
	
	public SearchResponse search(String index) throws IOException {
		return searchES (index, null, null, null, null, 0);
	}

	public SearchResponse search(String index, String fieldSort, SortOrder ord) throws IOException {
		return searchES (index, null, null, fieldSort, ord, 0);
	}
	
	public SearchResponse search(String index, String type, String fieldSort, SortOrder ord, int limit) throws IOException {
		return searchES (index, type, null, fieldSort, ord, limit);
	}

	public SearchResponse search(String index, int limit) throws IOException {
		return searchES (index, null, null, null, null, limit);		
	}
	
	public SearchResponse search(String index, String type, String field, List<String> words, String fieldSort, SortOrder ord, int limit) throws IOException {
		
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		for (String word : words) {
			boolQuery.should(QueryBuilders.matchQuery(field, word));
		}
		
		return searchES (index, type, boolQuery, fieldSort, ord, limit);
	}
	
	public SearchResponse searchByField(String index, String type, String field, List<String> words, String fieldSort, SortOrder ord, int limit, String userID) throws IOException {
		
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		for (String word : words) {
			boolQuery.should(QueryBuilders.matchQuery(field, word));
		}
		
		boolQuery.must(QueryBuilders.termQuery("userID", userID));
		
		return searchES (index, type, boolQuery, fieldSort, ord, limit);
	}
	
	private SearchResponse searchES (String index, String type, QueryBuilder qb, String fieldSort, SortOrder ord, int limit) throws IOException {
		
		try {
			Logger.getRootLogger().info("Elastic search: Searching all data (index: " + index + ")");		

			SearchRequestBuilder searchRequest = client.prepareSearch(index);
			
			// Add query
			if (qb != null) {
				searchRequest.setQuery(qb);
			} else {
				searchRequest.setQuery(QueryBuilders.matchAllQuery());
			}
			
			// Add type search
			if (type != null) {
				searchRequest.setTypes(type);
			}

			// Add field sort
			if (fieldSort != null && !fieldSort.isEmpty()) {
				searchRequest.addSort(fieldSort, ord);
			}
			
			// Add limit
			if (limit > 0) {
				searchRequest.setSize(limit);
			} else {
				searchRequest.setSize(DEFAULT_SIZE_LIMIT_ES);
			}
			
			SearchResponse response = searchRequest.get();
		
			return response;
		} catch (IndexNotFoundException e) {
			Logger.getRootLogger().warn("Elastic search: Searching and there are not index created previusly (index: " + index + ")");		
			return createEmptySearchResponse();
		} 
	}
	
	private static SearchResponse createEmptySearchResponse() {
		return new SearchResponse(InternalSearchResponse.empty(), "", 0, 0, 0, new ShardSearchFailure[0]); // Empty response
	}
}
