package cloud.codestore.core.usecases.synchronizesnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.synchronization.ConflictResolver;

import java.time.OffsetDateTime;

class SnippetConflictResolver extends ConflictResolver<Snippet> {
    @Override
    public void resolve(String snippetId, String etagA, String etagB) throws Exception {
        OffsetDateTime dateA = OffsetDateTime.parse(etagA);
        OffsetDateTime dateB = OffsetDateTime.parse(etagB);

        if (dateA.isAfter(dateB)) {
            applyItemA();
        } else if (dateB.isAfter(dateA)) {
            applyItemB();
        }
    }
}
