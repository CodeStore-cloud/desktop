package cloud.codestore.core;

/**
 * Represents a programming language.
 */
public enum Language {
    TEXT(0, "Text"),
    C(1, "C"),
    OBJECTIVE_C(2, "Objective C"),
    CPP(3, "C++"),
    CSHARP(4, "C#"),
    COBOL(5, "COBOL"),
    FORTRAN(6, "Fortran"),
    HTML(7, "HTML"),
    CSS(8, "CSS"),
    JAVASCRIPT(9, "JavaScript"),
    JAVA(10, "Java"),
    SQL(11, "SQL"),
    SCALA(12, "Scala"),
    LUA(13, "Lua"),
    LATEX(14, "LaTeX"),
    CLISP(15, "Common Lisp"),
    GROOVY(16, "Groovy"),
    PERL(17, "Perl"),
    PYTHON(18, "Python"),
    PHP(19, "PHP"),
    RUBY(20, "Ruby"),
    YAML(21, "YAML"),
    XML(22, "XML"),
    MATLAB(23, "MATLAB"),
    MATHEMATICA(24, "Mathematica"),
    SWIFT(25, "Swift"),
    GO(26, "Go"),
    SHELL(27, "Unix Shell"),
    DOCKERFILE(28, "Dockerfile"),
    BATCH(29, "Batch"),
    TYPESCRIPT(30, "TypeScript"),
    KOTLIN(31, "Kotlin");

    private final int id;
    private final String name;

    Language(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id of this programming language.
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name of this programming language.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the name of this programming language.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the default programming language which is applied to any code snippet that doesn't
     * explicitly specify a programming language.
     */
    public static Language getDefault() {
        return TEXT;
    }
}