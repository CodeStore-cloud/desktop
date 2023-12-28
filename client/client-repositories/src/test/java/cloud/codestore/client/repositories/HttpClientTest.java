package cloud.codestore.client.repositories;

import cloud.codestore.client.repositories.snippets.SnippetResource;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The http client")
class HttpClientTest {
    private static final String ACCESS_TOKEN = "dummy-token";

    private static MockWebServer mockBackEnd;
    private HttpClient client = new HttpClient("http://localhost:8080", ACCESS_TOKEN);

    @BeforeAll
    static void beforeAll() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start(8080);
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockBackEnd.shutdown();
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
                            }
                        }
                    }
                }""");

        assertThat(client.getSnippetCollectionUrl()).isEqualTo("http://localhost:8080/snippets");
        assertThat(client.getLanguageCollectionUrl()).isEqualTo("http://localhost:8080/languages");
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
    @DisplayName("sends the access token in the Authorization header")
    void sendAuthorizationHeader() throws InterruptedException {
        retrieveCollectionUrls();

        String token = mockBackEnd.takeRequest().getHeader(HttpHeaders.AUTHORIZATION);
        assertThat(token).isEqualTo("Bearer " + ACCESS_TOKEN);
    }

    private void setResponse(String responseBody) {
        mockBackEnd.enqueue(
                new MockResponse().setResponseCode(HttpStatus.OK.value())
                                  .setBody(responseBody)
                                  .addHeader(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE)
        );
    }
}