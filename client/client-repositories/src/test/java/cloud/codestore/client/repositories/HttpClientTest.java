package cloud.codestore.client.repositories;

import cloud.codestore.client.repositories.snippets.SnippetResource;
import cloud.codestore.client.repositories.tags.TagResource;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("The http client")
class HttpClientTest {
    private static final String ACCESS_TOKEN = "dummy-token";

    private MockWebServer mockBackEnd;
    private HttpClient client = new HttpClient(
            CompletableFuture.completedFuture("http://localhost:8080"),
            CompletableFuture.completedFuture(ACCESS_TOKEN),
            new CompletableFuture<>());

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start(8080);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("calls the callback function when fully initialized")
    void callsCallbackWhenFullyInitialized() {
        CompletableFuture<String> apiRootUrl = new CompletableFuture<>();
        CompletableFuture<String> apiAccessToken = new CompletableFuture<>();

        CompletableFuture<Void> clientInitialized = new CompletableFuture<>();
        new HttpClient(apiRootUrl, apiAccessToken, clientInitialized);
        assertThat(clientInitialized).isNotCompleted();

        apiRootUrl.complete("http://localhost:8080");
        apiAccessToken.complete(ACCESS_TOKEN);

        assertThat(clientInitialized).isCompleted();
    }

    @Test
    @DisplayName("sends the access token in the Authorization header")
    void sendAuthorizationHeader() throws InterruptedException {
        retrieveCollectionUrls();

        String token = mockBackEnd.takeRequest().getHeader(HttpHeaders.AUTHORIZATION);
        assertThat(token).isEqualTo("Bearer " + ACCESS_TOKEN);
    }

    @Test
    @DisplayName("retrieves the URLs of relationships dynamically")
    void retrieveCollectionUrls() {
        setResponse("""
                {
                    "data": {
                        "type": "core",
                        "id": "1",
                        "relationships": {
                            "snippets": {
                                "links": {
                                    "related": "http://localhost:8080/snippets"
                                }
                            },
                            "languages": {
                                "links": {
                                    "related": "http://localhost:8080/languages"
                                }
                            },
                            "tags": {
                                "links": {
                                    "related": "http://localhost:8080/tags"
                                }
                            }
                        }
                    }
                }""");

        assertThat(client.getSnippetCollectionUrl()).isEqualTo("http://localhost:8080/snippets");
        assertThat(client.getLanguageCollectionUrl()).isEqualTo("http://localhost:8080/languages");
        assertThat(client.getTagsCollectionUrl()).isEqualTo("http://localhost:8080/tags");
    }

    @Test
    @DisplayName("retrieves a single resource")
    void retrieveResource() {
        setResponse("""
                {
                    "data": {
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "My first snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/1"
                        }
                    }
                }""");

        var document = client.get("http://localhost:8080/snippets/1", SnippetResource.class);

        SnippetResource snippetResource = document.getData();
        assertThat(snippetResource).isNotNull();
        assertThat(snippetResource.getId()).isEqualTo("1");
        assertThat(snippetResource.getTitle()).isEqualTo("My first snippet");
        assertThat(snippetResource.getSelfLink()).isEqualTo("http://localhost:8080/snippets/1");
        assertThat(snippetResource.getMeta()).isNull();
    }

    @Test
    @DisplayName("retrieves a collection of resources")
    void retrieveResourceCollection() {
        setResponse("""
                {
                    "data": [{
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "My first snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/1"
                        }
                    }, {
                        "type": "snippet",
                        "id": "2",
                        "attributes": {
                            "title": "My second snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/2"
                        }
                    }, {
                        "type": "snippet",
                        "id": "3",
                        "attributes": {
                            "title": "My third snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/3"
                        }
                    }]
                }""");

        var document = client.getCollection("http://localhost:8080/snippets", SnippetResource.class);

        SnippetResource[] snippetResources = document.getData(SnippetResource.class);
        assertThat(snippetResources).isNotNull().hasSize(3);
    }

    @Test
    @DisplayName("deserializes the operation meta information")
    void retrieveOperations() {
        setResponse("""
                {
                    "data": {
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "My first snippet"
                        }
                    },
                    "meta": {
                        "operations": [{
                            "operation": "deleteSnippet",
                            "method": "DELETE",
                            "href": "http://localhost:8080/snippets/1"
                        }]
                    }
                }""");

        var document = client.get("http://localhost:8080/snippets/1", SnippetResource.class);

        SnippetResource snippetResource = document.getData();
        assertThat(snippetResource).isNotNull();

        ResourceMetaInfo meta = (ResourceMetaInfo) document.getMeta();
        assertThat(meta).isNotNull();
        assertThat(meta.getOperations()).containsExactly(new Operation("deleteSnippet"));
    }

    @Test
    @DisplayName("deletes a resource")
    void deleteResource() {
        setResponse("""
                {
                    "data": {
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "My first snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/1"
                        }
                    }
                }""");

        assertThatNoException().isThrownBy(() -> client.delete("http://localhost:8080/snippets/1"));
    }

    @Test
    @DisplayName("creates a resource")
    void createResource() throws InterruptedException {
        setResponse("""
                {
                    "data": {
                        "type": "tag",
                        "id": "1",
                        "attributes": {
                            "name": "test"
                        }
                    }
                }""");

        var responseDocument = client.post("http://localhost:8080/tags", new TagResource("test"));
        var createdTag = responseDocument.getData();

        assertThat(createdTag).isNotNull();
        assertThat(createdTag.getId()).isEqualTo("1");

        RecordedRequest request = mockBackEnd.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(JsonApiDocument.MEDIA_TYPE);
        assertThat(request.getBody().readUtf8()).isEqualToIgnoringWhitespace("""
                {
                    "data": {
                        "type": "tag",
                        "attributes": {
                            "name": "test"
                        }
                    }
                }""");
    }

    @Test
    @DisplayName("updates a resource")
    void updateResource() throws InterruptedException {
        setResponse("""
                {
                    "data": {
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "Updated Snippet"
                        },
                        "links": {
                            "self": "http://localhost:8080/snippets/1"
                        }
                    }
                }""");

        var resource = mock(SnippetResource.class);
        when(resource.getType()).thenReturn(SnippetResource.RESOURCE_TYPE);
        when(resource.getId()).thenReturn("1");
        when(resource.getTitle()).thenReturn("Updated Snippet");

        var responseDocument = client.patch("http://localhost:8080/snippets/1", resource);
        var updatedSnippet = responseDocument.getData();

        assertThat(updatedSnippet).isNotNull();
        assertThat(updatedSnippet.getId()).isEqualTo("1");

        RecordedRequest request = mockBackEnd.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(JsonApiDocument.MEDIA_TYPE);
        assertThat(request.getBody().readUtf8()).isEqualToIgnoringWhitespace("""
                {
                    "data": {
                        "type": "snippet",
                        "id": "1",
                        "attributes": {
                            "title": "Updated Snippet"
                        }
                    }
                }""");
    }

    private void setResponse(String responseBody) {
        mockBackEnd.enqueue(
                new MockResponse().setResponseCode(HttpStatus.OK.value())
                                  .setBody(responseBody)
                                  .addHeader(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE)
        );
    }
}