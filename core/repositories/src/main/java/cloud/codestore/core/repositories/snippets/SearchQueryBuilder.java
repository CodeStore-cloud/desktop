package cloud.codestore.core.repositories.snippets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cloud.codestore.core.repositories.snippets.SnippetIndex.SnippetField.*;

class SearchQueryBuilder {
    private static final float LANGUAGE_WEIGHT = 1.5f;
    private static final float TITLE_WEIGHT = 1.5f;
    private static final float TAG_WEIGHT = 1.5f;

    private String searchTerm;
    private List<String> whitespaceSeparatedTokens;
    private List<String> simpleSeparatedTokens;
    private BooleanQuery.Builder searchQuery;

    SearchQueryBuilder(String searchTerm) {
        this.searchTerm = searchTerm;
        if (!searchTerm.isBlank()) {
            whitespaceSeparatedTokens = tokenize(searchTerm, new WhitespaceAnalyzer());
            simpleSeparatedTokens = tokenize(searchTerm, new SimpleAnalyzer());
            searchQuery = new BooleanQuery.Builder();
        }
    }

    Query build() {
        if (searchTerm.isBlank()) {
            return new MatchAllDocsQuery();
        }

        for (String field : new String[]{LANGUAGE, TITLE, DESCRIPTION, CODE, TAG}) {
            switch (field) {
                case LANGUAGE -> tokenStream(field).forEach(token -> addTermQuery(field, token, LANGUAGE_WEIGHT));
                case TAG -> tokenStream(field).forEach(token -> addTermQuery(field, token, TAG_WEIGHT));
                case TITLE -> tokenStream(field).forEach(token -> addPrefixQuery(field, token, TITLE_WEIGHT));
                case DESCRIPTION, CODE -> tokenStream(field).forEach(token -> addPrefixQuery(field, token));
            }
        }

        return searchQuery.build();
    }

    private Stream<String> tokenStream(String field) {
        return switch (field) {
            case TITLE, LANGUAGE, TAG -> whitespaceSeparatedTokens.stream();
            case DESCRIPTION, CODE -> simpleSeparatedTokens.stream();
            default -> Stream.empty();
        };
    }

    private void addTermQuery(String field, String token) {
        searchQuery.add(new TermQuery(new Term(field, token)), BooleanClause.Occur.SHOULD);
    }

    private void addTermQuery(String field, String token, float boost) {
        searchQuery.add(new BoostQuery(new TermQuery(new Term(field, token)), boost), BooleanClause.Occur.SHOULD);
    }

    private void addPrefixQuery(String field, String token) {
        searchQuery.add(new PrefixQuery(new Term(field, token)), BooleanClause.Occur.SHOULD);
    }

    private void addPrefixQuery(String field, String token, float boost) {
        searchQuery.add(new BoostQuery(new PrefixQuery(new Term(field, token)), boost), BooleanClause.Occur.SHOULD);
    }

    private List<String> tokenize(String searchTerms, Analyzer analyzer) {
        List<String> tokens = new ArrayList<>();
        try (var tokenStream = analyzer.tokenStream("", searchTerms)) {
            CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken())
                tokens.add(attribute.toString());

            tokenStream.end();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return tokens;
    }
}
