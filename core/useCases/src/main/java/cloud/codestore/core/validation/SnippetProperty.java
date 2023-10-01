package cloud.codestore.core.validation;

public enum SnippetProperty {
    TITLE("title"),
    DESCRIPTION("description"),
    CODE("code"),
    TAGS("tags");

    private final String name;

    SnippetProperty(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
