package cloud.codestore.client.repositories;

import cloud.codestore.client.repositories.root.RootResource;
import cloud.codestore.client.repositories.snippets.SnippetResource;
import cloud.codestore.client.repositories.tags.TagResource;
import cloud.codestore.jsonapi.JsonApiObjectMapper;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class HttpClient {
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    private final WebClient client;
    private final String rootUrl;
    private String snippetCollectionUrl;

    public HttpClient(@Nonnull String rootUrl, @Nonnull String accessToken) {
        this.rootUrl = rootUrl;

        ObjectMapper objectMapper = new JsonApiObjectMapper()
                .registerResourceType(RootResource.RESOURCE_TYPE, RootResource.class)
                .registerResourceType(SnippetResource.RESOURCE_TYPE, SnippetResource.class)
                .registerResourceType(TagResource.RESOURCE_TYPE, TagResource.class);

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        this.client = WebClient.builder()
                               .uriBuilderFactory(uriBuilderFactory)
                               .defaultHeaders(httpHeaders -> {
                                   httpHeaders.set(HttpHeaders.ACCEPT, JsonApiDocument.MEDIA_TYPE);
                                   httpHeaders.setBearerAuth(accessToken);
                               })
                               .codecs(configurer -> {
                                   MimeType mimeType = MimeType.valueOf(JsonApiDocument.MEDIA_TYPE);
                                   configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, mimeType));
                                   configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, mimeType));
                                   configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE);
                               })
                               .build();
    }

    @Nonnull
    public String getSnippetCollectionUrl() {
        if (snippetCollectionUrl == null) {
            var document = get(rootUrl, RootResource.class);
            snippetCollectionUrl = document.getData().getSnippetsUrl();
        }

        return snippetCollectionUrl;
    }

    public <T extends ResourceObject> SingleResourceDocument<T> get(String url, Class<T> expectedType) {
        return client.get()
                     .uri(url)
                     .retrieve()
                     .onStatus(
                             HttpStatusCode::isError,
                             response -> Mono.error(
                                     new RuntimeException(String.valueOf(response.statusCode().value()))
                             )
                     )
                     .bodyToMono(new ParameterizedTypeReference<SingleResourceDocument<T>>() {})
                     .block();
    }

    public <T extends ResourceObject> ResourceCollectionDocument<T> getCollection(String url, Class<T> nestedType) {
        return client.get()
                     .uri(url)
                     .retrieve()
                     .onStatus(
                             HttpStatusCode::isError,
                             response -> Mono.error(
                                     new RuntimeException(String.valueOf(response.statusCode().value()))
                             )
                     )
                     .bodyToMono(new ParameterizedTypeReference<ResourceCollectionDocument<T>>() {})
                     .block();
    }
}
