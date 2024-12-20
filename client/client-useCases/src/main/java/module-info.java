module cloud.codestore.client.useCases {
    exports cloud.codestore.client;
    exports cloud.codestore.client.usecases.createsnippet;
    exports cloud.codestore.client.usecases.deletesnippet;
    exports cloud.codestore.client.usecases.listsnippets;
    exports cloud.codestore.client.usecases.readlanguages;
    exports cloud.codestore.client.usecases.readsnippet;
    exports cloud.codestore.client.usecases.updatesnippet;

    // @Nonnull / @Nullable
    requires jsr305;
}