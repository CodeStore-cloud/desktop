package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;

@UseCase
public class CreateTag {
    private final CreateTagQuery query;
    private final TagValidator validator;

    public CreateTag(CreateTagQuery query, TagValidator validator) {
        this.query = query;
        this.validator = validator;
    }

    public void create(@Nonnull String tag) throws InvalidTagException {
        validator.validate(tag);
        query.create(tag);
    }
}
