package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.Injectable;

import javax.annotation.Nonnull;

@Injectable
class TagValidator {
    private static final int MAX_TAG_LENGTH = 30;

    void validate(@Nonnull String tag) throws InvalidTagException {
        if (tag.isEmpty())
            throw new InvalidTagException("invalidTag.empty");

        if (tag.length() > MAX_TAG_LENGTH)
            throw new InvalidTagException("invalidTag.invalidLength");

        if (tag.contains(" "))
            throw new InvalidTagException("invalidTag.whitespace");
    }
}
