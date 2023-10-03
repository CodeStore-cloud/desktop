package cloud.codestore.core;

/**
 * Exception in case a requested tag does not exist.
 */
public class TagNotExistsException extends CoreException {
    private final String tag;

    public TagNotExistsException(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
