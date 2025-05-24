package cloud.codestore.client.repositories;

import cloud.codestore.client.repositories.language.LanguageResource;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class HttpClient {
    private static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf(JsonApiDocument.MEDIA_TYPE);
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    private WebClient client;
    private String rootUrl;
    private String snippetCollectionUrl;
    private String languageCollectionUrl;
    private String tagsCollectionUrl;

    public HttpClient(@Nonnull CompletableFuture<String> rootUrl, @Nonnull CompletableFuture<String> accessToken) {
        CompletableFuture.allOf(rootUrl, accessToken).thenAccept(result -> {
            this.rootUrl = rootUrl.join();

            ObjectMapper objectMapper = new JsonApiObjectMapper(new ResourceMetaInfo.ResourceMetaInfoDeserializer())
                    .registerResourceType(RootResource.RESOURCE_TYPE, RootResource.class)
                    .registerResourceType(SnippetResource.RESOURCE_TYPE, SnippetResource.class)
                    .registerResourceType(LanguageResource.RESOURCE_TYPE, LanguageResource.class)
                    .registerResourceType(TagResource.RESOURCE_TYPE, TagResource.class);

            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
            uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

            this.client = WebClient.builder()
                                   .uriBuilderFactory(uriBuilderFactory)
                                   .defaultHeaders(httpHeaders -> {
                                       httpHeaders.set(HttpHeaders.ACCEPT, JsonApiDocument.MEDIA_TYPE);
                                       httpHeaders.setBearerAuth(accessToken.join());
                                   })
                                   .codecs(configurer -> {
                                       MimeType mimeType = MimeType.valueOf(JsonApiDocument.MEDIA_TYPE);
                                       configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, mimeType));
                                       configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, mimeType));
                                       configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE);
                                   })
                                   .build();
        });
    }

    @Nonnull
    public String getSnippetCollectionUrl() {
        getCollectionUrls();
        return snippetCollectionUrl;
    }

    @Nonnull
    public String getLanguageCollectionUrl() {
        getCollectionUrls();
        return languageCollectionUrl;
    }

    @Nonnull
    public String getTagsCollectionUrl() {
        getCollectionUrls();
        return tagsCollectionUrl;
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

    public <T extends ResourceObject> SingleResourceDocument<T> post(String url, T resource) {
        return request(HttpMethod.POST, url, resource);
    }

    public <T extends ResourceObject> SingleResourceDocument<T> patch(String url, T resource) {
        return request(HttpMethod.PATCH, url, resource);
    }

    private <T extends ResourceObject> SingleResourceDocument<T> request(HttpMethod method, String url, T resource) {
        return client.method(method)
                     .uri(url)
                     .contentType(JSONAPI_MEDIATYPE)
                     .bodyValue(JsonApiDocument.of(resource))
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

    public void delete(String url) {
        client.delete()
              .uri(url)
              .retrieve()
              .onStatus(
                      HttpStatusCode::isError,
                      response -> Mono.error(
                              new RuntimeException(String.valueOf(response.statusCode().value()))
                      )
              );
    }

    private void getCollectionUrls() {
        if (snippetCollectionUrl == null || languageCollectionUrl == null || tagsCollectionUrl == null) {
            var document = get(rootUrl, RootResource.class);
            snippetCollectionUrl = document.getData().getSnippetsUrl();
            languageCollectionUrl = document.getData().getLanguagesUrl();
            tagsCollectionUrl = document.getData().getTagsUrl();
        }
    }
}
