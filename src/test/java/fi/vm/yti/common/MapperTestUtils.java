package fi.vm.yti.common;

import fi.vm.yti.common.dto.GroupManagementUserDTO;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperTestUtils {
    public static Model getModelFromFile(String filepath) {
        var m = ModelFactory.createDefaultModel();
        var stream = MapperTestUtils.class.getResourceAsStream(filepath);
        assertNotNull(stream);
        RDFDataMgr.read(m, stream, RDFLanguages.TURTLE);
        return m;
    }

    public static void mockWebClient(WebClient client, List<GroupManagementUserDTO> result) {
        var req = mock(WebClient.RequestHeadersUriSpec.class);
        var res = mock(WebClient.ResponseSpec.class);
        var mono = mock(Mono.class);

        when(res.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(mono);
        when(mono.block()).thenReturn(result);

        when(client.get()).thenReturn(req);
        when(req.uri(any(Function.class))).thenReturn(req);
        when(req.accept(any(MediaType.class))).thenReturn(req);
        when(req.retrieve()).thenReturn(res);
    }
}
