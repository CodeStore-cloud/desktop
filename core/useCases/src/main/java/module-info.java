module cloud.codestore.core.useCases {
    exports cloud.codestore.core;
    exports cloud.codestore.core.usecases.createsnippet;
    exports cloud.codestore.core.usecases.createtag;
    exports cloud.codestore.core.usecases.deletesnippet;
    exports cloud.codestore.core.usecases.listlanguages;
    exports cloud.codestore.core.usecases.readlanguage;
    exports cloud.codestore.core.usecases.readsnippet;
    exports cloud.codestore.core.usecases.readtags;
    exports cloud.codestore.core.usecases.updatesnippet;
    exports cloud.codestore.core.usecases.listsnippets;
    exports cloud.codestore.core.validation;

    opens cloud.codestore.core.usecases.createtag;

    // @Nonnull / @Nullable
    requires jsr305;
}