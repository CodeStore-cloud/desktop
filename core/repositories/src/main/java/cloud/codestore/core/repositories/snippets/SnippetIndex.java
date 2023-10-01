package cloud.codestore.core.repositories.snippets;

import cloud.codestore.core.Snippet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * An in-memory index which holds all available code snippets.
 */
@Component
class SnippetIndex {
    static final class SnippetField {
        static final String ID = "id";
        static final String LANGUAGE = "language";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String CODE = "code";
    }

    private Directory index;
    private DirectoryReader reader;
    private IndexSearcher searcher;

    SnippetIndex() {
        closeOnShutdown();
        index = new ByteBuffersDirectory();
    }

    /**
     * Searches for code snippets based on the given query.
     * @param query the query for searching.
     * @return a potentially empty list containing the IDs of the found code snippets.
     */
    @Nonnull
    Stream<String> query(Query query) {
        try {
            if (DirectoryReader.indexExists(index)) {
                if (reader == null) {
                    reader = DirectoryReader.open(index);
                    searcher = new IndexSearcher(reader);
                } else {
                    DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
                    if (newReader != null) {
                        reader.close();
                        reader = newReader;
                        searcher = new IndexSearcher(reader);
                    }
                }

                TopDocs searchResults = searcher.search(query, Integer.MAX_VALUE);
                return Arrays.stream(searchResults.scoreDocs)
                             .map(scoreDoc -> getId(scoreDoc.doc));
            }

            return Stream.empty();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Adds all code snippets of the given stream to this index.
     */
    void add(Stream<Snippet> snippets) {
        try (IndexWriter writer = createWriter()) {
            snippets.forEach(snippet -> add(snippet, writer));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Adds the given code snippet to the index.
     */
    void add(@Nonnull Snippet snippet) {
        try (IndexWriter writer = createWriter()) {
            add(snippet, writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Updates the given code snippet in the index.
     */
    void update(@Nonnull Snippet snippet) {
        try (IndexWriter writer = createWriter()) {
            writer.updateDocument(new Term(SnippetField.ID, snippet.getId()), document(snippet));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Removes the code snippet with the given ID from the index.
     */
    void remove(String snippetId) {
        try (IndexWriter writer = createWriter()) {
            writer.deleteDocuments(new Term(SnippetField.ID, snippetId));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void add(Snippet snippet, IndexWriter writer) {
        try {
            writer.addDocument(document(snippet));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private IndexWriter createWriter() throws IOException {
        var keywordAnalyzer = new KeywordAnalyzer();
        var simpleAnalyzer = new SimpleAnalyzer();
        var whitespaceAnalyzer = new WhitespaceAnalyzer();

        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put(SnippetField.TITLE, whitespaceAnalyzer);
        analyzerMap.put(SnippetField.DESCRIPTION, simpleAnalyzer);
        analyzerMap.put(SnippetField.CODE, simpleAnalyzer);
        analyzerMap.put(SnippetField.ID, keywordAnalyzer);
        analyzerMap.put(SnippetField.LANGUAGE, keywordAnalyzer);

        var analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(), analyzerMap);

        return new IndexWriter(index, new IndexWriterConfig(analyzer));
    }

    private String getId(int docId) {
        try {
            Document document = reader.storedFields().document(docId, Collections.singleton(SnippetField.ID));
            return document.get(SnippetField.ID);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Document document(Snippet snippet) {
        Document document = new Document();

        document.add(new StringField(SnippetField.ID, snippet.getId(), Field.Store.YES));
        document.add(new TextField(SnippetField.TITLE, snippet.getTitle().toLowerCase(), Field.Store.NO));
        document.add(new TextField(SnippetField.DESCRIPTION, snippet.getDescription(), Field.Store.NO));
        document.add(new TextField(SnippetField.CODE, snippet.getCode(), Field.Store.NO));

        int languageId = snippet.getLanguage().getId();
        document.add(new StringField(SnippetField.LANGUAGE, String.valueOf(languageId), Field.Store.NO));

        return document;
    }

    private void closeOnShutdown() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    try {
                        if (index != null) {
                            index.close();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                })
        );
    }
}
