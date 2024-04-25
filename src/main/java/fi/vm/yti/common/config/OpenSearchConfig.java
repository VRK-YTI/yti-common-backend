package fi.vm.yti.common.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    private final String openSearchUrl;

    @Autowired
    public OpenSearchConfig(@Value("${openSearch.url}") String openSearchUrl) {
        this.openSearchUrl = openSearchUrl;
    }

    @Bean
    protected OpenSearchClient openSearchClient() {
        var restClient = RestClient.builder(HttpHost.create(openSearchUrl))
            .setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000)
        ).build();

        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }
}
