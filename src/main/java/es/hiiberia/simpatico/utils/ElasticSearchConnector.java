package es.hiiberia.simpatico.utils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class ElasticSearchConnector {

	// Singleton
	private static ElasticSearchConnector INSTANCE = null;
	
	// Attributes
	private TransportClient client = null;
		
		
	// Private constructor
	private ElasticSearchConnector() throws UnknownHostException {
		
		Logger.getRootLogger().info("Elastic search: Connecting to"
				+ "\n  Host: " + SimpaticoProperties.elasticSearchIp 
				+ "\n  Port: " + SimpaticoProperties.elasticSearchPort
				+ "\n  Cluster name: " + SimpaticoProperties.elasticSearchClusterName);
			
        Settings settings = Settings.builder().put("cluster.name", SimpaticoProperties.elasticSearchClusterName).build(); 
        client = new PreBuiltTransportClient(settings); 
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(SimpaticoProperties.elasticSearchIp), SimpaticoProperties.elasticSearchPort));
	}
	
	public static ElasticSearchConnector getInstance() throws UnknownHostException {
		if (INSTANCE == null) INSTANCE = new ElasticSearchConnector();
		return INSTANCE;
	}
	
	public TransportClient getClient() {
		return client;
	}
	
	public boolean existsIndex (String index) {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}
	
	public CreateIndexResponse createIndex (String index) {
		return client.admin().indices().create(new CreateIndexRequest(index)).actionGet();
	}
	
	public IndexResponse insertData (String index, String type, String data) throws IOException {
		return client.prepareIndex(index , type).setSource(data).get();
	}
}
