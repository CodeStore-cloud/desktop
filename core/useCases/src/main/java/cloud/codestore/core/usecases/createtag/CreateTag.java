package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.Injectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Injectable
public class CreateTag {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTag.class);

    private final CreateTagQuery query;
    private final TagValidator validator;

    public CreateTag(CreateTagQuery query, TagValidator validator) {
        this.query = query;
        this.validator = validator;
    }

    public void create(@Nonnull String tag) throws InvalidTagException {
        validator.validate(tag);
        query.create(tag);
        LOGGER.info("Created Tag {}", tag);
    }
}
