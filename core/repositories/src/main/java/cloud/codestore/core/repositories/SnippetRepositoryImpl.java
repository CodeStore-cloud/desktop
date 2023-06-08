package cloud.codestore.core.repositories;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.SnippetRepository;

import java.time.OffsetDateTime;
import java.util.*;

public class SnippetRepositoryImpl implements SnippetRepository {
    private final Map<String, Snippet> snippets = new HashMap<>();

    public SnippetRepositoryImpl() {
        var snippet1 = new SnippetBuilder().id(UUID.randomUUID().toString())
                                           .language(Language.JAVA)
                                           .title("A simple Hello World example")
                                           .code("System.out.println(\"Hello, World!\")")
                                           .created(OffsetDateTime.now())
                                           .build();

        var snippet2 = new SnippetBuilder().id(UUID.randomUUID().toString())
                                           .language(Language.PYTHON)
                                           .title("A snippet with a description")
                                           .description("A short description describing the snippet.")
                                           .code("print('nothing to see here')")
                                           .created(OffsetDateTime.now())
                                           .build();

        var snippet3 = new SnippetBuilder().id(UUID.randomUUID().toString())
                                           .language(Language.JAVA)
                                           .title("UUID")
                                           .code("UUID.randomUUID().toString()")
                                           .created(OffsetDateTime.now())
                                           .build();

        snippets.put(snippet1.getId(), snippet1);
        snippets.put(snippet2.getId(), snippet2);
        snippets.put(snippet3.getId(), snippet3);
    }

    @Override
    public boolean contains(String snippetId) {
        return snippets.containsKey(snippetId);
    }

    @Override
    public Snippet get(String snippetId) {
        return snippets.get(snippetId);
    }

    @Override
    public List<Snippet> get() {
        return new ArrayList<>(snippets.values());
    }

    @Override
    public void put(Snippet snippet) {
        snippets.put(snippet.getId(), snippet);
    }

    @Override
    public void delete(String snippetId) {
        snippets.remove(snippetId);
    }
}
