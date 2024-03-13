package fi.vm.yti.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class GroupManagementConfig {

    @Value("${groupmanagement.url}")
    private String groupManagementURL;

    private final HttpHeaders defaultHttpHeaders = new HttpHeaders();

    GroupManagementConfig() {
        defaultHttpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Bean("groupManagementClient")
    WebClient defaultWebClient() {
        return WebClient.builder()
                .defaultHeaders(headers -> headers.addAll(defaultHttpHeaders))
                .baseUrl(groupManagementURL)
                .clientConnector(new ReactorClientHttpConnector(
                                HttpClient.create(getConnectionProvider("groupManagement"))
                        )
                ).build();
    }

    private static ConnectionProvider getConnectionProvider(String name) {
        return ConnectionProvider.builder(name)
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120)).build();
    }
}
